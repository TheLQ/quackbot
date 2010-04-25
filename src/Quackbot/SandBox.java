package Quackbot;

import java.lang.reflect.Field;

public class SandBox {

	public SandBox() {
		try {
			Class<?> clazz = new someClass().getClass();
			Field[] fields = clazz.getDeclaredFields();
			for(Field curField : fields)
				curField.setAccessible(true);
			System.out.println("Annon length: " + clazz.getField("stuff").toGenericString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class someClass {
		private int stuff;
		void things() {
		}
	}

	public static void main(String[] args) {
		new SandBox();
	}
}
