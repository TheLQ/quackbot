/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Quackbot.info;

import javax.script.Bindings;
import javax.script.ScriptContext;

/**
 *
 * @author admins
 */
public class JSCmdInfo {
    private String name,src,help;
    private boolean admin,ignore,listener,service,util,reqArg;
    private int params;
    private ScriptContext context;
    private Bindings scope;

    /**
     * @return the src
     */
    public String getSrc() {
	return src;
    }

    /**
     * @param src the src to set
     */
    public void setSrc(String src) {
	this.src = src;
    }

    /**
     * @return the help
     */
    public String getHelp() {
	return help;
    }

    /**
     * @param help the help to set
     */
    public void setHelp(String help) {
	this.help = help;
    }

    /**
     * @return the admin
     */
    public boolean isAdmin() {
	return admin;
    }

    /**
     * @param admin the admin to set
     */
    public void setAdmin(boolean admin) {
	this.admin = admin;
    }

    /**
     * @return the ignore
     */
    public boolean isIgnore() {
	return ignore;
    }

    /**
     * @param ignore the ignore to set
     */
    public void setIgnore(boolean ignore) {
	this.ignore = ignore;
    }

    /**
     * @return the listener
     */
    public boolean isListener() {
	return listener;
    }

    /**
     * @param listener the listener to set
     */
    public void setListener(boolean listener) {
	this.listener = listener;
    }

    /**
     * @return the service
     */
    public boolean isService() {
	return service;
    }

    /**
     * @param service the service to set
     */
    public void setService(boolean service) {
	this.service = service;
    }

    /**
     * @return the util
     */
    public boolean isUtil() {
	return util;
    }

    /**
     * @param util the util to set
     */
    public void setUtil(boolean util) {
	this.util = util;
    }

    /**
     * @return the reqArg
     */
    public boolean isReqArg() {
	return reqArg;
    }

    /**
     * @param reqArg the reqArg to set
     */
    public void setReqArg(boolean reqArg) {
	this.reqArg = reqArg;
    }

    /**
     * @return the context
     */
    public ScriptContext getContext() {
	return context;
    }

    /**
     * @param context the context to set
     */
    public void setContext(ScriptContext context) {
	this.context = context;
    }

    /**
     * @return the scope
     */
    public Bindings getScope() {
	return scope;
    }

    /**
     * @param scope the scope to set
     */
    public void setScope(Bindings scope) {
	this.scope = scope;
    }

    /**
     * @return the name
     */
    public String getName() {
	return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
	this.name = name;
    }

    /**
     * @return the params
     */
    public int getParams() {
	return params;
    }

    /**
     * @param params the params to set
     */
    public void setParams(int params) {
	this.params = params;
    }

}
