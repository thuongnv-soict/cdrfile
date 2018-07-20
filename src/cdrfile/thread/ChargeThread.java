package cdrfile.thread;

/**
 * <p>Title: CDR File(s) System</p>
 * <p>Description: VMS IS Departerment</p>
 * <p>Copyright: Copyright (c)  by eKnowledge 2004</p>
 * <p>Company: VietNam Mobile Telecom Services</p>
 * @author eKnowledge - Software
 * @version 1.0
 */

import java.sql.ResultSet;
import java.sql.Statement;

import cdrfile.charge.ChargeObject;
import cdrfile.global.Global;
import cdrfile.global.IOUtils;
import cdrfile.global.cdrfileParam;

public class ChargeThread extends ThreadInfo {

    public void finalize() {
        destroy();
        System.runFinalization();
        System.gc();
    }

    protected void processSession() throws Exception {
        int mRet = 0;
        boolean mLoadTariff = false;
        ChargeObject charge = null;
        IOUtils IOUtil = new IOUtils();
        Statement stmt = mConnection.createStatement();
        ResultSet rs = stmt
                       .executeQuery(
                               "SELECT b.file_id,a.file_type,a.id,a.center_id,"
                               +
                               " b.file_name,b.rec_total,a.convert_dir, a.charge_dir,"
                               + " a.collect_dir,a.note,b.current_dir,"
                               + " a.local_split_file_by_day "
                               +
                               " FROM data_param a, import_header b,node_cluster c "
                               + " WHERE b.status="
                               + Global.StateConverted
                               + " AND a.charge_thread_id ="
                               + getThreadID()
                               + " AND a.id=b.ftp_id AND a.run_on_node=c.id "
                               + " AND c.ip='"
                               + Global.getLocalSvrIP()
                               + "' ORDER BY b.current_dir,b.file_id");
        try {
            Global
                    .ExecuteSQL(mConnection,
                                "alter session set nls_date_format='yyyy/MM/dd hh24:mi:ss'");

            // TEST thu tinh cuoc
            /*
             * boolean blnFound = false; charge = new ChargeObject(mConnection);
             * blnFound = charge.chargeTESTCASE("OG", "%" + "907949136",
             * "08115", "20061003211824", Long.parseLong("20061003211824"), 7);
             */
            while (rs.next() && miThreadCommand != THREAD_STOP) {
                if (cdrfileParam.ChargeCDRFile) {
                    if (!mLoadTariff) {
                        charge = new ChargeObject(mConnection);
                        charge.setThreadID(getThreadID());
                        charge.setThreadName(getThreadName());
                        charge.setLogPathFileName(getLogPathFileName());
                        charge.LoadIMSI(mConnection);
                        mLoadTariff = true;
                    }
                    if (rs.getInt("local_split_file_by_day") == 1) {
                        writeLogFile(" - Charging file "
                                     + rs.getString("current_dir")
                                     + Global.mSeparate +
                                     rs.getString("file_name")
                                     + " - " + rs.getInt("file_id") + " from "
                                     + rs.getString("note"));
                        IOUtil.forceFolderExist(IOUtil.FillPath(rs
                                .getString("collect_dir"), Global.mSeparate)
                                                + rs.getString("current_dir"));
                        IOUtil.forceFolderExist(IOUtil.FillPath(rs
                                .getString("charge_dir"), Global.mSeparate)
                                                + rs.getString("current_dir"));
                        if ((rs.getString("file_type").compareTo("IN_PPS_V421") ==
                             0) ||
                            (rs.getString("file_type").compareTo("IN_PPS_V331") ==
                             0)) {
                            mRet = charge.chargeINFile(mConnection, rs
                                    .getInt("file_id"), IOUtil.FillPath(rs
                                    .getString("convert_dir"), Global.mSeparate)
                                    + rs.getString("current_dir"),
                                    IOUtil.FillPath(
                                            rs.getString("charge_dir"),
                                            Global.mSeparate)
                                    + rs.getString("current_dir"), rs
                                    .getString("file_name"),
                                    rs.getInt("rec_total"), IOUtil.FillPath(rs
                                    .getString("collect_dir"),
                                    Global.mSeparate)
                                    + rs.getString("current_dir"), rs
                                    .getString("center_id"));
                        } else {
                            mRet = charge.chargeMSCFile(mConnection, rs
                                    .getInt("file_id"), IOUtil.FillPath(rs
                                    .getString("convert_dir"), Global.mSeparate)
                                    + rs.getString("current_dir"),
                                    IOUtil.FillPath(
                                            rs.getString("charge_dir"),
                                            Global.mSeparate)
                                    + rs.getString("current_dir"), rs
                                    .getString("file_name"),
                                    rs.getInt("rec_total"), IOUtil.FillPath(rs
                                    .getString("collect_dir"),
                                    Global.mSeparate)
                                    + rs.getString("current_dir"), rs
                                    .getString("center_id"));
                        }

                    } else {
                        writeLogFile(" - Charging file "
                                     + rs.getString("file_name") + " - "
                                     + rs.getInt("file_id") + " from "
                                     + rs.getString("note"));
                        if ((rs.getString("file_type").compareTo("IN_PPS_V421") ==
                             0) ||
                            (rs.getString("file_type").compareTo("IN_PPS_V331") ==
                             0)) {
                            mRet = charge.chargeINFile(mConnection, rs
                                    .getInt("file_id"),
                                    rs.getString("convert_dir"), rs
                                    .getString("charge_dir"), rs
                                    .getString("file_name"), rs
                                    .getInt("rec_total"), rs
                                    .getString("collect_dir"), rs
                                    .getString("center_id"));
                        } else {
                            mRet = charge.chargeMSCFile(mConnection, rs
                                    .getInt("file_id"),
                                    rs.getString("convert_dir"), rs
                                    .getString("charge_dir"), rs
                                    .getString("file_name"), rs
                                    .getInt("rec_total"), rs
                                    .getString("collect_dir"), rs
                                    .getString("center_id"));
                        }

                    }
                    switch (mRet) {
                    case 0:
                        if (rs.getInt("local_split_file_by_day") == 1) {
                            IOUtil.deleteFile(IOUtil
                                              .FillPath(rs.getString(
                                    "convert_dir"),
                                    Global.mSeparate)
                                              + rs.getString("current_dir")
                                              + Global.mSeparate
                                              + rs.getString("file_name"));
                        } else {
                            IOUtil.deleteFile(IOUtil
                                              .FillPath(rs.getString(
                                    "convert_dir"),
                                    Global.mSeparate)
                                              + rs.getString("file_name"));
                        }
                        writeLogFile(" - File " + rs.getString("file_name")
                                     + " - " + rs.getInt("file_id")
                                     + " charged successfull.");
                        break;
                    case 1:
                        Global.writeEventThreadErr(Integer
                                .parseInt(getThreadID()), 2,
                                " - Please check again tariff table and data in file:"
                                + rs.getString("file_name") + " - "
                                + rs.getInt("file_id"));
                        writeLogFile(
                                " - Please check again tariff table and data in file:"
                                + rs.getString("file_name")
                                + " - "
                                + rs.getInt("file_id"));
                        break;
                    case 3:
                        break;
                    default:
                        Global.writeEventThreadErr(Integer
                                .parseInt(getThreadID()), 1, " - Charged file "
                                + rs.getString("file_name") + " - "
                                + rs.getInt("file_id")
                                + " without ERROR knowledge");
                        writeLogFile(" - Charged file "
                                     + rs.getString("file_name") + " - "
                                     + rs.getInt("file_id")
                                     + " without ERROR knowledge");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Global.writeEventThreadErr(Integer.parseInt(getThreadID()), 2, e
                                       .toString());
            writeLogFile(e.toString());
        } finally {
            try {
                rs.close();
                rs = null;
                stmt.close();
                stmt = null;
                IOUtil = null;
            } catch (Exception e) {
            }
        }
    }
}
