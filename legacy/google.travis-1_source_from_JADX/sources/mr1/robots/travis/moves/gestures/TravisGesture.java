package mr1.robots.travis.moves.gestures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mr1.robots.travis.TravisDofs;

public abstract class TravisGesture implements TravisDofs {
    public int beatType;
    public boolean hasRandomComponent;
    public List<Map<String, Float>> movements = new ArrayList();
    public Map<Integer, Map<Float, List<GestureParameter>>> moves = new HashMap();
    public List<List<Map<String, Float>>> movesPerBeat = new ArrayList();
    public List<Float> times = new ArrayList();
    public List<List<Float>> timesPerBeat = new ArrayList();
    public String transposeDof;
    public Map<Integer, Float> transpositions = new HashMap();
    public List<List<Map<String, List<Float>>>> velocitiesPerBeat = new ArrayList();
    public List<Map<String, List<Float>>> velocityVector = new ArrayList();

    protected class GestureParameter {
        private String dof;
        private float duration;
        private float position;
        private List<Float> velocities;

        public GestureParameter(String dof, float position, float beats, List<Float> velocities) {
            this.velocities = velocities;
            this.dof = dof;
            this.position = position;
            this.duration = beats;
        }

        public String getDof() {
            return this.dof;
        }

        public float getPos() {
            return this.position;
        }

        public float getDuration() {
            return this.duration;
        }

        public List<Float> getVelocities() {
            return this.velocities;
        }
    }

    public int getMoveCount(int beat, float timePerBeat) {
        System.out.println(" moves size  =" + this.moves.size());
        System.out.println(" timePerBeat = " + timePerBeat);
        System.out.println(" moves size 1 = " + ((Map) this.moves.get(Integer.valueOf(beat))).size());
        return ((List) ((Map) this.moves.get(Integer.valueOf(beat))).get(Float.valueOf(timePerBeat))).size();
    }

    public String getDof(int beat, float timePerBeat, int count) {
        return ((GestureParameter) ((List) ((Map) this.moves.get(Integer.valueOf(beat))).get(Float.valueOf(timePerBeat))).get(count)).getDof();
    }

    public float getPos(int beat, float timePerBeat, int count) {
        return ((GestureParameter) ((List) ((Map) this.moves.get(Integer.valueOf(beat))).get(Float.valueOf(timePerBeat))).get(count)).getPos();
    }

    public float getDuration(int beat, float timePerBeat, int count) {
        return ((GestureParameter) ((List) ((Map) this.moves.get(Integer.valueOf(beat))).get(Float.valueOf(timePerBeat))).get(count)).getDuration();
    }

    public List<Float> getVelocities(int beat, float timePerBeat, int count) {
        return ((GestureParameter) ((List) ((Map) this.moves.get(Integer.valueOf(beat))).get(Float.valueOf(timePerBeat))).get(count)).getVelocities();
    }
}
