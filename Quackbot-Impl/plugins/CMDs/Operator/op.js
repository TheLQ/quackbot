var help = "Attempts to op a person. Admin only command. Syntax: ?op <USER>";
var param = 1;
var admin = true;

function invoke(reqUser) {
	qb.op(event.channel,reqUser);
	Thread.sleep(500); //sleep for 500 ms
	var userObj = qb.getUser(event.channel,reqUser);
	if(userObj === null)
		qb.sendMsg(new BotMessage(event,reqUser+" does not exist"));
	else if(userObj.isOp()==true)
		qb.sendMsg(new BotMessage(event,reqUser+" is now an op"));
	else
		qb.sendMsg(new BotMessage(event,"Failed to op "+reqUser+" (bot not an op?)"));
}