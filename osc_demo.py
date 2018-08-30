import time
import pypot.robot
from pythonosc import dispatcher
from pythonosc import osc_server

starting_positions = {
    1: 10,
    2: 68.4 - 90,
    3: 0,
    4: -11.85,
    5: 5.41
}

def reset(sh):
    for m in sh.motors:
        m.compliant = False
    print("Setting motors to positions {0}".format(starting_positions))
    sh.goto_position({m.name: starting_positions[m.id] for m in sh.motors}, 0.5)

def setup():
    shimi = pypot.robot.from_json("shimi_robot_model.json")
    reset(shimi)
    return shimi

shimi = setup()

dispatcher = dispatcher.Dispatcher()

def foothandler(addr, pos, dur):
    for m in shimi.motors:
        if m.name == "m5":
            print("foot", m.present_position, pos, dur)
            shimi.goto_position({
                m.name: pos
            }, dur)

def phonehandler(addr, pos, dur):
    for m in shimi.motors:
        if m.name == "m4":
            print("phone", m.present_position, pos, dur)
            shimi.goto_position({
                m.name: pos
            }, dur)

def torsohandler(addr, pos, dur):
    for m in shimi.motors:
        if m.name == "m1":
            print("torso", m.present_position, pos, dur)
            shimi.goto_position({
                m.name: pos
            }, dur)

def neck_lr_handler(addr, pos, dur):
    for m in shimi.motors:
        if m.name == "m2":
            print("neck_lr", m.present_position, pos, dur)
            shimi.goto_position({
                m.name: pos
            }, dur)

def neck_ud_handler(addr, pos, dur):
    for m in shimi.motors:
        if m.name == "m3":
            print("neck_ud", m.present_position, pos, dur)
            shimi.goto_position({
                m.name: pos
            }, dur)

def compliantHandler(addr, compliant=1):
    if compliant > 0:
        for m in shimi.motors:
            m.compliant = True
    else:
        for m in shimi.motors:
            m.compliant = False

def resetHandler(addr, b):
    reset(shimi)

dispatcher.map("/foot", foothandler)
dispatcher.map("/phone", phonehandler)
dispatcher.map("/torso", torsohandler)
dispatcher.map("/neck_lr", neck_lr_handler)
dispatcher.map("/neck_ud", neck_ud_handler)
dispatcher.map("/exit", compliantHandler)
dispatcher.map("/reset", resetHandler)

server = osc_server.ThreadingOSCUDPServer(("127.0.0.1", 8000), dispatcher)
print("Serving on {}".format(server.server_address))
server.serve_forever()

