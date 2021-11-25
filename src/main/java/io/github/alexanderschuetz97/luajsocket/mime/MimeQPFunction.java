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
package io.github.alexanderschuetz97.luajsocket.mime;

import io.github.alexanderschuetz97.luajsocket.lib.LuaJSocketLib;
import io.github.alexanderschuetz97.luajsocket.socket.AbstractLuaJSocketFunction;
import io.github.alexanderschuetz97.luajsocket.util.ByteArrayOutputStreamWithBufferAccess;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import static io.github.alexanderschuetz97.luajsocket.util.Util.*;

/**
 * This function always returns one string unless it has 2 input parameters and the second input parameter concatenated to the second
 * has either a tab or a space within the last 2 bytes of the concatenated string.
 * (important BYTES not characters! UTF can have up to 4 bytes per char)
 * in this case the last 2 or 1 byte is returned as a new string of the second parameter.
 * I have decided not to question this, but this is what the luasocket equivalent function does. In addition to normal QP encoding it appears the luasocket encoding has some sort of a bug
 * the string ' a' translates to '=20a=' even tho it should translate to ' a'. This is not really an issue as i expect most decoders should be able to handle this but its still not according to spec.
 * This function once again behaves like the luasocket one and keeps this 'bug' (if it even is a bug, i just couldn't find the RFC for QP mentioning the necessity for this).
 */
public class MimeQPFunction extends AbstractLuaJSocketFunction {



    public MimeQPFunction(LuaJSocketLib env) {
        super(env);
    }

    @Override
    public Varargs invoke(Varargs args) {
        if (args.isnoneornil(1)) {
            return NIL_NIL;
        }

        LuaString slv = args.checkstring(1);
        LuaValue s2lv = args.arg(2).tostring();
        byte[] cbuf;
        if (s2lv.isnil()) {
            cbuf = toByteArray(slv);
        } else {
            LuaString s2l = s2lv.checkstring();
            cbuf = new byte[slv.m_length + s2l.m_length];
            System.arraycopy(slv.m_bytes, slv.m_offset, cbuf, 0, slv.m_length);
            System.arraycopy(s2l.m_bytes, s2l.m_offset, cbuf, slv.m_length, s2l.m_length);
        }


        boolean appendTheEnd = false;
        byte[] crlf = toByteArray(args.optstring(3, CRLF));
        ByteArrayOutputStreamWithBufferAccess baos = new ByteArrayOutputStreamWithBufferAccess(cbuf.length+64);
        int i;
        int forcedBytes = 0;
        for ( i=0; i < cbuf.length; i++) {
            byte c = cbuf[i];
            if (forcedBytes > 0) {
                baos.write(HEX_FOR_QP_ENCODING[unsignedByte(c)]);
                forcedBytes--;
                continue;
            }

            if (c == '\r' && i+1 < cbuf.length && cbuf[i+1] == '\n') {
                baos.write(crlf);
                i++;
                continue;
            }

            if (c == '=') {
                baos.write(EQUALS_FOR_QP_ENCODING);
                continue;
            }

            if (c == '\t' || c == ' ') {
                 if (cbuf.length-i > 2) {
                     baos.write(c);
                     continue;
                 }
                 if (s2lv.isnil()) {
                     appendTheEnd = true;
                 } else {
                     break;
                 }
            }

            if (c < '!' || c > '~') {
                forcedBytes = utf8Length(c)-1;
                baos.write(HEX_FOR_QP_ENCODING[unsignedByte(c)]);
                continue;
            }

            baos.write(c);
        }

        if (appendTheEnd) {
            baos.write('=');
        }

        if (s2lv.isnil()) {
            return varargsOf(LuaString.valueUsing(baos.getBuffer(), 0, baos.size()), NIL);
        }

        if (i < cbuf.length) {
            byte[] b = new byte[cbuf.length-i];
            System.arraycopy(cbuf, i, b, 0, b.length);
            return varargsOf(LuaString.valueUsing(baos.getBuffer(), 0, baos.size()), LuaString.valueUsing(b));
        }

        return varargsOf(LuaString.valueUsing(baos.getBuffer(), 0, baos.size()), EMPTYSTRING);



    }
}
