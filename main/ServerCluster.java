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

        //System.out.println(servers.size());
        sorted = new ArrayList<Integer>();
        List<Integer> temp = new ArrayList<Integer>();//Core counts for sorting. Discard when function ends.

        //System.out.println(sorted.size());
        int j = 0;
        for (Server s : servers) {
            //System.out.println(s.coreCount);
            for (int i = 0; i < servers.size(); i++){
                //System.out.println(sorted.toString());
                if (i >= sorted.size()) {
                    temp.add(s.coreCount);
                    sorted.add(j);
                    break;
                }

                if (s.coreCount > temp.get(i)) {
                    temp.add(i, s.coreCount);
                    sorted.add(i, j);
                    break;
                }
            }
            j++;
        }

        /*System.out.println(sorted.toString());
        System.out.println(sorted.size());

        for (Integer i : sorted) {
            System.out.println(servers.get(i).coreCount);
        }*/
    }

}
