package Quackbot.impl;

import java.util.ArrayList;
import org.apache.commons.lang.ArrayUtils;

/**
 * Test class for various things. Not relevent to anything
 * @author LordQuackstar
 */
public class SandBox {
	public SandBox() {
		int[] set = {1, 1, 1};
	}

	    static void recurse(int diceNumber, int[] values, final int MAX) {
        if (diceNumber == values.length) {
            System.out.println(java.util.Arrays.toString(values));
        } else {
            for (int v = 1; v <= MAX; v++) {
                values[diceNumber] = v;
                recurse(diceNumber + 1, values, MAX);
            }
        }
    }

	public static void main(String[] args) {
		//new SandBox();
		int[] into = new int[3];
		recurse(0, into, 4);
	}
}
