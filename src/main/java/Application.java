import cluster.management.LeaderElection;
import cluster.management.OnLeaderWorkerElection;
import cluster.management.ServiceRegistry;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

public class Application implements Watcher {
    private ZooKeeper zooKeeper;
    private static String ZOOKEEPER_ADDRESS = "localhost:2181";
    private static int SESSION_TIMEOUT = 3000;

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        int currentPort = args.length == 1 ? Integer.parseInt(args[0]) : 8000;
        Application application = new Application();
        ZooKeeper zooKeeper = application.connectToZookeeper();
        ServiceRegistry workerServiceRegistry = new ServiceRegistry(zooKeeper, ServiceRegistry.WORKERS_REGISTRY_ZNODE);
        ServiceRegistry coordinatorServiceRegistry = new ServiceRegistry(zooKeeper, ServiceRegistry.COORDINATOR_REGISTRY_ZNODE);
        OnLeaderWorkerElection onLeaderWorkerElection = new OnLeaderWorker(workerServiceRegistry, coordinatorServiceRegistry ,currentPort);
        LeaderElection leaderElection = new LeaderElection(zooKeeper, onLeaderWorkerElection);

        leaderElection.volunteerForLeader();
        leaderElection.electLeader();
        application.run();
        application.close();


    }
    private ZooKeeper connectToZookeeper() throws IOException {
        this.zooKeeper = new ZooKeeper(ZOOKEEPER_ADDRESS, SESSION_TIMEOUT, this);
        return zooKeeper;
    }

    private void run() throws InterruptedException {
        synchronized (zooKeeper) {
            zooKeeper.wait();
        }
    }

    private void close() throws InterruptedException {
        zooKeeper.close();
    }

    @Override
    public void process(WatchedEvent event) {
        switch(event.getType()){
            case None:
                if(event.getState() == Event.KeeperState.SyncConnected){
                    System.out.println("Connected to Zookeeper");
                }
                else{
                    System.out.println("Failed to connect to zookeeper");
                    zooKeeper.notifyAll();
                }
                break;

        }

    }
}
