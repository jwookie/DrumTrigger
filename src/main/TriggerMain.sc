TriggerMain{

	var controller;
	var triggerGUI;
	var triggerModel;

	//CONSTRUCTOR

	*new{|s|
	^super.new.initTriggerMain(s);
	}

	initTriggerMain{|server|

		controller = TriggerController.new;
		triggerGUI = TriggerGUI.new(controller);
		triggerModel = TriggerModel.new(controller);

		controller.setModel(triggerModel);
		controller.gui = triggerGUI;

		controller.addNewSection("Section One");
		controller.switchSteps(0);

	}

}