# Aliases for motor IDs
TORSO = 1
NECK_LR = 2
NECK_UD = 3
PHONE = 4
FOOT = 5

# Starting positions of motors
STARTING_POSITIONS = {
    TORSO: 10.0,
    NECK_LR: -21.6,
    NECK_UD: 0.0,
    PHONE: -11.85,
    FOOT: 5.41
}

# Angle limits for motors
ANGLE_LIMITS = {
    TORSO: [-99, 14.64],
    NECK_LR: [-111.6, 68.4],
    NECK_UD: [-50.4, 9.8],
    PHONE: [-101.85, 168.13],
    FOOT: [5.41, 21.14]
}