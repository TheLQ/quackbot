package Quackbot.impl;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Test class for various things. Not relevent to anything
 * @author LordQuackstar
 */
public class SandBox {
	public SandBox() {
		ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
		Bindings engineScope = jsEngine.getBindings(ScriptContext.ENGINE_SCOPE);
		try {
			jsEngine.eval("var hello = 'HELLO THERE';");
			jsEngine.eval("var hello = null;");
			System.out.println(engineScope.get("hello")==null);
		} catch (ScriptException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new SandBox();
	}
}
//-javaagent:lib/jrebel.jar -noverify

