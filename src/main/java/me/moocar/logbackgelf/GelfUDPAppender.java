package me.moocar.logbackgelf;

import ch.qos.logback.core.OutputStreamAppender;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by anthony on 3/8/15.
 */
public class GelfUDPAppender<E> extends OutputStreamAppender<E> {

    private String remoteHost;
    private int port;
    private int maxPacketSize = 512;

    @Override
    public void start() {
        if (isStarted()) return;
        int errorCount = 0;
        if (port <= 0) {
            errorCount++;
            addError("No port was configured for appender"
                    + name
                    + " For more information, please visit http://logback.qos.ch/codes.html#socket_no_port");

        }

        if (remoteHost == null) {
            errorCount++;
            addError("No remote host was configured for appender"
                    + name
                    + " For more information, please visit http://logback.qos.ch/codes.html#socket_no_host");
        }
        InetAddress address = null;
        if (errorCount == 0) {
            try {
                address = InternetUtils.getInetAddress(remoteHost);
            } catch (Exception e) {
                addError("Error creating InetAddress", e);
                errorCount++;
            }
        }

        String hostname = null;
        if (errorCount == 0) {
            try {
                hostname = InternetUtils.getLocalHostName();
            } catch (SocketException e) {
                addError("Error creating localhostname", e);
                errorCount++;
            } catch (UnknownHostException e) {
                addError("Could not create hostname");
                errorCount++;
            }
        }

        MessageIdProvider messageIdProvider = null;
        if (errorCount == 0) {
            try {
                messageIdProvider = new MessageIdProvider(hostname);
            } catch (NoSuchAlgorithmException e) {
                errorCount++;
                addError("Error creating digest", e);
            }
        }

        if (errorCount == 0) {
            GelfUDPOutputStream os = new GelfUDPOutputStream(address, port, maxPacketSize, messageIdProvider);
            try {
                os.start();
                this.setOutputStream(os);
                super.start();
            } catch (SocketException e) {
                addError("Could not connect to remote host", e);
            } catch (UnknownHostException e) {
                addError("unknown host: " + remoteHost);
            }
        }


    }

    @Override
    protected void writeOut(E event) throws IOException {
        addInfo("write out");
        super.writeOut(event);
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getMaxPacketSize() {
        return maxPacketSize;
    }

    public void setMaxPacketSize(int maxPacketSize) {
        this.maxPacketSize = maxPacketSize;
    }
}
