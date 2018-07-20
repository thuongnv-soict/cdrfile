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

public class ChargeTariff {
	private int CenterID;
	private String CallingISDN;
	private String CalledISDN;
	private String ZoneCodeParentID;
	private long DateFrom;
	private long DateTo;
	private int AirtimeTariff;
	private int IddSerTariff;
	private int HolidayDeduct;
	private int IndexOfChargeClassAir = -1;
	private int IndexOfChargeClassIddSer = -1;
	private int ProvinceHeader = 0;
	private int IndexOfChargeTariffSer = -1;

	public ChargeTariff(int pCenterID, String pCallingISDN, String pCalledISDN,
			long pDateFrom, long pDateTo, int pAirtimeTariff,
			int pIddSerTariff, int pHolidayDeduct, int pIndexOfChargeClassAir,
			int pIndexOfChargeClassIddSer, int pProvinceHeader,
			int pIndexOfChargeTariffSer) {
		CenterID = pCenterID;
		CallingISDN = pCallingISDN;
		CalledISDN = pCalledISDN;
		DateFrom = pDateFrom;
		DateTo = pDateTo;
		AirtimeTariff = pAirtimeTariff;
		IddSerTariff = pIddSerTariff;
		HolidayDeduct = pHolidayDeduct;
		IndexOfChargeClassAir = pIndexOfChargeClassAir;
		IndexOfChargeClassIddSer = pIndexOfChargeClassIddSer;
		ProvinceHeader = pProvinceHeader;
		IndexOfChargeTariffSer = pIndexOfChargeTariffSer;
	}

	public ChargeTariff(int pCenterID, String pCallingISDN, String pCalledISDN,
			long pDateFrom, long pDateTo, int pAirtimeTariff,
			int pIddSerTariff, int pHolidayDeduct, int pIndexOfChargeClassAir,
			int pIndexOfChargeClassIddSer, String pZoneCodeParentID) {
		CenterID = pCenterID;
		CallingISDN = pCallingISDN;
		CalledISDN = pCalledISDN;
		DateFrom = pDateFrom;
		DateTo = pDateTo;
		AirtimeTariff = pAirtimeTariff;
		IddSerTariff = pIddSerTariff;
		HolidayDeduct = pHolidayDeduct;
		IndexOfChargeClassAir = pIndexOfChargeClassAir;
		IndexOfChargeClassIddSer = pIndexOfChargeClassIddSer;
		ZoneCodeParentID = pZoneCodeParentID;
	}

	public ChargeTariff(int pCenterID, String pCallingISDN, String pCalledISDN,
			long pDateFrom, long pDateTo, int pAirtimeTariff,
			int pIddSerTariff, int pHolidayDeduct, int pIndexOfChargeClassAir,
			int pIndexOfChargeClassIddSer) {
		CenterID = pCenterID;
		CallingISDN = pCallingISDN;
		CalledISDN = pCalledISDN;
		DateFrom = pDateFrom;
		DateTo = pDateTo;
		AirtimeTariff = pAirtimeTariff;
		IddSerTariff = pIddSerTariff;
		HolidayDeduct = pHolidayDeduct;
		IndexOfChargeClassAir = pIndexOfChargeClassAir;
		IndexOfChargeClassIddSer = pIndexOfChargeClassIddSer;
	}

	public ChargeTariff(int pCenterID, String pCallingISDN, String pCalledISDN,
			long pDateFrom, long pDateTo, int pAirtimeTariff,
			int pIddSerTariff, int pHolidayDeduct, int pIndexOfChargeClassAir) {
		CenterID = pCenterID;
		CallingISDN = pCallingISDN;
		CalledISDN = pCalledISDN;
		DateFrom = pDateFrom;
		DateTo = pDateTo;
		AirtimeTariff = pAirtimeTariff;
		IddSerTariff = pIddSerTariff;
		HolidayDeduct = pHolidayDeduct;
		IndexOfChargeClassAir = pIndexOfChargeClassAir;
	}

	public int getCenterID() {
		return (CenterID);
	}

	public String getCallingISDN() {
		return (CallingISDN);
	}

	public String getCalledISDN() {
		return (CalledISDN);
	}

	public String getZoneCodeParentID() {
		return (ZoneCodeParentID);
	}

	public long getDateFrom() {
		return (DateFrom);
	}

	public long getDateTo() {
		return (DateTo);
	}

	public int getAirtimeTariff() {
		return (AirtimeTariff);
	}

	public int getIddSerTariff() {
		return (IddSerTariff);
	}

	public int getHolidayDeduct() {
		return (HolidayDeduct);
	}

	public int getIndexOfChargeClassAir() {
		return (IndexOfChargeClassAir);
	}

	public int getIndexOfChargeClassIddSer() {
		return (IndexOfChargeClassIddSer);
	}

	public int getIndexOfChargeTariffSer() {
		return (IndexOfChargeTariffSer);
	}

	public int getProvinceHeader() {
		return (ProvinceHeader);
	}
}
