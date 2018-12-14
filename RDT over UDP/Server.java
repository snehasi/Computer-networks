import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;
import org.apache.commons.lang.SerializationUtils;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

public class Server {
    public int LISTEN_PORT = 8000;
    public int CLIENT_PORT = 8001;

    public DatagramSocket datagramSocket;
    public InetAddress localhost;

    public int lastSeqNum = 0;

    public ArrayList<Packet> packetBuffer;
    public ArrayList<Integer> sequenceBuffer;

    public ArrayList<Packet> data;

    public ArrayList<Integer> sendBuffer;

    public boolean transferComplete = false;

    public Lock recvLock;
    public Lock sendLock;

    public static void main(String[] args) throws SocketException, UnknownHostException, InterruptedException {
        Server server = new Server();

        server.run();
    }

    public Server () throws SocketException, UnknownHostException {
        datagramSocket = new DatagramSocket(LISTEN_PORT);

        localhost = InetAddress.getLocalHost();

        packetBuffer = new ArrayList<>();
        sequenceBuffer = new ArrayList<>();

        data = new ArrayList<>();

        sendBuffer = new ArrayList<>();

        recvLock = new ReentrantReadWriteLock().writeLock();
        sendLock = new ReentrantReadWriteLock().writeLock();
    }

    public void run() throws InterruptedException {
        System.out.println("Starting server");

        Thread recvThread = new Thread(new RecvThread());
        Thread sendThread = new Thread(new SendThread());
        Thread inOrderThread = new Thread(new InOrderThread());

        recvThread.start();
        sendThread.start();
        inOrderThread.start();

        recvThread.join();
        sendThread.join();
        inOrderThread.join();

        System.out.println("Received following data");

        for (int i = 0; i < data.size(); i++)
            System.out.println(data.get(i).data);
    }

    class RecvThread implements Runnable {

        @Override
        public void run() {
            while (!transferComplete) {
                byte [] data = new byte[65535];

                DatagramPacket datagramPacket = new DatagramPacket(data, data.length);

                try {
                    datagramSocket.receive(datagramPacket);

                    Packet packet = Serializer.convertToPacket(data);

                    packet.print();

                    if (packet.sequenceNumber == -1) {
                        transferComplete = true;

                        sendLock.lock();

                        sendBuffer.add(packet.sequenceNumber);

                        sendLock.unlock();
                    }
                    else if (packet.sequenceNumber >= lastSeqNum && !sequenceBuffer.contains(packet.sequenceNumber))
                    {
                        recvLock.lock();

                        packetBuffer.add(packet);
                        sequenceBuffer.add(packet.sequenceNumber);

                        recvLock.unlock();

                        sendLock.lock();

                        sendBuffer.add(packet.sequenceNumber);

                        sendLock.unlock();
                    }
                    else
                    {
                        System.out.println(String.format("Discarding packet %d, already received", packet.sequenceNumber));

                        sendLock.lock();
                        sendBuffer.add(packet.sequenceNumber);
                        sendLock.unlock();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class SendThread implements Runnable {

        @Override
        public void run() {
            while (!transferComplete || !sendBuffer.isEmpty()) {
                sendLock.lock();
                if (!sendBuffer.isEmpty()) {
                    Integer sequenceNumber = sendBuffer.remove(0);
                    sendLock.unlock();

                    Packet packet = new Packet(sequenceNumber, true, "");

                    try {
                        byte [] data = Serializer.convertToBytes(packet);

                        DatagramPacket datagramPacket = new DatagramPacket(data, data.length, localhost, CLIENT_PORT);

                        datagramSocket.send(datagramPacket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    sendLock.unlock();
                }
            }
        }
    }

    class InOrderThread implements Runnable {
        @Override
        public void run() {
            while (!transferComplete || !packetBuffer.isEmpty()) {
                recvLock.lock();

                while (sequenceBuffer.contains(lastSeqNum + 1)) {
                    for (int i = 0; i < packetBuffer.size(); i++) {
                        if (packetBuffer.get(i).sequenceNumber == lastSeqNum + 1) {
                            data.add(packetBuffer.remove(i));

                            lastSeqNum++;
                            break;
                        }
                    }
                }

                recvLock.unlock();
            }
        }
    }
}
