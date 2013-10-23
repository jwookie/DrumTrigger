MidiChannel{

	var <> noteList;
	
	*new{
		^super.new.initMidiChannel();
	}

	initMidiChannel{
		noteList = List.new(0);
	}

	switchOn{|noteNum|
		var i = 0;
		var found =false;
		while{(found == false) && (i < noteList.size)}
		{
			if(noteList[i] == noteNum,{
				found=true;
			});
			i = i+1;
		};
		if(found == false,{
			noteList.add(noteNum);
		})
	}

	switchOff{|noteNum|
		var i = 0;
		var removed =false;
		while{(removed == false) && (i < noteList.size)}
		{
			if(noteList[i] == noteNum,{
				noteList.removeAt(i);
				removed=true;
			});
			i = i+1;
		}
	}

}