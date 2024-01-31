package cluster.management;

public interface OnLeaderWorkerElection {
    void onLeader();
    void onWorker();
}
