package com.cisco.ss.grpc.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.net.InetAddress;

public class RecognizeServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        final Server server = ServerBuilder.forPort(55555).addService(new RecognizeServiceImpl()).build();
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                server.shutdown();
                System.out.println("server shutting down");
            }
        });
        String host = InetAddress.getLocalHost().getHostAddress();
        System.out.println("server running at " + host + ":" + 55555);
        server.awaitTermination();

    }
}