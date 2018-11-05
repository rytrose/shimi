import music21 as m21
import pretty_midi as pm
import sounddevice as sd

class MidiAnalysis:
    def __init__(self, path):
        self.pm_obj = pm.PrettyMIDI(path)
        self.m21_obj = self._midi_file_to_m21(path)

    def _midi_file_to_m21(self, path):
        f = m21.midi.MidiFile()
        f.open(path)
        f.read()
        return m21.midi.translate.midiFileToStream(f)

    def get_tempo(self, t=0.0):
        """
        Gets the tempo of the MIDI file.
        :param t: time at which to get the tempo
        :return: the tempo of the MIDI file at time t in seconds per beat
        """
        tempo_change_times, tempi = self.pm_obj.get_tempo_changes()

        if t < 0:
            print("Unable to get tempo at time %f" % t)
            return

        for i in range(len(tempo_change_times)):
            tempo_change_time = tempo_change_times[i]
            tempo = tempi[i]

            # Look ahead for next tempo change
            if i < len(tempo_change_times) - 1:
                if t >= tempo_change_time and t < tempo_change_times[i + 1]:
                    return 1 / (tempo / 60)
            # If this is the last tempo, return it
            else:
                return 1 / (tempo / 60)

    def get_normalized_pitch_contour(self):
        """
        Gets a pitch contour for the MIDI melody, normalized for the range of the melody between 0-1.
        :return: a list of note objects with a norm_pitch attribute, and start/end times in seconds
        """
        interval = self.m21_obj.analyze('range')
        range = interval.semitones
        lowest = interval.noteStart.midi

        notes = []

        for note in self.pm_obj.instruments[0].notes:
            norm_pitch = (note.pitch - lowest) / range
            notes.append({
                "norm_pitch": norm_pitch,
                "start": note.start,
                "end": note.end
            })

        return notes

    def get_measure_keys(self):
        """
        Gets a key estimation for every measure, with timestamps.
        :return: a list of measures with a music21 key object and a timestamp
        """
        measure_times = self.pm_obj.get_downbeats()
        measure_keys = []

        for i in range(len(measure_times)):
            # 1-indexed measures
            measure_key = self.m21_obj.measures(i + 1, i + 2).analyze('key')
            measure_keys.append({
                "key": measure_key,
                "time": measure_times[i]
            })

        return measure_keys

    def play(self):
        out = self.pm_obj.synthesize()
        sd.play(out)