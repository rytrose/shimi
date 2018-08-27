package mr1.robots.travis.moves.gestures.emotions;

import java.util.HashMap;
import java.util.Map;

public class EmotionConstants {
    public static final String ANGRY_EMOTION = "anger";
    public static final String CONTENT_EMOTION = "content";
    public static final String DISGUSTED_EMOTION = "disgust";
    public static final Map<String, Map<String, Float>> EMOTION_PRESETS = new HashMap();
    public static final String FEARFUL_EMOTION = "fear";
    public static final String HAPPY_EMOTION = "happiness";
    public static final String SAD_EMOTION = "sadness";
    public static final String SURPRISED_EMOTION = "surprise";

    static {
        EMOTION_PRESETS.put(SAD_EMOTION, new HashMap());
        ((Map) EMOTION_PRESETS.get(SAD_EMOTION)).put("valence", Float.valueOf(-1.0f));
        ((Map) EMOTION_PRESETS.get(SAD_EMOTION)).put("arousal", Float.valueOf(-1.0f));
        ((Map) EMOTION_PRESETS.get(SAD_EMOTION)).put("alertness", Float.valueOf(0.0f));
        ((Map) EMOTION_PRESETS.get(SAD_EMOTION)).put("personality", Float.valueOf(0.0f));
        EMOTION_PRESETS.put(HAPPY_EMOTION, new HashMap());
    }
}
