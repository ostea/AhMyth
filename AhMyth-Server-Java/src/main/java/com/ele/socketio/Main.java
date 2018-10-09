package com.ele.socketio;

import com.ele.socketio.server.ConnectionServer;

public class Main {
    public static void main(String[] args) {
        ConnectionServer connectionServer = new ConnectionServer();
        connectionServer.start();
        connectionServer.interactive();
    }
}
