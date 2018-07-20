package cdrfile.convert;

public class StructFCDR {

	public String OrigAddress_msisdn = "";
	public String OrigAddress_msisdnUTF8 = "";
	public String bantin = "";
	public String ogtiAddress_msisdn = "";
	public String ogtiAddress_msisdnUTF8 = "";
	public String orglRecipAddress_msisdn = "";
	public String orglRecipAddress_msisdnUTF8 = "";
	public String origIntlMobileSubId = "";
	public String recipientRoutingNumber = "";
	public String smsContentDcs = "";
	public String smsContents = "";
	public String smscPresentationAddress_msisdn = "";
	public String smscPresentationAddress_msisdnUTF8 = "";
	public String ss8LastFailureReason = "";
	public String status = "";
	public String intlMobileSubId = "";
	public Integer lengthOfMessage;
	public String submitDate = "";
	public String submitTime = "";
	public String dgtiAddress_msisdn = "";
	public String dgtiAddress_msisdnUTF8 = "";
	
	
	@Override
	public String toString(){
		return (submitDate ==null?"":submitDate.trim())
				+ "|"+(submitTime ==null?"":submitTime.trim())
				+ "|"+(bantin ==null?"":bantin.trim())
				
				+ "|"+(OrigAddress_msisdn ==null?"":OrigAddress_msisdn.trim())
				+ "|"+(OrigAddress_msisdnUTF8 ==null?"":OrigAddress_msisdnUTF8.trim())
				
				
				+ "|"+(orglRecipAddress_msisdn ==null?"":orglRecipAddress_msisdn.trim())
				+ "|"+(orglRecipAddress_msisdnUTF8 ==null?"":orglRecipAddress_msisdnUTF8.trim())
				
			
				+ "|"+(ogtiAddress_msisdn ==null?"":ogtiAddress_msisdn.trim())
				+ "|"+(ogtiAddress_msisdnUTF8 ==null?"":ogtiAddress_msisdnUTF8.trim())
				
				+ "|"+(origIntlMobileSubId ==null?"":origIntlMobileSubId.trim())
				+ "|"+(recipientRoutingNumber ==null?"":recipientRoutingNumber.trim())
				+ "|"+(smsContentDcs ==null?"":smsContentDcs.trim())
				
				+ "|"+(smscPresentationAddress_msisdn ==null?"":smscPresentationAddress_msisdn.trim())
				+ "|"+(smscPresentationAddress_msisdnUTF8 ==null?"":smscPresentationAddress_msisdnUTF8.trim())
				
				+ "|"+(intlMobileSubId ==null?"":intlMobileSubId.trim())
				+ "|"+(lengthOfMessage ==null?"":lengthOfMessage)

				+ "|"+(dgtiAddress_msisdn ==null?"":dgtiAddress_msisdn.trim())
				+ "|"+(dgtiAddress_msisdnUTF8 ==null?"":dgtiAddress_msisdnUTF8.trim())
				;
	}
				
}
