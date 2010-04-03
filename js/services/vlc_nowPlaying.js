/* 
 * Reports what VLC is currently playing
 */

importClass(java.lang.Thread);

var service = true;

//Start checking loop in another thread
function invoke() {
	println("Initalizing vlc update");
	var previous = "";
	while(1) {
		try{
			Thread.sleep(5000);
			println("Checking");
			var current =  webTalk("http://localhost:8082/current.html");
			if(current == null) {
				println("Unable to connect to VLC, reload CMDs to retry");
				return;
			}
			if(current != previous)
				ctrl.sendGlobalMessage(current);
			else
				println("Nothing to do");
			previous = current;
		}
		catch(err) {
			println("ERROR "+err);
			if(err.toString().search("InterruptedException") != -1) {
				println("VLC thread interrupted");
				return;
			}
		}
	}
}