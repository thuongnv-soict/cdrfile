package cdrfile.thread;

/**
 * <p>Title: CDR File(s) System</p>
 * <p>Description: VMS IS Departerment</p>
 * <p>Copyright: Copyright (c)  by eKnowledge 2004</p>
 * <p>Company: VietNam Mobile Telecom Services</p>
 * @author eKnowledge - Software
 * @version 1.0
 */

import java.io.RandomAccessFile;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import cdrfile.global.ClientUtil;
import cdrfile.global.Global;
import cdrfile.global.IOUtils;
import cdrfile.global.cdrfileParam;

public abstract class ThreadInfo implements Runnable
{
    public static final String CARRIAGE_RETURN = "\r\n";
    public final int THREAD_NONE = 0;
    public final int THREAD_START = 1;
    public final int THREAD_STOP = 2;
    public final int THREAD_STARTED = 1;
    public final int THREAD_STOPPED = 2;

    public int miThreadStatus = THREAD_STOPPED;
    public int miThreadCommand = THREAD_NONE;
    protected int mTimeDelay = 60;
    protected Connection mConnection = null;
    public Thread mthrMain;
    protected String mstrThreadID;
    protected String mstrSubThreadID;
    protected String mstrThreadName;
    protected String mstrClassName;
    protected String mstrLogFileName;
    protected String mstrLogPathFileName = "";
    private int miDelayTime = 1;

    public ThreadInfo()
    {
        mstrSubThreadID = "";
    }

    public void finalize()
    {
        destroy();
        mstrThreadID = null;
        mstrThreadName = null;
        mstrClassName = null;
        mstrLogFileName = null;
        mstrLogPathFileName = null;
        closeConnection();
    }

    public void start()
    {
        // Destroy previous if it's constructed
        destroy();

        // Start new thread
        mthrMain = new Thread(this);
        mthrMain.start();
    }

    public void destroy()
    {
        if (mthrMain != null)
        {
            try
            {
                mthrMain = null;
            }
            catch (Exception e)
            {
                try
                {
                    writeLogFile(" - ThreadInfo_destroy:" + e.toString());
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                }
                e.printStackTrace();
            }
        }
    }

    public void setTimeDelay(int pTimeDelay)
    {
        mTimeDelay = pTimeDelay;
    }

    public void setThreadID(String pStrThreadID)
    {
        mstrThreadID = pStrThreadID;
    }

    public String getThreadID()
    {
        return mstrThreadID;
    }

    public void setSubThreadID(String pStrThreadID)
    {
        mstrSubThreadID = pStrThreadID;
    }

    public String getSubThreadID()
    {
        return mstrSubThreadID;
    }

    public void setThreadName(String pStrThreadName)
    {
        mstrThreadName = pStrThreadName;
    }

    public String getThreadName()
    {
        return mstrThreadName;
    }

    public void setLogName(String pStrLogName)
    {
        mstrLogPathFileName = pStrLogName;
    }

    public String getLogName()
    {
        return mstrLogFileName;
    }

    public void setLogPathFileName(String pStrLogName)
    {
        mstrLogPathFileName = pStrLogName;
    }

    public String getLogPathFileName()
    {
        return mstrLogPathFileName;
    }

    public void setClassName(String pstrClassName)
    {
        mstrClassName = pstrClassName;
    }

    public void setThreadCommand(int pIntStatus)
    {
        miThreadCommand = pIntStatus;
    }

    public String getClassName()
    {
        return mstrClassName;
    }

    public int getThreadStatus()
    {
        return miThreadStatus;
    }

    protected void openConnection() throws SQLException
    {
        // Make sure connection is closed
        try
        {
            closeConnection();
            // Connect to database
            mConnection = ClientUtil.openNewConnection();
            mConnection.setAutoCommit(false);
        }
        catch (Exception e)
        {
        }
    }

    protected void closeConnection()
    {
        try
        {
            if (mConnection != null)
            {
                mConnection.close();
                mConnection = null;
            }
        }
        catch (Exception e)
        {
        }
    }

    public void run()
    {
        miThreadStatus = THREAD_STARTED;
        updateStatus();
        // System.out.println(miThreadStatus);
        writeLogFile("Thread:" + mstrThreadID + " -> " + mstrThreadName + " started");

        while (miThreadCommand != THREAD_STOP)
        {
            try
            {
                configLogFile();
                openConnection();
                processSession();
            }
            catch (SQLException e)
            {
                switch (e.getErrorCode())
                {
                case 917:
                    writeLogFile(" - " + e.toString());
                    break;
                case 942:
                    writeLogFile(" - " + e.toString());
                    break;
                case 1034:

                    // ORA-01034 ORACLE not available
                    writeLogFile(" - " + e.toString());

                    // miThreadCommand = THREAD_STOP;
                    // Delay some time
                    try
                    {
                        Thread.sleep(cdrfileParam.ExceptionTimeDelay * 1000);
                    }
                    catch (Exception ex)
                    {
                        writeLogFile(" - " + ex.toString());
                    }
                    break
                        ;
                case 1401:

                    // java.sql.SQLException: ORA-01401: inserted value too
                    // large for column
                    writeLogFile(" - " + e.toString());
                    break;
                case 1089:

                    // ORA-01089: immediate shutdown in progress
                    writeLogFile(" - " + e.toString());

                    // miThreadCommand = THREAD_STOP;

                    // Delay some time
                    try
                    {
                        Thread.sleep(cdrfileParam.ExceptionTimeDelay * 1000);
                    }
                    catch (Exception ex)
                    {
                        writeLogFile(" - " + ex.toString());
                    }
                    break
                        ;
                case 1550:

                    // " - ORA-01555: snapshot too old: rollback segment number
                    // 4 with name "_SYSSMU4$" too small");
                    writeLogFile(" - " + e.toString());

                    // Delay some time
                    try
                    {
                        Thread.sleep(cdrfileParam.ExceptionTimeDelay * 1000);
                    }
                    catch (Exception ex)
                    {
                        writeLogFile(" - " + ex.toString());
                    }
                    break
                        ;
                case 1551:

                    // " - ORA-01555: snapshot too old: rollback segment number
                    // 4 with name "_SYSSMU4$" too small");
                    writeLogFile(" - " + e.toString());

                    // Delay some time
                    try
                    {
                        Thread.sleep(cdrfileParam.ExceptionTimeDelay * 1000);
                    }
                    catch (Exception ex)
                    {
                        writeLogFile(" - " + ex.toString());
                    }
                    break
                        ;
                case 1552:

                    // " - ORA-01555: snapshot too old: rollback segment number
                    // 4 with name "_SYSSMU4$" too small");
                    writeLogFile(" - " + e.toString());

                    // Delay some time
                    try
                    {
                        Thread.sleep(cdrfileParam.ExceptionTimeDelay * 1000);
                    }
                    catch (Exception ex)
                    {
                        writeLogFile(" - " + ex.toString());
                    }
                    break
                        ;
                case 1553:

                    // " - ORA-01555: snapshot too old: rollback segment number
                    // 4 with name "_SYSSMU4$" too small");
                    writeLogFile(" - " + e.toString());

                    // Delay some time
                    try
                    {
                        Thread.sleep(cdrfileParam.ExceptionTimeDelay * 1000);
                    }
                    catch (Exception ex)
                    {
                        writeLogFile(" - " + ex.toString());
                    }
                    break
                        ;
                case 1554:

                    // " - ORA-01555: snapshot too old: rollback segment number
                    // 4 with name "_SYSSMU4$" too small");
                    writeLogFile(" - " + e.toString());

                    // Delay some time
                    try
                    {
                        Thread.sleep(cdrfileParam.ExceptionTimeDelay * 1000);
                    }
                    catch (Exception ex)
                    {
                        writeLogFile(" - " + ex.toString());
                    }
                    break
                        ;
                case 1555:

                    // " - ORA-01555: snapshot too old: rollback segment number
                    // 4 with name "_SYSSMU4$" too small");
                    writeLogFile(" - " + e.toString());

                    // Delay some time
                    try
                    {
                        Thread.sleep(cdrfileParam.ExceptionTimeDelay * 1000);
                    }
                    catch (Exception ex)
                    {
                        writeLogFile(" - " + ex.toString());
                    }
                    break
                        ;
                case 17008:

                    // Connection close
                    writeLogFile(" - " + e.toString());

                    // Delay some time
                    try
                    {
                        Thread.sleep(cdrfileParam.ExceptionTimeDelay * 1000);
                    }
                    catch (Exception ex)
                    {
                        writeLogFile(" - " + ex.toString());
                    }
                    break
                        ;
                case 30036:

                    // - ORA-30036: unable to extend segment by 128 in undo
                    // tablespace 'UNDOTBS1'
                    writeLogFile(" - " + e.toString());

                    // Delay some time
                    try
                    {
                        Thread.sleep(cdrfileParam.ExceptionTimeDelay * 1000);
                    }
                    catch (Exception ex)
                    {
                        writeLogFile(" - " + ex.toString());
                    }
                    break
                        ;
                case 3113:

                    // Ora-03113: End of file on communication channe
                    writeLogFile(" - " + e.toString());

                    // miThreadCommand = THREAD_STOP;

                    // Delay some time
                    try
                    {
                        Thread.sleep(cdrfileParam.ExceptionTimeDelay * 1000);
                    }
                    catch (Exception ex)
                    {
                        writeLogFile(" - " + ex.toString());
                    }
                    break
                        ;
                case 27101:

                    // ORA-27101 Shared memory realm does not exist
                    writeLogFile(" - " + e.toString());

                    // miThreadCommand = THREAD_STOP;

                    // Delay some time
                    try
                    {
                        Thread.sleep(cdrfileParam.ExceptionTimeDelay * 1000);
                    }
                    catch (Exception ex)
                    {
                        writeLogFile(" - " + ex.toString());
                    }
                    break
                        ;
                case 17002:

                    // comp.lang.java.databases ORA-17002 Io exception
                    // miThreadCommand = THREAD_STOP;
                    writeLogFile(" - " + e.toString());

                    // Delay some time
                    try
                    {
                        Thread.sleep(cdrfileParam.ExceptionTimeDelay * 1000);
                    }
                    catch (Exception ex)
                    {
                        writeLogFile(" - " + ex.toString());
                    }
                    break
                        ;
                case 20000:

                    // Declare double plan tariff zone
                    writeLogFile(" - " + e.toString());

                    // Delay some time
                    try
                    {
                        Thread.sleep(cdrfileParam.ExceptionTimeDelay * 1000);
                    }
                    catch (Exception ex)
                    {
                        writeLogFile(" - " + ex.toString());
                    }
                    break
                        ;
                default:
                    e.printStackTrace();
                    writeLogFile(" - " + e.toString());

                    // miThreadCommand = THREAD_STOP;
                    break;
                }
            }
            catch (Exception e)
            {
                writeLogFile(" - " + e.toString() + "\r\n");
                // miThreadCommand = THREAD_STOP;
                // Delay some time
                try
                {
                    Global.writeEventThreadErr(Integer.parseInt(getThreadID()), 2, e.toString());
                    Thread.sleep(cdrfileParam.ExceptionTimeDelay * 1000);
                }
                catch (Exception ex)
                {
                    writeLogFile(" - " + ex.toString());
                }
            }
            finally
            {
                // Release connection
                try
                {
                    closeConnection();
                }
                catch (Exception e)
                {
                }

                // Wait some time
                try
                {
                    // Delay
                    for (int iIndex = 0; iIndex < miDelayTime && miThreadCommand != THREAD_STOP; iIndex++)
                    {
                        Thread.sleep(mTimeDelay * 1000); // Don vi thoi gian
                        // la giay
                    }
                }
                catch (OutOfMemoryError oomem)
                {
                    oomem.toString();
                    miThreadStatus = THREAD_STOPPED;
                    // Find the monitor if it is already loaded
                    for (int i = 0; i < Global.vThreadInfo.size(); i++)
                    {
                        Global.threadInfo = (ThreadInfo) Global.vThreadInfo.get(i);
                        if (Global.threadInfo.getThreadID().equals(mstrThreadID))
                        {
                            Global.threadInfo.setThreadCommand(Global.threadInfo.THREAD_STOP);
                            Global.vThreadInfo.remove(Global.threadInfo);
                            break;
                        }
                    }
                }
                catch (Exception e)
                {
                }
            }

            // Release memory
            System.runFinalization();
            System.gc();
        }

        // Report
        miThreadStatus = THREAD_STOPPED;
        // Find the monitor if it is already loaded
        for (int i = 0; i < Global.vThreadInfo.size(); i++)
        {
            Global.threadInfo = (ThreadInfo) Global.vThreadInfo.get(i);
            if (Global.threadInfo.getThreadID().equals(mstrThreadID))
            {
                Global.threadInfo.setThreadCommand(Global.threadInfo.THREAD_STOP);
                Global.vThreadInfo.remove(Global.threadInfo);
                break;
            }
        }
        writeLogFile("Thread:" + mstrThreadID + " -> " + mstrThreadName + " stopped.\r\n");
        updateStatus();
    }

    public void writeLogFile(String pStrLog)
    {
        configLogFile();
        pStrLog = Global.Format(new java.util.Date(), "dd/MM/yyyy HH:mm:ss") + " : " + pStrLog;
        if (mstrLogFileName != null && !mstrLogFileName.equals(""))
        {
            try
            {
                RandomAccessFile fl = new RandomAccessFile(mstrLogFileName, "rw");
                fl.seek(fl.length());
                fl.writeBytes(pStrLog + CARRIAGE_RETURN);
                fl.close();
            }
            catch (Exception e)
            {
                writeLogFile(" - ThreadInfo_writeLogFile: " + e.toString());
                e.printStackTrace();
            }
        }
    }

    private void configLogFile()
    {
        java.util.Date dtNow = new java.util.Date();
        IOUtils IOUtil = new IOUtils();
        if (!mstrLogPathFileName.equals(""))
        {
            if (!mstrLogPathFileName.endsWith("/") && !mstrLogPathFileName.endsWith("\\"))
            {
                mstrLogPathFileName += "/";
            }
            IOUtil.forceFolderExist(mstrLogPathFileName + Global.Format(dtNow, "yyyyMMdd") + "/");
            mstrLogFileName = mstrLogPathFileName + Global.Format(dtNow, "yyyyMMdd") + "/" + mstrLogFileName + ".log";
            if (mstrSubThreadID != null && !mstrSubThreadID.equals(""))
            {
                mstrLogFileName = mstrLogPathFileName + Global.Format(dtNow, "yyyyMMdd") + "/ThreadID_" + mstrThreadID + "_" + mstrSubThreadID + "_" + mstrThreadName + ".log";
            }
            else
            {
                mstrLogFileName = mstrLogPathFileName + Global.Format(dtNow, "yyyyMMdd") + "/ThreadID_" + mstrThreadID + "_" + mstrThreadName + ".log";

            }
        }
        dtNow = null;
        IOUtil = null;
    }


    private void updateStatus()
    {
        Connection cn = null;
        Statement stmt = null;
        try
        {
            cn = ClientUtil.openNewConnection();
            String strSQL = "UPDATE threads SET status=1 ";
            strSQL += "WHERE thread_id=" + mstrThreadID;
            stmt = cn.createStatement();
            stmt.executeUpdate(strSQL);
        }
        catch (Exception e)
        {
            writeLogFile(e.toString());
        }
        finally
        {
            try
            {
                stmt.close();
                stmt = null;
                cn.close();
                cn = null;
            }
            catch (Exception e)
            {
            }
        }
    }

    protected abstract void processSession() throws Exception;
}
