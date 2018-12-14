import java.io.Serializable;
import java.time.Duration;
import java.time.Instant;

public class Packet implements Serializable{
    public int sequenceNumber;
    public boolean isAck;
    public String data;
    public Instant time;

    public Packet(int sequenceNumber, boolean isAck, String data) {
        this.sequenceNumber = sequenceNumber;
        this.isAck = isAck;
        this.data = data;
        this.time = Instant.now();
    }

    public long getAge() {
        return Duration.between(this.time, Instant.now()).toMillis();
    }

    public void timestamp() {
        this.time = Instant.now();
    }

    public void print() {
        System.out.println("-------------------------------");
        System.out.println("Sequence Number: " + sequenceNumber);
        System.out.println("Is Ack: " + isAck);
        System.out.println("Data: " + data);
        System.out.println("-------------------------------");
    }
}
