/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Quackbot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

/**
 * /**
 * This is the general parameter configuration class that all plugin types need
 * to use for their implmentations.
 *
 * @author LordQuackstar
 * @param <T> Type of object that refrences to the actual field or variable in the
 *            implmentation
 */
public abstract class ParameterConfig<T> {
	protected int requiredCount = 0;
	protected int optionalCount = 0;
	protected List<T> requiredObjects = new ArrayList<T>();
	protected List<T> optionalObjects = new ArrayList<T>();

	/**
	 * With the given object, set its value to the argument
	 * @param args
	 */
	public abstract void fillByObject(T object, String argument) throws Exception;

	public void setOptionalCount(int count) {
		optionalCount = count;
	}

	public void setRequiredCount(int count) {
		requiredCount = count;
	}

	public void addOptionalObject(T object) {
		optionalObjects.add(object);
		optionalCount++;
	}

	public void addOptionalObjects(T[] object) {
		optionalObjects.addAll(Arrays.asList(object));
		optionalCount = optionalCount + object.length;
	}

	public void addRequiredObject(T object) {
		requiredObjects.add(object);
		requiredCount++;
	}

	public void addRequiredObjects(T[] object) {
		requiredObjects.addAll(Arrays.asList(object));
		requiredCount = requiredCount + object.length;
	}

	public void fillParameters(String[] args) throws Exception {
		List<T> mergedObjs = new ArrayList<T>(requiredObjects);
		mergedObjs.addAll(optionalObjects);
		if (mergedObjs.isEmpty() || ArrayUtils.isEmpty(args))
			return;
		for (int i = 0; i <= args.length-1; i++) 
			fillByObject(mergedObjs.get(i), args[i]);
	}

	public String toString() {
		String optionalRep = StringUtils.join(optionalObjects.toArray(), ", ");
		String requiredRep = StringUtils.join(requiredObjects.toArray(), ", ");
		return "RequiredCount=" + requiredCount + ", OptionalCount=" + optionalCount + ", RequiredObjects=[" + requiredRep + "], optionalObjects=[" + optionalRep + "]";
	}
}
