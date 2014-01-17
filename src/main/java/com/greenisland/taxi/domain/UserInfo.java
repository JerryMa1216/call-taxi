package com.greenisland.taxi.domain;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.hibernate.annotations.GenericGenerator;

import com.greenisland.taxi.common.utils.CustomDateSerializer;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-7-17下午3:41:13
 */
@Entity
@Table(name = "ts_user_info")
public class UserInfo implements Serializable {

	private static final long serialVersionUID = -3917573410575026924L;
	private String id;
	private String phoneNumber;
	private String captcha;
	private String userName;
	private Integer breakPromiseCount = 0;
	private Integer callCount = 0;
	private String address;// 常用地址
	private Integer requestCapCount;// 获取验证码次数
	private String activateState = "1";// 激活状态
	private Date breakPromissDate;// 爽约认定时间
	private String key;// 随机密钥，客户端随机生成的key，用于加密请求数据。
	private String token;// 访问令牌
	private Date createDate;
	private Date updateDate;

	private Set<CallApplyInfo> callApplyInfos;
	private Set<LocationInfo> locationInfos;

	public UserInfo() {
		super();
	}

	public UserInfo(String id, String phoneNumber, String captcha, String userName, Integer breakPromiseCount, Integer callCount, String address, String activateState, Integer requestCapCount) {
		super();
		this.id = id;
		this.phoneNumber = phoneNumber;
		this.captcha = captcha;
		this.userName = userName;
		this.breakPromiseCount = breakPromiseCount;
		this.callCount = callCount;
		this.address = address;
		this.activateState = activateState;
		this.requestCapCount = requestCapCount;
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

	@Column(name = "PHONE_NUMBER_")
	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	@Column(name = "CAPTCHA_")
	public String getCaptcha() {
		return captcha;
	}

	public void setCaptcha(String captcha) {
		this.captcha = captcha;
	}

	@Column(name = "USER_NAME_")
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Column(name = "BREAK_PROMISS_COUNT_")
	public Integer getBreakPromiseCount() {
		return breakPromiseCount;
	}

	public void setBreakPromiseCount(Integer breakPromiseCount) {
		this.breakPromiseCount = breakPromiseCount;
	}

	@Column(name = "CALL_COUNT_")
	public Integer getCallCount() {
		return callCount;
	}

	public void setCallCount(Integer callCount) {
		this.callCount = callCount;
	}

	@Column(name = "COMMON_ADDRESS_")
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Column(name = "ACTIVATE_STATE_")
	public String getActivateState() {
		return activateState;
	}

	public void setActivateState(String activateState) {
		this.activateState = activateState;
	}

	@Column(name = "REQUEST_CAP_COUNT_")
	public Integer getRequestCapCount() {
		return requestCapCount;
	}

	public void setRequestCapCount(Integer requestCapCount) {
		this.requestCapCount = requestCapCount;
	}

	@Column(name = "BREAK_PROMISS_DATE")
	@JsonSerialize(using = CustomDateSerializer.class)
	public Date getBreakPromissDate() {
		return breakPromissDate;
	}

	public void setBreakPromissDate(Date breakPromissDate) {
		this.breakPromissDate = breakPromissDate;
	}

	@OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_ID_", insertable = false, updatable = false)
	public Set<CallApplyInfo> getCallApplyInfos() {
		return callApplyInfos;
	}

	public void setCallApplyInfos(Set<CallApplyInfo> callApplyInfos) {
		this.callApplyInfos = callApplyInfos;
	}

	@OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
	@JoinColumn(name = "USER_ID_", insertable = false, updatable = false)
	public Set<LocationInfo> getLocationInfos() {
		return locationInfos;
	}

	public void setLocationInfos(Set<LocationInfo> locationInfos) {
		this.locationInfos = locationInfos;
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

	@Column(name = "KEY_")
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Column(name = "TOKEN_")
	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

}
