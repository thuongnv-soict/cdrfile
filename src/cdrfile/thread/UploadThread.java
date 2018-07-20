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

import cdrfile.global.Global;
import cdrfile.global.IOUtils;
import cdrfile.global.cdrfileParam;
import ftp.FTPClient;
import ftp.FTPConnectMode;
import ftp.FTPException;
import ftp.FTPTransferType;
import java.util.Date;

public class UploadThread extends ThreadInfo
{
    public void finalize()
    {
        destroy();
        System.runFinalization();
        System.gc();
    }

    protected void processSession() throws Exception
    {
        writeLogFile("Upload thread is starting.");
        String mCurrHost = "";
        IOUtils IOUtil = new IOUtils();
        FTPClient ftp = null;

//        String mSQL = "SELECT a.type_name,a.dir,a.ftp_server_ip,a.ftp_server_uid,a.ftp_server_pwd,a.ftp_server_dir, "+
//             "a.transfer_type,nvl(a.last_upload_time,0) last_upload_time ,a.ftp_mode,a.time_connect "+
//             "FROM upload_param a,node_cluster b"+
//             "WHERE isActive=1 " +
//             "AND a.run_on_node=b.id "+
//             "AND upload_thread_id =" + getThreadID() +
//             "AND ftp_server_dir is not null " +
//             "AND dir is not null " +
//             "AND b.ip='" + Global.getLocalSvrIP() +
//             "' ORDER BY a.id";

        String mSQL = "select a.type_name, a.ftp_server_ip, a.ftp_server_uid,a.ftp_server_pwd,a.dir,a.ftp_server_dir, " + "nvl(a.last_upload_time,0),a.transfer_type,a.ftp_mode,b.zip_after_download,b.zip_after_export, b.local_split_file_by_day, c.file_name, c.file_id, c.current_dir " + "from upload_param a, data_param b, import_header c, node_cluster d " + "where a.data_param_id = b.id " + "and b.id = c.ftp_id " + "and a.isactive = 1 " + "AND b.run_on_node = d.id " + "and a.ftp_server_dir is not null " + "and a.dir is not null " + "and d.ip = '"
            + Global.getLocalSvrIP() + "' and a.upload_thread_id = " + getThreadID() + " and c.status = " + Global.StateCopiedData + " and c.time_end_export > sysdate - 3 " + "and date_createfile > sysdate - 3 " + "order by c.time_end_export";
        Statement stmt = mConnection.createStatement();
        ResultSet rs = stmt.executeQuery(mSQL);
        try
        {
            while (rs.next() && miThreadCommand != THREAD_STOP)
            {
                if (!mCurrHost.equalsIgnoreCase(rs.getString("ftp_server_ip")))
                {
                    ftp = new FTPClient(rs.getString("ftp_server_ip"), 21, 60);
                    ftp.login(rs.getString("ftp_server_uid"), rs.getString("ftp_server_pwd"));
                    ftp.setTransferBufferSize(10240);
                    //ftp mode.
                    if (rs.getString("ftp_mode").compareTo("ACTIVE") == 0)
                    {
                        ftp.setConnectMode(FTPConnectMode.ACTIVE);
                    }
                    else
                    {
                        ftp.setConnectMode(FTPConnectMode.PASV);
                    }
                    ///Transfer type.
                    if (rs.getString("transfer_type").compareTo("BINARY") == 0)
                    {
                        ftp.setType(FTPTransferType.BINARY);
                    }
                    else
                    {
                        ftp.setType(FTPTransferType.ASCII);
                    }
                    writeLogFile("Connected to ftp server " + rs.getString("type_name") + "=>" + rs.getString("ftp_server_ip"));
                    mCurrHost = rs.getString("ftp_server_ip");
                }
                String fileName = rs.getString("file_name");
                String strSourPath = "";
                String strDestPath = "";
                if (rs.getInt("zip_after_download") == 1)
                {
                    fileName = fileName + ".zip";
                }
                if (rs.getInt("zip_after_export") == 1)
                {
                    fileName = fileName + ".zip";
                }

                if (rs.getInt("local_split_file_by_day") == 1)
                {
                    strSourPath = IOUtil.FillPath(rs.getString("dir"), Global.mSeparate) + rs.getString("current_dir") + Global.mSeparate;
                    strDestPath = IOUtil.FillPath(rs.getString("ftp_server_dir"), Global.mSeparate)+  rs.getString("current_dir") + Global.mSeparate;
                }
                else
                {
                    strSourPath = IOUtil.FillPath(rs.getString("dir"), Global.mSeparate);
                    strDestPath = IOUtil.FillPath(rs.getString("ftp_server_dir"), Global.mSeparate);
                }
                strSourPath = strSourPath + fileName;
                DoUpLoad(mConnection, ftp, rs.getString("type_name"), rs.getInt("file_id"),fileName, strSourPath,strDestPath );
                writeLogFile("Disconnecting " + rs.getString("type_name") + "...\r\n");
            }
        }
        catch (Exception ex)
        {
            writeLogFile("Error Details: " + ex.toString());
            Global.writeEventThreadErr(Integer.parseInt(getThreadID()), 2, ex.toString());
            throw ex;
        }
        finally
        {
            try
            {
                ftp.quit();
                ftp = null;
                rs.close();
                rs = null;
                stmt.close();
                stmt = null;
            }
            catch (Exception e)
            {
            }
        }
    }

    protected void DoUpLoad(java.sql.Connection pConnection, FTPClient ftp, String pNote, int fileId, String fileName,String sourcePath, String destPath ) throws Exception
    {
        try
        {
            pConnection.setAutoCommit(false);
            Global.ExecuteSQL(pConnection, "alter session set nls_date_format='yyyyMMddhh24miss'");

            try
            {
                ftp.mkdir(destPath);
            }
            catch (FTPException ex)
            {}
            destPath = destPath + fileName;
            ftp.put(sourcePath, destPath);
            String strCmd = "Update Import_header set status = 9 where file_id = " + fileId;
            Global.ExecuteSQL(pConnection, strCmd);
            pConnection.commit();
            writeLogFile("   .Upload file " + fileName + " is successfully.");
        }
        catch (FTPException ex)
        {
            switch (ex.getReplyCode())
            {
            case 530:
                writeLogFile(ex.getReplyCode() + " - Invalid user name or password:" + pNote);
                break;
            case 550:
                writeLogFile(ex.getReplyCode() + " - " + ex.getMessage());
                break;
            default:
                writeLogFile(ex.getReplyCode() + " - " + ex.getMessage());
                break;
            }
            throw ex;
        }
        catch (Exception e)
        {
            writeLogFile(" .Error details of doing FTP: " + e.toString());
            if (cdrfileParam.OnErrorResumeNext.compareTo("TRUE") == 0)
            {
                writeLogFile(" -Unknow Error in upload thread: " + e.toString());
            }
            else
            {
                System.out.println(" - " + e.toString());
                System.err.println(Global.Format(new java.util.Date(), "dd/MM/yyyy HH:mm:ss") + " : - ERROR in module DoFtp : " + e.toString());
                throw e;
            }
        }
    }
}
