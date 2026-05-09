package com.cse241.hotel.net;


public final class ChatServerMain {

    private ChatServerMain() {
    }

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 6666;
        ChatServer server = new ChatServer(port);
        server.start();
        System.out.println("Chat server running on port " + port);
        Thread.currentThread().join();
    }
}

