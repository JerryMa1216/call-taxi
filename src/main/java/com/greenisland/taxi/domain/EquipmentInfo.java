package com.greenisland.taxi.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-8-15下午5:01:15
 */
@Entity
@Table(name = "TS_EQUIPMENT_INFO")
public class EquipmentInfo {
	private String equipmentId;
	private Integer requestCaptchaCount;

	@Id
	@Column(name = "EQUIPMENT_ID_")
	public String getEquipmentId() {
		return equipmentId;
	}

	public void setEquipmentId(String equipmentId) {
		this.equipmentId = equipmentId;
	}

	@Column(name = "REQUEST_CAPTCHA_COUNT_")
	public Integer getRequestCaptchaCount() {
		return requestCaptchaCount;
	}

	public void setRequestCaptchaCount(Integer requestCaptchaCount) {
		this.requestCaptchaCount = requestCaptchaCount;
	}

	public EquipmentInfo() {
		super();
	}

	public EquipmentInfo(String equipmentId, Integer requestCaptchaCount) {
		super();
		this.equipmentId = equipmentId;
		this.requestCaptchaCount = requestCaptchaCount;
	}

}
