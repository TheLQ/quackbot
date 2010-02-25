/**
 * @(#)Quackbot.java
 *
 * Quackbot application
 *
 * @author 
 * @version 1.00 2010/2/16
 */
 
import org.jibble.pircbot.*;

public class Quackbot extends PircBot {
    
    public Quackbot() {
    	System.out.println("Whooo, 	quackbot!!!");
        setName("Quackbot");
    }
    
    public void onMessage(String channel, String sender, String login, String hostname, String message) {
        if (message.equalsIgnoreCase("time")) {
            String time = new java.util.Date().toString();
            sendMessage(channel, sender + ": The time is now " + time);
            System.out.println("Me bot!!");
        }
    }
}