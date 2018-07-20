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

public class ListData {
	private long FileSize;
	private String FileInfo;
	private String FileName;
	private String FilePath;
	private String FileDir;
	private long FileDateLongTime;

	public ListData(String pFilePath, String pFileName, long pFileSize,
			String pFileInfo, String pFileDir, long pFileDateLongTime) {
		FilePath = pFilePath;
		FileName = pFileName;
		FileSize = pFileSize;
		FileInfo = pFileInfo;
		FileDir = pFileDir;
		FileDateLongTime = pFileDateLongTime;
	}

	public String getFilePath() {
		return (FilePath);
	}

	public String getFileName() {
		return (FileName);
	}

	public long getFileSize() {
		return (FileSize);
	}

	public String getFileInfo() {
		return (FileInfo);
	}

	public String getFileDir() {
		return (FileDir);
	}

	public long getFileDateLongTime() {
		return (FileDateLongTime);
	}
}
