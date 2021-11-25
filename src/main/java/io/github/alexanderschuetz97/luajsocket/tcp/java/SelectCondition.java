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

import java.util.Set;

/**
 * Helper class for the select function.
 */
public class SelectCondition {

    private final Set<TCPMaster> waitForRead;
    private final Set<TCPMaster> waitForWrite;
    private final Object mutex = new Object();
    private volatile boolean atLeastOneReady;

    public SelectCondition(Set<TCPMaster> waitForRead, Set<TCPMaster> waitForWrite) {
        this.waitForRead = waitForRead;
        this.waitForWrite = waitForWrite;
    }

    public void init() {
        for (TCPMaster wfr : waitForRead) {
            if (wfr.isClient()) {
                if (wfr.getClient().readReady()) {
                    atLeastOneReady = true;
                    return;
                }
            }

            if (wfr.isServer()) {
                if (wfr.getServer().acceptReady()) {
                    atLeastOneReady = true;
                    return;
                }
            }

            wfr.setCondition(this);
        }

        for (TCPMaster wfw : waitForWrite) {

            if (wfw.isClient()) {
                if (wfw.getClient().writeReady()) {
                    atLeastOneReady = true;
                    return;
                }
            }
            wfw.setCondition(this);
        }

    }

    public void clear() {
        for (TCPMaster m : waitForWrite) {
            m.setCondition(null);
        }

        for (TCPMaster m : waitForRead) {
            m.setCondition(null);
        }

    }


    public void notifyWriteReady(TCPMaster master) {
        synchronized (mutex) {
            if (waitForWrite.contains(master)) {
                atLeastOneReady = true;
                mutex.notifyAll();
            }
        }
    }

    public void notifyReadReady(TCPMaster master) {
        synchronized (mutex) {
            if (waitForRead.contains(master)) {
                atLeastOneReady = true;
                mutex.notifyAll();
            }
        }
    }

    public boolean awaitSelection(int timeout) {
        synchronized (mutex) {
            if (atLeastOneReady) {
                return true;
            }

            if (timeout == 0) {
                return false;
            }

            try {
                if (timeout < 0) {
                    mutex.wait();
                } else {
                    mutex.wait(timeout);
                }
            } catch (InterruptedException e) {

            }

            return atLeastOneReady;
        }
    }
}

