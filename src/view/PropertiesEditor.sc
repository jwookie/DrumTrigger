PropertiesEditor{

	var logger;

	var window;
	var controller;

	//Labels and stuff
	var midiNumLabel,noteLengthLabel, octaveLabel, midiChanLabel,moveToNoteLabel,numTrigHitsLabel;

	//GUI components
	var <> midiNoteBox;
	var <> midiNoteGlobalButton;
	var <> midiChanBox;
	var <> midiChanGlobalButton;
	var <> octaveBox;
	var <> globalOctaveButton;
	var <> misifreTimeBox;
	var <> noteLengthBox;
	var <> bypassSequenceButton;
	var <> moveSequenceButton;
	var <> numTrigHitsBox;
	var <> randomButton;
	var <> randomGlobalButton;
	var <> triggerOtherSequenceButton;
	var <> triggerOtherSequenceGlobalButton;
	var <> otherSequenceCombo;
	var <> setOtherSequenceGlobalButton;
	var <> otherActionsCombo;
	var <> setOtherActionGlobalButton;
	var <> moveToSectionButton;
	var <> moveToSectionCombo;
	var <> otherActionValueBox;

	var muteSequenceButton;//just for debug at the moment

	//var <> otherActionsArray;

	//GUI positional vars
	var stepSettingsYpos;
	var stepSettingsXpos;


	//temp
	var sectionList;
	var currentSection;
	var editableProperties;


	*new{|initWindow,initController|
	^super.new.initPropertiesEditor(initWindow,initController);
	}

	initPropertiesEditor{|initWindow,initController|
		logger = Logger.new("PropertiesEditor");

		window = initWindow;
		controller = initController;

		/*otherActionsArray =
		[   "none",
			"skip step",
			"toggle skip",
			"reset",
			"section skip",
			"bypass",
			"mute sequence",
			"mute channel"];*/

		this.setOldVariables;
	}

	setOldVariables{
		stepSettingsYpos = 290;
		stepSettingsXpos = 30;

	}

	createEditor{

		this.createSequencePropertiesBackground;
		this.createMidiNoteEditor;
		this.createMidiChannelEditor;
		this.createOctaveEditor;
		this.createNoteLengthEditor;
		this.createBypassSequence;
		this.createMisfireTime;
		//this.createMuteSequence;
		this.createMoveSequence;
		this.createTriggerHitsEditor;
		this.createRandomNoteButton;
		this.createTriggerOtherSequenceEditors;
		this.createMoveToNextSectionEditors;

	}

	createSequencePropertiesBackground{

		var sectionPropertiesBG = StaticText.new(window,Rect(stepSettingsXpos, stepSettingsYpos-5, 320, 30));
		sectionPropertiesBG.background=Color.new255(190, 190, 190);
	}

	createMidiNoteEditor{

	//midi note number
	midiNumLabel = StaticText.new(window,Rect(stepSettingsXpos+5, stepSettingsYpos, 120, 20))
	.string_("Midi Trigger Note:")
	.stringColor_(Color.black);

	midiNoteBox = NumberBox.new(window,Rect(stepSettingsXpos+125, stepSettingsYpos, 30, 20))
	.action_{|v| controller.setMidiTriggerNote(v.value)};

	//up button
	Button.new(window,Rect(stepSettingsXpos+155, stepSettingsYpos, 10, 10))
	.states_([ [ "+", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ] ])
	.action_{ controller.incrementMidiNote(true) };
	//down button
	Button.new(window,Rect(stepSettingsXpos+155, stepSettingsYpos+10, 10, 10))
	.states_([ [ "-", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ] ])
	.action_{ controller.incrementMidiNote(false) };

	}

	createMidiChannelEditor{

	//midi channel number//(stepSettingsXpos+150, stepSettingsYpos, 100, 20)
	midiChanLabel = StaticText.new(window,Rect(stepSettingsXpos+175, stepSettingsYpos, 100, 20))
	.string_("Midi Channel:")
	.stringColor_(Color.black)
	.action_{|v| };
	//box//(stepSettingsXpos+200, stepSettingsYpos, 30, 20)
	midiChanBox = NumberBox.new(window,Rect(stepSettingsXpos+265, stepSettingsYpos, 30, 20))
	.action_{|v| controller.setMidiSendChan(v.value)};

	//up button//(stepSettingsXpos+230, stepSettingsYpos, 10, 10)
	Button.new(window,Rect(stepSettingsXpos+295, stepSettingsYpos, 10, 10))
	.states_([ [ "+", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ] ])
	.action_{controller.incrementMidiChan(true) };
	//down button//(stepSettingsXpos+230, stepSettingsYpos+10, 10, 10)
	Button.new(window,Rect(stepSettingsXpos+295, stepSettingsYpos+10, 10, 10))
	.states_([ [ "-", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ] ])
	.action_{controller.incrementMidiChan(false) };

	}

	createOctaveEditor{

		//octave setting
		octaveLabel = StaticText.new(window,Rect(stepSettingsXpos, stepSettingsYpos+30, 100, 20))
		.string_("Octave:")
		.stringColor_(Color.black)
		.action_{|v| };
		octaveBox = NumberBox.new(window,Rect(stepSettingsXpos+90, stepSettingsYpos+30, 30, 20))
		.action_{|v| controller.setOctave(v.value)};
		//up button
		Button.new(window,Rect(stepSettingsXpos+120, stepSettingsYpos+30, 10, 10))
		.states_([ [ "+", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ] ])
		.action_{controller.incrementOctave(true)};
		//down button
		Button.new(window,Rect(stepSettingsXpos+120, stepSettingsYpos+40, 10, 10))
		.states_([ [ "-", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ] ])
		.action_{controller.incrementOctave(false)};

		//keep setting button
		globalOctaveButton = Button.new(window,Rect(stepSettingsXpos+130, stepSettingsYpos+30, 20, 20))
						.states_([ [ "", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ],
						[ "", Color(1.0, 1.0, 1.0, 1.0), Color(0.0, 0.0, 1.0, 1.0) ] ])
						.action_{|v| controller.setUseOctaveGlobally(v.value)};
	}

	createMisfireTime{
		//note length
		StaticText.new(window,Rect(stepSettingsXpos+175, stepSettingsYpos+30, 80, 20))
					.string_("Misfire time:")
					.stringColor_(Color.black)
					.action_{|v| };
		misifreTimeBox = NumberBox.new(window,Rect(stepSettingsXpos+255, stepSettingsYpos+30, 50, 20))
					.action_{|v|controller.setMisfireTime(v.value)};
	}

	createNoteLengthEditor{
		//note length
		noteLengthLabel = StaticText.new(window,Rect(stepSettingsXpos+175, stepSettingsYpos+60, 50, 20))
					.string_("Length:")
					.stringColor_(Color.black)
					.action_{|v| };
		noteLengthBox = NumberBox.new(window,Rect(stepSettingsXpos+225, stepSettingsYpos+60, 50, 20))
					.action_{|v|controller.setNoteLength(v.value)};
	}


	createBypassSequence{

		//BYPASS SEQUENCE
		StaticText.new(window,Rect(stepSettingsXpos, stepSettingsYpos+60, 120, 20))
					.string_("Bypass sequence:")
					.stringColor_(Color.black)
					.action_{|v| };

		bypassSequenceButton = Button.new(window,Rect(stepSettingsXpos+120, stepSettingsYpos+60, 40, 20))
							.states_([ [ "Off", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ],
							[ "On", Color(1.0, 1.0, 1.0, 1.0), Color(0.0, 0.0, 1.0, 1.0) ] ])
							.action_{|v| controller.setBypassSequence(v.value)};
	}


	createMuteSequence{

	muteSequenceButton = Button.new(window,Rect(stepSettingsXpos+220, stepSettingsYpos+60, 40, 20))
			.states_([ [ "MUTE", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ]])
			.action_{|v|
					//this.stopMIDI(sectionList[currentSection].getSequence.getSendMidiData)
					controller.stopMidi;
					};

	}


	createMoveSequence{

		//move sequence to next note
		moveToNoteLabel= StaticText.new(window,Rect(stepSettingsXpos, stepSettingsYpos+90, 120, 20))
		.string_("Move Sequence:")
		.stringColor_(Color.black)
		.action_{|v| };

		moveSequenceButton = Button.new(window,Rect(stepSettingsXpos+105,
												stepSettingsYpos+90, 40, 20))
						.states_([ [ "Off", Color(0.0, 0.0, 0.0, 1.0),
						Color(1.0, 0.0, 0.0, 1.0) ],
						[ "On", Color(1.0, 1.0, 1.0, 1.0), Color(0.0, 0.0, 1.0, 1.0) ] ])
						.action_{|v|controller.setMoveSequence(v.value)};

	}

	createTriggerHitsEditor{

		// number of trigger hits to move sequence
		numTrigHitsLabel = StaticText.new(window,Rect(stepSettingsXpos+175, stepSettingsYpos+90, 100, 20))
		.string_("Trigger Hits:")
		.stringColor_(Color.black)
		.action_{|v| };

		numTrigHitsBox = NumberBox.new(window,Rect(stepSettingsXpos+255, stepSettingsYpos+90, 30, 20))
		.action_{|v| controller.setNumTrigHits(v.value)};
	}

	createRandomNoteButton{
	//RANDOM BUTTON
	StaticText.new(window,Rect(stepSettingsXpos, stepSettingsYpos+115, 100, 20))
	.string_("Random:")
	.stringColor_(Color.black)
	.action_{|v| };

	randomButton = Button.new(window,Rect(stepSettingsXpos+95, stepSettingsYpos+115, 40, 20))
					.states_([ [ "Off", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ],
					[ "On", Color(1.0, 1.0, 1.0, 1.0), Color(0.0, 0.0, 1.0, 1.0) ] ])
					.action_{|v|controller.setRandomNote(v.value)};
	randomGlobalButton = Button.new(window,Rect(stepSettingsXpos+135, stepSettingsYpos+115, 20, 20))
					.states_([ [ "", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ],
					[ "", Color(1.0, 1.0, 1.0, 1.0), Color(0.0, 0.0, 1.0, 1.0) ] ])
					.action_{|v|controller.setUseRandomGlobally(v.value)};
	}

	createTriggerOtherSequenceEditors{

	//TRIGGER OTHER SEQUENCE
	StaticText.new(window,Rect(stepSettingsXpos, stepSettingsYpos+140, 120, 20))
	.string_("Trigger Other:")
	.stringColor_(Color.black)
	.action_{|v| };

	triggerOtherSequenceButton = Button.new(window,Rect(stepSettingsXpos+95, stepSettingsYpos+140, 40, 20))
					.states_([ [ "Off", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ],
							[ "On", Color(1.0, 1.0, 1.0, 1.0), Color(0.0, 0.0, 1.0, 1.0) ] ])
					.action_{|v|controller.setTriggerOtherSequence(v.value)};

	"triggerOtherSequenceButton".postln;

	triggerOtherSequenceGlobalButton = Button.new(window,Rect(stepSettingsXpos+135, stepSettingsYpos+140, 20, 20))
							.states_([ [ "", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ],
							[ "", Color(1.0, 1.0, 1.0, 1.0), Color(0.0, 0.0, 1.0, 1.0) ] ])
							.action_{|v|controller.setTriggerOtherSequenceGlobally(v.value)};

	"triggerOtherGlobalButton".postln;
	//TRIGGER OTHER SEQUENCE COMBO
	StaticText.new(window,Rect(stepSettingsXpos, stepSettingsYpos+170, 120, 20))
	.string_("Other Sequence:")
	.stringColor_(Color.black)
	.action_{|v| };

	otherSequenceCombo = PopUpMenu.new(window,Rect(stepSettingsXpos+110, stepSettingsYpos+170, 140, 20))
	.stringColor_(Color.black)
		.action_{|v| controller.setOtherSequence(v.value)};

	setOtherSequenceGlobalButton = Button.new(window,Rect(stepSettingsXpos+250, stepSettingsYpos+170, 20, 20))
				.states_([ [ "", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ],
						[ "", Color(1.0, 1.0, 1.0, 1.0), Color(0.0, 0.0, 1.0, 1.0) ] ])
				.action_{|v|controller.setUseOtherSequenceGlobally(v.value)};

	"setOtherSeqGlobalButton".postln;
	//OTHER TRIGGER ACTION COMBO
	StaticText.new(window,Rect(stepSettingsXpos, stepSettingsYpos+200, 120, 20))
	.string_("Other Action:")
	.stringColor_(Color.black);

	otherActionsCombo = PopUpMenu.new(window,Rect(stepSettingsXpos+95, stepSettingsYpos+200, 140, 20))
	.stringColor_(Color.black)
	.action_{|v| controller.setOtherAction(v.value)};

	otherActionsCombo.items = controller.otherActionsArray;

	setOtherActionGlobalButton = Button.new(window,Rect(stepSettingsXpos+235, stepSettingsYpos+200, 20, 20))
				.states_([ [ "", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ],
							[ "", Color(1.0, 1.0, 1.0, 1.0), Color(0.0, 0.0, 1.0, 1.0) ] ])
		.action_{|v| controller.setUseOtherActionGlobally(v.value)};

	otherActionValueBox = NumberBox.new(window,Rect(stepSettingsXpos+260, stepSettingsYpos+200, 30, 20))
		.action_{|v| controller.setOtherActionValue(v.value)};
	otherActionValueBox.visible=false;
	}

	createMoveToNextSectionEditors{
	//MOVE TO NEXT SECTION
	StaticText.new(window,Rect(stepSettingsXpos, stepSettingsYpos+230, 120, 20))
	.string_("Change Section:")
	.stringColor_(Color.black)
	.action_{|v| };

	moveToSectionButton = Button.new(window,Rect(stepSettingsXpos+120, stepSettingsYpos+230, 40, 20))
			.states_([ [ "Off", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ],
			[ "On", Color(1.0, 1.0, 1.0, 1.0), Color(0.0, 0.0, 1.0, 1.0) ] ])
		.action_{|v|	controller.setMoveToSection(v.value)};

	moveToSectionCombo = PopUpMenu.new(window,Rect(210, stepSettingsYpos+230, 120, 20))
	.stringColor_(Color.black)
		.action_{|v| controller.setMoveSection(v.value)};

	}


}