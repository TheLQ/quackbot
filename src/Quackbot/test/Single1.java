/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Quackbot.test;

/**
 *
 * @author admins
 */
public class Single1 {
     private static final Single1 INSTANCE = new Single1();

     private Single1() {
	 System.out.println("Single1");
     }

     public void runMe() {
	 System.out.println("runMe");
     }

     public static final Single1 getInstance() {
       return INSTANCE;
     }

     public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//Main.getInstance();
			}
		});
	}
}
