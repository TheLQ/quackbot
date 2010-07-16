var help = "Locks the bot in channel. Syntax: ?lockChannel"
var param = {optional:0};
var admin = true;

function invoke(channel) {
	qb.chanLockList.add(QuackUtils.pickBest(channel,event.getChannel()));
	qb.sendMsg(new BotMessage(event,"Bot has been locked for this channel"));
}