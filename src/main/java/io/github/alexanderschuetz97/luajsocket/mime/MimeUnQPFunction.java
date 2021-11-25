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
package io.github.alexanderschuetz97.luajsocket.mime;

import io.github.alexanderschuetz97.luajsocket.lib.LuaJSocketLib;
import io.github.alexanderschuetz97.luajsocket.socket.AbstractLuaJSocketFunction;
import io.github.alexanderschuetz97.luajsocket.util.ByteArrayOutputStreamWithBufferAccess;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import static io.github.alexanderschuetz97.luajsocket.util.Util.NIL_NIL;

/**
 * This function decodes a string from qp encoding back to normal.
 * To generate a string in QP encoding {@link MimeQPFunction} may be used.
 * This function follows the standard concatenation pattern for stream processing in luasocket.
 * This is explained in {@link MimeB64Function}
 */
public class MimeUnQPFunction extends AbstractLuaJSocketFunction {

    public MimeUnQPFunction(LuaJSocketLib env) {
        super(env);
    }

    private Byte fromHex(byte b1, byte b2) {
        char c1 = Character.toUpperCase((char) b1);
        char c2 = Character.toUpperCase((char) b2);
        if (!Character.isDigit(c1) && (c1 < 'A' || c1 > 'F')) {
            return null;
        }

        if (!Character.isDigit(c2) && (c2 < 'A' || c2 > 'F')) {
            return null;
        }

        return (byte) Integer.parseInt(((char)b1) +""+((char)b2),16);
    }

    @Override
    public Varargs invoke(Varargs args) {
        if (args.isnoneornil(1)) {
            return NIL_NIL;
        }

        LuaString str = args.checkstring(1);
        LuaValue str2 = args.arg(2).tostring();
        ByteArrayOutputStreamWithBufferAccess baos = new ByteArrayOutputStreamWithBufferAccess();
        baos.write(str.m_bytes, str.m_offset, str.m_length);
        boolean isTail = true;
        if (!str2.isnil()) {
            isTail = false;
            LuaString lstr2 = str2.checkstring();
            baos.write(lstr2.m_bytes, lstr2.m_offset, lstr2.m_length);
        }

        ByteArrayOutputStreamWithBufferAccess result = new ByteArrayOutputStreamWithBufferAccess();
        byte[] bytes = baos.toByteArray();
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            if (b != '=') {
                result.write(b);
                continue;
            }

            if (bytes.length -2 <= i) {
                if (isTail) {
                    return varargsOf(LuaString.valueUsing(result.getBuffer(), 0, result.size()), NIL);
                }

                byte[] left = new byte[bytes.length - i];
                System.arraycopy(bytes, i, left, 0, left.length);
                return varargsOf(LuaString.valueUsing(result.getBuffer(), 0, result.size()), LuaString.valueUsing(left));
            }

            byte b1 = bytes[++i];
            byte b2 = bytes[++i];
            Byte res = fromHex(b1, b2);
            if (res == null) {
                result.write(b);
                result.write(b1);
                result.write(b2);
                continue;
            }

            result.write(res);
        }

        return varargsOf(LuaString.valueUsing(result.getBuffer(), 0, result.size()), isTail ? NIL : EMPTYSTRING);
    }
}
