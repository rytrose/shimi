package mr1.robots.travis.moves.gestures.emotions;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import mr1.robots.travis.TravisDofs;
import mr1.robots.travis.moves.gestures.TravisGesture;

public class TravisEmotion extends TravisGesture implements TravisDofs {
    final String NEG_EXAGGERATION = "negExaggeration";
    final String POS_EXAGGERATION = "posExaggeration";
    final String PRIMARY_AROUSAL = "primary arousal";
    final String SECONDARY_NEG_AROUSAL = "negativeArousal";
    final String SECONDARY_POS_AROUSAL = "positiveArousal";
    final String STANCE = "stance";
    final String VALENCE = "primary valence";
    final String VOLATILITY = "volatility";
    public float alertness;
    public float arousal;
    private List<Float> currentArousal = new ArrayList();
    private List<Float> currentExaggeration = new ArrayList();
    private List<Float> currentNegArousal = new ArrayList();
    private List<Float> currentPosArousal = new ArrayList();
    private List<Float> currentStance = new ArrayList();
    private List<Float> currentValence = new ArrayList();
    private List<Float> currentVolatility = new ArrayList();
    private long dancingStartTime;
    private float decayArousal;
    private float decayExaggeration;
    private float decayNegArousal;
    private float decayPosArousal;
    private float decayPrimaryValence;
    private float decayStance;
    public float decayTime = 4500.0f;
    private float decayVolatility;
    private float degreeMovesRandom;
    public float degreeRangeHead;
    public float degreeRangeNeck;
    private float degreeRangeRandom;
    public float exaggeration;
    public Map<String, Float> homeostaticState = new HashMap();
    private float lengthOfBeat = 0.2f;
    public int negBeats;
    public int negMovesPerBeat;
    public float negativeArousal;
    public boolean nodYes;
    private int pointCount = 0;
    public int posBeats;
    public int posMovesPerBeat;
    public Map<String, Float> positions = new HashMap();
    public float positiveArousal;
    public float primaryValence;
    public boolean shakeNo;
    public float stance;
    public long startTime;
    public float volatility;

    public TravisEmotion() {
        this.homeostaticState.put("valence", Float.valueOf(0.1f));
        this.homeostaticState.put("arousal", Float.valueOf(-1.0f));
        this.homeostaticState.put("stance", Float.valueOf(1.0f));
        this.homeostaticState.put("volatility", Float.valueOf(0.08f));
        this.homeostaticState.put("positiveArousal", Float.valueOf(0.01f));
        this.homeostaticState.put("negativeArousal", Float.valueOf(0.0f));
        this.homeostaticState.put("exaggeration", Float.valueOf(0.2f));
        this.positions.put(TravisDofs.HAND_MOTOR, Float.valueOf(0.0f));
        this.positions.put(TravisDofs.NECKUD_MOTOR, Float.valueOf(0.0f));
        this.positions.put(TravisDofs.NECKRL_MOTOR, Float.valueOf(0.0f));
        this.positions.put(TravisDofs.HEAD_MOTOR, Float.valueOf(0.0f));
        this.positions.put(TravisDofs.LEG_MOTOR, Float.valueOf(0.0f));
    }

    public void calculateEmotionalGesture(float valence, float arousal, float stance, float rationale, float posArousal, float negArousal, float exaggeration) {
        this.primaryValence = valence;
        this.arousal = arousal;
        this.stance = stance;
        this.positiveArousal = posArousal;
        this.negativeArousal = negArousal;
        if (rationale == 0.0f) {
            rationale = 0.001f;
        }
        this.volatility = rationale;
        this.exaggeration = exaggeration;
    }

    public void calculatePosture(float valence, float stance, float posArousal, float negArousal) {
        if (posArousal > 0.0f) {
            this.nodYes = true;
        } else {
            this.nodYes = false;
        }
        if (negArousal > 0.0f) {
            this.shakeNo = true;
        } else {
            this.shakeNo = false;
        }
        this.positions.put(TravisDofs.NECKUD_MOTOR, Float.valueOf(valence));
        if (valence == 0.0f) {
            valence = 0.001f;
        }
        if (valence >= 0.0f) {
            this.positions.put(TravisDofs.HEAD_MOTOR, Float.valueOf(stance * (-valence)));
            return;
        }
        this.positions.put(TravisDofs.HEAD_MOTOR, Float.valueOf(stance * valence));
    }

    public void calculatePosBeats(float beatLength, float rationale, float posArousal) {
        float arousal;
        if (posArousal > 0.0f) {
            if (rationale == 0.0f) {
                rationale = 0.001f;
            }
            Random rand = new Random();
            float max = Math.min(1.0f, rationale + posArousal);
            float min = Math.max(0.0f, posArousal - rationale);
            float range = max - min;
            arousal = (2.0f * (((((rand.nextFloat() * range) + min) - posArousal) * ((float) Math.pow((double) ((((rand.nextFloat() * range) + min) - posArousal) / rationale), 2.0d))) + posArousal)) - 4.0f;
        } else {
            arousal = (2.0f * posArousal) - 4.0f;
        }
        if (arousal > -1.0f) {
            float beatEstimated;
            float exact = (((3.36f / 2.0f) * arousal) + (3.36f / 2.0f)) + 0.14f;
            rand = new Random();
            if (rand.nextFloat() < 0.0f) {
                beatEstimated = (float) (rand.nextInt(3) + 1);
            } else {
                beatEstimated = 1.0f / (exact * beatLength);
                if (beatEstimated >= 1.0f) {
                    this.posMovesPerBeat = 1;
                } else if (((double) beatEstimated) < 0.5d) {
                    this.posMovesPerBeat = 3;
                } else if (((double) beatEstimated) < 0.5d || ((double) beatEstimated) >= 0.75d) {
                    this.posMovesPerBeat = 1;
                } else {
                    this.posMovesPerBeat = 2;
                }
            }
            if (beatEstimated < 1.0f) {
                beatEstimated = 1.0f;
            }
            this.posBeats = (int) beatEstimated;
        }
    }

    public void calculateNegBeats(float beatLength, float rationale, float negArousal) {
        float arousal;
        if (negArousal > 0.0f) {
            if (rationale == 0.0f) {
                rationale = 0.001f;
            }
            Random rand = new Random();
            float max = Math.min(1.0f, rationale + negArousal);
            float min = Math.max(0.0f, negArousal - rationale);
            float range = max - min;
            arousal = (2.0f * (((((rand.nextFloat() * range) + min) - negArousal) * ((float) Math.pow((double) ((((rand.nextFloat() * range) + min) - negArousal) / rationale), 2.0d))) + negArousal)) - 4.0f;
        } else {
            arousal = (2.0f * negArousal) - 4.0f;
        }
        if (arousal > -1.0f) {
            float beatEstimated;
            float exact = (((3.39f / 2.0f) * arousal) + (3.39f / 2.0f)) + 0.11f;
            rand = new Random();
            if (arousal > 0.0f) {
                float f = arousal * rationale;
            }
            if (rand.nextFloat() < 0.0f) {
                beatEstimated = (float) (rand.nextInt(3) + 1);
            } else {
                beatEstimated = 1.0f / (exact * beatLength);
                if (beatEstimated >= 1.0f) {
                    this.negMovesPerBeat = 1;
                } else if (((double) beatEstimated) < 0.5d) {
                    this.negMovesPerBeat = 3;
                } else if (((double) beatEstimated) < 0.5d || ((double) beatEstimated) >= 0.75d) {
                    this.negMovesPerBeat = 1;
                } else {
                    this.negMovesPerBeat = 2;
                }
            }
            if (beatEstimated < 1.0f) {
                beatEstimated = 1.0f;
            }
            this.negBeats = (int) beatEstimated;
        }
    }

    public void calculateExaggeration(float exaggeration, float volatility) {
        if (volatility == 0.0f) {
            volatility = 0.001f;
        }
        exaggeration = (1.0f + exaggeration) / 2.0f;
        float max = Math.min(1.0f, volatility + exaggeration);
        float min = Math.max(0.0f, exaggeration - volatility);
        float range = max - min;
        Random rand = new Random();
        float exaggerationNeck = ((((rand.nextFloat() * range) + min) - exaggeration) * ((float) Math.pow((double) ((((rand.nextFloat() * range) + min) - exaggeration) / volatility), 2.0d))) + exaggeration;
        this.degreeRangeHead = ((((rand.nextFloat() * range) + min) - exaggeration) * ((float) Math.pow((double) ((((rand.nextFloat() * range) + min) - exaggeration) / volatility), 2.0d))) + exaggeration;
        this.degreeRangeNeck = exaggerationNeck;
    }

    private float calculateGeneralArousal(float pos, float neg) {
        return pos + neg;
    }

    public void calculateDecay(int time, int decayType) {
        if (((float) time) < this.decayTime) {
            float valenceDiff = Math.abs(this.primaryValence - ((Float) this.homeostaticState.get("valence")).floatValue());
            float arousalDiff = Math.abs(this.arousal - ((Float) this.homeostaticState.get("arousal")).floatValue());
            float stanceDiff = Math.abs(this.stance - ((Float) this.homeostaticState.get("stance")).floatValue());
            float volatilityDiff = Math.abs(this.volatility - ((Float) this.homeostaticState.get("volatility")).floatValue());
            float posArousalDiff = Math.abs(this.positiveArousal - ((Float) this.homeostaticState.get("positiveArousal")).floatValue());
            float negArousalDiff = Math.abs(this.negativeArousal - ((Float) this.homeostaticState.get("negativeArousal")).floatValue());
            float exaggerationDiff = Math.abs(this.exaggeration - ((Float) this.homeostaticState.get("exaggeration")).floatValue());
            float ratio = 0.0f;
            switch (decayType) {
                case 0:
                    ratio = ((float) time) / this.decayTime;
                    break;
                case 1:
                    ratio = (float) Math.pow((double) (((float) time) / this.decayTime), 2.0d);
                    break;
                case 2:
                    ratio = (float) Math.pow((double) (((float) time) / this.decayTime), 4.0d);
                    break;
                case 3:
                    ratio = (float) Math.pow((double) (((float) time) / this.decayTime), 0.5d);
                    break;
                case DecayTypes.EXPONENTIAL_DELAY /*4*/:
                    ratio = (float) Math.pow((double) (((((float) time) / this.decayTime) * 2.0f) - 4.0f), 2.0d);
                    break;
                case DecayTypes.NO_DECAY /*5*/:
                    ratio = 0.0f;
                    break;
                case DecayTypes.RAPID_DECAY /*6*/:
                    ratio = (float) Math.sqrt((double) (((float) time) / this.decayTime));
                    break;
                case DecayTypes.SUPER_RAPID_DECAY /*7*/:
                    float x = ((float) time) / this.decayTime;
                    if (((double) x) <= 0.05d) {
                        x = 0.050001f;
                    }
                    ratio = (float) ((-1.0d / Math.pow((double) (20.0f * x), 2.0d)) + 1.0d);
                    break;
            }
            float valenceChange = valenceDiff * ratio;
            float arousalChange = arousalDiff * ratio;
            float stanceChange = stanceDiff * ratio;
            float volatilityChange = volatilityDiff * ratio;
            float posArousalChange = posArousalDiff * ratio;
            float negArousalChange = negArousalDiff * ratio;
            float exaggerationChange = exaggerationDiff * ratio;
            if (this.primaryValence > ((Float) this.homeostaticState.get("valence")).floatValue()) {
                this.decayPrimaryValence = this.primaryValence - valenceChange;
            } else {
                this.decayPrimaryValence = this.primaryValence + valenceChange;
            }
            if (this.arousal > ((Float) this.homeostaticState.get("arousal")).floatValue()) {
                this.decayArousal = this.arousal - arousalChange;
            } else {
                this.decayArousal = this.arousal + arousalChange;
            }
            if (this.stance > ((Float) this.homeostaticState.get("stance")).floatValue()) {
                this.decayStance = this.stance - stanceChange;
            } else {
                this.decayStance = this.stance + stanceChange;
            }
            if (this.volatility > ((Float) this.homeostaticState.get("volatility")).floatValue()) {
                this.decayVolatility = this.volatility - volatilityChange;
            } else {
                this.decayVolatility = this.volatility + volatilityChange;
            }
            if (this.positiveArousal > ((Float) this.homeostaticState.get("positiveArousal")).floatValue()) {
                this.decayPosArousal = this.positiveArousal - posArousalChange;
            } else {
                this.decayPosArousal = this.positiveArousal + posArousalChange;
            }
            if (this.negativeArousal > ((Float) this.homeostaticState.get("negativeArousal")).floatValue()) {
                this.decayNegArousal = this.negativeArousal - negArousalChange;
            } else {
                this.decayNegArousal = this.negativeArousal + negArousalChange;
            }
            if (this.exaggeration > ((Float) this.homeostaticState.get("exaggeration")).floatValue()) {
                this.decayExaggeration = this.exaggeration - exaggerationChange;
            } else {
                this.decayExaggeration = this.exaggeration + exaggerationChange;
            }
            makeAdjustments(this.decayPrimaryValence, calculateGeneralArousal(this.decayPosArousal, this.decayNegArousal), this.decayStance, this.decayVolatility, this.decayPosArousal, this.decayNegArousal, this.decayExaggeration);
            return;
        }
        this.primaryValence = ((Float) this.homeostaticState.get("valence")).floatValue();
        this.arousal = ((Float) this.homeostaticState.get("arousal")).floatValue();
        this.stance = ((Float) this.homeostaticState.get("stance")).floatValue();
        this.volatility = ((Float) this.homeostaticState.get("volatility")).floatValue();
        this.positiveArousal = ((Float) this.homeostaticState.get("positiveArousal")).floatValue();
        this.negativeArousal = ((Float) this.homeostaticState.get("negativeArousal")).floatValue();
        this.exaggeration = ((Float) this.homeostaticState.get("exaggeration")).floatValue();
        makeAdjustments(this.primaryValence, calculateGeneralArousal(this.positiveArousal, this.negativeArousal), this.stance, this.volatility, this.positiveArousal, this.negativeArousal, this.exaggeration);
    }

    public void initialMove() {
        makeAdjustments(this.primaryValence, this.arousal, this.stance, this.volatility, this.positiveArousal, this.negativeArousal, this.exaggeration);
    }

    public void makeAdjustments(float valence, float arousal, float stance, float volatility, float posArousal, float negArousal, float exaggeration) {
        updateCurrentValues(valence, arousal, stance, volatility, posArousal, negArousal, exaggeration);
        calculatePosture(valence, stance, posArousal, negArousal);
        calculatePosBeats(this.lengthOfBeat, volatility, posArousal);
        calculateNegBeats(this.lengthOfBeat, volatility, negArousal);
        calculateExaggeration(exaggeration, volatility);
    }

    private void updateCurrentValues(float valence, float arousal, float stance, float volatility, float posArousal, float negArousal, float exaggeration) {
        this.currentValence.add(Float.valueOf(valence));
        this.currentArousal.add(Float.valueOf(arousal));
        this.currentStance.add(Float.valueOf(stance));
        this.currentVolatility.add(Float.valueOf(volatility));
        this.currentPosArousal.add(Float.valueOf(posArousal));
        this.currentNegArousal.add(Float.valueOf(negArousal));
        this.currentExaggeration.add(Float.valueOf(exaggeration));
    }

    public void clearValues() {
        System.out.println("clearing values");
        this.currentValence = new ArrayList();
        this.currentArousal = new ArrayList();
        this.currentStance = new ArrayList();
        this.currentVolatility = new ArrayList();
        this.currentPosArousal = new ArrayList();
        this.currentNegArousal = new ArrayList();
        this.currentExaggeration = new ArrayList();
    }

    public void save(String type) {
        saveValues(this.currentValence, new StringBuilder(String.valueOf(type)).append("valence").toString());
        saveValues(this.currentStance, new StringBuilder(String.valueOf(type)).append("stance").toString());
        saveValues(this.currentVolatility, new StringBuilder(String.valueOf(type)).append("volatility").toString());
        saveValues(this.currentPosArousal, new StringBuilder(String.valueOf(type)).append("posArousal").toString());
        saveValues(this.currentNegArousal, new StringBuilder(String.valueOf(type)).append("negArousal").toString());
        saveValues(this.currentExaggeration, new StringBuilder(String.valueOf(type)).append("exaggeration").toString());
    }

    private void saveValues(List<Float> data, String fileName) {
        System.out.println("saving files");
        try {
            File f = new File("/sdcard/Music/results/" + fileName + ".txt");
            f.createNewFile();
            FileOutputStream fos = new FileOutputStream(f);
            String myData = "";
            for (int i = 0; i < data.size(); i++) {
                myData = new StringBuilder(String.valueOf(myData)).append(((Float) data.get(i)).toString()).append("\n").toString();
            }
            fos.write(myData.getBytes());
            fos.flush();
            fos.close();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
