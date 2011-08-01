package org.quackbot.dao.hibernate;

import org.hibernate.criterion.Restrictions;
import org.quackbot.dao.ServerDAO;
import org.quackbot.dao.hibernate.model.ServerEntryHibernate;
import org.quackbot.dao.model.AdminEntry;
import org.quackbot.dao.model.ChannelEntry;

/**
 *
 * @author Leon Blakey <lord.quackstar at gmail.com>
 */
public class ServerDAOHibernate extends GenericHbDAO<ServerEntryHibernate> implements ServerDAO<ServerEntryHibernate> {
	@Override
	public ServerEntryHibernate delete(ServerEntryHibernate entity) {
		for (ChannelEntry channel : entity.getChannels())
			channel.setServer(entity);
		for (AdminEntry admin : entity.getAdmins())
			admin.getServers().remove(entity);
		getSession().delete(entity);

		if (true)
			return entity;

		//Delete all channels manually (clone the set so a ConcurrentModificationException isn't raised as channel deletes itself)
		//for(ChannelDAO channel : new HashSet<ChannelDAO>(channels))
		//	channel.delete();
		System.out.println("--- BEGIN DELETING USER MAPS ---");
		getSession().createQuery("delete from UserChannelHb where USER_ID in (from UserDAOHb where SERVER_ID = :server_id)").setLong("server_id", entity.getId()).executeUpdate();
		System.out.println("--- BEGIN DELETING USERS ---");
		getSession().createQuery("delete from UserDAOHb where SERVER_ID = :server_id").setLong("server_id", entity.getId()).executeUpdate();
		System.out.println("--- BEGIN DELETING CHANNELS ---");
		entity.getChannels().clear();
		System.out.println("--- BEGIN DELETING ADMINS ---");


		//session.createQuery("delete from UserChannelHb where USER_ID in (from UserDAOHb where SERVER_ID = :server_id)").setInteger("server_id", serverId).executeUpdate();
		//session.refresh(this);
		//channels.clear();

		//Remove all admins
		//for(Iterator<AdminDAO> itr = admins.iterator(); itr.hasNext();) {
		//	AdminDAO curAdmin = itr.next();
		//	curAdmin.getServers().remove(this);
		//	itr.remove();
		//}
		entity.getAdmins().clear();

		/*
		for (Iterator<AdminDAO> itr = admins.iterator(); itr.hasNext();) {
		AdminDAO curAdmin = itr.next();
		curAdmin.getServers().remove(this);
		itr.remove();
		
		if(curAdmin.getName().equals("globalAdmin")) {
		
		}
		ClassMetadata metadata = session.getSessionFactory().getClassMetadata(AdminDAOHb.class);
		System.out.println("Identifyier for admin " + curAdmin.getName() + ": " + metadata.getIdentifier(curAdmin, (SessionImplementor) session));
		}*/
		System.out.println("--- BEGIN DELETING SERVER ---");
		getSession().delete(entity);
		System.out.println("--- DELETE FINISHED ---");
		return entity;
	}

	@Override
	public ServerEntryHibernate findByAddress(String serverAddress) {
		return (ServerEntryHibernate) getSession().createCriteria(ServerEntryHibernate.class).add(Restrictions.eq("address", serverAddress)).uniqueResult();
	}

	@Override
	public ServerEntryHibernate create(String address) {
		ServerEntryHibernate server = new ServerEntryHibernate();
		server.setAddress(address);
		return server;
	}
}
