package com.bladecoder.ink.runtime;

import java.util.HashMap;

public abstract class AbstractValue extends RTObject {
	public abstract ValueType getValueType();

	public abstract boolean isTruthy() throws Exception;

	public abstract AbstractValue cast(ValueType newType) throws Exception;

	public abstract Object getValueObject();

	@SuppressWarnings("unchecked")
	public static AbstractValue create(Object val) {
		// Implicitly lose precision from any doubles we get passed in
		if (val instanceof Double) {
			double doub = (Double) val;
			val = (float) doub;
		}

		// Implicitly convert bools into ints
		if (val instanceof Boolean) {
			boolean b = (Boolean) val;
			val = (int) (b ? 1 : 0);
		}

		if (val instanceof Integer) {
			return new IntValue((Integer) val);
		} else if (val instanceof Long) {
			return new IntValue(((Long) val).intValue());
		} else if (val instanceof Float) {
			return new FloatValue((Float) val);
		} else if (val instanceof Double) {
			return new FloatValue((((Double) val).floatValue()));
		} else if (val instanceof String) {
			return new StringValue((String) val);
		} else if (val instanceof Path) {
			return new DivertTargetValue((Path) val);
		} else if (val instanceof HashMap) {
			return new SetValue((HashMap < String, Integer > )val);
		}

		return null;
	}

	@Override
	public RTObject copy() {
		return create(getValueObject());
	}

}
