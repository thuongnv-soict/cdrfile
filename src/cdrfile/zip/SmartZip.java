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
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import cdrfile.global.Global;
import cdrfile.global.IOUtils;
import cdrfile.general.*;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class SmartZip extends Global
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
    int compression = 8;
    int recursionLevel = 0;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

    public SmartZip()
    {
    };

    public SmartZip(String pmstrThreadID, String pmstrThreadName, String pmstrLogPathFileName)
    {
        mstrThreadID = pmstrThreadID;
        mstrThreadName = pmstrThreadName;
        mstrLogPathFileName = pmstrLogPathFileName;
    }

    public void Zip(Connection pConnection, int pZipID, String pDescription, String pZipInfo, String pSourceZip, String pDestinationZip, int pSplitZipBackup, int pLocalSplitFileByDay, String pLastDateBackup, String pFileNameLastBackup, String mail_to)
    {

        IOUtil.forceFolderExist(pDestinationZip);

        List scriptEntrys = null;
        if (pLocalSplitFileByDay == 1)
        {
            doRecursion = true;
        }
        else
        {
            doRecursion = false;
        }
        scriptEntrys = new ArrayList();
        scriptEntrys.add(pSourceZip);
        if (scriptEntrys == null)
        {
            return;
        }

        try
        {
            setupArchiveMap(pConnection, pDescription, pZipID, pSourceZip, pDestinationZip, pSplitZipBackup, scriptEntrys, pZipInfo, pLastDateBackup, pFileNameLastBackup, mail_to);
        }
        catch (Exception e)
        {
            writeLogFile(" - " + e.toString());
        }
        finally
        {
            scriptEntrys = null;
        }
    }

    private void setupArchiveMap(Connection pConnection, String pDescription, int pZipID, String pSourceZip, String pDestinationZip, int pSplitZipBackup, List archiveFolders, String pZipInfo, String pLastDateBackup, String pFileNameLastBackup, String mail_to) throws Exception
    {
        SimpleDateFormat sdfMonth = new SimpleDateFormat("yyyyMM");
        File myFolder = null;
        File myFile = null;
        String mSQL = null;
        int mRet = 0;
        boolean FirstTime = true;
        General works = new General(mstrThreadID, mstrThreadName, mstrLogPathFileName);
        for (Iterator it = archiveFolders.iterator(); it.hasNext(); )
        {
            myFolder = new File((String) it.next());
            if (myFolder.exists())
            {
                pConnection.setAutoCommit(false);
                File[] files = myFolder.listFiles();
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
                            writeLogFile("- Zipping Backup CDR File of " + pDescription + " - " + pSourceZip);
                            FirstTime = false;
                        }
                        archiveQue = null;
                        archiveQue = new HashMap();
                        recursionLevel = 0;
                        addToArchiveQue(myFile);
                        if (pSplitZipBackup == 1)
                        {
                            writeLogFile("   . " + myFile.getName());
                            IOUtil.forceFolderExist(IOUtil.FillPath(pDestinationZip, Global.mSeparate) + sdfMonth.format(new java.util.Date(myFile.lastModified())));
                            mRet = Zip(pConnection, pZipID, IOUtil.FillPath(pDestinationZip, Global.mSeparate) + sdfMonth.format(new java.util.Date(myFile.lastModified())) + "/" + myFile.getName(), works, mail_to);
                        }
                        else
                        {
                            writeLogFile("   . " + myFile.getName());
                            mRet = Zip(pConnection, pZipID, IOUtil.FillPath(pDestinationZip, Global.mSeparate) + myFile.getName(), works, mail_to);
                        }
                        if (mRet == 0)
                        {
                            try
                            {
                                mSQL = "update data_param set zip_backup_info='" + sdf.format(new java.util.Date(myFile.lastModified())) + "',file_name_last_backup='" + myFile.getName() + "' where id=" + pZipID;
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
                    writeLogFile("- Zip Backup CDR File in " + pDescription + " successfully.");
                    FirstTime = false;
                }
            }
        }
    }

    private void addToArchiveQue(File file)
    {
        recursionLevel++;
        if (file.isDirectory())
        {
            if (doRecursion || recursionLevel < 2)
            {
                File[] files = file.listFiles();
                List fileList = Arrays.asList(files);
                for (Iterator it = fileList.iterator(); it.hasNext(); )
                {
                    addToArchiveQue((File) it.next());
                }
            }
        }
        else
        {
            String fileName = file.getAbsolutePath();
            String entryName = convertToEntryName(fileName);
            archiveQue.put(entryName, fileName);
        }
    }

    private String convertToEntryName(String filename)
    {
        int dirColon = filename.indexOf(":");
        if (dirColon != -1)
        {
            filename = filename.substring(dirColon + 2);
        }
        filename = filename.replace('\\', '/');
        return filename;
    }

    //* Nang cap pha 6*//
    private int Zip(Connection pConnection, int pZipID, String pDestinationZip, General works, String mail_to) throws IOException
    {
        Map.Entry entry = null;
        String currentFileName = null;
        String currentEntryName = null;
        ZipOutputStream zipStream = null;
        if (IOUtil.checkFileExist(pDestinationZip + ".zip"))
        {
            boolean isDiff = true;
            if (doRecursion || recursionLevel == 1)
            {
                String[] fileNames = new String[archiveQue.size()];
                int i = 0;
                for (Iterator it = archiveQue.entrySet().iterator(); it.hasNext(); )
                {
                    entry = (Map.Entry) it.next();
                    fileNames[i] = (String) entry.getValue();
                    i++;
                }
                isDiff = IOUtils.isDiff(fileNames, pDestinationZip + ".zip");
            }
            else
            {
                for (Iterator it = archiveQue.entrySet().iterator(); it.hasNext(); )
                {
                    entry = (Map.Entry) it.next();
                    currentFileName = (String) entry.getValue();
                }
                isDiff = IOUtils.isDiff(currentFileName, pDestinationZip + ".zip");
            }
            if (isDiff)
            {
                //Lay ngay he thong
                java.util.Calendar calendar = java.util.Calendar.getInstance();
                java.util.Date date = calendar.getTime();
                String currDate = Global.Format(date, "yyyyMMdd");

                String newbackupfilename = pDestinationZip + "_" + currDate + ".zip";
                zipStream = new ZipOutputStream(new FileOutputStream(newbackupfilename));
                //Cap nhat thong tin vao bang SMTP_BATCH_DETAIL
                try
                {
                    works.SendMail(pConnection, 1, mail_to, "- Du lieu file goc thay doi, tep backup moi : " + newbackupfilename);
                    /*General.addNewSMS(pConnection,pZipID, 7,
                     "Du lieu file goc thay doi, tep backup moi: " + newbackupfilename);*/

                    //Gui mail thong bao
                    works.SendSmtpMail(pConnection);
                }
                catch (Exception ex)
                {
                    writeLogFile("   .Loi gui mail thong bao du lieu goc thay doi khi backup: " + ex.getMessage());
                }
                writeLogFile("   .Du lieu file goc thay doi, tep backup moi : " + newbackupfilename);
                //----------------------------------------------------//

            }
            else
            {
                IOUtil.deleteFile(pDestinationZip);
                zipStream = new ZipOutputStream(new FileOutputStream(pDestinationZip + ".zip"));
            }
        }
        else
        {
            zipStream = new ZipOutputStream(new FileOutputStream(pDestinationZip + ".zip"));
        }
        zipStream.setLevel(compression);

        // int fileCount = archiveQue.size();

        FileInputStream fis = null;
        byte[] buffer = new byte[1048576];
        int readLength = 0;
        try
        {
            for (Iterator it = archiveQue.entrySet().iterator(); it.hasNext(); )
            {
                entry = (Map.Entry) it.next();
                currentEntryName = (String) entry.getKey().toString().substring(entry.getKey().toString().lastIndexOf("/") + 1);
                currentFileName = (String) entry.getValue();
                File currFile = new File(currentFileName);
                if (currFile.isDirectory())
                {
                    continue; //Ignore directory
                }
                zipStream.putNextEntry(new ZipEntry(currentEntryName));
                fis = new FileInputStream(currFile);

                for (int i = 0; ; i++)
                {
                    readLength = fis.read(buffer);
                    if (readLength < 0)
                    {
                        break;
                    }
                    zipStream.write(buffer, 0, readLength);
                }
                fis.close();
                zipStream.closeEntry();
            }
        }
        catch (IOException e)
        {
            throw e;
        }
        finally
        {
            try
            {
                zipStream.close();
            }
            catch (Exception e)
            {
            }
        }
        return 0;
    }

    public void writeLogFile(String pStrLog)
    {
        super.setThreadID(mstrThreadID);
        super.setThreadName(mstrThreadName);
        super.setLogPathFileName(mstrLogPathFileName);
        super.writeLogFile(pStrLog);
    }

    private void Zip(String[] inFileNames, String outFileName, boolean includePath) throws IOException
    {
        // Create a buffer for reading the files
        byte[] buf = new byte[1048576];

        try
        {
            // Create the ZIP file
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFileName));
            out.setLevel(9);
            // Compress the files
            for (int i = 0; i < inFileNames.length; i++)
            {
                FileInputStream in = new FileInputStream(inFileNames[i]);

                // Add ZIP entry to output stream.
                if (includePath)
                {
                    String strFileName = inFileNames[i].substring(inFileNames[i].lastIndexOf(":/") + 2);
                    out.putNextEntry(new ZipEntry(strFileName));
                }
                else
                {
                    String strFileName = inFileNames[i].substring(inFileNames[i].lastIndexOf("/") + 1);
                    out.putNextEntry(new ZipEntry(strFileName));
                }

                // Transfer bytes from the file to the ZIP file
                int len;
                while ((len = in.read(buf)) > 0)
                {
                    out.write(buf, 0, len);
                }

                // Complete the entry
                out.closeEntry();
                in.close();
            }

            // Complete the ZIP file
            out.close();
        }
        catch (IOException e)
        {
            throw e;
        }
    }

    public void Zip(String strSource, String outFileName, boolean includePath) throws IOException
    {
        String strTemp = strSource + ",";
        int intNumOfItem = 0, i = 0;
        while (i < strTemp.length())
        {
            if (strTemp.charAt(i) == ',')
            {
                intNumOfItem++;
            }
            i++;
        }
        String arrInFile[] = new String[intNumOfItem];
        i = 0;
        while (strTemp.length() > 1)
        {
            arrInFile[i] = strTemp.substring(0, strTemp.indexOf(","));
            strTemp = strTemp.substring(strTemp.indexOf(",") + 1);
            i++;
        }
        try
        {
            Zip(arrInFile, outFileName, includePath);
        }
        catch (IOException e)
        {
            throw e;
        }
    }

    public void ZipFile(String strSource, String strDestination) throws IOException
    {
        // Create a buffer for reading the files
        byte[] buf = new byte[1048576];

        try
        {
            FileInputStream in = new FileInputStream(strSource);
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(strDestination));
            out.setLevel(9);
            out.putNextEntry(new ZipEntry(strSource.substring(strSource.lastIndexOf("/") + 1)));
            // Compress the files
            int iReadCount = 0;
            while ((iReadCount = in.read(buf)) >= 0)
            {
                out.write(buf, 0, iReadCount);
            }

            // Complete the ZIP file
            in.close();
            out.close();
        }
        catch (IOException e)
        {
            throw e;
        }
    }

    public void GUnZipFile(String strSource, String strDestination) throws IOException
    {
        // Create a buffer for reading the files
        GZIPInputStream zipin = null;
        FileInputStream in = null;
        try
        {
            in = new FileInputStream(strSource);
            zipin = new GZIPInputStream(in);
        }
        catch (IOException e)
        {
            System.out.println("Couldn't open " + strSource + ".");
            return;
        }
        byte[] buffer = new byte[1048576];
        // decompress the file
        try
        {
            FileOutputStream out = new FileOutputStream(strDestination);
            int length;
            while ((length = zipin.read(buffer, 0, 1048576)) != -1)
            {
                out.write(buffer, 0, length);
            }
            out.close();
        }
        catch (IOException e)
        {
            System.out.println("Couldn't decompress " + strDestination + ".");
        }
        try
        {
            zipin.close();
            in.close();
        }
        catch (IOException e)
        {
        }
    }


    public void UnZipFile(String strSource, String strDestination) throws IOException
    {
        // Create a buffer for reading the files
        byte[] buf = new byte[1048576];
        ZipInputStream zipin;
        try
        {
            FileInputStream in = new FileInputStream(strSource);
            zipin = new ZipInputStream(in);
        }
        catch (IOException e)
        {
            throw new IOException("Could not open " + strSource);
        }

        try
        {
            FileOutputStream out = new FileOutputStream(strDestination);
            // DeCompress the files
            int iReadCount = 0;
            zipin.getNextEntry();
            while ((iReadCount = zipin.read(buf)) >= 0)
            {
                out.write(buf, 0, iReadCount);
            }
            out.close();
        }
        catch (IOException e)
        {
            throw new IOException("Could not decompress " + strDestination);
        }
        catch (Exception ex)
        {
            System.out.print("Unknow err ");
        }
        try
        {
            zipin.close();
        }
        catch (IOException e)
        {
        }
    }

    public void UnZip(String inZipFile, String outFolder) throws IOException, ClassCastException
    {
        if (!outFolder.substring(outFolder.length() - 1).equals("/"))
        {
            outFolder += "/";
        }

        ZipInputStream in = null;
        ZipFile zf = null;
        try
        {
            in = new ZipInputStream(new FileInputStream(inZipFile));
            zf = new ZipFile(inZipFile);

            // Get the first entry
            Enumeration entries = zf.entries();

            while (entries.hasMoreElements())
            {
                // Get the entry name

                ZipEntry ze = (ZipEntry) entries.nextElement();

                if (ze.isDirectory())
                {
                    in.getNextEntry();
                    File dirCreate = new File(outFolder + ze.getName());
                    if (!dirCreate.exists())
                    {
                        dirCreate.mkdir();
                    }
                }
                else
                {
                    String outFilename = (ze.getName());
                    // Open the output file
                    outFilename = outFolder + outFilename;
                    OutputStream out = new FileOutputStream(outFilename);
                    // ZipEntry entry = in.getNextEntry();
                    in.getNextEntry();
                    // Transfer bytes from the ZIP file to the output file
                    byte[] buf = new byte[1048576];
                    int len;
                    while ((len = in.read(buf)) > 0)
                    {
                        out.write(buf, 0, len);
                    }

                    // Close the streams
                    out.close();
                }
            }
        }
        catch (ClassCastException e)
        {
            throw e;
        }
        catch (IOException e)
        {
            throw e;
        }
        finally
        {
            if (in != null)
            {
                in.close();
            }
            if (zf != null)
            {
                zf.close();
            }
        }
    }

    public  void GZip(String strSource, String strDestination) throws IOException
    {
        // Create a buffer for reading the files
        byte[] buf = new byte[1048576];

        try
        {
            FileInputStream in = new FileInputStream(strSource);
            GZIPOutputStream out = new GZIPOutputStream(new FileOutputStream(strDestination));
            // Compress the files
            int iReadCount = 0;
            while ((iReadCount = in.read(buf)) >= 0)
            {
                out.write(buf, 0, iReadCount);
            }

            // Complete the ZIP file
            in.close();
            out.close();
        }
        catch (IOException e)
        {
            throw e;
        }
    }

    public void GUnZip(String strSource, String strDestination) throws IOException
    {
        // Create a buffer for reading the files
        byte[] buf = new byte[1048576];

        try
        {
            GZIPInputStream in = new GZIPInputStream(new FileInputStream(strSource));
            FileOutputStream out = new FileOutputStream(strDestination);

            // DeCompress the files
            int iReadCount = 0;
            while ((iReadCount = in.read(buf)) >= 0)
            {
                out.write(buf, 0, iReadCount);
            }

            in.close();
            out.close();
        }
        catch (IOException e)
        {
            throw e;
        }
    }


}
