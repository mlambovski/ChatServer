/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author Myro
 */
public class Server {

    private static int uniqueId;
    ServerSocket serverSocket;
    private ArrayList<ClientHandler> clientList;
    private ServerGUI gui;
    private SimpleDateFormat timeStamp;
    private int port;
    private boolean keepGoing;

    public Server(int port, ServerGUI gui) {
        this.gui = gui;
        this.port = port;
        timeStamp = new SimpleDateFormat("HH:mm:ss");
        clientList = new ArrayList<ClientHandler>();
    }

    public void runServer() {
        keepGoing = true;
        try {
            serverSocket = new ServerSocket(port, 15);
            waitForConnection();
            closeConnection();
        } catch (IOException e) {
            String msg = timeStamp.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
            display(msg);
        }
    }

    private void waitForConnection() throws IOException {
        while (keepGoing) {
            display("Server waiting for Clients on port " + port + ".");
            Socket socket = serverSocket.accept();
            if (!keepGoing) {
                break;
            }
            ClientHandler connectingClient = new ClientHandler(socket);
            for (int i = 0; i < clientList.size(); i++) {
                if (connectingClient.username.equals(clientList.get(i).username)) {
                    connectingClient.writeMessage(timeStamp.format(new Date()) + " Username " + connectingClient.username + " already exists!\n");
                    connectingClient.closeConnection();
                }
            }
            clientList.add(connectingClient);
            connectingClient.start();
        }
    }

    private void closeConnection() {
        try {
            serverSocket.close();
            for (int i = 0; i < clientList.size(); ++i) {
                ClientHandler tc = clientList.get(i);
                try {
                    tc.input.close();
                    tc.output.close();
                    tc.socket.close();
                } catch (IOException ioE) {

                }
            }
        } catch (Exception e) {
            display("Exception closing the server and clients: " + e);
        }
    }

    protected void stop() {
        keepGoing = false;
        try {
            new Socket("localhost", port);
        } catch (Exception e) {

        }
    }

    private void display(String messagge) {
        String time = timeStamp.format(new Date()) + " " + messagge;
        if (gui == null) {
            System.out.println(time);
        } else {
            gui.appendEvent(time + "\n");
        }
    }

    private synchronized void sendToRecipient(String message, String recipient) {
        String time = timeStamp.format(new Date());
        String messageLf = time + " " + message + "\n";

        for (int i = clientList.size(); --i >= 0;) {
            ClientHandler ct = clientList.get(i);
            if (recipient.equals(clientList.get(i).username)) {
                if (!ct.writeMessage(messageLf)) {
                    clientList.remove(i);
                    display("Disconnected Client " + ct.username + " removed from list.");
                }
            }
        }
    }

    private synchronized void broadcast(String message) {
        String time = timeStamp.format(new Date());
        String messageLf = time + " " + message + "\n";
        for (int i = clientList.size(); --i >= 0;) {
            ClientHandler ct = clientList.get(i);
            if (!ct.writeMessage(messageLf)) {
                clientList.remove(i);
                display("Disconnected Client " + ct.username + " removed from list.");
            }
        }
    }

    synchronized void removeClient(int clientID) {
        for (int i = 0; i < clientList.size(); ++i) {
            ClientHandler ct = clientList.get(i);
            if (ct.clientID == clientID) {
                clientList.remove(i);
                return;
            }
        }
    }

    class ClientHandler extends Thread {

        Socket socket;
        ObjectInputStream input;
        ObjectOutputStream output;
        int clientID;
        String username;
        ChatMessage chatMessage;
        String date;
        int fileTransferPort;

        ClientHandler(Socket socket) {
            clientID = ++uniqueId;
            this.socket = socket;
            System.out.println("Thread trying to create Object Input/Output Streams");
            try {
                output = new ObjectOutputStream(socket.getOutputStream());
                input = new ObjectInputStream(socket.getInputStream());
                String loginInfo = (String) input.readObject();
                String[] splitted = loginInfo.split("\\s+");
                username = splitted[0];
                fileTransferPort = Integer.parseInt(splitted[1]);
                display(username + " just connected.");
            } catch (IOException e) {
                display("Exception creating new Input/output Streams: " + e);
                return;
            } catch (ClassNotFoundException e) {
            }
            date = new Date().toString() + "\n";
        }

        public void run() {
            boolean keepGoing = true;
            while (keepGoing) {
                try {
                    chatMessage = (ChatMessage) input.readObject();
                } catch (IOException e) {
                    display(username + " Exception reading Streams: " + e);
                    break;
                } catch (ClassNotFoundException e2) {
                    break;
                }
                String message = chatMessage.getMessage();

                switch (chatMessage.getType()) {

                    case ChatMessage.MESSAGE:
                        if ("All".equals(chatMessage.getRecipient())) {
                            broadcast(username + ": " + message);
                            break;
                        } else {
                            sendToRecipient(username + ": " + message, chatMessage.getRecipient());
                            break;
                        }
                    case ChatMessage.LOGOUT:
                        display(username + " disconnected with a LOGOUT message.");
                        broadcast(username + " disconnected.");
                        keepGoing = false;
                        break;
                    case ChatMessage.USER_LIST:
                        writeMessage("List of the users connected:\n");
                        for (int i = 0; i < clientList.size(); ++i) {
                            ClientHandler handler = clientList.get(i);
                            writeMessage((i + 1) + ") " + handler.username + " " + handler.fileTransferPort + "\n");
                        }
                        break;
                    case ChatMessage.FILE:
                        sendToRecipient("You are receiving a file from: " + username + " ; File name: " + chatMessage.getFileName(), chatMessage.getRecipient());
                }
            }
            removeClient(clientID);
            closeConnection();
        }

        private void closeConnection() {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (Exception e) {
            }
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception e) {
            };
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (Exception e) {
            }
        }

        private boolean writeMessage(String message) {
            if (!socket.isConnected()) {
                closeConnection();
                return false;
            }
            try {
                output.writeObject(message);
            } catch (IOException e) {
                display("Error sending message to " + username);
                display(e.toString());
            }
            return true;
        }
    }
}
