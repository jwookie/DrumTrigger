TriggerController{

	var trace;

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

		trace = Trace.new("TriggerController");
		otherActionsArray =
		[   "none",
			"skip step",
			"toggle skip",
			"reset",
			"section skip",
			"bypass",
			"mute sequence",
			"mute channel",
			"ramp out",
			"ramp in",
			"goto step",
			"+ pitch bend",
			"- pitch bend",
			"reset pitch"];

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
		trace.debug("SET NOTE INDICATOR");
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

	setMisfireTime{|time|
		model.getCurrentSequence.misfireTime = time;
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
		model.getCurrentStep.bypassSequence = bypass;
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
		model.getCurrentStep.otherSequenceId = model.getCurrentSection.sequenceList[sequenceIndex].id;
		trace.debug(["SETTING OTHER SEQ ID",sequenceIndex]);
		model.getCurrentStep.useOtherSequenceGlobally.postln;
		if(model.getCurrentStep.useOtherSequenceGlobally == 0,{
			this.setGlobalOtherSequence(model.getCurrentStep.otherSequenceId);
		});
	}

	setUseOtherSequenceGlobally{|useOtherSequenceGlobal|
		trace.debug(["setTriggerOtherSequenceGlobally ", useOtherSequenceGlobal]);
		model.currentStep.postln;
		model.getCurrentStep.postln;
		model.getCurrentStep.useOtherSequenceGlobally.postln;
		model.getCurrentStep.useOtherSequenceGlobally = useOtherSequenceGlobal;
		if(useOtherSequenceGlobal == 0,{
			this.setGlobalOtherSequence(model.getCurrentStep.otherSequenceId);
		});
	}

	setGlobalOtherSequence{|globalOtherSequence|
		model.getCurrentSequence.stepArray.do{arg val;
			if(val.useOtherSequenceGlobally == 0,{
				val.otherSequenceId = globalOtherSequence;
			});
		}
	}

	setOtherAction{|otherActionIndex|
		model.getCurrentStep.otherActionIndex = otherActionIndex;
		if(model.getCurrentStep.setOtherActionGlobally == 0,{
				this.setGlobalOtherAction(otherActionIndex);
			});
		["otherActionIndex"].postln;
		if(otherActionIndex >= 7,
		{
			gui.propertiesEditor.otherActionValueBox.visible = true;
		},{
			gui.propertiesEditor.otherActionValueBox.visible = false;
		});
		if(model.getCurrentStep.otherActionIndex >= 11,{
			gui.propertiesEditor.otherActionAmountBox.visible = true;
			},{
				gui.propertiesEditor.otherActionAmountBox.visible = false;
		});


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

	setOtherActionValue{|value|
		model.getCurrentStep.otherActionValue = value;
	}

	setOtherActionAmount{|value|
		model.getCurrentStep.otherActionAmount = value;
	}

	setMoveToSection{|moveToSection|
		model.getCurrentStep.moveToSection = moveToSection;
	}

	setMoveSection{|sectionIndex|
		trace.debug(["set move to section id",sectionIndex,model.sectionList[sectionIndex],model.sectionList[sectionIndex].id]);
		model.getCurrentStep.moveSectionId = model.sectionList[sectionIndex].id;
	}

	addNewSection{|sectionName|

		var newIndex = model.sectionList.size;
		var newSection = Section.new(sectionName);
		trace.debug("addNewSection " + sectionName);
		newSection.id = model.getSeedNumber;
		model.sectionList.add(newSection);
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
		model.sectionNames.put(model.currentSection,sectionName);
		this.updateSectionCombos;
	}

	removeSection{
		if(model.sectionList.size != 1,{
			model.sectionList.remove(model.getCurrentSection);
			this.updateSectionCombos;
			this.changeSection(0);
		});
	}

	updateSectionCombos{
		var names = Array.fill(model.sectionList.size,{arg i;
			model.sectionList[i].sectionName;
		});
		gui.projectEditor.sectionCombo.items = names;//model.sectionNames.asArray;
		gui.propertiesEditor.moveToSectionCombo.items = names;// model.sectionNames.asArray;
	}

	changeSection{|sectionIndex|
		trace.debug(['CHANGE SECTION',sectionIndex]);
		model.currentSection = sectionIndex;
		{gui.projectEditor.sectionTitle.string = model.getCurrentSection.sectionName}.defer;
		this.updateSequenceCombos;
		this.changeSequence(0);

		if(gui.projectEditor.sectionCombo.value != sectionIndex,{
			gui.projectEditor.sectionCombo.value = sectionIndex;
		});

		this.updateMidiTrackers;
	}

	addNewSequence{|sequenceName|
		var newIndex = model.getCurrentSection.sequenceList.size;
		var newSequence = Sequence.new(sequenceName);//,newIndex)
		trace.debug("addNewSequence " + sequenceName);
		newSequence.id = model.getSeedNumber;

		model.getCurrentSection.sequenceList.add(newSequence);
		model.getCurrentSection.sequenceNames.add(sequenceName);

		this.updateSequenceCombos;

		gui.projectEditor.sequenceCombo.value = newIndex;

		this.changeSequence(newIndex);

		this.updateMidiTrackers;
	}

	renameSequence{|sequenceName|
		var seqIndex = model.getSequenceIndex(model.getCurrentSequence.id);
		trace.debug("renameSequence "+sequenceName + ' '+seqIndex);
		model.getCurrentSequence.sequenceName = sequenceName;
		model.getCurrentSection.sequenceNames.put(seqIndex,sequenceName);

		this.updateSequenceCombos;
		this.updateMidiTrackers;
		this.updateSequenceTitle;
		gui.projectEditor.sequenceCombo.value = seqIndex;
	}

	removeSequence{
		if(model.getCurrentSection.sequenceList.size != 1,{
			model.getCurrentSection.sequenceList.remove(model.getCurrentSequence);

			this.updateSequenceCombos;
			this.changeSection(0);
		});
	}

	saveSequenceDescription{|description|
		model.getCurrentSequence.description = gui.projectEditor.sequenceDescriptionText.string;
	}

	changeSequence{|sequenceIndex|
		model.currentSequence = sequenceIndex;
		this.updateSequenceTitle;
		this.updateSequenceDescription;
		this.applyNoteSettings;
		this.setActiveSteps;
		this.switchSteps(0);

	}

	updateMidiTrackers{
	trace.debug("updateMidiTrackers");
		gui.midiTracker.addTrackers(model.getCurrentSection);
	}

	updateSequenceTitle{
		trace.debug([model.getCurrentSequence,model.currentSequence,model.currentSection]);
		{gui.projectEditor.sequenceTitle.string = model.getCurrentSequence.sequenceName}.defer;
	}

	updateSequenceDescription{
		{gui.projectEditor.sequenceDescriptionText.string = model.getCurrentSequence.description}.defer;
	}

	updateSequenceCombos{
		var names = Array.fill(model.getCurrentSection.sequenceList.size,{arg i;
			model.getCurrentSection.sequenceList[i].sequenceName
		});

		gui.projectEditor.sequenceCombo.items = names;// model.getCurrentSection.sequenceNames.asArray;
		gui.propertiesEditor.otherSequenceCombo.items = names;//model.getCurrentSection.sequenceNames.asArray;
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
		gui.propertiesEditor.misifreTimeBox.value = model.getCurrentSequence.misfireTime;
		gui.propertiesEditor.noteLengthBox.value = model.getCurrentStep.noteLength;
		gui.propertiesEditor.numTrigHitsBox.value = model.getCurrentStep.numTrigHits;
		gui.propertiesEditor.bypassSequenceButton.value = model.getCurrentStep.bypassSequence;
		gui.propertiesEditor.moveSequenceButton.value = model.getCurrentStep.moveSequence;
		gui.propertiesEditor.randomButton.value = model.getCurrentStep.randomNote;
		gui.propertiesEditor.randomGlobalButton.value = model.getCurrentStep.useGlobalRandom;
		gui.propertiesEditor.triggerOtherSequenceButton.value = model.getCurrentStep.triggerOtherSequence;
		gui.propertiesEditor.triggerOtherSequenceGlobalButton.value = model.getCurrentStep.triggerOtherSequenceGlobally;

		gui.propertiesEditor.setOtherSequenceGlobalButton.value = model.getCurrentStep.useOtherSequenceGlobally;
		gui.propertiesEditor.otherActionsCombo.value = model.getCurrentStep.otherActionIndex;

		gui.propertiesEditor.setOtherActionGlobalButton.value = model.getCurrentStep.setOtherActionGlobally;


		gui.propertiesEditor.otherActionValueBox.value = model.getCurrentStep.otherActionValue;
		if(model.getCurrentStep.otherActionIndex >= 7,{
			gui.propertiesEditor.otherActionValueBox.visible = true;
			},{
				gui.propertiesEditor.otherActionValueBox.visible = false;
		});
		gui.propertiesEditor.otherActionAmountBox.value = model.getCurrentStep.otherActionAmount;
		if(model.getCurrentStep.otherActionIndex >= 11,{
			gui.propertiesEditor.otherActionAmountBox.visible = true;
			},{
				gui.propertiesEditor.otherActionAmountBox.visible = false;
		});

		gui.propertiesEditor.moveToSectionButton.value = model.getCurrentStep.moveToSection;
		//get index of section to switch to
		gui.propertiesEditor.moveToSectionCombo.value = model.getSectionIndex(model.getCurrentStep.moveSectionId);

		// get index of other sequence
		trace.debug("get index of other sequence");
		gui.propertiesEditor.otherSequenceCombo.value = model.getSequenceIndex(model.getCurrentStep.otherSequenceId);
	}


}