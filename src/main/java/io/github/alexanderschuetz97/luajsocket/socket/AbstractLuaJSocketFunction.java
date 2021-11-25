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
package io.github.alexanderschuetz97.luajsocket.socket;

import io.github.alexanderschuetz97.luajsocket.lib.LuaJSocketLib;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

/**
 * Abstract utility function for all LuaJSocket functions that need an instance of LuaJSocket
 * to perform various tasks.
 */
public abstract class AbstractLuaJSocketFunction extends VarArgFunction {

    protected final LuaJSocketLib luaJSocket;

    public AbstractLuaJSocketFunction(LuaJSocketLib env) {
        luaJSocket = env;
    }

    public LuaJSocketLib getLuaJSocket() {
        return luaJSocket;
    }

    public abstract Varargs invoke(Varargs args);
}
