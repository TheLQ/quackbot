/**
 * @(#)Sandbox.java
 *
 *
 * @author 
 * @version 1.00 2010/2/17
 */
 
package Quackbot;

import java.lang.reflect.*;
import java.util.*;
import static java.lang.System.out;
import static java.lang.System.err;


public class Sandbox {
	TreeMap<String,Method> methodList;
	
    public Sandbox() {
    	//Get all class information to get known functions
        Class<?> c = this.getClass();
	    Method[] allMethods = c.getDeclaredMethods();
	    //Filter out private methods and constructor
	    methodList = new TreeMap<String,Method>(String.CASE_INSENSITIVE_ORDER);
	    for(Method method : allMethods) {
	    	int modifier = method.getModifiers();
	    	String name = method.getName().toLowerCase();
	    	if(modifier != Modifier.PRIVATE && modifier != Modifier.PROTECTED && !name.equals("onMessage")) {
	    		methodList.put(name,method);
	    		System.out.println("Name: "+name);
	    	}
	    }
	    
	    String command = "THETiME";
	    try {
	    	//Does this method exist?
	    	if(!methodList.containsKey(command.toLowerCase())) {
	    		System.out.println("Command "+command+" dosen't exist");
	    		return;
	    	}
	    	Method reqMethod = methodList.get(command.toLowerCase());
	    	reqMethod.invoke(this);
	    }
	    catch(Exception e) {
	    	e.printStackTrace();
	    }
	    
    }
    
    public void tHeTimE() {
		System.out.println("The time is something");
    }
    
    public static void main(String[] args) {
    	new Sandbox();
    }
}