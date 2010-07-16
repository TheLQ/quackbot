var help = "Unlocks the bot in channel. Syntax: ?unlockChannel"
var parameters = {
	optional:1
};
var admin = true;

function invoke(channel) {
	qb.chanLockList.remove(QuackUtils.pickBest(channel,event.getChannel()));
	qb.sendMsg(new BotMessage(event,"Bot has been unlocked for channel "+QuackUtils.pickBest(channel,event.getChannel())));
}