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

public class ListIMSI {
	protected String IMSI;
	protected int CenterID;
	protected int SubsType;

	public ListIMSI(String pIMSI, int pCenterID, int pSubsType) {
		IMSI = pIMSI;
		CenterID = pCenterID;
		SubsType = pSubsType;
	}

	public String getIMSI() {
		return (IMSI);
	}

	public int getCenterID() {
		return (CenterID);
	}

	public int getSubsType() {
		return (SubsType);
	}

}
