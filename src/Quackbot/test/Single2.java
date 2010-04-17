/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Quackbot.test;

/**
 *
 * @author admins
 */
public class Single2 {
     private static final Single2 INSTANCE = new Single2();
     public Single1 oneInst = Single1.getInstance();

     private Single2() {
	 System.out.println("Single2");
     }

     public static final Single2 getInstance() {
       return INSTANCE;
     }
     	
}
