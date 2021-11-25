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
package io.github.alexanderschuetz97.luajsocket.lib;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaClosure;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaThread;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.DebugLib;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.lang.reflect.Method;

/**
 * This class is a mobdebug.lua compatible DebugLib.
 *
 * This more closely resembles how the normal c based lua acts WITHIN a debug hook.
 *
 * This class solves this issue by just calling the stuff instead of luaj using reflection.
 * Not fast but for debugging speed does not matter that much anyways.
 *
 * To use this simply create {@link JsePlatform#standardGlobals()} and then call {@link Globals#load(LuaValue)} with
 * a new instance of this class. If you manually construct your globals then the requirements are identical to the
 * standard DebugLib from LuaJ.
 */
public class MobDebugCompatibleDebugLib extends DebugLib {


    protected static Method getCallStack;
    protected static Method onReturn;
    protected static Method onInstruction;
    protected static Method onCall1;
    protected static Method onCall2;

    /**
     * Unlucky but unless i "move" myself inside the luaj package this is the only way to do it.
     * All of this is package private.
     */
    protected static void initReflection() {
        try {
            getCallStack = DebugLib.class.getDeclaredMethod("callstack");
            getCallStack.setAccessible(true);

            onReturn = DebugLib.CallStack.class.getDeclaredMethod("onReturn");
            onReturn.setAccessible(true);

            onInstruction = DebugLib.CallStack.class.getDeclaredMethod("onInstruction", int.class, Varargs.class, int.class);
            onInstruction.setAccessible(true);

            onCall1 = DebugLib.CallStack.class.getDeclaredMethod("onCall", LuaFunction.class);
            onCall1.setAccessible(true);

            onCall2 = DebugLib.CallStack.class.getDeclaredMethod("onCall", LuaClosure.class, Varargs.class, LuaValue[].class);
            onCall2.setAccessible(true);
        } catch (Throwable e) {
            throw new LuaError(e);
        }

    }

    protected Globals theGlobals;

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        theGlobals = env.checkglobals();
        synchronized (MobDebugCompatibleDebugLib.class) {
            if (getCallStack == null) {
                initReflection();
            }
        }
        return super.call(modname, env);

    }

    @Override
    public void onCall(LuaFunction f) {
        LuaThread.State s = theGlobals.running.state;
        if (s.inhook) {
            try {
                CallStack stack = (CallStack) getCallStack.invoke(this);
                onCall1.invoke(stack, f);
            } catch (Exception e) {
                throw new LuaError(e);
            }
            return;
        }
        super.onCall(f);
    }

    @Override
    public void onCall(LuaClosure c, Varargs varargs, LuaValue[] luaValues) {
        LuaThread.State s = theGlobals.running.state;
        if (s.inhook) {
            try {
                CallStack stack = (CallStack) getCallStack.invoke(this);
                onCall2.invoke(stack, c,varargs,luaValues);
            } catch (Exception e) {
                throw new LuaError(e);
            }
            return;
        }
        super.onCall(c, varargs, luaValues);
    }

    @Override
    public void onInstruction(int pc, Varargs v, int top) {
        LuaThread.State s = theGlobals.running.state;
        if (s.inhook) {
            try {
                CallStack stack2 = (CallStack) getCallStack.invoke(this);
                onInstruction.invoke(stack2, pc, v, top);
            } catch (Exception e) {
                throw new LuaError(e);
            }
            return;
        }
        super.onInstruction(pc, v, top);
    }

    @Override
    public void onReturn() {
        LuaThread.State s = theGlobals.running.state;
        if (s.inhook) {
            try {
                CallStack stack = (CallStack) getCallStack.invoke(this);
                onReturn.invoke(stack);
            } catch (Exception e) {
                throw new LuaError(e);
            }
            return;
        }

        super.onReturn();
    }


}
