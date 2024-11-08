import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private static final int PORT = 8818; // Choose a port number
    private Set<PrintWriter> clientWriters = new HashSet<>();

    public static void main(String[] args) throws Exception {
        System.out.println("The chat server is running.");
        new ChatServer().start();
    }

    public void start() throws Exception {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected.");
                ClientHandler handler = new ClientHandler(clientSocket);
                Thread t = new Thread(handler);
                t.start();
            }
        }
    }

    private class ClientHandler implements Runnable {
        private String name;
        private Socket socket;
        private BufferedReader reader;
        private PrintWriter writer;

        public ClientHandler(Socket clientSocket) {
            this.socket = clientSocket;
        }

        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);

                // Get the client's name
                writer.println("SUBMITNAME");
                name = reader.readLine();
                synchronized (clientWriters) {
                    for (PrintWriter writer : clientWriters) {
                        writer.println("MESSAGE " + name + " has joined");
                    }
                    clientWriters.add(writer);
                }

                // Broadcast messages
                String message;
                while ((message = reader.readLine()) != null) {
                    System.out.println("Received: " + message);
                    synchronized (clientWriters) {
                        for (PrintWriter writer : clientWriters) {
                            writer.println("MESSAGE " + name + ": " + message);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error handling client: " + e.getMessage());
            } finally {
                // Remove client when they disconnect
                if (name != null) {
                    synchronized (clientWriters) {
                        clientWriters.remove(writer);
                        for (PrintWriter writer : clientWriters) {
                            writer.println("MESSAGE " + name + " has left");
                        }
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error closing socket: " + e.getMessage());
                }
            }
        }
    }
}
