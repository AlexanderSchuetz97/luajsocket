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
import io.github.alexanderschuetz97.luajsocket.tcp.java.TCPSettings;
import io.github.alexanderschuetz97.luajsocket.util.Util;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/**
 * Sets a socket option to a client or server socket.
 * May also be called in advance on an uninitialized master object.
 */
public class TCPSetOptionFunction extends AbstractTCPFunction {

    public TCPSetOptionFunction(LuaJSocketLib env) {
        super(env);
    }

    @Override
    protected Varargs invoke(TCPMaster master, Varargs args) {
        String option = args.checkjstring(1);

        TCPSettings settings = master.getSettings();
        TCPSettings copy = new TCPSettings();
        settings.copyTo(copy);
        switch (option) {
            case "keepalive":
                copy.setKeepAlive(args.checkboolean(2));
                break;
            case "linger":
                LuaValue table = args.checktable(2);
                copy.setSoLinger(Util.toBoolean(table.get("on")), Util.optInt(table.get("timeout"), 0));
                break;
            case "reuseaddr":
                copy.setReuseAddress(args.checkboolean(2));
                break;
            case "tcp-nodelay":
                copy.setNoDelay(args.checkboolean(2));
                break;
            default:
                return LuaValue.NIL;
        }

        if (master.isClient()) {
            try {
                copy.apply(master.getClient().getSocket());
                copy.copyTo(settings);
                return LuaValue.ONE;
            } catch (Exception e) {
               return LuaValue.NIL;
            }
        }

        if (master.isServer()) {
            try {
                copy.apply(master.getServer().getSocket());
                copy.copyTo(settings);
                return LuaValue.ONE;
            } catch (Exception e) {
                return LuaValue.NIL;
            }
        }


        copy.copyTo(settings);
        return LuaValue.ONE;
    }
}
