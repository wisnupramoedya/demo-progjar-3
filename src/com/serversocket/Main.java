package com.serversocket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

public class Main {
    /**
     * Run server socket
     *
     */
    public static void main(String[] args) {
        try {
            ConfigService configService = new ConfigService();
            InetAddress address = InetAddress.getByName(configService.getIP());
            ServerSocket serverSocket = new ServerSocket(configService.getPort(), 50, address);
            System.out.println("Server started: http://" + configService.getIP() + ":" + configService.getPort());

            while (true) {
                ClientServer client = new ClientServer(serverSocket.accept(), configService);
                client.serve();
            }

        } catch (Exception e) {
            System.err.println("Configuration error: " + e.getMessage());
        }
    }
}
