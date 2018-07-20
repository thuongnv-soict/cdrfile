package cdrfile.convert;

/**
 * <p>Title: CDR File(s) System</p>
 * <p>Description: VMS IS Departerment</p>
 * <p>Copyright: VHC</p>
 * <p>Company: VietNam Mobile Telecom Services</p>
 * @author: VHC
 * @version: 1.0
 */
public class StructGGSN {

	public int recordType;
	//networkInitiation;
	public String servedIMSI;
	public String ggsnAddress;
	public int chargingID;
	public String sgsnAddress;
	public String accessPointNameNI;
	public String pdpType;
	public String servedPDPAddress;
	public String dynamicAddressFlag;
	public String recordOpeningTime;
	public long duration;
	public int causeForRecClosing;
	public String causeForRecClosingValue;
	public String diagnostics;
	public String diagnosticsValue;
	public long recordSequenceNumber;
	public String nodeID;
	//public String recordExtensions;				// bo
	public long localSequenceNumber;
	public String apnSelectionMode;
	public String servedMSISDN;
	public String chargingCharacteristics;
	public String chChSelectionMode;
	public String sgsnPLMNIdentifier;
	//public String pSFurnishChargingInformation;	// bo
	public String servedIMEISV;
	public int rATType;
	public String rATTypeValue;
	public String mSTimeZone;
	public String userLocationInformation;
	//public long datavolumeFBCUplink;
	//public long datavolumeFBCDownlink;
	
	public int sdRatingGroup;
	public String sdChargingRuleBaseName;
	public int sdResultCode;
	public long sdLocalSequenceNumber;
	public String sdTimeOfFirstUsage;
	public String sdTimeOfLastUsage;
	public long sdTimeUsage;
	public String sdServiceConditionChange;
	public String sdQoSInformationNeg;
	public String sdSgsnAddress;
	public String sdSgsnPLMNIdentifier;
	public long sdDatavolumeFBCUplink;
	public long sdDatavolumeFBCDownlink;
	public String sdTimeOfReport;
	public int sdRATType;
	public int sdFailureHandlingContinue;
	public String sdFailureHandlingContinueValue;
	public int sdServiceIdentifier;
	public String sdUserLocationInformation;
	
}
