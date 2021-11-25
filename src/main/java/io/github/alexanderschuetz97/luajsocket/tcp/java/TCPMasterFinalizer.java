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
package io.github.alexanderschuetz97.luajsocket.tcp.java;

import io.github.alexanderschuetz97.luajsocket.tcp.lua.TCPMasterUserdata;
import io.github.alexanderschuetz97.luajsocket.util.ReferenceQueueCleaner;

import java.io.IOException;

/**
 * Garbage collection class for TCPMaster. Performs cleanup actions if a lua script does not call close...
 */
public class TCPMasterFinalizer extends ReferenceQueueCleaner.CleanerRef<TCPMasterUserdata> {

    private final TCPMaster master;

    public TCPMasterFinalizer(TCPMasterUserdata referent) {
        super(referent);
        master = referent.getMaster();
    }

    public void clean() {
        try {
            master.close();
        } catch (IOException e) {
            //DC.
        }
    }
}
