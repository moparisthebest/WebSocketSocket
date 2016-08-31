# WebSocketSocket

WebSocketSocket implements a custom java.net.Socket over top of a WebSocket, and also a server component 
that accepts a WebSocket connection and connects to a host/port. This allows you to tunnel arbitrary TCP 
over a WebSocket, even TLS can be used over it.  Which potentially allows you to securely establish a 
trusted TLS connection even on a network that intercepts TLS on port 443.

Sample Usage
------------

Configure and run SocketWebSocketServer on a remote server.

```java
Socket sock = new WebSocketSocket("ws://remote-server:remote-port/websocket/ssh");
// use sock just like any other standard Socket
```

You can also run SocketProxy on the client which listens on a local TCP port, and connect to that with anything.

Licensing
---------
This project is licensed under the [GNU/LGPLv2.1][1], which allows use in Open Source or Proprietary programs.  If you need to modify this code though, you should contribute back to it.

Contributing
------------

1. Fork it. (Alternatively, if you **really** can't use github/git, email me a patch.)
2. Create a branch (`git checkout -b my_WebSocketSocket`)
3. Commit your changes (`git commit -am "Implemented method X"`)
4. Push to the branch (`git push origin my_WebSocketSocket`)
5. Open a [Pull Request][2]
6. Enjoy a refreshing beverage and wait

[1]:   https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
[2]: https://github.com/moparisthebest/WebSocketSocket/pulls
