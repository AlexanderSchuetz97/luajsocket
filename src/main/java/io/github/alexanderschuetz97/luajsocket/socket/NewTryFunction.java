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
package io.github.alexanderschuetz97.luajsocket.socket;

import io.github.alexanderschuetz97.luajsocket.lib.LuaJSocketLib;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

/**
 * This Function involved a lot of functions so let me name them to make it less configusion.
 * NewTryFunction -> A
 * Returned Function -> B
 * First Parameter Function -> C
 *
 * A (this function) will return the function B.
 *
 * When B is called with a non nil first argument or no arguments at all it will simply return whatever it was called
 * with as a result.
 *
 * If the first argument to B is nil however it will call C without parameters then after C returns it will call error
 * with the second argument of A as its parameter.
 *
 * Luasocket recommends using this to ensure that resources such as sockets are closed since lua lacks a finally paradigm
 * he made one himself. (Most of his methods return NIL as the first return value upon failure)
 *
 * This function is useless outside of using it for luasocket methods or methods that follow this pattern.
 */
public class NewTryFunction extends AbstractLuaJSocketFunction {

    public NewTryFunction(LuaJSocketLib env) {
        super(env);
    }

    @Override
    public Varargs invoke(Varargs args) {
        final LuaValue func = args.arg(1);
        return new VarArgFunction() {
            @Override
            public Varargs invoke(Varargs args) {
                if (args.narg() == 0) {
                    return NONE;
                }

                if (args.isnil(1)) {
                    if (func.isfunction()) {
                        func.call();
                    }
                    throw new LuaError(args.arg(2));
                }

                return args;
            }
        };

    }
}
