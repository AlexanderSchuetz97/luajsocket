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

package io.github.alexanderschuetz97.luajsocket.dns;

import io.github.alexanderschuetz97.luajsocket.lib.LuaJSocketLib;
import io.github.alexanderschuetz97.luajsocket.util.Util;
import io.github.alexanderschuetz97.luajsocket.socket.AbstractLuaJSocketFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * This function will perform a standard ip address lookup using InetAddress.getByName.
 * It will never return any additional information as the luasocket spec sais it should
 * (but it doesn't even specify what those additional infos are...)
 * instead it always returns a empty table in its stead.
 *
 * This function returns the result of the {@link InetAddress#getCanonicalHostName()} to lua.
 */
public class DNSToHostnameFunction extends AbstractLuaJSocketFunction {

    public DNSToHostnameFunction(LuaJSocketLib env) {
        super(env);
    }

    @Override
        public Varargs invoke(Varargs args) {
            if (args.narg() < 1) {
                return varargsOf(NIL, valueOf("Expected address (string) got nothing"));
            }
            LuaValue addr = args.arg1().tostring();
            if (addr.isnil()) {
                return varargsOf(NIL, valueOf("Expected address (string) got " + args.arg1().typename()));
            }

            try {
                return varargsOf(valueOf(InetAddress.getByName(addr.checkjstring()).getCanonicalHostName()), new LuaTable());
            } catch (UnknownHostException e) {
                return varargsOf(NIL, Util.stringToLuaString(e.getMessage()));
            }
        }
}
