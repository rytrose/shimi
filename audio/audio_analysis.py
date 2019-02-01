from pyo import *
import multiprocessing
import time


class AudioAnalysis(multiprocessing.Process):
    def __init__(self, connection):
        super(AudioAnalysis, self).__init__()
        self.daemon = True
        self._terminated = False
        self._connection = connection

    def run(self):
        self.server = Server(sr=16000, duplex=0)
        self.server.setOutputDevice(2)
        self.server.deactivateMidi()
        self.server.boot().start()

        a = PinkNoise(.1).mix(2).out()

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
        main_pipe.send({
            "function": "test",
            "args": ("sup",),
            "kwargs": {
                "my": "dude"
            }
        })

        time.sleep(1)

# import time
# import multiprocessing
# from random import uniform
# from pyo import Server, SineLoop
#
#
# class Group(multiprocessing.Process):
#     def __init__(self, num_of_sines):
#         super(Group, self).__init__()
#         self.daemon = True
#         self._terminated = False
#         self.num_of_sines = num_of_sines
#
#     def run(self):
#         # All code that should run on a separated
#         # core must be created in the run() method.
#         self.server = Server(sr=16000, duplex=0)
#         self.server.setOutputDevice(2)
#         self.server.deactivateMidi()
#         self.server.boot().start()
#
#         freqs = [uniform(400, 800) for i in range(self.num_of_sines)]
#         self.oscs = SineLoop(freq=freqs, feedback=0.1, mul=.005).out()
#
#         # Keeps the process alive...
#         while not self._terminated:
#             time.sleep(0.001)
#
#         self.server.stop()
#
#     def stop(self):
#         self._terminated = True
#
#
# if __name__ == '__main__':
#     # Starts four processes playing 500 oscillators each.
#     jobs = [Group(500) for i in range(4)]
#     [job.start() for job in jobs]
#
#
#     def quit():
#         "Stops the workers and quit the program."
#         [job.stop() for job in jobs]
#         exit()
