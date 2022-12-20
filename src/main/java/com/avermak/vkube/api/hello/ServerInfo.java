package com.avermak.vkube.api.hello;

public class ServerInfo {
    private String podName = null;
    private String podIP = null;
    private String nodeName = null;
    private String nodeIP = null;
    private long podTimeGMT = 0;

    public ServerInfo() {}

    public ServerInfo(String podName, String podIP, String nodeName, String nodeIP, long podTimeGMT) {
        this.podName = podName;
        this.podIP = podIP;
        this.nodeName = nodeName;
        this.nodeIP = nodeIP;
        this.podTimeGMT = podTimeGMT;
    }

    public String getPodName() {
        return podName;
    }

    public void setPodName(String podName) {
        this.podName = podName;
    }

    public String getPodIP() {
        return podIP;
    }

    public void setPodIP(String podIP) {
        this.podIP = podIP;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeIP() {
        return nodeIP;
    }

    public void setNodeIP(String nodeIP) {
        this.nodeIP = nodeIP;
    }

    public long getPodTimeGMT() {
        return podTimeGMT;
    }

    public void setPodTimeGMT(long podTimeGMT) {
        this.podTimeGMT = podTimeGMT;
    }

    /**
     * Updates any data elements that need refreshing (example: current pod time). Not all fields are updated.
     */
    public void update() {
        this.podTimeGMT = System.currentTimeMillis();
    }
}
