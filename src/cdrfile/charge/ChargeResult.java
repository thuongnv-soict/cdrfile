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
public class ChargeResult {
	public int numOfBlockAir = 0;
	public int numOfBlockIddSer = 0;
	public String strPO_CODE = "";
	public String strPO_Name = "";
	public int intCenOfCalling = 0;
	public int intCenOfCalled = 0;
	public int intCollectType = 0;
	public double dblTaxAir = 0;
	public double dblTaxIdd = 0;
	public double dblTaxSer = 0;
	public int intSubsType = 0;
	// Variable for rating and charging
	public double TaxFirstBlock = 0;
	public double TaxNextBlock = 0;

	public ChargeResult() {
	}

	public void clear() {
		numOfBlockAir = 0;
		numOfBlockIddSer = 0;
		strPO_CODE = "";
		intCenOfCalling = 0;
		intCenOfCalled = 0;
		intCollectType = 0;
		dblTaxAir = 0;
		dblTaxIdd = 0;
		dblTaxSer = 0;
		intSubsType = 0;
		TaxFirstBlock = 0;
		TaxNextBlock = 0;
	}
}
