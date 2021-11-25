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
package io.github.alexanderschuetz97.luajsocket;

import io.github.alexanderschuetz97.luajsocket.tcp.java.RingBuffer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RingBufferTest {

    private ExecutorService executorService;

    RingBuffer buffer;

    @Before
    public void before() {
        executorService = Executors.newCachedThreadPool();
        buffer = new RingBuffer();
    }

    @After
    public void after() {
        buffer.eof();
        executorService.shutdownNow();
        buffer = null;
        executorService = null;
    }

    @Test
    public void testReadAll() {
        final byte[] bbb = new byte[0xffff];
        new Random().nextBytes(bbb);
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    writeToBuffer(bbb, 1, 256, 5, 50);
                } catch (Exception e) {
                    e.printStackTrace();
                    Assert.fail();
                }
            }
        });

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            buffer.readAll(baos, -1, -1);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }

        Assert.assertArrayEquals(bbb, baos.toByteArray());
    }


    private void writeToBuffer(byte[] buf, int count, int step, int delayMin, int delayMax) throws IOException {
        Random rand = new Random();
        rand.setSeed(1L);
        while(count > 0) {
            count--;
            int wcount =0;
            while(wcount < buf.length) {
                wcount+=buffer.write(buf, wcount, Math.min(step,buf.length-wcount), -1, -1);
                try {
                    Thread.sleep(rand.nextInt((delayMax-delayMin)+1)+delayMin);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        buffer.eof();
    }
}
