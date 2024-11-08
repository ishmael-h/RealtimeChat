import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.net.*;

public class ChatClient {
    private String name;
    private JFrame frame;
    private JTextField textField;
    private JTextArea messageArea;
    private BufferedReader reader;
    private PrintWriter writer;
    private Socket socket;

    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
    }

    public ChatClient() {
        // GUI setup
        frame = new JFrame("Harmony");
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        textField = new JTextField(40);
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage(textField.getText());
                textField.setText("");
            }
        });
        panel.add(textField, BorderLayout.SOUTH);

        messageArea = new JTextArea(8, 40);
        messageArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(messageArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        frame.getContentPane().add(panel);
        frame.pack();

        // Connect to server
        try {
            socket = new Socket("localhost", 8818); // Replace with server IP if needed
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            // Handle name submission
            String line = reader.readLine();
            if (line.startsWith("SUBMITNAME")) {
                name = JOptionPane.showInputDialog(frame, "Enter your name:");
                writer.println(name);
            }

            // Start a new thread to listen for messages
            Thread t = new Thread(new IncomingReader());
            t.start();
        } catch (IOException e) {
            System.out.println("Error connecting to server: " + e.getMessage());
        }
    }

    private void sendMessage(String message) {
        writer.println(message);
    }

    private class IncomingReader implements Runnable {
        public void run() {
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("MESSAGE")) {
                        messageArea.append(line.substring(8) + "\n");
                    }
                }
            } catch (IOException e) {
                    System.out.println("Error reading from server: " + e.getMessage());
            }
        }
    }
}
