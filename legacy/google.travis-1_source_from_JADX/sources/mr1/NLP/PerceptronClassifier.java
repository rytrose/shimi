package mr1.NLP;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mr1.robots.travis.moves.gestures.emotions.EmotionConstants;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class PerceptronClassifier {
    private final int ANGER = 1;
    private final int DISGUST = 4;
    private final int FEAR = 5;
    private final int HAPPY = 3;
    private final int NEUTRAL = 0;
    private final int SAD = 2;
    private final int SURPRISE = 6;
    private List<String> emotions = new ArrayList();
    private JSONObject featureDict = new JSONObject();
    private Stemmer stemmer = new Stemmer();

    public PerceptronClassifier() {
        loadEmotionFeatures();
    }

    private void loadEmotionFeatures() {
        this.emotions.add("happy");
        this.emotions.add("sad");
        this.emotions.add(EmotionConstants.DISGUSTED_EMOTION);
        this.emotions.add(EmotionConstants.FEARFUL_EMOTION);
        this.emotions.add(EmotionConstants.ANGRY_EMOTION);
        this.emotions.add(EmotionConstants.SURPRISED_EMOTION);
        try {
            DataInputStream in = new DataInputStream(NaturalLanguage.class.getResourceAsStream("/mr1/robots/travis/perceptronEmotionFeatures.json"));
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String data = "";
            while (true) {
                String str = br.readLine();
                if (str == null) {
                    in.close();
                    this.featureDict = (JSONObject) JSONValue.parse(data);
                    return;
                }
                data = new StringBuilder(String.valueOf(data)).append(str).toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String classifyEmotion(String sentence) {
        int i;
        int j;
        int x;
        sentence = sentence.toLowerCase();
        String[] words = sentence.split(" ");
        Map<Integer, Long> values = new HashMap();
        for (i = 0; i < this.emotions.size(); i++) {
            values.put(Integer.valueOf(i), Long.valueOf(0));
        }
        System.out.println(sentence);
        for (j = 0; j < this.emotions.size(); j++) {
            for (i = 0; i < words.length; i++) {
                if (this.featureDict.containsKey(words[i])) {
                    values.put(Integer.valueOf(j), Long.valueOf(((Long) values.get(Integer.valueOf(j))).longValue() + ((Long) ((JSONArray) this.featureDict.get(words[i])).get(j)).longValue()));
                }
            }
        }
        if (words.length > 1) {
            for (j = 0; j < this.emotions.size(); j++) {
                for (i = 0; i < words.length - 1; i++) {
                    String bigram = words[i] + words[i + 1];
                    if (this.featureDict.containsKey(bigram)) {
                        values.put(Integer.valueOf(j), Long.valueOf(((Long) values.get(Integer.valueOf(j))).longValue() + ((Long) ((JSONArray) this.featureDict.get(bigram)).get(j)).longValue()));
                    }
                }
            }
        }
        if (words.length > 2) {
            for (j = 0; j < this.emotions.size(); j++) {
                for (i = 0; i < words.length - 2; i++) {
                    String trigram = words[i] + words[i + 1] + words[i + 2];
                    if (this.featureDict.containsKey(trigram)) {
                        values.put(Integer.valueOf(j), Long.valueOf(((Long) values.get(Integer.valueOf(j))).longValue() + ((Long) ((JSONArray) this.featureDict.get(trigram)).get(j)).longValue()));
                    }
                }
            }
        }
        if (words.length > 3) {
            for (j = 0; j < this.emotions.size(); j++) {
                for (i = 0; i < words.length - 3; i++) {
                    String quadgram = words[i] + words[i + 1] + words[i + 2] + words[i + 3];
                    if (this.featureDict.containsKey(quadgram)) {
                        values.put(Integer.valueOf(j), Long.valueOf(((Long) values.get(Integer.valueOf(j))).longValue() + ((Long) ((JSONArray) this.featureDict.get(quadgram)).get(j)).longValue()));
                    }
                }
            }
        }
        for (String stem : words) {
            String stem2 = this.stemmer.stem(stem);
            if (this.featureDict.containsKey("stem=" + stem2)) {
                for (j = 0; j < this.emotions.size(); j++) {
                    values.put(Integer.valueOf(j), Long.valueOf(((Long) values.get(Integer.valueOf(j))).longValue() + ((Long) ((JSONArray) this.featureDict.get("stem=" + stem2)).get(j)).longValue()));
                }
            }
        }
        String[] negations = new String[]{"no", "not", "nor", "neither", "n't"};
        i = 0;
        while (i < words.length) {
            for (j = 0; j < negations.length; j++) {
                if (words[i].equals(negations[j]) || words[i].contains("n't")) {
                    for (x = 0; x < this.emotions.size(); x++) {
                        values.put(Integer.valueOf(x), Long.valueOf(((Long) values.get(Integer.valueOf(x))).longValue() + ((Long) ((JSONArray) this.featureDict.get("NEG=" + negations[j])).get(j)).longValue()));
                    }
                }
            }
            i++;
        }
        String[] conjuncts = new String[0];
        i = 0;
        while (i < words.length) {
            for (j = 0; j < conjuncts.length; j++) {
                if (words[i].equals(conjuncts[j]) && words.length > 2 && i > 0 && i < words.length - 1) {
                    for (x = 0; x < this.emotions.size(); x++) {
                        values.put(Integer.valueOf(x), Long.valueOf(((Long) values.get(Integer.valueOf(x))).longValue() + ((Long) ((JSONArray) this.featureDict.get("coord=" + words[i - 1] + words[i + 1])).get(j)).longValue()));
                    }
                }
            }
            i++;
        }
        int max = -9999999;
        int maxIndex = 0;
        i = 0;
        while (i < this.emotions.size()) {
            System.out.println(" emotion weight = " + values.get(Integer.valueOf(i)));
            if (((Long) values.get(Integer.valueOf(i))).longValue() > 0 && ((Long) values.get(Integer.valueOf(i))).longValue() > ((long) max)) {
                max = ((Long) values.get(Integer.valueOf(i))).intValue();
                maxIndex = i;
            }
            i++;
        }
        return (String) this.emotions.get(maxIndex);
    }
}
