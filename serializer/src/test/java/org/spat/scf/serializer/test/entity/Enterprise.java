package org.spat.scf.serializer.test.entity;

import java.util.Date;

import org.spat.scf.serializer.annotation.SCFMember;
import org.spat.scf.serializer.annotation.SCFSerializable;

@SuppressWarnings("serial")

@SCFSerializable
public class Enterprise extends EntityBase {


	@SCFMember
	private long id; // 主键

	@SCFMember
	private long pid; // 上级单位ID

	@SCFMember
	private long adminid; // 管理员ID

	@SCFMember
	private String legalPerson; // 法人

	@SCFMember
	private String name; // 企业名称

	@SCFMember
	private int capitalRegistered;// 注册资金

	public int getCapitalRegistered() {
		return capitalRegistered;
	}

	public void setCapitalRegistered(int capitalRegistered) {
		this.capitalRegistered = capitalRegistered;
	}

	@SCFMember
	private int enterpriseType;// 企业 类型

	public int getEnterpriseType() {
		return enterpriseType;
	}

	public void setEnterpriseType(int enterpriseType) {
		this.enterpriseType = enterpriseType;
	}

	@SCFMember
	private int cityid; // 所在城市ID

	@SCFMember
	private String address; // 企业地址


	@SCFMember
	private String zipcode; // 邮政编码


	@SCFMember
	private String telphone; // 联系电话


	@SCFMember
	private String businessField; // 经营范围


	@SCFMember
	private Date startupdate; // 该企业的成立日期


	@SCFMember
	private Date fromdate; // 营业期限起始日期


	@SCFMember
	private Date todate; // 营业期限结束日期


	@SCFMember
	private String homepage;


	@SCFMember
	private String logo;


	@SCFMember
	private String introduction; // 企业简介


	@SCFMember
	private int authstate; // 审核状态


	@SCFMember
	private Date updateDate; // 该记录更新时间

	@SCFMember
	private Date createdate; // 该记录创建时间


	@SCFMember
	private boolean deleteFlag;

	@SCFMember
	private String params; // 扩展数据

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getPid() {
		return pid;
	}

	public void setPid(long pid) {
		this.pid = pid;
	}

	public long getAdminid() {
		return adminid;
	}

	public void setAdminid(long adminid) {
		this.adminid = adminid;
	}

	public String getLegalPerson() {
		return legalPerson;
	}

	public void setLegalPerson(String legalPerson) {
		this.legalPerson = legalPerson;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCityid() {
		return cityid;
	}

	public void setCityid(int cityid) {
		this.cityid = cityid;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getZipcode() {
		return zipcode;
	}

	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}

	public String getTelphone() {
		return telphone;
	}

	public void setTelphone(String telphone) {
		this.telphone = telphone;
	}

	public String getBusinessField() {
		return businessField;
	}

	public void setBusinessField(String businessField) {
		this.businessField = businessField;
	}

	public Date getStartupdate() {
		return startupdate;
	}

	public void setStartupdate(Date startupdate) {
		this.startupdate = startupdate;
	}

	public Date getFromdate() {
		return fromdate;
	}

	public void setFromdate(Date fromdate) {
		this.fromdate = fromdate;
	}

	public Date getTodate() {
		return todate;
	}

	public void setTodate(Date todate) {
		this.todate = todate;
	}

	public String getHomepage() {
		return homepage;
	}

	public void setHomepage(String homepage) {
		this.homepage = homepage;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getIntroduction() {
		return introduction;
	}

	public void setIntroduction(String introduction) {
		this.introduction = introduction;
	}

	public int getAuthstate() {
		return authstate;
	}

	public void setAuthstate(int authstate) {
		this.authstate = authstate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public Date getCreatedate() {
		return createdate;
	}

	public void setCreatedate(Date createdate) {
		this.createdate = createdate;
	}

	public boolean getDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(boolean deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public void Commit() {

	}

	@Override
	public String getCacheKey() {
		return EntityBase.generateCacheKey(getId(), this.getClass());
	}

}
