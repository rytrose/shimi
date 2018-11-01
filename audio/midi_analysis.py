import music21 as m21
import pretty_midi as pm

def midi_file_to_stream(path):
    f = m21.midi.MidiFile()
    f.open(path)
    f.read()
    return m21.midi.translate.midiFileToStream(f)

