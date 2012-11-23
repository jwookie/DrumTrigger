FileLoadAndSavePopup{

	var mainWindow;
	var popupWindow;
	var controller;
	var popupType;

	var saveSongButton,filenameText,loadSongButton;

	*new{|window,controller,type|
	^super.new.initSavePopup(window,controller,type);
	}

	initSavePopup{|initWindow,initController,initType|
		var xpos,ypos;

		mainWindow = initWindow;
		controller = initController;
		popupType = initType;

		//Rect Point
		xpos = mainWindow.bounds.leftTop.x +((mainWindow.bounds.width - 200)*0.5);
		ypos = mainWindow.bounds.leftTop.y + 300;

		popupWindow = Window.new(popupType + "Song",Rect(xpos, ypos, 200, 150)).front;
		popupWindow.alwaysOnTop = true;
		//popupWindow = Window.new("Save Song",point.x +(width - 200)*0.5,point.y+100,200,150).front;

		filenameText = TextField.new(popupWindow,Rect(25, 20, 150, 20));

		if(popupType == "Save",{

			saveSongButton = Button.new(popupWindow,Rect(50, 50, 100, 30))
			.states_([ [ "Save Song", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ]])
			.action_{|v| controller.onSaveSong(filenameText.value); this.closePopup };
			},{

			loadSongButton = Button.new(popupWindow,Rect(50, 50, 100, 30))
			.states_([ [ "Load Song", Color(0.0, 0.0, 0.0, 1.0), Color(1.0, 0.0, 0.0, 1.0) ]])
			.action_{|v| controller.onLoadSong(filenameText.value);this.closePopup;
			}
		});

	}

	closePopup{

		popupWindow.close;

	}
}