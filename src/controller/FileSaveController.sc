FileSaveController{

	var trace;

	var > model;
	var controller;
	var > saveAs;

	*new{|mainController|
		^super.new.initFileSaveController(mainController);
	}

	initFileSaveController{|mainController|
		trace = Trace.new("FileSaveController");
		controller = mainController;
		saveAs = true;
	}

	onSaveSong{|name|
		var file, filename, xml, dataRoot, sections,seed;
		if(name == "",{
			^false;
		});
		trace.debug("Saving" + name ++ "...");
		filename = BumTrigger.static_XML_FOLDER_PATH ++ name ++ ".xml";
		filename.postln;

		xml = DOMDocument.new;
		dataRoot = xml.createElement("data");
		xml.appendChild(dataRoot);

		seed = xml.createElement("seed");
		xml.appendChild(seed);
		this.addCData(xml,seed,"value",model.seedNumber);

		sections = model.sectionList;
		trace.debug(model.sectionList);
		sections.do({arg section;
			var sectionRoot,sequencesRoot;
			sectionRoot = xml.createElement("section");
			dataRoot.appendChild(sectionRoot);

			//section properties
			this.addCData(xml,sectionRoot,"sectionName",section.sectionName);
			//this.addCData(xml,sectionRoot,"sectionIndex",section.sectionIndex);

			//sequences in section
			sequencesRoot = xml.createElement("sequences");
			sectionRoot.appendChild(sequencesRoot);
			section.sequenceList.do({arg sequence;
				var sequenceRoot,stepsRoot,activeStepRoot;
				sequenceRoot = xml.createElement("sequence");
				sequencesRoot.appendChild(sequenceRoot);

				//add sequence properties
				this.addCData(xml,sequenceRoot,"sequenceName",sequence.sequenceName);
				this.addCData(xml,sequenceRoot,"sequenceDescription",sequence.description);
				this.addCData(xml,sequenceRoot,"id",sequence.id);
				this.addCData(xml,sequenceRoot,"midiTriggerNote",sequence.midiTriggerNote);
				this.addCData(xml,sequenceRoot,"midiSendChan",sequence.midiSendChan);
				this.addCData(xml,sequenceRoot,"misfireTime",sequence.misfireTime);

				//active steps
				activeStepRoot = xml.createElement("activeSteps");
				sequenceRoot.appendChild(activeStepRoot);
				sequence.activeSteps.do({arg val;
					this.addCData(xml,activeStepRoot,"value",val);
				});

				//add sequence steps
				stepsRoot = xml.createElement("steps");
				sequenceRoot.appendChild(stepsRoot);
				sequence.stepArray.do({arg step;
					var stepRoot,notesRoot;
					stepRoot = xml.createElement("step");
					stepsRoot.appendChild(stepRoot);

					//create properties
					this.addCData(xml,stepRoot,"octave",step.octave);
					this.addCData(xml,stepRoot,"useGlobalOctave",step.octave);
					this.addCData(xml,stepRoot,"bypassSequence",step.bypassSequence);
					this.addCData(xml,stepRoot,"noteLength",step.noteLength);
					this.addCData(xml,stepRoot,"moveSequence",step.moveSequence);
					this.addCData(xml,stepRoot,"numTrigHits",step.numTrigHits);
					this.addCData(xml,stepRoot,"randomNote",step.randomNote);
					this.addCData(xml,stepRoot,"useGlobalRandom",step.useGlobalRandom);
					this.addCData(xml,stepRoot,"triggerOtherSequence",step.triggerOtherSequence);
					this.addCData(xml,stepRoot,"triggerOtherSequenceGlobally",step.triggerOtherSequenceGlobally);
					this.addCData(xml,stepRoot,"otherSequenceId",step.otherSequenceId);
					this.addCData(xml,stepRoot,"useOtherSequenceGlobally",step.useOtherSequenceGlobally);
					this.addCData(xml,stepRoot,"otherActionIndex",step.otherActionIndex);
					this.addCData(xml,stepRoot,"setOtherActionGlobally",step.setOtherActionGlobally);
					this.addCData(xml,stepRoot,"moveToSection",step.moveToSection);
					this.addCData(xml,stepRoot,"moveSectionId",step.moveSectionId);
					this.addCData(xml,stepRoot,"otherActionValue",step.otherActionValue);
					this.addCData(xml,stepRoot,"otherActionAmount",step.otherActionAmount);

					//now create note settings
					notesRoot = xml.createElement("notes");
					stepRoot.appendChild(notesRoot);
					step.noteArray.do({arg val;
						this.addCData(xml,notesRoot,"noteValue",val);
					});
				});
			});
		});

		trace.debug("create file");
		//now save file
		file = File(filename, "w");
		trace.debug("write file "+xml);
		xml.write(file); // output to file with default formatting
		file.close;
		trace.debug("DONE");

		controller.setSongTitle(name);

		(name + "Saved").postln;

		this.updateSavedSongList(name)
	}

	updateSavedSongList{|name|
		var listFile,saveFilename,files,xml,data,fileXml,newNode;

		//"Updating saved song list..".postln;
		saveFilename = BumTrigger.static_XML_FOLDER_PATH ++ BumTrigger.static_XML_FILE_LIST ++ ".xml";

		xml = DOMDocument.new(saveFilename);

		data = xml.getDocumentElement.getElement("data");

		files = data.getFirstChild;

		fileXml = files.getFirstChild;
		while({fileXml != nil},{
			if(fileXml.getFirstChild.getNodeValue == name,{
				newNode = fileXml;
				files.removeChild(fileXml);
				fileXml = nil;
			},{
				fileXml = fileXml.getNextSibling;
			});
		});

		if(newNode == nil,{
			newNode = this.createCData(xml,"file",name);
			//this.addCData(xml,files,"file",name);
		});

		('NEW NODE ' + newNode).postln;

		files.insertBefore(newNode,files.getFirstChild);

		//now save file
		listFile = File(saveFilename, "w");
		xml.write(listFile); // output to file with default formatting
		listFile.close;
	}

	getFileList{
		var list,saveFilename,file,test,xml,data,newNode;

		saveFilename = BumTrigger.static_XML_FOLDER_PATH ++ BumTrigger.static_XML_FILE_LIST ++ ".xml";

		xml = DOMDocument.new(saveFilename);

		file = xml.getDocumentElement.getElement("data").getElement("files").getFirstChild;

		list = List.new(0);
		list.add("");//add blank space at top of list
		while ( { file != nil } , {
			list.add(file.getFirstChild.getNodeValue);
			file = file.getNextSibling;
		});

		list.postln;

		^list;
	}

	onLoadSong{|name|
		var filename,xml,data,sectionXml, sectionList,sectionNames,test,seed;
		("Loading" + name ++ "...").postln;

		//retrieve file and create xml
		filename = BumTrigger.static_XML_FOLDER_PATH ++ name ++ ".xml";
		xml = DOMDocument.new(filename);
		data = xml.getDocumentElement.getElement("data");

		seed = data.getNextSibling;
		if(seed != nil,{
			model.setSeedNumber(this.getIntValue(seed,"value"));
		});
		trace.debug(["SEED",seed,"VAL",model.seedNumber]);


		//init new section list and names
		sectionList = List.new(0);
		sectionNames = List.new(0);

		//grab first section node
		sectionXml = data.getFirstChild;

		while ( { sectionXml != nil } , {
			var newSection,sequenceXml;
			newSection = Section.new;
			newSection.sectionName = sectionXml.getElement("sectionName").getFirstChild.getNodeValue.postln;
			//newSection.sectionIndex = this.getIntValue(sectionXml,"sectionIndex");

			sequenceXml = sectionXml.getElement("sequences").getFirstChild;
			while ( { sequenceXml != nil } , {

				var newSequence,stepXml,stepCtr,activeCtr;
				newSequence = Sequence.new;
				newSequence.sequenceName = sequenceXml.getElement("sequenceName").getFirstChild.getNodeValue;
				trace.debug("Sequence:" + newSequence.sequenceName);
				newSequence.description = this.getTextValue(sequenceXml,"sequenceDescription");
				newSequence.id = this.getIntValue(sequenceXml,"id");
				newSequence.midiTriggerNote = this.getIntValue(sequenceXml,"midiTriggerNote");
				newSequence.midiSendChan = this.getIntValue(sequenceXml,"midiSendChan");
				newSequence.misfireTime = this.getFloatValue(sequenceXml,"misfireTime");

				//set active steps
				//"LOAD ACTIVE STEPS".postln;
				activeCtr = 0;
				sequenceXml.getElement("activeSteps").do({arg val;
					var stepValue;
					if(val.getNodeName == "value",{
						stepValue = this.getIntValue(val,"value");
						newSequence.activeSteps[activeCtr] = stepValue;
						activeCtr = activeCtr + 1;
					});
				});

				//load in steps
				//"LOAD STEPS ".postln;
				stepXml = sequenceXml.getElement("steps").getFirstChild;
				stepCtr = 0;
				while ( { stepXml != nil } , {
					var step,noteCtr;
					step = Step.new;

					step.octave = this.getIntValue(stepXml,"octave");
					step.useGlobalOctave = this.getIntValue(stepXml,"useGlobalOctave");
					step.bypassSequence = this.getIntValue(stepXml,"bypassSequence");
					step.noteLength = this.getFloatValue(stepXml,"noteLength");
					step.moveSequence = this.getIntValue(stepXml,"moveSequence");
					step.numTrigHits = this.getIntValue(stepXml,"numTrigHits");
					step.randomNote = this.getIntValue(stepXml,"randomNote");
					step.useGlobalRandom = this.getIntValue(stepXml,"useGlobalRandom");
					step.triggerOtherSequence = this.getIntValue(stepXml,"triggerOtherSequence");
					step.triggerOtherSequenceGlobally = this.getIntValue(stepXml,"triggerOtherSequenceGlobally");
					step.otherSequenceId = this.getIntValue(stepXml,"otherSequenceId");

					step.setOtherActionGlobally = this.getIntValue(stepXml,"setOtherActionGlobally");
					step.otherActionIndex = this.getIntValue(stepXml,"otherActionIndex");
					step.setOtherActionGlobally = this.getIntValue(stepXml,"setOtherActionGlobally");
					step.moveToSection = this.getIntValue(stepXml,"moveToSection");
					step.moveSectionId = this.getIntValue(stepXml,"moveSectionId");
					step.otherActionValue = this.getFloatValue(stepXml,"otherActionValue");
					step.otherActionAmount = this.getFloatValue(stepXml,"otherActionAmount");

					//"LOAD NOTES".postln;
					noteCtr = 0;
					stepXml.getElement("notes").do({arg val;
						var noteValue;
						if(val.getNodeName == "noteValue",{
							noteValue = this.getIntValue(val,"noteValue");
							step.noteArray[noteCtr] = noteValue;
							noteCtr = noteCtr + 1;
						});
					});

					newSequence.stepArray[stepCtr] = step;
					stepCtr = stepCtr + 1;
					stepXml = stepXml.getNextSibling;
					//("NEXT STEP "++stepXml).postln;

				});
				newSection.sequenceList.add(newSequence);
				newSection.sequenceNames.add(newSequence.sequenceName);
				sequenceXml = sequenceXml.getNextSibling;
				//("NEXT SEQUENCE "++sequenceXml).postln;
			});

			sectionList.add(newSection);
			sectionNames.add(newSection.sectionName);
			sectionXml = sectionXml.getNextSibling;
			//("NEXT SECTION "++sectionXml).postln;
		});

		//now pass new data back to the main controller
		controller.onLoadedDataReady(sectionList,sectionNames);
		controller.setSongTitle(name);

		(name + "Loaded").postln;

		this.updateSavedSongList(name);

	}

	addCData{|xml,root,name,val|
		root.appendChild(this.createCData(xml,name,val));
	}

	createCData{|xml,name,val|
		if(val == nil,{val = "NIL_VALUE"});
		^xml.createElement(name).appendChild(xml.createCDATASection(val));
	}

	getTextValue{|xml,name|
		if(xml.getElement(name) == nil,{
			^"";
		},{
			^xml.getElement(name).getFirstChild.getNodeValue;
		});
	}

	getIntValue{|xml,name|

		if(xml.getElement(name) == nil,{
			//trace.debug(name + "is NIL ! ");
			^0;
			},{
			//trace.debug(name + "is : "+xml.getElement(name).getFirstChild.getNodeValue);
			^xml.getElement(name).getFirstChild.getNodeValue.asInteger;
		});
	}


	getFloatValue{|xml,name|
		if(xml.getElement(name) == nil,{
			^0.0;
			},{
			^xml.getElement(name).getFirstChild.getNodeValue.asFloat;
		});
	}

}