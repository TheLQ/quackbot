var help = "Attempts to deop a person. Admin only command. Syntax: ?deop <USER>";
var param = 1;
var admin = true;

function invoke(reqUser) {
	qb.deOp(channel,reqUser);
	Thread.sleep(500); //sleep for 500 ms
	var userObj = qb.getUser(channel,reqUser);
	if(userObj === null)
		qb.sendMsg(new BotMessage(msgInfo,reqUser+" does not exist"));
	else if(userObj.isOp()==false)
		qb.sendMsg(new BotMessage(msgInfo,reqUser+" is no longer an op"));
	else
		qb.sendMsg(new BotMessage(msgInfo,"Failed to deop "+reqUser+" (bot not an op?)"));
}