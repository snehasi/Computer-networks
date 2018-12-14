import java.io.*;

public class Serializer {
    public static byte[] convertToBytes (Packet packet) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeObject(packet);
        return bos.toByteArray();
    }

    public static Packet convertToPacket(byte [] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = new ObjectInputStream(bis);
        return (Packet) in.readObject();
    }
}
