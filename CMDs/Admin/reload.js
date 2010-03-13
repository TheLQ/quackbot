var help = "Reloads all commands. Syntax: ?reload";
var param = 0;
var admin = true;

importPackage(Packages.Quackbot);

function invoke(newChan) {
	qb.mainInst.reloadCMDs();
}