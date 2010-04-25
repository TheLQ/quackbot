var help = "Reloads all commands. Syntax: ?reload";
var param = 0;
var admin = true;

function invoke() {
	InstanceTracker.getController().reloadPlugins();
}