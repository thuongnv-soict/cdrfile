package cdrfile.convert;

public class StructSMSFW {
	public String rec_time = "";
	public String inbound = "";
	public String responseInfo_mtRoutingRule = "";
	public String routingAction = "";
	public String responseInfo_deliveryResult = "";
	public String rejectInfo_mtRoutingRule = "";
	public String mapSmsc_gsmAddress = "";
	public String correlatedSriSm_mapImsi_imsi = "";
	public String smsDeliver_smsOriginator_gsmAddress = "";
	public String sccpCgPa_sccpAddress = "";
	public String sccpCgPa_country = "";
	public String sccpCgPa_network = "";
	public String correlatedSriSm_mapSmsc_gsmAddress = "";
	public String correlatedSriSm_mapMsisdn_gsmAddress = "";
	public String correlatedSriSm_mapMsc_gsmAddress = "";
	public String smsDeliver_smsUserData = "";
	@Override
	public String toString(){
		return (rec_time ==null?"":rec_time.trim())
			+ "|"+(inbound ==null?"":inbound.trim())
			+ "|"+(routingAction ==null?"":routingAction.trim())
			+ "|"+(responseInfo_deliveryResult ==null?"":responseInfo_deliveryResult.trim())
			+ "|"+(responseInfo_mtRoutingRule ==null?"":responseInfo_mtRoutingRule.trim())
			+ "|"+(rejectInfo_mtRoutingRule ==null?"":rejectInfo_mtRoutingRule.trim())
			+ "|"+(mapSmsc_gsmAddress ==null?"":mapSmsc_gsmAddress.trim())
			+ "|"+(correlatedSriSm_mapImsi_imsi ==null?"":correlatedSriSm_mapImsi_imsi.trim())
			+ "|"+(smsDeliver_smsOriginator_gsmAddress ==null?"":smsDeliver_smsOriginator_gsmAddress.trim())
			+ "|"+(sccpCgPa_sccpAddress ==null?"":sccpCgPa_sccpAddress.trim())
			+ "|"+(sccpCgPa_country ==null?"":sccpCgPa_country.trim())
			+ "|"+(sccpCgPa_network ==null?"":sccpCgPa_network.trim())
			+ "|"+(correlatedSriSm_mapSmsc_gsmAddress ==null?"":correlatedSriSm_mapSmsc_gsmAddress.trim())
			+ "|"+(correlatedSriSm_mapMsisdn_gsmAddress ==null?"":correlatedSriSm_mapMsisdn_gsmAddress.trim())
			+ "|"+(correlatedSriSm_mapMsc_gsmAddress ==null?"":correlatedSriSm_mapMsc_gsmAddress.trim())
			+ "|"+(smsDeliver_smsUserData ==null?"":smsDeliver_smsUserData.trim());
	}


}
