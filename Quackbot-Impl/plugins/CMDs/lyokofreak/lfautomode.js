var help = "Strips all modes except t on lyokofreak channel. Syntax: ?lfautomode";
var param = 0;
var admin = true;

function invoke() {
	qb.setMode(event.channel,"-mnGUf");
	qb.sendMsg(new BotMessage(event,"Removed all modes except t"));
}