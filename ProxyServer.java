// Anna Jackson
// CMPT 352
// Homework 3

// a forward proxy server

import java.io.*;
import java.net.*;

public class ProxyServer {
  static final int PORT = 8080;
  
  public static void main(String[] args) throws IOException {
    ServerSocket sock = null;
    try {
      sock = new ServerSocket(PORT); // creates server socket bound to port 8080
      while(true){
        Socket client = sock.accept(); // creates client socket and listens for connections
        Client clientThread = new Client(client);
        Thread thread = new Thread(clientThread);
        thread.start(); // runs client process in its own thread
      }
    }
    catch(IOException e){
    }
    finally {
      if (sock != null)
        sock.close(); // close the socket
    }
  }
}

class Client implements Runnable {
  Socket client;
  static BufferedReader reader;
  
  public Client(Socket clientThread){
    client = clientThread;
  }
  public void run(){
    try {
      process();
    }
    catch (IOException e){}
  }
  
  public void process() throws IOException {
    
    reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
    String line = reader.readLine();
    String[] temp = line.split(" ");
    line = temp[1];
    temp = line.split("/", 3);
    if (temp[0] != "GET") // if not a GET request, closes socket
      client.close();
    else {
      // reads and parses data from client
      String origin_host = temp[1];
      String resource;
      if (temp.length == 3)
        resource = temp[2];
      else
        resource = "";
      String get = "GET /" + resource + " HTTP/1.1\r\n" + // constructs GET request as a string
        "Host: " + origin_host + "\r\n" +
        "Connection: close\r\n\r\n";
      
      // writes GET request to origin
      Socket sock = new Socket(origin_host, 80); // creates socket bound to origin
      DataOutputStream sourceOutput = new DataOutputStream(sock.getOutputStream());
      sourceOutput.writeBytes(get);
      
      // receives data from origin and writes data to client
      DataOutputStream output = new DataOutputStream(client.getOutputStream());
      InputStream sourceInput = sock.getInputStream();
      int data;
      while ((data = sourceInput.read()) != -1)
        output.write(data);
      
      // close both sockets
      client.close();
      sock.close();
    }
  }
}