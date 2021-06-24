package client_server.client;

import client_server.domain.packet.Packet;
import lombok.Data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Data
public class StoreClientTCP {
    private static final int CLIENT_PORT = 2222;
    private static final int RECONNECT_MAX = 3;

    private Socket socket;
    private Packet response;

    public StoreClientTCP() {
    }

    public Packet sendPacket(byte[] packet) {
        final int reconnect_num = 1;

            try (final Socket socket = new Socket(InetAddress.getByName(null), CLIENT_PORT)) {
                clientTCP(socket,packet);
            }  catch (IOException e) {
                System.out.println("Reconnecting");
                reconnect(packet, reconnect_num);
            }

            return response;
    }

    private void reconnect(byte[] packet, int reconnect_num) {
        try {
            socket = new Socket(InetAddress.getByName(null), CLIENT_PORT);
            socket.setSoTimeout(3_000*reconnect_num);
            clientTCP(socket, packet);
        } catch (IOException e) {
            System.out.println("Reconnecting\tSERVER IS OFFLINE!!!");

            if(reconnect_num == RECONNECT_MAX){
                System.out.println("SERVER IS DEAD:(");
            }
            else{
                reconnect(packet, ++reconnect_num);
            }
        }
    }

    private void clientTCP(Socket socket, byte[] packet) throws IOException {
        final InputStream inputStream = socket.getInputStream();
        final OutputStream outputStream = socket.getOutputStream();

        outputStream.write(packet);
        outputStream.flush();
        Packet packetFromUser = new Packet(packet);

        final byte[] inputMessage = new byte[20000];
        final int messageSize = inputStream.read(inputMessage);
        byte[] fullPacket = new byte[messageSize];
        System.arraycopy(inputMessage, 0, fullPacket, 0, messageSize);

        Packet receivedPacket = new Packet(fullPacket);

        this.response = receivedPacket;

        if(packetFromUser.getbPktId().equals(receivedPacket.getbPktId()))
            System.out.println("CORRECT response!");
        else
            System.out.println("WRONG response!");

        System.out.println("Response from server: " + new String(receivedPacket.getBMsq().getMessage(), StandardCharsets.UTF_8)
                + "\t for user with ID: " + receivedPacket.getSrcId()
                + "\t for packet with ID: " + receivedPacket.getbPktId());
    }
}