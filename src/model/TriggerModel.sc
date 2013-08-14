TriggerModel{

	var logger ;

	var <> currentSection;
	var <> currentSequence;
	var <> currentStep;
	var <> triggerChannel;

	var <> numSteps;

	var < sectionList;
	var < sectionNames;

	var <> ready;

	var < seedNumber;

	*new{|s|
	^super.new.initTriggerModel(s);
	}

	initTriggerModel{|server|
		logger = Logger.new("TriggerModel");

		numSteps = 16;
		triggerChannel = 9;
		currentSection = 0;
		currentStep = 0;
		sectionList = List.new(0);
		sectionNames = List.new(0);
		ready = false;
		seedNumber = 0;
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

	setSeedNumber{|val|
		logger.debug("setting seedNumber "+val);
		seedNumber = val;
	}


	getSeedNumber{
		logger.debug("getSeedNumber: "+seedNumber);
		seedNumber = seedNumber + 1;
		^seedNumber;
	}

	getSequence{|id|
		var sequence;
		var i=0;
		while{i < this.getCurrentSection.sequenceList.size && sequence == nil}
		{
			if(this.getCurrentSection.sequenceList[i].id == id,{
				sequence = this.getCurrentSection.sequenceList[i];
			});
			i = i+1;
		}
		^sequence;

	}

	getSequenceIndex{|id|
		^this.getIndexFromListAndId(this.getCurrentSection.sequenceList,id);
	}

	getSectionIndex{|id|
		^this.getIndexFromListAndId(sectionList,id);
	}

	getCurrentSection{

		^sectionList[currentSection];

	}

	getCurrentSequence{

		^this.getCurrentSection.sequenceList[currentSequence];

	}

	getCurrentStep{

		^this.getCurrentSequence.stepArray[currentStep];

	}

	getIndexFromListAndId{|list,id|
	    var i = 0;
		var index;
	//logger.debug(["getIndexFromListAndId",list,list.size,id]);
		while{(index == nil) && (i < list.size)}
		{
			if(list[i].id == id,{
				index = i;
			});
			i = i+1;
		}
		^index;

	}


}