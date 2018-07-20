package cdrfile.ftpClient;

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

public class FtpParam {
	private int FtpID = 0;
	private String FileName;
	private long FileSize;
	private String Status;
	private int TimeConnect = 0;
	private int TimeDownload = 0;
	private long TimeCurrentConnect = 0;

	public FtpParam(int pFtpID, String pFileName, long pFileSize,
			String pStatus, int pTimeConnect, int pTimeDownload,
			long pTimeCurrentConnect) {
		FtpID = pFtpID;
		FileName = pFileName;
		FileSize = pFileSize;
		Status = pStatus;
		TimeConnect = pTimeConnect;
		TimeDownload = pTimeDownload;
		TimeCurrentConnect = pTimeCurrentConnect;
	}

	public int getFtpID() {
		return (FtpID);
	}

	public String getFileName() {
		return (FileName);
	}

	public long getFileSize() {
		return (FileSize);
	}

	public String getStatus() {
		return (Status);
	}

	public int getTimeConnect() {
		return (TimeConnect);
	}

	public int getTimeDownoad() {
		return (TimeDownload);
	}

	public long getTimeCurrentConnect() {
		return (TimeCurrentConnect);
	}

	public void setFtpID(int pFtpID) {
		FtpID = pFtpID;
	}

	public void setFileName(String pFileName) {
		FileName = pFileName;
	}

	public void setFileSize(long pFileSize) {
		FileSize = pFileSize;
	}

	public void setStatus(String pStatus) {
		Status = pStatus;
	}

	public void setTimeConnect(int pTimeConnect) {
		TimeConnect = pTimeConnect;
	}

	public void setTimeDownload(int pTimeDownload) {
		TimeDownload = pTimeDownload;
	}

	public void setTimeCurrentConnect(long pTimeCurrentConnect) {
		TimeCurrentConnect = pTimeCurrentConnect;
	}
}