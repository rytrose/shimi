import sys, os

sys.path.insert(1, os.path.join(sys.path[0], '..'))
from pythonosc import osc_server, dispatcher, udp_client
import threading
from audio.singing import Singing
import requests

class WebappController:
    def __init__(self):
        # Dispatches received messages to callbacks
        self.dispatcher = dispatcher.Dispatcher()

        #   handler functions need to take 2 arguments, first the address, then the arguments
        # self.dispatcher.map("/test", self._test_handler)

        # Server for listening for OSC messages
        self.local_address = "127.0.0.1"
        self.local_port = 6101
        self.osc_server = osc_server.ThreadingOSCUDPServer((self.local_address, self.local_port), self.dispatcher)

        # Listen (in a separate thread as to not block)
        threading.Thread(target=self.osc_server.serve_forever).start()

        # Client for sending OSC messages
        self.remote_address = "127.0.0.1"
        self.remote_port = 6100
        self.osc_client = udp_client.SimpleUDPClient(self.remote_address, self.remote_port)

        self.singing = Singing(init_pyo=True, resource_path="/Users/rytrose/Cloud/GTCMT/shimi/audio")

    def _sing_handler(self, address, args):
        args = list(args)
        msd_id = args


