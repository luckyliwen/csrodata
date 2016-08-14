package com.sap.csr.model;


import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.sap.csr.odata.ServiceConstant;

/**
 * Entity implementation class for Entity: Registration
 *
 */
//@IdClass(DonationPK.class)

@Entity(name="Donation")
@NamedQueries({ 
	@NamedQuery(name=ServiceConstant.DONATION_BY_DATE, query="select sum(d.amount)  from Donation d where d.modifiedTime < :modifiedTime"),
	@NamedQuery(name="SAPDonation", query="select d from Donation d where d.donatorId = 'SAP' ")
})
public class Donation implements Serializable {

	private static long serialVersionUID = 1L;
	
	@Id  @GeneratedValue
    private long donationId;
	
	private String donatorId;   //sender
	
	private String donatoryId;   //receive
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date  modifiedTime;
	
	private String donatorName;
    private String donatoryName;
	
	private long  amount;  //
	private long teamId;
	private String comment;
	
//	private String agent; //the person will help to give money
//	private String agentName;
	
	//@JoinColumn(name="DONATORY_ID", referencedColumnName = "USERID", insertable = false, updatable = false)
	  // @ManyToOne
	  // private Registration registration;

	//@Id
		//@Column(name="MODIFIEDTIME",columnDefinition="TIMESTAMP DEFAULT CURRENT_TIME ON UPDATE CURRENT_TIME")
		//private java.sql.Timestamp  modifiedTime; 
		
		//@Column(name="MODIFIEDTIME", columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
	
	public Donation() {
		super();
	}

	public Donation(String donatorId, String donatoryId, Date modifiedTime, long amount, String comment) {
		//setDonationKey( new DonationKey(donatorId, donatoryId,modifiedTime));
		this.donatorId = donatorId;
		this.donatoryId = donatoryId;
		this.modifiedTime = modifiedTime;
		this.amount = amount;
		this.comment = comment;
	}
	
	

//	/**
//	 * @return the projectId
//	 */
//	public final long getProjectId() {
//		return projectId;
//	}
//
//	/**
//	 * @param projectId the projectId to set
//	 */
//	public final void setProjectId(long projectId) {
//		this.projectId = projectId;
//	}

//	/**
//	 * @return the donatorId
//	 */
//	public final String getDonatorId() {
//		return donatorId;
//	}
//
//	/**
//	 * @param donatorId the donatorId to set
//	 */
//	public final void setDonatorId(String donatorId) {
//		this.donatorId = donatorId;
//	}
//
//	/**
//	 * @return the donatoryId
//	 */
//	public final String getDonatoryId() {
//		return donatoryId;
//	}
//
//	/**
//	 * @param donatoryId the donatoryId to set
//	 */
//	public final void setDonatoryId(String donatoryId) {
//		this.donatoryId = donatoryId;
//	}
	

	/**
	 * @return the amount
	 */
	public final long getAmount() {
		return amount;
	}

	/**
	 * @param amount the amount to set
	 */
	public final void setAmount(long amount) {
		this.amount = amount;
	}

//	/**
//	 * @return the modifiedTime
//	 */
//	public final Date getModifiedTime() {
//		return modifiedTime;
//	}
//
//	/**
//	 * @param modifiedTime the modifiedTime to set
//	 */
//	public final void setModifiedTime(Date modifiedTime) {
//		this.modifiedTime = modifiedTime;
//	}

	@PreUpdate
	@PrePersist
	public void createModifiedTime() {
//		if (donationKey != null) {
//			donationKey.setModifiedTime(new Date());
//		}
		if ( modifiedTime == null) {
			modifiedTime = new Date();
		}
	}



//	/**
//	 * @return the donationKey
//	 */
//	public final DonationKey getDonationKey() {
//		return donationKey;
//	}
//
//
//
//	/**
//	 * @param donationKey the donationKey to set
//	 */
//	public final void setDonationKey(DonationKey donationKey) {
//		this.donationKey = donationKey;
//	}
//


//	/**
//	 * @return the registration
//	 */
//	public final Registration getRegistration() {
//		return registration;
//	}
//
//
//
//	/**
//	 * @param registration the registration to set
//	 */
//	public final void setRegistration(Registration registration) {
//		this.registration = registration;
//	}



	/**
	 * @return the comment
	 */
	public final String getComment() {
		return comment;
	}



	/**
	 * @param comment the comment to set
	 */
	public final void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the donatorId
	 */
	public final String getDonatorId() {
		return donatorId;
	}

	/**
	 * @param donatorId the donatorId to set
	 */
	public final void setDonatorId(String donatorId) {
		this.donatorId = donatorId;
	}

	/**
	 * @return the donatoryId
	 */
	public final String getDonatoryId() {
		return donatoryId;
	}

	/**
	 * @param donatoryId the donatoryId to set
	 */
	public final void setDonatoryId(String donatoryId) {
		this.donatoryId = donatoryId;
	}

	/**
	 * @return the modifiedTime
	 */
	public final Date getModifiedTime() {
		return modifiedTime;
	}

	/**
	 * @param modifiedTime the modifiedTime to set
	 */
	public final void setModifiedTime(Date modifiedTime) {
		this.modifiedTime = modifiedTime;
	}

	/**
	 * @return the donatorName
	 */
	public final String getDonatorName() {
		return donatorName;
	}

	/**
	 * @param donatorName the donatorName to set
	 */
	public final void setDonatorName(String donatorName) {
		this.donatorName = donatorName;
	}

	/**
	 * @return the donatoryName
	 */
	public final String getDonatoryName() {
		return donatoryName;
	}

	/**
	 * @param donatoryName the donatoryName to set
	 */
	public final void setDonatoryName(String donatoryName) {
		this.donatoryName = donatoryName;
	}

	/**
	 * @return the teamId
	 */
	public final long getTeamId() {
		return teamId;
	}

	/**
	 * @param teamId the teamId to set
	 */
	public final void setTeamId(long teamId) {
		this.teamId = teamId;
	}

	/**
	 * @return the donationId
	 */
	public final long getDonationId() {
		return donationId;
	}

	/**
	 * @param donationId the donationId to set
	 */
	public final void setDonationId(long donationId) {
		this.donationId = donationId;
	}

//	/**
//	 * @return the agent
//	 */
//	public final String getAgent() {
//		return agent;
//	}
//
//	/**
//	 * @param agent the agent to set
//	 */
//	public final void setAgent(String agent) {
//		this.agent = agent;
//	}
//
//	/**
//	 * @return the agentName
//	 */
//	public final String getAgentName() {
//		return agentName;
//	}
//
//	/**
//	 * @param agentName the agentName to set
//	 */
//	public final void setAgentName(String agentName) {
//		this.agentName = agentName;
//	}	
}