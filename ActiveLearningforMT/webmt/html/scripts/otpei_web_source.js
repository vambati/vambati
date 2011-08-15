	//creating a namespace
	YAHOO.namespace("otpei.container");

	//regular expression for splitting the span tag that is supplied
	var re=/<.*?>/;

	//flags for the basic panel
	var cl_flag=0;
	var cl_flag1=0;


	var Dom = YAHOO.util.Dom;
	var Event = YAHOO.util.Event; 
	var DDM = YAHOO.util.DragDropMgr;
	var Sel = YAHOO.util.Selector;
	//the variables required from yui

	//the variables that contain the data information
	var engstr;
	var hindistr;

	//the variables that are required for basic panel for storing the original color and the html object
	var bgcolor;
	var curhtml;

	//variables for mapping table temporary
	var mappings={};

	//variables that are required and part of main panel
	var engtag;
	var hinditag;
	var transpanel;
	var mappanel;
	var translatemenu;

	var wordlist = new Array();
	var sentlen;
	var items = {};
	var mappings = {};
	var count = 0;
	var flag = 0;
	var draglist;

	var itc_flag = 0;
	var	wm_flag = 0;
	var	nt_flag = 0;

	//function that creates the basic and mainpanels
	function init()
	{
		YAHOO.otpei.container.bpanel = new YAHOO.widget.Panel("bpanel", { zIndex:3, width:"500px",visible:false, constraintoviewport:true, close:true, draggable:false} );
		YAHOO.otpei.container.bpanel.setHeader('<div class="head_text">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Original English Text:</div><div class="knob"></div>');
		YAHOO.otpei.container.bpanel.setBody("&nbsp;&nbsp;&nbsp;"+"welcome to OTPEI");
		YAHOO.otpei.container.bpanel.setFooter('<br><div>&nbsp;&nbsp;&nbsp;&nbsp;</div><button id="inv_mpanel"class="button_text">Contribute a better translation</button>');
		YAHOO.otpei.container.bpanel.render(document.body);

		YAHOO.util.Event.addListener('bpanel', 'mouseover', function() 
		{ 
			curhtml.style.backgroundColor="rgb(230, 236, 249)";
			cl_flag1=1; 
			YAHOO.otpei.container.bpanel.show(); 
		}, this, true);

		YAHOO.util.Event.addListener('bpanel', 'mouseout', function() 
		{ 
			cl_flag1=0; 
			curhtml.style.backgroundColor=bgcolor;
			YAHOO.otpei.container.bpanel.hide(); 

		}, this, true);

        YAHOO.otpei.container.mpanel = new YAHOO.widget.Panel("mpanel",  
                                                    {  
														y:10,
														x:100,
														width:"900px",
														height:"800px",
														close: true, 
														visible: false,
													    underlay:"shadow",   
														zindex:4,
														modal: true
                                                    } 
                                                );
		YAHOO.otpei.container.mpanel.setBody('<h1 style="font-size:large" align="center">Web Translation</h1><center><i>(English >> Hindi)</i></center><br><br><div id="outerdiv">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>English Sentence:</b>&nbsp;&nbsp;<span id="engsent"></span>&nbsp;&nbsp;<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>Translation:</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span id="hindisent"></span><br><br><div id="translatemenu">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input id="imptid" name="imptid" value="Improve Translation" type="button" onclick="onImproveTransClick()">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input id="mapid" name="mapid" value="Display Word Mappings" type="button" onclick="onMappingClick()">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input id="newtid" name="newtid" value="Enter New Translation" type="button" onclick="onNewTranslateClick()"></div><fieldset id="mappanel" style="visibility:hidden"></fieldset><fieldset id="transpanel" style="visibility:hidden"></fieldset>');
		YAHOO.util.Event.addListener("inv_mpanel", "click",mp_inv ,YAHOO.otpei.container.spanel, true);
		YAHOO.otpei.container.mpanel.cfg.subscribeToConfigEvent("visible", function (p_sType, p_aArgs) { 
	     
	        var oValue = p_aArgs[0]; 
			if(!oValue)
			{
				transpanel.innerHTML = "";
				Dom.setStyle(transpanel,"visibility","hidden");
				mappanel.innerHTML = "";
				Dom.setStyle(mappanel,"visibility","hidden");
			}
	     
	    }); 

	}

	var mp_inv=function()
	{
			YAHOO.otpei.container.mpanel.setBody('<h1 style="font-size:large" align="center">Web Translation</h1><center><i>(English >> Hindi)</i></center><br><br><div id="outerdiv">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>English Sentence:</b>&nbsp;&nbsp;<span id="engsent"></span>&nbsp;&nbsp;<br><br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>Translation:</b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<span id="hindisent"></span><br><br><div id="translatemenu">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input id="imptid" name="imptid" value="Improve Translation" type="button" onclick="onImproveTransClick()">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input id="mapid" name="mapid" value="Display Word Mappings" type="button" onclick="onMappingClick()">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input id="newtid" name="newtid" value="Enter New Translation" type="button" onclick="onNewTranslateClick()"></div><fieldset id="mappanel" style="visibility:hidden"></fieldset><fieldset id="transpanel" style="visibility:hidden"></fieldset>');

			//resetting the flags
			cl_flag=0;
			cl_flag1=0;
			//hiding the basic panel
			YAHOO.otpei.container.bpanel.hide();
			
			//adding the main panel to the body
			YAHOO.otpei.container.mpanel.render(document.body); 

			engtag = Dom.get('engsent');
			engtag.innerHTML=engstr;

			hinditag = Dom.get('hindisent');
			hinditag.innerHTML=hindistr;
			
			mappanel = Dom.get('mappanel');
			transpanel = Dom.get('transpanel');

			itc_flag = 0;
			wm_flag = 0;
			nt_flag = 0;
			
			wordlist = new Array();
			sentlen = "";
			items = {};
			count = 0;
			flag = 0;
			draglist = "";

			YAHOO.otpei.container.mpanel.show();
	};

	//the function that is invoked when the user tipons a otpei sentence
	function tipon(idval)
	{
		//temporary string required to handle the splitting part
		var tempstr="";
		var tempstr1="";
		var tempstr2="";
		var tempstr3="";
		var tempstr4="";
		mappings={};

		//to handle the highlight events
		if(curhtml != null)
		{
			curhtml.style.backgroundColor=bgcolor;
		}
		bgcolor=idval.style.backgroundColor;
		curhtml = idval;
		idval.style.backgroundColor="rgb(230, 236, 249)";

		//flag to handle the bpanel hide and show
		cl_flag=1;

		var words=document.getElementById(idval.id).innerHTML.split(re);
		var y;

		var wlen = words.length;
		var x;

		//old belief
		//document.getElementById(idval.id).offsetTop works in both ie and mozilla
		//document.getElementById(idval.id).offsetHeight works in both ie and mozilla
		//document.getElementById(idval.id).offsetTop+document.getElementById(idval.id).offsetHeight is enough for mozilla
		//parseInt(document.body.topMargin) works only in ie
		//document.getElementById(idval.id).offsetTop+parseInt(document.body.topMargin) works in ie

		if(words[0]=="")//mozilla specific code
		{
			//handling the words
			if(words[1]=="")
			{
				tempstr1=words[2];
				x = 3;
			}
			else
			{
				x = 2;
				tempstr1=words[1];
			}
			tempstr2 = tempstr1.split("$mapsent#");
			engstr=tempstr2[0];
			
			//the word mappings structure
			tempstr3 = tempstr2[1].split("$sep#");
			for (i=0;i<tempstr3.length;i++)
			{
				tempstr4=tempstr3[i].split("$map#")
				mappings[tempstr4[0]]=tempstr4[1];
			}

			for(i=x;i<wlen;i++)
			{
				if(words[i] != "")
				{
					tempstr = tempstr + words[i];
				}
			}
			hindistr = tempstr;		
			y=Dom.getY(idval)+document.getElementById(idval.id).offsetHeight;
		}
		else
		{
			if(words[0]=="")
			{
				tempstr1=words[1];
				x = 2;
			}
			else
			{
				tempstr1=words[0];
				x = 1;
			}
			tempstr2 = tempstr1.split("$mapsent#");
			engstr=tempstr2[0];

			tempstr3 = tempstr2[1].split("$sep#");
			for (i=0;i<tempstr3.length;i++)
			{
				tempstr4=tempstr3[i].split("$map#")
				mappings[tempstr4[0]]=tempstr4[1];
			}

			for(i=x;i<wlen;i++)
			{
				tempstr = tempstr + words[i];
			}
			hindistr = tempstr;
			y=document.getElementById(idval.id).offsetTop+document.getElementById(idval.id).offsetHeight+parseInt(document.body.topMargin);

		}

		YAHOO.otpei.container.bpanel.setBody("&nbsp;&nbsp;"+engstr);
		YAHOO.otpei.container.bpanel.cfg.setProperty("x", Dom.getX(idval));
		YAHOO.otpei.container.bpanel.cfg.setProperty("Y", y);
		YAHOO.otpei.container.bpanel.render(document.getElementById(idval));
		YAHOO.otpei.container.bpanel.show();
	}
	
	function tipoffsen()
	{
		cl_flag =0;
		window.setTimeout("tipoff()",1500);
	}
	
	function tipoff()
	{
		if(cl_flag==0 && cl_flag1==0)
		{
			curhtml.style.backgroundColor=bgcolor;
			YAHOO.otpei.container.bpanel.hide();
		}
	}

	function onImproveTransClick()
	{
		if(itc_flag == 0)
		{
			transpanel.innerHTML = "";
			itc_flag = 1;
			nt_flag = 0;

			var ele1,ele2,ele3,ele4,ele6,ele7,ele8,ele9,ele10,ele11,ele12,ele13,ele14,ele15,ele16;
			ele1 = transpanel;

			ele2 = document.createElement("legend");
			ele2.innerHTML = "Improve Translation";
			ele2.style.fontWeight = "bold";
			ele1.appendChild(ele2);

			ele3 = document.createElement("table");
			ele3.id = "imprtableid";
			ele1.appendChild(ele3);

			ele4 = ele3.insertRow(0);

			ele5 = ele4.insertCell(0);

			ele6 = document.createElement("div");
			ele6.className = "draglist";
			ele6.id="dl";
			ele6.name="dl";
			ele5.appendChild(ele6);
			ele5.appendChild(document.createTextNode('\u00A0'));
			ele5.appendChild(document.createTextNode('\u00A0'));
			ele5.appendChild(document.createTextNode('\u00A0'));

			ele17 = document.createElement("span");
			ele17.id = "inst";
			ele17.innerHTML = "<i>(Press <b>Ctrl</b> + <b>LeftMouseClick</b> for multiple selection)</i>";
			ele5.appendChild(ele17);

			ele7 = ele4.insertCell(1);

			ele8 = document.createElement("div");
			ele8.id= "newword";
			ele7.appendChild(ele8);
			ele7.appendChild(document.createElement("br"));
			ele7.appendChild(document.createElement("br"));

			ele9 = document.createElement("div");
			ele9.id="editdiv";
			ele7.appendChild(ele9);
			ele7.appendChild(document.createElement("br"));
			ele7.appendChild(document.createElement("br"));

			ele10 = document.createElement("div");
			ele10.id = "deldiv";
			ele7.appendChild(ele10);
			ele7.appendChild(document.createElement("br"));
			ele7.appendChild(document.createElement("br"));

			ele15 = document.createElement("div");
			ele15.id = "findiv";
			ele7.appendChild(ele15);
			ele7.appendChild(document.createElement("br"));
			ele7.appendChild(document.createElement("br"));

			ele11 = document.createElement("input");	
			ele11.type="text";
			ele11.id="textid";
			ele11.size="30";
			ele11.value="";
			ele8.appendChild(document.createTextNode('\u00A0'));
			ele8.appendChild(document.createTextNode('\u00A0'));
			ele8.appendChild(document.createTextNode('\u00A0'));
			ele8.appendChild(document.createTextNode('\u00A0'));
			ele8.appendChild(document.createTextNode('\u00A0'));
			ele8.appendChild(document.createTextNode('\u00A0'));
			ele8.appendChild(document.createTextNode('\u00A0'));
			ele8.appendChild(document.createTextNode('\u00A0'));
			ele8.appendChild(ele11);
			ele8.appendChild(document.createElement("br"));
			ele8.appendChild(document.createElement("br"));


			ele12 = document.createElement("input");
			ele12.type="button";
			ele12.id="insid";
			ele12.value="Insert Word/Phrase";
			ele12.onclick = function addWord() 
			{
				var nw = document.getElementById("textid");
				if(nw.value != "")
				{
					var new_word = wordlist.push(nw.value);
					var dli = document.createElement("div");
					dli.className = "editable";
					dli.id = "di1_"+new_word;
					dli.innerHTML = wordlist[new_word-1];
					draglist.appendChild(dli);
					new YAHOO.example.DDList("di1_" + new_word);
					sentlen = new_word;
					nw.value = "";
					editable.init();
				}
				else
				{
					alert("enter some word or phrase");
				}
			};
			ele8.appendChild(document.createTextNode('\u00A0'));
			ele8.appendChild(document.createTextNode('\u00A0'));
			ele8.appendChild(document.createTextNode('\u00A0'));
			ele8.appendChild(document.createTextNode('\u00A0'));
			ele8.appendChild(document.createTextNode('\u00A0'));
			ele8.appendChild(document.createTextNode('\u00A0'));
			ele8.appendChild(document.createTextNode('\u00A0'));
			ele8.appendChild(document.createTextNode('\u00A0'));
			ele8.appendChild(ele12);

			ele13 = document.createElement("input");
			ele13.type="button";
			ele13.id="editb";
			ele13.value="Merge Words/Phrases";
			ele13.onclick = function mergeEdit()
			{
				if(count!=0)
				{
					var draglist_items = draglist.getElementsByTagName("div");
					var merge_list = {};
					var tflag =0;
					var replacedtag;
					var mergestring = "";
					for (i=0;i<draglist_items.length;i=i+1) 
					{
						if(items[draglist_items[i].id])
						{
							tflag++;
							merge_list[draglist_items[i].id] = draglist_items[i];
						}
						if(tflag == 1)
						{
							var replacedtag = draglist_items[i];
						}
					}
					delete merge_list[replacedtag.id];
					for(var eachid in merge_list)
					{
						var deldiv = merge_list[eachid];
						mergestring = mergestring+" "+deldiv.innerHTML;
						draglist.removeChild(deldiv);
					}	
					replacedtag.innerHTML = replacedtag.innerHTML+mergestring;
					Dom.setStyle(replacedtag, 'color','black');
					items = {};
					count = 0;
				}
				else
				{
					alert('select some words');
				}
			};

			
			ele9.appendChild(document.createTextNode('\u00A0'));
			ele9.appendChild(document.createTextNode('\u00A0'));
			ele9.appendChild(document.createTextNode('\u00A0'));
			ele9.appendChild(document.createTextNode('\u00A0'));
			ele9.appendChild(document.createTextNode('\u00A0'));
			ele9.appendChild(document.createTextNode('\u00A0'));
			ele9.appendChild(document.createTextNode('\u00A0'));
			ele9.appendChild(document.createTextNode('\u00A0'));
			ele9.appendChild(ele13);

			ele14 = document.createElement("input");
			ele14.type="button";
			ele14.id="delb";
			ele14.value="Delete Word(s)";
			ele14.onclick = function delTag () 
			{
				if(count!=0)
				{
					for(var eachid in items)
					{
						var deldiv = items[eachid];
						draglist.removeChild(deldiv);
					}
					items = {};
					count = 0;
				}
				else
				{
					alert("select some words");
				}
			};
			ele10.appendChild(document.createTextNode('\u00A0'));
			ele10.appendChild(document.createTextNode('\u00A0'));
			ele10.appendChild(document.createTextNode('\u00A0'));
			ele10.appendChild(document.createTextNode('\u00A0'));
			ele10.appendChild(document.createTextNode('\u00A0'));
			ele10.appendChild(document.createTextNode('\u00A0'));
			ele10.appendChild(document.createTextNode('\u00A0'));
			ele10.appendChild(document.createTextNode('\u00A0'));
			ele10.appendChild(ele14);
			
			ele16 = document.createElement("input");
			ele16.type="button";
			ele16.id="finid";
			ele16.value="Finalize Translation";
			draglist = document.getElementById("dl");
			ele16.onclick = function finalizeTranslation () 
			{
				var draglist_items = draglist.getElementsByTagName("div");
				var finalstring = "";
				for(i=0;i<draglist_items.length;i=i+1) 
				{
					finalstring += draglist_items[i].innerHTML + " ";
				}
				var r=confirm("Do you want to finalize translation?");
				if(r == true)
				{
					var outerdiv = Dom.get('outerdiv');
					translatemenu = Dom.get('translatemenu');	
					mappanel = Dom.get('mappanel');
					transpanel = Dom.get('transpanel');

					engtag.readOnly=true;
					translatemenu.innerHTML = "";
					transpanel.innerHTML = "";
					mappanel.innerHTML = "";
	


					outerdiv.removeChild(translatemenu);
					outerdiv.removeChild(transpanel);
					outerdiv.removeChild(mappanel);
					
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					
					ele1 = document.createElement("span");
					ele1.id = "modifiedsent";
					ele1.innerHTML = "<b>Improved Translation:</b>&nbsp;&nbsp;&nbsp;&nbsp;"+finalstring;
					outerdiv.appendChild(ele1);

					outerdiv.appendChild(document.createElement("br"));
					outerdiv.appendChild(document.createElement("br"));
					outerdiv.appendChild(document.createElement("br"));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					
					ele2 = document.createElement("span");
					ele2.id = "thnxid";
					ele2.style.backgroundColor = "rgb(255, 255, 153)";
					ele2.innerHTML = "<b>Thank you for contributing your translation suggestion to Moses Translate.</b>";
					outerdiv.appendChild(ele2);

					outerdiv.appendChild(document.createElement("br"));
					outerdiv.appendChild(document.createElement("br"));
					outerdiv.appendChild(document.createElement("br"));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					var sUrl   = "/cgi-bin/otpei_database_con_web.cgi"
					var qstr = sUrl+'?eng=' + escape(engtag.innerHTML)+'&moses=' + escape(hinditag.innerHTML)+'&modify=' + escape(finalstring); 
					var request = YAHOO.util.Connect.asyncRequest('GET', qstr, callback1);	
				}
			};
			ele15.appendChild(document.createTextNode('\u00A0'));
			ele15.appendChild(document.createTextNode('\u00A0'));
			ele15.appendChild(document.createTextNode('\u00A0'));
			ele15.appendChild(document.createTextNode('\u00A0'));
			ele15.appendChild(document.createTextNode('\u00A0'));
			ele15.appendChild(document.createTextNode('\u00A0'));
			ele15.appendChild(document.createTextNode('\u00A0'));
			ele15.appendChild(document.createTextNode('\u00A0'));
			ele15.appendChild(ele16);

			wordlist = hindistr.split(" ");
			sentlen = wordlist.length;

			while( draglist.hasChildNodes() ) 
			{
				draglist.removeChild( draglist.lastChild );
			}

			for (i = 0; i < sentlen; i++) 
			{
				if(wordlist[i]!="")
				{
					dli = document.createElement("div");
					dli.className = "editable";
					dli.id = "di1_"+(i+1);
					dli.innerHTML = wordlist[i];
					draglist.appendChild(dli);
				}
			}

			Dom.setStyle(transpanel,"visibility","visible");

			YAHOO.example.DDApp.init();
			editable.init();
		}
		else
		{
			transpanel.innerHTML = "";
			wordlist = new Array();
			sentlen;
			items = {};
			count = 0;
			flag = 0;
			draglist = "";
			itc_flag = 0;
			Dom.setStyle(transpanel,"visibility","hidden");
		}
	}

	function onMappingClick()
	{
		if(wm_flag == 0)
		{
			mappanel.innerHTML = "";
			wm_flag = 1;

			var ele1,ele2,ele3,ele4,ele5,ele6,ele7;

			ele1 = mappanel;

			ele2 = document.createElement("legend");
			ele2.innerHTML = "Word Mappings";
			ele2.style.fontWeight = "bold";
			ele1.appendChild(ele2);

			ele6 = document.createElement("table");
			ele6.id = "outertable";
			ele6.width="50%";
			ele1.appendChild(ele6);

			ele7 = ele6.insertRow(0);
			ele8 = ele7.insertCell(0);

			ele3 = document.createElement("table");
			ele3.id = "maptableid";
			ele3.cellPadding="5";
			ele3.border="1";
			ele3.width="50%";

			ele4 = ele3.insertRow(0);
			ele5 = ele3.insertRow(1);

			var i = 0;

			for(var eachid in mappings)
			{
				var tempele = ele4.insertCell(i);
				tempele.innerHTML = eachid;
				tempele.align = "center";
				tempele.style.backgroundColor = "LightGrey";
				i = i+1;
			}
			
			i = 0;
			for(var eachid in mappings)
			{
				var tempele = ele5.insertCell(i);
				tempele.innerHTML = mappings[eachid];
				tempele.align = "center";
				tempele.style.backgroundColor = "Lavender";
				i = i+1;
			}

			ele8.appendChild(ele3);
			Dom.setStyle(mappanel,"visibility","visible");
		}
		else
		{
			mappanel.innerHTML = "";
			Dom.setStyle(mappanel,"visibility","hidden");
			wm_flag = 0;
		}
	}

	function onNewTranslateClick() 
	{
		if(nt_flag == 0)
		{
			transpanel.innerHTML = "";
			itc_flag = 0;
			nt_flag = 1;

			var ele1,ele2,ele3,ele4;

			ele1 = transpanel;

			ele2 = document.createElement("legend");
			ele2.innerHTML = "New Translation";
			ele2.style.fontWeight = "bold";
			ele1.appendChild(ele2);
			ele1.appendChild(document.createElement("br"));
			ele1.appendChild(document.createTextNode('\u00A0'));
			ele1.appendChild(document.createTextNode('\u00A0'));
			ele1.appendChild(document.createTextNode('\u00A0'));
			ele1.appendChild(document.createTextNode('\u00A0'));

			ele3 = document.createElement("textarea");
			ele3.id = "newtxtid";
			ele3.rows = "10";
			ele3.cols = "80";
			ele1.appendChild(ele3);
			ele1.appendChild(document.createElement("br"));
			ele1.appendChild(document.createElement("br"));
			ele1.appendChild(document.createTextNode('\u00A0'));
			ele1.appendChild(document.createTextNode('\u00A0'));
			ele1.appendChild(document.createTextNode('\u00A0'));
			ele1.appendChild(document.createTextNode('\u00A0'));
			ele1.appendChild(document.createTextNode('\u00A0'));
			ele1.appendChild(document.createTextNode('\u00A0'));

			ele4 = document.createElement("input");
			ele4.type="button";
			ele4.id="newtrid";
			ele4.value="Submit";
			ele4.onclick = function enterNewTranslation () 
			{
				var txtar = Dom.get('newtxtid');
				var finalstring = txtar.value;
				var r=confirm("Do you want to finalize translation?");

				if(r == true)
				{

					var outerdiv = Dom.get('outerdiv');

					translatemenu = Dom.get('translatemenu');	
					mappanel = Dom.get('mappanel');
					transpanel = Dom.get('transpanel');

					engtag.readOnly=true;
					translatemenu.innerHTML = "";
					transpanel.innerHTML = "";
					mappanel.innerHTML = "";
					
					outerdiv.removeChild(translatemenu);
					outerdiv.removeChild(transpanel);
					outerdiv.removeChild(mappanel);

					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));

					ele5 = document.createElement("span");
					ele5.id = "modifiedsent";
					ele5.innerHTML = "<b>Improved Translation:</b>&nbsp;&nbsp;&nbsp;&nbsp;"+finalstring;
					outerdiv.appendChild(ele5);

					outerdiv.appendChild(document.createElement("br"));
					outerdiv.appendChild(document.createElement("br"));
					outerdiv.appendChild(document.createElement("br"));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));

					ele6 = document.createElement("span");
					ele6.id = "thnxid";
					ele6.style.backgroundColor = "rgb(255, 255, 153)";
					ele6.innerHTML = "<b>Thank you for contributing your translation suggestion to Moses Translate.</b>";
					outerdiv.appendChild(ele6);

					outerdiv.appendChild(document.createElement("br"));
					outerdiv.appendChild(document.createElement("br"));
					outerdiv.appendChild(document.createElement("br"));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
					outerdiv.appendChild(document.createTextNode('\u00A0'));
var sUrl   = "/cgi-bin/otpei_database_con_web.cgi"
					var qstr = sUrl+'?eng=' + escape(engtag.innerHTML)+'&moses=' + escape(hinditag.innerHTML)+'&modify=' + escape(finalstring); 
					var request = YAHOO.util.Connect.asyncRequest('GET', qstr, callback1);	
				}
			}
			ele1.appendChild(ele4);
			ele1.appendChild(document.createElement("br"));
			ele1.appendChild(document.createElement("br"));

			Dom.setStyle(transpanel,"visibility","visible");
		}
		else
		{
			transpanel.innerHTML = "";
			Dom.setStyle(transpanel,"visibility","hidden");
			nt_flag = 0;
		}
	}

	YAHOO.example.DDApp = 
	{
		
		init: function() 
		{ 
			new YAHOO.util.DDTarget("dl");
			for (var j=1;j<=sentlen;j=j+1) 
			{ 
	                var dd = new YAHOO.example.DDList("di1_" + j);
     				dd.on('b4StartDragEvent', b4StartDrag);
	        }
	    }
	};

	var b4StartDrag = function() 
	{
		var clickEl = this.getEl();
		if(Dom.getFirstChild(clickEl))
		{
			var child = Dom.getFirstChild(clickEl);
			editable.check();
		}
		if(count!=0)
		{
			for (var eachele in items)
			{
				Dom.setStyle(items[eachele], 'color','black'); 
	            delete items[eachele];
				count = count-1;
			}
		}

	}

	YAHOO.example.DDList = function(id, sGroup, config) 
	{ 
		YAHOO.example.DDList.superclass.constructor.call(this, id, sGroup, config); 
		var el = this.getDragEl(); 
		Dom.setStyle(el, "opacity", 0.67); // The proxy is slightly transparent 
		this.goingLeft = false; 
		this.lastX = 0;
		this.lastY = 0;
		this.goingUp = false; 
	}; 

	YAHOO.extend(YAHOO.example.DDList, YAHOO.util.DDProxy, {  
	    startDrag: function(x, y) 
		{ 
	        // make the proxy look like the source element 
	        var dragEl = this.getDragEl(); 
	        var clickEl = this.getEl(); 
	        Dom.setStyle(clickEl, "visibility", "hidden"); 
	        dragEl.innerHTML = clickEl.innerHTML; 
	        Dom.setStyle(dragEl, "color", Dom.getStyle(clickEl, "color")); 
	        Dom.setStyle(dragEl, "backgroundColor", Dom.getStyle(clickEl, "backgroundColor")); 
	        Dom.setStyle(dragEl, "border", "2px solid gray"); 
	    },

		onMouseDown: function(e) 
		{ 
			var tar = Event.getTarget(e);
			var mergestr = "";
			var sortedArray = new Array();
			Event.stopEvent(e);
			if (e.ctrlKey) 
			{ 
				if (!items[tar.id]) 
				{
					if(count == 0)
					{
						Dom.setStyle(tar, 'color','red');
						items[tar.id] = tar;							
						count = count + 1;
					}
					else
					{
						flag = 0;
						for (var eachele in items)
						{
							if((Dom.getPreviousSibling(items[eachele]) == null) && (Dom.getNextSibling(items[eachele]) != null))
							{
								if(((Dom.getNextSibling(items[eachele])).id == tar.id))
								{
									Dom.setStyle(tar, 'color','red');
									items[tar.id] = tar;
									count = count + 1;
									flag = 1;
									break;
								}
							}
							else if((Dom.getNextSibling(items[eachele]) == null) && (Dom.getPreviousSibling(items[eachele]) != null))
							{
								if(((Dom.getPreviousSibling(items[eachele])).id == tar.id))
								{
									Dom.setStyle(tar, 'color','red');
									items[tar.id] = tar;
									count = count + 1;
									flag = 1;
									break;
								}
							}
							else
							{
								if(((Dom.getNextSibling(items[eachele])).id == tar.id) || ((Dom.getPreviousSibling(items[eachele])).id == tar.id))
								{
									Dom.setStyle(tar, 'color','red');
									items[tar.id] = tar;
									count = count + 1;
									flag = 1;
									break;
								}
							}
						}
						if(flag == 0)
						{
							for (var i in items) 
							{
								Dom.setStyle(items[i], 'color','black');
								delete items[i];
							}
							count = 0;
							Dom.setStyle(tar, 'color','red');
							items[tar.id] = tar;							
							count = count + 1;
						}
					}
				} 
				else 
				{				
					var search = tar;
					while(true)
					{
						if(Dom.getNextSibling(search) != null)
						{
							if(items[Dom.getNextSibling(search).id])
							{
									Dom.setStyle(search, 'color','black');
									delete items[search.id];
									search = items[Dom.getNextSibling(search).id];
									count = count-1;
							}
							else
							{
								Dom.setStyle(search, 'color','black'); 
								delete items[search.id];
								count = count -1;
								break;
							}
						}
						else
						{
							Dom.setStyle(search, 'color','black'); 
							delete items[search.id];
							count = count - 1;
							break;
						}
					}
				} 
			}
	    },

	    endDrag: function(e) 
		{ 
	        var srcEl = this.getEl(); 
	        var proxy = this.getDragEl(); 
	        Dom.setStyle(proxy, "visibility", ""); 
	        var a = new YAHOO.util.Motion(  
	            proxy, {  
	                points: {  
	                    to: Dom.getXY(srcEl) 
	                } 
	            },  
	            0.2,  
	            YAHOO.util.Easing.easeOut) 

	        var proxyid = proxy.id; 
	        var thisid = this.id; 
	        a.onComplete.subscribe(function() { 
	                Dom.setStyle(proxyid, "visibility", "hidden"); 
	                Dom.setStyle(thisid, "visibility", ""); 
	            }); 
	        a.animate(); 
	    }, 
	 
	    onDrag: function(e) 
		{ 
	        var x = Event.getPageX(e); 
			var y = Event.getPageY(e);
	        if (x < this.lastX) { 
	            this.goingLeft = 1; 
	        } else if (x > this.lastX) { 
	            this.goingLeft = 0; 
	        }
			else
			{
				this.goingLeft = -1;
			}
	        if (y < this.lastY) { 
	            this.goingUp = true; 
	        } else if (y > this.lastY) { 
	            this.goingUp = false; 
	        }
	        this.lastX = x; 
			this.lastY = y; 
	    }, 

	    onDragOver: function(e, id) 
		{ 
	        var srcEl = this.getEl(); 
	        var destEl = Dom.get(id); 
	        if (destEl.className.toLowerCase() == "editable") { 
	            var orig_p = srcEl.parentNode; 
	            var p = destEl.parentNode; 
	            if (this.goingLeft == 1 ) { 
	                p.insertBefore(srcEl, destEl); 
	            } else if(this.goingLeft == 0) { 
	                p.insertBefore(srcEl, destEl.nextSibling); 
	            }
				else
				{
					p.insertBefore(srcEl, destEl);
					if(this.goingUp)
					{
						p.insertBefore(srcEl, destEl);
					}
					else
					{
						p.insertBefore(srcEl, destEl.nextSibling);
					}
				}
	            DDM.refreshCache(); 
	        } 
	    } 
	}); 

	var handleSuccess1 = function(o)
	{	
		if(o.responseText !== undefined)
		{

		}
	}

	var handleFailure1 = function(o)
	{
		if(o.responseText !== undefined)
		{
			clean();
		}
	}

	var callback1 =
	{
	  success:handleSuccess1,
	  failure:handleFailure1,
	  argument: { foo:"foo", bar:"bar" }
	};


	YAHOO.util.Event.addListener(window, "load", init);
