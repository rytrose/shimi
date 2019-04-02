from login import get_authorized_spotipy
import pickle
import pretty_midi as pm
import numpy as np

if __name__ == '__main__':
    # s = get_authorized_spotipy("rytrose")
    analysis = pickle.load(open("september.p", "rb"))

    opening_section = analysis["sections"][0]
    initial_tempo = opening_section["tempo"]

    midi_file = pm.PrettyMIDI(initial_tempo=initial_tempo)

    initial_key = opening_section["key"]
    if initial_key != -1:
        midi_key = initial_key + (((opening_section["mode"] + 1) % 2) * 12)
        midi_file.key_signature_changes = [pm.KeySignature(midi_key, 0.0)]

    initial_time_signature = opening_section["time_signature"]
    midi_file.time_signature_changes = [pm.TimeSignature(initial_time_signature, 4, 0.0)]

    melody = pm.Instrument(0)

    for segment in analysis["segments"]:
        note = pm.Note(127, np.argmax(segment["pitches"]) + 69, segment["start"],
                       segment["start"] + segment["duration"])
        melody.notes.append(note)

    midi_file.instruments = [melody]

    midi_file.write("september_gen.mid")
