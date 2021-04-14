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
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

/**
 * This function will wrap a function in a pcall.
 *
 * The return value of this function is either the return value of the original function
 * or whatever argument was passed to "error" somewhere inside the original function.
 */
public class ProtectFunction extends AbstractLuaJSocketFunction {


    public ProtectFunction(LuaJSocketLib env) {
        super(env);
    }

    @Override
    public Varargs invoke(Varargs args) {
        final LuaFunction func = args.checkfunction(1);
        return new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
               try {
                   return func.invoke(args);
               } catch (LuaError e) {
                   return e.getMessageObject();
               }
            }
        };
    }
}
