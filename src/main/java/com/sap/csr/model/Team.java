
package com.sap.csr.model;

import java.beans.Transient;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PostLoad;
import javax.persistence.Query;

import org.eclipse.persistence.annotations.JoinFetch;
import org.eclipse.persistence.annotations.JoinFetchType;

import com.sap.csr.odata.JpaEntityManagerFactory;
import com.sap.csr.odata.ServiceConstant;


@Entity(name="Team")
public class Team implements Serializable {

	@Id  @GeneratedValue
    private long teamId;
	 
	private String ownerId;
	
	@Column(unique=true)
	private String name;
	
	private String memberList;
	
	 transient private 	 EntityManager em = null;

	public Team() {
		super();
	}
	
	public EntityManager getEntityManager() {
		
		try {
			InitialContext ctx = new InitialContext();
			//TenantContext tenantctx = (TenantContext) ctx.lookup(HCP_TENANTCONEXT_PATH);
			//String tenantId = tenantctx.getTenant().getId();
			Map<String, Object> properties = new HashMap<String, Object>();
			//properties.put(EntityManagerProperties.MULTITENANT_PROPERTY_DEFAULT, tenantId);
//			EntityManager
			em = JpaEntityManagerFactory.getEntityManagerFactory().createEntityManager(properties);
			return em; 
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@PostLoad
	public void autoGetMemberList() {
//		EntityManager em = 
		getEntityManager();
		String regQueryStr = "select r.userId, r.lastName, r.firstName from Registration r where r.teamId = :teamId";
		Query query = em.createQuery(regQueryStr);
		query.setParameter("teamId", teamId);
		
		List<Object> list = query.getResultList();
		StringBuffer sb = new StringBuffer("");
		for (Object row: list) {
			if ( row instanceof Object[]) {
				Object[] aInfo = (Object[])row;
				sb.append(aInfo[0].toString());
				sb.append(":");
				sb.append(aInfo[1]+","+aInfo[2]+";");
			}
		}
		memberList = sb.toString();
	}
	
//	/**
//	 * @return the groupId
//	 */
//	public final long getGroupId() {
//		return groupId;
//	}
//	/**
//	 * @param groupId the groupId to set
//	 */
//	public final void setGroupId(long groupId) {
//		this.groupId = groupId;
//	}
	/**
	 * @return the ownerId
	 */
	public final String getOwnerId() {
		return ownerId;
	}
	/**
	 * @param ownerId the ownerId to set
	 */
	public final void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}
	/**
	 * @return the name
	 */
	public final String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public final void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the memberList
	 */
	public final String getMemberList() {
		return memberList;
	}
	/**
	 * @param memberList the memberList to set
	 */
	public final void setMemberList(String memberList) {
		this.memberList = memberList;
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
	
	
}