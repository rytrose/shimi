package mr1.robots.travis.moves;

import android.content.Context;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import mr1.motor.MotorController;
import mr1.motor.specific.ADKMX28MotorController;
import mr1.robots.travis.TravisDofs;
import mr1.robots.travis.moves.gestures.CombinationGesture;
import mr1.robots.travis.moves.gestures.DiscoGesture;
import mr1.robots.travis.moves.gestures.DiscoGesture2;
import mr1.robots.travis.moves.gestures.Funk;
import mr1.robots.travis.moves.gestures.GestureConstants;
import mr1.robots.travis.moves.gestures.Groove;
import mr1.robots.travis.moves.gestures.HipHopGesture;
import mr1.robots.travis.moves.gestures.RockGesture;
import mr1.robots.travis.moves.gestures.RockGesture2;
import mr1.robots.travis.moves.gestures.RockGesture3;
import mr1.robots.travis.moves.gestures.RockGesture4;
import mr1.robots.travis.moves.gestures.Swoop;
import mr1.robots.travis.moves.gestures.TravisGesture;
import mr1.robots.travis.moves.gestures.emotions.DecayTypes;
import mr1.robots.travis.moves.gestures.emotions.TravisEmotion;

public class Gesture implements TravisDofs, ThreadScheduler {
    protected Runnable angryGesture;
    protected Runnable angryUp;
    public float arousal;
    protected float beatDuration;
    protected Runnable bow;
    protected Runnable breathGesture;
    private volatile String currentGestureTag = "";
    public int decayTime;
    public int decayType;
    protected Runnable disgustGesture;
    protected boolean emotionTrigger;
    public TravisEmotion emotionVars = new TravisEmotion();
    public float emotiveBeats;
    public Runnable emotiveMove = new C00651();
    public float exaggeration;
    protected Runnable fearGesture;
    protected Runnable gesture;
    private Map<String, TravisGesture> gestures = new HashMap();
    protected Runnable handTwist;
    protected Runnable happyGesture;
    protected Runnable headNod;
    protected Runnable hiphopNodHigh;
    protected Runnable hiphopNodLow;
    protected Runnable laughGesture;
    private MotorController mc;
    private Move myMove;
    public float negArousal;
    protected Runnable negResponse;
    protected Runnable neutralBreathing;
    protected Runnable nodYes;
    protected Runnable nodYesCenter;
    protected Runnable nodYesLeft;
    protected Runnable playfulNod;
    public float posArousal;
    protected Runnable posResponse;
    protected Runnable reverseNodYes;
    protected Runnable sadGesture;
    protected Runnable shakeNeck;
    protected Runnable shakeNeckCenter;
    protected Runnable shakeNeckCenterReverse;
    protected Runnable shakeNo;
    protected Runnable shakeNoLeft;
    public float stance;
    protected Runnable startledGesture;
    protected Runnable surpriseGesture;
    protected Runnable swoopLeftHigh;
    protected Runnable swoopRightHigh;
    protected Runnable swoopingCenterLeft;
    protected Runnable swoopingCenterRight;
    protected Runnable swoopingLeft;
    protected Runnable swoopingLeftMid;
    protected Runnable swoopingRight;
    protected Runnable swoopingRightMid;
    protected Runnable tap;
    private volatile int transposeKey;
    public float valence;
    public float volatility;

    /* renamed from: mr1.robots.travis.moves.Gesture$1 */
    class C00651 implements Runnable {
        int beatCounter = 0;
        int negBeatCounter = 0;
        int tempNegBeats = 0;
        int tempPosBeats = 0;

        C00651() {
        }

        public void run() {
            if (Gesture.this.emotionTrigger) {
                Gesture.this.emotionVars.calculateEmotionalGesture(Gesture.this.valence, Gesture.this.arousal, Gesture.this.stance, Gesture.this.volatility, Gesture.this.posArousal, Gesture.this.negArousal, Gesture.this.exaggeration);
                Gesture.this.emotionVars.startTime = System.currentTimeMillis() - 1;
                Gesture.this.emotionTrigger = false;
                Gesture.this.emotionVars.decayTime = (float) Gesture.this.decayTime;
                if (Gesture.this.decayType == 4) {
                    Gesture.this.emotionVars.calculateDecay((int) (System.currentTimeMillis() - Gesture.this.emotionVars.startTime), Gesture.this.decayType);
                } else {
                    Gesture.this.emotionVars.initialMove();
                }
                Gesture.this.move(TravisDofs.NECKUD_MOTOR, ((Float) Gesture.this.emotionVars.positions.get(TravisDofs.NECKUD_MOTOR)).floatValue(), Gesture.this.emotiveBeats);
                this.beatCounter = 1000;
                this.negBeatCounter = 1000;
            } else {
                Gesture.this.emotionVars.calculateDecay((int) (System.currentTimeMillis() - Gesture.this.emotionVars.startTime), Gesture.this.decayType);
                Gesture.this.move(TravisDofs.NECKUD_MOTOR, ((Float) Gesture.this.emotionVars.positions.get(TravisDofs.NECKUD_MOTOR)).floatValue(), Gesture.this.emotiveBeats);
            }
            if (this.tempPosBeats <= this.beatCounter) {
                if (Gesture.this.emotionVars.nodYes) {
                    Gesture.this.nodYesDegree(Gesture.this.emotiveBeats * ((float) Gesture.this.emotionVars.posBeats), ((Float) Gesture.this.emotionVars.positions.get(TravisDofs.HEAD_MOTOR)).floatValue(), Gesture.this.emotionVars.degreeRangeHead, ((Float) Gesture.this.emotionVars.positions.get(TravisDofs.NECKUD_MOTOR)).floatValue(), Gesture.this.emotionVars.posMovesPerBeat);
                    this.tempPosBeats = Gesture.this.emotionVars.posBeats;
                    this.beatCounter = 0;
                } else {
                    Gesture.this.move(TravisDofs.HEAD_MOTOR, ((Float) Gesture.this.emotionVars.positions.get(TravisDofs.HEAD_MOTOR)).floatValue(), Gesture.this.emotiveBeats);
                    this.beatCounter = 0;
                }
            }
            if (this.tempNegBeats <= this.negBeatCounter && Gesture.this.emotionVars.shakeNo) {
                Gesture.this.shakeNoDegree(Gesture.this.emotiveBeats * ((float) Gesture.this.emotionVars.negBeats), 0.0f, Gesture.this.emotionVars.degreeRangeNeck, Gesture.this.emotionVars.negMovesPerBeat);
                this.tempNegBeats = Gesture.this.emotionVars.negBeats;
                this.negBeatCounter = 0;
            }
            this.beatCounter++;
            this.negBeatCounter++;
        }
    }

    /* renamed from: mr1.robots.travis.moves.Gesture$20 */
    class AnonymousClass20 implements Runnable {
        boolean nodStarted = false;
        float range;
        int repeatCount;
        private final /* synthetic */ float val$beats;
        private final /* synthetic */ float val$center;
        private final /* synthetic */ float val$degree;
        private final /* synthetic */ float val$height;
        private final /* synthetic */ int val$repeats;

        AnonymousClass20(float f, float f2, float f3, int i, float f4) {
            this.val$center = f;
            this.val$degree = f2;
            this.val$beats = f3;
            this.val$repeats = i;
            this.val$height = f4;
            this.range = Math.abs(Math.min(1.0f - f, -1.0f - f));
        }

        public void run() {
            if (this.nodStarted) {
                Gesture.this.move(TravisDofs.HEAD_MOTOR, this.val$center + (this.val$degree * this.range), (this.val$beats / ((float) this.val$repeats)) / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                Gesture.this.move(TravisDofs.NECKUD_MOTOR, this.val$height - 0.1f, (this.val$beats / ((float) this.val$repeats)) / 2.0f);
                this.repeatCount++;
                this.nodStarted = false;
                if (this.repeatCount < this.val$repeats) {
                    Gesture.handler.postDelayed(Gesture.this.nodYes, (long) ((int) (((this.val$beats / ((float) this.val$repeats)) / 2.0f) * Gesture.this.beatDuration)));
                    return;
                }
                return;
            }
            Gesture.this.move(TravisDofs.HEAD_MOTOR, this.val$center - (this.val$degree * this.range), (this.val$beats / ((float) this.val$repeats)) / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
            Gesture.this.move(TravisDofs.NECKUD_MOTOR, this.val$height + 0.1f, (this.val$beats / ((float) this.val$repeats)) / 2.0f);
            this.nodStarted = true;
            Gesture.handler.postDelayed(Gesture.this.nodYes, (long) ((int) (((this.val$beats / ((float) this.val$repeats)) / 2.0f) * Gesture.this.beatDuration)));
        }
    }

    /* renamed from: mr1.robots.travis.moves.Gesture$2 */
    class C00662 implements Runnable {
        int count = 0;
        Random random;
        List<Float> timesPerBeat;
        private final /* synthetic */ int val$beat;
        private final /* synthetic */ float val$beats;
        private final /* synthetic */ boolean val$following;
        private final /* synthetic */ String val$gestureTag;

        C00662(String str, int i, boolean z, float f) {
            this.val$gestureTag = str;
            this.val$beat = i;
            this.val$following = z;
            this.val$beats = f;
            this.timesPerBeat = (List) ((TravisGesture) Gesture.this.gestures.get(str)).timesPerBeat.get(i);
            this.random = new Random();
        }

        public void run() {
            System.out.println("size " + this.timesPerBeat.size());
            System.out.println("beat " + this.val$beat);
            if (((TravisGesture) Gesture.this.gestures.get(this.val$gestureTag)).hasRandomComponent && (!this.val$following || (this.val$following && !((TravisGesture) Gesture.this.gestures.get(this.val$gestureTag)).transposeDof.equals(TravisDofs.NECKRL_MOTOR)))) {
                if (this.random.nextDouble() > 0.5d) {
                    Gesture.this.myMove.moveWithVel(((TravisGesture) Gesture.this.gestures.get(this.val$gestureTag)).transposeDof, 0.5f, 0.4f);
                } else {
                    Gesture.this.myMove.moveWithVel(((TravisGesture) Gesture.this.gestures.get(this.val$gestureTag)).transposeDof, -0.5f, 0.4f);
                }
            }
            if (this.val$beat == 0 && !((TravisGesture) Gesture.this.gestures.get(this.val$gestureTag)).transposeDof.equals("") && this.random.nextDouble() > 0.4d) {
                Gesture.this.transposeKey = this.random.nextInt(((TravisGesture) Gesture.this.gestures.get(this.val$gestureTag)).transpositions.size());
                System.out.println("transposekey = " + Gesture.this.transposeKey);
            }
            int i = 0;
            while (i < ((TravisGesture) Gesture.this.gestures.get(this.val$gestureTag)).getMoveCount(this.val$beat, ((Float) this.timesPerBeat.get(this.count)).floatValue())) {
                System.out.println("count " + this.count);
                if (!this.val$following || (this.val$following && !((TravisGesture) Gesture.this.gestures.get(this.val$gestureTag)).getDof(this.val$beat, ((Float) this.timesPerBeat.get(this.count)).floatValue(), i).equals(TravisDofs.NECKRL_MOTOR))) {
                    if (!((TravisGesture) Gesture.this.gestures.get(this.val$gestureTag)).transposeDof.equals(((TravisGesture) Gesture.this.gestures.get(this.val$gestureTag)).getDof(this.val$beat, ((Float) this.timesPerBeat.get(this.count)).floatValue(), i))) {
                        Gesture.this.move(((TravisGesture) Gesture.this.gestures.get(this.val$gestureTag)).getDof(this.val$beat, ((Float) this.timesPerBeat.get(this.count)).floatValue(), i), ((TravisGesture) Gesture.this.gestures.get(this.val$gestureTag)).getPos(this.val$beat, ((Float) this.timesPerBeat.get(this.count)).floatValue(), i), ((TravisGesture) Gesture.this.gestures.get(this.val$gestureTag)).getDuration(this.val$beat, ((Float) this.timesPerBeat.get(this.count)).floatValue(), i) * this.val$beats, ((TravisGesture) Gesture.this.gestures.get(this.val$gestureTag)).getVelocities(this.val$beat, ((Float) this.timesPerBeat.get(this.count)).floatValue(), i));
                    } else if (((String) GestureConstants.GESTURE_TRANSPOSITION_TYPES.get(this.val$gestureTag)).equals("ADD")) {
                        Gesture.this.move(((TravisGesture) Gesture.this.gestures.get(this.val$gestureTag)).getDof(this.val$beat, ((Float) this.timesPerBeat.get(this.count)).floatValue(), i), ((TravisGesture) Gesture.this.gestures.get(this.val$gestureTag)).getPos(this.val$beat, ((Float) this.timesPerBeat.get(this.count)).floatValue(), i) + ((Float) ((TravisGesture) Gesture.this.gestures.get(this.val$gestureTag)).transpositions.get(Integer.valueOf(Gesture.this.transposeKey))).floatValue(), ((TravisGesture) Gesture.this.gestures.get(this.val$gestureTag)).getDuration(this.val$beat, ((Float) this.timesPerBeat.get(this.count)).floatValue(), i) * this.val$beats, ((TravisGesture) Gesture.this.gestures.get(this.val$gestureTag)).getVelocities(this.val$beat, ((Float) this.timesPerBeat.get(this.count)).floatValue(), i));
                    } else if (((String) GestureConstants.GESTURE_TRANSPOSITION_TYPES.get(this.val$gestureTag)).equals("MULTIPLY")) {
                        Gesture.this.move(((TravisGesture) Gesture.this.gestures.get(this.val$gestureTag)).getDof(this.val$beat, ((Float) this.timesPerBeat.get(this.count)).floatValue(), i), ((TravisGesture) Gesture.this.gestures.get(this.val$gestureTag)).getPos(this.val$beat, ((Float) this.timesPerBeat.get(this.count)).floatValue(), i) * ((Float) ((TravisGesture) Gesture.this.gestures.get(this.val$gestureTag)).transpositions.get(Integer.valueOf(Gesture.this.transposeKey))).floatValue(), ((TravisGesture) Gesture.this.gestures.get(this.val$gestureTag)).getDuration(this.val$beat, ((Float) this.timesPerBeat.get(this.count)).floatValue(), i) * this.val$beats, ((TravisGesture) Gesture.this.gestures.get(this.val$gestureTag)).getVelocities(this.val$beat, ((Float) this.timesPerBeat.get(this.count)).floatValue(), i));
                    }
                }
                i++;
            }
            this.count++;
            if (this.timesPerBeat.size() > this.count) {
                Gesture.handler.postDelayed(Gesture.this.gesture, (long) ((int) ((((Float) this.timesPerBeat.get(this.count)).floatValue() * this.val$beats) * Gesture.this.beatDuration)));
            }
        }
    }

    /* renamed from: mr1.robots.travis.moves.Gesture$30 */
    class AnonymousClass30 implements Runnable {
        boolean nodStarted;
        float range;
        int repeatCount = 0;
        private final /* synthetic */ float val$beats;
        private final /* synthetic */ float val$center;
        private final /* synthetic */ float val$degree;
        private final /* synthetic */ int val$repeats;

        AnonymousClass30(float f, float f2, float f3, int i) {
            this.val$center = f;
            this.val$degree = f2;
            this.val$beats = f3;
            this.val$repeats = i;
            this.range = Math.abs(Math.min(1.0f - f, -1.0f - f));
            this.nodStarted = false;
        }

        public void run() {
            if (this.nodStarted) {
                Gesture.this.move(TravisDofs.NECKRL_MOTOR, this.val$center + (this.range * this.val$degree), (this.val$beats / ((float) this.val$repeats)) / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                this.repeatCount++;
                this.nodStarted = false;
                if (this.repeatCount < this.val$repeats) {
                    Gesture.handler.postDelayed(Gesture.this.shakeNo, (long) ((int) (((this.val$beats / ((float) this.val$repeats)) / 2.0f) * Gesture.this.beatDuration)));
                    return;
                }
                return;
            }
            Gesture.this.move(TravisDofs.NECKRL_MOTOR, this.val$center - (this.range * this.val$degree), (this.val$beats / ((float) this.val$repeats)) / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
            this.nodStarted = true;
            Gesture.handler.postDelayed(Gesture.this.shakeNo, (long) ((int) (((this.val$beats / ((float) this.val$repeats)) / 2.0f) * Gesture.this.beatDuration)));
        }
    }

    public void cancelMoves() {
        this.myMove.cancelMoves();
        handler.removeCallbacks(this.sadGesture);
        handler.removeCallbacks(this.happyGesture);
        handler.removeCallbacks(this.laughGesture);
        handler.removeCallbacks(this.surpriseGesture);
        handler.removeCallbacks(this.startledGesture);
        handler.removeCallbacks(this.fearGesture);
        handler.removeCallbacks(this.disgustGesture);
        handler.removeCallbacks(this.angryGesture);
        handler.removeCallbacks(this.neutralBreathing);
        handler.removeCallbacks(this.swoopingLeft);
        handler.removeCallbacks(this.swoopingRight);
        handler.removeCallbacks(this.swoopingCenterLeft);
        handler.removeCallbacks(this.swoopingCenterRight);
        handler.removeCallbacks(this.tap);
        handler.removeCallbacks(this.gesture);
        handler.removeCallbacks(this.breathGesture);
        handler.removeCallbacks(this.nodYesLeft);
        handler.removeCallbacks(this.nodYesCenter);
        handler.removeCallbacks(this.nodYes);
        handler.removeCallbacks(this.posResponse);
        handler.removeCallbacks(this.shakeNoLeft);
        handler.removeCallbacks(this.shakeNo);
        handler.removeCallbacks(this.negResponse);
        handler.removeCallbacks(this.bow);
        handler.removeCallbacks(this.playfulNod);
        handler.removeCallbacks(this.reverseNodYes);
    }

    public Gesture(Context c, float beatDuration) {
        this.mc = new ADKMX28MotorController(c, "/mr1/robots/travis/motor_config.xml");
        this.myMove = new Move(c, this.mc);
        this.myMove.setBeatDuration(beatDuration);
        this.beatDuration = beatDuration;
        loadGestures();
        System.out.println("beat dur = " + this.beatDuration);
    }

    public Gesture(Context c, Move move, float beatDuration) {
        this.mc = new ADKMX28MotorController(c, "/mr1/robots/travis/motor_config.xml");
        this.myMove = move;
        this.myMove.setBeatDuration(beatDuration);
        this.beatDuration = beatDuration;
        loadGestures();
    }

    public void setAlternateMove(Move myMove) {
        this.myMove = myMove;
    }

    public Float getPosition(String dof) {
        return this.myMove.getPosition(dof);
    }

    public void setBeatDuration(float dur) {
        this.myMove.setBeatDuration(dur);
    }

    public float getBeatDuration() {
        return this.myMove.getBeatDuration();
    }

    public MotorController getMotorController() {
        return this.mc;
    }

    public void home() {
        this.myMove.moveWithVel(TravisDofs.NECKRL_MOTOR, 0.0f, 0.4f);
        this.myMove.moveWithVel(TravisDofs.NECKUD_MOTOR, 0.0f, 0.6f);
        this.myMove.moveWithVel(TravisDofs.HAND_MOTOR, 0.4f, 0.4f);
        this.myMove.moveWithVel(TravisDofs.HEAD_MOTOR, 0.0f, 0.4f);
        this.myMove.moveWithVel(TravisDofs.LEG_MOTOR, -1.0f, 0.4f);
    }

    public void homeDown() {
        this.myMove.moveWithVel(TravisDofs.NECKRL_MOTOR, 0.0f, 0.6f);
        this.myMove.moveWithVel(TravisDofs.NECKUD_MOTOR, -0.6f, 0.3f);
        this.myMove.moveWithVel(TravisDofs.HAND_MOTOR, 0.4f, 0.6f);
        this.myMove.moveWithVel(TravisDofs.HEAD_MOTOR, 0.0f, 0.6f);
        this.myMove.moveWithVel(TravisDofs.LEG_MOTOR, -1.0f, 0.6f);
    }

    public void loadGestures() {
        this.gestures.put("HipHopGesture", new HipHopGesture());
        this.gestures.put("RockGesture", new RockGesture());
        this.gestures.put("CombinationGesture", new CombinationGesture());
        this.gestures.put("RockGesture2", new RockGesture2());
        this.gestures.put("RockGesture3", new RockGesture3());
        this.gestures.put("RockGesture4", new RockGesture4());
        this.gestures.put("DiscoGesture", new DiscoGesture());
        this.gestures.put("DiscoGesture2", new DiscoGesture2());
        this.gestures.put("Groove", new Groove());
        this.gestures.put("Swoop", new Swoop());
        this.gestures.put("Funk", new Funk());
    }

    public void gesturalMove(String gestureTag, int beat, float beats, boolean following) {
        handler.removeCallbacks(this.gesture);
        if (!this.currentGestureTag.equals(gestureTag)) {
            this.transposeKey = 0;
            this.currentGestureTag = gestureTag;
        }
        this.gesture = new C00662(gestureTag, beat, following, beats);
        handler.postDelayed(this.gesture, (long) (((Float) ((List) ((TravisGesture) this.gestures.get(gestureTag)).timesPerBeat.get(beat)).get(0)).floatValue() * this.beatDuration));
    }

    public void generalMove(Map<String, List<Float>> moves) {
        for (String key : moves.keySet()) {
            this.myMove.move(key, ((Float) ((List) moves.get(key)).get(0)).floatValue(), ((Float) ((List) moves.get(key)).get(1)).floatValue());
        }
    }

    public void move(String dof, float pos, float beats, List<Float> velocities) {
        this.myMove.move(dof, pos, beats, velocities);
    }

    public void move(String dof, float pos, float beats) {
        this.myMove.move(dof, pos, beats);
    }

    public void move(Curve curve) {
        System.out.println("******************************************");
        if (curve.acceleration.size() < 1) {
            this.myMove.move(curve.dof, curve.pos, curve.beats);
            return;
        }
        System.out.println("******************************************");
        this.myMove.move(curve.dof, curve.pos, curve.beats, curve.acceleration);
    }

    public void moveMaxSpeed(String dof, float pos) {
        this.myMove.moveMaxSpeed(dof, pos);
    }

    public void move(List<Curve> curves) {
        for (int i = 0; i < curves.size(); i++) {
            move((Curve) curves.get(i));
        }
    }

    public void neckLeft(float beats) {
        move(TravisDofs.NECKRL_MOTOR, 0.9f, beats);
    }

    public void neckRight(float beats) {
        move(TravisDofs.NECKRL_MOTOR, -0.9f, beats);
    }

    public void headUp(float beats) {
        move(TravisDofs.HEAD_MOTOR, -1.0f, beats);
    }

    public void headDown(float beats) {
        move(TravisDofs.HEAD_MOTOR, 1.0f, beats);
    }

    public void swoopRight(final float beats) {
        handler.removeCallbacks(this.swoopingRight);
        this.swoopingRight = new Runnable() {
            boolean neckStarted = false;
            boolean up = true;

            public void run() {
                System.out.println("********** swooping right");
                if (!this.neckStarted) {
                    Gesture.this.move(TravisDofs.NECKRL_MOTOR, -0.8f, beats, Arrays.asList(new Float[]{Float.valueOf(0.16f), Float.valueOf(0.2f), Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.7f), Float.valueOf(0.5f), Float.valueOf(0.3f), Float.valueOf(0.2f), Float.valueOf(0.16f)}));
                    this.neckStarted = true;
                }
                if (this.up) {
                    Gesture.this.move(TravisDofs.HEAD_MOTOR, 1.0f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.16f), Float.valueOf(0.2f), Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    Gesture.this.move(TravisDofs.NECKUD_MOTOR, 0.1f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.75f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    this.up = false;
                    Gesture.handler.postDelayed(Gesture.this.swoopingRight, (long) ((int) ((beats / 2.0f) * Gesture.this.beatDuration)));
                    return;
                }
                Gesture.this.move(TravisDofs.HEAD_MOTOR, -1.0f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.7f), Float.valueOf(0.5f), Float.valueOf(0.3f), Float.valueOf(0.2f), Float.valueOf(0.16f)}));
                Gesture.this.move(TravisDofs.NECKUD_MOTOR, 0.4f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.75f), Float.valueOf(0.1f)}));
                this.up = true;
            }
        };
        handler.post(this.swoopingRight);
    }

    public void swoopLeft(final float beats) {
        handler.removeCallbacks(this.swoopingLeft);
        this.swoopingLeft = new Runnable() {
            boolean neckStarted = false;
            boolean up = true;

            public void run() {
                if (!this.neckStarted) {
                    Gesture.this.move(TravisDofs.NECKRL_MOTOR, 0.8f, beats, Arrays.asList(new Float[]{Float.valueOf(0.16f), Float.valueOf(0.2f), Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.7f), Float.valueOf(0.5f), Float.valueOf(0.3f), Float.valueOf(0.2f), Float.valueOf(0.16f)}));
                    this.neckStarted = true;
                }
                if (this.up) {
                    Gesture.this.move(TravisDofs.HEAD_MOTOR, 1.0f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.16f), Float.valueOf(0.2f), Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    Gesture.this.move(TravisDofs.NECKUD_MOTOR, 0.1f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.75f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    this.up = false;
                    Gesture.handler.postDelayed(Gesture.this.swoopingLeft, (long) ((int) ((beats / 2.0f) * Gesture.this.beatDuration)));
                    return;
                }
                Gesture.this.move(TravisDofs.HEAD_MOTOR, -1.0f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.7f), Float.valueOf(0.5f), Float.valueOf(0.3f), Float.valueOf(0.2f), Float.valueOf(0.16f)}));
                Gesture.this.move(TravisDofs.NECKUD_MOTOR, 0.4f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.75f), Float.valueOf(0.1f)}));
                this.up = true;
            }
        };
        handler.post(this.swoopingLeft);
    }

    public void swoopRightHigh(final float beats) {
        handler.removeCallbacks(this.swoopRightHigh);
        this.swoopRightHigh = new Runnable() {
            boolean neckStarted = false;
            boolean up = true;

            public void run() {
                System.out.println("********** swooping right");
                if (!this.neckStarted) {
                    Gesture.this.move(TravisDofs.NECKRL_MOTOR, -0.8f, beats, Arrays.asList(new Float[]{Float.valueOf(0.16f), Float.valueOf(0.2f), Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.7f), Float.valueOf(0.5f), Float.valueOf(0.3f), Float.valueOf(0.2f), Float.valueOf(0.16f)}));
                    this.neckStarted = true;
                }
                if (this.up) {
                    Gesture.this.move(TravisDofs.HEAD_MOTOR, 1.0f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.16f), Float.valueOf(0.2f), Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    Gesture.this.move(TravisDofs.NECKUD_MOTOR, 0.4f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.75f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    this.up = false;
                    Gesture.handler.postDelayed(Gesture.this.swoopRightHigh, (long) ((int) ((beats / 2.0f) * Gesture.this.beatDuration)));
                    return;
                }
                Gesture.this.move(TravisDofs.HEAD_MOTOR, -1.0f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.7f), Float.valueOf(0.5f), Float.valueOf(0.3f), Float.valueOf(0.2f), Float.valueOf(0.16f)}));
                Gesture.this.move(TravisDofs.NECKUD_MOTOR, 0.8f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.75f), Float.valueOf(0.1f)}));
                this.up = true;
            }
        };
        handler.post(this.swoopRightHigh);
    }

    public void swoopLeftHigh(final float beats) {
        handler.removeCallbacks(this.swoopLeftHigh);
        this.swoopLeftHigh = new Runnable() {
            boolean neckStarted = false;
            boolean up = true;

            public void run() {
                if (!this.neckStarted) {
                    Gesture.this.move(TravisDofs.NECKRL_MOTOR, 0.8f, beats, Arrays.asList(new Float[]{Float.valueOf(0.16f), Float.valueOf(0.2f), Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.7f), Float.valueOf(0.5f), Float.valueOf(0.3f), Float.valueOf(0.2f), Float.valueOf(0.16f)}));
                    this.neckStarted = true;
                }
                if (this.up) {
                    Gesture.this.move(TravisDofs.HEAD_MOTOR, 1.0f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.16f), Float.valueOf(0.2f), Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    Gesture.this.move(TravisDofs.NECKUD_MOTOR, 0.4f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.75f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    this.up = false;
                    Gesture.handler.postDelayed(Gesture.this.swoopLeftHigh, (long) ((int) ((beats / 2.0f) * Gesture.this.beatDuration)));
                    return;
                }
                Gesture.this.move(TravisDofs.HEAD_MOTOR, -1.0f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.7f), Float.valueOf(0.5f), Float.valueOf(0.3f), Float.valueOf(0.2f), Float.valueOf(0.16f)}));
                Gesture.this.move(TravisDofs.NECKUD_MOTOR, 0.8f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.75f), Float.valueOf(0.1f)}));
                this.up = true;
            }
        };
        handler.post(this.swoopLeftHigh);
    }

    public void swoopRightMid(final float beats) {
        handler.removeCallbacks(this.swoopingRightMid);
        this.swoopingRightMid = new Runnable() {
            boolean neckStarted = false;
            boolean up = true;

            public void run() {
                System.out.println("********** swooping right");
                if (!this.neckStarted) {
                    Gesture.this.move(TravisDofs.NECKRL_MOTOR, -0.8f, beats, Arrays.asList(new Float[]{Float.valueOf(0.16f), Float.valueOf(0.2f), Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.7f), Float.valueOf(0.5f), Float.valueOf(0.3f), Float.valueOf(0.2f), Float.valueOf(0.16f)}));
                    this.neckStarted = true;
                }
                if (this.up) {
                    Gesture.this.move(TravisDofs.HEAD_MOTOR, 1.0f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.16f), Float.valueOf(0.2f), Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    Gesture.this.move(TravisDofs.NECKUD_MOTOR, -0.4f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.75f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    this.up = false;
                    Gesture.handler.postDelayed(Gesture.this.swoopingRightMid, (long) ((int) ((beats / 2.0f) * Gesture.this.beatDuration)));
                    return;
                }
                Gesture.this.move(TravisDofs.HEAD_MOTOR, -1.0f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.7f), Float.valueOf(0.5f), Float.valueOf(0.3f), Float.valueOf(0.2f), Float.valueOf(0.16f)}));
                Gesture.this.move(TravisDofs.NECKUD_MOTOR, 0.1f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.75f), Float.valueOf(0.1f)}));
                this.up = true;
            }
        };
        handler.post(this.swoopingRightMid);
    }

    public void swoopLeftMid(final float beats) {
        handler.removeCallbacks(this.swoopingLeftMid);
        this.swoopingLeftMid = new Runnable() {
            boolean neckStarted = false;
            boolean up = true;

            public void run() {
                if (!this.neckStarted) {
                    Gesture.this.move(TravisDofs.NECKRL_MOTOR, 0.8f, beats, Arrays.asList(new Float[]{Float.valueOf(0.16f), Float.valueOf(0.2f), Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.7f), Float.valueOf(0.5f), Float.valueOf(0.3f), Float.valueOf(0.2f), Float.valueOf(0.16f)}));
                    this.neckStarted = true;
                }
                if (this.up) {
                    Gesture.this.move(TravisDofs.HEAD_MOTOR, 1.0f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.16f), Float.valueOf(0.2f), Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    Gesture.this.move(TravisDofs.NECKUD_MOTOR, -0.4f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.75f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    this.up = false;
                    Gesture.handler.postDelayed(Gesture.this.swoopingLeftMid, (long) ((int) ((beats / 2.0f) * Gesture.this.beatDuration)));
                    return;
                }
                Gesture.this.move(TravisDofs.HEAD_MOTOR, -1.0f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.7f), Float.valueOf(0.5f), Float.valueOf(0.3f), Float.valueOf(0.2f), Float.valueOf(0.16f)}));
                Gesture.this.move(TravisDofs.NECKUD_MOTOR, 0.1f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.75f), Float.valueOf(0.1f)}));
                this.up = true;
            }
        };
        handler.post(this.swoopingLeftMid);
    }

    public void swoopCenterLeft(final float beats) {
        handler.removeCallbacks(this.swoopingCenterLeft);
        this.swoopingCenterLeft = new Runnable() {
            boolean neckStarted = false;
            boolean up = true;

            public void run() {
                if (!this.neckStarted) {
                    Gesture.this.move(TravisDofs.NECKRL_MOTOR, 0.3f, beats, Arrays.asList(new Float[]{Float.valueOf(0.16f), Float.valueOf(0.2f), Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.7f), Float.valueOf(0.5f), Float.valueOf(0.3f), Float.valueOf(0.2f), Float.valueOf(0.16f)}));
                    this.neckStarted = true;
                }
                if (this.up) {
                    Gesture.this.move(TravisDofs.HEAD_MOTOR, 0.7f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.16f), Float.valueOf(0.2f), Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    Gesture.this.move(TravisDofs.NECKUD_MOTOR, -0.5f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.75f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    this.up = false;
                    Gesture.handler.postDelayed(Gesture.this.swoopingCenterLeft, (long) ((int) ((beats / 2.0f) * Gesture.this.beatDuration)));
                    return;
                }
                Gesture.this.move(TravisDofs.HEAD_MOTOR, -0.7f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.7f), Float.valueOf(0.5f), Float.valueOf(0.3f), Float.valueOf(0.2f), Float.valueOf(0.16f)}));
                Gesture.this.move(TravisDofs.NECKUD_MOTOR, -0.2f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.75f), Float.valueOf(0.1f)}));
                this.up = true;
            }
        };
        handler.post(this.swoopingCenterLeft);
    }

    public void swoopCenterRight(final float beats) {
        handler.removeCallbacks(this.swoopingCenterRight);
        this.swoopingCenterRight = new Runnable() {
            boolean neckStarted = false;
            boolean up = true;

            public void run() {
                System.out.println("********** swooping right");
                if (!this.neckStarted) {
                    Gesture.this.move(TravisDofs.NECKRL_MOTOR, -0.3f, beats, Arrays.asList(new Float[]{Float.valueOf(0.16f), Float.valueOf(0.2f), Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.7f), Float.valueOf(0.5f), Float.valueOf(0.3f), Float.valueOf(0.2f), Float.valueOf(0.16f)}));
                    this.neckStarted = true;
                }
                if (this.up) {
                    Gesture.this.move(TravisDofs.HEAD_MOTOR, 0.7f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.16f), Float.valueOf(0.2f), Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    Gesture.this.move(TravisDofs.NECKUD_MOTOR, -0.5f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.75f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    this.up = false;
                    Gesture.handler.postDelayed(Gesture.this.swoopingCenterRight, (long) ((int) ((beats / 2.0f) * Gesture.this.beatDuration)));
                    return;
                }
                Gesture.this.move(TravisDofs.HEAD_MOTOR, -0.7f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.7f), Float.valueOf(0.5f), Float.valueOf(0.3f), Float.valueOf(0.2f), Float.valueOf(0.16f)}));
                Gesture.this.move(TravisDofs.NECKUD_MOTOR, -0.2f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.75f), Float.valueOf(0.1f)}));
                this.up = true;
            }
        };
        handler.post(this.swoopingCenterRight);
    }

    public void swoopDown(float beats) {
        move(TravisDofs.HEAD_MOTOR, 1.0f, beats, Arrays.asList(new Float[]{Float.valueOf(0.12f), Float.valueOf(0.16f), Float.valueOf(0.2f), Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
        move(TravisDofs.NECKRL_MOTOR, 0.0f, beats, Arrays.asList(new Float[]{Float.valueOf(0.12f), Float.valueOf(0.16f), Float.valueOf(0.2f), Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
        move(TravisDofs.NECKUD_MOTOR, 0.1f, beats, Arrays.asList(new Float[]{Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
    }

    public void swoopUpRight(float beats) {
        move(TravisDofs.HEAD_MOTOR, -0.8f, beats, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.9f), Float.valueOf(0.8f), Float.valueOf(0.6f), Float.valueOf(0.3f)}));
        move(TravisDofs.NECKRL_MOTOR, -0.9f, beats, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.9f), Float.valueOf(0.8f), Float.valueOf(0.6f), Float.valueOf(0.3f)}));
        move(TravisDofs.NECKUD_MOTOR, 0.4f, beats, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.7f), Float.valueOf(0.4f)}));
    }

    public void swoopUpLeft(float beats) {
        move(TravisDofs.HEAD_MOTOR, -0.8f, beats, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.9f), Float.valueOf(0.8f), Float.valueOf(0.6f), Float.valueOf(0.3f)}));
        move(TravisDofs.NECKRL_MOTOR, 0.9f, beats, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.9f), Float.valueOf(0.8f), Float.valueOf(0.6f), Float.valueOf(0.3f)}));
        move(TravisDofs.NECKUD_MOTOR, 0.4f, beats, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.7f), Float.valueOf(0.4f)}));
    }

    public void discoDown(float beats) {
        move(TravisDofs.HEAD_MOTOR, 1.0f, 0.6f * beats);
        move(TravisDofs.NECKRL_MOTOR, 0.0f, 0.6f * beats);
        move(TravisDofs.NECKUD_MOTOR, 0.4f, 0.6f * beats);
    }

    public void discoRight(float beats) {
        move(TravisDofs.HEAD_MOTOR, -0.8f, 0.6f * beats);
        move(TravisDofs.NECKRL_MOTOR, -0.9f, 0.6f * beats);
        move(TravisDofs.NECKUD_MOTOR, 1.0f, 0.6f * beats);
    }

    public void discoLeft(float beats) {
        move(TravisDofs.HEAD_MOTOR, -0.8f, 0.6f * beats);
        move(TravisDofs.NECKRL_MOTOR, 0.9f, 0.6f * beats);
        move(TravisDofs.NECKUD_MOTOR, 1.0f, 0.6f * beats);
    }

    public void swoopDownRight(float beats) {
        move(TravisDofs.HEAD_MOTOR, 1.0f, beats, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.95f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
        move(TravisDofs.NECKRL_MOTOR, -0.65f, beats, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.95f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
        move(TravisDofs.NECKUD_MOTOR, -0.5f, beats);
    }

    public void swoopDownLeft(float beats) {
        move(TravisDofs.HEAD_MOTOR, 1.0f, beats, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.95f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
        move(TravisDofs.NECKRL_MOTOR, 0.65f, beats, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.95f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
        move(TravisDofs.NECKUD_MOTOR, -0.5f, beats);
    }

    public void swoopNeckRight(float beats) {
        move(TravisDofs.NECKRL_MOTOR, -0.8f, beats, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.95f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
    }

    public void swoopNeckLeft(float beats) {
        move(TravisDofs.NECKRL_MOTOR, 0.8f, beats, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.95f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
    }

    public void lookAtLeg(float beats) {
        move(TravisDofs.HEAD_MOTOR, 1.0f, beats);
        move(TravisDofs.NECKRL_MOTOR, -0.65f, beats);
        move(TravisDofs.NECKUD_MOTOR, -0.8f, beats);
        move(TravisDofs.HAND_MOTOR, -0.6f, beats);
    }

    public void lookAtPhone(float beats) {
        move(TravisDofs.HEAD_MOTOR, 1.0f, beats);
        move(TravisDofs.NECKRL_MOTOR, 0.75f, beats, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.9f), Float.valueOf(0.8f), Float.valueOf(0.6f), Float.valueOf(0.3f)}));
        move(TravisDofs.NECKUD_MOTOR, -0.6f, beats);
        move(TravisDofs.HAND_MOTOR, -0.6f, beats, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.9f), Float.valueOf(0.8f), Float.valueOf(0.6f), Float.valueOf(0.3f)}));
    }

    public void lookAtPhone_Alt(float beats) {
        move(TravisDofs.HEAD_MOTOR, 1.0f, beats);
        move(TravisDofs.NECKRL_MOTOR, 0.75f, 0.6f * beats);
        move(TravisDofs.NECKUD_MOTOR, -0.4f, beats);
        move(TravisDofs.HAND_MOTOR, -0.6f, beats);
    }

    public void home(float beats) {
        move(TravisDofs.NECKRL_MOTOR, 0.0f, beats);
        move(TravisDofs.NECKUD_MOTOR, 0.4f, beats);
        move(TravisDofs.HAND_MOTOR, 0.4f, beats);
        move(TravisDofs.HEAD_MOTOR, 0.0f, beats);
    }

    public void tap(final float beats) {
        handler.removeCallbacks(this.tap);
        this.tap = new Runnable() {
            boolean tapStarted = false;

            public void run() {
                if (this.tapStarted) {
                    Gesture.this.move(TravisDofs.LEG_MOTOR, -1.0f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    return;
                }
                Gesture.this.move(TravisDofs.LEG_MOTOR, 0.9f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                this.tapStarted = true;
                Gesture.handler.postDelayed(Gesture.this.tap, (long) ((int) ((beats / 2.0f) * Gesture.this.beatDuration)));
            }
        };
        handler.post(this.tap);
    }

    public void handTwist(final float beats) {
        handler.removeCallbacks(this.handTwist);
        this.handTwist = new Runnable() {
            boolean twistStarted = false;

            public void run() {
                if (this.twistStarted) {
                    Gesture.this.move(TravisDofs.HAND_MOTOR, -0.5f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    return;
                }
                Gesture.this.move(TravisDofs.HAND_MOTOR, 0.5f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                this.twistStarted = true;
                Gesture.handler.postDelayed(Gesture.this.handTwist, (long) ((int) ((beats / 2.0f) * Gesture.this.beatDuration)));
            }
        };
        handler.post(this.handTwist);
    }

    public void nodYes(final float beats) {
        handler.removeCallbacks(this.nodYes);
        this.nodYes = new Runnable() {
            boolean nodStarted = false;

            public void run() {
                if (this.nodStarted) {
                    Gesture.this.move(TravisDofs.HEAD_MOTOR, 0.5f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    Gesture.this.move(TravisDofs.NECKUD_MOTOR, 0.3f, beats / 2.0f);
                    return;
                }
                Gesture.this.move(TravisDofs.HEAD_MOTOR, -0.6f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                Gesture.this.move(TravisDofs.NECKUD_MOTOR, 0.4f, beats / 2.0f);
                this.nodStarted = true;
                Gesture.handler.postDelayed(Gesture.this.nodYes, (long) ((int) ((beats / 2.0f) * Gesture.this.beatDuration)));
            }
        };
        this.nodYes.run();
    }

    public void hiphopNod(final float beats) {
        handler.removeCallbacks(this.nodYes);
        this.nodYes = new Runnable() {
            boolean nodStarted = false;

            public void run() {
                if (this.nodStarted) {
                    Gesture.this.move(TravisDofs.HEAD_MOTOR, 0.8f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    Gesture.this.move(TravisDofs.NECKUD_MOTOR, 0.0f, beats / 2.0f);
                    return;
                }
                Gesture.this.move(TravisDofs.HEAD_MOTOR, -0.8f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                Gesture.this.move(TravisDofs.NECKUD_MOTOR, 0.4f, beats / 2.0f);
                this.nodStarted = true;
                Gesture.handler.postDelayed(Gesture.this.nodYes, (long) ((int) ((beats / 2.0f) * Gesture.this.beatDuration)));
            }
        };
        this.nodYes.run();
    }

    public void hiphopNodHigh(final float beats) {
        handler.removeCallbacks(this.hiphopNodHigh);
        this.hiphopNodHigh = new Runnable() {
            boolean nodStarted = false;

            public void run() {
                if (this.nodStarted) {
                    Gesture.this.move(TravisDofs.HEAD_MOTOR, 0.8f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    Gesture.this.move(TravisDofs.NECKUD_MOTOR, 0.6f, beats / 2.0f);
                    return;
                }
                Gesture.this.move(TravisDofs.HEAD_MOTOR, -0.8f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                Gesture.this.move(TravisDofs.NECKUD_MOTOR, 1.0f, beats / 2.0f);
                this.nodStarted = true;
                Gesture.handler.postDelayed(Gesture.this.hiphopNodHigh, (long) ((int) ((beats / 2.0f) * Gesture.this.beatDuration)));
            }
        };
        this.hiphopNodHigh.run();
    }

    public void hiphopNodLow(final float beats) {
        handler.removeCallbacks(this.hiphopNodLow);
        this.hiphopNodLow = new Runnable() {
            boolean nodStarted = false;

            public void run() {
                if (this.nodStarted) {
                    Gesture.this.move(TravisDofs.HEAD_MOTOR, 0.8f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    Gesture.this.move(TravisDofs.NECKUD_MOTOR, -0.5f, beats / 2.0f);
                    return;
                }
                Gesture.this.move(TravisDofs.HEAD_MOTOR, -0.8f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                Gesture.this.move(TravisDofs.NECKUD_MOTOR, -0.1f, beats / 2.0f);
                this.nodStarted = true;
                Gesture.handler.postDelayed(Gesture.this.hiphopNodLow, (long) ((int) ((beats / 2.0f) * Gesture.this.beatDuration)));
            }
        };
        this.hiphopNodLow.run();
    }

    public void headNod(final float beats) {
        handler.removeCallbacks(this.headNod);
        this.headNod = new Runnable() {
            boolean nodStarted = false;

            public void run() {
                if (this.nodStarted) {
                    Gesture.this.move(TravisDofs.HEAD_MOTOR, 0.0f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    return;
                }
                Gesture.this.move(TravisDofs.HEAD_MOTOR, -1.0f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                this.nodStarted = true;
                Gesture.handler.postDelayed(Gesture.this.headNod, (long) ((int) ((beats / 2.0f) * Gesture.this.beatDuration)));
            }
        };
        this.headNod.run();
    }

    public void reverseNodLow(float beats) {
        reverseNodYesDegree(beats, 1.0f, -0.3f);
    }

    public void reverseNodMid(float beats) {
        reverseNodYesDegree(beats, 1.0f, 0.0f);
    }

    public void reverseNodHigh(float beats) {
        reverseNodYesDegree(beats, 1.0f, 0.9f);
    }

    public void reverseNodYesDegree(final float beats, final float degree, final float height) {
        handler.removeCallbacks(this.reverseNodYes);
        this.reverseNodYes = new Runnable() {
            float center = 0.1f;
            boolean nodStarted = false;
            float range = Math.abs(Math.min(1.0f - this.center, -1.0f - this.center));

            public void run() {
                if (this.nodStarted) {
                    Gesture.this.move(TravisDofs.HEAD_MOTOR, this.center + (degree * this.range), beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    Gesture.this.move(TravisDofs.NECKUD_MOTOR, height - 0.1f, beats / 2.0f);
                    return;
                }
                Gesture.this.move(TravisDofs.HEAD_MOTOR, this.center - (degree * this.range), beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                Gesture.this.move(TravisDofs.NECKUD_MOTOR, height + 0.1f, beats / 2.0f);
                this.nodStarted = true;
                Gesture.handler.postDelayed(Gesture.this.nodYes, (long) ((int) ((beats / 2.0f) * Gesture.this.beatDuration)));
            }
        };
        this.reverseNodYes.run();
    }

    public void nodYesDegree(final float beats, final float degree, final float height) {
        handler.removeCallbacks(this.nodYes);
        this.nodYes = new Runnable() {
            float center = -0.1f;
            boolean nodStarted = false;
            float range = Math.abs(Math.min(1.0f - this.center, -1.0f - this.center));

            public void run() {
                if (this.nodStarted) {
                    Gesture.this.move(TravisDofs.HEAD_MOTOR, this.center + (degree * this.range), beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    Gesture.this.move(TravisDofs.NECKUD_MOTOR, height - 0.1f, beats / 2.0f);
                    return;
                }
                Gesture.this.move(TravisDofs.HEAD_MOTOR, this.center - (degree * this.range), beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                Gesture.this.move(TravisDofs.NECKUD_MOTOR, height + 0.1f, beats / 2.0f);
                this.nodStarted = true;
                Gesture.handler.postDelayed(Gesture.this.nodYes, (long) ((int) ((beats / 2.0f) * Gesture.this.beatDuration)));
            }
        };
        this.nodYes.run();
    }

    public void nodYesDegree(float beats, float center, float degree, float height, int repeats) {
        handler.removeCallbacks(this.nodYes);
        this.nodYes = new AnonymousClass20(center, degree, beats, repeats, height);
        this.nodYes.run();
    }

    private float getRange(float center) {
        float min = 1.0f - center;
        Math.min(Math.abs(1.0f - center), Math.abs(-1.0f - center));
        return min / 2.0f;
    }

    public void nodYesLeft(final float beats) {
        handler.removeCallbacks(this.nodYesLeft);
        move(TravisDofs.NECKRL_MOTOR, 0.8f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.9f), Float.valueOf(0.8f), Float.valueOf(0.6f), Float.valueOf(0.3f)}));
        move(TravisDofs.HAND_MOTOR, 0.4f, 0.7f * beats, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.9f), Float.valueOf(0.8f), Float.valueOf(0.6f), Float.valueOf(0.3f)}));
        this.nodYesLeft = new Runnable() {
            int nodCount = 0;

            public void run() {
                switch (this.nodCount) {
                    case 0:
                        Gesture.this.nodYes(beats / 3.0f);
                        this.nodCount++;
                        Gesture.handler.postDelayed(Gesture.this.nodYesLeft, (long) ((int) ((beats / 3.0f) * Gesture.this.beatDuration)));
                        return;
                    case 1:
                        Gesture.this.nodYes(beats / 3.0f);
                        this.nodCount++;
                        Gesture.handler.postDelayed(Gesture.this.nodYesLeft, (long) ((int) ((beats / 3.0f) * Gesture.this.beatDuration)));
                        return;
                    case 2:
                        Gesture.this.nodYes(beats / 3.0f);
                        this.nodCount++;
                        return;
                    default:
                        return;
                }
            }
        };
        this.nodYesLeft.run();
    }

    public void playfulNod(final float beats) {
        handler.removeCallbacks(this.playfulNod);
        this.playfulNod = new Runnable() {
            int stepCount = 0;

            public void run() {
                switch (this.stepCount) {
                    case 0:
                        Gesture.this.move(TravisDofs.NECKUD_MOTOR, -0.25f, beats * 0.4f);
                        Gesture.this.move(TravisDofs.HEAD_MOTOR, -1.0f, beats * 0.4f);
                        Gesture.this.move(TravisDofs.NECKRL_MOTOR, 0.2f, beats * 0.4f);
                        this.stepCount++;
                        Gesture.handler.postDelayed(Gesture.this.playfulNod, (long) ((int) ((beats * 0.55f) * Gesture.this.beatDuration)));
                        return;
                    case 1:
                        Gesture.this.nodYes(beats * 0.15f);
                        this.stepCount++;
                        Gesture.handler.postDelayed(Gesture.this.playfulNod, (long) ((int) ((beats * 0.15f) * Gesture.this.beatDuration)));
                        return;
                    case 2:
                        Gesture.this.move(TravisDofs.NECKRL_MOTOR, 0.0f, beats * 0.4f);
                        Gesture.this.nodYes(beats * 0.15f);
                        this.stepCount++;
                        Gesture.handler.postDelayed(Gesture.this.playfulNod, (long) ((int) ((beats * 0.15f) * Gesture.this.beatDuration)));
                        return;
                    case 3:
                        Gesture.this.nodYes(beats * 0.15f);
                        this.stepCount++;
                        return;
                    default:
                        return;
                }
            }
        };
        handler.post(this.playfulNod);
    }

    public void positiveResponse(final float beats) {
        handler.removeCallbacks(this.posResponse);
        this.posResponse = new Runnable() {
            boolean lookedAtPhone = false;

            public void run() {
                if (this.lookedAtPhone) {
                    Gesture.this.nodYesLeft(beats * 0.4f);
                    return;
                }
                Gesture.this.lookAtPhone(beats * 0.4f);
                this.lookedAtPhone = true;
                Gesture.handler.postDelayed(Gesture.this.posResponse, (long) ((int) ((beats * 0.6f) * Gesture.this.beatDuration)));
            }
        };
        this.posResponse.run();
    }

    public void negativeResponse(final float beats) {
        handler.removeCallbacks(this.negResponse);
        this.negResponse = new Runnable() {
            boolean lookedAtPhone = false;

            public void run() {
                if (this.lookedAtPhone) {
                    Gesture.this.shakeNoLeft(beats * 0.4f);
                    return;
                }
                Gesture.this.lookAtPhone(beats * 0.4f);
                this.lookedAtPhone = true;
                Gesture.handler.postDelayed(Gesture.this.negResponse, (long) ((int) ((beats * 0.6f) * Gesture.this.beatDuration)));
            }
        };
        this.negResponse.run();
    }

    public void shakeNo(final float beats) {
        handler.removeCallbacks(this.shakeNo);
        this.shakeNo = new Runnable() {
            boolean nodStarted = false;

            public void run() {
                if (this.nodStarted) {
                    Gesture.this.move(TravisDofs.HEAD_MOTOR, -0.5f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    Gesture.this.move(TravisDofs.NECKRL_MOTOR, 0.8f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    Gesture.this.move(TravisDofs.NECKUD_MOTOR, 0.3f, beats / 2.0f);
                    return;
                }
                Gesture.this.move(TravisDofs.HEAD_MOTOR, -0.4f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                Gesture.this.move(TravisDofs.NECKRL_MOTOR, -0.2f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                Gesture.this.move(TravisDofs.NECKUD_MOTOR, 0.4f, beats / 2.0f);
                this.nodStarted = true;
                Gesture.handler.postDelayed(Gesture.this.shakeNo, (long) ((int) ((beats / 2.0f) * Gesture.this.beatDuration)));
            }
        };
        this.shakeNo.run();
    }

    public void shakeNeck(final float beats) {
        handler.removeCallbacks(this.shakeNeck);
        this.shakeNeck = new Runnable() {
            boolean nodStarted = false;

            public void run() {
                if (this.nodStarted) {
                    Gesture.this.move(TravisDofs.NECKRL_MOTOR, 0.8f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    return;
                }
                Gesture.this.move(TravisDofs.NECKRL_MOTOR, -0.2f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                this.nodStarted = true;
                Gesture.handler.postDelayed(Gesture.this.shakeNeck, (long) ((int) ((beats / 2.0f) * Gesture.this.beatDuration)));
            }
        };
        this.shakeNeck.run();
    }

    public void shakeNeckCenter(final float beats) {
        handler.removeCallbacks(this.shakeNeckCenter);
        this.shakeNeckCenter = new Runnable() {
            boolean nodStarted = false;

            public void run() {
                if (this.nodStarted) {
                    Gesture.this.move(TravisDofs.NECKRL_MOTOR, 0.5f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    return;
                }
                Gesture.this.move(TravisDofs.NECKRL_MOTOR, -0.5f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                this.nodStarted = true;
                Gesture.handler.postDelayed(Gesture.this.shakeNeckCenter, (long) ((int) ((beats / 2.0f) * Gesture.this.beatDuration)));
            }
        };
        this.shakeNeckCenter.run();
    }

    public void shakeNeckCenterReverse(final float beats) {
        handler.removeCallbacks(this.shakeNeckCenterReverse);
        this.shakeNeckCenterReverse = new Runnable() {
            boolean nodStarted = false;

            public void run() {
                if (this.nodStarted) {
                    Gesture.this.move(TravisDofs.NECKRL_MOTOR, -0.5f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    return;
                }
                Gesture.this.move(TravisDofs.NECKRL_MOTOR, 0.5f, beats / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                this.nodStarted = true;
                Gesture.handler.postDelayed(Gesture.this.shakeNeckCenterReverse, (long) ((int) ((beats / 2.0f) * Gesture.this.beatDuration)));
            }
        };
        this.shakeNeckCenterReverse.run();
    }

    public void shakeNoDegree(float beats, float center, float range, float degree) {
        handler.removeCallbacks(this.shakeNo);
        final float f = center;
        final float f2 = range;
        final float f3 = degree;
        final float f4 = beats;
        this.shakeNo = new Runnable() {
            boolean nodStarted = false;

            public void run() {
                if (this.nodStarted) {
                    Gesture.this.move(TravisDofs.NECKRL_MOTOR, f + (f2 * f3), f4 / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                    return;
                }
                Gesture.this.move(TravisDofs.NECKRL_MOTOR, f - (f2 * f3), f4 / 2.0f, Arrays.asList(new Float[]{Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.6f), Float.valueOf(0.7f), Float.valueOf(0.8f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
                this.nodStarted = true;
                Gesture.handler.postDelayed(Gesture.this.shakeNo, (long) ((int) ((f4 / 2.0f) * Gesture.this.beatDuration)));
            }
        };
        this.shakeNo.run();
    }

    public void shakeNoDegree(float beats, float center, float degree, int repeats) {
        handler.removeCallbacks(this.shakeNo);
        this.shakeNo = new AnonymousClass30(center, degree, beats, repeats);
        this.shakeNo.run();
    }

    public void shakeNoLeft(final float beats) {
        handler.removeCallbacks(this.shakeNoLeft);
        move(TravisDofs.HAND_MOTOR, 0.4f, 0.7f * beats, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.9f), Float.valueOf(0.8f), Float.valueOf(0.6f), Float.valueOf(0.3f)}));
        this.shakeNoLeft = new Runnable() {
            int nodCount = 0;

            public void run() {
                switch (this.nodCount) {
                    case 0:
                        Gesture.this.shakeNo(beats / 3.0f);
                        this.nodCount++;
                        Gesture.handler.postDelayed(Gesture.this.shakeNoLeft, (long) ((int) ((beats / 3.0f) * Gesture.this.beatDuration)));
                        return;
                    case 1:
                        Gesture.this.shakeNo(beats / 3.0f);
                        this.nodCount++;
                        Gesture.handler.postDelayed(Gesture.this.shakeNoLeft, (long) ((int) ((beats / 3.0f) * Gesture.this.beatDuration)));
                        return;
                    case 2:
                        Gesture.this.shakeNo(beats / 3.0f);
                        this.nodCount++;
                        return;
                    default:
                        return;
                }
            }
        };
        this.shakeNoLeft.run();
    }

    public void bowDown(float beats) {
        move(TravisDofs.NECKUD_MOTOR, -1.0f, beats);
        move(TravisDofs.HEAD_MOTOR, 1.0f, beats);
    }

    public void bowUp(float beats) {
        move(TravisDofs.NECKUD_MOTOR, 1.0f, beats);
        move(TravisDofs.HEAD_MOTOR, -1.0f, beats);
    }

    public void bow(final float beats) {
        handler.removeCallbacks(this.bow);
        this.bow = new Runnable() {
            boolean isUp = false;

            public void run() {
                if (this.isUp) {
                    Gesture.this.bowDown(beats / 2.8f);
                    return;
                }
                Gesture.this.bowUp(beats / 2.8f);
                this.isUp = true;
                Gesture.handler.postDelayed(Gesture.this.bow, (long) ((int) ((beats / 2.0f) * Gesture.this.beatDuration)));
            }
        };
        this.bow.run();
    }

    public void emotionMove(float beats, float valence, float arousal, float stance, float rationality, float posArousal, float negArousal, float exaggeration, int decayType, int decayTime) {
        this.emotiveBeats = beats;
        this.valence = valence;
        this.arousal = arousal;
        this.stance = stance;
        this.volatility = rationality;
        this.decayType = decayType;
        this.posArousal = posArousal;
        this.negArousal = negArousal;
        this.decayTime = decayTime;
        this.exaggeration = exaggeration;
        this.emotionTrigger = true;
    }

    public void emotionUpdate(float beats) {
        this.emotiveBeats = beats;
    }

    public void newInstructions() {
        this.emotionVars.clearValues();
    }

    public void saveValues(String type) {
        this.emotionVars.save(type);
    }

    public void sadness(final float beats) {
        handler.removeCallbacks(this.sadGesture);
        this.sadGesture = new Runnable() {
            boolean inhale = false;
            int stepCount = 0;

            public void run() {
                switch (this.stepCount) {
                    case 0:
                        Gesture.this.move(TravisDofs.NECKUD_MOTOR, -1.0f, beats * 0.22f);
                        Gesture.this.move(TravisDofs.HEAD_MOTOR, 1.0f, beats * 0.22f);
                        Gesture.handler.postDelayed(Gesture.this.sadGesture, (long) ((int) ((beats * 0.25f) * Gesture.this.beatDuration)));
                        this.stepCount++;
                        return;
                    case 1:
                        Gesture.this.breath(2, beats * 0.75f, -0.9f);
                        return;
                    default:
                        return;
                }
            }
        };
        handler.post(this.sadGesture);
    }

    public void happiness(final float beats) {
        handler.removeCallbacks(this.happyGesture);
        move(TravisDofs.HEAD_MOTOR, -0.3f, beats / 4.0f);
        move(TravisDofs.HAND_MOTOR, -1.0f, beats / 3.0f, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.9f), Float.valueOf(0.8f), Float.valueOf(0.6f), Float.valueOf(0.4f), Float.valueOf(0.2f), Float.valueOf(0.1f)}));
        this.happyGesture = new Runnable() {
            int nodCount = 0;

            public void run() {
                switch (this.nodCount) {
                    case 0:
                        Gesture.this.nodYesDegree(beats * 0.3f, 1.0f, 0.5f);
                        this.nodCount++;
                        Gesture.handler.postDelayed(Gesture.this.happyGesture, (long) ((int) ((beats * 0.3f) * Gesture.this.beatDuration)));
                        return;
                    case 1:
                        Gesture.this.nodYesDegree(beats * 0.22f, 0.8f, 0.7f);
                        Gesture.this.move(TravisDofs.HAND_MOTOR, 0.4f, beats / 3.0f, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.9f), Float.valueOf(0.8f), Float.valueOf(0.6f)}));
                        this.nodCount++;
                        Gesture.handler.postDelayed(Gesture.this.happyGesture, (long) ((int) ((beats * 0.22f) * Gesture.this.beatDuration)));
                        return;
                    case 2:
                        Gesture.this.nodYesDegree(beats * 0.17f, 0.6f, 0.7f);
                        this.nodCount++;
                        Gesture.handler.postDelayed(Gesture.this.happyGesture, (long) ((int) ((beats * 0.17f) * Gesture.this.beatDuration)));
                        return;
                    case 3:
                        Gesture.this.nodYesDegree(beats * 0.1f, 0.5f, 0.8f);
                        this.nodCount++;
                        Gesture.handler.postDelayed(Gesture.this.happyGesture, (long) ((int) ((beats * 0.1f) * Gesture.this.beatDuration)));
                        return;
                    case DecayTypes.EXPONENTIAL_DELAY /*4*/:
                        Gesture.this.nodYesDegree(beats * 0.07f, 0.3f, 0.8f);
                        this.nodCount++;
                        Gesture.handler.postDelayed(Gesture.this.happyGesture, (long) ((int) ((beats * 0.07f) * Gesture.this.beatDuration)));
                        return;
                    case DecayTypes.NO_DECAY /*5*/:
                        Gesture.this.nodYesDegree(beats * 0.07f, 0.3f, 0.8f);
                        this.nodCount++;
                        Gesture.handler.postDelayed(Gesture.this.happyGesture, (long) ((int) ((beats * 0.07f) * Gesture.this.beatDuration)));
                        return;
                    case DecayTypes.RAPID_DECAY /*6*/:
                        Gesture.this.nodYesDegree(beats * 0.07f, 0.3f, 0.8f);
                        this.nodCount++;
                        Gesture.handler.postDelayed(Gesture.this.happyGesture, (long) ((int) ((beats * 0.07f) * Gesture.this.beatDuration)));
                        return;
                    case DecayTypes.SUPER_RAPID_DECAY /*7*/:
                        Gesture.this.nodYesDegree(beats * 0.22f, 0.7f, 0.7f);
                        this.nodCount++;
                        Gesture.handler.postDelayed(Gesture.this.happyGesture, (long) ((int) ((beats * 0.22f) * Gesture.this.beatDuration)));
                        return;
                    default:
                        return;
                }
            }
        };
        handler.postDelayed(this.happyGesture, (long) ((int) ((beats / 4.0f) * this.beatDuration)));
    }

    public void laughter(final float beats) {
        handler.removeCallbacks(this.laughGesture);
        move(TravisDofs.HEAD_MOTOR, -0.3f, beats / 4.0f);
        move(TravisDofs.HAND_MOTOR, -1.0f, beats / 3.0f, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.9f), Float.valueOf(0.8f), Float.valueOf(0.6f), Float.valueOf(0.4f), Float.valueOf(0.2f), Float.valueOf(0.1f)}));
        this.laughGesture = new Runnable() {
            int nodCount = 0;

            public void run() {
                switch (this.nodCount) {
                    case 0:
                        Gesture.this.nodYesDegree(beats * 0.3f, 1.0f, 0.5f);
                        this.nodCount++;
                        Gesture.handler.postDelayed(Gesture.this.laughGesture, (long) ((int) ((((double) beats) * 0.3d) * ((double) Gesture.this.beatDuration))));
                        return;
                    case 1:
                        Gesture.this.nodYesDegree(beats * 0.22f, 0.8f, 0.7f);
                        this.nodCount++;
                        Gesture.handler.postDelayed(Gesture.this.laughGesture, (long) ((int) ((((double) beats) * 0.22d) * ((double) Gesture.this.beatDuration))));
                        return;
                    case 2:
                        Gesture.this.move(TravisDofs.HAND_MOTOR, 0.5f, beats / 3.0f, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.9f), Float.valueOf(0.8f), Float.valueOf(0.6f), Float.valueOf(0.4f), Float.valueOf(0.2f), Float.valueOf(0.1f)}));
                        Gesture.this.nodYesDegree(beats * 0.17f, 0.6f, 0.7f);
                        this.nodCount++;
                        Gesture.handler.postDelayed(Gesture.this.laughGesture, (long) ((int) ((((double) beats) * 0.17d) * ((double) Gesture.this.beatDuration))));
                        return;
                    case 3:
                        Gesture.this.nodYesDegree(beats * 0.1f, 0.5f, 0.7f);
                        this.nodCount++;
                        Gesture.handler.postDelayed(Gesture.this.laughGesture, (long) ((int) ((((double) beats) * 0.1d) * ((double) Gesture.this.beatDuration))));
                        return;
                    case DecayTypes.EXPONENTIAL_DELAY /*4*/:
                        Gesture.this.nodYesDegree(beats * 0.07f, 0.3f, 0.8f);
                        this.nodCount++;
                        Gesture.handler.postDelayed(Gesture.this.laughGesture, (long) ((int) ((((double) beats) * 0.07d) * ((double) Gesture.this.beatDuration))));
                        return;
                    case DecayTypes.NO_DECAY /*5*/:
                        Gesture.this.nodYesDegree(beats * 0.07f, 0.3f, 0.8f);
                        this.nodCount++;
                        Gesture.handler.postDelayed(Gesture.this.laughGesture, (long) ((int) ((((double) beats) * 0.07d) * ((double) Gesture.this.beatDuration))));
                        return;
                    case DecayTypes.RAPID_DECAY /*6*/:
                        Gesture.this.nodYesDegree(beats * 0.07f, 0.3f, 0.9f);
                        this.nodCount++;
                        Gesture.handler.postDelayed(Gesture.this.laughGesture, (long) ((int) ((((double) beats) * 0.07d) * ((double) Gesture.this.beatDuration))));
                        return;
                    default:
                        return;
                }
            }
        };
        handler.postDelayed(this.laughGesture, (long) ((int) ((beats / 4.0f) * this.beatDuration)));
    }

    public void startled(final float beats) {
        handler.removeCallbacks(this.startledGesture);
        tap(beats / 3.0f);
        moveMaxSpeed(TravisDofs.NECKUD_MOTOR, 1.0f);
        moveMaxSpeed(TravisDofs.HEAD_MOTOR, -1.0f);
        this.startledGesture = new Runnable() {
            int nodCount = 0;

            public void run() {
                switch (this.nodCount) {
                    case 0:
                        Gesture.this.shakeNoDegree(beats / 3.0f, 0.0f, 1.0f, 0.1f);
                        this.nodCount++;
                        Gesture.handler.postDelayed(Gesture.this.startledGesture, (long) ((int) ((beats / 3.0f) * Gesture.this.beatDuration)));
                        return;
                    case 1:
                        Gesture.this.moveMaxSpeed(TravisDofs.HEAD_MOTOR, 0.0f);
                        Gesture.this.shakeNoDegree(beats / 3.0f, 0.0f, 1.0f, 0.1f);
                        Gesture.this.move(TravisDofs.HAND_MOTOR, 0.4f, beats / 3.0f, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.9f), Float.valueOf(0.8f), Float.valueOf(0.6f)}));
                        this.nodCount++;
                        Gesture.handler.postDelayed(Gesture.this.startledGesture, (long) ((int) ((beats / 3.0f) * Gesture.this.beatDuration)));
                        return;
                    case 2:
                        Gesture.this.shakeNoDegree(beats / 3.0f, 0.0f, 1.0f, 0.1f);
                        this.nodCount++;
                        return;
                    default:
                        return;
                }
            }
        };
        handler.post(this.startledGesture);
    }

    public void surprise(float beats) {
        handler.removeCallbacks(this.surpriseGesture);
        tap(beats / 3.0f);
        this.surpriseGesture = new Runnable() {
            boolean started = false;

            public void run() {
                Gesture.this.moveMaxSpeed(TravisDofs.NECKRL_MOTOR, -0.9f);
                Gesture.this.moveMaxSpeed(TravisDofs.HEAD_MOTOR, -1.0f);
            }
        };
        handler.post(this.surpriseGesture);
    }

    public void surprise2(final float beats) {
        handler.removeCallbacks(this.surpriseGesture);
        tap(beats / 3.0f);
        this.surpriseGesture = new Runnable() {
            boolean started = false;

            public void run() {
                if (this.started) {
                    Gesture.this.move(TravisDofs.NECKUD_MOTOR, 0.1f, beats / 2.0f);
                    Gesture.this.moveMaxSpeed(TravisDofs.HEAD_MOTOR, -1.0f);
                    return;
                }
                Gesture.this.move(TravisDofs.NECKUD_MOTOR, 1.0f, beats / 2.0f);
                Gesture.this.moveMaxSpeed(TravisDofs.HEAD_MOTOR, 1.0f);
                this.started = true;
                Gesture.handler.postDelayed(Gesture.this.surpriseGesture, (long) ((int) ((beats / 2.0f) * Gesture.this.beatDuration)));
            }
        };
        handler.post(this.surpriseGesture);
    }

    public void disgust(final float beats) {
        handler.removeCallbacks(this.disgustGesture);
        this.disgustGesture = new Runnable() {
            int stepCount = 0;

            public void run() {
                switch (this.stepCount) {
                    case 0:
                        Gesture.this.move(TravisDofs.NECKUD_MOTOR, -0.4f, beats * 0.4f);
                        Gesture.this.move(TravisDofs.HEAD_MOTOR, -1.0f, beats * 0.4f);
                        Gesture.this.move(TravisDofs.NECKRL_MOTOR, 0.35f, beats * 0.4f);
                        this.stepCount++;
                        Gesture.handler.postDelayed(Gesture.this.disgustGesture, (long) ((int) ((beats * 0.7f) * Gesture.this.beatDuration)));
                        return;
                    case 1:
                        Gesture.this.move(TravisDofs.NECKUD_MOTOR, 0.4f, beats * 0.3f);
                        Gesture.this.move(TravisDofs.HEAD_MOTOR, 1.0f, beats * 0.3f);
                        Gesture.this.shakeNoDegree(beats * 0.1f, 0.2f, 0.8f, 0.2f);
                        this.stepCount++;
                        Gesture.handler.postDelayed(Gesture.this.disgustGesture, (long) ((int) ((beats * 0.1f) * Gesture.this.beatDuration)));
                        return;
                    case 2:
                        Gesture.this.shakeNoDegree(beats * 0.1f, 0.2f, 0.8f, 0.2f);
                        this.stepCount++;
                        Gesture.handler.postDelayed(Gesture.this.disgustGesture, (long) ((int) ((beats * 0.1f) * Gesture.this.beatDuration)));
                        return;
                    case 3:
                        Gesture.this.shakeNoDegree(beats * 0.1f, 0.2f, 0.8f, 0.2f);
                        this.stepCount++;
                        return;
                    default:
                        return;
                }
            }
        };
        handler.post(this.disgustGesture);
    }

    public void fear(final float beats) {
        handler.removeCallbacks(this.fearGesture);
        this.fearGesture = new Runnable() {
            int stepCount = 0;

            public void run() {
                switch (this.stepCount) {
                    case 0:
                        Gesture.this.moveMaxSpeed(TravisDofs.NECKUD_MOTOR, 1.0f);
                        Gesture.this.moveMaxSpeed(TravisDofs.HEAD_MOTOR, 1.0f);
                        Gesture.this.shakeNoDegree(beats / 3.0f, 0.1f, 1.0f, 0.08f);
                        this.stepCount++;
                        Gesture.handler.postDelayed(Gesture.this.fearGesture, (long) ((int) ((beats * 0.33f) * Gesture.this.beatDuration)));
                        return;
                    case 1:
                        Gesture.this.shakeNoDegree(beats / 3.0f, 0.1f, 1.0f, 0.08f);
                        this.stepCount++;
                        Gesture.handler.postDelayed(Gesture.this.fearGesture, (long) ((int) ((beats * 0.33f) * Gesture.this.beatDuration)));
                        return;
                    case 2:
                        Gesture.this.shakeNoDegree(beats / 3.0f, 0.1f, 1.0f, 0.08f);
                        this.stepCount++;
                        return;
                    default:
                        return;
                }
            }
        };
        handler.post(this.fearGesture);
    }

    public void fear2(final float beats) {
        handler.removeCallbacks(this.fearGesture);
        this.fearGesture = new Runnable() {
            int stepCount = 0;

            public void run() {
                switch (this.stepCount) {
                    case 0:
                        Gesture.this.move(TravisDofs.NECKUD_MOTOR, -1.0f, beats * 0.4f);
                        Gesture.this.move(TravisDofs.HEAD_MOTOR, -1.0f, beats * 0.4f);
                        Gesture.this.move(TravisDofs.HAND_MOTOR, -1.0f, beats * 0.4f, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.9f), Float.valueOf(0.8f), Float.valueOf(0.6f), Float.valueOf(0.4f), Float.valueOf(0.2f), Float.valueOf(0.1f)}));
                        Gesture.this.shakeNoDegree(beats * 0.1f, 0.0f, 1.0f, 0.1f);
                        this.stepCount++;
                        Gesture.handler.postDelayed(Gesture.this.fearGesture, (long) ((int) ((beats * 0.1f) * Gesture.this.beatDuration)));
                        return;
                    case 1:
                        Gesture.this.shakeNoDegree(beats * 0.1f, 0.0f, 1.0f, 0.1f);
                        this.stepCount++;
                        Gesture.handler.postDelayed(Gesture.this.fearGesture, (long) ((int) ((beats * 0.1f) * Gesture.this.beatDuration)));
                        return;
                    case 2:
                        Gesture.this.shakeNoDegree(beats * 0.1f, 0.0f, 1.0f, 0.1f);
                        this.stepCount++;
                        Gesture.handler.postDelayed(Gesture.this.fearGesture, (long) ((int) ((beats * 0.1f) * Gesture.this.beatDuration)));
                        return;
                    case 3:
                        Gesture.this.shakeNoDegree(beats * 0.1f, 0.0f, 1.0f, 0.1f);
                        Gesture.handler.postDelayed(Gesture.this.fearGesture, (long) ((int) ((beats * 0.1f) * Gesture.this.beatDuration)));
                        this.stepCount++;
                        return;
                    case DecayTypes.EXPONENTIAL_DELAY /*4*/:
                        Gesture.this.shakeNoDegree(beats * 0.1f, 0.0f, 1.0f, 0.1f);
                        this.stepCount++;
                        return;
                    default:
                        return;
                }
            }
        };
        handler.post(this.fearGesture);
    }

    public void anger(final float beats) {
        handler.removeCallbacks(this.angryGesture);
        this.angryGesture = new Runnable() {
            int repeatCount = 0;
            int stepCount = 0;

            public void run() {
                switch (this.stepCount) {
                    case 0:
                        Gesture.this.move(TravisDofs.HEAD_MOTOR, 0.5f, beats * 0.15f);
                        Gesture.this.move(TravisDofs.NECKUD_MOTOR, -0.8f, beats * 0.4f);
                        this.stepCount++;
                        Gesture.handler.postDelayed(Gesture.this.angryGesture, (long) ((int) ((beats * 0.15f) * Gesture.this.beatDuration)));
                        return;
                    case 1:
                        Gesture.this.moveMaxSpeed(TravisDofs.HEAD_MOTOR, -1.0f);
                        this.stepCount++;
                        return;
                    case 2:
                        Gesture.this.shakeNoDegree(beats * 0.18f, 0.0f, 1.0f, 0.1f);
                        Gesture.this.nodYesDegree(beats * 0.36f, 1.0f, -0.8f);
                        this.stepCount++;
                        Gesture.handler.postDelayed(Gesture.this.angryGesture, (long) ((int) ((beats * 0.18f) * Gesture.this.beatDuration)));
                        return;
                    case 3:
                        Gesture.this.shakeNoDegree(beats * 0.18f, 0.0f, 1.0f, 0.15f);
                        return;
                    default:
                        return;
                }
            }
        };
        handler.post(this.angryGesture);
    }

    public void anger2(final float beats) {
        handler.removeCallbacks(this.angryGesture);
        this.angryGesture = new Runnable() {
            int repeatCount = 0;
            int stepCount = 0;

            public void run() {
                if (this.repeatCount < 2) {
                    switch (this.stepCount) {
                        case 0:
                            Gesture.this.tap(beats * 0.13f);
                            Gesture.this.move(TravisDofs.NECKUD_MOTOR, -1.0f, beats * 0.2f);
                            Gesture.this.move(TravisDofs.HEAD_MOTOR, 1.0f, beats * 0.2f);
                            Gesture.this.shakeNoDegree(beats * 0.13f, 0.0f, 1.0f, 0.6f);
                            Gesture.this.move(TravisDofs.HAND_MOTOR, -0.3f, beats * 0.13f);
                            this.stepCount++;
                            Gesture.handler.postDelayed(Gesture.this.angryGesture, (long) ((int) ((beats * 0.13f) * Gesture.this.beatDuration)));
                            return;
                        case 1:
                            Gesture.this.shakeNoDegree(beats * 0.14f, 0.0f, 1.0f, 0.2f);
                            Gesture.this.move(TravisDofs.HAND_MOTOR, 0.0f, beats * 0.07f);
                            this.stepCount++;
                            Gesture.handler.postDelayed(Gesture.this.angryGesture, (long) ((int) ((beats * 0.07f) * Gesture.this.beatDuration)));
                            return;
                        case 2:
                            Gesture.this.tap(beats * 0.14f);
                            Gesture.this.shakeNoDegree(beats * 0.07f, 0.0f, 1.0f, 0.2f);
                            Gesture.this.move(TravisDofs.HAND_MOTOR, -0.3f, beats * 0.07f);
                            this.stepCount++;
                            Gesture.handler.postDelayed(Gesture.this.angryGesture, (long) ((int) ((beats * 0.07f) * Gesture.this.beatDuration)));
                            return;
                        case 3:
                            Gesture.this.move(TravisDofs.HEAD_MOTOR, 1.0f, beats * 0.2f);
                            Gesture.this.move(TravisDofs.HAND_MOTOR, 0.0f, beats * 0.07f);
                            Gesture.this.tap(beats * 0.1f);
                            Gesture.this.shakeNoDegree(beats * 0.07f, 0.0f, 1.0f, 0.2f);
                            this.stepCount++;
                            Gesture.handler.postDelayed(Gesture.this.angryGesture, (long) ((int) ((beats * 0.07f) * Gesture.this.beatDuration)));
                            return;
                        case DecayTypes.EXPONENTIAL_DELAY /*4*/:
                            Gesture.this.move(TravisDofs.HAND_MOTOR, 0.0f, beats * 0.16f);
                            Gesture.this.move(TravisDofs.NECKUD_MOTOR, -0.8f, beats * 0.2f);
                            Gesture.this.tap(beats * 0.16f);
                            Gesture.this.shakeNoDegree(beats * 0.16f, 0.0f, 1.0f, 1.0f);
                            Gesture.handler.postDelayed(Gesture.this.angryGesture, (long) ((int) ((beats * 0.16f) * Gesture.this.beatDuration)));
                            this.stepCount++;
                            return;
                        case DecayTypes.NO_DECAY /*5*/:
                            Gesture.this.tap(beats * 0.13f);
                            Gesture.this.move(TravisDofs.NECKUD_MOTOR, -1.0f, beats * 0.2f);
                            Gesture.this.shakeNoDegree(beats * 0.13f, 0.0f, 1.0f, 0.6f);
                            Gesture.this.move(TravisDofs.HAND_MOTOR, -0.3f, beats * 0.13f);
                            this.stepCount++;
                            Gesture.handler.postDelayed(Gesture.this.angryGesture, (long) ((int) ((beats * 0.13f) * Gesture.this.beatDuration)));
                            break;
                        case DecayTypes.RAPID_DECAY /*6*/:
                            break;
                        case DecayTypes.SUPER_RAPID_DECAY /*7*/:
                            break;
                        case 8:
                            Gesture.this.tap(beats * 0.14f);
                            Gesture.this.shakeNoDegree(beats * 0.27f, 0.0f, 1.0f, 0.6f);
                            Gesture.this.move(TravisDofs.HAND_MOTOR, 0.0f, beats * 0.07f);
                            this.stepCount++;
                            this.repeatCount++;
                            return;
                        default:
                            return;
                    }
                    Gesture.this.tap(beats * 0.13f);
                    Gesture.this.move(TravisDofs.NECKUD_MOTOR, -0.8f, beats * 0.2f);
                    Gesture.this.shakeNoDegree(beats * 0.13f, 0.0f, 1.0f, 0.6f);
                    Gesture.this.move(TravisDofs.HAND_MOTOR, 0.0f, beats * 0.13f);
                    this.stepCount++;
                    Gesture.handler.postDelayed(Gesture.this.angryGesture, (long) ((int) ((beats * 0.13f) * Gesture.this.beatDuration)));
                    Gesture.this.shakeNoDegree(beats * 0.14f, 0.0f, 1.0f, 0.6f);
                    Gesture.this.move(TravisDofs.HAND_MOTOR, -0.3f, beats * 0.07f);
                    this.stepCount++;
                    Gesture.handler.postDelayed(Gesture.this.angryGesture, (long) ((int) ((beats * 0.14f) * Gesture.this.beatDuration)));
                }
            }
        };
        handler.post(this.angryGesture);
    }

    public void angryUp(final float beats) {
        handler.removeCallbacks(this.angryUp);
        this.angryUp = new Runnable() {
            int repeatCount = 0;
            int stepCount = 0;

            public void run() {
                if (this.repeatCount < 2) {
                    switch (this.stepCount) {
                        case 0:
                            Gesture.this.tap(beats * 0.13f);
                            Gesture.this.move(TravisDofs.NECKUD_MOTOR, 1.0f, beats * 0.2f);
                            Gesture.this.move(TravisDofs.HEAD_MOTOR, 1.0f, beats * 0.2f);
                            Gesture.this.shakeNoDegree(beats * 0.13f, 0.0f, 1.0f, 0.6f);
                            Gesture.this.move(TravisDofs.HAND_MOTOR, -0.3f, beats * 0.13f);
                            this.stepCount++;
                            Gesture.handler.postDelayed(Gesture.this.angryUp, (long) ((int) ((beats * 0.13f) * Gesture.this.beatDuration)));
                            return;
                        case 1:
                            Gesture.this.shakeNoDegree(beats * 0.14f, 0.0f, 1.0f, 0.2f);
                            Gesture.this.move(TravisDofs.HAND_MOTOR, 0.0f, beats * 0.07f);
                            this.stepCount++;
                            Gesture.handler.postDelayed(Gesture.this.angryUp, (long) ((int) ((beats * 0.07f) * Gesture.this.beatDuration)));
                            return;
                        case 2:
                            Gesture.this.tap(beats * 0.14f);
                            Gesture.this.shakeNoDegree(beats * 0.07f, 0.0f, 1.0f, 0.2f);
                            Gesture.this.move(TravisDofs.HAND_MOTOR, -0.3f, beats * 0.07f);
                            this.stepCount++;
                            Gesture.handler.postDelayed(Gesture.this.angryUp, (long) ((int) ((beats * 0.07f) * Gesture.this.beatDuration)));
                            return;
                        case 3:
                            Gesture.this.move(TravisDofs.HEAD_MOTOR, 1.0f, beats * 0.2f);
                            Gesture.this.move(TravisDofs.HAND_MOTOR, 0.0f, beats * 0.07f);
                            Gesture.this.tap(beats * 0.1f);
                            Gesture.this.shakeNoDegree(beats * 0.07f, 0.0f, 1.0f, 0.2f);
                            this.stepCount++;
                            Gesture.handler.postDelayed(Gesture.this.angryUp, (long) ((int) ((beats * 0.07f) * Gesture.this.beatDuration)));
                            return;
                        case DecayTypes.EXPONENTIAL_DELAY /*4*/:
                            Gesture.this.move(TravisDofs.HAND_MOTOR, 0.0f, beats * 0.16f);
                            Gesture.this.move(TravisDofs.NECKUD_MOTOR, 0.8f, beats * 0.2f);
                            Gesture.this.tap(beats * 0.16f);
                            Gesture.this.shakeNoDegree(beats * 0.16f, 0.0f, 1.0f, 1.0f);
                            Gesture.handler.postDelayed(Gesture.this.angryUp, (long) ((int) ((beats * 0.16f) * Gesture.this.beatDuration)));
                            this.stepCount++;
                            return;
                        case DecayTypes.NO_DECAY /*5*/:
                            Gesture.this.tap(beats * 0.13f);
                            Gesture.this.move(TravisDofs.NECKUD_MOTOR, 1.0f, beats * 0.2f);
                            Gesture.this.shakeNoDegree(beats * 0.13f, 0.0f, 1.0f, 0.6f);
                            Gesture.this.move(TravisDofs.HAND_MOTOR, -0.3f, beats * 0.13f);
                            this.stepCount++;
                            Gesture.handler.postDelayed(Gesture.this.angryUp, (long) ((int) ((beats * 0.13f) * Gesture.this.beatDuration)));
                            break;
                        case DecayTypes.RAPID_DECAY /*6*/:
                            break;
                        case DecayTypes.SUPER_RAPID_DECAY /*7*/:
                            break;
                        case 8:
                            Gesture.this.tap(beats * 0.14f);
                            Gesture.this.shakeNoDegree(beats * 0.27f, 0.0f, 1.0f, 0.6f);
                            Gesture.this.move(TravisDofs.HAND_MOTOR, 0.0f, beats * 0.07f);
                            this.stepCount++;
                            this.repeatCount++;
                            return;
                        default:
                            return;
                    }
                    Gesture.this.tap(beats * 0.13f);
                    Gesture.this.move(TravisDofs.NECKUD_MOTOR, 0.8f, beats * 0.2f);
                    Gesture.this.shakeNoDegree(beats * 0.13f, 0.0f, 1.0f, 0.6f);
                    Gesture.this.move(TravisDofs.HAND_MOTOR, 0.0f, beats * 0.13f);
                    this.stepCount++;
                    Gesture.handler.postDelayed(Gesture.this.angryUp, (long) ((int) ((beats * 0.13f) * Gesture.this.beatDuration)));
                    Gesture.this.shakeNoDegree(beats * 0.14f, 0.0f, 1.0f, 0.6f);
                    Gesture.this.move(TravisDofs.HAND_MOTOR, -0.3f, beats * 0.07f);
                    this.stepCount++;
                    Gesture.handler.postDelayed(Gesture.this.angryUp, (long) ((int) ((beats * 0.14f) * Gesture.this.beatDuration)));
                }
            }
        };
        handler.post(this.angryUp);
    }

    public void breathGesture(final float beats, final float neckUDPos) {
        handler.removeCallbacks(this.breathGesture);
        this.breathGesture = new Runnable() {
            boolean inhale = false;

            public void run() {
                if (this.inhale) {
                    Gesture.this.move(TravisDofs.NECKUD_MOTOR, neckUDPos, beats * 0.35f);
                    Gesture.this.move(TravisDofs.HEAD_MOTOR, 0.4f, beats * 0.35f, Arrays.asList(new Float[]{Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(0.8f), Float.valueOf(0.6f), Float.valueOf(0.4f), Float.valueOf(0.3f)}));
                    return;
                }
                Gesture.this.move(TravisDofs.NECKUD_MOTOR, neckUDPos, beats * 0.65f);
                Gesture.this.move(TravisDofs.HEAD_MOTOR, -0.3f, beats * 0.65f, Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(0.8f), Float.valueOf(0.6f), Float.valueOf(0.4f), Float.valueOf(0.2f), Float.valueOf(0.1f)}));
                this.inhale = true;
                Gesture.handler.postDelayed(Gesture.this.breathGesture, (long) ((int) ((beats * 0.6f) * Gesture.this.beatDuration)));
            }
        };
        handler.post(this.breathGesture);
    }

    public void breath(final int nBreaths, final float beats, final float neckUDPos) {
        handler.removeCallbacks(this.neutralBreathing);
        this.neutralBreathing = new Runnable() {
            int breathCount = 0;

            public void run() {
                Gesture.this.breathGesture(beats / ((float) nBreaths), neckUDPos);
                this.breathCount++;
                if (this.breathCount < nBreaths) {
                    Gesture.handler.postDelayed(Gesture.this.neutralBreathing, (long) ((int) ((beats / ((float) nBreaths)) * Gesture.this.beatDuration)));
                }
            }
        };
        handler.post(this.neutralBreathing);
    }

    public void happyPose() {
        this.myMove.moveWithVel(TravisDofs.NECKRL_MOTOR, 0.0f, 0.4f);
        this.myMove.moveWithVel(TravisDofs.NECKUD_MOTOR, 0.8f, 0.3f);
        this.myMove.moveWithVel(TravisDofs.HEAD_MOTOR, -0.5f, 0.4f);
    }

    public void sadPose() {
        this.myMove.moveWithVel(TravisDofs.NECKRL_MOTOR, 0.0f, 0.4f);
        this.myMove.moveWithVel(TravisDofs.NECKUD_MOTOR, -1.0f, 0.3f);
        this.myMove.moveWithVel(TravisDofs.HEAD_MOTOR, 1.0f, 0.4f);
    }

    public void fearPose() {
        this.myMove.moveWithVel(TravisDofs.NECKRL_MOTOR, 0.2f, 0.4f);
        this.myMove.moveWithVel(TravisDofs.NECKUD_MOTOR, 1.0f, 0.3f);
        this.myMove.moveWithVel(TravisDofs.HEAD_MOTOR, 1.0f, 0.4f);
    }

    public void disgustPose() {
        this.myMove.moveWithVel(TravisDofs.NECKRL_MOTOR, 0.35f, 0.4f);
        this.myMove.moveWithVel(TravisDofs.NECKUD_MOTOR, -0.45f, 0.3f);
        this.myMove.moveWithVel(TravisDofs.HEAD_MOTOR, -1.0f, 0.4f);
    }

    public void surprisePose() {
        this.myMove.moveWithVel(TravisDofs.NECKRL_MOTOR, -0.9f, 0.4f);
        this.myMove.moveWithVel(TravisDofs.NECKUD_MOTOR, 0.3f, 0.3f);
        this.myMove.moveWithVel(TravisDofs.HEAD_MOTOR, -1.0f, 0.4f);
    }

    public void angryPose() {
        this.myMove.moveWithVel(TravisDofs.NECKRL_MOTOR, 0.4f, 0.4f);
        this.myMove.moveWithVel(TravisDofs.NECKUD_MOTOR, -0.9f, 0.3f);
        this.myMove.moveWithVel(TravisDofs.HEAD_MOTOR, 0.8f, 0.4f);
    }
}
