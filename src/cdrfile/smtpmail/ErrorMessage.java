package cdrfile.smtpmail;

/**
 * <p>
 * Title: CDR File System
 * </p>
 * <p>
 * Description: VMS IS Departerment
 * </p>
 * <p>
 * Copyright: Copyright (c) by eKnowledge 2005
 * </p>
 * <p>
 * Company: VietNam Mobile Telecom Services
 * </p>
 * 
 * @author eKnowledge - Software
 * @version 1.0
 */

public class ErrorMessage {
	private String errName;
	private String errMess;
	private String mailTo;

	public ErrorMessage(String perrName, String perrMess, String pmailTo) {
		errName = perrName;
		errMess = perrMess;
		mailTo = pmailTo;
	}

	public String getErrName() {
		return (errName);
	}

	public String getErrMess() {
		return (errMess);
	}

	public String getMailTo() {
		return (mailTo);
	}

	public void setErrMess(String pErrMess) {
		errMess = pErrMess;
	}
}
