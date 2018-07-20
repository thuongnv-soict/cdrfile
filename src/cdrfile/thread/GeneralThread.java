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
import cdrfile.general.*;
import cdrfile.global.Global;
import java.util.Vector;

public class GeneralThread extends ThreadInfo
{
    protected CheckfileParam checkfileParam = null;
    protected Vector vCheckfileParam = new Vector();
    protected int iFtpID = 0;
    public void finalize()
    {
        destroy();
        System.runFinalization();
        System.gc();
    }

    public void processSession() throws Exception
    {
        writeLogFile("General thread is running.");
        long mTime;
        boolean Found = false;
        General works = new General(getThreadID(), getThreadName(), getLogPathFileName());
        String mSQL = "SELECT * FROM node_cluster b" + " WHERE b.ip='" + Global.getLocalSvrIP() + "'";

        Statement stmt = mConnection.createStatement();
        ResultSet rs = stmt.executeQuery(mSQL);
        boolean isRunOnMainNode = false;
        if (rs.next())
        {
            if (rs.getInt("id") == 1)
            {
                isRunOnMainNode = true;
            }
        }
//        works.DeleteCDFile(mConnection);
        try
        {
            if (isRunOnMainNode)
            {
                try
                {
                    works.CheckFreeDiskSpace(mConnection);

                    works.SendSmtpMail(mConnection);

                    works.SendSmtpMailWithAttachment(mConnection);
                }
                catch (Exception ex)
                {
                    throw ex;
                }
                finally
                {
                    works.sentSMS(mConnection);
                }
            }
        }
        catch (Exception ex)
        { // end try catch
            throw ex;
        }
        finally
        {
            try
            {
                mSQL = null;
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
}
