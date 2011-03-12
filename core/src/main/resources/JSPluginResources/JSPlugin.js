
var name, help = "", admin = false, enabled = true, file, alreadySetup = false;

function getRequiredParams() {
	var specified = 0;
	var onCommandSpec = QuackUtils.onCommandParse(onCommand);
	if(typeof(parameters) == 'object') {
		if(QuackUtils.isArray(parameters))
			specified = parameters[0]
		else if(typeof parameters.required != 'undefined')
			specified =  parameters.required;
	}
	//Must be a number
	else if(typeof parameters != 'undefined')
		specified = parameters;

	//Subtract the optional if nessesary
	optional = getOptionalParams();
	if(specified != 0) //If manually specified, return it
		return specified;
	else if(optional != 0 && onCommandSpec != 0) //If there is an onCommand and optional, subtract it out
		return onCommandSpec - optional;
	//Only left with onCommand or 0
	return onCommandSpec;
}

function getOptionalParams() {
	var specified = 0;
	if(typeof(parameters) == 'object')
		if(QuackUtils.isArray(parameters))
			specified = parameters[1]
		else if(typeof parameters.optional != 'undefined')
			specified = parameters.optional;
	//You can't label individual params as optional, so it can only be specified
	return specified;
}