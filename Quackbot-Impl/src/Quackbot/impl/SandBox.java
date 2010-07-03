package Quackbot.impl;

import Quackbot.GUI;
import Quackbot.plugins.JavaPlugin;
import Quackbot.plugins.core.Help;

/**
 * Test class for various things. Not relevent to anything
 * @author LordQuackstar
 */
public class SandBox {
	public SandBox() {
		try {
			new JavaPlugin(Help.class);
			/**
			ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
			jsEngine.eval(new FileReader("plugins/testCase.js"));
			Bindings engineScope = jsEngine.getBindings(ScriptContext.ENGINE_SCOPE);

			//Transform JS array into Java array
			NativeArray narr = (NativeArray) engineScope.get("args");
			String[] array = new String[(int) narr.getLength()];
			for (Object o : narr.getIds()) {
				//For some reason can't cast the whole array to Integer[], but it works per element
				int index = (Integer) o;
				array[index] = (String) narr.get(index, null);
			}
			System.out.println(StringUtils.join(array));**/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new SandBox();
	}
}
