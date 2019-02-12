from pyo import *
import multiprocessing
import time
import threading

LEFT = 0
RIGHT = 1


class Sample:
    def __init__(self, path):
        PVSIZE = 1024
        PVOLAPS = 4

        self.path = path
        self.info = sndinfo(path)
        self.NUM_FRAMES = self.info[0]
        self.LENGTH = self.info[1]
        self.SR = self.info[2]
        self.snd_player = SfPlayer(self.path)

        import pdb
        pdb.set_trace()

        self.pv_analysis = PVAnal(self.snd_player, size=PVSIZE, overlaps=PVOLAPS)
        self.speed_table = LinTable([(0, 1), (512, 1)], size=512)
        self.speed_object = PVBufTabLoops(self.pv_analysis, self.speed_table, length=self.LENGTH)
        self.trans_value = Sig(1)
        self.trans_object = PVTranspose(self.speed_object, transpo=self.trans_value)
        self.pv_synth = PVSynth(self.trans_object)

    def play(self):
        self.speed_object.reset()
        self.pv_synth.out()

    def stop(self):
        self.pv_synth.stop()

    def set_transposition(self, val):
        self.trans_value.setValue(val)

    def set_speed(self, val):
        self.speed_table.replace([(0, val), (512, val)])

class AudioAnalysisClient:
    def __init__(self):
        (self.client_pipe, self.server_pipe) = multiprocessing.Pipe()
        self.analysis_server = AudioAnalysisServer(self.server_pipe)
        self.analysis_server.start()

    def _call(self, function_string, *args, **kwargs):
        call_obj = {
            "function": function_string,
            "args": args,
            "kwargs": kwargs
        }

        self.client_pipe.send(call_obj)
        res = self.client_pipe.recv()
        return res

    def get_freq(self):
        return self._call("get_freq")

    def get_freq_midi(self):
        return self._call("get_freq_midi")

    def stretch_and_shift(self, input_file, output_file, stretch_factor=1, n_steps=0):
        offline_server = Server(audio='offline')
        offline_server.boot()

        input_table = SndTable(input_file)
        pos = Phasor(freq=input_table.getRate() * (1 / stretch_factor), mul=input_table.getSize())
        gran = Granulator(table=input_table, env=WinTable(7), pitch=1 + (n_steps / 12),
                          pos=pos, grains=10, basedur=.1, mul=0.1).mix(2).out()

        offline_server.recordOptions(dur=input_table.getDur() * stretch_factor, filename=output_file)
        offline_server.start()


class AudioAnalysisServer(multiprocessing.Process):
    def __init__(self, connection, duplex=True):
        super(AudioAnalysisServer, self).__init__()
        self.daemon = True
        self._terminated = False
        self._connection = connection
        self.duplex = duplex

    def run(self):
        self.server = Server()
        pa_list_devices()

        # Mac testing
        # self.server.setInputDevice(0)
        # self.server.setOutputDevice(1)
        if self.duplex:
            self.server = Server(sr=16000, ichnls=4)
            self.server.setInOutDevice(2)
        else:
            self.server = Server(sr=16000, duplex=0)
            self.server.setOutputDevice(2)
        self.server.deactivateMidi()
        self.server.boot().start()

        in_0 = Input(chnl=0, mul=1)
        in_1 = Input(chnl=1, mul=1)
        in_mono = in_0 + in_1
        # _ = in_mono.mix(2).out()
        # delayed = Delay(in_mono, delay=4, maxdelay=4).mix(2).out()
        in_analysis = 15 * in_mono

        self.freq_hz = Yin(in_analysis)
        self.freq_midi = FToM(self.freq_hz)

        while not self._terminated:
            to_do = self._connection.recv()
            func = getattr(self, to_do["function"])
            args = to_do["args"]
            kwargs = to_do["kwargs"]
            res = func(self, *args, **kwargs)
            self._connection.send(res)

        self.server.stop()

    def stop(self):
        self._terminated = True

    def get_freq(self, *args, **kwargs):
        return self.freq_hz.get()

    def get_freq_midi(self, *args, **kwargs):
        return self.freq_midi.get()

    def sing(self, *args, **kwargs):
        pass


if __name__ == '__main__':
    a = AudioAnalysisClient()
