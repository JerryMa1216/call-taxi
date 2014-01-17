package com.greenisland.taxi.domain;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.GenericGenerator;

/**
 * 出租车对象
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-7-30上午10:26:56
 */
@Entity
@Table(name = "ts_taxi_info")
public class TaxiInfo {
	private String id;
	private String taxiPlateNumber;
	private String driverName;
	private String dirverPhoneNumber;// 司机手机号
	private Integer breakPromiseCount;// 爽约次数
	private String companyId;// 集团公司id
	private String isEmpty;// 载客状态
	private String speed;// 速度
	private String longitude;//经度
	private String latitude;//纬度
	private String gpsTime;//gps时间
	private Date createDate;
	private Date updateDate;

	private CompanyInfo companyInfo;
	private Set<CallApplyInfo> callApplyInfos;// 订单信息

	public TaxiInfo(String id, String taxiPlateNumber, String driverName, String dirverPhoneNumber, Integer breakPromiseCount, String companyId, String isEmpty, String speed) {
		super();
		this.id = id;
		this.taxiPlateNumber = taxiPlateNumber;
		this.driverName = driverName;
		this.dirverPhoneNumber = dirverPhoneNumber;
		this.breakPromiseCount = breakPromiseCount;
		this.companyId = companyId;
		this.isEmpty = isEmpty;
		this.speed = speed;
	}

	public TaxiInfo() {
	}

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

	@Column(name = "TAXI_PLATE_NUMBER_")
	public String getTaxiPlateNumber() {
		return taxiPlateNumber;
	}

	public void setTaxiPlateNumber(String taxiPlateNumber) {
		this.taxiPlateNumber = taxiPlateNumber;
	}

	@Column(name = "DRIVER_NAME_")
	public String getDriverName() {
		return driverName;
	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	@Column(name = "DRIVER_PHONE_NUMBER_")
	public String getDirverPhoneNumber() {
		return dirverPhoneNumber;
	}

	public void setDirverPhoneNumber(String dirverPhoneNumber) {
		this.dirverPhoneNumber = dirverPhoneNumber;
	}

	@Column(name = "BREAK_PROMISS_COUNT_")
	public Integer getBreakPromiseCount() {
		return breakPromiseCount;
	}

	public void setBreakPromiseCount(Integer breakPromiseCount) {
		this.breakPromiseCount = breakPromiseCount;
	}

	@Column(name = "COMPANY_ID_")
	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	@Column(name = "IS_EMPTY_")
	public String getIsEmpty() {
		return isEmpty;
	}

	public void setIsEmpty(String isEmpty) {
		this.isEmpty = isEmpty;
	}
	@Transient
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "COMPANY_ID_", insertable = false, updatable = false)
	public CompanyInfo getCompanyInfo() {
		return companyInfo;
	}

	public void setCompanyInfo(CompanyInfo companyInfo) {
		this.companyInfo = companyInfo;
	}

	@Column(name = "CREATE_DATE_")
	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	@Column(name = "UPDATE_DATE_")
	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	@OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
	@JoinColumn(name = "TAXI_ID_", insertable = false, updatable = false)
	public Set<CallApplyInfo> getCallApplyInfos() {
		return callApplyInfos;
	}

	public void setCallApplyInfos(Set<CallApplyInfo> callApplyInfos) {
		this.callApplyInfos = callApplyInfos;
	}

	@Column(name = "SPEED_")
	public String getSpeed() {
		return speed;
	}

	public void setSpeed(String speed) {
		this.speed = speed;
	}
	@Column(name="LONGITUDE_")
	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	@Column(name="LATITUDE_")
	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	@Column(name="GPS_TIME_")
	public String getGpsTime() {
		return gpsTime;
	}

	public void setGpsTime(String gpsTime) {
		this.gpsTime = gpsTime;
	}
}
