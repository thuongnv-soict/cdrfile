package cdrfile.thread;

/**
 * <p>Title: CDR File(s) System</p>
 * <p>Description: VMS IS Departerment</p>
 * <p>Copyright: Copyright (c)  by eKnowledge 2004</p>
 * <p>Company: VietNam Mobile Telecom Services</p>
 * @author eKnowledge - Software
 * @version 1.0
 */

import java.io.File;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;

import cdrfile.general.General;
import cdrfile.global.Global;
import cdrfile.global.IOUtils;
import cdrfile.global.cdrfileParam;
import cdrfile.zip.SmartZip;

//----------------------------------------------------------------------------
//Change History
//2014.10.29 datnh: update LastModified = sysdate
//----------------------------------------------------------------------------
public class ExportThread extends ThreadInfo
{

    public void finalize()
    {
        destroy();
        System.runFinalization();
        System.gc();
    }

    protected synchronized void processSession() throws Exception
    {
        String mFileCharged = null;
        String mFilePathExp = null;
        IOUtils IOUtil = new IOUtils();
        String mSQL = "SELECT b.id,a.file_id,a.file_name,b.file_type,b.center_id," +
            " a.current_dir,b.note,b.local_putfile_dir,b.convert_dir," +
            " nvl(b.split_export_by_day,0) split_export_by_day," +
            " nvl(b.zip_after_export,0) zip_after_export,b.charge_dir," +
            " to_char(date_createfile,'dd/MM/yyyy hh24:mi:ss') date_createfile, " +
            " b.local_split_file_by_day,a.file_size,b.local_getfile_dir," +
            " b.check_header,b.mail_to,d.id file_type_id " +
            " FROM import_header a,data_param b,node_cluster c,file_type d " +
            " WHERE a.status=" + Global.StateRated +
            " AND b.export_thread_id = " + getThreadID() +
            " AND b.run_on_node=c.id AND c.ip='" + Global.getLocalSvrIP() +
            "' AND b.id=a.ftp_id AND b.local_putfile_dir IS NOT NULL " +
            " AND b.file_type =d.file_type" + " ORDER BY a.file_id";
        int pFileID = 0;
        File file = null;
        Statement stmt = mConnection.createStatement();
        ResultSet rsFile = stmt.executeQuery(mSQL);
        SmartZip zip = new SmartZip();

        try
        {
            while (rsFile.next() && miThreadCommand != THREAD_STOP)
            {
                mConnection.setAutoCommit(false);
                pFileID = rsFile.getInt("file_id");
                mSQL = "UPDATE import_header SET time_begin_export=sysdate ";
                mSQL += "WHERE file_id=" + pFileID;
                Global.ExecuteSQL(mConnection, mSQL);
                mConnection.commit();
                if (rsFile.getInt("local_split_file_by_day") == 1)
                {
                    if (cdrfileParam.ChargeCDRFile)
                    {
                        mFileCharged = IOUtil.FillPath(rsFile.getString("charge_dir"), Global.mSeparate) + rsFile.getString("current_dir") + Global.mSeparate + rsFile.getString("file_name");
                    }
                    else
                    {
                        mFileCharged = IOUtil.FillPath(rsFile.getString("convert_dir"), Global.mSeparate) + rsFile.getString("current_dir") + Global.mSeparate + rsFile.getString("file_name");
                    }
                }
                else
                {
                    if (cdrfileParam.ChargeCDRFile)
                    {
                        mFileCharged = IOUtil.FillPath(rsFile.getString("charge_dir"), Global.mSeparate) + rsFile.getString("file_name");
                    }
                    else
                    {
                        mFileCharged = IOUtil.FillPath(rsFile.getString("convert_dir"), Global.mSeparate) + rsFile.getString("file_name");
                    }
                }

                if (rsFile.getInt("split_export_by_day") == 0)
                {
                    mFilePathExp = IOUtil.FillPath(rsFile.getString("local_putfile_dir"), Global.mSeparate);
                }
                else
                {
                    mFilePathExp = IOUtil.FillPath(rsFile.getString("local_putfile_dir"), Global.mSeparate) + rsFile.getString("current_dir");
                }

                /////////
                //Kiem tra check file err- HungDT sua ngay 23/12/2010
                //Chuyen ham check file err tu class convert thread sang export thread
                //Sua lai class zip yeu cau phai khoi tu bien zip truoc khi thuc hien,
                //do goi ham static => kha nang xung dot gia tri trong bien

//                if (rsFile.getInt("check_header") == 1)
//                {
//                    int errorFile = Global.CheckFileErr(mFileCharged, rsFile.getInt("file_type_id"));
//                    if (errorFile == 1)
//                    {
//                        mSQL = "UPDATE import_header SET status=1,note=note||' ";
//                        mSQL += " - File struct error and reconvert : " + rsFile.getString("file_type");
//                        mSQL += "' WHERE file_id=" + rsFile.getInt("file_id");
//                        Global.ExecuteSQL(mConnection, mSQL);
//                        mConnection.commit();
//                        writeLogFile(" - Error structure file " + rsFile.getString("file_name") + " - " + rsFile.getInt("file_id") + " => reconvert");
//                        break;
//                    }
//                }
                //Kiem tra check file err- HungDT sua ngay 23/12/2010


                writeLogFile(" - Exporting file " + rsFile.getString("file_name") + " - " + rsFile.getInt("file_id") + " to " + mFilePathExp);


                IOUtil.forceFolderExist(mFilePathExp);
                mFilePathExp += Global.mSeparate + rsFile.getString("file_name");

                // 2014.10.29 datnh: update LastModified = sysdate
                if (rsFile.getInt("zip_after_export") == 1)
                {
                    zip.Zip(mFileCharged, mFilePathExp + ".zip.tmp", false);
                    file = new File(mFilePathExp + ".zip.tmp");
                    //file.setLastModified(Global.convertDateTimeToLong(rsFile.getString("date_createfile")));
                    file.setLastModified((new Date()).getTime());
                    file.renameTo(new File(mFilePathExp + ".zip"));
                    IOUtil.deleteFile(mFileCharged);
                }
                else
                {
                    IOUtil.renameFile(mFileCharged, mFilePathExp);
                    file = new File(mFilePathExp);
                    //file.setLastModified(Global.convertDateTimeToLong(rsFile.getString("date_createfile")));
                    file.setLastModified((new Date()).getTime());
                    //xoa file.
                }
                writeLogFile(" - Exported data finish.");
                if (cdrfileParam.ChargeCDRFile)
                {
                    Global.ExecuteSQL(mConnection, " UPDATE import_header SET FILE_SIZE_AFTER_CONVERT =" + IOUtil.getFileSize(mFilePathExp + ".zip") + ",time_end_export=sysdate," + " status=" + Global.StateExportedData + " WHERE file_id = " + rsFile.getInt("file_id"));
                }
                else
                {
                    Global.ExecuteSQL(mConnection, " UPDATE import_header SET FILE_SIZE_AFTER_CONVERT =" + IOUtil.getFileSize(mFilePathExp + ".zip") + ", time_end_export=sysdate," + " status=" + Global.StateCollectTrafficTurnover + " WHERE file_id = " + rsFile.getInt("file_id"));
                }

            } // end loop while rsFile
        }
        catch (Exception ex)
        { // end try catch
            Global.writeEventThreadErr(Integer.parseInt(getThreadID()), 2, ex.toString());
            mConnection.rollback();
            General.SendMail(mConnection, 2, rsFile.getString("mail_to"), ex.toString() + rsFile.getString("file_name") + " - " + rsFile.getInt("file_id"));
            /*General.addNewSMS(mConnection,rsFile.getInt("id"), 3,
                  "Eport file "
                  + rsFile.getString("file_name")
                  + " - "
                  + rsFile.getInt("file_id")
                  + " has occured error");*/


            if (cdrfileParam.OnErrorResumeNext.compareTo("TRUE") == 0)
            {
                mSQL = "UPDATE import_header SET status=" + Global.StateExportedDataError + ",note='" + ex.toString() + "' WHERE file_id = " + pFileID;
                Global.ExecuteSQL(mConnection, mSQL);
                writeLogFile("      .Exported with error :" + ex.toString());
                mConnection.commit();
            }
            else
            {
                General.SendMail(mConnection, 2, rsFile.getString("mail_to"), ex.toString() + rsFile.getString("file_name") + " - " + rsFile.getInt("file_id"));
                /*General.addNewSMS(mConnection, rsFile.getInt("id"), 3,
                  "Eported file "
                  + rsFile.getString("file_name")
                  + " - "
                  + rsFile.getInt("file_id")
                  + " has occurred error");*/

                throw ex;
            }
        }
        finally
        {
            try
            {
                mSQL = null;
                mFilePathExp = null;
                IOUtil = null;
                rsFile.close();
                rsFile = null;
                stmt.close();
                stmt = null;
            }
            catch (Exception e)
            {
            }
        }
    }
}
