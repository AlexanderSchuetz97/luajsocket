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

import static io.github.alexanderschuetz97.luajsocket.util.Util.CRLF;
import static io.github.alexanderschuetz97.luajsocket.util.Util.NIL_ZERO;

/**
 * This function replaces all common end of line markers with either CRLF or the third parameter.
 * This function follows the standard concatenation pattern for stream processing in luasocket.
 * This is explained in {@link MimeB64Function}
 */
public class MimeEolFunction extends AbstractLuaJSocketFunction {

    public MimeEolFunction(LuaJSocketLib env) {
        super(env);
    }

    @Override
    public Varargs invoke(Varargs args) {
        int state = args.checkint(1);
        LuaValue strV = args.arg(2).tostring();
        if (strV.isnil()) {
            return NIL_ZERO;
        }
        LuaString str = strV.checkstring();
        LuaString marker = args.optstring(3, CRLF);
        ByteArrayOutputStreamWithBufferAccess baos = new ByteArrayOutputStreamWithBufferAccess(str.m_length+(marker.m_length > 1 ? 16 : 0));
        for (int i = 0; i < str.m_length; i++) {
            byte b = str.m_bytes[i+str.m_offset];
            switch (b) {
                case '\r':
                    if (state == '\n') {
                        state = 0;
                        continue;
                    }
                    baos.write(marker.m_bytes, marker.m_offset, marker.m_length);
                    if (state == '\r') {
                        state = 0;
                        continue;
                    }
                    state = '\r';
                    continue;
                case '\n':
                    if (state == '\r') {
                        state = 0;
                        continue;
                    }
                    baos.write(marker.m_bytes, marker.m_offset, marker.m_length);
                    if (state == '\n') {
                        state = 0;
                        continue;
                    }
                    state = '\n';
                    continue;
                default:
                    baos.write(b);
                    state = 0;
            }
        }

        LuaString luaString = LuaString.valueUsing(baos.getBuffer(), 0, baos.size());
        return varargsOf(luaString, valueOf(state));
    }
}
