/**
 * Utility class webTalk, gets online webpage
 */

var util = true;

function webTalk(url) {
	var netPkgs = new JavaImporter(java.io,java.net);
	with (netPkgs) {
		try {
			println("Visiting url: "+url);

			// Get the response
			var rd = new BufferedReader(new InputStreamReader(new URL(url).openConnection().getInputStream()));
			var allLine = "";
			var line = "";
			while ((line = rd.readLine()) != null)
				allLine = allLine+line;
			rd.close();

			log.debug("Done visiting url");
			return  allLine;
		} catch (err) {
			log.error("ERROR "+err);
			if(err.toString().search("Connection refused: connect") != -1) {
				return null;
			}
		}
		}
}
