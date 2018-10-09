package com.ele.socketio.server;

import com.corundumstudio.socketio.SocketIOClient;

public class ClientInfo {
    private SocketIOClient mIOClient;
    private String mDid;
    private String mIp;
    private int mPort;
    private String mBrand;
    private String mModel;
    private String mManf;
    private String mAndroidID;
    private String mRelease;

    public ClientInfo(SocketIOClient IOClient, String ip, int port, String did, String brand, String model, String release, String manf, String androidID) {
        mIOClient = IOClient;
        mDid = did;
        mIp = ip;
        mPort = port;
        mBrand = brand;
        mModel = model;
        mManf = manf;
        mAndroidID = androidID;
        mRelease = release;
    }

    public ClientInfo() {
    }

    public SocketIOClient getIOClient() {
        return mIOClient;
    }

    public void setIOClient(SocketIOClient IOClient) {
        mIOClient = IOClient;
    }

    public String getDid() {
        return mDid;
    }

    public void setDid(String did) {
        mDid = did;
    }

    public String getIp() {
        return mIp;
    }

    public void setIp(String ip) {
        mIp = ip;
    }

    public int getPort() {
        return mPort;
    }

    public void setPort(int port) {
        mPort = port;
    }

    public String getBrand() {
        return mBrand;
    }

    public void setBrand(String brand) {
        mBrand = brand;
    }

    public String getModel() {
        return mModel;
    }

    public void setModel(String model) {
        mModel = model;
    }

    public String getManf() {
        return mManf;
    }

    public void setManf(String manf) {
        mManf = manf;
    }

    public String getAndroidID() {
        return mAndroidID;
    }

    public void setAndroidID(String androidID) {
        mAndroidID = androidID;
    }

    public String getRelease() {
        return mRelease;
    }

    public void setRelease(String release) {
        mRelease = release;
    }

    @Override
    public String toString() {
        return "ClientInfo{" +
                "mIOClient=" + mIOClient +
                ", mDid='" + mDid + '\'' +
                ", mIp='" + mIp + '\'' +
                ", mPort=" + mPort +
                ", mBrand='" + mBrand + '\'' +
                ", mModel='" + mModel + '\'' +
                ", mManf='" + mManf + '\'' +
                ", mAndroidID='" + mAndroidID + '\'' +
                ", mRelease='" + mRelease + '\'' +
                '}';
    }
}
