var help = "Generic ban command. 1=Ban login. 2=Ban login and host. 3=Ban host subnet. Syntax: ?ban <option_num> <user> OR ?ban <mask> (latter assumed if command contains the @ symbol";
var admin = true;

function invoke() {

	var hostmask = "*!*@"+event.getHostname();
	qb.ban(event.channel,hostmask);
	qb.sendMsg(new BotMessage(event.channel,"Banned user "+event.sender+" with "+hostmask));
}