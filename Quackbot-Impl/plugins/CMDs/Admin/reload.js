var help = "Reloads all commands. Syntax: ?reload";
var admin = true;

function invoke() {
	Controller.instance.reloadPlugins();
}