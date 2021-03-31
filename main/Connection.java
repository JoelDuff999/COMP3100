package main;

import java.io.BufferedReader;
import java.net.ConnectException;
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
		try {
			socket = new Socket(ip, port);
		} catch (ConnectException e) {
			System.err.println("Connection refused on address " + ip + " and port " + port + ".\nIs the server running with these settings?");
			System.exit(-1);
		}
			
        out = new PrintStream(socket.getOutputStream());
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }
    
    public void write(String message) {
        out.print(message);
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
            if (ch == 32 || ch == 10) {//Space or newline
                return ret;
            }
            ret = ret + Character.toString((char) ch);
        }
        return ret;
    }

    /**
     * Reads several words.
     * Does not have to read an entire message, but that is the intended use.
     * @param len The length of the message to read, in words.
     * @return The message in an array of length len.
     */
    public String[] readMSG(int len) throws IOException {
        String[] out = new String[len];
        for (int i = 0; i < len; i++) {
            out[i] = readWord();
        }
        return out;
    }
    
    public void close() throws IOException {
        socket.close();
    }
}
