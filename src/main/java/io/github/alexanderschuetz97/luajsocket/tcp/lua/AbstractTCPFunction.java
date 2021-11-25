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
import io.github.alexanderschuetz97.luajsocket.socket.AbstractLuaJSocketFunction;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/**
 * Superclass for all TCP function that checks if the first argument to them is
 * TCPMasterUserdata. (as in did you use the : connotation when calling them or call them with .(self,...))
 */
abstract class AbstractTCPFunction extends AbstractLuaJSocketFunction {

    public AbstractTCPFunction(LuaJSocketLib env) {
        super(env);
    }

    @Override
    public Varargs invoke(Varargs args) {
        LuaValue value = args.arg1();
        if (!(value instanceof TCPMasterUserdata)) {
            throw new LuaError("Expected TCP Master Object got " + value.typename());
        }

        return invoke(((TCPMasterUserdata) value).getMaster(), args.subargs(2));
    }

    protected abstract Varargs invoke(TCPMaster master, Varargs args);
}
