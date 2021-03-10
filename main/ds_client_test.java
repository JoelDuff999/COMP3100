package main;

import main.Connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.io.InputStreamReader;
import java.io.PrintStream;

public class ds_client_test {
    public static void main(String[] args) throws UnknownHostException, IOException {
    	//TODO MANDATORY. Accept -a argument (scheduling algorithm name).
    	//TODO MANDATORY Accept -p argument (port number).
		
		HashMap<String,String> parsedArgs = new HashMap<String,String>();
		//Default values
		parsedArgs.put("port", "50000");//
		parsedArgs.put("-a", "atl");//AllToLargest
    	
		for (int i = 0; i < args.length; i++) {
			switch(args[i]) {
				case "-p":
					i++;
					parsedArgs.put("port", args[i]);
					break;
				case "-a":
					i++;
					parsedArgs.put("-a", args[i]);
					break;
				default:
					System.err.println("Default called for " + args[i]);
					break;
			}
		}
		
    	/*Socket socket = new Socket("localhost", Integer.parseInt(parsedArgs.get("-p")));
    	PrintStream out = new PrintStream(socket.getOutputStream());
    	BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));*/
		
		Connection socket = new Connection("localhost", Integer.parseInt(parsedArgs.get("port")));
		
    	String message;
    	
    	//Handshake
    	System.out.println("C: HELO");
    	socket.write("HELO");
    	message = socket.readWord();
    	if (!message.equals("OK")) {
    		socket.close();
    		throw new IOException("Handshake failed.\n(HELO did not recieve OK, received " + message + " instead.)");
    	}
    	System.out.println("S: OK\nC: AUTH Group_21");
    	socket.write("AUTH Group_21");
    	message = socket.readWord();
    	if (!message.equals("OK")) {
    		socket.close();
    		throw new IOException("Handshake failed.\n(AUTH refused. OK expected, " + message + "received.)");
    	}
    	System.out.println("S: OK");
    	
    	//TODO Read ds-system.xml
    	
    	//System.out.println("C: REDY");
    	//socket.write("REDY");
    	
    	socket.close();
        return;
    }
    
    /**
	 * Defunct
     * Assumes the first character is a letter in order to enforce blocking.
     */
    public static String readWord(BufferedReader in) throws IOException {
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
    
    /**
     * BLOCKING
     * <br>
     * A simplified way of checking that the server returns OK. Consumes input, so maybe don't use this? We'll have to see if OK always comes after a request.
     * 
     * @param in The server message stream.
     * @return If the next message from the server is "OK".
     *//*
    public static boolean ok(BufferedReader in) throws IOException {
    	return readWord(in).equals("OK");
    }*/
}
