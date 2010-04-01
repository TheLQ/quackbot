/* 
 * Reports what VLC is currently playing
 */

importClass(java.lang.Thread);

var service = true;
var param = 0; //Prevent error with cmd loading

//Start checking loop in another thread
ctrl.threadPool_js.execute(new java.lang.Runnable() {
	run: function() {
		return; //temporarily disable service
		println("Initalizing vlc update");
		var previous = "";
		while(1) {
			try{
				Thread.sleep(5000);
				println("Checking");
				var netPkgs = new JavaImporter(java.io,java.net);
				with (netPkgs) {
					var url = "http://localhost:8082/current.html";
					println("Visiting url: "+url);

					// Get the response
					var rd = new BufferedReader(new InputStreamReader(new URL(url).openConnection().getInputStream()));
					var allLine = "";
					var line = "";
					while ((line = rd.readLine()) != null)
						allLine = allLine+line;
					rd.close();

					println("Done visiting url");
					var current =  allLine;
					if(current != previous) {
						ctrl.sendGlobalMessage(current);
					}
					else {
						println("Nothing to do");
					}
					previous = current;
					}
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
});
