var help = "Generic ban command. 1=Ban login. 2=Ban login and host. 3=Ban host subnet. Syntax: ?ban <option_num> <user> OR ?ban <mask> (latter assumed if command contains the @ symbol";
var admin = true;

function invoke() {

	var hostmask = "*!*@"+msgInfo.getHostname();
	qb.ban(msgInfo.channel,hostmask);
	qb.sendMsg(new BotMessage(msgInfo.channel,"Banned user "+msgInfo.sender+" with "+hostmask));
}