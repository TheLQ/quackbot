var help = "Unlocks the bot globaly. Syntax: ?unlockBot"
var param = 0;
var admin = true;

function invoke() {
	qb.botLocked = false;
	qb.sendMessage(channel,"Bot has been unlocked globaly");
}