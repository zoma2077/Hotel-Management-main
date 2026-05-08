package com.cse241.hotel.net.client;

import com.cse241.hotel.net.ChatRole;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Consumer;

public final class ChatClient {

    public interface Listener {
        void onSystem(String text);

        void onMessage(String fromUsername, ChatRole fromRole, String text);

        void onDisconnected(String reason);
    }

    private final String host;
    private final int port;
    private final String username;
    private final ChatRole role;
    private final Listener listener;

    private volatile Socket socket;
    private volatile PrintWriter out;
    private Thread listenThread;

    public ChatClient(String host, int port, String username, ChatRole role, Listener listener) {
        this.host = Objects.requireNonNull(host);
        this.port = port;
        this.username = Objects.requireNonNull(username);
        this.role = Objects.requireNonNull(role);
        this.listener = Objects.requireNonNull(listener);
    }

    public void connect() throws IOException {
        if (socket != null) {
            return;
        }
        Socket s = new Socket(host, port);
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true);
        this.socket = s;
        this.out = writer;
        writer.println("JOIN|" + username + "|" + role);
        writer.flush();

        listenThread = new Thread(() -> listenLoop(s), "chat-client-listen");
        listenThread.setDaemon(true);
        listenThread.start();
    }

    public void disconnect() {
        Socket s = socket;
        socket = null;
        if (s != null) {
            try {
                s.close();
            } catch (IOException ignored) {
            }
        }
        safeCall(l -> l.onDisconnected("Disconnected"));
    }

    public boolean isConnected() {
        Socket s = socket;
        return s != null && s.isConnected() && !s.isClosed();
    }

    public void sendMessage(String text) {
        if (text == null || text.isBlank()) {
            return;
        }
        PrintWriter writer = out;
        if (writer == null) {
            return;
        }
        synchronized (writer) {
            writer.println("MSG|" + escape(text.trim()));
            writer.flush();
        }
    }

    private void listenLoop(Socket s) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = in.readLine()) != null) {
                handleServerLine(line);
            }
            safeCall(l -> l.onDisconnected("Server closed connection"));
        } catch (IOException e) {
            safeCall(l -> l.onDisconnected("Connection error: " + e.getMessage()));
        } finally {
            try {
                s.close();
            } catch (IOException ignored) {
            }
            socket = null;
            out = null;
        }
    }

    private void handleServerLine(String line) {
        if (line == null) {
            return;
        }
        if (line.startsWith("SYS|")) {
            safeCall(l -> l.onSystem(unescape(line.substring("SYS|".length()))));
            return;
        }
        if (line.startsWith("FROM|")) {
            String[] parts = line.split("\\|", 4);
            if (parts.length < 4) {
                return;
            }
            String fromUsername = unescape(parts[1]);
            ChatRole fromRole;
            try {
                fromRole = ChatRole.valueOf(parts[2]);
            } catch (IllegalArgumentException e) {
                return;
            }
            String text = unescape(parts[3]);
            safeCall(l -> l.onMessage(fromUsername, fromRole, text));
        }
    }

    private void safeCall(Consumer<Listener> c) {
        try {
            c.accept(listener);
        } catch (RuntimeException ignored) {
        }
    }

    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\n", "\\n").replace("|", "\\p");
    }

    private static String unescape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\p", "|").replace("\\n", "\n").replace("\\\\", "\\");
    }
}

