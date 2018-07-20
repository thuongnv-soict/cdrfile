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
public class StructRTPV {
     public String Header;
     public String GlobalServiceIdentifier; // 2 byte
     public String TagCallingNumber;
     public String TagCallType;
     public String TeleserviceIndicator;
     public String TagIMSI;
     public String TagReFill_ScratchType;
     public String TagReFill_ScratchNumber;
     public String INServiceResultIndicator;
     public String NetworkCauseResultIndicator;
     public int TagReFill_ScratchValue;
     public String CallStartDateTime;
     public String RecordType;
     public String CallType;
     public String FmtOfCalling;
     public String FmtOfCalled;
     public String FmtOfTranslatedNumber;
     public String FmtOfOriginalCalled;
     public String CallingOrg;
     public String CalledOrg;
     public int TagCallDuration1;
      public String CallEndDateTime1;
}
