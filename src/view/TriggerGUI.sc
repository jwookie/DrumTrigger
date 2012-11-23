TriggerGUI{

	var > controller;
	var window;

	//constants
	var < numFixedSteps;
	var < numMatrixKeys;

	var < propertiesEditor;
	var < projectEditor;
	var < midiSendEditor;
	var < midiTracker;

	var < stepSwitches;
	var < matrixKeyArray;
	var < activeSteps;
	var < noteIndicators;

	var < popup;

	//GUI components
	var keysStartYpos;

	//temp
	var sectionList;
	var currentSection;
	var editableProperties;

	*new{|initController|
	^super.new.initTriggerGUI(initController);
	}

	initTriggerGUI{|initController|

		controller = initController;

		window = Window.new("Drum Trigger",Rect(300, 100, 700, 550)).front;
		window.onClose_{controller.onMainWindowClosed};
		window.alwaysOnTop = true;

		propertiesEditor = PropertiesEditor.new(window,controller);
		projectEditor = ProjectEditor.new(window,controller);
		midiSendEditor = MidiSendEditor.new(window,controller.midiController);
		midiTracker = MidiTracker.new(window,controller);

		keysStartYpos = 250;

		this.setConstants;

		this.createGUI;

	}

	setConstants{

		numFixedSteps = 16;
		numMatrixKeys = 24;

	}

	showPopup{|saveController,type|
		popup = FileLoadAndSavePopup.new(window,saveController,type);
	}

	createGUI{

		this.drawKeys(10,10);

		"create note buttons..".postln;
		this.createNoteButtons;

		"matrix editor created".postln;

		propertiesEditor.createEditor;

		"properties editor created".postln;
		projectEditor.createEditor;
		"project editor created".postln;
		midiSendEditor.createEditor;
		"midi send editor created".postln;

	}

	/*createCurrentStepInidicator{

		numFixedSteps

	}*/


	createNoteButtons{
		var keyMatrixXpos = 10;
		var ypos = keysStartYpos;

		matrixKeyArray = Array.fill(numFixedSteps,{Array.fill(numMatrixKeys,{0})});
		//buttons that load settings and allow editing of each step in sequence
		stepSwitches = Array.newClear(numFixedSteps);

		activeSteps = Array.fill(numFixedSteps,{0});
		noteIndicators = Array.fill(numFixedSteps,{0});

		numFixedSteps.do{arg i;
			var stepSwitch,activeStepButt,noteIndicator;

			numMatrixKeys.do{arg j;
				matrixKeyArray[i][j] = Button.new(window,
					Rect(keyMatrixXpos + ((i+1)*20) , ypos-((j+1)*10), 20, 10))
				.states_([ [ "-", Color(1.0, 0.0, 0.0, 0.5),
					Color(1.0, 0.0, 0.0, 0.5) ],
					[ "+", Color(1.0, 0.8, 0.1, 1.0),
						Color(0.3, 0.2, 0.1, 1.0) ] ])

				.action_{|v| var noteNum = j;
					//sectionList[currentSection].switchNoteOn(noteNum,i,v.value)
					controller.switchNoteOn(noteNum,i,v.value)
				}
			};

			//yellow activate step buttons
			activeStepButt = Button.new(window,Rect(keyMatrixXpos + ((i+1)*20) , ypos, 20, 10))
			.states_([ [ "", Color.clear, Color.clear ],
				[ "", Color.black, Color.black] ])
			.action_{|v| 	controller.activateStep(i,v.value)};

			activeSteps[i] = activeStepButt;

			/*noteIndicator = StaticText.new(window,Rect(keyMatrixXpos + ((i+1)*20), ypos + 10, 20, 5));
			noteIndicator.background=Color.white;
			noteIndicators[i] = noteIndicator;*/


			stepSwitch = Button.new(window,Rect(keyMatrixXpos + ((i+1)*20) , ypos+15 , 20, 10))
			.states_([ [ "", Color(1.0, 0.0, 0.0, 1.0),
				Color(1.0, 0.0, 0.0, 1.0) ],
				[ "", Color(1.0, 1.0, 1.0, 1.0),
					Color(0.0, 0.0, 1.0, 1.0) ] ])
			.action_{|v| 	var stepIndex = i;
				controller.switchSteps(stepIndex)};

			stepSwitches[i] = stepSwitch;

		};

	}




	drawKeys{|xpos,ypos|
	"drawing keys..".postln;
	 window.drawFunc = {

		/*Pen.color = Color.black;
 		Pen.addRect(Rect(0, 0, 700, 550));
 		Pen.perform(\fill);*/

 		Pen.color = Color.white;
 		Pen.addRect(Rect(xpos,0+ypos , 20, 10));
 		Pen.addRect(Rect(xpos,20 +ypos, 20, 10));
 		Pen.addRect(Rect(xpos,40+ypos , 20, 10));
 		Pen.addRect(Rect(xpos,60+ypos , 20, 10));
 		Pen.addRect(Rect(xpos,70+ypos , 20, 10));
 		Pen.addRect(Rect(xpos,90 +ypos, 20, 10));
 		Pen.addRect(Rect(xpos,110+ypos , 20, 10));

 		Pen.addRect(Rect(xpos,120+ypos , 20, 10));
 		Pen.addRect(Rect(xpos,140 +ypos, 20, 10));
 		Pen.addRect(Rect(xpos,160+ypos , 20, 10));
 		Pen.addRect(Rect(xpos,180+ypos , 20, 10));
 		Pen.addRect(Rect(xpos,190+ypos , 20, 10));
 		Pen.addRect(Rect(xpos,210 +ypos, 20, 10));
 		Pen.addRect(Rect(xpos,230+ypos , 20, 10));

 		Pen.perform(\fill);

 		Pen.strokeColor = Color.black;
 		Pen.addRect(Rect(xpos,0+ypos , 20, 10));
 		Pen.addRect(Rect(xpos,20 +ypos, 20, 10));
 		Pen.addRect(Rect(xpos,40+ypos , 20, 10));
 		Pen.addRect(Rect(xpos,60+ypos , 20, 10));
 		Pen.addRect(Rect(xpos,70+ypos , 20, 10));
 		Pen.addRect(Rect(xpos,90 +ypos, 20, 10));
 		Pen.addRect(Rect(xpos,110+ypos , 20, 10));

 		Pen.addRect(Rect(xpos,120+ypos , 20, 10));
 		Pen.addRect(Rect(xpos,140 +ypos, 20, 10));
 		Pen.addRect(Rect(xpos,160+ypos , 20, 10));
 		Pen.addRect(Rect(xpos,180+ypos , 20, 10));
 		Pen.addRect(Rect(xpos,190+ypos , 20, 10));
 		Pen.addRect(Rect(xpos,210 +ypos, 20, 10));
 		Pen.addRect(Rect(xpos,230+ypos , 20, 10));

 		Pen.stroke;

 		Pen.color = Color.black;
 		Pen.addRect(Rect(xpos,10+ypos , 20, 10));
 		Pen.addRect(Rect(xpos,30+ypos , 20, 10));
 		Pen.addRect(Rect(xpos,50+ypos , 20, 10));
 		Pen.addRect(Rect(xpos,80 +ypos, 20, 10));
 		Pen.addRect(Rect(xpos,100 +ypos, 20, 10));

 		Pen.addRect(Rect(xpos,130+ypos , 20, 10));
 		Pen.addRect(Rect(xpos,150+ypos , 20, 10));
 		Pen.addRect(Rect(xpos,170+ypos , 20, 10));
 		Pen.addRect(Rect(xpos,200 +ypos, 20, 10));
 		Pen.addRect(Rect(xpos,220 +ypos, 20, 10));
		Pen.perform(\fill);


 	};


 }

}