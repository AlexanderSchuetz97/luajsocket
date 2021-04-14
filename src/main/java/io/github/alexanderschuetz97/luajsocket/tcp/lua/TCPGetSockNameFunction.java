//
// Copyright Alexander Schütz, 2021
//
// This file is part of luajsocket.
//
// luajsocket is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// luajsocket is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// A copy of the GNU General Public License should be provided
// in the COPYING file in top level directory of luajsocket.
// If not, see <https://www.gnu.org/licenses/>.
//
package io.github.alexanderschuetz97.luajsocket.tcp.lua;

import io.github.alexanderschuetz97.luajsocket.lib.LuaJSocketLib;
import io.github.alexanderschuetz97.luajsocket.tcp.java.TCPMaster;
import io.github.alexanderschuetz97.luajsocket.util.Util;
import org.luaj.vm2.Varargs;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Returns the ip:port of the local endpoint.
 * Can be called on a client and server.
 */
public class TCPGetSockNameFunction extends AbstractTCPFunction {

    public TCPGetSockNameFunction(LuaJSocketLib env) {
        super(env);
    }

    @Override
    protected Varargs invoke(TCPMaster master, Varargs args) {
        if (!master.isInitialized()) {
            return NIL;
        }

        if (master.isClient()) {
            Socket client = master.getClient().getSocket();

            String host = Util.ipAddressToString(client.getLocalAddress());
            int port = client.getLocalPort();
            if (host == null) {
                return varargsOf(NIL, valueOf("Unexpected Address"));
            }

            return varargsOf(valueOf(host), valueOf(port));
        }

        if (master.isServer()) {

            SocketAddress address = master.getServer().getSocket().getLocalSocketAddress();
            if (!(address instanceof InetSocketAddress)) {
                return varargsOf(NIL, valueOf("Not an Inet Socket."));
            }

            InetSocketAddress inetSocketAddress = (InetSocketAddress) address;
            String host = Util.ipAddressToString(inetSocketAddress.getAddress());
            int port = inetSocketAddress.getPort();
            if (host == null) {
                return varargsOf(NIL, valueOf("Unexpected Address"));
            }

            return varargsOf(valueOf(host), valueOf(port));
        }

        return NIL;
    }
}
