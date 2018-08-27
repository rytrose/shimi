package mr1.robots.travis.moves.gestures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mr1.robots.travis.TravisDofs;

public class Swoop extends TravisGesture implements TravisDofs {
    private Map<String, Float> limits;
    private Map<String, List<Float>> velocities;

    public Swoop() {
        this.limits = new HashMap();
        this.velocities = new HashMap();
        this.hasRandomComponent = false;
        this.transposeDof = TravisDofs.NECKUD_MOTOR;
        this.transpositions.put(Integer.valueOf(0), Float.valueOf(0.0f));
        this.transpositions.put(Integer.valueOf(1), Float.valueOf(0.6f));
        this.transpositions.put(Integer.valueOf(2), Float.valueOf(-0.7f));
        List<GestureParameter> tempMoves = new ArrayList();
        Map<Float, List<GestureParameter>> beatMap = new HashMap();
        List<Float> perBeat = new ArrayList();
        this.beatType = 2;
        tempMoves.add(new GestureParameter(TravisDofs.HEAD_MOTOR, 1.0f, 0.5f, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)})));
        tempMoves.add(new GestureParameter(TravisDofs.NECKRL_MOTOR, -0.8f, 1.0f, Arrays.asList(new Float[]{Float.valueOf(0.16f), Float.valueOf(0.2f), Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.7f), Float.valueOf(0.5f), Float.valueOf(0.3f), Float.valueOf(0.2f), Float.valueOf(0.16f)})));
        tempMoves.add(new GestureParameter(TravisDofs.NECKUD_MOTOR, 0.1f, 0.5f, Arrays.asList(new Float[]{Float.valueOf(0.7f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)})));
        beatMap.put(Float.valueOf(0.0f), tempMoves);
        tempMoves = new ArrayList();
        tempMoves.add(new GestureParameter(TravisDofs.NECKUD_MOTOR, 0.4f, 0.5f, Arrays.asList(new Float[]{Float.valueOf(0.7f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)})));
        tempMoves.add(new GestureParameter(TravisDofs.HEAD_MOTOR, -1.0f, 0.5f, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)})));
        beatMap.put(Float.valueOf(0.5f), tempMoves);
        this.moves.put(Integer.valueOf(0), beatMap);
        perBeat.add(Float.valueOf(0.0f));
        perBeat.add(Float.valueOf(0.5f));
        this.timesPerBeat.add(perBeat);
        tempMoves = new ArrayList();
        beatMap = new HashMap();
        perBeat = new ArrayList();
        tempMoves.add(new GestureParameter(TravisDofs.HEAD_MOTOR, 1.0f, 0.5f, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)})));
        tempMoves.add(new GestureParameter(TravisDofs.NECKRL_MOTOR, 0.8f, 1.0f, Arrays.asList(new Float[]{Float.valueOf(0.16f), Float.valueOf(0.2f), Float.valueOf(0.3f), Float.valueOf(0.5f), Float.valueOf(0.7f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(0.7f), Float.valueOf(0.5f), Float.valueOf(0.3f), Float.valueOf(0.2f), Float.valueOf(0.16f)})));
        tempMoves.add(new GestureParameter(TravisDofs.NECKUD_MOTOR, 0.1f, 0.5f, Arrays.asList(new Float[]{Float.valueOf(0.7f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)})));
        beatMap.put(Float.valueOf(0.0f), tempMoves);
        tempMoves = new ArrayList();
        tempMoves.add(new GestureParameter(TravisDofs.NECKUD_MOTOR, 0.4f, 0.5f, Arrays.asList(new Float[]{Float.valueOf(0.7f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)})));
        tempMoves.add(new GestureParameter(TravisDofs.HEAD_MOTOR, -1.0f, 0.5f, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)})));
        beatMap.put(Float.valueOf(0.5f), tempMoves);
        this.moves.put(Integer.valueOf(1), beatMap);
        perBeat.add(Float.valueOf(0.0f));
        perBeat.add(Float.valueOf(0.5f));
        this.timesPerBeat.add(perBeat);
    }
}
