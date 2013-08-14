Step{

	var logger;

	var <> noteArray;
	var <> editableProperties;
	var <> numProperties;
	var <> numMatrixKeys;
	var <> setOtherSequenceGlobally;

	//var <> midiNoteBox;//, <> midiNoteGlobalButton;
	//var <> midiChanBox;//, <> midiChanGlobalButton;
	var <> octave;
	var <> useGlobalOctave;
	var <> bypassSequence;
	var <> noteLength;
	var <> moveSequence;
	var <> numTrigHits;
	var <> randomNote;
	var <> useGlobalRandom;
	var <> triggerOtherSequence;
	var <> triggerOtherSequenceGlobally;
	var <> otherSequenceId;
	var <> useOtherSequenceGlobally;
	var <> otherActionIndex;
	var <> setOtherActionGlobally;
	var <> moveToSection;
	var <> moveSectionId;
	var <> otherActionValue;

	var <> hitCounter;


	*new{|s|
		^super.new.initStep();
	}

	initStep{
		logger = Logger.new("Step");

		//"STEP initStep".postln;
		numMatrixKeys=24;
		numProperties = 20;
		noteArray = Array.fill(numMatrixKeys,{0});
		editableProperties = Array.fill(numProperties,{0});

		octave = 0;
		useGlobalOctave = 0;
		bypassSequence = 0;
		noteLength = 0;
		moveSequence = 1;
		numTrigHits = 0;
		randomNote = 0;
		useGlobalRandom = 0;
		triggerOtherSequence = 0;
		triggerOtherSequenceGlobally = 0;
		//otherSequenceId = 0;
		useOtherSequenceGlobally = 0;
		otherActionIndex = 0;
		setOtherActionGlobally = 0;
		moveToSection = 0;
		moveSectionId = 0;
		otherActionValue = 0;

		hitCounter = 0;

	}


	getStepDataForFileWriting{

		var tempDataList = List.new(0);
		tempDataList.add(noteArray);
		numProperties.do{arg i;

			tempDataList.add(editableProperties[i]);

		};

		//"step - ".post;tempDataList.asArray.postln;

		^tempDataList.asArray;
	}




}