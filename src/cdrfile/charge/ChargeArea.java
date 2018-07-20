package cdrfile.charge;

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

public class ChargeArea {
	// variable for charge_area
	private String AreaChargeID;
	private long DateFrom;
	private long DateTo;
	private double Tax;

	public ChargeArea(String pAreaChargeID, long pDateFrom, long pDateTo,
			double pTax) {
		AreaChargeID = pAreaChargeID;
		DateFrom = pDateFrom;
		DateTo = pDateTo;
		Tax = pTax;
	}

	public String getAreaChargeID() {
		return (AreaChargeID);
	}

	public long getDateFrom() {
		return (DateFrom);
	}

	public long getDateTo() {
		return (DateTo);
	}

	public double getTax() {
		return (Tax);
	}
}
