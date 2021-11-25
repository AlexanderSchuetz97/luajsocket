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
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;


/**
 * Sends data to a endpoint.
 * Can only be called in a client.
 *
 * Data is sent asynchronously so data sent here may only be set with a little delay.
 * This method will only timeout if the send buffers are full.
 *
 * calling close on a socket will ensure that all data is sent however. If an IO error occurs while sending data
 * Java has no method of finding out just how much data is lost with sockets. This means that this information cannot
 * be passed to lua.
 */
public class TCPSendFunction extends AbstractTCPFunction {

    public TCPSendFunction(LuaJSocketLib env) {
        super(env);
    }

    @Override
    protected Varargs invoke(TCPMaster master, Varargs args) {
        if (master.isClosed()) {
            return varargsOf(NIL, valueOf("closed"));
        }

        if (!master.isClient()) {
            return varargsOf(NIL, valueOf("Not a client."));
        }

        LuaString payload = args.checkstring(1);
        int off = Math.max(0, args.optint(2, 1)-1);
        int len = Math.max(0, args.optint(3, payload.m_length)-off);

        if (len == 0) {
            return LuaValue.ZERO;
        }


        int timeout = master.getSettings().getSingleTimeout();
        int totalTimeout = master.getSettings().getTotalTimeout();
        try {
            int l = master.getClient().write(payload.m_bytes, payload.m_offset+off, len, timeout, totalTimeout);
            if (l != len) {
                return varargsOf(NIL, valueOf("timeout"), LuaString.valueUsing(payload.m_bytes, payload.m_offset+off+l, len-l));
            }
        } catch (Exception e) {
            return varargsOf(NIL, Util.stringToLuaString(e.getMessage()));
        }

        return valueOf(len+off);
    }
}

