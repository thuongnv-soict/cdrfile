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

public class ChargePlan {
	// variable for charge_plan
	private int ChargeClassID;
	private long DateFrom;
	private long DateTo;
	private int TimeFrom;
	private int TimeTo;
	private int FirstBlock;
	private int NextBlock;
	private int CollectType;
	private int WeekInfo;
	private int IndexOfChargeAreaFistBlock = -1;
	private int IndexOfChargeAreaNextBlock = -1;

	public ChargePlan(int pChargeClassID, long pDateFrom, long pDateTo,
			int pTimeFrom, int pTimeTo, int pFirstBlock, int pNextBlock,
			int pCollectType, int pWeekInfo, int pIndexOfChargeAreaFistBlock,
			int pIndexOfChargeAreaNextBlock) {
		ChargeClassID = pChargeClassID;
		DateFrom = pDateFrom;
		DateTo = pDateTo;
		TimeFrom = pTimeFrom;
		TimeTo = pTimeTo;
		FirstBlock = pFirstBlock;
		NextBlock = pNextBlock;
		CollectType = pCollectType;
		WeekInfo = pWeekInfo;
		IndexOfChargeAreaFistBlock = pIndexOfChargeAreaFistBlock;
		IndexOfChargeAreaNextBlock = pIndexOfChargeAreaNextBlock;
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

	public int getTimeFrom() {
		return (TimeFrom);
	}

	public int getTimeTo() {
		return (TimeTo);
	}

	public int getFirstBlock() {
		return (FirstBlock);
	}

	public int getNextBlock() {
		return (NextBlock);
	}

	public int getCollectType() {
		return (CollectType);
	}

	public int getWeekInfo() {
		return (WeekInfo);
	}

	public int getIndexOfChargeAreaFistBlock() {
		return (IndexOfChargeAreaFistBlock);
	}

	public int getIndexOfChargeAreaNextBlock() {
		return (IndexOfChargeAreaNextBlock);
	}
}
