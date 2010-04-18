var help = "Unlocks the bot globaly. Syntax: ?unlockBot"
var param = 0;
var admin = true;

function invoke() {
	qb.botLocked = false;
	qb.sendMsg(new BotMessage(msgInfo,"Bot has been unlocked globaly"));
}