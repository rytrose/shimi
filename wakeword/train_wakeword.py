import sys
import base64
import requests
from resources.credentials.snowboy_credentials import SNOWBOY_API_KEY
import argparse
import sounddevice as sd
import soundfile as sf
import time
import pyaudio
import wave

def get_wave(fname):
    """Returns base64 encoding of a specified file."""
    with open(fname, "rb") as infile:
        infile_bytes = infile.read()
        return base64.b64encode(infile_bytes).decode("utf-8")


def train_wakeword(age_group, gender, out):
    """Trains a model for the Snowboy wakeword engine.
    
    Args:
        age_group (str): Represents the age group of the speaker. Value should be in ["0_9", "10_19", "20_29", "30_39", "40_49", "50_59", "60+"].
        gender (str): Represents the gender of the speaker. Value should be in ["M", "F"].
        out (str): Filename at which to save the generated model.
    """

    audio = pyaudio.PyAudio()

    fs = 16000
    endpoint = "https://snowboy.kitt.ai/api/v1/train/"
    token = SNOWBOY_API_KEY
    hotword_name = "Hey Shimi"
    language = "en"
    microphone = "Seeed Studio ReSpeaker Mic Array v2.0"
    temp_file_prefix = "resources/temp/" + out + "_temp"

    files = []
    for i in range(1, 4):
        print("Press enter to begin recording the wakeword. [%d of 3]" % i)
        input()
        file = temp_file_prefix + str(i) + ".wav"
        record_pyaudio(audio, file)
        files.append(file)

    data = {
        "name": hotword_name,
        "language": language,
        "age_group": age_group,
        "gender": gender,
        "microphone": microphone,
        "token": token,
        "voice_samples": [
            {"wave": get_wave(files[0])},
            {"wave": get_wave(files[1])},
            {"wave": get_wave(files[2])}
        ]
    }

    print("Sending to snowboy...")
    response = requests.post(endpoint, json=data)
    if response.ok:
        print("Saving new model...")
        with open("resources/models/" + out, "wb") as outfile:
            outfile.write(response.content)
    else:
        print("Request failed.")
        print(response.text)

    audio.terminate()

def record_pyaudio(audio, name):
    """Records from Shimi's microphone.
    
    Args:
        audio (pyaudio.PyAudio): Instance of PyAudio object, interface to recording device.
        name (str): Filename at which to save the recording.
    """
    FORMAT = pyaudio.paInt16
    CHANNELS = 1
    RATE = 16000
    CHUNK = 1024

    stream = audio.open(input_device_index=2, format=FORMAT, channels=CHANNELS,
                        rate=RATE, input=True,
                        frames_per_buffer=CHUNK)

    frames = []

    try:
        print("Recording...press ^C to stop.")
        while True:
            data = stream.read(CHUNK)
            frames.append(data)
    except KeyboardInterrupt:
        stream.stop_stream()
        stream.close()

    waveFile = wave.open(name, 'wb')
    waveFile.setnchannels(CHANNELS)
    waveFile.setsampwidth(audio.get_sample_size(FORMAT))
    waveFile.setframerate(RATE)
    waveFile.writeframes(b''.join(frames))
    waveFile.close()

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="On-demand hotword model training.")
    parser.add_argument("-g", "--gender", type=str, default="M", choices=["M", "F"])
    parser.add_argument("-a", "--age_group", type=str, default="20_29",
                        choices=["0_9", "10_19", "20_29", "30_39", "40_49", "50_59", "60+"])
    parser.add_argument("-o", "--out", type=str, required=True)
    args = parser.parse_args()

    train_wakeword(args.age_group, args.gender, args.out)

