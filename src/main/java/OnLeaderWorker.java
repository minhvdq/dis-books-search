import cluster.management.OnLeaderWorkerElection;
import cluster.management.ServiceRegistry;
import networking.WebClient;
import networking.WebServer;
import org.apache.zookeeper.KeeperException;
import search.SearchCoordinator;
import search.SearchWorker;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class OnLeaderWorker implements OnLeaderWorkerElection {
    private final ServiceRegistry workerServiceRegistry;
    private final ServiceRegistry coordinatorServiceRegistry;
    private final int port;
    private WebServer webServer;
    public OnLeaderWorker(ServiceRegistry workerServiceRegistry, ServiceRegistry coordinatorServiceRegistry, int port){
        this.workerServiceRegistry = workerServiceRegistry;
        this.coordinatorServiceRegistry = coordinatorServiceRegistry;
        this.port = port;
    }
    @Override
    public void onLeader() {
        workerServiceRegistry.unregisterFromCluster();
        workerServiceRegistry.registerToUpdate();

        if(webServer != null ){
            webServer.stop();
        }
        System.out.println("hehe1");

        SearchCoordinator searchCoordinator = new SearchCoordinator(workerServiceRegistry, new WebClient());
        System.out.println("searchCoordinator is created ");
        webServer = new WebServer(port, searchCoordinator);
        System.out.println("port: " + port);
        webServer.startServer();
        System.out.println("hehe");

        try{
            String currentServerAddress =
                    String.format("http://%s:%d%s", InetAddress.getLocalHost().getCanonicalHostName(), port, searchCoordinator.getEndpoint());
            System.out.println(currentServerAddress);
            coordinatorServiceRegistry.registerToCluster(currentServerAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onWorker() {
        SearchWorker searchWorker = new SearchWorker();
        if(webServer == null ) {
            webServer = new WebServer(port, searchWorker);
            webServer.startServer();
        }
        try {
            String currentServerAddress =
                    String.format("http://%s:%d%s", InetAddress.getLocalHost().getCanonicalHostName(), port, searchWorker.getEndpoint());
            workerServiceRegistry.registerToCluster(currentServerAddress);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        }

    }
}
