package main;

import main.Connection;
import main.Server;
import main.ServerCluster;

//Sockets
import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.io.InputStreamReader;
import java.io.PrintStream;

//XML Parsing
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.File;

public class ds_client_test {
    public static void main(String[] args) throws UnknownHostException, IOException {
       
        boolean verbose = false;
        HashMap<String,String> parsedArgs = new HashMap<String,String>();
        //Default values
        parsedArgs.put("port", "50000");//
        parsedArgs.put("-a", "atl");//AllToLargest
        parsedArgs.put("syspath", "./ds_system.xml");
        
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

                case "-v":
                    verbose = true;
                    break;

                case "-path":
                    i++;
                    parsedArgs.put("syspath", args[i]);
                    break;
                 
                case "-h":
               	System.out.println("-p : Sets the port the client is to connect to. (Default being port 50,000)");
               	System.out.println("-a : Sets the algorithm that will be used by the client. (Default is set to All-to-Largest");
               	System.out.println("-v : Sets the communication between the Client and Server to Visible/Hidden. (Default is Visible)");
               	System.out.println("-path: Sets the Path to System.xml document.");
               	System.exit(0);
               	break;

                default:
                    //Debug string. Should not occur.
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
        //Print HELO to the console
        System.out.println("C: HELO");
        //Send HELO to the server.
        socket.write("HELO");
        //Server should send us one word back (OK), read a word and see if we received OK.
        message = socket.readWord();
        if (!message.equals("OK")) {
            socket.close();
            //Throw an error if we don't get OK.
            throw new IOException("Handshake failed.\n(HELO did not recieve OK, received " + message + " instead.)");
        }
        
        //Print on the console that we received OK and are sending AUTH.
        System.out.println("S: OK\nC: AUTH Group_21");
        //Send AUTH
        socket.write("AUTH Group_21");
        //Read response (Should be OK)
        message = socket.readWord();
        if (!message.equals("OK")) {
            socket.close();
            //Throw an error if we don't get OK
            throw new IOException("Handshake failed.\n(AUTH refused. OK expected, " + message + "received.)");
        }
        System.out.println("S: OK");
        
        //TODO Read ds-system.xml
        
        try {
            File xml = new File(parsedArgs.get("syspath"));//TODO VERIFY THIS ADDRESS
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xml);
            doc.getDocumentElement().normalize();

            NodeList serverlist = doc.getElementsByTagName("server");
			
			ServerCluster servers = new ServerCluster(serverlist.getLength());
			//System.out.println(serverlist.item(0));
			//System.out.println(serverlist.item(0).getNodeType());
            for (int i = 0; i < serverlist.getLength(); i++) {
                //PARSE SERVERS
				servers.createServer((Element)serverlist.item(i));
            }
        } catch (Exception e) {
            System.err.println(e);
        }
        
        //Finish the handshake.
        System.out.println("C: REDY");
        socket.write("REDY");
        System.out.println(socket.readWord());
        socket.close();

        return;
    }
    
    /**
     * Defunct
     * Assumes the first character is a letter in order to enforce blocking.
     *
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
    }*/
    
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
