package cdrfile.convert;

/**
 * <p>Title: CDR File(s) System</p>
 * <p>Description: VMS IS Departerment</p>
 * <p>Copyright: Copyright (c)  by eKnowledge 2012</p>
 * <p>Company: VietNam Mobile Telecom Services</p>
 * @author eKnowledge - Software
 * @version 7.0
 */

//----------------------------------------------------------------------------
//Change History
//2013.11.04 datnh
//		- Them column numberOfSequence
//----------------------------------------------------------------------------

public class StructSGSN
{
    public int recordType;
    public String networkInitiation;
    public String servedIMSI;
    public String servedIMEI;
    public String sgsnAddress;
    public String msNetworkCapability;
    public String routingAreaCode;
    public int locationAreaCode;
    public int cellIdentifier;
    public int chargingID;
    public String ggsnAddressUsed;
    public String accessPointNameNI;
    public String pDPType;
    public String servedPDPAddress;
    //public String listOfTrafficVolumes;
    public String qosRequested;
    public String qosNegotiated;
    public long dataVolumeGPRSUplink;
    public long dataVolumeGPRSDownlink;
    public String changeCondition;
    public String changeTime;
    public String failureHandlingContinue;
    public String userLocationInformation;
    public String recordOpeningTime;
    public long duration;
    public String sGSNChange;
    public int causeForRecClosing;
    public long diagnostics;
    public int recordSequenceNumber;
    public String nodeId;
    public String recordExtensions;
    public long localSequenceNumber;
    public String aPNSelectionMode;
    public String accessPointNameOI;
    public String servedMSISDN;
    public String chargingCharacteristics;
    public int rATType;
    public String cAMELInformationPDP;
    public int rNCUnsentDownlinkVolume;
    public String chChSelectionMode;
    public String dynamicAddressFlag;
    public int numberOfSequence;
}
