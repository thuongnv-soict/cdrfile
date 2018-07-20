package cdrfile.convert;

/**
 * @author galaxy
 * @createDate 3:02:06 PM
 * StructLTEEricsson.java
 *
 */
public class StructEricssonLTE {
	//sGWRecord
	public int recordTypeNum;
	public String recordTypeStr;
	public String servedIMSI;
	public String sGWiPBinV4Address;
	public long chargingID;
	public String servingNodeiPBinV4Address;
	public String accessPointNameNI;
	public String pdpPDNType;
	public String servedPDPPDNiPBinV4Address;
	public String servedPDPPDNiPBinV6Address;
	public int dataVolumeGPRSUplink;
	public int dataVolumeGPRSDownlink;
	public int changeConditionNum;
	public String changeConditionStr;
	public String changeTime;
	public String userLocationInformation;
	public int ePCQoSqCI;
	public int ePCQoSmaxRequestedBandwithUL;
	public int ePCQoSmaxRequestedBandwithDL;
	public int ePCQoSguaranteedBitrateUL;
	public int ePCQoSguaranteedBitrateDL;
	public int ePCQoSaRP;
	public String recordOpeningTime;
	public int duration;
	public int causeForRecClosingNum;
	public String causeForRecClosingStr;
	public int recordSequenceNumber;
	public String nodeID;
	public long localSequenceNumber;
	public String servedMSISDN;
	public String chargingCharacteristics;
	public String servingNodePLMNIdentifier;
	public String servedIMEISV;
	public int rATTypeNum;
	public String rATTypeStr;
	public String mSTimeZone;
	public String sGWChangeStr;
	public int sGWChangeNum;
	public String servingNodeTypeStr;
	public int servingNodeTypeNum;
	public String pGWiPBinV4Address;
	public String pGWiPBinV6Address;
	public String pGWPLMNIdentifier;
	public long pDNConnectionID;
	public String iMSIunauthenticatedFlag;
	public String servedPDPPDNiPBinV4AddressExt;
	public String servedPDPPDNiPBinV6AddressExt;
	public String sGWiPv6Address;
	public String servingNodeiPv6Address;
	public String pGWiPv6AddressUsed;
	
	//SGSNPDPRecord
	//public int recordType;
	public String networkInitiation;
	//public String servedIMSI;
	public String servedIMEI;
	public String sgsniPBinV4Address;
	public String msNetworkCapability;
	public String routingArea;
	public String locationAreaCode;
	public String cellIdentifier;
	//public String chargingID;
	public String ggsniPBinV4Address;
	//public String accessPointNameNI;
	public String pdpType;

	//servedPDPAddress
	public String servedPDPiPBinV4Address;
	public String servedPDPiPBinV6Address;

	//listOfTrafficVolumes
	public String qosRequested;
	public String qosNegotiated;
	//public String dataVolumeGPRSUplink;
	//public String dataVolumeGPRSDownlink;
	//public String changeCondition;
	//public String changeTime;
	//public String userLocationInformation;

	//public String recordOpeningTime;
	//public String duration;
	public String sgsnChange;
	//public String causeForRecClosing;

	//diagnostics
	public int gsm0408Cause;
	public int gsm0902MapErrorValue;

	//public String recordSequenceNumber;
	//public String nodeID;
	
	//recordExtensions
	public String identifier;
	public String significance;
	public String ts48018BssgpCause;
	public String ts25413RanapCause;
	public String bssgpExttsBssgpRanapCauseBssgp;
	public String bssgpExttsBssgpRanapMessageType;
	public String bssgpExttsBssgpRanapMessageSource;
	public String bssgpExttsBssgpRanapCauseTimeStamp;
	public String ranapExttsBssgpRanapCause;
	public String ranapExttsBssgpRanapMessageType;
	public String ranapExttsBssgpRanapMessageSource;
	public String ranapExttsBssgpRanapCauseTimeStamp;

	//public String localSequenceNumber;
	public int apnSelectionModeNum;
	public String apnSelectionModeStr;
	public String accessPointNameOI;
	//public String servedMSISDN;
	//public String chargingCharacteristics;
	//public String rATType;

	//cAMELInformationPDP
	public String sCFAddress;
	public int serviceKey;
	public String defaultTransactionHandling;
	public String cAMELAccessPointNameNI;
	public String cAMELAccessPointNameOI;
	public int numberOfDPEncountered;
	public int levelOfCAMELServiceNum;
	public String levelOfCAMELServiceStr;
	public String freeFormatData;
	public String FDAppendIndicator;

	public String chChSelectionModeStr;
	public int chChSelectionModeNum;
	public String dynamicAddressFlag;
	public String pLMNIdentifier;
	//public String mSTimeZone;

	//SGSNSMORecord
	//public String recordType;
	//public String servedIMSI;
	//public String servedIMEI;
	//public String servedMSISDN;
	//public String msNetworkCapability;
	public String serviceCentre;
	public String recordingEntity;
	public String locationArea;
	//public String routingArea;
	//public String cellIdentifier;
	public String messageReference;
	public String eventTimeStamp;

	//smsResult
	//public String gsm0408Cause;
	//public String gsm0902MapErrorValue;

	//public String nodeID;
	//public String localSequenceNumber;
	//public String chargingCharacteristics;
	//public String rATType;
	public String destinationNumber;

	//cAMELInformationSMS
	//public String sCFAddress;
	//public String serviceKey;
	public String defaultSMSHandlingStr;
	public int defaultSMSHandlingNum;
	public String cAMELCallingPartyNumber;
	public String cAMELDestinationSubscriberNumber;
	public String cAMELSMSCAddress;
	//public String freeFormatData;
	public String smsReferenceNumber;

	//public String chChSelectionMode;
	//public String pLMNIdentifier;

	//SGSNSMTRecord
	//public String recordType;
	//public String servedIMSI;
	//public String servedIMEI;
	//public String servedMSISDN;
	//public String msNetworkCapability;
	//public String serviceCentre;
	//public String recordingEntity;
	//public String locationArea;
	//public String routingArea;
	//public String cellIdentifier;
	//public String eventTimeStamp;

	//smsResult
	//public String gsm0408Cause;
	//public String gsm0902MapErrorValue;

	//public String nodeID;
	//public String localSequenceNumber;
	//public String chargingCharacteristics;
	//public String rATType;
	//public String chChSelectionMode;
	public String numberOfSM;
	public String locationAreaLastSM;
	public String routingAreaLastSM;
	public String cellIdentifierLastSM;
	public String pLMNIdentifierLastSM;
	//public String pLMNIdentifier;
	
	//GGSNPDPRecord
	//Release 97
	//public String recordType;
	//public String servedIMSI;
	//public String ggsniPBinV4Address;
	//public String chargingID;
	//public String sgsniPBinV4Address;
	public String accessPointName;
	//public String pdpType;
	//public String servedPDPiPBinV4Address;
	//public String servedPDPiPBinV6Address;
	//public String dynamicAddressFlag;
	//qoSNegotiated
	public String reliability;
	public String delay;
	public String precedence;
	public String peakThroughput;
	public String meanThroughput;
	//public String dataVolumeGPRSUplink;
	//public String dataVolumeGPRSDownlink;
	public String changeCondition;
	//public String changeTime;
	//public String recordOpeningTime;
	//public String duration;
	public String causeForRecClosing;
	//public String recordSequenceNumber;
	//public String nodeID;
	//public String servedMSISDN;
	public String sgsnPLMNIdentifier;

	//Release 98
	//public String recordType;
	//public String servedIMSI;
	//public String ggsniPBinV4Address;
	//public String chargingID;
	//public String sgsniPBinV4Address;
	//public String accessPointName;
	//public String pdpType;
	//public String servedPDPiPBinV4Address;
	//public String servedPDPiPBinV6Address;
	//public String dynamicAddressFlag;
	//qoSNegotiated
	//public String reliability;
	//public String delay;
	//public String precedence;
	//public String peakThroughput;
	//public String meanThroughput;
	//public String dataVolumeGPRSUplink;
	//public String dataVolumeGPRSDownlink;
	//public String changeCondition;
	//public String changeTime;
	//public String recordOpeningTime;
	//public String duration;
	//public String causeForRecClosing;
	//public String recordSequenceNumber;
	//public String nodeID;
	//public String localSequenceNumber;
	public String apnSelectionMode;
	//public String servedMSISDN;
	//public String sgsnPLMNIdentifier;

	//Release 99
	//public String recordType;
	//public String servedIMSI;
	//public String ggsniPBinV4Address;
	//public String chargingID;
	//public String sgsniPBinV4Address;
	//public String accessPointNameNI;
	//public String pdpType;
	//public String servedPDPiPBinV4Address;
	//public String servedPDPiPBinV6Address;
	//public String dynamicAddressFlag;
	//qoSNegotiated
	//gsmQosInformation
	//public String reliability;
	//public String delay;
	//public String precedence;
	//public String peakThroughput;
	//public String meanThroughput;
	public String umtsQosInformation;
	//public String dataVolumeGPRSUplink;
	//public String dataVolumeGPRSDownlink;
	//public String changeCondition;
	//public String changeTime;
	//public String recordOpeningTime;
	//public String duration;
	//public String causeForRecClosing;
	//public String recordSequenceNumber;
	//public String nodeID;
	//public String localSequenceNumber;
	//public String apnSelectionMode;
	//public String servedMSISDN;
	//public String chargingCharacteristics;
	//public String sgsnPLMNIdentifier;

	//Release 4
	//public String recordType;
	//public String servedIMSI;
	//public String ggsniPBinV4Address;
	//public String chargingID;
	//public String sgsniPBinV4Address;
	//public String accessPointNameNI;
	//public String pdpType;
	//public String servedPDPiPBinV4Address;
	//public String servedPDPiPBinV6Address;
	//public String dynamicAddressFlag;
	public String qoSNegotiated;
	//public String dataVolumeGPRSUplink;
	//public String dataVolumeGPRSDownlink;
	//public String changeCondition;
	//public String changeTime;
	//public String recordOpeningTime;
	//public String duration;
	//public String causeForRecClosing;
	//public String recordSequenceNumber;
	//public String nodeID;
	//public String localSequenceNumber;
	//public String apnSelectionMode;
	//public String servedMSISDN;
	//public String chargingCharacteristics;
	//public String chChSelectionMode;
	//public String sgsnPLMNIdentifier;

	//Release 5
	//public String recordType;
	//public String servedIMSI;
	//public String ggsniPBinV4Address;
	//public String chargingID;
	//public String sgsniPBinV4Address;
	//public String accessPointNameNI;
	//public String pdpType;
	//public String servedPDPiPBinV4Address;
	//public String servedPDPiPBinV6Address;
	//public String dynamicAddressFlag;
	//public String qoSNegotiated;
	//public String dataVolumeGPRSUplink;
	//public String dataVolumeGPRSDownlink;
	//public String changeCondition;
	//public String changeTime;
	//public String recordOpeningTime;
	//public String duration;
	//public String causeForRecClosing;
	//public String recordSequenceNumber;
	//public String nodeID;
	//public String localSequenceNumber;
	//public String apnSelectionMode;
	//public String servedMSISDN;
	//public String chargingCharacteristics;
	//public String chChSelectionMode;
	public String iMSsignalingContext;
	//public String sgsnPLMNIdentifier;
	//public String servedIMEISV;
	//public String rATType;

	//Release 6
	//public String recordType;
	//public String servedIMSI;
	//public String ggsniPBinV4Address;
	//public String chargingID;
	//public String sgsniPBinV4Address;
	//public String accessPointNameNI;
	//public String pdpType;
	//public String servedPDPiPBinV4Address;
	//public String servedPDPiPBinV6Address;
	//public String dynamicAddressFlag;
	//public String qoSNegotiated;
	//public String dataVolumeGPRSUplink;
	//public String dataVolumeGPRSDownlink;
	//public String changeCondition;
	//public String changeTime;
	//public String recordOpeningTime;
	//public String duration;
	//public String causeForRecClosing;
	//public String recordSequenceNumber;
	//public String nodeID;
	//recordExtensions
	//public String localSequenceNumber;
	//public String apnSelectionMode;
	//public String servedMSISDN;
	//public String chargingCharacteristics;
	//public String chChSelectionMode;
	//public String iMSsignalingContext;
	//public String sgsnPLMNIdentifier;
	//public String servedIMEISV;
	//public String rATType;
	//public String mSTimeZone;
	//public String userLocationInformation;

	//Release 7
	//public String recordType;
	//public String servedIMSI;
	//public String ggsniPBinV4Address;
	//public String chargingID;
	//public String sgsniPBinV4Address;
	//public String accessPointNameNI;
	//public String pdpType;
	//public String servedPDPiPBinV4Address;
	//public String servedPDPiPBinV6Address;
	//public String dynamicAddressFlag;
	//public String qoSNegotiated;
	//public String dataVolumeGPRSUplink;
	//public String dataVolumeGPRSDownlink;
	//public String changeCondition;
	//public String changeTime;
	//public String userLocationInformation;
	//public String recordOpeningTime;
	//public String duration;
	//public String causeForRecClosing;
	//public String recordSequenceNumber;
	//public String nodeID;
	//recordExtensions
	//public String localSequenceNumber;
	//public String apnSelectionMode;
	//public String servedMSISDN;
	//public String chargingCharacteristics;
	//public String chChSelectionMode;
	//public String iMSsignalingContext;
	//public String sgsnPLMNIdentifier;
	//public String servedIMEISV;
	//public String rATType;
	//public String mSTimeZone;
	//public String userLocationInformation;

	//EGSNPDPRecord
	//Release 6
	//public String recordType;
	//public String servedIMSI;
	//public String ggsniPBinV4Address;
	//public String chargingID;
	//public String sgsniPBinV4Address;
	//public String accessPointNameNI;
	//public String pdpType;
	//public String servedPDPiPBinV4Address;
	//public String servedPDPiPBinV6Address;
	//public String dynamicAddressFlag;
	//public String qoSNegotiated;
	//public String dataVolumeGPRSUplink;
	//public String dataVolumeGPRSDownlink;
	//public String changeCondition;
	//public String changeTime;
	//public String recordOpeningTime;
	//public String duration;
	//public String causeForRecClosing;
	//public String recordSequenceNumber;
	//public String nodeID;
	//recordExtensions
	//public String localSequenceNumber;
	//public String apnSelectionMode;
	//public String servedMSISDN;
	//public String chargingCharacteristics;
	//public String chChSelectionMode;
	//public String iMSsignalingContext;
	//public String sgsnPLMNIdentifier;
	public String pSFreeFormatDataBC;
	public String pSFFDAppendIndicatorBC;
	//public String servedIMEISV;
	//public String rATType;
	//public String mSTimeZone;
	//public String userLocationInformation;
	public int ratingGroup;
	public int resultCode;
	//public String localSequenceNumber;
	public String timeOfFirstUsage;
	public String timeOfLastUsage;
	public int timeUsage;
	public String serviceConditionChange;
	public String qoSInformationNeg;
	public String sgsniPBinV4AddressServiceData;
	public String sgsniPBinV6AddressServiceData;
	public String sGSNPLMNIdentifier;
	public String datavolumeFBCUplink;
	public String datavolumeFBCDownlink;
	public String timeOfReport;
	//public String rATType;
	public String failureHandlingContinue;
	public String serviceIdentifier;
	public String pSFreeFormatDataB2;
	public String pSFFDAppendIndicatorB2;
	public String aFRecordInformation;

	//Release 7
	//public String recordType;
	//public String servedIMSI;
	//public String ggsniPBinV4Address;
	//public String chargingID;
	//public String sgsniPBinV4Address;
	//public String accessPointNameNI;
	//public String pdpType;
	//public String servedPDPiPBinV4Address;
	//public String servedPDPiPBinV6Address;
	//public String dynamicAddressFlag;
	//public String qoSNegotiated;
	//public String dataVolumeGPRSUplink;
	//public String dataVolumeGPRSDownlink;
	//public String changeCondition;
	//public String changeTime;
	//public String userLocationInformation;
	//public String recordOpeningTime;
	//public String duration;
	//public String causeForRecClosing;
	//public String recordSequenceNumber;
	//public String nodeID;
	//recordExtensions
	//public String localSequenceNumber;
	//public String apnSelectionMode;
	//public String servedMSISDN;
	//public String chargingCharacteristics;
	//public String chChSelectionMode;
	//public String iMSsignalingContext;
	//public String sgsnPLMNIdentifier;
	//public String pSFreeFormatDataBC;
	//public String pSFFDAppendIndicatorBC;
	//public String servedIMEISV;
	//public String rATType;
	//public String mSTimeZone;
	//public String userLocationInformation;

	//listOfServiceData
	//public String ratingGroup;
	//public String resultCode;
	//public String localSequenceNumber;
	//public String timeOfFirstUsage;
	//public String timeOfLastUsage;
	//public String timeUsage;
	//public String serviceConditionChange;
	//public String qoSInformationNeg;
	//public String sgsniPBinV4AddressServiceData;
	//public String sgsniPBinV6AddressServiceData;
	//public String sGSNPLMNIdentifier;
	//public String datavolumeFBCUplink;
	//public String datavolumeFBCDownlink;
	//public String timeOfReport;
	//public String rATType;
	//public String failureHandlingContinue;
	//public String serviceIdentifier;
	//public String pSFreeFormatDataB2;
	//public String pSFFDAppendIndicatorB2;
	//public String aFRecordInformation;
	public String userLocationInformationServiceData;
	public String localSequenceNumberServiceData;
	public String numberOfEvents;
	public String eventTimeStamps;

	//PGWRecord
	//Release 8
	//public String recordType;
	//public String servedIMSI;
	//public String pGWiPBinV4Address;
	//public String chargingID;
	//public String servingNodeiPBinV4Address;
	//public String accessPointNameNI;
	//public String pdpPDNType;
	//public String servedPDPPDNiPBinV4Address;
	//public String servedPDPPDNiPBinV6Address;
	//public String dynamicAddressFlag;
	//public String dataVolumeGPRSUplink;
	//public String dataVolumeGPRSDownlink;
	//public String changeCondition;
	//public String changeTime;
	//public String userLocationInformation;
	//public String qCI;
	//public String maxRequestedBandwithUL;
	//public String maxRequestedBandwithDL;
	//public String guaranteedBitrateUL;
	//public String guaranteedBitrateDL;
	//public String aRP;
	//public String recordOpeningTime;
	//public String duration;
	//public String causeForRecClosing;
	//public String recordSequenceNumber;
	//public String nodeID;

	//recordExtensions
	//public String identifier;
	//public String significance;
	public String requestTypeCreditControl;
	public String requestStatusCreditControl;
	public String resultCodeCreditControl;
	public String ccRequestNumberCreditControl;
	public String creditControlSessionId;
	public String ccsRealm;
	public String requestTypePolicyControl;
	public String requestStatusPolicyControl;
	public String resultCodePolicyControl;
	public String stopTimePolicyControl;
	public String pcsRealm;
	public String policyControlSessionId;
	public String userCategory;
	public String ruleSpaceId;

	//ServiceContainer
	public String ratingGroupServiceContainer;
	//public String serviceIdentifier;
	//public String localSequenceNumber;
	public String method;
	public String inactivity;
	public String resolution;
	public String ccRequestNumber;
	public String serviceSpecificUnits;

	//listOfURI
	public String count;
	public String uri;
	public String uriIdentifier;
	public String uriDataVolumeUplink;
	public String uriDataVolumeDownlink;
	public String listOfUriTimeStamps;
	public String uriTimeStamp;

	//TimeReport
	public String ratingGroupTimeReport;
	public String startTimeReport;
	public String endTime;
	public String dataVolumeUplink;
	public String dataVolumeDownlink;

	//public String localSequenceNumber;
	//public String apnSelectionMode;
	//public String servedMSISDN;
	//public String chargingCharacteristics;
	//public String chChSelectionMode;
	//public String iMSsignalingContext;
	//public String servingNodePLMNIdentifier;
	//public String pSFreeFormatDataBC;
	//public String pSFFDAppendIndicatorBC;
//	public String servedIMEISV;
	//public String rATType;
	///public String mSTimeZone;
	//public String userLocationInformation;
	public String userLocationInformation3GPP2;

	//listOfServiceData
	//public String ratingGroup;
	//public String resultCode;
	//public String localSequenceNumber;
	//public String timeOfFirstUsage;
	//public String timeOfLastUsage;
	//public String timeUsage;
	//public String serviceConditionChange;
	//public String qCI;
	//public String maxRequestedBandwithUL;
	//public String maxRequestedBandwithDL;
	//public String guaranteedBitrateUL;
	//public String guaranteedBitrateDL;
	//public String aRP;
	//public String servingNodeiPBinV4Address;
	//public String datavolumeFBCUplink;
	//public String datavolumeFBCDownlink;
	//public String timeOfReport;
	//public String failureHandlingContinue;
	//public String serviceIdentifier;
	public String pSFreeFormatDataServiceData;
	public String pSFFDAppendIndicatorServiceData;
	public String afChargingIdentifier;
	//public String userLocationInformationServiceData;
	public String userLocationInformationServiceData3GPP2;
	//public String numberOfEvents;
	//public String eventTimeStamps;
	public String servingNodeType;
	//public String pGWPLMNIdentifier;
	public String startTime;
	public String stopTime;
	//public String pDNConnectionID;
	//public String servedPDPPDNiPBinV4AddressExt;
	public String userLocationInformationTrafficVolumes;
}
