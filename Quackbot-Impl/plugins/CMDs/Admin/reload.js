var help = "Reloads all commands.  Syntax: ?reload";
var admin = true;

function onCommand() {
	Controller.instance.reloadPlugins();
	return "Plugins reloaded successuflly";
}