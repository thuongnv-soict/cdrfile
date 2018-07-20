package epg.cdr.sgw;

import java.util.Map;

public class SgwModel {
	private String SGWRecord;		
	private int recordType;		//INTEGER
	private String servedIMSI;	//STRING
	private String sGWAddress;		//CHOICE
	private long chargingID	;	//INTEGER
	private String servingNodeAddress;			//SEQUENCE
	private String accessPontiNameNI;			//IA5String
	private String pdpPDNType	;		//STRING
	private String servedPDPPDNAddress	;		//CHOICE
	private boolean dynamicAddressFlag	;	//BOOLEAN
//	private String listOfTrafficVolumes	;		//SEQUENCE
	private String recordOpeningTime;			//STRING
	private int duration;			//INTEGER
	private int causeForRecClosing;			//INTEGER
	private String diagnostics	;	//CHOICE
	private int recordSequenceNumber;		//INTEGER
	private String nodeID	;	//IA5String
	private int localSequenceNumber	;	//INTEGER
	private String apnSelectionMode	;	//ENUM
	private String servedMSISDN	;	//STRING
	private String chargingCharacteristics;		//STRING
	private String chChSelectionMode;		//ENUM
	private String servingNodePLMNIdentifier;		//STRING
	private String servedIMEISV	;		//STRING
	private int rATType	;		//INTEGER
	private String mSTimeZone	;		//STRING
	private String userLocationInformation;			//STRING
	private boolean sGWChange	;		//BOOLEAN
	private String servingNodeType	;		//SEQUENCE
	private String pGWAddressUsed	;		//CHOICE
	private String pGWPLMNIdentifier;			//STRING
	private String startTime	;		//STRING
	private String stopTime	;		//STRING
	private long pDNConnectionChargingID	;		//INTEGER
	private String servedPDPPDNAddressExt;			//CHOICE
	private String dynamicAddressFlagExt;			//BOOLEAN

	
	
	//not use
	private ChangeOfCharCondition listOfTrafficVolumes	;		//SEQUENCE
	private String iMSSignalingContext	;		//NULL
	private String iMSIunauthenticatedFlag;		//NULL
	private String userCSGInformation;			//SEQUENCE
	private String lowPriorityIndicator	;		//NULL
	private String recordExtension	;		//SEQUENCE
	public String getSGWRecord() {
		return SGWRecord;
	}
	public void setSGWRecord(String sGWRecord) {
		this.SGWRecord = sGWRecord;
	}
	public int getRecordType() {
		return recordType;
	}
	public void setRecordType(int recordType) {
		this.recordType = recordType;
	}
	public String getServedIMSI() {
		return servedIMSI;
	}
	public void setServedIMSI(String servedIMSI) {
		this.servedIMSI = servedIMSI;
	}
	public String getsGWAddress() {
		return sGWAddress;
	}
	public void setsGWAddress(String sGWAddress) {
		this.sGWAddress = sGWAddress;
	}
	public long getChargingID() {
		return chargingID;
	}
	public void setChargingID(long chargingID) {
		this.chargingID = chargingID;
	}
	public String getServingNodeAddress() {
		return servingNodeAddress;
	}
	public void setServingNodeAddress(String servingNodeAddress) {
		this.servingNodeAddress = servingNodeAddress;
	}
	public String getAccessPontiNameNI() {
		return accessPontiNameNI;
	}
	public void setAccessPontiNameNI(String accessPontiNameNI) {
		this.accessPontiNameNI = accessPontiNameNI;
	}
	public String getPdpPDNType() {
		return pdpPDNType;
	}
	public void setPdpPDNType(String pdpPDNType) {
		this.pdpPDNType = pdpPDNType;
	}
	public String getServedPDPPDNAddress() {
		return servedPDPPDNAddress;
	}
	public void setServedPDPPDNAddress(String servedPDPPDNAddress) {
		this.servedPDPPDNAddress = servedPDPPDNAddress;
	}
	public boolean getDynamicAddressFlag() {
		return dynamicAddressFlag;
	}
	public void setDynamicAddressFlag(boolean dynamicAddressFlag) {
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
	public int getCauseForRecClosing() {
		return causeForRecClosing;
	}
	public void setCauseForRecClosing(int causeForRecClosing) {
		this.causeForRecClosing = causeForRecClosing;
	}
	public String getDiagnostics() {
		return diagnostics;
	}
	public void setDiagnostics(String diagnostics) {
		this.diagnostics = diagnostics;
	}
	public int getRecordSequenceNumber() {
		return recordSequenceNumber;
	}
	public void setRecordSequenceNumber(int recordSequenceNumber) {
		this.recordSequenceNumber = recordSequenceNumber;
	}
	public String getNodeID() {
		return nodeID;
	}
	public void setNodeID(String nodeID) {
		this.nodeID = nodeID;
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
	public String getChChSelectionMode() {
		return chChSelectionMode;
	}
	public void setChChSelectionMode(String chChSelectionMode) {
		this.chChSelectionMode = chChSelectionMode;
	}
	public String getServingNodePLMNIdentifier() {
		return servingNodePLMNIdentifier;
	}
	public void setServingNodePLMNIdentifier(String servingNodePLMNIdentifier) {
		this.servingNodePLMNIdentifier = servingNodePLMNIdentifier;
	}
	public String getServedIMEISV() {
		return servedIMEISV;
	}
	public void setServedIMEISV(String servedIMEISV) {
		this.servedIMEISV = servedIMEISV;
	}
	public int getrATType() {
		return rATType;
	}
	public void setrATType(int rATType) {
		this.rATType = rATType;
	}
	public String getmSTimeZone() {
		return mSTimeZone;
	}
	public void setmSTimeZone(String mSTimeZone) {
		this.mSTimeZone = mSTimeZone;
	}
	public String getUserLocationInformation() {
		return userLocationInformation;
	}
	public void setUserLocationInformation(String userLocationInformation) {
		this.userLocationInformation = userLocationInformation;
	}
	public boolean getsGWChange() {
		return sGWChange;
	}
	public void setsGWChange(boolean sGWChange) {
		this.sGWChange = sGWChange;
	}
	public String getServingNodeType() {
		return servingNodeType;
	}
	public void setServingNodeType(String servingNodeType) {
		this.servingNodeType = servingNodeType;
	}
	public String getpGWAddressUsed() {
		return pGWAddressUsed;
	}
	public void setpGWAddressUsed(String pGWAddressUsed) {
		this.pGWAddressUsed = pGWAddressUsed;
	}
	public String getpGWPLMNIdentifier() {
		return pGWPLMNIdentifier;
	}
	public void setpGWPLMNIdentifier(String pGWPLMNIdentifier) {
		this.pGWPLMNIdentifier = pGWPLMNIdentifier;
	}
	public String getStartTime() {
		return startTime;
	}
	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
	public String getStopTime() {
		return stopTime;
	}
	public void setStopTime(String stopTime) {
		this.stopTime = stopTime;
	}
	public long getpDNConnectionChargingID() {
		return pDNConnectionChargingID;
	}
	public void setpDNConnectionChargingID(long pDNConnectionChargingID) {
		this.pDNConnectionChargingID = pDNConnectionChargingID;
	}
	public String getServedPDPPDNAddressExt() {
		return servedPDPPDNAddressExt;
	}
	public void setServedPDPPDNAddressExt(String servedPDPPDNAddressExt) {
		this.servedPDPPDNAddressExt = servedPDPPDNAddressExt;
	}
	public String getDynamicAddressFlagExt() {
		return dynamicAddressFlagExt;
	}
	public void setDynamicAddressFlagExt(String dynamicAddressFlagExt) {
		this.dynamicAddressFlagExt = dynamicAddressFlagExt;
	}
	public ChangeOfCharCondition getListOfTrafficVolumes() {
		return listOfTrafficVolumes;
	}
	public void setListOfTrafficVolumes(ChangeOfCharCondition listOfTrafficVolumes) {
		this.listOfTrafficVolumes = listOfTrafficVolumes;
	}
	public String getiMSSignalingContext() {
		return iMSSignalingContext;
	}
	public void setiMSSignalingContext(String iMSSignalingContext) {
		this.iMSSignalingContext = iMSSignalingContext;
	}
	public String getiMSIunauthenticatedFlag() {
		return iMSIunauthenticatedFlag;
	}
	public void setiMSIunauthenticatedFlag(String iMSIunauthenticatedFlag) {
		this.iMSIunauthenticatedFlag = iMSIunauthenticatedFlag;
	}
	public String getUserCSGInformation() {
		return userCSGInformation;
	}
	public void setUserCSGInformation(String userCSGInformation) {
		this.userCSGInformation = userCSGInformation;
	}
	public String getLowPriorityIndicator() {
		return lowPriorityIndicator;
	}
	public void setLowPriorityIndicator(String lowPriorityIndicator) {
		this.lowPriorityIndicator = lowPriorityIndicator;
	}
	public String getRecordExtension() {
		return recordExtension;
	}
	public void setRecordExtension(String recordExtension) {
		this.recordExtension = recordExtension;
	}
	
}


// Class listOfTrafficVolumes


