package mr1.robots.travis.moves.gestures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mr1.robots.travis.TravisDofs;

public class RockGesture extends TravisGesture implements TravisDofs {
    private Map<String, Float> limits;
    private Map<String, List<Float>> velocities;

    public RockGesture() {
        this.limits = new HashMap();
        this.velocities = new HashMap();
        this.hasRandomComponent = false;
        this.transposeDof = TravisDofs.NECKRL_MOTOR;
        this.transpositions.put(Integer.valueOf(0), Float.valueOf(1.0f));
        this.transpositions.put(Integer.valueOf(1), Float.valueOf(-1.0f));
        List<GestureParameter> tempMoves = new ArrayList();
        Map<Float, List<GestureParameter>> beatMap = new HashMap();
        List<Float> perBeat = new ArrayList();
        this.beatType = 2;
        tempMoves.add(new GestureParameter(TravisDofs.HEAD_MOTOR, 0.8f, 1.0f, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)})));
        tempMoves.add(new GestureParameter(TravisDofs.NECKRL_MOTOR, 0.5f, 1.0f, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)})));
        tempMoves.add(new GestureParameter(TravisDofs.NECKUD_MOTOR, 0.7f, 1.0f, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)})));
        beatMap.put(Float.valueOf(0.0f), tempMoves);
        this.moves.put(Integer.valueOf(0), beatMap);
        perBeat.add(Float.valueOf(0.0f));
        this.timesPerBeat.add(perBeat);
        tempMoves = new ArrayList();
        beatMap = new HashMap();
        perBeat = new ArrayList();
        tempMoves.add(new GestureParameter(TravisDofs.HEAD_MOTOR, -0.8f, 1.0f, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)})));
        tempMoves.add(new GestureParameter(TravisDofs.NECKRL_MOTOR, 0.0f, 1.0f, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)})));
        tempMoves.add(new GestureParameter(TravisDofs.NECKUD_MOTOR, 0.2f, 1.0f, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)})));
        beatMap.put(Float.valueOf(0.0f), tempMoves);
        this.moves.put(Integer.valueOf(1), beatMap);
        perBeat.add(Float.valueOf(0.0f));
        this.timesPerBeat.add(perBeat);
    }
}
