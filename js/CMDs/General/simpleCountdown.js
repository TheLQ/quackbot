var help = "Starts countdown of specified length. Syntax: ?simpleCountdown <seconds>";
var param = 1;


var futureMs = 0;
var msRemain = 0;
var closestMin = 0;

importClass(java.lang.System);

function invoke(seconds) {
	futureMs = System.currentTimeMillis()+(seconds*1000);
	try{
		recalculate();
		while(msRemain > 60000) {
			qb.sendMessage(channel,timeRemaining(futureMs)+" remaining! (min countdows)");
			if(closestMin < 900)
				continue;
			qb.log("sleeping for "+closestMin+" | msremain: "+msRemain)
			Thread.sleep(closestMin)
			recalculate();
		}

		//Now at 1 min
		qb.sendMessage(channel,"1 minuite remaining! (hard coded)");

		//Wait for 30 sec
		Thread.sleep(30000)
		qb.sendMessage(channel,"30 seconds remaining! (hard coded)");

		//Wait for 20 sec
		Thread.sleep(20000)
		qb.sendMessage(channel,"20 seconds remaining! (hard coded)");

		//Wait for 10 sec
		Thread.sleep(30000)
		qb.sendMessage(channel,"10 seconds remaining! (hard coded)");

		//Wait for 5 sec
		Thread.sleep(5000)
		for(var i=5;i>0;i--) {
			qb.sendMessage(channel,i+" seconds remaining! (hard coded)");
			Thread.sleep(1000);
		}

		qb.sendMessage(channel,"Whoo, end!!");
	}
	catch(err) {
		println("ERROR "+err);
		if(err.toString().search("InterruptedException") != -1) {
			println("Countdown thread interrupted");
			return;
		}
	}
}

function recalculate() {
	msRemain = futureMs-System.currentTimeMillis();
	closestMin = msRemain % 60000;
}