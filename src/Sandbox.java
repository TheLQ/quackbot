/**
 * @(#)Sandbox.java
 *
 * Test Class
 *
 * @author  Lord.Quackstar
 */
 
import java.lang.reflect.*;
import java.util.*;

public class Sandbox {
	TreeMap<String,Method> methodList;
	TreeMap<String,Object> cmds;
	
    public Sandbox() {
    	SandBox2 sb2 = new SandBox2();
	    Method[] allMethods = sb2.getClass().getDeclaredMethods();
	    //Filter out private methods and constructor
	    methodList = new TreeMap<String,Method>(String.CASE_INSENSITIVE_ORDER);
	    cmds = new TreeMap<String,Object>(String.CASE_INSENSITIVE_ORDER);
	    cmds.put("SandBox2",sb2);
	    for(Method method : allMethods) {
	    	int modifier = method.getModifiers();
	    	String name = method.getName().toLowerCase();
	    	if(modifier != Modifier.PRIVATE && modifier != Modifier.PROTECTED && !name.equals("onMessage")) {
	    		methodList.put(name,method);
	    		System.out.println("Name: "+name);
	    	}
	    }
	    
	    String command = "typeMsg";
	    try {
	    	//Does this method exist?
	    	if(!methodList.containsKey(command.toLowerCase())) {
	    		System.out.println("Command "+command+" dosen't exist");
	    		return;
	    	}
	    	Method reqMethod = methodList.get(command.toLowerCase());
	    	Object sb21 = (Object)cmds.get("SandBox2");
	    	//Class<?> sb2_class = sb21.getClass().getField("");
	    	
	    	reqMethod.invoke(sb21);
	    }
	    catch(Exception e) {
	    	e.printStackTrace();
	    }
	    
    }
    
    public static void main(String[] args) {
    	new Sandbox();
    }
}