package cdrfile.thread;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import cdrfile.global.Global;
import cdrfile.global.IOUtils;
import ftp.FTPClient;
import ftp.FTPFile;
import java.io.File;
import cdrfile.general.General;
/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class CheckUploadThread extends ThreadInfo
{
    public void finalize()
    {
        destroy();
        System.runFinalization();
        System.gc();
    }

    protected void processSession() throws Exception
    {
        writeLogFile("Check file thread is starting.");
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DAY_OF_YEAR, -1);
        IOUtils IOUtil = new IOUtils();
        FTPClient pFtp =null;

        String mSQL = "select * from data_param " +
            "where remote_putfile_dir is not null " +
            "and local_putfile_dir is not null " +
            "and used_getfile = 1 and ftp_thread_id is not null " +
            "order by id";
        Statement stmt = mConnection.createStatement();
        ResultSet rs = stmt.executeQuery(mSQL);

        try
        {
            while (rs.next() && miThreadCommand != THREAD_STOP)
            {
                pFtp = new FTPClient(rs.getString("ftp_host_ip"), 21, rs.getInt("time_out"));

                if(rs.getInt("local_split_file_by_day") == 1)
                {
                    String curDir = dateFormat.format(cal.getTime());
                    // dem so luong file trong thu muc local.
                    String strSource = IOUtil.FillPath(rs.getString("local_putfile_dir"), Global.mSeparate) + curDir;
                    File dir = new File(strSource);
                    String[] children = dir.list();
                    int numberLocalFile = children.length;
                    //Dem so luong file trong thu muc remote
                    String strDest = IOUtil.FillPath(rs.getString("remote_putfile_dir"),Global.mSeparate) + curDir;
                    pFtp.chdir(strDest);
                    pFtp.pwd();
                    FTPFile[] listings = pFtp.dirDetails("");
                    int numberRemoteFile = listings.length;
                    if(numberRemoteFile < numberLocalFile)
                    {
                        int numberMissFile = numberLocalFile - numberRemoteFile;
                        //Notice.
                        writeLogFile("Warning: Process for put file into server [" + rs.getString("ftp_host_ip") + "] from local dir ["+ rs.getString("local_putfile_dir") +"] to remote dir ["+rs.getString("remote_putfile_dir")+"] war missed files occur. Number missed file is: " +  numberMissFile + ". Please check again.");
                        //Send SMS.
                        General.addNewSMS(mConnection,rs.getInt("id"), 2, "Warning: Process for put file into server [" + rs.getString("ftp_host_ip") + "] from local dir ["+ rs.getString("local_putfile_dir") +"] to remote dir ["+rs.getString("remote_putfile_dir")+"] war missed files occur. Number missed file is: " +  numberMissFile + ". Please check again.");
                    }
                }
                else
                {

                }
            }
        }
        catch(Exception ex)
        {
            writeLogFile("Error Details: " + ex.toString());
        }
        finally
        {
            pFtp.quit();
            pFtp = null;
        }
    }
}
