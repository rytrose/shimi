import sys, os

sys.path.insert(1, os.path.join(sys.path[0], '..'))
from pythonosc import osc_server, dispatcher, udp_client
import threading
from audio.singing import Singing
import requests
import os.path as op

TEMP_DIR = 'temp'
TEMP_AUDIO_FILENAME = 'temp.wav'
TEMP_CNN_FILENAME = 'temp.txt'
TEMP_MELODIA_FILENAME = 'temp.p'
TEMP_AUDIO_DIR = op.join(TEMP_DIR, "audio")
TEMP_CNN_DIR = op.join(TEMP_DIR, "cnn")
TEMP_MELODIA_DIR = op.join(TEMP_DIR, "melodia")


class WebappController:
    def __init__(self):
        # Dispatches received messages to callbacks
        self.dispatcher = dispatcher.Dispatcher()

        #   handler functions need to take 2 arguments, first the address, then the arguments
        self.dispatcher.map("/sing", self._sing_handler)
        self.dispatcher.map("/process", self._process_handler)

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

    def _sing_handler(self, address, msd_id, extraction_type):
        # Get wav file
        r = requests.get("http://shimi-dataset-server.serveo.net/fetch/audio/%s" % msd_id)
        open(op.join(TEMP_AUDIO_DIR, TEMP_AUDIO_FILENAME), 'wb').write(r.content)

        # Get melody extraction file
        if extraction_type == "melodia":
            r = requests.get("http://shimi-dataset-server.serveo.net/fetch/melodia/%s" % msd_id)
            open(op.join(TEMP_MELODIA_DIR, TEMP_MELODIA_FILENAME), 'wb').write(r.content)
            self.singing.sing_audio(op.join(TEMP_AUDIO_DIR, TEMP_AUDIO_FILENAME), "melodia",
                                    op.join(TEMP_MELODIA_DIR, TEMP_MELODIA_FILENAME))
            os.remove(op.join(TEMP_MELODIA_DIR, TEMP_MELODIA_FILENAME))
        else:
            r = requests.get("http://shimi-dataset-server.serveo.net/fetch/cnn/%s" % msd_id)
            open(op.join(TEMP_CNN_DIR, TEMP_CNN_FILENAME), 'wb').write(r.content)
            self.singing.sing_audio(op.join(TEMP_AUDIO_DIR, TEMP_AUDIO_FILENAME), "cnn",
                                    op.join(TEMP_CNN_DIR, TEMP_CNN_FILENAME))
            os.remove(op.join(TEMP_CNN_DIR, TEMP_CNN_FILENAME))

        os.remove(op.join(TEMP_AUDIO_DIR, TEMP_AUDIO_FILENAME))

    def _process_handler(self, address, msd_id):
        r = requests.get("http://shimi-dataset-server.serveo.net/process/%s" % msd_id)
        print("Process response", r.content)


if __name__ == '__main__':
    c = WebappController()
