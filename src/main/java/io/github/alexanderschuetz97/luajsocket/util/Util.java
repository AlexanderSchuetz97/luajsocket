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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Contains various util functions and constants.
 */
public class Util {

    /**
     * LuaString for CRLF
     */
    public static final LuaString CRLF = LuaString.valueOf("\r\n");

    /**
     * Bytes for CR LF
     */
    public static final byte[] CRLF_BYTES = new byte[]{'\r','\n'};
    /**
     * Bytes for CRLF in QP Encoding.
     */
    public static final byte[] CRLF_BYTES_QP = new byte[]{'=','\r','\n'};
    /**
     * Bytes for escaping the '=' character in QP encoding.
     */
    public static final byte[] EQUALS_FOR_QP_ENCODING = new byte[]{'=','3','D'};

    /**
     * equivalent to lua return nil, 0
     */
    public static final Varargs NIL_ZERO = LuaValue.varargsOf(LuaValue.NIL, LuaValue.ZERO);

    /**
     * equivalent to lua return nil, nil
     */
    public static final Varargs NIL_NIL = LuaValue.varargsOf(LuaValue.NIL, LuaValue.NIL);

    /**
     * Base 64 Table.
     */
    public static final byte[] BASE64_TABLE = new byte[] {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
    };

    /**
     * Inverse base 64 table. Non Base64 Characters are null.
     * EX: 'A' -> 0
     * EX: 'D' -> 3
     */
    public static final Integer[] B64TABLE_REVERSE = new Integer[123];

    static {
        for (int i = 0; i < BASE64_TABLE.length; i++) {
            B64TABLE_REVERSE[BASE64_TABLE[i]] = i;
        }
    }

    /**
     * Array that holds 2 character upper case hexadecimal representation of all integer values from including 0x0 to including 0xff.
     */
    public static final String[] HEX = new String[0xff+1];
    /**
     * Array that holds 2 ASCI byte upper case hexadecimal representation of all integer values from including 0x0 to including 0xff.
     * EX: 0x00 -> 0x30, 0x30 (Hint 0x30 is the character 0 in the ascii table)
     * EX: 0x25 -> 0x32, 0x35 (0x32 is '2', 0x35 is '5')
     */
    public static final byte[][] HEX_BYTES = new byte[0xff+1][];
    /**
     * Same as HEX_BYTES but instead the array is always 3 bytes long. The first byte is always 0x3D or '=' because
     * this is the prefix needed for QP encoding.
     */
    public static final byte[][] HEX_FOR_QP_ENCODING = new byte[0xff+1][];



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

    /**
     * Converts a LuaTables array part to a list.
     * Indexes will be shifted by one. lua 1 -> java 0 because indexes in java start at 0.
     */
    public static List<LuaValue> tableToArrayListIPairs(LuaValue table) {
        List<LuaValue> tempL = new ArrayList<>();
        if (table == null || !table.istable()) {
            return tempL;
        }
        LuaValue k = LuaValue.ZERO;
        while ( true ) {
            Varargs n = table.inext(k);
            if ( (k = n.arg1()).isnil() )
                break;
            LuaValue v = n.arg(2);
            tempL.add(v);
        }
        return tempL;
    }

    /**
     * Converts a java string to a LuaString or NIL if null.
     */
    public static LuaValue stringToLuaString(String string) {
        if (string == null) {
            return LuaValue.NIL;
        }

        return LuaValue.valueOf(string);
    }

    /**
     * Returns the lua boolean value of the parameter or false if not a bool.
     */
    public static boolean toBoolean(LuaValue value) {
        if (value == null) {
            return false;
        }

        if (!value.isboolean()) {
            return false;
        }

        return value.checkboolean();
    }

    /**
     * Returns the value of the parameter or the fallback if the parameter is null/nil or not a int.
     */
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

    /**
     * Returns the value of the parameter or the fallback if the parameter is null/nil or not a string.
     */
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

    /**
     * Copies a lua string to a byte array.
     */
    public static byte[] toByteArray(LuaString ls) {
        byte[] b = new byte[ls.m_length];
        System.arraycopy(ls.m_bytes, ls.m_offset, b, 0, ls.m_length);
        return b;
    }

    /**
     * Converts a java map to a lua table.
     * Keys and values are coerced using CoerceJavaToLua.
     */
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

    /**
     * Read all bytes of a input stream into a byte array.
     */
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

    /**
     * Reads all bytes from the input stream into a string using default encoding.
     */
    public static String inputStreamToString(InputStream inputStream) throws IOException {
        return new String(readAllBytesFromInputStream(inputStream));
    }

    /**
     * Returns a 2 character long hexadecimal representation of the unsigned value of the input.
     */
    public static String byteToHex(byte b) {
        return HEX[unsignedByte(b)];
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
            section = byteToHex(ipv6[i*2]);
            section += byteToHex(ipv6[(i*2)+1]);
            if (i != 0) {
                ipv6StrBuilder.append(":");
            }
            ipv6StrBuilder.append(section);
        }

        return ipv6StrBuilder.toString();
    }

    public static Map<String, byte[]> readZipFile(InputStream zip) throws IOException {
        Map<String, byte[]> files = new HashMap<>();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[512];

        try (ZipInputStream zipInput = new ZipInputStream(zip)) {
            while (true) {
                ZipEntry entry = zipInput.getNextEntry();
                if (entry == null) {
                    return files;
                }

                baos.reset();
                int i = 0;
                while (i != -1) {
                    i = zipInput.read(buffer);
                    if (i > 0) {
                        baos.write(buffer, 0, i);
                    }
                }

                files.put(entry.getName(), baos.toByteArray());
                zipInput.closeEntry();
            }
        }


    }

}
