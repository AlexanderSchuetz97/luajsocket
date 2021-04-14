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
import org.luaj.vm2.Varargs;

/**
 * Util function that removes parameters from the result value.
 * ex: skip(2,"a","b","c","d") returns "c","d"
 * ex: skip(1,"a","b","c","d") returns "b","c","d"
 * ex: skip(5,"a","b","c","d") returns
 * ex: skip(0,"a","b","c","d") returns 0,"a","b","c","d"
 *
 * Unfortunately the original luasocket skip function returns "random" functions from the lua stack if you
 * pass negative parameters to it. This function does not do so because it cant. negative parameters are equal
 * to calling it with 0.
 */
public class SkipFunction extends AbstractLuaJSocketFunction {

    public SkipFunction(LuaJSocketLib env) {
        super(env);
    }

    @Override
    public Varargs invoke(Varargs args) {
        int a = args.checkint(1);
        if (a <= 0) {
            return args;
        }
        return args.subargs(a+2);
    }
}
