MidiIOController{

	classvar < static_RAMP_CC = 7;
	classvar < static_PITCH_MID = 8192;

	var trace;

	var > model;
	var controller;

	var midiOut;

	var middleCVal;
	var list_RampTasks;
	var channels;

	*new{|mainController|
		^super.new.initMidiController(mainController);

	}

	initMidiController{|mainController|

		trace = Trace.new("MidiIOController");
		controller = mainController;
		middleCVal = 36;
		list_RampTasks = List.new();
		this.initMidi;
		channels = Array.fill(16,{MidiChannel.new});

	}

	setAsReady{|ready|

		model.ready = ready;

	}

	initMidi{
		var inPorts = 2;
		var outPorts = 1;
		trace.debug("Initialising MIDI..");

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
				sequence.pitchBendPosition = 0;
			};
		};
		//reset any cc controllers..
		16.do{arg i;
			midiOut.control(i,MidiIOController.static_RAMP_CC,127);
		};
		//reset pitch cc control
		16.do{arg i;
			midiOut.bend(i,MidiIOController.static_PITCH_MID);
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
				trace.debug(["NOTE ON",sendChan,midiNum]);
				midiOut.noteOn(sendChan,midiNum,velocity);
				notesPlayed.add(midiNum);
				channels[sendChan].switchOn(midiNum);
			});
		};
		sequence.currentMidiData = [sendChan,notesPlayed.asArray];
		if(step.noteLength > 0,{
			if(sequence.noteLengthTask != nil,{
				sequence.noteLengthTask.stop;
			});
			this.createNoteLengthTask(sequence,step);
		});

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

	//midi data is an array - [midiChan,[midiNotes..]]
	stopMIDI{|midiData|
		if(midiData != nil,{
			midiData[1].do{arg val;
				midiOut.noteOff(midiData[0],val);
				channels[midiData[0]].switchOff(val);
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

	createNoteLengthTask{|sequence,step|
		var lengthTask;
		//creates a task that switches off the notes just played after a certain amount of time
		lengthTask = Task({
			1.do({
				step.noteLength.wait;
				this.stopMIDI(sequence.currentMidiData);
			});
		});
		sequence.noteLengthTask = lengthTask;
		sequence.noteLengthTask.start;
	}

	onMidiIn{|src,chan,num|
		if(chan == model.triggerChannel, {
			var skipSectionId = nil;
			trace.debug(["ON MIDI IN",model.ready,chan,num]);
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

						trace.debug(["move to section?",step.moveToSection,step.moveSectionId]);
						if(step.moveToSection == 1,{
							//if skip section is set, save the id
							//for after all the sequences have performed their actions
							if(skipSectionId != nil,{
								trace.error(["SKIP SECTION SET TWICE ! !"]);
							});
							skipSectionId = step.moveSectionId;
						});
					},{
						//trigger misfiring
						//trace.debug(["--- MISFIRE ---",val.sequenceName]);
					});//end of trigger flag
				});//end of midiTriggerNote
			};//end of sequence list
			trace.debug(["skip section?",skipSectionId]);
			if(skipSectionId != nil,{
				trace.debug(["- - - - - - MOVING TO SECTION - - - - - - - - - - - - - -  "]);
				{controller.changeSection(model.getSectionIndex(skipSectionId))}.defer;
			});

		});//end of trigger channel

	}

	performTriggerAction{|sequence,actionArray|
		var actionNum;
		var step = sequence.stepArray[sequence.currentStep];
		trace.debug(["PERFORM TRIGGER ACTION",sequence.sequenceName,actionArray]);

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
				controller.changeSection(model.getSectionIndex(step.moveSectionId));
			},{if(actionNum == 4,{
				//MUTE SEQUENCE
				this.stopMIDI(sequence.currentMidiData);

			})})})})});
		};
	}

	performOtherTriggerAction{|step|
		//seqIndex,action
		var action = step.otherActionIndex;
		//var sequence = model.getCurrentSection.sequenceList[seqIndex];
		var sequence = model.getSequence(step.otherSequenceId);
		trace.debug(["PERFORM OTHER TRIGGER ON",sequence.sequenceName,action]);

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
				trace.debug("RESET");
				sequence.currentStep = 0;},
			4,{
				trace.debug("TOGGLE CHANGE SECTION");
				//currentStep = 0;
				sequence.stepArray[sequence.currentStep].moveToSection = 1;},
			5,{
				trace.debug("TOGGLE BYPASS SECTION");
				if(sequence.stepArray[sequence.currentStep].bypassSequence == 1,{
					sequence.stepArray[sequence.currentStep].bypassSequence = 0;
					},{
						sequence.stepArray[sequence.currentStep].bypassSequence = 1;
			    })},
			6,{
				trace.debug(["MUTE SEQUENCE"]);
				this.stopMIDI(sequence.currentMidiData);},
			7,{
				trace.debug(["MUTE CHANNEL",step.otherActionValue,channels[step.otherActionValue].noteList.asArray]);
				this.stopMIDI([step.otherActionValue-1,channels[step.otherActionValue-1].noteList.asArray]);
			   },
			8,{
				trace.debug(["FADE OUT"]);
				this.startRampTask("out",sequence,step.otherActionValue);},
			9,{
				trace.debug(["FADE IN"]);
				this.startRampTask("IN",sequence,step.otherActionValue);},
			10,{
				trace.debug(["GOTO STEP"]);
				sequence.currentStep = (step.otherActionValue - 1).floor;},
			11,{
				this.pitchBend("up",sequence,step)},
			12,{
				this.pitchBend("down",sequence,step)},
			13,{
				this.pitchBend("reset",sequence,step)}
		)

	}

	startRampTask{|direction,sequence,length|
		var rampCC = MidiIOController.static_RAMP_CC;
		var rampTask;
		var len = (length*50).floor;
		trace.debug(["START FADE OUT",len]);
		rampTask = Task({
			len.do({arg i;
				var val;
				trace.debug([i,sequence.midiSendChan-1,rampCC,(127 * ((len-i)/len)).ceil]);
				if(direction == "out",{
					val = (127 * ((len-i)/len)).ceil;
					},{
						val = 127 - (127 * ((len-i)/len)).ceil;
				});
				midiOut.control(sequence.midiSendChan-1,rampCC,val);
				0.02.wait;

			});
			trace.debug(["RAMP OUT DONE"]);
			list_RampTasks.remove(rampTask);
		});
		rampTask.start;
		list_RampTasks.add(rampTask);
	}

	pitchBend{|direction,sequence,step|
		//calulate value to send
		var incVal = (MidiIOController.static_PITCH_MID*step.otherActionAmount)/step.otherActionValue;
		var pitchBendVal;
		//update pitch bend position
		switch(direction,
			"up",{
				sequence.pitchBendPosition = sequence.pitchBendPosition + 1;
				pitchBendVal = min(sequence.pitchBendPosition,step.otherActionValue) * incVal;
			},
			"down",{
				sequence.pitchBendPosition = sequence.pitchBendPosition - 1;
				pitchBendVal = max(sequence.pitchBendPosition,0-step.otherActionValue) * incVal;
			},
			"reset",{
				sequence.pitchBendPosition = 0;
				midiOut.bend(sequence.midiSendChan-1,MidiIOController.static_PITCH_MID)
				^"";//return
			}
		);
		trace.debug("pitch bend val "+pitchBendVal);
		midiOut.bend(sequence.midiSendChan-1,MidiIOController.static_PITCH_MID + pitchBendVal);
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
		trace.debug("- - - -");
		//if it is, convert to midi num
		if(note != nil,{
			trace.debug(["ON KEY DOWN",note + 36]);
			//note = note + 36;
			this.onMidiIn(0,model.triggerChannel,note+36);
		});
	}

	killMidi{
		var i = 0;
		channels.do{arg val;
			this.stopMIDI([i, val.noteList.asArray]);
			i = i+1;
		}

	}

}