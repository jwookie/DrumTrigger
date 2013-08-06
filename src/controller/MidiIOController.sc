MidiIOController{

	var logger;

	var > model;
	var controller;

	var midiOut;

	var middleCVal;
	var list_RampTasks;

	*new{|mainController|
		^super.new.initMidiController(mainController);

	}

	initMidiController{|mainController|

		logger = Logger.new("MidiIOController");
		controller = mainController;
		middleCVal = 36;
		list_RampTasks = List.new();
		this.initMidi;

	}

	setAsReady{|ready|

		model.ready = ready;

	}

	initMidi{
		var inPorts = 2;
		var outPorts = 1;
		logger.debug("Initialising MIDI..");

		MIDIClient.init(inPorts,outPorts);
		// explicitly intialize the client
		inPorts.do({ arg i;
			MIDIIn.connect(i, MIDIClient.sources.at(i));
		});

		midiOut = MIDIOut(0);
		midiOut.latency=0.001;

		MIDIIn.noteOn = {arg src, chan, num, vel; /*this.toggleMidiInidcator(true,chan);*/this.onMidiIn(src,chan,num)};
		MIDIIn.noteOff = {arg src, chan, num, vel; /*this.toggleMidiInidcator(false,chan)*/};
	}

	resetAll{
		//reset model
		model.sectionList.do{arg section;
			section.sequenceList.do{arg sequence;
				sequence.currentStep = 0;
			};
		};
		//reset any cc controllers..
		16.do{arg i;
			midiOut.control(i,7,127);
		};
		//set back to first section
		controller.changeSection(0);

		list_RampTasks.do{arg val;val.stop()};



	}

	onSetTriggerChannel{|chan|
		model.triggerChannel = chan;
	}

	sendMidi{|sequence|
		var notesPlayed = List.new(0);
		var velocity = 127;
		var step;
		var sendChan;

		step = sequence.stepArray[sequence.currentStep];
		sendChan = sequence.midiSendChan - 1;//minus one to sync up with sequencer

		if(sequence.currentMidiData!=nil,{this.stopMIDI(sequence.currentMidiData)});

		step.noteArray.do{arg val,i;
			if(val == 1,{
				var midiNum = i + (step.octave*12) + middleCVal;
				logger.debug(["NOTE ON",sendChan,midiNum]);
				midiOut.noteOn(sendChan,midiNum,velocity);
				notesPlayed.add(midiNum);
			});
		};
		sequence.currentMidiData = [sendChan,notesPlayed.asArray];

		controller.setNoteIndicator(sequence);
	}

	skipStep{|sequence|
		sequence.hitCount = 0;
			if(sequence.stepArray[sequence.currentStep].randomNote == 0,{

				//first increment step
				if(sequence.currentStep+1 < Sequence.static_NUM_STEPS,{
					sequence.currentStep = sequence.currentStep+1
					},{
						sequence.currentStep = 0
				});

				//then increment until step is active
				while{sequence.activeSteps[sequence.currentStep]==0}{
					//["SEQ STEP ",sequence.currentStep].postln;
					if(sequence.currentStep+1 < Sequence.static_NUM_STEPS,{
						sequence.currentStep = sequence.currentStep+1;
						},{
							sequence.currentStep = 0
					});
				};

				},{

					sequence.currentStep = Sequence.static_NUM_STEPS.rand;
					while{sequence.activeSteps[sequence.currentStep]==0}{sequence.currentStep = Sequence.static_NUM_STEPS.rand};
			});
	}

	stopMIDI{|midiData|
		if(midiData != nil,{
			midiData[1].do{arg val;
				midiOut.noteOff(midiData[0],val);
			}
		});
	}

	startMisfireTask{|sequence|
		var triggerTask;
		//creates a task that prevents the sequence from misfiring
		triggerTask = Task({
			1.do({
				sequence.triggerFlag = false;
				sequence.misfireTime.wait;
				sequence.triggerFlag = true;

			});
		});
		triggerTask.start;
	}

	onMidiIn{|src,chan,num|
		if(chan == model.triggerChannel, {
			logger.debug(["ON MIDI IN",model.ready,chan,num]);
			if(model.ready == false,{^false;});
			model.getCurrentSection.sequenceList.do{arg val;
				var actionArray;// = [0];//sequence.onMidiIn;
				var step;
				if(num == val.midiTriggerNote,{
					if(val.triggerFlag == true,{
						//start trigger task to stop misifiring
						this.startMisfireTask(val);

						step = val.stepArray[val.currentStep];
						//first get the action
						if(step.bypassSequence == 0,{
							if(step.moveToSection == -11,{//TEMP FIX
								actionArray = [3];
								},{
									if(step.moveSequence == 1,{
										if(step.numTrigHits > (step.hitCounter + 1),{
											actionArray = [2];
											step.hitCounter = step.hitCounter + 1;
											},{
												actionArray = [1];
												step.hitCounter = 0;
										})
										},{
											actionArray = [2];
									})
							});
							},{
								actionArray = [0];
						});

						this.performTriggerAction(val,actionArray);
						if(step.triggerOtherSequence == 1,{
							this.performOtherTriggerAction(step);
						});

						if(step.moveToSection == 1,{
							logger.debug(["MOVING TO SECTION - "]);
							logger.debug([model.sectionList[step.moveSectionIndex].sectionName]);
							controller.changeSection(step.moveSectionIndex);
							//model.currentSection = step.moveSectionIndex;
						});
					},{
						//trigger misfiring
						logger.debug(["--- MISFIRE ---",val.sequenceName]);
					});
				});
			}
		});

	}

	performTriggerAction{|sequence,actionArray|
		var actionNum;
		//var actionArray = [0];//sequence.onMidiIn;
		var index = sequence.sequenceIndex;
		var step = sequence.stepArray[sequence.currentStep];
		logger.debug(["PERFORM TRIGGER ACTION",sequence.sequenceName,actionArray]);

		actionArray.do{arg val;
			actionNum = val;//actionArray[0];

			//SWITCH FOR ACTIONS TO PERFORM ON
			//RECEIVING TRIGGER SIGNAL
			// STILL IN PROGRESS, CURRENTLY:
			// 0 - Do nothing
			// 1 - play note and skip step
			// 2 - play notes, dont skip step
			// 3 - play notes, change section
			// 4 - mute a sequence

			if(actionNum == 0, {
				//DO NOTHING
			},{if(actionNum == 1, {
				//SEND MIDI, SKIP STEP
				//sectionList[currentSection].currentSequence = index;
				this.sendMidi(sequence);
				this.skipStep(sequence);
			},{if(actionNum == 2,{
				//SEND MIDI, DON'T SKIP STEP
				//sectionList[currentSection].currentSequence = index;
				this.sendMidi(sequence);
			},{if(actionNum == 3,{
				//SEND MIDI, CHANGE SECTION
				this.sendMidi(sequence);
				controller.changeSection(step.moveSectionIndex);
			},{if(actionNum == 4,{
				//MUTE SEQUENCE
				this.stopMIDI(sequence.currentMidiData);

			})})})})});
		};
	}

	performOtherTriggerAction{|step|
		//seqIndex,action
		var seqIndex = step.otherSequenceIndex;
		var action = step.otherActionIndex;
		//var sequence = model.getCurrentSection.sequenceList[seqIndex];
		var sequence = model.getSequence(step.otherSequenceId);
		logger.debug(["PERFORM OTHER TRIGGER ON",sequence.sequenceName,seqIndex,action]);

		switch(action,
			0,{},
			1,{
			//SKIP STEP
				this.skipStep(sequence);},
			2,{
				//TOGGLE SKIP
				if(sequence.stepArray[sequence.currentStep].moveSequence == 1,{
					sequence.stepArray[sequence.currentStep].moveSequence = 0
					},{
						sequence.stepArray[sequence.currentStep].moveSequence = 1
			    })},
			3,{
				logger.debug("RESET");
				sequence.currentStep = 0;},
			4,{
				logger.debug("TOGGLE CHANGE SECTION");
				//currentStep = 0;
				sequence.stepArray[sequence.currentStep].moveToSection = 1;},
			5,{
				logger.debug("TOGGLE BYPASS SECTION");
				if(sequence.stepArray[sequence.currentStep].bypassSequence == 1,{
					sequence.stepArray[sequence.currentStep].bypassSequence = 0;
					},{
						sequence.stepArray[sequence.currentStep].bypassSequence = 1;
			    })},
			6,{
				logger.debug(["MUTE SEQUENCE"]);
				this.stopMIDI(sequence.currentMidiData);},
			7,{
				logger.debug(["MUTE CHANNEL"]);
				//TODO..
			   },
			8,{
				logger.debug(["FADE OUT"]);
				this.startRampTask("out",sequence,step.otherActionValue);},
			9,{
				logger.debug(["FADE IN"]);
				this.startRampTask("IN",sequence,step.otherActionValue);},
			10,{
				logger.debug(["GOTO STEP"]);
				sequence.currentStep = (step.otherActionValue - 1).floor;}
		)



		/*
		if(action == 0,{
			//DO NOTHING
		},{if(action == 1,{
			//SKIP STEP
			this.skipStep(sequence);
		},{if(action == 2,{
			//TOGGLE SKIP
			if(sequence.stepArray[sequence.currentStep].moveSequence == 1,{
				sequence.stepArray[sequence.currentStep].moveSequence = 0
			},{
				sequence.stepArray[sequence.currentStep].moveSequence = 1
			})

		},{if(action == 3,{
			"RESET".postln;
			sequence.currentStep = 0;

		},{if(action == 4,{
			"TOGGLE CHANGE SECTION".postln;
			//currentStep = 0;
			sequence.stepArray[sequence.currentStep].moveToSection = 1;

		},{if(action == 5,{
			"TOGGLE BYPASS SECTION".postln;
			if(sequence.stepArray[sequence.currentStep].bypassSequence == 1,{
				sequence.stepArray[sequence.currentStep].bypassSequence = 0;
			},{
				sequence.stepArray[sequence.currentStep].bypassSequence = 1;
			})

		},{if(action == 6,{
			["MUTE SEQUENCE"].postln;
			this.stopMIDI(sequence.currentMidiData);
			//this.skipStep(sequence);

		},{if(action == 7,{
			["MUTE CHANNEL"].postln;
			//TODO..

		},{if(action == 8,{
			["FADE OUT"].postln;
			this.startRampTask("out",sequence,step.otherActionValue);

		},{if(action == 9,{
			["FADE IN"].postln;
			this.startRampTask("IN",sequence,step.otherActionValue);

		},{if(action == 10,{
			["GOTO STEP"].postln;
			sequence.currentStep = (step.otherActionValue - 1).floor;

		}) })	})	})	})	})	}) }) }) }) })
		*/
	}

	startRampTask{|direction,sequence,length|
		var rampTask;
		var len = (length*50).floor;
		logger.debug(["START FADE OUT",len]);
		rampTask = Task({
			len.do({arg i;
				var val;
				logger.debug([i,sequence.midiSendChan-1,7,(127 * ((len-i)/len)).ceil]);
				if(direction == "out",{
					val = (127 * ((len-i)/len)).ceil;
					},{
						val = 127 - (127 * ((len-i)/len)).ceil;
				});
				midiOut.control(sequence.midiSendChan-1,7,val);
				0.02.wait;

			});
			logger.debug(["RAMP OUT DONE"]);
			list_RampTasks.remove(rampTask);
		});
		rampTask.start;
		list_RampTasks.add(rampTask);
	}

	onKeyDown{|keyVal|
		var note = nil;
		var i = 0;
		var keynotes = [65,87,83,69,68,70,84,71,89,72,85,74,75,79,76,80,59,39];


		//check if key pressed is valid
		while({(note == nil) && (i < keynotes.size)},{
			if(keynotes[i] == keyVal,{
				note = i;
				},{
				i=i+1
			});
		});
		logger.debug("- - - -");
		//if it is, convert to midi num
		if(note != nil,{
			logger.debug(["ON KEY DOWN",note + 36]);
			//note = note + 36;
			this.onMidiIn(0,model.triggerChannel,note+36);
		});
	}

	killMidi{
		logger.debug("KILL MIDI");
		16.do{arg i;

			127.do{arg j;
				midiOut.noteOff(i,j);

			}
		}
	}

	/*toggleMidiInidcator{|on,chan|
		var col;

		if(chan == model.triggerChannel, {
			if(on == true,{col = Color.red},{col = Color.white});
			{midiIndicator.background = col}.defer;
		})

	}*/

}