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
package io.github.alexanderschuetz97.luajsocket.mime;

import io.github.alexanderschuetz97.luajsocket.lib.LuaJSocketLib;
import io.github.alexanderschuetz97.luajsocket.socket.AbstractLuaJSocketFunction;
import io.github.alexanderschuetz97.luajsocket.util.ByteArrayOutputStreamWithBufferAccess;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

/**
 * Function required by the SMTP protocol to escape "\r\n." with "\r\n..".
 * This function follows the standard concatenation pattern for stream processing in luasocket.
 * This is explained in {@link MimeB64Function}
 */
public class MimeDotFunction extends AbstractLuaJSocketFunction {

    public MimeDotFunction(LuaJSocketLib env) {
        super(env);
    }

    @Override
    public Varargs invoke(Varargs args) {
        LuaValue number = args.checknumber(1);
        LuaValue str = args.arg(2).tostring();
        if (str.isnil()) {
            return varargsOf(NIL, number);
        }
        int state = number.checkint();
        LuaString lstr = str.checkstring();
        ByteArrayOutputStreamWithBufferAccess baos = new ByteArrayOutputStreamWithBufferAccess(lstr.m_length+16);
        for (int i = 0; i < lstr.m_length;i++) {
            byte b = lstr.m_bytes[i+lstr.m_offset];
            baos.write(b);
            switch (b) {
                case '\r':
                    state = 1;
                    continue;
                case '\n':
                    state = state == 1 ? 2 : 0;
                    continue;
                case '.':
                    if (state == 2) {
                        baos.write('.');
                    }
                    state = 0;
                    continue;
                default:
                    state = 0;
            }
        }

        LuaString luaString = LuaString.valueUsing(baos.getBuffer(), 0, baos.size());
        return varargsOf(luaString, valueOf(state));
    }
}
