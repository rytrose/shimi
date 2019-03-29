from bluetooth import *
import threading
from subprocess import Popen
import atexit
import json


class BluetoothClient:
    def __init__(self):
        self.uuid = "e872f7b9-315c-4d25-b771-d6c59297cd37"
        self.pairing_process = None
        self.socket = None
        self.port = None
        self.client_socket = None
        self.client_info = None
        self.listening_thread = None

        # self.discover_devices()
        atexit.register(self.stop_pairing)
        self.allow_pairing()

        self.setup_socket()
        self.advertise()
        self.wait_for_connection()

    def stop_pairing(self):
        print("Stopping bluetooth agent, will no longer accept pair requests.")
        self.pairing_process.terminate()

    def allow_pairing(self):
        self.pairing_process = Popen("bt-agent -c NoInputNoOutput".split(' '))
        print("Accepting pair requests.")

    def setup_socket(self):
        self.socket = BluetoothSocket(RFCOMM)
        self.socket.bind(("", PORT_ANY))
        self.socket.listen(1)
        self.port = self.socket.getsockname()

    def discover_devices(self):
        nearby_devices = discover_devices(lookup_names=True)
        print("Found %d devices" % len(nearby_devices))

        for addr, name in nearby_devices:
            print("  %s - %s" % (addr, name))

    def advertise(self):
        advertise_service(
            self.socket, "ShimiService",
            service_id=self.uuid,
            service_classes=[self.uuid, SERIAL_PORT_CLASS],
            profiles=[SERIAL_PORT_PROFILE]
        )
        print("Waiting for RFCOMM connection.", self.port)

    def stop_advertising(self):
        stop_advertising(self.socket)

    def wait_for_connection(self):
        self.client_socket, self.client_info = self.socket.accept()
        self.listening_thread = threading.Thread(target=self.listen, args=(self.client_socket,))
        self.listening_thread.start()
        print("Connected to client:", self.client_info)
        self.stop_advertising()
        self.stop_pairing()

    def send(self, message):
        self.socket.send(message)

    def listen(self, socket):
        try:
            while True:
                data = socket.recv(1024)
                if len(data) == 0:
                    break
                print("received [%s]" % data)
                socket.send("{\"address\": \"test\", \"data\": { \"hello\": \"android\" }}")
        except IOError as e:
            print(e)


if __name__ == '__main__':
    c = BluetoothClient()
