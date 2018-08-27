package mr1.robots.travis.moves.gestures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mr1.robots.travis.TravisDofs;

public class HipHopGesture extends TravisGesture {
    private Map<String, Float> limits;
    private Map<String, List<Float>> velocities;

    public HipHopGesture() {
        this.limits = new HashMap();
        this.velocities = new HashMap();
        this.hasRandomComponent = true;
        this.transposeDof = TravisDofs.NECKRL_MOTOR;
        this.transpositions.put(Integer.valueOf(0), Float.valueOf(0.0f));
        this.transpositions.put(Integer.valueOf(1), Float.valueOf(-0.5f));
        this.beatType = 1;
        List<GestureParameter> tempMoves = new ArrayList();
        Map<Float, List<GestureParameter>> beatMap = new HashMap();
        List<Float> perBeat = new ArrayList();
        this.beatType = 2;
        tempMoves.add(new GestureParameter(TravisDofs.HEAD_MOTOR, -0.5f, 0.5f, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f)})));
        tempMoves.add(new GestureParameter(TravisDofs.NECKUD_MOTOR, 0.2f, 0.5f, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)})));
        beatMap.put(Float.valueOf(0.0f), tempMoves);
        perBeat.add(Float.valueOf(0.0f));
        tempMoves = new ArrayList();
        tempMoves.add(new GestureParameter(TravisDofs.HEAD_MOTOR, 0.4f, 0.5f, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f)})));
        tempMoves.add(new GestureParameter(TravisDofs.NECKUD_MOTOR, -0.3f, 0.5f, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)})));
        beatMap.put(Float.valueOf(0.5f), tempMoves);
        this.moves.put(Integer.valueOf(0), beatMap);
        perBeat.add(Float.valueOf(0.5f));
        this.timesPerBeat.add(perBeat);
    }
}
