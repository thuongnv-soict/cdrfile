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
import cdrfile.zip.BackupUtil;
import cdrfile.global.Global;
import cdrfile.zip.TextBackupUtil;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.io.File;
import java.util.List;
import java.util.Iterator;
import java.sql.Connection;

public class ZipBackupThread extends ThreadInfo
{

    public void finalize()
    {
        destroy();
        System.runFinalization();
        System.gc();
    }

    public void processSession() throws Exception
    {

        String mSQL = "SELECT a.id,a.zip_backup_info,a.local_getfile_dir,";
        mSQL += "a.zip_backup_dir,a.local_split_file_by_day,";
        mSQL += "a.split_zip_backup_by_month,a.note,";
        mSQL += "to_char(sysdate-decode(nvl(a.zip_backup_after_days,0),0,1,";
        mSQL += "a.zip_backup_after_days),'yyyymmddhh24miss') last_date_backup, ";
        mSQL += "a.file_name_last_backup,a.mail_to FROM data_param a,node_cluster b ";
        mSQL += "WHERE a.zip_backup_dir is not null and a.used_getfile=1 ";
        mSQL += "AND nvl(a.zip_backup_after_days,0)>0  ";
        mSQL += "AND a.run_on_node=b.id AND zip_thread_id=" + getThreadID();
        mSQL += " AND b.ip='" + Global.getLocalSvrIP() + "' ORDER BY a.id";

        Statement stmt = mConnection.createStatement();
        ResultSet rs = stmt.executeQuery(mSQL);
        BackupUtil backup = new BackupUtil(getThreadID(), getThreadName(), getLogPathFileName());

        writeLogFile(" --- Backup Bin is running...");
        try
        {
            while (rs.next() && miThreadCommand != THREAD_STOP)
            {
                backup.backupUtil(mConnection, rs.getInt("id"), rs.getString("note"), rs.getString("zip_backup_info"), rs.getString("local_getfile_dir"), rs.getString("zip_backup_dir"), rs.getInt("split_zip_backup_by_month"), rs.getInt("local_split_file_by_day"), rs.getString("last_date_backup"), rs.getString("file_name_last_backup"), 1);
            }
        }
        catch (Exception e)
        {
            writeLogFile(" - " + e.toString());
        }
        ////////////////////////////////////////////////////////////////////

        rs.close();
        rs = null;
        stmt.close();
        stmt = null;
        mSQL = "";
        //////////////////////////

        mSQL = "SELECT a.id,";
        mSQL += "a.zip_backup_info_text,";
        mSQL += "a.local_putfile_dir,";
        mSQL += "a.zip_backup_dir_text,";
        mSQL += "a.local_split_file_by_day,";
        mSQL += "a.split_zip_backup_by_month,";
        mSQL += "a.note,";
        mSQL += "to_char(sysdate-decode(nvl(a.zip_backup_after_days_text,0),0,1,a.zip_backup_after_days_text),'yyyymmddhh24miss') last_date_backup,";
        mSQL += "a.file_name_last_backup_text,";
        mSQL += "a.mail_to";
        mSQL += " FROM data_param a,node_cluster b";
        mSQL += " WHERE a.zip_backup_dir_text is not null";
        mSQL += " AND nvl(a.zip_backup_after_days_text,0)>0 ";
        mSQL += " AND a.used_getfile=1";
        mSQL += " AND a.run_on_node=b.id";
        mSQL += " AND a.zip_thread_id  = " + getThreadID();
        mSQL += " AND b.ip='" + Global.getLocalSvrIP();
        mSQL += "' AND convert_thread_ID is not null";
        mSQL += "  ORDER BY a.id ";

        stmt = mConnection.createStatement();
        rs = stmt.executeQuery(mSQL);
        writeLogFile(" --- Backup Text  is running......");
        try
        {
            while (rs.next() && miThreadCommand != THREAD_STOP)
            {
                backup.backupUtil(mConnection, rs.getInt("id"), rs.getString("note"), rs.getString("zip_backup_info_text"), rs.getString("local_putfile_dir"), rs.getString("zip_backup_dir_text"), rs.getInt("split_zip_backup_by_month"), rs.getInt("local_split_file_by_day"), rs.getString("last_date_backup"), rs.getString("file_name_last_backup_text"), 2);
            }
        }
        catch (Exception e)
        {
            writeLogFile(" - " + e.toString());
        }
        finally
        {
            backup = null;
            rs.close();
            rs = null;
            stmt.close();
            stmt = null;
            mSQL = "";
        }
        //xoa file.

        try
        {

            DeleteCDFile(mConnection);

        }
        catch (Exception e)
        {
            writeLogFile(" - Del" + e.toString());
        }
        writeLogFile(" ---* Finish all *---");
    }

    private void DeleteCDFile(Connection pConnection) throws Exception
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
        mSQL += " to_char((sysdate-DELETE_CDRFILE_AFTER_DAYS),'yyyymmddhh24miss') last_time_delete";
        mSQL += " FROM data_param a,node_cluster b ";
        mSQL += " WHERE a.run_on_node=b.id ";
        mSQL += " AND DELETE_CDRFILE_AFTER_DAYS>7";			// datnh, 2014.11.06: change 30 -> 20
        // ---datnh, 2013.12.17: Do khong backup CDR text
        //mSQL += " AND zip_backup_info_text > to_char((sysdate-DELETE_CDRFILE_AFTER_DAYS),'yyyymmddhh24miss')";
        mSQL += " AND zip_thread_id =" + getThreadID();
        mSQL += " AND b.ip='" + Global.getLocalSvrIP();
        mSQL += "' AND used_getfile=1 and convert_thread_id is not null";

        Statement stmt = pConnection.createStatement();
        ResultSet rs = stmt.executeQuery(mSQL);
        writeLogFile(" --- Delete Text is running.....");
        try
        {
            while (rs.next())
            {
                mRet = rs.getInt("DELETE_CDRFILE_AFTER_DAYS");

                if (mRet > 7)				// datnh, 2014.11.06: change 30 -> 20
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
            mSQL += "  to_char((sysdate-DELETE_CDRFILE_AFTER_DAYS),'yyyymmddhh24miss') last_time_delete ";
            mSQL += " FROM data_param a,node_cluster b ";
            mSQL += " WHERE a.run_on_node=b.id ";
            mSQL += " AND DELETE_CDRFILE_AFTER_DAYS>7";			// datnh, 2014.11.06: change 30 -> 20
            mSQL += " AND zip_backup_info > to_char((sysdate-DELETE_CDRFILE_AFTER_DAYS),'yyyymmddhh24miss')";
            mSQL += " AND zip_thread_id =" + getThreadID();
            mSQL += " AND b.ip='" + Global.getLocalSvrIP();
            mSQL += "' AND used_getfile=1";

            stmt = pConnection.createStatement();
            rs = stmt.executeQuery(mSQL);
            writeLogFile(" --- Delete Bin is running.....");
            while (rs.next())
            {
                mRet = rs.getInt("DELETE_CDRFILE_AFTER_DAYS");

                // log
                if (mRet > 7)					// datnh, 2014.11.06: change 30 -> 20
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


}
