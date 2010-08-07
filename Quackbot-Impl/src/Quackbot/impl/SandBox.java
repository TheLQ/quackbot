/*
 * Copyright (C) 2009-2010 Leon Blakey
 *
 * Quackedbot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quackedbot  is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package Quackbot.impl;

import Quackbot.BaseCommand;
import Quackbot.err.QuackbotException;
import Quackbot.hook.HookManager;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.apache.commons.lang.StringUtils;

public class SandBox {
	public SandBox() {

		try {

			new FileReader(new File(getClass().getResource("JSPluginResources/QuackUtils.js").toURI()));
			final JTextPane textPane = new JTextPane();
			textPane.setEditable(false);
			textPane.setAlignmentX(Component.CENTER_ALIGNMENT);
			final JScrollPane contentPane = new JScrollPane(textPane);
			contentPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			contentPane.setAlignmentX(Component.RIGHT_ALIGNMENT);
			contentPane.setBorder(BorderFactory.createTitledBorder("Bots"));
			final JScrollBar scroll = contentPane.getVerticalScrollBar();
			/*scroll.addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
			System.out.println("Value: " + scroll.getValue() + " | Visible: " + scroll.getVisibleAmount() + " | Maximum: " + scroll.getMaximum() + " | Combined: " + (scroll.getValue() + scroll.getVisibleAmount()));
			}
			});*/
			JFrame frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setTitle("Quackbot GUI Control Panel");
			frame.setMinimumSize(new Dimension(400, 200));
			frame.add(contentPane); //add to JFrame
			frame.setVisible(true); //make JFrame visible

			System.out.println("BEG: " + (scroll.getValue() + scroll.getVisibleAmount()) + " | " + scroll.getMaximum() + " | " + scroll.getValue() + " | " + (scroll.getVisibleAmount() == scroll.getMaximum()));

			new Thread() {
				@Override
				public void run() {
					while (true)
						try {
							Thread.sleep(1000);
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									try {
										JScrollBar scrollBar = scroll;
										boolean preCheck = ((scrollBar.getVisibleAmount() != scrollBar.getMaximum()) && (scrollBar.getValue() + scrollBar.getVisibleAmount() == scrollBar.getMaximum()));
										String text = "FAGAHSIDFNJASDKFJSD\n";
										System.out.println("Value: " + scroll.getValue()
												+ " | Visible: " + scrollBar.getVisibleAmount()
												+ " | Maximum: " + scrollBar.getMaximum()
												+ " | Combined: " + (scrollBar.getValue() + scrollBar.getVisibleAmount())
												+ " | Vis!=Max : " + (scrollBar.getVisibleAmount() != scrollBar.getMaximum())
												+ " | Comb=Max: " + (scrollBar.getValue() + scrollBar.getVisibleAmount() == scrollBar.getMaximum())
												+ " | Eval: " + preCheck);
										StyledDocument doc = textPane.getStyledDocument();
										doc.insertString(doc.getLength(), text, doc.getStyle(""));
										if (!preCheck)
											textPane.setCaretPosition(doc.getLength());
									} catch (BadLocationException ex) {
										ex.printStackTrace();
									}
								}
							});

						} catch (Exception e) {
							e.printStackTrace();
						}
				}
			}.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new SandBox();
	}

	public void oldStorage() {
		try {
			final ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
			final Bindings scope = jsEngine.getBindings(ScriptContext.ENGINE_SCOPE);
			final Invocable inv = (Invocable) jsEngine;

			jsEngine.eval(new FileReader("plugins/Quackutils.js"));
			jsEngine.eval(new FileReader("plugins/testCase.js"));
			jsEngine.eval(new FileReader("plugins/JSPlugin.js"));
			if (jsEngine.get("onCommand") == null) {
				System.out.println("Decalring onCommand");
				jsEngine.eval("function onCommand() {return null;}");
			}


			BaseCommand cmd = inv.getInterface(BaseCommand.class);
			cmd.setup("JSPlugin", null, true, true, new File("stuff"), 0, 0);
			System.out.println("Required: " + cmd.getRequiredParams());
			System.out.println("Optional: " + cmd.getOptionalParams());


		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public BaseCommand load(File file) throws Exception {
		String name = StringUtils.split(file.getName(), ".")[0];
		System.out.println("New JavaScript Plugin: " + name);

		//Make an Engine and a context to use for this plugin
		ScriptEngine jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
		jsEngine.eval(new FileReader("plugins/QuackUtils.js"));
		jsEngine.eval(new FileReader(file));

		//Should we just ignore this?
		if (castToBoolean(jsEngine.get("ignore"))) {
			System.out.println("Ignore variable set, skipping");
			return null;
		}

		//Add the QuackUtils js utility class
		//Object quackUtils = scope.get("QuackUtils");

		//Is this a hook?
		for (String curFunction : jsEngine.getBindings(ScriptContext.ENGINE_SCOPE).keySet())
			if (HookManager.getNames().contains(curFunction))
				//It contains a hook method, assume that the whole thing is a hook
				//HookManager.addPluginHook(((Invocable) jsEngine).getInterface(BaseHook.class));
				//return;
				throw new QuackbotException("Hooks not supported");

		//Must be a Command
		jsEngine = new ScriptEngineManager().getEngineByName("JavaScript");
		jsEngine.eval(new FileReader("plugins/QuackUtils.js"));
		jsEngine.eval(new FileReader("plugins/JSPlugin.js"));
		jsEngine.eval(new FileReader(file));
		if (jsEngine.get("onCommand") == null) {
			System.out.println("Decalring onCommand");
			jsEngine.eval("function onCommand() {return null;}");
		}
		BaseCommand cmd = JSPluginProxy.newInstance(((Invocable) jsEngine).getInterface(BaseCommand.class), jsEngine);
		cmd.setup(name, null, true, true, file, 0, 0);
		return cmd;
	}

	public static boolean castToBoolean(Object obj) {
		if (obj == null || !(obj instanceof Boolean))
			return false;
		return (Boolean) obj;
	}

	public static class JSPluginProxy implements InvocationHandler {
		BaseCommand obj;
		ScriptEngine jsEngine;

		public JSPluginProxy(BaseCommand obj, ScriptEngine jsEngine) {
			this.obj = obj;
			this.jsEngine = jsEngine;
		}

		public static BaseCommand newInstance(BaseCommand obj, ScriptEngine jsEngine) {
			return (BaseCommand) Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(), new JSPluginProxy(obj, jsEngine));
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Object returned = null;
			try {
				if (method.getName().equalsIgnoreCase("onCommand"))
					//Throw away calls since onCommand is called indirectly
					return null;
				if (method.getName().equalsIgnoreCase("onCommandPM"))
					obj.getBot().sendMessage((String) args[0], (String) ((Invocable) jsEngine).invokeFunction("onCommand", (Object[]) args[3]));
				if (method.getName().equalsIgnoreCase("onCommandChannel"))
					obj.getBot().sendMessage((String) args[0], (String) args[1], (String) ((Invocable) jsEngine).invokeFunction("onCommand", (Object[]) args[4]));
				returned = method.invoke(obj, args);
			} catch (InvocationTargetException e) {
				System.out.println("Exception");
				//Unwrap several times
				throw e.getCause().getCause().getCause();
			}
			return returned;
		}
	}
}
//-javaagent:lib/jrebel.jar -noverify

