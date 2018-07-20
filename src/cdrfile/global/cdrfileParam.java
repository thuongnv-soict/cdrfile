package cdrfile.global;

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

public class cdrfileParam
{
    public static String MainProgram = "";
    public static int MainTimeDelay = 0;
    public static int ExceptionTimeDelay = 100;
    public static String OnErrorResumeNext;
    public static int TimeCheckupMissedFile = 0;
    public static long TimeCheckup = 0;
    public static boolean ChargeCDRFile = false;
    public static String AlreadyDateTimeSendMail = "";
    public static int TimeContentCheckup = 0;
    public static long AlreadyDateTimeSendSMS = 0;
    public static long AlreadyCheckNoFileToDownload = 0;
    public static long AlreadyCheckFreeDiskSpace = 0;
    public static String mSMSUrl = "http://10.50.9.91:8080/soap?wsdl";//"http://10.151.6.223:8080/services/SMSGWFunction?wsdl";
    public static String mSMSUserName = "loyalty";
    public static String mSMSPassword = "loyalty123";
    public static String mSMSSystem = "CDRBIN";
    public static boolean isReportedInToday = false;
    public static int dayCDRReport = 0;

}
