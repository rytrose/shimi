package mr1.robots.travis.music;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.util.List;
import mr1.research.music.BeatPlayer;
import mr1.research.music.BeatPlayer.iOnBeatListener;
import mr1.research.music.ClassBeatPlayer;
import mr1.research.music.FileBeatPlayer;
import mr1.research.music.RandomBeatPlayer;

public class TravisAudioPlayback {
    private static final String TAG = "TravisAudioPlayback";
    private final String BASEPATH;
    private final String BCPATH;
    private final String BEATSPATH;
    /* renamed from: D */
    private boolean f13D = true;
    private BeatPlayer beatPlayer;
    private MediaPlayer chosenSongPlayer;
    private ClassBeatPlayer classBeatPlayer;
    private Context context;
    private SongData currentSong;
    private FileBeatPlayer fileBeatPlayer;
    private boolean playing = false;
    private RandomBeatPlayer randomBeatPlayer;
    private List<SongData> songs;

    public static class SongData {
        public final long bias;
        public final String fileNumber;
        public final int startPos;
        public final float tempo;
        public final float volume;

        public SongData(String fileNumber, float tempo, long bias) {
            this(fileNumber, tempo, 1.0f, bias, 0);
        }

        public SongData(String fileNumber, float tempo, float volume, long bias, int startPos) {
            this.fileNumber = fileNumber;
            this.tempo = tempo;
            this.volume = volume;
            this.bias = bias;
            this.startPos = startPos;
        }
    }

    public TravisAudioPlayback(Activity a, String basePath, String bcDir, String beatsDir) {
        this.context = a.getApplicationContext();
        this.BASEPATH = basePath;
        this.BCPATH = new StringBuilder(String.valueOf(basePath)).append(bcDir).toString();
        this.BEATSPATH = new StringBuilder(String.valueOf(basePath)).append(beatsDir).toString();
        this.classBeatPlayer = new ClassBeatPlayer();
        this.fileBeatPlayer = new FileBeatPlayer();
        this.randomBeatPlayer = new RandomBeatPlayer();
    }

    public void addClass(long tempo, long bias) {
        this.classBeatPlayer.addClass(tempo, bias);
    }

    public void setSongList(List<SongData> list) {
        this.songs = list;
    }

    public void chooseClassSong(int c) {
        this.chosenSongPlayer = createMediaPlayer(this.BCPATH + c);
        this.classBeatPlayer.choose(c, this.chosenSongPlayer.getDuration());
        this.beatPlayer = this.classBeatPlayer;
    }

    public void chooseSongFromTempo(float tempo) {
        float min = Float.MAX_VALUE;
        int songIndex = -1;
        for (int i = 0; i < this.songs.size(); i++) {
            float dif = Math.abs(tempo - ((SongData) this.songs.get(i)).tempo);
            if (dif < min) {
                min = dif;
                songIndex = i;
            }
        }
        Log.i(TAG, "Chosen song index " + songIndex);
        chooseSong(songIndex);
    }

    public void chooseSongRandom(int number) {
        this.currentSong = (SongData) this.songs.get(number);
        String baseName = this.currentSong.fileNumber;
        this.randomBeatPlayer.load(this.BEATSPATH + baseName + ".txt", this.currentSong.bias);
        this.beatPlayer = this.randomBeatPlayer;
        this.chosenSongPlayer = createMediaPlayer(this.BASEPATH + baseName);
        if (this.chosenSongPlayer == null) {
            Toast.makeText(this.context, "Couldn't create player from " + baseName, 0).show();
        }
    }

    public void chooseSong(int number) {
        this.currentSong = (SongData) this.songs.get(number);
        String baseName = this.currentSong.fileNumber;
        this.fileBeatPlayer.load(this.BEATSPATH + baseName + ".txt", this.currentSong.bias, (long) this.currentSong.startPos);
        this.beatPlayer = this.fileBeatPlayer;
        this.chosenSongPlayer = createMediaPlayer(this.BASEPATH + baseName);
        if (this.chosenSongPlayer == null) {
            Toast.makeText(this.context, "Couldn't create player from " + baseName, 0).show();
        }
    }

    private MediaPlayer createMediaPlayer(String fileName) {
        if (this.f13D) {
            Log.d(TAG, "Creating MP for " + fileName + ".wav");
        }
        return MediaPlayer.create(this.context, Uri.fromFile(new File(new StringBuilder(String.valueOf(fileName)).append(".wav").toString())));
    }

    public void playSong() {
        this.beatPlayer.start(this.chosenSongPlayer);
        if (this.chosenSongPlayer != null) {
            this.chosenSongPlayer.seekTo(this.currentSong.startPos);
            this.chosenSongPlayer.setVolume(this.currentSong.volume, this.currentSong.volume);
            this.chosenSongPlayer.start();
            return;
        }
        Toast.makeText(this.context, "No song to play", 0).show();
    }

    public void stop() {
        if (this.chosenSongPlayer != null) {
            this.chosenSongPlayer.stop();
        }
        if (this.beatPlayer != null) {
            this.beatPlayer.stop();
        }
        this.beatPlayer = null;
    }

    public boolean isPlaying() {
        return this.playing;
    }

    public void setOnSongBeatListener(iOnBeatListener listener) {
        if (this.beatPlayer != null) {
            this.beatPlayer.setBeatListener(listener);
        }
    }
}
