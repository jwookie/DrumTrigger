BumTrigger{

	classvar < static_XML_FOLDER_PATH = "/Users/JonnyBaker/Desktop/test_xml/";
	classvar < static_XML_FILE_LIST = "save_list";
	var triggerMain;

	*new{|s|
	^super.new.initBumTrigger(s);
	}

	initBumTrigger{|server|

		triggerMain = TriggerMain.new;

	}


}