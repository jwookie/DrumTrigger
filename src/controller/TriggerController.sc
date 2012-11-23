TriggerController{
	//trigger controller

	var currentStep;
	var > model;
	var > gui;

	var fileSaveController;
	var < midiController;


	var < projectController;

	var < otherActionsArray;

	var currentStepPlaying;

	*new{
		^super.new.initTriggerController;
	}

	initTriggerController{

		otherActionsArray =
		[   "none",
			"skip step",
			"toggle skip",
			"reset",
			"section skip",
			"bypass",
			"mute sequence",
			"mute channel"];

		currentStepPlaying = 0;

		projectController = ProjectController.new;
		fileSaveController = FileSaveController.new(this);
		midiController = MidiIOController.new(this);


	}

	setModel{|trigModel|
		model = trigModel;
		projectController.model = model;
		fileSaveController.model = model;
		midiController.model = model;
	}

	onSaveSong{
		if(gui.popup != nil, {gui.popup.closePopup});
		gui.showPopup(fileSaveController,"Save");
	}

	onLoadSong{
		if(gui.popup != nil, {gui.popup.closePopup});
		gui.showPopup(fileSaveController,"Load");
	}

	setSongTitle{|title|
		gui.projectEditor.songTitle.string = title;
	}

	onMainWindowClosed{
		if(gui.popup != nil, {gui.popup.closePopup});
	}

	onLoadedDataReady{|sectionList,sectionNames|
		model.setNewSectionListData(sectionList);
		model.setNewSectionNamesData(sectionNames);

		this.updateSectionCombos;
		this.changeSection(0);
	}

	setNoteIndicator{|sequence|
		//"SET NOTE INDICATOR".postln;
		gui.midiTracker.updateTracker(sequence);
	}

	switchSteps{|stepNum|

		if((model.currentStep != nil) && (model.currentStep != stepNum), {
			gui.stepSwitches[model.currentStep].value = 0
		});

		model.currentStep = stepNum;
		//model.getCurrentSequence.currentStep = currentStep;

		gui.stepSwitches[stepNum].value = 1;

		this.applyStepSettings;
	}

	activateStep{|index,value|
		[index,value].postln;
		model.getCurrentSequence.activeSteps[index] = value;
	}

	switchNoteOn{|noteNum,stepNum,value|
		"switch note - ".post;
		noteNum.post;
		" on step - ".post;
		stepNum.postln;
		value.postln;

		model.getCurrentSequence.stepArray[stepNum].noteArray[noteNum] = value;
	}

	setMidiTriggerNote{|note|
		model.getCurrentSequence.midiTriggerNote = note;
	}

	incrementMidiNote{|moveUp|
		var midiNote = model.getCurrentSequence.midiTriggerNote;
		midiNote.post; moveUp.postln;
		if(moveUp == true,{
			model.getCurrentSequence.midiTriggerNote = midiNote +1;
		},{
			model.getCurrentSequence.midiTriggerNote = midiNote -1;
		});

		this.applyStepSettings;
	}

	setMidiSendChan{|channelNum|
		model.getCurrentSequence.midiSendChan = channelNum;
	}

	incrementMidiChan{|moveUp|
		var midiChan = model.getCurrentSequence.midiSendChan;
		if(moveUp == true,{
			this.setMidiSendChan(midiChan +1);
		},{
			this.setMidiSendChan(midiChan +1);
		});
		this.applyStepSettings;
	}

	setOctave{|octave|
		"SETTING OCTAVE ".post; octave.postln;
		model.getCurrentStep.octave = octave;
		if(model.getCurrentStep.useGlobalOctave == 0,{
			this.setGlobalOctave(octave);
		})
	}

	setUseOctaveGlobally{|useGlobal|
		model.getCurrentStep.useGlobalOctave = useGlobal;
		if(useGlobal == 0,{
			this.setGlobalOctave(model.getCurrentStep.octave)
		})
	}

	setGlobalOctave{|octave|
		model.getCurrentSequence.stepArray.do{arg val;
			if(val.useGlobalOctave == 0,{
				val.octave = octave;
			})
		}
	}

	incrementOctave{|moveUp|
		var octave = model.getCurrentStep.octave;
		if(moveUp == true,{
			this.setOctave(octave +1);
		},{
			this.setOctave(octave -1);
		});

		this.applyStepSettings;
	}

	setNoteLength{|length|
		model.getCurrentStep.noteLength = length;
	}

	setBypassSequence{|bypass|
		model.getCurrentSequence.bypassSequence = bypass;
	}

	stopMidi{

	}

	setMoveSequence{|moveSequence|
		model.getCurrentStep.moveSequence = moveSequence;
	}

	setNumTrigHits{|numHits|
		model.getCurrentStep.numTrigHits = numHits;
	}

	setRandomNote{|randomNote|
		model.getCurrentStep.randomNote = randomNote;
		if(model.getCurrentStep.useGlobalRandom == 0,{
			this.setGlobalRandom(randomNote);
		})
	}

	setUseRandomGlobally{|useGlobalRandom|
		model.getCurrentStep.useGlobalRandom = useGlobalRandom;
		if(useGlobalRandom == 1,{
			this.setGlobalRandom(model.getCurrentStep.randomNote)
		})
	}

	setGlobalRandom{|randomNote|
		model.getCurrentSequence.stepArray.do{arg val;
			if(val.useGlobalRandom == 0,{
				val.randomNote = randomNote;
			})
		}
	}

	setTriggerOtherSequence{|triggerOtherSequence|
		model.getCurrentStep.triggerOtherSequence = triggerOtherSequence;
		if(model.getCurrentStep.triggerOtherSequenceGlobally == 0,{
			this.setGlobalTriggerOtherSequence(triggerOtherSequence);
		})
	}

	setTriggerOtherSequenceGlobally{|useGlobal|

		model.getCurrentStep.triggerOtherSequenceGlobally = useGlobal;
		if(useGlobal == 0,{
			this.setGlobalTriggerOtherSequence(model.getCurrentStep.triggerOtherSequence);
		})
	}

	setGlobalTriggerOtherSequence{|triggerOtherSequence|
		model.getCurrentSequence.stepArray.do{arg val;
			if(val.triggerOtherSequenceGlobally == 0,{
				val.triggerOtherSequence = triggerOtherSequence;
			})
		}
	}

	setOtherSequence{|sequenceIndex|
		model.getCurrentStep.otherSequenceIndex = sequenceIndex;
		"SETTING OTHER SEQ INDEX ".post; sequenceIndex.postln;
		model.getCurrentStep.useOtherSequenceGlobally.postln;
		if(model.getCurrentStep.useOtherSequenceGlobally == 0,{
			this.setGlobalOtherSequence(model.getCurrentStep.otherSequenceIndex);
		});
	}

	setUseOtherSequenceGlobally{|useOtherSequenceGlobal|
		"CONTROLLER setTriggerOtherSequenceGlobally ".post; useOtherSequenceGlobal.postln;
		model.currentStep.postln;
		model.getCurrentStep.postln;
		model.getCurrentStep.useOtherSequenceGlobally.postln;
		model.getCurrentStep.useOtherSequenceGlobally = useOtherSequenceGlobal;
		if(useOtherSequenceGlobal == 0,{
			this.setGlobalOtherSequence(model.getCurrentStep.otherSequenceIndex);
		});
	}

	setGlobalOtherSequence{|globalOtherSequence|
		model.getCurrentSequence.stepArray.do{arg val;
			if(val.useOtherSequenceGlobally == 0,{
				val.otherSequenceIndex = globalOtherSequence;
			});
		}
	}

	setOtherAction{|otherActionIndex|
		model.getCurrentStep.otherActionIndex = otherActionIndex;
			if(model.getCurrentStep.setOtherActionGlobally == 0,{
				this.setGlobalOtherAction(otherActionIndex);
			})
	}

	setUseOtherActionGlobally{|useActionGlobally|
		model.getCurrentStep.setOtherActionGlobally = useActionGlobally;
	}

	setGlobalOtherAction{|actionIndex|
		model.getCurrentSequence.stepArray.do{arg val;
			if(val.setOtherActionGlobally == 0,{
				val.otherActionIndex = actionIndex;
			});
		}
	}

	setMoveToSection{|moveToSection|
		model.getCurrentStep.moveToSection = moveToSection;
	}

	setMoveSection{|sectionIndex|
		model.getCurrentStep.moveSectionIndex = sectionIndex;
	}

	addNewSection{|sectionName|

		var newIndex = model.sectionList.size;
		model.sectionList.add(Section.new(sectionName,newIndex));
		model.currentSection = newIndex;
		model.sectionNames.add(sectionName);

		this.updateSectionCombos;
		gui.projectEditor.sectionCombo.value = newIndex;

		this.addNewSequence("Sequence One");
		this.changeSection(newIndex);
	}

	renameSection{|sectionName|

		model.getCurrentSection.sectionName = sectionName;
		gui.projectEditor.sectionTitle.string = sectionName;
		model.sectionNames.put(model.getCurrentSection.sectionIndex,sectionName);
		this.updateSectionCombos;
	}

	updateSectionCombos{
		gui.projectEditor.sectionCombo.items = model.sectionNames.asArray;
		gui.propertiesEditor.moveToSectionCombo.items = model.sectionNames.asArray;
	}

	changeSection{|sectionIndex|
		model.currentSection = sectionIndex;
		gui.projectEditor.sectionTitle.string = model.getCurrentSection.sectionName;
		this.updateSequenceCombos;
		this.changeSequence(0);

		this.updateMidiTrackers;
	}

	addNewSequence{|sequenceName|
		var newIndex = model.getCurrentSection.sequenceList.size;
		model.getCurrentSection.sequenceList.add(Sequence.new(sequenceName,newIndex));
		model.getCurrentSection.sequenceNames.add(sequenceName);

		this.updateSequenceCombos;

		gui.projectEditor.sequenceCombo.value = newIndex;

		this.changeSequence(newIndex);

		this.updateMidiTrackers;
	}

	renameSequence{|sequenceName|
		model.getCurrentSequence.sequenceName = sequenceName;
		model.getCurrentSection.sequenceNames.put(model.getCurrentSequence.sequenceIndex,sequenceName);
		this.updateSequenceCombos;
		this.updateMidiTrackers;
		this.updateSequenceTitle;
	}

	changeSequence{|sequenceIndex|
		model.currentSequence = sequenceIndex;
		this.updateSequenceTitle;
		this.applyNoteSettings;
		this.setActiveSteps;
		this.switchSteps(0);
		//this.setNoteIndicator;

	}

	updateMidiTrackers{
		gui.midiTracker.addTrackers(model.getCurrentSection);
	}

	updateSequenceTitle{
		gui.projectEditor.sequenceTitle.string = model.getCurrentSequence.sequenceName;
	}

	updateSequenceCombos{
		gui.projectEditor.sequenceCombo.items = model.getCurrentSection.sequenceNames.asArray;
		gui.propertiesEditor.otherSequenceCombo.items = model.getCurrentSection.sequenceNames.asArray;
	}

	applyNoteSettings{
		gui.numFixedSteps.do{arg i;
			gui.numMatrixKeys.do{arg j;
				var noteVal = model.getCurrentSequence.stepArray[i].noteArray[j].value;
				gui.matrixKeyArray[i][j].value = noteVal;
			}
		}
	}

	setActiveSteps{
		gui.activeSteps.do{arg val, i;
			val.value = model.getCurrentSequence.activeSteps[i];
			}
	}




	applyStepSettings{
		//"APPLY STEP SETTINGS".postln;
		gui.propertiesEditor.midiNoteBox.value = model.getCurrentSequence.midiTriggerNote;
		gui.propertiesEditor.midiChanBox.value = model.getCurrentSequence.midiSendChan;
		gui.propertiesEditor.octaveBox.value = model.getCurrentStep.octave;
		gui.propertiesEditor.globalOctaveButton.value = model.getCurrentStep.useGlobalOctave;
		gui.propertiesEditor.noteLengthBox.value = model.getCurrentStep.noteLength;
		gui.propertiesEditor.numTrigHitsBox.value = model.getCurrentStep.numTrigHits;
		gui.propertiesEditor.moveSequenceButton.value = model.getCurrentStep.moveSequence;
		gui.propertiesEditor.randomButton.value = model.getCurrentStep.randomNote;
		gui.propertiesEditor.randomGlobalButton.value = model.getCurrentStep.useGlobalRandom;
		gui.propertiesEditor.triggerOtherSequenceButton.value = model.getCurrentStep.triggerOtherSequence;
		gui.propertiesEditor.triggerOtherSequenceGlobalButton.value = model.getCurrentStep.triggerOtherSequenceGlobally;
		gui.propertiesEditor.otherSequenceCombo.value = model.getCurrentStep.otherSequenceIndex;
		gui.propertiesEditor.setOtherSequenceGlobalButton.value = model.getCurrentStep.useOtherSequenceGlobally;
		gui.propertiesEditor.otherActionsCombo.value = model.getCurrentStep.otherActionIndex;
		gui.propertiesEditor.setOtherActionGlobalButton.value = model.getCurrentStep.setOtherActionGlobally;
		gui.propertiesEditor.moveToSectionButton.value = model.getCurrentStep.moveToSection;
		gui.propertiesEditor.moveToSectionCombo.value = model.getCurrentStep.moveSectionIndex;
	}

}