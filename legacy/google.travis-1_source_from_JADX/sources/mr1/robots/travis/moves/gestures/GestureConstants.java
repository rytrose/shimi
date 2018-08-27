package mr1.robots.travis.moves.gestures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestureConstants {
    public static final List<String> GENRES = new ArrayList();
    public static final Map<String, List<String>> GENRE_GESTURE_MAPPINGS = new HashMap();
    public static final Map<String, Integer> GESTURE_BEAT_TYPES = new HashMap();
    public static final Map<String, String> GESTURE_TRANSPOSITION_TYPES = new HashMap();

    static {
        GENRES.add("HipHopGesture");
        GENRES.add("RockGesture2");
        GENRES.add("RockGesture");
        GENRES.add("RockGesture3");
        GENRES.add("RockGesture4");
        GENRES.add("DiscoGesture");
        GENRES.add("DiscoGesture2");
        GENRES.add("Groove");
        GENRES.add("Swoop");
        GENRES.add("Funk");
        GESTURE_BEAT_TYPES.put("HipHopGesture", Integer.valueOf(1));
        GESTURE_BEAT_TYPES.put("RockGesture2", Integer.valueOf(2));
        GESTURE_BEAT_TYPES.put("RockGesture", Integer.valueOf(2));
        GESTURE_BEAT_TYPES.put("RockGesture3", Integer.valueOf(2));
        GESTURE_BEAT_TYPES.put("RockGesture4", Integer.valueOf(2));
        GESTURE_BEAT_TYPES.put("DiscoGesture", Integer.valueOf(2));
        GESTURE_BEAT_TYPES.put("DiscoGesture2", Integer.valueOf(4));
        GESTURE_BEAT_TYPES.put("Groove", Integer.valueOf(8));
        GESTURE_BEAT_TYPES.put("Swoop", Integer.valueOf(2));
        GESTURE_BEAT_TYPES.put("Funk", Integer.valueOf(8));
        GESTURE_TRANSPOSITION_TYPES.put("HipHopGesture", "NONE");
        GESTURE_TRANSPOSITION_TYPES.put("RockGesture2", "ADD");
        GESTURE_TRANSPOSITION_TYPES.put("RockGesture3", "ADD");
        GESTURE_TRANSPOSITION_TYPES.put("RockGesture4", "ADD");
        GESTURE_TRANSPOSITION_TYPES.put("RockGesture", "MULTIPLY");
        GESTURE_TRANSPOSITION_TYPES.put("DiscoGesture", "MULTIPLY");
        GESTURE_TRANSPOSITION_TYPES.put("DiscoGesture", "NONE");
        GESTURE_TRANSPOSITION_TYPES.put("Groove", "NONE");
        GESTURE_TRANSPOSITION_TYPES.put("Swoop", "ADD");
        GESTURE_TRANSPOSITION_TYPES.put("Funk", "ADD");
        GENRE_GESTURE_MAPPINGS.put("Rock", Arrays.asList(new String[]{"RockGesture", "RockGesture2", "Swoop", "RockGesture3", "RockGesture4"}));
        GENRE_GESTURE_MAPPINGS.put("HardRock", Arrays.asList(new String[]{"RockGesture2", "Swoop", "RockGesture3"}));
        GENRE_GESTURE_MAPPINGS.put("Disco", Arrays.asList(new String[]{"DiscoGesture", "RockGesture3", "DiscoGesture2"}));
        GENRE_GESTURE_MAPPINGS.put("HipHop", Arrays.asList(new String[]{"HipHopGesture", "RockGesture3"}));
        GENRE_GESTURE_MAPPINGS.put("Default", Arrays.asList(new String[]{"Swoop", "HipHopGesture", "RockGesture3"}));
        GENRE_GESTURE_MAPPINGS.put("Groove", Arrays.asList(new String[]{"Groove", "HipHopGesture"}));
    }
}
