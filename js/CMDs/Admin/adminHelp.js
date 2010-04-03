var help = "Shows either all admin commands or help for a specific admin command. Syntax: ?adminHelp <OPTIONAL:command>";
var param = 1;
var ReqArg = true;

importClass(Packages.org.apache.commons.lang.StringUtils);
importClass(Packages.java.util.TreeSet);
importClass(Packages.java.util.TreeMap);

function invoke(command) {
	if(command=="null") {
		//User wants command list
		cmdList = new TreeSet();
		itr = qb.mainInst.cmds.entrySet().iterator();
		while(itr.hasNext()) {
			currentEntry = itr.next();
			if(currentEntry.getValue().get("admin") == true && currentEntry.getValue().get("ignore")==false)
				cmdList.add(currentEntry.getKey());
		}

		//Send to user
		qb.sendMessage(channel, sender + ": Possible admin only commands: "+StringUtils.join(cmdList.toArray(),", "));
	}
	else {
		if(!qb.methodExists(command))
			return;
		qb.sendMessage(channel, sender + ": "+qb.mainInst.cmds.get(command).get("help"));
	}
}