package client_server.server.serverTCP;
import client_server.handlers.ThreadHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerTCP {

    private static final int SERVER_PORT = 2222;

    public static void main(String[] args) {

        try (final ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {

            while (true) {
                try{
                    Socket socket = serverSocket.accept();

                    final byte[] inputMessage = new byte[20000];
                    final InputStream inputStream = socket.getInputStream();
                    final int messageSize = inputStream.read(inputMessage);

                    new Thread(() -> {
                        try {
                            byte[] fullPacket = new byte[messageSize];
                            System.arraycopy(inputMessage, 0, fullPacket, 0, messageSize);
                            final OutputStream outputStream = socket.getOutputStream();
                            outputStream.write(ThreadHandler.process(fullPacket));

                            outputStream.flush();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}