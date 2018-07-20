package cdrfile.global;

public class ConstSgwTag {
	
	public static final int LENGTH_FLAG = 128; // 80~128
	public static final int LENGTH_FLAG2 = 160; // 160~A0
	
	public static final int SGW_RECORD =	78;
	public static final int RECORDTYPE = 0;
	public static final int SERVEDIMSI = 3;
	public static final int S_GWADDRESS = 4;
	public static final int CHARGINGID = 5;
	public static final int SERVINGNODEADDRESS = 6;
	public static final int ACCESSPONITNAMENI = 7;
	public static final int PDPPDNTYPE = 8;
	public static final int SERVEDPDPPDNADDRESS = 9;
	public static final int DYNAMICADDRESSFLAG = 11;
	//public static final int LISTOFTRAFFICVOLUMES = 12;
	public static final int RECORDOPENINGTIME = 13;
	public static final int DURATION = 14;
	public static final int CAUSEFORRECCLOSING = 15;
	public static final int DIAGNOSTICS = 16;
	public static final int RECORDSEQUENCENUMBER = 17;
	public static final int NODEID = 18;
	public static final int LOCALSEQUENCENUMBER = 20;
	public static final int APNSELECTIONMODE = 21;
	public static final int SERVEDMSISDN = 22;
	public static final int CHARGINGCHARACTERISTICS = 23;
	public static final int CHCHSELECTIONMODE = 	24;
	public static final int SERVINGNODEPLMNIDENTIFIER	 = 27;
	public static final int SERVEDIMEISV = 29;
	public static final int RATTYPE = 30;
	public static final int MSTIMEZONE = 31;
	public static final int USERLOCATIONINFORMATION = 32;
	public static final int S_GWCHANGE = 34;
	public static final int SERVINGNODETYPE = 35;
	public static final int P_GWADDRESSUSED = 36;
	public static final int P_GWPLMNIDENTIFIER = 37;
	public static final int STARTTIME	 = 38;
	public static final int STOPTIME	 = 39;
	public static final int PDNCONNECTIONCHARGINGID = 40;
	public static final int SERVEDPDPPDNADDRESSEXT = 43;
	public static final int DYNAMICADDRESSFLAGEXT	 = 47;

	//not use
	public static final int LISTOFTRAFFICVOLUMES  = 12;
	public static final int IMSSIGNALINGCONTEXT 	 = 25;
	public static final int IMIUNAUTHENTICATEDFLAG = 41;
	public static final int USERCSGINFOMATION = 42;
	public static final int LOWPRIORITYINDICATOR  = 	44;
	public static final int RECORDEXTENSION  = 19;
	
	// List of traffic volumem
	public static final int LTV_QOSREQUESTED  = 1;
	public static final int LTV_QOSNEGOTIATED  = 2;
	public static final int LTV_DATAVOLUMEGPRSUPLINK  = 3;
	public static final int LTV_DATAVOLUMEGPRSDOWNLINK  = 4;
	public static final int LTV_CHANGECONDITION  = 5;
	public static final int LTV_CHANGETIME  = 6;
	public static final int LTV_USERLOCATIONINFORMATION  = 8;
	public static final int LTV_EPCQOSINFORMATION  = 9;
	// LTV_EPCQOSINFORMATION
	public static final int LTV_EPC_QCI = 1;
	public static final int LTV_EPC_MAXREQUESTEDBANDWITHUL = 2;
	public static final int LTV_EPC_MAXREQUESTEDBANDWITHDL = 3;
	public static final int LTV_EPC_GUARANTEEDBITRATEUL = 4;
	public static final int LTV_EPC_GUARANTEEDBITRATEDL = 5;
	public static final int LTV_EPC_ARP = 6;
	public static final int LTV_EPC_APNAGGREGATEMAXBITRATEUL = 7;
	public static final int LTV_EPC_APNAGGREGATEMAXBITRATEDL = 8;

}
