package cdrfile.global;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * <p>Title: CDR File(s) System</p>
 * <p>Description: VMS IS Departerment</p>
 * <p>Copyright: Copyright (c) by eKnowledge 2005</p>
 * <p>Company: VietNam Mobile Telecom Services</p>
 * @author eKnowledge - Software
 * @version 1.0
 */

import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import cdrfile.thread.ThreadInfo;

//----------------------------------------------------------------------------
//Change History
//2013.11.04 datnh
//		- Them column "number of seq" vao mSGSNHeaderCharge, mSGSNHeaderNoCharge
//2013.12.09 datnh
//		- Them tai khoan vi WALLET1, WALLET2
//2013.12.10 datnh
//		- Them tai khoan IRD
//2014.03.04 datnh
//		- Them thong tin Correlation Identifier
//2014.04.03 datnh
//		- Them thong tin MSC smsResult
//2017.08.20 taivv
//- cap nhat tai khoan DATA_LN
//2017.10.16 taivv
//- cap nhat tai khoan DATA_VC
//----------------------------------------------------------------------------

public class Global
{
    public static String mTimeSentMailMessage = "";
    public static final String mSeparate = "/";
    public static final String cstrDelimited = ";";
    public static final int ErrFileNotFound = 1;
    public static final int OKFileConverted = 2;
    public static final int ErrFileConverted = 3;
    public static final int ErrCenterNotDefined = 4;

    // Status of header file
    public static final int StateFileFtpOK = 1;
    public static final int StateConvertedError = -1;
    public static final int StateConverted = 2;
    public static final int StateRatedError = -2;
    public static final int StateRated = 3;
    public static final int StateExportedData = 4;
    public static final int StateExportedDataError = -3;
    public static final int StateCollectTrafficTurnover = 5;
    public static final int StateCopiedData = 8;
    public static final int StateCollectTrafficTurnoverError = -4;
    public static final int StateFinishDispose = 6;
    public static final int StateDeletedTmp = 7;
    public static Vector vThreadInfo = new Vector();
    public static ThreadInfo threadInfo = null;
    protected String mstrThreadID;
    protected String mstrThreadName;
    protected String mstrLogFileName = "";
    public static int mPortManager = 0;
    public static String strUrl;
    public static String strUserName = "cdrfile";
    public static String strPassword;
    public static final String OwnerDB = "cdrfile_owner";
    ///public static String mstrLogPathFileName = "E:\\export\\home\\cdrfile\\log";
    public static String mstrLogPathFileName = "/export/home/cdrfile/log/";

    IOUtils IOUtil = new IOUtils();
    public static final String mMSCHeaderNoCharge = "STT|FC|call type|po code|tax airtime|tax idd|tax service|" +
         "calling isdn|imsi|call sta time|duration|call end time|" +
         "called isdn|cell id|service center|ic route|og route|" +
         "tar class|ts code|bs code|in mark|char indi|org call id|" +
         "rec seq num|translate num|calling imei|calling org|" +
         "called org|subs type|bl air|bl idd/ser|calling cen|" +
         "called cen|collect type|mess type|system type|rateIndication|FCIData|NetworkCallReference|"+
         "LevelOfCamelService|serviceKey|GSM SCF Address|call identification number|seizureTime|callEmlppPriority|"+
         "causeForTerm|supplServicesUsed|camelDestinationNumber|smsResult|" +
         "callerIP|calledIP|CallTypeDetail|RMnumber|originalCalledNumber" +
         "|calling isdn change|called isdn change";

    public static final String mMSCHeaderCharge = "RecType;CallType;CallingISDN;IMSI;CallStaTime;" + "CallDuration;CallEndTime;CalledISDN;CellID;" + "ServiceCenter;IcRoute;OgRoute;TarClass;" + "ReqTel;ReqBeare;INSer;CharInd;CallOrgISDN;" + "TransISDN;RecSeq;IMEI;CallingOrg;CalledOrg";
    public static final String mHeaderForAcounting = "Block|Record|Name|Transmission check sum|Type of record|Exchange identity|Source|Total number of used remuneration verification counters|" + "Total number of used remuneration counters|Year the last time a periodic output was made|Month the last time a periodic output was made|Day the last time a periodic output was made|Hour the last time a periodic output was mad|"
        + "Minute the last time a periodic output was made|Year for the current output|Month for the current output|Day for the current output|Hour for the current output|" + "Minute for the current output";
    public static final String mCounterForAcounting = "Block|Record|Name|Transmission check sum|Type of record|Accounting class number|Type of accounting|Message counter|" + "Octet counter";
    public static final String mICCHeaderNoCharge =

       "STT|FC|call type|po code|tax airtime|tax idd|tax service|calling isdn|imsi|call sta time|duration|total duration of IN process|call end time|called isdn|location|org call id|scratch type|scratch number|scratch value|acc profile|calling org|called org|event type name|cell Id|SGSN address|net element id|net element trans id|zone origin area|zone dest area|location indicator|SubScriber type|used duration|Destination Charging Matrix|bundle DK limit date|bundle DK allocated|bundle DK consumed|bundle DK remaining|bundle DK initial|bundle KM limit date|bundle KM allocated|bundle KM consumed|"

       + "bundle KM remaining|bundle KM initial|bundle package limit date|bundle package allocated|bundle package consumed|bundle package remaining|bundle package initial|bundle OP limit date|bundle OP allocated|bundle OP consumed|bundle OP remaining|bundle OP initial|bonus KMDK1 limit date|bonus KMDK1 allocated|bonus KMDK1 consumed|bonus KMDK1 remaining|bonus KMDK1 initial|bonus KMDK2 limit date|bonus KMDK2 allocated|bonus KMDK2 consumed|bonus KMDK2 remaining|bonus KMDK2 initial|bonus KMDK3 limit date|bonus KMDK3 allocated|bonus KMDK3 consumed|bonus KMDK3 remaining|bonus KMDK3 initial|"

       + "bonus KM1 limit date|bonus KM1 allocated|bonus KM1 consumed|bonus KM1 remaining|bonus KM1 initial|bonus KM2 limit date|bonus KM2 allocated|bonus KM2 consumed|bonus KM2 remaining|bonus KM2 initial|bonus KM3 limit date|bonus KM3 allocated|bonus KM3 consumed|bonus KM3 remaining|bonus KM3 initial|credit before adjustment|"
       + "credit charged|credit remaining|network service id|feeName|Transaction description"
       + "|amount swapped|fee swapped|day swapped|specific charging indicator|name of CUG"
       + "|bundle LM limit date|bundle LM allocated|bundle LM consumed|bundle LM remaining|bundle LM initial|bundle VNPT limit date|bundle VNPT allocated|bundle VNPT consumed|bundle VNPT remaining|bundle VNPT initial"
       + "|discarded credit|bundle VNPT1 limit date|bundle VNPT1 allocated|bundle VNPT1 consumed|bundle VNPT1 remaining|bundle VNPT1 initial"
       + "|bundle credit1 limit date|bundle credit1 allocated|bundle credit1 consumed|bundle credit1 remaining|bundle credit1 initial"
       + "|org call sta time|tariff name"
       + "|bonus KM2T limit date|bonus KM2T allocated|bonus KM2T consumed|bonus KM2T remaining|bonus KM2T initial"
       + "|bonus KM3T limit date|bonus KM3T allocated|bonus KM3T consumed|bonus KM3T remaining|bonus KM3T initial"
       + "|bundle VOICETH limit date|bundle VOICETH allocated|bundle VOICETH consumed|bundle VOICETH remaining|bundle VOICETH initial"
       + "|bonus KM1T limit date|bonus KM1T allocated|bonus KM1T consumed|bonus KM1T remaining|bonus KM1T initial"
       + "|bundle GPRS0 limit date|bundle GPRS0 allocated|bundle GPRS0 consumed|bundle GPRS0 remaining|bundle GPRS0 initial"
       + "|bundle WALLET1 limit date|bundle WALLET1 allocated|bundle WALLET1 consumed|bundle WALLET1 remaining|bundle WALLET1 initial"
       + "|bundle WALLET2 limit date|bundle WALLET2 allocated|bundle WALLET2 consumed|bundle WALLET2 remaining|bundle WALLET2 initial"
       + "|bundle IRD limit date|bundle IRD allocated|bundle IRD consumed|bundle IRD remaining|bundle IRD initial"
       + "|causeRecClose|correlation identifier|community identifier|topup profile"
       + "|old fnf num|new fnf num|acc pre lang|acc cur lang"
       + "|bundle GROUP limit date|bundle GROUP allocated|bundle GROUP consumed|bundle GROUP remaining|bundle GROUP initial"
       + "|bundle KM99T limit date|bundle KM99T allocated|bundle KM99T consumed|bundle KM99T remaining|bundle KM99T initial"
       + "|bundle LM1 name|bundle LM1 limit date|bundle LM1 allocated|bundle LM1 consumed|bundle LM1 remaining|bundle LM1 initial"
       + "|bundle DATA_KM1 name|bundle DATA_KM1 limit date|bundle DATA_KM1 allocated|bundle DATA_KM1 consumed|bundle DATA_KM1 remaining|bundle DATA_KM1 initial"
       + "|InServiceResult"
       + "|bundle DATA6 name|bundle DATA6 consumed|bundle DATA6 remaining|bundle DATA6 initial"
       + "|bundle KMKNDL name|bundle KMKNDL consumed|bundle KMKNDL remaining|bundle KMKNDL initial"
       + "|bundle LM_DL name|bundle LM_DL consumed|bundle LM_DL remaining|bundle LM_DL initial"
       + "|bundle DATA5 name|bundle DATA5 consumed|bundle DATA5 remaining|bundle DATA5 initial"
       + "|bundle CS name|bundle CS consumed|bundle CS remaining|bundle CS initial"
       + "|bundle wifi name|bundle wifi consumed|bundle wifi remaining|bundle wifi initial"
       + "|bundle DataDem name|bundle DataDem consumed|bundle DataDem remaining|bundle DataDem initial"
       + "|bundle mloyalty name|bundle mloyalty consumed|bundle mloyalty remaining|bundle mloyalty initial"
       + "|bonus KM4 name|bonus KM4 consumed|bonus KM4 remaining|bonus KM4 initial"
       + "|bonus KM4T name|bonus KM4T consumed|bonus KM4T remaining|bonus KM4T initial"
       + "|bonus KMDK4 name|bonus KMDK4 consumed|bonus KMDK4 remaining|bonus KMDK4 initial"
       + "|bundle THOAILM1 name|bundle THOAILM1 consumed|bundle THOAILM1 remaining|bundle THOAILM1 initial"
       
       + "|bonus IRA name|bonus IRA consumed|bonus IRA remaining|bonus IRA initial"
       + "|bonus IRB name|bonus IRB consumed|bonus IRB remaining|bonus IRB initial"
       + "|bonus IRVS name|bonus IRVS consumed|bonus IRVS remaining|bonus IRVS initial"
       + "|bonus IRSMS name|bonus IRSMS consumed|bonus IRSMS remaining|bonus IRSMS initial"
       + "|bundle VOICE_KM1 name|bundle VOICE_KM1 consumed|bundle VOICE_KM1 remaining|bundle VOICE_KM1 initial"
       + "|bundle DATA_31 name|bundle DATA_31 consumed|bundle DATA_31 remaining|bundle DATA_31 initial"
       + "|route number"
       
       + "|bundle SMSRefill name|bundle SMSRefill consumed|bundle SMSRefill remaining|bundle SMSRefill initial"
       + "|bundle VIDEO name|bundle VIDEO consumed|bundle VIDEO remaining|bundle VIDEO initial"
       + "|bundle WALLET3 name|bundle WALLET3 consumed|bundle WALLET3 remaining|bundle WALLET3 initial"
       + "|bundle RM name|bundle RM consumed|bundle RM remaining|bundle RM initial"
       + "|bundle SMS3 name|bundle SMS3 consumed|bundle SMS3 remaining|bundle SMS3 initial"
       + "|bundle DataZ1 name|bundle DataZ1 consumed|bundle DataZ1 remaining|bundle DataZ1 initial"
       + "|bundle DataZ2 name|bundle DataZ2 consumed|bundle DataZ2 remaining|bundle DataZ2 initial"
       + "|bundle DataZ3 name|bundle DataZ3 consumed|bundle DataZ3 remaining|bundle DataZ3 initial"
       + "|bundle LN name|bundle LN consumed|bundle LN remaining|bundle LN initial"
       + "|bonus TK1 name|bonus TK1 consumed|bonus TK1 remaining|bonus TK1 initial"
       + "|bonus KM1V name|bonus KM1V consumed|bonus KM1V remaining|bonus KM1V initial"
       + "|bonus KM2V name|bonus KM2V consumed|bonus KM2V remaining|bonus KM2V initial"
       + "|bonus KM3V name|bonus KM3V consumed|bonus KM3V remaining|bonus KM3V initial"
       + "|bonus KM4V name|bonus KM4V consumed|bonus KM4V remaining|bonus KM4V initial"
       + "|reason code"
       + "|bonus Credit2 name|bonus Credit2 consumed|bonus Credit2 remaining|bonus Credit2 initial"
       + "|bonus Credit3 name|bonus Credit3 consumed|bonus Credit3 remaining|bonus Credit3 initial"
       + "|bundle DATA_LN name|bundle DATA_LN consumed|bundle DATA_LN remaining|bundle DATA_LN initial"
       + "|bundle DATA_VC name|bundle DATA_VC consumed|bundle DATA_VC remaining|bundle DATA_VC initial"
       + "|Reseller ID"
       
       //thuongnv add field calling isdn change
       + "|calling isdn change";

    public static final String mICCHeaderCharge = "RecType;CallType;CallingISDN;IMSI;CallStaTime;" + "CallDuration;CallEndTime;CalledISDN;LocatInd;" + "AccProfile;RemainCredit;CallCost;DisCredit;" + "CharClass;TelInd;NetInd;INSer;CharInd;" + "CallOrgISDN;TransISDN;ReFillType;RefillNum;" + "RefillVal;CallingOrg;CalledOrg;BonusCredit";

    public static final String mPPSHeaderNoCharge = "STT|FC|call type|po code|tax airtime|tax idd|" + "tax service|calling isdn|imsi|call sta time|" + "duration|call end time|called isdn|location|" + "remain credit|call cost|dis credit|tar class|" + "ts code|nw result|in serv|char indi|org call id|" + "translate num|scratch type|scratch number|" + "scratch value|acc profile|calling org|" + "called org|subs type|bl air|bl idd/ser|" + "calling cen|called cen|collect type|bonus credit|"
        + "Traffic Class|APNChargArea|Charging Id|GGSN Addr|GPRSDataVol|" + "TranVolRollOver|ContProId|ContProTransId|Price|Currency|Bonus|Bucket Name|RUM Kind|Consumed Unit|Remaining Unit|End Validity Date|Bucket Start|Bucket End|location indicator";
    public static final String mPPSHeaderCharge = "RecType;CallType;CallingISDN;IMSI;CallStaTime;" + "CallDuration;CallEndTime;CalledISDN;LocatInd;" + "AccProfile;RemainCredit;CallCost;DisCredit;" + "CharClass;TelInd;NetInd;INSer;CharInd;" + "CallOrgISDN;TransISDN;ReFillType;RefillNum;" + "RefillVal;CallingOrg;CalledOrg;BonusCredit";
    public static final String mMOHeader = "STT|sequence|msgType|srcAddr|" + "destAddr|submitMultiID|isDLR|status|" + "receiveTime|sendTime|smscId|" + "smppService|errCode|aptemptCount|" + "validity|smscSequenceNumber|statusDetail|smsContent";
    public static final String mMTHeader = "STT|sequence|msgType|srcAddr|" + "destAddr|submitMultiID|isDLR|status|" + "receiveTime|sendTime|smscId|" + "smppService|errCode|aptemptCount|" + "validity|smscSequenceNumber|statusDetail|smsContent";
    public static final String mRTPVHeaderNoCharge = "STT|calling number|call type|tele ind|imsi|Refill means|Refill amount|" + "result ind|ISDN|Trans number used for refill|" + "INDR Start Date Time";
    public static final String mRTPVHeaderCharge = "STT|calling number|call type|tele ind|imsi|Refill means|Refill amount|" + "result ind|ISDN|Trans used for refill|" + "INDR Start Date Time";
    public static final String mSMSCHeaderNoCharge =
        "STT|FC|CallType|PoCode|TaxAirtime|TaxIdd|TaxService|"
        + "BillingTime|OriginatorAddr|DestinationAddr|ReceiveTime|Status|TextSize|"
        + "NumOfDeliveryAttempt|StatusReport|ClientName|MessType|OriginatingMSC|"
        + "OriginatingGroup|Subsystem Number";
    public static final String mSMSCHeaderCharge = "RecType;BillingTime;OriginatorAddr;DestinationAddr;" + "ReceiveTime;Status;TextSize;NumOfDeliveryAttempt;StatusReport;ClientName;" + "MessType;OriginatingMSC;OriginatingGroup";

    public static final String mSGSNHeaderCharge =
        "STT;recordType;networkInitiation;servedIMSI;servedIMEI;sgsnAddress;msNetworkCapability;routingAreaCode;locationAreaCode;"
        + "cellIdentifier;chargingID;ggsnAddressUsed;accessPointNameNI;pDPType;servedPDPAddress;dataVolumeGPRSUplink;dataVolumeGPRSDownlink;recordOpeningTime;"
        + "duration;sGSNChange;causeForRecClosing;diagnostics;recordSequenceNumber;nodeId;recordExtensions;localSequenceNumber;"
        + "aPNSelectionMode;accessPointNameOI;servedMSISDN;chargingCharacteristics;rATType;cAMELInformationPDP;rNCUnsentDownlinkVolume;"
        + "chChSelectionMode;dynamicAddressFlag;numberOfSeq";
    public static final String mSGSGHeaderNoCharge =
        "STT|recordType|networkInitiation|servedIMSI|servedIMEI|sgsnAddress|msNetworkCapability|routingAreaCode|locationAreaCode|"
        + "cellIdentifier|chargingID|ggsnAddressUsed|accessPointNameNI|pDPType|servedPDPAddress|dataVolumeGPRSUplink|dataVolumeGPRSDownlink|recordOpeningTime|"
        + "duration|sGSNChange|causeForRecClosing|diagnostics|recordSequenceNumber|nodeId|recordExtensions|localSequenceNumber|"
        + "aPNSelectionMode|accessPointNameOI|servedMSISDN|chargingCharacteristics|rATType|cAMELInformationPDP|rNCUnsentDownlinkVolume|"
        + "chChSelectionMode|dynamicAddressFlag|numberOfSeq";
    
    public static final String mICCNEIFHeaderNoCharge =

    	       "STT|Sequence|Imsi|Isdn|Neif info|Profile|Time stamp|main value|bonus value|Transaction id|Topup profile";

    public static final String mICCNEIFHeaderCharge = "Sequence;Imsi;Isdn;NeifInfo;Profile;" + "TimeStamp;MainValue;BonusValue;TransactionId;" + "TopupProfile";
    
    public static final String mGGSNHeaderCharge =
        	"STT;recordType;servedIMSI;ggsnAddress;chargingID;sgsnAddress;accessPointNameNI;pdpType;servedPDPAddress;dynamicAddressFlag;" 
        	+ "recordOpeningTime;duration;causeForRecClosing;diagnostics;recordSequenceNumber;nodeID;" 
        	+ "localSequenceNumber;apnSelectionMode;servedMSISDN;chargingCharacteristics;chChSelectionMode;sgsnPLMNIdentifier;" 
        	+ "servedIMEISV;rATType;mSTimeZone;userLocationInformation;"
        	+ "sdRatingGroup;sdChargingRuleBaseName;sdResultCode;sdLocalSequenceNumber;sdTimeOfFirstUsage;sdTimeOfLastUsage;" 
        	+ "sdTimeUsage;sdServiceConditionChange;sdQoSInformationNeg;sdSgsnAddress;sdSGSNPLMNIdentifier;sdDatavolumeFBCUplink;" 
        	+ "sdDatavolumeFBCDownlink;sdTimeOfReport;sdRATType;sdFailureHandlingContinue;sdServiceIdentifier;sdUserLocationInformation";
        	
    public static final String mGGSNHeaderNoCharge =
        	"EGSNPDPRecord|recordType|servedIMSI|ggsnAddress|chargingID|sgsnAddress|accessPointNameNI|pdpType|servedPDPAddress|dynamicAddressFlag|" 
        	+ "recordOpeningTime|duration|causeForRecClosing|diagnostics|recordSequenceNumber|nodeID|" 
        	+ "localSequenceNumber|apnSelectionMode|servedMSISDN|chargingCharacteristics|chChSelectionMode|sgsnPLMNIdentifier|" 
        	+ "servedIMEISV|rATType|mSTimeZone|userLocationInformation|"
        	+ "sdRatingGroup|sdChargingRuleBaseName|sdResultCode|sdLocalSequenceNumber|sdTimeOfFirstUsage|sdTimeOfLastUsage|"
        	+ "sdTimeUsage|sdServiceConditionChange|sdQoSInformationNeg|sdSgsnAddress|sdSGSNPLMNIdentifier|sdDatavolumeFBCUplink|"
        	+ "sdDatavolumeFBCDownlink|sdTimeOfReport|sdRATType|sdFailureHandlingContinue|sdServiceIdentifier|sdUserLocationInformation";
    
    public static final String mSamSungHeaderCharge =
    		"Record Type;Served IMSI;IMSI Unauthenticated Flag;Served IMEISV;S-GW Address used;P-GW Address used;Charging ID;"
        	+ "Serving Node Address;Serving Node Type;S-GW Change;Access Point Name Network Identifier;P-GW PLMN Identifier;"
        	+ "PDN Connection ID;PDP/PDN Type;Served PDP/PDN Address;Served PDP/PDN Address Extension;Dynamic Address Flag;"
        	+ "Record Opening Time;MS Time Zone;Start Time;Stop Time;Duration;Cause for Record Closing;Diagnostics;Record Sequence Number;"
        	+ "Node ID;Record Extensions;Local Record Sequence Number;APN Selection Mode;Served MSISDN;User CSG Information;"
        	+ "User Location Information;Charging Characteristics;Charging Characteristics Selection Mode;"
        	+ "IMS Signalling Context;External Charging Identifier;Serving node PLMN Identifier;PS Furnish Charging Information;"
        	+ "CAMEL Information;RAT Type;QoS Requested;QoS Negotiated;Data Volume Uplink;Data Volume Downlink;Change Condition;"
        	+ "Change Time;sd Rating Group;sd Charging Rule Base Name;sd Result Code;sd Local Sequence Number;sd Time of First Usage;"
        	+ "sd Time of Last Usage;sd Time Usage;sd Service Condition Change;sd Qos Information;sd Serving Node Address;sd Report Time;"
        	+ "sd RAT type;sd Failure Handling Continue;sd Service Identifier;sd user location information;sd PS Free Format Data;"
        	+ "sd PS FFD Append Indicator;sd AF Charing Identifier;sd Media Component Number;sd Flow Number;sd Number of Events;"
        	+ "sd Event Time Stamps;sd Time Quota Type;sd Base Time Interval;sd Service Specific Data;sd Service Specific Type";
    
    public static final String mSamSungHeaderNoCharge =
        	"Record Type|Served IMSI|IMSI Unauthenticated Flag|Served IMEISV|S-GW Address used|P-GW Address used|Charging ID|"
        	+ "Serving Node Address|Serving Node Type|S-GW Change|Access Point Name Network Identifier|P-GW PLMN Identifier|"
        	+ "PDN Connection ID|PDP/PDN Type|Served PDP/PDN Address|Served PDP/PDN Address Extension|Dynamic Address Flag|"
        	+ "Record Opening Time|MS Time Zone|Start Time|Stop Time|Duration|Cause for Record Closing|Diagnostics|Record Sequence Number|"
        	+ "Node ID|Record Extensions|Local Record Sequence Number|APN Selection Mode|Served MSISDN|User CSG Information|"
        	+ "User Location Information|Charging Characteristics|Charging Characteristics Selection Mode|"
        	+ "IMS Signalling Context|External Charging Identifier|Serving node PLMN Identifier|PS Furnish Charging Information|"
        	+ "CAMEL Information|RAT Type|QoS Requested|QoS Negotiated|Data Volume Uplink|Data Volume Downlink|Change Condition|"
        	+ "Change Time|sd Rating Group|sd Charging Rule Base Name|sd Result Code|sd Local Sequence Number|sd Time of First Usage|"
        	+ "sd Time of Last Usage|sd Time Usage|sd Service Condition Change|sd Qos Information|sd Serving Node Address|sd Report Time|"
        	+ "sd RAT type|sd Failure Handling Continue|sd Service Identifier|sd user location information|sd PS Free Format Data|"
        	+ "sd PS FFD Append Indicator|sd AF Charing Identifier|sd Media Component Number|sd Flow Number|sd Number of Events|"
        	+ "sd Event Time Stamps|sd Time Quota Type|sd Base Time Interval|sd Service Specific Data|sd Service Specific Type";
    
    public static final String mEricssonHeaderCharge =
    		"recordType;servedIMSI;sGWiPBinV4Address;chargingID;servingNodeiPBinV4Address;accessPointNameNI;"
    		+ "pdpPDNType;servedPDPPDNiPBinV4Address;servedPDPPDNiPBinV6Address;dataVolumeGPRSUplink;"
    		+ "dataVolumeGPRSDownlink;changeConditionStr;changeTime;userLocationInformation;ePCQoSqCI;"
    		+ "ePCQoSmaxRequestedBandwithUL;ePCQoSmaxRequestedBandwithDL;ePCQoSguaranteedBitrateUL;"
    		+ "ePCQoSguaranteedBitrateDL;ePCQoSaRP;recordOpeningTime;duration;causeForRecClosing;recordSequenceNumber;"
    		+ "nodeID;localSequenceNumber;servedMSISDN;chargingCharacteristics;servingNodePLMNIdentifier;servedIMEISV;"
    		+ "rATType;mSTimeZone;sGWChange;servingNodeType;pGWiPBinV4Address;pGWiPBinV6Address;pGWPLMNIdentifier;"
    		+ "pDNConnectionID;iMSIunauthenticatedFlag;servedPDPPDNiPBinV4AddressExt;servedPDPPDNiPBinV6AddressExt;"
    		+ "sGWiPv6Address;servingNodeiPv6Address;pGWiPv6AddressUsed;networkInitiation;servedIMEI;sgsniPBinV4Address;"
    		+ "msNetworkCapability;routingArea;locationAreaCode;cellIdentifier;ggsniPBinV4Address;pdpType;servedPDPiPBinV4Address;"
    		+ "servedPDPiPBinV6Address;qosRequested;qosNegotiated;sgsnChange;gsm0408Cause;gsm0902MapErrorValue;"
    		+ "identifier;significance;ts48018BssgpCause;ts25413RanapCause;bssgpExttsBssgpRanapCauseBssgp;"
    		+ "bssgpExttsBssgpRanapMessageType;bssgpExttsBssgpRanapMessageSource;bssgpExttsBssgpRanapCauseTimeStamp;"
    		+ "ranapExttsBssgpRanapCause;ranapExttsBssgpRanapMessageType;ranapExttsBssgpRanapMessageSource;ranapExttsBssgpRanapCauseTimeStamp;"
    		+ "apnSelectionMode;accessPointNameOI;sCFAddress;serviceKey;defaultTransactionHandling;cAMELAccessPointNameNI;"
    		+ "cAMELAccessPointNameOI;numberOfDPEncountered;levelOfCAMELService;freeFormatData;FDAppendIndicator;"
    		+ "chChSelectionMode;dynamicAddressFlag;pLMNIdentifier;serviceCentre;recordingEntity;locationArea;"
    		+ "messageReference;eventTimeStamp;destinationNumber;defaultSMSHandling;cAMELCallingPartyNumber;"
    		+ "cAMELDestinationSubscriberNumber;cAMELSMSCAddress;smsReferenceNumber;numberOfSM;locationAreaLastSM;"
    		+ "routingAreaLastSM;cellIdentifierLastSM;pLMNIdentifierLastSM;accessPointName;reliability;delay;precedence;"
    		+ "peakThroughput;meanThroughput;changeCondition;causeForRecClosing;sgsnPLMNIdentifier;apnSelectionMode;"
    		+ "umtsQosInformation;qoSNegotiated;iMSsignalingContext;pSFreeFormatDataBC;pSFFDAppendIndicatorBC;ratingGroup;"
    		+ "resultCode;timeOfFirstUsage;timeOfLastUsage;timeUsage;serviceConditionChange;qoSInformationNeg;"
    		+ "sgsniPBinV4AddressServiceData;sgsniPBinV6AddressServiceData;sGSNPLMNIdentifier;datavolumeFBCUplink;"
    		+ "datavolumeFBCDownlink;timeOfReport;failureHandlingContinue;serviceIdentifier;pSFreeFormatDataB2;"
    		+ "pSFFDAppendIndicatorB2;aFRecordInformation;userLocationInformationServiceData;numberOfEvents;eventTimeStamps;"
    		+ "requestTypeCreditControl;requestStatusCreditControl;resultCodeCreditControl;ccRequestNumberCreditControl;"
    		+ "creditControlSessionId;ccsRealm;requestTypePolicyControl;requestStatusPolicyControl;resultCodePolicyControl;"
    		+ "stopTimePolicyControl;pcsRealm;policyControlSessionId;userCategory;ruleSpaceId;ratingGroupServiceContainer;"
    		+ "method;inactivity;resolution;ccRequestNumber;serviceSpecificUnits;count;uri;uriIdentifier;uriDataVolumeUplink;"
    		+ "uriDataVolumeDownlink;listOfUriTimeStamps;uriTimeStamp;ratingGroupTimeReport;startTimeReport;endTime;"
    		+ "dataVolumeUplink;dataVolumeDownlink;userLocationInformation3GPP2;pSFreeFormatDataServiceData;"
    		+ "pSFFDAppendIndicatorServiceData;afChargingIdentifier;userLocationInformationServiceData3GPP2;servingNodeType;"
    		+ "startTime;stopTime";
    
    public static final String mEricssonHeaderNoCharge =
    		"recordType|servedIMSI|sGWiPBinV4Address|chargingID|servingNodeiPBinV4Address|accessPointNameNI|"
			+ "pdpPDNType|servedPDPPDNiPBinV4Address|servedPDPPDNiPBinV6Address|dataVolumeGPRSUplink|"
			+ "dataVolumeGPRSDownlink|changeConditionStr|changeTime|userLocationInformation|ePCQoSqCI|"
			+ "ePCQoSmaxRequestedBandwithUL|ePCQoSmaxRequestedBandwithDL|ePCQoSguaranteedBitrateUL|"
			+ "ePCQoSguaranteedBitrateDL|ePCQoSaRP|recordOpeningTime|duration|causeForRecClosing|recordSequenceNumber|"
			+ "nodeID|localSequenceNumber|servedMSISDN|chargingCharacteristics|servingNodePLMNIdentifier|servedIMEISV|"
			+ "rATType|mSTimeZone|sGWChange|servingNodeType|pGWiPBinV4Address|pGWiPBinV6Address|pGWPLMNIdentifier|"
			+ "pDNConnectionID|iMSIunauthenticatedFlag|servedPDPPDNiPBinV4AddressExt|servedPDPPDNiPBinV6AddressExt|"
			+ "sGWiPv6Address|servingNodeiPv6Address|pGWiPv6AddressUsed|networkInitiation|servedIMEI|sgsniPBinV4Address|"
			+ "msNetworkCapability|routingArea|locationAreaCode|cellIdentifier|ggsniPBinV4Address|pdpType|servedPDPiPBinV4Address|"
			+ "servedPDPiPBinV6Address|qosRequested|qosNegotiated|sgsnChange|gsm0408Cause|gsm0902MapErrorValue|"
			+ "identifier|significance|ts48018BssgpCause|ts25413RanapCause|bssgpExttsBssgpRanapCauseBssgp|"
			+ "bssgpExttsBssgpRanapMessageType|bssgpExttsBssgpRanapMessageSource|bssgpExttsBssgpRanapCauseTimeStamp|"
			+ "ranapExttsBssgpRanapCause|ranapExttsBssgpRanapMessageType|ranapExttsBssgpRanapMessageSource|ranapExttsBssgpRanapCauseTimeStamp|"
			+ "apnSelectionMode|accessPointNameOI|sCFAddress|serviceKey|defaultTransactionHandling|cAMELAccessPointNameNI|"
			+ "cAMELAccessPointNameOI|numberOfDPEncountered|levelOfCAMELService|freeFormatData|FDAppendIndicator|"
			+ "chChSelectionMode|dynamicAddressFlag|pLMNIdentifier|serviceCentre|recordingEntity|locationArea|"
			+ "messageReference|eventTimeStamp|destinationNumber|defaultSMSHandling|cAMELCallingPartyNumber|"
			+ "cAMELDestinationSubscriberNumber|cAMELSMSCAddress|smsReferenceNumber|numberOfSM|locationAreaLastSM|"
			+ "routingAreaLastSM|cellIdentifierLastSM|pLMNIdentifierLastSM|accessPointName|reliability|delay|precedence|"
			+ "peakThroughput|meanThroughput|changeCondition|causeForRecClosing|sgsnPLMNIdentifier|apnSelectionMode|"
			+ "umtsQosInformation|qoSNegotiated|iMSsignalingContext|pSFreeFormatDataBC|pSFFDAppendIndicatorBC|ratingGroup|"
			+ "resultCode|timeOfFirstUsage|timeOfLastUsage|timeUsage|serviceConditionChange|qoSInformationNeg|"
			+ "sgsniPBinV4AddressServiceData|sgsniPBinV6AddressServiceData|sGSNPLMNIdentifier|datavolumeFBCUplink|"
			+ "datavolumeFBCDownlink|timeOfReport|failureHandlingContinue|serviceIdentifier|pSFreeFormatDataB2|"
			+ "pSFFDAppendIndicatorB2|aFRecordInformation|userLocationInformationServiceData|numberOfEvents|eventTimeStamps|"
			+ "requestTypeCreditControl|requestStatusCreditControl|resultCodeCreditControl|ccRequestNumberCreditControl|"
			+ "creditControlSessionId|ccsRealm|requestTypePolicyControl|requestStatusPolicyControl|resultCodePolicyControl|"
			+ "stopTimePolicyControl|pcsRealm|policyControlSessionId|userCategory|ruleSpaceId|ratingGroupServiceContainer|"
			+ "method|inactivity|resolution|ccRequestNumber|serviceSpecificUnits|count|uri|uriIdentifier|uriDataVolumeUplink|"
			+ "uriDataVolumeDownlink|listOfUriTimeStamps|uriTimeStamp|ratingGroupTimeReport|startTimeReport|endTime|"
			+ "dataVolumeUplink|dataVolumeDownlink|userLocationInformation3GPP2|pSFreeFormatDataServiceData|"
			+ "pSFFDAppendIndicatorServiceData|afChargingIdentifier|userLocationInformationServiceData3GPP2|servingNodeType|"
			+ "startTime|stopTime";

    /**
     * 
	 * Added by H.Anh
     */
    public final static String pgwNokiaLTEHeader = "Record;recordType;severdIMSI;pgwAddress;chargingId;servingNodeAddress;"
    		+"accessPointNameID;pdpPDNType;severedPDPPDNAdress;dynamicAddressFlag;"
    		+ "recordOpeningTime;duration;causeForRecordClosing;Diagnostics;recordSequenceNumber;nodeId;localSequenceNumber;"
    		+ "apnSelectionMode;servedMSISDN;chargingCharacteristics;chChSelectionMode;servingNodePLMNId"
    		+ ";servedIMEISV;rATType;msTimeZone;userLocationInfor;"
    		+ "ratingGroup;chargingRuleBaseName;resultCode;localSequenceNumber;timeOfFirstUsage;timeOfLastUsage;"
    		+ "timeUsage;serviceConditionChange;qoSInformationNeg;servingNodeAddress;SGSNPLMNIdentifier;datavolumeFBCUplink;"
    		+ "datavolumeFBCDownlink;timeOfReport;rATType;failureHandlingContinue;serviceIdentifier;userLocationInfor;"
    		
    		+ "pSFurnishChargingInfor;aFRecordInformation;eventBasedChargingInformation;timeQuotaMechanism;"
    		+ "NetworkInitiatedPDPContext;iMSSignalingContext;externalChargingID;PSFurnishChargingInformation;CAMELInformation";
    
    public final static String sgwNokiaLTEHeader = "STT|recordType|networkInitiation|severdIMSI|servedIMEISV|sGWAddress|msNetworkCapability|"
    		+ "routingAreaCode|locationAreaCode|userLocationInformation|chargingID|pGWAddressUsed|accessPointNameNI|pdpPDNType|servedPDPPDNAddress|datavolumeFBCUplink|"
    		+ "datavolumeFBCDownlink|recordOpeningTime|duration|sGWChange|causeForRecClosing|diagnostics|recordSequenceNumber|nodeID|"
    		+ "recordExtensions|localSequenceNumber|apnSelectionMode|accessPointNameOI|servedMSISDN|chargingCharacteristics|"
    		+ "rATType|cAMELInformationPDP|rNCUnsentDownlinkVolume|chChSelectionMode|dynamicAddressFlag|numberOfSeq";
    
    /**
     * 
	 * Added by H.Anh
     */
    
    public static final String IGW_SONUS_HEADER = "RECORD_TYPE|GATE_WAY_NAME|START_TIME_SYS|NODE_TIME|START_DATE|START_TIME" +
    		"|TIME_RX_TO_PSX|TIME_RX_TO_ALERT|TIME_RX_TO_SERVEST|DISCONNET_DATE|DISCONNET_TIME|TIME_DISC_RX_TO_COMP|CALL_SERVICE_DURATION" +
    		"|CALL_DISCONNECT_REASON|SERVICE_DELIVER|CALL_DIRECTION|SERVICE_PROVIDER|CALLING_NUMBER|CALLED_NUMBER|TRANS_1|TRANS_2|BILLING_NUMBER"+
    		"|ROUTE_LABEL|ROUTE_ATTEMPT_NUMBER|ROUTE_SELECTED_GATEWAY|ROUTE_SELECTED_TRUNK_GROUP|EGRESS_LOCAL|EGRESS_REMOTE|INGRESS_TRUNK_NAME|DIAL_NUMBER"+
    		"|DISCONNECT_INITIATOR|EGRESS_TRUNK_NAME|INCOMMING_CALL_NUMBER|CPG_NUMBER|INGRESS_NET_TYPE|INGRESS_CODEC_TYPE|INGRESS_AUDIO_ENCODE" +
    		"|EGRESS_NET_TYPE|EGRESS_CODEC_TYPE|EGRESS_AUDIO_ENCODE|POLICY_REPONSE_CALL|INGRESS_LOCAL_IP|INGRESS_REMOTE_IP|OVERLOAD_STATUS"
    		+"|ACC_ID|INGRESS_PSTN|INGRESS_IP|EGERSS_PSTN|EGERSS_IP|CALL_GROUP_ID|CALLING_NAME|INGRESS_LOCAL_IP|INGRESS_REMOTE_IP"
    		+"|CALLING_NUMBER_CHANGE|CALLED_NUMBER_CHANGE";

    public static final String FCDR_HEADER = "Date|Time|inbound|Orig_msis|Orig_msisUTF8|Recip_msis|Recip_msisUTF8|ogti_msis|ogti_msisUTF8|origIntlMobileSubId|RoutingNumber|smsContentDcs"
			+"|smscPre_msis|smscPre_msisUTF8|intlMobileSubId|length|dgti_msis|dgti_msisUTF8";
  
    public void finalize()
    {
        try
        {
            mstrThreadID = null;
            mstrThreadName = null;
            mstrLogFileName = null;

            // Release memory
            System.runFinalization();
            System.gc();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public static int FormatDayOfWeek(String pDateIn, String pformatIn)
    {
        String Days[] =
            {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        int j = 0;
        for (int i = 0; i < Days.length; i++)
        {
            if (Days[i].compareTo((new java.text.SimpleDateFormat("E")).format((new java.text.SimpleDateFormat(pformatIn)).parse(pDateIn, new java.text.ParsePosition(0)))) == 0)
            {
                j = i;
                break;
            }
        }
        return (j + 1);
    }

    public static String Format(java.util.Date dtImput, String strPattern)
    {
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat(strPattern);
        return fmt.format(dtImput);
    }

    public static String Format(long number, String strPattern)
    {
        java.text.DecimalFormat fmt = new java.text.DecimalFormat(strPattern);
        return fmt.format(number);
    }

    public static String Format(String pDateIn, String pformatIn, String pformatOut)
    {
        return (new SimpleDateFormat(pformatOut)).format((new SimpleDateFormat(pformatIn)).parse(pDateIn, new ParsePosition(0)));
    }

    public static long convertDateTimeToLong(String pDateTime)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        java.util.Date date = null;
        try
        {
            date = sdf.parse(pDateTime);
        }
        catch (Exception e)
        {
            date = new Date();
            System.err.println(Global.Format(new java.util.Date(), "dd/MM/yyyy HH:mm:ss") + " : - ERROR in module convertDateTimeToLong : " + e.toString());
        }
        return date.getTime();
    }

    /**
     * Compare two date string
     * @param pDateTime1 String
     * @param pDateTime2 String
     * @return int
     * -1: error format date
     *  0: pDateTime1 = pDateTime2
     *  1: pDateTime1 < pDateTime2
     *  2: pDateTime1 > pDateTime2
     */
    public static int compareTo(String pDateTime1, String pDateTime2)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        java.util.Date date1 = null;
        java.util.Date date2 = null;
        try
        {
            date1 = sdf.parse(pDateTime1);
            date2 = sdf.parse(pDateTime2);
            if (date1.before(date2))
            {
                return 1;
            }
            else if (date1.after(date2))
            {
                return 2;
            }
        }
        catch (Exception e)
        {
            return -1;
        }
        return 0;
    }

    public static String Format(double number, String strPattern)
    {
        java.text.DecimalFormat fmt = new java.text.DecimalFormat(strPattern);
        return fmt.format(number);
    }

    public int GetSequenceImportHeader(java.sql.Connection cn) throws SQLException
    {
        Statement stmt = cn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT import_id_seq.NEXTVAL FROM DUAL");
        if (!rs.next())
        {
            rs.close();
            rs = null;
            stmt.close();
            stmt = null;

            throw new SQLException("Sequence import_seq not found");
        }
        int mReturn = rs.getInt(1);
        rs.close();
        rs = null;
        stmt.close();
        stmt = null;
        return mReturn;
    }

    public int GetSequenceExportHeader(java.sql.Connection cn) throws SQLException
    {
        Statement stmt = cn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT export_id_seq.NEXTVAL FROM DUAL");
        if (!rs.next())
        {
            rs.close();
            rs = null;
            stmt.close();
            stmt = null;

            throw new SQLException("Sequence export_seq not found");
        }
        int mReturn = rs.getInt(1);
        rs.close();
        rs = null;
        stmt.close();
        stmt = null;
        return mReturn;
    }

    public static boolean TableExisted(java.sql.Connection cn, String pTableOwner, String pTableName) throws SQLException
    {
        int mResult = 0;
        String sql = "begin ?:=cdrfile.table_existed(?,?); end;";
        CallableStatement cs = cn.prepareCall(sql);
        try
        {
            cs.registerOutParameter(1, Types.INTEGER);
            cs.setString(2, pTableOwner);
            cs.setString(3, pTableName);
            cs.execute();
            mResult = cs.getInt(1);
        }
        catch (SQLException ex)
        {
            throw ex;
        }
        finally
        {
            try
            {
                cs.close();
                cs = null;
            }
            catch (Exception e)
            {
            }
        }
        if (mResult == 0)
        {
            return (false);
        }
        else
        {
            return (true);
        }
    }

    public static int ExecuteSQL(java.sql.Connection cn, String pSQL) throws SQLException
    {
        int mRecReturn = 0;
        Statement stmt = cn.createStatement();
        try
        {
            mRecReturn = stmt.executeUpdate(pSQL);
        }
        catch (SQLException e)
        {
            throw e;
        }
        finally
        {
            try
            {
                stmt.close();
                stmt = null;
            }
            catch (Exception e)
            {
            }
        }
        return mRecReturn;
    }

    public int UpdateStatusFileDispose(java.sql.Connection cn, long pFileID, int pStatus, String pString) throws Exception
    {
        int mRecReturn = 0;
        String mSQL = "UPDATE import_header SET status=" + pStatus + ",note='" + pString + "'" + " WHERE file_id = " + pFileID;
        mRecReturn = ExecuteSQL(cn, mSQL);
        return mRecReturn;
    }

    public static int ExecuteOutParameterInt(java.sql.Connection cn, String pSQL) throws SQLException
    {
        int mResult = 0;
        String sql = "begin " + pSQL + "; end;";
        CallableStatement cs = cn.prepareCall(sql);
        try
        {
            cs.registerOutParameter(1, Types.INTEGER);
            cs.execute();
            mResult = cs.getInt(1);
        }
        catch (SQLException ex)
        {
            throw ex;
        }
        finally
        {
            try
            {
                cs.close();
                cs = null;
            }
            catch (Exception e)
            {
            }
        }

        return (mResult);
    }

    public static String ExecuteOutParameterStr(java.sql.Connection cn, String pSQL) throws SQLException
    {
        String mResult = "";
        String sql = "begin " + pSQL + "; end;";
        CallableStatement cs = cn.prepareCall(sql);
        try
        {
            cs.registerOutParameter(1, Types.VARCHAR);
            cs.execute();
            mResult = cs.getString(1);
        }
        catch (SQLException ex)
        {
            throw ex;
        }
        finally
        {
            try
            {
                cs.close();
                cs = null;
            }
            catch (Exception e)
            {
            }
        }

        return (mResult);
    }

    public String Iif(String pExpression, String pReturnTrue, String pReturnFalse)
    {
        return "0"; // (pExpression ? pReturnTrue : pReturnFalse);
    }

    public static void writeEventThreadErr(int pThreadID, int pEventLevel, String pEventName) throws SQLException
    {
        Connection cn = null;
        try
        {
            cn = ClientUtil.openNewConnection();
            String strSQL = "INSERT INTO event_thread_err(event_level,event_date," + "thread_id,event_name) values(" + pEventLevel + ",sysdate," + pThreadID + ",'" + pEventName + "')";
            Statement stmt = cn.createStatement();
            stmt.executeUpdate(strSQL);
            stmt.close();
        }
        catch (Exception e)
        {
        }
        finally
        {
            try
            {
                cn.close();
            }
            catch (Exception e)
            {
            }
        }
    }

    public void writeLogFile(String pStrLog)
    {
        configLogFile();
        pStrLog = Format(new java.util.Date(), "dd/MM/yyyy HH:mm:ss") + " : " + pStrLog;
        if (mstrLogFileName != null && !mstrLogFileName.equals(""))
        {
            try
            {
                RandomAccessFile fl = new RandomAccessFile(mstrLogFileName, "rw");
                fl.seek(fl.length());
                fl.writeBytes(pStrLog + "\r\n");
                fl.close();
            }
            catch (Exception e)
            {
                System.out.println("Error in write log.");
                e.printStackTrace();
            }
        }
    }

    private void configLogFile()
    {
        java.util.Date dtNow = new java.util.Date();
        if (!mstrLogPathFileName.equals(""))
        {
            if (!mstrLogPathFileName.endsWith("/") && !mstrLogPathFileName.endsWith("\\"))
            {
                mstrLogPathFileName += "/";
            }
            IOUtil.forceFolderExist(mstrLogPathFileName + Global.Format(dtNow, "yyyyMMdd") + "/");
            mstrLogFileName = mstrLogPathFileName + Global.Format(dtNow, "yyyyMMdd") + "/ThreadID_" + mstrThreadID + "_" + mstrThreadName + ".log";
        }
        dtNow = null;
    }

    public void setThreadID(String pStrThreadID)
    {
        mstrThreadID = pStrThreadID;
    }

    public String getThreadID()
    {
        return mstrThreadID;
    }

    public void setThreadName(String pStrThreadName)
    {
        mstrThreadName = pStrThreadName;
    }

    public String getThreadName()
    {
        return mstrThreadName;
    }

    public void setLogPathFileName(String pStrLogName)
    {
        mstrLogPathFileName = pStrLogName;
    }

    public String getLogPathFileName()
    {
        return mstrLogPathFileName;
    }

    public static boolean isNumeric_std(String s)
    {
        String mValue = "";
        try
        {
            for (int i = 0; i < s.length(); i++)
            {
                if (s.substring(i, i + 1).compareTo("0") == 0)
                {
                    mValue = s.substring(i + 1);
                }
                else
                {
                    break;
                }
            }
            if (mValue.compareTo("") == 0)
            {
                mValue = s;
            }
            Long.parseLong(mValue);
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    public static boolean isNumeric(String s)
    {
        try
        {
            Long.parseLong(s);
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    public static String lpad(String pStrPattern, int pLength, String pStrReverse)
    {
        String mStrTmp = "";
        if (pStrPattern.compareTo("") == 0)
        {
            for (int i = 0; i < pLength; i++)
            {
                mStrTmp += pStrReverse;
            }
            return mStrTmp;
        }
        if (pStrPattern.length() > pLength)
        {
            mStrTmp = pStrPattern.substring(0, pLength);
        }
        else
        {
            mStrTmp = pStrPattern;
            for (int i = pStrPattern.length(); i < pLength; i++)
            {
                mStrTmp += pStrReverse;
            }
        }
        return mStrTmp;
    }

    public static String rpad(String pStrPattern, int pLength, String pStrReverse)
    {
        String mStrTmp = "";
        if (pStrPattern.compareTo("") == 0)
        {
            for (int i = 0; i < pLength; i++)
            {
                mStrTmp += pStrReverse;
            }
            return mStrTmp;
        }
        if (pStrPattern.length() > pLength)
        {
            mStrTmp = pStrPattern.substring(0, pLength);
        }
        else
        {
            for (int i = pStrPattern.length(); i < pLength; i++)
            {
                mStrTmp += pStrReverse;
            }
            mStrTmp = mStrTmp + pStrPattern;
        }
        return mStrTmp;
    }

    public static String HexToBCD(int pByte)
    {
        String mByte;
        if (Integer.toHexString(pByte).length() == 1)
        {
            mByte = "0" + Integer.toHexString(pByte);
        }
        else
        {
            mByte = Integer.toHexString(pByte);
        }
        return mByte;
    }

    public static String HexToTBCD(int pByte)
    {
        String mByte;
        if (Integer.toHexString(pByte).length() == 1)
        {
            mByte = Integer.toHexString(pByte) + "0";
        }
        else
        {
            mByte = Integer.toHexString(pByte).substring(Integer.toHexString(pByte).length() - 1) + Integer.toHexString(pByte).substring(0, 1);
        }
        return mByte;
    }
    public static String HexToTBCDHA(int pByte)
    {
        String mByte;
        if (Integer.toHexString(pByte).length() == 1)
        {
            mByte = Integer.toHexString(pByte) + "0";
        }
        else
        {
        	String a = Integer.toHexString(pByte).substring(0, 1);
        	if( (a.equals("f"))){
        		mByte = Integer.toHexString(pByte).substring(Integer.toHexString(pByte).length() - 1) +"";
        	}
        	else {
        		mByte = Integer.toHexString(pByte).substring(Integer.toHexString(pByte).length() - 1) + a+"";
        	}
        }
        return mByte;
    }
    public static String HexToText(int pByte)
    {
        String mByte;
        if (Integer.toHexString(pByte).length() == 1)
        {
            mByte ="0" + Integer.toHexString(pByte) ;
        }
        else
        {
        	
        		mByte = Integer.toHexString(pByte);
        	
        }
        return mByte;
    }
    public static String HexToDecimal(int pByte)
    {
        return Format(pByte, "00");
    }

    public static String Dec2Hex(int d)
    {
        String digits = "0123456789ABCDEF";
        if (d == 0)
        {
            return "0";
        }
        String hex = "";
        while (d > 0)
        {
            int digit = d % 16; // rightmost digit
            hex = digits.charAt(digit) + hex; // string concatenation
            d = d / 16;
        }
        return hex;
    }

    public static int Hex2Dec(String s)
    {
        String digits = "0123456789ABCDEF";
        s = s.toUpperCase();
        int val = 0;
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            int d = digits.indexOf(c);
            val = 16 * val + d;
        }
        return val;
    }
    
    public static long Hex2DecLong(String s)
    {
        String digits = "0123456789ABCDEF";
        s = s.toUpperCase();
        long val = 0;
        for (int i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            long d = digits.indexOf(c);
            val = 16 * val + d;
        }
        return val;
    }

    public static String HexToBINARY(int pByte)
    {
        String mByte;
        switch (pByte)
        {
        case 0:
            mByte = "00000000";
            break;
        default:
            mByte = Integer.toBinaryString(pByte);
            break;
        }
        return mByte;
    }

    public static int fixSignedByte(byte byteValue)
    {
        if (byteValue >= 0)
        {
            return byteValue;
        }
        else
        {
            return (byteValue + 256);
        }
    }

    public static char fixSignedCharByte(int btValue)
    {
        if (btValue >= 0)
        {
            return (char) btValue;
        }
        return (char) (btValue + 256);
    }

    public static long convertTomiliseconds(long pSeconds)
    {
        return (long) (pSeconds * 1000.0);
    }

    public static int BinToDecimal(String pByte)
    {
        return Integer.parseInt(pByte, 2);
    }

    public static int BinToDecimal(int pNumber)
    {
        int decimalTotal = 0;
        int powerOfTwo = 1;
        int digit;
        while (pNumber > 0)
        {
            digit = pNumber % 10;
            decimalTotal += (digit * powerOfTwo);
            powerOfTwo = powerOfTwo * 2;
            pNumber = (pNumber - digit) / 10;
        }
        return decimalTotal;
    }

    public static int BinToDecimal(byte[] pbuffer, int offset, int len)
    {
        if (len <= 0)
        {
            return 0;
        }
        int val = 0;
        for (int i = offset; i < offset + len; i++)
        {
            val <<= 8;
            val |= fixSignedByte(pbuffer[i]);
        }
        return val;
    }

    public static long BinToLong(byte[] pbuffer, int offset, int len)
    {
        if (len <= 0)
        {
            return 0;
        }
        long val = 0;
        for (int i = offset; i < offset + len; i++)
        {
            val <<= 8;
            val |= fixSignedByte(pbuffer[i]);
        }
        return val;
    }


    public static String BinToHexa(int pNumber)
    {
        char val, l, h;
        String value = "";
        for (int i = 0; i < Integer.toString(pNumber).length(); i++)
        {
            val = fixSignedCharByte(pNumber);
            h = (char) (val >> 4);
            if (h < 10)
            {
                h = (char) ('0' + h);
            }
            else
            {
                h = (char) ('A' + h - 10);
            }

            val <<= 12;
            l = (char) (val >> 12);
            if (l < 10)
            {
                l = (char) ('0' + l);
            }
            else
            {
                l = (char) ('A' + l - 10);
            }

            value += h;
            value += l;
        }
        return value;
    }

    public static String DecToHexa(String pNumber)
    {
        return Integer.toHexString(Integer.parseInt(pNumber));
    }

    public static int TimeToSeconds(String pTimeString)
    {
        String mHour = "";
        String mMinute = "";
        String mSecond = "";
        int mToSecond = 0;
        if (pTimeString.indexOf(":") > 0)
        {
            mHour = pTimeString.substring(0, 2);
            mMinute = pTimeString.substring(3, 5);
            mSecond = pTimeString.substring(6);
            mToSecond = Integer.parseInt(mHour) * 3600 + Integer.parseInt(mMinute) * 60 + Integer.parseInt(mSecond);
        }
        else
        {
            mHour = pTimeString.substring(0, 2);
            mMinute = pTimeString.substring(2, 4);
            mSecond = pTimeString.substring(4);
            mToSecond = Integer.parseInt(mHour) * 3600 + Integer.parseInt(mMinute) * 60 + Integer.parseInt(mSecond);
        }
        return (mToSecond);
    }

    public static int getbyteValues(byte iNumber)
    {

        return iNumber;
    }

    public static String increaseNumber(String iNumber)
    {

        if (String.valueOf(iNumber).length() < 2)
        {
            return "0" + iNumber;
        }
        else
        {
            return String.valueOf(iNumber);
        }
    }

    public static String cdate(String pDate)
    {
        String mDay = null;
        String mMonth = null;
        String mYear = null;
        String mHour = "00";
        String mMinute = "00";
        String tmp = null;
        String mCurrentDateTime = null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

        mCurrentDateTime = sdf.format(new java.util.Date());
        mYear = mCurrentDateTime.substring(0, 4);

        tmp = pDate;
        mMonth = tmp.substring(0, 3);
        tmp = tmp.substring(4);
        mDay = tmp.substring(0, 2);
        if (Integer.parseInt(mDay.trim()) < 10)
        {
            mDay = "0" + mDay.trim();
        }
        if (tmp.indexOf(":") > 0)
        {
            tmp = tmp.substring(tmp.indexOf(" ") + 1);
            mHour = tmp.substring(0, 2);
            tmp = tmp.substring(tmp.indexOf(":") + 1);
            mMinute = tmp;
        }
        else
        {
            mYear = tmp.substring(tmp.indexOf(" ") + 1);
        }

        if (mMonth.compareTo("Jan") == 0)
        {
            mMonth = "01";
        }
        else if (mMonth.compareTo("Feb") == 0)
        {
            mMonth = "02";
        }
        else if (mMonth.compareTo("Mar") == 0)
        {
            mMonth = "03";
        }
        else if (mMonth.compareTo("Apr") == 0)
        {
            mMonth = "04";
        }
        else if (mMonth.compareTo("May") == 0)
        {
            mMonth = "05";
        }
        else if (mMonth.compareTo("Jun") == 0)
        {
            mMonth = "06";
        }
        else if (mMonth.compareTo("Jul") == 0)
        {
            mMonth = "07";
        }
        else if (mMonth.compareTo("Aug") == 0)
        {
            mMonth = "08";
        }
        else if (mMonth.compareTo("Sep") == 0)
        {
            mMonth = "09";
        }
        else if (mMonth.compareTo("Oct") == 0)
        {
            mMonth = "10";
        }
        else if (mMonth.compareTo("Nov") == 0)
        {
            mMonth = "11";
        }
        else if (mMonth.compareTo("Dec") == 0)
        {
            mMonth = "12";
        }
        tmp = mYear + mMonth.trim() + mDay.trim();

        if (Integer.parseInt(tmp) > Integer.parseInt(mCurrentDateTime.substring(0, 8)))
        {
            tmp = (Integer.parseInt(mYear) - 1) + mMonth + mDay + mHour + mMinute + "00";
        }
        else
        {
            tmp = mYear + mMonth.trim() + mDay.trim() + mHour.trim() + mMinute.trim() + "00";
        }

        mDay = null;
        mMonth = null;
        mYear = null;
        mHour = "00";
        mMinute = "00";
        sdf = null;
        mCurrentDateTime = null;
        return tmp;
    }

    public static String nvl(String strInput, String strReturnValue)
    {
        if (strInput == null)
        {
            return strReturnValue;
        }
        return strInput;
    }

    public static double round(double value, int decimalPlace)
    {
        double power_of_ten = 1;
        while (decimalPlace-- > 0)
        {
            power_of_ten *= 10.0;
        }
        return Math.round(value * power_of_ten) / power_of_ten;
    } 

    @SuppressWarnings("resource")
	public static int CheckFileErr(String fileName, int fileType) throws Exception
    {
        int err = 0;
        //File f = new File(fileName);
        FileReader fileReader = new FileReader(fileName);
        BufferedReader fh = new BufferedReader(fileReader);
        String mStrHeader = "";
        //err = f.length();
        try
        {
            fileReader = new FileReader(fileName);
            String s;
            Vector mArrHeaders = null;
            Vector mArrValues = null;

            switch (fileType)
            {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 7:
                mStrHeader = mMSCHeaderNoCharge;
                break;
            case 9:
            case 10:
                mStrHeader = mPPSHeaderNoCharge;
                break;
            case 12:
                mStrHeader = mSMSCHeaderNoCharge;
                break;
            case 14:
                mStrHeader = mRTPVHeaderNoCharge;
                break;
            case 15:
                mStrHeader = mHeaderForAcounting;
                break;
            case 16:
                mStrHeader = mMOHeader;
                break;
            case 17:
                mStrHeader = mMTHeader;
                break;
            case 18:
                mStrHeader = mICCHeaderNoCharge;
                break;

            default:
                break;
            }
            mArrHeaders = StringUtils.vectorFromString(mStrHeader, "|");
            for (int intIndex = 0; intIndex < mArrHeaders.size(); intIndex++)
            {
                mArrHeaders.setElementAt(mArrHeaders.elementAt(intIndex).toString().toUpperCase(), intIndex);
            }
            while ((s = fh.readLine()) != null)
            {
                mArrValues = StringUtils.vectorFromString(s, "|");
                if (mArrValues.size() != mArrHeaders.size())
                {
                    err = 1;
                    break;
                }
            }
        }
        catch (Exception e)
        {
            err = 1;
        }
        finally
        {
            fileReader.close();
            fh.close();
        }
        return err;
    }

    public static int CheckFileNoRecord(String pPathFileName) throws Exception
    {
        String mHeaderNoCharge = "STT|FC|call type|po code|tax airtime|tax idd|tax service|" + "calling isdn|imsi|call sta time|duration|call end time|" + "called isdn|cell id|service center|ic route|og route|" + "tar class|ts code|bs code|in mark|char indi|org call id|" + "rec seq num|translate num|calling imei|calling org|" + "called org|subs type|bl air|bl idd/ser|calling cen|" + "called cen|collect type";

        String mHeaderCharge = "RecType;CallType;CallingISDN;IMSI;CallStaTime;" + "CallDuration;CallEndTime;CalledISDN;CellID;" + "ServiceCenter;IcRoute;OgRoute;TarClass;" + "ReqTel;ReqBeare;INSer;CharInd;CallOrgISDN;" + "TransISDN;RecSeq;IMEI;CallingOrg;CalledOrg";
        Vector vtFieldValue = null;
        int[] miDelimitedFields;
        DelimitedFile delimitedFile = new DelimitedFile();
        RandomAccessFile fileChecked = null;
        int mRec = 1;
        try
        {
            fileChecked = new RandomAccessFile(pPathFileName, "r");
            if (fileChecked.length() < 200)
            {
                delimitedFile.openDelimitedFile(pPathFileName, 5242880);
                if (cdrfileParam.ChargeCDRFile)
                {
                    vtFieldValue = StringUtils.vectorFromString(mHeaderCharge, ";");
                }
                else
                {
                    vtFieldValue = StringUtils.vectorFromString(mHeaderNoCharge, ";");
                }

                miDelimitedFields = new int[vtFieldValue.size()];
                for (int i = 0; i < miDelimitedFields.length; i++)
                {
                    miDelimitedFields[i] = delimitedFile.findColumn(((String) vtFieldValue.elementAt(i)).trim());
                }
                if (delimitedFile.next() == false)
                {
                    mRec = 0;
                }
                else
                {
                    mRec = 1;
                }
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
        finally
        {
            try
            {
                fileChecked.close();
                fileChecked = null;
                delimitedFile.closeDelimitedFile();
                delimitedFile = null;
                vtFieldValue = null;
            }
            catch (Exception e)
            {
            }
        }
        return mRec;
    }


    public static String getLocalSvrIP() throws UnknownHostException
    {
        String thisIP = "";
        try
        {
            InetAddress addr = InetAddress.getLocalHost();
            thisIP = addr.getHostAddress().toString();
        }
        catch (UnknownHostException e)
        {
        }
        return thisIP;
    }

    public static String getRemoteSvrIP(Connection pCn, String pLocalIP) throws Exception
    {
        String mIP = "";
        String strSQL = "SELECT ip into ? from node_cluster where ip='" + pLocalIP + "' and status='ACTIVE'";
        try
        {
            mIP = Global.ExecuteOutParameterStr(pCn, strSQL);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return mIP;
    }
    
    public static int getDayNow(){
    	Calendar cal = Calendar.getInstance();
    	return cal.get(Calendar.DAY_OF_MONTH); 
    }
    
    /**
     * @param date yyyyMMddHHmmss
     * @param timezone
     * */
    public static String FormatFullDate(String date, int timezone)
    {
    	String[] days = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};
    	String result = "";
    	
    	if(date != null && !date.equals("") && date.contains("+")) 
    		date = date.substring(0,date.indexOf("+"));
    	
    	SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
    	SimpleDateFormat ndf = new SimpleDateFormat("MMM dd HH:mm:ss yyyy");
    	
    	Calendar c = Calendar.getInstance();
    	Calendar c_now = Calendar.getInstance();
    	
    	try {
    		c_now.setTime(new Date());
			c.setTime(sdf.parse(date));
			c.add(Calendar.HOUR_OF_DAY, timezone); 
			
			if(c_now.getTime().compareTo(c.getTime()) < 0){
				result = "";
			}else{
				result = days[c.get(Calendar.DAY_OF_WEEK) - 1] + " "+ ndf.format(c.getTime());
			} 
		} catch (ParseException e) {
			e.printStackTrace(); 
		}   
    	
        return result;
    }
    
    // Read bit in byte
    public static BitSet fromByte(byte b)
    {
        BitSet bits = new BitSet(8);
        for (int i = 1; i <= 8; i++)
        {
            bits.set(i, (b & 1) == 1);
            b >>= 1;
        }
        return bits;
    }
    
    public static String FormatDate(String date, int timezone)
    { 
    	String result = "";
    	
    	if(date != null && !date.equals("") && date.contains("+")) 
    		date = date.substring(0,date.indexOf("+"));
    	
    	SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
    	SimpleDateFormat ndf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    	
    	Calendar c = Calendar.getInstance();
    	Calendar c_now = Calendar.getInstance();
    	
    	try {
    		c_now.setTime(new Date());
			c.setTime(sdf.parse(date));
			c.add(Calendar.HOUR_OF_DAY, timezone); 
			
			if(c_now.getTime().compareTo(c.getTime()) < 0){
				result = "";
			}else{
				result = ndf.format(c.getTime());
			} 
		} catch (ParseException e) {
			e.printStackTrace(); 
		}  
    	
        return result;
    }
    
    public static void main(String arg[]) throws Exception {
		
		String str = "1232131+123";
		System.out.println(str.substring(2));
	}
}
