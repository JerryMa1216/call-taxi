package com.greenisland.taxi.common.utils;

public enum CancelReasonEnum {
	A(4), B(5), C(6), D(7), E(8);
	public final int value;

	/**
	 * @param value
	 */
	private CancelReasonEnum(int value) {
		this.value = value;
	}

	public static CancelReasonEnum getInstance(int value) {
		for (CancelReasonEnum code : values()) {
			if (code.value == value)
				return code;
		}
		return null;
	}

}
