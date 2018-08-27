package google.travis;

import android.content.Context;
import java.util.List;
import mr1.motor.MotorController;
import mr1.robots.travis.TravisDofs;
import mr1.robots.travis.moves.ThreadScheduler;

public class Sounds implements TravisDofs, ThreadScheduler {
    private float beatDuration;
    private MotorController mc;
    private TravisPdManager pdManager;

    public Sounds(Context c, MotorController mc, float beatDuration) {
        this.pdManager = new TravisPdManager(c);
        this.mc = mc;
        this.beatDuration = beatDuration;
    }

    public Sounds(Context c, float beatDuration) {
        this.pdManager = new TravisPdManager(c);
        this.beatDuration = beatDuration;
    }

    public void testSound() {
        this.pdManager.bell60(1.0f);
    }

    public void triggerSound(List<SoundCommand> soundCommands) {
        for (int i = 0; i < soundCommands.size(); i++) {
            String tempString = ((SoundCommand) soundCommands.get(i)).sender.toLowerCase();
            System.out.println(tempString);
            if (tempString.contains("handup")) {
                this.pdManager.handUp(Float.valueOf(((SoundCommand) soundCommands.get(i)).value1.toString()).floatValue() * this.beatDuration);
            } else if (tempString.contains("handdown")) {
                this.pdManager.handDown(Float.valueOf(((SoundCommand) soundCommands.get(i)).value1.toString()).floatValue() * this.beatDuration);
            } else if (tempString.contains("pbsample1")) {
                this.pdManager.pbSample1(((SoundCommand) soundCommands.get(i)).value1.toString());
            } else if (tempString.contains("pbduration1")) {
                this.pdManager.pbDuration1(Float.valueOf(((SoundCommand) soundCommands.get(i)).value1.toString()).floatValue() * this.beatDuration);
            } else if (tempString.contains("pbpitch1")) {
                this.pdManager.pbPitch1(Float.valueOf(((SoundCommand) soundCommands.get(i)).value1.toString()).floatValue());
            } else if (tempString.contains("pbrampspecshift1")) {
                this.pdManager.pbRampSpecShift1(Float.valueOf(((SoundCommand) soundCommands.get(i)).value2.toString()).floatValue(), Float.valueOf(((SoundCommand) soundCommands.get(i)).value1.toString()).floatValue() * this.beatDuration);
            } else if (tempString.contains("pbsample2")) {
                this.pdManager.pbSample2(((SoundCommand) soundCommands.get(i)).value1.toString());
            } else if (tempString.contains("pbduration2")) {
                this.pdManager.pbDuration2(Float.valueOf(((SoundCommand) soundCommands.get(i)).value1.toString()).floatValue() * this.beatDuration);
            } else if (tempString.contains("pbpitch2")) {
                this.pdManager.pbPitch2(Float.valueOf(((SoundCommand) soundCommands.get(i)).value1.toString()).floatValue());
            } else if (tempString.contains("pbrampspecshift2")) {
                this.pdManager.pbRampSpecShift2(Float.valueOf(((SoundCommand) soundCommands.get(i)).value2.toString()).floatValue(), Float.valueOf(((SoundCommand) soundCommands.get(i)).value1.toString()).floatValue() * this.beatDuration);
            } else if (tempString.contains("basssample")) {
                this.pdManager.bassSample(1.0f);
            } else if (tempString.contains("dtsample1")) {
                this.pdManager.dtsample1(Float.valueOf(((SoundCommand) soundCommands.get(i)).value1.toString()).floatValue() * this.beatDuration);
            } else if (tempString.contains("dtsample2")) {
                this.pdManager.dtsample2(Float.valueOf(((SoundCommand) soundCommands.get(i)).value1.toString()).floatValue() * this.beatDuration);
            } else if (tempString.contains("dtsample3")) {
                this.pdManager.dtsample3(Float.valueOf(((SoundCommand) soundCommands.get(i)).value1.toString()).floatValue() * this.beatDuration);
            } else if (tempString.contains("dtsample4")) {
                this.pdManager.dtsample4(Float.valueOf(((SoundCommand) soundCommands.get(i)).value1.toString()).floatValue() * this.beatDuration);
            } else if (tempString.contains("bass41")) {
                this.pdManager.bass41(1.0f);
            } else if (tempString.contains("bass36")) {
                this.pdManager.bass36(1.0f);
            } else if (tempString.contains("bass37")) {
                this.pdManager.bass37(1.0f);
            } else if (tempString.contains("bass44")) {
                this.pdManager.bass44(1.0f);
            } else if (tempString.contains("bass43")) {
                this.pdManager.bass43(1.0f);
            } else if (tempString.contains("bass40")) {
                this.pdManager.bass40(1.0f);
            } else if (tempString.contains("bass39")) {
                this.pdManager.bass39(1.0f);
            } else if (tempString.contains("melody60")) {
                this.pdManager.melody60(1.0f);
            } else if (tempString.contains("melody64")) {
                this.pdManager.melody64(1.0f);
            } else if (tempString.contains("melody65")) {
                this.pdManager.melody65(1.0f);
            } else if (tempString.contains("melody67")) {
                this.pdManager.melody67(1.0f);
            } else if (tempString.contains("melody68")) {
                this.pdManager.melody68(1.0f);
            } else if (tempString.contains("melody72")) {
                this.pdManager.melody72(1.0f);
            } else if (tempString.contains("melody73")) {
                this.pdManager.melody73(1.0f);
            } else if (tempString.contains("bell60")) {
                this.pdManager.bell60(1.0f);
            } else if (tempString.contains("bell63")) {
                this.pdManager.bell63(1.0f);
            } else if (tempString.contains("bell64")) {
                this.pdManager.bell64(1.0f);
            } else if (tempString.contains("bell65")) {
                this.pdManager.bell65(1.0f);
            } else if (tempString.contains("bell67")) {
                this.pdManager.bell67(1.0f);
            } else if (tempString.contains("bell68")) {
                this.pdManager.bell68(1.0f);
            } else if (tempString.contains("cutesample")) {
                if (tempString.contains("short")) {
                    this.pdManager.cuteSampleShort(Float.valueOf(((SoundCommand) soundCommands.get(i)).value1.toString()).floatValue() * this.beatDuration);
                } else if (tempString.contains("long")) {
                    this.pdManager.cuteSampleLong(Float.valueOf(((SoundCommand) soundCommands.get(i)).value1.toString()).floatValue() * this.beatDuration);
                } else {
                    this.pdManager.cuteSample(Float.valueOf(((SoundCommand) soundCommands.get(i)).value1.toString()).floatValue() * this.beatDuration);
                }
            } else if (tempString.contains("cute1sample")) {
                if (tempString.contains("short")) {
                    this.pdManager.cute1SampleShort(Float.valueOf(((SoundCommand) soundCommands.get(i)).value1.toString()).floatValue() * this.beatDuration);
                } else {
                    this.pdManager.cute1Sample(Float.valueOf(((SoundCommand) soundCommands.get(i)).value1.toString()).floatValue() * this.beatDuration);
                }
            } else if (tempString.contains("cute2sample")) {
                if (tempString.contains("short")) {
                    this.pdManager.cute2SampleShort(Float.valueOf(((SoundCommand) soundCommands.get(i)).value1.toString()).floatValue() * this.beatDuration);
                } else {
                    this.pdManager.cute2Sample(Float.valueOf(((SoundCommand) soundCommands.get(i)).value1.toString()).floatValue() * this.beatDuration);
                }
            }
        }
    }
}
