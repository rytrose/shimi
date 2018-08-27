package mr1.robots.travis;

import android.content.Context;
import java.util.HashMap;
import mr1.motor.MotorController;
import mr1.motor.specific.ADKMX28MotorController;
import mr1.research.behaviors.BehaviorController;
import mr1.robots.travis.behaviors.FootTapBehavior;
import mr1.robots.travis.behaviors.IdleBehavior;
import mr1.robots.travis.behaviors.LookAtPhoneBehavior;
import mr1.robots.travis.behaviors.NodBehavior;
import mr1.robots.travis.behaviors.SurpriseBehavior;
import mr1.robots.travis.behaviors.SwingBehavior;
import mr1.robots.travis.behaviors.TapBehavior;
import mr1.robots.travis.behaviors.TravisBehavior.iOnBehaviorEndedListener;
import mr1.robots.travis.behaviors.ZeroBehavior;

public class TravisBehaviorController extends BehaviorController implements iOnBehaviorEndedListener {
    private MotorController mc;

    public TravisBehaviorController(Context c) {
        this.mc = new ADKMX28MotorController(c, "/mr1/robots/travis/motor_config.xml");
        this.behaviors = new HashMap();
        this.behaviors.put("ZERO", new ZeroBehavior(this.mc));
        this.behaviors.put("IDLE", new IdleBehavior(this.mc));
        this.behaviors.put("TAP", new TapBehavior(this.mc));
        this.behaviors.put("FOOT", new FootTapBehavior(this.mc));
        LookAtPhoneBehavior lapb = new LookAtPhoneBehavior(this.mc);
        lapb.setOnBehaviorEndedListener(this);
        this.behaviors.put("LOOKATPHONE", lapb);
        this.behaviors.put("SWING", new SwingBehavior(this.mc));
        this.behaviors.put("NOD", new NodBehavior(this.mc));
        this.behaviors.put("WHOA", new SurpriseBehavior(this.mc));
    }

    public void onBehaviorEnded(String name) {
        if (name.equals("LOOKATPHONE")) {
            startBehavior("IDLE", null);
        } else if (name.equals("IDLE")) {
            startBehavior("OFF", null);
        }
    }

    public MotorController getMotorController() {
        return this.mc;
    }
}
