package cdrfile.manager;

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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import cdrfile.global.ClientUtil;
import cdrfile.global.Global;
import cdrfile.global.cdrfileParam;
import cdrfile.thread.ThreadInfo;
import cdrfile.general.General;

// import java.util.Date;

public class ConvertManager implements Runnable {
	protected Thread mthrMain;
	//public static int mServerPort=2;
        private String mgroup_shell = null;
	private String mstrLogFileName = "/ConvertManager.log";
	private Properties fApplicationConfig = new Properties();

	public ConvertManager( String group_shell) {
            mgroup_shell = group_shell;
	}

	public void start() {
		if (mthrMain != null) {
			mthrMain = null;
		}
		mthrMain = new Thread(this);
		mthrMain.start();
	}

	public void run() {
		int StatusConnection = -9; // 0 Not connect, 1 Connection already
		// create

		while (true) {
			Connection cn = null;
			try {
				cn = ClientUtil.openNewConnection();
				mstrLogFileName = Global.mstrLogPathFileName + "/ConvertManager.log";
				loadSysParametter(cn);
				if (cdrfileParam.MainProgram.compareTo("RUNNING") == 0) {
					if (StatusConnection == -9) {
						writeLogFile(" -> Convert Manager starting.");
						fApplicationConfig.load((ClientUtil.class)
								.getResourceAsStream("/cdrfile/cdrfile.cfg"));
						writeLogFile(" - Connecting to ORACLE via "
								+ fApplicationConfig.getProperty("DBURL")
										.toString());
						StatusConnection = 1;
					}
					unloadThread(cn);
					loadThread(cn);
				} else {
					if (StatusConnection != -9) {
						StatusConnection = 0;
					}
					unloadThread(cn);
				}

				switch (StatusConnection) {
				case -2:
					StatusConnection = 1;
					writeLogFile(" - Network is now again connected.");
					break;
				case -1:
					StatusConnection = 1;
					writeLogFile(" - Convert Manager again started.");
					break;
				case 0:
					writeLogFile(" -> Convert Manager stopped.\r\n");
					StatusConnection = -9;
					break;
				case 1:
					writeLogFile(" -> Convert Manager started.");
					StatusConnection = 2;
					break;
				}
			} catch (SQLException e) {
				switch (e.getErrorCode()) {
				case 1034:
					writeLogFile(" - ORA-1034: ORACLE not available.Could not create connection to database.");
					if (StatusConnection != -1) {
						StatusConnection = -1;
						writeLogFile(" -> Convert Ericsson Manager stopped.\r\n");
						writeLogFile(" - Service automatic will be retrying connect to DB after "
								+ cdrfileParam.ExceptionTimeDelay
								/ 10
								+ " minute.");
					}

					// Delay some time
					try {
						Thread.sleep(cdrfileParam.ExceptionTimeDelay * 10000);
					} catch (Exception ex) {
						writeLogFile(" - " + ex.toString());
					}
					break;
				case 1089:
					writeLogFile(" - ORA-01089: immediate shutdown in progress - no operations are permitted.");
					if (StatusConnection != -1) {
						StatusConnection = -1;
						writeLogFile(" -> Convert Ericsson Manager stopped.\r\n");
						writeLogFile(" - Service automatic will be retrying connect to DB after "
								+ cdrfileParam.ExceptionTimeDelay
								/ 10
								+ " minute.");
					}

					// Delay some time
					try {
						Thread.sleep(cdrfileParam.ExceptionTimeDelay * 10000);
					} catch (Exception ex) {
						writeLogFile(" - " + ex.toString());
					}
					break;
				case 1555:

					// " - ORA-01555: snapshot too old: rollback segment number
					// 4 with name "_SYSSMU4$" too small");
					// Find the monitor if it is already loaded
					writeLogFile(" - " + e.toString());
					break;
				case 3113:
					writeLogFile(" - ORA-03113: end-of-file on communication channel.");
					if (StatusConnection != -1) {
						StatusConnection = -1;
						writeLogFile(" -> Convert Ericsson Manager stopped.\r\n");
						writeLogFile(" - Service automatic will be retrying connect to DB after "
								+ cdrfileParam.ExceptionTimeDelay
								/ 10
								+ " minute.");
					}

					// Delay some time
					try {
						Thread.sleep(cdrfileParam.ExceptionTimeDelay * 10000);
					} catch (Exception ex) {
						writeLogFile(" - " + ex.toString());
					}
					break;
				case 27101:
					writeLogFile(" - ORA-27101: ORACLE shared memory realm does not exist");
					if (StatusConnection != -1) {
						StatusConnection = -1;
						writeLogFile(" - Service automatic will be retrying connect to DB after "
								+ cdrfileParam.ExceptionTimeDelay
								/ 10
								+ " minute.");
						writeLogFile(" - Convert Ericsson Manager stopped.\r\n");
					}

					// Delay some time
					try {
						Thread.sleep(cdrfileParam.ExceptionTimeDelay * 10000);
					} catch (Exception ex) {
						writeLogFile(" - " + ex.toString());
					}
					break;
				case 17002:

					// comp.lang.java.databases ORA-17002 Io exception
					if (StatusConnection != -2) {
						StatusConnection = -2;
					}
					writeLogFile(" - Connection refused.Service automatic will retry connect after 5 minute.");
					writeLogFile("   . " + e.toString() + "\r\n");

					// Delay some time
					try {
						Thread.sleep(cdrfileParam.ExceptionTimeDelay * 10000);
					} catch (Exception ex) {
						writeLogFile(" - " + ex.toString());
					}
					break;
				default:
					writeLogFile(" - " + e.toString());
					break;
				}
			} catch (Exception e) {
				writeLogFile(" - " + e.toString());
				try {
					Thread.sleep(cdrfileParam.ExceptionTimeDelay * 1000);
				} catch (Exception ex) {
				}
			} finally {
				// Close connection
				try {
					cn.close();
					cn = null;
				} catch (Exception e) {
				}

				// Delay some time
				try {
					Thread.sleep(cdrfileParam.MainTimeDelay * 1000);
				} catch (Exception e) {
				}
			}
			// Release memory
			System.runFinalization();
			System.gc();
		} // end while
	}

	public void finalize() {
		destroy();
	}


	private void loadThread(Connection cn) throws Exception {
		boolean blnFound = false;
		String mThreadID = null;
		String mThreadName = null;
		String mClassName = null;
		String mLogName = null;
                String mSubThreadID = null;
                String strSQL = "";
                String clauseShell = "";
                if (Global.isNumeric(mgroup_shell)){
                    clauseShell = "group_shell = "+ mgroup_shell;
                }else{
                    clauseShell = "group_shell is null";
                }
                strSQL = "SELECT a.thread_id,a.thread_name,a.class_name,";
                strSQL += " a.startup_type,a.log_dir,a.time_delay, c.subthread_ID ";
                strSQL += " FROM (SELECT a.thread_id,a.thread_name,b.class_name,startup_type,log_dir,time_delay FROM threads a, class_module b,node_cluster c";
                strSQL += " WHERE a.class_id=b.class_id AND startup_type=1 AND "+clauseShell;
                strSQL += " AND b.status='ACTIVE' AND b.class_type=2 AND ";
                strSQL += "((nvl(a.run_on_node, 0) = 0 or nvl(a.run_on_node, 0) = c.id) AND(c.ip = '"+Global.getLocalSvrIP()+"'))) a,";
                strSQL += " thread_sub c WHERE a.thread_id=c.thread_ID(+) ";
                strSQL += " ORDER BY thread_id desc";

                Statement stmtThread = cn.createStatement();
		ResultSet rsThread = stmtThread.executeQuery(strSQL);
                try {
                    while (rsThread.next()) {
                        // get thread ID
                        mThreadID = rsThread.getString("thread_id");
                        if (mThreadID == null) {
                            mThreadID = "";
                        }

                        mSubThreadID = rsThread.getString("subthread_id");
                        if (mSubThreadID == null) {
                            mSubThreadID = "";
                        }

                        mThreadName = rsThread.getString("thread_name");
                        if (mThreadName == null) {
                            mThreadName = "";
                        }
                        mClassName = rsThread.getString("class_name");
                        if (mClassName == null) {
                            mClassName = "";
                        }
                        mLogName = rsThread.getString("log_dir");
                        if (mLogName == null) {
                            mLogName = "";
                        }

                        blnFound = false;
                        for (int i = 0; i < Global.vThreadInfo.size(); i++) {
                            Global.threadInfo = (ThreadInfo) Global.
                                                vThreadInfo.get(i);
                            if (Global.threadInfo.getThreadID().equals(
                                    mThreadID) && Global.threadInfo.getSubThreadID().equals(mSubThreadID)) {
                                blnFound = true;
                                if (Global.threadInfo.mthrMain.isAlive()) {
                                    //no thing to change
                                    /*System.out.println("Check thread alive " + Global.threadInfo.getThreadID());
                                     Thread.sleep(500);
                                     Global.threadInfo
                                     .setThreadCommand(Global.threadInfo.THREAD_STOP);
                                     System.out.println("Set  thread die: " + Global.threadInfo.getThreadID());*/
                                } else {
                                    writeLogFile(" - Thread :" +
                                                 Global.threadInfo.getThreadID() +
                                                 " has just died => Need to restart thread "
                                                 + mThreadName);
                                    Global.vThreadInfo.remove(Global.
                                            threadInfo);
                                    General.SendMail(cn, 2, "", "Thread :" +
                                            Global.threadInfo.getThreadID() +
                                            " has just died. Check system or data immediately, pls.");
                                    /*General.addNewSMS(cn,Integer.parseInt(Global.threadInfo.getThreadID()), 2,
                                        "Thread :"
                                        + " has just died. Check system or data immediately, pls.");*/

                                    cn.commit();
                                }
                                break;
                            }
                        }
                        if (!blnFound) { // Not found -> load it
                            try {
                                if (mSubThreadID.equals("")){
                                    writeLogFile(
                                            " - Loading plug-in thread ThreadID: "
                                            + mThreadID + " - ThreadName: " +
                                            mThreadName);
                                }else{
                                    writeLogFile(
                                            " - Loading plug-in sub threadID: "
                                            + mSubThreadID + " of  ThreadID: " +
                                            mThreadID + " - threadName: " +
                                            mThreadName);
                                }
                                ThreadInfo thread = (ThreadInfo) Class.forName(
                                        mClassName).newInstance();
                                thread.setThreadID(mThreadID);
                                thread.setSubThreadID(mSubThreadID);
                                thread.setThreadName(mThreadName);
                                thread.setClassName(mClassName);
                                thread.setLogName(mLogName);
                                thread.setTimeDelay(rsThread.getInt("time_delay"));
                                Global.vThreadInfo.add(thread);
                                blnFound = true;
                                thread.start();
                            } catch (Exception e) {
                                if (mSubThreadID.equals("")){
                                    writeLogFile(" - loadThread: " +
                                                 e.toString());
                                }else
                                    writeLogFile(" - loadSubThread: " +
                                                 e.toString());
//                                e.printStackTrace();
                            }
                        }
                    }
                } catch (Exception ex) {
                    if (mSubThreadID.equals("")){
                                writeLogFile(" - loadThread: " +
                                             ex.toString());
                            }else
                                writeLogFile(" - loadSubThread: " +
                                             ex.toString());

			ex.printStackTrace();
		} finally {
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

	private void unloadThread(Connection cn) throws Exception {
                String strSQL = "";
                String clauseShell = "";
                if (Global.isNumeric(mgroup_shell)){
                    clauseShell = "group_shell = "+ mgroup_shell;
                }else{
                    clauseShell = "group_shell is null";
                }
                strSQL = "SELECT a.thread_id,a.thread_name, c.subthread_ID";
                strSQL += " FROM ( SELECT a.thread_id,a.thread_name FROM threads a, class_module b,node_cluster c  ";
                strSQL += " WHERE a.class_id=b.class_id AND startup_type= 0 AND "+ clauseShell;
                strSQL += " AND b.class_type=2 AND ";
                strSQL += "((nvl(a.run_on_node, 0) = 0 or nvl(a.run_on_node, 0) = c.id) AND(c.ip = '"+Global.getLocalSvrIP()+"'))) a,";
                strSQL += "thread_sub c WHERE a.thread_id=c.thread_ID(+)";

                Statement stmt = cn.createStatement();
		ResultSet rs = stmt.executeQuery(strSQL);
		while (rs.next()) {
			// get thread ID
			String mStrThreadID = rs.getString(1);
                        String mStrSubThreadID = rs.getString(3);
                        if (mStrSubThreadID!= null && !mStrSubThreadID.equals("")){
                            // Find the monitor if it is already loaded
                            for (int i = 0; i < Global.vThreadInfo.size(); i++) {
                                Global.threadInfo = (ThreadInfo) Global.
                                        vThreadInfo.get(i);
                                if (Global.threadInfo.getThreadID().equals(
                                        mStrThreadID) &&
                                    Global.threadInfo.
                                    getSubThreadID().equals(mStrSubThreadID)) {
                                    writeLogFile(
                                            " - UnLoaded plug-in sub thread ID: "
                                            + mStrSubThreadID +
                                            " of ThreadID: " + mStrThreadID +
                                            " - ThreadName: "
                                            + rs.getString("thread_name"));
                                    Global.threadInfo
                                            .setThreadCommand(Global.threadInfo.
                                            THREAD_STOP);
                                    Global.vThreadInfo.remove(Global.threadInfo);
                                    break;
                                }
                            }
                        }else{
                            for (int i = 0; i < Global.vThreadInfo.size(); i++) {
                                Global.threadInfo = (ThreadInfo) Global.
                                        vThreadInfo.get(i);
                                if (Global.threadInfo.getThreadID().equals(
                                        mStrThreadID)) {
                                    writeLogFile(
                                            " - UnLoaded plug-in thread ThreadID: "
                                            +
                                            " - ThreadName: "
                                            + rs.getString("thread_name"));
                                    Global.threadInfo
                                            .setThreadCommand(Global.threadInfo.
                                            THREAD_STOP);
                                    Global.vThreadInfo.remove(Global.threadInfo);
                                    break;
                                }
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

	private void writeLogFile(String pStrLog) {
                if (Global.isNumeric(mgroup_shell)){
                    mstrLogFileName = Global.mstrLogPathFileName +
                                      "/ConvertManager_" + mgroup_shell + ".log";
                }else{
                    mstrLogFileName = Global.mstrLogPathFileName + "/ConvertManager.log";
                }
		pStrLog = Global.Format(new java.util.Date(), "dd/MM/yyyy HH:mm:ss")
				+ pStrLog;
		if (mstrLogFileName != null && !mstrLogFileName.equals("")) {
			try {
				RandomAccessFile fl = new RandomAccessFile(mstrLogFileName,
						"rw");
				fl.seek(fl.length());
				fl.writeBytes(pStrLog + "\r\n");
				fl.close();
				fl = null;
			} catch (Exception e) {
				writeLogFile(" - writeLogFile: " + e.toString());
				e.printStackTrace();
			}
		}
	}

	private void destroy() {
		if (mthrMain != null) {
			try {
				mthrMain = null;
			} catch (Exception e) {
				writeLogFile(" - destroy: " + e.toString());
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		if (args.length > 0)
                {
                    Global.mstrLogPathFileName = args[0];
		}
                String group_shell = null;
                if (args.length > 1)
                {
                    group_shell  = args[1];
                }
		ConvertManager threadMain = new ConvertManager(group_shell);
		threadMain.start();
		Thread.currentThread().setName(
				ClusterManager.class.getName() + ".main()");
	}

	private void loadSysParametter(Connection cn) throws Exception {
		String strSQL = "select ptr_name,ptr_value " + "from sys_param_detail";
		Statement stmt = cn.createStatement();
		ResultSet rs = stmt.executeQuery(strSQL);
		try {
			while (rs.next()) {
				if (rs.getString("ptr_name").compareTo("MainProgram") == 0) {
					if (cdrfileParam.MainProgram.compareTo(rs
							.getString("ptr_value")) != 0) {
						cdrfileParam.MainProgram = rs.getString("ptr_value");
					}
				}

				if (rs.getString("ptr_name").compareTo("MainTimeDelay") == 0) {
					if (cdrfileParam.MainTimeDelay != rs.getInt("ptr_value")) {
						cdrfileParam.MainTimeDelay = rs.getInt("ptr_value");
					}
				}
				if (rs.getString("ptr_name").compareTo("ExceptionTimeDelay") == 0) {
					if (cdrfileParam.ExceptionTimeDelay != rs
							.getInt("ptr_value")) {
						cdrfileParam.ExceptionTimeDelay = rs
								.getInt("ptr_value");
					}
				}
				if (rs.getString("ptr_name").compareTo("OnErrorResumeNext") == 0) {
					cdrfileParam.OnErrorResumeNext = rs.getString("ptr_value");
				}
				if (rs.getString("ptr_name").compareTo("ChargeCDRFile") == 0) {
					if (cdrfileParam.ChargeCDRFile != Boolean.getBoolean(rs
							.getString("ptr_value"))) {
						cdrfileParam.ChargeCDRFile = Boolean.getBoolean(rs
								.getString("ptr_value"));
					}
				}

			}
		} catch (Exception e) {
			writeLogFile(" - " + e.toString());
		} finally {
			// Release
			strSQL = null;
			rs.close();
			rs = null;
			stmt.close();
			stmt = null;
		}
	}
}
