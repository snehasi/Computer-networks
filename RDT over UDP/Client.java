import org.apache.commons.lang.SerializationUtils;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.Thread.*;

public class Client {
    public int SERVER_PORT = 8000;
    public int LISTEN_PORT = 8001;

    public DatagramSocket datagramSocket;
    public InetAddress localhost;

    public ArrayList<Packet> toSend;
    public ArrayList<Packet> inTransit;

    public ArrayList<Packet> sendBuffer;

    public long timeout;
    public int windowSize;

    public boolean transferComplete;

    public ReentrantLock recvLock;
    public ReentrantLock sendLock;

    public static void main(String[] args) throws SocketException, UnknownHostException, InterruptedException {
        Client client = new Client();

        client.run();
    }

    public Client() throws SocketException, UnknownHostException {
        datagramSocket = new DatagramSocket(LISTEN_PORT);

        localhost = InetAddress.getLocalHost();

        toSend = new ArrayList<>();
        inTransit = new ArrayList<>();

        sendBuffer = new ArrayList<>();

        timeout = 1000;

        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter window size: ");

        windowSize = scanner.nextInt();

        transferComplete = false;

        recvLock = new ReentrantLock();
        sendLock = new ReentrantLock();
    }

    public void run() throws InterruptedException {
        for (int i = 0; i < 1000; i++)
            toSend.add(new Packet(i + 1, false, String.format("Message %d", i + 1)));

        Thread sendThread = new Thread(new SendThread());
        Thread recvThread = new Thread(new RecvThread());

        Thread windowThread = new Thread(new WindowThread());
        Thread timeoutThread = new Thread(new TimeoutThread());

        sendThread.start();
        recvThread.start();

        windowThread.start();
        timeoutThread.start();

        sendThread.join();
        recvThread.join();
        windowThread.join();
        timeoutThread.join();

        System.out.println("Transfer complete");
    }

    class SendThread implements Runnable {
        @Override
        public void run() {
            while (!transferComplete) {
                sendLock.lock();
                if (!sendBuffer.isEmpty()) {
                    Packet packet = sendBuffer.remove(0);
                    sendLock.unlock();

                    try {
                        byte [] data = Serializer.convertToBytes(packet);

                        DatagramPacket datagramPacket = new DatagramPacket(data, data.length, localhost, SERVER_PORT);

                        datagramSocket.send(datagramPacket);
                    } catch (IOException e) {
                        sendBuffer.add(0, packet);
                    }
                }
                else {
                    sendLock.unlock();
                }
            }

        }
    }

    class RecvThread implements Runnable {
        @Override
        public void run() {
            while (!transferComplete) {
                byte [] data = new byte[65535];

                DatagramPacket datagramPacket = new DatagramPacket(data, data.length);

                try {
                    datagramSocket.setSoTimeout((int) timeout);
                    datagramSocket.receive(datagramPacket);

                    Packet packet = (Packet) Serializer.convertToPacket(data);

                    if (packet.isAck) {
                        System.out.println("Received Ack: " + packet.sequenceNumber);

                        recvLock.lock();

                        for (int i = 0; i < inTransit.size(); i++) {
                            if (inTransit.get(i).sequenceNumber == packet.sequenceNumber) {
                                inTransit.remove(i);

                                break;
                            }
                        }
                        recvLock.unlock();
                    }

                } catch (IOException e) {
                } catch (ClassNotFoundException e) {
                }
            }
        }
    }

    class WindowThread implements Runnable {
        @Override
        public void run() {
            while (!toSend.isEmpty()) {
                recvLock.lock();
                sendLock.lock();

                while (inTransit.size() < windowSize && !toSend.isEmpty()) {
                    Packet packet = toSend.remove(0);

                    packet.timestamp();

                    inTransit.add(packet);
                    sendBuffer.add(packet);
                }

                sendLock.unlock();
                recvLock.unlock();
            }

            Packet packet = new Packet(-1, false, "");

            packet.timestamp();

            recvLock.lock();
            sendLock.lock();

            inTransit.add(packet);
            sendBuffer.add(packet);

            sendLock.unlock();
            recvLock.unlock();

            while (!inTransit.isEmpty());

            transferComplete = true;
        }
    }

    class TimeoutThread implements Runnable {
        @Override
        public void run() {
            while (!transferComplete) {
                recvLock.lock();
                sendLock.lock();

                for (int i = 0; i < inTransit.size(); i++) {
                    Packet packet = inTransit.get(i);

                    if (packet.getAge() >= timeout)
                    {
                        System.out.println(String.format("Packet %d timed out", packet.sequenceNumber));
                        packet.timestamp();

                        inTransit.set(i, packet);

                        sendBuffer.add(packet);
                    }
                }

                recvLock.unlock();
                sendLock.unlock();

                try {
                    sleep(timeout);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
