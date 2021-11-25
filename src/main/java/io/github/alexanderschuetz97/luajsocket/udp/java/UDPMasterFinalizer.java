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
package io.github.alexanderschuetz97.luajsocket.udp.java;

import io.github.alexanderschuetz97.luajsocket.udp.lua.UDPMasterUserdata;
import io.github.alexanderschuetz97.luajsocket.util.ReferenceQueueCleaner;

/**
 * Garbage collection class for UDPMaster. Performs cleanup actions if a lua script does not call close...
 */
public class UDPMasterFinalizer extends ReferenceQueueCleaner.CleanerRef<UDPMasterUserdata> {

    private final UDPMaster master;

    public UDPMasterFinalizer(UDPMasterUserdata referent) {
        super(referent);
        master = referent.getMaster();
    }

    public void clean() {
        master.close();
    }
}
