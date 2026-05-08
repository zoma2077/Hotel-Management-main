package com.cse241.hotel.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minimal multi-client chat server using {@link ServerSocket} + {@link Socket}.
 *
 * <p>Protocol (one line per message):
 * <ul>
 *   <li>Client -> Server: {@code JOIN|username|role}</li>
 *   <li>Client -> Server: {@code MSG|text}</li>
 *   <li>Server -> Client: {@code FROM|username|role|text}</li>
 *   <li>Server -> Client: {@code SYS|text}</li>
 * </ul>
 */
public final class ChatServer {

    private final int port;
    private final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();

    private volatile boolean running;
    private volatile ServerSocket serverSocket;
    private Thread acceptThread;

    public ChatServer(int port) {
        this.port = port;
    }

    public synchronized void start() throws IOException {
        if (running) {
            return;
        }
        running = true;
        serverSocket = new ServerSocket(port);
        acceptThread = new Thread(this::acceptLoop, "chat-server-accept");
        acceptThread.setDaemon(true);
        acceptThread.start();
    }

    public synchronized void stop() {
        running = false;

        ServerSocket ss = serverSocket;
        serverSocket = null;
        if (ss != null) {
            try {
                ss.close();
            } catch (IOException ignored) {
            }
        }

        for (ClientHandler client : clients) {
            client.closeQuietly();
        }
        clients.clear();
    }

    public boolean isRunning() {
        return running;
    }

    public int getPort() {
        return port;
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket socket = Objects.requireNonNull(serverSocket).accept();
                ClientHandler handler = new ClientHandler(socket, this);
                clients.add(handler);
                handler.start();
            } catch (IOException e) {
                if (running) {
                    // Likely transient accept error; keep loop alive.
                    broadcastSys("Server accept error: " + e.getMessage());
                }
                break;
            }
        }
    }

    void onClientReady(ClientHandler handler) {
        broadcastSys(handler.getUsername() + " joined (" + handler.getRole() + ").");
    }

    void onClientLeft(ClientHandler handler) {
        clients.remove(handler);
        if (handler.getUsername() != null) {
            broadcastSys(handler.getUsername() + " left.");
        }
    }

    void broadcastFrom(ClientHandler sender, String text) {
        String username = sender.getUsername();
        ChatRole role = sender.getRole();
        if (username == null || role == null) {
            return;
        }
        broadcastRaw("FROM|" + escape(username) + "|" + role + "|" + escape(text));
    }

    void broadcastSys(String text) {
        broadcastRaw("SYS|" + escape(text));
    }

    private void broadcastRaw(String line) {
        for (ClientHandler client : clients) {
            client.sendLine(line);
        }
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\n", "\\n").replace("|", "\\p");
    }
}

