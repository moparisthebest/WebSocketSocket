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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by mopar on 8/30/16.
 */
public class SocketProxy implements Runnable {

    public static Socket newSocket() throws IOException {
        //return new Socket("127.0.0.1", 22);
        return new WebSocketSocket("ws://127.0.0.1:8080/websocket/ssh");
    }

    public static void main(String[] args) throws Throwable {
        try (final ServerSocket ss = new ServerSocket(Integer.parseInt(System.getProperty("port", "9001")))) {
            while (!Thread.interrupted()) {
                final Socket s = ss.accept();
                System.out.println("accepted");
                new Thread(new SocketProxy(s)).start();
            }
        }
    }

    private final Socket s;

    public SocketProxy(final Socket s) {
        this.s = s;
    }

    @Override
    public void run() {
        try (Socket out = newSocket();
             InputStream isIn = s.getInputStream();
             InputStream isOut = out.getInputStream();
             OutputStream osIn = s.getOutputStream();
             OutputStream osOut = out.getOutputStream()
        ) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    copy(isIn, osOut);
                }
            }).start();
            copy(isOut, osIn);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("closed");
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void copy(final InputStream from, final OutputStream to) {
        final byte[] buffer = new byte[8 * 1024];
        int bytesRead;
        try {
            while ((bytesRead = from.read(buffer)) != -1)
                to.write(buffer, 0, bytesRead);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
