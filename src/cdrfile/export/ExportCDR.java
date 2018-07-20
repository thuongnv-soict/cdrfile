package cdrfile.export;

/**
 * <p>Title: CDR File(s) System</p>
 * <p>Description: VMS IS Departerment</p>
 * <p>Copyright: Copyright (c)  by eKnowledge 2004</p>
 * <p>Company: VietNam Mobile Telecom Services</p>
 * @author eKnowledge - Software
 * @version 1.0
 */

import java.io.File;
import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;

import cdrfile.global.DelimitedFile;
import cdrfile.global.Global;
import cdrfile.global.IOUtils;
import cdrfile.global.StringUtils;
import cdrfile.global.TextFile;
import cdrfile.global.cdrfileParam;
import cdrfile.zip.SmartZip;

public class ExportCDR extends Global
{
    protected DelimitedFile delimitedFile = new DelimitedFile();
    IOUtils IOUtil = new IOUtils();
    SmartZip zip = new SmartZip();
    public int exportSwitchCDR(String pPathRated, String pFileName,
        String pPathExport, String pFileCenterCode, int pZipExported,
        String pDateCreateFile) throws Exception
    {
        File file = null;
        int mRecN = 0;
        String mText = null;
        int[] miDelimitedFields;
        String mHeader = "RecType;CallType;CallingISDN;IMSI;CallStaTime;"
            + "CallDuration;CallEndTime;CalledISDN;CellID;"
            + "ServiceCenter;IcRoute;OgRoute;TarClass;"
            + "ReqTel;ReqBeare;INSer;CharInd;CallOrgISDN;"
            + "TransISDN;RecSeq;IMEI;CallingOrg;CalledOrg;"
            + "PO_CODE;TaxAir;TaxIDD;TaxSer;CalledCen;"
            + "CollectType;SubsType;CallingCen";
        TextFile fileExp = new TextFile();
        // Initialize
        fileExp.openFile(pPathExport, 5242880);
        mText = " STT " + "|" + "FC" + "|" + Global.lpad("call type", 9, " ")
            + "|" + Global.lpad("     po code", 18, " ") + "|"
            + Global.rpad("tax airtime", 11, " ") + "|"
            + Global.rpad("tax idd", 7, " ") + "|"
            + Global.rpad("tax service", 11, " ") + "|"
            + Global.lpad("    calling isdn", 20, " ") + "|"
            + Global.lpad("      imsi", 16, " ") + "|"
            + Global.lpad("   call sta time", 19, " ") + "|"
            + Global.rpad("duration", 8, " ") + "|"
            + Global.lpad("  call end time", 19, " ") + "|"
            + Global.lpad("   called isdn", 20, " ") + "|"
            + Global.lpad("    cell id", 16, " ") + "|"
            + Global.lpad("  service center", 18, " ") + "|"
            + Global.lpad(" ic route", 10, " ") + "|"
            + Global.lpad(" og route", 10, " ") + "|"
            + Global.lpad("tar class", 9, " ") + "|"
            + Global.lpad("ts code", 7, " ") + "|"
            + Global.lpad("bs code", 9, " ") + "|"
            + Global.lpad("in mark", 7, " ") + "|"
            + Global.lpad("char indi", 9, " ") + "|"
            + Global.lpad("   org call id", 18, " ") + "|"
            + Global.lpad("rec seq num", 11, " ") + "|"
            + Global.lpad("   translate num", 20, " ") + "|"
            + Global.lpad("    calling imei", 20, " ") + "|"
            + Global.lpad("    calling org", 22, " ") + "|"
            + Global.lpad("     called org", 22, " ");
        fileExp.addText(mText);
        mText = Global.lpad("-", 5, "-") + "|" + "--" + "|"
            + Global.lpad("-", 9, "-") + "|" + Global.lpad("-", 18, "-")
            + "|" + Global.rpad("-", 11, "-") + "|"
            + Global.rpad("-", 7, "-") + "|" + Global.rpad("-", 11, "-")
            + "|" + Global.lpad("-", 20, "-") + "|"
            + Global.lpad("-", 16, "-") + "|" + Global.lpad("-", 19, "-")
            + "|" + Global.rpad("-", 8, "-") + "|"
            + Global.lpad("-", 19, "-") + "|" + Global.lpad("-", 20, "-")
            + "|" + Global.lpad("-", 16, "-") + "|"
            + Global.lpad("-", 18, "-") + "|" + Global.lpad("-", 10, "-")
            + "|" + Global.lpad("-", 10, "-") + "|"
            + Global.lpad("-", 9, "-") + "|" + Global.lpad("-", 7, "-")
            + "|" + Global.lpad("-", 9, "-") + "|"
            + Global.lpad("-", 7, "-") + "|" + Global.lpad("-", 9, "-")
            + "|" + Global.lpad("-", 18, "-") + "|"
            + Global.lpad("-", 11, "-") + "|" + Global.lpad("-", 20, "-")
            + "|" + Global.lpad("-", 20, "-") + "|"
            + Global.lpad("-", 22, "-") + "|" + Global.lpad("-", 22, "-");
        fileExp.addText(mText);
        try
        {
            mText = IOUtil.FillPath(pPathRated, Global.mSeparate) + pFileName;
            delimitedFile.openDelimitedFile(mText, 5242880);
            Vector vtFieldValue = StringUtils.vectorFromString(mHeader, ";");
            miDelimitedFields = new int[vtFieldValue.size()];
            for (int i = 0; i < miDelimitedFields.length; i++)
            {
                miDelimitedFields[i] = delimitedFile
                    .findColumn(((String) vtFieldValue.elementAt(i)).trim());
            }
            while (delimitedFile.next())
            {
                mRecN++;
                mText = Global.lpad(String.valueOf(mRecN), 5, " ")
                    + "|"
                    + Global.rpad(pFileCenterCode, 2, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("CallType")), 9, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("PO_CODE")), 18, " ")
                    + "|"
                    + Global.rpad(delimitedFile.getString(delimitedFile
                    .findColumn("TaxAir")), 11, " ")
                    + "|"
                    + Global.rpad(delimitedFile.getString(delimitedFile
                    .findColumn("TaxIdd")), 7, " ")
                    + "|"
                    + Global.rpad(delimitedFile.getString(delimitedFile
                    .findColumn("TaxSer")), 11, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("CallingISDN")), 20, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("IMSI")), 16, " ")
                    + "|"
                    + Global
                    .lpad(
                    delimitedFile
                    .getString(
                    delimitedFile
                    .findColumn("CallStaTime"))
                    .substring(6, 8)
                    + "/"
                    + delimitedFile
                    .getString(
                    delimitedFile
                    .findColumn("CallStaTime"))
                    .substring(4, 6)
                    + "/"
                    + delimitedFile
                    .getString(
                    delimitedFile
                    .findColumn("CallStaTime"))
                    .substring(0, 4)
                    + " "
                    + delimitedFile
                    .getString(
                    delimitedFile
                    .findColumn("CallStaTime"))
                    .substring(8, 10)
                    + ":"
                    + delimitedFile
                    .getString(
                    delimitedFile
                    .findColumn("CallStaTime"))
                    .substring(10, 12)
                    + ":"
                    + delimitedFile
                    .getString(
                    delimitedFile
                    .findColumn("CallStaTime"))
                    .substring(12, 14), 19,
                    " ")
                    + "|"
                    + Global.rpad(delimitedFile.getString(delimitedFile
                    .findColumn("CallDuration")), 8, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("CallEndTime")), 19, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("CalledISDN")), 20, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("CellID")), 16, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("ServiceCenter")), 18, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("IcRoute")), 10, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("OgRoute")), 10, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("TarClass")), 9, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("ReqTel")), 7, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("ReqBeare")), 9, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("INSer")), 7, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("CharInd")), 9, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("CallOrgISDN")), 18, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("RecSeq")), 11, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("TransISDN")), 20, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("IMEI")), 20, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("CallingOrg")), 22, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("CalledOrg")), 22, " ");
                fileExp.addText(mText);
            }
            fileExp.closeFile();

            if (pZipExported == 1)
            {
                zip.Zip(pPathExport, pPathExport + ".zip", false);
                file = new File(pPathExport + ".zip");
                file.setLastModified(Global
                    .convertDateTimeToLong(pDateCreateFile));
            }
            else
            {
                file = new File(pPathExport);
                file.setLastModified(Global
                    .convertDateTimeToLong(pDateCreateFile));
            }
        }
        catch (Exception e)
        {
            System.err.println(Global.Format(new java.util.Date(),
                "dd/MM/yyyy HH:mm:ss")
                + " : ERROR in module exportSwitchCDR " + e.toString());
            if (cdrfileParam.OnErrorResumeNext.compareTo("TRUE") == 0)
            {
                return Global.ErrFileConverted;
            }
            else
            {
                throw e;
            }
        }
        finally
        {
            try
            {
                delimitedFile.closeDelimitedFile();
            }
            catch (Exception e)
            {
            }
        }
        return mRecN;
    }

    public int reExportSwitchCDR(Connection pConnection, String pPathExport,
        int pFileID) throws Exception
    {
        int mRecN = 0;
        int mRecTotal = 0;
        String mText = null;
        String mSQL;
        String mFileName;
        mSQL = "SELECT DISTINCT c.file_type,b.ftp_id,"
            + "nvl(b.re_rate,0) re_rate,b.file_name " + "FROM tmp"
            + pFileID + " a,import_header b, data_param c "
            + "WHERE a.file_id=b.file_id and b.ftp_id=c.id";
        Statement stmtFile = pConnection.createStatement();
        ResultSet rsFile = stmtFile.executeQuery(mSQL);

        mSQL = "SELECT rownum order_rec,center_id file_center_code,call_type,"
            + "nvl(po_code,' ') po_code, nvl(tax_airtime,0) tax_airtime,"
            + "nvl(tax_idd,0) tax_idd, nvl(tax_service,0) tax_service,"
            + "nvl(calling_isdn,' ') calling_isdn,"
            + "nvl(calling_imsi,' ') calling_imsi,"
            + "call_sta_time, nvl(duration,0) duration,"
            + "nvl(call_end_time,' ') call_end_time,"
            + "nvl(called_isdn,' ') called_isdn,"
            + "nvl(cell_id,' ') cell_id,nvl(service_center,' ') service_center,"
            + "nvl(ic_route,' ') ic_route,nvl(og_route,' ') og_route,"
            + "nvl(tariff_class,' ') tariff_class,nvl(ts_code,' ') ts_code,"
            + "nvl(bs_code,' ') bs_code, nvl(in_mark,0) in_mark,"
            + "nvl(charging_indicator,' ') charging_indicator,"
            + "nvl(org_call_id,' ') org_call_id,"
            + "nvl(rec_seq_number,0) rec_seq_number from " + Global.OwnerDB
            + ".tmp" + pFileID + ", data_param where switch_id=id";
        Statement stmt = pConnection.createStatement();
        ResultSet rs = null;
        RandomAccessFile fileExp = null;

        try
        {
            while (rsFile.next())
            {
                // Initialize
                mRecN = 0;
                mFileName = rsFile.getString("file_name") + ".TXT."
                    + rsFile.getInt("re_rate") + 1;
                pPathExport += mFileName;

                fileExp = new RandomAccessFile(pPathExport, "rw");
                fileExp.seek(fileExp.length());
                if (rsFile.getString("file_type").compareTo("IN_PPS_V331") == 0)
                {
                    mText = " STT " + "|" + "FC" + "|"
                        + Global.lpad("call type", 9, " ") + "|"
                        + Global.lpad("     po code", 18, " ") + "|"
                        + Global.rpad("tax airtime", 11, " ") + "|"
                        + Global.rpad("tax idd", 7, " ") + "|"
                        + Global.rpad("tax service", 11, " ") + "|"
                        + Global.lpad("   calling isdn", 20, " ") + "|"
                        + Global.lpad("      imsi", 16, " ") + "|"
                        + Global.lpad("  call sta time", 19, " ") + "|"
                        + Global.rpad("duration", 8, " ") + "|"
                        + Global.lpad("  call end time", 18, " ") + "|"
                        + Global.lpad("   called isdn", 20, " ") + "|"
                        + Global.lpad("    location", 16, " ") + "|"
                        + Global.lpad("  remain credit", 18, " ") + "|"
                        + Global.lpad(" call cost", 10, " ") + "|"
                        + Global.lpad("dis credit", 10, " ") + "|"
                        + Global.lpad("tar class", 9, " ") + "|"
                        + Global.lpad("ts code", 7, " ") + "|"
                        + Global.lpad("nw result", 9, " ") + "|"
                        + Global.lpad("in serv", 7, " ") + "|"
                        + Global.lpad("char indi", 9, " ") + "|"
                        + Global.lpad("   org call id", 18, " ") + "|"
                        + Global.lpad("rec seq num", 11, " ");
                }
                else
                {
                    mText = " STT " + "|" + "FC" + "|"
                        + Global.lpad("call type", 9, " ") + "|"
                        + Global.lpad("     po code", 18, " ") + "|"
                        + Global.rpad("tax airtime", 11, " ") + "|"
                        + Global.rpad("tax idd", 7, " ") + "|"
                        + Global.rpad("tax service", 11, " ") + "|"
                        + Global.lpad("   calling isdn", 20, " ") + "|"
                        + Global.lpad("      imsi", 16, " ") + "|"
                        + Global.lpad("  call sta time", 19, " ") + "|"
                        + Global.rpad("duration", 8, " ") + "|"
                        + Global.lpad("  call end time", 18, " ") + "|"
                        + Global.lpad("   called isdn", 20, " ") + "|"
                        + Global.lpad("    cell id", 16, " ") + "|"
                        + Global.lpad("  service center", 18, " ") + "|"
                        + Global.lpad(" ic route", 10, " ") + "|"
                        + Global.lpad(" og route", 10, " ") + "|"
                        + Global.lpad("tar class", 9, " ") + "|"
                        + Global.lpad("ts code", 7, " ") + "|"
                        + Global.lpad("bs code", 9, " ") + "|"
                        + Global.lpad("in mark", 7, " ") + "|"
                        + Global.lpad("char indi", 9, " ") + "|"
                        + Global.lpad("   org call id", 18, " ") + "|"
                        + Global.lpad("rec seq num", 11, " ");
                }
                fileExp.writeBytes(mText + "\r\n");
                mText = Global.lpad("-", 5, "-") + "|" + "--" + "|"
                    + Global.lpad("-", 9, "-") + "|"
                    + Global.lpad("-", 18, "-") + "|"
                    + Global.rpad("-", 11, "-") + "|"
                    + Global.rpad("-", 7, "-") + "|"
                    + Global.rpad("-", 11, "-") + "|"
                    + Global.lpad("-", 20, "-") + "|"
                    + Global.lpad("-", 16, "-") + "|"
                    + Global.lpad("-", 19, "-") + "|"
                    + Global.rpad("-", 8, "-") + "|"
                    + Global.lpad("-", 18, "-") + "|"
                    + Global.lpad("-", 20, "-") + "|"
                    + Global.lpad("-", 16, "-") + "|"
                    + Global.lpad("-", 18, "-") + "|"
                    + Global.lpad("-", 10, "-") + "|"
                    + Global.lpad("-", 10, "-") + "|"
                    + Global.lpad("-", 9, "-") + "|"
                    + Global.lpad("-", 7, "-") + "|"
                    + Global.lpad("-", 9, "-") + "|"
                    + Global.lpad("-", 7, "-") + "|"
                    + Global.lpad("-", 9, "-") + "|"
                    + Global.lpad("-", 18, "-") + "|"
                    + Global.lpad("-", 11, "-");
                fileExp.writeBytes(mText + "\r\n");

                rs = stmt.executeQuery(mSQL);
                try
                {
                    while (rs.next())
                    {
                        mRecN++;
                        mRecTotal++;
                        mText = Global.rpad(rs.getString("order_rec"), 5, " ")
                            + "|"
                            + Global.rpad(rs.getString("file_center_code"),
                            2, " ")
                            + "|"
                            + Global
                            .lpad(rs.getString("call_type"), 9, " ")
                            + "|"
                            + Global.lpad(rs.getString("po_code"), 18, " ")
                            + "|"
                            + Global.rpad(rs.getString("tax_airtime"), 11,
                            " ")
                            + "|"
                            + Global.rpad(rs.getString("tax_idd"), 7, " ")
                            + "|"
                            + Global.rpad(rs.getString("tax_service"), 11,
                            " ")
                            + "|"
                            + Global.lpad(rs.getString("calling_isdn"), 20,
                            " ")
                            + "|"
                            + Global.lpad(rs.getString("calling_imsi"), 16,
                            " ")
                            + "|"
                            + Global.lpad(rs.getString("call_sta_time"),
                            19, " ")
                            + "|"
                            + Global.rpad(rs.getString("duration"), 8, " ")
                            + "|"
                            + Global.lpad(rs.getString("call_end_time"),
                            18, " ")
                            + "|"
                            + Global.lpad(rs.getString("called_isdn"), 20,
                            " ")
                            + "|"
                            + Global.lpad(rs.getString("cell_id"), 16, " ")
                            + "|"
                            + Global.lpad(rs.getString("service_center"),
                            18, " ")
                            + "|"
                            + Global
                            .lpad(rs.getString("ic_route"), 10, " ")
                            + "|"
                            + Global
                            .lpad(rs.getString("og_route"), 10, " ")
                            + "|"
                            + Global.lpad(rs.getString("tariff_class"), 9,
                            " ")
                            + "|"
                            + Global.lpad(rs.getString("ts_code"), 7, " ")
                            + "|"
                            + Global.lpad(rs.getString("bs_code"), 9, " ")
                            + "|"
                            + Global.lpad(rs.getString("in_mark"), 7, " ")
                            + "|"
                            + Global.lpad(rs
                            .getString("charging_indicator"), 9,
                            " ")
                            + "|"
                            + Global.lpad(rs.getString("org_call_id"), 18,
                            " ")
                            + "|"
                            + Global.lpad(rs.getString("rec_seq_number"),
                            11, " ");
                        fileExp.writeBytes(mText + "\r\n");
                    }
                    fileExp.close();
                    fileExp = null;
                }
                catch (Exception ex)
                {
                    throw ex;
                }
                if (mRecN > 0)
                {
                    Global.ExecuteSQL(pConnection, "INSERT INTO export_header("
                        + "ftp_id,file_id,file_name,records,date_create) "
                        + "values(" + rsFile.getInt("ftp_id") + ","
                        + rsFile.getInt("file_id") + ",'" + mFileName
                        + "'," + mRecN + ",sysdate)");
                    Global.ExecuteSQL(pConnection, "UPDATE import_header "
                        + "SET re_rate=re_rate+1 " + "WHERE file_id="
                        + rsFile.getInt("file_id"));
                    writeLogFile("      .Exported data to " + mFileName
                        + " => " + mRecN + " records.");
                }
            }
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            try
            {
                mText = null;
                mSQL = null;
                rs.close();
                rs = null;
                stmt.close();
                stmt = null;
                rsFile.close();
                rsFile = null;
                stmtFile.close();
                stmtFile = null;
            }
            catch (Exception e)
            {
            }
        }
        return mRecTotal;
    }

    public int exportInCDR(String pPathRated, String pFileName,
        String pPathExport, String pFileCenterCode, int pZipExported,
        String pDateCreateFile) throws Exception
    {
        File file = null;
        // String mSQL;
        int mRecN = 0;
        String mText = null;
        int[] miDelimitedFields;
        TextFile fileExp = new TextFile();
        String mHeader = "RecType;CallType;CallingISDN;IMSI;CallStaTime;"
            + "CallDuration;CallEndTime;CalledISDN;LocatInd;"
            + "AccProfile;RemainCredit;CallCost;DisCredit;CharClass;"
            + "TelInd;NetInd;INSer;CharInd;CallOrgISDN;"
            + "TransISDN;ReFillType;RefillNum;RefillVal;"
            + "CallingOrg;CalledOrg;PO_CODE;TaxAir;TaxIDD;TaxSer;"
            + "CalledCen;CollectType;SubsType;CallingCen";

        // Initialize
        fileExp.openFile(pPathExport, 5242880);
        mText = " STT " + "|" + "FC" + "|" + Global.lpad("call type", 9, " ")
            + "|" + Global.lpad("     po code", 18, " ") + "|"
            + Global.rpad("tax airtime", 11, " ") + "|"
            + Global.rpad("tax idd", 7, " ") + "|"
            + Global.rpad("tax service", 11, " ") + "|"
            + Global.lpad("    calling isdn", 20, " ") + "|"
            + Global.lpad("      imsi", 16, " ") + "|"
            + Global.lpad("   call sta time", 19, " ") + "|"
            + Global.rpad("duration", 8, " ") + "|"
            + Global.lpad("  call end time", 19, " ") + "|"
            + Global.lpad("   called isdn", 20, " ") + "|"
            + Global.lpad("location", 8, " ") + "|"
            + Global.lpad("remain credit", 13, " ") + "|"
            + Global.lpad("call cost", 9, " ") + "|"
            + Global.lpad("dis credit", 10, " ") + "|"
            + Global.lpad("tar class", 9, " ") + "|"
            + Global.lpad("ts code", 7, " ") + "|"
            + Global.lpad("nw result", 9, " ") + "|"
            + Global.lpad("in serv", 7, " ") + "|"
            + Global.lpad("char indi", 9, " ") + "|"
            + Global.lpad("   org call id", 18, " ") + "|"
            + Global.lpad("   translate num", 20, " ") + "|"
            + Global.lpad("scratch type", 12, " ") + "|"
            + Global.lpad("scratch number", 14, " ") + "|"
            + Global.lpad("scratch value", 13, " ") + "|"
            + Global.lpad("acc profile", 11, " ") + "|"
            + Global.lpad("    calling org", 22, " ") + "|"
            + Global.lpad("     called org", 22, " ");
        fileExp.addText(mText);
        mText = Global.lpad("-", 5, "-") + "|" + "--" + "|"
            + Global.lpad("-", 9, "-") + "|" + Global.lpad("-", 18, "-")
            + "|" + Global.rpad("-", 11, "-") + "|"
            + Global.rpad("-", 7, "-") + "|" + Global.rpad("-", 11, "-")
            + "|" + Global.lpad("-", 20, "-") + "|"
            + Global.lpad("-", 16, "-") + "|" + Global.lpad("-", 19, "-")
            + "|" + Global.rpad("-", 8, "-") + "|"
            + Global.lpad("-", 19, "-") + "|" + Global.lpad("-", 20, "-")
            + "|" + Global.lpad("-", 8, "-") + "|"
            + Global.lpad("-", 13, "-") + "|" + Global.lpad("-", 9, "-")
            + "|" + Global.lpad("-", 10, "-") + "|"
            + Global.lpad("-", 9, "-") + "|" + Global.lpad("-", 7, "-")
            + "|" + Global.lpad("-", 9, "-") + "|"
            + Global.lpad("-", 7, "-") + "|" + Global.lpad("-", 9, "-")
            + "|" + Global.lpad("-", 18, "-") + "|"
            + Global.lpad("-", 20, "-") + "|" + Global.lpad("-", 12, "-")
            + "|" + Global.lpad("-", 14, "-") + "|"
            + Global.lpad("-", 13, "-") + "|" + Global.lpad("-", 11, "-")
            + "|" + Global.lpad("-", 22, "-") + "|"
            + Global.lpad("-", 22, "-");
        fileExp.addText(mText);
        try
        {
            mText = IOUtil.FillPath(pPathRated, Global.mSeparate) + pFileName;
            delimitedFile.openDelimitedFile(mText, 5242880);
            Vector vtFieldValue = StringUtils.vectorFromString(mHeader, ";");
            miDelimitedFields = new int[vtFieldValue.size()];
            for (int i = 0; i < miDelimitedFields.length; i++)
            {
                miDelimitedFields[i] = delimitedFile
                    .findColumn(((String) vtFieldValue.elementAt(i)).trim());
            }
            while (delimitedFile.next())
            {
                mRecN++;

                mText = Global.lpad(String.valueOf(mRecN), 5, " ")
                    + "|"
                    + Global.rpad(pFileCenterCode, 2, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("CallType")), 9, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("PO_CODE")), 18, " ")
                    + "|"
                    + Global.rpad(delimitedFile.getString(delimitedFile
                    .findColumn("TaxAir")), 11, " ")
                    + "|"
                    + Global.rpad(delimitedFile.getString(delimitedFile
                    .findColumn("TaxIdd")), 7, " ")
                    + "|"
                    + Global.rpad(delimitedFile.getString(delimitedFile
                    .findColumn("TaxSer")), 11, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("CallingISDN")), 20, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("IMSI")), 16, " ")
                    + "|"
                    + Global
                    .lpad(
                    delimitedFile
                    .getString(
                    delimitedFile
                    .findColumn("CallStaTime"))
                    .substring(6, 8)
                    + "/"
                    + delimitedFile
                    .getString(
                    delimitedFile
                    .findColumn("CallStaTime"))
                    .substring(4, 6)
                    + "/"
                    + delimitedFile
                    .getString(
                    delimitedFile
                    .findColumn("CallStaTime"))
                    .substring(0, 4)
                    + " "
                    + delimitedFile
                    .getString(
                    delimitedFile
                    .findColumn("CallStaTime"))
                    .substring(8, 10)
                    + ":"
                    + delimitedFile
                    .getString(
                    delimitedFile
                    .findColumn("CallStaTime"))
                    .substring(10, 12)
                    + ":"
                    + delimitedFile
                    .getString(
                    delimitedFile
                    .findColumn("CallStaTime"))
                    .substring(12, 14), 19,
                    " ")
                    + "|"
                    + Global.rpad(delimitedFile.getString(delimitedFile
                    .findColumn("CallDuration")), 8, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("CallEndTime")), 19, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("CalledISDN")), 20, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("LocatInd")), 8, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("RemainCredit")), 13, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("CallCost")), 9, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("DisCredit")), 10, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("CharClass")), 9, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("TelInd")), 7, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("NetInd")), 9, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("INSer")), 7, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("CharInd")), 9, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("CallOrgISDN")), 18, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("TransISDN")), 20, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("ReFillType")), 12, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("RefillNum")), 14, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("RefillVal")), 13, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("AccProfile")), 11, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("CallingOrg")), 22, " ")
                    + "|"
                    + Global.lpad(delimitedFile.getString(delimitedFile
                    .findColumn("CalledOrg")), 22, " ");
                fileExp.addText(mText);
            }
            fileExp.closeFile();

            if (pZipExported == 1)
            {
                zip.Zip(pPathExport, pPathExport + ".zip", false);
                file = new File(pPathExport);
                file.setLastModified(Global
                    .convertDateTimeToLong(pDateCreateFile));
            }
            else
            {
                file = new File(pPathExport);
                file.setLastModified(Global
                    .convertDateTimeToLong(pDateCreateFile));
            }
        }
        catch (Exception e)
        {
            System.err.println(Global.Format(new java.util.Date(),
                "dd/MM/yyyy HH:mm:ss")
                + " : ERROR in module exportInCDR " + e.toString());
            throw e;
        }
        finally
        {
            try
            {
                delimitedFile.closeDelimitedFile();
            }
            catch (Exception e)
            {
            }
        }
        return mRecN;
    }

    public int reExportInCDR(java.sql.Connection pConnection,
        String pPathExport, int pFileID) throws Exception
    {
        int mRecN = 0;
        int mRecTotal = 0;
        String mText = null;
        String mSQL;
        String mFileName;
        mSQL = "SELECT DISTINCT c.file_type,b.ftp_id,"
            + "nvl(b.re_rate,0) re_rate,b.file_name " + "FROM tmp"
            + pFileID + " a,import_header b, data_param c "
            + "WHERE a.file_id=b.file_id and b.ftp_id=c.id";
        Statement stmtFile = pConnection.createStatement();
        ResultSet rsFile = stmtFile.executeQuery(mSQL);

        mSQL = "SELECT rownum order_rec,center_id file_center_code,call_type,"
            + "nvl(po_code,' ') po_code, nvl(tax_airtime,0) tax_airtime,"
            + "nvl(tax_idd,0) tax_idd, nvl(tax_service,0) tax_service,"
            + "nvl(calling_isdn,' ') calling_isdn,"
            + "nvl(calling_imsi,' ') calling_imsi,"
            + "call_sta_time, nvl(duration,0) duration,"
            + "nvl(call_end_time,' ') call_end_time,"
            + "nvl(called_isdn,' ') called_isdn,"
            + "nvl(cell_id,' ') cell_id,nvl(service_center,' ') service_center,"
            + "nvl(ic_route,' ') ic_route,nvl(og_route,' ') og_route,"
            + "nvl(tariff_class,' ') tariff_class,nvl(ts_code,' ') ts_code,"
            + "nvl(bs_code,' ') bs_code, nvl(in_mark,0) in_mark,"
            + "nvl(charging_indicator,' ') charging_indicator,"
            + "nvl(org_call_id,' ') org_call_id,"
            + "nvl(rec_seq_number,0) rec_seq_number from " + Global.OwnerDB
            + ".tmp" + pFileID + ", data_param where switch_id=id";
        Statement stmt = pConnection.createStatement();
        ResultSet rs = null;
        RandomAccessFile fileExp = null;

        try
        {
            while (rsFile.next())
            {
                // Initialize
                mRecN = 0;
                mFileName = rsFile.getString("file_name") + ".TXT."
                    + rsFile.getInt("re_rate") + 1;
                pPathExport += mFileName;

                fileExp = new RandomAccessFile(pPathExport, "rw");
                fileExp.seek(fileExp.length());
                if (rsFile.getString("file_type").compareTo("IN_PPS_V331") == 0)
                {
                    mText = " STT " + "|" + "FC" + "|"
                        + Global.lpad("call type", 9, " ") + "|"
                        + Global.lpad("     po code", 18, " ") + "|"
                        + Global.rpad("tax airtime", 11, " ") + "|"
                        + Global.rpad("tax idd", 7, " ") + "|"
                        + Global.rpad("tax service", 11, " ") + "|"
                        + Global.lpad("   calling isdn", 20, " ") + "|"
                        + Global.lpad("      imsi", 16, " ") + "|"
                        + Global.lpad("  call sta time", 19, " ") + "|"
                        + Global.rpad("duration", 8, " ") + "|"
                        + Global.lpad("  call end time", 18, " ") + "|"
                        + Global.lpad("   called isdn", 20, " ") + "|"
                        + Global.lpad("    location", 16, " ") + "|"
                        + Global.lpad("  remain credit", 18, " ") + "|"
                        + Global.lpad(" call cost", 10, " ") + "|"
                        + Global.lpad("dis credit", 10, " ") + "|"
                        + Global.lpad("tar class", 9, " ") + "|"
                        + Global.lpad("ts code", 7, " ") + "|"
                        + Global.lpad("nw result", 9, " ") + "|"
                        + Global.lpad("in serv", 7, " ") + "|"
                        + Global.lpad("char indi", 9, " ") + "|"
                        + Global.lpad("   org call id", 18, " ") + "|"
                        + Global.lpad("rec seq num", 11, " ");
                }
                else
                {
                    mText = " STT " + "|" + "FC" + "|"
                        + Global.lpad("call type", 9, " ") + "|"
                        + Global.lpad("     po code", 18, " ") + "|"
                        + Global.rpad("tax airtime", 11, " ") + "|"
                        + Global.rpad("tax idd", 7, " ") + "|"
                        + Global.rpad("tax service", 11, " ") + "|"
                        + Global.lpad("   calling isdn", 20, " ") + "|"
                        + Global.lpad("      imsi", 16, " ") + "|"
                        + Global.lpad("  call sta time", 19, " ") + "|"
                        + Global.rpad("duration", 8, " ") + "|"
                        + Global.lpad("  call end time", 18, " ") + "|"
                        + Global.lpad("   called isdn", 20, " ") + "|"
                        + Global.lpad("    cell id", 16, " ") + "|"
                        + Global.lpad("  service center", 18, " ") + "|"
                        + Global.lpad(" ic route", 10, " ") + "|"
                        + Global.lpad(" og route", 10, " ") + "|"
                        + Global.lpad("tar class", 9, " ") + "|"
                        + Global.lpad("ts code", 7, " ") + "|"
                        + Global.lpad("bs code", 9, " ") + "|"
                        + Global.lpad("in mark", 7, " ") + "|"
                        + Global.lpad("char indi", 9, " ") + "|"
                        + Global.lpad("   org call id", 18, " ") + "|"
                        + Global.lpad("rec seq num", 11, " ");
                }
                fileExp.writeBytes(mText + "\r\n");
                mText = Global.lpad("-", 5, "-") + "|" + "--" + "|"
                    + Global.lpad("-", 9, "-") + "|"
                    + Global.lpad("-", 18, "-") + "|"
                    + Global.rpad("-", 11, "-") + "|"
                    + Global.rpad("-", 7, "-") + "|"
                    + Global.rpad("-", 11, "-") + "|"
                    + Global.lpad("-", 20, "-") + "|"
                    + Global.lpad("-", 16, "-") + "|"
                    + Global.lpad("-", 19, "-") + "|"
                    + Global.rpad("-", 8, "-") + "|"
                    + Global.lpad("-", 18, "-") + "|"
                    + Global.lpad("-", 20, "-") + "|"
                    + Global.lpad("-", 16, "-") + "|"
                    + Global.lpad("-", 18, "-") + "|"
                    + Global.lpad("-", 10, "-") + "|"
                    + Global.lpad("-", 10, "-") + "|"
                    + Global.lpad("-", 9, "-") + "|"
                    + Global.lpad("-", 7, "-") + "|"
                    + Global.lpad("-", 9, "-") + "|"
                    + Global.lpad("-", 7, "-") + "|"
                    + Global.lpad("-", 9, "-") + "|"
                    + Global.lpad("-", 18, "-") + "|"
                    + Global.lpad("-", 11, "-");
                fileExp.writeBytes(mText + "\r\n");

                rs = stmt.executeQuery(mSQL);
                try
                {
                    while (rs.next())
                    {
                        mRecN++;
                        mRecTotal++;
                        mText = Global.rpad(rs.getString("order_rec"), 5, " ")
                            + "|"
                            + Global.rpad(rs.getString("file_center_code"),
                            2, " ")
                            + "|"
                            + Global
                            .lpad(rs.getString("call_type"), 9, " ")
                            + "|"
                            + Global.lpad(rs.getString("po_code"), 18, " ")
                            + "|"
                            + Global.rpad(rs.getString("tax_airtime"), 11,
                            " ")
                            + "|"
                            + Global.rpad(rs.getString("tax_idd"), 7, " ")
                            + "|"
                            + Global.rpad(rs.getString("tax_service"), 11,
                            " ")
                            + "|"
                            + Global.lpad(rs.getString("calling_isdn"), 20,
                            " ")
                            + "|"
                            + Global.lpad(rs.getString("calling_imsi"), 16,
                            " ")
                            + "|"
                            + Global.lpad(rs.getString("call_sta_time"),
                            19, " ")
                            + "|"
                            + Global.rpad(rs.getString("duration"), 8, " ")
                            + "|"
                            + Global.lpad(rs.getString("call_end_time"),
                            18, " ")
                            + "|"
                            + Global.lpad(rs.getString("called_isdn"), 20,
                            " ")
                            + "|"
                            + Global.lpad(rs.getString("cell_id"), 16, " ")
                            + "|"
                            + Global.lpad(rs.getString("service_center"),
                            18, " ")
                            + "|"
                            + Global
                            .lpad(rs.getString("ic_route"), 10, " ")
                            + "|"
                            + Global
                            .lpad(rs.getString("og_route"), 10, " ")
                            + "|"
                            + Global.lpad(rs.getString("tariff_class"), 9,
                            " ")
                            + "|"
                            + Global.lpad(rs.getString("ts_code"), 7, " ")
                            + "|"
                            + Global.lpad(rs.getString("bs_code"), 9, " ")
                            + "|"
                            + Global.lpad(rs.getString("in_mark"), 7, " ")
                            + "|"
                            + Global.lpad(rs
                            .getString("charging_indicator"), 9,
                            " ")
                            + "|"
                            + Global.lpad(rs.getString("org_call_id"), 18,
                            " ")
                            + "|"
                            + Global.lpad(rs.getString("rec_seq_number"),
                            11, " ");
                        fileExp.writeBytes(mText + "\r\n");
                    }
                }
                catch (Exception ex)
                {
                    throw ex;
                }
                if (mRecN > 0)
                {
                    Global.ExecuteSQL(pConnection, "INSERT INTO export_header("
                        + "ftp_id,file_id,file_name,records,date_create) "
                        + "values(" + rsFile.getInt("ftp_id") + ","
                        + rsFile.getInt("file_id") + ",'" + mFileName
                        + "'," + mRecN + ",sysdate)");
                    Global.ExecuteSQL(pConnection, "UPDATE import_header "
                        + "SET re_rate=re_rate+1 " + "WHERE file_id="
                        + rsFile.getInt("file_id"));
                    writeLogFile("      .Exported data to " + mFileName
                        + " => " + mRecN + " records.");
                }
            }
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            try
            {
                mText = null;
                mSQL = null;
                fileExp.close();
                fileExp = null;
                rs.close();
                rs = null;
                stmt.close();
                stmt = null;
                rsFile.close();
                rsFile = null;
                stmtFile.close();
                stmtFile = null;
            }
            catch (Exception e)
            {
            }
        }
        return mRecTotal;
    }

}
