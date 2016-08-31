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

import org.glassfish.grizzly.websockets.WebSocketEngine;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

public class WebSocketSocketServlet extends HttpServlet {

    private WebSocketSocketApplication app;

    @Override
    public void init(ServletConfig config) throws ServletException {
        app = new WebSocketSocketApplication();
        WebSocketEngine.getEngine().register(config.getServletContext().getContextPath(), "/ssh", app);
    }

    @Override
    public void destroy() {
        WebSocketEngine.getEngine().unregister(app);
    }
}
