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
import io.github.alexanderschuetz97.luajsocket.socket.AbstractLuaJSocketFunction;
import io.github.alexanderschuetz97.luajsocket.util.Util;
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
 * This function returns a string representation of {@link InetAddress} that is either:
 * xxx.xxx.xxx.xxx for ipv4 or
 * xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx:xxxx for ipv6
 *
 * The ipv4 address has leading 0s removed for each block.
 * The ipv6 address always has full length since the minimization rules are not that simple for ipv6. (They are not not implemented yet...) //TODO
 */
public class DNSToIpFunction extends AbstractLuaJSocketFunction {

    public DNSToIpFunction(LuaJSocketLib env) {
        super(env);
    }

    @Override
    public Varargs invoke(Varargs args) {
        if (args.narg() < 1) {
            return varargsOf(NIL, valueOf("Expected address got nothing"));
        }
        LuaValue addr = args.arg1().tostring();
        if (addr.isnil()) {
            return varargsOf(NIL, valueOf("Expected address got " + args.arg1().typename()));
        }

        InetAddress address;

        try {
            address = InetAddress.getByName(addr.checkjstring());
        } catch (UnknownHostException e) {
            return varargsOf(NIL, Util.stringToLuaString(e.getMessage()));
        }

        String toStr = Util.ipAddressToString(address);
        if (toStr == null) {
            return varargsOf(NIL, valueOf("Unexpected address type " + address.getClass().getName()));
        }

        return valueOf(toStr);
    }



}
