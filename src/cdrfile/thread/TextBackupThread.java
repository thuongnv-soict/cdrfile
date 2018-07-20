package cdrfile.thread;

import cdrfile.global.Global;
import java.sql.ResultSet;
import java.sql.Statement;
import cdrfile.zip.TextBackupUtil;

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
public class TextBackupThread extends ThreadInfo {
    public TextBackupThread() {
    }

    public void finalize() {
        destroy();
        System.runFinalization();
        System.gc();
    }

    public void processSession() throws Exception {
        writeLogFile("Text Backup thread is running.");
        String mSQL = "SELECT a.id,";
        mSQL += "a.zip_backup_info_text,";
        mSQL += "a.local_putfile_dir,";
        mSQL += "a.zip_backup_dir_text,";
        mSQL += "a.local_split_file_by_day,";
        mSQL += "a.split_zip_backup_by_month,";
        mSQL += "a.note,";
        mSQL += "to_char(sysdate-decode(nvl(a.zip_backup_after_days,0),0,1,a.zip_backup_after_days),'yyyymmddhh24miss') last_date_backup,";
        mSQL += "a.file_name_last_backup_text,";
        mSQL += "a.mail_to";
        mSQL += " FROM data_param a,node_cluster b";
        mSQL += " WHERE a.zip_backup_dir is not null";
        mSQL += " AND nvl(a.zip_backup_after_days,0)>0 ";
        mSQL += " AND a.used_getfile=1";
        mSQL += " AND a.run_on_node=b.id";
        mSQL += " AND TEXTBACKUP_THREAD_ID = " + getThreadID();
        mSQL += " AND a.run_on_node=b.id";
        mSQL += " AND b.ip='" + Global.getLocalSvrIP();
        mSQL += "' AND convert_thread_ID is not null";
        mSQL += "  ORDER BY a.id ";
        Statement stmt = mConnection.createStatement();
        ResultSet rs = stmt.executeQuery(mSQL);
        TextBackupUtil backup = new TextBackupUtil(getThreadID(), getThreadName(),
                getLogPathFileName());
        try {
            while (rs.next() && miThreadCommand != THREAD_STOP) {
                backup.backup(mConnection,
                              rs.getInt("id"),
                              rs.getString("note"),
                              rs.getString("zip_backup_info_text"),
                              rs.getString("local_putfile_dir"),
                              rs.getString("zip_backup_dir_text"),
                              rs.getInt("split_zip_backup_by_month"),
                              rs.getInt("local_split_file_by_day"),
                              rs.getString("last_date_backup"),
                              rs.getString("file_name_last_backup_text"),
                              rs.getString("mail_to"));
            }
        } catch (Exception e) {
            writeLogFile(" - " + e.toString());
        } finally {
            backup = null;
            rs.close();
            rs = null;
            stmt.close();
            stmt = null;
            mSQL = "";
        }
    }

}
