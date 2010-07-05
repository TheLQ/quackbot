package Quackbot.impl;

import java.io.FileReader;
import java.util.Map.Entry;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import sun.org.mozilla.javascript.internal.NativeArray;
import sun.org.mozilla.javascript.internal.NativeObject;

/**
 * Test class for various things. Not relevent to anything
 * @author LordQuackstar
 */
public class SandBox {
	public SandBox() {
		try {
			ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
			jsEngine.eval(new FileReader("plugins/testCase.js"));
			Bindings engineScope = jsEngine.getBindings(ScriptContext.ENGINE_SCOPE);

			for(Entry<String,Object> entry : engineScope.entrySet()) {
				System.out.println(entry.toString()+"           "+entry.getValue().getClass().getName());
			}

			NativeObject args = (NativeObject)engineScope.get("args");
			System.out.println("Whoo"+((NativeArray)args.get("optional", null)) );


			//Transform JS array into Java array
			/*Object invFuncSuper = engineScope.get("invoke");
			if(invFuncSuper == null)
				throw new QuackbotException("No invoke function defined!");
			if(!(invFuncSuper instanceof NativeFunction))
				throw new QuackbotException("invoke name overwritten by variable. Must rename invoke variable to something else");

			NativeFunction function = (NativeFunction)invFuncSuper;
			System.out.println(function.getLength());
			Invocable inv = (Invocable) jsEngine;
			inv.invokeFunction("invoke", new Object[]{});
			
			NativeArray narr = (NativeArray) engineScope.get("args");
			String[] array = new String[(int) narr.getLength()];
			for (Object o : narr.getIds()) {
				//For some reason can't cast the whole array to Integer[], but it works per element
				int index = (Integer) o;
				array[index] = (String) narr.get(index, null);
			}
			System.out.println(StringUtils.join(array));*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new SandBox();
	}
}
//-javaagent\:lib/jrebel.jar -noverify