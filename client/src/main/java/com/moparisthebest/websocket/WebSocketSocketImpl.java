/*
 * WebSocketSocket implements a custom java.net.Socket over top of a
 * WebSocket, and also a server component that accepts a WebSocket
 * connection and connects to a host/port.
 * Copyright (C) 2016 Travis Burtrum (moparisthebest)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.moparisthebest.websocket;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ws.WebSocket;
import com.ning.http.client.ws.WebSocketByteListener;
import com.ning.http.client.ws.WebSocketUpgradeHandler;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutionException;

/**
 * Created by mopar on 8/30/16.
 */
public class WebSocketSocketImpl extends SocketImpl implements WebSocketByteListener {

    private String url;
    private AsyncHttpClient httpClient = null;
    private WebSocket webSocket = null;
    private boolean closed = false;
    private boolean connected = false;

    private boolean closedInputStream = false;
    private boolean closedOutputStream = false;

    private final Object monitor = new Object();
    private final PipedOutputStream incoming = new PipedOutputStream();
    private final PipedInputStream in = new PipedInputStream();
    private final WebSocketOutputStream out = new WebSocketOutputStream();

    WebSocketSocketImpl() {
        super();
        this.fd = new FileDescriptor();
    }

    @Override
    protected void accept(SocketImpl socket) throws IOException {
        throw new WebSocketSocketException("bind not supported");
    }

    @Override
    protected void bind(InetAddress host, int port) throws IOException {
        throw new WebSocketSocketException("bind not supported");
    }

    @Override
    protected int available() throws IOException {
        return in.available();
    }

    private void checkClose() throws IOException {
        if (closedInputStream && closedOutputStream) {
            close();
        }
    }

    @Override
    protected synchronized void close() throws IOException {
        if (closed) {
            return;
        }
        closed = true;
        this.webSocket.close();
        this.httpClient.close();
        this.in.close();
        this.incoming.close();
        this.out.close();
        System.out.println("WebSocketSocketImpl.closed");
        connected = false;
    }

    @Override
    @SuppressWarnings("hiding")
    protected void connect(String host, int port) throws IOException {
        throw new WebSocketSocketException("Cannot connect to this type of address: " + InetAddress.class);
    }

    @Override
    @SuppressWarnings("hiding")
    protected void connect(InetAddress address, int port) throws IOException {
        throw new WebSocketSocketException("Cannot connect to this type of address: " + InetAddress.class);
    }

    @Override
    protected void connect(SocketAddress addr, int timeout) throws IOException {
        if (!(addr instanceof WebSocketAddress)) {
            throw new WebSocketSocketException("Can only connect to endpoints of type "
                    + WebSocketAddress.class.getName());
        }
        in.connect(incoming);
        final WebSocketAddress socketAddress = (WebSocketAddress) addr;
        url = socketAddress.getUrl();
        httpClient = new AsyncHttpClient();
        WebSocket webSocket = null;
        try {
            webSocket = httpClient.prepareGet(url).execute(new WebSocketUpgradeHandler.Builder()
                    .addWebSocketListener(this).build()).get();
            synchronized (monitor) {
                if (this.webSocket == null) // only wait if this is still null
                    monitor.wait(timeout); // wait until we hear back about the socket
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new WebSocketSocketException("connection failed", e);
        }
        if (this.webSocket == null) {
            if (webSocket != null)
                webSocket.close();
            httpClient.closeAsynchronously();
            httpClient = null;
            throw new WebSocketSocketException("connection timed out");
        }
        this.address = socketAddress.getAddress();
        this.port = socketAddress.getPort();
        this.localport = 0;
        this.connected = true;
    }

    @Override
    protected void create(boolean stream) throws IOException {
    }

    @Override
    protected InputStream getInputStream() throws IOException {
        if (!connected) {
            throw new IOException("Not connected/not bound");
        }
        return in;
    }

    @Override
    protected OutputStream getOutputStream() throws IOException {
        if (!connected) {
            throw new IOException("Not connected/not bound");
        }
        return out;
    }

    @Override
    protected void listen(int backlog) throws IOException {
        // listen not supported
    }

    @Override
    protected boolean supportsUrgentData() {
        return true;
    }

    @Override
    protected void sendUrgentData(int data) throws IOException {
        getOutputStream().write(new byte[]{(byte) (data & 0xFF)}, 0, 1);
    }

    private final class WebSocketOutputStream extends OutputStream {
        private boolean streamClosed = false;

        @Override
        public void write(int oneByte) throws IOException {
            final byte[] buf1 = new byte[]{(byte) oneByte};
            write(buf1, 0, 1);
        }

        @Override
        public void write(final byte[] buff, final int off, final int len) throws IOException {
            //System.out.println("WebSocketSocketImpl.WebSocketOutputStream.write("+len);
            if (streamClosed) {
                throw new WebSocketSocketException("This OutputStream has already been closed.");
            }
            if (len > buff.length - off) {
                throw new IndexOutOfBoundsException();
            }
            // handle off/len
            if (off == 0 && len == buff.length)
                webSocket.sendMessage(buff);
            else {
                // otherwise we need to copy into a new array...
                final byte[] toSend = new byte[len];
                System.arraycopy(buff, off, toSend, 0, len);
                webSocket.sendMessage(toSend);
            }
        }

        @Override
        public void close() throws IOException {
            if (streamClosed) {
                return;
            }
            streamClosed = true;
            // close?
            closedOutputStream = true;
            checkClose();
        }
    }

    @Override
    public String toString() {
        return super.toString() + "[url=" + this.url + "; connected="
                + connected + "]";
    }

    @Override
    public Object getOption(int optID) throws SocketException {
        try {
            switch (optID) {
                case SocketOptions.SO_KEEPALIVE:
                case SocketOptions.TCP_NODELAY:
                    //return NativeUnixSocket.getSocketOptionInt(fd, optID) != 0 ? true : false;
                case SocketOptions.SO_LINGER:
                case SocketOptions.SO_TIMEOUT:
                case SocketOptions.SO_RCVBUF:
                case SocketOptions.SO_SNDBUF:
                    //return NativeUnixSocket.getSocketOptionInt(fd, optID);
                default:
                    throw new WebSocketSocketException("Unsupported option: " + optID);
            }
        } catch (final WebSocketSocketException e) {
            throw e;
        } catch (final Exception e) {
            throw new WebSocketSocketException("Error while getting option", e);
        }
    }

    @Override
    public void setOption(int optID, Object value) throws SocketException {
        try {
            switch (optID) {
                case SocketOptions.SO_LINGER:

                    if (value instanceof Boolean) {
                        final boolean b = (Boolean) value;
                        if (b) {
                            throw new SocketException("Only accepting Boolean.FALSE here");
                        }
                        //NativeUnixSocket.setSocketOptionInt(fd, optID, -1);
                        return;
                    }
                    //NativeUnixSocket.setSocketOptionInt(fd, optID, expectInteger(value));
                    return;
                case SocketOptions.SO_RCVBUF:
                case SocketOptions.SO_SNDBUF:
                case SocketOptions.SO_TIMEOUT:
                    //NativeUnixSocket.setSocketOptionInt(fd, optID, expectInteger(value));
                    return;
                case SocketOptions.SO_KEEPALIVE:
                case SocketOptions.TCP_NODELAY:
                    //NativeUnixSocket.setSocketOptionInt(fd, optID, expectBoolean(value));
                    return;
                default:
                    throw new WebSocketSocketException("Unsupported option: " + optID);
            }
        } catch (final WebSocketSocketException e) {
            throw e;
        } catch (final Exception e) {
            throw new WebSocketSocketException("Error while setting option", e);
        }
    }

    /*
    todo: implement these?
    @Override
    protected void shutdownInput() throws IOException {
        if (!closed && fd.valid()) {
            close();
        }
    }

    @Override
    protected void shutdownOutput() throws IOException {
        if (!closed && fd.valid()) {
            close();
        }
    }
    */

    // websocketbytelistener
    public void onMessage(final byte[] bytes) {
        //System.out.println("WebSocketSocketImpl.onMessage()");
        try {
            incoming.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onOpen(final WebSocket websocket) {
        //System.out.println("WebSocketSocketImpl.onOpen()");
        this.webSocket = websocket;
        synchronized (monitor) {
            monitor.notifyAll();
        }
    }

    @Override
    public void onClose(WebSocket websocket) {
        //System.out.println("WebSocketSocketImpl.onClose()");
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(Throwable t) {
        //System.out.println("WebSocketSocketImpl.onError()");
        t.printStackTrace();
        synchronized (monitor) {
            monitor.notifyAll();
        }
        try {
            close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
