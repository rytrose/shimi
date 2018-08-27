package mr1.NLP;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import mr1.robots.travis.moves.gestures.emotions.EmotionConstants;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class NaturalLanguage {
    private final int ANGER = 1;
    private final int DISGUST = 4;
    private final int FEAR = 5;
    private final int HAPPY = 3;
    private final int NEUTRAL = 0;
    private final int SAD = 2;
    private final int SURPRISE = 6;
    private JSONObject[] emotions = new JSONObject[7];
    private Map<Integer, String> mappings = new HashMap();

    public NaturalLanguage() {
        loadEmotionFeatures();
    }

    private void loadEmotionFeatures() {
        this.mappings.put(Integer.valueOf(1), EmotionConstants.ANGRY_EMOTION);
        this.mappings.put(Integer.valueOf(2), "sad");
        this.mappings.put(Integer.valueOf(3), "happy");
        this.mappings.put(Integer.valueOf(4), EmotionConstants.DISGUSTED_EMOTION);
        this.mappings.put(Integer.valueOf(5), EmotionConstants.FEARFUL_EMOTION);
        this.mappings.put(Integer.valueOf(0), "neutral");
        this.mappings.put(Integer.valueOf(6), EmotionConstants.SURPRISED_EMOTION);
        try {
            DataInputStream in = new DataInputStream(NaturalLanguage.class.getResourceAsStream("/mr1/robots/travis/emotionFeatures.json"));
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String data = "";
            while (true) {
                String str = br.readLine();
                if (str == null) {
                    in.close();
                    JSONObject featureDict = (JSONObject) JSONValue.parse(data);
                    this.emotions[1] = (JSONObject) featureDict.get(EmotionConstants.ANGRY_EMOTION);
                    this.emotions[2] = (JSONObject) featureDict.get("sad");
                    this.emotions[3] = (JSONObject) featureDict.get("happy");
                    this.emotions[4] = (JSONObject) featureDict.get(EmotionConstants.DISGUSTED_EMOTION);
                    this.emotions[5] = (JSONObject) featureDict.get(EmotionConstants.FEARFUL_EMOTION);
                    this.emotions[6] = (JSONObject) featureDict.get(EmotionConstants.SURPRISED_EMOTION);
                    this.emotions[0] = (JSONObject) featureDict.get("neutral");
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
        sentence = sentence.toLowerCase();
        String[] words = sentence.split(" ");
        Map<Integer, Long> values = new HashMap();
        for (i = 0; i < this.emotions.length; i++) {
            values.put(Integer.valueOf(i), Long.valueOf(0));
        }
        System.out.println(sentence);
        for (j = 0; j < this.emotions.length; j++) {
            for (i = 0; i < words.length; i++) {
                if (this.emotions[j].containsKey(words[i])) {
                    long temp = ((Long) values.get(Integer.valueOf(j))).longValue() + ((Long) this.emotions[j].get(words[i])).longValue();
                    values.put(Integer.valueOf(j), Long.valueOf(temp));
                }
            }
        }
        if (words.length > 1) {
            for (j = 0; j < this.emotions.length; j++) {
                for (i = 0; i < words.length - 1; i++) {
                    String bigram = words[i] + words[i + 1];
                    if (this.emotions[j].containsKey(bigram)) {
                        temp = ((Long) values.get(Integer.valueOf(j))).longValue() + ((Long) this.emotions[j].get(bigram)).longValue();
                        values.put(Integer.valueOf(j), Long.valueOf(2 * temp));
                    }
                }
            }
        }
        if (words.length > 2) {
            for (j = 0; j < this.emotions.length; j++) {
                for (i = 0; i < words.length - 2; i++) {
                    String trigram = words[i] + words[i + 1] + words[i + 2];
                    if (this.emotions[j].containsKey(trigram)) {
                        temp = ((Long) values.get(Integer.valueOf(j))).longValue() + ((Long) this.emotions[j].get(trigram)).longValue();
                        values.put(Integer.valueOf(j), Long.valueOf(3 * temp));
                    }
                }
            }
        }
        String firstWord = "firstword=" + words[0];
        String lastWord = "lastword=" + words[words.length - 1];
        for (j = 0; j < this.emotions.length; j++) {
            if (this.emotions[j].containsKey(firstWord)) {
                temp = ((Long) values.get(Integer.valueOf(j))).longValue() + ((Long) this.emotions[j].get(firstWord)).longValue();
                values.put(Integer.valueOf(j), Long.valueOf(temp));
            }
            if (this.emotions[j].containsKey(lastWord)) {
                temp = ((Long) values.get(Integer.valueOf(j))).longValue() + ((Long) this.emotions[j].get(lastWord)).longValue();
                values.put(Integer.valueOf(j), Long.valueOf(temp));
            }
        }
        if (words.length > 1) {
            String first2Word = "first2words=" + words[0] + words[1];
            String last2Word = "last2words=" + words[words.length - 1] + words[words.length - 2];
            for (j = 0; j < this.emotions.length; j++) {
                if (this.emotions[j].containsKey(first2Word)) {
                    temp = ((Long) values.get(Integer.valueOf(j))).longValue() + ((Long) this.emotions[j].get(first2Word)).longValue();
                    values.put(Integer.valueOf(j), Long.valueOf(temp));
                }
                if (this.emotions[j].containsKey(last2Word)) {
                    temp = ((Long) values.get(Integer.valueOf(j))).longValue() + ((Long) this.emotions[j].get(last2Word)).longValue();
                    values.put(Integer.valueOf(j), Long.valueOf(temp));
                }
            }
        }
        int max = 0;
        int maxIndex = 0;
        i = 0;
        while (i < this.emotions.length) {
            System.out.println(" emotion weight = " + values.get(Integer.valueOf(i)));
            if (i > 0 && ((Long) values.get(Integer.valueOf(i))).longValue() > ((long) max)) {
                max = ((Long) values.get(Integer.valueOf(i))).intValue();
                maxIndex = i;
            }
            i++;
        }
        return (String) this.mappings.get(Integer.valueOf(maxIndex));
    }
}
