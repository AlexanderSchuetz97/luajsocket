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
package io.github.alexanderschuetz97.luajsocket.util;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Util {

    public static final LuaString CRLF = LuaString.valueOf("\r\n");
    public static final byte[] CRLF_BYTES = new byte[]{'\r','\n'};
    public static final byte[] CRLF_BYTES_QP = new byte[]{'=','\r','\n'};
    public static final Varargs NIL_ZERO = LuaValue.varargsOf(LuaValue.NIL, LuaValue.ZERO);
    public static final Varargs NIL_NIL = LuaValue.varargsOf(LuaValue.NIL, LuaValue.NIL);
    public static final byte[] BASE64_TABLE = new byte[] {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };

    public static final Integer[] B64TABLE_REVERSE = new Integer[123];

    static {
        for (int i = 0; i < BASE64_TABLE.length; i++) {
            B64TABLE_REVERSE[BASE64_TABLE[i]] = i;
        }
    }

    public static final String[] HEX = new String[0xff+1];
    public static final byte[][] HEX_BYTES = new byte[0xff+1][];
    public static final byte[][] HEX_FOR_QP_ENCODING = new byte[0xff+1][];
    public static final byte[] EQUALS_FOR_QP_ENCODING = new byte[]{'=','3','D'};

    static {
        for (int i = 0; i <= 0xff; i++) {
            String s = Integer.toHexString(i).toUpperCase();
            while (s.length() < 2) {
                s  = "0" + s;
            }

            HEX[i] = s;
            HEX_BYTES[i] = new byte[]{ (byte) s.charAt(0), (byte) s.charAt(1)};
            HEX_FOR_QP_ENCODING[i] = new byte[]{'=', (byte) s.charAt(0), (byte) s.charAt(1)};
        }

        for (int i = 0; i <= 0xff; i++) {
            String s = HEX[i];
            HEX_BYTES[i] = new byte[]{ (byte) s.charAt(0), (byte) s.charAt(1)};

        }
    }

    /**
     * returns the length in bytes of a utf8 character given the first byte
     */
    public static int utf8Length(byte b) {
        int fb = unsignedByte(b);
        if (fb < 0x80) {
            return 1;
        }

        if (fb < 0xe0) {
            return 2;
        }

        if (fb < 0xf0) {
            return 3;
        }

        return 4;
    }

    public static List<LuaValue> tableToArrayListIPairs(LuaValue table) {
        List<LuaValue> tempL = new ArrayList<>();
        if (table == null || !table.istable()) {
            return tempL;
        }
        LuaValue k = LuaValue.NIL;
        while ( true ) {
            Varargs n = table.inext(k);
            if ( (k = n.arg1()).isnil() )
                break;
            LuaValue v = n.arg(2);
            tempL.add(v);
        }
        return tempL;
    }

    public static LuaValue stringToLuaString(String string) {
        if (string == null) {
            return LuaValue.NIL;
        }

        return LuaValue.valueOf(string);
    }

    public static LuaValue toFunction(LuaValue value) {
        if (value == null || !value.isfunction()) {
            return LuaValue.NIL;
        }

        return value.checkfunction();
    }

    public static boolean toBoolean(LuaValue value) {
        if (value == null) {
            return false;
        }

        if (!value.isboolean()) {
            return false;
        }

        return value.checkboolean();
    }

    public static int optInt(LuaValue value, int fallback) {
        if (value == null) {
            return fallback;
        }

        LuaValue lv = value.tonumber();
        if (lv.isnil()) {
            return fallback;
        }

        if (!lv.isint()) {
            return fallback;
        }


        return lv.checkint();
    }

    public static String optString(LuaValue value, String fallback) {
        if (value == null) {
            return fallback;
        }

        LuaValue vs = value.tostring();
        if (vs.isnil()) {
            return fallback;
        }

        return vs.checkjstring();
    }

    public static byte[] toByteArray(LuaString ls) {
        byte[] b = new byte[ls.m_length];
        System.arraycopy(ls.m_bytes, ls.m_offset, b, 0, ls.m_length);
        return b;
    }

    public static LuaTable mapToTable(Map<?,?> map) {
        LuaTable lt = new LuaTable();
        if (map == null) {
            return lt;
        }

        for (Map.Entry e : map.entrySet()) {
            lt.set(CoerceJavaToLua.coerce(e.getKey()), CoerceJavaToLua.coerce(e.getValue()));
        }

        return lt;
    }

    public static byte[] readAllBytesFromInputStream(InputStream inputStream) throws IOException {
        byte[] buf = new byte[512];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i = 0;
        while(i != -1) {
            i = inputStream.read(buf);
            if (i > 0) {
                baos.write(buf, 0, i);
            }
        }

        return baos.toByteArray();
    }

    public static String inputStreamToString(InputStream inputStream) throws IOException {
        return new String(readAllBytesFromInputStream(inputStream));
    }

    public static String bthx(byte b) {
        String hx = Integer.toHexString(unsignedByte(b));
        if (hx.length() == 1) {
            return "0"+hx;
        }

        return hx;
    }

    public static int unsignedByte(byte b) {
        return ((int) b) & 0xff;
    }

    public static String ipAddressToString(InetAddress address) {
        if (address == null) {
            return null;
        }

        byte[] bytes = address.getAddress();
        if (bytes == null) {
            return null;
        }

        switch (bytes.length) {
            case 4:
                return unsignedByte(bytes[0]) + "." + unsignedByte(bytes[1]) + "." + unsignedByte(bytes[2]) + "." + unsignedByte(bytes[3]);
            case 16:
                return ipv6ToString(bytes);
            default:
                return null;
        }
    }

    public static String ipv6ToString(byte[] ipv6) {
        StringBuilder ipv6StrBuilder = new StringBuilder(39);

        for (int i = 0; i < 8; i++) {
            String section;
            section = bthx(ipv6[i*2]);
            section += bthx(ipv6[(i*2)+1]);
            if (i != 0) {
                ipv6StrBuilder.append(":");
            }
            ipv6StrBuilder.append(section);
        }

        return ipv6StrBuilder.toString();
    }
}
