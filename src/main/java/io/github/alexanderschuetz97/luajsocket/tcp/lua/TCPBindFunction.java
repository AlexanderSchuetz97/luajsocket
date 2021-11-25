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
// in the COPYING file in top level directory of luajsocket.
// If not, see <https://www.gnu.org/licenses/>.
//
package io.github.alexanderschuetz97.luajsocket.tcp.lua;

import io.github.alexanderschuetz97.luajsocket.lib.LuaJSocketLib;
import io.github.alexanderschuetz97.luajsocket.tcp.java.TCPMaster;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import static io.github.alexanderschuetz97.luajsocket.util.Util.*;


/**
 * Create a server socket. can only be called on uninitialized master object.
 */
public class TCPBindFunction extends AbstractTCPFunction {

    public TCPBindFunction(LuaJSocketLib env) {
        super(env);
    }

    @Override
    protected Varargs invoke(TCPMaster master, Varargs args) {

        if (master.isInitialized()) {
            return varargsOf(NIL, valueOf("Already initalized."));
        }

        String host = args.checkjstring(1);
        if ("*".equals(host)) {
            host = "0.0.0.0";
        }
        int port = args.checkint(2);

        try {
            ServerSocket socket = new ServerSocket();
            master.getSettings().apply(socket);
            socket.bind(new InetSocketAddress( InetAddress.getByName(host), port));
            master.setServer(socket);
        } catch (Exception e) {
            return varargsOf(NIL, stringToLuaString(e.getMessage()));
        }

        return LuaValue.ONE;
    }
}
