var help = "Locks the bot globaly. Syntax: ?lockBot"
var param = 0;
var admin = true;

function invoke() {
	qb.botLocked = true;
	qb.sendMsg(new BotMessage(msgInfo,"Bot has been locked globaly"));
}