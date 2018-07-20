package cdrfile.global;

public class EnumeRatedSgw {
	
	enum RecordType{ 
		egsnPDPRecord(70), sGWRecord(84), pGWRecord(85);
		private int value;
		private RecordType(int value){this.value = value;}
	}
	enum Ltv_changeCondition{ 
		 qoSChange(0), tariffTime(1), recordClosure(2),
		  failureHandlingContinueOngoing           (3),  // -- only on eG-CDR
		  failureHandlingRetryandTerminateOngoing  (4),   //-- only on eG-CDR
		  failureHandlingTerminateOngoing          (5),   //-- only on eG-CDR
		  cGI_SAICHange                            (6),   //-- bearer modification. "CGI-SAI Change"
		  rAIChange                                (7),  // -- bearer modification. "RAI Change"
		  dT_Establishment                         (8),
		  dT_Removal                               (9),
		  eCGIChange                               (10),  //-- bearer modification. "ECGI Change". Not in eG-CDRs
		  tAIChange                                (11), // -- bearer modification. "TAI Change". Not �in eG-CDRs
		  userLocationChange                       (12) ;
		private int value;
		private Ltv_changeCondition(int value){this.value = value;}
	}
	enum ServingNodeType{
		sGSN(0), pMIPSGW(1) ,gTPSGW(2) ,ePDG(3), hSGW(4) ,mME(5);   
		private int value;
		private ServingNodeType(int value) {this.value = value;}
	}
	enum CSGAccessMode{
		closeMode(0), hybridMode(1);   
		private int value;
		private CSGAccessMode(int value){this.value = value;}
	}
	enum SubscriptionIdType{
		eND_USER_E164(0), eND_USER_IMSI(1) ,eND_USER_SIP_URI(2) ,eND_USER_NAI(3), eND_USER_PRIVATE(4);   
		private int value;
		private SubscriptionIdType(int value) {this.value = value;}
	}
	enum TimeQuotaType{
		dISCRETETIMEPERIOD(0), cONTINUOUSTIMEPERIOD(1) ;   
		private int value;
		private TimeQuotaType(int value) {this.value = value;}
	}
	enum PositionMethodFailure_Diagnostic{
		congestion									(0), 
		insufficientResources						(1), 
		insufficientMeasurementData					(2),
		inconsistentMeasurementData					(3), 
		locationProcedureNotCompleted				(4), 
		locationProcedureNotSupportedByTargetMS		(5),
		qoSNotAttainable							(6), 
		positionMethodNotAvailableInNetwork			(7), 
		positionMethodNotAvailableInLocationArea	(8);
		private int value;
		private PositionMethodFailure_Diagnostic(int value) {this.value = value;}
	}
	enum UnauthorizedLCSClient_Diagnostic{
		noAdditionalInformation(0), clientNotInMSPrivacyExceptionList(1),
		callToClientNotSetup(2),  privacyOverrideNotApplicable(3),
		disallowedByLocalRegulatoryRequirements(4), unauthorizedPrivacyClass(5),
		unauthorizedCallSessionUnrelatedExternalClient(6),  unauthorizedCallSessionRelatedExternalClient(7);
		private int value;
		private UnauthorizedLCSClient_Diagnostic(int value) {this.value = value;}
	}
	enum APNSelectionMode{
		mSorNetworkProvidedSubscriptionVerified (0), mSProvidedSubscriptionNotVerified(1), networkProvidedSubscriptionNotVerified(2);
		private int value;
		private APNSelectionMode(int value) {this.value = value;}
	}
	enum ChChSelectionMode{
		servingNodeSupplied      (0), // For GGSN/S-GW/P-GW
		subscriptionSpecific     (1), //-- For SGSN only
		aPNSpecific              (2), //-- For SGSN only
		homeDefault              (3), //-- For SGSN, GGSN, S-GW and P-GW
		roamingDefault           (4), //-- For SGSN, GGSN, S-GW and P-GW
		visitingDefault          (5);  //-- For SGSN, GGSN, S-GW and P-GW
		private int value;
		private ChChSelectionMode(int value) {this.value = value;}
	}
	
   public static String getEnumeRated(int value, String name){
	   String result="Not Exist";
	   int nameInt=0;
	   if(name.equals("RecordType")){
		   nameInt=1;
	   }else if(name.equals("Ltv_changeCondition")){
		   nameInt=2;
	   }else if(name.equals("ServingNodeType")){
		   nameInt=3;
	   }
	   else if(name.equals("CSGAccessMode")){
		   nameInt=4;
	   }
	   else if(name.equals("SubscriptionIdType")){
		   nameInt=5;
	   }
	   else if(name.equals("TimeQuotaType")){
		   nameInt=6;
	   }
	   else if(name.equals("PositionMethodFailure_Diagnostic")){
		   nameInt=7;
	   }
	   else if(name.equals("UnauthorizedLCSClient_Diagnostic")){
		   nameInt=8;
	   }
	   else if(name.equals("APNSelectionMode")){
		   nameInt=9;
	   }
	   else if(name.equals("ChChSelectionMode")){
		   nameInt=10;
	   }
	   
	   
	   switch (nameInt) {
	   case 1:
			for (RecordType c : RecordType.values()){
				if(value == c.value){
					result= c.toString();
					break;
				}
			}
			break;
		case 2:
			for (Ltv_changeCondition c : Ltv_changeCondition.values()){
				if(value == c.value){
					result= c.toString();
					break;
				}
			}
			break;
		case 3:
			for (ServingNodeType c : ServingNodeType.values()){
				if(value == c.value){
					result= c.toString();
					break;
				}
			}
			break;
		case 4:
			for (CSGAccessMode c : CSGAccessMode.values()){
				if(value == c.value){
					result= c.toString();
					break;
				}
			}
			break;
		case 5:
			for (SubscriptionIdType c : SubscriptionIdType.values()){
				if(value == c.value){
					result= c.toString();
					break;
				}
			}
			break;
		case 6:
			for (TimeQuotaType c : TimeQuotaType.values()){
				if(value == c.value){
					result= c.toString();
					break;
				}
			}
			break;
		case 7:
			for (PositionMethodFailure_Diagnostic c : PositionMethodFailure_Diagnostic.values()){
				if(value == c.value){
					result= c.toString();
					break;
				}
			}
			break;
		case 8:
			for (UnauthorizedLCSClient_Diagnostic c : UnauthorizedLCSClient_Diagnostic.values()){
				if(value == c.value){
					result= c.toString();
					break;
				}
			}
			break;
		case 9:
			for (APNSelectionMode c : APNSelectionMode.values()){
				if(value == c.value){
					result= c.toString();
					break;
				}
			}
			break;
		case 10:
			for (ChChSelectionMode c : ChChSelectionMode.values()){
				if(value == c.value){
					result= c.toString();
					break;
				}
			}
			break;
		default:
			result = "Not exist enumeRated";
			break;
		}
	   
       return result;
   }
   
   
   public static void main(String []args){
	   System.out.println(EnumeRatedSgw.getEnumeRated(1, "Ltv_changeCondition"));
   }
}
