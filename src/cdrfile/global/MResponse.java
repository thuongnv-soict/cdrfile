package cdrfile.global;

import java.util.HashMap;
import java.util.Map;

public class MResponse {
	public static Map<String, String> mResponse(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("0","success");
		map.put("1","timeout");
		map.put("a","systemFailureError");
		map.put("b","dataMissingError");
		map.put("c","unexpectedDataValueError");
		map.put("d","facilityNotSupportedError");
		map.put("f","absentSubscriberError");
		map.put("12","unidentifiedSubscriberError");
		map.put("13","illegalSubscriberError");
		map.put("14","illegalEquipmentError");
		map.put("15","subscriberBusyForMtSmsError");
		map.put("51","invalidSmeAddressError");
		map.put("52","equipmentProtocolError");
		map.put("53","equipmentNotSmEquippedError");
		map.put("54","memoryCapacityExceededError");
		map.put("63","otherErrors");
		map.put("64","sccpAborted");
		map.put("69","noPagingError");
		map.put("6a","imsiDetachedError");
		map.put("6b","roamingRestrictionsError");
		map.put("6e","shortMsgType0NotSupportedError");
		map.put("6f","canNotReplaceShortMsgError");
		map.put("70","unspecifiedProtocolIdError");
		map.put("71","msgClassNotSupportedError");
		map.put("72","unspecifiedDataCodingSchemeError");
		map.put("73","tpduNotSupported");
		map.put("74","simStorageFullError");
		map.put("75","noSmStorageCapabilityInSimError");
		map.put("76","errorInMs");
		map.put("77","simApplToolKitbusyError");
		map.put("78","simDataDownloadError");
		map.put("79","applSpecificError");
		map.put("7a","equipUnspecifiedErrorCause");
		map.put("7b","ueDeregistered");
		map.put("7c","noResponseViaIpsmGw");
		map.put("7d","fallbackToMapVersion1Requested");
		map.put("7e","fallbackToMapVersion2Requested");
		map.put("7f","tcapAborted");
		return map;
	}
}
