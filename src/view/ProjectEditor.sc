ProjectEditor{

	var window;
	var controller;

	//GUI components
	var < songTitle;

	var < sectionTitle;
	var < sectionNameText;
	var < sectionCombo;

	var < sequenceTitle;
	var < sequenceNameText;
	var < sequenceCombo;


	//GUI positional vars
	var ypos;
	var xpos;


	*new{|initWindow,initController|
	^super.new.initProjectEditor(initWindow,initController);
	}

	initProjectEditor{|initWindow,initController|

		window = initWindow;
		controller = initController;//.projectController;
		this.setOldVariables;
	}

	createEditor{

		this.createSongEditors;
		this.createSectionEditors;
		this.createSequenceEditors;

	}

	setOldVariables{
		ypos = 80;
		xpos = 360;

	}

	createSongEditors{

		songTitle = StaticText.new(window,Rect(xpos, 10, 160, 30));
		songTitle.align = \center;
		songTitle.font = Font("Monaco", 14);
		songTitle.string = "New Song";
		songTitle.background = Color.blue;
		songTitle.stringColor_(Color.white);

		Button.new(window,Rect(xpos+170, 10, 70, 30))
		.states_([ [ "Save Song", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ]])
		.action_{|v| controller.onSaveSong };

		Button.new(window,Rect(xpos+250, 10, 70, 30))
		.states_([ [ "Load Song", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ]])
		.action_{|v| controller.onLoadSong};
	}

	createSectionEditors{

		//stuff for new sections
		sectionCombo = PopUpMenu.new(window,Rect(xpos+210, ypos, 110, 20)).stringColor_(Color.black);
		sectionCombo.action_{|v| controller.changeSection(v.value,sectionCombo.item)};
		//static title
		sectionTitle = StaticText.new(window,Rect(xpos, ypos - 30, 320, 20));
		sectionTitle.background=Color.magenta;
		sectionTitle.align = \center;
		sectionTitle.font = Font("Monaco", 12);
		sectionTitle.string = "TEST";
		//new section text
		sectionNameText = TextField.new(window,Rect(xpos+105, ypos, 100, 20))
		.action_{|v| sectionNameText.stringColor_(Color.black)};
		//sectionNameText.string = "add section";
		//sectionNameText.stringColor_(Color.grey);
		//add new section button
		Button.new(window,Rect(xpos+60, ypos, 40, 20))
		.states_([ [ "Add", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ]])
		.action_{|v| controller.addNewSection(sectionNameText.value);sectionNameText.string = ""};
		//rename section button
		Button.new(window,Rect(xpos, ypos, 60, 20))
		.states_([ [ "Rename", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ]])
		.action_{|v| controller.renameSection(sectionNameText.value);sectionNameText.string = ""};
		//remove section button
		Button.new(window,Rect(xpos+325, ypos, 60, 20))
		.states_([ [ "Remove", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ]])
		.action_{|v| controller.removeSection()};

	}

	createSequenceEditors{

		//combo box holding different trigger settings, used to switch between sequences for different triggers
		sequenceCombo = PopUpMenu.new(window,Rect(xpos+210, ypos+60, 110, 20))
		.stringColor_(Color.black)
		.action_{|v| controller.changeSequence(v.value)};

		sequenceTitle = StaticText.new(window,Rect(xpos, ypos + 30, 320, 20));
		sequenceTitle.background=Color.blue;
		sequenceTitle.align = \center;
		sequenceTitle.font = Font("Monaco", 12);
		sequenceTitle.string = "TEST";
		sequenceTitle.stringColor = Color.white;

		sequenceNameText = TextField.new(window,Rect(xpos+105, ypos+60, 100, 20))
		.action_{|v| "HERE".postln;sequenceNameText.stringColor_(Color.black)};
		//sequenceNameText.string = "add sequence";
		//sequenceNameText.stringColor_(Color.grey);
		//ADD NEW SEQUENCE
		Button.new(window,Rect(xpos+60, ypos+60, 40, 20))
		.states_([ [ "Add", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ]])
		.action_{|v| controller.addNewSequence(sequenceNameText.value); sequenceNameText.string = ""};
		//RENAME SEQUENCE
		Button.new(window,Rect(xpos, ypos+60, 60, 20))
		.states_([ [ "Rename", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ]])
		.action_{|v| controller.renameSequence(sequenceNameText.value); sequenceNameText.string = ""};
		//REMOVE SEQUENCE
		Button.new(window,Rect(xpos+325, ypos+60, 60, 20))
		.states_([ [ "Remove", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ]])
		.action_{|v| controller.removeSequence()};

	}


}