package com.greenisland.taxi.common.utils;

public class CancelReasonConvert {
	public String getReason(CancelReasonEnum cancelReasonEnum) {
		StringBuilder result = new StringBuilder();
		switch (cancelReasonEnum) {
		case A:
			result.append("已和司机友好协商取消订单");
			break;
		case B:
			result.append("我有事先走了");
			break;
		case C:
			result.append("司机违约，没有来接我");
			break;
		case D:
			result.append("司机违约，迟到太久了");
			break;
		case E:
			result.append("其他原因");
			break;
		default:
			break;
		}
		return result.toString();
	}
}
