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

import java.net.InetSocketAddress;

/**
 * Created by mopar on 8/30/16.
 */
public class WebSocketAddress extends InetSocketAddress {

    private final String url;

    public WebSocketAddress(final String url) {
        super(0);
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "WebSocketAddress{" +
                "url='" + url + '\'' +
                "} " + super.toString();
    }
}
