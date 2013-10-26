Section{

	var trace;

	var <> sequenceList;
	var <> sequenceNames;
	var <> sectionName;

	var <> id;

	var < currentSequence;

	*new{|name,index,s|
		^super.new.initSection(name,index);
		//sectionName = name;
	}

	initSection{|name,index|
		trace = Trace.new("Section");

		sequenceList = List.new(0);
		sequenceNames = List.new(0);

		sectionName = name;

		currentSequence = 0;
	}


	trackMidiIn{|src,num|

		sequenceList.do{arg val;

			if(num == val.globalMidiIn,{val.onMidiIn(num)});


		}
	}
}