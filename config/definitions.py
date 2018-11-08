# Aliases for motor IDs
TORSO = 1
NECK_LR = 2
NECK_UD = 3
PHONE = 4
FOOT = 5

# Starting positions of motors
STARTING_POSITIONS = {
    TORSO: 10.0, # 0.0 down, 1.0 up
    NECK_LR: -21.6, # 0.0 left, 1.0 right
    NECK_UD: -3.0, # 0.0 up, 1.0 down
    PHONE: -11.85, # 0.0 down, 1.0 up
    FOOT: 5.41
}

# Angle limits for motors
ANGLE_LIMITS = {
    TORSO: [-99, 14.64],
    NECK_LR: [-111.6, 68.4],
    NECK_UD: [-49.01, 11.21],
    PHONE: [-101.85, 168.13],
    FOOT: [5.41, 21.14]
}