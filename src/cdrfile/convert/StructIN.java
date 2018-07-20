package cdrfile.convert;

/**
 * <p>
 * Title: CDR File System
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

public class StructIN {
    public String Header; // 2 byte
    public String GlobalServiceIdentifier; // 2 byte
    public String CallStartDateTime; // 2 byte
    public String TagCallingNumber; // 63EA
    public String TagCalledNumber; // 64EA
    public String TagTranslateNumber; // 65EA
    public String TagCallCost; // 66EA
    public String CallEndDateTime1; // 6 byte
    public int TagCallDuration1; // 67EA
    public String TagCallType; //
    public String TeleserviceIndicator; //
    public String NetworkCauseResultIndicator; //
    public String INServiceResultIndicator; //
    public String TagIMSI; // 17
    public String TagRemainningCredit; // 18
    public String TagReFill_ScratchType;
    public String TagReFill_ScratchNumber;
    public int TagReFill_ScratchValue;
    public String TagReFill_NumberOfRefill;
    public String TagDiscardedCredit;
    public String TagLocationNumber;
    public int TagCallDuration2;
    public String CallEndDateTime2; // 6 byte
    public String TagChargingClassNumber;
    public String TagSpecificChargingIndicator;
    public String TagPeriodicAutomaticRefill;
    public String TagAccountProfileModification;
    public String TagOriginalCalledNumber;

    public String TagBonnusCredit;
    public String RecordType;
    public String CallType;
    public String FmtOfCalling;
    public String FmtOfCalled;
    public String FmtOfTranslatedNumber;
    public String FmtOfOriginalCalled;
    public String CallingOrg;
    public String CalledOrg;
    public String locationIndicator;
    //GPRS
    public String TrafficClass;
    public String APNChargingArea;
    public String ChargingId;
    public String GGSNAddress;
    public String GPRSDataVolume;
    public String TransVolRollOver;
    public String ContentProviderId;
    public String ContentProviderTransactionId;
    public String Price;
    public String Currency;
    public String Bonus;

    public String map;
    public String TagRTecCallCost;

    //Bucket
    public String bucketName;
    public String RUMKind;
    public String consumedUnit;
    public String remainingUnit;
    public String endValidityDate;
    public String bucketStart;
    public String bucketEnd;
}
