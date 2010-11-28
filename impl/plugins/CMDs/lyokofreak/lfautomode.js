var help = "Strips all modes except t and G on lyokofreak channel. Syntax: ?lfautomode";
var admin = true;

function invoke() {
	getBot().setMode(event.channel,"-mnUf");
	return "Removed all modes except t and G";
}