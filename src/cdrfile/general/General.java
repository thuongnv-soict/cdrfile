package cdrfile.general;

/**
 * <p>Title: CDR File(s) System</p>
 * <p>Description: VMS IS Departerment</p>
 * <p>Copyright: Copyright (c)  by eKnowledge 2004</p>
 * <p>Company: VietNam Mobile Telecom Services</p>
 * @author eKnowledge - Software
 * @version 1.0
 */

import java.io.File;
import java.io.PrintStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import cdrfile.global.*;
import cdrfile.sms.util.SMSUtility;
import cdrfile.smtpmail.MailMessage;
import javax.mail.Session;
import javax.mail.Message;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.activation.FileDataSource;
import javax.activation.DataHandler;
import javax.mail.Transport;
import javax.mail.Multipart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;

public class General extends Global
{
    IOUtils IOUtil = new IOUtils();

    public General(String pmstrThreadID, String pmstrThreadName, String pmstrLogPathFileName)
    {
        mstrThreadID = pmstrThreadID;
        mstrThreadName = pmstrThreadName;
        mstrLogPathFileName = pmstrLogPathFileName;
    }

    public void finalize()
    {
        System.runFinalization();
        System.gc();
    }

    public void DeleteTMP(Connection pConnection, int pID) throws Exception
    {
        int Found = 0;
        String strSQL = "SELECT a.file_id,a.file_name,b.collect_dir," + "a.current_dir,b.local_split_file_by_day " + " FROM import_header a,data_param b,node_cluster c " + " WHERE a.status =" + StateCollectTrafficTurnover + " AND a.ftp_id=b.id AND b.run_on_node=c.id " + " AND b.id= " + pID + " AND general_thread_id =" + getThreadID() + " AND c.ip='" + Global.getLocalSvrIP() + "' ORDER BY file_id";
        Statement stmt = pConnection.createStatement();
        ResultSet rs = stmt.executeQuery(strSQL);
        try
        {
            strSQL = "SELECT count(*) INTO ? FROM sys_param a," + "sys_param_detail b WHERE a.id = b.ptr_id and " + " b.ptr_name='AutoCleanDBEnviroment' AND ptr_value='TRUE'";
            if (ExecuteOutParameterInt(pConnection, strSQL) > 0)
            {
                while (rs.next())
                {
                    if (Found == 0)
                    {
                        Found = 1;
                    }
                    writeLogFile("- Deleteing TEMPORARY file " + rs.getString(2) + " - " + rs.getInt(1));
                    if (rs.getInt("local_split_file_by_day") == 1)
                    {
                        IOUtil.deleteFile(IOUtil.FillPath(rs.getString("collect_dir"), Global.mSeparate) + rs.getString("current_dir") + Global.mSeparate + rs.getString("file_name"));
                    }
                    else
                    {
                        IOUtil.deleteFile(IOUtil.FillPath(rs.getString("collect_dir"), Global.mSeparate) + rs.getString("file_name"));
                    }

                    strSQL = "UPDATE import_header SET status= " + StateDeletedTmp;
                    strSQL += "WHERE file_id=" + rs.getInt(1);
                    ExecuteSQL(pConnection, strSQL);
                    pConnection.commit();
                }
            }
        }
        catch (Exception e)
        {
            pConnection.rollback();
            throw e;
        }
        finally
        {
            // Release
            rs.close();
            stmt.close();
            if (Found == 1)
            {
                writeLogFile("- Delete TEMPORARY and clear ENVIRONMENT finish.");
            }
        }
    }

    public void AlterTableSpaceCoalesce(Connection pConnection) throws Exception
    {
        try
        {
            ExecuteSQL(pConnection, "ALTER TABLESPACE users COALESCE");
            ExecuteSQL(pConnection, "ALTER TABLESPACE indx COALESCE");
        }
        catch (Exception e)
        {
            writeLogFile(" - " + e.toString());
        }
    }

    private String CheckFileMissed(Connection pConnection, int pFtpID, int pTimeCheck, int pMinSeqPos, int pMaxSeqPos, int pFisrtSeq, int pLastSeq, String pNote, int pSplitByDay, int pRenameAfterDownload) throws SQLException, Exception
    {
        // int i = 0;
        int vSeq = 0;
        int vCurrentIndex = 0;
        String mMissingFileName = "";
        String mCurrentDir = "";
        String mRet = "";
        String mSQL = "";
        String mHeader = "";
        String mExt = "";
        boolean mReturnRec = false;
        if (pRenameAfterDownload == 1)
        {
            mSQL = "SELECT a.file_name_org file_name," + "hex2dec(substr(a.file_name_org, " + pMinSeqPos + "," + pMaxSeqPos + "-" + pMinSeqPos + ")) seq, " + "max(to_char(a.date_createfile,'yyyymmdd')) current_dir," + "substr(a.file_name_org,0," + pMinSeqPos + "-1) Header," + "substr(a.file_name_org," + pMaxSeqPos + ") Ext,b.mail_to" + " FROM import_header a , data_param b " + " WHERE a.date_createfile>sysdate- " + pTimeCheck + " AND a.ftp_id=b.id AND a.ftp_id=" + pFtpID
                + " AND hex2dec(substr(a.file_name_org, " + pMinSeqPos + "," + pMaxSeqPos + "-" + pMinSeqPos + "))>=" + pFisrtSeq + " AND hex2dec(substr(a.file_name_org, " + pMinSeqPos + "," + pMaxSeqPos + "-" + pMinSeqPos + "))<=" + pLastSeq + " GROUP BY a.file_name_org,substr(a.file_name_org, " + pMinSeqPos + "," + pMaxSeqPos + "-" + pMinSeqPos + "),b.mail_to" + " ORDER BY seq";
        }
        else
        {
            mSQL = "SELECT a.file_name,substr(a.file_name, " + pMinSeqPos + "," + pMaxSeqPos + "-" + pMinSeqPos + ") seq, " + "max(to_char(a.date_createfile,'yyyymmdd')) current_dir," + "substr(a.file_name,0," + pMinSeqPos + "-1) Header," + "substr(a.file_name," + pMaxSeqPos + ") Ext,b.mail_to " + " FROM import_header a, data_param b " + " WHERE a.date_createfile>sysdate- " + pTimeCheck + " AND a.ftp_id=b.id and b.id=" + pFtpID + " AND substr(a.file_name, " + pMinSeqPos + "," + pMaxSeqPos + "-"
                + pMinSeqPos + ")>=" + pFisrtSeq + " AND substr(a.file_name, " + pMinSeqPos + "," + pMaxSeqPos + "-" + pMinSeqPos + ")<=" + pLastSeq + " GROUP BY a.file_name,substr(a.file_name, " + pMinSeqPos + "," + pMaxSeqPos + "-" + pMinSeqPos + "),b.mail_to" + " ORDER BY seq";
        }

        Statement stmt = pConnection.createStatement();
        ResultSet rs = stmt.executeQuery(mSQL);
        pConnection.setAutoCommit(false);
        try
        {
            while (rs.next())
            {
                mReturnRec = true;
                mMissingFileName = "";
                vSeq = rs.getInt("seq");
                mCurrentDir = rs.getString("current_dir");
                mHeader = rs.getString("Header");
                mExt = rs.getString("Ext");
                if (vCurrentIndex == 0)
                {
                    vCurrentIndex = vSeq;
                }
                else
                {
                    if (vCurrentIndex + 1 == vSeq)
                    {
                        // Tot, chi so tuan tu
                        vCurrentIndex = vSeq;
                    }
                    else
                    { // chi so da nhay cach quang
                        for (int j = vCurrentIndex + 1; j < vSeq; j++)
                        {
                            if (pRenameAfterDownload == 1)
                            {
                                mMissingFileName = Global.nvl(mHeader, "") + Global.Dec2Hex(j) + Global.nvl(mExt, "");
                            }
                            else
                            {
                                mMissingFileName = Global.nvl(mHeader, "") + rpad(Integer.toString(j), pMaxSeqPos - pMinSeqPos, "0") + Global.nvl(mExt, "");
                            }

                            mRet = CheckFileMissedDB(pConnection, pFtpID, mMissingFileName, pTimeCheck, pRenameAfterDownload);

                            if (mRet.compareTo("0") == 0)
                            {
                                mSQL = "UPDATE missed_file SET status=0 where switch_id=" + pFtpID + " and file_name='" + mMissingFileName + "' and status=0";
                                if (ExecuteSQL(pConnection, mSQL) == 0)
                                {
                                    pConnection.commit();
                                    mSQL = "SELECT max(nvl(id,0)) INTO ? FROM missed_file";
                                    int id = ExecuteOutParameterInt(pConnection, mSQL);
                                    if (pSplitByDay == 1)
                                    {
                                        mSQL = "INSERT INTO missed_file(id,switch_id,file_name,status," + "current_dir_missed) VALUES(" + (id + 1) + "," + pFtpID + ",'" + mMissingFileName + "',0,'" + mCurrentDir + "')";
                                    }
                                    else
                                    {
                                        mSQL = "INSERT INTO missed_file(id,switch_id,file_name,status)" + " VALUES(" + (id + 1) + "," + pFtpID + ",'" + mMissingFileName + "',0)";
                                    }
                                    ExecuteSQL(pConnection, mSQL);
                                    pConnection.commit();
                                    SendMail(pConnection, 1, rs.getString("mail_to"), "- Missing file from : " + pNote + " - " + mCurrentDir + "/" + mMissingFileName);

                                    writeLogFile("   .Missing file : " + mCurrentDir + "/" + mMissingFileName);
                                    /*General.addNewSMS(pConnection,pFtpID, 4,
                                     "Missing file:" + pNote + " File name:"+ mMissingFileName);*/
                                }
                            }
                        }
                        vCurrentIndex = vSeq;
                    }
                }
            }
            if (mReturnRec == true)
            {
                if (vSeq < pLastSeq)
                {
                    for (int j = vSeq + 1; j <= pLastSeq; j++)
                    {
                        if (pRenameAfterDownload == 1)
                        {
                            mMissingFileName = Global.nvl(mHeader, "") + Global.Dec2Hex(j) + Global.nvl(mExt, "");
                        }
                        else
                        {
                            mMissingFileName = Global.nvl(mHeader, "") + rpad(Integer.toString(j), pMaxSeqPos - pMinSeqPos, "0") + Global.nvl(mExt, "");
                        }

                        mRet = CheckFileMissedDB(pConnection, pFtpID, mMissingFileName, pTimeCheck, pRenameAfterDownload);

                        if (mRet.compareTo("0") == 0)
                        {
                            mSQL = "UPDATE missed_file SET status=0 where switch_id=" + pFtpID + " and file_name='" + mMissingFileName + "' and status=0";
                            if (ExecuteSQL(pConnection, mSQL) == 0)
                            {
                                pConnection.commit();
                                mSQL = "SELECT max(nvl(id,0)) INTO ? FROM missed_file";
                                int id = ExecuteOutParameterInt(pConnection, mSQL);
                                if (pSplitByDay == 1)
                                {
                                    mSQL = "INSERT INTO missed_file(id,switch_id,file_name,status," + "current_dir_missed) VALUES(" + (id + 1) + "," + pFtpID + ",'" + mMissingFileName + "',0,'" + mCurrentDir + "')";
                                }
                                else
                                {
                                    mSQL = "INSERT INTO missed_file(id,switch_id,file_name,status)" + " VALUES(" + (id + 1) + "," + pFtpID + ",'" + mMissingFileName + "',0)";
                                }
                                ExecuteSQL(pConnection, mSQL);
                                pConnection.commit();
                                SendMail(pConnection, 1, rs.getString("mail_to"), "- Missing file from : " + pNote + " - " + mCurrentDir + "/" + mMissingFileName);
                                writeLogFile("   .Missing file : " + mCurrentDir + "/" + mMissingFileName);
                                /*General.addNewSMS(pConnection,pFtpID, 4,
                                 "Missing file:" + pNote + " File name:"+ mMissingFileName);*/
                            }

                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            writeLogFile(" - " + e.toString());
        }
        finally
        {
            pConnection.commit();
            rs.close();
            rs = null;
            stmt.close();
            mSQL = "";
        }
        return "0";
    }

    private String CheckFileMissedDB(Connection pConnection, int pFtpID, String pFileName, int pTimeCheck, int pRenameAfterDownload) throws SQLException
    {
        String mCurrentDir = "";
        String mSQL = "";
        if (pRenameAfterDownload == 1)
        {
            mSQL = "SELECT nvl(decode(current_dir,null," + " to_char(date_createfile,'yyyymmdd'),current_dir),'0') INTO ?" + " FROM import_header " + " WHERE date_createfile>sysdate-1-" + pTimeCheck + " AND file_name_org='" + pFileName + "' AND ftp_id=" + pFtpID;
        }
        else
        {
            mSQL = "SELECT nvl(decode(current_dir,null," + " to_char(date_createfile,'yyyymmdd'),current_dir),'0') INTO ?" + " FROM import_header " + " WHERE date_createfile>sysdate-1-" + pTimeCheck + " AND file_name='" + pFileName + "' AND ftp_id=" + pFtpID;
        }

        String sql = "begin " + mSQL + "; end;";
        CallableStatement cs = pConnection.prepareCall(sql);
        try
        {
            cs.registerOutParameter(1, Types.VARCHAR);
            cs.execute();
            mCurrentDir = cs.getString(1);
        }
        catch (SQLException ex)
        {
            switch (ex.getErrorCode())
            {
            case 1403:
                mCurrentDir = "0";
                break;
            default:
                throw ex;
            }
        }
        finally
        {
            try
            {
                cs.close();
                cs = null;
            }
            catch (Exception e)
            {
            }
        }
        return mCurrentDir;
    }

    private void CheckFileMissedMinMaxSeq(Connection pConnection, int pFtpID, String pNote, int pMinSeq, int pMaxSeq, int pMinSeqPos, int pMaxSeqPos, int pSplitByDay, int pTimeCheck, int pRenameAfterDownload) throws SQLException, Exception
    {
        boolean FirstRow = false;
        int mFirstSeq = 0;
        int mLastSeq = 0;
        // int i = 0;
        String mSQL = "";
        if (pRenameAfterDownload == 1)
        {
            mSQL = "SELECT file_name_org file_name, " + " substr(file_name_org, " + pMinSeqPos + "," + pMaxSeqPos + "-" + pMinSeqPos + ") seq " + " FROM import_header " + " WHERE date_createfile>sysdate- " + pTimeCheck + " AND ftp_id=" + pFtpID + " ORDER BY date_createfile";
        }
        else
        {
            mSQL = "SELECT file_name file_name, " + " substr(file_name, " + pMinSeqPos + "," + pMaxSeqPos + "-" + pMinSeqPos + ") seq " + " FROM import_header " + " WHERE date_createfile>sysdate- " + pTimeCheck + " AND ftp_id=" + pFtpID + " ORDER BY date_createfile";
        }

        Statement stmt = pConnection.createStatement();
        ResultSet rs = stmt.executeQuery(mSQL);
        try
        {
            while (rs.next())
            {
                if (FirstRow == false)
                {
                    if (pRenameAfterDownload == 1)
                    {
                        mFirstSeq = Global.Hex2Dec(rs.getString("seq"));
                    }
                    else
                    {
                        mFirstSeq = rs.getInt("seq");
                    }
                    FirstRow = true;
                }
                else
                {
                    if (pRenameAfterDownload == 1)
                    {
                        mLastSeq = Global.Hex2Dec(rs.getString("seq"));
                    }
                    else
                    {
                        mLastSeq = rs.getInt("seq");
                    }
                }
            }

            if (mLastSeq > mFirstSeq)
            { // Sequence increase normally
                CheckFileMissed(pConnection, pFtpID, pTimeCheck, pMinSeqPos, pMaxSeqPos, mFirstSeq, mLastSeq, pNote, pSplitByDay, pRenameAfterDownload);
            }
            else
            {
                CheckFileMissed(pConnection, pFtpID, pTimeCheck, pMinSeqPos, pMaxSeqPos, mFirstSeq, pMaxSeq, pNote, pSplitByDay, pRenameAfterDownload);
                CheckFileMissed(pConnection, pFtpID, pTimeCheck, pMinSeqPos, pMaxSeqPos, pMinSeq, mLastSeq, pNote, pSplitByDay, pRenameAfterDownload);
            }
        }
        catch (Exception e)
        {
            writeLogFile(" - " + e.toString());
            System.err.println(Global.Format(new java.util.Date(), "dd/MM/yyyy HH:mm:ss") + " : ERROR in module CheckFileMissedMinMaxSeq with FTP_ID : " + pFtpID + " - " + e.toString());
        }
        finally
        {
            rs.close();
            rs = null;
            stmt.close();
            mSQL = "";
        }
    }

    private void CheckFileMissedNoMinMaxSeq(Connection pConnection, int pFtpID, int pTimeCheck, int pMinSeqPos, int pMaxSeqPos, int pFisrtSeq, String pNote, int pSplitByDay, int pRenameAfterDownload) throws SQLException, Exception
    {
        // int i = 0;
        int vSeq = 0;
        int vCurrentIndex = 0;
        String mMissingFileName = "";
        String mCurrentDir = "";
        String mRet = "";
        String mSQL = "SELECT a.file_name,substr(a.file_name, " + pMinSeqPos + "," + pMaxSeqPos + "-" + pMinSeqPos + ") seq, " + "max(to_char(a.date_createfile,'yyyymmdd')) current_dir," + "substr(a.file_name,0," + pMinSeqPos + "-1) Header,substr(a.file_name," + pMaxSeqPos + ") Ext,b.mail_to" + " FROM import_header a, data_param b " + " WHERE a.date_createfile>sysdate- " + pTimeCheck + " AND a.ftp_id=b.id AND a.ftp_id=" + pFtpID + " AND substr(a.file_name, " + pMinSeqPos + "," + pMaxSeqPos
            + "-" + pMinSeqPos + ")>=" + pFisrtSeq + " GROUP BY a.file_name,substr(a.file_name, " + pMinSeqPos + "," + pMaxSeqPos + "-" + pMinSeqPos + ")" + ",b.mail_to ORDER BY a.file_name";
        Statement stmt = pConnection.createStatement();
        ResultSet rs = stmt.executeQuery(mSQL);
        try
        {
            while (rs.next())
            {
                mMissingFileName = "";
                vSeq = rs.getInt("seq");
                mCurrentDir = rs.getString("current_dir");
                if (vCurrentIndex == 0)
                {
                    vCurrentIndex = vSeq;
                }
                else
                {
                    if (vCurrentIndex + 1 == vSeq)
                    {
                        // Tot, chi so tuan tu
                        vCurrentIndex = vSeq;
                    }
                    else
                    { // chi so da nhay cach quang
                        for (int j = vCurrentIndex + 1; j < vSeq; j++)
                        {
                            mMissingFileName = Global.nvl(rs.getString("Header"), "") + rpad(Integer.toString(j), pMaxSeqPos - pMinSeqPos, "0") + Global.nvl(rs.getString("Ext"), "");
                            mRet = CheckFileMissedDB(pConnection, pFtpID, mMissingFileName, pTimeCheck, pRenameAfterDownload);

                            if (mRet.compareTo("0") == 0)
                            {
                                mSQL = "UPDATE missed_file SET status=0 where switch_id=" + pFtpID + " and file_name='" + mMissingFileName + "' and status=0";
                                if (ExecuteSQL(pConnection, mSQL) == 0)
                                {
                                    pConnection.commit();
                                    mSQL = "SELECT max(nvl(id,0)) INTO ? FROM missed_file";
                                    int id = ExecuteOutParameterInt(pConnection, mSQL);
                                    if (pSplitByDay == 1)
                                    {
                                        mSQL = "INSERT INTO missed_file(id,switch_id,file_name,status," + "current_dir_missed) VALUES(" + (id + 1) + "," + pFtpID + ",'" + mMissingFileName + "',0,'" + mCurrentDir + "')";
                                    }
                                    else
                                    {
                                        mSQL = "INSERT INTO missed_file(id,switch_id,file_name,status)" + " VALUES(" + (id + 1) + "," + pFtpID + ",'" + mMissingFileName + "',0)";
                                    }
                                    ExecuteSQL(pConnection, mSQL);
                                    pConnection.commit();
                                    SendMail(pConnection, 1, rs.getString("mail_to"), "- Missing file from : " + pNote + " - " + mCurrentDir + "/" + mMissingFileName);
                                    writeLogFile("   .Missing file : " + mCurrentDir + "/" + mMissingFileName);
                                    /*General.addNewSMS(pConnection,pFtpID, 2,
                                     "Missing file:" + pNote + " File name:"+ mMissingFileName);*/
                                }

                            }
                        }
                        vCurrentIndex = vSeq;
                    }
                }
            }
        }
        catch (Exception e)
        {
            writeLogFile(" - " + e.toString());
        }
        finally
        {
            rs.close();
            rs = null;
            stmt.close();
            mSQL = "";
        }
    }

    public void CheckFileMissed(Connection pConnection, int pID) throws SQLException, Exception
    {
        String mSQL = "SELECT id,note,ftp_host_ip,nvl(min_seq,-1) min_seq," + "nvl(max_seq,-1) max_seq,seq_from,seq_to,time_check," + "local_split_file_by_day,rename_after_download " + " FROM data_param WHERE used_getfile=1 and check_file=1" + " AND id= " + pID;
        Statement stmt = pConnection.createStatement();
        ResultSet rs = stmt.executeQuery(mSQL);
        try
        {
            //Ma cu
            mSQL = "SELECT count(*) INTO ? FROM sys_param a,sys_param_detail b" + " WHERE a.id = b.ptr_id " + " AND b.ptr_name='AutoCheckupMissedFile'" + " AND ptr_value='TRUE'";
            //Nang cap cau hinh cho 1 loai file
            /*mSQL = "SELECT count(*) INTO ? FROM sys_param a,sys_param_detail b"
                   + " WHERE a.id = b.ptr_id "
                   + " AND b.ptr_name='AutoCheckupMissedFile'"
                   + " AND ptr_value='TRUE' AND b.id= "+ pID;*/
            if (ExecuteOutParameterInt(pConnection, mSQL) > 0)
            {
                while (rs.next())
                {
                    writeLogFile("- Checking missed file " + rs.getString("note"));
                    if ((rs.getInt("min_seq") != -1) && (rs.getInt("max_seq") != -1))
                    {
                        CheckFileMissedMinMaxSeq(pConnection, rs.getInt("id"), rs.getString("note"), rs.getInt("min_seq"), rs.getInt("max_seq"), rs.getInt("seq_from"), rs.getInt("seq_to"), rs.getInt("local_split_file_by_day"), rs.getInt("time_check"), rs.getInt("rename_after_download"));
                    }
                    else
                    {
                        CheckFileMissedNoMinMaxSeq(pConnection, rs.getInt("id"), rs.getInt("time_check"), rs.getInt("seq_from"), rs.getInt("seq_to"), rs.getInt("min_seq"), rs.getString("note"), rs.getInt("local_split_file_by_day"), rs.getInt("rename_after_download"));
                    }
                }
            }
        }
        catch (Exception e)
        {
            writeLogFile(" - " + e.toString() + " ID:" + pID);
        }
        finally
        {
            rs.close();
            rs = null;
            stmt.close();
            stmt = null;
            mSQL = "";
        }
    }

    public void CheckFreeDiskSpace(Connection pConnection) throws Exception
    {

        long mCurrentSpace = 0;
        Statement stmt = null;
        ResultSet rs = null;
        try
        {
            String mSQL = "select b.ptr_name,ptr_value " + "from sys_param a,sys_param_detail b " + "where a.id=b.ptr_id and a.ptr_name='CheckFreeDiskSpace'";
            stmt = pConnection.createStatement();
            rs = stmt.executeQuery(mSQL);
            String mCheckFreeDiskSpace = "";
            while (rs.next())
            {
                if (rs.getString("ptr_name").compareTo("TimeCheckFreeDiskSpace") == 0)
                {
                    mCheckFreeDiskSpace = rs.getString("ptr_value");
                }
            }
            if (mCheckFreeDiskSpace == null || mCheckFreeDiskSpace.equals(""))
            {
                return;
            }
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            java.util.Date now = calendar.getTime();
            long currTime = now.getTime();
            if (currTime - cdrfileParam.AlreadyCheckFreeDiskSpace >= Long.parseLong(mCheckFreeDiskSpace) * 60 * 1000)
            {
                mSQL = "SELECT count(*) INTO ? FROM sys_param a,sys_param_detail b " + "WHERE a.id = b.ptr_id and " + " b.ptr_name='AutoCheckupFreeSpace' AND ptr_value='TRUE'";
                if (ExecuteOutParameterInt(pConnection, mSQL) > 0)
                {
                    writeLogFile("- Checking free space is started...");
                    mSQL = "select b.ptr_name,ptr_value " + "from sys_param a,sys_param_detail b " + "where a.id=b.ptr_id and a.ptr_name='CheckupFreeSpace' " + "and b.ptr_name <>'AutoCheckupFreeSpace'";
                    stmt = pConnection.createStatement();
                    rs = stmt.executeQuery(mSQL);

                    while (rs.next())
                    {
                        mCurrentSpace = DiskUtil.getFreeSpace(rs.getString("ptr_name")) / (1024 * 1024);
                        if (mCurrentSpace <= rs.getInt("ptr_value"))
                        {
                            SendMail(pConnection, 1, "", " - Free space available on : " + rs.getString("ptr_name") + " : " + mCurrentSpace + " Gb.\r\n");
                            /*General.addNewSMS(pConnection,pFtpID, 2,
                                         "Free space available on : "
                                     + rs.getString("ptr_name") + " : "
                                     + mCurrentSpace + " Gb");*/


                        }
                    }
                    writeLogFile("- Checking free space is finished...");
                    cdrfileParam.AlreadyCheckFreeDiskSpace = currTime;
                }

            }
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            if (rs != null)
            {
                rs.close();
            }
            rs = null;
            if (stmt != null)
            {
                stmt.close();
            }
            stmt = null;
        }
    }

    public void sentSMS(Connection pConnection) throws Exception
    {
        String mSQL = "select b.ptr_name,ptr_value " + "from sys_param a,sys_param_detail b " + "where a.id=b.ptr_id and a.ptr_name='SMSAlert'";
        Statement stmt = pConnection.createStatement();
        ResultSet rs = stmt.executeQuery(mSQL);

        Statement sms_stmt = pConnection.createStatement();
        ResultSet sms_rs = null;
        pConnection.setAutoCommit(false);
        String mTimeSendSMS = "";

        try
        {
            while (rs.next())
            {
                if (rs.getString("ptr_name").compareTo("TimeSendSMS") == 0)
                {
                    mTimeSendSMS = rs.getString("ptr_value");
                }
            }
            if (mTimeSendSMS == null || mTimeSendSMS.equals(""))
            {
                return;
            }
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            java.util.Date now = calendar.getTime();
            long currTime = now.getTime();
            if (currTime - cdrfileParam.AlreadyDateTimeSendSMS >= Long.parseLong(mTimeSendSMS) * 60 * 1000)
            {
                writeLogFile("- Sending SMS...");
                mSQL = "select * from sms_queue where sent = 0";
                sms_rs = sms_stmt.executeQuery(mSQL);
                while (sms_rs.next())
                {
                    int id = sms_rs.getInt("id");
                    String isdn = sms_rs.getString("isdn");
                    String content = sms_rs.getString("content");
                    SMSUtility.sendSMSAlert(isdn, content);
                    mSQL = "UPDATE sms_queue SET sent = 1";
                    mSQL += " WHERE id=" + id;
                    Global.ExecuteSQL(pConnection, mSQL);
                    pConnection.commit();
                }
                cdrfileParam.AlreadyDateTimeSendSMS = currTime;
            }
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            rs.close();
            rs = null;
            stmt.close();
            stmt = null;
            if (sms_rs != null)
            {
                sms_rs.close();
                sms_rs = null;
            }
            sms_stmt.close();
            sms_stmt = null;
            mSQL = "";

        }

    }

    public void SendSmtpMail(Connection pConnection) throws Exception
    {
        String mSQL = "select b.ptr_name,ptr_value " + "from sys_param a,sys_param_detail b " + "where a.id=b.ptr_id and a.ptr_name='MailMessage'";
        Statement stmt = pConnection.createStatement();
        ResultSet rs = stmt.executeQuery(mSQL);
        ResultSet rs_batch = null;
        Statement stmt_batch = null;
        PrintStream out = null;
        String mailhost = "127.0.0.1"; // or another mail host
        String from = "CDRFILE System Mail Message<MailMessage@cdrfile.vms.com.vn>";
        String to = "hungdt@vms.com.vn";
        String tocc = "";
        int Port = 25;
        String mTimeSendMailMessage = "";
        String MailSubject = "CDR File System Mail Alert";
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        String mCurrentDateTime = sdf.format(new java.util.Date());
        try
        {
            if ((mTimeSentMailMessage.compareTo(mCurrentDateTime) != 0))
            {
                while (rs.next())
                {
                    if (rs.getString("ptr_name").compareTo("MailHost") == 0)
                    {
                        mailhost = rs.getString("ptr_value");
                    }
                    if (rs.getString("ptr_name").compareTo("Port") == 0)
                    {
                        Port = rs.getInt("ptr_value");
                    }
                    if (rs.getString("ptr_name").compareTo("MailFrom") == 0)
                    {
                        from = rs.getString("ptr_value");
                    }
                    if (rs.getString("ptr_name").compareTo("MailSubject") == 0)
                    {
                        MailSubject = rs.getString("ptr_value");
                    }
                    if (rs.getString("ptr_name").compareTo("MailTo") == 0)
                    {
                        to = rs.getString("ptr_value");
                    }
                    if (rs.getString("ptr_name").compareTo("MailToCC") == 0)
                    {
                        tocc = rs.getString("ptr_value");
                    }
                    if (rs.getString("ptr_name").compareTo("TimeSendMailMessage") == 0)
                    {
                        mTimeSendMailMessage = rs.getString("ptr_value");
                    }
                }
                if ((mTimeSendMailMessage.indexOf(mCurrentDateTime) >= 0) && (cdrfileParam.AlreadyDateTimeSendMail.compareTo(mCurrentDateTime) != 0))
                {
                    mSQL = "select batch_id, to_char(process_date,'dd/mm/yyyy hh24:mi:ss') as process_date, recipient, status, class_type_id from smtp_batch where status=0 order by process_date";
                    stmt_batch = pConnection.createStatement();
                    rs_batch = stmt_batch.executeQuery(mSQL);
                    if (rs_batch != null)
                    {
                        writeLogFile("- Sending mail altert is started...");
                        while (rs_batch.next())
                        {
                            MailMessage msg = new MailMessage(mailhost);
                            msg.setPort(Port);
                            msg.from(from);
                            msg.to(to);
                            if (rs_batch.getString("recipient") != null)
                            {
                                msg.to(rs_batch.getString("recipient"));
                            }
                            if (tocc != null)
                            {
                                msg.cc(tocc);
                            }
                            msg.setSubject(MailSubject);
                            out = msg.getPrintStream();
                            mSQL = "select content,to_char(process_date,'dd/mm/yyyy hh24:mi:ss') as process_date from smtp_batch_detail where batch_id=" + rs_batch.getInt("batch_id") + " and to_char(process_date,'dd/mm/yyyy hh24:mi:ss') = '" + rs_batch.getString("process_date") + "' order by id";
                            rs = stmt.executeQuery(mSQL);
                            while (rs.next())
                            {
                                out.println(rs.getString("process_date") + " - " + rs.getString("content") + "\r\n");
                            }
                            // out.println();
                            msg.sendAndClose();
                            mSQL = "update smtp_batch set status=1 where batch_id=" + rs_batch.getInt("batch_id");
                            ExecuteSQL(pConnection, mSQL);
                            pConnection.commit();
                        }
                        writeLogFile("- Sending mail altert is finished...");
                        cdrfileParam.AlreadyDateTimeSendMail = mCurrentDateTime;
                    }
                }
            }
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            rs.close();
            rs = null;
            stmt.close();
            stmt = null;
            if (rs_batch != null)
            {
                rs_batch.close();
            }
            rs_batch = null;
            if (stmt_batch != null)
            {
                stmt_batch.close();
            }
            stmt_batch = null;
            if (out != null)
            {
                out.close();
            }
            mSQL = "";
        }
    }


    public void checkContentTimeCDR(Connection pConnection, int pID, String pmailTo) throws Exception
    {
        String mSQL = "SELECT CHECK_TIME_CONTENT  INTO ? ";
        mSQL += "FROM DATA_PARAM ";
        mSQL += "WHERE ID = " + pID;
        int mRet = 0;
        try
        {
            mRet = ExecuteOutParameterInt(pConnection, mSQL);
        }
        catch (Exception e)
        {
            writeLogFile(" - " + e.toString());
            mRet = 0;
        }
        if (mRet > 0)
        {
            // Lay cac file ma co thoi diem phat sinh cuoc goi cuoi cung nho hon thoi diem tao file so gio cho truoc
            Statement stmt = pConnection.createStatement();
            mSQL = "select a.note,b.file_id, b.file_name, round(24*(b.date_createfile - to_date(b.min_calling_time,'dd/mm/yyyy HH24:mi:ss')),0) as hours ";
            mSQL += "from data_param a,import_header b ";
            mSQL += "where min_calling_time is not null ";
            mSQL += "and ftp_id = a.id and a.id =" + pID;
            mSQL += "and a.time_content_before_hours>0 ";
            mSQL += "and b.date_createfile > sysdate - a.check_time_content_duration/24 ";
            mSQL += "and b.date_createfile - (a.time_content_before_hours)/24 > to_date(b.min_calling_time,'dd/mm/yyyy HH24:mi:ss')";
            // Lay cac file ma co thoi diem phat sinh cuoc goi cuoi cung lon hon thoi diem tao file so gio cho truoc

            ResultSet rs = stmt.executeQuery(mSQL);
            try
            {
                while (rs.next())
                {
                    String content = "- Last calling time less than the created time of CDR file :" + rs.getString("note") + " - File id:" + rs.getInt("file_id") + " , file name:" + rs.getString("file_name") + " " + rs.getDouble("hours") + " hours";
                    SendMail(pConnection, 1, pmailTo, content);
                    /*General.addNewSMS(pConnection,pID, 6,
                     "Last calling time < date created file - "
                                         + rs.getString("note") + " - File id:"
                     + rs.getInt("file_id") + " , file name:" +
                                         rs.getInt("file_name"));*/


                    //writeLogFile(content);
                }
                //SendSmtpMail(pConnection);
            }
            catch (Exception ex)
            {
                writeLogFile("   .Send mail error with check time content of file" + ex.getMessage());
            }
            finally
            {
                rs.close();
            }

            mSQL = "select a.note,b.file_id, b.file_name, round(24*(b.date_createfile - to_date(b.min_calling_time,'dd/mm/yyyy HH24:mi:ss')),0) as hours ";
            mSQL += "from data_param a,import_header b ";
            mSQL += "where min_calling_time is not null ";
            mSQL += "and ftp_id = a.id and a.id =" + pID;
            mSQL += "and a.time_content_after_hours >0 ";
            mSQL += "and b.date_createfile > sysdate - a.check_time_content_duration/24 ";
            mSQL += "and b.date_createfile + (a.time_content_after_hours)/24 < to_date(b.min_calling_time,'dd/mm/yyyy HH24:mi:ss')";
            rs = stmt.executeQuery(mSQL);
            try
            {
                while (rs.next())
                {
                    String content = "- Last calling time greater than the created time of CDR file :" + rs.getString("note") + " - File id:" + rs.getInt("file_id") + " , file name:" + rs.getString("file_name") + " " + rs.getDouble("hours") + " hours";
                    SendMail(pConnection, 1, pmailTo, content);
                    /*General.addNewSMS(pConnection,pID, 6,
                     "Last calling time > date created file - "
                                         + rs.getString("note") + " - File id:"
                     + rs.getInt("file_id") + " , file name:" +
                                         rs.getInt("file_name"));*/

                    writeLogFile(content);
                }
                //SendSmtpMail(pConnection);
            }
            catch (Exception ex)
            {
                writeLogFile("   .Send mail error with check time content of file" + ex.getMessage());
            }
            finally
            {
                rs.close();
            }

        }
    }

//    Modify 26/7
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public void DeleteCDFile(Connection pConnection) throws Exception
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        int mRet = -1;
        boolean FirstTime;
        File myFolderGet = null;
        File myFolderPut = null;
        File myFile = null;
        File mysubFile = null;
        String mSQL = "";

        mSQL = "SELECT a.id,a.note,DELETE_CDRFILE_AFTER_DAYS,a.local_putfile_dir,";
        mSQL += "    to_char(sysdate-(case when DELETE_CDRFILE_AFTER_DAYS<0 then 1 else DELETE_CDRFILE_AFTER_DAYS end)    ,'yyyymmddhh24miss') last_time_delete ";
        mSQL += " FROM data_param a,node_cluster b ";
        mSQL += " WHERE a.run_on_node=b.id ";
        mSQL += " AND zip_thread_id =" + getThreadID();
        mSQL += " AND b.ip='" + Global.getLocalSvrIP();
        mSQL += "' AND used_getfile=1 and convert_thread_id is not null";

        Statement stmt = pConnection.createStatement();
        ResultSet rs = stmt.executeQuery(mSQL);
        try
        {
            while (rs.next())
            {
                mRet = rs.getInt("DELETE_CDRFILE_AFTER_DAYS");

                if (mRet > 0)
                {
                    myFolderPut = new File((String) rs.getString("local_putfile_dir"));
                    //Process text file.
                    if (myFolderPut.exists())
                    {
                        File[] files = myFolderPut.listFiles();
                        List fileList = Arrays.asList(files);
                        FirstTime = true;
                        for (Iterator its = fileList.iterator(); its.hasNext(); )
                        {
                            myFile = (File) its.next();
                            if (Long.parseLong(sdf.format(new java.util.Date(myFile.lastModified()))) < Long.parseLong(rs.getString("last_time_delete")))
                            {
                                if (FirstTime)
                                {
                                    writeLogFile("- Deleting CDR File of " + rs.getString("note") + " - " + rs.getString("local_putfile_dir"));
                                    FirstTime = false;
                                }
                                if (myFile.isDirectory())
                                {
                                    File[] filesub = myFile.listFiles();
                                    List filesubList = Arrays.asList(filesub);
                                    for (Iterator itsub = filesubList.iterator(); itsub.hasNext(); )
                                    {
                                        mysubFile = (File) itsub.next();
                                        mysubFile.delete();
                                    }
                                    if (myFile.delete())
                                    {
                                        writeLogFile("   . Deleted " + myFile.getName());
                                    }
                                }
                                else
                                {
                                    if (myFile.delete())
                                    {
                                        writeLogFile("   . Deleted " + myFile.getName());
                                    }
                                }
                            }

                        }
                    }
                }
            }

            //////////Ket thuc delete file text/////////////////////

//            sdf = null;
            rs.close();
            rs = null;
            stmt.close();
            stmt = null;
            mSQL = "";

            /////////////////////////////////////////////////
            //Process Bin File
            mSQL = "SELECT a.id,a.note,a.local_getfile_dir,DELETE_CDRFILE_AFTER_DAYS,";
            mSQL += "    to_char(sysdate-(case when DELETE_CDRFILE_AFTER_DAYS<0 then 1 else DELETE_CDRFILE_AFTER_DAYS end)    ,'yyyymmddhh24miss') last_time_delete ";
            mSQL += " FROM data_param a,node_cluster b ";
            mSQL += " WHERE a.run_on_node=b.id ";
            mSQL += " AND zip_thread_id =" + getThreadID();
            mSQL += " AND b.ip='" + Global.getLocalSvrIP();
            mSQL += "' AND used_getfile=1";

            stmt = pConnection.createStatement();
            rs = stmt.executeQuery(mSQL);

            while (rs.next())
            {
                mRet = rs.getInt("DELETE_CDRFILE_AFTER_DAYS");
                if (mRet > 0)
                {
                    myFolderGet = new File((String) rs.getString("local_getfile_dir"));
                    if (myFolderGet.exists())
                    {
                        File[] files = myFolderGet.listFiles();
                        List fileList = Arrays.asList(files);
                        FirstTime = true;

                        for (Iterator its = fileList.iterator(); its.hasNext(); )
                        {
                            myFile = (File) its.next();
                            if (Long.parseLong(sdf.format(new java.util.Date(myFile.lastModified()))) < Long.parseLong(rs.getString("last_time_delete")))
                            {
                                if (FirstTime)
                                {
                                    writeLogFile("- Deleting CDR File of " + rs.getString("note") + " - " + rs.getString("local_getfile_dir"));
                                    FirstTime = false;
                                }
                                if (myFile.isDirectory())
                                {
                                    File[] filesub = myFile.listFiles();
                                    List filesubList = Arrays.asList(filesub);
                                    for (Iterator itsub = filesubList.iterator(); itsub.hasNext(); )
                                    {
                                        mysubFile = (File) itsub.next();
                                        mysubFile.delete();
                                    }
                                    if (myFile.delete())
                                    {
                                        writeLogFile("   . Deleted " + myFile.getName());
                                    }
                                }
                                else
                                {
                                    if (myFile.delete())
                                    {
                                        writeLogFile("   . Deleted " + myFile.getName());
                                    }
                                }
                            }

                        }
                    }
                }
            }

        }

        catch (Exception e)
        {
//            e.printStackTrace();
            writeLogFile(" - " + e.toString());
        }
        finally
        {
            sdf = null;
            rs.close();
            rs = null;
            stmt.close();
            stmt = null;
            mSQL = "";
        }
    }


///////////////////////////////////////////////////////////////////////////////////////////////////

    public void DeleteCDFile(Connection pConnection, int pID) throws Exception
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        List scriptEntrys = null;
        int mRet = -1;
        boolean FirstTime;
        File myFolder = null;
        File myFile = null;
        File mysubFile = null;
        String mSQL = "SELECT DELETE_CDRFILE_AFTER_DAYS INTO ? ";
        mSQL += "FROM DATA_PARAM ";
        mSQL += "WHERE ID = " + pID;
        try
        {
            mRet = ExecuteOutParameterInt(pConnection, mSQL);
        }
        catch (Exception e)
        {
            writeLogFile(" - " + e.toString());
            mRet = -1;
        }
        if (mRet == -1)
        {
            mSQL = "SELECT nvl(ptr_value,0) INTO ? ";
            mSQL += "FROM sys_param a,sys_param_detail b ";
            mSQL += "WHERE a.id = b.ptr_id and ";
            mSQL += "b.ptr_name='AutoDeleteCDRFileAfterDays'";
            try
            {
                mRet = ExecuteOutParameterInt(pConnection, mSQL);
            }
            catch (Exception e)
            {
                writeLogFile(" - " + e.toString());
            }
        }

        mSQL = "SELECT a.id,a.note,a.local_getfile_dir, " + "to_char(sysdate-" + mRet + ",'yyyymmddhh24miss') last_time_delete " + " FROM data_param a,node_cluster b " + " WHERE a.run_on_node=b.id AND a.id = " + pID + " AND general_thread_id =" + getThreadID() + " AND b.ip='" + Global.getLocalSvrIP() + "' AND used_getfile=1";
        Statement stmt = pConnection.createStatement();
        ResultSet rs = stmt.executeQuery(mSQL);
        try
        {
            if (mRet > 0)
            {
                while (rs.next())
                {
                    scriptEntrys = new ArrayList();
                    scriptEntrys.add(rs.getString("local_getfile_dir"));
                    if (scriptEntrys == null)
                    {
                        return;
                    }
                    for (Iterator it = scriptEntrys.iterator(); it.hasNext(); )
                    {
                        myFolder = new File((String) it.next());
                        if (myFolder.exists())
                        {
                            File[] files = myFolder.listFiles();
                            List fileList = Arrays.asList(files);
                            FirstTime = true;
                            for (Iterator its = fileList.iterator(); its.hasNext(); )
                            {
                                myFile = (File) its.next();
                                if (Long.parseLong(sdf.format(new java.util.Date(myFile.lastModified()))) < Long.parseLong(rs.getString("last_time_delete")))
                                {
                                    if (FirstTime)
                                    {
                                        writeLogFile("- Deleting CDR File of " + rs.getString("note") + " - " + rs.getString("local_getfile_dir"));
                                        FirstTime = false;
                                    }
                                    if (myFile.isDirectory())
                                    {
                                        File[] filesub = myFile.listFiles();
                                        List filesubList = Arrays.asList(filesub);
                                        for (Iterator itsub = filesubList.iterator(); itsub.hasNext(); )
                                        {
                                            mysubFile = (File) itsub.next();
                                            mysubFile.delete();
                                        }
                                        if (myFile.delete())
                                        {
                                            writeLogFile("   . Deleted " + myFile.getName());
                                        }
                                    }
                                    else
                                    {
                                        if (myFile.delete())
                                        {
                                            writeLogFile("   . Deleted " + myFile.getName());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            writeLogFile(" - " + e.toString());
        }
        finally
        {
            sdf = null;
            scriptEntrys = null;
            rs.close();
            rs = null;
            stmt.close();
            stmt = null;
            mSQL = "";
        }
    }

    public void CheckNoDataFileToDownload(Connection pConnection) throws Exception
    {
        String mSQL = "select b.ptr_name,ptr_value " + "from sys_param a,sys_param_detail b " + "where a.id=b.ptr_id and a.ptr_name='CheckNoFileToDownload'";
        Statement stmt = pConnection.createStatement();
        ResultSet rs = stmt.executeQuery(mSQL);
        String mTimeCheckNoFileToDownload = "";
        try
        {
            while (rs.next())
            {
                if (rs.getString("ptr_name").compareTo("TimeCheckNoFileToDownload") == 0)
                {
                    mTimeCheckNoFileToDownload = rs.getString("ptr_value");
                }
            }
            if (mTimeCheckNoFileToDownload == null || mTimeCheckNoFileToDownload.equals(""))
            {
                return;
            }
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            java.util.Date now = calendar.getTime();
            long currTime = now.getTime();
            if (currTime - cdrfileParam.AlreadyCheckNoFileToDownload >= Long.parseLong(mTimeCheckNoFileToDownload) * 60 * 1000)
            {
                writeLogFile("- Checking no file to download is started...");
                mSQL = "select c.time_check_nofile,a.ftp_id,b.max_date," + "c.file_type,c.note,c.center_id,a.file_name," + "c.ftp_host_ip,c.remote_getfile_dir,c.mail_to " + "from import_header a,(select ftp_id,max(file_id) file_id," + "max(date_getfile) max_date from import_header,data_param " + "where date_createfile>=to_date(file_info,'yyyyMMddhh24miss')" + "and file_info<>'0' and used_getfile=1 and ftp_id=id " + "and time_check_nofile>0 group by ftp_id ) b, data_param c "
                    + "where a.file_id=b.file_id and a.ftp_id=c.id " + "and (sysdate-b.max_date)* 24 > c.time_check_nofile";
                if (stmt != null)
                {
                    stmt = pConnection.createStatement();
                }

                rs = stmt.executeQuery(mSQL);

                while (rs.next())
                {
                    mSQL = "No file to download from " + rs.getString("note") + "\n";
                    mSQL += "      CENTER     :" + rs.getInt("center_id") + "\n";
                    mSQL += "      FTP_ID     :" + rs.getString("ftp_id") + "\n";
                    mSQL += "      IP         :" + rs.getString("ftp_host_ip") + "\n";
                    mSQL += "      REMOTE_DIR :" + rs.getString("remote_getfile_dir") + "\n";
                    mSQL += "      FILE_TYPE  :" + rs.getString("file_type") + "\n";
                    mSQL += "      FILE_NAME  :" + rs.getString("file_name") + "\n";
                    mSQL += "      LAST_DATE  :" + rs.getString("max_date") + "\r\n";

                    SendMail(pConnection, 1, rs.getString("mail_to"), mSQL);
                    /*General.addNewSMS(pConnection, rs.getInt("ftp_id"), 5,
                        "No file to download from "
                        + rs.getString("note"));*/

                }
                writeLogFile("- Checking no file to download is finished...");
                cdrfileParam.AlreadyCheckNoFileToDownload = currTime;
            }

        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            rs.close();
            rs = null;
            stmt.close();
            stmt = null;
            mSQL = "";
        }
    }

    public void CreatePartition(Connection pConnection) throws SQLException
    {
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyyMM");
        int mIsExst = 0;
        Statement stmt = pConnection.createStatement();
        Statement stmtIdxPartRs = pConnection.createStatement();
        Statement stmtIdxRs = pConnection.createStatement();
        int NextMonth = 0;
        int i = 0;
        String NewParName = null;
        // String PriorParName = null;
        String NextParName = null;
        String strSQL = null;
        String DateNextMonth = null;
        String DateAfterNextMonth = null;
        long mNextMonth = (long) (5270400 * 1000.0);
        long mAfterNextMonth = (long) (7948800 * 1000.0);
        // 7948800=3600*24*61->add 91 day to check partition already existed
        // 5270400=3600*24*61->add 61 day to check partition already existed
        // 2678400=3600*24*31->add 31 day to check partition already existed
        String mDay = null;
        try
        {
            mNextMonth += new java.util.Date().getTime();
            mAfterNextMonth += new java.util.Date().getTime();
            DateNextMonth = fmt.format(new java.util.Date(mNextMonth));
            DateAfterNextMonth = fmt.format(new java.util.Date(mAfterNextMonth));
            strSQL = "SELECT count(*) INTO ? FROM table_part ";
            strSQL += "WHERE data_month=" + DateNextMonth;
            if (Global.ExecuteOutParameterInt(pConnection, strSQL) == 0)
            {
                writeLogFile(" - Creating new Partition for system environment...");
                pConnection.setAutoCommit(false);
                strSQL = "SELECT a.table_name,a.partition_format ";
                strSQL += "FROM table_init a, sys.all_part_tables b ";
                strSQL += "WHERE b.owner='CDRFILE_OWNER' AND a.table_name=b.table_name";
                ResultSet rs = stmt.executeQuery(strSQL);
                i = Integer.parseInt(DateNextMonth.substring(DateNextMonth.length() - 2));
                switch (i)
                {
                case 1:
                    NextMonth = Integer.parseInt(DateNextMonth) + 1;
                    break;
                case 12:
                    NextMonth = Integer.parseInt(DateNextMonth) + 100 - 11;
                    break;
                default:
                    NextMonth = Integer.parseInt(DateNextMonth) + 1;
                    break;
                }
                NewParName = "DATA" + DateNextMonth;
                while (rs.next())
                {
                    if (rs.getString("partition_format").compareTo("YYYYMM") == 0)
                    {
                        mDay = DateAfterNextMonth + "01)";
                    }
                    else
                    {
                        mDay = "TO_DATE('" + DateAfterNextMonth.substring(0, 4) + "/" + DateAfterNextMonth.substring(4) + "/01 00:00:00', 'SYYYY-MM-DD HH24:MI:SS', 'NLS_CALENDAR=GREGORIAN'))";
                    }
                    writeLogFile("      .Checking new Partition has been already exist in table " + rs.getString("table_name"));
                    strSQL = "Declare ";
                    strSQL += "PriorName varchar2(100):='';";
                    strSQL += "NextName varchar2(100):=''; ";
                    strSQL += "IsExist number:=0; ";
                    strSQL += "Begin ";
                    strSQL += " :IsExist:=CDRFILE.CHECK_PARTITION('SMS','" + rs.getString("table_name");
                    strSQL += "','" + NewParName + "',:PriorName,:NextName); ";
                    strSQL += "End;";
                    CallableStatement cs = pConnection.prepareCall(strSQL);
                    cs.registerOutParameter(1, Types.INTEGER);
                    cs.registerOutParameter(2, Types.VARCHAR);
                    cs.registerOutParameter(3, Types.VARCHAR);
                    cs.execute();
                    mIsExst = 0;
                    mIsExst = cs.getInt(1);
                    // PriorParName = cs.getString(2);
                    NextParName = cs.getString(3);
                    cs.close();
                    cs = null;
                    if (mIsExst != 1)
                    {
                        // Find partition next to check exist yet?
                        // if next partition is null
                        if ((NextParName.compareTo("") == 0) || (NextParName.compareTo("0") == 0))
                        {
                            writeLogFile("      .Create new Partition for table " + rs.getString("table_name"));
                            // Add new partition
                            strSQL = "ALTER TABLE " + rs.getString("table_name");
                            strSQL += " ADD PARTITION " + NewParName + " VALUES LESS THAN (";
                            strSQL += mDay;
                        }
                        else
                        {
                            writeLogFile("      .Split new Partition for table " + rs.getString("table_name"));
                            // split partition
                            strSQL = "ALTER TABLE " + rs.getString("table_name");
                            strSQL += " SPLIT PARTITION " + NextParName + " AT (" + NextMonth;
                            strSQL += ") INTO (PARTITION " + NewParName + ",PARITION " + NextParName;
                        }
                        Global.ExecuteSQL(pConnection, strSQL);
                        // Rebuild global index and local index partition again
                        writeLogFile("      .Rebuilding global index and local index partition ...");
                        strSQL = "SELECT index_name,status,partitioned FROM sys.all_indexes ";
                        strSQL += "WHERE owner='CDRFILE_OWNER' and table_name='";
                        strSQL += rs.getString("table_name") + "'";
                        ResultSet IdxRs = stmtIdxRs.executeQuery(strSQL);
                        while (IdxRs.next())
                        {
                            if ((IdxRs.getString("partitioned").compareTo("NO") == 0) && (IdxRs.getString("status").compareTo("UNUSABLE") == 0))
                            {
                                writeLogFile("         .Rebuilding global index - " + IdxRs.getString("index_name"));
                                strSQL = "ALTER INDEX " + IdxRs.getString("index_name");
                                strSQL += " REBUILD DEFAULT TABLESPACE INDX";
                                Global.ExecuteSQL(pConnection, strSQL);
                            }
                            else if (IdxRs.getString("partitioned").compareTo("YES") == 0)
                            {
                                strSQL = "SELECT index_name,partition_name,status ";
                                strSQL += "FROM sys.all_ind_partitions ";
                                strSQL += "WHERE index_name='" + IdxRs.getString("index_name") + "' ";
                                strSQL += "AND index_owner='CDRFILE_OWNER' AND status='UNUSABLE'";
                                ResultSet IdxParRs = stmtIdxRs.executeQuery(strSQL);
                                while (IdxParRs.next())
                                {
                                    writeLogFile("         .Rebuilding local partition index - " + IdxParRs.getString("index_name"));

                                    strSQL = "ALTER INDEX " + IdxParRs.getString("index_name");
                                    strSQL += " REBUILD PARTITION " + IdxParRs.getString("partition_name");
                                    strSQL += " DEFAULT TABLESPACE INDX";
                                    Global.ExecuteSQL(pConnection, strSQL);
                                }
                                IdxParRs.close();
                            }
                        } // end while IdxRs
                        IdxRs.close();
                        IdxRs = null;
                    } // end if (mIsExst!=1)
                } // end while rs.next()
                rs.close();
                rs = null;
                strSQL = "INSERT INTO table_part(data_month) VALUES(" + DateNextMonth + ")";
                Global.ExecuteSQL(pConnection, strSQL);
                pConnection.commit();
                writeLogFile(" - Created new Partition for system successfully.");
            }
        }
        catch (Exception e)
        {
            pConnection.rollback();
            writeLogFile(" - " + e.toString());
        }
        finally
        {
            NewParName = null;
            // PriorParName = null;
            NextParName = null;
            strSQL = null;
            DateNextMonth = null;

            fmt = null;
            stmt.close();
            stmt = null;
            stmtIdxPartRs.close();
            stmtIdxPartRs = null;
            stmtIdxRs.close();
            stmtIdxRs = null;
        }
    }

    public static void addNewSMS(Connection pConnection, int pId, int psmsType, String pcontent) throws SQLException
    {
        CallableStatement cs = null;
        ResultSet rs = null;
        try
        {
            pConnection.setAutoCommit(false);

            cs = pConnection.prepareCall("{call sms_pg.addNewSMS(?,?,?)}");
            cs.setInt(1, pId);
            cs.setInt(2, psmsType);
            cs.setString(3, pcontent);
            rs = cs.executeQuery();
            pConnection.commit();
        }
        catch (SQLException ex)
        {
            throw ex;
        }
        finally
        {
            if (rs != null)
            {
                rs.close();
            }
            if (cs != null)
            {
                cs.close();
            }
            rs = null;
            cs = null;
        }
    }

    public static void SendMail(Connection pConnection, int pClassType, String pRecipient, String pContent) throws Exception
    {
        String mSQL = "SELECT max(nvl(batch_id,0)) INTO ? FROM smtp_batch where status=0 and class_type_id=" + pClassType;
        int i = 0;
        try
        {
            String sql = "begin " + mSQL + "; end;";
            CallableStatement cs = pConnection.prepareCall(sql);
            try
            {
                cs.registerOutParameter(1, Types.VARCHAR);
                cs.execute();
                i = cs.getInt(1);
            }
            catch (SQLException ex)
            {
                switch (ex.getErrorCode())
                {
                case 1403:
                    i = 0;
                    break;
                default:
                    throw ex;
                }
            }
            finally
            {
                try
                {
                    cs.close();
                    cs = null;
                }
                catch (Exception e)
                {
                }
            }

            if (pConnection.getAutoCommit())
            {
                pConnection.setAutoCommit(false);
            }
            if (i > 0)
            {
                mSQL = "SELECT to_char(process_date,'dd/MM/yyyy hh24:mi:ss') INTO ? FROM smtp_batch where batch_id=" + i + " and status= 0 and class_type_id = " + pClassType;
                String processDate = ExecuteOutParameterStr(pConnection, mSQL);
                mSQL = "UPDATE smtp_batch SET recipient='" + Global.nvl(pRecipient, "") + "' where status=0 and class_type_id=" + pClassType;
                Global.ExecuteSQL(pConnection, mSQL);
                mSQL = "insert into smtp_batch_detail(batch_id,content,process_date) values(" + i + ",'" + pContent + "',to_date('" + processDate + "','dd/MM/yyyy hh24:mi:ss'))";
                Global.ExecuteSQL(pConnection, mSQL);
            }
            else
            {
                mSQL = "SELECT seq_smtp_batch.NEXTVAL INTO ? FROM dual";
                i = ExecuteOutParameterInt(pConnection, mSQL);
                mSQL = "insert into smtp_batch(batch_id,process_date,recipient,status,class_type_id) values(" + i + ",sysdate,'" + Global.nvl(pRecipient, "") + "',0," + pClassType + ")";
                Global.ExecuteSQL(pConnection, mSQL);
                mSQL = "SELECT to_char(process_date,'dd/MM/yyyy hh24:mi:ss') INTO ? FROM smtp_batch where batch_id=" + i + " and status= 0 and class_type_id = " + pClassType;
                String processDate = ExecuteOutParameterStr(pConnection, mSQL);

                mSQL = "insert into smtp_batch_detail(batch_id,content,process_date) values(" + i + ",'" + pContent + "',to_date('" + processDate + "','dd/MM/yyyy hh24:mi:ss'))";
                Global.ExecuteSQL(pConnection, mSQL);
            }
            pConnection.commit();
        }
        catch (Exception e)
        {
            throw e;
        }
    }

    public void SendSmtpMailWithAttachment(Connection pConnection) throws Exception
    {
        writeLogFile("- Sending mail report is starting...");
        SimpleDateFormat sdf = new SimpleDateFormat("HH");
        String mCurrentDateTime = sdf.format(new java.util.Date());
        Calendar now = Calendar.getInstance();
        if (now.get(Calendar.DAY_OF_YEAR) != cdrfileParam.dayCDRReport)
        {
            cdrfileParam.isReportedInToday = false;
        }
        if (cdrfileParam.isReportedInToday == true)
        {
            return;
        }

        String mSQL = "select b.ptr_name,ptr_value " +
            "from sys_param a,sys_param_detail b " +
            "where a.id=b.ptr_id and a.ptr_name='MailCDRREport'";
        Statement stmt = pConnection.createStatement();
        ResultSet rs = stmt.executeQuery(mSQL);
        String mailhost = "127.0.0.1"; // or another mail host
        String from = "CDRFILE System Mail Message<MailMessage@cdrfile.vms.com.vn>";
        String to = "hungdt@vms.com.vn";
        String textBody = "Report cdr file";
        String strReportFilePath = "/u02/oracle/";
        int Port = 25;
        String mTimeSendMailMessage = "";
        String MailSubject = "CDR File System Mail Alert";

        try
        {
            while (rs.next())
            {
                if (rs.getString("ptr_name").compareTo("Report_MailHost") == 0)
                {
                    mailhost = rs.getString("ptr_value");
                }
                if (rs.getString("ptr_name").compareTo("Report_Port") == 0)
                {
                    Port = rs.getInt("ptr_value");
                }
                if (rs.getString("ptr_name").compareTo("Report_MailFrom") == 0)
                {
                    from = rs.getString("ptr_value");
                }
                if (rs.getString("ptr_name").compareTo("RePort_MailSubject") == 0)
                {
                    MailSubject = rs.getString("ptr_value");
                }
                if (rs.getString("ptr_name").compareTo("Report_MailTo") == 0)
                {
                    to = rs.getString("ptr_value");
                }
                if (rs.getString("ptr_name").compareTo("Report_TimeSendMailMessage") == 0)
                {
                    mTimeSendMailMessage = rs.getString("ptr_value");
                }
                if (rs.getString("ptr_name").compareTo("Report_Text_Body") == 0)
                {
                    textBody = rs.getString("ptr_value");
                }
                if (rs.getString("ptr_name").compareTo("Report_File_Path") == 0)
                {
                    strReportFilePath = rs.getString("ptr_value");
                }
            }
            if (mTimeSendMailMessage.indexOf(mCurrentDateTime) >= 0)
            {
                if (!exportDataReportToExcel(pConnection))
                    return;
                sendMailAttachment(mailhost, Port, to, from, MailSubject, textBody, strReportFilePath);
                cdrfileParam.isReportedInToday = true;
                writeLogFile("- Sending mail altert is finished...");
            }

        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            rs.close();
            rs = null;
            stmt.close();
            stmt = null;
            mSQL = "";
        }
    }

    private boolean exportDataReportToExcel(Connection pConnection)
    {
        CallableStatement cs = null;
        try
        {
            cs = pConnection.prepareCall("{call report_cdrfile_daily}");
            cs.execute();
            return true;
        }
        catch (Exception ex)
        {
            return false;
        }
        finally
        {
            try
            {
                cs.close();
            }
            catch (SQLException ex1)
            {
            }
            cs = null;
        }
    }
    /**
     * sendMailAttachment
     *
     * @param mailhost String
     * @param Port int
     * @param to String
     * @param from String
     * @param MailSubject String
     * @param textBody String
     * @param strReportFilePath String
     */
    private void sendMailAttachment(String mailhost, int Port, String to, String from, String MailSubject, String textBody, String strReportFilePath) throws MessagingException
    {
        Properties props = System.getProperties();
        props.put("mail.smtp.host", mailhost);
        props.put("mail.smtp.port", Port);
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props);
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        msg.setSubject(MailSubject);
        msg.setText(textBody);

        msg.setHeader("X-Mailer", "LOTONtechEmail");
        msg.setSentDate(new Date());
        MimeBodyPart attachmentPart = new MimeBodyPart();
        FileDataSource fileDataSource = new FileDataSource(strReportFilePath)
        {
            @Override public String getContentType()
            {
                return "application/octet-stream";
            }
        };
        attachmentPart.setDataHandler(new DataHandler(fileDataSource));
        attachmentPart.setFileName(strReportFilePath);
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(attachmentPart);
        msg.setContent(multipart);
        msg.saveChanges();

        Transport.send(msg);
        writeLogFile("- Sending mail cdr file report is complete...");
    }


    public static void main(String args[])
    {
        /*
         * Connection cn = null; Statement stmt = null; ResultSet rs = null;
         */
        General works = new General("1", "2", "3");
        try
        {
            // cn = ClientUtil.openNewConnection();
            works.SendSmtpMailWithAttachment(ClientUtil.openNewConnection());
        }
        catch (Exception de)
        {
            de.printStackTrace();
        }
        finally
        {
            try
            {
                works = null;
                /*
                 * rs.close(); rs = null; stmt.close(); stmt = null; cn.close();
                 */
            }
            catch (Exception e)
            {
            }
        }
    }
}
