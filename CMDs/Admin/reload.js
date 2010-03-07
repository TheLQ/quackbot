var help = "Reloads all commands. Syntax: ?reload";
var param = 0;
var admin = true;

function invoke(newChan) {
	qb.mainInst.threadPool.execute(new loadCMDs(qb.mainInst));
}