MidiSendEditor{

	var window;
	var controller;

	var midiKeys;

	var triggerChannel,triggerChanNumberBox,midiIndicator;

	//GUI positional vars
	var ypos;
	var xpos;

	*new{|initWindow,initController|
	^super.new.initMidiSendEditor(initWindow,initController);
	}

	initMidiSendEditor{|initWindow,initController|

		window = initWindow;
		controller = initController;

		this.setOldVariables;
	}

	setOldVariables{
		ypos = 220;
		xpos = 360;

	}

	createEditor{

		//Initiliases MIDI in
		/*Button.new(window,Rect(xpos, ypos, 60, 30))
		.states_([ [ "Init MIDI", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ]])
		.action_{|v| this.initMIDI };*/

		//SKIP STEP
		//Move sequence forward by skipping to next step
		/*Button.new(window,Rect(xpos+80, ypos+40, 60, 30))
		.states_([ [ "Skip Step", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ]])
		.action_{|v| this.skipStep };*/


		StaticText.new(window,Rect(xpos, ypos, 100, 20))
		.stringColor_(Color.black)
		.string_("Tigger Midi In:")
		.action_{|v| };

		midiIndicator = StaticText.new(window,Rect(xpos+140, ypos, 20, 20));
		midiIndicator.background=Color.black;

		triggerChanNumberBox = NumberBox.new(window,Rect(xpos+100, ypos, 30, 20))
		.action_{|v| controller.onSetTriggerChannel(v.value)};

		//RESET sequence
		Button.new(window,Rect(xpos+80, ypos+30, 60, 30))
		.states_([ [ "Reset", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ]])
		.action_{|v| controller.resetAll };

		//USE KEYBOARD AS MIDI CONTROLLER
		midiKeys = Button.new(window,Rect(xpos+160, ypos+30, 80, 30))
		.states_([ [ "KEYS OFF", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ],
			[ "KEYS ON", Color(1.0, 1.0, 1.0, 1.0), Color(0.0, 0.0, 1.0, 1.0) ] ])
		.action_{|v| if(v.value == 1,{
			midiKeys.keyDownAction =
			{
				arg view,char,modifiers,unicode,keycode;
				controller.onKeyDown(unicode)}
			},{
				midiKeys.keyDownAction = nil})
		};


		//Kill midi
		Button.new(window,Rect(xpos, ypos+30, 60, 30))
		.states_([ [ "Kill MIDI", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ]])
		.action_{|v| controller.killMidi };

	}



}