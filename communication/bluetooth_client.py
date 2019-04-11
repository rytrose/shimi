from bluetooth import *
import threading
from subprocess import Popen
import atexit
import json
import time


class BluetoothClient:
    def __init__(self):
        """Establishes a Bluetooth server and RFCOMM socket port on a hardcoded UUID."""
        self.uuid = "e872f7b9-315c-4d25-b771-d6c59297cd37"
        self.pairing_process = None
        self.socket = None
        self.port = None
        self.client_socket = None
        self.client_info = None
        self.listening_thread = None
        self.mappings = {}

        self.setup_socket()
        atexit.register(self.stop_pairing)

    def connect(self):
        """Starts to accept pairing requests and opens the RFCOMM server."""
        self.allow_pairing()
        self.advertise()
        self.wait_for_connection()

    def map(self, address, function):
        """Maps an identifying address to a function for an incoming message over Bluetooth.

        Args:
            address: A string identifier on which to route a message.
            function: A function to be called when a message with address provided is received.
        """
        self.mappings[address] = function

    def stop_pairing(self):
        """Stops allowing pairing requests."""
        print("Stopping bluetooth agent, will no longer accept pair requests.")
        self.pairing_process.terminate()

    def allow_pairing(self):
        """Starts allowing pairing requests."""
        self.pairing_process = Popen("bt-agent -c NoInputNoOutput".split(' '))
        print("Accepting pair requests.")

    def setup_socket(self):
        """Creates an RFCOMM socket and opens it."""
        self.socket = BluetoothSocket(RFCOMM)
        self.socket.bind(("", PORT_ANY))
        self.socket.listen(1)
        self.port = self.socket.getsockname()

    def discover_devices(self):
        """Searches for and prints a list of Bluetooth devices found."""
        nearby_devices = discover_devices(lookup_names=True)
        print("Found %d devices" % len(nearby_devices))

        for addr, name in nearby_devices:
            print("\t%s - %s" % (addr, name))

    def advertise(self):
        """Advertises an  RFCOMM channel at self.uuid."""
        advertise_service(
            self.socket, "ShimiService",
            service_id=self.uuid,
            service_classes=[self.uuid, SERIAL_PORT_CLASS],
            profiles=[SERIAL_PORT_PROFILE]
        )
        print("Waiting for RFCOMM connection.", self.port)

    def stop_advertising(self):
        """Stops advertising an RFCOMM channel at self.uuid."""
        stop_advertising(self.socket)

    def wait_for_connection(self):
        """A blocking function that waits for a connection to RFCOMM channel at self.uuid."""
        self.client_socket, self.client_info = self.socket.accept()
        print("Connected to client:", self.client_info)
        self.listening_thread = threading.Thread(
            target=self.listen, args=(self.client_socket, self.mappings))
        self.listening_thread.start()
        self.stop_advertising()
        self.stop_pairing()

    def send(self, address, data):
        """Sends a formatted JSON string message to the connected Bluetooth client.

        Args:
            address (str): An identifier that allows the reciever to route the message.
            data (dict): Any data to be sent to the Bluetooth client.
        """
        message = json.dumps({
            "address": address,
            "data": data
        })

        self.client_socket.sendall(message + "|")

    def listen(self, socket, mappings):
        """A blocking function that continually listens for incoming messages from a connected Bluetooth client.

        Should be called on a new thread. 

        Args:
            socket (bluetooth.BluetoothSocket): The connected Bluetooth socket to listen on.
            mappings (dict): A mapping between addresses and function calls.
        """
        try:
            collected = ""
            while True:
                data = socket.recv(1024)
                print(data)
                if len(data) == 0:
                    break
                collected += data.decode("UTF-8")

                if "|" in collected:
                    split = collected.split("|")
                    collected = split.pop(len(split) - 1)

                    for json_string in split:
                        try:
                            message = json.loads(json_string)
                            if "address" in message.keys() and message["address"] in mappings.keys():
                                threading.Thread(target=mappings[message["address"]], args=(
                                    message["data"],)).start()
                            else:
                                print(
                                    "Received message with unknown address:", message)
                        except Exception as e:
                            print("Unable to parse incoming data.", e)
        except IOError as e:
            print("Bluetooth connection error:", e)
            self.connect()


if __name__ == '__main__':
    c = BluetoothClient()
    c.connect()
