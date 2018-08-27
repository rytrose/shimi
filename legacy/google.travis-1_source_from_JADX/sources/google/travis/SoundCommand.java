package google.travis;

public class SoundCommand {
    String sender;
    Object value1;
    Object value2;

    public SoundCommand(String sender, Object value1) {
        this.value1 = value1;
        this.sender = sender;
    }

    public SoundCommand(String sender, Object value1, Object value2) {
        this.value1 = value1;
        this.sender = sender;
        this.value2 = value2;
    }
}
