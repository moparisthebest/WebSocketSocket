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

package com.moparisthebest.websocket.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Logger;

import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.*;

public class SocketWebSocket extends DefaultWebSocket implements Runnable {
    private static final Logger logger = Grizzly.logger(SocketWebSocket.class);

    private final Socket s;
    private final OutputStream os;
    private final InputStream is;

    public SocketWebSocket(ProtocolHandler protocolHandler,
                           HttpRequestPacket request,
                           WebSocketListener... listeners) throws IOException {
        super(protocolHandler, request, listeners);
        s = new Socket("127.0.0.1", 22);
        os = s.getOutputStream();
        is = s.getInputStream();
        new Thread(this).start();
    }

    public void run() {
        final byte[] buff = new byte[8 * 1024];
        int len;
        try {
            while ((len = is.read(buff)) != -1) {
                // handle off/len
                if (len == buff.length)
                    this.send(buff);
                else {
                    // otherwise we need to copy into a new array...
                    final byte[] toSend = new byte[len];
                    System.arraycopy(buff, 0, toSend, 0, len);
                    this.send(toSend);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMessage(final byte[] bytes) {
        //System.out.println("ChatWebSocket got bytes: "+bytes.length);
        try {
            os.write(bytes);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onClose(final DataFrame frame) {
        super.onClose(frame);
        try {
            is.close();
            os.close();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }
}
