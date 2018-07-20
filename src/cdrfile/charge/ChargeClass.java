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

public class ChargeClass {
	// variable for charge_class
	private int ChargeClassID;
	private long DateFrom;
	private long DateTo;
	private int TimeDestroy;
	private int TimeMin;
	private int TimeBlock;
	private int IndexOfChargePlan = -1;

	public ChargeClass(int pChargeClassID, long pDateFrom, long pDateTo,
			int pTimeDestroy, int pTimeMin, int pTimeBlock,
			int pIndexOfChargePlan) {
		ChargeClassID = pChargeClassID;
		DateFrom = pDateFrom;
		DateTo = pDateTo;
		TimeDestroy = pTimeDestroy;
		TimeMin = pTimeMin;
		TimeBlock = pTimeBlock;
		IndexOfChargePlan = pIndexOfChargePlan;
	}

	public int getChargeClassID() {
		return (ChargeClassID);
	}

	public long getDateFrom() {
		return (DateFrom);
	}

	public long getDateTo() {
		return (DateTo);
	}

	public int getTimeDestroy() {
		return (TimeDestroy);
	}

	public int getTimeMin() {
		return (TimeMin);
	}

	public int getTimeBlock() {
		return (TimeBlock);
	}

	public int getIndexOfChargePlan() {
		return (IndexOfChargePlan);
	}
}
