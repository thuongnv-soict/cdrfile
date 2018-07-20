package cdrfile.thread;

import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.Statement;
import cdrfile.global.Global;
import cdrfile.global.Global;
import cdrfile.global.IOUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
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
public class CopyThread extends ThreadInfo
{
    public void finalize()
    {
        destroy();
        System.runFinalization();
        System.gc();
    }

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    protected synchronized void processSession() throws Exception
    {

        writeLogFile("Resource file type copy thread is starting.");
        String mSourceFile = null;
        IOUtils IOUtil = new IOUtils();
        String mSQL = "select a.id, b.file_id, b.file_name, a.local_putfile_dir,a.local_split_file_by_day, b.current_dir,a.zip_after_download,a.zip_after_export, " + "to_char(b.time_end_export,'yyyymmddhh24miss') time_end_export " + "from data_param a, import_header b " + "where a.id = b.ftp_id and a.copy_thread_id = " + getThreadID() + " and b.status = "
            + Global.StateCollectTrafficTurnover + " and b.time_end_export > sysdate - 3 and date_createfile > sysdate - 12" + " order by b.time_end_export";

        Statement stmt = mConnection.createStatement();
        ResultSet rs = stmt.executeQuery(mSQL);
        String timeEndExport = "";
        boolean isTrue = false;
        int id = 0;
        try
        {
            while (rs.next() && miThreadCommand != THREAD_STOP)
            {
                String fileName = rs.getString("file_name");
                String mPath = "";
                if (rs.getInt("zip_after_download") == 1)
                {
                    fileName = fileName + ".zip";
                }
                if (rs.getInt("zip_after_export") == 1)
                {
                    fileName = fileName + ".zip";
                }

                timeEndExport = rs.getString("time_end_export");
                id = rs.getInt("id");

                if (rs.getInt("local_split_file_by_day") == 1)
                {
                    mPath = IOUtil.FillPath(rs.getString("local_putfile_dir"), Global.mSeparate) + rs.getString("current_dir") + Global.mSeparate;
                }
                else
                {
                    mPath = IOUtil.FillPath(rs.getString("local_putfile_dir"), Global.mSeparate);
                }

                mSourceFile = mPath + fileName;
                if (copy(id, mSourceFile, rs.getInt("file_id"), fileName, timeEndExport, rs.getString("current_dir")) == 1)
                {
                    isTrue = true;
                }

                /*mSourceFile = IOUtil.FillPath(rs.getString("local_putfile_dir"), Global.mSeparate) + rs.getString("current_dir") + Global.mSeparate + rs.getString("file_name");
                                 if(copy(rs.getInt("id"), mSourceFile + ".zip", rs.getInt("file_id"), rs.getString("file_name"), timeEndExport, rs.getString("current_dir"))== 1)
                                 {
                    isTrue = true;
                                 }*/
            }
            if (isTrue)
            {
                Global.ExecuteSQL(mConnection, "update upload_param set time_lastfile_copy = '" + timeEndExport + "' where data_param_id = " + id);
                writeLogFile("Resource file type copy thread was finished.");
            }
        }
        catch (Exception e)
        {
            General.SendMail(mConnection, 4, "", " - Copy Thread Error: " + e.toString());
            writeLogFile(" - Copy Thread Error: " + e.toString());
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

    private int copy(int id, String strSourceDir, int fileId, String fileName, String timeEndExport, String currDir) throws Exception
    {
        int isSucc = 0;
        long longDate1 = Long.parseLong(timeEndExport);
        boolean isTrue = false;
        String strDestDir = null;
        IOUtils IOUtil = new IOUtils();

        String mSQL = "select dir, nvl(time_lastfile_copy,0) time_lastfile_copy from upload_param where data_param_id = " + id;
        Statement stmt = mConnection.createStatement();
        ResultSet rs = stmt.executeQuery(mSQL);

        long longDate2 = 0;
        try
        {
            while (rs.next() && miThreadCommand != THREAD_STOP)
            {
                longDate2 = Long.parseLong(rs.getString("time_lastfile_copy"));
                if (longDate1 > longDate2)
                {
                    strDestDir = IOUtil.FillPath(rs.getString("dir"), Global.mSeparate) + currDir + Global.mSeparate;
                    IOUtil.forceFolderExist(strDestDir);
                    strDestDir += fileName;
                    if (copyFile(strSourceDir, strDestDir) == 1)
                    {
                        isTrue = true;
                    }
                }
            }
            if (isTrue)
            {
                writeLogFile("File " + fileName + " is Copied.");
                Global.ExecuteSQL(mConnection, "update import_header set status = " + Global.StateCopiedData + " where file_id = " + fileId);
                isSucc = 1;
            }
        }
        catch (Exception e)
        {
            General.SendMail(mConnection, 4, "", " - Can not copy file name is " + fileName + " into " + strDestDir + " folder because " + e.toString());
            writeLogFile(" - Can't copy file name is " + fileName + " into [" + strDestDir + "] folder because " + e.toString());
            isSucc = 0;
        }
        finally
        {
            rs.close();
            rs = null;
            stmt.close();
            stmt = null;
            mSQL = "";
        }
        return isSucc;
    }

    private int copyFile(String sourceDir, String destDir) throws Exception
    {
        int isSucc = 0;
        try
        {
            File f1 = new File(sourceDir);
            File f2 = new File(destDir);
            InputStream in = new FileInputStream(f1);

            OutputStream out = new FileOutputStream(f2);

            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0)
            {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            isSucc = 1;
        }
        catch (Exception ex)
        {
            writeLogFile("Copy file error: " + ex.toString());
            isSucc = 0;
        }
        return isSucc;
    }

    public int copyFile111(String sourceDir, String destDir) throws IOException
    {
        int success = 0;
        InputStream in = null;
        OutputStream out = null;
        try
        {
            File f1 = new File(sourceDir);
            File f2 = new File(destDir);
            in = new BufferedInputStream(new FileInputStream(f1));
            out = new BufferedOutputStream(new FileOutputStream(f2));
            int ch;
            while ((ch = in.read()) != -1)
            {
                out.write(ch);
            }
            out.flush(); // just in case
            success = 1;
        }
        catch (Exception ex)
        {
            writeLogFile("Copy file error: " + ex.toString());
            success = 0;
        }
        finally
        {
            if (out != null)
            {
                try
                {
                    out.close();
                }
                catch (IOException ioe)
                {
                }
            }
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException ioe)
                {
                }
            }
        }
        return success;
    }

}
