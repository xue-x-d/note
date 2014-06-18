package com.shomop.crm.model;

import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "user")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY) // 表示开启二级缓存，并使用read-only策略
//@Cacheable 开启查询缓存
public class User implements Identifier<String>, Cloneable {

	private String id;
	private String username; // 用户名
	private String salt; // 加密随机数
	private String hash; // 密码hash值
	private String userMobileNumber; // 用户手机号码
	private int mktBalance; // 余额，剩余营销短信条数
	private int appBalance; // 余额，剩余应用短信条数
	private String signature; // 企业短信签名
	private Date creationDate;
	private Date updateDate;
	private String accessToken; // 淘宝授权码
	private Date authDate; // 授权时间
	private Date expiresTime; // r2过期时间
	private boolean deleted;
	private boolean isPermit;
	private long userId;
	private String refreshToken; // 刷新码
	private Date purchaseTime;// 订购时间
	private Date lastPurchaseTime;// 最后赠送短信时间
	private boolean subscribeBriefing;// 是否订阅简报
	private int purchaseVersion; // 购买版本
	private long sid;// 店铺id,shop+sid.taobao.com
	private long lastDownBuyerTime;

	// private long level;
	@Id
	@Column(name = "id", length = 32)
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "uuid")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "username", length = 50)
	public String getUsername() {
		return username;
	}

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Column(name = "hash", length = 32)
	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	@Column(name = "salt", length = 32)
	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	@Column(name = "userMobileNumber", length = 20)
	public String getUserMobileNumber() {
		return userMobileNumber;
	}

	public void setUserMobileNumber(String userMobileNumber) {
		this.userMobileNumber = userMobileNumber;
	}

	@Column(name = "mktBalance")
	public int getMktBalance() {
		return mktBalance;
	}

	public void setMktBalance(int mktBalance) {
		this.mktBalance = mktBalance;
	}

	@Column(name = "appBalance")
	public int getAppBalance() {
		return appBalance;
	}

	public void setAppBalance(int appBalance) {
		this.appBalance = appBalance;
	}

	@Column(name = "signature", length = 20)
	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	@Column(name = "creationDate")
	@Type(type = "bigIntTime")
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Column(name = "updateDate")
	@Type(type = "bigIntTime")
	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	@Column(name = "expiresTime")
	@Type(type = "bigIntTime")
	public Date getExpiresTime() {
		return expiresTime;
	}

	public void setExpiresTime(Date expiresTime) {
		this.expiresTime = expiresTime;
	}

	@Column(name = "purchaseTime")
	@Type(type = "bigIntTime")
	public Date getPurchaseTime() {
		return purchaseTime;
	}

	public void setPurchaseTime(Date purchaseTime) {
		this.purchaseTime = purchaseTime;
	}

	@Column(name = "accessToken", columnDefinition = "TEXT")
	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	@Column(name = "refreshToken", columnDefinition = "TEXT")
	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	@Column(name = "authDate")
	@Type(type = "bigIntTime")
	public Date getAuthDate() {
		return authDate;
	}

	public void setAuthDate(Date authDate) {
		this.authDate = authDate;
	}

	public boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public boolean getIsPermit() {
		return isPermit;
	}

	public void setIsPermit(boolean isPermit) {
		this.isPermit = isPermit;
	}

	@Override
	public User clone() throws CloneNotSupportedException {
		return (User) super.clone();
	}

	@Column(name = "lastPurchaseTime")
	@Type(type = "bigIntTime")
	public Date getLastPurchaseTime() {
		return lastPurchaseTime;
	}

	public void setLastPurchaseTime(Date lastPurchaseTime) {
		this.lastPurchaseTime = lastPurchaseTime;
	}

	public boolean getSubscribeBriefing() {
		return subscribeBriefing;
	}

	public void setSubscribeBriefing(boolean subscribeBriefing) {
		this.subscribeBriefing = subscribeBriefing;
	}

	@Column(name = "purchaseVersion", columnDefinition = "TINYINT")
	public int getPurchaseVersion() {
		return purchaseVersion;
	}

	public void setPurchaseVersion(int purchaseVersion) {
		this.purchaseVersion = purchaseVersion;
	}

	public long getSid() {
		return sid;
	}

	public void setSid(long sid) {
		this.sid = sid;
	}

	public long getLastDownBuyerTime() {
		return lastDownBuyerTime;
	}

	public void setLastDownBuyerTime(long lastDownBuyerTime) {
		this.lastDownBuyerTime = lastDownBuyerTime;
	}
	// @Column(columnDefinition = "TINYINT")
	// public long getLevel() {
	// return level;
	// }
	//
	// public void setLevel(long level) {
	// this.level = level;
	// }

}
