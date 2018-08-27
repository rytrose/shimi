package mr1.research.behaviors;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public class BehaviorController {
    private static String TAG;
    /* renamed from: D */
    private boolean f11D = true;
    public Map<String, iBehavior> behaviors;
    private iBehavior currentBehavior;
    private String defaultBehaviorName;

    public interface iBehavior {
        void start(Object obj);

        void stop();
    }

    public BehaviorController() {
        TAG = getClass().toString();
        this.behaviors = new HashMap();
    }

    public void setDefaultBehavior(String def) {
        this.defaultBehaviorName = def;
    }

    public void startBehavior(String name, Object data) {
        if (this.currentBehavior != null) {
            this.currentBehavior.stop();
        }
        if (name != null) {
            this.currentBehavior = (iBehavior) this.behaviors.get(name);
            if (this.currentBehavior != null) {
                this.currentBehavior.start(data);
                return;
            } else if (this.f11D) {
                Log.w(TAG, "Behavior " + name + " not found. Ignoring.");
                return;
            } else {
                return;
            }
        }
        this.currentBehavior = null;
    }

    public void stop() {
        startBehavior(this.defaultBehaviorName, null);
    }

    public iBehavior getCurrentBehavior() {
        return this.currentBehavior;
    }
}
