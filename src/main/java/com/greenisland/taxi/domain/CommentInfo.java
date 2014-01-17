package com.greenisland.taxi.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.hibernate.annotations.GenericGenerator;

import com.greenisland.taxi.common.utils.CustomDateSerializer;

/**
 * 
 * @author Jerry
 * @E-mail jerry.ma@bstek.com
 * @version 2013-7-30上午10:43:27
 */
@Entity
@Table(name = "ts_comment")
public class CommentInfo {
	private String id;
	private Integer level;
	private String content;
	private String applyId;
	private Date createDate;
	private Date updateDate;
	private CallApplyInfo applyInfo;

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

	@Column(name = "COMMENT_LEVEL_")
	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	@Column(name = "COMMENT_CONTENT_")
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public CommentInfo() {
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
	@Transient
	public CallApplyInfo getApplyInfo() {
		return applyInfo;
	}

	public void setApplyInfo(CallApplyInfo applyInfo) {
		this.applyInfo = applyInfo;
	}

	@Column(name = "APPLY_ID_")
	public String getApplyId() {
		return applyId;
	}

	public void setApplyId(String applyId) {
		this.applyId = applyId;
	}

}
