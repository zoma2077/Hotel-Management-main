package com.cse241.hotel.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

final class ClientHandler {

    private final Socket socket;
    private final ChatServer server;

    private volatile String username;
    private volatile ChatRole role;

    private PrintWriter out;
    private Thread thread;

    ClientHandler(Socket socket, ChatServer server) {
        this.socket = Objects.requireNonNull(socket);
        this.server = Objects.requireNonNull(server);
    }

    void start() {
        thread = new Thread(this::run, "chat-client-" + socket.getRemoteSocketAddress());
        thread.setDaemon(true);
        thread.start();
    }

    String getUsername() {
        return username;
    }

    ChatRole getRole() {
        return role;
    }

    void sendLine(String line) {
        PrintWriter writer = out;
        if (writer == null) {
            return;
        }
        synchronized (writer) {
            writer.println(line);
            writer.flush();
        }
    }

    void closeQuietly() {
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    private void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)
        ) {
            this.out = writer;

            String joinLine = in.readLine();
            if (joinLine == null) {
                return;
            }
            if (!handleJoin(joinLine)) {
                sendLine("SYS|Invalid JOIN. Expected JOIN|username|role");
                return;
            }
            server.onClientReady(this);

            String line;
            while ((line = in.readLine()) != null) {
                if (line.startsWith("MSG|")) {
                    String text = unescape(line.substring("MSG|".length()));
                    server.broadcastFrom(this, text);
                } else {
                    sendLine("SYS|Unknown command");
                }
            }
        } catch (IOException ignored) {
            // connection dropped
        } finally {
            closeQuietly();
            server.onClientLeft(this);
        }
    }

    private boolean handleJoin(String line) {
        if (!line.startsWith("JOIN|")) {
            return false;
        }
        String[] parts = line.split("\\|", 3);
        if (parts.length != 3) {
            return false;
        }
        String rawUsername = parts[1].trim();
        String rawRole = parts[2].trim();
        if (rawUsername.isEmpty() || rawUsername.length() > 32) {
            return false;
        }
        ChatRole parsedRole;
        try {
            parsedRole = ChatRole.valueOf(rawRole);
        } catch (IllegalArgumentException e) {
            return false;
        }
        this.username = rawUsername;
        this.role = parsedRole;
        return true;
    }

    private static String unescape(String s) {
        if (s == null) {
            return "";
        }
        // Match ChatServer.escape
        return s.replace("\\p", "|").replace("\\n", "\n").replace("\\\\", "\\");
    }
}

