package main;

import org.w3c.dom.Element;

public class ServerCluster {

    //Discuss which structure to use.
    //I'm partial to arrays because we know how many servers there are,
    //and if we sort them we can directly access them based on largest first priority.
    private Server[] servers;

    public ServerCluster(int size) {
        servers = new Server[size];
    }

    public void createServer(Element details) {
        new Server(details);
    }

}