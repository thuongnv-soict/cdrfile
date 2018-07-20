package cdrfile.manager;

/**
 * <p>
 * Title: CDR File(s) System
 * </p>
 * <p>
 * Description: VMS IS Departerment
 * </p>
 * <p>
 * Copyright: Copyright (c) by eKnowledge 2004
 * </p>
 * <p>
 * Company: VietNam Mobile Telecom Services
 * </p>
 *
 * @author eKnowledge - Software
 * @version 1.0
 */

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import cdrfile.global.ClientUtil;
import cdrfile.global.Global;
import cdrfile.telnet.TelnetClient;
import cdrfile.telnet.TelnetServer;

public class ClusterManager extends Thread
{
    protected Thread mthrMain;
    static Connection cn = null;

    public ClusterManager()
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

    public void run()
    {
        int StatusConnection = -9; // 0 Not connect, 1 Connection already
        boolean tenetServerLoaded = false;
        int port = 8118;
        while (true)
        {
            try
            {
                cn = ClientUtil.openNewConnection();
                if (StatusConnection != 1)
                {
                    String strSQL = "SELECT port into ? from node_cluster where ip='" + Global.getLocalSvrIP() + "'";
                    if ((!tenetServerLoaded) && (Global.mPortManager > 0))
                    {
                        port = Global.ExecuteOutParameterInt(cn, strSQL) + Global.mPortManager;
                        TelnetServer telnet = new TelnetServer(port);
                        telnet.start();
                    }
                    tenetServerLoaded = true;
                }
                TelnetClient tc = new TelnetClient();
                tc.CheckServiceCluster(cn, Global.mPortManager);
                StatusConnection = 1;
            }
            catch (SQLException e)
            {
                switch (e.getErrorCode())
                {
                case 1034:
                    System.out.println(Global.Format(new java.util.Date(), "dd/MM/yyyy HH:mm:ss") + " - ORA-1034: ORACLE not available.Could not create connection to database.");
                    if (StatusConnection != -1)
                    {
                        StatusConnection = -1;
                    }

                    // Delay some time
                    try
                    {
                        Thread.sleep(50000);
                    }
                    catch (Exception ex)
                    {
                        System.out.println(Global.Format(new java.util.Date(), "dd/MM/yyyy HH:mm:ss") + " - " + ex.toString());
                    }
                    break
                        ;
                case 1089:
                    System.out.println(Global.Format(new java.util.Date(), "dd/MM/yyyy HH:mm:ss") + " - ORA-01089: immediate shutdown in progress - no operations are permitted.");
                    if (StatusConnection != -1)
                    {
                        StatusConnection = -1;
                    }

                    // Delay some time
                    try
                    {
                        Thread.sleep(60000);
                    }
                    catch (Exception ex)
                    {
                        System.out.println(Global.Format(new java.util.Date(), "dd/MM/yyyy HH:mm:ss") + " - " + ex.toString());
                    }
                    break
                        ;
                case 1555:

                    // " - ORA-01555: snapshot too old: rollback segment
                    // number
                    // 4 with name "_SYSSMU4$" too small");
                    // Find the monitor if it is already loaded
                    System.out.println(Global.Format(new java.util.Date(), "dd/MM/yyyy HH:mm:ss") + " - " + e.toString());
                    break;
                case 3113:
                    System.out.println(Global.Format(new java.util.Date(), "dd/MM/yyyy HH:mm:ss") + " - ORA-03113: end-of-file on communication channel.");
                    if (StatusConnection != -1)
                    {
                        StatusConnection = -1;
                    }

                    // Delay some time
                    try
                    {
                        Thread.sleep(60000);
                    }
                    catch (Exception ex)
                    {
                        System.out.println(Global.Format(new java.util.Date(), "dd/MM/yyyy HH:mm:ss") + " - " + ex.toString());
                    }
                    break
                        ;
                case 27101:
                    System.out.println(Global.Format(new java.util.Date(), "dd/MM/yyyy HH:mm:ss") + " - ORA-27101: ORACLE shared memory realm does not exist");
                    if (StatusConnection != -1)
                    {
                        StatusConnection = -1;
                    }

                    // Delay some time
                    try
                    {
                        Thread.sleep(60000);
                    }
                    catch (Exception ex)
                    {
                        System.out.println(Global.Format(new java.util.Date(), "dd/MM/yyyy HH:mm:ss") + " - " + ex.toString());
                    }
                    break
                        ;
                case 17002:

                    // comp.lang.java.databases ORA-17002 Io exception
                    if (StatusConnection != -2)
                    {
                        StatusConnection = -2;
                    }

                    System.out.println(Global.Format(new java.util.Date(), "dd/MM/yyyy HH:mm:ss") + " - Connection refused.Service automatic will retry connect after some minutes.");
                    System.out.println(Global.Format(new java.util.Date(), "dd/MM/yyyy HH:mm:ss") + "   . " + e.toString() + "\r\n");

                    // Delay some time
                    try
                    {
                        Thread.sleep(60000);
                    }
                    catch (Exception ex)
                    {
                        System.out.println(Global.Format(new java.util.Date(), "dd/MM/yyyy HH:mm:ss") + " - " + ex.toString());
                    }
                    break
                        ;
                default:
                    System.out.println(Global.Format(new java.util.Date(), "dd/MM/yyyy HH:mm:ss") + " - " + e.toString());
                    break;
                }
            }
            catch (Exception e)
            {
                try
                {
                    Thread.sleep(100000);
                }
                catch (Exception ex)
                {
                }
            }
            finally
            {
                // Delay some time
                try
                {
                    Thread.sleep(100000); // sleep 5 minutes
                }
                catch (Exception e)
                {
                }
            }
            // Release memory
            System.runFinalization();
            System.gc();
        }
    }

    public static void main(String args[]) throws IOException
    {
        int port = 8118;

        try
        {
            TelnetServer telnet = new TelnetServer(port);
            telnet.start();
            ClusterManager mainMgr = new ClusterManager();
            mainMgr.start();
        }
        catch (IOException e)
        {
            System.err.print(e.toString());
            System.exit(0);
        }
    }
}
