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
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.hibernate.annotations.GenericGenerator;

import com.greenisland.taxi.common.utils.CustomDateSerializer;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-7-30上午10:42:41
 */
@Entity
@Table(name = "ts_company")
public class CompanyInfo {
	private String id;
	private String name;
	private Date createDate;
	private Date updateDate;

	private Set<TaxiInfo> taxiInfos;

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

	@Column(name = "COMPANY_NAME_")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CompanyInfo() {
		super();
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

	@OneToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
	@JoinColumn(name = "COMPANY_ID_", insertable = false, updatable = false)
	public Set<TaxiInfo> getTaxiInfos() {
		return taxiInfos;
	}

	public void setTaxiInfos(Set<TaxiInfo> taxiInfos) {
		this.taxiInfos = taxiInfos;
	}

	public CompanyInfo(String id, String name, Date createDate, Date updateDate) {
		super();
		this.id = id;
		this.name = name;
		this.createDate = createDate;
		this.updateDate = updateDate;
	}

}
