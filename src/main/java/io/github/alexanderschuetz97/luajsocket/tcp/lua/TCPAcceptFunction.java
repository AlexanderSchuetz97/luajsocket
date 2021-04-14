package io.github.alexanderschuetz97.luajsocket.tcp.lua;
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
import io.github.alexanderschuetz97.luajsocket.lib.LuaJSocketLib;
import io.github.alexanderschuetz97.luajsocket.tcp.java.TCPMaster;
import io.github.alexanderschuetz97.luajsocket.util.Util;
import org.luaj.vm2.Varargs;

import java.util.concurrent.TimeoutException;

/**
 * Accept a socket. Can only be called on the server.
 */
public class TCPAcceptFunction extends AbstractTCPFunction {

    public TCPAcceptFunction(LuaJSocketLib env) {
        super(env);
    }

    @Override
    protected Varargs invoke(TCPMaster master, Varargs args) {
        if (master.isClosed()) {
            return varargsOf(NIL, valueOf("closed"));
        }

        if (!master.isServer()) {
            return varargsOf(NIL, valueOf("TCP Object is not server."));
        }

        int timeout = master.getSettings().getMinTimeout();

        TCPMasterUserdata socket;
        try {
            socket = master.getServer().accept(timeout);
        } catch (TimeoutException e) {
            return varargsOf(NIL, valueOf("timeout"));
        } catch (Exception e) {
            return varargsOf(NIL, Util.stringToLuaString(e.getMessage()));
        }

        return socket;
    }
}
