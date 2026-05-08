package com.cse241.hotel.net;

/**
 * Demo entry point to run the chat server standalone.
 *
 * <p>Run: {@code mvn -DskipTests javafx:run} is for the UI app; for server, run
 * this main from your IDE, or via Maven exec if you add that plugin.</p>
 */
public final class ChatServerMain {

    private ChatServerMain() {
    }

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 5555;
        ChatServer server = new ChatServer(port);
        server.start();
        System.out.println("Chat server running on port " + port);
        Thread.currentThread().join();
    }
}

