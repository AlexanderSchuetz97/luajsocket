package io.github.alexanderschuetz97.luajsocket.socket;
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

import io.github.alexanderschuetz97.luajsocket.lib.LuaJSocketLib;
import io.github.alexanderschuetz97.luajsocket.tcp.java.SelectCondition;
import io.github.alexanderschuetz97.luajsocket.tcp.java.TCPMaster;
import io.github.alexanderschuetz97.luajsocket.tcp.lua.TCPMasterUserdata;
import io.github.alexanderschuetz97.luajsocket.util.Util;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This function takes 2 tables as parameters and a optional timeout.
 * The 2 tables should contain socket objects in numeric indices (created by socket.connect/socket.bind etc)
 *
 * The first table is sockets that should be checked if they can receive data without blocking
 * The second table is sockets that should be checked if they can send data without blocking
 *
 * This function returns when any of the sockets can perform its desired operation without blocking or if the timeout elapses.
 * The function returns a table which contains all ready socket objects (multiple may be ready) and is linked both with
 * number indices and the socket objects itself (to allow for faster checking if a specific socket is ready)
 */
public class SelectFunction extends AbstractLuaJSocketFunction {


    public SelectFunction(LuaJSocketLib env) {
        super(env);
    }

    @Override
    public Varargs invoke(Varargs args) {
        Collection<LuaValue> recvt = Util.tableToArrayListIPairs(args.arg1());
        Collection<LuaValue> sendt = Util.tableToArrayListIPairs(args.arg(2));
        int timeout = args.optint(3, -1);

        //Timeout is in seconds...
        if (timeout > 0) {
            timeout*=1000;
        }

        LuaTable resultReadyForWrite = new LuaTable();
        LuaTable resultReadyForRead = new LuaTable();

        Set<TCPMaster> readReady = new LinkedHashSet<>();
        Set<TCPMaster> writeReady = new LinkedHashSet<>();

        List<TCPMasterUserdata> readReadyUserdata = new ArrayList<>();
        List<TCPMasterUserdata> writeReadyUserdata = new ArrayList<>();

        for (LuaValue v : recvt) {
            if (!(v instanceof TCPMasterUserdata)) {
                continue;
            }
            TCPMasterUserdata tcpMaster = ((TCPMasterUserdata) v);
            if (!tcpMaster.getMaster().isOpen()) {
                continue;
            }

            if (readReady.add(tcpMaster.getMaster())) {
                readReadyUserdata.add(tcpMaster);
            }

        }

        for (LuaValue v : sendt) {
            if (!(v instanceof TCPMasterUserdata)) {
                continue;
            }
            TCPMasterUserdata tcpMaster = ((TCPMasterUserdata) v);
            if (!tcpMaster.getMaster().isClient()) {
                continue;
            }

            if (writeReady.add(tcpMaster.getMaster())) {
                writeReadyUserdata.add(tcpMaster);
            }
        }

        if (readReady.isEmpty() && writeReady.isEmpty()) {
            if (timeout < 0) {
                return varargsOf(resultReadyForRead, resultReadyForWrite, NIL);
            }
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                return varargsOf(resultReadyForRead, resultReadyForWrite, NIL);
            }

            return varargsOf(resultReadyForWrite, resultReadyForRead, valueOf("timeout"));
        }

        SelectCondition condition = new SelectCondition(readReady, writeReady);
        condition.init();
        boolean selected = condition.awaitSelection(timeout);
        condition.clear();
        if (!selected) {
            return varargsOf(resultReadyForWrite, resultReadyForRead, valueOf("timeout"));
        }

        int i;
        i = 1;
        for (TCPMasterUserdata tcpMasterUserdata : readReadyUserdata) {
            TCPMaster tcpMaster = tcpMasterUserdata.getMaster();
            if (tcpMaster.isClient()) {
                if (!tcpMaster.getClient().readReady()) {
                    continue;
                }
            }

            if (tcpMaster.isServer()) {
                if (!tcpMaster.getServer().acceptReady()) {
                    continue;
                }
            }



            resultReadyForRead.set(i, tcpMasterUserdata);
            resultReadyForRead.set(tcpMasterUserdata, tcpMasterUserdata);
            i++;
        }

        i = 1;
        for (TCPMasterUserdata tcpMasterUserdata : writeReadyUserdata) {
            TCPMaster tcpMaster = tcpMasterUserdata.getMaster();
            if (tcpMaster.isClient()) {
                if (!tcpMaster.getClient().writeReady()) {
                    continue;
                }
            }

            resultReadyForWrite.set(i, tcpMasterUserdata);
            resultReadyForWrite.set(tcpMasterUserdata, tcpMasterUserdata);
            i++;
        }



        return varargsOf(resultReadyForRead, resultReadyForWrite, NIL);
    }
}
