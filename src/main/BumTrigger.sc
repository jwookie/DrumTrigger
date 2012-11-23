BumTrigger{

	classvar < static_XML_FOLDER_PATH = "/Users/jonathanbaker/Desktop/test_xml/";
	var triggerMain;

	*new{|s|
	^super.new.initBumTrigger(s);
	}

	initBumTrigger{|server|

		triggerMain = TriggerMain.new;

	}


}