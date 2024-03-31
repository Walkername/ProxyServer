package org.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

public class ProxyServer {
    public static void main(String[] args) throws Exception {
        startProxyServer();
    }

    private static String getMessageContent(BufferedReader in) throws Exception {
        StringBuilder message = new StringBuilder();
        String line;
        while (!Objects.equals(line = in.readLine(), null)) {
            message.append(line).append("\r\n");
            System.out.println(line);
            if (line.equals("0")) {
                break;
            }
        }
        return message.toString();
    }

    private static String connectTo(String host, int port, String request) throws Exception {
        Socket socket = new Socket(host, port);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        out.printf(request);

        String response = getMessageContent(in);

        return response;
    }

    private static void startProxyServer() throws Exception {
        ServerSocket serverSocket = new ServerSocket(1111);

        Socket socket;
        boolean connectionToServer = false;
        String hostName = "";

        while (true) {
            socket = serverSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            String requestFromClient = in.readLine();
            System.out.println("From client: \n" + requestFromClient + "\n");
            String[] elementsOfRequest = requestFromClient.split(" ");

            String reqFile = elementsOfRequest[1];
            if (reqFile.charAt(0) == '/' && reqFile.length() > 1) {
                reqFile = reqFile.substring(1);
            }

            String requestToServer;
            String response;

            if (connectionToServer) {
                requestToServer = "GET /" + reqFile + " HTTP/1.1\r\nHost: " + hostName + "\r\n\r\n";
            }
            else {
                connectionToServer = true;
                hostName = reqFile;
                requestToServer = "GET / HTTP/1.1\r\nHost: " + reqFile + "\r\n\r\n";
            }

            response = connectTo(hostName, 80, requestToServer);
            System.out.println(response + "\n");

            out.println(response);
            in.close();
            out.close();
        }
    }
}