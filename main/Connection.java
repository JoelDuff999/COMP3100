package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.io.InputStreamReader;
import java.io.PrintStream;

class Connection {
    
    private Socket socket;
    private PrintStream out;
    private BufferedReader in;
    
    public Connection(String ip, int port) throws IOException, UnknownHostException {
        socket = new Socket(ip, port);
        out = new PrintStream(socket.getOutputStream());
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    
    public void write(String message) {
        out.print(message);
        /*
         * if (-n is set) {
         *     out.println();
         * }
         * 
         */
    }
    
    //Avoid using this.
    public int read() throws IOException {
        return in.read();
    }
    
    /**
     * Assumes the first character is a letter in order to enforce blocking.
     */
    //TODO Update to support -n
    public String readWord() throws IOException {
        String ret = Character.toString((char) in.read());
        int ch;
        while (in.ready()) {
            ch = in.read();
            if (ch == 32) {//Space
                return ret;
            }
            ret = ret + Character.toString((char) ch);
        }
        return ret;
    }
    
    public void close() throws IOException {
        socket.close();
    }
}
