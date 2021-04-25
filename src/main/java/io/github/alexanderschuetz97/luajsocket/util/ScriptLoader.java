package io.github.alexanderschuetz97.luajsocket.util;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility to load the precompiled lua scripts.
 * Those scripts are loaded from java bytecode unless a debugLib is present.
 * Presence of a debugLib forces recompilation using globals.compiler.
 */
public class ScriptLoader {

    private static ScriptLoader instance;

    private final Map<String, Class<? extends LuaFunction>> loaded = new HashMap<>();

    public static synchronized ScriptLoader instance() {
        if (instance == null) {
            instance = new ScriptLoader();
        }
        return instance;
    }

    public static void setInstance(ScriptLoader loader) {
        instance = loader;
    }

    public ScriptLoader() {
        init();
    }

    protected URLClassLoader getClassLoader() {
        URL url = ScriptLoader.class.getResource("/luasocket/compiled.zip");
        if (url == null) {
            throw new LuaError("resource /luasocket/compiled.zip is missing");
        }
        return new URLClassLoader(new URL[]{url}, ScriptLoader.class.getClassLoader());
    }

    protected void init() {
       URLClassLoader urlClassLoader = getClassLoader();
        try {
            loaded.put("ftp.lua", (Class<? extends LuaFunction>) Class.forName("ftp", true, urlClassLoader));
            loaded.put("headers.lua", (Class<? extends LuaFunction>) Class.forName("headers", true, urlClassLoader));
            loaded.put("http.lua", (Class<? extends LuaFunction>) Class.forName("http", true, urlClassLoader));
            loaded.put("ltn12.lua", (Class<? extends LuaFunction>) Class.forName("ltn12", true, urlClassLoader));
            loaded.put("mime.lua", (Class<? extends LuaFunction>) Class.forName("mime", true, urlClassLoader));
            loaded.put("smtp.lua", (Class<? extends LuaFunction>) Class.forName("smtp", true, urlClassLoader));
            loaded.put("socket.lua", (Class<? extends LuaFunction>) Class.forName("socket", true, urlClassLoader));
            loaded.put("tp.lua", (Class<? extends LuaFunction>) Class.forName("tp", true, urlClassLoader));
            loaded.put("url.lua", (Class<? extends LuaFunction>) Class.forName("url", true, urlClassLoader));
        } catch (Exception e) {
            throw new LuaError("Failed to load precompiled lua scripts from jar " + e.getClass().getName() + " " + e.getMessage());
        }
    }

    public LuaValue load(Globals globals, String aScript) {
        if (globals.debuglib != null) {
            InputStream in = ScriptLoader.class.getResourceAsStream("/luasocket/"+aScript);

            if (in == null) {
                throw new LuaError("resource /luasocket/" + aScript + " is missing");
            }

            String script = null;
            try {
                script = Util.inputStreamToString(in);
            } catch (IOException e) {
                throw new LuaError("Error reading resource /luasocket/" + aScript);
            }

            return globals.load(script, aScript, globals);
        }

        Class<? extends LuaFunction> clazz = loaded.get(aScript);
        if (clazz == null) {
            throw new LuaError("Script " + aScript + " not found");
        }

        try {
            LuaFunction function = clazz.newInstance();
            function.initupvalue1(globals);
            return function;
        } catch (Exception e) {
            throw new LuaError("Error loading " + aScript + " " + e.getClass().getName() + " " + e.getMessage());
        }
    }
}
