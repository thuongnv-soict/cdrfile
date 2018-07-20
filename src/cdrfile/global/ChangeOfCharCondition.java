package cdrfile.global;

public class ChangeOfCharCondition{
	private int qosRequested;    //-- NOT YET SUPPORTED
	private int qosNegotiated;   //-- NOT YET SUPPORTED
	private int dataVolumeGPRSUplink; //DataVolumeGPRS OPTIONAL,
	private int dataVolumeGPRSDownlink; //DataVolumeGPRS OPTIONAL,
	private String changeCondition;       //ChangeCondition,
	private String changeTime;            // TimeStamp,
	private String userLocationInformation; // OCTET STRING OPTIONAL, -- NOT YET SUPPORTED
	
	private int ePCQoSInfor_qCI;                       //[1] INTEGER,
	private int ePCQoSInfor_maxRequestedBandwithUL;    //[2] INTEGER OPTIONAL,
	private int ePCQoSInfor_maxRequestedBandwithDL;    //[3] INTEGER OPTIONAL,
	private int ePCQoSInfor_guaranteedBitrateUL;       //[4] INTEGER OPTIONAL,
	private int ePCQoSInfor_guaranteedBitrateDL;       //[5] INTEGER OPTIONAL,
	private int ePCQoSInfor_aRP ;                      //[6] INTEGER OPTIONAL,
	private int ePCQoSInfor_aPNAggregateMaxBitrateUL;  //[7] INTEGER OPTIONAL,
	private int ePCQoSInfor_aPNAggregateMaxBitrateDL;  //[8] INTEGER OPTIONAL
	public int getQosRequested() {
		return qosRequested;
	}
	public void setQosRequested(int qosRequested) {
		this.qosRequested = qosRequested;
	}
	public int getQosNegotiated() {
		return qosNegotiated;
	}
	public void setQosNegotiated(int qosNegotiated) {
		this.qosNegotiated = qosNegotiated;
	}
	public int getDataVolumeGPRSUplink() {
		return dataVolumeGPRSUplink;
	}
	public void setDataVolumeGPRSUplink(int dataVolumeGPRSUplink) {
		this.dataVolumeGPRSUplink = dataVolumeGPRSUplink;
	}
	public int getDataVolumeGPRSDownlink() {
		return dataVolumeGPRSDownlink;
	}
	public void setDataVolumeGPRSDownlink(int dataVolumeGPRSDownlink) {
		this.dataVolumeGPRSDownlink = dataVolumeGPRSDownlink;
	}
	public String getChangeCondition() {
		return changeCondition;
	}
	public void setChangeCondition(String changeCondition) {
		this.changeCondition = changeCondition;
	}
	public String getChangeTime() {
		return changeTime;
	}
	public void setChangeTime(String changeTime) {
		this.changeTime = changeTime;
	}
	public String getUserLocationInformation() {
		return userLocationInformation;
	}
	public void setUserLocationInformation(String userLocationInformation) {
		this.userLocationInformation = userLocationInformation;
	}
	public int getePCQoSInfor_qCI() {
		return ePCQoSInfor_qCI;
	}
	public void setePCQoSInfor_qCI(int ePCQoSInfor_qCI) {
		this.ePCQoSInfor_qCI = ePCQoSInfor_qCI;
	}
	public int getePCQoSInfor_maxRequestedBandwithUL() {
		return ePCQoSInfor_maxRequestedBandwithUL;
	}
	public void setePCQoSInfor_maxRequestedBandwithUL(int ePCQoSInfor_maxRequestedBandwithUL) {
		this.ePCQoSInfor_maxRequestedBandwithUL = ePCQoSInfor_maxRequestedBandwithUL;
	}
	public int getePCQoSInfor_maxRequestedBandwithDL() {
		return ePCQoSInfor_maxRequestedBandwithDL;
	}
	public void setePCQoSInfor_maxRequestedBandwithDL(int ePCQoSInfor_maxRequestedBandwithDL) {
		this.ePCQoSInfor_maxRequestedBandwithDL = ePCQoSInfor_maxRequestedBandwithDL;
	}
	public int getePCQoSInfor_guaranteedBitrateUL() {
		return ePCQoSInfor_guaranteedBitrateUL;
	}
	public void setePCQoSInfor_guaranteedBitrateUL(int ePCQoSInfor_guaranteedBitrateUL) {
		this.ePCQoSInfor_guaranteedBitrateUL = ePCQoSInfor_guaranteedBitrateUL;
	}
	public int getePCQoSInfor_guaranteedBitrateDL() {
		return ePCQoSInfor_guaranteedBitrateDL;
	}
	public void setePCQoSInfor_guaranteedBitrateDL(int ePCQoSInfor_guaranteedBitrateDL) {
		this.ePCQoSInfor_guaranteedBitrateDL = ePCQoSInfor_guaranteedBitrateDL;
	}
	public int getePCQoSInfor_aRP() {
		return ePCQoSInfor_aRP;
	}
	public void setePCQoSInfor_aRP(int ePCQoSInfor_aRP) {
		this.ePCQoSInfor_aRP = ePCQoSInfor_aRP;
	}
	public int getePCQoSInfor_aPNAggregateMaxBitrateUL() {
		return ePCQoSInfor_aPNAggregateMaxBitrateUL;
	}
	public void setePCQoSInfor_aPNAggregateMaxBitrateUL(int ePCQoSInfor_aPNAggregateMaxBitrateUL) {
		this.ePCQoSInfor_aPNAggregateMaxBitrateUL = ePCQoSInfor_aPNAggregateMaxBitrateUL;
	}
	public int getePCQoSInfor_aPNAggregateMaxBitrateDL() {
		return ePCQoSInfor_aPNAggregateMaxBitrateDL;
	}
	public void setePCQoSInfor_aPNAggregateMaxBitrateDL(int ePCQoSInfor_aPNAggregateMaxBitrateDL) {
		this.ePCQoSInfor_aPNAggregateMaxBitrateDL = ePCQoSInfor_aPNAggregateMaxBitrateDL;
	}
	
	
}
