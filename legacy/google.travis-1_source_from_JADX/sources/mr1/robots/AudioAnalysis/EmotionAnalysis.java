package mr1.robots.AudioAnalysis;

import java.util.ArrayList;
import java.util.List;

public class EmotionAnalysis {
    private float arousal;
    private double avgRMS = 0.0d;
    public String genre = "HipHopGesture";
    private float negArousal;
    private float[] onsetChangeBoundaries;
    private int onsetDensity = 0;
    private List<Long> onsetTimes = new ArrayList();
    private float pitch = 0.0f;
    private float[] pitchBoundaries;
    private float[] pitchChangeBoundaries;
    private int pitchDensity = 0;
    private List<Long> pitchTimes = new ArrayList();
    private List<Float> pitches = new ArrayList();
    private float posArousal;
    private float rationale;
    private int rmsCount = 0;
    private List<Double> rmsData = new ArrayList();
    private double rmsDiff = 0.0d;
    private float rmsDiffTotal = 0.0f;
    private int rmsPoints = ((int) (5.0f / this.rmsWindowSize));
    private float rmsSlope = 0.0f;
    private float rmsWindowSize = 0.05f;
    private float stance;
    public int tempo;
    private int timeWindow = 3000;
    private double totalRMS = 0.0d;
    public boolean update = false;
    private float valence;
    private double varRMS = 0.0d;
    private float[] volumeBoundaries;
    private float windowedAvgPitch = 0.0f;
    private double windowedAvgRMS = 0.0d;

    public EmotionAnalysis() {
        resetInitialValues();
    }

    private void resetInitialValues() {
        this.volumeBoundaries = new float[]{37.0f, 45.0f, 51.0f};
        this.pitchBoundaries = new float[]{42.0f, 65.0f, 80.0f};
        this.pitchChangeBoundaries = new float[]{0.0f, 15.0f, 20.0f};
        this.onsetChangeBoundaries = new float[]{0.0f, 7.0f, 20.0f};
    }

    private void clear() {
        this.pitches.clear();
    }

    private void evaluate() {
        if (this.pitchDensity >= 15) {
            this.valence = 1.0f;
            this.stance = 1.0f;
        } else if (this.pitchDensity < 15) {
            if (this.pitchDensity < 5) {
                this.valence = -1.0f;
                this.stance = -1.0f;
            } else {
                this.valence = 0.0f;
                this.stance = 0.0f;
            }
        }
        if (this.onsetDensity >= 15) {
            this.arousal = 1.0f;
        } else if (this.onsetDensity >= 15) {
        } else {
            if (this.onsetDensity < 3) {
                this.arousal = -1.0f;
            } else {
                this.arousal = 0.0f;
            }
        }
    }

    private void calcValence(float value) {
        if (value < this.pitchBoundaries[0]) {
            this.valence = -1.0f;
        } else {
            this.valence = calcAccordingToBoundary(value, this.pitchBoundaries);
        }
        if (this.valence >= 0.0f) {
            this.stance = 0.1f;
        }
        if (this.valence < 0.0f) {
            this.stance = -1.0f;
        }
    }

    private float calcAccordingToBoundary(float value, float[] boundaries) {
        if (value < boundaries[1]) {
            return -1.0f;
        }
        value = 1.0f - ((boundaries[2] - value) / (boundaries[2] - boundaries[1]));
        if (value > 1.0f) {
            return 1.0f;
        }
        return value;
    }

    private float getAvg(List<Float> list) {
        float avg = 0.0f;
        if (list.size() <= 0) {
            return 0.0f;
        }
        for (int i = 0; i < list.size(); i++) {
            avg += ((Float) list.get(i)).floatValue();
        }
        return avg / ((float) list.size());
    }

    public void addPitchInfo(Object... args) {
        this.pitch = ((Float) args[0]).floatValue();
        this.pitchTimes.add(Long.valueOf(System.currentTimeMillis()));
        this.pitches.add(Float.valueOf(this.pitch));
        calcValence(this.pitch);
    }

    public void addRMS(double rms) {
        if (this.rmsData.size() < this.rmsPoints) {
            this.rmsData.add(Double.valueOf(rms));
            this.totalRMS += rms;
            return;
        }
        phraseSegmentation(rms, ((Double) this.rmsData.get(this.rmsData.size() - 1)).doubleValue());
        this.totalRMS -= ((Double) this.rmsData.get(0)).doubleValue();
        this.totalRMS += rms;
        this.rmsData.remove(0);
        this.rmsData.add(Double.valueOf(rms));
        this.avgRMS = this.totalRMS / ((double) this.rmsPoints);
        double tempSquaredAvg = 0.0d;
        for (int i = 0; i < this.rmsPoints; i++) {
            tempSquaredAvg += Math.pow(((Double) this.rmsData.get(i)).doubleValue() - this.avgRMS, 2.0d);
        }
        this.varRMS = tempSquaredAvg / ((double) this.rmsPoints);
        calculateOnsetDensity();
        calculatePitchedOnsetDensity();
        this.rmsCount++;
        this.update = false;
        if (this.rmsCount >= 30) {
            this.update = true;
            this.rmsCount = 0;
            detectGenre();
        }
    }

    private void phraseSegmentation(double rms, double lastRMS) {
        this.rmsDiff = lastRMS - rms;
        this.rmsDiffTotal = (float) (((double) this.rmsDiffTotal) + this.rmsDiff);
    }

    public void addOnset() {
        this.onsetTimes.add(Long.valueOf(System.currentTimeMillis()));
    }

    private void calculateOnsetDensity() {
        Long boundaryTime = Long.valueOf(System.currentTimeMillis() - ((long) this.timeWindow));
        int index = -1;
        int i = 0;
        while (i < this.onsetTimes.size() && ((Long) this.onsetTimes.get(i)).longValue() < boundaryTime.longValue()) {
            index = i;
            i++;
        }
        if (index > -1) {
            for (i = 0; i <= index; i++) {
                this.onsetTimes.remove(0);
            }
        }
        this.onsetDensity = this.onsetTimes.size();
    }

    private void calculatePitchedOnsetDensity() {
        Long boundaryTime = Long.valueOf(System.currentTimeMillis() - ((long) this.timeWindow));
        int index = -1;
        int i = 0;
        while (i < this.pitchTimes.size() && ((Long) this.pitchTimes.get(i)).longValue() < boundaryTime.longValue()) {
            index = i;
            i++;
        }
        if (index > -1) {
            for (i = 0; i <= index; i++) {
                this.pitchTimes.remove(0);
                this.pitches.remove(0);
            }
        }
        this.pitchDensity = this.pitchTimes.size();
    }

    private void pitchHistogram() {
    }

    private void detectGenre() {
        if (this.onsetDensity > 6) {
            this.tempo = 1;
            if (this.avgRMS < 50.0d) {
                if (this.onsetDensity > 17) {
                    this.genre = "RockGesture2";
                } else {
                    this.genre = "RockGesture3";
                }
            } else if (this.onsetDensity < 6) {
                this.genre = "RockGesture4";
            } else {
                this.genre = "DiscoGesture2";
            }
        } else if (this.avgRMS < 46.0d) {
            this.genre = "HipHopGesture";
            if (this.onsetDensity < 2) {
                this.tempo = 3;
            } else {
                this.tempo = 1;
            }
        } else {
            this.tempo = 1;
            if (this.onsetDensity < 3) {
                this.genre = "Groove";
            } else {
                this.genre = "Swoop";
            }
        }
    }

    public double getRMSAverage() {
        return this.avgRMS;
    }

    public double getRMSVariance() {
        return this.varRMS;
    }

    public int getNoteDensity() {
        return this.onsetDensity;
    }

    public float getPitch() {
        return this.pitch;
    }

    public int getPitchedOnsetDensity() {
        return this.pitchDensity;
    }

    public double getRMSDiff() {
        return (double) this.rmsSlope;
    }

    public float getValence() {
        System.out.println(this.valence);
        return this.valence;
    }

    public float getArousal() {
        return this.arousal;
    }

    public float getStance() {
        return this.stance;
    }
}
