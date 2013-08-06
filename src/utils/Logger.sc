Logger{

	var clazzName;

	*new{|clazz|
		^super.new.initLogger(clazz);

	}

	initLogger{|clazz|
		clazzName = clazz;
	}

	debug{|msg|
		[clazzName,msg].postln;
	}

	info{|msg|
		[clazzName,"INFO",msg].postln;
	}

	error{|msg|
		[clazzName,"ERROR",msg].postln;
	}

	warning{|msg|
		[clazzName,"WARNING",msg].postln;
	}


}