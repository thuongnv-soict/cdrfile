package cdrfile.convert;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
//----------------------------------------------------------------------------
//2017.08.20 taivv
//- cap nhat tai khoan DATA_LN
//2017.10.16 taivv
//- cap nhat tai khoan DATA_VC
//----------------------------------------------------------------------------
public class StructICC
{
    public String Header; // 2 byte
    public String GlobalServiceIdentifier; // 2 byte
    public String CallStartDateTime; // 2 byte

    public String TagCallingNumber; // 63EA
    public String TagCalledNumber; // 64EA
    public String CallEndDateTime1; // 6 byte
    public int TagCallDuration1; // 67EA

    public String TagCallType; //
    public String TagIMSI; // 17
    public String TagRemainningCredit; // 18
    public String TagReFill_ScratchType;
    public String TagReFill_ScratchNumber;
    //public int TagReFill_ScratchValue;
    public String TagReFill_ScratchValue;
    public String TagReFill_NumberOfRefill;

    public String TagLocationNumber;
    public int TagCallDuration2;
    public int TagCallDuration;
    public String CallEndDateTime2; // 6 byte

    public String TagPeriodicAutomaticRefill;
    public String TagAccountProfileModification;
    public String TagOriginalCalledNumber;
    public String TagOriginalCalledNumber1;
    public String TagOriginalCalledNumberM2U;
    public String eventTypeName;
    public String RecordType;
    public String CallType;
    public String FmtOfCalling;
    public String FmtOfCalled;
    public String FmtOfOriginalCalled;
    public String CallingOrg;
    public String CalledOrg;
    //cell id
    public String nsl_lac;
    public String nsl_ci;
    public String sgsnAddress;
    //GPRS
    public String ChargingId;
    public String TagRTecCallCost;
    //ICC - new resource.
    //GPRS usage.
    public String NetEleID;
    public String NetEleTranID;
    public int eventCostVat;
    public String zoneOrgArea;
    public String zoneDestArea;
    //Call cost

    public String creditBeforeTrans;
    public String locationIndicator;
//    Bundle
    public String consumedUnit;
    public String remainingUnit;
    public String initialUnit;
    public String bunldeUnit;
//    Bonus
    public String consumedUnitBonus;
    public String remainingUnitBonus;
    public String initialUnitBonus;

    public String subScriberType;
    public String usedQuantity1;//Used duration
    public String DestCharingMatrix;
//    Bundle - Account Tree.
/////Tai khoan tien.
    //Tai khoan khuyen mai dinh ky 1.
    public String limitDateKMDK1Bucket;
    public String allocatedKMDK1Bucket;
    public String consumedKMDK1Unit;
    public String remainingKMDK1Unit;
    public String initialKMDK1Unit;

    //Tai khoan khuyen mai dinh ky 2.
    public String limitDateKMDK2Bucket;
    public String allocatedKMDK2Bucket;
    public String consumedKMDK2Unit;
    public String remainingKMDK2Unit;
    public String initialKMDK2Unit;

    //Tai khoan khuyen mai dinh ky 3.
    public String limitDateKMDK3Bucket;
    public String allocatedKMDK3Bucket;
    public String consumedKMDK3Unit;
    public String remainingKMDK3Unit;
    public String initialKMDK3Unit;

    //Tai khoan khuyen mai 1.
    public String limitDateKM1Bucket;
    public String allocatedKM1Bucket;
    public String consumedKM1Unit;
    public String remainingKM1Unit;
    public String initialKM1Unit;

    //Tai khoan khuyen mai 2.
    public String limitDateKM2Bucket;
    public String allocatedKM2Bucket;
    public String consumedKM2Unit;
    public String remainingKM2Unit;
    public String initialKM2Unit;

    //Tai khoan khuyen mai 3.
    public String limitDateKM3Bucket;
    public String allocatedKM3Bucket;
    public String consumedKM3Unit;
    public String remainingKM3Unit;
    public String initialKM3Unit;
//////tai khoan san luong
    //tai khoan thoai
    public String limitDateKMBucket;
    public String allocatedKMBucket;
    public String consumedKMUnit;
    public String remainingKMUnit;
    public String initialKMUnit;

    public String limitDateDKBucket;
    public String allocatedDKBucket;
    public String consumedDKUnit;
    public String remainingDKUnit;
    public String initialDKUnit;

    public String limitDatePacketBucket;
    public String allocatedPacketBucket;
    public String consumedPacketUnit;
    public String remainingPacketUnit;
    public String initialPacketUnit;

    public String limitDatePacketOPBucket;
    public String allocatedPacketOPBucket;
    public String consumedPacketOPUnit;
    public String remainingPacketOPUnit;
    public String initialPacketOPUnit;

    public String bundleIdentifier;
    public String serviceId;
    public String feeName;
    public String cellGlobalIdentifier189;
    public String cellGlobalIdentifier;
    public String countryCode;
    public String networkCode;

    public String TranDesc;
    public String TagCallingNumber1;

    public String AmountSwapped;
    public String Fee;
    public String NumberOfDaysSwapped;
    public String SpecificChargingIndicator;
    public String NameOfCUG;

    public String bundleLMLimitDate;
    public String bundleLMAllocated;
    public String bundleLMConsumed;
    public String bundleLMRemaining;
    public String bundleLMInitial;
    public String bundleCLMLimitDate;
    public String bundleCLMAllocated;
    public String bundleCLMConsumed;
    public String bundleCLMRemaining;
    public String bundleCLMInitial;

    public String duplicatedNotes;

    //discarded credit
    public int discardedCredit;

    // tai khoan CREDIT1
    public String bundleCredit1LimitDate;
    public String bundleCredit1Allocated;
    public String bundleCredit1Consumed;
    public String bundleCredit1Remaining;
    public String bundleCredit1Initial;
    // tai khoan VNPT1
    public String bundleVNPT1LimitDate;
    public String bundleVNPT1Allocated;
    public String bundleVNPT1Consumed;
    public String bundleVNPT1Remaining;
    public String bundleVNPT1Initial;

    public String orgCallStaTime;
    public String reasonCode;
    public String tariffPlanName;

    //tai khoan KM1T
    public String bonusKM1TLimitDate;
    public String bonusKM1TAllocated;
    public String bonusKM1TConsumed;
    public String bonusKM1TRemaining;
    public String bonusKM1TInitial;
    //tai khoan KM2T
    public String bonusKM2TLimitDate;
    public String bonusKM2TAllocated;
    public String bonusKM2TConsumed;
    public String bonusKM2TRemaining;
    public String bonusKM2TInitial;

    //tai khoan KM3T
    public String bonusKM3TLimitDate;
    public String bonusKM3TAllocated;
    public String bonusKM3TConsumed;
    public String bonusKM3TRemaining;
    public String bonusKM3TInitial;

    //tai khoan VOICETH
    public String bundleVOICETHLimitDate;
    public String bundleVOICETHAllocated;
    public String bundleVOICETHConsumed;
    public String bundleVOICETHRemaining;
    public String bundleVOICETHInitial;

    //tai khoan GPRS_0
    public String bundleGPRS0LimitDate;
    public String bundleGPRS0Allocated;
    public String bundleGPRS0Consumed;
    public String bundleGPRS0Remaining;
    public String bundleGPRS0Initial;

	// Tai khoan vi
    public String bundleWALLET1LimitDate;
    public String bundleWALLET1Allocated;
    public String bundleWALLET1Consumed;
    public String bundleWALLET1Remaining;
    public String bundleWALLET1Initial;

    public String bundleWALLET2LimitDate;
    public String bundleWALLET2Allocated;
    public String bundleWALLET2Consumed;
    public String bundleWALLET2Remaining;
    public String bundleWALLET2Initial;
    
    // Tai khoan IRD
    public String bundleIRDLimitDate;
    public String bundleIRDAllocated;
    public String bundleIRDConsumed;
    public String bundleIRDRemaining;
    public String bundleIRDInitial;
    
    //causeRecClose
    public String causeRecClose;
    
    //Correlation Identifier
    public String correlationIdentifier;
    
    //Community Identifier
    public String communityIdentifier;
    
    //Topup profile
    public String topupProfile;
    
    public String oldFnfNum;
    public String newFnfNum;
    public String accPreLang;
    public String accCurLang;
    
    public String bundleGROUPLimitDate;
    public String bundleGROUPAllocated;
    public String bundleGROUPConsumed;
    public String bundleGROUPRemaining;
    public String bundleGROUPInitial;
    
    // KM99T
    public String bundleKM99TLimitDate;
    public String bundleKM99TAllocated;
    public String bundleKM99TConsumed;
    public String bundleKM99TRemaining;
    public String bundleKM99TInitial;
    
	// LM1
    public String bundleLM1Name;
    public String bundleLM1LimitDate;
    public String bundleLM1Allocated;
    public String bundleLM1Consumed;
    public String bundleLM1Remaining;
    public String bundleLM1Initial;
    
	// DATA_KM1
    public String bundleDATA_KM1Name;
    public String bundleDATA_KM1LimitDate;
    public String bundleDATA_KM1Allocated;
    public String bundleDATA_KM1Consumed;
    public String bundleDATA_KM1Remaining;
    public String bundleDATA_KM1Initial;
    
    public String InServiceResult;
    
    // DATA6 
    public String bundleDATA6Name;
    public String bundleDATA6Consumed;
    public String bundleDATA6Remaining;
    public String bundleDATA6Initial;
    
    // KMKNDL
    public String bundleKMKNDLName;
    public String bundleKMKNDLConsumed;
    public String bundleKMKNDLRemaining;
    public String bundleKMKNDLInitial;
    
	// LM_DL
    public String bundleLM_DLName;
    public String bundleLM_DLConsumed;
    public String bundleLM_DLRemaining;
    public String bundleLM_DLInitial;
    
	// DATA5 
    public String bundleDATA5Name;
    public String bundleDATA5Consumed;
    public String bundleDATA5Remaining;
    public String bundleDATA5Initial;
    
    // Tai khoan chia se 
    public String bundleCSName;
    public String bundleCSConsumed;
    public String bundleCSRemaining;
    public String bundleCSInitial;
    
    // Tai khoan wifi calling 
    public String bundleWifiName;
    public String bundleWifiConsumed;
    public String bundleWifiRemaining;
    public String bundleWifiInitial;
    
    // Tai khoan DataDem 
    public String bundleDataDemName;
    public String bundleDataDemConsumed;
    public String bundleDataDemRemaining;
    public String bundleDataDemInitial;
    
    // Tai khoan M-loyalty 
    public String bundleMLoyaltyName;
    public String bundleMLoyaltyConsumed;
    public String bundleMLoyaltyRemaining;
    public String bundleMLoyaltyInitial;
    
    //Tai khoan KM4
    public String bonusKM4Name;
    public String bonusKM4Consumed;
    public String bonusKM4Remaining;
    public String bonusKM4Initial;
    
    //Tai khoan KM4T
    public String bonusKM4TName;
    public String bonusKM4TConsumed;
    public String bonusKM4TRemaining;
    public String bonusKM4TInitial;
    
    //Tai khoan KMDK4
    public String bonusKMDK4Name;
    public String bonusKMDK4Consumed;
    public String bonusKMDK4Remaining;
    public String bonusKMDK4Initial;
    
    //Tai khoan THOAILM1
    public String bundleTHOAILM1Name;
    public String bundleTHOAILM1Consumed;
    public String bundleTHOAILM1Remaining;
    public String bundleTHOAILM1Initial;
    
    //Tai khoan IRA
    public String bonusIRAName;
    public String bonusIRAConsumed;
    public String bonusIRARemaining;
    public String bonusIRAInitial;
    
    //Tai khoan IRB
    public String bonusIRBName;
    public String bonusIRBConsumed;
    public String bonusIRBRemaining;
    public String bonusIRBInitial;
    
    //Tai khoan IRVS
    public String bonusIRVSName;
    public String bonusIRVSConsumed;
    public String bonusIRVSRemaining;
    public String bonusIRVSInitial;
    
    //Tai khoan IRA
    public String bonusIRSMSName;
    public String bonusIRSMSConsumed;
    public String bonusIRSMSRemaining;
    public String bonusIRSMSInitial;
    
    //Tai khoan VOICE KM1
    public String bundleVOICE_KM1Name;
    public String bundleVOICE_KM1Consumed;
    public String bundleVOICE_KM1Remaining;
    public String bundleVOICE_KM1Initial;
    
    //Tai khoan DATA_31
    public String bundleDATA_31Name;
    public String bundleDATA_31Consumed;
    public String bundleDATA_31Remaining;
    public String bundleDATA_31Initial;
    
    //Tai khoan route number
    public String routeNumber;
    
    //Tai khoan SMSRefill
	public String bundleSMSRefillName ;
	public String bundleSMSRefillConsumed ;
	public String bundleSMSRefillRemaining ;
	public String bundleSMSRefillInitial ;
	
	//Tai khoan VIDEO
	public String bundleVIDEOName ;
	public String bundleVIDEOConsumed ;
	public String bundleVIDEORemaining ;
	public String bundleVIDEOInitial ;
	
	//Tai khoan WALLET3
	public String bundleWALLET3Name ;
	public String bundleWALLET3Consumed ;
	public String bundleWALLET3Remaining ;
	public String bundleWALLET3Initial ;
	
	//Tai khoan RM
	public String bundleRMName ;
	public String bundleRMConsumed ;
	public String bundleRMRemaining ;
	public String bundleRMInitial ;
	
	//Tai khoan SMS3
	public String bundleSMS3Name ;
	public String bundleSMS3Consumed ;
	public String bundleSMS3Remaining ;
	public String bundleSMS3Initial ;
	
	//Tai khoan DataZ1
	public String bundleDataZ1Name ;
	public String bundleDataZ1Consumed ;
	public String bundleDataZ1Remaining ;
	public String bundleDataZ1Initial ;
	
	//Tai khoan DataZ2
	public String bundleDataZ2Name ;
	public String bundleDataZ2Consumed ;
	public String bundleDataZ2Remaining ;
	public String bundleDataZ2Initial ;
	
	//Tai khoan DataZ3
	public String bundleDataZ3Name ;
	public String bundleDataZ3Consumed ;
	public String bundleDataZ3Remaining ;
	public String bundleDataZ3Initial ;
	
	//Tai khoan khach hang lau nam
	public String bundleLNName;
	public String bundleLNConsumed;
	public String bundleLNRemaining;
	public String bundleLNInitial;
    
	//Tai khoan nguyen gia
	public String bonusTK1Name;
    public String bonusTK1Consumed;
    public String bonusTK1Remaining;
    public String bonusTK1Initial;
    
    //Tai khoan KM1V
    public String bonusKM1VName;
    public String bonusKM1VConsumed;
    public String bonusKM1VRemaining;
    public String bonusKM1VInitial;
    
    //Tai khoan KM2V
    public String bonusKM2VName;
    public String bonusKM2VConsumed;
    public String bonusKM2VRemaining;
    public String bonusKM2VInitial;
    
    //Tai khoan KM3V
    public String bonusKM3VName;
    public String bonusKM3VConsumed;
    public String bonusKM3VRemaining;
    public String bonusKM3VInitial;
    
    //Tai khoan KM4V
    public String bonusKM4VName;
    public String bonusKM4VConsumed;
    public String bonusKM4VRemaining;
    public String bonusKM4VInitial;
    
    //Tai khoan Credit2
    public String bonusCredit2Name;
    public String bonusCredit2Consumed;
    public String bonusCredit2Remaining;
    public String bonusCredit2Initial;
    
    //Tai khoan Credit3
    public String bonusCredit3Name;
    public String bonusCredit3Consumed;
    public String bonusCredit3Remaining;
    public String bonusCredit3Initial;
    
    //Tai khoan DATA_LN khach hang lau nam
	public String bundleDATA_LNName;
	public String bundleDATA_LNConsumed;
	public String bundleDATA_LNRemaining;
	public String bundleDATA_LNInitial;
	
	//Tai khoan DATA_VC CTKM T4 Vui ve
	public String bundleDATA_VCName;
	public String bundleDATA_VCConsumed;
	public String bundleDATA_VCRemaining;
	public String bundleDATA_VCInitial;
	
	//Them thong tin Reseller ID
	public String resellerId;
}
