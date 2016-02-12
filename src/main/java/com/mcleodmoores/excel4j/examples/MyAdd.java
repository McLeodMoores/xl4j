package com.mcleodmoores.excel4j.examples;

import com.mcleodmoores.excel4j.TypeConversionMode;
import com.mcleodmoores.excel4j.XLArgument;
import com.mcleodmoores.excel4j.XLFunction;
import com.mcleodmoores.excel4j.values.XLNumber;

public class MyAdd {
	private MyAdd() {
	}
	
	@XLFunction(name = "MyAdd",
			    description = "Add 2 numbers",
			    category = "Mine",
			    typeConversionMode = TypeConversionMode.SIMPLEST_RESULT)
	public static XLNumber myadd(@XLArgument(name = "num 1", description = "The first number") final XLNumber one, 
			                     @XLArgument(name = "num 2", description = "The second number") final XLNumber two) {
		return XLNumber.of(one.getValue() + two.getValue());
	}
}
