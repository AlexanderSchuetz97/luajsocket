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
package io.github.alexanderschuetz97.luajsocket.udp.lua;

import io.github.alexanderschuetz97.luajsocket.udp.java.UDPMaster;
import org.luaj.vm2.LuaUserdata;
import org.luaj.vm2.LuaValue;

import java.util.HashMap;
import java.util.Map;

public class UDPMasterUserdata extends LuaUserdata {
    private final UDPMaster master;


    private final Map<String, LuaValue> functionMap = new HashMap<>();

    public UDPMasterUserdata(UDPMaster master) {
        super(master);
        this.master = master;
    }

    public UDPMaster getMaster() {
        return master;
    }

    public void setFunction(String func, LuaValue value) {
        functionMap.put(func, value);
    }

    @Override
    public LuaValue get(LuaValue key) {
        if (!key.isstring()) {
            return super.get(key);
        }
        LuaValue lv = functionMap.get(key.checkjstring());
        if (lv == null) {
            return super.get(key);
        }

        return lv;
    }
}
