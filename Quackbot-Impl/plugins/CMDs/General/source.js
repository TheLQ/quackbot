var help = "Gives URL of souce code. Syntax: ?source";
var param = 0;

function invoke() {
	qb.sendMsg(new BotMessage(event,"Source code is located at http://quackbot.googlecode.com"));
}