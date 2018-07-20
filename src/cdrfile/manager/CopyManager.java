package cdrfile.manager;

import java.util.Properties;
import cdrfile.global.Global;
import java.io.RandomAccessFile;
import cdrfile.global.cdrfileParam;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Connection;
import cdrfile.thread.ThreadInfo;
import cdrfile.general.General;
import cdrfile.global.ClientUtil;
import java.sql.SQLException;

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
public class CopyManager implements Runnable
{
    protected Thread mthrMain;
    private String mstrLogFileName = "/CopyManager.log";
    private Properties fApplicationConfig = new Properties();

    public CopyManager()
    {
    }

    public void start()
    {
        if (mthrMain != null)
        {
            mthrMain = null;
        }
        mthrMain = new Thread(this);
        mthrMain.start();
    }

    public void finalize()
    {
        destroy();
    }

    private void destroy()
    {
        if (mthrMain != null)
        {
            try
            {
                mthrMain = null;
            }
            catch (Exception e)
            {
                writeLogFile(" - destroy: " + e.toString());
                e.printStackTrace();
            }
        }
    }

    private void writeLogFile(String pStrLog)
    {
        mstrLogFileName = Global.mstrLogPathFileName + "/CopyManager.log";
        pStrLog = Global.Format(new java.util.Date(), "dd/MM/yyyy HH:mm:ss") + pStrLog;
        if (mstrLogFileName != null && !mstrLogFileName.equals(""))
        {
            try
            {
                RandomAccessFile fl = new RandomAccessFile(mstrLogFileName, "rw");
                fl.seek(fl.length());
                fl.writeBytes(pStrLog + "\r\n");
                fl.close();
                fl = null;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private void loadThread(Connection cn) throws Exception
    {
        boolean blnFound = false;
        String mThreadID = null;
        String mThreadName = null;
        String mClassName = null;
        String mLogName = null;

        String strSQL = "SELECT a.thread_id,a.thread_name,b.class_name,";
        strSQL += " startup_type,log_dir,time_delay ";
        strSQL += " FROM threads a, class_module b";
        strSQL += " WHERE a.class_id=b.class_id AND startup_type=1 ";
        strSQL += " AND b.status='ACTIVE' AND b.class_type=7";
        strSQL += " ORDER BY thread_id desc";

        writeLogFile(" Command: " + strSQL);

        Statement stmtThread = cn.createStatement();
        ResultSet rsThread = stmtThread.executeQuery(strSQL);
        try
        {
            while (rsThread.next())
            {
                mThreadID = rsThread.getString("thread_id");
                if (mThreadID == null)
                {
                    mThreadID = "";
                }
                mThreadName = rsThread.getString("thread_name");
                if (mThreadName == null)
                {
                    mThreadName = "";
                }
                mClassName = rsThread.getString("class_name");
                if (mClassName == null)
                {
                    mClassName = "";
                }
                mLogName = rsThread.getString("log_dir");
                if (mLogName == null)
                {
                    mLogName = "";
                }

                blnFound = false;
                for (int i = 0; i < Global.vThreadInfo.size(); i++)
                {
                    Global.threadInfo = (ThreadInfo) Global.vThreadInfo.get(i);
                    if (Global.threadInfo.getThreadID().equals(mThreadID))
                    {
                        blnFound = true;
                        if (Global.threadInfo.mthrMain.isAlive())
                        {

                        }
                        else
                        {
                            writeLogFile(" - Thread :" + Global.threadInfo.getThreadID() + " has just died => Need to restart thread " + mThreadName);
                            Global.vThreadInfo.remove(Global.threadInfo);
                            General.SendMail(cn, 2, "", "Thread :" + Global.threadInfo.getThreadID() + " has just died. Check system or data immediately, pls.");
                            cn.commit();
                        }
                        break;
                    }
                }

                if (!blnFound)
                { // Not found -> load it
                    try
                    {
                        writeLogFile(" - Loading plug-in thread ThreadID: " + mThreadID + " - ThreadName: " + mThreadName);
                        ThreadInfo thread = (ThreadInfo) Class.forName(mClassName).newInstance();
                        thread.setThreadID(mThreadID);
                        thread.setThreadName(mThreadName);
                        thread.setClassName(mClassName);
                        thread.setLogName(mLogName);
                        thread.setTimeDelay(rsThread.getInt("time_delay"));
                        Global.vThreadInfo.add(thread);
                        blnFound = true;
                        thread.start();
                    }
                    catch (Exception e)
                    {
                        writeLogFile(" - loadThread: " + e.toString());
                    }
                }
            }
        }
        catch (Exception ex)
        {
            writeLogFile(" - loadThread ex: " + ex.toString());
            ex.printStackTrace();
        }
        finally
        {
            // Release
            rsThread.close();
            rsThread = null;
            stmtThread.close();
            stmtThread = null;

            mThreadID = null;
            mThreadName = null;
            mClassName = null;
            mLogName = null;
            Global.threadInfo = null;
        }
    }

    private void unloadThread(Connection cn) throws Exception
    {
        String strSQL = "SELECT thread_id,thread_name FROM threads a,class_module b ";
        strSQL += " WHERE a.class_id=b.class_id AND a.startup_type=0 ";
        strSQL += " AND b.class_type=7 ";
        Statement stmt = cn.createStatement();
        ResultSet rs = stmt.executeQuery(strSQL);
        while (rs.next())
        {
            // get thread ID
            String mStrThreadID = rs.getString("thread_id");

            // Find the monitor if it is already loaded
            for (int i = 0; i < Global.vThreadInfo.size(); i++)
            {
                Global.threadInfo = (ThreadInfo) Global.vThreadInfo.get(i);
                if (Global.threadInfo.getThreadID().equals(mStrThreadID))
                {
                    writeLogFile(" - UnLoaded plug-in thread ThreadID: " + mStrThreadID + " - ThreadName: " + rs.getString("thread_name"));
                    Global.threadInfo.setThreadCommand(Global.threadInfo.THREAD_STOP);
                    Global.vThreadInfo.remove(Global.threadInfo);
                    break;
                }
            }
        }

        // Release
        rs.close();
        rs = null;
        stmt.close();
        stmt = null;
        Global.threadInfo = null;
    }

    private void loadSysParametter(Connection cn) throws Exception
    {
        String strSQL = "select ptr_name,ptr_value from sys_param_detail";
        Statement stmt = cn.createStatement();
        ResultSet rs = stmt.executeQuery(strSQL);
        try
        {
            while (rs.next())
            {
                if (rs.getString("ptr_name").compareTo("MainProgram") == 0)
                {
                    if (cdrfileParam.MainProgram.compareTo(rs.getString("ptr_value")) != 0)
                    {
                        cdrfileParam.MainProgram = rs.getString("ptr_value");
                    }
                }

                if (rs.getString("ptr_name").compareTo("MainTimeDelay") == 0)
                {
                    if (cdrfileParam.MainTimeDelay != rs.getInt("ptr_value"))
                    {
                        cdrfileParam.MainTimeDelay = rs.getInt("ptr_value");
                    }
                }
                if (rs.getString("ptr_name").compareTo("ExceptionTimeDelay") == 0)
                {
                    if (cdrfileParam.ExceptionTimeDelay != rs.getInt("ptr_value"))
                    {
                        cdrfileParam.ExceptionTimeDelay = rs.getInt("ptr_value");
                    }
                }
                if (rs.getString("ptr_name").compareTo("OnErrorResumeNext") == 0)
                {
                    cdrfileParam.OnErrorResumeNext = rs.getString("ptr_value");
                }
            }
        }
        catch (Exception e)
        {
            writeLogFile(" - " + e.toString());
        }
        finally
        {
            // Release
            strSQL = null;
            rs.close();
            rs = null;
            stmt.close();
            stmt = null;
        }
    }

    public void run()
    {
        int StatusConnection = -9;

        while (true)
        {
            Connection cn = null;
            try
            {
                cn = ClientUtil.openNewConnection();
                mstrLogFileName = Global.mstrLogPathFileName + "/CopyManager.log";
                loadSysParametter(cn);
                if (cdrfileParam.MainProgram.compareTo("RUNNING") == 0)
                {
                    if (StatusConnection == -9)
                    {
                        writeLogFile(" -> Copy Manager is starting.");
                        fApplicationConfig.load((ClientUtil.class).getResourceAsStream("/cdrfile/cdrfile.cfg"));
                        writeLogFile(" - Connecting to ORACLE via " + fApplicationConfig.getProperty("DBURL").toString());
                        StatusConnection = 1;
                    }
                    unloadThread(cn);
                    loadThread(cn);
                }
                else
                {
                    if (StatusConnection != -9)
                    {
                        StatusConnection = 0;
                    }
                    unloadThread(cn);
                }

                switch (StatusConnection)
                {
                case -2:
                    StatusConnection = 1;
                    writeLogFile(" - Network is now again connected.");
                    break;
                case -1:
                    StatusConnection = 1;
                    writeLogFile(" - Copy Manager again started.");
                    break;
                case 0:
                    writeLogFile(" -> Copy Manager stopped.\r\n");
                    StatusConnection = -9;
                    break;
                case 1:
                    writeLogFile(" -> Copy Manager started.");
                    StatusConnection = 2;
                    break;
                }
            }
            catch (SQLException e)
            {
                switch (e.getErrorCode())
                {
                case 1034:
                    writeLogFile(" - ORA-1034: ORACLE not available.Could not create connection to database.");
                    if (StatusConnection != -1)
                    {
                        StatusConnection = -1;
                        writeLogFile(" -> Copy Manager stopped.\r\n");
                        writeLogFile(" - Service automatic will be retrying connect to DB after " + cdrfileParam.ExceptionTimeDelay / 10 + " minute.");
                    }

                    // Delay some time
                    try
                    {
                        Thread.sleep(cdrfileParam.ExceptionTimeDelay * 10000);
                    }
                    catch (Exception ex)
                    {
                        writeLogFile(" - " + ex.toString());
                    }
                    break;
                case 1089:
                    writeLogFile(" - ORA-01089: immediate shutdown in progress - no operations are permitted.");

                    // Find the monitor if it is already loaded
                    Global.vThreadInfo.removeAllElements();
                    Global.vThreadInfo.clear();
                    if (StatusConnection != -1)
                    {
                        StatusConnection = -1;
                        writeLogFile(" -> Copy Manager stopped.\r\n");
                        writeLogFile(" - Service automatic will be retrying connect to DB after " + cdrfileParam.ExceptionTimeDelay / 10 + " minute.");
                    }
                    try
                    {
                        Thread.sleep(cdrfileParam.ExceptionTimeDelay * 10000);
                    }
                    catch (Exception ex)
                    {
                        writeLogFile(" - " + ex.toString());
                    }
                    break;
                case 1555:
                    writeLogFile(" - " + e.toString());
                    break;
                case 3113:
                    writeLogFile(" - ORA-03113: end-of-file on communication channel.");

                    // Find the monitor if it is already loaded
                    Global.vThreadInfo.removeAllElements();
                    Global.vThreadInfo.clear();
                    if (StatusConnection != -1)
                    {
                        StatusConnection = -1;
                        writeLogFile(" -> Copy Manager stopped.\r\n");
                        writeLogFile(" - Service automatic will be retrying connect to DB after " + cdrfileParam.ExceptionTimeDelay / 10 + " minute.");
                    }

                    try
                    {
                        Thread.sleep(cdrfileParam.ExceptionTimeDelay * 10000);
                    }
                    catch (Exception ex)
                    {
                        writeLogFile(" - " + ex.toString());
                    }
                    break;
                case 27101:
                    writeLogFile(" - ORA-27101: ORACLE shared memory realm does not exist");

                    // Find the monitor if it is already loaded
                    Global.vThreadInfo.removeAllElements();
                    Global.vThreadInfo.clear();
                    if (StatusConnection != -1)
                    {
                        StatusConnection = -1;
                        writeLogFile(" - Service automatic will be retrying connect to DB after " + cdrfileParam.ExceptionTimeDelay / 10 + " minute.");
                        writeLogFile(" - Copy Manager stopped.\r\n");
                    }

                    // Delay some time
                    try
                    {
                        Thread.sleep(cdrfileParam.ExceptionTimeDelay * 10000);
                    }
                    catch (Exception ex)
                    {
                        writeLogFile(" - " + ex.toString());
                    }
                    break;
                case 17002:
                    if (StatusConnection != -2)
                    {
                        StatusConnection = -2;
                    }
                    writeLogFile(" - Connection refused.Service automatic will retry connect after 5 minute.");
                    writeLogFile("   . " + e.toString() + "\r\n");

                    // Delay some time
                    try
                    {
                        Thread.sleep(cdrfileParam.ExceptionTimeDelay * 10000);
                    }
                    catch (Exception ex)
                    {
                        writeLogFile(" - " + ex.toString());
                    }
                    break;
                default:
                    writeLogFile(" - " + e.toString());
                    break;
                }
            }
            catch (Exception e)
            {
                writeLogFile(" - " + e.toString());
                try
                {
                    Thread.sleep(cdrfileParam.ExceptionTimeDelay * 1000);
                }
                catch (Exception ex)
                {
                }
            }
            finally
            {
                try
                {
                    cn.close();
                    cn = null;
                }
                catch (Exception e)
                {
                }
                try
                {
                    Thread.sleep(cdrfileParam.MainTimeDelay * 1000);
                }
                catch (Exception e)
                {
                }
            }
            // Release memory
            System.runFinalization();
            System.gc();
        } // end while

    }

    public static void main(String[] args)
    {
        if (args.length > 0)
        {
            Global.mstrLogPathFileName = args[0];
        }
        CopyManager threadMain = new CopyManager();
        threadMain.start();
        Thread.currentThread().setName(ClusterManager.class.getName() + ".main()");
    }

}
