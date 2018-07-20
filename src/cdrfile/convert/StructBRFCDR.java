package cdrfile.convert;

/**
 * <p>Title: CDR File(s) System</p>
 * <p>Description: VMS IS Departerment</p>
 * <p>Copyright: Copyright (c)  by eKnowledge 2004</p>
 * <p>Company: VietNam Mobile Telecom Services</p>
 * @author eKnowledge - Software
 * @version 5.0
 */

public class StructBRFCDR
{
  //Struc SMSC
  public int BillingTimeByte; //4byte
  public String DestinationAddress = null; //= new String[21];
  public String OriginatorAddress = null; //= new String[21];
  public int MessageClass;
  public int ProtocolID;
  public int MessageReceiveTimeByte;
  public int Status;
  public int HandsetNotificationIndicator;
  public int LastFailureReason;
  public int RPPriorityRequest;
  public int TextSize;
  public int NumberOfDeliveryAttemps;
  public int StatusReportsGenerated;
  public int AcknowledgmentIndicator;
  public String ClientName; // = new String[16];
  public int ServiceType;
  public int ChargeIndicator;
  public int PrivacyIndicator;
  public int ValidityIndicator;
  public int NumberOfMessagesInSMClient;
  public int MessageType;
  public int SubparameterLength;
  public int SubcriberMarketIdentifier;
  public int ServingMSCMarketIdentifier;
  public int MessageID;
  public int GroupID;
  public int ReservedForFutureUse;
  public int TeleserviceId;
  public String CallbackAddress; //= new String[21];
  public String Subparameter; //= new String[24];
  public int AlphabetIndicator;
  public int LanguageIndicator;
  public int SSN;
  public int DPC;
  public String IMSI; //= new String[21];
  public int OriginatingMSC;
  public int ReservedForEndhancement1;
  public int OriginatingMSCPointCode;
  public int OriginatingGroup;
  public int ValidationType;
  public int ReservedForEndhancement2;
  public int BillingSequenceNumber;
  public String SM; //= new String[256];
  public int ReservedForUse;
  //End of structure
  public String CommandCode;
}
