package cdrfile.global;

/**
 * <p>Title: CDR File(s) System</p>
 * <p>Description: VMS IS Departerment</p>
 * <p>Copyright: Copyright (c)  by eKnowledge 2004</p>
 * <p>Company: VietNam Mobile Telecom Services</p>
 * @author eKnowledge - Software
 * @version 1.0
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ClientUtil {
	private static Properties fApplicationConfig = new Properties();

	static {
		loadApplicationConfig();
	}

	public static void loadApplicationConfig() {
		try {
			fApplicationConfig.load((ClientUtil.class)
					.getResourceAsStream("/cdrfile/cdrfile.cfg"));
		} catch (Exception e) {
			fApplicationConfig.clear();
//			e.printStackTrace();
			System.exit(0);
		}
	}

	public static Connection openNewConnection() throws Exception {
		try {
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			Global.strUrl = fApplicationConfig.getProperty("DBURL");
			Global.strUserName = fApplicationConfig.getProperty("DBUserName");
			Global.strPassword = fApplicationConfig.getProperty("DBPassword");

			if (Global.strUserName.compareTo("cdrfile") == 0) {
				 //System.out.println(Crypt.getPassword(DriverManager.
				 //getConnection(Global.strUrl, Global.strUserName,
				 //Global.strPassword), Global.OwnerDB));
				 return DriverManager.getConnection(Global.strUrl,
						Global.OwnerDB, Crypt
								.getPassword(DriverManager.getConnection(
										Global.strUrl, Global.strUserName,
										Global.strPassword), Global.OwnerDB));
			} else
				return DriverManager.getConnection(Global.strUrl,
						Global.strUserName, Global.strPassword);
		} catch (SQLException e) {
			loadApplicationConfig();
			Global.strUserName = fApplicationConfig.getProperty("DBUserName");
			throw e;
		}
	}
}
