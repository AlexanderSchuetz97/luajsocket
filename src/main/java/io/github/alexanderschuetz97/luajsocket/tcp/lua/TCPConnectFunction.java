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
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * This function connects a tcp master object to a endpoint turning it into a client.
 */
public class TCPConnectFunction extends AbstractTCPFunction {

    public TCPConnectFunction(LuaJSocketLib env) {
        super(env);
    }

    @Override
    protected Varargs invoke(TCPMaster master, Varargs args) {
        if (master.isInitialized()) {
            return varargsOf(NIL, valueOf("TCP Object already used."));
        }

        if (args.narg() < 2) {
            return varargsOf(NIL, valueOf("Too few arguments. Expected host and port."));
        }
        String host = args.checkjstring(1);
        int port = args.checkint(2);

        int timeout = master.getSettings().getMinTimeout();
        Socket socket = new Socket();
        try {
            master.getSettings().apply(socket);
            if (timeout < 0) {
                socket.connect(new InetSocketAddress(InetAddress.getByName(host), port));
            } else {
                socket.connect(new InetSocketAddress(InetAddress.getByName(host), port), Math.max(1,timeout));
            }
            master.setClient(socket);
        } catch (SocketTimeoutException e) {
            return varargsOf(NIL, valueOf("timeout"));
        } catch (Exception e) {
            return varargsOf(NIL, Util.stringToLuaString(e.getMessage()));
        }

        return LuaValue.ONE;
    }
}
