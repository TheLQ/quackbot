var ignore = true;
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
	setRequiredParams: function(paramConfig) {
		//Is it even set?
		if(typeof parameters == "undefined")
			return null;
		else if(typeof(parameters) == 'object')
			if(QuackUtils.isArray(parameters))   //Is this an array?
				for(i in parameters)
					paramConfig.addRequiredObject(parameters[i]);
			else if(typeof parameters.required == "undefined") 
				return null;
			else if(QuackUtils.isArray(parameters.required)) //Is the required field an array?
				for(i in parameters.required)
					paramConfig.addRequiredObject(parameters.required[i]);
			else //Required field must be a string or a number
				QuackUtils.handleStringNum(paramConfig, true, parameters.required);
		//Must be a string or a number
		else
			QuackUtils.handleStringNum(paramConfig, true, parameters);
	},
	setOptionalParams: function(paramConfig) {
		if(typeof(parameters) == 'object')
			//Is this an array or non-existant?
			if(parameters.length || typeof parameters.optional == 'undefined')
				return null;
			//Is the optional field an array?
			else if(parameters.optional.length && typeof parameters.optional != "string")
				for(i in parameters.optional)
					paramConfig.addOptionalObject(parameters.optional[i]);
			//Must be a string or a number
			else
				QuackUtils.handleStringNum(paramConfig,false,parameters.optional);
		else
			return null;
	},
	handleStringNum: function(paramConfig,required,object) {
		if(typeof(object) == 'number')
			if(required)
				paramConfig.setRequiredCount(object);
			else
				paramConfig.setOptionalCount(object);
		else if(typeof(object) == 'string')
			if(required)
				paramConfig.addRequiredObject(object);
			else
				paramConfig.addOptionalObject(object);
		else
			throw 'Unknown type in parameters for plugin, is '+typeof(object);
	},
	isArray: function(object) {
		return object.length && typeof object != "string";
	},
	stringClass: new java.lang.String().getClass()
}

