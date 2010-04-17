/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Quackbot.info.test_jp;

/**
 *
 * @author lordquackstar
 */
/**
 * Copyright (C) 2006 - present Software Sensation Inc.
 * All Rights Reserved.
 *
 * This file is part of jPersist.
 *
 * jPersist is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the accompanying license
 * for more details.
 *
 * You should have received a copy of the license along with jPersist; if not,
 * go to http://www.softwaresensation.com and download the latest version.
 */
import Quackbot.info.Admin;
import Quackbot.info.Channel;
import Quackbot.info.Server;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import jpersist.DatabaseManager;
import jpersist.Entity;
import jpersist.JPersistException;
import jpersist.PersistentObject;
import jpersist.annotations.UpdateNullValues;

/* uncomment for DBCP (Apache Commons Connection Pooling) - Need commons-pool and commons-dbcp
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
 */
public class DatabaseExample {

    public DatabaseExample(DatabaseManager dbm) throws JPersistException {
	int tryMe = 0;

	if (tryMe == 0) {
	    dbm.executeUpdate("delete from server");
	    Server newServ = new Server("irc.freenode.net", "8000", null);
	    newServ.getChannels().add(new Channel("#quackbot"));
	    newServ.getAdmins().add(new Admin("LordQuackstar"));
	    newServ.save(dbm);

	    Collection<Channel> c = dbm.loadObjects(new ArrayList<Channel>(), new Channel(0), true);
	    System.out.println("C size: "+c.size());
	    for (Channel curServer : c) {
		    System.out.println(curServer);
		}
	    /*Collection<Server> c = dbm.loadObjects(new ArrayList<Server>(), Server.class, true);
	    System.out.println("C size: "+c.size());
	    for (Server curServer : c) {
		dbm.loadAssociations(curServer);
		List<Channel> contacts = curServer.getChannels();
		System.out.println("Contact num: "+contacts.size());
		for (Channel curContact : contacts) {
		    System.out.println(curContact);
		}
	    }
	    */
	} else if (tryMe == 1) {
	    // Clean out contacts
	    dbm.executeUpdate("delete from contacts");

	    // Inserting contact with associations
	    Contact contact = new Contact("deisenhower", "mypasswd5", "Dwight", "Eisenhower", "United States", "deisenhower@unitedstates.gov");

	    contact.getSupport().add(new Support("Request", "New", "no phone", "deisenhower@unitedstates.gov", "Can I have my bust on a dollar, please."));
	    contact.getSupport().add(new Support("Response", "Pending", "no phone", "deisenhower@unitedstates.gov", "Yes, but you may have to share it."));
	    contact.getSupport().add(new Support("Request", "New", "no phone", "deisenhower@unitedstates.gov", "Share it with who?"));

	    contact.getOrders().add(new Order("Dwight D. Eisenhower Dollar", new Integer(100), new Double(1.00), "unverified"));
	    contact.getOrders().add(new Order("Susan B. Anthony Dollar", new Integer(100), new Double(1.00), "unverified"));

	    // Saving within an automatic transaction (covers all relationships)
	    contact.save(dbm);

	    // Add an associated record and update
	    contact.getSupport().add(new Support("Response", "Closed", "no phone", "deisenhower@unitedstates.gov", "You'll have to share with Susan Anthony."));
	    contact.save(dbm);

	    Collection<Contact> c = dbm.loadObjects(new ArrayList<Contact>(), Contact.class, true);
	    for (Contact curServer : c) {
		//dbm.loadAssociations(curServer);
		List<Support> contacts = curServer.getSupport();
		for (Support curContact : contacts) {
		    System.out.println(curContact);
		}
	    }
	}
    }

    public static void main(String[] args) throws JPersistException {
	DatabaseManager dbm = null;

	try {
	    DatabaseManager.setLogLevel(Level.OFF);
	    dbm = new DatabaseManager("quackbot", 10, "com.mysql.jdbc.Driver", "jdbc:mysql://localhost/quackbot", null, null, "root", null);
	    new DatabaseExample(dbm);
	} finally {
	    // also closes any open jpersist.Database
	    dbm.close();
	}
    }

    @UpdateNullValues
    public static class Contact extends PersistentObject {

	private static final long serialVersionUID = 100L;
	private String contactId, password, firstName, lastName, companyName, email;
	private List<Support> support = new ArrayList<Support>();
	private List<Order> orders = new ArrayList<Order>();

	public Contact() {
	}

	public Contact(String contactId) {
	    this.contactId = contactId;
	}

	public Contact(String contactId, String password, String firstName,
		String lastName, String companyName, String email) {
	    this.contactId = contactId;
	    this.password = password;
	    this.firstName = firstName;
	    this.lastName = lastName;
	    this.companyName = companyName;
	    this.email = email;
	}

	public String getContactId() {
	    return contactId;
	}

	public void setContactId(String id) {
	    contactId = id;
	}

	public String getPassword() {
	    return password;
	}

	public void setPassword(String passwd) {
	    password = passwd;
	}

	public String getFirstName() {
	    return firstName;
	}

	public void setFirstName(String fName) {
	    firstName = fName;
	}

	public String getLastName() {
	    return lastName;
	}

	public void setLastName(String lName) {
	    lastName = lName;
	}

	public String getCompanyName() {
	    return companyName;
	}

	public void setCompanyName(String name) {
	    companyName = name;
	}

	public String getEmail() {
	    return email;
	}

	public void setEmail(String email) {
	    this.email = email;
	}

	// Associations
	public List<Support> getDbAssociation(Support c) {
	    System.out.println("Called get DB association - sup");
	    return support;
	}

	public void setDbAssociation(Support c, List<Support> s) {
	    System.out.println("Called set DB association - sup");
	    support = s;
	}

	public List<Order> getDbAssociation(Order c) {
	    System.out.println("Called get DB association - order");
	    return orders;
	}

	public void setDbAssociation(Order c, List<Order> o) {
	    System.out.println("Called set DB association - order");
	    orders = o;
	}

	// association convenience (is optional)
	public List<Support> getSupport() {
	    return support;
	}

	public void setSupport(List<Support> support) {
	    this.support = support;
	}

	public List<Order> getOrders() {
	    return orders;
	}

	public void setOrders(List<Order> orders) {
	    this.orders = orders;
	}

	public String toString() {
	    String returnString = contactId + ", " + firstName + ", " + lastName
		    + ", " + companyName + ", " + email + "\n";

	    if (support != null) {
		for (Support s : support) {
		    returnString += s + "\n";
		}
	    }

	    if (orders != null) {
		for (Order o : orders) {
		    returnString += o + "\n";
		}
	    }

	    return returnString;
	}
    }

    public static class Order extends Entity // can optionally extend Entity for esthetics
    {

	private static final long serialVersionUID = 100L;
	private Long orderId;
	private Integer quantity;
	private Double price;
	private String contactId, product, status;

	public Order() {
	}

	public Order(String product, Integer quantity, Double price, String status) {
	    this.product = product;
	    this.quantity = quantity;
	    this.price = price;
	    this.status = status;
	}

	public Order(String contactId, String product, Integer quantity, Double price, String status) {
	    this.contactId = contactId;
	    this.product = product;
	    this.quantity = quantity;
	    this.price = price;
	    this.status = status;
	}

	public Long getOrderId() {
	    return orderId;
	}

	public void setOrderId(Long orderId) {
	    this.orderId = orderId;
	}

	public String getContactId() {
	    return contactId;
	}

	public void setContactId(String contactId) {
	    this.contactId = contactId;
	}

	public String getProduct() {
	    return product;
	}

	public void setProduct(String product) {
	    this.product = product;
	}

	public Integer getQuantity() {
	    return quantity;
	}

	public void setQuantity(Integer quantity) {
	    this.quantity = quantity;
	}

	public Double getPrice() {
	    return price;
	}

	public void setPrice(Double price) {
	    this.price = price;
	}

	public String getStatus() {
	    return status;
	}

	public void setStatus(String status) {
	    this.status = status;
	}

	public String toString() {
	    return orderId + ", " + contactId + ", "
		    + quantity + ", " + price + ", "
		    + product + ", " + status;
	}
    }

    public static class Support extends PersistentObject {

	private static final long serialVersionUID = 100L;
	private Long supportId;
	private String contactId, code, status, phone, email, request;

	public Support() {
	}

	public Support(String code, String status, String phone, String email, String request) {
	    this.code = code;
	    this.status = status;
	    this.phone = phone;
	    this.email = email;
	    this.request = request;
	}

	public Support(String contactId, String code, String status,
		String phone, String email, String request) {
	    this.contactId = contactId;
	    this.code = code;
	    this.status = status;
	    this.phone = phone;
	    this.email = email;
	    this.request = request;
	}

	public Long getSupportId() {
	    return supportId;
	}

	public void setSupportId(Long id) {
	    supportId = id;
	}

	public String getContactId() {
	    return contactId;
	}

	public void setContactId(String id) {
	    contactId = id;
	}

	public String getCode() {
	    return code;
	}

	public void setCode(String code) {
	    this.code = code;
	}

	public String getStatus() {
	    return status;
	}

	public void setStatus(String status) {
	    this.status = status;
	}

	public String getPhone() {
	    return phone;
	}

	public void setPhone(String phone) {
	    this.phone = phone;
	}

	public String getEmail() {
	    return email;
	}

	public void setEmail(String email) {
	    this.email = email;
	}

	public String getRequest() {
	    return request;
	}

	public void setRequest(String request) {
	    this.request = request;
	}

	public String toString() {
	    return supportId + ", " + contactId + ", " + code + ","
		    + status + ", " + phone + ", "
		    + email + ", " + request;
	}
    }
}

