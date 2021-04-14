# luajsocket
Luajsocket is a port of the luasocket library to luaj. It allows for easy socket communication inside lua scripts running in a luaj vm.

## License
Luajsocket is released under the GNU General Public License Version 3. <br>
A copy of the GNU General Public License Version 3 can be found in the COPYING file.<br>

Luajsocket contains components sourced from the luasocket library. <br>
Those components are all contained within the folder "src/main/lua/luasocket".<br>
All files in the folder "src/main/lua/luasocket" are released under the LuaSocket 3.0 license.<br>
A copy of the LuaSocket 3.0 license can be found in the src/main/lua/luasocket/LICENSE file.<br>
For more information regarding luasocket see:<br>
https://github.com/diegonehab/luasocket/

## Dependencies
* Java 7 or newer
* luaj version 3.0.1

## Usage
In Java:
````
Globals globals = JsePlatform.standardGlobals();
globals.load(new LuaJSocketLib());
//.... (Standart LuaJ from this point)
globals.load(new InputStreamReader(new FileInputStream("test.lua")), "test.lua").call();
````
In test.lua:
````
local socket = require('socket')
-- Using socket.gettime() as an example here to see if library was loaded.
print("Seconds since Jan 1st 1970 ", tostring(socket.gettime()))
````
#### How to compile luajsocket
It is recommended to uncomment the maven-gpg-plugin section from the pom.xml
before building. It is only inside the pom.xml because it needs to be there
for uploading to maven central. 

#### Debugging Luaj using mobdebug.lua
Using luajsocket you can finally debug luaj code using any lua ide that supports "mobdebug.lua"<br>
Similar libraries will probably work too. "mobdebug.lua" is known to be working.<br>
Unfortunately luaj's DebugLib behaves different from the standard lua debug library.<br>
DebugLib does not track the stack of the luavm while inside the hook function.<br>
"mobdebug.lua" requires this however. <br>
To debug using mobdebug.lua a different DebugLib has to be used.<br><br>
In Java:
````
//Do not use the debug globals! They will not work!
Globals globals = JsePlatform.standardGlobals();

//This class comes with luajsocket because 
//it only required very minor adjustments to make it work
globals.load(new MobDebugCompatibleDebugLib()); 

globals.load(new LuaJSocketLib());
//.... (Standart LuaJ from this point)
globals.load(new InputStreamReader(new FileInputStream("test.lua")), "test.lua").call();
````
In test.lua:
````
-- Subsitute this with your path to mobdebug.lua
-- The one from github or ZeroBrane Studio works just fine
mobdebug = dofile("mobdebug.lua")

-- this call will try to connect to a mobdebug debugging server on the local machine
-- use an IDE like ZeroBrane Studio to create this server. 
-- You may also specify IP+port for remote debugging 
mobdebug.start()

local x = 1
-- set a breakpoint before this print in the IDE
print(x) 
````
#####Further info on using mobdebug to debug luaj
Using the EmmyLua Debugger in IntelliJ will work,<br>
however EmmyLua will crash when you use the evaluate feature with a syntax error.<br>
This is because luaj creates a multi line error string that mobdebug.lua will send <br>
over the network. EmmyLua will not expect a multi line string. You will have <br>
to patch the error message in mobdebug.lua before it is sent to always be <br>
a static message like "Error happened" then it won't crash the debugger. <br>
ZeroBrane Studio does not suffer from this issue and thus does not require a patch.<br>

Additionally, the debugger will require you to give your lua "chunks" proper names.<br> 
The chunk name can be set by the second parameter to globals.load.<br>
ZeroBrane Studio requires the name to be the file name that is open inside of it.<br>
Ex: literally "test.lua"<br>
EmmyLua requires the full absolute path to the file to be the chunk name.<br> 
If it does not find the file by its absolute path it will look for a relative path starting in the user home directory.<br>
The only way to be "compatible" with both debuggers at the same time<br> is to place (or copy) your lua files to your home directory.
