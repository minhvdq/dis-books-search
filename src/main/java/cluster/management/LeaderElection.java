package cluster.management;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.util.Collections;
import java.util.List;

public class LeaderElection implements Watcher {


    private ZooKeeper zooKeeper;
    private final String ELECTION_NAMESPACE = "/election";
    private String currentZnode;

    private OnLeaderWorkerElection onLeaderWorkerElection;

    public LeaderElection ( ZooKeeper zooKeeper, OnLeaderWorkerElection onLeaderWorkerElection){
        this.zooKeeper = zooKeeper;
        this.onLeaderWorkerElection = onLeaderWorkerElection;
    }

    public void volunteerForLeader() throws InterruptedException, KeeperException {
        String znodeModelPath = ELECTION_NAMESPACE + "/c_";
        String znodeFullPath = zooKeeper.create(znodeModelPath, new byte[] {}, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        this.currentZnode = znodeFullPath.replace(ELECTION_NAMESPACE + "/", "");
        System.out.println("Created Znode:  " + this.currentZnode);
    }
    public void electLeader () throws InterruptedException, KeeperException {
        String precedeZnodeName = "";
        Stat precedeZnode = null;
        while(precedeZnode == null) {
            List<String> children = zooKeeper.getChildren(ELECTION_NAMESPACE, false);
            Collections.sort(children);
            String smallestChild = children.get(0);
            if (currentZnode.equalsIgnoreCase(smallestChild)) {
                System.out.println("Im the leader ");
                onLeaderWorkerElection.onLeader();
                return;
            } else {
                System.out.println("Not the leader, leader is: " + smallestChild);
                int precedeIndex = Collections.binarySearch(children, currentZnode) - 1;
                precedeZnodeName = children.get(precedeIndex);
                precedeZnode = zooKeeper.exists(ELECTION_NAMESPACE + "/" + precedeZnodeName, this);

            }
        }
        onLeaderWorkerElection.onWorker();
        System.out.println("Watching znode: " + precedeZnodeName);
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        try{
            electLeader();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (KeeperException e) {
            throw new RuntimeException(e);
        }
    }
}
