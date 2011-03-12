importPackage(Packages.Quackbot);
importPackage(Packages.Quackbot.info);
importPackage(Packages.Quackbot.hook);
importPackage(Packages.Quackbot.err);
importClass(Packages.java.lang.Thread);
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
	isArray: function(object) {
		return object.length && typeof object != 'string';
	},
	onCommandParse: function(theOnCommand) {
		if(theOnCommand.toSource() == "function onCommand() {return null;}")
			return 0;
		return theOnCommand.arity;
	},
	stringClass: new java.lang.String().getClass()
}