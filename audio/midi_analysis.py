import music21 as m21
import pretty_midi as pm
import sounddevice as sd


class MidiAnalysis:
    """Runs musical feature extraction methods on a MIDI file."""

    def __init__(self, path):
        self.pm_obj = pm.PrettyMIDI(path)
        self.m21_obj = self._midi_file_to_m21(path)

    def _midi_file_to_m21(self, path):
        """Helper function to open a MIDI file in music21."""
        f = m21.midi.MidiFile()
        f.open(path)
        f.read()
        return m21.midi.translate.midiFileToStream(f)

    def get_tempo(self, t=0.0):
        """Gets the tempo of the MIDI file.

        Args:
            t (float, optional): The time in seconds at which to get the tempo.

        Returns: 
            float: The tempo of the MIDI file at time t in seconds per beat.
        """
        tempo_change_times, tempi = self.pm_obj.get_tempo_changes()

        if t < 0:
            print("Unable to get tempo at time %f" % t)
            return

        for i in range(len(tempo_change_times)):
            tempo_change_time = tempo_change_times[i]
            tempo = tempi[i]

            if i < len(tempo_change_times) - 1:  # Look ahead for next tempo change
                if t >= tempo_change_time and t < tempo_change_times[i + 1]:
                    return 1 / (tempo / 60)
            else:  # If this is the last tempo, return it
                return 1 / (tempo / 60)

    def get_normalized_pitch_contour(self):
        """Gets a pitch contour for the MIDI melody, normalized for the range of the melody between 0-1.

        Returns: 
            List[dict]: Notes with a norm_pitch attribute, and start/end times in seconds.
        """
        interval = self.m21_obj.analyze('range')
        pitch_range = interval.semitones
        lowest = interval.noteStart.midi

        notes = []

        if pitch_range == 0:
            notes = [8.0 for _ in range(len(self.pm_obj.instruments[0].notes))]
            return notes

        for note in self.pm_obj.instruments[0].notes:
            norm_pitch = (note.pitch - lowest) / pitch_range
            notes.append({
                "norm_pitch": norm_pitch,
                "start": note.start,
                "end": note.end
            })

        return notes

    def get_measure_keys(self):
        """Gets a key estimation for every measure, with timestamps.

        Returns: 
            List[dict]: Measures with a music21 key object and a timestamp.
        """
        measure_times = self.pm_obj.get_downbeats()
        measure_keys = []

        for i in range(len(measure_times)):
            measure_key = self.m21_obj.measures(i + 1, i + 2).analyze('key')  # 1-indexed measures
            measure_keys.append({
                "key": measure_key,
                "time": measure_times[i]
            })

        return measure_keys

    def get_length(self):
        """Gets the length of the piece in seconds.

        Returns: 
            float: The length in seconds.
        """
        num_beats = len(self.pm_obj.get_beats())
        tempo = self.get_tempo()
        return (num_beats * tempo) + ((4 - (num_beats % 4)) * tempo)

    def get_shortest_note_length(self):
        """Gets the length of the shortest note that occurs in the piece, in seconds.

        Returns:
            float: The length of the shortest note in seconds.
        """
        
        shortest_length = 99999
        for note in self.pm_obj.instruments[0].notes:
            length = note.end - note.start
            if length < shortest_length:
                shortest_length = length

        return shortest_length

    def get_longest_note_length(self):
        """Gets the length of the longest note that occurs in the piece, in seconds.

        Returns: 
            float: The length of the longest note in seconds.
        """
        longest_length = -1
        for note in self.pm_obj.instruments[0].notes:
            length = note.end - note.start
            if length > longest_length:
                longest_length = length

        return longest_length

    def play(self):
        """Play basic synthesized audio of MIDI file."""
        out = self.pm_obj.synthesize()
        sd.play(out)
