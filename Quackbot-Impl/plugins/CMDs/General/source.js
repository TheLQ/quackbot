var help = "Gives URL of souce code. Syntax: ?source";

function onCommand() {
	qb.sendMsg(new BotMessage(event,"Source code is located at http://quackbot.googlecode.com"));
}