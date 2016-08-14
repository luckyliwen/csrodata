package com.sap.csr.odata;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.olingo.odata2.api.annotation.edm.EdmFacets;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImportParameter;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImport.ReturnType;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImport.ReturnType.Type;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImportParameter;
import org.apache.olingo.odata2.api.annotation.edm.EdmType;
import org.apache.olingo.odata2.api.exception.ODataException;
import org.eclipse.persistence.config.EntityManagerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.cloud.account.TenantContext;
import com.sap.csr.model.Donation;
import com.sap.csr.model.Project;
import com.sap.csr.model.Registration;
import com.sap.csr.model.UserInfo;
import com.sap.security.um.service.UserManagementAccessor;
import com.sap.security.um.user.PersistenceException;
import com.sap.security.um.user.UnsupportedUserAttributeException;
import com.sap.security.um.user.User;
import com.sap.security.um.user.UserProvider;

public class CSRProcessor implements ServiceConstant {
	final Logger logger = LoggerFactory.getLogger(CSRProcessor.class);
	
	private EntityManager em = null;
//	private UserInfo  currentUserInfo = null;  

	public CSRProcessor() throws NamingException, SQLException {
		
	}
	
	
	public class TeamDonation {
		public long teamId, amount;
		public TeamDonation(long teamId, long amount) {
			this.teamId = teamId;
			this.amount = amount;
		}
		
		public String toString() {
			return "teamId amount " + teamId + " " + amount;
		}
		public String toJson() {
			StringBuffer sb = new StringBuffer(60);
			sb.append("{\"TeamId\":");
			sb.append(String.valueOf(teamId));
			sb.append(", \"Amount\":");
			sb.append(String.valueOf(amount));
			sb.append("}");
			return sb.toString();
		}
	}
	
	public class AmountComparator implements Comparator<TeamDonation> {
		public int compare(TeamDonation td0, TeamDonation td1) {
			return Long.compare(td1.amount, td0.amount);
		}
	}
	
	//As now team may change dynamically, so we need get the team dynamically
		public String getTeamDonationDynamically() {
			getEntityManager();

			List<TeamDonation> tdList = new ArrayList<TeamDonation>();
			
			// get the team id list:
			String teamQueryStr = "select t.teamId from Team t where t.teamId <> 0"; 
			Query query = em.createQuery(teamQueryStr);
			List<Object> teamIds = query.getResultList();
			
			for (Object idObj : teamIds) {
				long teamId = ((Long)idObj).longValue();
				
				//then by the id try to get the register id belong to same team
				String regQueryStr = "select r.userId from Registration r where r.teamId = " + teamId;
				Query regQuery = em.createQuery(regQueryStr);
				List<Object> regIds = regQuery.getResultList();
				
				//then create the query for same team 
				StringBuffer querySb = new StringBuffer(300);
				querySb.append("select sum(d.amount) from Donation d "); 
				
				if (regIds.isEmpty())
					continue;
				
				querySb.append(" where ");
				int idx = 0;
				for (Object regIdObj: regIds) {
					if (idx>0) {
						querySb.append( " or ");
					}
					
					querySb.append( "d.donatoryId =\"");
					querySb.append(regIdObj);
					querySb.append("\" " );
					
					idx++;
				}
				
				//add to result list
				Query donationQuery = em.createQuery(querySb.toString());
				Object sumObj = donationQuery.getSingleResult();
				if (sumObj != null) {
					long  sum = (Long)sumObj;
					if ( sum != 0) {
						tdList.add( new TeamDonation(teamId, (Long)sum));
					}
				}
			}
			
			//then sort it 
			Collections.sort(tdList, new AmountComparator());
			
			//then create the json string
			StringBuffer sb = new StringBuffer(2000);
			sb.append("[");
			
			int idx = 0;
			for (TeamDonation td: tdList) {
				if (idx > 0) {
					sb.append(",");
				}
				
				sb.append(td.toJson());
				idx++;
			}
			sb.append("]");
			
			return sb.toString();
	}
		
	
	private void getEntityManager() {
		
		try {
			InitialContext ctx = new InitialContext();
			//TenantContext tenantctx = (TenantContext) ctx.lookup(HCP_TENANTCONEXT_PATH);
			//String tenantId = tenantctx.getTenant().getId();
			Map<String, Object> properties = new HashMap<String, Object>();
			//properties.put(EntityManagerProperties.MULTITENANT_PROPERTY_DEFAULT, tenantId);
			em = JpaEntityManagerFactory.getEntityManagerFactory().createEntityManager(properties);
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@EdmFunctionImport(name = FUNC_GET_MYEGISTRATION, entitySet = ES_REGISTRATION, 
		returnType = @ReturnType(type = Type.ENTITY, isCollection = false) )
	public Registration getMyRegistration() {
			Query query = null;
			UserInfo userInfo = UserMng.getCurrentUserInfo();
			
//			logger.error( "$$ get user info " + userInfo.toString());
			getEntityManager();
			
			query = em.createNamedQuery(REGISTRATION_BY_USERID);
			query.setParameter("userId", userInfo.getUserId());
			Registration reg;
			try {
				reg = (Registration)query.getSingleResult();
			} catch (Exception e) {
				Util.logException("Get singleResult for Resistrations",e);
				reg = null;
			} finally {
				em.close();
			}
			
			
			if (reg == null) {
				reg = Registration.createNewRegistration(userInfo);
			}
			
			//also use the updateFlag to tell whether is admin or not 
			if ( userInfo.isAdmin() ) {
				reg.setUpdateFlag("admin");
			}
			
			return reg;
	}

	@EdmFunctionImport(name = FUNC_GET_USRERINFO, entitySet = ES_USERINFO,
			returnType = @ReturnType(type = Type.ENTITY, isCollection = false) )
	public UserInfo getUserInfo() {
   	    return UserMng.getCurrentUserInfo();
	}
	
	@EdmFunctionImport(name = ODATA_ISREGISTED, 
			returnType = @ReturnType(type = Type.SIMPLE, isCollection = false) )
	public boolean isRegistered(){
//		List<Registration> registrations = getMyRegistration();
//		return !registrations.isEmpty();
		return false;
	}
	
	@EdmFunctionImport(name = "DelTable", 
			returnType = @ReturnType(type = Type.SIMPLE, isCollection = false) )
	public boolean delTable(@EdmFunctionImportParameter(name = "Table") String table) {
		if ( !isAdmin()) {
			throw new Error("Only admin can do this");
		}
		
		getEntityManager();
		
		String []tables = {};
		String []allTables = {"Registration", "Donation", "Attachment", "Team"};
		if (table.equals("all")) {
			tables = allTables;
		} else {
			tables= table.split(",");
		}
		
		em.getTransaction().begin();
		for (String et : tables) {
			String queryStr = "delete from " + et;
			Query query = em.createQuery(queryStr);
			query.executeUpdate();
		}
		em.getTransaction().commit();
		
		return true;
	}
	
	@EdmFunctionImport(name = "GetTotalDonation", 
			returnType = @ReturnType(type = Type.SIMPLE, isCollection = false) )
	public long getTotalDonation() {
		getEntityManager();
		
		String queryStr =  "select sum(d.amount) from Donation d ";
		Query query = em.createQuery(queryStr);
		Object sum = null;
		try {
			sum= query.getSingleResult();
			return ((Long)sum).longValue();
		} catch(Exception e) {
			return 0;
		}
	}
	
	@EdmFunctionImport(name = "GetRegistrationInfo", returnType = @ReturnType(type = Type.SIMPLE, isCollection = false) )
	public String getRegisrationInfo() {
		getEntityManager();
		
		StringBuffer sb = new StringBuffer("{\"vip\":");
		int normalApproved=0;
		
		String regQueryStr = "select count(r) from Registration r where r.status=\"Approved\" and r.vip=\"true\"";
		Query query = em.createQuery(regQueryStr);
		Object result = query.getSingleResult();
		sb.append(result.toString());
		
		
		sb.append(",\"normal\":");
		regQueryStr = "select count(r) from Registration r where r.status=\"Approved\" and r.vip=\"false\"";
		query = em.createQuery(regQueryStr);
		result = query.getSingleResult();
		sb.append(result.toString());
		normalApproved = ((Long)result).intValue();
	
		
		query = em.createNamedQuery(PROJECT_BY_MARATHON);
		Project prj = (Project)query.getSingleResult();
		int remain = prj.getFreeVipNum() + prj.getMaxRegisterNum() - normalApproved;
		
		sb.append(",\"free\":");
		sb.append( String.valueOf(remain));
		sb.append("}");
		
		return sb.toString();
	}
	
	
	@EdmFunctionImport(name = "GetStatistics", returnType = @ReturnType(type = Type.SIMPLE, isCollection = false) )
	public String getStatistics(@EdmFunctionImportParameter(name = "Top") String top) {
		getEntityManager();

		// then the registration by status:
		String regQueryStr = "select r.status, count(r) from Registration r group by r.status";
		Query query = em.createQuery(regQueryStr);

		StringBuffer sb = new StringBuffer("{\"Registration\":");
		String[] regName = { "Status", "Count" };
		boolean[] regFlag = { true, false };
		String result = formatResultAsArray(query.getResultList(), regName, regFlag);
		sb.append(result);

		// received
		/*
		 * String queryStr =
		 * "select d.donatoryId,  sum(d.amount) from Donation d " +
		 * " group by d.donatoryId order by sum(d.amount) DESC";
		 */
		String queryStr = "select d.donatoryId, d.donatoryName, sum(d.amount) from Donation d "
				+ " group by d.donatoryId, d.donatoryName order by sum(d.amount) DESC d.donatoryId <> 'CHARITY'";
		query = em.createQuery(queryStr);
		int topVal = Integer.parseInt(top);
		query.setMaxResults(topVal);

		String[] donationName = { "User", "Amount" };
		boolean[] donationFlag = { true, false };
		result = formatResultAsArray_IdName(query.getResultList(), donationName, donationFlag);

		sb.append(",\"Received\":");
		sb.append(result);

		// for all received
		queryStr = "select d.donatoryId, d.donatoryName, sum(d.amount) from Donation d "
				+ " group by d.donatoryId, d.donatoryName order by sum(d.amount) DESC where ";
		query = em.createQuery(queryStr);
		String[] donationNameEx = { "User", "Name", "Amount" };
		boolean[] donationFlagEx = { true, true, false };
		result = formatResultAsArray(query.getResultList(), donationNameEx, donationFlagEx);
		sb.append(",\"ReceivedAll\":");
		sb.append(result);

		// then for giving
		/*
		 * queryStr = "select d.donatorId,  sum(d.amount) from Donation d " +
		 * " group by d.donatorId order by sum(d.amount) DESC";
		 */

		queryStr = "select d.donatorId, d.donatorName , sum(d.amount) from Donation d "
				+ " group by d.donatorId , d.donatorName order by sum(d.amount) DESC where d.donatorId <> 'SAP' ";

		query = em.createQuery(queryStr);
		query.setMaxResults(topVal);
		// result = formatResultAsArray(query.getResultList(), donationName,
		// donationFlag);

		result = formatResultAsArray_IdName(query.getResultList(), donationName, donationFlag);

		sb.append(",\"Giving\":");
		sb.append(result);

		// for all of giving
		queryStr = "select d.donatorId, d.donatorName , sum(d.amount) from Donation d "
				+ " group by d.donatorId , d.donatorName order by sum(d.amount) DESC";
		query = em.createQuery(queryStr);
		result = formatResultAsArray(query.getResultList(), donationNameEx, donationFlagEx);
		sb.append(",\"GivingAll\":");
		sb.append(result);

		// donation by team
		//??as after donation, they may change team, so we need get dynamically
//		String[] byTeamName = { "TeamId", "Amount" };
//		boolean[] byTeamFlag = { false, false };
//		queryStr = "select d.teamId,  sum(d.amount) from Donation d "
//				+ " group by d.teamId order by sum(d.amount) DESC";
//		query = em.createQuery(queryStr);
//		result = formatResultAsArray(query.getResultList(), byTeamName, byTeamFlag);
		result = getTeamDonationDynamically();
		sb.append(",\"TeamDonation\":");
		sb.append(result);

		// donation by date
		try {
			Date minDate = null, maxDate = null;

			queryStr = "select min(d.modifiedTime) from Donation d";
			query = em.createQuery(queryStr);
			Object min = query.getSingleResult();
			if (min instanceof java.util.Date) {
				Date startDate = (Date) min;
				minDate = new Date(startDate.getYear(), startDate.getMonth(), startDate.getDate());
			}

			if (minDate != null) {

				queryStr = "select max(d.modifiedTime) from Donation d";
				query = em.createQuery(queryStr);
				Object max = query.getSingleResult();
				if (max instanceof java.util.Date) {
					Date endDate = (Date) max;
					maxDate = new Date(endDate.getYear(), endDate.getMonth(), endDate.getDate());
				}

				// now get the result day by day
				List<Object[]> list = new ArrayList<Object[]>();
				Date curDate = minDate;
				
				while (true) {

					String strDate = String.valueOf(1900 + curDate.getYear());
					int m = curDate.getMonth() + 1;
					if (m < 10) {
						strDate += '0' + String.valueOf(m);
					} else {
						strDate += String.valueOf(m);
					}

					int d = curDate.getDate();
					if (d < 10) {
						strDate += '0' + String.valueOf(d);
					} else {
						strDate += String.valueOf(d);
					}

					Object[] arr = new Object[2];
					arr[0] = strDate;

					// for query, date need be next day
					curDate.setDate(curDate.getDate() + 1);
					query = em.createNamedQuery(DONATION_BY_DATE);
					query.setParameter("modifiedTime", curDate);

					Object sum = query.getSingleResult();
					if (sum instanceof Long) {
						arr[1] = sum;
						list.add(arr);
					}

					if (curDate.after(maxDate)) {
						break;
					}
				}

				String[] byDate = { "Date", "Amount" };
				boolean[] byDateFlag = { true, false };
				result = formatResultAsArray(list, byDate, byDateFlag);
				sb.append(",\"DonationByDate\":");
				sb.append(result);
			} else {
				sb.append(",\"DonationByDate\":[]");
			}

			// logger.error(" class " + min.getClass().getName() + " val " +
			// min.toString());
			// sb.append(",\"DonationBydate\":" + min.getClass().getName()+" " +
			// min.toString());
		} catch (NoResultException e) {
			logger.error("NoResultException error", e);
			e.printStackTrace();
		}

		// last for donation, the summary and count, why when no data it will
		// get null?
		queryStr = "select sum(d.amount) from Donation d ";
		query = em.createQuery(queryStr);
		Object sum = null;
		try {
			sum = query.getSingleResult();
		} catch (Exception e) {
			sum = new String("0");
		}
		if (sum == null) {
			sum = "0";
		}

		sb.append(",\"Donation\":{ \"Total\":\"");
		sb.append(sum.toString());

		sb.append("\",\"Count\":\"");
		queryStr = "select count(d) from Donation d";
		query = em.createQuery(queryStr);
		Object count = query.getSingleResult();
		sb.append(count.toString());
		
		//also add the sap 
		sb.append("\",\"BySAP\":\"");
		Query queryBySap = em.createNamedQuery("SAPDonation");
		Donation donationBySap = (Donation)queryBySap.getSingleResult();
		sb.append( donationBySap.getAmount());
		
		
		sb.append("\"}");

		sb.append("}");
		em.close();

		return sb.toString();
	}
	
	/**
	 * 
	 * @param list
	 * @param names
	 * @param flags : whether need add the " 
	 * @return
	 */
	private String formatResultAsArray(List<Object[]> list, String[] names, boolean []flags ) {
		StringBuffer sb = new StringBuffer("[");
		int row = 0;
		for (Object[] objs: list) {
			if (row > 0) {
				sb.append(",{");
			} else {
				sb.append("{");
			}
			
			int i=0;
			for (String name : names ) {
				if (i==0) {
					sb.append("\"" + name +"\":");
				} else {
					sb.append(",\"" + name +"\":");
				}
				if ( flags[i]) {
					sb.append("\"" +  objs[i] + "\"");
				} else {
					sb.append(objs[i]);
				}
				i++;
			}
			sb.append("}");
			row ++;
		}
		sb.append("]");
		
		return sb.toString();
	}
	
	
	/**
	 * first two param is id and name, combine togeter as name(Id)
	 * @param list
	 * @param names
	 * @param flags : whether need add the " 
	 * @return
	 */
	private String formatResultAsArray_IdName(List<Object[]> list, String[] names, boolean []flags ) {
		StringBuffer sb = new StringBuffer("[");
		int row = 0;
		for (Object[] objs: list) {
			if (row > 0) {
				sb.append(",{");
			} else {
				sb.append("{");
			}
			//here need two index
			int i=0, flagIdx=0;
			for (String name : names ) {
				if (i==0) {
					sb.append("\"" + name +"\":");
				} else {
					sb.append(",\"" + name +"\":");
				}
				
				Object value = objs[i];
				if (i==0) {
					value = objs[1] + " (" + objs[0] + ")";
					i = 1;
				}
				
				if ( flags[flagIdx]) {
					sb.append("\"" +  value + "\"");
				} else {
					sb.append( value);
				}
				
				i++;
				flagIdx++;
			}
			sb.append("}");
			row ++;
		}
		sb.append("]");
		
		return sb.toString();
	}
	
	@EdmFunctionImport(name = "FixSubmittime", 
			returnType = @ReturnType(type = Type.SIMPLE, isCollection = false) )
	public boolean FixSubmittime() {
		if ( !isAdmin()) {
			throw new Error("Only admin can do this");
		}
		
		getEntityManager();
		
		em.getTransaction().begin();
		TypedQuery<Registration> query = em.createNamedQuery(REGISTRATION_NO_SUBMITTIME, Registration.class);
		List<Registration> list = query.getResultList();
		for (Registration reg : list) {
			reg.setSubmittedTime( reg.getModifiedTime());
			reg.setUpdateFlag("");
		}
		
		em.getTransaction().commit();
		em.close();
		
		return true;
		
	}
	
	//from 05/09 by 1:1, total amount = total_amount - runner club ;
	public void doDonationBySAP(long amount) {
		//for simple, we will create the default one with amount 0
		Date time = new Date();
		if (time.getDate() < 5) {
			return;
		}
		//??after notify by CSR team then change the logic here
		
		Query query = em.createNamedQuery("SAPDonation");
		Donation donation = (Donation)query.getSingleResult();
		long intialAmount = 130000;  //
		//only when still can increase then need update
		if ( donation.getAmount() < intialAmount) {
			if ( (donation.getAmount() + amount) < intialAmount) {
				donation.setAmount( donation.getAmount() + amount   );
			} else  {
				donation.setAmount( intialAmount );
			}
		}
	}
	
	//as now the olingo not support: 
	//<?xml version='1.0' encoding='UTF-8'?><error xmlns="http://schemas.microsoft.com/ado/2007/08/dataservices/metadata"><code/><message xml:lang="en">The type 'int' of the value object is not supported.</message></error>
	@EdmFunctionImport(name = "DoDonation", 
			returnType = @ReturnType(type = Type.SIMPLE, isCollection = false) )
	public boolean doDonation(@EdmFunctionImportParameter(name = "To") String to, 
			@EdmFunctionImportParameter(name = "Amount") String amount,
			@EdmFunctionImportParameter(name = "Comment") String comment) throws ODataException{
	
		getEntityManager();
		
		String [] toIds = to.split(",");
		UserInfo user = UserMng.getCurrentUserInfo();
		String donator = user.getUserId();
		Date time = new Date();
		long  amountValue = Long.parseLong(amount);
		
		UserInfo currUser = UserMng.getCurrentUserInfo();
		String donatorName = currUser.getLastName()+ " " + currUser.getFirstName();
		
		
		em.getTransaction().begin();
		for (String toId: toIds) {
			toId = toId.trim();
			
			Query query = em.createNamedQuery(REGISTRATION_BY_USERID);
			query.setParameter("userId", toId);
			Registration reg = (Registration)query.getSingleResult();
			String donatoryName = reg.getLastName()+ " " + reg.getFirstName();
			
			Donation donation = new Donation(donator, toId, time, amountValue, comment);
			donation.setDonatorName(donatorName);
			donation.setDonatoryName(donatoryName);
			
			donation.setTeamId(reg.getTeamId());
			
			em.persist(donation);
			
			//also notify them
			EmailMng em = new EmailMng();
			String email = reg.getEmail();
			String subject = "Congratulations, you got " + amountValue + "(RMB) donation from " + donatorName;
			StringBuffer sb = new StringBuffer("Hello,");
			sb.append(donatoryName);
			sb.append("\r\n\tCongratulations, you got " + amountValue + "(RMB) donation from " + donatorName + " (ID: " + donator + ").");
			if ( comment.length()>0) {
				sb.append("\r\n\tMessage: ");
				sb.append(comment);
			}
			
			try {
				em.sendEmail( email, subject, sb.toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//last two week SAP will donation also
		doDonationBySAP( toIds.length * amountValue);
		
		em.getTransaction().commit();
		em.close();
		
		
		
		
		return true;
	}
	
	
	@EdmFunctionImport(name = "DoDonationProxy", 
			returnType = @ReturnType(type = Type.SIMPLE, isCollection = false) )
	public boolean doDonationProxy(@EdmFunctionImportParameter(name = "To") String to, 
			@EdmFunctionImportParameter(name = "Amount") String amount,
			@EdmFunctionImportParameter(name = "Comment") String comment,
			@EdmFunctionImportParameter(name = "DonatorId") String donatorId,
			@EdmFunctionImportParameter(name = "DonatorName") String donatorName
			) throws ODataException{
	
		getEntityManager();
		
		String [] toIds = to.split(",");
		UserInfo user = UserMng.getCurrentUserInfo();
		
		Date time = new Date();
		long  amountValue = Long.parseLong(amount);
		
		UserInfo currUser = UserMng.getCurrentUserInfo();
		String agentName = currUser.getLastName()+ " " + currUser.getFirstName();
		
		
		em.getTransaction().begin();
		for (String toId: toIds) {
			toId = toId.trim();
			
			Query query = em.createNamedQuery(REGISTRATION_BY_USERID);
			query.setParameter("userId", toId);
			Registration reg = (Registration)query.getSingleResult();
			String donatoryName = reg.getLastName()+ " " + reg.getFirstName();
			
			Donation donation = new Donation(donatorId, toId, time, amountValue, comment);
			donation.setDonatorName(donatorName);
			donation.setDonatoryName(donatoryName);
			
//			donation.setAgent(currUser.getUserId());
//			donation.setAgentName(agentName);
			
			donation.setTeamId(reg.getTeamId());
			
			em.persist(donation);
			
			//also notify them
			EmailMng em = new EmailMng();
			String email = reg.getEmail();
			String subject = "Congratulations, you got " + amountValue + "(RMB) donation from " + donatorName;
			StringBuffer sb = new StringBuffer("Hello,");
			sb.append(donatoryName);
			sb.append("\r\n\tCongratulations, you got " + amountValue + "(RMB) donation from " + donatorName + " (ID: " + donatorId + ").");
			if ( comment.length()>0) {
				sb.append("\r\n\tMessage: ");
				sb.append(comment);
			}
			
			try {
				em.sendEmail( email, subject, sb.toString());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		em.getTransaction().commit();
		em.close();
				
		return true;
	}
	
	@EdmFunctionImport(name = "TestEmail", 
			returnType = @ReturnType(type = Type.SIMPLE, isCollection = false) )
	public boolean TestEmail(@EdmFunctionImportParameter(name = "To") String to, 
			@EdmFunctionImportParameter(name = "Subject") String subject,
			@EdmFunctionImportParameter(name = "Body") String body) throws ODataException{
		EmailMng email = new EmailMng();
		try {
			return email.sendEmail(to, subject, body);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			String error  = Util.logException("test email", e);
			
			//return false;
			throw new ODataException(error);
		}
	}
	
	@EdmFunctionImport(name = "IsAdmin", returnType = @ReturnType(type = Type.SIMPLE, isCollection = false) )	
	public boolean isAdmin(){
		UserInfo userInfo = UserMng.getCurrentUserInfo();
		return userInfo.isAdmin();
	}
	
}
