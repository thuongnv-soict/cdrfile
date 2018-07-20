package cdrfile.thread;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.Statement;
import cdrfile.global.Global;

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
public class CopyThread11  extends ThreadInfo
{
    public void finalize()
    {
        destroy();
        System.runFinalization();
        System.gc();
    }

    protected synchronized void processSession() throws Exception
    {
        writeLogFile("Resource file type copy thread is starting.");
        String mSQL = "Select * from data_param a, import_header b" +
            "where a.id = b.ftp_id " +
            "and a.copy_thread_id is not null " +
            "and a.local_putfile_dir is not null " +
            "and status = " + Global.StateCollectTrafficTurnover;

        Statement stmt = mConnection.createStatement();
        ResultSet rs = stmt.executeQuery(mSQL);
        try
        {
            while (rs.next() && miThreadCommand != THREAD_STOP)
            {
                if(!rs.getString("dir").trim().equals(""))
                {
                    copy(rs.getInt("id"), rs.getString("local_putfile_dir"));
                }
            }
        }
        catch (Exception e)
        {
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

    private void copy(int id, String strSourceDir) throws Exception
    {
        String mSQL = "select * from data_param_type where id = " + id;
        Statement stmt = mConnection.createStatement();
        ResultSet rs = stmt.executeQuery(mSQL);
        File sourceDir = null;
        File destDir = null;
        try
        {
            sourceDir =  new File(strSourceDir);
            destDir = new File(rs.getString("dir"));
            while (rs.next() && miThreadCommand != THREAD_STOP)
            {
                copyDirectory(sourceDir, destDir);
               // mSQL = "update data_param set copy_lastfile_time = '" + exportEndTime + "' where id = " + id;
            }
        }
        catch (Exception e)
        {
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

    private void copyDirectory(File srcPath, File dstPath) throws IOException
    {
        if (srcPath.isDirectory())
        {
            if (!dstPath.exists())
            {
                dstPath.mkdir();
            }

            String files[] = srcPath.list();
            for (int i = 0; i < files.length; i++)
            {
                copyDirectory(new File(srcPath, files[i]), new File(dstPath, files[i]));
            }
        }
        else
        {
            if (!srcPath.exists())
            {
                writeLogFile(srcPath + " does not exist.");
            }
            else
            {
                InputStream in = new FileInputStream(srcPath);
                OutputStream out = new FileOutputStream(dstPath);

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0)
                {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            }
        }
    }
}
