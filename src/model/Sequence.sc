Sequence{

	classvar < static_NUM_STEPS=16;

	var logger;

	var controller;
	var model;

	var <> id;

	var <> currentStep;

	var <> midiTriggerNote;
	var <> midiSendChan;
	var <> bypassSequence;
	var <> globalOctave;

	var <> stepArray;
	var <> numSteps;
	var <> activeSteps;
	var <> globalChannel;
	var <> globalMidiIn;
	var <> sequenceName;
	//var <> sequenceIndex;

	var <> currentMidiData;

	var <> trigOtherSeq;
	var <> triggerSequence;

	var <> tracker;

	var <> noteLengthTask;
	var <> triggerFlag;
	var <> misfireTime;

	var <> hitCount;
	var random;
	var sequenceMoveNote;


	*new{|name,index,s|
		^super.new.initSequence(name,index);

	}

	initSequence{|name,index|
		logger =  Logger.new("Sequence");
		//properties
		sequenceName = name;
		//sequenceIndex = index;
		midiTriggerNote = 0;
		midiSendChan = 0;

		//local vars
		currentStep=0;
		hitCount = 0;

		//steps
		numSteps = 16;
		stepArray = Array.fill(numSteps,{Step.new});
		activeSteps = Array.fill(numSteps,{1});

		triggerFlag = true;
		misfireTime = 0.25;
		//creates a task that prevents the sequence from misfiring
		/*triggerTask = Task({
			1.do({
				triggerFlag = false;
				0.25.wait;
				triggerFlag = true;

			});
		});*/


	}

	//getCurrentStep{
		//^stepArray[currentStep];
	//}

	reset{
		"SEQ RESET".postln;
		currentStep=0;

	}

	getSeqDataForFileWriting{

		var tempStepList = List.new(0);
		tempStepList.add(sequenceName);

		//tempStepList.add(Array.fill(numSteps,{arg i;stepArray[i].getStepDataForFileWriting}));
		tempStepList.add(activeSteps);

		numSteps.do{arg i;
			tempStepList.add(stepArray[i].getStepDataForFileWriting);
		};

		//"seq - ".post;tempStepList.asArray.postln;

		^tempStepList.asArray;

	}

	setSeqDataFromFile{|sequenceData,newActiveSteps|

		"SEQUENCE:setSeqDataFromFile ".post;
		sequenceData[0][1].postln;
		numSteps.do{arg i;

			stepArray[i].noteArray = sequenceData[i][0];
			stepArray[i].editableProperties = sequenceData[i][1];

		};

		activeSteps = newActiveSteps;


	}

	getSendMidiData{

		var midiData = [	stepArray[currentStep].getMidiChan,
						stepArray[currentStep].getOctave,
						stepArray[currentStep].getNotesOn];		"SEQ::getSendMidiData  currentStep: ".post;currentStep.postln;
		//stepArray[currentStep].getMidiChan
		currentMidiData=midiData;
		^midiData;

	}

	onMidiIn{
		var actionNum;
		var otherAction;
		var returnArr = [0];
		//"SEQUENCE ON MIDI IN".postln;
//		if((sequenceMoveNote == 1)&&(hitCount >= stepArray[currentStep].getNumTrigHits),{
//			actionNum = 1;
//			hitCount = 0;
//		},{
//			actionNum = 2;
//			hitCount = hitCount+1;
//		});

		if(stepArray[currentStep].getBypassSequence == 0,{

			if(stepArray[currentStep].getMoveSection == 1,{
				actionNum = 3;

			},{

				if(stepArray[currentStep].getMoveNote == 1,{
					if(hitCount >= stepArray[currentStep].getNumTrigHits,{
						actionNum = 1;
						hitCount = 0;
					},{
						actionNum = 2;
						hitCount = hitCount+1;
					})
				},{
					actionNum = 2;
				})

			});

			returnArr = [actionNum];

			if(this.getTrigOtherSeq == 1,{
				otherAction = this.getOtherTriggerAction;

				//this.getOtherTriggerSequence.postln;
				//otherAction.postln;

				//some other actions must be passed
				//to DrumTrigger - mute sequence/channel etc..
				//the rest can be called directly from here
				if(otherAction == 6,{
					returnArr = [actionNum,4]
				},{
				if(otherAction == 7,{
					returnArr = [actionNum,5]
				},{
					this.getOtherTriggerSequence.performOtherTrigger(this.getOtherTriggerAction);

					})
				});
			});

		});

		"SEQUENCE ON MIDI IN".post; returnArr.post;"SEQUENCE getTrigOtherSeq".post; this.getTrigOtherSeq.postln;
		^returnArr;

	}

	skipStep{

		if(stepArray[currentStep].getRandom == 0,{

		if(currentStep<15,{currentStep = currentStep+1},{currentStep = 0});

		while{activeSteps[currentStep]==0}{

			if(currentStep<15,{currentStep = currentStep+1},{currentStep = 0});
		}

		},{

			currentStep = 16.rand;
			while{activeSteps[currentStep]==0}{currentStep = 16.rand};

		});

		currentStep.postln;
	}




	performOtherTrigger{|action|

		//var action = stepArray[currentStep].getOtherTriggerAction;
		"SEQ - ".post;sequenceName.post;" performOtherTrigger: ".post;action.postln;
		if(action == 0,{
			//DO NOTHING
		},{if(action == 1,{
			//SKIP STEP
			this.skipStep;
		},{if(action == 2,{
			//TOGGLE SKIP
			if(sequenceMoveNote == 1,{
				sequenceMoveNote = 0
			},{
				sequenceMoveNote = 1
			})

		},{if(action == 3,{
			"RESET".postln;
			currentStep = 0;

		},{if(action == 4,{
			"TOGGLE CHANGE SECTION".postln;
			//currentStep = 0;
			this.setMoveSection(1);

		},{if(action == 5,{
			"TOGGLE BYPASS SECTION".postln;
			if(stepArray[currentStep].getBypassSequence == 1,{
				stepArray[currentStep].setBypassSequence(0);
			},{
				stepArray[currentStep].setBypassSequence(1);
			})

		})
		})

		})

		})

		})

		})

	}

}
