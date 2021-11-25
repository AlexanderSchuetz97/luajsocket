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

import java.io.IOException;

/**
 * Performs a partial shutdown of a socket.
 * Close still needs to be called afterwards!
 */
public class TCPShutdownFunction extends AbstractTCPFunction {

    public TCPShutdownFunction(LuaJSocketLib env) {
        super(env);
    }

    @Override
    protected Varargs invoke(TCPMaster master, Varargs args) {
        if (!master.isClient()) {
            return LuaValue.NIL;
        }

        switch (args.checkjstring(1)) {
            case "both":
                try {
                    master.getClient().shutdownOutput();
                } catch (IOException e) {
                    //DC
                }
                try {
                    master.getClient().shutdownInput();
                } catch (IOException e) {
                    //DC.
                }
                return LuaValue.ONE;
            case "send":
                try {
                    master.getClient().shutdownOutput();
                } catch (IOException e) {
                    //DC
                }
                return LuaValue.ONE;
            case "receive":
                try {
                    master.getClient().shutdownInput();
                } catch (IOException e) {
                    //DC.
                }
                return LuaValue.ONE;
            default:
                return LuaValue.NIL;
        }
    }
}
