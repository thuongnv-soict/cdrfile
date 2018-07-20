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

public class TextBackupUtil extends Global
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

    public TextBackupUtil(String pmstrThreadID, String pmstrThreadName, String pmstrLogPathFileName)
    {
        mstrThreadID = pmstrThreadID;
        mstrThreadName = pmstrThreadName;
        mstrLogPathFileName = pmstrLogPathFileName;
    }

    public void backup(Connection pConnection, int pZipID, String pDescription, String pZipInfo, String pSourceZip, String pDestinationZip, int pSplitZipBackup, int pLocalSplitFileByDay, String pLastDateBackup, String pFileNameLastBackup, String mail_to)
    {
        try
        {
            IOUtil.forceFolderExist(pDestinationZip);
            copy(pConnection, pDescription, pZipID, pSourceZip, pDestinationZip, pSplitZipBackup, pZipInfo, pLastDateBackup, pFileNameLastBackup);
        }
        catch (Exception e)
        {
            writeLogFile(" - " + e.toString());
        }
    }


    private void copy(Connection pConnection, String pDescription, int pZipID, String pSourceZip, String pDestinationZip, int pSplitZipBackup, String pZipInfo, String pLastDateBackup, String pFileNameLastBackup) throws Exception
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
                            mSQL = "update data_param set zip_backup_info_text='" + sdf.format(new java.util.Date(myFile.lastModified())) + "',file_name_last_backup_text='" + myFile.getName() + "' where id=" + pZipID;
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
            String currDate = Format(new java.util.Date(), "dd/MM/yyyy HH:mm:ss");
            writeLogFile(src + " Noi dung da thay doi, File moi duoc backup lai vao ngay : " + currDate);
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

    private String split(String str)
    {
        String ret = "";
        if (str != null)
        {
            int cross = str.lastIndexOf("/");
            if (cross > 0)
            {
                ret = str.substring(cross + 1);
            }
        }
        return ret;
    }

    private void delete(File file) throws IOException
    {

        if (file.isDirectory())
        {

            //directory is empty, then delete it
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


}
