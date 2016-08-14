package com.sap.csr.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

import com.sap.csr.odata.ServiceConstant;

@Entity(name="Project")
@NamedQueries({ 
	@NamedQuery(name=ServiceConstant.PROJECT_BY_MARATHON, query="select p from Project p where p.projectName = \"marathon2016\""),
})

public class Project implements Serializable {
	@Id
	private String projectName;
	private String vipIds;
	
	private int maxRegisterNum;
	private int freeVipNum;  //reversed vip ids, then not enough regisger, so can add to the maxRegisterNum
	
	public Project() {
		
	}

	/**
	 * @return the projectName
	 */
	public final String getProjectName() {
		return projectName;
	}

	/**
	 * @param projectName the projectName to set
	 */
	public final void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	/**
	 * @return the vipIds
	 */
	public final String getVipIds() {
		return vipIds;
	}

	/**
	 * @param vipIds the vipIds to set
	 */
	public final void setVipIds(String vipIds) {
		this.vipIds = vipIds;
	}

	/**
	 * @return the maxRegisterNum
	 */
	public final int getMaxRegisterNum() {
		return maxRegisterNum;
	}

	/**
	 * @param maxRegisterNum the maxRegisterNum to set
	 */
	public final void setMaxRegisterNum(int maxRegisterNum) {
		this.maxRegisterNum = maxRegisterNum;
	}

	/**
	 * @return the freeVipNum
	 */
	public final int getFreeVipNum() {
		return freeVipNum;
	}

	/**
	 * @param freeVipNum the freeVipNum to set
	 */
	public final void setFreeVipNum(int freeVipNum) {
		this.freeVipNum = freeVipNum;
	}
	
	public long getTotalNum() {
		return freeVipNum + maxRegisterNum;
	}

	
	
}

