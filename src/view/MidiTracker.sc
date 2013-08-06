MidiTracker{

	var logger;

	var window;
	var controller;

	var < trackers;

	var startX;
	var startY;

	var background;
	var scrollView;

	*new{|initWindow,initController|
		^super.new.initMidiTracker(initWindow,initController);

	}

	initMidiTracker{|initWindow,initController|

		logger = Logger.new("MidiTracker");
		window = initWindow;
		controller = initController;

		trackers = List.new(0);

		startX = 360;
		startY = 285;

		scrollView = ScrollView.new(window,Rect(startX, startY, 330, 255));

		background = StaticText.new(scrollView,Rect(0, 0, 320, 10));
		background.background=Color.new255(190, 190, 190);

	}

	addTrackers{|section|
		var removeTracker;
		section.sequenceList.do{arg val, i;
			var newTracker;
			if(i < trackers.size,{
				trackers[i].setTitle(val.sequenceName);
				trackers[i].setStepTracker(val.currentStep);
				},{
					newTracker = Tracker.new(scrollView,val,5,5 + (i*40));
					newTracker.setStepTracker(val.currentStep);
					trackers.add(newTracker);
			});
			val.tracker = newTracker;

		};

		while{trackers.size != section.sequenceList.size}{
			removeTracker = trackers.pop;
			removeTracker.remove;
		};

		background.bounds = Rect(0,0,260,(trackers.size * 40)+10);
	}

	updateTracker{|sequence|
		logger.debug(["UPDATE TRACKER",sequence.sequenceIndex,trackers,trackers.size]);
		/*trackers.do{arg val;
			if(val.title.string == sequence.sequenceName,{
				tracker = val;
			});
		};*/
		sequence.tracker.setStepTracker(sequence.currentStep);
		//trackers[sequence.sequenceIndex].setStepTracker(sequence.currentStep);
	}

}