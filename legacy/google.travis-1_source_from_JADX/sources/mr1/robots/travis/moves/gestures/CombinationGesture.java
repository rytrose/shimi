package mr1.robots.travis.moves.gestures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mr1.robots.travis.TravisDofs;

public class CombinationGesture extends TravisGesture {
    private Map<String, Float> limits;
    private Map<String, List<Float>> velocities;

    public CombinationGesture() {
        this.limits = new HashMap();
        this.velocities = new HashMap();
        this.beatType = 2;
        this.times.add(Float.valueOf(0.0f));
        this.limits.put(TravisDofs.HEAD_MOTOR, Float.valueOf(0.6f));
        this.limits.put(TravisDofs.NECKRL_MOTOR, Float.valueOf(0.5f));
        this.limits.put(TravisDofs.NECKUD_MOTOR, Float.valueOf(0.3f));
        this.velocities.put(TravisDofs.HEAD_MOTOR, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
        this.velocities.put(TravisDofs.NECKRL_MOTOR, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
        this.velocities.put(TravisDofs.NECKUD_MOTOR, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
        this.velocityVector.add(this.velocities);
        this.movements.add(this.limits);
        this.movesPerBeat.add(this.movements);
        this.timesPerBeat.add(this.times);
        this.velocitiesPerBeat.add(this.velocityVector);
        this.movements = new ArrayList();
        this.velocities = new HashMap();
        this.velocityVector = new ArrayList();
        this.limits = new HashMap();
        this.times = new ArrayList();
        this.times.add(Float.valueOf(0.0f));
        this.limits.put(TravisDofs.HEAD_MOTOR, Float.valueOf(-1.0f));
        this.limits.put(TravisDofs.NECKRL_MOTOR, Float.valueOf(-0.5f));
        this.limits.put(TravisDofs.NECKUD_MOTOR, Float.valueOf(-0.1f));
        this.velocities.put(TravisDofs.HEAD_MOTOR, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
        this.velocities.put(TravisDofs.NECKRL_MOTOR, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
        this.velocities.put(TravisDofs.NECKUD_MOTOR, Arrays.asList(new Float[]{Float.valueOf(0.2f), Float.valueOf(0.4f), Float.valueOf(0.7f), Float.valueOf(0.9f), Float.valueOf(1.0f), Float.valueOf(1.0f), Float.valueOf(1.0f)}));
        this.velocityVector.add(this.velocities);
        this.movements.add(this.limits);
        this.movesPerBeat.add(this.movements);
        this.timesPerBeat.add(this.times);
        this.velocitiesPerBeat.add(this.velocityVector);
    }
}
