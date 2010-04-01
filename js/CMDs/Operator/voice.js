var help = "Attempts to voice a person. Admin only command. Syntax: ?voice <USER>";
var param = 1;
var admin = true;

function invoke(reqUser) {
	qb.voice(channel,reqUser);
	Thread.sleep(500); //sleep for 500 ms
	var userObj = qb.getUser(channel,reqUser);
	if(userObj === null)
		qb.sendMessage(channel,reqUser+" does not exist");
	else if(userObj.hasVoice()==true)
		qb.sendMessage(channel,reqUser+" has been voiced");
	else
		qb.sendMessage(channel,"Failed to voice "+reqUser+" (bot not an op?)");
}