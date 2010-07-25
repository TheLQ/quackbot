var util = false;
var hook = null;
var ignore = false;
var parameters = 0;
var admin = false;
var enabled = true;
var help = '';
var QuackUtils = {
	toJavaArray: function(type, arr) {
		var jArr;
		if(arr.length) {
			jArr = java.lang.reflect.Array.newInstance(type, arr.length);
			for(var i=0;i<arr.length;i++)
				jArr[i] = arr[i];
		}
		else {
			jArr = java.lang.reflect.Array.newInstance(type, 1);
			jArr[0] = arr;
		}
		return jArr;
	},
	getRequiredParams: function() {
		//Is it even set?
		if(typeof parameters == 'undefined')
			return 0;
		else if(typeof(parameters) == 'object')
			if(QuackUtils.isArray(parameters))
				return parameters[0]
			else if(typeof parameters.required == 'undefined')
				return 0;
			else //Required field must be a number
				return parameters.required;
		//Must be a number
		else
			return parameters;
	},
	getOptionalParams: function() {
		if(typeof parameters == 'undefined')
			return 0;
		else if(typeof(parameters) == 'object')
			if(QuackUtils.isArray(parameters))
				return parameters[1]
			else if(typeof parameters.optional== 'undefined')
				return 0;
			else //Required field must be a string or a number
				return parameters.optional;
		//Must be a number, but only for required
		else
			return 0;
	},
	isArray: function(object) {
		return object.length && typeof object != 'string';
	},
	pickBest: function(param, defult) {
		if(typeof param == 'undefined')
			return defult;
		else
			return param;
	},
	stringClass: new java.lang.String().getClass()
}
function getEnabled() {
	return command.getEnabled();
}
function setEnabled(value) {
	return command.setEnabled(value);
}
function getAdmin() {
	return command.getAdmin();
}
function setAdmin(value) {
	return command.setAdmin(value);
}
function getHelp() {
	return command.getHelp();
}
function setHelp(value) {
	return command.setHelp(value);
}
function getEnabled() {
	return command.getEnabled();
}