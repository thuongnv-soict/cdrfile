package cdrfile.convert;

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
public class StructAlcatelCDR {
    // Struc SMSC
    public String Version; // 1 byte
    public String MSCTypeAndCallType; // 1 byte
    public String CallOriginChargingIndicator; // 1 byte
    public String RequiredTeleservice; // 1 byte
    public String RequiredBearerService;
    public String CallStaTime; // 6 byte year-month-day-hour-minute-second
    public int CallDuration; // 3 byte
    public String CallEndTime; // 5 byte
    public String IMEI;
    public String NumberofInvokedINservices;
    public String TAGSMOCalledNumber;
    public String LinkInformation;
    public String MobileSubscriberIdentity;
    public String MSCIdentity;
    public String CallPartnerIdentity;
    public String MSLocationIdentity;
    public String MSLocationIdentityExtension;
    public String RequiredBearerCapability;
    public String InformationReceivedFromtheFixednetwork;
    public String TAGforIncomingTrunkGroup;
    public String RecordType;
    public String CallType;
    public String Calling_isdn;
    public String Called_isdn;
    public String Cell_id;
    public String ic_route;
    public String og_route;
    public String tariff_class;
    public String in_mark;
    public String org_call_id;
    public int rec_seq_number;
    public String Service_center;
    public String CallingOrg;
    public String CalledOrg;
    public String MsgTypeIndicator;
    public String EmlppPriorityLevel;

    public String InvokedCamelService;
    public int LengthOfCamel;
    public int LastNumberGivenBySCP;
    public int InvokedCAMELServNumber;
    public int SCPaddress;
    public String ExtIndicatorNaturePlan;
    public String Address;
    public String StorageOfServiceKey;
    public int NewNumberGivenBySCP;
    public String SCPInfo;
    public String SCPType;
    public int LengthOfFCIData;
    public String FCIData;
    public int LengthOfSCIData;
    public int IPOnRelease;
    public String CAMELinfo;
    public String NumOfDPencountered;
    public String DefaultCallHandling;
    public String levelOfCAMEL;
    public String CAMELInitiatingCF;
    
    public String originalCalledNumber;

//    public String levelOfCamelService;
//    public String camelServiceKey;
}
