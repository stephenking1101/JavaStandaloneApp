var CURRENT_DATE = new Date();
var GENERATE_TYPE_FOR_DYNA_FORM_DESIGN =
			[['dyna_form_xml', 'dynaForm XML'],
			 ['input_form', 'inputForm class'],
			 ['copy_input_form', 'copyInputForm method']];
var GENERATE_TYPE_FOR_TABLE_DESIGN =
			[['db_script', 'DB script'],
			 ['detail', 'detail class'],
			 ['set_local_by_detail', 'setLocalByDetail method'],
			 ['get_detail_by_local', 'getDetailByLocal method'],
			 ['set_detail_by_input_form', 'setDetailByInputForm method'],
			 ['basic_view', 'basic view class'],
			 ['detail_view', 'detail view class'],
			 ['dyna_form_fields', 'dynaForm Fields']];
var GENERATE_TYPE_FOR_SERVICE_LOCAL =
			[['delegate', 'delegate class']];

function initScreen()
{
	onChangeInputFormat();
}

function generateCode()
{
	var result;
	var inputFormat = getFieldValue('input_format');
	var generateType = getFieldValue('generate_type');

	switch (inputFormat)
	{
		case 'dyna_form_design':
			switch (generateType)
			{
				case 'dyna_form_xml':
					result = genDynaForm();
					break;
				case 'input_form':
					result = genInputForm();
					break;
				case 'copy_input_form':
					result = genCopyInputForm();
					break;
			}
			break;
		case 'table_design':
			switch (generateType)
			{
				case 'db_script':
					result = genDbScript();
					break;
				case 'detail':
					result = genDetail();
					break;
				case 'set_local_by_detail':
					result = genSetLocalByDetail();
					break;
				case 'get_detail_by_local':
					result = genGetDetailByLocal();
					break;
				case 'set_detail_by_input_form':
					result = genSetDetailByInputForm();
					break;
				case 'basic_view':
					result = genBasicView();
					break;
				case 'detail_view':
					result = genDetailView();
					break;
				case 'dyna_form_fields':
					result = genDynaFormFields();
					break;
			}
			break;
		case 'service_local':
			if (generateType == 'delegate')
				result = genDelegate();
			break;
	}

	if (result != null)
	{
		getElement('output_text').innerText = result;
		saveToFile(result);
	}
}

function getLines()
{
	return getElement('input_text').innerText.split('\r\n');
}

function genDynaForm()
{
	var lines = getLines();
	var result = '<form-bean name="' + getFieldValue('class_name') + '" type="org.apache.struts.action.DynaActionForm">' + '\n';

	for (var i = 0; i < lines.length; i++)
	{
		var cells = lines[i].split('\t');
		var fieldName = cells[0];
		var fieldType = '';
		if (cells.length > 1 && cells[1] == 'Y')
			fieldType = '[]';

		result += '    ' + '<form-property name="' + fieldName + '" type="java.lang.String' + fieldType + '"/>' + '\n';
	}
	result += '</form-bean>';

	return result;
}


function genInputForm()
{
	var className = getFieldValue('class_name');
	if (className.indexOf('InputForm') == -1)
	{
		alert('Invalid class name! The name must end with "InputForm".');
		return null;
	}

	var lines = getLines();
	var result = getCopyRight();
	result += 'package ' + getFieldValue('package_name') + ';\n';
	result += 'public class ' + className + '\n';
	result += '{\n';

	for (var i = 0; i < lines.length; i++)
	{
		var cells = lines[i].split('\t');
		var fieldName = cells[0];
		var fieldType = '';
		if (cells.length > 1 && cells[1] == 'Y')
			fieldType = '[]';

		result += '    ' + 'private String' + fieldType + ' ' + fieldName + ';\n';
	}

	result += '\n';

	for (var i = 0; i < lines.length; i++)
	{
		var cells = lines[i].split('\t');
		var fieldName = cells[0];
		var upperFieldName = getUpperName(fieldName);
		var fieldType = '';
		if (cells.length > 1 && cells[1] == 'Y')
			fieldType = '[]';

		result += '    ' + 'public void ' + 'set' + upperFieldName + '(String' + fieldType + ' ' + fieldName +')\n';
		result += '    ' + '{\n';
		result += '    ' + '    ' + 'this.' + fieldName + ' = ' + fieldName + ';\n'
		result += '    ' + '}\n';

		result += '    ' + 'public String' + fieldType + ' get' + upperFieldName + '()\n';
		result += '    ' + '{\n';
		result += '    ' + '    ' + 'return ' + fieldName + ';\n'
		result += '    ' + '}\n';
	}

	result += '}\n';

	return result;

}

function genCopyInputForm()
{
	var lines = getLines();
	var className = getFieldValue('class_name');
	var result = '    ' + 'private ' + className +
				' copyInputForm(DynaActionForm dynaForm, String userId, String userName) throws Exception' + '\n';
	result += '    ' + '{\n';
	result += '    ' + '    ' + className + ' result = new ' + className + '();\n';
	result += '\n';

	for (var i = 0; i < lines.length; i++)
	{
		var cells = lines[i].split('\t');
		var fieldName = cells[0];
		var upperFieldName = getUpperName(fieldName);
		var fieldType = '';
		if (cells.length > 1 && cells[1] == 'Y')
			fieldType = '[]';

		var inputFormRefName = 'result';
		
		result += '    ' + '    ' + inputFormRefName +'.set' + upperFieldName + '(';
		if (fieldType.indexOf('[]') != -1)
			result += 'getPropertyArray';
		else
			result += 'getProperty';

		result += '(dynaForm, "' + fieldName + '"));\n';
	}

	result += '\n';
	result += '    ' + '    ' + inputFormRefName + '.setUserId(userId);\n';
	result += '    ' + '    ' + inputFormRefName + '.setUserName(userName);\n\n';
	result += '    ' + '    ' + 'return ' + inputFormRefName + ';\n';
	result += '    ' + '}\n';

	return result;

}

function genDbScript()
{
	var lines = getLines();
	var tableName = getFieldValue('class_name');
	var result = 'create table FOS.' + tableName + '(\n';

	var primaryKeys = new Array();
	var i;
	for (i = 0; i < lines.length; i++)
	{
		var cells = lines[i].split('\t');
		if (cells.length <= 3)
			break;

		result += '    ' + cells[0] + ' ' + cells[1];
		if (cells[4].toLowerCase() == 'no')
			result += ' not null, \n';
		else
			result += ', \n';

		if (cells[2].toLowerCase() == 'yes')
			primaryKeys[primaryKeys.length] = cells[0];
	}

	result += '    ' + 'constraint PK_' + getFieldValue('alias_name') + ' primary key (';
	for (var k = 0; k < primaryKeys.length; k++)
	{
		result += primaryKeys[k];
		if (k < primaryKeys.length - 1)
			result += ', ';
	}
	result += '));\n'

	if (i < lines.length)
	{
		result += '\n';

		var seqNo = 1;
		var foreignKeyFields;
		var referenceFields;
		var referenceTable = '';
		for (var j = i; j < lines.length; j++)
		{
			var cells = lines[j].split('\t');
			if (cells.length < 3)
				continue;

			if (referenceTable == '')
			{
				foreignKeyFields = cells[0];
				referenceTable = cells[1];
				referenceFields = cells[2];
			}
			else if (referenceTable == cells[1])
			{
				foreignKeyFields += ', ' + cells[0];
				referenceFields += ', ' + cells[2];
			}
			else
			{
				result += genFkRefScript(
					seqNo, foreignKeyFields, referenceTable, referenceFields);

				seqNo++ ;
				foreignKeyFields = cells[0];
				referenceTable = cells[1];
				referenceFields = cells[2];
			}
		}

		result += genFkRefScript(
			seqNo, foreignKeyFields, referenceTable, referenceFields);

	}

	return result;
}

function genFkRefScript(
	seqNo,
	foreignKeyFields,
	referenceTable,
	referenceFields)
{
	var seq;
	if (seqNo < 10)
		seq = '0' + seqNo.toString();
	else
		seq = seqNo.toString();

	var result = 'alter table FOS.' + getFieldValue('class_name') + '\n';
	result += '    ' + 'add constraint FK_' + getFieldValue('alias_name') + '_' + seq +
				' foreign key (' + foreignKeyFields + ')' + '\n';
	result += '    ' + 'references FOS.' + referenceTable + ' (' + referenceFields + ');\n\n';

	return result;
}

function genDetail()
{
	var className = getFieldValue('class_name');
	if (className.indexOf('Detail') == -1)
	{
		alert('Invalid class name! The name must end with "Detail".');
		return null;
	}

	var lines = getLines();
	var result = getCopyRight();

	result += 'package ' + getFieldValue('package_name') + ';\n';
	result += 'public class ' + className + ' implements java.io.Serializable' + '\n';
	result += '{\n';

	for (var i = 0; i < lines.length; i++)
	{
		var cells = lines[i].split('\t');
		if (cells.length <= 3)
			break;

		var fieldName = convertName(cells[0].toLowerCase());
		var dbFieldType = getDbFieldType(cells[1]);
		var javaFieldType = convertType(dbFieldType);

		result += '    ' + 'private ' + javaFieldType + ' ' + fieldName + ';\n';
	}

	result += '\n';

	for (var i = 0; i < lines.length; i++)
	{
		var cells = lines[i].split('\t');
		if (cells.length <= 3)
			break;

		var fieldName = convertName(cells[0].toLowerCase());
		var upperFieldName = getUpperName(fieldName);
		var dbFieldType = getDbFieldType(cells[1]);
		var javaFieldType = convertType(dbFieldType);

		result += '    ' + 'public void ' + 'set' + upperFieldName + '(' + javaFieldType + ' ' + fieldName +')\n';
		result += '    ' + '{\n';
		result += '    ' + '    ' + 'this.' + fieldName + ' = ' + fieldName + ';\n'
		result += '    ' + '}\n';

		result += '    ' + 'public ' + javaFieldType + ' get' + upperFieldName + '()\n';
		result += '    ' + '{\n';
		result += '    ' + '    ' + 'return ' + fieldName + ';\n'
		result += '    ' + '}\n';
	}

	result += '}\n';

	return result;
}

function genSetLocalByDetail()
{
	var name =  getUpperName(getFieldValue('class_name'));
	var index = name.indexOf('Detail');
	if (index != -1)
		name = name.substring(0, index);
	else
	{
		alert('Invalid clss name! The name must end with "Detail".');
		return null;
	}

	var lines = getLines();

	var localClassName = name + 'Local';
	var detailClassName = name + 'Detail';

//	var localRefName = getLowerName(localClassName);
//	var detailRefName = getLowerName(detailClassName);

	var localRefName = 'local';
	var detailRefName = 'detail';

	var result = '    ' + 'protected void set' + name + 'Local(' + localClassName + ' ' + localRefName + ', ' +
				detailClassName + ' ' + detailRefName + ')\n';
	result += '    ' + '{\n';
	result += '    ' + '    ' + 'if (' + localRefName + ' == null || ' + detailRefName + ' == null)\n';
	result += '    ' + '    ' + '    ' + 'return;\n';
	result += '\n';

	for (var i = 0; i < lines.length; i++)
	{
		var cells = lines[i].split('\t');
		if (cells.length <= 3)
			break;

		if (cells[2].toLowerCase() != 'yes')
		{
			var fieldName = convertName(cells[0].toLowerCase());
			var upperFieldName = getUpperName(fieldName);

			result += '    ' + '    ' + localRefName + '.set' + upperFieldName + '(' +
					  detailRefName + '.get' + upperFieldName + '());\n';
		}
	}

	result += '    ' + '}\n';

	return result;
}

function genGetDetailByLocal()
{
	var name =  getUpperName(getFieldValue('class_name'));
	var index = name.indexOf('Detail');
	if (index != -1)
		name = name.substring(0, index);
	else
	{
		alert('Invalid clss name! The name must end with "Detail".');
		return null;
	}

	var lines = getLines();

	var localClassName = name + 'Local';
	var detailClassName = name + 'Detail';

//	var localRefName = getLowerName(localClassName);
	var localRefName = 'local';

	var result = '    ' + 'protected ' + detailClassName + ' get' + name + 'Detail(' + 
					localClassName + ' ' + localRefName + ')\n';
	result += '    ' + '{\n';
	result += '    ' + '    ' + detailClassName + ' result = null;\n';
	result += '    ' + '    ' + 'if (' + localRefName + ' != null )\n';
	result += '    ' + '    ' + '{\n';
	result += '    ' + '    ' + '    ' + 'result = new ' + detailClassName + '();\n';

	for (var i = 0; i < lines.length; i++)
	{
		var cells = lines[i].split('\t');
		if (cells.length <= 3)
			break;

		var fieldName = convertName(cells[0].toLowerCase());
		var upperFieldName = getUpperName(fieldName);

		result += '    ' + '    ' + '    ' + 'result.set' + upperFieldName + '(' +
				  localRefName + '.get' + upperFieldName + '());\n';
	}

	result += '    ' + '    ' + '}\n';
	result += '    ' + '    ' + 'return result;\n';
	result += '    ' + '}\n';

	return result;
}

function genSetDetailByInputForm()
{
	var name =  getUpperName(getFieldValue('class_name'));
	var index = name.indexOf('Detail');
	if (index != -1)
		name = name.substring(0, index);
	else
	{
		alert('Invalid clss name! The name must end with "Detail".');
		return null;
	}

	var lines = getLines();

	var inputFormClassName = name + 'InputForm';
	var detailClassName = name + 'Detail';

	var inputFormRefName = 'form';
//	var detailRefName = getLowerName(detailClassName);
	var detailRefName = 'detail';

	var result = '    ' + 'private void set' + name + 'Detail(' + inputFormClassName + ' ' + inputFormRefName + ', ' +
				detailClassName + ' ' + detailRefName + ')\n';
	result += '    ' + '{\n';
	result += '    ' + '    ' + 'if (' + inputFormRefName + ' == null || ' + detailRefName + ' == null)\n';
	result += '    ' + '    ' + '    ' + 'return;\n';
	result += '\n';

	for (var i = 0; i < lines.length; i++)
	{
		var cells = lines[i].split('\t');
		if (cells.length <= 3)
			break;

		var fieldName = convertName(cells[0].toLowerCase());
		var upperFieldName = getUpperName(fieldName);
		var dbFieldType = getDbFieldType(cells[1]);
		var javaFieldType = convertType(dbFieldType);

		result += '    ' + '    ' + detailRefName + '.set' + upperFieldName + '(';
		switch (javaFieldType)
		{
			case 'String':
				result += inputFormRefName + '.get' + upperFieldName + '());\n';
				break;
			case 'java.math.BigDecimal':
				result += 'new BigDecimal(' + inputFormRefName + '.get' + upperFieldName + '()));\n';
				break;
			case 'java.sql.Date':
				result += 'Date.valueOf(' + inputFormRefName + '.get' + upperFieldName + '()));\n';
				break;
			case 'java.sql.Time':
				result += 'Time.valueOf(' + inputFormRefName + '.get' + upperFieldName + '()));\n';
				break;
			case 'java.sql.Timestamp':
				result += 'Timestamp.valueOf(' + inputFormRefName + '.get' + upperFieldName + '()));\n';
				break;
			case 'boolean':
				result += 'Boolean.valueOf(' + inputFormRefName + '.get' + upperFieldName + '()).booleanValue());\n';
				break;
			case 'byte':
				result += 'Byte.valueOf(' + inputFormRefName + '.get' + upperFieldName + '()).byteValue());\n';
				break;
			case 'short':
				result += 'Short.valueOf(' + inputFormRefName + '.get' + upperFieldName + '()).shortValue());\n';
				break;
			case 'int':
				result += 'Integer.valueOf(' + inputFormRefName + '.get' + upperFieldName + '()).intValue());\n';
				break;
			case 'long':
				result += 'Long.valueOf(' + inputFormRefName + '.get' + upperFieldName + '()).longValue());\n';
				break;
			case 'float':
				result += 'Float.valueOf(' + inputFormRefName + '.get' + upperFieldName + '()).floatValue());\n';
				break;
			case 'double':
				result += 'Double.valueOf(' + inputFormRefName + '.get' + upperFieldName + '()).doubleValue());\n';
				break;
			case 'byte[]':
				result += inputFormRefName + '.get' + upperFieldName + '().getBytes());\n';
				break;
		}

	}

	result += '    ' + '}\n';

	return result;
}
function genBasicView()
{
	var name =  getUpperName(getFieldValue('class_name'));
	var index = name.indexOf('BasicView');
	if (index != -1)
		name = name.substring(0, index);
	else
	{
		alert('Invalid class name! The name must end with "BasicView".');
		return null;
	}

	var lines = getLines();
	var result = getCopyRight();
	result += 'package ' + getFieldValue('package_name') + ';\n';

	var detailClassName = name + 'Detail';
	var basicViewClassName = name + 'BasicView';

	var detailRefName = getLowerName(detailClassName);

	result += 'public class ' + basicViewClassName + ' implements java.io.Serializable' + '\n';
	result += '{\n';

	result += '    ' + 'protected ' + detailClassName + ' ' + detailRefName + ';\n';
	result += '\n';

	result += '    ' + 'public ' + basicViewClassName + '()\n';
	result += '    ' + '{\n';
	result += '    ' + '    ' + 'this.' + detailRefName + ' = new ' + detailClassName + '();\n';
	result += '    ' + '}\n';

	result += '    ' + 'public ' + basicViewClassName + '(' + detailClassName + ' ' + detailRefName + ')\n';
	result += '    ' + '{\n';
	result += '    ' + '    ' + 'this.' + detailRefName + ' = ' + detailRefName + ';\n';
	result += '    ' + '}\n';

	for (var i = 0; i < lines.length; i++)
	{
		var cells = lines[i].split('\t');
		if (cells.length <= 3)
			break;

		if (cells[7].toLowerCase() == 'b')
		{
			var viewFieldName = cells[6];
			var upperViewFieldName = getUpperName(viewFieldName);
			var detailFieldName = convertName(cells[0].toLowerCase());
			var upperDetailFieldName = getUpperName(detailFieldName);

			var dbFieldType = getDbFieldType(cells[1]);
			var javaFieldType = convertType(dbFieldType);

			result += '    ' + 'public ' + javaFieldType + ' get' + upperViewFieldName + '()\n';
			result += '    ' + '{\n';
			result += '    ' + '    ' + 'return ' +  detailRefName + '.get' + upperDetailFieldName + '();\n'
			result += '    ' + '}\n';
		}
	}

	result += '}\n';

	return result;
}

function genDetailView()
{
	var name =  getUpperName(getFieldValue('class_name'));
	var index = name.indexOf('DetailView');
	if (index != -1)
		name = name.substring(0, index);
	else
	{
		alert('Invalid class name! The name must end with "DetailView".');
		return null;
	}

	var detailClassName = name + 'Detail';
	var detailViewClassName = name + 'DetailView';
	var basicViewClassName = name + 'BasicView';
	var detailRefName = getLowerName(detailClassName);

	var lines = getLines();
	var result = getCopyRight();
	result += 'package ' + getFieldValue('package_name') + ';\n';


	result += 'public class ' + detailViewClassName + ' extends ' + basicViewClassName + '\n';
	result += '{\n';

	result += '    ' + 'public ' + detailViewClassName + '(){}\n';

	result += '    ' + 'public ' + detailViewClassName + '(' + detailClassName + ' ' + detailRefName + ')\n';
	result += '    ' + '{\n';
	result += '    ' + '    ' + 'super(' + detailRefName + ');\n';
	result += '    ' + '}\n';

	for (var i = 0; i < lines.length; i++)
	{
		var cells = lines[i].split('\t');
		if (cells.length <= 3)
			break;

		if (cells[7].toLowerCase() == 'd')
		{
			var viewFieldName = cells[6];
			var upperViewFieldName = getUpperName(viewFieldName);
			var detailFieldName = convertName(cells[0].toLowerCase());
			var upperDetailFieldName = getUpperName(detailFieldName);

			var dbFieldType = getDbFieldType(cells[1]);
			var javaFieldType = convertType(dbFieldType);

			result += '    ' + 'public ' + javaFieldType + ' get' + upperViewFieldName + '()\n';
			result += '    ' + '{\n';
			result += '    ' + '    ' + 'return ' +  detailRefName + '.get' + upperDetailFieldName + '();\n'
			result += '    ' + '}\n';
		}
	}

	result += '}\n';

	return result;
}

function genDynaFormFields()
{
	var lines = getLines();
	var result = '';
	for (var i = 0; i < lines.length; i++)
	{
		var cells = lines[i].split('\t');
		if (cells.length <= 3)
			break;

		var fieldName = convertName(cells[0].toLowerCase());
		result += fieldName + '\n';
	}
	return result;
}

function genDelegate()
{
	var name =  getUpperName(getFieldValue('class_name'));
	var index = name.indexOf('Delegate');
	if (index != -1)
		name = name.substring(0, index);
	else
	{
		alert('Invalid class name! The name must end with "Delegate".');
		return null;
	}

	var delegateClassName = name + 'Delegate';
	var homeClassName = name + 'ServiceLocalHome';
	var homeRefName = getLowerName(name) + 'ServiceHome';

	var lines = getLines();
	var methods = getMethods(lines);

	var result = getCopyRight();
	result += 'package ' + getFieldValue('package_name') + ';\n';
	result += getImports4Delegate();

	result += 'public class ' + delegateClassName + '\n';
	result += '{\n';

	result += '    ' + 'private ' + homeClassName + ' ' + homeRefName + ';\n';
	result += '    ' + 'public ' + delegateClassName + '() throws ApplicationException\n';
	result += '    ' + '{\n';
	result += '    ' + '    ' + 'getLocalHomes();\n';
	result += '    ' + '}\n';

	result += '    ' + 'private getLocalHomes() throws ApplicationException \n';
	result += '    ' + '{\n';
	result += '    ' + '    ' + 'try\n';
	result += '    ' + '    ' + '{\n';
	result += '    ' + '    ' + '    ' + homeRefName + ' = (' + homeClassName + ')\n';
	result += '    ' + '    ' + '    ' + '    ' +
				'EJBHomeFactory.singleton().getHome(JndiConstant.' + name.toUpperCase() + '_SERVICE);\n';
	result += '    ' + '    ' + '}\n';
	result += '    ' + '    ' + 'catch (NamingException exc)\n';
	result += '    ' + '    ' + '{\n';
	result += '    ' + '    ' + '    ' + 'throw new SystemException(exc);\n';
	result += '    ' + '    ' + '}\n';
	result += '    ' + '}\n';

	for (var i = 0; i < methods.length; i++)
	{
		var aMethod = methods[i];
		var tokens = aMethod.split(' ');
		var returnType = tokens[1];

		result += '    ' + aMethod.substring(0, aMethod.length - 1) + '\n';
		result += '    ' + '{\n';

		if (returnType != 'void')
			result += '    ' + '    ' + returnType + ' result = null;\n';

		result += '    ' + '    ' + 'try\n';
		result += '    ' + '    ' + '{\n';

		result += '    ' + '    ' + '    ';
		if (returnType != 'void')
			result += 'result = ';

		result += homeRefName + '.create().' + getCallMethod(tokens[2] + ' ' + tokens[3]) + ';\n';
		result += '    ' + '    ' + '}\n';

		result += '    ' + '    ' + 'catch (CreateException exc)\n';
		result += '    ' + '    ' + '{\n';
		result += '    ' + '    ' + '    ' + 'throw new SystemException(exc);\n';
		result += '    ' + '    ' + '}\n';

		result += '    ' + '    ' + 'catch (EJBException exc)\n';
		result += '    ' + '    ' + '{\n';
		var exceptionKey;
		if (tokens[2].indexOf('get') == 0)
			exceptionKey = 'FAIL_TO_READ_RECORD';
		else
			exceptionKey = 'FAIL_TO_UPDATE_RECORD';

		result += '    ' + '    ' + '    ' +
			'throw new FunctionException(MessageResource.' + exceptionKey + ', exc);\n';

		result += '    ' + '    ' + '}\n';

		if (returnType != 'void')
			result += '    ' + '    ' + 'return result;\n';

		result += '    ' + '}\n';
	}
	result += '}\n';

	return result;
}

function getMethods(lines)
{
	var result = null;
	if (lines != null && lines.length > 0)
	{
		result = new Array();
		var aMethod = '';
		for (var i = 0; i < lines.length; i++)
		{
			aMethod += trimStr(lines[i]) + ' ';
			if (aMethod.indexOf(';') != -1)
			{
				result[result.length] = '' + trimStr(aMethod);
				aMethod = '';
			}
		}
	}

	return result;
}

function getCallMethod(token)
{
	var result = null;
	if (token != null && token != '')
	{
		var index1 = token.indexOf('(');
		var index2 = token.indexOf(' ');
		if (index1 != -1 && index2 != -1)
		{
			result = token.substring(0, index1 + 1) + token.substring(index2 + 1);
		}
	}
	return result;
}

function getImports4Delegate()
{
	var result = '' +
		'import javax.ejb.CreateException;\n' +
		'import javax.ejb.EJBException;\n' +
		'import javax.naming.NamingException;\n' +
		'\n' +
		'import com.hsbcprivatebank.efos.global.pb.base.common.ejbtool.EJBHomeFactory;\n' +
		'import com.hsbcprivatebank.efos.global.pb.base.common.ejbtool.JndiConstant;\n' +
		'\n' +
		'import com.hsbcprivatebank.efos.global.pb.base.common.exception.ApplicationException;\n' +
		'import com.hsbcprivatebank.efos.global.pb.base.common.exception.FunctionException;\n' +
		'import com.hsbcprivatebank.efos.global.pb.base.common.exception.MessageResource;\n' +
		'import com.hsbcprivatebank.efos.global.pb.base.common.exception.SystemException;\n' +
		'\n';
	return result;
}

function onChangeInputFormat()
{
	removeOptions('generate_type');

	var inputType = getFieldValue('input_format');
	var newOptions;

	if (inputType == 'dyna_form_design')
	{
		newOptions = GENERATE_TYPE_FOR_DYNA_FORM_DESIGN;
	}
	else if (inputType == 'table_design')
	{
		newOptions = GENERATE_TYPE_FOR_TABLE_DESIGN;
	}
	else if (inputType == 'service_local')
	{
		newOptions = GENERATE_TYPE_FOR_SERVICE_LOCAL;
	}
	addOptions('generate_type', newOptions);
	clearFieldValues();
	if (inputType == 'service_local')
	{
		setFieldValue('generate_type', 'delegate');
		getElement('generate_type').fireEvent('onchange');
	}
	else
		showAllFields();

}

function onChangeGenerateType()
{
	initVisibility();
	getElement('output_text').innerText = '';
}

function convertName(dbFieldName)
{
	var result = null;
	if (dbFieldName != null && dbFieldName.length > 0)
	{
		var name = dbFieldName.split('_');
		for (var i = 0; i < name.length; i++)
		{
			if (i == 0)
				result = name[i];
			else
				result += getUpperName(name[i]);
		}
	}

	return result;
}

function getUpperName(aName)
{
	var result = null;
	if (aName != null && aName.length > 0)
		result = aName.substring(0, 1).toUpperCase() + aName.substring(1);

	return result;
}

function getLowerName(aName)
{
	var result = null;
	if (aName != null && aName.length > 0)
		result = aName.substring(0, 1).toLowerCase() + aName.substring(1);

	return result;
}

function getDbFieldType(dbFieldType)
{
	var result = null;
	if (dbFieldType != null && dbFieldType.length > 0)
	{
		var index = dbFieldType.indexOf('(');
		if (index != -1)
			result = dbFieldType.substring(0, index);
		else
			result = dbFieldType;
	}

	return result;
}

function convertType(dbFieldType)
{
	var result = null;

	switch (dbFieldType.toUpperCase())
	{
		case 'CHAR':
		case 'VARCHAR':
		case 'LONGVARCHAR':
			result = 'String';
			break;

		case 'NUMERIC':
		case 'DECIMAL':
			result = 'java.math.BigDecimal';
			break;

		case 'BIT':
			result = 'boolean';
			break;
		case 'TINYINT':
			result = 'byte';
			break;
		case 'SMALLINT':
			result = 'short';
			break;
		case 'INTEGER':
			result = 'int';
			break;
		case 'BIGINT':
			result = 'long';
			break;
		case 'REAL':
			result = 'float';
			break;

		case 'FLOAT':
		case 'DOUBLE':
			result = 'double';
			break;

		case 'BINARY':
		case 'VARBINARY':
		case 'LONGVARBINARY':
			result = 'byte[]';
			break;

		case 'DATE':
			result = 'java.sql.Date';
			break;
		case 'TIME':
			result = 'java.sql.Time';
			break;
		case 'TIMESTAMP':
			result = 'java.sql.Timestamp';
			break;
	}
	return result;
}

function showAllFields()
{
	showElement('output_path');
	showElement('class_name');
	showElement('alias_name');
	showElement('package_name');
}

function clearFieldValues()
{
	setFieldValue('generate_type', '');
	setFieldValue('class_name', '');
	setFieldValue('alias_name', '');
	setFieldValue('package_name', '');
	setFieldValue('input_text', '');
	setFieldValue('output_text', '');
}

function initVisibility()
{
	var inputFormat = getFieldValue('input_format');
	var generateType = getFieldValue('generate_type');

	showElement('class_name');
					
	switch (inputFormat)
	{
		case 'dyna_form_design':
			hideElement('alias_name');
			if (generateType == 'input_form')
			{
				showElement('output_path');
				showElement('package_name');
			}
			else
			{
				hideElement('output_path');
				hideElement('package_name');
			}
			break;
		case 'table_design':
			hideElement('alias_name');
			if (generateType == 'db_script')
			{
				showElement('output_path');
				showElement('alias_name');
				hideElement('package_name');
			}
			else if (generateType == 'detail' ||
					 generateType == 'basic_view' ||
					 generateType == 'detail_view')
			{
				showElement('output_path');
				showElement('package_name');
			}
			else if (generateType == 'set_local_by_detail' ||
					 generateType == 'get_detail_by_local' ||
					 generateType == 'set_detail_by_input_form')
			{
				hideElement('output_path');
				hideElement('package_name');
			}
			else if (generateType == 'dyna_form_fields')
			{
				hideElement('output_path');
				hideElement('class_name');				
				hideElement('package_name');
			}
			break;
		case 'service_local':
			hideElement('alias_name');
			break;
	}

}

function getCopyRight()
{
	return '' +
		'\/**' + '\n' +
		' * COPYRIGHT (C) 2003 HSBC REPUBLIC BANK (SUISSE) SA. ALL RIGHTS RESERVED.' + '\n' +
		' *' + '\n' +
		' * No part of this publication may be reproduced, stored in a retrieval system,' + '\n' +
		' * or transmitted, on any form or by any means, electronic, mechanical, photocopying,' + '\n' +
		' * recording, or otherwise, without the prior written permission of HSBC REPUBLIC BANK.' + '\n' +
		' *' + '\n' +
		' * Created By: ' + getFieldValue('author_name') + ' with Code Generator\n' +
		' * Created On: ' + CURRENT_DATE.toDateString() + '\n' +
		' *' + '\n' +
		' * Amendment History:' + '\n' +
		' *' + '\n' +
		' * Amended By       Amended On      Amendment Description' + '\n' +
		' * ------------     -----------     ---------------------------------------------' + '\n' +
		' *' + '\n' +
		' *\/' + '\n';
}

function saveToFile(content)
{
	var outPath = getFieldValue('output_path');
	var isOutputPathVisible =
		(getElement('output_path').style.visibility.toLowerCase() == 'visible');

	if (!isOutputPathVisible || content == null || outPath.indexOf('\\') == -1)
		return;

	var fName = getFieldValue('class_name');
	if (getFieldValue('generate_type') == 'db_script')
		fName = fName.toLowerCase() + '.sql';
	else
	 	fName += '.java';

	var fso = new ActiveXObject("Scripting.FileSystemObject");
	var f1 = fso.CreateTextFile(outPath + '\\' + fName, true);
	f1.Write(content);
	f1.Close();
}
