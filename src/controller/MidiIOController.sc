MidiIOController{

	var > model;
	var controller;

	var midiOut;

	var middleCVal;

	*new{|mainController|
		^super.new.initMidiController(mainController);

	}

	initMidiController{|mainController|

		controller = mainController;
		middleCVal = 36;
		this.initMidi;

	}

	initMidi{
		var inPorts = 2;
		var outPorts = 1;
		"Initialising MIDI..".postln;

		MIDIClient.init(inPorts,outPorts);
		// explicitly intialize the client
		inPorts.do({ arg i;
			i.postln;
			MIDIIn.connect(i, MIDIClient.sources.at(i).postln);
		});

		midiOut = MIDIOut(0);
		midiOut.latency=0.001;

		MIDIIn.noteOn = {arg src, chan, num, vel; /*this.toggleMidiInidcator(true,chan);*/this.onMidiIn(src,chan,num)};
		MIDIIn.noteOff = {arg src, chan, num, vel; /*this.toggleMidiInidcator(false,chan)*/};
	}

	resetAll{
		model.getCurrentSection.sequenceList.do{arg val;
			val.currentStep = 0;
		}
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

		step.noteArray.postln;

		step.noteArray.do{arg val,i;
			if(val == 1,{
				var midiNum = i + (step.octave*12) + middleCVal;
				["NOTE ON",sendChan,midiNum].postln;
				midiOut.noteOn(sendChan,midiNum,velocity);
				notesPlayed.add(midiNum);
			});
		};
		sequence.currentMidiData = [sendChan,notesPlayed.asArray];

		//controller.postln;
		controller.setNoteIndicator(sequence);
	}

	skipStep{|sequence|
		/*var step = sequence.stepArray[sequence.currentStep];
		//[sequence.hitCount , step.numTrigHits].postln;
		if(sequence.hitCount < step.numTrigHits,{
			sequence.hitCount = sequence.hitCount+1;
		},{*/
			sequence.hitCount = 0;
			if(sequence.stepArray[sequence.currentStep].randomNote == 0,{

				//first increment step
				if(sequence.currentStep+1 < Sequence.static_NUM_STEPS,{
					sequence.currentStep = sequence.currentStep+1
					},{
						sequence.currentStep = 0
				});

				//then increment until step is active
				//["STEP ACTIVE ?",sequence.activeSteps[sequence.currentStep]].postln;
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

		//});

		//currentStep.postln;
	}

	stopMIDI{|midiData|
		//midiData.postln;
		if(midiData != nil,{
			midiData[1].do{arg val;
				midiOut.noteOff(midiData[0],val);
			}
		});
	}

	startMisfireTask{|sequence|
		var triggerTask;
		["STSRT MISIFIRE TASK ",sequence.sequenceName,sequence.misfireTime].postln;
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
		//[src,chan,num].postln;
		//model.triggerChannel;
		if(chan == model.triggerChannel, {
			["ON MIDI IN",chan,num,"SECTION",model.currentSection].postln;
			model.getCurrentSection.sequenceList.do{arg val;
				var actionArray;// = [0];//sequence.onMidiIn;
				//var index = sequence.sequenceIndex;
				var step;
				//[num,val.midiTriggerNote].postln;
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
										//[step.hitCounter , step.numTrigHits].postln;
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
							["-------- ABOUT TO PREFORM OTHER TRIGGER",step.otherActionIndex].postln;
							this.performOtherTriggerAction(step.otherSequenceIndex,step.otherActionIndex);
						});

						if(step.moveToSection == 1,{
							["MOVING TO SECTION - "].postln;
							[model.sectionList[step.moveSectionIndex].sectionName].postln;
							model.currentSection = step.moveSectionIndex;
						});
					},{
						//trigger misfiring
						["--- MISFIRE ---",val.sequenceName].postln;
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
		["PERFORM TRIGGER ACTION",sequence.sequenceName,actionArray].postln;

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

	performOtherTriggerAction{|seqIndex,action|
		var sequence;// = model.getCurrentSection.sequenceList[seqIndex];
		["PERFORM OTHER TRIGGER !",seqIndex,action].postln;
		sequence = model.getCurrentSection.sequenceList[seqIndex];
		//sequence.postln;
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
			["MUTE SEQUENCE, SKIP STEP"].postln;
			this.stopMIDI(sequence.currentMidiData);
			//this.skipStep(sequence);


		}) })	})	})	})	})	})
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

		"ON KEY DOWN".postln;
		//[keyVal,note].postln;
		//if it is, convert to midi num
		if(note != nil,{
			note = note + 36;
			this.onMidiIn(0,model.triggerChannel,note);
		});
	}

	killMidi{
		"KILL MIDI".postln;
		16.do{arg i;

			80.do{arg j;
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