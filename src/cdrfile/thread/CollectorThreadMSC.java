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

import cdrfile.collector.Collection;
import cdrfile.global.Global;
import cdrfile.global.IOUtils;

public class CollectorThreadMSC extends ThreadInfo {

	public void finalize() {
		destroy();
		System.runFinalization();
		System.gc();
	}

	public void processSession() throws Exception {
		// int mRet = 0;
		Collection collec = new Collection();
		IOUtils IOUtil = new IOUtils();
		collec.setThreadID(getThreadID());
		collec.setThreadName(getThreadName());
		collec.setLogPathFileName(getLogPathFileName());
		Statement stmt = mConnection.createStatement();
		String mSQL = null;
		ResultSet rs = stmt
				.executeQuery("SELECT b.file_name,b.file_id,"
						+ " a.file_type, a.collect_dir, a.id,"
						+ " b.current_dir, a.local_split_file_by_day "
						+ " FROM data_param a, import_header b,node_cluster c "
						+ " WHERE b.status="
						+ Global.StateExportedData
						+ " AND a.run_on_node=c.id AND c.ip='"
						+ Global.getLocalSvrIP()
						+ "' AND a.file_type in ('ALCATEL_R6','ALCATEL_R9','ERICSSON_R10','ERICSSON_R11') "
						+ " AND a.id=b.ftp_id ORDER BY b.file_id");
		try {
			while (rs.next() && miThreadCommand != THREAD_STOP) {
				mConnection.rollback();
				mConnection.setAutoCommit(false);
				mSQL = "UPDATE import_header SET time_begin_collective=sysdate ";
				mSQL += "WHERE file_id=" + rs.getInt("file_id");
				Global.ExecuteSQL(mConnection, mSQL);
				writeLogFile(" - Beginning collective Traffic and TurnOver Revenue file "
						+ rs.getString("file_name")
						+ " - "
						+ rs.getInt("file_id"));
				if (rs.getInt("local_split_file_by_day") == 1)
					collec.CollectiveSwitch(mConnection, rs.getInt("id"),
							IOUtil.FillPath(rs.getString("collect_dir"),
									Global.mSeparate)
									+ rs.getString("current_dir"), rs
									.getString("file_name"));
				else
					collec.CollectiveSwitch(mConnection, rs.getInt("id"), rs
							.getString("collect_dir"), rs
							.getString("file_name"));

				writeLogFile(" - File " + rs.getString("file_name") + " - "
						+ rs.getInt("file_id") + " collective successful");

				writeLogFile(" - Beginning collective traffic by CELL file "
						+ rs.getString("file_name") + " - "
						+ rs.getInt("file_id"));

				if (rs.getInt("local_split_file_by_day") == 1)
					collec.CollectiveSwitchCell(mConnection, rs.getInt("id"),
							IOUtil.FillPath(rs.getString("collect_dir"),
									Global.mSeparate)
									+ rs.getString("current_dir"), rs
									.getString("file_name"));
				else
					collec.CollectiveSwitchCell(mConnection, rs.getInt("id"),
							rs.getString("collect_dir"), rs
									.getString("file_name"));

				writeLogFile(" - File " + rs.getString("file_name") + " - "
						+ rs.getInt("file_id")
						+ " collective traffic by CELL successful");

				mSQL = "UPDATE import_header SET time_end_collective=sysdate,";
				mSQL += "status=" + Global.StateCollectTrafficTurnover;
				mSQL += " WHERE file_id=" + rs.getInt("file_id");
				Global.ExecuteSQL(mConnection, mSQL);
				mConnection.commit();
			}
		} catch (Exception e) {
			mConnection.rollback();
			Global.writeEventThreadErr(Integer.parseInt(getThreadID()), 2, e
					.toString());
			throw e;
		} finally {
			try {
				collec = null;
				rs.close();
				rs = null;
				stmt.close();
				stmt = null;
				IOUtil = null;
			} catch (Exception e) {
			}
		}
	}
}
