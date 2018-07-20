package cdrfile.collector;

/**
 * <p>Title: CDR File(s) System</p>
 * <p>Description: VMS IS Departerment</p>
 * <p>Copyright: Copyright (c)  by eKnowledge 2005</p>
 * <p>Company: VietNam Mobile Telecom Services</p>
 * @author eKnowledge - Software
 * @version 1.0
 */

import java.io.FileNotFoundException;
import java.sql.PreparedStatement;
import java.util.Vector;

import oracle.jdbc.OraclePreparedStatement;
import cdrfile.global.DelimitedFile;
import cdrfile.global.Global;
import cdrfile.global.IOUtils;
import cdrfile.global.StringUtils;

public class Collection extends Global {
	IOUtils IOUtil = new IOUtils();

	public Collection() throws Exception {
	}

	public int CollectiveSwitch(java.sql.Connection pConnection, int pSwitchID,
			String pPathRated, String pFileName) throws Exception {
		String mSQL = null;
		PreparedStatement pstmtInsert_CALL = null;
		PreparedStatement pstmtUpdate_CALL = null;
		PreparedStatement pstmtInsert_SMS = null;
		PreparedStatement pstmtUpdate_SMS = null;
		int[] miDelimitedFields;
		DelimitedFile delimitedFile = new DelimitedFile();
		String mHeader = "CallType;CallStaTime;CallDuration;CellID;SubsType;CallingCen;"
				+ "PO_CODE;TaxAir;TaxIDD;TaxSer;CalledCen;CollectType";
		try {
			pConnection.setAutoCommit(false);

			mSQL = "INSERT INTO collect_switch_call(switch_id,call_type,"
					+ "subs_type,po_code,call_date,num_of_records,traffic_peak,"
					+ "traffic_offpeak,turnover_peak,turnover_offpeak,tax_vnd,"
					+ "tax_usd,cen_of_calling,cen_of_called) "
					+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			pstmtInsert_CALL = (OraclePreparedStatement) pConnection
					.prepareStatement(mSQL);

			mSQL = "UPDATE collect_switch_call "
					+ "SET num_of_records=num_of_records+?,"
					+ "traffic_peak=traffic_peak+?,traffic_offpeak=traffic_offpeak+?,"
					+ "turnover_peak=turnover_peak+?,turnover_offpeak=turnover_offpeak+?,"
					+ "tax_vnd=tax_vnd+?,tax_usd=tax_usd+? "
					+ "WHERE switch_id=? and call_type=? and subs_type=? "
					+ "and po_code=? and call_date=? and cen_of_calling=? "
					+ "and cen_of_called=?";
			pstmtUpdate_CALL = (OraclePreparedStatement) pConnection
					.prepareStatement(mSQL);

			mSQL = "INSERT INTO collect_switch_sms(switch_id,call_type,"
					+ "subs_type,po_code,call_date,num_of_records,tax_vnd,tax_usd,"
					+ "cen_of_calling,cen_of_called) VALUES(?,?,?,?,?,?,?,?,?,?)";
			pstmtInsert_SMS = (OraclePreparedStatement) pConnection
					.prepareStatement(mSQL);

			mSQL = "UPDATE collect_switch_sms "
					+ "SET num_of_records=num_of_records+?,"
					+ "tax_vnd=tax_vnd+?,tax_usd=tax_usd+? "
					+ "WHERE switch_id=? and call_type=? and subs_type=? "
					+ "and po_code=? and call_date=? and cen_of_calling=? "
					+ "and cen_of_called=?";
			pstmtUpdate_SMS = (OraclePreparedStatement) pConnection
					.prepareStatement(mSQL);

			mSQL = IOUtil.FillPath(pPathRated, Global.mSeparate) + pFileName;
			delimitedFile.openDelimitedFile(mSQL, 5242880);
			Vector vtFieldValue = StringUtils.vectorFromString(mHeader, ";");
			miDelimitedFields = new int[vtFieldValue.size()];
			for (int i = 0; i < miDelimitedFields.length; i++) {
				miDelimitedFields[i] = delimitedFile
						.findColumn(((String) vtFieldValue.elementAt(i)).trim());
			}
			while (delimitedFile.next()) {
				if ((delimitedFile.getString(
						delimitedFile.findColumn("PO_CODE")).compareTo("") != 0)
						&& ((delimitedFile.getString(
								delimitedFile.findColumn("CallType"))
								.compareTo("OG") == 0)
								|| (delimitedFile.getString(
										delimitedFile.findColumn("CallType"))
										.compareTo("DV") == 0) || (delimitedFile
								.getString(delimitedFile.findColumn("CallType"))
								.compareTo("IC") == 0))) {
					pstmtUpdate_CALL.setInt(1, 1);
					if (Integer.parseInt(delimitedFile.getString(delimitedFile
							.findColumn("CollectType"))) == 1) // rush_hour
					{
						pstmtUpdate_CALL.setInt(2, Integer
								.parseInt(delimitedFile.getString(delimitedFile
										.findColumn("CallDuration"))));
						pstmtUpdate_CALL.setInt(3, 0);
						pstmtUpdate_CALL.setInt(4, (int) Math.ceil(Integer
								.parseInt(delimitedFile.getString(delimitedFile
										.findColumn("CallDuration"))) / 60));
						pstmtUpdate_CALL.setInt(5, 0);
					} else {
						pstmtUpdate_CALL.setInt(2, 0);
						pstmtUpdate_CALL.setInt(3, Integer
								.parseInt(delimitedFile.getString(delimitedFile
										.findColumn("CallDuration"))));
						pstmtUpdate_CALL.setInt(4, 0);
						pstmtUpdate_CALL.setInt(5, (int) Math.ceil(Integer
								.parseInt(delimitedFile.getString(delimitedFile
										.findColumn("CallDuration"))) / 60));
					}
					pstmtUpdate_CALL.setDouble(6, Double
							.parseDouble(delimitedFile.getString(delimitedFile
									.findColumn("TaxAir")))
							+ Double.parseDouble(delimitedFile
									.getString(delimitedFile
											.findColumn("TaxSer"))));
					pstmtUpdate_CALL.setDouble(7, Double
							.parseDouble(delimitedFile.getString(delimitedFile
									.findColumn("TaxIdd"))));
					pstmtUpdate_CALL.setInt(8, pSwitchID);
					if (delimitedFile.getString(
							delimitedFile.findColumn("CallType")).compareTo(
							"DV") == 0)
						pstmtUpdate_CALL.setString(9, "OG");
					else
						pstmtUpdate_CALL.setString(9,
								delimitedFile.getString(delimitedFile
										.findColumn("CallType")));
					pstmtUpdate_CALL.setString(10, delimitedFile
							.getString(delimitedFile.findColumn("SubsType")));
					pstmtUpdate_CALL.setString(11, delimitedFile
							.getString(delimitedFile.findColumn("PO_CODE")));
					pstmtUpdate_CALL.setString(12, delimitedFile.getString(
							delimitedFile.findColumn("CallStaTime")).substring(
							0, 8));
					pstmtUpdate_CALL
							.setInt(13, Integer.parseInt(delimitedFile
									.getString(delimitedFile
											.findColumn("CallingCen"))));
					pstmtUpdate_CALL.setInt(14, Integer.parseInt(delimitedFile
							.getString(delimitedFile.findColumn("CalledCen"))));
					if (pstmtUpdate_CALL.executeUpdate() == 0) {
						pstmtInsert_CALL.setInt(1, pSwitchID);
						if (delimitedFile.getString(
								delimitedFile.findColumn("CallType"))
								.compareTo("DV") == 0)
							pstmtInsert_CALL.setString(2, "OG");
						else
							pstmtInsert_CALL.setString(2, delimitedFile
									.getString(delimitedFile
											.findColumn("CallType")));
						pstmtInsert_CALL.setString(3,
								delimitedFile.getString(delimitedFile
										.findColumn("SubsType")));
						pstmtInsert_CALL
								.setString(4, delimitedFile
										.getString(delimitedFile
												.findColumn("PO_CODE")));
						pstmtInsert_CALL.setString(5, delimitedFile.getString(
								delimitedFile.findColumn("CallStaTime"))
								.substring(0, 8));
						pstmtInsert_CALL.setInt(6, 1);
						if (Integer.parseInt(delimitedFile
								.getString(delimitedFile
										.findColumn("CollectType"))) == 1) // rush_hour
						{
							pstmtInsert_CALL
									.setInt(
											7,
											Integer
													.parseInt(delimitedFile
															.getString(delimitedFile
																	.findColumn("CallDuration"))));
							pstmtInsert_CALL.setInt(8, 0);
							pstmtInsert_CALL
									.setInt(
											9,
											(int) Math
													.ceil(Integer
															.parseInt(delimitedFile
																	.getString(delimitedFile
																			.findColumn("CallDuration"))) / 60));
							pstmtInsert_CALL.setInt(10, 0);
						} else {
							pstmtInsert_CALL.setInt(7, 0);
							pstmtInsert_CALL
									.setInt(
											8,
											Integer
													.parseInt(delimitedFile
															.getString(delimitedFile
																	.findColumn("CallDuration"))));
							pstmtInsert_CALL.setInt(9, 0);
							pstmtInsert_CALL
									.setInt(
											10,
											(int) Math
													.ceil(Integer
															.parseInt(delimitedFile
																	.getString(delimitedFile
																			.findColumn("CallDuration"))) / 60));
						}
						pstmtInsert_CALL.setDouble(11, Double
								.parseDouble(delimitedFile
										.getString(delimitedFile
												.findColumn("TaxAir")))
								+ Double.parseDouble(delimitedFile
										.getString(delimitedFile
												.findColumn("TaxSer"))));
						pstmtInsert_CALL.setDouble(12, Double
								.parseDouble(delimitedFile
										.getString(delimitedFile
												.findColumn("TaxIdd"))));
						pstmtInsert_CALL.setInt(13, Integer
								.parseInt(delimitedFile.getString(delimitedFile
										.findColumn("CallingCen"))));
						pstmtInsert_CALL.setInt(14, Integer
								.parseInt(delimitedFile.getString(delimitedFile
										.findColumn("CalledCen"))));
						pstmtInsert_CALL.executeUpdate();
					}
				} // end if call type
				else if ((delimitedFile.getString(
						delimitedFile.findColumn("PO_CODE")).compareTo("") != 0)
						&& ((delimitedFile.getString(
								delimitedFile.findColumn("CallType"))
								.compareTo("SMO") == 0) || (delimitedFile
								.getString(delimitedFile.findColumn("CallType"))
								.compareTo("SMT") == 0))) {
					pstmtUpdate_SMS.setInt(1, 1);
					pstmtUpdate_SMS.setDouble(2, Double
							.parseDouble(delimitedFile.getString(delimitedFile
									.findColumn("TaxAir")))
							+ Double.parseDouble(delimitedFile
									.getString(delimitedFile
											.findColumn("TaxSer"))));
					pstmtUpdate_SMS.setDouble(3, Double
							.parseDouble(delimitedFile.getString(delimitedFile
									.findColumn("TaxIdd"))));
					pstmtUpdate_SMS.setInt(4, pSwitchID);
					pstmtUpdate_SMS.setString(5, delimitedFile
							.getString(delimitedFile.findColumn("CallType")));
					pstmtUpdate_SMS.setString(6, delimitedFile
							.getString(delimitedFile.findColumn("SubsType")));
					pstmtUpdate_SMS.setString(7, delimitedFile
							.getString(delimitedFile.findColumn("PO_CODE")));
					pstmtUpdate_SMS.setString(8, delimitedFile.getString(
							delimitedFile.findColumn("CallStaTime")).substring(
							0, 8));
					pstmtUpdate_SMS
							.setInt(9, Integer.parseInt(delimitedFile
									.getString(delimitedFile
											.findColumn("CallingCen"))));
					pstmtUpdate_SMS.setInt(10, Integer.parseInt(delimitedFile
							.getString(delimitedFile.findColumn("CalledCen"))));
					if (pstmtUpdate_SMS.executeUpdate() == 0) {
						pstmtInsert_SMS.setInt(1, pSwitchID);
						pstmtInsert_SMS.setString(2,
								delimitedFile.getString(delimitedFile
										.findColumn("CallType")));
						pstmtInsert_SMS.setString(3,
								delimitedFile.getString(delimitedFile
										.findColumn("SubsType")));
						pstmtInsert_SMS
								.setString(4, delimitedFile
										.getString(delimitedFile
												.findColumn("PO_CODE")));
						pstmtInsert_SMS.setString(5, delimitedFile.getString(
								delimitedFile.findColumn("CallStaTime"))
								.substring(0, 8));
						pstmtInsert_SMS.setInt(6, 1);
						pstmtInsert_SMS.setDouble(7, Double
								.parseDouble(delimitedFile
										.getString(delimitedFile
												.findColumn("TaxAir")))
								+ Double.parseDouble(delimitedFile
										.getString(delimitedFile
												.findColumn("TaxSer"))));
						pstmtInsert_SMS.setDouble(8, Double
								.parseDouble(delimitedFile
										.getString(delimitedFile
												.findColumn("TaxIdd"))));
						pstmtInsert_SMS.setInt(9, Integer
								.parseInt(delimitedFile.getString(delimitedFile
										.findColumn("CallingCen"))));
						pstmtInsert_SMS.setInt(10, Integer
								.parseInt(delimitedFile.getString(delimitedFile
										.findColumn("CalledCen"))));
						pstmtInsert_SMS.executeUpdate();
					}
				}
			} // end loop rs
		} catch (FileNotFoundException e) {
			writeLogFile(" - " + e.toString());
		} catch (Exception e) {
			pConnection.rollback();
			throw e;
		} finally {
			try {
				mSQL = null;
				delimitedFile.closeDelimitedFile();
				pstmtInsert_CALL.close();
				pstmtInsert_CALL = null;
				pstmtUpdate_CALL.close();
				pstmtUpdate_CALL = null;
				pstmtInsert_SMS.close();
				pstmtInsert_SMS = null;
				pstmtUpdate_SMS.close();
				pstmtUpdate_SMS = null;
			} catch (Exception e) {
			}
		}
		return 0;
	}

	public int CollectiveIN(java.sql.Connection pConnection, int pSwitchID,
			String pPathRated, String pFileName) throws Exception {
		String mSQL = null;
		PreparedStatement pstmtInsert_CALL = null;
		PreparedStatement pstmtUpdate_CALL = null;
		PreparedStatement pstmtInsert_SMS = null;
		PreparedStatement pstmtUpdate_SMS = null;
		int[] miDelimitedFields;
		DelimitedFile delimitedFile = new DelimitedFile();
		String mHeader = "CallType;CallStaTime;CallDuration;SubsType;CallingCen;"
				+ "PO_CODE;TaxAir;TaxIDD;TaxSer;CalledCen;CollectType";

		try {
			pConnection.setAutoCommit(false);

			mSQL = "INSERT INTO collect_in_call(switch_id,call_type,"
					+ "subs_type,po_code,call_date,num_of_records,traffic_peak,"
					+ "traffic_offpeak,turnover_peak,turnover_offpeak,tax_vnd,"
					+ "tax_usd,cen_of_calling,cen_of_called) "
					+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			pstmtInsert_CALL = (OraclePreparedStatement) pConnection
					.prepareStatement(mSQL);

			mSQL = "UPDATE collect_in_call "
					+ "SET num_of_records=num_of_records+?,"
					+ "traffic_peak=traffic_peak+?,traffic_offpeak=traffic_offpeak+?,"
					+ "turnover_peak=turnover_peak+?,turnover_offpeak=turnover_offpeak+?,"
					+ "tax_vnd=tax_vnd+?,tax_usd=tax_usd+? "
					+ "WHERE switch_id=? and call_type=? and subs_type=? "
					+ "and po_code=? and call_date=? and cen_of_calling=? "
					+ "and cen_of_called=?";
			pstmtUpdate_CALL = (OraclePreparedStatement) pConnection
					.prepareStatement(mSQL);

			mSQL = "INSERT INTO collect_in_sms(switch_id,call_type,"
					+ "subs_type,po_code,call_date,num_of_records,tax_vnd,tax_usd,"
					+ "cen_of_calling,cen_of_called) VALUES(?,?,?,?,?,?,?,?,?,?)";
			pstmtInsert_SMS = (OraclePreparedStatement) pConnection
					.prepareStatement(mSQL);

			mSQL = "UPDATE collect_in_sms "
					+ "SET num_of_records=num_of_records+?,"
					+ "tax_vnd=tax_vnd+?,tax_usd=tax_usd+? "
					+ "WHERE switch_id=? and call_type=? and subs_type=? "
					+ "and po_code=? and call_date=? and cen_of_calling=? "
					+ "and cen_of_called=?";
			pstmtUpdate_SMS = (OraclePreparedStatement) pConnection
					.prepareStatement(mSQL);

			mSQL = IOUtil.FillPath(pPathRated, Global.mSeparate) + pFileName;
			delimitedFile.openDelimitedFile(mSQL, 5242880);
			Vector vtFieldValue = StringUtils.vectorFromString(mHeader, ";");
			miDelimitedFields = new int[vtFieldValue.size()];
			for (int i = 0; i < miDelimitedFields.length; i++) {
				miDelimitedFields[i] = delimitedFile
						.findColumn(((String) vtFieldValue.elementAt(i)).trim());
			}
			while (delimitedFile.next()) {
				if ((delimitedFile.getString(
						delimitedFile.findColumn("PO_CODE")).compareTo("") != 0)
						&& ((delimitedFile.getString(
								delimitedFile.findColumn("CallType"))
								.compareTo("OG") == 0) || (delimitedFile
								.getString(delimitedFile.findColumn("CallType"))
								.compareTo("IC") == 0))) {
					pstmtUpdate_CALL.setInt(1, 1);
					if (Integer.parseInt(delimitedFile.getString(delimitedFile
							.findColumn("CollectType"))) == 1) // rush_hour
					{
						pstmtUpdate_CALL.setInt(2, Integer
								.parseInt(delimitedFile.getString(delimitedFile
										.findColumn("CallDuration"))));
						pstmtUpdate_CALL.setInt(3, 0);
						pstmtUpdate_CALL.setInt(4, (int) Math.ceil(Integer
								.parseInt(delimitedFile.getString(delimitedFile
										.findColumn("CallDuration"))) / 60));
						pstmtUpdate_CALL.setInt(5, 0);
					} else {
						pstmtUpdate_CALL.setInt(2, 0);
						pstmtUpdate_CALL.setInt(3, Integer
								.parseInt(delimitedFile.getString(delimitedFile
										.findColumn("CallDuration"))));
						pstmtUpdate_CALL.setInt(4, 0);
						pstmtUpdate_CALL.setInt(5, (int) Math.ceil(Integer
								.parseInt(delimitedFile.getString(delimitedFile
										.findColumn("CallDuration"))) / 60));
					}
					pstmtUpdate_CALL.setDouble(6, Double
							.parseDouble(delimitedFile.getString(delimitedFile
									.findColumn("TaxAir")))
							+ Double.parseDouble(delimitedFile
									.getString(delimitedFile
											.findColumn("TaxSer"))));
					pstmtUpdate_CALL.setDouble(7, Double
							.parseDouble(delimitedFile.getString(delimitedFile
									.findColumn("TaxIdd"))));
					pstmtUpdate_CALL.setInt(8, pSwitchID);
					pstmtUpdate_CALL.setString(9, delimitedFile
							.getString(delimitedFile.findColumn("CallType")));
					pstmtUpdate_CALL.setString(10, delimitedFile
							.getString(delimitedFile.findColumn("SubsType")));
					pstmtUpdate_CALL.setString(11, delimitedFile
							.getString(delimitedFile.findColumn("PO_CODE")));
					pstmtUpdate_CALL.setString(12, delimitedFile.getString(
							delimitedFile.findColumn("CallStaTime")).substring(
							0, 8));
					pstmtUpdate_CALL
							.setInt(13, Integer.parseInt(delimitedFile
									.getString(delimitedFile
											.findColumn("CallingCen"))));
					pstmtUpdate_CALL.setInt(14, Integer.parseInt(delimitedFile
							.getString(delimitedFile.findColumn("CalledCen"))));
					if (pstmtUpdate_CALL.executeUpdate() == 0) {
						pstmtInsert_CALL.setInt(1, pSwitchID);
						pstmtInsert_CALL.setString(2,
								delimitedFile.getString(delimitedFile
										.findColumn("CallType")));
						pstmtInsert_CALL.setString(3,
								delimitedFile.getString(delimitedFile
										.findColumn("SubsType")));
						pstmtInsert_CALL
								.setString(4, delimitedFile
										.getString(delimitedFile
												.findColumn("PO_CODE")));
						pstmtInsert_CALL.setString(5, delimitedFile.getString(
								delimitedFile.findColumn("CallStaTime"))
								.substring(0, 8));
						pstmtInsert_CALL.setInt(6, 1);
						if (Integer.parseInt(delimitedFile
								.getString(delimitedFile
										.findColumn("CollectType"))) == 1) // rush_hour
						{
							pstmtInsert_CALL
									.setInt(
											7,
											Integer
													.parseInt(delimitedFile
															.getString(delimitedFile
																	.findColumn("CallDuration"))));
							pstmtInsert_CALL.setInt(8, 0);
							pstmtInsert_CALL
									.setInt(
											9,
											(int) Math
													.ceil(Integer
															.parseInt(delimitedFile
																	.getString(delimitedFile
																			.findColumn("CallDuration"))) / 60));
							pstmtInsert_CALL.setInt(10, 0);
						} else {
							pstmtInsert_CALL.setInt(7, 0);
							pstmtInsert_CALL
									.setInt(
											8,
											Integer
													.parseInt(delimitedFile
															.getString(delimitedFile
																	.findColumn("CallDuration"))));
							pstmtInsert_CALL.setInt(9, 0);
							pstmtInsert_CALL
									.setInt(
											10,
											(int) Math
													.ceil(Integer
															.parseInt(delimitedFile
																	.getString(delimitedFile
																			.findColumn("CallDuration"))) / 60));
						}
						pstmtInsert_CALL.setDouble(11, Double
								.parseDouble(delimitedFile
										.getString(delimitedFile
												.findColumn("TaxAir")))
								+ Double.parseDouble(delimitedFile
										.getString(delimitedFile
												.findColumn("TaxSer"))));
						pstmtInsert_CALL.setDouble(12, Double
								.parseDouble(delimitedFile
										.getString(delimitedFile
												.findColumn("TaxIdd"))));
						pstmtInsert_CALL.setInt(13, Integer
								.parseInt(delimitedFile.getString(delimitedFile
										.findColumn("CallingCen"))));
						pstmtInsert_CALL.setInt(14, Integer
								.parseInt(delimitedFile.getString(delimitedFile
										.findColumn("CalledCen"))));
						pstmtInsert_CALL.executeUpdate();
					}
				} // end if call type
				else if ((delimitedFile.getString(
						delimitedFile.findColumn("PO_CODE")).compareTo("") != 0)
						&& ((delimitedFile.getString(
								delimitedFile.findColumn("CallType"))
								.compareTo("SMO") == 0) || (delimitedFile
								.getString(delimitedFile.findColumn("CallType"))
								.compareTo("SMT") == 0))) {
					pstmtUpdate_SMS.setInt(1, 1);
					pstmtUpdate_SMS.setDouble(2, Double
							.parseDouble(delimitedFile.getString(delimitedFile
									.findColumn("TaxAir")))
							+ Double.parseDouble(delimitedFile
									.getString(delimitedFile
											.findColumn("TaxSer"))));
					pstmtUpdate_SMS.setDouble(3, Double
							.parseDouble(delimitedFile.getString(delimitedFile
									.findColumn("TaxIdd"))));
					pstmtUpdate_SMS.setInt(4, pSwitchID);
					pstmtUpdate_SMS.setString(5, delimitedFile
							.getString(delimitedFile.findColumn("CallType")));
					pstmtUpdate_SMS.setString(6, delimitedFile
							.getString(delimitedFile.findColumn("SubsType")));
					pstmtUpdate_SMS.setString(7, delimitedFile
							.getString(delimitedFile.findColumn("PO_CODE")));
					pstmtUpdate_SMS.setString(8, delimitedFile.getString(
							delimitedFile.findColumn("CallStaTime")).substring(
							0, 8));
					pstmtUpdate_SMS
							.setInt(9, Integer.parseInt(delimitedFile
									.getString(delimitedFile
											.findColumn("CallingCen"))));
					pstmtUpdate_SMS.setInt(10, Integer.parseInt(delimitedFile
							.getString(delimitedFile.findColumn("CalledCen"))));
					if (pstmtUpdate_SMS.executeUpdate() == 0) {
						pstmtInsert_SMS.setInt(1, pSwitchID);
						pstmtInsert_SMS.setString(2,
								delimitedFile.getString(delimitedFile
										.findColumn("CallType")));
						pstmtInsert_SMS.setString(3,
								delimitedFile.getString(delimitedFile
										.findColumn("SubsType")));
						pstmtInsert_SMS
								.setString(4, delimitedFile
										.getString(delimitedFile
												.findColumn("PO_CODE")));
						pstmtInsert_SMS.setString(5, delimitedFile.getString(
								delimitedFile.findColumn("CallStaTime"))
								.substring(0, 8));
						pstmtInsert_SMS.setInt(6, 1);
						pstmtInsert_SMS.setDouble(7, Double
								.parseDouble(delimitedFile
										.getString(delimitedFile
												.findColumn("TaxAir")))
								+ Double.parseDouble(delimitedFile
										.getString(delimitedFile
												.findColumn("TaxSer"))));
						pstmtInsert_SMS.setDouble(8, Double
								.parseDouble(delimitedFile
										.getString(delimitedFile
												.findColumn("TaxIdd"))));
						pstmtInsert_SMS.setInt(9, Integer
								.parseInt(delimitedFile.getString(delimitedFile
										.findColumn("CallingCen"))));
						pstmtInsert_SMS.setInt(10, Integer
								.parseInt(delimitedFile.getString(delimitedFile
										.findColumn("CalledCen"))));
						pstmtInsert_SMS.executeUpdate();
					}
				}
			} // end loop rs
		} catch (FileNotFoundException e) {
			writeLogFile(" - " + e.toString());
		} catch (Exception e) {
			pConnection.rollback();
			throw e;
		} finally {
			try {
				mSQL = null;
				delimitedFile.closeDelimitedFile();
				pstmtInsert_CALL.close();
				pstmtInsert_CALL = null;
				pstmtUpdate_CALL.close();
				pstmtUpdate_CALL = null;
				pstmtInsert_SMS.close();
				pstmtInsert_SMS = null;
				pstmtUpdate_SMS.close();
				pstmtUpdate_SMS = null;
			} catch (Exception e) {
			}
		}
		return 0;
	}

	public int CollectiveSwitchCell(java.sql.Connection pConnection,
			int pSwitchID, String pPathRated, String pFileName)
			throws Exception {
		String mSQL = null;
		PreparedStatement pstmtInsert_CELL = null;
		PreparedStatement pstmtUpdate_CELL = null;
		int[] miDelimitedFields;
		DelimitedFile delimitedFile = new DelimitedFile();
		String mHeader = "RecType;CallType;CallingISDN;IMSI;CallStaTime;"
				+ "CallDuration;CallEndTime;CalledISDN;CellID;"
				+ "ServiceCenter;IcRoute;OgRoute;TarClass;"
				+ "ReqTel;ReqBeare;INSer;CharInd;CallOrgISDN;"
				+ "TransISDN;RecSeq;SubsType;CallingCen;IMEI;"
				+ "PO_CODE;TaxAir;TaxIDD;TaxSer;CalledCen;CollectType";
		try {
			pConnection.setAutoCommit(false);

			mSQL = "INSERT INTO collect_switch_cell(switch_id,cell_id,"
					+ "call_type,call_date,num_of_records,traffic_peak,"
					+ "traffic_offpeak,turnover_peak,turnover_offpeak) "
					+ "VALUES(?,?,?,?,?,?,?,?,?)";
			pstmtInsert_CELL = (OraclePreparedStatement) pConnection
					.prepareStatement(mSQL);

			mSQL = "UPDATE collect_switch_cell "
					+ "SET num_of_records=num_of_records+?,"
					+ "traffic_peak=traffic_peak+?,traffic_offpeak=traffic_offpeak+?,"
					+ "turnover_peak=turnover_peak+?,turnover_offpeak=turnover_offpeak+? "
					+ "WHERE switch_id=? and cell_id=? and call_type=? and call_date=? ";
			pstmtUpdate_CELL = (OraclePreparedStatement) pConnection
					.prepareStatement(mSQL);

			mSQL = IOUtil.FillPath(pPathRated, Global.mSeparate) + pFileName;
			delimitedFile.openDelimitedFile(mSQL, 5242880);
			Vector vtFieldValue = StringUtils.vectorFromString(mHeader, ";");
			miDelimitedFields = new int[vtFieldValue.size()];
			for (int i = 0; i < miDelimitedFields.length; i++) {
				miDelimitedFields[i] = delimitedFile
						.findColumn(((String) vtFieldValue.elementAt(i)).trim());
			}
			while (delimitedFile.next()) {
				if ((delimitedFile.getString(
						delimitedFile.findColumn("CallType")).compareTo("DV") != 0)
						&& (delimitedFile.getString(
								delimitedFile.findColumn("CellID")).compareTo(
								"") != 0)
						&& (delimitedFile.getString(
								delimitedFile.findColumn("CollectType"))
								.compareTo("") != 0)) {
					pstmtUpdate_CELL.setInt(1, 1);
					if (Integer.parseInt(delimitedFile.getString(delimitedFile
							.findColumn("CollectType"))) == 1) // rush_hour
					{
						pstmtUpdate_CELL.setInt(2, Integer
								.parseInt(delimitedFile.getString(delimitedFile
										.findColumn("CallDuration"))));
						pstmtUpdate_CELL.setInt(3, 0);
						pstmtUpdate_CELL.setInt(4, (int) Math.ceil(Integer
								.parseInt(delimitedFile.getString(delimitedFile
										.findColumn("CallDuration"))) / 60));
						pstmtUpdate_CELL.setInt(5, 0);
					} else {
						pstmtUpdate_CELL.setInt(2, 0);
						pstmtUpdate_CELL.setInt(3, Integer
								.parseInt(delimitedFile.getString(delimitedFile
										.findColumn("CallDuration"))));
						pstmtUpdate_CELL.setInt(4, 0);
						pstmtUpdate_CELL.setInt(5, (int) Math.ceil(Integer
								.parseInt(delimitedFile.getString(delimitedFile
										.findColumn("CallDuration"))) / 60));
					}
					pstmtUpdate_CELL.setInt(6, pSwitchID);
					pstmtUpdate_CELL.setString(7, delimitedFile
							.getString(delimitedFile.findColumn("CellID")));
					if (delimitedFile.getString(
							delimitedFile.findColumn("CallType")).compareTo(
							"DV") == 0)
						pstmtUpdate_CELL.setString(8, "OG");
					else
						pstmtUpdate_CELL.setString(8,
								delimitedFile.getString(delimitedFile
										.findColumn("CallType")));
					pstmtUpdate_CELL.setString(9, delimitedFile.getString(
							delimitedFile.findColumn("CallStaTime")).substring(
							0, 8));
					if (pstmtUpdate_CELL.executeUpdate() == 0) {
						pstmtInsert_CELL.setInt(1, pSwitchID);
						pstmtInsert_CELL.setString(2, delimitedFile
								.getString(delimitedFile.findColumn("CellID")));
						if (delimitedFile.getString(
								delimitedFile.findColumn("CallType"))
								.compareTo("DV") == 0)
							pstmtInsert_CELL.setString(3, "OG");
						else
							pstmtInsert_CELL.setString(3, delimitedFile
									.getString(delimitedFile
											.findColumn("CallType")));
						pstmtInsert_CELL.setString(4, delimitedFile.getString(
								delimitedFile.findColumn("CallStaTime"))
								.substring(0, 8));

						pstmtInsert_CELL.setInt(5, 1);
						if (Integer.parseInt(delimitedFile
								.getString(delimitedFile
										.findColumn("CollectType"))) == 1) // rush_hour
						{
							pstmtInsert_CELL
									.setInt(
											6,
											Integer
													.parseInt(delimitedFile
															.getString(delimitedFile
																	.findColumn("CallDuration"))));
							pstmtInsert_CELL.setInt(7, 0);
							pstmtInsert_CELL
									.setInt(
											8,
											(int) Math
													.ceil(Integer
															.parseInt(delimitedFile
																	.getString(delimitedFile
																			.findColumn("CallDuration"))) / 60));
							pstmtInsert_CELL.setInt(9, 0);
						} else {
							pstmtInsert_CELL.setInt(6, 0);
							pstmtInsert_CELL
									.setInt(
											7,
											Integer
													.parseInt(delimitedFile
															.getString(delimitedFile
																	.findColumn("CallDuration"))));
							pstmtInsert_CELL.setInt(8, 0);
							pstmtInsert_CELL
									.setInt(
											9,
											(int) Math
													.ceil(Integer
															.parseInt(delimitedFile
																	.getString(delimitedFile
																			.findColumn("CallDuration"))) / 60));
						}
						pstmtInsert_CELL.executeUpdate();
					}
				} // end if call type
			} // end loop rs
		} catch (FileNotFoundException e) {
			writeLogFile(" - " + e.toString());
		} catch (Exception e) {
			pConnection.rollback();
			throw e;
		} finally {
			try {
				mSQL = null;
				delimitedFile.closeDelimitedFile();
				pstmtInsert_CELL.close();
				pstmtInsert_CELL = null;
				pstmtUpdate_CELL.close();
				pstmtUpdate_CELL = null;
			} catch (Exception e) {
			}
		}
		return 0;
	}
}
