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
		//logger.debug("addTrackers "+section.sequenceList.size+' : '+trackers.size);
		section.sequenceList.do{arg val, i;
			var newTracker;
			if(i < trackers.size,{
				trackers[i].setTitle(val.sequenceName);
				newTracker = trackers[i];
				},{
					newTracker = Tracker.new(scrollView,val,5,5 + (i*40));
					trackers.add(newTracker);
			});
			newTracker.setStepTracker(val.currentStep);
			val.tracker = newTracker;
		};

		while{trackers.size != section.sequenceList.size}{
			removeTracker = trackers.pop;
			removeTracker.remove;
		};

		background.bounds = Rect(0,0,260,(trackers.size * 40)+10);
	}

	updateTracker{|sequence|
		//logger.debug(["UPDATE TRACKER",sequence.sequenceName,sequence.tracker,trackers,trackers.size]);
		sequence.tracker.setStepTracker(sequence.currentStep);
	}

}