Section{

	var <> sequenceList;
	var <> sequenceNames;
	var <> sectionName;
	var <> sectionIndex;

	var < currentSequence;

	*new{|name,index,s|
		^super.new.initSection(name,index);
		//sectionName = name;
	}

	initSection{|name,index|

		sequenceList = List.new(0);
		sequenceNames = List.new(0);

		sectionName = name;
		sectionIndex = index;

		currentSequence = 0;
	}


	trackMidiIn{|src,num|

		sequenceList.do{arg val;

			if(num == val.globalMidiIn,{val.onMidiIn(num)});


		}
	}
}