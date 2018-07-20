/*
 *
 * Created       : 2000 Jan 30 (Sun) 20:35:41 by Harold Carr.
 * Last Modified : 2000 Jan 31 (Mon) 03:59:34 by Harold Carr.
 */

package cdrfile.telnet;

import java.io.*;
import java.net.*;
import java.sql.*;
import cdrfile.global.*;

public class TelnetClient {
	private String mstrLogFileName = "";

	public static void main(String[] av) {
		/*try{
		Process p = Runtime.getRuntime().exec("ls -la");
		BufferedReader buf = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line="";
		while ((line=buf.readLine())!=null)
		{
			System.out.println(line);
			p.waitFor();
		}
		} catch (Exception e) {
			e.printStackTrace();
		}*/

	/*	String host = "localhost";
		int port = 23;
		switch (av.length) {
		case 2:
			port = Integer.parseInt(av[1]);
		case 1:
			host = av[0];
		case 0:
			break;
		default:
			System.exit(-1);
		}
		new TelnetClient(System.in, System.out, host, port);*/
	}

	public TelnetClient() {
	}

	public TelnetClient(InputStream in, PrintStream out, String host, int port) {
		TelnetWrapper telnetWrapper = new TelnetWrapper();
		try {
			telnetWrapper.connect(host, port);
		} catch (IOException e) {
			System.out.println("TelnetClient: Got exception during connect: "
					+ e);
			e.printStackTrace();
		}
		createAndStartReader(telnetWrapper, out);
		byte[] buf = new byte[256];
		int n = 0;
		try {
			while (n >= 0) {
				n = in.read(buf);
				if (n > 0) {
					byte[] sendBuf = new byte[n];
					System.arraycopy(buf, 0, sendBuf, 0, n);
					// Must be transpose (not send) or connect is closed.
					telnetWrapper.getHandler().transpose(sendBuf);
				}
			}
		} catch (IOException e) {
			System.out
					.println("TelnetClient: Got exception in read/write loop: "
							+ e);
			e.printStackTrace();
			return;
		} finally {
			try {
				telnetWrapper.disconnect();
			} catch (IOException e) {
				System.out
						.println("TelnetClient: got exception in disconnect: "
								+ e);
				e.printStackTrace();
			}
		}
	}

	private static String waitForTelnetServerResponse(InputStream is,
			String end, long timeout) throws Exception {
		byte buffer[] = new byte[32];
		long starttime = System.currentTimeMillis();
		int ret_read = 0;
		String readbytes = new String();
		while ((readbytes.indexOf(end) < 0)
				&& ((System.currentTimeMillis() - starttime) < timeout)) {
			if (is.available() > 0) {
				ret_read = is.read(buffer);
				readbytes = readbytes + new String(buffer, 0, ret_read);
			} else {
				Thread.sleep(5000);
			}
		}
		return (readbytes);
	}

	public void CheckServiceCluster(Connection pCn, int pPortManager)
			throws SQLException, IOException, Exception {
		String mStr = "";
		Statement stmt = null;
		ResultSet rs = null;
		int mPort = 0;
		String mIP = "";
		Socket socket = null;
		InputStream in = null;
		PrintWriter out = null;
		int mRet = 0;
		try {
			mStr = "select * from node_cluster where ip<>'"
					+ Global.getLocalSvrIP() + "' and status='ACTIVE'";
			stmt = pCn.createStatement();
			rs = stmt.executeQuery(mStr);
			while (rs.next()) {
				mPort = rs.getInt("port");
				mIP = rs.getString("ip");
				if (pPortManager == 0) {
					mRet = CheckService(mIP, mPort);
				} else {
					mRet = CheckService(mIP, (mPort + pPortManager));
				}
				if (mRet == 0) {
					if (pPortManager > 0) {
						mStr = "UPDATE data_param SET remote_node_alive=0 "
								+ " WHERE used_getfile=1 AND remote_node_alive=1 "
								+ " AND run_on_node IN ("
								+ "SELECT id FROM node_cluster WHERE ip='"
								+ mIP + "' and status='ACTIVE'" + ")";
						Global.ExecuteSQL(pCn, mStr);
					}
				} else {
					if (pPortManager > 0) {
						mStr = "UPDATE data_param SET remote_node_alive=1 "
								+ " WHERE used_getfile=1 AND remote_node_alive=0 "
								+ " AND run_on_node IN ("
								+ "SELECT id FROM node_cluster WHERE ip='"
								+ mIP + "' and status='ACTIVE'" + ")";
						Global.ExecuteSQL(pCn, mStr);
					}
				}
			}
		} catch (IOException e) {
			if (e.toString().indexOf("Connection refused: connect", 1) > 0) {
				System.out
						.println("Remote NODE CLUSTER not running... Connection refused: connect");
				// Remote services has been down. Main Services is working hard
				// temporary

			} else if (e.toString().indexOf("Network is unreachable: connect",
					1) > 0)
				System.out.println("Network is unreachable: connect.");
			else
				e.printStackTrace();

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			try {
				// Release
				rs.close();
				rs = null;
				stmt.close();
				stmt = null;
				mStr = null;
				out.close();
				in.close();
				socket.close();
			} catch (Exception ex) {
			}
		}
	}

	private int CheckService(String pIP, int pPort) throws Exception {
		Socket socket = null;
		InputStream in = null;
		PrintWriter out = null;
		int alive = 0;
		try {
			writeLogFile(" - Checking service on remote node IP " + pIP
					+ " - port " + pPort);
			socket = new Socket(pIP, pPort);
			in = socket.getInputStream();
			out = new PrintWriter(socket.getOutputStream(), true);
			waitForTelnetServerResponse(in, "Return to continue:", 1000);
			out.println("cdrfiletelnetadmin");
			out.flush();
			waitForTelnetServerResponse(in, "Return to continue:", 1000);
			out.println("cdrfiletelnetadmin");
			out.flush();
			waitForTelnetServerResponse(in, "Return to continue:", 1000);
			if (Global.mPortManager == 1)
				writeLogFile(" - Service FTP has alived.");
			else if (Global.mPortManager == 2)
				writeLogFile(" - Service CONVERT has alived.");
			else if (Global.mPortManager == 3)
				writeLogFile(" - Service CHARGE has alived.");
			else if (Global.mPortManager == 4)
				writeLogFile(" - Service EXPORT has alived.");
			else if (Global.mPortManager == 5)
				writeLogFile(" - Service COLLECT has alived.");
			else if (Global.mPortManager == 6)
				writeLogFile(" - Service GENERAL has alived.");

			out.println("bye");
			out.flush();
			waitForTelnetServerResponse(in, "Return to continue:", 1000);

			alive = 1;
		} catch (IOException e) {
			alive = 0;
			if (e.toString().indexOf("Connection refused: connect", 1) > 0) {
				writeLogFile("Remote NODE CLUSTER not running... Connection refused: connect");
				// Remote services has been down. Main Services is working hard
				// temporary
				writeLogFile("Connection refused: connect.");
			} else if (e.toString().indexOf("Network is unreachable: connect",
					1) > 0) {
				writeLogFile("Network is unreachable: connect.");
			} else if (e.toString().indexOf("No route to host", 1) > 0)
				writeLogFile("No route to host");
			else
				writeLogFile(e.toString());

		} catch (Exception ex) {
			alive = 0;
			writeLogFile(ex.toString());
		} finally {
			try {
				// Release
				out.close();
				in.close();
				socket.close();
			} catch (Exception ex) {
			}
		}
		return alive; // alive
	}

	public void writeLogFile(String pStrLog) throws Exception {
		configLogFile();
		pStrLog = Global.Format(new java.util.Date(), "dd/MM/yyyy HH:mm:ss")
				+ " : " + pStrLog;
		if (mstrLogFileName != null && !mstrLogFileName.equals("")) {
			try {
				RandomAccessFile fl = new RandomAccessFile(mstrLogFileName,
						"rw");
				fl.seek(fl.length());
				fl.writeBytes(pStrLog + "\r\n");
				fl.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void configLogFile() throws Exception {
		java.util.Date dtNow = new java.util.Date();
		IOUtils IOUtil = new IOUtils();
		if (!Global.mstrLogPathFileName.equals("")) {
			if (!Global.mstrLogPathFileName.endsWith("/")
					&& !Global.mstrLogPathFileName.endsWith("\\")) {
				Global.mstrLogPathFileName += "/";
			}
			IOUtil.forceFolderExist(Global.mstrLogPathFileName + "/cluster/"
					+ Global.Format(dtNow, "yyyyMMdd") + "/");
			mstrLogFileName = Global.mstrLogPathFileName + "/cluster/"
					+ Global.Format(dtNow, "yyyyMMdd") + "/ClusterManager.log";

		}
		dtNow = null;
		IOUtil = null;
	}
/*
	private void RemoteClusterStarter(String pHost) {
	}
*/
	class ReaderThread extends Thread {
		TelnetWrapper telnetWrapper;
		PrintStream out;

		ReaderThread(TelnetWrapper telnetWrapper, PrintStream out) {
			super("TelnetReaderThread");
			this.telnetWrapper = telnetWrapper;
			this.out = out;
		}

		public void run() {
			System.out.println("thread-starting");
			byte[] buf = new byte[256];
			int n = 0;
			while (n >= 0) {
				try {
					n = telnetWrapper.read(buf);
					if (n > 0) {
						out.print(new String(buf, 0, n));
					}
				} catch (IOException e) {
					System.out
							.println("ReaderThread.run: got exception in read/write loop: "
									+ e);
					e.printStackTrace();
					return;
				}
			}
		}
	}

	public Thread createAndStartReader(TelnetWrapper telnetWrapper,
			PrintStream out) {
		ReaderThread readerThread = new ReaderThread(telnetWrapper, out);
		readerThread.start();
		return readerThread;
	}

	public int execshell(String strCommand, int iTimeout) throws Exception {
		// Execute command
		Process process = Runtime.getRuntime().exec(strCommand);

		// Run cleaner
		new ProcessCleaner(process);

		StringBuffer strLog = new StringBuffer();
		StringBuffer strErrLog = new StringBuffer();
		long lStartDate = System.currentTimeMillis();
		char cLog;
		char cErrLog;

		try {
			while (true) {
				int iAvailable = process.getInputStream().available();
				int iErrAvailable = process.getErrorStream().available();
				while (iAvailable > 0 || iErrAvailable > 0) {
					if (iAvailable > 0) {
						// Fill log
						cLog = (char) process.getInputStream().read();
						if (cLog == '\n' || cLog == 13) {
							if (strLog.length() > 0) {
								System.out.println("\t" + strLog.toString());
							}
							lStartDate = new java.util.Date().getTime();
							strLog = new StringBuffer();
						} else
							strLog.append(cLog);
						iAvailable--;
					} else {
						// Fill log
						cErrLog = (char) process.getErrorStream().read();
						if (cErrLog == '\n' || cErrLog == 13) {
							if (strErrLog.length() > 0)
							{
								System.out.println("\t" + strErrLog.toString());
							}
							lStartDate = new java.util.Date().getTime();
							strErrLog = new StringBuffer();
						} else
							strErrLog.append(cErrLog);
						iAvailable--;
					}
				}
				if (System.currentTimeMillis() - lStartDate > iTimeout) {
					process.destroy();
					throw new Exception(
							"Time out reached, command execution was destroyed.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return process.exitValue();
	}
}

// End of file.
