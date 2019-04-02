import os, sys

sys.path.insert(1, os.path.join(sys.path[0], '..'))
from shimi import Shimi
from communication.bluetooth_client import BluetoothClient
from webapp.webapp_controller import SingingProcessWrapper
import multiprocessing
import threading
import sqlite3
import os.path as op

ALL_SONGS = 0
PROCESSED_SONGS = 1
QUERIED_SONGS = 2

TEMP_DIR = 'temp'
TEMP_AUDIO_FILENAME = 'temp.wav'
TEMP_CNN_FILENAME = 'temp.txt'
TEMP_MELODIA_FILENAME = 'temp.p'
TEMP_AUDIO_DIR = op.join(TEMP_DIR, "audio")
TEMP_CNN_DIR = op.join(TEMP_DIR, "cnn")
TEMP_MELODIA_DIR = op.join(TEMP_DIR, "melodia")


class SingingBluetoothDemo:
    def __init__(self):
        self.shimi = Shimi()

        self.bluetooth_client = BluetoothClient()
        self.bluetooth_client.map("fetch_songs", self.on_fetch_songs)
        self.bluetooth_client.map("sing", self.on_sing)
        self.bluetooth_client.map("process", self.on_process)
        threading.Thread(target=self.bluetooth_client.connect).start()

        (self.singing_client_pipe, self.singing_server_pipe) = multiprocessing.Pipe()
        self.singing_process = SingingProcessWrapper(self.singing_server_pipe)
        self.singing_process.start()

        self.db_path = '/media/nvidia/disk3/shimi_library.db'

    def fetch_all_songs(self, num_results, offset):
        count_query = "select count(msd_id) from songs"
        query = "select msd_id, title, artist_name, release, processed from songs order by title asc limit ? offset ?"
        self.fetch_songs(count_query, query, [num_results, offset])

    def fetch_processed_songs(self, num_results, offset):
        count_query = "select count(msd_id) from songs where processed=1"
        query = "select msd_id, title, artist_name, release, processed from songs where processed=1 order by title asc limit ? offset ?"
        self.fetch_songs(count_query, query, [num_results, offset])

    def fetch_queried_songs(self, search_query, num_results, offset):
        count_query = "select count(msd_id) from songs where (title like '%'|| ? || '%' or artist_name like '%'|| ? || '%' or release like '%'|| ? || '%')"
        query = "select msd_id, title, artist_name, release, processed from songs where (title like '%'|| ? || '%' or artist_name like '%'|| ? || '%' or release like '%'|| ? || '%') order by title asc limit ? offset ?"
        self.fetch_songs(count_query, query, [search_query, search_query, search_query, num_results, offset],
                         count_params=[search_query, search_query, search_query])

    def fetch_songs(self, count_query, query, params, count_params=None):
        db_connection = sqlite3.connect(self.db_path)
        db_client = db_connection.cursor()

        if count_params:
            db_client.execute(count_query, count_params)
        else:
            db_client.execute(count_query)
        num_songs_in_query = db_client.fetchone()[0]

        message = {
            "num_songs_in_query": num_songs_in_query,
            "songs": []
        }

        db_client.execute(query, params)
        rows = db_client.fetchall()
        for row in rows:
            message["songs"].append({
                "msd_id": row[0],
                "title": row[1],
                "artist": row[2],
                "release": row[3],
                "processed": bool(row[4])
            })

        self.bluetooth_client.send("songs", message)

    def on_fetch_songs(self, message):
        fetch_type = message["type"]
        num_results = message["num"]
        offset = message["offset"]
        search_query = message["query"]

        if fetch_type == ALL_SONGS:
            self.fetch_all_songs(num_results, offset)
        elif fetch_type == PROCESSED_SONGS:
            self.fetch_processed_songs(num_results, offset)
        else:
            self.fetch_queried_songs(search_query, num_results, offset)

    def on_sing(self, message):
        msd_id = message["msd_id"]

        print("Prepping to sing %s..." % msd_id)

        # Get wav file
        r = requests.get("http://shimi-dataset-server.serveo.net/fetch/audio/%s" % msd_id)
        open(op.join(TEMP_AUDIO_DIR, TEMP_AUDIO_FILENAME), 'wb').write(r.content)

        # Get melody extraction file
        if extraction_type == "melodia":
            r = requests.get("http://shimi-dataset-server.serveo.net/fetch/melodia/%s" % msd_id)
            open(op.join(TEMP_MELODIA_DIR, TEMP_MELODIA_FILENAME), 'wb').write(r.content)

            singing_opts = {
                "audio_file": op.join(TEMP_AUDIO_DIR, TEMP_AUDIO_FILENAME),
                "extraction_type": "melodia",
                "analysis_file": op.join(TEMP_MELODIA_DIR, TEMP_MELODIA_FILENAME)
            }

        else:
            r = requests.get("http://shimi-dataset-server.serveo.net/fetch/cnn/%s" % msd_id)
            open(op.join(TEMP_CNN_DIR, TEMP_CNN_FILENAME), 'wb').write(r.content)

            singing_opts = {
                "audio_file": op.join(TEMP_AUDIO_DIR, TEMP_AUDIO_FILENAME),
                "extraction_type": "cnn",
                "analysis_file": op.join(TEMP_CNN_DIR, TEMP_CNN_FILENAME)
            }

        self.client_pipe.send(singing_opts)
        res = self.client_pipe.recv()
        print(res)

    def on_process(self, message):
        msd_id = message["msd_id"]
        r = requests.get("http://shimi-dataset-server.serveo.net/process/%s" % msd_id)
        print("Process response", r.content)

if __name__ == '__main__':
    d = SingingBluetoothDemo()
