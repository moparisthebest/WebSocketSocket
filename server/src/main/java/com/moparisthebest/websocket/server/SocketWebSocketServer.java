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

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketEngine;

public class SocketWebSocketServer {
    // the port to listen on
    public static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        // create a Grizzly HttpServer to server static resources from 'webapp', on PORT.
        //final HttpServer server = HttpServer.createSimpleServer("./src/main/webapp/", PORT);
        final HttpServer server = new HttpServer();
        final NetworkListener networkListener = new NetworkListener("grizzly", "localhost", PORT);

        // Register the WebSockets add on with the HttpServer
        networkListener.registerAddOn(new WebSocketAddOn());

        server.addListener(networkListener);

        // initialize websocket chat application
        final WebSocketApplication chatApplication = new WebSocketSocketApplication();

        // register the application
        WebSocketEngine.getEngine().register("/websocket", "/ssh", chatApplication);

        try {
            server.start();
            System.out.println("Press any key to stop the server...");
            //noinspection ResultOfMethodCallIgnored
            System.in.read();
        } finally {
            // stop the server
            server.shutdownNow();
        }
    }
}
