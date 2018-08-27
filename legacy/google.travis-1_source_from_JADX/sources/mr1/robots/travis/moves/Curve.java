package mr1.robots.travis.moves;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Curve {
    public List<Float> acceleration = new ArrayList();
    public float beats;
    public String command;
    private HashMap<String, List<Float>> curveTypes = new HashMap();
    public String dof;
    public float pos;
    public String type;
    public float velocity;

    public Curve(String dof, float pos, float beats, List<Float> acceleration) {
        loadCurveTypes();
        this.dof = dof;
        this.pos = pos;
        this.beats = beats;
        this.acceleration = acceleration;
    }

    public Curve(String dof, float pos, float beats, String accelType) {
        loadCurveTypes();
        this.dof = dof;
        this.pos = pos;
        this.beats = beats;
        this.acceleration = getCurveVector(accelType);
    }

    public Curve(String dof, float pos, float beats) {
        loadCurveTypes();
        this.dof = dof;
        this.pos = pos;
        this.beats = beats;
        this.acceleration = getCurveVector("constant");
    }

    private void loadCurveTypes() {
        this.curveTypes.put("constant", Arrays.asList(new Float[]{Float.valueOf(1.0f)}));
        this.curveTypes.put("accelerate", Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.25f), Float.valueOf(0.275f), Float.valueOf(0.35f), Float.valueOf(0.4f), Float.valueOf(0.6f), Float.valueOf(0.8f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
        this.curveTypes.put("decelerate", Arrays.asList(new Float[]{Float.valueOf(1.0f), Float.valueOf(0.9f), Float.valueOf(0.8f), Float.valueOf(0.7f), Float.valueOf(0.6f), Float.valueOf(0.5f), Float.valueOf(0.4f), Float.valueOf(0.35f), Float.valueOf(0.3f), Float.valueOf(0.28f), Float.valueOf(0.25f), Float.valueOf(0.2f), Float.valueOf(0.1f)}));
    }

    private List<Float> getCurveVector(String curveType) {
        return (List) this.curveTypes.get(curveType);
    }
}
