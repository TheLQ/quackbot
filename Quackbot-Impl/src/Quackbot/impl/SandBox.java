package Quackbot.impl;

import java.io.File;
import org.apache.commons.lang.StringUtils;

/**
 * Test class for various things. Not relevent to anything
 * @author LordQuackstar
 */
public class SandBox {
	public SandBox() {
		try {
			System.out.println("Before split");
			System.out.println("Name: "+new File("plugins").getName());
			System.out.println("After");
			//Get extension of file
			String[] extArr = StringUtils.split(new File("plugins").getName(), '.');
			System.out.println("After split");
			if (extArr.length < 2)
				return;
			String ext = extArr[1];
			System.out.println("Finsihed");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new SandBox();
	}
}
//-javaagent:lib/jrebel.jar -noverify
