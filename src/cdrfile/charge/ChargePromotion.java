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

public class ChargePromotion {
	private long DateFrom;
	private long DateTo;
	private int TimeFrom;
	private int TimeTo;
	private double PercentageDeduct;

	public ChargePromotion(long pDateFrom, long pDateTo, int pTimeFrom,
			int pTimeTo, double pPercentageDeduct) {
		DateFrom = pDateFrom;
		DateTo = pDateTo;
		TimeFrom = pTimeFrom;
		TimeTo = pTimeTo;
		PercentageDeduct = pPercentageDeduct;
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

	public double getPercentageDeduct() {
		return (PercentageDeduct);
	}
}
