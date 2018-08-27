package mr1.core.network;

import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Segment;
import com.echonest.api.v4.Song;
import com.echonest.api.v4.SongParams;
import com.echonest.api.v4.TimedEvent;
import com.echonest.api.v4.Track;
import com.echonest.api.v4.TrackAnalysis;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.List;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class EchoNestQuery {
    protected static final String API_KEY = "T95LOYE9LBGAEDRX3";
    protected static final String BUCKET_TAG = "bucket";
    protected static final String CREATE = "create";
    protected static final String ECHONEST_SERVER = "http://developer.echonest.com/catalog/create";
    protected static final String FORMAT_TAG = "format";
    protected static final String ID_TAG = "id";
    protected static final String KEY_TAG = "api_key";
    protected static final String NAME_TAG = "name";
    protected static final String READ = "read";
    protected static final String TYPE_TAG = "type";
    protected static final String UPLOAD = "upload";
    private TrackAnalysis analysis;
    private EchoNestAPI echoNest = new EchoNestAPI(API_KEY);
    private Song song;
    private String spotifyID;
    private Track track;

    public void analyze(String id) {
        try {
            this.track = this.echoNest.newTrackByID(id);
            this.analysis = this.track.getAnalysis();
        } catch (EchoNestException e1) {
            e1.printStackTrace();
        }
    }

    public void analyzeFromTitleAndArtist(String title, String artist) {
        try {
            SongParams params = new SongParams();
            if (!title.equals("")) {
                params.setTitle(title);
            }
            if (!artist.equals("")) {
                params.setArtist(artist);
            }
            params.includeAudioSummary();
            params.addIDSpace("spotify-WW");
            params.includeTracks();
            params.setLimit(true);
            params.setResults(1);
            params.add("sort", "song_hotttnesss-desc");
            this.song = (Song) this.echoNest.searchSongs(params).get(0);
            this.track = this.song.getTrack("spotify-WW");
            this.spotifyID = this.track.getForeignID();
            this.analysis = this.track.getAnalysis();
        } catch (EchoNestException e1) {
            e1.printStackTrace();
        }
    }

    public void analyzeFromFile(String jsonFile) {
        try {
            JSONParser parser = new JSONParser();
            DataInputStream in = new DataInputStream(new FileInputStream(jsonFile));
            this.analysis = new TrackAnalysis((JSONObject) parser.parse(new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")))));
            in.close();
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    public boolean songExists() {
        if (this.analysis != null) {
            return true;
        }
        return false;
    }

    public double getDuration() {
        return this.analysis.getDuration().doubleValue();
    }

    public List<TimedEvent> getBeats() {
        return this.analysis.getBeats();
    }

    public double getTempo() {
        return this.analysis.getTempo().doubleValue();
    }

    public List<TimedEvent> getSections() {
        return this.analysis.getSections();
    }

    public List<Segment> getSegments() {
        return this.analysis.getSegments();
    }

    public List<TimedEvent> getTatums() {
        return this.analysis.getTatums();
    }

    public String getSpotifyID() {
        return this.spotifyID;
    }
}
