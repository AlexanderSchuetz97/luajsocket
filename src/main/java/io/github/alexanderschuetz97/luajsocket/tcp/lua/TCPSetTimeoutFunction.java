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
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.util.concurrent.TimeUnit;

/**
 * Sets timeout of IO operations on the socket.
 * May also be called in advance on an uninitialized master object.
 */
public class TCPSetTimeoutFunction extends AbstractTCPFunction{

    public TCPSetTimeoutFunction(LuaJSocketLib env) {
        super(env);
    }

    @Override
    protected Varargs invoke(TCPMaster master, Varargs args) {
        int timeout = args.optint(1, -1);

        switch (args.optstring(2, LuaValue.valueOf("b")).checkjstring()) {
            case ("b"):
                master.getSettings().setSingleTimeout((int) TimeUnit.SECONDS.toMillis(timeout));
                return LuaValue.ONE;
            case ("t"):
                master.getSettings().setTotalTimeout((int) TimeUnit.SECONDS.toMillis(timeout));
                return LuaValue.ONE;
            default:
                return LuaValue.varargsOf(LuaValue.NIL, LuaValue.valueOf("Invalid blocking mode."));
        }
    }
}
