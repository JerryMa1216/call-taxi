package com.greenisland.taxi.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "TS_LOG")
public class SystemLog {
	private String id;
	private String content;
	private Date createDate;
	private String applyId;
	private String taxiPlatenumber;
	private String mechineType;
	private String callType;

	@Id
	@GenericGenerator(name = "idGenerator", strategy = "uuid")
	@GeneratedValue(generator = "idGenerator")
	@Column(name = "ID_")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "CONTENT_")
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Column(name = "CREATE_DATE_")
	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	@Column(name = "APPLY_ID_")
	public String getApplyId() {
		return applyId;
	}

	public void setApplyId(String applyId) {
		this.applyId = applyId;
	}

	@Column(name = "TAXI_PLATENUMBER_")
	public String getTaxiPlatenumber() {
		return taxiPlatenumber;
	}

	public void setTaxiPlatenumber(String taxiPlatenumber) {
		this.taxiPlatenumber = taxiPlatenumber;
	}

	@Column(name = "MECHINE_TYPE_")
	public String getMechineType() {
		return mechineType;
	}

	public void setMechineType(String mechineType) {
		this.mechineType = mechineType;
	}

	@Column(name = "CALL_TYPE_")
	public String getCallType() {
		return callType;
	}

	public void setCallType(String callType) {
		this.callType = callType;
	}

}
