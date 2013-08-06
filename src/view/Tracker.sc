Tracker{

	//var > model;
	var window;
	var controller;
	var < title;

	var startX;
	var startY;
	var currentStep;

	var < stepTrackers;

	*new{|initWindow,sequence,initStartX,initStartY|
		^super.new.initTracker(initWindow,sequence,initStartX,initStartY);

	}

	initTracker{|initWindow,sequence,initStartX,initStartY|

		window = initWindow;
		startX = initStartX;
		startY = initStartY;

		currentStep=0;

		title = StaticText.new(window,Rect(startX, startY, 235, 15));
		title.background=Color.blue;
		title.align = \center;
		title.font = Font("Monaco", 12);
		title.string = sequence.sequenceName;
		title.stringColor = Color.white;

		stepTrackers = Array.fill(Sequence.static_NUM_STEPS,{0});

		Sequence.static_NUM_STEPS.do{arg i;
			var tracker;
			tracker = StaticText.new(window,Rect(startX+(i*15), startY+20, 10, 10));
			tracker.background=Color.white;

			stepTrackers[i] = tracker;
		}

	}

	remove{
		title.remove;
		stepTrackers.do{arg val;
			val.remove;
		}
	}

	setTitle{|name|
		title.string = name;
	}

	setStepTracker{|stepNum|
		//["setStepTracker",stepNum,currentStep].postln;
		{stepTrackers[currentStep].background=Color.white;
		currentStep = stepNum;
		stepTrackers[currentStep].background=Color.red}.defer;
	}

}