package main;

import org.w3c.dom.Element;
import java.util.*;

public class ServerCluster {

    //Discuss which structure to use.
    //I'm partial to arrays because we know how many servers there are,
    //and if we sort them we can directly access them based on largest first priority.
    private List<Server> servers;
    private List<Integer> sorted;

    public ServerCluster(int size) {
        servers = new ArrayList<Server>(size);

    }

    public void createServer(Element details) {
        servers.add(new Server(details));
    }

    public void sortedList(){
        /*While (servers != null) {

        }*/
        sorted = new ArrayList<Integer>(servers.size());
        for (Server s : servers) {
            System.out.println(s.coreCount);
            for (int i = 0; i < servers.size(); i++){
                System.out.println(i);
                if (i > sorted.size() || s.coreCount > sorted.get(i)){ //might not be null
                    System.out.println("true");
                    sorted.add(i, s.coreCount);
                }
            }
        }
        System.out.println(sorted.toString());
        System.out.println(sorted.size());
    }


}