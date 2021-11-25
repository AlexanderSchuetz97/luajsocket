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
package io.github.alexanderschuetz97.luajsocket.dns;

import io.github.alexanderschuetz97.luajsocket.lib.LuaJSocketLib;
import io.github.alexanderschuetz97.luajsocket.socket.AbstractLuaJSocketFunction;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.Varargs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Function to get the DNS hostname by using all means available to a java application.
 * 1. InetAddress.getLocalHost().getHostName();
 * -> This will fail if the "hostname" is not in /etc/hosts or its windows equivalent.
 * 2. ENV variable COMPUTERNAME is read (this will work on some windows machines)
 * 3. ENV variable HOSTNAME is read (this will work on some linux machines)
 * 4. Use Process builder to run the hostname program and read the hostname from stdin.
 *
 * If all of the above fails then empty string is used.
 * This function caches the first successful hostname retrieved by this procedure.
 */
public class DNSGetHostnameFunction extends AbstractLuaJSocketFunction {

    protected String cache;

    public DNSGetHostnameFunction(LuaJSocketLib env) {
        super(env);
    }

    protected boolean isHostname(String aStr) {
        return aStr != null && aStr.length() > 0;
    }

    protected String resolve() {
        String hostname = null;
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            //DC.
        }

        if (isHostname(hostname)) {
            return hostname;
        }

        hostname = System.getenv("COMPUTERNAME");
        if (isHostname(hostname)) {
            return hostname;
        }

        hostname = System.getenv("HOSTNAME");
        if (isHostname(hostname)) {
            return hostname;
        }

        try {
            Process process = Runtime.getRuntime().exec("hostname");
            try (BufferedReader stdin = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                hostname = stdin.readLine();
            }
        } catch (IOException e) {
            //DC
        }

        if (isHostname(hostname)) {
            return hostname;
        }

        //We tried.
        return null;
    }

    @Override
    public Varargs invoke(Varargs args) {
        if (cache != null) {
            return valueOf(cache);
        }
        cache = resolve();

        if (cache == null) {
            return LuaString.EMPTYSTRING;
        }

        return valueOf(cache);
    }
}

