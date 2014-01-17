package com.greenisland.taxi.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.hibernate.annotations.GenericGenerator;

import com.greenisland.taxi.common.utils.CustomDateSerializer;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-7-30上午10:32:55
 */
@Entity
@Table(name = "ts_locatioin")
public class LocationInfo {
	private String id;
	private Date gpsTime;
	private String gpsLongitude;// GPS经度
	private String gpsLatitude;// GPS纬度
	private String type;// GPS获取类型
	private String userId;
	private Date createDate;
	private Date updateDate;

	private UserInfo userInfo;

	public LocationInfo() {
		super();
	}

	public LocationInfo(String id, Date gpsTime, String gpsLongitude, String gpsLatitude, String type, String userId) {
		super();
		this.id = id;
		this.gpsTime = gpsTime;
		this.gpsLongitude = gpsLongitude;
		this.gpsLatitude = gpsLatitude;
		this.type = type;
		this.userId = userId;
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

	@Column(name = "GPS_TIME_")
	@JsonSerialize(using = CustomDateSerializer.class)
	public Date getGpsTime() {
		return gpsTime;
	}

	public void setGpsTime(Date gpsTime) {
		this.gpsTime = gpsTime;
	}

	@Column(name = "GPS_LONGITUDE_")
	public String getGpsLongitude() {
		return gpsLongitude;
	}

	public void setGpsLongitude(String gpsLongitude) {
		this.gpsLongitude = gpsLongitude;
	}

	@Column(name = "GPS_LATITUDE_")
	public String getGpsLatitude() {
		return gpsLatitude;
	}

	public void setGpsLatitude(String gpsLatitude) {
		this.gpsLatitude = gpsLatitude;
	}

	@Column(name = "TPYE_")
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Column(name = "USER_ID_")
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Column(name = "CREATE_DATE_")
	@JsonSerialize(using = CustomDateSerializer.class)
	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	@Column(name = "UPDATE_DATE_")
	@JsonSerialize(using = CustomDateSerializer.class)
	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}
	@Transient
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_ID_", insertable = false, updatable = false)
	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

}
