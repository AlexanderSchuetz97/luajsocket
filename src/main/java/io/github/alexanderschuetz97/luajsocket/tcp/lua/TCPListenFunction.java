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
import io.github.alexanderschuetz97.luajsocket.socket.AbstractLuaJSocketFunction;
import org.luaj.vm2.Varargs;

/**
 * Noop function. In luasocket this calls the "listen" method in glibc. Java does this in the bind method so no need to
 * do anything.
 */
public class TCPListenFunction extends AbstractLuaJSocketFunction {

    public TCPListenFunction(LuaJSocketLib env) {
        super(env);
    }

    @Override
    public Varargs invoke(Varargs args) {
        return ONE;
    }
}
