TriggerModel{

	var <> currentSection;
	var <> currentSequence;
	var <> currentStep;
	var <> triggerChannel;

	var <> numSteps;

	var < sectionList;
	var < sectionNames;

	*new{|s|
	^super.new.initTriggerModel(s);
	}

	initTriggerModel{|server|

		numSteps = 16;
		triggerChannel = 9;
		currentSection = 0;
		currentStep = 0;
		sectionList = List.new(0);
		sectionNames = List.new(0);
		//add initial section
		//sectionList.add(Section.new("Section One"));
	}

	setNewSectionListData{|newList|
		sectionList = newList;
	}

	setNewSectionNamesData{|newNames|
		sectionNames = newNames;
	}

	//resetModel

	getCurrentSection{

		^sectionList[currentSection];

	}

	getCurrentSequence{

		^this.getCurrentSection.sequenceList[currentSequence];

	}

	getCurrentStep{

		^this.getCurrentSequence.stepArray[currentStep];

	}


}