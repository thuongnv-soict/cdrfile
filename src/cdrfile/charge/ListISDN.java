package cdrfile.charge;

/**
 * <p>
 * Title: CDR File System
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

public class ListISDN {
	protected String ISDN;
	protected int CenterID;
	protected int SubsType;

	public ListISDN(String pISDN, int pCenterID, int pSubsType) {
		ISDN = pISDN;
		CenterID = pCenterID;
		SubsType = pSubsType;
	}

	public String getISDN() {
		return (ISDN);
	}

	public int getCenterID() {
		return (CenterID);
	}

	public int getSubsType() {
		return (SubsType);
	}

}
