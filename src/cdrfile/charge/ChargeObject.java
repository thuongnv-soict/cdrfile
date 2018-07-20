package cdrfile.charge;

/**
 * <p>Title: CDR File(s) System</p>
 * <p>Description: VMS IS Departerment</p>
 * <p>Copyright: Copyright (c)  by eKnowledge 2004</p>
 * <p>Company: VietNam Mobile Telecom Services</p>
 * @author eKnowledge - Software
 * @version 1.0
 */

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import oracle.jdbc.OraclePreparedStatement;
import cdrfile.global.DelimitedFile;
import cdrfile.global.Global;
import cdrfile.global.IOUtils;
import cdrfile.global.StringUtils;
import cdrfile.global.TextFile;
import cdrfile.global.cdrfileParam;

public class ChargeObject extends Global {
	protected ChargeTariff CacheChargeTariffOGIntl = null;
	protected Vector vCacheChargeTariffOGIntl = new Vector();
	protected ChargeTariff CacheChargeTariffICIntl = null;
	protected Vector vCacheChargeTariffICIntl = new Vector();
	protected ChargeTariff CacheChargeTariffOGNatl = null;
	protected Vector vCacheChargeTariffOGNatl = new Vector();
	protected ChargeTariff CacheChargeTariffICNatl = null;
	protected Vector vCacheChargeTariffICNatl = new Vector();
	protected ChargeTariff CacheChargeTariffSMOIntl = null;
	protected Vector vCacheChargeTariffSMOIntl = new Vector();
	protected ChargeTariff CacheChargeTariffSMONatl = null;
	protected Vector vCacheChargeTariffSMONatl = new Vector();
	protected ChargeTariff CacheChargeTariffSMTIntl = null;
	protected Vector vCacheChargeTariffSMTIntl = new Vector();
	protected ChargeTariff CacheChargeTariffSMTNatl = null;
	protected Vector vCacheChargeTariffSMTNatl = new Vector();
	protected ChargeTariff CacheChargeTariffCALLServices = null;
	protected Vector vCacheChargeTariffCALLServices = new Vector();
	protected ChargeClass CacheChargeClass = null;
	protected Vector vCacheChargeClass = new Vector();
	protected ChargePlan CacheChargePlan = null;
	protected Vector vCacheChargePlan = new Vector();
	protected ChargeArea CacheChargeArea = null;
	protected Vector vCacheChargeArea = new Vector();
	protected int iChargeClassAir = -1;
	protected int iChargeClassIdd = -1;
	protected int iChargeClassSer = -1;
	protected int iChargeTariffSer = -1;
	protected ChargePromotion CacheChargeCALLPromotion = null;
	protected Vector vCacheChargeCALLPromotion = new Vector();
	protected ChargePromotion CacheChargeSMSPromotion = null;
	protected Vector vCacheChargeSMSPromotion = new Vector();
	protected ChargeResult chargeResult = new ChargeResult();
	protected DelimitedFile delimitedFile = new DelimitedFile();
	protected ListIMSI listIMSI = null;
	protected Vector vListIMSI = new Vector();
	protected ListISDN listISDN = null;
	protected Vector vListISDN = new Vector();
	protected String mStrValues = "";
	protected boolean FindCenter;
	protected static final int mChargeAirtime = 1;
	protected static final int mChargeIdd = 2;
	protected static final int mChargeServices = 3;
	IOUtils IOUtil = new IOUtils();

	public ChargeObject(Connection pConnection) throws Exception {
		LoadChargeCALLPromotion(pConnection);
		LoadChargeSMSPromotion(pConnection);
		LoadChargeArea(pConnection);
		LoadChargePlan(pConnection);
		LoadChargeClass(pConnection);
		LoadChargeTariffCALLServices(pConnection);
		LoadChargeTariffOGIntl(pConnection);
		LoadChargeTariffICIntl(pConnection);
		LoadChargeTariffOGNatl(pConnection);
		LoadChargeTariffICNatl(pConnection);
		LoadChargeTariffSMOIntl(pConnection);
		LoadChargeTariffSMTIntl(pConnection);
		LoadChargeTariffSMONatl(pConnection);
		LoadChargeTariffSMTNatl(pConnection);
	}

	public void finalize() {
		if (vListIMSI != null) {
			vListIMSI.removeAllElements();
			vListIMSI.clear();
			vListIMSI = null;
		}
		if (listIMSI != null) {
			listIMSI = null;
		}
		if (vListISDN != null) {
			vListISDN.removeAllElements();
			vListISDN.clear();
			vListISDN = null;
		}
		if (listISDN != null) {
			listISDN = null;
		}
		System.runFinalization();
		System.gc();
	}

	public void LoadIMSI(Connection pConnection) throws Exception {
		Statement stmt = pConnection.createStatement();
		ResultSet rs = stmt
				.executeQuery("SELECT * FROM imsi_prefix ORDER BY LENGTH(imsi_header) DESC,imsi_header");
		if (vListIMSI != null) {
			vListIMSI.removeAllElements();
			vListIMSI.clear();
		}
		try {
			while (rs.next()) {
				// Add param to vector vListISDN
				listIMSI = new ListIMSI(rs.getString("imsi_header"), rs
						.getInt("center_id"), rs.getInt("subscriber_type"));
				vListIMSI.add(listIMSI);
			}
		} catch (Exception ex) {
		} finally {
			try {
				rs.close();
				rs = null;
				stmt.close();
				stmt = null;
			} catch (Exception e) {
			}
		}
	}

	public void LoadISDN(Connection pConnection) throws Exception {
		Statement stmt = pConnection.createStatement();
		ResultSet rs = stmt
				.executeQuery("SELECT * FROM isdn_prefix ORDER BY LENGTH(isdn_header) DESC,isdn_header");
		if (vListISDN != null) {
			vListISDN.removeAllElements();
			vListISDN.clear();
		}
		try {
			while (rs.next()) {
				// Add param to vector vListISDN
				listISDN = new ListISDN(rs.getString("isdn_header"), rs
						.getInt("center_id"), rs.getInt("subscriber_type"));
				vListISDN.add(listISDN);
			}
		} catch (Exception ex) {
		} finally {
			try {
				rs.close();
				rs = null;
				stmt.close();
				stmt = null;
			} catch (Exception e) {
			}
		}
	}

	protected void LoadChargeClass(Connection pConnection) throws Exception {
		int iIndexOfTariffPlanID = -1;
		boolean Found = false;
		String mSQL = "SELECT tariff_class_id,time_destroy,time_min,time_block,"
				+ "to_char(date_from,'yyyymmddhh24miss') date_from,"
				+ "to_char(date_to,'yyyymmddhh24miss') date_to "
				+ "FROM tariff_class_detail WHERE effect=1 "
				+ "ORDER BY tariff_class_id,date_from DESC";
		Statement stmt = pConnection.createStatement();
		ResultSet rs = stmt.executeQuery(mSQL);
		if (vCacheChargeClass != null) {
			vCacheChargeClass.removeAllElements();
			vCacheChargeClass.clear();
		}
		try {
			while (rs.next()) {
				iIndexOfTariffPlanID = -1;
				for (int i = 0; i < vCacheChargePlan.size(); i++) {
					CacheChargePlan = (ChargePlan) vCacheChargePlan.get(i);
					if (CacheChargePlan.getChargeClassID() == rs
							.getInt("tariff_class_id")) {
						Found = true;
						iIndexOfTariffPlanID = i;
						break;
					}
				}
				if (Found == true) {
					// Add param to vector
					CacheChargeClass = new ChargeClass(rs
							.getInt("tariff_class_id"),
							rs.getLong("date_from"), rs.getLong("date_to"), rs
									.getInt("time_destroy"), rs
									.getInt("time_min"), rs
									.getInt("time_block"), iIndexOfTariffPlanID);
					vCacheChargeClass.add(CacheChargeClass);
				} else {
					writeLogFile(" - "
							+ "Unused tariff_class readiness effective : "
							+ rs.getInt("tariff_charge_id"));
				}
			}
		} catch (Exception ex) {
			System.err.println(Global.Format(new java.util.Date(),
					"dd/MM/yyyy HH:mm:ss")
					+ " : ERROR in module LoadChargeClass : " + ex.toString());
		} finally {
			try {
				rs.close();
				rs = null;
				stmt.close();
				stmt = null;
			} catch (Exception e) {
			}
		}
	}

	protected void LoadChargePlan(Connection pConnection) throws Exception {
		int iChargeAreaFirstBlock = -1;
		int iChargeAreaNextBlock = -1;
		boolean Found = false;
		String mSQL = "SELECT tariff_class_id, "
				+ "to_char(date_from,'yyyymmddhh24miss') date_from,"
				+ "to_char(date_to,'yyyymmddhh24miss') date_to,time_from,"
				+ "time_to,first_block,next_block,first_block_charge,"
				+ "next_block_charge,collect_type,week_info "
				+ "FROM tariff_plan ORDER BY tariff_class_id,"
				+ "week_info desc,date_from desc,time_from";
		Statement stmt = pConnection.createStatement();
		ResultSet rs = stmt.executeQuery(mSQL);
		if (vCacheChargePlan != null) {
			vCacheChargePlan.removeAllElements();
			vCacheChargePlan.clear();
		}
		try {
			while (rs.next()) {
				iChargeAreaFirstBlock = -1;
				iChargeAreaNextBlock = -1;
				for (int i = 0; i < vCacheChargeArea.size(); i++) {
					CacheChargeArea = (ChargeArea) vCacheChargeArea.get(i);
					if (CacheChargeArea.getAreaChargeID().compareTo(
							rs.getString("first_block_charge")) == 0) {
						Found = true;
						iChargeAreaFirstBlock = i;
						break;
					}
				}
				if (!Found)
					writeLogFile(" - Undefined Charge_Area : "
							+ rs.getString("first_block_charge")
							+ " in table Tariff_Plan : "
							+ rs.getInt("tariff_charge_id"));
				Found = false;
				for (int i = 0; i < vCacheChargeArea.size(); i++) {
					CacheChargeArea = (ChargeArea) vCacheChargeArea.get(i);
					if (CacheChargeArea.getAreaChargeID().compareTo(
							rs.getString("next_block_charge")) == 0) {
						Found = true;
						iChargeAreaNextBlock = i;
						break;
					}
				}
				if (!Found)
					writeLogFile(" - Undefined Charge_Area : "
							+ rs.getString("next_block_charge")
							+ " in table Tariff_Plan : "
							+ rs.getInt("tariff_charge_id"));

				if (Found == true)
					// Add param to vector
					CacheChargePlan = new ChargePlan(rs
							.getInt("tariff_class_id"),
							rs.getLong("date_from"), rs.getLong("date_to"), rs
									.getInt("time_from"), rs.getInt("time_to"),
							rs.getInt("first_block"), rs.getInt("next_block"),
							rs.getInt("collect_type"), rs.getInt("week_info"),
							iChargeAreaFirstBlock, iChargeAreaNextBlock);
				vCacheChargePlan.add(CacheChargePlan);
			}
		} catch (Exception ex) {
			System.err.println(Global.Format(new java.util.Date(),
					"dd/MM/yyyy HH:mm:ss")
					+ " : ERROR in module LoadChargePlan : " + ex.toString());
		} finally {
			try {
				rs.close();
				rs = null;
				stmt.close();
				stmt = null;
			} catch (Exception e) {
			}
		}
	}

	protected void LoadChargeCALLPromotion(Connection pConnection)
			throws Exception {
		String mSQL = "SELECT to_char(date_from,'yyyymmddhh24miss') date_from,"
				+ "to_char(date_to,'yyyymmddhh24miss') date_to,"
				+ "time_from,time_to,percentage "
				+ "FROM date_promotion WHERE record_type='CALL' "
				+ "ORDER BY date_from desc,time_from ";
		Statement stmt = pConnection.createStatement();
		ResultSet rs = stmt.executeQuery(mSQL);
		if (vCacheChargeCALLPromotion != null) {
			vCacheChargeCALLPromotion.removeAllElements();
			vCacheChargeCALLPromotion.clear();
		}
		try {
			while (rs.next()) {
				// Add param to vector CacheChargePromotion
				CacheChargeCALLPromotion = new ChargePromotion(rs
						.getLong("date_from"), rs.getLong("date_to"), rs
						.getInt("time_from"), rs.getInt("time_to"), rs
						.getDouble("percentage"));
				vCacheChargeCALLPromotion.add(CacheChargeCALLPromotion);
			}
		} catch (Exception ex) {
			System.err.println(Global.Format(new java.util.Date(),
					"dd/MM/yyyy HH:mm:ss")
					+ " : ERROR in module LoadChargeCALLPromotion : "
					+ ex.toString());
		} finally {
			try {
				rs.close();
				rs = null;
				stmt.close();
				stmt = null;
			} catch (Exception e) {
			}
		}
	}

	protected void LoadChargeSMSPromotion(Connection pConnection)
			throws Exception {
		String mSQL = "SELECT to_char(date_from,'yyyymmddhh24miss') date_from,"
				+ "to_char(date_to,'yyyymmddhh24miss') date_to,"
				+ "time_from,time_to,percentage "
				+ "FROM date_promotion WHERE record_type='SMS' "
				+ "ORDER BY date_from desc,time_from ";
		Statement stmt = pConnection.createStatement();
		ResultSet rs = stmt.executeQuery(mSQL);
		if (vCacheChargeSMSPromotion != null) {
			vCacheChargeSMSPromotion.removeAllElements();
			vCacheChargeSMSPromotion.clear();
		}
		try {
			while (rs.next()) {
				// Add param to vector CacheChargePromotion
				CacheChargeSMSPromotion = new ChargePromotion(rs
						.getLong("date_from"), rs.getLong("date_to"), rs
						.getInt("time_from"), rs.getInt("time_to"), rs
						.getDouble("percentage"));
				vCacheChargeSMSPromotion.add(CacheChargeSMSPromotion);
			}
		} catch (Exception ex) {
			System.err.println(Global.Format(new java.util.Date(),
					"dd/MM/yyyy HH:mm:ss")
					+ " : ERROR in module LoadChargeSMSPromotion : "
					+ ex.toString());
		} finally {
			try {
				rs.close();
				rs = null;
				stmt.close();
				stmt = null;
			} catch (Exception e) {
			}
		}
	}

	protected void LoadChargeArea(Connection pConnection) throws Exception {
		String mSQL = "SELECT area_charge_id,tax,"
				+ "to_char(date_from,'yyyymmddhh24miss') date_from,"
				+ "to_char(date_to,'yyyymmddhh24miss') date_to "
				+ "FROM area_charge_detail WHERE effect=1 "
				+ "ORDER BY area_charge_id,date_from DESC";
		Statement stmt = pConnection.createStatement();
		ResultSet rs = stmt.executeQuery(mSQL);
		if (vCacheChargeArea != null) {
			vCacheChargeArea.removeAllElements();
			vCacheChargeArea.clear();
		}
		try {
			while (rs.next()) {
				// Add param to vector CacheChargeArea
				CacheChargeArea = new ChargeArea(
						rs.getString("area_charge_id"),
						rs.getLong("date_from"), rs.getLong("date_to"), rs
								.getDouble("tax"));
				vCacheChargeArea.add(CacheChargeArea);
			}
		} catch (Exception ex) {
			System.err.println(Global.Format(new java.util.Date(),
					"dd/MM/yyyy HH:mm:ss")
					+ " : ERROR in module LoadChargeArea : " + ex.toString());
		} finally {
			try {
				rs.close();
				rs = null;
				stmt.close();
				stmt = null;
			} catch (Exception e) {
			}
		}
	}

	protected boolean chargeOGIntl(String pCallingISDN, String pCalledISDN,
			String pStrCallDateTime, long pCallDateTime, int pDuration)
			throws Exception {
		boolean blnFound = false;
		int mCallTime = Integer.parseInt(pStrCallDateTime.substring(8));
		try {
			for (int i = 0; i < vCacheChargeTariffOGIntl.size(); i++) {
				CacheChargeTariffOGIntl = (ChargeTariff) vCacheChargeTariffOGIntl
						.get(i);
				if ((pCallingISDN.startsWith(CacheChargeTariffOGIntl
						.getCallingISDN()))
						&& (pCalledISDN.startsWith(CacheChargeTariffOGIntl
								.getCalledISDN()))
						&& (pCallDateTime >= CacheChargeTariffOGIntl
								.getDateFrom())
						&& (pCallDateTime <= CacheChargeTariffOGIntl
								.getDateTo())) {
					blnFound = true;
					chargeResult.strPO_CODE = CacheChargeTariffOGIntl
							.getCalledISDN();
					chargeResult.intCenOfCalled = CacheChargeTariffOGIntl
							.getCenterID();
					iChargeClassAir = CacheChargeTariffOGIntl
							.getIndexOfChargeClassAir();
					iChargeClassIdd = CacheChargeTariffOGIntl
							.getIndexOfChargeClassIddSer();
					// Rating Air
					if (iChargeClassAir != -1) {
						// chargeCALLNatl()
						blnFound = chargeCall(mChargeAirtime, iChargeClassAir,
								CacheChargeTariffOGIntl.getAirtimeTariff(),
								"chargeOGNatl()", pCallingISDN, pCalledISDN,
								pStrCallDateTime, pCallDateTime, mCallTime,
								pDuration);
					}
					// Rating Idd
					if (iChargeClassIdd != -1) {
						// chargeCALLIntl()
						blnFound = chargeCall(mChargeIdd, iChargeClassIdd,
								CacheChargeTariffOGIntl.getIddSerTariff(),
								"chargeOGIntl()", pCallingISDN, pCalledISDN,
								pStrCallDateTime, pCallDateTime, mCallTime,
								pDuration);
					}
					// Giam cuoc khuyen mai
					for (int k = 0; k < vCacheChargeCALLPromotion.size(); k++) {
						CacheChargeCALLPromotion = (ChargePromotion) vCacheChargeCALLPromotion
								.get(k);
						if ((CacheChargeCALLPromotion.getDateFrom() <= pCallDateTime)
								&& (CacheChargeCALLPromotion.getDateTo() >= pCallDateTime)
								&& (CacheChargeCALLPromotion.getTimeFrom() <= mCallTime)
								&& (CacheChargeCALLPromotion.getTimeTo() >= mCallTime)
								&& (CacheChargeCALLPromotion
										.getPercentageDeduct() >= 0)) {
							if (CacheChargeTariffOGIntl.getHolidayDeduct() == 1)
								chargeResult.dblTaxAir = Global.round(
										chargeResult.dblTaxAir
												* CacheChargeCALLPromotion
														.getPercentageDeduct(),
										3);
							else if (CacheChargeTariffOGIntl.getHolidayDeduct() == 2)
								chargeResult.dblTaxIdd = Global.round(
										chargeResult.dblTaxIdd
												* CacheChargeCALLPromotion
														.getPercentageDeduct(),
										3);
							else if (CacheChargeTariffOGIntl.getHolidayDeduct() == 3) {
								chargeResult.dblTaxAir = Global.round(
										chargeResult.dblTaxAir
												* CacheChargeCALLPromotion
														.getPercentageDeduct(),
										3);
								chargeResult.dblTaxIdd = Global.round(
										chargeResult.dblTaxIdd
												* CacheChargeCALLPromotion
														.getPercentageDeduct(),
										3);
							}
							break;
						}
					}
					break;
				}
			}
		} catch (Exception e) {
			System.err.println(Global.Format(new java.util.Date(),
					"dd/MM/yyyy HH:mm:ss")
					+ " : ERROR in module chargeOGIntl : " + e.toString());
		}
		return blnFound;
	}

	protected boolean chargeICIntl(String pCallingISDN, String pCalledISDN,
			String pStrCallDateTime, long pCallDateTime, int pDuration)
			throws Exception {
		boolean blnFound = false;
		int mCallTime = Integer.parseInt(pStrCallDateTime.substring(8));
		try {
			for (int i = 0; i < vCacheChargeTariffICIntl.size(); i++) {
				CacheChargeTariffICIntl = (ChargeTariff) vCacheChargeTariffICIntl
						.get(i);
				if ((pCallingISDN.startsWith(CacheChargeTariffICIntl
						.getCallingISDN()))
						&& (pCalledISDN.startsWith(CacheChargeTariffICIntl
								.getCalledISDN()))
						&& (pCallDateTime >= CacheChargeTariffICIntl
								.getDateFrom())
						&& (pCallDateTime <= CacheChargeTariffICIntl
								.getDateTo())) {
					blnFound = true;
					chargeResult.strPO_CODE = CacheChargeTariffICIntl
							.getCalledISDN();
					chargeResult.intCenOfCalled = CacheChargeTariffICIntl
							.getCenterID();
					iChargeClassAir = CacheChargeTariffICIntl
							.getIndexOfChargeClassAir();
					iChargeClassIdd = CacheChargeTariffICIntl
							.getIndexOfChargeClassIddSer();
					// Rating Air
					if (iChargeClassAir != -1) {
						// chargeCALLNatl()
						blnFound = chargeCall(mChargeAirtime, iChargeClassAir,
								CacheChargeTariffICIntl.getAirtimeTariff(),
								"chargeICNatl()", pCallingISDN, pCalledISDN,
								pStrCallDateTime, pCallDateTime, mCallTime,
								pDuration);
					}
					// Rating Idd
					if (iChargeClassIdd != -1) {
						// chargeCALLIntl()
						blnFound = chargeCall(mChargeIdd, iChargeClassIdd,
								CacheChargeTariffICIntl.getIddSerTariff(),
								"chargeICIntl()", pCallingISDN, pCalledISDN,
								pStrCallDateTime, pCallDateTime, mCallTime,
								pDuration);
					}
					// Giam cuoc khuyen mai
					for (int k = 0; k < vCacheChargeCALLPromotion.size(); k++) {
						CacheChargeCALLPromotion = (ChargePromotion) vCacheChargeCALLPromotion
								.get(k);
						if ((CacheChargeCALLPromotion.getDateFrom() <= pCallDateTime)
								&& (CacheChargeCALLPromotion.getDateTo() >= pCallDateTime)
								&& (CacheChargeCALLPromotion.getTimeFrom() <= mCallTime)
								&& (CacheChargeCALLPromotion.getTimeTo() >= mCallTime)
								&& (CacheChargeCALLPromotion
										.getPercentageDeduct() >= 0)) {
							if (CacheChargeTariffICIntl.getHolidayDeduct() == 1)
								chargeResult.dblTaxAir = Global.round(
										chargeResult.dblTaxAir
												* CacheChargeCALLPromotion
														.getPercentageDeduct(),
										3);
							else if (CacheChargeTariffICIntl.getHolidayDeduct() == 2)
								chargeResult.dblTaxIdd = Global.round(
										chargeResult.dblTaxIdd
												* CacheChargeCALLPromotion
														.getPercentageDeduct(),
										3);
							else if (CacheChargeTariffICIntl.getHolidayDeduct() == 3) {
								chargeResult.dblTaxAir = Global.round(
										chargeResult.dblTaxAir
												* CacheChargeCALLPromotion
														.getPercentageDeduct(),
										3);
								chargeResult.dblTaxIdd = Global.round(
										chargeResult.dblTaxIdd
												* CacheChargeCALLPromotion
														.getPercentageDeduct(),
										3);
							}
							break;
						}
					}
					break;
				}
			}
		} catch (Exception e) {
			System.err.println(Global.Format(new java.util.Date(),
					"dd/MM/yyyy HH:mm:ss")
					+ " : ERROR in module chargeICIntl : " + e.toString());
		}
		return blnFound;
	}

	protected boolean chargeOGNatl(String pCallingISDN, String pCalledISDN,
			String pStrCallDateTime, long pCallDateTime, int pDuration)
			throws Exception {
		boolean blnFound = false;
		int mCallTime = Integer.parseInt(pStrCallDateTime.substring(8));
		try {
			for (int i = 0; i < vCacheChargeTariffOGNatl.size(); i++) {
				CacheChargeTariffOGNatl = (ChargeTariff) vCacheChargeTariffOGNatl
						.get(i);
				if ((pCallingISDN.startsWith(CacheChargeTariffOGNatl
						.getCallingISDN()))
						&& (pCalledISDN.startsWith(CacheChargeTariffOGNatl
								.getCalledISDN()))
						&& (pCallDateTime >= CacheChargeTariffOGNatl
								.getDateFrom())
						&& (pCallDateTime <= CacheChargeTariffOGNatl
								.getDateTo())) {
					blnFound = true;
					chargeResult.strPO_CODE = CacheChargeTariffOGNatl
							.getCalledISDN();
					chargeResult.intCenOfCalled = CacheChargeTariffOGNatl
							.getCenterID();
					iChargeClassAir = CacheChargeTariffOGNatl
							.getIndexOfChargeClassAir();
					iChargeClassIdd = CacheChargeTariffOGNatl
							.getIndexOfChargeClassIddSer();
					iChargeTariffSer = CacheChargeTariffOGNatl
							.getIndexOfChargeTariffSer();
					// Charging Airtime
					if (iChargeClassAir != -1) {
						// chargeCALLNatl()
						blnFound = chargeCall(mChargeAirtime, iChargeClassAir,
								CacheChargeTariffOGNatl.getAirtimeTariff(),
								"chargeOGNatl()", pCallingISDN, pCalledISDN,
								pStrCallDateTime, pCallDateTime, mCallTime,
								pDuration);
					}
					// Rating Idd
					if (iChargeClassIdd != -1) {
						// chargeCALLIntl()
						blnFound = chargeCall(mChargeIdd, iChargeClassIdd,
								CacheChargeTariffOGNatl.getIddSerTariff(),
								"chargeOGIntl()", pCallingISDN, pCalledISDN,
								pStrCallDateTime, pCallDateTime, mCallTime,
								pDuration);
					}
					// Charging Services
					if (iChargeTariffSer != -1) {
						// ReCharging tax airtime and new charging service
						for (int k = iChargeTariffSer; k < vCacheChargeTariffCALLServices
								.size(); k++) {
							CacheChargeTariffCALLServices = (ChargeTariff) vCacheChargeTariffCALLServices
									.get(k);
							if (CacheChargeTariffCALLServices
									.getZoneCodeParentID().compareTo(
											chargeResult.strPO_CODE) == 0) {
								if ((pCallingISDN
										.startsWith(CacheChargeTariffCALLServices
												.getCallingISDN()))
										&& (pCalledISDN
												.startsWith(CacheChargeTariffCALLServices
														.getCalledISDN()))
										&& (pCallDateTime >= CacheChargeTariffCALLServices
												.getDateFrom())
										&& (pCallDateTime <= CacheChargeTariffCALLServices
												.getDateTo())) {
									blnFound = true;
									chargeResult.strPO_CODE = CacheChargeTariffCALLServices
											.getCalledISDN();
									chargeResult.intCenOfCalled = CacheChargeTariffCALLServices
											.getCenterID();
									iChargeClassSer = CacheChargeTariffCALLServices
											.getIndexOfChargeClassIddSer();
									if (iChargeClassAir != CacheChargeTariffCALLServices
											.getIndexOfChargeClassAir()) {
										// chargeCALLNatl()
										blnFound = chargeCall(
												mChargeAirtime,
												CacheChargeTariffCALLServices
														.getIndexOfChargeClassAir(),
												CacheChargeTariffCALLServices
														.getAirtimeTariff(),
												"chargeOGNatl()", pCallingISDN,
												pCalledISDN, pStrCallDateTime,
												pCallDateTime, mCallTime,
												pDuration);
									}
									if (iChargeClassSer != -1) {
										// chargeCALLSer()
										blnFound = chargeCall(mChargeServices,
												iChargeClassSer,
												CacheChargeTariffCALLServices
														.getIddSerTariff(),
												"chargeCALLSer()",
												pCallingISDN, pCalledISDN,
												pStrCallDateTime,
												pCallDateTime, mCallTime,
												pDuration);
									}
									break;
								}
							} else
								break;
						}
					}
					// Giam cuoc khuyen mai
					for (int k = 0; k < vCacheChargeCALLPromotion.size(); k++) {
						CacheChargeCALLPromotion = (ChargePromotion) vCacheChargeCALLPromotion
								.get(k);
						if ((CacheChargeCALLPromotion.getDateFrom() <= pCallDateTime)
								&& (CacheChargeCALLPromotion.getDateTo() >= pCallDateTime)
								&& (CacheChargeCALLPromotion.getTimeFrom() <= mCallTime)
								&& (CacheChargeCALLPromotion.getTimeTo() >= mCallTime)
								&& (CacheChargeCALLPromotion
										.getPercentageDeduct() >= 0)) {
							if (CacheChargeTariffOGNatl.getHolidayDeduct() == 1)
								chargeResult.dblTaxAir = Global.round(
										chargeResult.dblTaxAir
												* CacheChargeCALLPromotion
														.getPercentageDeduct(),
										3);
							else if (CacheChargeTariffOGNatl.getHolidayDeduct() == 2)
								chargeResult.dblTaxSer = Global.round(
										chargeResult.dblTaxSer
												* CacheChargeCALLPromotion
														.getPercentageDeduct(),
										3);
							else if (CacheChargeTariffOGNatl.getHolidayDeduct() == 3) {
								chargeResult.dblTaxAir = Global.round(
										chargeResult.dblTaxAir
												* CacheChargeCALLPromotion
														.getPercentageDeduct(),
										3);
								chargeResult.dblTaxSer = Global.round(
										chargeResult.dblTaxSer
												* CacheChargeCALLPromotion
														.getPercentageDeduct(),
										3);
							}
							break;
						}
					}
					break;
				}
			}
		} catch (Exception e) {
			System.err
					.println(Global.Format(new java.util.Date(),
							"dd/MM/yyyy HH:mm:ss")
							+ " : ERROR in module chargeOGNatl : "
							+ "-"
							+ e.toString());
		}
		return blnFound;
	}

	protected boolean chargeICNatl(String pCallingISDN, String pCalledISDN,
			String pStrCallDateTime, long pCallDateTime, int pDuration)
			throws Exception {
		boolean blnFound = false;
		int mCallTime = Integer.parseInt(pStrCallDateTime.substring(8));
		try {
			for (int i = 0; i < vCacheChargeTariffICNatl.size(); i++) {
				CacheChargeTariffICNatl = (ChargeTariff) vCacheChargeTariffICNatl
						.get(i);
				if ((pCallingISDN.startsWith(CacheChargeTariffICNatl
						.getCallingISDN()))
						&& (pCalledISDN.startsWith(CacheChargeTariffICNatl
								.getCalledISDN()))
						&& (pCallDateTime >= CacheChargeTariffICNatl
								.getDateFrom())
						&& (pCallDateTime <= CacheChargeTariffICNatl
								.getDateTo())) {
					blnFound = true;
					chargeResult.strPO_CODE = CacheChargeTariffICNatl
							.getCalledISDN();
					chargeResult.intCenOfCalled = CacheChargeTariffICNatl
							.getCenterID();
					iChargeClassAir = CacheChargeTariffICNatl
							.getIndexOfChargeClassAir();
					iChargeClassIdd = CacheChargeTariffICNatl
							.getIndexOfChargeClassIddSer();
					iChargeTariffSer = CacheChargeTariffICNatl
							.getIndexOfChargeTariffSer();
					// Charging Airtime
					if (iChargeClassAir != -1) {
						// chargeCALLNatl()
						blnFound = chargeCall(mChargeAirtime, iChargeClassAir,
								CacheChargeTariffICNatl.getAirtimeTariff(),
								"chargeICNatl()", pCallingISDN, pCalledISDN,
								pStrCallDateTime, pCallDateTime, mCallTime,
								pDuration);
					}
					// Rating Idd
					if (iChargeClassIdd != -1) {
						// chargeCALLIntl()
						blnFound = chargeCall(mChargeIdd, iChargeClassIdd,
								CacheChargeTariffICNatl.getIddSerTariff(),
								"chargeICIntl()", pCallingISDN, pCalledISDN,
								pStrCallDateTime, pCallDateTime, mCallTime,
								pDuration);
					}
					// Giam cuoc khuyen mai
					for (int k = 0; k < vCacheChargeCALLPromotion.size(); k++) {
						CacheChargeCALLPromotion = (ChargePromotion) vCacheChargeCALLPromotion
								.get(k);
						if ((CacheChargeCALLPromotion.getDateFrom() <= pCallDateTime)
								&& (CacheChargeCALLPromotion.getDateTo() >= pCallDateTime)
								&& (CacheChargeCALLPromotion.getTimeFrom() <= mCallTime)
								&& (CacheChargeCALLPromotion.getTimeTo() >= mCallTime)
								&& (CacheChargeCALLPromotion
										.getPercentageDeduct() >= 0)) {
							if (CacheChargeTariffICNatl.getHolidayDeduct() == 1)
								chargeResult.dblTaxAir = Global.round(
										chargeResult.dblTaxAir
												* CacheChargeCALLPromotion
														.getPercentageDeduct(),
										3);
							else if (CacheChargeTariffICNatl.getHolidayDeduct() == 2)
								chargeResult.dblTaxSer = Global.round(
										chargeResult.dblTaxSer
												* CacheChargeCALLPromotion
														.getPercentageDeduct(),
										3);
							else if (CacheChargeTariffICNatl.getHolidayDeduct() == 3) {
								chargeResult.dblTaxAir = Global.round(
										chargeResult.dblTaxAir
												* CacheChargeCALLPromotion
														.getPercentageDeduct(),
										3);
								chargeResult.dblTaxSer = Global.round(
										chargeResult.dblTaxSer
												* CacheChargeCALLPromotion
														.getPercentageDeduct(),
										3);
							}
							break;
						}
					}
					break;
				}
			}
		} catch (Exception e) {
			System.err
					.println(Global.Format(new java.util.Date(),
							"dd/MM/yyyy HH:mm:ss")
							+ " : ERROR in module chargeICNatl : "
							+ "-"
							+ e.toString());
		}
		return blnFound;
	}

	protected boolean chargeSMONatl(String pCallingISDN, String pCalledISDN,
			String pStrCallDateTime, long pCallDateTime, int pDuration)
			throws Exception {
		boolean blnFound = false;
		int mCallTime = Integer.parseInt(pStrCallDateTime.substring(8));
		try {
			for (int i = 0; i < vCacheChargeTariffSMONatl.size(); i++) {
				CacheChargeTariffSMONatl = (ChargeTariff) vCacheChargeTariffSMONatl
						.get(i);
				if ((pCallingISDN.startsWith(CacheChargeTariffSMONatl
						.getCallingISDN()))
						&& (pCalledISDN.startsWith(CacheChargeTariffSMONatl
								.getCalledISDN()))
						&& (pCallDateTime >= CacheChargeTariffSMONatl
								.getDateFrom())
						&& (pCallDateTime <= CacheChargeTariffSMONatl
								.getDateTo())) {
					blnFound = true;
					chargeResult.strPO_CODE = CacheChargeTariffSMONatl
							.getCalledISDN();
					chargeResult.intCenOfCalled = CacheChargeTariffSMONatl
							.getCenterID();
					iChargeClassAir = CacheChargeTariffSMONatl
							.getIndexOfChargeClassAir();
					// Rating Air
					if (iChargeClassAir != -1) {
						// chargeSMSNatl()
						blnFound = chargeSMS(mChargeAirtime, iChargeClassAir,
								CacheChargeTariffSMONatl.getAirtimeTariff(),
								"chargeSMONatl()", pCallingISDN, pCalledISDN,
								pStrCallDateTime, pCallDateTime, mCallTime,
								pDuration);
					}
					/*
					 * for (int k = 0; k < vCacheChargeSMSPromotion.size(); k++) {
					 * CacheChargeSMSPromotion = (ChargePromotion)
					 * vCacheChargeSMSPromotion.get(k); if
					 * ((CacheChargeSMSPromotion.getDateFrom() <= pCallDateTime) &&
					 * (CacheChargeSMSPromotion.getDateTo() >= pCallDateTime) &&
					 * (CacheChargeSMSPromotion.getTimeFrom() <= mCallTime) &&
					 * (CacheChargeSMSPromotion.getTimeTo() >= mCallTime) &&
					 * (CacheChargeSMSPromotion.getPercentageDeduct() >= 0)) {
					 * if (CacheChargeTariffSMSNatl.getHolidayDeduct() == 1)
					 * chargeResult.dblTaxAir =
					 * Global.round(chargeResult.dblTaxAir *
					 * CacheChargeSMSPromotion. getPercentageDeduct(), 3); else
					 * if (CacheChargeTariffSMSNatl.getHolidayDeduct() == 2)
					 * chargeResult.dblTaxSer =
					 * Global.round(chargeResult.dblTaxSer *
					 * CacheChargeSMSPromotion. getPercentageDeduct(), 3); else
					 * if (CacheChargeTariffSMSNatl.getHolidayDeduct() == 3) {
					 * chargeResult.dblTaxAir =
					 * Global.round(chargeResult.dblTaxAir *
					 * CacheChargeSMSPromotion. getPercentageDeduct(), 3);
					 * chargeResult.dblTaxSer =
					 * Global.round(chargeResult.dblTaxSer *
					 * CacheChargeSMSPromotion. getPercentageDeduct(), 3); } } }
					 */
					break;
				}
			}
		} catch (Exception e) {
			System.err.println(Global.Format(new java.util.Date(),
					"dd/MM/yyyy HH:mm:ss")
					+ " : ERROR in module chargeSMONatl : " + e.toString());
		}
		return blnFound;
	}

	protected boolean chargeSMTNatl(String pCallingISDN, String pCalledISDN,
			String pStrCallDateTime, long pCallDateTime, int pDuration)
			throws Exception {
		boolean blnFound = false;
		int mCallTime = Integer.parseInt(pStrCallDateTime.substring(8));
		try {
			for (int i = 0; i < vCacheChargeTariffSMTNatl.size(); i++) {
				CacheChargeTariffSMTNatl = (ChargeTariff) vCacheChargeTariffSMTNatl
						.get(i);
				if ((pCallingISDN.startsWith(CacheChargeTariffSMTNatl
						.getCallingISDN()))
						&& (pCalledISDN.startsWith(CacheChargeTariffSMTNatl
								.getCalledISDN()))
						&& (pCallDateTime >= CacheChargeTariffSMTNatl
								.getDateFrom())
						&& (pCallDateTime <= CacheChargeTariffSMTNatl
								.getDateTo())) {
					blnFound = true;
					chargeResult.strPO_CODE = CacheChargeTariffSMTNatl
							.getCalledISDN();
					chargeResult.intCenOfCalled = CacheChargeTariffSMTNatl
							.getCenterID();
					iChargeClassAir = CacheChargeTariffSMTNatl
							.getIndexOfChargeClassAir();
					// Rating Air
					if (iChargeClassAir != -1) {
						// chargeSMSNatl()
						blnFound = chargeSMS(mChargeAirtime, iChargeClassAir,
								CacheChargeTariffSMTNatl.getAirtimeTariff(),
								"chargeSMTNatl()", pCallingISDN, pCalledISDN,
								pStrCallDateTime, pCallDateTime, mCallTime,
								pDuration);
					}
					/*
					 * for (int k = 0; k < vCacheChargeSMSPromotion.size(); k++) {
					 * CacheChargeSMSPromotion = (ChargePromotion)
					 * vCacheChargeSMSPromotion.get(k); if
					 * ((CacheChargeSMSPromotion.getDateFrom() <= pCallDateTime) &&
					 * (CacheChargeSMSPromotion.getDateTo() >= pCallDateTime) &&
					 * (CacheChargeSMSPromotion.getTimeFrom() <= mCallTime) &&
					 * (CacheChargeSMSPromotion.getTimeTo() >= mCallTime) &&
					 * (CacheChargeSMSPromotion.getPercentageDeduct() >= 0)) {
					 * if (CacheChargeTariffSMSNatl.getHolidayDeduct() == 1)
					 * chargeResult.dblTaxAir =
					 * Global.round(chargeResult.dblTaxAir *
					 * CacheChargeSMSPromotion. getPercentageDeduct(), 3); else
					 * if (CacheChargeTariffSMSNatl.getHolidayDeduct() == 2)
					 * chargeResult.dblTaxSer =
					 * Global.round(chargeResult.dblTaxSer *
					 * CacheChargeSMSPromotion. getPercentageDeduct(), 3); else
					 * if (CacheChargeTariffSMSNatl.getHolidayDeduct() == 3) {
					 * chargeResult.dblTaxAir =
					 * Global.round(chargeResult.dblTaxAir *
					 * CacheChargeSMSPromotion. getPercentageDeduct(), 3);
					 * chargeResult.dblTaxSer =
					 * Global.round(chargeResult.dblTaxSer *
					 * CacheChargeSMSPromotion. getPercentageDeduct(), 3); } } }
					 */
					break;
				}
			}
		} catch (Exception e) {
			System.err.println(Global.Format(new java.util.Date(),
					"dd/MM/yyyy HH:mm:ss")
					+ " : ERROR in module chargeSMTNatl : " + e.toString());
		}
		return blnFound;
	}

	protected boolean chargeSMOIntl(String pCallingISDN, String pCalledISDN,
			String pStrCallDateTime, long pCallDateTime, int pDuration)
			throws Exception {
		boolean blnFound = false;
		int mCallTime = Integer.parseInt(pStrCallDateTime.substring(8));
		try {
			for (int i = 0; i < vCacheChargeTariffSMOIntl.size(); i++) {
				CacheChargeTariffSMOIntl = (ChargeTariff) vCacheChargeTariffSMOIntl
						.get(i);
				if ((pCallingISDN.startsWith(CacheChargeTariffSMOIntl
						.getCallingISDN()))
						&& (pCalledISDN.startsWith(CacheChargeTariffSMOIntl
								.getCalledISDN()))
						&& (pCallDateTime >= CacheChargeTariffSMOIntl
								.getDateFrom())
						&& (pCallDateTime <= CacheChargeTariffSMOIntl
								.getDateTo())) {
					blnFound = true;
					chargeResult.strPO_CODE = CacheChargeTariffSMOIntl
							.getCalledISDN();
					chargeResult.intCenOfCalled = CacheChargeTariffSMOIntl
							.getCenterID();
					iChargeClassIdd = CacheChargeTariffSMOIntl
							.getIndexOfChargeClassIddSer();
					// Rating Idd
					if (iChargeClassIdd != -1) {
						// chargeSMSIntl()
						blnFound = chargeSMS(mChargeIdd, iChargeClassIdd,
								CacheChargeTariffSMOIntl.getIddSerTariff(),
								"chargeSMOIntl()", pCallingISDN, pCalledISDN,
								pStrCallDateTime, pCallDateTime, mCallTime,
								pDuration);
					}
					/*
					 * for (int k = 0; k < vCacheChargeSMSPromotion.size(); k++) {
					 * CacheChargeSMSPromotion = (ChargePromotion)
					 * vCacheChargeSMSPromotion.get(k); if
					 * ((CacheChargeSMSPromotion.getDateFrom() <= pCallDateTime) &&
					 * (CacheChargeSMSPromotion.getDateTo() >= pCallDateTime) &&
					 * (CacheChargeSMSPromotion.getTimeFrom() <= mCallTime) &&
					 * (CacheChargeSMSPromotion.getTimeTo() >= mCallTime) &&
					 * (CacheChargeSMSPromotion.getPercentageDeduct() >= 0)) {
					 * if (CacheChargeTariffSMSIntl.getHolidayDeduct() == 1)
					 * chargeResult.dblTaxAir =
					 * Global.round(chargeResult.dblTaxAir *
					 * CacheChargeSMSPromotion. getPercentageDeduct(), 3); else
					 * if (CacheChargeTariffSMSIntl.getHolidayDeduct() == 2)
					 * chargeResult.dblTaxIdd =
					 * Global.round(chargeResult.dblTaxIdd *
					 * CacheChargeSMSPromotion. getPercentageDeduct(), 3); else
					 * if (CacheChargeTariffSMSIntl.getHolidayDeduct() == 3) {
					 * chargeResult.dblTaxAir =
					 * Global.round(chargeResult.dblTaxAir *
					 * CacheChargeSMSPromotion. getPercentageDeduct(), 3);
					 * chargeResult.dblTaxIdd =
					 * Global.round(chargeResult.dblTaxIdd *
					 * CacheChargeSMSPromotion. getPercentageDeduct(), 3); } } }
					 */
					break;
				}
			}
		} catch (Exception e) {
			System.err.println(Global.Format(new java.util.Date(),
					"dd/MM/yyyy HH:mm:ss")
					+ " : ERROR in module chargeSMSIntl : " + e.toString());
		}
		return blnFound;
	}

	protected boolean chargeSMTIntl(String pCallingISDN, String pCalledISDN,
			String pStrCallDateTime, long pCallDateTime, int pDuration)
			throws Exception {
		boolean blnFound = false;
		int mCallTime = Integer.parseInt(pStrCallDateTime.substring(8));
		try {
			for (int i = 0; i < vCacheChargeTariffSMTIntl.size(); i++) {
				CacheChargeTariffSMTIntl = (ChargeTariff) vCacheChargeTariffSMTIntl
						.get(i);
				if ((pCallingISDN.startsWith(CacheChargeTariffSMTIntl
						.getCallingISDN()))
						&& (pCalledISDN.startsWith(CacheChargeTariffSMTIntl
								.getCalledISDN()))
						&& (pCallDateTime >= CacheChargeTariffSMTIntl
								.getDateFrom())
						&& (pCallDateTime <= CacheChargeTariffSMTIntl
								.getDateTo())) {
					blnFound = true;
					chargeResult.strPO_CODE = CacheChargeTariffSMTIntl
							.getCalledISDN();
					chargeResult.intCenOfCalled = CacheChargeTariffSMTIntl
							.getCenterID();
					iChargeClassIdd = CacheChargeTariffSMTIntl
							.getIndexOfChargeClassIddSer();
					// Rating Idd
					if (iChargeClassIdd != -1) {
						// chargeSMSIntl()
						blnFound = chargeSMS(mChargeIdd, iChargeClassIdd,
								CacheChargeTariffSMTIntl.getIddSerTariff(),
								"chargeSMTIntl()", pCallingISDN, pCalledISDN,
								pStrCallDateTime, pCallDateTime, mCallTime,
								pDuration);
					}
					/*
					 * for (int k = 0; k < vCacheChargeSMSPromotion.size(); k++) {
					 * CacheChargeSMSPromotion = (ChargePromotion)
					 * vCacheChargeSMSPromotion.get(k); if
					 * ((CacheChargeSMSPromotion.getDateFrom() <= pCallDateTime) &&
					 * (CacheChargeSMSPromotion.getDateTo() >= pCallDateTime) &&
					 * (CacheChargeSMSPromotion.getTimeFrom() <= mCallTime) &&
					 * (CacheChargeSMSPromotion.getTimeTo() >= mCallTime) &&
					 * (CacheChargeSMSPromotion.getPercentageDeduct() >= 0)) {
					 * if (CacheChargeTariffSMSIntl.getHolidayDeduct() == 1)
					 * chargeResult.dblTaxAir =
					 * Global.round(chargeResult.dblTaxAir *
					 * CacheChargeSMSPromotion. getPercentageDeduct(), 3); else
					 * if (CacheChargeTariffSMSIntl.getHolidayDeduct() == 2)
					 * chargeResult.dblTaxIdd =
					 * Global.round(chargeResult.dblTaxIdd *
					 * CacheChargeSMSPromotion. getPercentageDeduct(), 3); else
					 * if (CacheChargeTariffSMSIntl.getHolidayDeduct() == 3) {
					 * chargeResult.dblTaxAir =
					 * Global.round(chargeResult.dblTaxAir *
					 * CacheChargeSMSPromotion. getPercentageDeduct(), 3);
					 * chargeResult.dblTaxIdd =
					 * Global.round(chargeResult.dblTaxIdd *
					 * CacheChargeSMSPromotion. getPercentageDeduct(), 3); } } }
					 */
					break;
				}
			}
		} catch (Exception e) {
			System.err.println(Global.Format(new java.util.Date(),
					"dd/MM/yyyy HH:mm:ss")
					+ " : ERROR in module chargeSMTIntl : " + e.toString());
		}
		return blnFound;
	}

	protected void LoadChargeTariffOGIntl(Connection pConnection)
			throws Exception {
		String mSQL = "select center_id,nvl(originator_address,'%') "
				+ "originator_address,destination_address,"
				+ "to_char(date_from,'yyyymmddhh24miss') date_from,"
				+ "to_char(date_to,'yyyymmddhh24miss') date_to,"
				+ "nvl(airtime_class,0) airtime_class,"
				+ "nvl(idd_class,0) idd_class,nvl(service_class,0) "
				+ "service_class, holiday_deduct,tariff_charge_id "
				+ "from tariff_charge where call_type='OG' and "
				+ "destination_address in "
				+ "(select zone_code from zone_charge where zone_type='IDD') "
				+ "order by length(originator_address) desc,"
				+ "originator_address,length(destination_address) desc, "
				+ "to_number(destination_address) desc,date_from desc";
		Statement stmt = pConnection.createStatement();
		ResultSet rs = stmt.executeQuery(mSQL);
		boolean Found = false;

		if (vCacheChargeTariffOGIntl != null) {
			vCacheChargeTariffOGIntl.removeAllElements();
			vCacheChargeTariffOGIntl.clear();
		}
		try {
			while (rs.next()) {
				Found = false;
				iChargeClassAir = -1;
				iChargeClassIdd = -1;
				for (int i = 0; i < vCacheChargeClass.size(); i++) {
					CacheChargeClass = (ChargeClass) vCacheChargeClass.get(i);
					if (CacheChargeClass.getChargeClassID() == rs
							.getInt("airtime_class")) {
						Found = true;
						iChargeClassAir = i;
						break;
					}
				}
				for (int i = 0; i < vCacheChargeClass.size(); i++) {
					CacheChargeClass = (ChargeClass) vCacheChargeClass.get(i);
					if (CacheChargeClass.getChargeClassID() == rs
							.getInt("idd_class")) {
						Found = true;
						iChargeClassIdd = i;
						break;
					}
				}
				if (Found == true) {
					// Add param to vector vListISDN
					CacheChargeTariffOGIntl = new ChargeTariff(rs
							.getInt("center_id"), rs
							.getString("originator_address"), rs
							.getString("destination_address"), rs
							.getLong("date_from"), rs.getLong("date_to"), rs
							.getInt("airtime_class"), rs.getInt("idd_class"),
							rs.getInt("holiday_deduct"), iChargeClassAir,
							iChargeClassIdd);
					vCacheChargeTariffOGIntl.add(CacheChargeTariffOGIntl);
				} else {
					writeLogFile(" - "
							+ "Undefined tariff plan for OG Intl tariff_charge : "
							+ rs.getInt("tariff_charge_id"));
				}
			}
		} catch (Exception ex) {
			System.err.println(Global.Format(new java.util.Date(),
					"dd/MM/yyyy HH:mm:ss")
					+ " : ERROR in module LoadChargeTariffOGIntl : "
					+ ex.toString());
		} finally {
			try {
				rs.close();
				rs = null;
				stmt.close();
				stmt = null;
			} catch (Exception e) {
			}
		}
	}

	protected void LoadChargeTariffICIntl(Connection pConnection)
			throws Exception {
		String mSQL = "select center_id,nvl(originator_address,'%') "
				+ "originator_address,destination_address,"
				+ "to_char(date_from,'yyyymmddhh24miss') date_from,"
				+ "to_char(date_to,'yyyymmddhh24miss') date_to,"
				+ "nvl(airtime_class,0) airtime_class,"
				+ "nvl(idd_class,0) idd_class,nvl(service_class,0) "
				+ "service_class, holiday_deduct,tariff_charge_id "
				+ "from tariff_charge where call_type='IC' and "
				+ "destination_address in "
				+ "(select zone_code from zone_charge where zone_type='IDD') "
				+ "order by length(originator_address) desc,"
				+ "originator_address,length(destination_address) desc, "
				+ "to_number(destination_address) desc,date_from desc";
		Statement stmt = pConnection.createStatement();
		ResultSet rs = stmt.executeQuery(mSQL);
		boolean Found = false;

		if (vCacheChargeTariffICIntl != null) {
			vCacheChargeTariffICIntl.removeAllElements();
			vCacheChargeTariffICIntl.clear();
		}
		try {
			while (rs.next()) {
				Found = false;
				iChargeClassAir = -1;
				iChargeClassIdd = -1;
				for (int i = 0; i < vCacheChargeClass.size(); i++) {
					CacheChargeClass = (ChargeClass) vCacheChargeClass.get(i);
					if (CacheChargeClass.getChargeClassID() == rs
							.getInt("airtime_class")) {
						Found = true;
						iChargeClassAir = i;
						break;
					}
				}
				for (int i = 0; i < vCacheChargeClass.size(); i++) {
					CacheChargeClass = (ChargeClass) vCacheChargeClass.get(i);
					if (CacheChargeClass.getChargeClassID() == rs
							.getInt("idd_class")) {
						Found = true;
						iChargeClassIdd = i;
						break;
					}
				}
				if (Found == true) {
					// Add param to vector vListISDN
					CacheChargeTariffICIntl = new ChargeTariff(rs
							.getInt("center_id"), rs
							.getString("originator_address"), rs
							.getString("destination_address"), rs
							.getLong("date_from"), rs.getLong("date_to"), rs
							.getInt("airtime_class"), rs.getInt("idd_class"),
							rs.getInt("holiday_deduct"), iChargeClassAir,
							iChargeClassIdd);
					vCacheChargeTariffICIntl.add(CacheChargeTariffICIntl);
				} else {
					writeLogFile(" - "
							+ "Undefined tariff plan for IC Intl tariff_charge : "
							+ rs.getInt("tariff_charge_id"));
				}
			}
		} catch (Exception ex) {
			System.err.println(Global.Format(new java.util.Date(),
					"dd/MM/yyyy HH:mm:ss")
					+ " : ERROR in module LoadChargeTariffICIntl : "
					+ ex.toString());
		} finally {
			try {
				rs.close();
				rs = null;
				stmt.close();
				stmt = null;
			} catch (Exception e) {
			}
		}
	}

	protected void LoadChargeTariffOGNatl(Connection pConnection)
			throws Exception {
		String mSQL = "select a.center_id,nvl(a.originator_address,'%') "
				+ "originator_address,a.destination_address,"
				+ "to_char(a.date_from,'yyyymmddhh24miss') date_from,"
				+ "to_char(a.date_to,'yyyymmddhh24miss') date_to,"
				+ "nvl(a.airtime_class,0) airtime_class,"
				+ "nvl(a.idd_class,0) idd_class,nvl(a.service_class,0) "
				+ "service_class, a.holiday_deduct,a.tariff_charge_id, "
				+ "nvl(b.province_header,0) province_header "
				+ "from tariff_charge a, zone_charge b "
				+ "where a.call_type='OG' and a.destination_address in "
				+ "(select zone_code from zone_charge where zone_type='AIR') "
				+ "and a.destination_address=b.zone_code "
				+ "order by length(a.originator_address) desc,"
				+ "a.originator_address,length(a.destination_address) desc,"
				+ "to_number(a.destination_address) desc,a.date_from desc";
		Statement stmt = pConnection.createStatement();
		ResultSet rs = stmt.executeQuery(mSQL);
		boolean Found = false;

		if (vCacheChargeTariffOGNatl != null) {
			vCacheChargeTariffOGNatl.removeAllElements();
			vCacheChargeTariffOGNatl.clear();
		}
		try {
			while (rs.next()) {
				Found = false;
				iChargeClassAir = -1;
				iChargeClassIdd = -1;
				iChargeTariffSer = -1;
				for (int i = 0; i < vCacheChargeClass.size(); i++) {
					CacheChargeClass = (ChargeClass) vCacheChargeClass.get(i);
					if (CacheChargeClass.getChargeClassID() == rs
							.getInt("airtime_class")) {
						Found = true;
						iChargeClassAir = i;
						break;
					}
				}
				for (int i = 0; i < vCacheChargeClass.size(); i++) {
					CacheChargeClass = (ChargeClass) vCacheChargeClass.get(i);
					if (CacheChargeClass.getChargeClassID() == rs
							.getInt("idd_class")) {
						Found = true;
						iChargeClassIdd = i;
						break;
					}
				}
				if (rs.getInt("province_header") == 1) {
					for (int i = 0; i < vCacheChargeTariffCALLServices.size(); i++) {
						CacheChargeTariffCALLServices = (ChargeTariff) vCacheChargeTariffCALLServices
								.get(i);
						if (CacheChargeTariffCALLServices.getZoneCodeParentID()
								.compareTo(rs.getString("destination_address")) == 0) {
							Found = true;
							iChargeTariffSer = i;
							break;
						}
					}
				}
				if (Found == true) {
					// Add param to vector vListISDN
					CacheChargeTariffOGNatl = new ChargeTariff(rs
							.getInt("center_id"), rs
							.getString("originator_address"), rs
							.getString("destination_address"), rs
							.getLong("date_from"), rs.getLong("date_to"), rs
							.getInt("airtime_class"), rs.getInt("idd_class"),
							rs.getInt("holiday_deduct"), iChargeClassAir,
							iChargeClassIdd, rs.getInt("province_header"),
							iChargeTariffSer);
					vCacheChargeTariffOGNatl.add(CacheChargeTariffOGNatl);
				} else {
					writeLogFile(" - "
							+ "Undefined tariff plan for OG Natl tariff_charge : "
							+ rs.getInt("tariff_charge_id"));
				}
			}
		} catch (Exception ex) {
			System.err.println(Global.Format(new java.util.Date(),
					"dd/MM/yyyy HH:mm:ss")
					+ " : ERROR in module LoadChargeTariffOGNatl : "
					+ ex.toString());
		} finally {
			try {
				rs.close();
				rs = null;
				stmt.close();
				stmt = null;
			} catch (Exception e) {
			}
		}
	}

	protected void LoadChargeTariffICNatl(Connection pConnection)
			throws Exception {
		String mSQL = "select a.center_id,nvl(a.originator_address,'%') "
				+ "originator_address,a.destination_address,"
				+ "to_char(a.date_from,'yyyymmddhh24miss') date_from,"
				+ "to_char(a.date_to,'yyyymmddhh24miss') date_to,"
				+ "nvl(a.airtime_class,0) airtime_class,"
				+ "nvl(a.idd_class,0) idd_class,nvl(a.service_class,0) "
				+ "service_class, a.holiday_deduct,a.tariff_charge_id, "
				+ "nvl(b.province_header,0) province_header "
				+ "from tariff_charge a, zone_charge b "
				+ "where a.call_type='IC' and a.destination_address in "
				+ "(select zone_code from zone_charge where zone_type='AIR') "
				+ "and a.destination_address=b.zone_code "
				+ "order by length(a.originator_address) desc,"
				+ "a.originator_address,length(a.destination_address) desc,"
				+ "to_number(a.destination_address) desc,a.date_from desc";
		Statement stmt = pConnection.createStatement();
		ResultSet rs = stmt.executeQuery(mSQL);
		boolean Found = false;

		if (vCacheChargeTariffICNatl != null) {
			vCacheChargeTariffICNatl.removeAllElements();
			vCacheChargeTariffICNatl.clear();
		}
		try {
			while (rs.next()) {
				Found = false;
				iChargeClassAir = -1;
				iChargeClassIdd = -1;
				iChargeTariffSer = -1;
				for (int i = 0; i < vCacheChargeClass.size(); i++) {
					CacheChargeClass = (ChargeClass) vCacheChargeClass.get(i);
					if (CacheChargeClass.getChargeClassID() == rs
							.getInt("airtime_class")) {
						Found = true;
						iChargeClassAir = i;
						break;
					}
				}
				for (int i = 0; i < vCacheChargeClass.size(); i++) {
					CacheChargeClass = (ChargeClass) vCacheChargeClass.get(i);
					if (CacheChargeClass.getChargeClassID() == rs
							.getInt("idd_class")) {
						Found = true;
						iChargeClassIdd = i;
						break;
					}
				}
				if (rs.getInt("province_header") == 1) {
					for (int i = 0; i < vCacheChargeTariffCALLServices.size(); i++) {
						CacheChargeTariffCALLServices = (ChargeTariff) vCacheChargeTariffCALLServices
								.get(i);
						if (CacheChargeTariffCALLServices.getZoneCodeParentID()
								.compareTo(rs.getString("destination_address")) == 0) {
							Found = true;
							iChargeTariffSer = i;
							break;
						}
					}
				}
				if (Found == true) {
					// Add param to vector vListISDN
					CacheChargeTariffICNatl = new ChargeTariff(rs
							.getInt("center_id"), rs
							.getString("originator_address"), rs
							.getString("destination_address"), rs
							.getLong("date_from"), rs.getLong("date_to"), rs
							.getInt("airtime_class"), rs.getInt("idd_class"),
							rs.getInt("holiday_deduct"), iChargeClassAir,
							iChargeClassIdd, rs.getInt("province_header"),
							iChargeTariffSer);
					vCacheChargeTariffICNatl.add(CacheChargeTariffICNatl);
				} else {
					writeLogFile(" - "
							+ "Undefined tariff plan for IC Natl tariff_charge : "
							+ rs.getInt("tariff_charge_id"));
				}
			}
		} catch (Exception ex) {
			System.err.println(Global.Format(new java.util.Date(),
					"dd/MM/yyyy HH:mm:ss")
					+ " : ERROR in module LoadChargeTariffICNatl : "
					+ ex.toString());
		} finally {
			try {
				rs.close();
				rs = null;
				stmt.close();
				stmt = null;
			} catch (Exception e) {
			}
		}
	}

	protected void LoadChargeTariffSMOIntl(Connection pConnection)
			throws Exception {
		String mSQL = "select center_id,nvl(originator_address,'%') "
				+ "originator_address,destination_address,"
				+ "to_char(date_from,'yyyymmddhh24miss') date_from,"
				+ "to_char(date_to,'yyyymmddhh24miss') date_to,"
				+ "nvl(airtime_class,0) airtime_class,"
				+ "nvl(idd_class,0) idd_class,nvl(service_class,0) "
				+ "service_class, holiday_deduct,tariff_charge_id "
				+ "from tariff_charge where call_type='SMO' and "
				+ "destination_address in "
				+ "(select zone_code from zone_charge where zone_type='IDD') "
				+ "order by length(originator_address) desc,"
				+ "originator_address,length(destination_address) desc,"
				+ "to_number(destination_address) desc,date_from desc";
		Statement stmt = pConnection.createStatement();
		ResultSet rs = stmt.executeQuery(mSQL);
		boolean Found;
		if (vCacheChargeTariffSMOIntl != null) {
			vCacheChargeTariffSMOIntl.removeAllElements();
			vCacheChargeTariffSMOIntl.clear();
		}
		try {
			while (rs.next()) {
				Found = false;
				iChargeClassAir = -1;
				iChargeClassIdd = -1;
				for (int i = 0; i < vCacheChargeClass.size(); i++) {
					CacheChargeClass = (ChargeClass) vCacheChargeClass.get(i);
					if (CacheChargeClass.getChargeClassID() == rs
							.getInt("airtime_class")) {
						Found = true;
						iChargeClassAir = i;
						break;
					}
				}
				for (int i = 0; i < vCacheChargeClass.size(); i++) {
					CacheChargeClass = (ChargeClass) vCacheChargeClass.get(i);
					if (CacheChargeClass.getChargeClassID() == rs
							.getInt("idd_class")) {
						Found = true;
						iChargeClassIdd = i;
						break;
					}
				}
				if (Found == true) {
					// Add param to vector vListISDN
					CacheChargeTariffSMOIntl = new ChargeTariff(rs
							.getInt("center_id"), rs
							.getString("originator_address"), rs
							.getString("destination_address"), rs
							.getLong("date_from"), rs.getLong("date_to"), rs
							.getInt("airtime_class"), rs.getInt("idd_class"),
							rs.getInt("holiday_deduct"), iChargeClassAir,
							iChargeClassIdd);
					vCacheChargeTariffSMOIntl.add(CacheChargeTariffSMOIntl);
				} else {
					writeLogFile(" - "
							+ "Undefined tariff plan for SMO Intl tariff_charge : "
							+ rs.getInt("tariff_charge_id"));
				}
			}
		} catch (Exception ex) {
			System.err.println(Global.Format(new java.util.Date(),
					"dd/MM/yyyy HH:mm:ss")
					+ " : ERROR in module LoadChargeTariffSMOIntl : "
					+ ex.toString());
		} finally {
			try {
				rs.close();
				rs = null;
				stmt.close();
				stmt = null;
			} catch (Exception e) {
			}
		}
	}

	protected void LoadChargeTariffSMTIntl(Connection pConnection)
			throws Exception {
		String mSQL = "select center_id,nvl(originator_address,'%') "
				+ "originator_address,destination_address,"
				+ "to_char(date_from,'yyyymmddhh24miss') date_from,"
				+ "to_char(date_to,'yyyymmddhh24miss') date_to,"
				+ "nvl(airtime_class,0) airtime_class,"
				+ "nvl(idd_class,0) idd_class,nvl(service_class,0) "
				+ "service_class, holiday_deduct,tariff_charge_id "
				+ "from tariff_charge where call_type='SMT' and "
				+ "destination_address in "
				+ "(select zone_code from zone_charge where zone_type='IDD') "
				+ "order by length(originator_address) desc,"
				+ "originator_address,length(destination_address) desc,"
				+ "to_number(destination_address) desc,date_from desc";
		Statement stmt = pConnection.createStatement();
		ResultSet rs = stmt.executeQuery(mSQL);
		boolean Found;
		if (vCacheChargeTariffSMTIntl != null) {
			vCacheChargeTariffSMTIntl.removeAllElements();
			vCacheChargeTariffSMTIntl.clear();
		}
		try {
			while (rs.next()) {
				Found = false;
				iChargeClassAir = -1;
				iChargeClassIdd = -1;
				for (int i = 0; i < vCacheChargeClass.size(); i++) {
					CacheChargeClass = (ChargeClass) vCacheChargeClass.get(i);
					if (CacheChargeClass.getChargeClassID() == rs
							.getInt("airtime_class")) {
						Found = true;
						iChargeClassAir = i;
						break;
					}
				}
				for (int i = 0; i < vCacheChargeClass.size(); i++) {
					CacheChargeClass = (ChargeClass) vCacheChargeClass.get(i);
					if (CacheChargeClass.getChargeClassID() == rs
							.getInt("idd_class")) {
						Found = true;
						iChargeClassIdd = i;
						break;
					}
				}
				if (Found == true) {
					// Add param to vector vListISDN
					CacheChargeTariffSMTIntl = new ChargeTariff(rs
							.getInt("center_id"), rs
							.getString("originator_address"), rs
							.getString("destination_address"), rs
							.getLong("date_from"), rs.getLong("date_to"), rs
							.getInt("airtime_class"), rs.getInt("idd_class"),
							rs.getInt("holiday_deduct"), iChargeClassAir,
							iChargeClassIdd);
					vCacheChargeTariffSMTIntl.add(CacheChargeTariffSMTIntl);
				} else {
					writeLogFile(" - "
							+ "Undefined tariff plan for SMT Intl tariff_charge : "
							+ rs.getInt("tariff_charge_id"));
				}
			}
		} catch (Exception ex) {
			System.err.println(Global.Format(new java.util.Date(),
					"dd/MM/yyyy HH:mm:ss")
					+ " : ERROR in module LoadChargeTariffSMTIntl : "
					+ ex.toString());
		} finally {
			try {
				rs.close();
				rs = null;
				stmt.close();
				stmt = null;
			} catch (Exception e) {
			}
		}
	}

	protected void LoadChargeTariffSMONatl(Connection pConnection)
			throws Exception {
		String mSQL = "select center_id,nvl(originator_address,'%') "
				+ "originator_address,destination_address,"
				+ "to_char(date_from,'yyyymmddhh24miss') date_from,"
				+ "to_char(date_to,'yyyymmddhh24miss') date_to,"
				+ "nvl(airtime_class,0) airtime_class,"
				+ "nvl(idd_class,0) idd_class,nvl(service_class,0) "
				+ "service_class, holiday_deduct,tariff_charge_id "
				+ "from tariff_charge where call_type='SMO' "
				+ "and destination_address in "
				+ "(select zone_code from zone_charge where zone_type='AIR') "
				+ "order by length(originator_address) desc,"
				+ "originator_address,length(destination_address) desc,"
				+ "to_number(destination_address) desc,date_from desc";
		Statement stmt = pConnection.createStatement();
		ResultSet rs = stmt.executeQuery(mSQL);
		boolean Found;
		if (vCacheChargeTariffSMONatl != null) {
			vCacheChargeTariffSMONatl.removeAllElements();
			vCacheChargeTariffSMONatl.clear();
		}
		try {
			while (rs.next()) {
				Found = false;
				iChargeClassAir = -1;
				for (int i = 0; i < vCacheChargeClass.size(); i++) {
					CacheChargeClass = (ChargeClass) vCacheChargeClass.get(i);
					if (CacheChargeClass.getChargeClassID() == rs
							.getInt("airtime_class")) {
						Found = true;
						iChargeClassAir = i;
						break;
					}
				}

				if (Found == true) {
					// Add param to vector vListISDN
					CacheChargeTariffSMONatl = new ChargeTariff(rs
							.getInt("center_id"), rs
							.getString("originator_address"), rs
							.getString("destination_address"), rs
							.getLong("date_from"), rs.getLong("date_to"), rs
							.getInt("airtime_class"), rs
							.getInt("service_class"), rs
							.getInt("holiday_deduct"), iChargeClassAir);
					vCacheChargeTariffSMONatl.add(CacheChargeTariffSMONatl);
				} else {
					writeLogFile(" - "
							+ "Undefined tariff plan for SMO tariff_charge : "
							+ rs.getInt("tariff_charge_id"));
				}
			}
		} catch (Exception ex) {
			System.err.println(Global.Format(new java.util.Date(),
					"dd/MM/yyyy HH:mm:ss")
					+ " : ERROR in module LoadChargeTariffSMONatl : "
					+ ex.toString());
		} finally {
			try {
				rs.close();
				rs = null;
				stmt.close();
				stmt = null;
			} catch (Exception e) {
			}
		}
	}

	protected void LoadChargeTariffSMTNatl(Connection pConnection)
			throws Exception {
		String mSQL = "select center_id,nvl(originator_address,'%') "
				+ "originator_address,destination_address,"
				+ "to_char(date_from,'yyyymmddhh24miss') date_from,"
				+ "to_char(date_to,'yyyymmddhh24miss') date_to,"
				+ "nvl(airtime_class,0) airtime_class,"
				+ "nvl(idd_class,0) idd_class,nvl(service_class,0) "
				+ "service_class, holiday_deduct,tariff_charge_id "
				+ "from tariff_charge where call_type='SMT' "
				+ "and destination_address in "
				+ "(select zone_code from zone_charge where zone_type='AIR') "
				+ "order by length(originator_address) desc,"
				+ "originator_address,length(destination_address) desc,"
				+ "to_number(destination_address) desc,date_from desc";
		Statement stmt = pConnection.createStatement();
		ResultSet rs = stmt.executeQuery(mSQL);
		boolean Found;
		if (vCacheChargeTariffSMTNatl != null) {
			vCacheChargeTariffSMTNatl.removeAllElements();
			vCacheChargeTariffSMTNatl.clear();
		}
		try {
			while (rs.next()) {
				Found = false;
				iChargeClassAir = -1;
				for (int i = 0; i < vCacheChargeClass.size(); i++) {
					CacheChargeClass = (ChargeClass) vCacheChargeClass.get(i);
					if (CacheChargeClass.getChargeClassID() == rs
							.getInt("airtime_class")) {
						Found = true;
						iChargeClassAir = i;
						break;
					}
				}

				if (Found == true) {
					// Add param to vector vListISDN
					CacheChargeTariffSMTNatl = new ChargeTariff(rs
							.getInt("center_id"), rs
							.getString("originator_address"), rs
							.getString("destination_address"), rs
							.getLong("date_from"), rs.getLong("date_to"), rs
							.getInt("airtime_class"), rs
							.getInt("service_class"), rs
							.getInt("holiday_deduct"), iChargeClassAir);
					vCacheChargeTariffSMTNatl.add(CacheChargeTariffSMTNatl);
				} else {
					writeLogFile(" - "
							+ "Undefined tariff plan for SMT tariff_charge : "
							+ rs.getInt("tariff_charge_id"));
				}
			}
		} catch (Exception ex) {
			System.err.println(Global.Format(new java.util.Date(),
					"dd/MM/yyyy HH:mm:ss")
					+ " : ERROR in module LoadChargeTariffSMTNatl : "
					+ ex.toString());
		} finally {
			try {
				rs.close();
				rs = null;
				stmt.close();
				stmt = null;
			} catch (Exception e) {
			}
		}
	}

	protected void LoadChargeTariffCALLServices(Connection pConnection)
			throws Exception {
		String mSQL = "select a.center_id,nvl(a.originator_address,'%') "
				+ "originator_address,a.destination_address,"
				+ "to_char(a.date_from,'yyyymmddhh24miss') date_from,"
				+ "to_char(a.date_to,'yyyymmddhh24miss') date_to,"
				+ "nvl(a.airtime_class,0) airtime_class,"
				+ "nvl(a.idd_class,0) idd_class,nvl(a.service_class,0) "
				+ "service_class, a.holiday_deduct,a.tariff_charge_id, "
				+ "zone_code_parent_id "
				+ "from tariff_charge a, zone_charge b "
				+ "where a.call_type='OG' and a.destination_address in "
				+ "(select zone_code from zone_charge where zone_type='SER') "
				+ "and a.destination_address=b.zone_code "
				+ "order by a.zone_code_parent_id,length(a.originator_address) desc,"
				+ "a.originator_address,length(a.destination_address) desc,"
				+ "to_number(a.destination_address) desc,a.date_from desc";
		Statement stmt = pConnection.createStatement();
		ResultSet rs = stmt.executeQuery(mSQL);
		boolean Found = false;
		if (vCacheChargeTariffCALLServices != null) {
			vCacheChargeTariffCALLServices.removeAllElements();
			vCacheChargeTariffCALLServices.clear();
		}
		try {
			while (rs.next()) {
				Found = false;
				iChargeClassAir = -1;
				iChargeClassSer = -1;
				for (int i = 0; i < vCacheChargeClass.size(); i++) {
					CacheChargeClass = (ChargeClass) vCacheChargeClass.get(i);
					if (CacheChargeClass.getChargeClassID() == rs
							.getInt("airtime_class")) {
						Found = true;
						iChargeClassAir = i;
						break;
					}
				}
				for (int i = 0; i < vCacheChargeClass.size(); i++) {
					CacheChargeClass = (ChargeClass) vCacheChargeClass.get(i);
					if (CacheChargeClass.getChargeClassID() == rs
							.getInt("service_class")) {
						Found = true;
						iChargeClassSer = i;
						break;
					}
				}
				if (Found == true) {
					// Add param to vector vListISDN
					CacheChargeTariffCALLServices = new ChargeTariff(rs
							.getInt("center_id"), rs
							.getString("originator_address"), rs
							.getString("destination_address"), rs
							.getLong("date_from"), rs.getLong("date_to"), rs
							.getInt("airtime_class"), rs
							.getInt("service_class"), rs
							.getInt("holiday_deduct"), iChargeClassAir,
							iChargeClassSer, rs
									.getString("zone_code_parent_id"));
					vCacheChargeTariffCALLServices
							.add(CacheChargeTariffCALLServices);
				} else {
					writeLogFile(" - "
							+ "Undefined tariff plan for CALL Services tariff_charge : "
							+ rs.getInt("tariff_charge_id"));
				}
			}
		} catch (Exception ex) {
			System.err.println(Global.Format(new java.util.Date(),
					"dd/MM/yyyy HH:mm:ss")
					+ " : ERROR in module LoadChargeTariffCALLServices : "
					+ ex.toString());
		} finally {
			try {
				rs.close();
				rs = null;
				stmt.close();
				stmt = null;
			} catch (Exception e) {
			}
		}
	}

	public int chargeINFile(Connection pConnection, int pFileID,
			String pPathConverted, String pPathCharge, String pFileName,
			int pTotalRec, String pPathCollect, String pFileCenterCode)
			throws Exception {
		String mSQL = "";
		int mRecRated = 0;
		int mRecUnRated = 0;
		int mRecN = 0;
		boolean blnFound = false;
		boolean blnRecRate = false;
		int[] miDelimitedFields;
		String mHeader = "";
		int mResult = 0;
		String RecType = "";
		String CallType = "";
		String CallingISDN = "";
		String IMSI = "";
		String CallStaTime = "";
		String CallDuration = "";
		String CallEndTime = "";
		String CalledISDN = "";
		String LocatInd = "";
		String AccProfile = "";
		String RemainCredit = "";
		String CallCost = "";
		String DisCredit = "";
		String CharClass = "";
		String TelInd = "";
		String NetInd = "";
		String INSer = "";
		String CharInd = "";
		String CallOrgISDN = "";
		String TransISDN = "";
		String ReFillType = "";
		String RefillNum = "";
		String RefillVal = "";
		String CallingOrg = "";
		String CalledOrg = "";

		RandomAccessFile fileCharge = null;
		IOUtil.forceFolderExist(pPathCharge);
		mSQL = IOUtil.FillPath(pPathCharge, Global.mSeparate) + pFileName;
		IOUtil.deleteFile(mSQL);
		TextFile fileExp = new TextFile();
		fileExp.openFile(mSQL, 5242880);

		mHeader = "CallType;CallStaTime;CallDuration;SubsType;CallingCen;"
				+ "PO_CODE;TaxAir;TaxIDD;TaxSer;CalledCen;CollectType;"
				+ "BlockAir;BlockIddSer";
		mSQL = IOUtil.FillPath(pPathCollect, Global.mSeparate) + pFileName;
		IOUtil.deleteFile(mSQL);
		fileCharge = new RandomAccessFile(mSQL, "rw");
		fileCharge.seek(fileCharge.length());
		fileCharge.writeBytes(mHeader + "\r\n");

		mSQL = "STT|FC|call type|po code|tax airtime|tax idd|"
				+ "tax service|calling isdn|imsi|call sta time|"
				+ "duration|call end time|called isdn|location|"
				+ "remain credit|call cost|dis credit|tar class|"
				+ "ts code|nw result|in serv|char indi|org call id|"
				+ "translate num|scratch type|scratch number|"
				+ "scratch value|acc profile|calling org|"
				+ "called org|subs type|bl air|bl idd/ser|"
				+ "calling cen|called cen|collect type";
		fileExp.addText(mSQL);
		mHeader = "RecType;CallType;CallingISDN;IMSI;CallStaTime;"
				+ "CallDuration;CallEndTime;CalledISDN;LocatInd;"
				+ "AccProfile;RemainCredit;CallCost;DisCredit;CharClass;"
				+ "TelInd;NetInd;INSer;CharInd;CallOrgISDN;TransISDN;"
				+ "ReFillType;RefillNum;RefillVal;CallingOrg;CalledOrg";

		mSQL = "INSERT INTO undefined_tariff(";
		mSQL += "record_type,call_type,calling_isdn,calling_imsi,call_sta_time,";
		mSQL += "duration,call_end_time,called_isdn,cell_id,";
		mSQL += "acc_profile,remain_credit,call_cost,dis_credit,tariff_class,";
		mSQL += "ts_code,bs_code,in_mark,charging_indicator,org_call_id,";
		mSQL += "translate_num,scratch_type,scratch_number,scratch_value,";
		mSQL += "file_id) VALUES";
		mSQL += "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		PreparedStatement pstmtUnRated = (OraclePreparedStatement) pConnection
				.prepareStatement(mSQL);
		((OraclePreparedStatement) pstmtUnRated).setExecuteBatch(1000);
		try {
			pConnection.setAutoCommit(false);
			// Begin Rate
			mSQL = "UPDATE import_header SET time_begin_rate=sysdate "
					+ "WHERE file_id=" + pFileID;
			Global.ExecuteSQL(pConnection, mSQL);

			mSQL = IOUtil.FillPath(pPathConverted, Global.mSeparate)
					+ pFileName;
			delimitedFile.openDelimitedFile(mSQL, 5242880);
			Vector vtFieldValue = StringUtils.vectorFromString(mHeader, ";");
			miDelimitedFields = new int[vtFieldValue.size()];
			for (int i = 0; i < miDelimitedFields.length; i++) {
				miDelimitedFields[i] = delimitedFile
						.findColumn(((String) vtFieldValue.elementAt(i)).trim());
			}

			while (delimitedFile.next()) {
				mRecN++;
				iChargeClassAir = -1;
				iChargeClassIdd = -1;
				iChargeClassSer = -1;
				blnFound = false;
				blnRecRate = false;
				chargeResult.clear();

				RecType = delimitedFile.getString(delimitedFile
						.findColumn("RecType"));
				CallType = delimitedFile.getString(delimitedFile
						.findColumn("CallType"));
				CallingISDN = delimitedFile.getString(delimitedFile
						.findColumn("CallingISDN"));
				IMSI = delimitedFile
						.getString(delimitedFile.findColumn("IMSI"));
				CallStaTime = delimitedFile.getString(delimitedFile
						.findColumn("CallStaTime"));
				CallDuration = delimitedFile.getString(delimitedFile
						.findColumn("CallDuration"));
				CallEndTime = delimitedFile.getString(delimitedFile
						.findColumn("CallEndTime"));
				CalledISDN = delimitedFile.getString(delimitedFile
						.findColumn("CalledISDN"));
				LocatInd = delimitedFile.getString(delimitedFile
						.findColumn("LocatInd"));
				AccProfile = delimitedFile.getString(delimitedFile
						.findColumn("AccProfile"));
				RemainCredit = delimitedFile.getString(delimitedFile
						.findColumn("RemainCredit"));
				CallCost = delimitedFile.getString(delimitedFile
						.findColumn("CallCost"));
				DisCredit = delimitedFile.getString(delimitedFile
						.findColumn("DisCredit"));
				CharClass = delimitedFile.getString(delimitedFile
						.findColumn("CharClass"));
				TelInd = delimitedFile.getString(delimitedFile
						.findColumn("TelInd"));
				NetInd = delimitedFile.getString(delimitedFile
						.findColumn("NetInd"));
				INSer = delimitedFile.getString(delimitedFile
						.findColumn("INSer"));
				CharInd = delimitedFile.getString(delimitedFile
						.findColumn("CharInd"));
				CallOrgISDN = delimitedFile.getString(delimitedFile
						.findColumn("CallOrgISDN"));
				TransISDN = delimitedFile.getString(delimitedFile
						.findColumn("TransISDN"));
				ReFillType = delimitedFile.getString(delimitedFile
						.findColumn("ReFillType"));
				RefillNum = delimitedFile.getString(delimitedFile
						.findColumn("RefillNum"));
				RefillVal = delimitedFile.getString(delimitedFile
						.findColumn("RefillVal"));
				CallingOrg = delimitedFile.getString(delimitedFile
						.findColumn("CallingOrg"));
				CalledOrg = delimitedFile.getString(delimitedFile
						.findColumn("CalledOrg"));

				if (((CallType.compareTo("OG") == 0) || (CallType
						.compareTo("DV") == 0))
						&& ((Integer.parseInt(CallDuration) > 0) && (CalledISDN
								.compareTo("") != 0))) {
					blnRecRate = true;
					if (CalledISDN.startsWith("00"))
						blnFound = chargeOGIntl("%" + CallingISDN, CalledISDN,
								CallStaTime, Long.parseLong(CallStaTime),
								Integer.parseInt(CallDuration));
					else
						blnFound = chargeOGNatl("%" + CallingISDN, CalledISDN,
								CallStaTime, Long.parseLong(CallStaTime),
								Integer.parseInt(CallDuration));
				} else if ((CallType.compareTo("IC") == 0)
						&& ((Integer.parseInt(CallDuration) > 0) && (CalledISDN
								.compareTo("") != 0))) {
					blnRecRate = true;
					if (CalledISDN.startsWith("00"))
						blnFound = chargeICIntl("%" + CallingISDN, CalledISDN,
								CallStaTime, Long.parseLong(CallStaTime),
								Integer.parseInt(CallDuration));
					else
						blnFound = chargeICNatl("%" + CallingISDN, CalledISDN,
								CallStaTime, Long.parseLong(CallStaTime),
								Integer.parseInt(CallDuration));
				} else if ((CallType.compareTo("SMO") == 0)
						&& (CalledISDN.compareTo("") != 0)) {
					blnRecRate = true;
					if (CalledISDN.startsWith("00"))
						blnFound = chargeSMOIntl("%" + CallingISDN, CalledISDN,
								CallStaTime, Long.parseLong(CallStaTime),
								Integer.parseInt(CallDuration));
					else
						blnFound = chargeSMONatl("%" + CallingISDN, CalledISDN,
								CallStaTime, Long.parseLong(CallStaTime),
								Integer.parseInt(CallDuration));
				} else if ((CallType.compareTo("SMT") == 0)
						&& (CalledISDN.compareTo("") != 0)) {
					blnRecRate = true;
					if (CalledISDN.startsWith("00"))
						blnFound = chargeSMTIntl("%" + CallingISDN, CalledISDN,
								CallStaTime, Long.parseLong(CallStaTime),
								Integer.parseInt(CallDuration));
					else
						blnFound = chargeSMTNatl("%" + CallingISDN, CalledISDN,
								CallStaTime, Long.parseLong(CallStaTime),
								Integer.parseInt(CallDuration));
				}

				if ((CallType.compareTo("DV") == 0)
						|| (CallType.compareTo("OG") == 0)
						|| (CallType.compareTo("SMO") == 0)) {
					FindCenter = false;
					for (int i = 0; i < vListISDN.size(); i++) {
						listISDN = (ListISDN) vListISDN.get(i);
						if ((CallingISDN.length() > listISDN.getISDN().length())
								&& (CallingISDN.substring(0,
										listISDN.getISDN().length()).compareTo(
										listISDN.getISDN()) == 0)) {
							chargeResult.intCenOfCalling = listISDN
									.getCenterID();
							chargeResult.intSubsType = listISDN.getSubsType();
							FindCenter = true;
							break;
						}
					}
					if (!FindCenter) {
						writeLogFile("     .Undefined center for ISDN:  "
								+ CallingISDN);
					}
				}

				if (blnRecRate) {
					if (blnFound) {
						mRecRated++;
						mSQL = mRecN + "|" + pFileCenterCode + "|" + CallType
								+ "|" + chargeResult.strPO_CODE + "|"
								+ chargeResult.dblTaxAir + "|"
								+ chargeResult.dblTaxIdd + "|"
								+ chargeResult.dblTaxSer + "|" + CallingISDN
								+ "|" + IMSI + "|"
								+ CallStaTime.substring(6, 8) + "/"
								+ CallStaTime.substring(4, 6) + "/"
								+ CallStaTime.substring(0, 4) + " "
								+ CallStaTime.substring(8, 10) + ":"
								+ CallStaTime.substring(10, 12) + ":"
								+ CallStaTime.substring(12, 14) + "|"
								+ CallDuration + "|" + CallEndTime + "|"
								+ CalledISDN + "|" + LocatInd + "|"
								+ RemainCredit + "|" + CallCost + "|"
								+ DisCredit + "|" + CharClass + "|" + TelInd
								+ "|" + NetInd + "|" + INSer + "|" + CharInd
								+ "|" + CallOrgISDN + "|" + TransISDN + "|"
								+ ReFillType + "|" + RefillNum + "|"
								+ RefillVal + "|" + AccProfile + "|"
								+ CallingOrg + "|" + CalledOrg + "|"
								+ chargeResult.intSubsType + "|"
								+ chargeResult.numOfBlockAir + "|"
								+ chargeResult.numOfBlockIddSer + "|"
								+ chargeResult.intCenOfCalling + "|"
								+ chargeResult.intCenOfCalled + "|"
								+ chargeResult.intCollectType;
						fileExp.addText(mSQL);

						appendValue(CallType);
						appendValue(CallStaTime);
						appendValue(CallDuration);
						appendValue(String.valueOf(chargeResult.intSubsType));
						appendValue(String
								.valueOf(chargeResult.intCenOfCalling));
						appendValue(chargeResult.strPO_CODE);
						appendValue(String.valueOf(chargeResult.dblTaxAir));
						appendValue(String.valueOf(chargeResult.dblTaxIdd));
						appendValue(String.valueOf(chargeResult.dblTaxSer));
						appendValue(String.valueOf(chargeResult.intCenOfCalled));
						appendValue(String.valueOf(chargeResult.intCollectType));
						appendValue(String.valueOf(chargeResult.numOfBlockAir));
						appendValue(String
								.valueOf(chargeResult.numOfBlockIddSer));
						fileCharge.writeBytes(mStrValues + "\r\n");
						mStrValues = "";
					} else {
						mRecUnRated++;
						mSQL = mRecN + "|" + pFileCenterCode + "|" + CallType
								+ "|||||" + CallingISDN + "|" + IMSI + "|"
								+ CallStaTime.substring(6, 8) + "/"
								+ CallStaTime.substring(4, 6) + "/"
								+ CallStaTime.substring(0, 4) + " "
								+ CallStaTime.substring(8, 10) + ":"
								+ CallStaTime.substring(10, 12) + ":"
								+ CallStaTime.substring(12, 14) + "|"
								+ CallDuration + "|" + CallEndTime + "|"
								+ CalledISDN + "|" + LocatInd + "|"
								+ RemainCredit + "|" + CallCost + "|"
								+ DisCredit + "|" + CharClass + "|" + TelInd
								+ "|" + NetInd + "|" + INSer + "|" + CharInd
								+ "|" + CallOrgISDN + "|" + TransISDN + "|"
								+ ReFillType + "|" + RefillNum + "|"
								+ RefillVal + "|" + AccProfile + "|"
								+ CallingOrg + "|" + CalledOrg + "||||||";
						fileExp.addText(mSQL);
						// Insert record unrated into DB
						pstmtUnRated.setString(1, RecType);
						pstmtUnRated.setString(2, CallType);
						pstmtUnRated.setString(3, CallingISDN);
						pstmtUnRated.setString(4, IMSI);
						pstmtUnRated.setString(5, CallStaTime);
						pstmtUnRated.setString(6, CallDuration);
						pstmtUnRated.setString(7, CallEndTime);
						pstmtUnRated.setString(8, CalledISDN);
						pstmtUnRated.setString(9, LocatInd);
						pstmtUnRated.setString(10, AccProfile);
						pstmtUnRated.setString(11, RemainCredit);
						pstmtUnRated.setString(12, CallCost);
						pstmtUnRated.setString(13, DisCredit);
						pstmtUnRated.setString(14, CharClass);
						pstmtUnRated.setString(15, TelInd);
						pstmtUnRated.setString(16, NetInd);
						pstmtUnRated.setString(17, INSer);
						pstmtUnRated.setString(18, CharInd);
						pstmtUnRated.setString(19, CallOrgISDN);
						pstmtUnRated.setString(20, TransISDN);
						pstmtUnRated.setString(21, ReFillType);
						pstmtUnRated.setString(22, RefillNum);
						pstmtUnRated.setString(23, RefillVal);
						pstmtUnRated.setInt(24, pFileID);

						pstmtUnRated.executeUpdate();
					}
				} else {
					mSQL = mRecN + "|" + pFileCenterCode + "|" + CallType
							+ "|||||" + CallingISDN + "|" + IMSI + "|"
							+ CallStaTime.substring(6, 8) + "/"
							+ CallStaTime.substring(4, 6) + "/"
							+ CallStaTime.substring(0, 4) + " "
							+ CallStaTime.substring(8, 10) + ":"
							+ CallStaTime.substring(10, 12) + ":"
							+ CallStaTime.substring(12, 14) + "|"
							+ CallDuration + "|" + CallEndTime + "|"
							+ CalledISDN + "|" + LocatInd + "|" + RemainCredit
							+ "|" + CallCost + "|" + DisCredit + "|"
							+ CharClass + "|" + TelInd + "|" + NetInd + "|"
							+ INSer + "|" + CharInd + "|" + CallOrgISDN + "|"
							+ TransISDN + "|" + ReFillType + "|" + RefillNum
							+ "|" + RefillVal + "|" + AccProfile + "|"
							+ CallingOrg + "|" + CalledOrg + "||||||";
					fileExp.addText(mSQL);
				}
			} // end of RS
			if (mRecUnRated > 0) {
				writeLogFile("     .Zone charge undefined tariff : "
						+ Global.rpad(Integer.toString(mRecUnRated), 6, " "));
				mSQL = " UPDATE import_header ";
				mSQL += " SET rec_unrate=" + mRecUnRated;
				mSQL += " WHERE file_id=" + pFileID;
				Global.ExecuteSQL(pConnection, mSQL);
			}
			writeLogFile("     .Other record uncharged       : "
					+ Global.rpad(Integer.toString(pTotalRec - mRecRated
							- mRecUnRated), 6, " "));
			writeLogFile("     .Num of record charged        : "
					+ Global.rpad(Integer.toString(mRecRated), 6, " "));
			writeLogFile("     --------------------------------------");
			writeLogFile("     Total record charged          : "
					+ Global.rpad(Integer.toString(pTotalRec), 6, " "));

			mSQL = "UPDATE import_header SET time_end_rate=sysdate,";
			mSQL += "rec_rate = " + mRecRated + ",rec_export=" + pTotalRec;
			mSQL += ",status=" + Global.StateRated + " WHERE file_id="
					+ pFileID;
			Global.ExecuteSQL(pConnection, mSQL);
			pConnection.commit();

			mResult = 0;

		} catch (FileNotFoundException e) {
			writeLogFile("   -> " + e.toString());
			mSQL = "UPDATE import_header SET status=" + Global.StateRatedError
					+ ",note='" + e.toString() + "' WHERE file_id = " + pFileID;
			Global.ExecuteSQL(pConnection, mSQL);
			pConnection.commit();
			if (cdrfileParam.OnErrorResumeNext.compareTo("TRUE") == 0) {
				return Global.ErrFileConverted;
			} else {
				throw e;
			}
		} catch (SQLException e) {
			System.err.println(Global.Format(new java.util.Date(),
					"dd/MM/yyyy HH:mm:ss")
					+ " : ERROR in module chargeINFile " + e.toString());
			mResult = 1;
			throw e;
		} catch (Exception ex) {
			pConnection.rollback();
			mSQL = "UPDATE import_header SET time_end_rate=sysdate,";
			mSQL += "note='" + ex.toString() + "',status="
					+ Global.StateRatedError;
			mSQL += " WHERE file_id=" + pFileID;
			Global.ExecuteSQL(pConnection, mSQL);
			pConnection.commit();
			mResult = 1;
			writeLogFile("   -> " + ex.toString());
			throw ex;
		} finally {
			try {
				fileExp.closeFile();
				fileCharge.close();
				delimitedFile.closeDelimitedFile();
				mSQL = null;
				pstmtUnRated.close();
				pstmtUnRated = null;
			} catch (Exception e) {
			}
		}
		return mResult;
	}

	public int chargeMSCFile(Connection pConnection, int pFileID,
			String pPathConverted, String pPathCharge, String pFileName,
			int pTotalRec, String pPathCollect, String pFileCenterCode)
			throws Exception {
		String mSQL = "";
		int mRecRated = 0;
		int mRecUnRated = 0;
		int mRecN = 0;
		boolean blnFound = false;
		boolean blnRecRate = false;
		int[] miDelimitedFields;
		String mHeader = "";
		int mResult = 0;
		String RecType = "";
		String CallType = "";
		String CallingISDN = "";
		String IMSI = "";
		String CallStaTime = "";
		String CallDuration = "";
		String CallEndTime = "";
		String CalledISDN = "";
		String CellID = "";
		String ServiceCenter = "";
		String IcRoute = "";
		String OgRoute = "";
		String TarClass = "";
		String ReqTel = "";
		String ReqBeare = "";
		String INSer = "";
		String CharInd = "";
		String CallOrgISDN = "";
		String TransISDN = "";
		String RecSeq = "";
		String IMEI = "";
		String CallingOrg = "";
		String CalledOrg = "";

		RandomAccessFile fileCharge = null;

		IOUtil.forceFolderExist(pPathCharge);
		mSQL = IOUtil.FillPath(pPathCharge, Global.mSeparate) + pFileName;
		IOUtil.deleteFile(mSQL);
		TextFile fileExp = new TextFile();
		fileExp.openFile(mSQL, 5242880);

		mHeader = "CallType;CallStaTime;CallDuration;CellID;SubsType;CallingCen;"
				+ "PO_CODE;TaxAir;TaxIDD;TaxSer;CalledCen;CollectType;"
				+ "BlockAir;BlockIddSer";
		mSQL = IOUtil.FillPath(pPathCollect, Global.mSeparate) + pFileName;
		IOUtil.deleteFile(mSQL);
		fileCharge = new RandomAccessFile(mSQL, "rw");
		fileCharge.seek(fileCharge.length());
		fileCharge.writeBytes(mHeader + "\r\n");

		mSQL = "STT|FC|call type|po code|tax airtime|tax idd|tax service|"
				+ "calling isdn|imsi|call sta time|duration|call end time|"
				+ "called isdn|cell id|service center|ic route|og route|"
				+ "tar class|ts code|bs code|in mark|char indi|org call id|"
				+ "rec seq num|translate num|calling imei|calling org|"
				+ "called org|subs type|bl air|bl idd/ser|calling cen|"
				+ "called cen|collect type";
		fileExp.addText(mSQL);

		mHeader = "RecType;CallType;CallingISDN;IMSI;CallStaTime;"
				+ "CallDuration;CallEndTime;CalledISDN;CellID;"
				+ "ServiceCenter;IcRoute;OgRoute;TarClass;"
				+ "ReqTel;ReqBeare;INSer;CharInd;CallOrgISDN;"
				+ "TransISDN;RecSeq;IMEI;CallingOrg;CalledOrg";
		mSQL = "INSERT INTO undefined_tariff(";
		mSQL += "record_type,call_type,calling_isdn,calling_imsi,call_sta_time,";
		mSQL += "duration,call_end_time,called_isdn,cell_id,";
		mSQL += "service_center,ic_route,og_route,tariff_class,";
		mSQL += "ts_code,bs_code,in_mark,charging_indicator,org_call_id,";
		mSQL += "translate_num,rec_seq_number,calling_imei,file_id) VALUES";
		mSQL += "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		PreparedStatement pstmtUnRated = (OraclePreparedStatement) pConnection
				.prepareStatement(mSQL);
		((OraclePreparedStatement) pstmtUnRated).setExecuteBatch(1000);
		try {
			pConnection.setAutoCommit(false);
			// Begin Rate
			mSQL = "UPDATE import_header SET time_begin_rate=sysdate "
					+ "WHERE file_id=" + pFileID;
			Global.ExecuteSQL(pConnection, mSQL);

			mSQL = IOUtil.FillPath(pPathConverted, Global.mSeparate)
					+ pFileName;
			delimitedFile.openDelimitedFile(mSQL, 5242880);
			Vector vtFieldValue = StringUtils.vectorFromString(mHeader, ";");
			miDelimitedFields = new int[vtFieldValue.size()];
			for (int i = 0; i < miDelimitedFields.length; i++) {
				miDelimitedFields[i] = delimitedFile
						.findColumn(((String) vtFieldValue.elementAt(i)).trim());
			}

			while (delimitedFile.next()) {
				mRecN++;
				iChargeClassAir = -1;
				iChargeClassIdd = -1;
				iChargeClassSer = -1;
				blnFound = false;
				blnRecRate = false;
				chargeResult.clear();

				RecType = delimitedFile.getString(delimitedFile
						.findColumn("RecType"));
				CallType = delimitedFile.getString(delimitedFile
						.findColumn("CallType"));
				CallingISDN = delimitedFile.getString(delimitedFile
						.findColumn("CallingISDN"));
				IMSI = delimitedFile
						.getString(delimitedFile.findColumn("IMSI"));
				CallStaTime = delimitedFile.getString(delimitedFile
						.findColumn("CallStaTime"));
				CallDuration = delimitedFile.getString(delimitedFile
						.findColumn("CallDuration"));
				CallEndTime = delimitedFile.getString(delimitedFile
						.findColumn("CallEndTime"));
				CalledISDN = delimitedFile.getString(delimitedFile
						.findColumn("CalledISDN"));
				CellID = delimitedFile.getString(delimitedFile
						.findColumn("CellID"));
				ServiceCenter = delimitedFile.getString(delimitedFile
						.findColumn("ServiceCenter"));
				IcRoute = delimitedFile.getString(delimitedFile
						.findColumn("IcRoute"));
				OgRoute = delimitedFile.getString(delimitedFile
						.findColumn("OgRoute"));
				TarClass = delimitedFile.getString(delimitedFile
						.findColumn("TarClass"));
				ReqTel = delimitedFile.getString(delimitedFile
						.findColumn("ReqTel"));
				ReqBeare = delimitedFile.getString(delimitedFile
						.findColumn("ReqBeare"));
				INSer = delimitedFile.getString(delimitedFile
						.findColumn("INSer"));
				CharInd = delimitedFile.getString(delimitedFile
						.findColumn("CharInd"));
				CallOrgISDN = delimitedFile.getString(delimitedFile
						.findColumn("CallOrgISDN"));
				TransISDN = delimitedFile.getString(delimitedFile
						.findColumn("TransISDN"));
				RecSeq = delimitedFile.getString(delimitedFile
						.findColumn("RecSeq"));
				IMEI = delimitedFile
						.getString(delimitedFile.findColumn("IMEI"));
				CallingOrg = delimitedFile.getString(delimitedFile
						.findColumn("CallingOrg"));
				CalledOrg = delimitedFile.getString(delimitedFile
						.findColumn("CalledOrg"));

				if (((CallType.compareTo("OG") == 0) || (CallType
						.compareTo("DV") == 0))
						&& ((Integer.parseInt(CallDuration) > 0) && (CalledISDN
								.compareTo("") != 0))) {
					blnRecRate = true;
					if (CalledISDN.startsWith("00"))
						blnFound = chargeOGIntl("%" + CallingISDN, CalledISDN,
								CallStaTime, Long.parseLong(CallStaTime),
								Integer.parseInt(CallDuration));
					else
						blnFound = chargeOGNatl("%" + CallingISDN, CalledISDN,
								CallStaTime, Long.parseLong(CallStaTime),
								Integer.parseInt(CallDuration));
				} else if ((CallType.compareTo("IC") == 0)
						&& ((Integer.parseInt(CallDuration) > 0) && (CalledISDN
								.compareTo("") != 0))) {
					blnRecRate = true;
					if (CalledISDN.startsWith("00"))
						blnFound = chargeICIntl("%" + CallingISDN, CalledISDN,
								CallStaTime, Long.parseLong(CallStaTime),
								Integer.parseInt(CallDuration));
					else
						blnFound = chargeICNatl("%" + CallingISDN, CalledISDN,
								CallStaTime, Long.parseLong(CallStaTime),
								Integer.parseInt(CallDuration));
				} else if ((CallType.compareTo("SMO") == 0)
						&& (CalledISDN.compareTo("") != 0)) {
					blnRecRate = true;
					if (CalledISDN.startsWith("00"))
						blnFound = chargeSMOIntl("%" + CallingISDN, CalledISDN,
								CallStaTime, Long.parseLong(CallStaTime),
								Integer.parseInt(CallDuration));
					else
						blnFound = chargeSMONatl("%" + CallingISDN, CalledISDN,
								CallStaTime, Long.parseLong(CallStaTime),
								Integer.parseInt(CallDuration));
				} else if ((CallType.compareTo("SMT") == 0)
						&& (CalledISDN.compareTo("") != 0)) {
					blnRecRate = true;
					if (CalledISDN.startsWith("00"))
						blnFound = chargeSMTIntl("%" + CallingISDN, CalledISDN,
								CallStaTime, Long.parseLong(CallStaTime),
								Integer.parseInt(CallDuration));
					else
						blnFound = chargeSMTNatl("%" + CallingISDN, CalledISDN,
								CallStaTime, Long.parseLong(CallStaTime),
								Integer.parseInt(CallDuration));
				}
				if ((CallType.compareTo("TS") != 0)
						&& (CallType.compareTo("RM") != 0)
						&& (IMSI.length() > 0)) {
					FindCenter = false;
					for (int i = 0; i < vListIMSI.size(); i++) {
						listIMSI = (ListIMSI) vListIMSI.get(i);
						if (IMSI.substring(0, listIMSI.getIMSI().length())
								.compareTo(listIMSI.getIMSI()) == 0) {
							chargeResult.intCenOfCalling = listIMSI
									.getCenterID();
							chargeResult.intSubsType = listIMSI.getSubsType();
							FindCenter = true;
							break;
						} else if ((listIMSI.getIMSI().compareTo("%") == 0)
								&& (IMSI.substring(0, 5).compareTo("45201") != 0)) {
							chargeResult.intCenOfCalling = listIMSI
									.getCenterID();
							chargeResult.intSubsType = listIMSI.getSubsType();
							FindCenter = true;
							break;
						}
					}
					if (!FindCenter) {
						writeLogFile("     .Undefined center for SIM:  " + IMSI);
					}
				}

				if (blnRecRate) {
					if (blnFound) {
						mRecRated++;
						mSQL = mRecN + "|" + pFileCenterCode + "|" + CallType
								+ "|" + chargeResult.strPO_CODE + "|"
								+ chargeResult.dblTaxAir + "|"
								+ chargeResult.dblTaxIdd + "|"
								+ chargeResult.dblTaxSer + "|" + CallingISDN
								+ "|" + IMSI + "|"
								+ CallStaTime.substring(6, 8) + "/"
								+ CallStaTime.substring(4, 6) + "/"
								+ CallStaTime.substring(0, 4) + " "
								+ CallStaTime.substring(8, 10) + ":"
								+ CallStaTime.substring(10, 12) + ":"
								+ CallStaTime.substring(12, 14) + "|"
								+ CallDuration + "|" + CallEndTime + "|"
								+ CalledISDN + "|" + CellID + "|"
								+ ServiceCenter + "|" + IcRoute + "|" + OgRoute
								+ "|" + TarClass + "|" + ReqTel + "|"
								+ ReqBeare + "|" + INSer + "|" + CharInd + "|"
								+ CallOrgISDN + "|" + RecSeq + "|" + TransISDN
								+ "|" + IMEI + "|" + CallingOrg + "|"
								+ CalledOrg + "|" + chargeResult.intSubsType
								+ "|" + chargeResult.numOfBlockAir + "|"
								+ chargeResult.numOfBlockIddSer + "|"
								+ chargeResult.intCenOfCalling + "|"
								+ chargeResult.intCenOfCalled + "|"
								+ chargeResult.intCollectType;
						fileExp.addText(mSQL);

						appendValue(CallType);
						appendValue(CallStaTime);
						appendValue(CallDuration);
						appendValue(CellID);
						appendValue(String.valueOf(chargeResult.intSubsType));
						appendValue(String
								.valueOf(chargeResult.intCenOfCalling));
						appendValue(chargeResult.strPO_CODE);
						appendValue(String.valueOf(chargeResult.dblTaxAir));
						appendValue(String.valueOf(chargeResult.dblTaxIdd));
						appendValue(String.valueOf(chargeResult.dblTaxSer));
						appendValue(String.valueOf(chargeResult.intCenOfCalled));
						appendValue(String.valueOf(chargeResult.intCollectType));
						appendValue(String.valueOf(chargeResult.numOfBlockAir));
						appendValue(String
								.valueOf(chargeResult.numOfBlockIddSer));
						fileCharge.writeBytes(mStrValues + "\r\n");
						mStrValues = "";
					} else {
						mRecUnRated++;
						mSQL = mRecN + "|" + pFileCenterCode + "|" + CallType
								+ "|||||" + CallingISDN + "|" + IMSI + "|"
								+ CallStaTime.substring(6, 8) + "/"
								+ CallStaTime.substring(4, 6) + "/"
								+ CallStaTime.substring(0, 4) + " "
								+ CallStaTime.substring(8, 10) + ":"
								+ CallStaTime.substring(10, 12) + ":"
								+ CallStaTime.substring(12, 14) + "|"
								+ CallDuration + "|" + CallEndTime + "|"
								+ CalledISDN + "|" + CellID + "|"
								+ ServiceCenter + "|" + IcRoute + "|" + OgRoute
								+ "|" + TarClass + "|" + ReqTel + "|"
								+ ReqBeare + "|" + INSer + "|" + CharInd + "|"
								+ CallOrgISDN + "|" + RecSeq + "|" + TransISDN
								+ "|" + IMEI + "|" + CallingOrg + "|"
								+ CalledOrg + "||||||";
						fileExp.addText(mSQL);

						// Insert record unrate into DB
						pstmtUnRated.setString(1, RecType);
						pstmtUnRated.setString(2, CallType);
						pstmtUnRated.setString(3, CallingISDN);
						pstmtUnRated.setString(4, IMSI);
						pstmtUnRated.setString(5, CallStaTime);
						pstmtUnRated.setString(6, CallDuration);
						pstmtUnRated.setString(7, CallEndTime);
						pstmtUnRated.setString(8, CalledISDN);
						pstmtUnRated.setString(9, CellID);
						pstmtUnRated.setString(10, ServiceCenter);
						pstmtUnRated.setString(11, IcRoute);
						pstmtUnRated.setString(12, OgRoute);
						pstmtUnRated.setString(13, TarClass);
						pstmtUnRated.setString(14, ReqTel);
						pstmtUnRated.setString(15, ReqBeare);
						pstmtUnRated.setString(16, INSer);
						pstmtUnRated.setString(17, CharInd);
						pstmtUnRated.setString(18, CallOrgISDN);
						pstmtUnRated.setString(19, TransISDN);
						pstmtUnRated.setString(20, RecSeq);
						pstmtUnRated.setString(21, IMEI);
						pstmtUnRated.setInt(22, pFileID);
						pstmtUnRated.executeUpdate();
					}
				} else {
					mSQL = mRecN + "|" + pFileCenterCode + "|" + CallType
							+ "|||||" + CallingISDN + "|" + IMSI + "|"
							+ CallStaTime.substring(6, 8) + "/"
							+ CallStaTime.substring(4, 6) + "/"
							+ CallStaTime.substring(0, 4) + " "
							+ CallStaTime.substring(8, 10) + ":"
							+ CallStaTime.substring(10, 12) + ":"
							+ CallStaTime.substring(12, 14) + "|"
							+ CallDuration + "|" + CallEndTime + "|"
							+ CalledISDN + "|" + CellID + "|" + ServiceCenter
							+ "|" + IcRoute + "|" + OgRoute + "|" + TarClass
							+ "|" + ReqTel + "|" + ReqBeare + "|" + INSer + "|"
							+ CharInd + "|" + CallOrgISDN + "|" + RecSeq + "|"
							+ TransISDN + "|" + IMEI + "|" + CallingOrg + "|"
							+ CalledOrg + "||||||";
					fileExp.addText(mSQL);
				}
			} // end of RS
			if (mRecUnRated > 0) {
				writeLogFile("     .Zone charge undefined tariff : "
						+ Global.rpad(Integer.toString(mRecUnRated), 6, " "));
				mSQL = " UPDATE import_header ";
				mSQL += " SET rec_unrate=" + mRecUnRated;
				mSQL += " WHERE file_id=" + pFileID;
				Global.ExecuteSQL(pConnection, mSQL);
			}
			writeLogFile("     .Other record uncharged       : "
					+ Global.rpad(Integer.toString(pTotalRec - mRecRated
							- mRecUnRated), 6, " "));
			writeLogFile("     .Num of record charged        : "
					+ Global.rpad(Integer.toString(mRecRated), 6, " "));
			writeLogFile("     --------------------------------------");
			writeLogFile("     Total record charged          : "
					+ Global.rpad(Integer.toString(pTotalRec), 6, " "));

			mSQL = "UPDATE import_header SET time_end_rate=sysdate,";
			mSQL += "rec_rate = " + mRecRated + ",rec_export=" + pTotalRec;
			mSQL += ",status=" + Global.StateRated + " WHERE file_id="
					+ pFileID;
			Global.ExecuteSQL(pConnection, mSQL);
			pConnection.commit();

			mResult = 0;

		} catch (FileNotFoundException e) {
			writeLogFile("   -> " + e.toString());
			mSQL = "UPDATE import_header SET status=" + Global.StateRatedError
					+ ",note='" + e.toString() + "' WHERE file_id = " + pFileID;
			Global.ExecuteSQL(pConnection, mSQL);
			pConnection.commit();
			if (cdrfileParam.OnErrorResumeNext.compareTo("TRUE") == 0) {
				return Global.ErrFileConverted;
			} else {
				throw e;
			}
		} catch (SQLException e) {
			System.err.println(Global.Format(new java.util.Date(),
					"dd/MM/yyyy HH:mm:ss")
					+ " : ERROR in module chargeMSCFile " + e.toString());
			mResult = 1;
			throw e;
		} catch (Exception ex) {
			pConnection.rollback();
			mSQL = "UPDATE import_header SET time_end_rate=sysdate,";
			mSQL += "note='" + ex.toString() + "',status="
					+ Global.StateRatedError;
			mSQL += " WHERE file_id=" + pFileID;
			Global.ExecuteSQL(pConnection, mSQL);
			pConnection.commit();
			mResult = 1;
			writeLogFile("   -> " + ex.toString());
			throw ex;
		} finally {
			try {
				fileExp.closeFile();
				fileCharge.close();
				delimitedFile.closeDelimitedFile();
				mSQL = null;
				pstmtUnRated.close();
				pstmtUnRated = null;
			} catch (Exception e) {
			}
		}
		return mResult;
	}

	protected void appendValue(String strValue) {
		if (mStrValues.length() == 0) {
			mStrValues = strValue;
		} else {
			mStrValues += Global.cstrDelimited;
			mStrValues += strValue;
		}
	}

	protected String getFieldValues(int[] intFields) throws Exception {
		String strResult = "";
		int intLength = intFields.length - 1;
		for (int i = 0; i <= intLength; i++) {
			if (intLength == i) {
				strResult += delimitedFile.getString(intFields[i]);
			} else {
				strResult += delimitedFile.getString(intFields[i]) + ";";
			}
		}
		return strResult;
	}

	protected boolean chargeCall(int pChargeType, int pIndexOfChargeClass,
			int pChargeClassID, String pFuncCall, String pCallingISDN,
			String pCalledISDN, String pStrCallDateTime, long pCallDateTime,
			int pCallTime, int pDuration) throws Exception {
		int mTotalBlockCharge = 0;
		int mNumOfBlockRemain = 0;
		boolean blnFound = false;
		int mDayOfWeek = 0;

		try {
			mDayOfWeek = Global.FormatDayOfWeek(pStrCallDateTime,
					"yyyyMMddHHmmss");
			for (int j = pIndexOfChargeClass; j < vCacheChargeClass.size(); j++) {
				CacheChargeClass = (ChargeClass) vCacheChargeClass.get(j);
				if ((CacheChargeClass.getChargeClassID() == pChargeClassID)
						&& (CacheChargeClass.getDateFrom() <= pCallDateTime)
						&& (CacheChargeClass.getDateTo() >= pCallDateTime)) {
					for (int k = CacheChargeClass.getIndexOfChargePlan(); k < vCacheChargePlan
							.size(); k++) {
						CacheChargePlan = (ChargePlan) vCacheChargePlan.get(k);
						if ((CacheChargePlan.getWeekInfo() == mDayOfWeek)
								|| (CacheChargePlan.getWeekInfo() == 0)) // ap
						// dung
						// cho
						// tat
						// ca
						// cac
						// ngay
						{
							if ((CacheChargePlan.getDateFrom() <= pCallDateTime)
									&& (CacheChargePlan.getDateTo() >= pCallDateTime)
									&& (CacheChargePlan.getTimeFrom() <= pCallTime)
									&& (CacheChargePlan.getTimeTo() >= pCallTime)) {
								chargeResult.intCollectType = CacheChargePlan
										.getCollectType();
								// Rating detail
								if (pDuration < CacheChargeClass
										.getTimeDestroy()) {
									blnFound = true;
									chargeResult.dblTaxAir = 0;
									chargeResult.dblTaxIdd = 0;
									chargeResult.dblTaxSer = 0;
									chargeResult.numOfBlockAir = 0;
									chargeResult.numOfBlockIddSer = 0;
									break;
								}
								for (int m = CacheChargePlan
										.getIndexOfChargeAreaFistBlock(); m < vCacheChargeArea
										.size(); m++) {
									CacheChargeArea = (ChargeArea) vCacheChargeArea
											.get(m);
									if ((CacheChargeArea.getDateFrom() <= pCallDateTime)
											&& (CacheChargeArea.getDateTo() >= pCallDateTime)) {
										chargeResult.TaxFirstBlock = CacheChargeArea
												.getTax();
										break;
									}
								}
								for (int m = CacheChargePlan
										.getIndexOfChargeAreaNextBlock(); m < vCacheChargeArea
										.size(); m++) {
									CacheChargeArea = (ChargeArea) vCacheChargeArea
											.get(m);
									if ((CacheChargeArea.getDateFrom() <= pCallDateTime)
											&& (CacheChargeArea.getDateTo() >= pCallDateTime)) {
										chargeResult.TaxNextBlock = CacheChargeArea
												.getTax();
										break;
									}
								}
								blnFound = true;
								if (pDuration <= CacheChargeClass.getTimeMin()) {
									if (pChargeType == mChargeAirtime) {
										chargeResult.dblTaxAir = chargeResult.TaxFirstBlock;
										chargeResult.numOfBlockAir = CacheChargePlan
												.getFirstBlock();
									} else if (pChargeType == mChargeIdd) {
										chargeResult.dblTaxIdd = chargeResult.TaxFirstBlock;
										chargeResult.numOfBlockIddSer = CacheChargePlan
												.getFirstBlock();
									} else if (pChargeType == mChargeServices) {
										chargeResult.dblTaxSer = chargeResult.TaxFirstBlock;
										chargeResult.numOfBlockIddSer = CacheChargePlan
												.getFirstBlock();
										;
									}
								} else {
									mTotalBlockCharge = (int) Math
											.ceil((double) pDuration
													/ CacheChargeClass
															.getTimeBlock());
									mNumOfBlockRemain = mTotalBlockCharge
											- CacheChargePlan.getFirstBlock();
									mTotalBlockCharge = (int) Math
											.ceil((double) mNumOfBlockRemain
													/ CacheChargePlan
															.getNextBlock());
									if (pChargeType == mChargeAirtime) {
										chargeResult.numOfBlockAir = (int) Math
												.ceil((double) pDuration
														/ CacheChargeClass
																.getTimeBlock());
										chargeResult.dblTaxAir = Global
												.round(
														chargeResult.TaxFirstBlock
																+ (mTotalBlockCharge * chargeResult.TaxNextBlock),
														3);
									} else if (pChargeType == mChargeIdd) {
										chargeResult.numOfBlockIddSer = (int) Math
												.ceil((double) pDuration
														/ CacheChargeClass
																.getTimeBlock());
										chargeResult.dblTaxIdd = Global
												.round(
														chargeResult.TaxFirstBlock
																+ (mTotalBlockCharge * chargeResult.TaxNextBlock),
														3);
									} else if (pChargeType == mChargeServices) {
										chargeResult.numOfBlockIddSer = (int) Math
												.ceil((double) pDuration
														/ CacheChargeClass
																.getTimeBlock());
										chargeResult.dblTaxSer = Global
												.round(
														chargeResult.TaxFirstBlock
																+ (mTotalBlockCharge * chargeResult.TaxNextBlock),
														3);
									}
								}
								break;
							}
						}
					}
					break;
				}
			}
			if (!blnFound) {
				writeLogFile("     .Undefined tariff plan in module "
						+ pFuncCall + " with param input:");
				writeLogFile("        -> Calling_ISDN : "
						+ pCallingISDN.substring(1));
				writeLogFile("        -> Called_ISDN  : " + pCalledISDN);
				writeLogFile("        -> CallDateTime : " + pCallDateTime);
				writeLogFile("        -> Duration     : " + pDuration);
			}
		} catch (Exception e) {
			System.err.println(Global.Format(new java.util.Date(),
					"dd/MM/yyyy HH:mm:ss")
					+ " : ERROR in module chargeCall : " + e.toString());
		}
		return blnFound;
	}

	protected boolean chargeSMS(int pChargeType, int pIndexOfChargeClass,
			int pChargeClassID, String pFuncCall, String pCallingISDN,
			String pCalledISDN, String pStrCallDateTime, long pCallDateTime,
			int pCallTime, int pDuration) throws Exception {
		boolean blnFound = false;
		int mDayOfWeek = 0;

		try {
			mDayOfWeek = Global.FormatDayOfWeek(pStrCallDateTime,
					"yyyyMMddHHmmss");
			for (int j = pIndexOfChargeClass; j < vCacheChargeClass.size(); j++) {
				CacheChargeClass = (ChargeClass) vCacheChargeClass.get(j);
				if ((CacheChargeClass.getChargeClassID() == pChargeClassID)
						&& (CacheChargeClass.getDateFrom() <= pCallDateTime)
						&& (CacheChargeClass.getDateTo() >= pCallDateTime)) {
					for (int k = CacheChargeClass.getIndexOfChargePlan(); k < vCacheChargePlan
							.size(); k++) {
						CacheChargePlan = (ChargePlan) vCacheChargePlan.get(k);
						if ((CacheChargePlan.getWeekInfo() == mDayOfWeek)
								|| (CacheChargePlan.getWeekInfo() == 0)) // ap
						// dung
						// cho
						// tat
						// ca
						// cac
						// ngay
						{
							if ((CacheChargePlan.getDateFrom() <= pCallDateTime)
									&& (CacheChargePlan.getDateTo() >= pCallDateTime)
									&& (CacheChargePlan.getTimeFrom() <= pCallTime)
									&& (CacheChargePlan.getTimeTo() >= pCallTime)) {
								chargeResult.intCollectType = CacheChargePlan
										.getCollectType();
								// Rating detail
								for (int m = CacheChargePlan
										.getIndexOfChargeAreaFistBlock(); m < vCacheChargeArea
										.size(); m++) {
									CacheChargeArea = (ChargeArea) vCacheChargeArea
											.get(m);
									if ((CacheChargeArea.getDateFrom() <= pCallDateTime)
											&& (CacheChargeArea.getDateTo() >= pCallDateTime)) {
										chargeResult.TaxFirstBlock = CacheChargeArea
												.getTax();
										break;
									}
								}
								for (int m = CacheChargePlan
										.getIndexOfChargeAreaNextBlock(); m < vCacheChargeArea
										.size(); m++) {
									CacheChargeArea = (ChargeArea) vCacheChargeArea
											.get(m);
									if ((CacheChargeArea.getDateFrom() <= pCallDateTime)
											&& (CacheChargeArea.getDateTo() >= pCallDateTime)) {
										chargeResult.TaxNextBlock = CacheChargeArea
												.getTax();
										break;
									}
								}
								blnFound = true;
								if (pDuration <= CacheChargeClass.getTimeMin()) {
									if (pChargeType == mChargeAirtime) {
										chargeResult.dblTaxAir = chargeResult.TaxFirstBlock;
										chargeResult.numOfBlockAir = CacheChargePlan
												.getFirstBlock();
									} else if (pChargeType == mChargeIdd) {
										chargeResult.dblTaxIdd = chargeResult.TaxFirstBlock;
										chargeResult.numOfBlockIddSer = CacheChargePlan
												.getFirstBlock();
									} else if (pChargeType == mChargeServices) {
										chargeResult.dblTaxSer = chargeResult.TaxFirstBlock;
										chargeResult.numOfBlockIddSer = CacheChargePlan
												.getFirstBlock();
										;
									}
								} else {
									if (pChargeType == mChargeAirtime) {
										chargeResult.dblTaxAir = chargeResult.TaxFirstBlock;
									} else if (pChargeType == mChargeIdd) {
										chargeResult.dblTaxIdd = chargeResult.TaxFirstBlock;
									} else if (pChargeType == mChargeServices) {
										chargeResult.dblTaxSer = chargeResult.TaxFirstBlock;
									}
								}
								break;
							}
						}
					}
					break;
				}
			}
			if (!blnFound) {
				writeLogFile("     .Undefined tariff plan in module "
						+ pFuncCall + " with param input:");
				writeLogFile("        -> Calling_ISDN : "
						+ pCallingISDN.substring(1));
				writeLogFile("        -> Called_ISDN  : " + pCalledISDN);
				writeLogFile("        -> CallDateTime : " + pCallDateTime);
				writeLogFile("        -> Duration     : " + pDuration);
			}
		} catch (Exception e) {
			System.err.println(Global.Format(new java.util.Date(),
					"dd/MM/yyyy HH:mm:ss")
					+ " : ERROR in module chargeSMS : " + e.toString());
		}
		return blnFound;
	}

	public boolean chargeTESTCASE(String pCallType, String pCallingISDN,
			String pCalledISDN, String pStrCallStaTime, long pCallDateTime,
			int pDuration) throws Exception {
		boolean mFound = false;
		chargeResult.clear();
		if (pCallType.compareTo("OG") == 0) {
			if (pCalledISDN.substring(0, 2).compareTo("00") != 0)
				mFound = chargeOGNatl(pCallingISDN, pCalledISDN,
						pStrCallStaTime, pCallDateTime, pDuration);
			else
				mFound = chargeOGIntl(pCallingISDN, pCalledISDN,
						pStrCallStaTime, pCallDateTime, pDuration);
		} else if (pCallType.compareTo("IC") == 0) {
			if (pCalledISDN.substring(0, 2).compareTo("00") != 0)
				mFound = chargeICNatl(pCallingISDN, pCalledISDN,
						pStrCallStaTime, pCallDateTime, pDuration);
			else
				mFound = chargeICIntl(pCallingISDN, pCalledISDN,
						pStrCallStaTime, pCallDateTime, pDuration);
		} else if (pCallType.compareTo("SMO") == 0) {
			if (pCalledISDN.substring(0, 2).compareTo("00") != 0)
				mFound = chargeSMONatl(pCallingISDN, pCalledISDN,
						pStrCallStaTime, pCallDateTime, pDuration);
			else
				mFound = chargeSMOIntl(pCallingISDN, pCalledISDN,
						pStrCallStaTime, pCallDateTime, pDuration);
		} else if (pCallType.compareTo("SMT") == 0) {
			if (pCalledISDN.substring(0, 2).compareTo("00") != 0)
				mFound = chargeSMTNatl(pCallingISDN, pCalledISDN,
						pStrCallStaTime, pCallDateTime, pDuration);
			else
				mFound = chargeSMTIntl(pCallingISDN, pCalledISDN,
						pStrCallStaTime, pCallDateTime, pDuration);
		}
		return mFound;
	}
}
