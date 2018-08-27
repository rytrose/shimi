package mr1.robots.travis.moves.sounds;

import android.content.Context;
import java.util.List;
import java.util.Map;
import mr1.motor.MotorController;
import mr1.robots.travis.moves.Move;

public class SoundMove extends Move {
    private SoundFunction sound;

    public interface SoundFunction {
        void triggerAudio(String str, float f, float f2, float f3, Map<String, Float> map);

        void triggerAudioSample(String str, float f);

        void updateAudio(String str, float f, float f2, float f3);
    }

    public SoundMove(Context c, MotorController mc, SoundFunction sound) {
        super(c, mc);
        this.sound = sound;
    }

    public void move(String dof, float pos, float beats) {
        System.out.println("******* moving " + dof);
        if (((Integer) this.movements.get(dof)).intValue() > 0) {
            handler.removeCallbacks(((AccelerationMover) this.movers.get(dof)).animator);
        }
        float radialPos = getRadialPos(dof, pos);
        float[] values = getAvgVelocity(dof, radialPos, beats);
        this.sound.triggerAudio(dof, beats, values[1], values[0], this.positions);
        setPosition(dof, Float.valueOf(radialPos));
        moveTo(dof, values[1], values[0]);
    }

    protected void updateAudio(String dof, float beats, float pos, float vel) {
        this.sound.updateAudio(dof, beats, pos, vel);
    }

    public void triggerGestureSound(String gesture, float beats) {
        this.sound.triggerAudioSample(gesture, beats * this.beatDuration);
    }

    public void move(String dof, float pos, float beats, List<Float> velocities) {
        System.out.println("**************");
        List<List<Float>> posAndVels = calculateTrajectory(dof, pos, beats, velocities);
        this.sound.triggerAudio(dof, beats, pos, ((Float) ((List) posAndVels.get(1)).get(0)).floatValue(), this.positions);
        animate(dof, beats, posAndVels);
    }

    public void noSoundMove(String dof, float pos, float beats, List<Float> velocities) {
        super.move(dof, pos, beats, velocities);
    }

    public void noSoundMove(String dof, float pos, float beats) {
        super.move(dof, pos, beats);
    }
}
