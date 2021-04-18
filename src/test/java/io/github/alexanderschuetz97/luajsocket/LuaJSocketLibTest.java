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
package io.github.alexanderschuetz97.luajsocket;

import io.github.alexanderschuetz97.luajsocket.lib.LuaJSocketLib;
import io.github.alexanderschuetz97.luajsocket.lib.MobDebugCompatibleDebugLib;
import io.github.alexanderschuetz97.luajsocket.util.Util;
import org.junit.*;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;
import sun.misc.BASE64Encoder;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.alexanderschuetz97.luajsocket.util.Util.toByteArray;
import static io.github.alexanderschuetz97.luajsocket.util.Util.mapToTable;


public class LuaJSocketLibTest {


    public static final ExecutorService EX = Executors.newCachedThreadPool();

    //chosen randomly.
    private static final int AVAILABLE_PORT = 26547;

    private static final Map<Integer, String> B64TV = new HashMap<>();
    private static final Map<String, String> B64TV2 = new HashMap<>();
    static {
        B64TV.put(0,"AA==");
        B64TV.put(1,"AQ==");
        B64TV.put(2,"Ag==");
        B64TV.put(3,"Aw==");
        B64TV.put(4,"BA==");
        B64TV.put(5,"BQ==");
        B64TV.put(6,"Bg==");
        B64TV.put(7,"Bw==");
        B64TV.put(8,"CA==");
        B64TV.put(9,"CQ==");
        B64TV.put(10,"Cg==");
        B64TV.put(11,"Cw==");
        B64TV.put(12,"DA==");
        B64TV.put(13,"DQ==");
        B64TV.put(14,"Dg==");
        B64TV.put(15,"Dw==");
        B64TV.put(16,"EA==");
        B64TV.put(17,"EQ==");
        B64TV.put(18,"Eg==");
        B64TV.put(19,"Ew==");
        B64TV.put(20,"FA==");
        B64TV.put(21,"FQ==");
        B64TV.put(22,"Fg==");
        B64TV.put(23,"Fw==");
        B64TV.put(24,"GA==");
        B64TV.put(25,"GQ==");
        B64TV.put(26,"Gg==");
        B64TV.put(27,"Gw==");
        B64TV.put(28,"HA==");
        B64TV.put(29,"HQ==");
        B64TV.put(30,"Hg==");
        B64TV.put(31,"Hw==");
        B64TV.put(32,"IA==");
        B64TV.put(33,"IQ==");
        B64TV.put(34,"Ig==");
        B64TV.put(35,"Iw==");
        B64TV.put(36,"JA==");
        B64TV.put(37,"JQ==");
        B64TV.put(38,"Jg==");
        B64TV.put(39,"Jw==");
        B64TV.put(40,"KA==");
        B64TV.put(41,"KQ==");
        B64TV.put(42,"Kg==");
        B64TV.put(43,"Kw==");
        B64TV.put(44,"LA==");
        B64TV.put(45,"LQ==");
        B64TV.put(46,"Lg==");
        B64TV.put(47,"Lw==");
        B64TV.put(48,"MA==");
        B64TV.put(49,"MQ==");
        B64TV.put(50,"Mg==");
        B64TV.put(51,"Mw==");
        B64TV.put(52,"NA==");
        B64TV.put(53,"NQ==");
        B64TV.put(54,"Ng==");
        B64TV.put(55,"Nw==");
        B64TV.put(56,"OA==");
        B64TV.put(57,"OQ==");
        B64TV.put(58,"Og==");
        B64TV.put(59,"Ow==");
        B64TV.put(60,"PA==");
        B64TV.put(61,"PQ==");
        B64TV.put(62,"Pg==");
        B64TV.put(63,"Pw==");
        B64TV.put(64,"QA==");
        B64TV.put(65,"QQ==");
        B64TV.put(66,"Qg==");
        B64TV.put(67,"Qw==");
        B64TV.put(68,"RA==");
        B64TV.put(69,"RQ==");
        B64TV.put(70,"Rg==");
        B64TV.put(71,"Rw==");
        B64TV.put(72,"SA==");
        B64TV.put(73,"SQ==");
        B64TV.put(74,"Sg==");
        B64TV.put(75,"Sw==");
        B64TV.put(76,"TA==");
        B64TV.put(77,"TQ==");
        B64TV.put(78,"Tg==");
        B64TV.put(79,"Tw==");
        B64TV.put(80,"UA==");
        B64TV.put(81,"UQ==");
        B64TV.put(82,"Ug==");
        B64TV.put(83,"Uw==");
        B64TV.put(84,"VA==");
        B64TV.put(85,"VQ==");
        B64TV.put(86,"Vg==");
        B64TV.put(87,"Vw==");
        B64TV.put(88,"WA==");
        B64TV.put(89,"WQ==");
        B64TV.put(90,"Wg==");
        B64TV.put(91,"Ww==");
        B64TV.put(92,"XA==");
        B64TV.put(93,"XQ==");
        B64TV.put(94,"Xg==");
        B64TV.put(95,"Xw==");
        B64TV.put(96,"YA==");
        B64TV.put(97,"YQ==");
        B64TV.put(98,"Yg==");
        B64TV.put(99,"Yw==");
        B64TV.put(100,"ZA==");
        B64TV.put(101,"ZQ==");
        B64TV.put(102,"Zg==");
        B64TV.put(103,"Zw==");
        B64TV.put(104,"aA==");
        B64TV.put(105,"aQ==");
        B64TV.put(106,"ag==");
        B64TV.put(107,"aw==");
        B64TV.put(108,"bA==");
        B64TV.put(109,"bQ==");
        B64TV.put(110,"bg==");
        B64TV.put(111,"bw==");
        B64TV.put(112,"cA==");
        B64TV.put(113,"cQ==");
        B64TV.put(114,"cg==");
        B64TV.put(115,"cw==");
        B64TV.put(116,"dA==");
        B64TV.put(117,"dQ==");
        B64TV.put(118,"dg==");
        B64TV.put(119,"dw==");
        B64TV.put(120,"eA==");
        B64TV.put(121,"eQ==");
        B64TV.put(122,"eg==");
        B64TV.put(123,"ew==");
        B64TV.put(124,"fA==");
        B64TV.put(125,"fQ==");
        B64TV.put(126,"fg==");
        B64TV.put(127,"fw==");
        B64TV.put(128,"gA==");
        B64TV.put(129,"gQ==");
        B64TV.put(130,"gg==");
        B64TV.put(131,"gw==");
        B64TV.put(132,"hA==");
        B64TV.put(133,"hQ==");
        B64TV.put(134,"hg==");
        B64TV.put(135,"hw==");
        B64TV.put(136,"iA==");
        B64TV.put(137,"iQ==");
        B64TV.put(138,"ig==");
        B64TV.put(139,"iw==");
        B64TV.put(140,"jA==");
        B64TV.put(141,"jQ==");
        B64TV.put(142,"jg==");
        B64TV.put(143,"jw==");
        B64TV.put(144,"kA==");
        B64TV.put(145,"kQ==");
        B64TV.put(146,"kg==");
        B64TV.put(147,"kw==");
        B64TV.put(148,"lA==");
        B64TV.put(149,"lQ==");
        B64TV.put(150,"lg==");
        B64TV.put(151,"lw==");
        B64TV.put(152,"mA==");
        B64TV.put(153,"mQ==");
        B64TV.put(154,"mg==");
        B64TV.put(155,"mw==");
        B64TV.put(156,"nA==");
        B64TV.put(157,"nQ==");
        B64TV.put(158,"ng==");
        B64TV.put(159,"nw==");
        B64TV.put(160,"oA==");
        B64TV.put(161,"oQ==");
        B64TV.put(162,"og==");
        B64TV.put(163,"ow==");
        B64TV.put(164,"pA==");
        B64TV.put(165,"pQ==");
        B64TV.put(166,"pg==");
        B64TV.put(167,"pw==");
        B64TV.put(168,"qA==");
        B64TV.put(169,"qQ==");
        B64TV.put(170,"qg==");
        B64TV.put(171,"qw==");
        B64TV.put(172,"rA==");
        B64TV.put(173,"rQ==");
        B64TV.put(174,"rg==");
        B64TV.put(175,"rw==");
        B64TV.put(176,"sA==");
        B64TV.put(177,"sQ==");
        B64TV.put(178,"sg==");
        B64TV.put(179,"sw==");
        B64TV.put(180,"tA==");
        B64TV.put(181,"tQ==");
        B64TV.put(182,"tg==");
        B64TV.put(183,"tw==");
        B64TV.put(184,"uA==");
        B64TV.put(185,"uQ==");
        B64TV.put(186,"ug==");
        B64TV.put(187,"uw==");
        B64TV.put(188,"vA==");
        B64TV.put(189,"vQ==");
        B64TV.put(190,"vg==");
        B64TV.put(191,"vw==");
        B64TV.put(192,"wA==");
        B64TV.put(193,"wQ==");
        B64TV.put(194,"wg==");
        B64TV.put(195,"ww==");
        B64TV.put(196,"xA==");
        B64TV.put(197,"xQ==");
        B64TV.put(198,"xg==");
        B64TV.put(199,"xw==");
        B64TV.put(200,"yA==");
        B64TV.put(201,"yQ==");
        B64TV.put(202,"yg==");
        B64TV.put(203,"yw==");
        B64TV.put(204,"zA==");
        B64TV.put(205,"zQ==");
        B64TV.put(206,"zg==");
        B64TV.put(207,"zw==");
        B64TV.put(208,"0A==");
        B64TV.put(209,"0Q==");
        B64TV.put(210,"0g==");
        B64TV.put(211,"0w==");
        B64TV.put(212,"1A==");
        B64TV.put(213,"1Q==");
        B64TV.put(214,"1g==");
        B64TV.put(215,"1w==");
        B64TV.put(216,"2A==");
        B64TV.put(217,"2Q==");
        B64TV.put(218,"2g==");
        B64TV.put(219,"2w==");
        B64TV.put(220,"3A==");
        B64TV.put(221,"3Q==");
        B64TV.put(222,"3g==");
        B64TV.put(223,"3w==");
        B64TV.put(224,"4A==");
        B64TV.put(225,"4Q==");
        B64TV.put(226,"4g==");
        B64TV.put(227,"4w==");
        B64TV.put(228,"5A==");
        B64TV.put(229,"5Q==");
        B64TV.put(230,"5g==");
        B64TV.put(231,"5w==");
        B64TV.put(232,"6A==");
        B64TV.put(233,"6Q==");
        B64TV.put(234,"6g==");
        B64TV.put(235,"6w==");
        B64TV.put(236,"7A==");
        B64TV.put(237,"7Q==");
        B64TV.put(238,"7g==");
        B64TV.put(239,"7w==");
        B64TV.put(240,"8A==");
        B64TV.put(241,"8Q==");
        B64TV.put(242,"8g==");
        B64TV.put(243,"8w==");
        B64TV.put(244,"9A==");
        B64TV.put(245,"9Q==");
        B64TV.put(246,"9g==");
        B64TV.put(247,"9w==");
        B64TV.put(248,"+A==");
        B64TV.put(249,"+Q==");
        B64TV.put(250,"+g==");
        B64TV.put(251,"+w==");
        B64TV.put(252,"/A==");
        B64TV.put(253,"/Q==");
        B64TV.put(254,"/g==");
        B64TV.put(255,"/w==");

        B64TV2.put("", "");
        B64TV2.put("f", "Zg==");
        B64TV2.put("fo", "Zm8=");
        B64TV2.put("foo", "Zm9v");
        B64TV2.put("foob", "Zm9vYg==");
        B64TV2.put("fooba", "Zm9vYmE=");
        B64TV2.put("foobar", "Zm9vYmFy");

    }

    Globals globals;
    ExecutorService executorService;
    ServerSocket server;

    @Before
    public void before() throws Exception {
        globals = JsePlatform.standardGlobals();
        globals.load(new MobDebugCompatibleDebugLib());

        executorService = Executors.newCachedThreadPool();
        globals.load(new LuaJSocketLib());
        server = new ServerSocket();
        server.setReuseAddress(true);
        server.bind(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), AVAILABLE_PORT));
    }

    @After
    public void after() {
        executorService.shutdownNow();
        executorService = null;
        globals = null;
        try {
            if (server != null) {
                server.close();
            }
        } catch (IOException e) {
            //DC
        }
        server = null;

    }

    @Test
    public void testRequire() {
        Assert.assertTrue("require socket failed", globals.load("return require('socket') ~= nil").call().checkboolean());
        Assert.assertTrue("require ltn12 failed", globals.load("return require('ltn12') ~= nil").call().checkboolean());
        Assert.assertTrue("require socket.url failed", globals.load("return require('socket.url') ~= nil").call().checkboolean());
        Assert.assertTrue("require socket.headers failed", globals.load("return require('socket.headers') ~= nil").call().checkboolean());
        Assert.assertTrue("require mime failed", globals.load("return require('mime') ~= nil").call().checkboolean());
        Assert.assertTrue("require mime.core failed", globals.load("return require('mime.core') ~= nil").call().checkboolean());
        Assert.assertTrue("require socket.http failed", globals.load("return require('socket.http') ~= nil").call().checkboolean());
        Assert.assertTrue("require socket.ftp failed", globals.load("return require('socket.ftp') ~= nil").call().checkboolean());
        Assert.assertTrue("require socket.ftp failed", globals.load("return require('socket.smtp') ~= nil").call().checkboolean());
    }

    @Test
    @Ignore
    public void testHttp() {
        Varargs res = globals.load("print(require('socket.http').request('http://localhost'))").invoke();
    }

    @Test
    @Ignore
    public void testDebug() throws Exception {
        globals.load(new InputStreamReader(new FileInputStream("/drives/4tb1/Sync/Alex/Code/LuaDebugTest/test.lua")), "test.lua").call();
    }

    @Test
    @Ignore
    public void testFTP() {
        String script2 = "x,y = require('socket.ftp').get('ftp://speedtest.tele2.net/512KB.zip;type=i') print(#x)";
        Varargs res = globals.load(script2).invoke();
    }

    @Test
    public void testTime() {
        Varargs res = globals.load("print(tostring(require('socket').gettime()))").invoke();
    }


    //////////////
    // MIME STUFF
    /////////////

    @Test
    public void testunqp() {
        Varargs res;

        res = globals.load("return require('mime.core').unqp('RRRE')").invoke();
        Assert.assertEquals("RRRE", res.arg1().checkjstring());
        Assert.assertTrue(res.isnil(2));

        res = globals.load("return require('mime.core').unqp('RRRE=30')").invoke();
        Assert.assertEquals("RRRE0", res.arg1().checkjstring());
        Assert.assertTrue(res.isnil(2));

        res = globals.load("return require('mime.core').unqp('RRRE=3X')").invoke();
        Assert.assertEquals("RRRE=3X", res.arg1().checkjstring());
        Assert.assertTrue(res.isnil(2));

        res = globals.load("return require('mime.core').unqp('RRRE=30=')").invoke();
        Assert.assertEquals("RRRE0", res.arg1().checkjstring());
        Assert.assertTrue(res.isnil(2));

        res = globals.load("return require('mime.core').unqp('RRRE=3==')").invoke();
        Assert.assertEquals("RRRE=3=", res.arg1().checkjstring());
        Assert.assertTrue(res.isnil(2));

        res = globals.load("return require('mime.core').unqp('RRRE=3','')").invoke();
        Assert.assertEquals("RRRE", res.arg1().checkjstring());
        Assert.assertEquals("=3", res.arg(2).checkjstring());

        res = globals.load("return require('mime.core').unqp('RRRE=3','0=')").invoke();
        Assert.assertEquals("RRRE0", res.arg1().checkjstring());
        Assert.assertEquals("=", res.arg(2).checkjstring());

        res = globals.load("return require('mime.core').unqp('RRRE=3','0')").invoke();
        Assert.assertEquals("RRRE0", res.arg1().checkjstring());
        Assert.assertEquals("", res.arg(2).checkjstring());
    }

    @Test
    public void testRandomB64() {
        Random rng = new Random();
        rng.setSeed(133345616598765347L);
        for (int i = 0; i < 1024;i++) {
            Varargs res;
            byte[] testData = new byte[rng.nextInt(1024)+1];
            rng.nextBytes(testData);

            byte[] copy = Arrays.copyOf(testData, testData.length);

            globals.set("testdata", LuaString.valueUsing(testData));
            res = globals.load("return  require('mime.core').b64(testdata)").invoke();
            String b64 = res.arg1().checkjstring();
            String cmp = new BASE64Encoder().encode(copy).replace("\r", "").replace("\n", "");
            Assert.assertEquals(cmp, b64);
            Assert.assertTrue(res.isnil(2));

            globals.set("testdata", b64);
            res = globals.load("return  require('mime.core').unb64(testdata)").invoke();
            byte[] luaResult = toByteArray(res.arg1().checkstring());
            Assert.assertArrayEquals(copy, luaResult);
            Assert.assertTrue(res.isnil(2));
        }

    }

    @Test
    public void testunb64() {
        Varargs res;

        res = globals.load("return  require('mime.core').unb64('YWE=')").invoke();
        Assert.assertEquals("aa", res.arg1().checkjstring());
        Assert.assertTrue(res.isnil(2));

        res = globals.load("return  require('mime.core').unb64('YWF', 'hdw==')").invoke();
        Assert.assertEquals("aaaw", res.arg1().checkjstring());
        Assert.assertEquals("", res.checkjstring(2));

        res = globals.load("return  require('mime.core').unb64('YWF', 'hdw=')").invoke();
        Assert.assertEquals("aaa", res.arg1().checkjstring());
        Assert.assertEquals("dw=", res.checkjstring(2));


        res = globals.load("return  require('mime.core').unb64('????')").invoke();
        Assert.assertTrue(res.isnil(1));
        Assert.assertTrue(res.isnil(2));
    }

    @Test
    public void testQPWRP() {
        Varargs res;

        res = globals.load("return  require('mime.core').qpwrp(4,'a=FEa=FEs', 4)").invoke();
        Assert.assertEquals("a=\r\n=FE=\r\na=\r\n=FE=\r\ns", res.arg1().checkjstring());
        Assert.assertEquals(3, res.arg(2).checkint());
    }

    @Test
    public void testWRP() {
        Varargs res;

        res = globals.load("return require('mime.core').wrp(0,'aaa', 1)").invoke();
        Assert.assertEquals("\r\na\r\na\r\na", res.arg1().checkjstring());
        Assert.assertEquals(0, res.arg(2).checkint());

        res = globals.load("return require('mime.core').wrp(5,'a\\naa', 1)").invoke();
        Assert.assertEquals("a\r\na\r\na", res.arg1().checkjstring());
        Assert.assertEquals(0, res.arg(2).checkint());

        res = globals.load("return require('mime.core').wrp(3,'aaa', 1)").invoke();
        Assert.assertEquals("aaa", res.arg1().checkjstring());
        Assert.assertEquals(0, res.arg(2).checkint());

        res = globals.load("return require('mime.core').wrp(4,'aaa', 1)").invoke();
        Assert.assertEquals("aaa", res.arg1().checkjstring());
        Assert.assertEquals(1, res.arg(2).checkint());

        res = globals.load("return require('mime.core').wrp(4,'aaa\\r\\naaa', 2)").invoke();
        Assert.assertEquals("aaa\r\naa\r\na", res.arg1().checkjstring());
        Assert.assertEquals(1, res.arg(2).checkint());
    }

    @Test
    public void testQP() {
        Varargs res;

        res = globals.load("return require('mime.core').qp('ä')").invoke();
        Assert.assertEquals("=C3=A4", res.arg1().checkjstring());
        Assert.assertTrue(res.isnil(2));

        res = globals.load("return require('mime.core').qp('Ⲁ')").invoke();
        Assert.assertEquals("=E2=B2=80", res.arg1().checkjstring());
        Assert.assertTrue(res.isnil(2));

        //Java has no character for this and will encode this with a "high surrogate" making it 7 bytes long
        //This will ofc result in a different result that is no longer comparable to using plain luasocket so
        //this will manually construct a lua string with only the given symbol.
        LuaString ls = LuaString.valueOf(new byte[]{(byte) 0xf0, (byte) 0x90,(byte) 0x84,(byte) 0x89,});
        globals.set("symbol", ls);
        res = globals.load("return require('mime.core').qp(symbol)").invoke();
        Assert.assertEquals("=F0=90=84=89", res.arg1().checkjstring());
        Assert.assertTrue(res.isnil(2));

        res = globals.load("return require('mime.core').qp(' a')").invoke();
        Assert.assertEquals("=20a=", res.arg1().checkjstring());
        Assert.assertTrue(res.isnil(2));

        res = globals.load("return require('mime.core').qp(' aa')").invoke();
        Assert.assertEquals(" aa", res.arg1().checkjstring());
        Assert.assertTrue(res.isnil(2));

        res = globals.load("return require('mime.core').qp(' ä')").invoke();
        Assert.assertEquals(" =C3=A4", res.arg1().checkjstring());
        Assert.assertTrue(res.isnil(2));

        res = globals.load("return require('mime.core').qp(' ', 'ä')").invoke();
        Assert.assertEquals(" =C3=A4", res.arg1().checkjstring());
        Assert.assertEquals("", res.arg(2).checkjstring());

        res = globals.load("return require('mime.core').qp('a ')").invoke();
        Assert.assertEquals("a=20=", res.arg1().checkjstring());
        Assert.assertTrue(res.isnil(2));

        res = globals.load("return require('mime.core').qp(' ','a')").invoke();
        Assert.assertEquals("", res.arg1().checkjstring());
        Assert.assertEquals(" a", res.arg(2).checkjstring());


        res = globals.load("return require('mime.core').qp('aaa')").invoke();
        Assert.assertEquals("aaa", res.arg1().checkjstring());
        Assert.assertTrue(res.isnil(2));


        res = globals.load("return require('mime.core').qp('!~')").invoke();
        Assert.assertEquals("!~", res.arg1().checkjstring());
        Assert.assertTrue(res.isnil(2));

        res = globals.load("return require('mime.core').qp('')").invoke();
        Assert.assertEquals("", res.arg1().checkjstring());
        Assert.assertTrue(res.isnil(2));

        res = globals.load("return require('mime.core').qp('=')").invoke();
        Assert.assertEquals("=3D", res.arg1().checkjstring());
        Assert.assertTrue(res.isnil(2));

        res = globals.load("return require('mime.core').qp('a=a')").invoke();
        Assert.assertEquals("a=3Da", res.arg1().checkjstring());
        Assert.assertTrue(res.isnil(2));

        res = globals.load("return require('mime.core').qp('=a=a')").invoke();
        Assert.assertEquals("=3Da=3Da", res.arg1().checkjstring());
        Assert.assertTrue(res.isnil(2));



        res = globals.load("return require('mime.core').qp('abc\\nasd')").invoke();
        Assert.assertEquals("abc=0Aasd", res.arg1().checkjstring());
        Assert.assertTrue(res.isnil(2));

        res = globals.load("return require('mime.core').qp('abc\\r\\nasd')").invoke();
        Assert.assertEquals("abc\r\nasd", res.arg1().checkjstring());
        Assert.assertTrue(res.isnil(2));

        res = globals.load("return require('mime.core').qp('abcasd\\r\\nasd')").invoke();
        Assert.assertEquals("abcasd\r\nasd", res.arg1().checkjstring());
        Assert.assertTrue(res.isnil(2));

        res = globals.load("return require('mime.core').qp('abcasd\\r','\\nas')").invoke();
        Assert.assertEquals("abcasd\r\nas", res.arg1().checkjstring());
        Assert.assertEquals("", res.arg(2).checkjstring());

        res = globals.load("return require('mime.core').qp('a cää@asd\\r','\\nas', 'ooo')").invoke();
        Assert.assertEquals("a c=C3=A4=C3=A4@asdoooas", res.arg1().checkjstring());
        Assert.assertEquals("", res.arg(2).checkjstring());
    }

    @Test
    public void testEOL() {
        Varargs res = globals.load("return require(\"mime.core\").eol(10, \"\\r\\nhi\\n\\n\", \"2\")").invoke();
        Assert.assertEquals("2hi22", res.arg1().checkjstring());
        Assert.assertEquals(0, res.checkint(2));

        res = globals.load("return require(\"mime.core\").eol(10, \"\\r\\nhi\\n\", \"2\")").invoke();
        Assert.assertEquals("2hi2", res.arg1().checkjstring());
        Assert.assertEquals(10, res.checkint(2));

        res = globals.load("return require(\"mime.core\").eol(10, \"\\r\\nhi\\r\", \"2\")").invoke();
        Assert.assertEquals("2hi2", res.arg1().checkjstring());
        Assert.assertEquals(13, res.checkint(2));

        res = globals.load("return require(\"mime.core\").eol(10, \"\\r\\nhi\\r\\n\", \"2\")").invoke();
        Assert.assertEquals("2hi2", res.arg1().checkjstring());
        Assert.assertEquals(0, res.checkint(2));

        res = globals.load("return require(\"mime.core\").eol(13, \"\\nhi\\n\\r\", \"2\")").invoke();
        Assert.assertEquals("hi2", res.arg1().checkjstring());
        Assert.assertEquals(0, res.checkint(2));
    }

    @Test
    public void testDot() {
        Varargs res = globals.load("return require('mime.core').dot(2, \".\\r\\nStuffing the message.\\r\\n.\\r\\n.\")").invoke();
        Assert.assertEquals("..\r\nStuffing the message.\r\n..\r\n..", res.arg1().checkjstring());
        Assert.assertEquals(0, res.checkint(2));

        res = globals.load("return require('mime.core').dot(1, \".\\r\\nStuffing the message.\\r\\n.\\r\\n.\")").invoke();
        Assert.assertEquals(".\r\nStuffing the message.\r\n..\r\n..", res.arg1().checkjstring());
        Assert.assertEquals(0, res.checkint(2));

        res = globals.load("return require('mime.core').dot(0, \".\\r\\nStuffing the message.\\r\\n.\\r\\n.\")").invoke();
        Assert.assertEquals(".\r\nStuffing the message.\r\n..\r\n..", res.arg1().checkjstring());
        Assert.assertEquals(0, res.checkint(2));

        res = globals.load("return require('mime.core').dot(1, \"\\n.\\r\\nStuffing the message.\\r\\n.\\r\\n.\")").invoke();
        Assert.assertEquals("\n..\r\nStuffing the message.\r\n..\r\n..", res.arg1().checkjstring());
        Assert.assertEquals(0, res.checkint(2));

        res = globals.load("return require('mime.core').dot(1, \"\\n.\\r\\nStuffing the message.\\r\\n.\\r\\n.\\r\")").invoke();
        Assert.assertEquals("\n..\r\nStuffing the message.\r\n..\r\n..\r", res.arg1().checkjstring());
        Assert.assertEquals(1, res.checkint(2));

        res = globals.load("return require('mime.core').dot(1, \"\\n.\\r\\nStuffing the message.\\r\\n.\\r\\n.\\r\\n\")").invoke();
        Assert.assertEquals("\n..\r\nStuffing the message.\r\n..\r\n..\r\n", res.arg1().checkjstring());
        Assert.assertEquals(2, res.checkint(2));
    }


    @Test
    public void testB64TV2() {
        String script = "t = {}\n";
        script+="for k,v in pairs(tv) do\n";
        script+="t[k] = require(\"mime.core\").b64(k)\n";
        script+="end\n";
        script+="return t\n";
        globals.set("tv", mapToTable(B64TV2));
        LuaTable table = globals.load(script).call().checktable();
        for (Map.Entry<String, String> e : B64TV2.entrySet()) {
            Assert.assertEquals(e.getKey(), e.getValue(), table.get(e.getKey()).checkjstring());
        }
    }

    @Test
    public void testB64TV1() {
        String script = "l = load or loadstring\n";
        script+="i = 0\n";
        script+="t={}\n";
        script+="while(i <= 255) do\n";
        script+="str = l(\"return '\\\\\"..i..\"'\")()\n";
        script+="t[i] = require(\"mime.core\").b64(str)\n";
        script+="i = i + 1\n";
        script+="end\n";
        script+="return t\n";


        LuaTable table = globals.load(script).call().checktable();
        for (int i = 0; i <= 255; i++) {
            Assert.assertEquals(B64TV.get(i), table.get(i).checkjstring());
        }
    }

    @Test
    public void testB64Filter() {
        Assert.assertEquals("QUJDRA==", globals.load("v = require('mime').encode('base64')\n return v('ABCD') .. v(nil)").call().checkjstring());
    }

    //////////////
    // DNS STUFF
    /////////////

    @Test
    public void testDNSToHostname() {
        String dns = globals.load("return require('socket').dns.tohostname('193.0.14.129')").call().checkjstring();
        Assert.assertEquals("k.root-servers.net", dns);
    }


    @Test
    public void testDNSToIp() {
        String dns = globals.load("return require('socket').dns.toip('k.root-servers.net')").call().checkjstring();
        Assert.assertEquals("193.0.14.129", dns);
    }

    @Test
    public void testDNSGetHostname() {
        String dns = globals.load("return require('socket').dns.gethostname()").call().checkjstring();
        Assert.assertNotNull("hostname is null!", dns);
        Assert.assertFalse("hostname is null!", dns.isEmpty());
    }


    //////////////
    // TCP STUFF
    /////////////

    @Test
    public void testTCPClientSend() throws Exception {
        Future<String> f = pollFirstSocket();

        String script = "";
        script+="socket = require('socket')\n";
        script+="master = socket.tcp()\n";
        script+="master:connect('127.0.0.1', " + AVAILABLE_PORT+ ")\n";
        script+="master:send('Hello World')\n";
        script+="master:close()\n";
        script+="return true\n";

        Assert.assertTrue(globals.load(script,"beep").call().checkboolean());
        Assert.assertEquals("Hello World", f.get());
    }

    @Test
    public void testTCPClientReceive() throws Exception {
        Future<Boolean> f = sendFirstSocket("Hello World");

        String script = "";
        script+="socket = require('socket')\n";
        script+="master = socket.tcp()\n";
        script+="master:connect('127.0.0.1', " + AVAILABLE_PORT+ ")\n";
        script+="msg = master:receive('*a')\n";
        script+="master:close()\n";
        script+="return msg\n";

        Assert.assertEquals("Hello World", globals.load(script, "recv test.lua").call().checkjstring());
        Assert.assertTrue(f.get());
    }

    @Test
    public void testTCPServer() throws Exception {
        String script = "";
        script+="socket = require('socket')\n";
        script+="master = socket.tcp()\n";
        script+="master:setoption('reuseaddr',true)\n";
        script+="master:bind('*', " + (AVAILABLE_PORT+1)+ ")\n";
        script+="spin:set(1)\n";
        script+="client = master:accept()\n";
        script+="client:send('Hello World')\n";
        script+="client:close()\n";
        script+="master:close()\n";

        final AtomicInteger spin = new AtomicInteger();
        globals.set("spin", CoerceJavaToLua.coerce(spin));

        Future f = runScript(script);

        long tsp = System.currentTimeMillis();
        while (spin.get() != 1) {
            if (f.isDone()) {
                f.get(1, TimeUnit.MILLISECONDS);
                Assert.fail("lua script stopped unexepectedly.");
            }

            if (System.currentTimeMillis()-tsp > 5000) {
                Assert.fail("Lua Script failed to bind socket.");
            }

            Thread.sleep(20);
        }

        Socket s = new Socket();
        s.connect(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), AVAILABLE_PORT+1));
        s.setSoTimeout(5000);
        String res = new String(Util.readAllBytesFromInputStream(s.getInputStream()), StandardCharsets.UTF_8);
        s.close();
        Assert.assertEquals("Hello World", res);
    }

    @Test
    public void testSelect() throws Exception {
        String script = "";
        script+="socket = require('socket')\n";
        script+="client1 = socket.tcp()\n";
        script+="client2 = socket.tcp()\n";
        script+="client1:connect('127.0.0.1', " + AVAILABLE_PORT+ ")\n";
        script+="client2:connect('127.0.0.1', " + AVAILABLE_PORT+ ")\n";
        script+="client1:settimeout(0)\n";
        script+="client2:settimeout(0)\n";
        script+="rcv = {}\n";
        script+="rcv[1] = client1\n";
        script+="rcv[2] = client2\n";
        script+="rcvt, sendt, err = socket.select(rcv, nil, 1)\n";
        script+="if err ~= 'timeout' then\n";
        script+="print(err)\n";
        script+="client1:send('N')\n";
        script+="client1:close()\n";
        script+="client2:close()\n";
        script+="return\n";
        script+="end\n";
        script+="client1:send('Y')\n";
        script+="rcvt, sendt, err = socket.select(rcv, nil, 5)\n";
        script+="if rcvt[client2] == nil or err ~= nil then\n";
        script+="print(err)\n";
        script+="client1:send('N')\n";
        script+="client1:close()\n";
        script+="client2:close()\n";
        script+="return\n";
        script+="end\n";
        script+="client1:send('Y')\n";
        script+="msg = client2:receive('*a')\n";
        script+="client1:close()\n";
        script+="client2:close()\n";
        script+="return msg\n";

        Future<Varargs> f = runScript(script);

        server.setSoTimeout(5000);
        Socket client1 = server.accept();
        Socket client2 = server.accept();
        Assert.assertEquals('Y', client1.getInputStream().read());
        Thread.sleep(1000);
        client2.getOutputStream().write("Hello World".getBytes(StandardCharsets.UTF_8));
        client2.getOutputStream().flush();
        client2.close();
        Assert.assertEquals('Y', client1.getInputStream().read());
        client1.close();
        Assert.assertEquals("Hello World", f.get(500000000, TimeUnit.MILLISECONDS).checkjstring(1));
    }

    private Future<Varargs> runScript(final String script) {
        return EX.submit(new Callable<Varargs>() {
            @Override
            public Varargs call() throws Exception{
                try {
                    return globals.load(script, "script").call();
                } catch (Exception exc) {
                    exc.printStackTrace();
                    throw exc;
                }
            }
        });
    }

    private Future<Boolean> sendFirstSocket(final String content) {
        return EX.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception{
                Socket sock = server.accept();
                sock.getOutputStream().write(content.getBytes(StandardCharsets.UTF_8));
                sock.getOutputStream().flush();
                sock.close();
                return Boolean.TRUE;
            }
        });
    }


    private Future<String> pollFirstSocket() {
        return EX.submit(new Callable<String>() {
            @Override
            public String call() throws Exception{
                Socket sock = server.accept();
                InputStream inputStream = sock.getInputStream();
                String s = new String(Util.readAllBytesFromInputStream(inputStream), StandardCharsets.UTF_8);
                sock.close();
                return s;
            }
        });
    };

}
