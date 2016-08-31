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

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

/**
 * Created by mopar on 8/30/16.
 */
public class WebSocketSocket extends Socket {
    protected WebSocketSocketImpl impl;
    protected WebSocketAddress addr;

    private WebSocketSocket(final WebSocketSocketImpl impl) throws SocketException {
        super(impl);
        this.impl = impl;
    }

    public WebSocketSocket() throws SocketException {
        this(new WebSocketSocketImpl());
    }

    public WebSocketSocket(final WebSocketAddress addr) throws IOException {
        this();
        this.connect(addr);
    }

    public WebSocketSocket(final String url) throws IOException {
        this(new WebSocketAddress(url));
    }

    /**
     * Binds this {@link WebSocketSocket} to the given bindpoint. Only bindpoints of the type
     * {@link WebSocketAddress} are supported.
     */
    @Override
    public void bind(final SocketAddress bindpoint) throws WebSocketSocketException {
        throw new WebSocketSocketException("bind not supported");
    }

    @Override
    public void connect(final SocketAddress endpoint, final int timeout) throws IOException {
        if (!(endpoint instanceof WebSocketAddress)) {
            throw new WebSocketSocketException("Can only connect to endpoints of type "
                    + WebSocketAddress.class.getName());
        }
        super.connect(endpoint, timeout);
        this.addr = (WebSocketAddress) endpoint;
    }

    @Override
    public String toString() {
        if (isConnected()) {
            return "WebSocketSocket[url=" + addr.getUrl() + "]";
        }
        return "WebSocketSocket[unconnected]";
    }
}
