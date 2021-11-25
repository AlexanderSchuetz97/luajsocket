//
// Copyright Alexander Schütz, 2021
//
// This file is part of luajsocket.
//
// luajsocket is free software: you can redistribute it and/or modify
// it under the terms of the GNU Lesser General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// luajsocket is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Lesser General Public License for more details.
//
// A copy of the GNU Lesser General Public License should be provided
// in the COPYING & COPYING.LESSER files in top level directory of luajsocket.
// If not, see <https://www.gnu.org/licenses/>.
//
package io.github.alexanderschuetz97.luajsocket.tcp.lua;

import io.github.alexanderschuetz97.luajsocket.lib.LuaJSocketLib;
import io.github.alexanderschuetz97.luajsocket.tcp.java.TCPMaster;
import io.github.alexanderschuetz97.luajsocket.util.Util;
import org.luaj.vm2.Varargs;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Returns the ip:port of the connected endpoint.
 * Can only be called on a client.
 */
public class TCPGetPeerNameFunction extends AbstractTCPFunction {

    public TCPGetPeerNameFunction(LuaJSocketLib env) {
        super(env);
    }

    @Override
    protected Varargs invoke(TCPMaster master, Varargs args) {
        if (!master.isClient()) {
            return varargsOf(NIL, valueOf("TCP Object is not client."));
        }

        SocketAddress address = master.getClient().getSocket().getRemoteSocketAddress();
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
}
