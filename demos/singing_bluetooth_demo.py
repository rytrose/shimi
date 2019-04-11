import os, sys

sys.path.insert(1, os.path.join(sys.path[0], '..'))
from shimi import Shimi
from communication.bluetooth_client import BluetoothClient
from webapp.webapp_controller import SingingProcessWrapper
import multiprocessing
import threading
import sqlite3
import os.path as op
import time

ALL_SONGS = 0
PROCESSED_SONGS = 1
QUERIED_SONGS = 2

LOCAL_STORAGE_DIR = "/media/nvidia/disk3/singing_files"
LOCAL_AUDIO_DIR = op.join(LOCAL_STORAGE_DIR, "audio")
LOCAL_CNN_DIR = op.join(LOCAL_STORAGE_DIR, "cnn_outputs")
LOCAL_MELODIA_DIR = op.join(LOCAL_STORAGE_DIR, "melodia_outputs")


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
        count_query = "select count(msd_id) from songs where processed=1"
        query = "select msd_id, title, artist_name, release, processed from songs where processed=1 order by title asc limit ? offset ?"
        self.fetch_songs(count_query, query, [num_results, offset])

    # def fetch_processed_songs(self, num_results, offset):
    #     count_query = "select count(msd_id) from songs where processed=1"
    #     query = "select msd_id, title, artist_name, release, processed from songs where processed=1 order by title asc limit ? offset ?"
    #     self.fetch_songs(count_query, query, [num_results, offset])

    def fetch_queried_songs(self, search_query, num_results, offset):
        count_query = "select count(msd_id) from songs where (title like '%'|| ? || '%' or artist_name like '%'|| ? || '%' or release like '%'|| ? || '%') and processed=1"
        query = "select msd_id, title, artist_name, release, processed from songs where (title like '%'|| ? || '%' or artist_name like '%'|| ? || '%' or release like '%'|| ? || '%') and processed=1 order by title asc limit ? offset ?"
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
        extraction_type = message["extraction_type"]
        print("Prepping to sing %s..." % msd_id)

        # Get melody extraction file
        if extraction_type == "melodia":
            singing_opts = {
                "audio_file": op.join(LOCAL_AUDIO_DIR, msd_id + ".wav"),
                "extraction_type": "melodia",
                "analysis_file": op.join(LOCAL_MELODIA_DIR, "melodia_" + msd_id + ".p")
            }

        else:
            singing_opts = {
                "audio_file": op.join(LOCAL_AUDIO_DIR, msd_id + ".wav"),
                "extraction_type": "cnn",
                "analysis_file": op.join(LOCAL_CNN_DIR, "cnn_" + msd_id + ".txt")
            }

        self.singing_client_pipe.send(singing_opts)
        res = self.singing_client_pipe.recv()
        print(res)

    def on_process(self, message):
        msd_id = message["msd_id"]
        r = requests.get("http://shimi-dataset-server.serveo.net/process/%s" % msd_id)
        print("Process response", r.content)


if __name__ == '__main__':
    d = SingingBluetoothDemo()
