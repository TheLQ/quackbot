var help = "Unlocks the bot globaly. Syntax: ?unlockBot"
var admin = true;

function invoke() {
	qb.botLocked = false;
	qb.sendMsg(new BotMessage(event,"Bot has been unlocked globaly"));
}