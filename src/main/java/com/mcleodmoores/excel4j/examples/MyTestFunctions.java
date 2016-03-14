package com.mcleodmoores.excel4j.examples;

import java.util.ArrayList;
import java.util.List;

import com.mcleodmoores.excel4j.TypeConversionMode;
import com.mcleodmoores.excel4j.XLArgument;
import com.mcleodmoores.excel4j.XLFunction;
import com.mcleodmoores.excel4j.values.XLArray;
import com.mcleodmoores.excel4j.values.XLBoolean;
import com.mcleodmoores.excel4j.values.XLError;
import com.mcleodmoores.excel4j.values.XLLocalReference;
import com.mcleodmoores.excel4j.values.XLMultiReference;
import com.mcleodmoores.excel4j.values.XLNumber;
import com.mcleodmoores.excel4j.values.XLObject;
import com.mcleodmoores.excel4j.values.XLString;
import com.mcleodmoores.excel4j.values.XLValue;

public class MyTestFuncs {
	@XLFunction(name = "MyStringCat",
		    description = "Concat 2 strings",
		    category = "Mine",
		    typeConversionMode = TypeConversionMode.SIMPLEST_RESULT)
	public static XLString myadd(@XLArgument(name = "string 1", description = "The first string") final XLString one, 
			                     @XLArgument(name = "string 2", description = "The second string") final XLString two) {
		return XLString.of("Hello" + one.getValue() + two.getValue());
	}
	
	@XLFunction(name = "MyXOR",
		    description = "XOR 2 booleans",
		    category = "Mine",
		    typeConversionMode = TypeConversionMode.SIMPLEST_RESULT)
	public static XLBoolean myXOR(@XLArgument(name = "boolean 1", description = "The first boolean") final XLBoolean one, 
			                     @XLArgument(name = "boolean 2", description = "The second boolean") final XLBoolean two) {
		return XLBoolean.from(one.getValue() ^ two.getValue());
	}
	
	@XLFunction(name = "MyLocalReference",
		    description = "Local reference tostring",
		    category = "Mine",
		    typeConversionMode = TypeConversionMode.SIMPLEST_RESULT,
		    isMacroEquivalent = true,
		    isMultiThreadSafe = false)
	public static XLString myLocalReference(@XLArgument(name = "local reference", description = "The local reference (range)") final XLLocalReference ref) {
		return XLString.of(ref.toString());
	}
	
	@XLFunction(name = "MyMultiReference",
		    description = "Multi reference tostring",
		    category = "Mine",
		    typeConversionMode = TypeConversionMode.SIMPLEST_RESULT,
		    isMacroEquivalent = true,
		    isMultiThreadSafe = false)
			
	public static XLString myXOR(@XLArgument(name = "multi reference", description = "The multi reference (range)") final XLMultiReference ref) {
		return XLString.of(ref.toString());
	}
	
	@XLFunction(name = "MyArray",
		    description = "Multi reference tostring",
		    category = "Mine",
		    typeConversionMode = TypeConversionMode.SIMPLEST_RESULT)
	public static XLArray myArray() {
		XLValue[][] arr = { { XLNumber.of(1), XLString.of("Two"), XLNumber.of(3) }, 
				            { XLString.of("One"), XLNumber.of(2), XLString.of("3") } }; 
		return XLArray.of(arr);
	}
	
	@XLFunction(name = "MakeList",
		    description = "Make a list from a range/array",
		    category = "Mine",
		    typeConversionMode = TypeConversionMode.OBJECT_RESULT)
	public static List<?> makeList(@XLArgument(name = "entries", description = "The values to put in the list (range)") final XLArray arr) {
		ArrayList<XLValue> list = new ArrayList<XLValue>();
		XLValue[][] array = arr.getArray();
		for (int j=0; j<array.length; j++) {
			XLValue[] row = array[j];
			for (int i=0; i<row.length; i++) {
				list.add(row[i]);
			}
		}
		return list;
	}
	
	@XLFunction(name = "ListElement",
		    description = "Get an element from a list",
		    category = "Mine",
		    typeConversionMode = TypeConversionMode.SIMPLEST_RESULT)
	public static XLValue listElem(final List<XLValue> list, final int index) {
		if (index >= list.size()) {
			return XLError.NA;
		}
		return list.get(index);
	}
}
