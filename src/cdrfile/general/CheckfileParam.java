package cdrfile.general;

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

public class CheckfileParam {
	private int FtpID = 0;
	private long TimeCurrentCheck = 0;
        private long timeCurrentCheckContent = 0;

	public CheckfileParam(int pFtpID, long pTimeCurrentCheck, long pTimeCurrentCheckContent) {
		FtpID = pFtpID;
		TimeCurrentCheck = pTimeCurrentCheck;
                timeCurrentCheckContent = pTimeCurrentCheckContent;
	}

	public int getFtpID() {
		return (FtpID);
	}

	public long getTimeCurrentChecked() {
		return (TimeCurrentCheck);
	}
        public long getTimeCurrentCheckedContent(){
            return timeCurrentCheckContent;
        }

	public void setFtpID(int pFtpID) {
		FtpID = pFtpID;
	}

	public void setTimeCurrentCheck(long pTimeCurrentCheck) {
		TimeCurrentCheck = pTimeCurrentCheck;
	}
        public void setTimeCurrentCheckContent(long pTimeCurrentCheckContent) {
                timeCurrentCheckContent = pTimeCurrentCheckContent;
        }

}
