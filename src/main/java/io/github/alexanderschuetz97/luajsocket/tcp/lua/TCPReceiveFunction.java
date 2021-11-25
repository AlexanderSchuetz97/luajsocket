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
import io.github.alexanderschuetz97.luajsocket.tcp.java.TCPClient;
import io.github.alexanderschuetz97.luajsocket.tcp.java.TCPMaster;
import io.github.alexanderschuetz97.luajsocket.util.ByteArrayOutputStreamWithBufferAccess;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.Varargs;

import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * This function receives data from the tcp connection.
 * Can only be called on client.
 * Note: The socket is implemented asynchronously so the data you are reading here has already been read in the past.
 * The socket stats however are only incremented when data is actually read here.
 */
public class TCPReceiveFunction extends AbstractTCPFunction {

    public TCPReceiveFunction(LuaJSocketLib env) {
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

        ByteArrayOutputStreamWithBufferAccess baos = new ByteArrayOutputStreamWithBufferAccess();
        if (args.isstring(2)) {
            LuaString ls = args.checkstring(2);;
            baos.write(ls.m_bytes, ls.m_offset, ls.m_length);
        }

        TCPClient client = master.getClient();
        int timeout = master.getSettings().getSingleTimeout();
        int timeoutTotal = master.getSettings().getTotalTimeout();

        try {
            if (args.isnoneornil(1)) {
                client.readLine(baos, timeout, timeoutTotal);
            } else if (args.isnumber(1)) {
                int count = args.checkint(1);
                client.readBytes(baos, count, timeout, timeoutTotal);
            } else if (args.isstring(1)) {
                switch (args.checkjstring(1)) {
                    case "*a":
                        client.readAll(baos, timeout, timeoutTotal);
                        break;
                    case "*l":
                        client.readLine(baos, timeout, timeoutTotal);
                        break;
                    default:
                        return varargsOf(NIL, valueOf("Unsupported pattern " + args.checkjstring(1)));
                }
            } else {
                return varargsOf(NIL, valueOf("unexpected first parameter expected string or number got " + args.arg1().typename()));
            }
        } catch (EOFException e) {
            return varargsOf(NIL, valueOf("closed"), LuaString.valueUsing(baos.getBuffer(), 0, baos.size()));
        } catch (IOException e) {
            return varargsOf(NIL, valueOf("error " + e.getMessage() + " " + e.getClass().getName()), LuaString.valueUsing(baos.getBuffer(), 0, baos.size()));
        } catch (TimeoutException e) {
            return varargsOf(NIL, valueOf("timeout"), LuaString.valueUsing(baos.getBuffer(), 0, baos.size()));
        }

        return LuaString.valueUsing(baos.getBuffer(), 0, baos.size());
    }

}