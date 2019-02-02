from pyo import *
import multiprocessing
import time
import threading


class AudioAnalysis(multiprocessing.Process):
    def __init__(self, connection, duplex=True):
        super(AudioAnalysis, self).__init__()
        self.daemon = True
        self._terminated = False
        self._connection = connection
        self.duplex = duplex

    def run(self):
        if self.duplex:
            self.server = Server(sr=16000, ichnls=4)
            self.server.setInOutDevice(2)
        else:
            self.server = Server(sr=16000, duplex=0)
            self.server.setOutputDevice(2)
        self.server.deactivateMidi()
        self.server.boot().start()

        threading.Thread(target=self.server.gui, args=locals()).start()

        in_0 = Input(chnl=0, mul=0.25)
        in_1 = Input(chnl=1, mul=0.25)
        in_2 = Input(chnl=2, mul=0.25)
        in_3 = Input(chnl=3, mul=0.25)
        input = in_0 + in_1 + in_2 + in_3
        scope = Scope(input)

        while not self._terminated:
            to_do = self._connection.recv()
            func = getattr(self, to_do["function"])
            args = to_do["args"]
            kwargs = to_do["kwargs"]
            func(self, *args, **kwargs)

        self.server.stop()

    def stop(self):
        self._terminated = True

    def test(self, *args, **kwargs):
        for i, arg in enumerate(args):
            print("arg: %d" % i, arg)

        for k, v in kwargs.items():
            print("kwarg: %s" % k, v)

if __name__ == '__main__':
    (main_pipe, audio_pipe) = multiprocessing.Pipe()
    a = AudioAnalysis(audio_pipe)
    a.start()

    while True:
        time.sleep(1)