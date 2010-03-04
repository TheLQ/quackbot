/**
 * @(#)Sandbox.java
 *
 * Test Class
 *
 * @author  Lord.Quackstar
 */
 
import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import javax.swing.*;
import java.io.*;

public class Sandbox {
	
    public Sandbox() {
    	try {
    		SandBox2 sb2 = new SandBox2();
    		File file = new File("SandBox2.class");
    		if(!file.exists())
    			System.out.println("Does not exist!");
    			
			ClassLoader parentClassLoader = this.getClass().getClassLoader();
			while(JOptionPane.showConfirmDialog(null, "Recompile?") == JOptionPane.YES_OPTION) {
				SandBoxInter classInst = (SandBoxInter)new ClassReloader(parentClassLoader,file).loadClass("SandBox2").newInstance();
	    	}
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    
    /****Custom class loader to allow class reloading***/
	public class ClassReloader extends ClassLoader{
		File file = null;
		public ClassReloader(ClassLoader parent,File file) {
			super(parent);
			this.file = file;
		}
	
		public Class loadClass(String name) throws ClassNotFoundException {
			if(!name.equals("SandBox2")) {
				return super.loadClass(name) ;
			}
			
			System.out.println("Name: "+name);
			try {
				String url = "file:"+file.toString();
				URL myUrl = file.toURI().toURL();
				URLConnection connection = myUrl.openConnection();
				InputStream input = connection.getInputStream();
				ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				int data = input.read();
	
				while(data != -1){
					buffer.write(data);
					data = input.read();
				}
	
				input.close();
	
				byte[] classData = buffer.toByteArray();
	
				return defineClass(name,classData, 0, classData.length);
	
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace(); 
			}
	
			return null;
		}
	
	}
    
    public static void main(String[] args) {
    	new Sandbox();
    }
}