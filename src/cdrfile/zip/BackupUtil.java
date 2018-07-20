package cdrfile.zip;

/**
 * <p>Title: CDR File(s) System</p>
 * <p>Description: VMS IS Departerment</p>
 * <p>Copyright: Copyright (c)  by eKnowledge 2005</p>
 * <p>Company: VietNam Mobile Telecom Services</p>
 * @author eKnowledge - Software
 * @version 1.0
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import cdrfile.global.Global;
import cdrfile.global.IOUtils;
import cdrfile.global.LastModifiedFileComparator;
import cdrfile.general.*;
import java.io.InputStream;
import java.sql.Statement;
import java.sql.ResultSet;

public class BackupUtil extends Global
{

    String mstrThreadID;
    String mstrThreadName;
    String mstrLogPathFileName;
    IOUtils IOUtil = new IOUtils();
    Map archiveQue = null;
    // String archiveLocation = null;
    String fileSeparator = null;

    // options
    // boolean doFolderMode = false;
    boolean doRecursion = true;
    int compression = 9;
    int recursionLevel = 0;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    public BackupUtil(String pmstrThreadID, String pmstrThreadName, String pmstrLogPathFileName)
    {
        mstrThreadID = pmstrThreadID;
        mstrThreadName = pmstrThreadName;
        mstrLogPathFileName = pmstrLogPathFileName;
    }

    public void backupUtil(Connection pConnection, int pZipID, String pDescription, String pZipInfo, String pSourceZip, String pDestinationZip, int pSplitZipBackup, int pLocalSplitFileByDay, String pLastDateBackup, String pFileNameLastBackup, int pSqlupdatetype)
    {
        try
        {
            IOUtil.forceFolderExist(pDestinationZip);
            copy(pConnection, pDescription, pZipID, pSourceZip, pDestinationZip, pSplitZipBackup, pZipInfo, pLastDateBackup, pFileNameLastBackup, pSqlupdatetype);
        }
        catch (Exception e)
        {
            writeLogFile(" - " + e.toString());
        }
    }


    private void copy(Connection pConnection, String pDescription, int pZipID, String pSourceZip, String pDestinationZip, int pSplitZipBackup, String pZipInfo, String pLastDateBackup, String pFileNameLastBackup, int pSqlupdatetype) throws Exception
    {
        SimpleDateFormat sdfMonth = new SimpleDateFormat("yyyyMM");
        File myFolder = null;
        File myFile = null;
        String mSQL = null;
        int mRet = 0;
        boolean FirstTime = true;
        myFolder = new File(pSourceZip);
        if (myFolder.exists())
        {
            pConnection.setAutoCommit(false);
            File[] files = myFolder.listFiles();
            Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
            List fileList = Arrays.asList(files);

            //Copy.
            FirstTime = true;
            for (Iterator its = fileList.iterator(); its.hasNext(); )
            {
                myFile = (File) its.next();
                if (((Long.parseLong(sdf.format(new java.util.Date(myFile.lastModified()))) > Long.parseLong(pZipInfo)) && (Long.parseLong(sdf.format(new java.util.Date(myFile.lastModified()))) < Long.parseLong(pLastDateBackup)))
                    || ((Long.parseLong(sdf.format(new java.util.Date(myFile.lastModified()))) == Long.parseLong(pZipInfo)) && (Long.parseLong(sdf.format(new java.util.Date(myFile.lastModified()))) < Long.parseLong(pLastDateBackup)) && (pFileNameLastBackup.toLowerCase().compareTo(myFile.getName().toLowerCase()) < 0)))
                {
                    if (FirstTime)
                    {
                        writeLogFile("- Copy Backup CDR File of " + pDescription + " - " + pSourceZip);
                        FirstTime = false;
                    }
                    writeLogFile("   . " + myFile.getName());
                    if (pSplitZipBackup == 1)
                    {
                        IOUtil.forceFolderExist(IOUtil.FillPath(pDestinationZip, Global.mSeparate) + sdfMonth.format(new java.util.Date(myFile.lastModified())));
                        mRet = cop(myFile.toString(), IOUtil.FillPath(pDestinationZip, Global.mSeparate) + sdfMonth.format(new java.util.Date(myFile.lastModified())) + "/" + myFile.getName());
                    }
                    else
                    {
                        mRet = cop(myFile.toString(), IOUtil.FillPath(pDestinationZip, Global.mSeparate) + sdfMonth.format(new java.util.Date(myFile.lastModified())));
                    }
                    if (mRet == 0)
                    {
                        try
                        {

                            if (pSqlupdatetype == 1)
                            {
                                mSQL = "update data_param set zip_backup_info='" + sdf.format(new java.util.Date(myFile.lastModified())) + "',file_name_last_backup='" + myFile.getName() + "' where id=" + pZipID;
                            }
                            else
                            {
                                mSQL = "update data_param set zip_backup_info_text='" + sdf.format(new java.util.Date(myFile.lastModified())) + "',file_name_last_backup_text='" + myFile.getName() + "' where id=" + pZipID;
                            }
                            Global.ExecuteSQL(pConnection, mSQL);
                            pConnection.commit();
                        }
                        catch (Exception e)
                        {
                            throw e;
                        }
                    }

                }
            }
            if (FirstTime == false)
            {
                writeLogFile("- Copy Backup CDR File in " + pDescription + " successfully.");
                FirstTime = false;
            }
        }
    }

    public void writeLogFile(String pStrLog)
    {
        super.setThreadID(mstrThreadID);
        super.setThreadName(mstrThreadName);
        super.setLogPathFileName(mstrLogPathFileName);
        super.writeLogFile(pStrLog);
    }

    private int cop(String src, String dest) throws IOException
    {

        File source = new File(src);
        File destination = new File(dest);

        if (IOUtil.checkFileExist(dest))
        {
            delete(destination);
            writeLogFile(src + " Noi dung da thay doi, File moi duoc backup lai.");
        }

        copyDirectory(source, destination);
        return 0;
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

    private void delete(File file) throws IOException
    {

        if (file.isDirectory())
        {
            if (file.list().length == 0)
            {
                file.delete();
            }
            else
            {
                String files[] = file.list();

                for (String temp : files)
                {
                    File fileDelete = new File(file, temp);
                    delete(fileDelete);
                }

                if (file.list().length == 0)
                {
                    file.delete();
                }
            }
        }
        else
        {
            file.delete();
        }
    }

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
                //can  del.
                writeLogFile(rs.getString("local_putfile_dir"));
                writeLogFile(rs.getString("last_time_delete"));
                writeLogFile(Integer.toString(rs.getInt("DELETE_CDRFILE_AFTER_DAYS")));
                if (mRet > 10)
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
                // log
                if (mRet > 10)
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
