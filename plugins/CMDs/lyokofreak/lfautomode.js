var help = "Strips all modes except t on lyokofreak channel. Syntax: ?lfautomode";
var param = 0;
var admin = true;

function invoke() {
	qb.setMode(channel,"-mnGUf");
	qb.sendMessage(new BotMessage(channel,"Removed all modes except t"));
}