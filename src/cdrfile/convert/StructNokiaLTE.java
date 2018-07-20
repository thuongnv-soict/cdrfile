package cdrfile.convert;

import java.util.HashMap;
import java.util.Map;

public class StructNokiaLTE {

	public int recordType;
	public String severedIMSI;
	public String pgwAddress;
	public long chargingId;
	public String servingNodeAddress;
	public String accessPointNameID;
	public String pdpPDNType;
	public String severedPDPPDNAdress;
	public String dynamicAddressFlag;
	public String recordOpeningTime;
	public int duration;
	public String causeForRecordClosing;
	public final static Map<Integer, String> causeForRecordClosingMap = new HashMap<Integer, String>();
	public String Diagnostics;
	public final static Map<Integer, String> diagnoticsMap = new HashMap<Integer, String>();
	public long recordSequenceNumber;
	public String nodeId;
	public int localSequenceNumber;
	public String apnSelectionMode;
	public final static Map<Integer, String> apnSelectionModeMap = new HashMap<Integer, String>();
	public String servedMSISDN;
	public String chargingCharacteristics;
	public String chargingCharacteristicsSelectionMode;
	public final static Map<Integer, String> chargingCharacteristicsSMMap = new HashMap<Integer, String>();
	public String servingNodePLMNId;
	
	public String servedIMEISV;
	public int RATType;
	public String rATType;
	public final static Map<Integer, String> rATTypeMap = new HashMap<Integer, String>();
	public String msTimeZone;
	public String userLocationInfor;
	public String MCCMNC;
	
	// fileds of list of service data
//	public String PlistOfServiceData;
	public int ratingGroupId;
	public String chargingRuleBaseName;
	public int resultCode;
	public int localSequenceNumberListData;
	public String timeOfFirstUsage;
	public String timeOfLastUsage;
	public int timeUsage;
	public String serviceConditionChange;
	public String qoSInformationNeg;
	public String servingNodeAddressListData;
	public String SGSNPLMNIdentifier;
	public int datavolumeFBCUplink;
	public int datavolumeFBCDownlink;
	public String timeOfReport;
	public String failureHandlingContinue;
	public int serviceIdentifier;
	public String userLocationInforListData;
	public String pSFurnishChargingInforListData;
	public String aFRecordInformation;
	
	public int eventBasedNumberOfEvents;
	public String eventBasedEventTimeStamps;
	
	public String timeQuotaType;
	public int baseTimeInterval;
	
	public String NetworkInitiatedPDPContext;
	
	public String iMSSignalingContext;
	public String externalChargingID;
	
	public String CAMELInformation;
	
	public String pSFurnishChargingInfor;
	public String PSFurnishChargingInfor;

	public String recordExtensions;
	public String networkInitiation;
	public String msNetworkCapability;
	public String accessPointNameOI;
	public String rNCUnsentDownlinkVolume;
	public String numberOfSeq;

	public String sGWAddress;
	public boolean sGWChange;
	public String pGWAddressUsed;	
	public long pDNConnectionChargingID;
	public String pGWPLMNIdentifier;
	
	public String routingAreaCode;
	public String locationAreaCode;
	
	public StructNokiaLTE() {
		super();
		routingAreaCode = "";
		locationAreaCode="";
		sGWAddress = "";
		pGWAddressUsed = "";
		severedIMSI = "";
		pgwAddress = "";
		servingNodeAddress = "";
		accessPointNameID = "";
		pdpPDNType = "";
		severedPDPPDNAdress = "";
		recordOpeningTime = "";
		causeForRecordClosing = "";
		Diagnostics = "";
		nodeId = "";
		servedMSISDN = "";
		chargingCharacteristics = "";
		chargingCharacteristicsSelectionMode = "";
		servingNodePLMNId = "";
		pSFurnishChargingInfor = "";
		servedIMEISV = "";
		rATType = "";
		msTimeZone = "";
		userLocationInfor = "";
		MCCMNC = "";
		

	//	PlistOfServiceData = "";
		chargingRuleBaseName = "";
		timeOfFirstUsage = "";
		timeOfLastUsage = "";
		serviceConditionChange = "";
		qoSInformationNeg = "";
		servingNodeAddressListData = "";
		SGSNPLMNIdentifier = "";
		timeOfReport = "";
		failureHandlingContinue = "";

		pSFurnishChargingInfor = "";
		pSFurnishChargingInforListData = "";
		userLocationInforListData = "";
		aFRecordInformation = "";
		eventBasedEventTimeStamps = "";
		timeQuotaType = "";
		
		NetworkInitiatedPDPContext = "";
		iMSSignalingContext = "";
		externalChargingID = "";
		PSFurnishChargingInfor ="";
		CAMELInformation = "";
		
		recordExtensions = "";
		networkInitiation = "";
		msNetworkCapability = "";
		accessPointNameOI = "";
		rNCUnsentDownlinkVolume = "";
		numberOfSeq = "";
		
		
		
		apnSelectionModeMap.put(0, "mSorNetworkProvidedSubscriptionVerified");
		apnSelectionModeMap.put(1,"mSProvidedSubscriptionNotVerified");
		apnSelectionModeMap.put(2, "networkProvidedSubscriptionNotVerified");
		
		causeForRecordClosingMap.put(0, "NormalRelease");
		causeForRecordClosingMap.put(4, "AbnormalRelease");
		causeForRecordClosingMap.put(16, "VolumeLimit");
		causeForRecordClosingMap.put(17, "TimeLimit");
		causeForRecordClosingMap.put(18, "ServingNodeChange");
		causeForRecordClosingMap.put(19, "MaxChangeCondition");
		causeForRecordClosingMap.put(20, "managementIntervention");
		  causeForRecordClosingMap.put(21, "intraSGSNIntersystemChange");
		  causeForRecordClosingMap.put(22, "rATChange");
		  causeForRecordClosingMap.put(22, "RATChange");
		  causeForRecordClosingMap.put(23, "MSTimeZoneChange");
		  causeForRecordClosingMap.put(24, "SGSNPLMNIDChange");
		  causeForRecordClosingMap.put(52, "unauthorizedRequestingNetwork");
		  causeForRecordClosingMap.put(53, "unauthorizedLCSClient");
		  causeForRecordClosingMap.put(54, "positionMethodFailure");
		  causeForRecordClosingMap.put(58, "unknownOrUnreachableLCSClient");
		  causeForRecordClosingMap.put(59, "listofDownstreamNodeChange");

		diagnoticsMap.put(8, "DeletionRequested");
		diagnoticsMap.put(36, "SessionTimerExpired");
		diagnoticsMap.put(37, "IdleSessionTimerExpired");
		diagnoticsMap.put(57, "RADIUSDisconnected");
		diagnoticsMap.put(59, "CLIBearerDisconnectionRequest");
		diagnoticsMap.put(70, "OCSDisconnected");
		diagnoticsMap.put(11, "ServingNodeUnreachable");
		diagnoticsMap.put(30, "ErrorIndicationReceivedFromTheServingNode(SN)");
		diagnoticsMap.put(35, "FlexiNGConfigurationChanged");
		diagnoticsMap.put(42, "UpdateIPBearerRequestToTheSNHasFailed");
		diagnoticsMap.put(56, "IPBearerNotFound");
		diagnoticsMap.put(71, "TEIDConflictTheSNAssignedTheBearer�sUserPlaneTEIDToAnotherBearer");
		diagnoticsMap.put(89, "RoamingStatusChangesNotAllowed");
		diagnoticsMap.put(100, "DiameterServerWasUnreachableDueToALackOfTransportConnection");
		diagnoticsMap.put(101, "DiameterServerDidNotRespondWithinTheAllowedTime");
		diagnoticsMap.put(102, "DiameterServerSentAResultCodeInTheCCAIndicatingAnError");
		diagnoticsMap.put(103, "DiameterServerSentAnInvalidCCA");
		diagnoticsMap.put(104, "DiameterServerSentAnAbortSessionRequest");
		chargingCharacteristicsSMMap.put(0, "ServingNodeSupplied");
		chargingCharacteristicsSMMap.put(3, "HomeDefault");
		chargingCharacteristicsSMMap.put(4, "RoamingDefault");
		chargingCharacteristicsSMMap.put(5, "VisitingDefault");
		rATTypeMap.put(1, "UTRAN");
		rATTypeMap.put(2, "GERAN");
		rATTypeMap.put(3, "WLAN");
		rATTypeMap.put(4, "GAN");
		rATTypeMap.put(5, "HSPA�evolution");
		rATTypeMap.put(6, "EUTRAN");
		rATTypeMap.put(7, "virtual");


	}



	public String getRoutingAreaCode() {
		return routingAreaCode;
	}



	public void setRoutingAreaCode(String routingAreaCode) {
		this.routingAreaCode = routingAreaCode;
	}



	public String getLocationAreaCode() {
		return locationAreaCode;
	}



	public void setLocationAreaCode(String locationAreaCode) {
		this.locationAreaCode = locationAreaCode;
	}



	public int getRecordType() {
		return recordType;
	}

	public void setRecordType(int recordType) {
		this.recordType = recordType;
	}

	public String getSeveredIMSI() {
		return severedIMSI;
	}

	public void setSeveredIMSI(String severedIMSI) {
		this.severedIMSI = severedIMSI;
	}

	public String getPgwAddress() {
		return pgwAddress;
	}

	public void setPgwAddress(String pgwAddress) {
		this.pgwAddress = pgwAddress;
	}

	public long getChargingId() {
		return chargingId;
	}

	public void setCharingId(long chargingId) {
		this.chargingId = chargingId;
	}

	public String getServingNodeAddress() {
		return servingNodeAddress;
	}

	public void setServingNodeAddress(String servingNodeAddress) {
		this.servingNodeAddress = servingNodeAddress;
	}

	public String getAccessPointNameID() {
		return accessPointNameID;
	}

	public void setAccessPointNameID(String accessPointNameID) {
		this.accessPointNameID = accessPointNameID;
	}

	public String getSeveredPDPPDNAdress() {
		return severedPDPPDNAdress;
	}

	public void setSeveredPDPDNPAdress(String severedPDPPDNAdress) {
		this.severedPDPPDNAdress = severedPDPPDNAdress;
	}

	public String isDynamicAddressFlag() {
		return dynamicAddressFlag;
	}

	public void setDynamicAddressFlag(String dynamicAddressFlag) {
		this.dynamicAddressFlag = dynamicAddressFlag;
	}

	public String getRecordOpeningTime() {
		return recordOpeningTime;
	}

	public void setRecordOpeningTime(String recordOpeningTime) {
		this.recordOpeningTime = recordOpeningTime;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getCauseForRecordClosing() {
		return causeForRecordClosing;
	}

	public void setCauseForRecordClosing(String causeForRecordClosing) {
		this.causeForRecordClosing = causeForRecordClosing;
	}

	public String getDiagnostics() {
		return Diagnostics;
	}

	public void setDiagnostics(String diagnostics) {
		Diagnostics = diagnostics;
	}

	public long getRecordSequenceNumber() {
		return recordSequenceNumber;
	}

	public void setRecordSequenceNumber(long recordSequenceNumber) {
		this.recordSequenceNumber = recordSequenceNumber;
	}

	public String getNodeId() {
		return nodeId;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}

	public int getLocalSequenceNumber() {
		return localSequenceNumber;
	}

	public void setLocalSequenceNumber(int localSequenceNumber) {
		this.localSequenceNumber = localSequenceNumber;
	}

	public String getApnSelectionMode() {
		return apnSelectionMode;
	}

	public void setApnSelectionMode(String apnSelectionMode) {
		this.apnSelectionMode = apnSelectionMode;
	}

	public String getServedMSISDN() {
		return servedMSISDN;
	}

	public void setServedMSISDN(String servedMSISDN) {
		this.servedMSISDN = servedMSISDN;
	}

	public String getChargingCharacteristics() {
		return chargingCharacteristics;
	}

	public void setChargingCharacteristics(String chargingCharacteristics) {
		this.chargingCharacteristics = chargingCharacteristics;
	}

	public String getChargingCharacteristicsSelectionMode() {
		return chargingCharacteristicsSelectionMode;
	}

	public void setChargingCharacteristicsSelectionMode(String chargingCharacteristicsSelectionMode) {
		this.chargingCharacteristicsSelectionMode = chargingCharacteristicsSelectionMode;
	}

	public String getServingNodePLMNId() {
		return servingNodePLMNId;
	}

	public void setServingNodePLMNId(String servingNodePLMNId) {
		this.servingNodePLMNId = servingNodePLMNId;
	}

	public String getpSFurnishChargingInfor() {
		return pSFurnishChargingInfor;
	}

	public void setpSFurnishChargingInfor(String pSFurnishChargingInfor) {
		this.pSFurnishChargingInfor = pSFurnishChargingInfor;
	}

	public String getServedIMEISV() {
		return servedIMEISV;
	}

	public void setServedIMEISV(String servedIMEISV) {
		this.servedIMEISV = servedIMEISV;
	}
	public int getRATType() {
		return RATType;
	}

	public void setRATType(int RATType) {
		this.RATType = RATType;
	}
	public String getrATType() {
		return rATType;
	}

	public void setrATType(String rATType) {
		this.rATType = rATType;
	}

	public String getPdpPDNType() {
		return pdpPDNType;
	}

	public void setPdpPDNType(String pdpPDNType) {
		this.pdpPDNType = pdpPDNType;
	}

	public String getMsTimeZone() {
		return msTimeZone;
	}

	public void setMsTimeZone(String msTimeZone) {
		this.msTimeZone = msTimeZone;
	}

	public String getUserLocationInfor() {
		return userLocationInfor;
	}

	public void setMCCMNC(String MCCMNC) {
		this.MCCMNC = MCCMNC;
	}
	public String getMCCMNC() {
		return MCCMNC;
	}

	public void setUserLocationInfor(String userLocationInfor) {
		this.userLocationInfor = userLocationInfor;
	}
	
	public int getRatingGroupId() {
		return ratingGroupId;
	}

	public void setRatingGroupId(int ratingGroupId) {
		this.ratingGroupId = ratingGroupId;
	}

	public String getChargingRuleBaseName() {
		return chargingRuleBaseName;
	}

	public void setChargingRuleBaseName(String chargingRuleBaseName) {
		this.chargingRuleBaseName = chargingRuleBaseName;
	}

	public int getResultCode() {
		return resultCode;
	}

	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}

	public int getLocalSequenceNumberListData() {
		return localSequenceNumberListData;
	}

	public void setLocalSequenceNumberListData(int localSequenceNumberListData) {
		this.localSequenceNumberListData = localSequenceNumberListData;
	}

	public String getTimeOfFirstUsage() {
		return timeOfFirstUsage;
	}

	public void setTimeOfFirstUsage(String timeOfFirstUsage) {
		this.timeOfFirstUsage = timeOfFirstUsage;
	}

	public String getTimeOfLastUsage() {
		return timeOfLastUsage;
	}

	public void setTimeOfLastUsage(String timeOfLastUsage) {
		this.timeOfLastUsage = timeOfLastUsage;
	}

	public int getTimeUsage() {
		return timeUsage;
	}

	public void setTimeUsage(int timeUsage) {
		this.timeUsage = timeUsage;
	}

	public String getServiceConditionChange() {
		return serviceConditionChange;
	}

	public void setServiceConditionChange(String serviceConditionChange) {
		this.serviceConditionChange = serviceConditionChange;
	}
	
	public String getSGSNPLMNIdentifier() {
		return SGSNPLMNIdentifier;
	}

	public void setSGSNPLMNIdentifier(String SGSNPLMNIdentifier) {
		this.SGSNPLMNIdentifier = SGSNPLMNIdentifier;
	}

	public int getDatavolumeFBCUplink() {
		return datavolumeFBCUplink;
	}

	public void setDatavolumeFBCUplink(int datavolumeFBCUplink) {
		this.datavolumeFBCUplink = datavolumeFBCUplink;
	}

	public int getDatavolumeFBCDownlink() {
		return datavolumeFBCDownlink;
	}

	public void setDatavolumeFBCDownlink(int datavolumeFBCDownlink) {
		this.datavolumeFBCDownlink = datavolumeFBCDownlink;
	}

	public String getTimeOfReport() {
		return timeOfReport;
	}

	public void setTimeOfReport(String timeOfReport) {
		this.timeOfReport = timeOfReport;
	}

	public String getFailureHandlingContinue() {
		return failureHandlingContinue;
	}

	public void setFailureHandlingContinue(String failureHandlingContinue) {
		this.failureHandlingContinue = failureHandlingContinue;
	}

	public int getServiceIdentifier() {
		return serviceIdentifier;
	}

	public void setServiceIdentifier(int serviceIdentifier) {
		this.serviceIdentifier = serviceIdentifier;
	}

	public String getpSFurnishChargingInforListData() {
		return pSFurnishChargingInforListData;
	}

	public void setpSFurnishChargingInforListData(String pSFurnishChargingInforListData) {
		this.pSFurnishChargingInforListData = pSFurnishChargingInforListData;
	}

	public String getaFRecordInformation() {
		return aFRecordInformation;
	}

	public void setaFRecordInformation(String aFRecordInformation) {
		this.aFRecordInformation = aFRecordInformation;
	}

	public String getUserLocationInforListData() {
		return userLocationInforListData;
	}

	public void setUserLocationInforListData(String userLocationInforListData) {
		this.userLocationInforListData = userLocationInforListData;
	}

	
	public int geteventBasedNumberOfEvents() {
		return eventBasedNumberOfEvents;
	}

	public void seteventBasedNumberOfEvents(int eventBasedNumberOfEvents) {
		this.eventBasedNumberOfEvents = eventBasedNumberOfEvents;
	}
	
	public String geteventBasedEventTimeStamps() {
		return eventBasedEventTimeStamps;
	}

	public void seteventBasedEventTimeStamps(String eventBasedEventTimeStamps) {
		this.eventBasedEventTimeStamps = eventBasedEventTimeStamps;
	}
	
	
	public String gettimeQuotaType() {
		return timeQuotaType;
	}

	public void settimeQuotaType(String timeQuotaType) {
		this.timeQuotaType = timeQuotaType;
	}
	
	public int getbaseTimeInterval() {
		return baseTimeInterval;
	}

	public void setbaseTimeInterval(int baseTimeInterval) {
		this.baseTimeInterval = baseTimeInterval;
	}
	public String getNetworkInitiatedPDPContext() {
		return NetworkInitiatedPDPContext;
	}

	public void setNetworkInitiatedPDPContext(String NetworkInitiatedPDPContext) {
		this.NetworkInitiatedPDPContext = NetworkInitiatedPDPContext;
	}
	public String getiMSSignalingContext() {
		return iMSSignalingContext;
	}

	public void setiMSSignalingContext(String iMSSignalingContext) {
		this.iMSSignalingContext = iMSSignalingContext;
	}
	
	public String getexternalChargingID() {
		return externalChargingID;
	}

	public void setexternalChargingID(String externalChargingID) {
		this.externalChargingID = externalChargingID;
	}
	public String getCAMELInformation() {
		return CAMELInformation;
	}

	public void setCAMELInformation(String CAMELInformation) {
		this.CAMELInformation = CAMELInformation;
	}
	public String getPSFurnishChargingInfor() {
		return PSFurnishChargingInfor;
	}

	public void setPSFurnishChargingInfor(String PSFurnishChargingInfor) {
		this.PSFurnishChargingInfor = PSFurnishChargingInfor;
	}
	public void setChargingId(long chargingId) {
		this.chargingId = chargingId;
	}

	public void setSeveredPDPPDNAdress(String severedPDPPDNAdress) {
		this.severedPDPPDNAdress = severedPDPPDNAdress;
	}

	public String getQoSInformationNeg() {
		return qoSInformationNeg;
	}

	public void setQoSInformationNeg(String qoSInformationNeg) {
		this.qoSInformationNeg = qoSInformationNeg;
	}

	public String getServingNodeAddressListData() {
		return servingNodeAddressListData;
	}

	public void setServingNodeAddressListData(String servingNodeAddressListData) {
		this.servingNodeAddressListData = servingNodeAddressListData;
	}

	public String getsGWAddress() {
		return sGWAddress;
	}

	public void setsGWAddress(String sGWAddress) {
		this.sGWAddress = sGWAddress;
	}

	public boolean issGWChange() {
		return sGWChange;
	}

	public void setsGWChange(boolean sGWChange) {
		this.sGWChange = sGWChange;
	}

	public String getpGWAddressUsed() {
		return pGWAddressUsed;
	}

	public void setpGWAddressUsed(String pGWAddressUsed) {
		this.pGWAddressUsed = pGWAddressUsed;
	}

	public long getpDNConnectionChargingID() {
		return pDNConnectionChargingID;
	}

	public void setpDNConnectionChargingID(long pDNConnectionChargingID) {
		this.pDNConnectionChargingID = pDNConnectionChargingID;
	}

	public String getDynamicAddressFlag() {
		return dynamicAddressFlag;
	}


	public String getpGWPLMNIdentifier() {
		return pGWPLMNIdentifier;
	}

	public void setpGWPLMNIdentifier(String pGWPLMNIdentifier) {
		this.pGWPLMNIdentifier = pGWPLMNIdentifier;
	}
	
	public String getrecordExtensions() {
		return recordExtensions;
	}

	public void setrecordExtensions(String recordExtensions) {
		this.recordExtensions = recordExtensions;
	}

	public String getnetworkInitiation() {
		return networkInitiation;
	}

	public void setnetworkInitiation(String networkInitiation) {
		this.networkInitiation = networkInitiation;
	}
	public String getmsNetworkCapability() {
		return msNetworkCapability;
	}

	public void setmsNetworkCapability(String msNetworkCapability) {
		this.msNetworkCapability = msNetworkCapability;
	}
	public String getaccessPointNameOI() {
		return accessPointNameOI;
	}

	public void setaccessPointNameOI(String accessPointNameOI) {
		this.accessPointNameOI = accessPointNameOI;
	}
	public String getrNCUnsentDownlinkVolume() {
		return rNCUnsentDownlinkVolume;
	}

	public void setrNCUnsentDownlinkVolume(String rNCUnsentDownlinkVolume) {
		this.rNCUnsentDownlinkVolume = rNCUnsentDownlinkVolume;
	}
	public String getnumberOfSeq() {
		return numberOfSeq;
	}

	public void setnumberOfSeq(String numberOfSeq) {
		this.numberOfSeq = numberOfSeq;
	}

}
