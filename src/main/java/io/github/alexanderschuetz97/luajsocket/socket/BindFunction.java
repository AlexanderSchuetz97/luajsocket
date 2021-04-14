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
package io.github.alexanderschuetz97.luajsocket.socket;

import io.github.alexanderschuetz97.luajsocket.lib.LuaJSocketLib;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/**
 * This function is a shortcut to bind a tcp socket.
 * It will enable "reuseaddr" before binding.
 *
 * Arguments are identical to bind and passed to it as is.
 */
public class BindFunction extends AbstractLuaJSocketFunction {

    public BindFunction(LuaJSocketLib env) {
        super(env);
    }

    @Override
    public Varargs invoke(Varargs args) {
        LuaValue value = luaJSocket.createTCP();
        Varargs res = value.get("setoption").invoke(varargsOf(valueOf("reuseaddr"), TRUE));
        if (res.isnoneornil(1)) {
            return res;
        }

        res = value.get("bind").invoke(varargsOf(value, args));
        if (res.isnoneornil(1)) {
            return res;
        }

        return value;
    }
}
