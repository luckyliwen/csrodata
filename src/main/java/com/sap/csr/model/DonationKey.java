package com.sap.csr.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Embeddable
public class DonationKey implements Serializable {
	    @Column( name = "DONATOR_ID")
		private String donatorId;   //sender
	    
		@Column( name = "DONATORY_ID")
		private String donatoryId;   //receive
		
		@Temporal(TemporalType.TIMESTAMP)
		private Date modifiedTime;
		

		/**
		 * @param donatorId
		 * @param donatoryId
		 * @param modifiedTime
		 */
		public DonationKey(String donatorId, String donatoryId, Date modifiedTime) {
			super();
			this.donatorId = donatorId;
			this.donatoryId = donatoryId;
			this.modifiedTime = modifiedTime;
		}

		/**
		 * 
		 */
		public DonationKey() {
			super();
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

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((donatorId == null) ? 0 : donatorId.hashCode());
			result = prime * result + ((donatoryId == null) ? 0 : donatoryId.hashCode());
			result = prime * result + ((modifiedTime == null) ? 0 : modifiedTime.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DonationKey other = (DonationKey) obj;
			if (donatorId == null) {
				if (other.donatorId != null)
					return false;
			} else if (!donatorId.equals(other.donatorId))
				return false;
			if (donatoryId == null) {
				if (other.donatoryId != null)
					return false;
			} else if (!donatoryId.equals(other.donatoryId))
				return false;
			if (modifiedTime == null) {
				if (other.modifiedTime != null)
					return false;
			} else if (!modifiedTime.equals(other.modifiedTime))
				return false;
			return true;
		}
}
