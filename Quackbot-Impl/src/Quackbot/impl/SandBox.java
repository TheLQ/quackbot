/**
 * @(#)SandBox.java
 *
 * Copyright Leon Blakey/Lord.Quackstar, 2009-2010
 *
 * This file is part of Quackbot
 *
 * Quackbot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quackbot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Quackbot.  If not, see <http://www.gnu.org/licenses/>.
 *
 * -javaagent:lib/jrebel.jar -noverify
 */
package Quackbot.impl;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class SandBox {

	public SandBox() {
		try {
			AccessController.doPrivileged(new PrivilegedAction() {

				public Object run() {
					try {
						Field field = ScriptEngineManager.class.getDeclaredField("DEBUG");
						field.setAccessible(true);
						field.setBoolean(null, true);

					} catch (Exception e) {
						e.printStackTrace();
					}
					return null;
				}
			});

			ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
			jsEngine.getBindings(ScriptContext.ENGINE_SCOPE).put("teh", "this");
			Compilable compilingEngine = (Compilable) jsEngine;
			File file = new File("plugins/testCase.js");
			if (!file.exists()) {
				System.err.println("Does not exist!");
			}
			CompiledScript cs = compilingEngine.compile(new FileReader(file));
			Invocable inv = (Invocable) cs.getEngine();
			inv.invokeFunction("invoke", new Object[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new SandBox();
	}
}
