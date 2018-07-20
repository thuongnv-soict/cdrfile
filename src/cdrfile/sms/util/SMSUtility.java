package cdrfile.sms.util;

import cdrfile.sms.service.SOAPRequestPortBindingStub;
import cdrfile.global.cdrfileParam;
import cdrfile.sms.service.SOAPRequestServiceLocator;
import cdrfile.sms.service.EntityActionSoapBindingStub;
import cdrfile.sms.service.EntityActionServiceLocator;

public class SMSUtility
{

    /*public static String sendSMSAlert(String isdn, String strContents) throws Exception
    {
        java.net.URL point = new java.net.URL(cdrfileParam.mSMSUrl);
        SOAPRequestPortBindingStub requestClient = new SOAPRequestPortBindingStub(point, new SOAPRequestServiceLocator());
        return requestClient.sendMessage(isdn, strContents, "0",cdrfileParam.mSMSSystem,  cdrfileParam.mSMSUserName, cdrfileParam.mSMSPassword);

    }*/

    public static String sendSMSAlert(String isdn, String strContents) throws Exception
    {
        java.net.URL point = new java.net.URL("http://10.3.11.136:9100/QueueSMS/services/EntityAction?wsdl");
        EntityActionSoapBindingStub requestClient = new EntityActionSoapBindingStub(point, new EntityActionServiceLocator());
        return requestClient.sendSMSAlert(isdn, "PAKH",strContents);

    }
    public static void main(String[] args){
        try{
            SMSUtility.sendSMSAlert("0937459889","Toi da test xong roi.");
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

}
