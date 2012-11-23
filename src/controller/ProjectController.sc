ProjectController {

	var > model;

	*new{
		^super.new.initProjectController;
	}

	initProjectController{

	}

	addNewSequence{|sequenceName|
		var newIndex = model.getCurrentSection.sequenceList.length;
		model.getCurrentSection.sequenceList.add(Sequence.new(sequenceName,newIndex));
		this.changeSequence(newIndex);
	}

	renameSequence{|sequenceName|

	}

	changeSequence{|sequenceIndex|
		model.getCurrentSection.currentSequence = sequenceIndex;
	}

}