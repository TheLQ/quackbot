/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Quackbot;

/**
 *
 * @author admins
 */
public class InstanceTracker {
    private static Main mainInst = null;
   private static Controller ctrlInst = null;

    /**
     * @return the mainInst
     */
    public static Main getMainInst() {
	return mainInst;
    }

    /**
     * @param aMainInst the mainInst to set
     */
    public static void setMainInst(Main aMainInst) {
	mainInst = aMainInst;
    }

    /**
     * @return the ctrlInst
     */
    public static Controller getCtrlInst() {
	return ctrlInst;
    }

    /**
     * @param aCtrlInst the ctrlInst to set
     */
    public static void setCtrlInst(Controller aCtrlInst) {
	ctrlInst = aCtrlInst;
    }
}
