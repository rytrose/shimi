import json
from pprint import pprint
from pythonosc import dispatcher
from pythonosc import osc_server

def posenet_receiver(addr, pose_string):
    pose = json.loads(pose_string)
    pprint(pose)

dispatcher = dispatcher.Dispatcher()

dispatcher.map('/posenet', posenet_receiver)

server = osc_server.ThreadingOSCUDPServer(("localhost", 8000), dispatcher)
print("Serving on {}".format(server.server_address))
server.serve_forever()