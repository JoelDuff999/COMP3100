package main;

import main.Connection;
import main.Server;
import main.ServerCluster;

//Sockets
import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
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
    private static final String DOT = ".";
    static boolean verbose;
    static boolean nl;
    static String[] largest = new String[] {"0", "", ""};//Botched hack because GETS Capable is apparently equivalent to GETS Avail
    public static void main(String[] args) throws UnknownHostException, IOException {

        verbose = false;
        HashMap<String,String> parsedArgs = new HashMap<String,String>();
        //Default values
        parsedArgs.put("port", "50000");//ds-server defaults to 500000, so the client will too.
        parsedArgs.put("-a", "atl");//AllToLargest
        parsedArgs.put("syspath", "./ds-system.xml");
        nl = false;

        //This try-catch just checks that ds-system.xml exists and throws an error if it does not.
        //This error will be thrown if no arguments are provided. Ignore it.
        try {
            parsedArgs.put("syspath", args[0]);
            File xml = new File(parsedArgs.get("syspath"));
            if (!xml.exists() || xml.isDirectory()) {
                System.err.println(parsedArgs.get("syspath") + " is not a file!");
                System.err.println("Please open ds-system.xml in the ds-server pre-compiled folder.\n");
                usage();
            }
        } catch (Exception e) {
            System.err.println("\nPath to ds-system.xml not provided. Defaulting to ./ds-system.xml");
            System.err.println("This is expected for automated marking scripts which pass no arguments and will not impact functionality.");
            //e.printStackTrace();
            System.err.println();
            //usage();
        }

        //Argument parsing. See usage or technical documentation for individual details.
        for (int i = 1; i < args.length; i++) {
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
                    usage();
               	break;

                case "-n":
                    nl = true;
                    break;

                default:
                    //Debug string. Should not occur.
                    System.err.println("Default called for " + args[i]);
                    break;
            }
        }

        //If no ds-system.xml is parsed, print usage.
        if (parsedArgs.get("syspath").equals("")) { usage(); };

        //Connection objects are a nice wrapper for Sockets, BufferedReaders and PrintStreams.
        Connection socket = new Connection("localhost", Integer.parseInt(parsedArgs.get("port")));
        
        String message;
        
        //Handshake
        //Print HELO to the console
        System.out.println("xlarge2".compareTo("xlarge"));
        if (verbose) { System.out.println("C: HELO"); };
        //Send HELO to the server.
        socket.write("HELO", nl);
        //Server should send us one word back (OK), read a word and see if we received OK.
        message = socket.readWord();
        if (!message.equals("OK")) {
            socket.close();
            //Throw an error if we don't get OK.
            throw new IOException("Handshake failed.\n(HELO did not recieve OK, received " + message + " instead.)");
        }
        
        //Print on the console that we received OK and are sending AUTH.
        //Verbosity ignored because this is important.
        if (verbose) { System.out.println("S: OK"); };
        System.out.println("C: AUTH " + System.getProperty("user.name"));
        //Send AUTH
        socket.write("AUTH " + System.getProperty("user.name"), nl);
        //Read response (Should be OK)
        message = socket.readWord();
        if (!message.equals("OK")) {
            socket.close();
            //Throw an error if we don't get OK
            throw new IOException("Handshake failed.\n(AUTH refused. OK expected, " + message + "received.)");
        }
        System.out.println("S: OK");

        //Parse ds-system.xml
        try {
            File xml = new File(parsedArgs.get("syspath"));
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
            servers.sortedList();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        //Finished parsing.

        //Handshake complete.
        if (verbose) { System.out.println("C: REDY"); };
        socket.write("REDY", nl);

        //Parse server commands.
        boolean working = true;
        while (working) {
            message = socket.readWord();
            if (verbose) { System.out.println("S: " + message); };

            switch (message) {
                case "JOBN":
                    JOBN_Handle(socket);
                    break;

                case "JOBP":
                    JOBN_Handle(socket);
                    break;
                
                case "JCPL": //TODO: Check on this
                    socket.readMSG(4);
                    socket.write("REDY", nl);
                    break;
                
                case "RESF":
                    socket.readMSG(3);
                    break;

                case "RESR":
                    socket.readMSG(3);
                    break;

                case "ERR": //TODO: Check on this
                    break;

                case "NONE":
                    working = false;
                    socket.write("QUIT", nl);
                    if (verbose) { System.out.println("C: QUIT"); };
                    continue;

                default:
                    break;
            }
        }

        message = socket.readWord();
        if (verbose) { System.out.println("S: " + message); };
        socket.close();

        return;
    }

    public static void JOBN_Handle(Connection socket) throws IOException {//TODO Deal with IOException.
        //submitTime, jobID, estRuntime, core, memory, disk
        String[] job = new String[] {
                    socket.readWord(),
                    socket.readWord(),
                    socket.readWord(),
                    socket.readWord(),
                    socket.readWord(),
                    socket.readWord()
        };
        if (verbose) { System.out.println(" " + Arrays.toString(job)); };

        //GET a DATA stream of servers cabable of fulfilling the jobs core, memory and disk requirements.
        socket.write("GETS Capable " + job[3] + " " + job[4] + " " + job[5], nl);
        if (verbose) { System.out.println("C: GETS Capable " + job[3] + " " + job[4] + " " + job[5]); };

        //The server returns metadata about the incoming datastream.
        String[] data = socket.readMSG(3);
        //System.out.println(Arrays.toString(data));
        //int count = Integer.parseInt(socket.readMSG(3)[1]);

        //We are ready to accept the datastream.
        socket.write("OK", nl);

        String message;

        //size,
        //String[] largest = new String[] {"0", "", ""};
        //If the largest server hasn't been found, find the largest server.
        //In practice, this finds the largest server overall by sending GETS when no servers have jobs.
        if (largest[0].equals("0")) {
            String[] temp;
            for (int i = Integer.parseInt(data[1]); i > 0; i--) {
                message = socket.readWord();
                //System.out.print(message);
                //System.out.print(", ");
                //If the end of the datastream is reached.
                //Signalled by the server sending a period (.)
                //This should never actually be called due to the for loop conditions,
                //but I'm not going to delete it with so little time to test.
                if (message.equals(".")) {
                    if (verbose) {
                        System.out.println("C: OK");
                    }
                    ;
                    socket.write("OK", nl);
                    break;
                }


                temp = socket.readMSG(8);

                //TODO Check for server failure.
                /*if (message.equals("xlarge") || message.equals("xlarge2")) {
                    System.out.println("Largest");
                    System.out.println(Arrays.toString(largest));
                    System.out.println(Arrays.toString(new String[]{temp[3], message, temp[0]}));
                }*/
                if (Integer.parseInt(temp[3]) > Integer.parseInt(largest[0]) || (Integer.parseInt(temp[3]) == Integer.parseInt(largest[0]) && message.compareTo(largest[1]) < 0)) {
                    largest[0] = temp[3];
                    largest[1] = message;
                    largest[2] = temp[0];
                }

//                message = socket.readWord();
//                System.out.print(message);
//                System.out.print(", ");

            }
        } else {
            for (int i = Integer.parseInt(data[1]); i > 0; i--) {
                socket.readMSG(9);
            }
        }

        //Manually ignoring "." because wtf was I thinking but it's too late to fix now.
        if (verbose) { System.out.println("C: OK"); };
        socket.write("OK", nl);
        message = socket.readWord();
        //System.out.println(message);

        //SCHeDule a job to the largest server.
        if (verbose) { System.out.println("C: SCHD " + job[1] + " " + largest[1] + " 0"); };
        socket.write("SCHD " + job[1] + " " + largest[1] + " 0", nl);// + largest[2]);

        message = socket.readWord();
        if (verbose) { System.out.println("S: " + message); };

        if (verbose) { System.out.println("C: REDY"); };
        socket.write("REDY", nl);

//        if (message.equals("OK")) {
//
//        }

    }

    public static void usage() {
        System.out.println("Usage:\n\tjava main.ds_client_test path-to-ds-system.xml [options]");
        System.out.println("\nOptions:");
        System.out.println("\t-p port\tSets the port the client is to connect to. (Default being port 50,000)");
        System.out.println("\t-a\tSets the algorithm that will be used by the client. (Default is set to All-to-Largest");
        System.out.println("\t-v\tSets the communication between the Client and Server to Visible/Hidden. (Default is Visible)");
        //System.out.println("\t-path: Sets the Path to System.xml document.");
        System.out.println("\t-n\tNewline terminated message mode.");
        System.exit(0);
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
