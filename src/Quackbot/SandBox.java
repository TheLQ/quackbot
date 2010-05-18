package Quackbot;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.AppenderSkeleton;

public class SandBox {
	public List<AppenderSkeleton> ctrlAppenders = new ArrayList<AppenderSkeleton>();

	public SandBox() {
		//ctrlAppenders.add(new ControlAppender());
	}

	public static void main(String[] args) {
		new SandBox();
	}

}
