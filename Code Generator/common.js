function getElement(elemName)
{
	return document.getElementById(elemName);
}

function getFieldValue(elemName)
{
	return getElement(elemName).value;
}

function setFieldValue(elemName, newValue)
{
	getElement(elemName).value = newValue;
}

function hideElement(elemName)
{
	var elem = getElement(elemName);
	elem.style.visibility = 'hidden';
	
	var labelElem = getElement('lbl_' + elemName);
	if (labelElem != null)
		labelElem.style.visibility = 'hidden';
}

function showElement(elemName)
{
	var elem = getElement(elemName);
	elem.style.visibility = 'visible';

	var labelElem = getElement('lbl_' + elemName);
	if (labelElem != null)
		labelElem.style.visibility = 'visible';
}

function enableElement(elemName)
{
	var elem = getElement(elemName);
	elem.disabled = false;
	elem.style.backgroundColor= '';
}
function disableElement(elemName)
{
	var elem = getElement(elemName);
	elem.disabled = true;
	elem.style.backgroundColor= '#EEEEEE';	
}

function removeOptions(selectName)
{
	var selectElement = getElement(selectName);
	var len = selectElement.options.length;
	for (var i = 0; i < len; i++)
	{
		selectElement.remove(0);
	}
}

function addOptions(selectName, newOptions)
{
	var selectElement = getElement(selectName);
	var len = newOptions.length;
	for (var i = 0; i < len; i++)
	{
		var opt = document.createElement("OPTION");
		opt.value = newOptions[i][0];
		opt.text = newOptions[i][1];
		selectElement.add(opt);		
	}
}

function trimStr(aStr) {
	if (aStr == null || aStr == '') 
	{
		return aStr;
	}
	else 
	{
		aStr = '' + aStr;
		for (var i = aStr.substring(0, 1); i == ' ' || i == '\t';) 
		{
			aStr = aStr.substring(1);
			i = aStr.substring(0, 1);
		}

		for (var j = aStr.substring(aStr.length - 1); j == ' ' || j == '\t';) 
		{
			aStr = aStr.substring(0, aStr.length - 1);
			j = aStr.substring(aStr.length - 1);
		}
	}
	return aStr;
}