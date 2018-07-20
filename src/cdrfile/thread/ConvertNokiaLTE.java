package cdrfile.thread;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import cdrfile.convert.StructNokiaLTE;
import cdrfile.global.Global;
import cdrfile.global.IOUtils;
import cdrfile.global.TextFile;
import cdrfile.global.cdrfileParam;
import epg.cdr.sgw.ChangeOfCharCondition;
import epg.cdr.sgw.ConstSgwTag;
import epg.cdr.sgw.EnumeRatedSgw;
import epg.cdr.sgw.SgwModel;

public class ConvertNokiaLTE {


	public static void main(String[] args) throws Exception {
		new ConvertNokiaLTE().convertNokiaLTE("", "", 1, "", "", 1, 1);
	}
	
//	protected StructEricssonLTE structEricssonLTE = new StructEricssonLTE();
	
	protected String mTagModuleCode = "";
	protected int mTagModuleLength = 0;
	protected String mTagFieldCode = "";
	protected int mTagFieldLength = 0;
    protected String mStrValues = "";
    protected byte mBuffer[] = null;
    protected int mByteN = 0;
    protected int mFileByte = 0;
    protected int mRecOG = 0;
    protected int mRecIC = 0;
    protected int mRecSMO = 0;
    protected int mRecSMT = 0;
    protected int mRecAnnoun = 0;
    protected int mRecDivert = 0;
    protected int mRecTransit = 0;
    protected int mRecOther = 0;
    protected int mRecConvert = 0;
    protected int mRecData = 0;
    protected String mValue = "";
    protected int mRet = 0;
    protected int mRecLength = 0;
    protected int mLength = 0;
    protected String mSQL = null; 
    
    
    StringBuilder str = new StringBuilder();
    StringBuilder textRecord = new StringBuilder(); // ghi lai toan bo noi dung ban ghi duoi dang text
    StructNokiaLTE structNokiaLTE = new StructNokiaLTE();
	
	private int convertNokiaLTE(String pSourceConvert, String pFileName,
			int pFileID, String pDestinationConvert, String pCurrent_dir,
			int pLocalSplitFilebyDay, int pCenterID) throws Exception {
		
		/**
		 * Text file io tool
		 */
		TextFile fileConvert = new TextFile();

		/**
		 * Read source file
		 */
		RandomAccessFile fileCDR = null;

		/**
		 * IO tool
		 */
		IOUtils IOUtil = new IOUtils();

		/**
		 * source file's path
		 */
		String mSource = null;

		/**
		 * source file's length
		 */
		int mFileLength = 0;
		
		/**
		 * Number of record CDR
		 */
		int mRecS = 0;
		int mRecP = 0;
		
		/**
		 * Time
		 */
		String firstCallingTime = "";
        String lastCallingTime = "";
		
		try {
			//Global.ExecuteSQL(mConnection, "alter session set nls_date_format='dd/mm/yyyy hh24:mi:ss'");
			
			// get path
			//mSource = IOUtil.FillPath(pSourceConvert, Global.mSeparate) + pFileName;
			
			mSource = "C:\\Users\\VHCSOFT\\Desktop\\input\\pgwcdr_00000004_20170718020845.asn";
			// open file to read
			fileCDR = new RandomAccessFile(mSource, "r");
			
			// split by day
			/*if (pLocalSplitFilebyDay == 1) {
				mSource = IOUtil.FillPath(pDestinationConvert, Global.mSeparate) + pCurrent_dir;
				IOUtil.forceFolderExist(mSource); // maker folder
				mSource += Global.mSeparate + pFileName;
			} else {
				mSource = IOUtil.FillPath(pDestinationConvert, Global.mSeparate) + pFileName;
			}
			
			// delete exists file
			IOUtil.deleteFile(mSource);*/
			
			fileConvert.openFile("C:\\Users\\VHCSOFT\\Desktop\\output\\pgwcdr_00000004_20170718020845.txt", 5242880);
			
			

			//mConnection.setAutoCommit(false);
			
			//mSQL = "UPDATE import_header SET time_begin_convert=sysdate ";
			//mSQL += "WHERE file_id=" + pFileID;
			
		///	Global.ExecuteSQL(mConnection, mSQL);
			
			mFileLength = (int) fileCDR.length();
			
			// khai bao buffer
			mBuffer = new byte[mFileLength];
			
			mLength = fileCDR.read(mBuffer);
			
			mFileByte = 0;
			
			// Total length of the CDR file in octets (header + CDR payload)
			//int cdrDataLength = 0;
			mFileByte = 0;
			boolean isFirstCall = true; // kiem tra xem neu la lan dau tien thi add header
			while (mFileByte < mFileLength) { 
				mRecLength = 0;
				mByteN = 0;
				mTagModuleCode = "";
				mTagModuleLength = 0;
				
				// mTagModuleCode
				mTagModuleCode += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
				mByteN ++;
				
				if (mTagModuleCode.compareTo("bf") == 0 || mTagModuleCode.compareTo("9f") == 0) {
					mLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
					if (mLength > 128) {
						mByteN++;
						mRet = mLength - 128;
						for (int i = 1; i <= mRet; i++) {
							mTagModuleCode += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					} else {
						mTagModuleCode += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
						mByteN++;
					}
				}
				
				//mByteN = 2
				mRecLength += Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
				mByteN ++;
				
				// mret = 2
				if(mRecLength > 128){
					mRet = mRecLength - 128; 
					mRecLength = 0;
					for(int i=1; i<= mRet; i++){
						mRecLength += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (mRet - i));
						mByteN++;
					}
					mRecLength += mByteN;
				}else{
					mRecLength = mFileLength;
				}
				

				/*if (isFirstCall) {
					fileConvert.addText(Global.NokiaLTEHeader);
				}*/
				
				if (mTagModuleCode.compareTo("bf4e") == 0) {
					if (mRecS == 0){
						fileConvert.addText(Global.sgwNokiaLTEHeader);
					}
					
					mRecS ++;
					sGWRecord(mRecS);
					fileConvert.addText(textRecord.toString());
					
				} else if (mTagModuleCode.compareTo("bf4f") == 0){ 
					if (mRecP == 0){
						fileConvert.addText(Global.pgwNokiaLTEHeader);
					} 
					 
					mRecP++;
					convertPGWRecord(mRecP);
					fileConvert.addText(textRecord.toString()); 
				} else {
					mByteN = (mByteN - 1) + mRecLength;
					mFileByte += mByteN;
				} 
				
				// write to text file
				if (cdrfileParam.ChargeCDRFile) {
					fileConvert.addText(mStrValues);
					mStrValues = "";
				} else {}
			}
			
			mRecConvert = mRecS + mRecP;
			
	/*		writeLogFile("         - sGW               : " + Global.rpad(Integer.toString(mRecS), 6, " "));
			writeLogFile("         - pGW               : " + Global.rpad(Integer.toString(mRecP), 6, " "));
			
			writeLogFile("      -------------------------------");
			writeLogFile("      Total record converted : " + Global.rpad(Integer.toString(mRecConvert), 6, " "));
			
			if (cdrfileParam.ChargeCDRFile) {
				mSQL = "UPDATE import_header SET time_end_convert=sysdate,status=" + Global.StateConverted + ",rec_total=" + mRecConvert + ",min_calling_time='" + lastCallingTime + "',max_calling_time='" + firstCallingTime + "' WHERE file_id = " + pFileID;
			} else {
				mSQL = "UPDATE import_header SET time_end_convert=sysdate,status=" + Global.StateRated + ",rec_total=" + mRecConvert + ",min_calling_time='" + lastCallingTime + "',max_calling_time='" + firstCallingTime + "' WHERE file_id = " + pFileID;
			}*/
			
		//	Global.ExecuteSQL(mConnection, mSQL);
			
		//	mConnection.commit();
		} catch (FileNotFoundException e) {
			e.printStackTrace();

			return Global.ErrFileNotFound;
		} catch (Exception ex) {
			ex.printStackTrace();
			//mConnection.rollback();
			mSQL = "UPDATE import_header SET status="
					+ Global.StateConvertedError + ",note='"
					+ ex.toString() + " at rec:" + mRecConvert
					+ "' WHERE file_id = " + pFileID;
			//Global.ExecuteSQL(mConnection, mSQL); // update file error converted
			//mConnection.commit();

			if (cdrfileParam.OnErrorResumeNext.compareTo("TRUE") == 0) {
			/*	writeLogFile(" - " + ex.toString() + " - at record:"
						+ mRecConvert);*/
				return Global.ErrFileConverted;
			} else {
			//	System.out.println(mRecConvert + " " + ex.toString());
				throw ex;
			}
		} finally {
			try {
				mSource = null;
				mSQL = null;
				// mValue = null;
				mBuffer = new byte[0];
				IOUtil = null;
				fileConvert.closeFile();
				fileConvert = null;
				fileCDR.close();
				fileCDR = null;
			} catch (Exception ex) {}
		}

		return Global.OKFileConverted;
	}

	/**
	 * 
	 * @author H.anh 
	 */
	private void convertPGWRecord(int stt) {
		int tagLength = 0;
		long chargingId = 0;
		int duration = 0;
		long recordSequence = 0;
		long pdnConnection = 0;
		int localSequence = 0;
		int apnSelectionMode = 0;
		int ratingId = 0;
		int checkListData = 0;
		int currentPostion = mFileByte + mByteN;
		int resultCode = 0;
		int eventBasedNumberOfEvents =0;
		int timeQuotaType =0;
		int baseTimeInterval =0;
		currentPostion += 4;
		int count = 0;

		// SeveredImsi
		tagLength = toUnsignedByte(mBuffer[currentPostion]);
		tagLength = tagLength > 128 ? tagLength-128 : tagLength;
		for (int i = 0; i < tagLength; i++) {
			str.append(Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion])));
		}
		String severedIMSI = str.toString();
		if (severedIMSI.endsWith("f")) {
			severedIMSI = severedIMSI.substring(0, severedIMSI.length()-1);
		}
		structNokiaLTE.setSeveredIMSI(severedIMSI);
		str.setLength(0);

		// P-gw Address
		currentPostion += 4;
		tagLength = toUnsignedByte(mBuffer[currentPostion]);
		tagLength = tagLength > 128 ? tagLength-128 : tagLength;
		while (tagLength-- > 0) {
			str.append(toUnsignedByte(mBuffer[++currentPostion]) + ".");
		}
		String tmp = str.toString();
		structNokiaLTE.setPgwAddress(tmp.substring(0, tmp.length()-1));
		str.setLength(0);

		// charingId
		currentPostion += 2;
		tagLength = toUnsignedByte(mBuffer[currentPostion]);
		tagLength = tagLength > 128 ? tagLength-128 : tagLength;
		while (tagLength-- > 0) {
			chargingId = (long) (chargingId << 8) + toUnsignedByte(mBuffer[++currentPostion]);
		}
		structNokiaLTE.setCharingId(chargingId);
		
		// Serving Node Address
		currentPostion += 2;
		tagLength = toUnsignedByte(mBuffer[currentPostion]);
		tagLength = tagLength > 128 ? tagLength-128 : tagLength;
		if (tagLength == 6) {
			currentPostion += 2;
			tagLength = toUnsignedByte(mBuffer[currentPostion]);
			tagLength = tagLength > 128 ? tagLength-128 : tagLength;
			while (tagLength-- > 0) {
				str.append(toUnsignedByte(mBuffer[++currentPostion]) + ".");
			} 
			tmp = str.toString();
			structNokiaLTE.setServingNodeAddress(tmp.substring(0, tmp.length()-1));
			str.setLength(0);
		} else {
			// ip v6
			currentPostion += tagLength;
		}

		

		// Access Point Name Network Identifier

		if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.ACCESS_POINT_NAME_NETWORKID) {
			tagLength = toUnsignedByte(mBuffer[++currentPostion]);
			tagLength = tagLength > 128 ? tagLength-128 : tagLength;
			while (tagLength-- > 0) {
				str.append(toChar(mBuffer[++currentPostion]));
			}
			structNokiaLTE.setAccessPointNameID(str.toString());
			str.setLength(0);
		} else {
			currentPostion--;
		}

		// PDP/PDN Type
		if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.PDP_PDN_TYPE) {
			tagLength = toUnsignedByte(mBuffer[++currentPostion]);
			tagLength = tagLength > 128 ? tagLength-128 : tagLength;
			//String s="";
			while (tagLength-- > 0) {
				String s =Integer.toHexString(mBuffer[++currentPostion]);
				str.append(s);
			}
			structNokiaLTE.setPdpPDNType((str.toString()).substring(6));
			str.setLength(0);
		} else {
			currentPostion--;
		}

		// Severed PDP/PDN Address
		if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.SERVED_PDP_PDN_ADDRESS) {
			currentPostion += 5;
			tagLength = toUnsignedByte(mBuffer[currentPostion]);
			tagLength = tagLength > 128 ? tagLength-128 : tagLength;
			if (tagLength == 4) {
				while (tagLength-- > 0) {
					str.append(toUnsignedByte(mBuffer[++currentPostion]) + ".");
				}
				tmp = str.toString();
				structNokiaLTE.setSeveredPDPDNPAdress(tmp.substring(0, tmp.length()-1));
			} else {
				currentPostion += tagLength;
			}
			str.setLength(0);
		} else {
			currentPostion--;
		}

		// Dynamic Flag
		if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.DYNAMIC_ADDRESS_FLAG) {
			currentPostion += 2;
			structNokiaLTE.setDynamicAddressFlag(mBuffer[currentPostion] == 1 ? "true" : "false");
		} else {
			currentPostion--;
		}

		//Record Opening Time
		currentPostion += 2;
		tagLength = toUnsignedByte(mBuffer[currentPostion]);
		tagLength = tagLength > 128 ? tagLength-128 : tagLength;
		for (int i = 0; i < tagLength; i++) {
			if (i == 6) {
				str.append(toChar(mBuffer[++currentPostion]));
			} else {
				if(i==0){
					int a = toUnsignedByte(mBuffer[++currentPostion]);
					String b = Integer.toHexString(a);
					//toUnsignedByte(mBuffer[++currentPostion]);
					str.append("20");
					str.append(a < 10 ? "0" + b : b);
				}
				else{
				int a = toUnsignedByte(mBuffer[++currentPostion]);
				String b = Integer.toHexString(a);
				str.append(a < 10 ? "0" + b : b);
				}
			}
		}
		structNokiaLTE.setRecordOpeningTime(str.toString());
		str.setLength(0);

		// Duration
		currentPostion += 2;
		tagLength = mBuffer[currentPostion];
		tagLength = tagLength > 128 ? tagLength-128 : tagLength;
		while (tagLength-- > 0) {
			duration = (int) (duration << 8) + toUnsignedByte(mBuffer[++currentPostion]);
		}
		structNokiaLTE.setDuration(duration);

		// Cause for record Closing
		currentPostion += 3;
		structNokiaLTE.setCauseForRecordClosing(StructNokiaLTE.causeForRecordClosingMap.get((int) mBuffer[currentPostion]));
		
		// Diagnostics
		if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.DIAGNOSTICS) {
			tagLength = mBuffer[++currentPostion];
			
			//currentPostion += 4;
			
				if (Global.HexToText(toUnsignedByte(mBuffer[++currentPostion])).equals("80")){
					currentPostion+=2;
					int Diagnostics = 0;
					Diagnostics = (int) mBuffer[currentPostion];
					structNokiaLTE.setDiagnostics(StructNokiaLTE.diagnoticsMap.get(Diagnostics));
				}
				else{
					for (int i =1; i< tagLength; i++)
						toUnsignedByte(mBuffer[++currentPostion]);
				}
				
			
			
		} else {
			currentPostion--;
		}
		
		// Record Sequence Number
		if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.RECORD_SEQUENCE_NUMBER) {
			tagLength = toUnsignedByte(mBuffer[++currentPostion]);
			recordSequence = 0;
			while(tagLength-- > 0) {
				recordSequence = (long) (recordSequence << 8) + toUnsignedByte(mBuffer[++currentPostion]);
				
			}
			structNokiaLTE.setRecordSequenceNumber(recordSequence);
		}
		 else {
				currentPostion--;
			}
		
		// Node id
		if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.NODEID) {
			tagLength = mBuffer[++currentPostion];  
			while (tagLength-- > 0) {
				str.append(toChar(mBuffer[++currentPostion]));						
			}
			structNokiaLTE.setNodeId(str.toString());
			str.setLength(0);
		} else {
			currentPostion--;
		}
		
		//recordExtensions
		if (toUnsignedByte(mBuffer[++currentPostion]) ==0xB3) {
			tagLength = mBuffer[++currentPostion];  
			while (tagLength-- > 0) {
				str.append(toUnsignedByte(mBuffer[++currentPostion]));						
			}
			
			str.setLength(0);
		} else {
			currentPostion--;
		}
		
		// Local sequence number
		if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.LOCAL_SEQUENCE_NUMBER) {
			tagLength = toUnsignedByte(mBuffer[++currentPostion]);
			while (tagLength-- > 0) {
				localSequence = (int) (localSequence << 8) + toUnsignedByte(mBuffer[++currentPostion]);
			}
			System.out.println("localSequence = "+ localSequence);
			structNokiaLTE.setLocalSequenceNumber(localSequence);
		} else {
			currentPostion--;
		}
		
		// Apn selection mode
		if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.APN_SELECTION_MODE) {
			tagLength = toUnsignedByte(mBuffer[++currentPostion]);
			while (tagLength-- > 0) {
				apnSelectionMode = (int) (apnSelectionMode << 8) + toUnsignedByte(mBuffer[++currentPostion]);
			}
			structNokiaLTE.setApnSelectionMode(apnSelectionMode + "");
			
		} else {
			currentPostion--;
		}
		
		// Severed MSISDN
		if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.SERVED_MSISDN) {
			tagLength = toUnsignedByte(mBuffer[++currentPostion]);
			while (tagLength-- > 0) {
				str.append(Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion])));
			}
			structNokiaLTE.setServedMSISDN(str.toString());
			str.setLength(0);
		} else {
			currentPostion--;
		}
		
		// Charging Characteristics
		if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.CHARGING_CHARACTERISTICS) {
			tagLength = toUnsignedByte(mBuffer[++currentPostion]);
			while (tagLength-- > 0) {
				int a = toUnsignedByte(mBuffer[++currentPostion]);
				if (a <10)
				str.append("0"+(Integer.toHexString(a)));
				else str.append((Integer.toHexString(a)));
			}
			structNokiaLTE.setChargingCharacteristics(str.toString());
			str.setLength(0);
		} else {
			currentPostion--;
		}
		
		// Charging Characteristics Selection Mode
		if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.CHARGING_CHARACTERISTICS_SELECTION_MODE) {
			currentPostion += 2;
			structNokiaLTE.setChargingCharacteristicsSelectionMode(StructNokiaLTE.chargingCharacteristicsSMMap.get((int) mBuffer[currentPostion]));
		} else {
			currentPostion--;
		}
		//externalChargingID

		if ( toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.externalChargingID) {
			tagLength = toUnsignedByte(mBuffer[++currentPostion]);
			while(tagLength-- > 0) {
				str.append(Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion])));
			}
			structNokiaLTE.setexternalChargingID(str.toString());
			str.setLength(0);
		} else {
			currentPostion--;
		}
		// Serving Node PLMN Identifier
		if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.SERVING_NODE_PLMN_ID) {
			tagLength = toUnsignedByte(mBuffer[++currentPostion]);
			while(tagLength-- > 0) {
				str.append(Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion])));
			}
			structNokiaLTE.setServingNodePLMNId(str.toString());
			str.setLength(0);
			
		} else {
			currentPostion--;
		}
		
		//servedIMEISV
		if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.served_IMEISV) {
			tagLength = toUnsignedByte(mBuffer[++currentPostion]);
			while(tagLength-- > 0) {
				str.append(Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion])));
			}
			structNokiaLTE.setServedIMEISV(str.toString());
			str.setLength(0);
			
		} else {
			currentPostion--;
		}
		
		// Rat type
		if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.RATTYPE) {
			currentPostion += 2;
			int a = toUnsignedByte(mBuffer[currentPostion]);
			structNokiaLTE.setRATType(a);
			structNokiaLTE.setrATType(StructNokiaLTE.rATTypeMap.get(a));
		} else {
			currentPostion--;
		}
		
		// Ms time zone
		if (toUnsignedByte(mBuffer[++currentPostion])<< 8 + toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.MS_TIME_ZONE) {
			tagLength = toUnsignedByte(mBuffer[++currentPostion]);
			while(tagLength-- > 0) {
				int a = toUnsignedByte(mBuffer[++currentPostion]);
				if (a <10)
				str.append("0"+(Integer.toHexString(a)));
				else str.append((Integer.toHexString(a)));
			}
			structNokiaLTE.setMsTimeZone(str.toString());
			str.setLength(0);
		} else {
			currentPostion -= 2;
		}
		
		// user location information
		if (toUnsignedByte(mBuffer[++currentPostion])<< 8 + toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.USER_LOCATION_INFORMATION) {
			tagLength = toUnsignedByte(mBuffer[++currentPostion]);
			/*1while(tagLength-- > 0) {
				str.append(Global.HexToText(toUnsignedByte(mBuffer[++currentPostion])));
			}*/
			String s1 = Global.HexToText(toUnsignedByte(mBuffer[++currentPostion]));
			System.out.println (s1.substring(0,1));
			/*String s2 = "";
			if (s1.substring(1).equals("8")) s2 += " TAI ";
			else{
				if (s1.substring(1).equals("4")) s2 += (" RAI ");
				else {
					if (s1.substring(1).equals("2")) s2 += (" SAI ");
					else{
						if (s1.substring(1).equals("1")) s2 += (" CGI ");
					}
				}
			}*/
			if (s1.substring(0,1).equals("1")) {
				//str.append("ECGI ");
				for (int i = 1; i <= 2; i++)
				{
					Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion]));
				}
				
				Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion]));
				for (int i=5; i<= tagLength; i++){
					if (Global.HexToText(toUnsignedByte(mBuffer[++currentPostion])).equals("54"))
					{
						//str.append(s2);
						currentPostion--;
						for (int j = 1; j <= 2; j++)
						{
							str.append(Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion])));
						}
						str.append("-"+Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion]))+ "-");
						int a = 0;
						a += toUnsignedByte(mBuffer[++currentPostion])*65536;
						a += toUnsignedByte(mBuffer[++currentPostion])*256;
						a += toUnsignedByte(mBuffer[++currentPostion]);
						str.append(String.valueOf(a));
						str.append("-"+ String.valueOf(toUnsignedByte(mBuffer[++currentPostion])));
						break;
					}
					currentPostion--;
					toUnsignedByte(mBuffer[++currentPostion]);
				}
			}
			else {
				//str.append(s2);
				for (int i = 1; i <= 2; i++)
				{
					str.append(Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion])));
				}
				
				str.append("-"+Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion]))+"-");
				
				int a = 0;
				a += toUnsignedByte(mBuffer[++currentPostion])*256;
				a += toUnsignedByte(mBuffer[++currentPostion]);
				str.append(String.valueOf(a));
				
				int b = 0;
				b += toUnsignedByte(mBuffer[++currentPostion])*256;
				b += toUnsignedByte(mBuffer[++currentPostion]);
				str.append("-"+String.valueOf(b));
					
			}
			
			structNokiaLTE.setUserLocationInfor(str.toString());
			str.setLength(0);
			
		} else {
			currentPostion -= 2;
		}
		
		
		// list of service data
		if (toUnsignedByte(mBuffer[++currentPostion])<< 8 + toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.LIST_OF_SERVICE_DATA) {
			checkListData = currentPostion;
			System.out.println(checkListData);
			if (mBuffer[++currentPostion] < 0) {
				count = mBuffer[currentPostion] + 128;
				checkListData += count + 1;
				tagLength = 0;
				while (count-- > 0) {
					tagLength = (int) (tagLength << 8) + toUnsignedByte(mBuffer[++currentPostion]);
				}
				
			} else {
				tagLength = toUnsignedByte(mBuffer[currentPostion]);
				checkListData += 1;
			}
			System.out.println(tagLength);
			checkListData += tagLength;
			while (currentPostion < checkListData) {
				if (mBuffer[++currentPostion] == 0x30) {
					count = mBuffer[++currentPostion] < 0 ? mBuffer[currentPostion] + 128 : 1;
					currentPostion += count;
					
					// rating group
					if (toUnsignedByte(mBuffer[currentPostion]) == 0x81) {
						tagLength = mBuffer[++currentPostion] < 0 ? mBuffer[currentPostion] + 128 : mBuffer[currentPostion];
						ratingId = 0;
						while (tagLength-- > 0) {
							ratingId = (int) (ratingId << 8) + toUnsignedByte(mBuffer[++currentPostion]);
						}
						System.out.println(ratingId);
						structNokiaLTE.setRatingGroupId(ratingId);
					}
					 else {
							currentPostion--;
						}
					// charging Rule Base Name
					if (toUnsignedByte(mBuffer[++currentPostion]) == 0x82) {
						tagLength = toUnsignedByte(mBuffer[++currentPostion]);
						while (tagLength-- > 0) {
							str.append(toChar(mBuffer[++currentPostion]));
						}
						System.out.println("charging Rule Base Name = "+ str.toString() );
						structNokiaLTE.setChargingRuleBaseName(str.toString());
						str.setLength(0);
					} else {
						currentPostion--;
					}
					
					// result code
					if (toUnsignedByte(mBuffer[++currentPostion]) == 0x83) {
						tagLength = toUnsignedByte(mBuffer[++currentPostion]);
						while (tagLength-- > 0) {
							resultCode = (int) (resultCode << 8) + toUnsignedByte(mBuffer[++currentPostion]);
							structNokiaLTE.setResultCode(resultCode);
						}
					} else {
						currentPostion--;
					}
					
					// Local sequence number
					if (toUnsignedByte(mBuffer[++currentPostion]) == 0x84) {
						tagLength = toUnsignedByte(mBuffer[++currentPostion]);
						int localSequenceNumberList = 0;
						while (tagLength-- > 0) {
							localSequenceNumberList = (int) (localSequenceNumberList << 8) + toUnsignedByte(mBuffer[++currentPostion]);
						}
						structNokiaLTE.setLocalSequenceNumberListData(localSequenceNumberList);
					} else {
						currentPostion--;
					}
					
					// time of fist
					if (toUnsignedByte(mBuffer[++currentPostion]) == 0x85) {
						tagLength = toUnsignedByte(mBuffer[++currentPostion]);
						for (int i = 0; i < tagLength; i++) {
							if (i == 6) {
								str.append(toChar(mBuffer[++currentPostion]));
							} else {
								int a = toUnsignedByte(mBuffer[++currentPostion]);
								String b = Integer.toHexString(a);
								str.append(a < 10 ? "0" + b : b);
							}
						}
						structNokiaLTE.setTimeOfFirstUsage(str.toString());
						str.setLength(0);
					} else {
						currentPostion--;
					}
					
					// time of last
					if (toUnsignedByte(mBuffer[++currentPostion]) == 0x86) {
						tagLength = toUnsignedByte(mBuffer[++currentPostion]);
						for (int i = 0; i < tagLength; i++) {
							if (i == 6) {
								str.append(toChar(mBuffer[++currentPostion]));
							} else {
								int a = toUnsignedByte(mBuffer[++currentPostion]);
								String b = Integer.toHexString(a);
								str.append(a < 10 ? "0" + b : b);
							}
						}
						structNokiaLTE.setTimeOfLastUsage(str.toString());
						str.setLength(0);
					} else {
						currentPostion--;
					}
					
					// time usage
					if (toUnsignedByte(mBuffer[++currentPostion]) == 0x87) {
						int timeDuration = 0;
						tagLength = toUnsignedByte(mBuffer[++currentPostion]);
						while (tagLength-- > 0) {
							timeDuration = (int) (timeDuration << 8) + toUnsignedByte(mBuffer[++currentPostion]);
						}
						structNokiaLTE.setTimeUsage(timeDuration);
					} else {
						currentPostion--;
					}
					
					// Service Condition Change
					if (toUnsignedByte(mBuffer[++currentPostion]) == 0x88) {
						tagLength = mBuffer[++currentPostion];
						while (tagLength-- > 0) {
							str.append(mBuffer[++currentPostion] == 0 ? "00000000" : Integer.toBinaryString(toUnsignedByte(mBuffer[currentPostion])));
						}
						structNokiaLTE.setServiceConditionChange(str.toString());
						str.setLength(0);
					} else {
						currentPostion--;
					}
					
					// qoS Information Neg
					if (toUnsignedByte(mBuffer[++currentPostion]) == 0xA9) {
						tagLength = mBuffer[++currentPostion];
						currentPostion += tagLength;
					} else {
						currentPostion--;
					}
					
					// serving node address
					if (toUnsignedByte(mBuffer[++currentPostion]) == 0xAA) {
						currentPostion += 3;
						tagLength = mBuffer[currentPostion];
						if (tagLength == 4) {
							while (tagLength-- > 0) {
								str.append(toUnsignedByte(mBuffer[++currentPostion]) + ".");
							}
							tmp = str.toString();
							System.out.println("tmp = " + tmp);
							structNokiaLTE.setServingNodeAddressListData(tmp.substring(0,tmp.length()-1));
							str.setLength(0);
						} else {
							currentPostion += tagLength;
						}
					} else {
						currentPostion--;
					}
					
					//SGSN PLMN Identifier---null
					
					
					// mBuffer volume FBC Up link
					if (toUnsignedByte(mBuffer[++currentPostion]) == 0x8C) {
						tagLength = mBuffer[++currentPostion];
						int mBufferUp = 0;
						while (tagLength -- > 0) {
							mBufferUp = (mBufferUp << 8) + toUnsignedByte(mBuffer[++currentPostion]);
						}
						structNokiaLTE.setDatavolumeFBCUplink(mBufferUp);
						
					} else {
						currentPostion--;
					}
					
					// mBuffer volume FBC Down link
					if (toUnsignedByte(mBuffer[++currentPostion]) == 0x8D) {
						tagLength = mBuffer[++currentPostion];
						int mBufferDown = 0;
						while (tagLength -- > 0) {
							mBufferDown = (mBufferDown << 8) + toUnsignedByte(mBuffer[++currentPostion]);
						}
						structNokiaLTE.setDatavolumeFBCUplink(mBufferDown);
						
					} else {
						currentPostion--;
					}
					
					// time of report
					if (toUnsignedByte(mBuffer[++currentPostion]) == 0x8E) {
						tagLength = mBuffer[++currentPostion];
						for (int i = 0; i < tagLength; i++) {
							if (i == 6) {
								str.append(toChar(mBuffer[++currentPostion]));
							} else {
									int a = toUnsignedByte(mBuffer[++currentPostion]);
									String b = Integer.toHexString(a);
								str.append(a < 10 ? "0" + b : b);
							}
						}
						structNokiaLTE.setTimeOfReport(str.toString());
						str.setLength(0);
						
					} else {
						currentPostion--;
					}
					//failureHandlingContinue
					
					if (toUnsignedByte(mBuffer[++currentPostion]) == 0x90) {
						tagLength = mBuffer[++currentPostion];
						for (int i = 0; i < tagLength; i++) {
							
								str.append(toChar(mBuffer[++currentPostion]));
							
						}
						structNokiaLTE.setFailureHandlingContinue(str.toString());
						str.setLength(0);
						
					} else {
						currentPostion--;
					}
					
					// service Identifier
					if (toUnsignedByte(mBuffer[++currentPostion]) == 0x91) {
						tagLength = mBuffer[++currentPostion];
						int serviceId = 0;
						while (tagLength-- > 0) {
							serviceId = (serviceId << 8) + toUnsignedByte(mBuffer[++currentPostion]);
						}
						structNokiaLTE.setServiceIdentifier(serviceId);
						
					} else {
						currentPostion--;
					}

					//pSFurnishChargingInformation
					if (toUnsignedByte(mBuffer[++currentPostion]) == 0xB2) {
						count = mBuffer[++currentPostion] < 0 ? mBuffer[currentPostion] + 128 : 1;
						currentPostion += count;
						if (toUnsignedByte(mBuffer[currentPostion]) == 0x81) {
							tagLength = toUnsignedByte(mBuffer[++currentPostion]);
							while (tagLength-- > 0) {
								str.append(toUnsignedByte(mBuffer[++currentPostion]));
							}
							str.append("_");
							structNokiaLTE.setpSFurnishChargingInforListData(str.toString());
							str.setLength(0);
						}
						else {
							currentPostion--;
						}
						if (toUnsignedByte(mBuffer[++currentPostion]) == 0x82) {
							tagLength = toUnsignedByte(mBuffer[++currentPostion]);
							while (tagLength-- > 0) {
								str.append(toUnsignedByte(mBuffer[++currentPostion]));
							}
							structNokiaLTE.setpSFurnishChargingInfor(str.toString());
							str.setLength(0);
						} else {
							currentPostion--;
						}
					}else {
						currentPostion--;
					}
				
					
					//aFRecordInformation
					if (toUnsignedByte(mBuffer[++currentPostion]) == 0xB3) {
						count = mBuffer[++currentPostion] < 0 ? mBuffer[currentPostion] + 128 : 1;
						currentPostion += count;
						if (toUnsignedByte(mBuffer[currentPostion]) == 0x81) {
							tagLength = toUnsignedByte(mBuffer[++currentPostion]);
							while (tagLength-- > 0) {
								str.append(toUnsignedByte(mBuffer[++currentPostion]));
							}
							structNokiaLTE.setaFRecordInformation(str.toString());
							str.setLength(0);
						}
						else {
							currentPostion--;
						}
						if (toUnsignedByte(mBuffer[++currentPostion]) == 0xA2) {
							tagLength = toUnsignedByte(mBuffer[++currentPostion]);
							while (tagLength-- > 0) {
								str.append(toUnsignedByte(mBuffer[++currentPostion]));
							}
							
							str.setLength(0);
						} else {
							currentPostion--;
						}
					}
					else {
						currentPostion--;
					}
					// user Location Information
					if (toUnsignedByte(mBuffer[++currentPostion]) == 0x94) {
						tagLength = mBuffer[++currentPostion];
						while (tagLength-- > 0) {
							str.append(Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion])));
						}
						structNokiaLTE.setUserLocationInfor(str.toString());
						str.setLength(0);
						
					} else {
						currentPostion--;
					}
					//eventBasedChargingInformation

					if (toUnsignedByte(mBuffer[++currentPostion]) == 0xB5) {
						count = mBuffer[++currentPostion] < 0 ? mBuffer[currentPostion] + 128 : 1;
						currentPostion += count;
						if (toUnsignedByte(mBuffer[currentPostion]) == 0x81) {
							tagLength = toUnsignedByte(mBuffer[++currentPostion]);
							eventBasedNumberOfEvents =0;
							while (tagLength-- > 0) {
								eventBasedNumberOfEvents = (eventBasedNumberOfEvents << 8) + toUnsignedByte(mBuffer[++currentPostion]);
							}
							structNokiaLTE.seteventBasedNumberOfEvents(eventBasedNumberOfEvents);
							
						}
						else {
							currentPostion--;
						}
						if (toUnsignedByte(mBuffer[++currentPostion]) == 0x82) {
							tagLength = toUnsignedByte(mBuffer[++currentPostion]);
							str.append("_");
							while (tagLength-- > 0) {
								str.append(toUnsignedByte(mBuffer[++currentPostion]));
							}
							structNokiaLTE.seteventBasedEventTimeStamps(str.toString());
							str.setLength(0);
						} else {
							currentPostion--;
						}
					}
					else {
						currentPostion--;
					}
					
					//timeQuotaMechanism

					if (toUnsignedByte(mBuffer[++currentPostion]) == 0xB6) {
						count = mBuffer[++currentPostion] < 0 ? mBuffer[currentPostion] + 128 : 1;
						currentPostion += count;
						if (toUnsignedByte(mBuffer[currentPostion]) == 0x81) {
							tagLength = toUnsignedByte(mBuffer[++currentPostion]);
							timeQuotaType =0;
							String StimeQuotaType ="";
							while (tagLength-- > 0) {
								timeQuotaType = (timeQuotaType << 8) + toUnsignedByte(mBuffer[++currentPostion]);
							}
							if (timeQuotaType==0) StimeQuotaType = "dISCRETETIMEPERIOD_";
							else StimeQuotaType = "cONTINUOUSTIMEPERIOD_";
							structNokiaLTE.settimeQuotaType(StimeQuotaType);
							
						}
						else {
							currentPostion--;
						}
						if (toUnsignedByte(mBuffer[++currentPostion]) == 0x82) {
							tagLength = toUnsignedByte(mBuffer[++currentPostion]);
							baseTimeInterval =0;
							while (tagLength-- > 0) {
								baseTimeInterval = (baseTimeInterval << 8) + toUnsignedByte(mBuffer[++currentPostion]);
							}
							structNokiaLTE.setbaseTimeInterval(baseTimeInterval);
							
						} else {
							currentPostion--;
						}
					}else {
						currentPostion--;
					}
					
					
					
				} else currentPostion = checkListData;
				
			} // end list data record
			

		} else {
			currentPostion -= 2;
		}
		// servingNodeType

				if (toUnsignedByte(mBuffer[++currentPostion])<< 8 + toUnsignedByte(mBuffer[++currentPostion]) ==0xBF << 8 + 0x23 ) {
					tagLength = toUnsignedByte(mBuffer[++currentPostion]);
					while(tagLength-- > 0) {
						str.append(Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion])));
					}
					
					str.setLength(0);
				} else {
					currentPostion -= 2;
				}
		// p-GWPLMNIdentifier

		if (toUnsignedByte(mBuffer[++currentPostion])<< 8 + toUnsignedByte(mBuffer[++currentPostion]) ==0x9F << 8 + 0x25 ) {
			tagLength = toUnsignedByte(mBuffer[++currentPostion]);
			while(tagLength-- > 0) {
				str.append(Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion])));
			}
					
			str.setLength(0);
		} else {
					currentPostion -= 2;
				}
		
		if (toUnsignedByte(mBuffer[++currentPostion])<< 8 + toUnsignedByte(mBuffer[++currentPostion]) ==0x9F << 8 + 0x26 ) {
			tagLength = toUnsignedByte(mBuffer[++currentPostion]);
			while(tagLength-- > 0) {
				str.append(Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion])));
			}
					
			str.setLength(0);
		} else {
					currentPostion -= 2;
				}
		
		if (toUnsignedByte(mBuffer[++currentPostion])<< 8 + toUnsignedByte(mBuffer[++currentPostion]) ==0x9F << 8 + 0x27 ) {
			tagLength = toUnsignedByte(mBuffer[++currentPostion]);
			while(tagLength-- > 0) {
				str.append(Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion])));
			}
					
			str.setLength(0);
		} else {
					currentPostion -= 2;
				}
		
		// pDNConnectionChargingID

				if (toUnsignedByte(mBuffer[++currentPostion])<< 8 + toUnsignedByte(mBuffer[++currentPostion]) ==0x9F << 8 + 0x29 ) {
					tagLength = toUnsignedByte(mBuffer[++currentPostion]);
					while(tagLength-- > 0) {
						str.append(Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion])));
					}
							
					str.setLength(0);
				} else {
							currentPostion -= 2;
						}
		mFileByte += mRecLength;

		/**
		 * 
		 * add text to file
		 */
	
		textRecord.setLength(0);
		textRecord.append(stt + "|");
		textRecord.append("85|");
		textRecord.append((structNokiaLTE.getSeveredIMSI()+"|"));
		textRecord.append((structNokiaLTE.getPgwAddress()+"|"));
		textRecord.append((structNokiaLTE.getChargingId()+"|"));
		textRecord.append((structNokiaLTE.getServingNodeAddress()+"|"));
		textRecord.append((structNokiaLTE.getAccessPointNameID()+"|"));
		textRecord.append((structNokiaLTE.getPdpPDNType()+"|"));
		textRecord.append((structNokiaLTE.getSeveredPDPPDNAdress()+"|"));
		textRecord.append((structNokiaLTE.isDynamicAddressFlag()+"|"));
		textRecord.append((structNokiaLTE.getRecordOpeningTime()+"|"));
		textRecord.append((structNokiaLTE.getDuration()+"|"));
		textRecord.append((structNokiaLTE.getCauseForRecordClosing()+"|"));
		textRecord.append((structNokiaLTE.getDiagnostics()+"|"));
		textRecord.append((structNokiaLTE.getRecordSequenceNumber()+"|"));
		textRecord.append((structNokiaLTE.getNodeId()+"|"));
		textRecord.append((structNokiaLTE.getLocalSequenceNumber()+"|"));
		textRecord.append((structNokiaLTE.getApnSelectionMode()+"|"));
		textRecord.append((structNokiaLTE.getServedMSISDN()+"|"));
		textRecord.append((structNokiaLTE.getChargingCharacteristics()+"|"));
		textRecord.append((structNokiaLTE.getChargingCharacteristicsSelectionMode()+"|"));
		textRecord.append((structNokiaLTE.getServingNodePLMNId()+"|"));
		
		textRecord.append((structNokiaLTE.getServedIMEISV()+"|"));
		textRecord.append((structNokiaLTE.getrATType()+"|"));
		textRecord.append((structNokiaLTE.getMsTimeZone()+"|"));
		
		textRecord.append((structNokiaLTE.getUserLocationInfor()+"|"));
		textRecord.append((structNokiaLTE.getRatingGroupId()+"|"));
		textRecord.append((structNokiaLTE.getChargingRuleBaseName()+"|"));
		textRecord.append((structNokiaLTE.getResultCode()+"|"));
		textRecord.append((structNokiaLTE.getLocalSequenceNumberListData()+"|"));
		textRecord.append((structNokiaLTE.getTimeOfFirstUsage()+"|"));
		textRecord.append((structNokiaLTE.getTimeOfLastUsage()+"|"));
		textRecord.append((structNokiaLTE.getTimeUsage()+"|"));
		textRecord.append((structNokiaLTE.getServiceConditionChange()+"|"));
		textRecord.append((structNokiaLTE.getQoSInformationNeg()+"|"));
		textRecord.append((structNokiaLTE.getServingNodeAddressListData()+"|"));
		textRecord.append((structNokiaLTE.getSGSNPLMNIdentifier()+"|"));
		textRecord.append((structNokiaLTE.getDatavolumeFBCUplink()+"|"));
		textRecord.append((structNokiaLTE.getDatavolumeFBCDownlink()+"|"));
		textRecord.append((structNokiaLTE.getTimeOfReport()+"|"));
		textRecord.append((structNokiaLTE.getRATType()+"|"));
		textRecord.append((structNokiaLTE.getFailureHandlingContinue()+"|"));
		if ((structNokiaLTE.getServiceIdentifier()) != 0)
			textRecord.append((structNokiaLTE.getServiceIdentifier()+"|"));
		textRecord.append((structNokiaLTE.getUserLocationInforListData()+"|"));
		textRecord.append(((structNokiaLTE.getpSFurnishChargingInforListData())+ (structNokiaLTE.getpSFurnishChargingInfor())+"|"));
		
		textRecord.append((structNokiaLTE.getaFRecordInformation()+"|"));
		textRecord.append(((structNokiaLTE.geteventBasedNumberOfEvents()) + (structNokiaLTE.geteventBasedEventTimeStamps())+"|"));
		
		if ((structNokiaLTE.gettimeQuotaType().equals("dISCRETETIMEPERIOD-"))|| (structNokiaLTE.gettimeQuotaType().equals("cONTINUOUSTIMEPERIOD-"))) {
			textRecord.append((structNokiaLTE.gettimeQuotaType() + "-"));
			textRecord.append(structNokiaLTE.getbaseTimeInterval()+"|");
		}
		else textRecord.append((structNokiaLTE.gettimeQuotaType() + "|"));
			
		
		textRecord.append(structNokiaLTE.getNetworkInitiatedPDPContext()+"|");
		textRecord.append(structNokiaLTE.getiMSSignalingContext()+"|");
		textRecord.append(structNokiaLTE.getexternalChargingID()+"|");
		textRecord.append(((structNokiaLTE.getpSFurnishChargingInforListData())+ (structNokiaLTE.getpSFurnishChargingInfor())+"|"));
		textRecord.append(structNokiaLTE.getCAMELInformation());
		
	}

	/*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*/		
	/*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*/		
	/*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*/		
	/*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*/		
	/*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*/		
	/*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*/		
	/*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*/		
	/*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*/		
	/*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*/		
	/*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*/		
	/*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*/		
	/*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*/		
	/*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*/		
	/*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*/		
	/*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*/		
	/*@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@*/
	
	
	private void sGWRecord(int stt) {
		
			int tagLength = 0;
			long chargingId = 0;
			int duration = 0;
			long recordSequence = 0;
			long pdnConnection = 0;
			int localSequence = 0;
			int apnSelectionMode = 0;
			int ratingId = 0;
			int checkListData = 0;
			int currentPostion = mFileByte + mByteN;
			int resultCode = 0;
			int eventBasedNumberOfEvents =0;
			int timeQuotaType =0;
			int baseTimeInterval =0;
			currentPostion += 4;
			int count = 0;

			// SeveredImsi83
			tagLength = toUnsignedByte(mBuffer[currentPostion]);
			tagLength = tagLength > 128 ? tagLength-128 : tagLength;
			for (int i = 0; i < tagLength; i++) {
				str.append(Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion])));
			}
			String severedIMSI = str.toString();
			if (severedIMSI.endsWith("f")) {
				severedIMSI = severedIMSI.substring(0, severedIMSI.length()-1);
			}
			structNokiaLTE.setSeveredIMSI(severedIMSI);
			str.setLength(0);

			// P-gw AddressA4
			currentPostion += 4;
			tagLength = toUnsignedByte(mBuffer[currentPostion]);
			tagLength = tagLength > 128 ? tagLength-128 : tagLength;
			while (tagLength-- > 0) {
				str.append(toUnsignedByte(mBuffer[++currentPostion]) + ".");
			}
			String tmp = str.toString();
			structNokiaLTE.setsGWAddress(tmp.substring(0, tmp.length()-1));
			str.setLength(0);

			// charingId85
			currentPostion += 2;
			tagLength = toUnsignedByte(mBuffer[currentPostion]);
			tagLength = tagLength > 128 ? tagLength-128 : tagLength;
			while (tagLength-- > 0) {
				chargingId = (long) (chargingId << 8) + toUnsignedByte(mBuffer[++currentPostion]);
			}
			structNokiaLTE.setCharingId(chargingId);
			
			// Serving Node Address A6
			currentPostion += 2;
			tagLength = toUnsignedByte(mBuffer[currentPostion]);
			tagLength = tagLength > 128 ? tagLength-128 : tagLength;
			if (tagLength == 6) {
				currentPostion += 2;
				tagLength = toUnsignedByte(mBuffer[currentPostion]);
				tagLength = tagLength > 128 ? tagLength-128 : tagLength;
				while (tagLength-- > 0) {
					str.append(toUnsignedByte(mBuffer[++currentPostion]) + ".");
				} 
				tmp = str.toString();
				//structNokiaLTE.setServingNodeAddress(tmp.substring(0, tmp.length()-1));
				str.setLength(0);
			} else {
				// ip v6
				currentPostion += tagLength;
			}

			

			// Access Point Name Network Identifier 87

			if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.ACCESS_POINT_NAME_NETWORKID) {
				tagLength = toUnsignedByte(mBuffer[++currentPostion]);
				tagLength = tagLength > 128 ? tagLength-128 : tagLength;
				while (tagLength-- > 0) {
					str.append(toChar(mBuffer[++currentPostion]));
				}
				structNokiaLTE.setAccessPointNameID(str.toString());
				str.setLength(0);
			} else {
				currentPostion--;
			}

			// PDP/PDN Type 88 
			if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.PDP_PDN_TYPE) {
				tagLength = toUnsignedByte(mBuffer[++currentPostion]);
				tagLength = tagLength > 128 ? tagLength-128 : tagLength;
				//String s="";
				while (tagLength-- > 0) {
					String s =Integer.toHexString(mBuffer[++currentPostion]);
					str.append(s);
				}
				structNokiaLTE.setPdpPDNType((str.toString()).substring(6));
				str.setLength(0);
			} else {
				currentPostion--;
			}

			// Severed PDP/PDN Address A9
			if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.SERVED_PDP_PDN_ADDRESS) {
				currentPostion += 5;
				tagLength = toUnsignedByte(mBuffer[currentPostion]);
				tagLength = tagLength > 128 ? tagLength-128 : tagLength;
				if (tagLength == 4) {
					while (tagLength-- > 0) {
						str.append(toUnsignedByte(mBuffer[++currentPostion]) + ".");
					}
					tmp = str.toString();
					structNokiaLTE.setSeveredPDPDNPAdress(tmp.substring(0, tmp.length()-1));
				} else {
					currentPostion += tagLength;
				}
				str.setLength(0);
			} else {
				currentPostion--;
			}

			// Dynamic Flag 8b
			if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.DYNAMIC_ADDRESS_FLAG) {
				currentPostion += 2;
				structNokiaLTE.setDynamicAddressFlag(mBuffer[currentPostion] == 1 ? "true" : "false");
			} else {
				currentPostion--;
			}

			//ac
			
			if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.ac) {
				tagLength = toUnsignedByte(mBuffer[++currentPostion]);
				while(tagLength-- > 0) {
					str.append(Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion])));
				}
						
				str.setLength(0);
			} else {
						currentPostion --;
					}
			
			//Record Opening Time 8D
			if (toUnsignedByte(mBuffer[++currentPostion]) == 0x8D){
			currentPostion ++;
			tagLength = toUnsignedByte(mBuffer[currentPostion]);
			tagLength = tagLength > 128 ? tagLength-128 : tagLength;
			for (int i = 0; i < tagLength; i++) {
				if (i == 6) {
					str.append(toChar(mBuffer[++currentPostion]));
				} else {
					if(i==0){
						int a = toUnsignedByte(mBuffer[++currentPostion]);
						String b = Integer.toHexString(a);
						//toUnsignedByte(mBuffer[++currentPostion]);
						str.append("20");
						str.append(a < 10 ? "0" + b : b);
					}
					else{
					int a = toUnsignedByte(mBuffer[++currentPostion]);
					String b = Integer.toHexString(a);
					str.append(a < 10 ? "0" + b : b);
					}
				}
			}
			structNokiaLTE.setRecordOpeningTime(str.toString());
			str.setLength(0);
			}
			else{
				currentPostion--;
			}

			// Duration 8E
			if (toUnsignedByte(mBuffer[++currentPostion]) == 0x8E){
			currentPostion ++;
			tagLength = mBuffer[currentPostion];
			tagLength = tagLength > 128 ? tagLength-128 : tagLength;
			while (tagLength-- > 0) {
				duration = (int) (duration << 8) + toUnsignedByte(mBuffer[++currentPostion]);
			}
			structNokiaLTE.setDuration(duration);
			}
			else {
				currentPostion--;
			}
			// Cause for record Closing 8F
			if (toUnsignedByte(mBuffer[++currentPostion]) == 0x8F){
			currentPostion += 2;
			structNokiaLTE.setCauseForRecordClosing(StructNokiaLTE.causeForRecordClosingMap.get((int) mBuffer[currentPostion]));
			}
			else{
				currentPostion--;
			}
			// Diagnostics B0
			if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.DIAGNOSTICS) {
				tagLength = mBuffer[++currentPostion];
				
				//currentPostion += 4;
				
					if (Global.HexToText(toUnsignedByte(mBuffer[++currentPostion])).equals("80")){
						currentPostion+=2;
						int Diagnostics = 0;
						Diagnostics = (int) mBuffer[currentPostion];
						structNokiaLTE.setDiagnostics(StructNokiaLTE.diagnoticsMap.get(Diagnostics));
					}
					else{
						for (int i =1; i< tagLength; i++)
							toUnsignedByte(mBuffer[++currentPostion]);
					}
					
				
				
			} else {
				currentPostion--;
			}
			
			// Record Sequence Number 91
			if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.RECORD_SEQUENCE_NUMBER) {
				tagLength = toUnsignedByte(mBuffer[++currentPostion]);
				recordSequence = 0;
				while(tagLength-- > 0) {
					recordSequence = (long) (recordSequence << 8) + toUnsignedByte(mBuffer[++currentPostion]);
					
				}
				structNokiaLTE.setRecordSequenceNumber(recordSequence);
			}
			 else {
					currentPostion--;
				}
			
			// Node id 92
			if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.NODEID) {
				tagLength = mBuffer[++currentPostion]; 
				System.out.println("dem: "+tagLength);
				while (tagLength-- > 0) {
					str.append(toChar(mBuffer[++currentPostion]));						
				}
				System.out.println("dem: "+str.toString());
				structNokiaLTE.setNodeId(str.toString());
				str.setLength(0);
			} else {
				currentPostion--;
			}
			
			//recordExtensions B3
			if (toUnsignedByte(mBuffer[++currentPostion]) ==0xB3) {
				tagLength = mBuffer[++currentPostion];  
				while (tagLength-- > 0) {
					str.append(toUnsignedByte(mBuffer[++currentPostion]));						
				}
				
				str.setLength(0);
			} else {
				currentPostion--;
			}
			
			// Local sequence number 94
			if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.LOCAL_SEQUENCE_NUMBER) {
				tagLength = toUnsignedByte(mBuffer[++currentPostion]);
				while (tagLength-- > 0) {
					localSequence = (int) (localSequence << 8) + toUnsignedByte(mBuffer[++currentPostion]);
				}
				System.out.println("localSequence = "+ localSequence);
				structNokiaLTE.setLocalSequenceNumber(localSequence);
			} else {
				currentPostion--;
			}
			
			// Apn selection mode 95
			if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.APN_SELECTION_MODE) {
				tagLength = toUnsignedByte(mBuffer[++currentPostion]);
				while (tagLength-- > 0) {
					apnSelectionMode = (int) (apnSelectionMode << 8) + toUnsignedByte(mBuffer[++currentPostion]);
				}
				structNokiaLTE.setApnSelectionMode(apnSelectionMode + "");
				
			} else {
				currentPostion--;
			}
			
			// Severed MSISDN 96
			if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.SERVED_MSISDN) {
				tagLength = toUnsignedByte(mBuffer[++currentPostion]);
				while (tagLength-- > 0) {
					str.append(Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion])));
				}
				structNokiaLTE.setServedMSISDN(str.toString());
				str.setLength(0);
			} else {
				currentPostion--;
			}
			
			// Charging Characteristics 97
			if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.CHARGING_CHARACTERISTICS) {
				tagLength = toUnsignedByte(mBuffer[++currentPostion]);
				while (tagLength-- > 0) {
					int a = toUnsignedByte(mBuffer[++currentPostion]);
					if (a <10)
					str.append("0"+(Integer.toHexString(a)));
					else str.append((Integer.toHexString(a)));
				}
				structNokiaLTE.setChargingCharacteristics(str.toString());
				str.setLength(0);
			} else {
				currentPostion--;
			}
			
			// Charging Characteristics Selection Mode 98
			if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.CHARGING_CHARACTERISTICS_SELECTION_MODE) {
				currentPostion += 2;
				structNokiaLTE.setChargingCharacteristicsSelectionMode(StructNokiaLTE.chargingCharacteristicsSMMap.get((int) mBuffer[currentPostion]));
			} else {
				currentPostion--;
			}
			//externalChargingID 9A

			if ( toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.externalChargingID) {
				tagLength = toUnsignedByte(mBuffer[++currentPostion]);
				while(tagLength-- > 0) {
					str.append(Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion])));
				}
				//structNokiaLTE.setexternalChargingID(str.toString());
				str.setLength(0);
			} else {
				currentPostion--;
			}
			// Serving Node PLMN Identifier 9B
			if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.SERVING_NODE_PLMN_ID) {
				tagLength = toUnsignedByte(mBuffer[++currentPostion]);
				while(tagLength-- > 0) {
					str.append(Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion])));
				}
				//structNokiaLTE.setServingNodePLMNId(str.toString());
				str.setLength(0);
				
			} else {
				currentPostion--;
			}
			
			//servedIMEISV 9D
			if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.served_IMEISV) {
				tagLength = toUnsignedByte(mBuffer[++currentPostion]);
				while(tagLength-- > 0) {
					str.append(Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion])));
				}
				structNokiaLTE.setServedIMEISV(str.toString());
				str.setLength(0);
				
			} else {
				currentPostion--;
			}
			
			// Rat type 9E
			if (toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.RATTYPE) {
				currentPostion += 2;
				int a = toUnsignedByte(mBuffer[currentPostion]);
				
				structNokiaLTE.setrATType(StructNokiaLTE.rATTypeMap.get(a));
			} else {
				currentPostion--;
			}
			
			// Ms time zone 9F1F
			if (toUnsignedByte(mBuffer[++currentPostion])<< 8 + toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.MS_TIME_ZONE) {
				tagLength = toUnsignedByte(mBuffer[++currentPostion]);
				while(tagLength-- > 0) {
					int a = toUnsignedByte(mBuffer[++currentPostion]);
					if (a <10)
					str.append("0"+(Integer.toHexString(a)));
					else str.append((Integer.toHexString(a)));
				}
				//structNokiaLTE.setMsTimeZone(str.toString());
				str.setLength(0);
			} else {
				currentPostion -= 2;
			}
			
			// user location information 9F20
			if (toUnsignedByte(mBuffer[++currentPostion])<< 8 + toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.USER_LOCATION_INFORMATION) {
				tagLength = toUnsignedByte(mBuffer[++currentPostion]);
				/*1while(tagLength-- > 0) {
					str.append(Global.HexToText(toUnsignedByte(mBuffer[++currentPostion])));
				}*/
				String s1 = Global.HexToText(toUnsignedByte(mBuffer[++currentPostion]));
				System.out.println (s1.substring(0,1));
				/*String s2 = "";
				if (s1.substring(1).equals("8")) s2 += " TAI ";
				else{
					if (s1.substring(1).equals("4")) s2 += (" RAI ");
					else {
						if (s1.substring(1).equals("2")) s2 += (" SAI ");
						else{
							if (s1.substring(1).equals("1")) s2 += (" CGI ");
						}
					}
				}*/
				if (s1.substring(0,1).equals("1")) {
					//str.append("ECGI ");
					for (int i = 1; i <= 2; i++)
					{
						Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion]));
					}
					
					Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion]));
					for (int i=5; i<= tagLength; i++){
						if (Global.HexToText(toUnsignedByte(mBuffer[++currentPostion])).equals("54"))
						{
							//str.append(s2);
							currentPostion--;
							for (int j = 1; j <= 2; j++)
							{
								str.append(Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion])));
							}
							str.append("-"+Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion]))+ "-");
							int a = 0;
							a += toUnsignedByte(mBuffer[++currentPostion])*65536;
							a += toUnsignedByte(mBuffer[++currentPostion])*256;
							a += toUnsignedByte(mBuffer[++currentPostion]);
							str.append(String.valueOf(a));
							str.append("-"+ String.valueOf(toUnsignedByte(mBuffer[++currentPostion])));
							break;
						}
						currentPostion--;
						toUnsignedByte(mBuffer[++currentPostion]);
					}
				}
				else {
					//str.append(s2);
					for (int i = 1; i <= 2; i++)
					{
						str.append(Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion])));
					}
					
					str.append("-"+Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion]))+"-");
					
					int a = 0;
					a += toUnsignedByte(mBuffer[++currentPostion])*256;
					a += toUnsignedByte(mBuffer[++currentPostion]);
					str.append(String.valueOf(a));
					
					int b = 0;
					b += toUnsignedByte(mBuffer[++currentPostion])*256;
					b += toUnsignedByte(mBuffer[++currentPostion]);
					str.append("-"+String.valueOf(b));
						
				}
				
				structNokiaLTE.setUserLocationInfor(str.toString());
				str.setLength(0);
				
			} else {
				currentPostion -= 2;
			}
			// sGWChange
			
			if (toUnsignedByte(mBuffer[++currentPostion])<< 8 + toUnsignedByte(mBuffer[++currentPostion]) ==0x9F << 8 + 0x22 ) {
				currentPostion += 2;
				boolean sGWChange;
				if (mBuffer[currentPostion] == 1) sGWChange = true;
				else sGWChange = false;
				structNokiaLTE.setsGWChange(sGWChange);
			} else {
						currentPostion -= 2;
					}
			
			// list of service data
			if (toUnsignedByte(mBuffer[++currentPostion])<< 8 + toUnsignedByte(mBuffer[++currentPostion]) == PGWNokiaTagConstanct.LIST_OF_SERVICE_DATA) {
				checkListData = currentPostion;
				System.out.println(checkListData);
				if (mBuffer[++currentPostion] < 0) {
					count = mBuffer[currentPostion] + 128;
					checkListData += count + 1;
					tagLength = 0;
					while (count-- > 0) {
						tagLength = (int) (tagLength << 8) + toUnsignedByte(mBuffer[++currentPostion]);
					}
					
				} else {
					tagLength = toUnsignedByte(mBuffer[currentPostion]);
					checkListData += 1;
				}
				System.out.println(tagLength);
				checkListData += tagLength;
				while (currentPostion < checkListData) {
					if (mBuffer[++currentPostion] == 0x30) {
						count = mBuffer[++currentPostion] < 0 ? mBuffer[currentPostion] + 128 : 1;
						currentPostion += count;
						
						// rating group
						if (toUnsignedByte(mBuffer[currentPostion]) == 0x81) {
							tagLength = mBuffer[++currentPostion] < 0 ? mBuffer[currentPostion] + 128 : mBuffer[currentPostion];
							ratingId = 0;
							while (tagLength-- > 0) {
								ratingId = (int) (ratingId << 8) + toUnsignedByte(mBuffer[++currentPostion]);
							}
							System.out.println(ratingId);
							//structNokiaLTE.setRatingGroupId(ratingId);
						}
						 else {
								currentPostion--;
							}
						// charging Rule Base Name
						if (toUnsignedByte(mBuffer[++currentPostion]) == 0x82) {
							tagLength = toUnsignedByte(mBuffer[++currentPostion]);
							while (tagLength-- > 0) {
								str.append(toChar(mBuffer[++currentPostion]));
							}
							System.out.println("charging Rule Base Name = "+ str.toString() );
							//structNokiaLTE.setChargingRuleBaseName(str.toString());
							str.setLength(0);
						} else {
							currentPostion--;
						}
						
						// result code
						if (toUnsignedByte(mBuffer[++currentPostion]) == 0x83) {
							tagLength = toUnsignedByte(mBuffer[++currentPostion]);
							while (tagLength-- > 0) {
								resultCode = (int) (resultCode << 8) + toUnsignedByte(mBuffer[++currentPostion]);
								//structNokiaLTE.setResultCode(resultCode);
							}
						} else {
							currentPostion--;
						}
						
						// Local sequence number
						if (toUnsignedByte(mBuffer[++currentPostion]) == 0x84) {
							tagLength = toUnsignedByte(mBuffer[++currentPostion]);
							int localSequenceNumberList = 0;
							while (tagLength-- > 0) {
								localSequenceNumberList = (int) (localSequenceNumberList << 8) + toUnsignedByte(mBuffer[++currentPostion]);
							}
							//structNokiaLTE.setLocalSequenceNumberListData(localSequenceNumberList);
						} else {
							currentPostion--;
						}
						
						// time of fist
						if (toUnsignedByte(mBuffer[++currentPostion]) == 0x85) {
							tagLength = toUnsignedByte(mBuffer[++currentPostion]);
							for (int i = 0; i < tagLength; i++) {
								if (i == 6) {
									str.append(toChar(mBuffer[++currentPostion]));
								} else {
									int a = toUnsignedByte(mBuffer[++currentPostion]);
									String b = Integer.toHexString(a);
									str.append(a < 10 ? "0" + b : b);
								}
							}
							//structNokiaLTE.setTimeOfFirstUsage(str.toString());
							str.setLength(0);
						} else {
							currentPostion--;
						}
						
						// time of last
						if (toUnsignedByte(mBuffer[++currentPostion]) == 0x86) {
							tagLength = toUnsignedByte(mBuffer[++currentPostion]);
							for (int i = 0; i < tagLength; i++) {
								if (i == 6) {
									str.append(toChar(mBuffer[++currentPostion]));
								} else {
									int a = toUnsignedByte(mBuffer[++currentPostion]);
									String b = Integer.toHexString(a);
									str.append(a < 10 ? "0" + b : b);
								}
							}
							//structNokiaLTE.setTimeOfLastUsage(str.toString());
							str.setLength(0);
						} else {
							currentPostion--;
						}
						
						// time usage
						if (toUnsignedByte(mBuffer[++currentPostion]) == 0x87) {
							int timeDuration = 0;
							tagLength = toUnsignedByte(mBuffer[++currentPostion]);
							while (tagLength-- > 0) {
								timeDuration = (int) (timeDuration << 8) + toUnsignedByte(mBuffer[++currentPostion]);
							}
							//structNokiaLTE.setTimeUsage(timeDuration);
						} else {
							currentPostion--;
						}
						
						// Service Condition Change
						if (toUnsignedByte(mBuffer[++currentPostion]) == 0x88) {
							tagLength = mBuffer[++currentPostion];
							while (tagLength-- > 0) {
								str.append(mBuffer[++currentPostion] == 0 ? "00000000" : Integer.toBinaryString(toUnsignedByte(mBuffer[currentPostion])));
							}
							//structNokiaLTE.setServiceConditionChange(str.toString());
							str.setLength(0);
						} else {
							currentPostion--;
						}
						
						// qoS Information Neg
						if (toUnsignedByte(mBuffer[++currentPostion]) == 0xA9) {
							tagLength = mBuffer[++currentPostion];
							currentPostion += tagLength;
						} else {
							currentPostion--;
						}
						
						// serving node address
						if (toUnsignedByte(mBuffer[++currentPostion]) == 0xAA) {
							currentPostion += 3;
							tagLength = mBuffer[currentPostion];
							if (tagLength == 4) {
								while (tagLength-- > 0) {
									str.append(toUnsignedByte(mBuffer[++currentPostion]) + ".");
								}
								tmp = str.toString();
								System.out.println("tmp = " + tmp);
								//structNokiaLTE.setServingNodeAddressListData(tmp.substring(0,tmp.length()-1));
								str.setLength(0);
							} else {
								currentPostion += tagLength;
							}
						} else {
							currentPostion--;
						}
						
						//SGSN PLMN Identifier---null
						
						
						// mBuffer volume FBC Up link
						if (toUnsignedByte(mBuffer[++currentPostion]) == 0x8C) {
							tagLength = mBuffer[++currentPostion];
							int mBufferUp = 0;
							while (tagLength -- > 0) {
								mBufferUp = (mBufferUp << 8) + toUnsignedByte(mBuffer[++currentPostion]);
							}
							structNokiaLTE.setDatavolumeFBCUplink(mBufferUp);
							
						} else {
							currentPostion--;
						}
						
						// mBuffer volume FBC Down link
						if (toUnsignedByte(mBuffer[++currentPostion]) == 0x8D) {
							tagLength = mBuffer[++currentPostion];
							int mBufferDown = 0;
							while (tagLength -- > 0) {
								mBufferDown = (mBufferDown << 8) + toUnsignedByte(mBuffer[++currentPostion]);
							}
							structNokiaLTE.setDatavolumeFBCUplink(mBufferDown);
							
						} else {
							currentPostion--;
						}
						
						// time of report
						if (toUnsignedByte(mBuffer[++currentPostion]) == 0x8E) {
							tagLength = mBuffer[++currentPostion];
							for (int i = 0; i < tagLength; i++) {
								if (i == 6) {
									str.append(toChar(mBuffer[++currentPostion]));
								} else {
										int a = toUnsignedByte(mBuffer[++currentPostion]);
										String b = Integer.toHexString(a);
									str.append(a < 10 ? "0" + b : b);
								}
							}
							//structNokiaLTE.setTimeOfReport(str.toString());
							str.setLength(0);
							
						} else {
							currentPostion--;
						}
						//failureHandlingContinue
						
						if (toUnsignedByte(mBuffer[++currentPostion]) == 0x90) {
							tagLength = mBuffer[++currentPostion];
							for (int i = 0; i < tagLength; i++) {
								
									str.append(toChar(mBuffer[++currentPostion]));
								
							}
							//structNokiaLTE.setFailureHandlingContinue(str.toString());
							str.setLength(0);
							
						} else {
							currentPostion--;
						}
						
						// service Identifier
						if (toUnsignedByte(mBuffer[++currentPostion]) == 0x91) {
							tagLength = mBuffer[++currentPostion];
							int serviceId = 0;
							while (tagLength-- > 0) {
								serviceId = (serviceId << 8) + toUnsignedByte(mBuffer[++currentPostion]);
							}
							//structNokiaLTE.setServiceIdentifier(serviceId);
							
						} else {
							currentPostion--;
						}

						//pSFurnishChargingInformation
						if (toUnsignedByte(mBuffer[++currentPostion]) == 0xB2) {
							count = mBuffer[++currentPostion] < 0 ? mBuffer[currentPostion] + 128 : 1;
							currentPostion += count;
							if (toUnsignedByte(mBuffer[currentPostion]) == 0x81) {
								tagLength = toUnsignedByte(mBuffer[++currentPostion]);
								while (tagLength-- > 0) {
									str.append(toUnsignedByte(mBuffer[++currentPostion]));
								}
								str.append("_");
								//structNokiaLTE.setpSFurnishChargingInforListData(str.toString());
								str.setLength(0);
							}
							else {
								currentPostion--;
							}
							if (toUnsignedByte(mBuffer[++currentPostion]) == 0x82) {
								tagLength = toUnsignedByte(mBuffer[++currentPostion]);
								while (tagLength-- > 0) {
									str.append(toUnsignedByte(mBuffer[++currentPostion]));
								}
								//structNokiaLTE.setpSFurnishChargingInfor(str.toString());
								str.setLength(0);
							} else {
								currentPostion--;
							}
						}else {
							currentPostion--;
						}
					
						
						//aFRecordInformation
						if (toUnsignedByte(mBuffer[++currentPostion]) == 0xB3) {
							count = mBuffer[++currentPostion] < 0 ? mBuffer[currentPostion] + 128 : 1;
							currentPostion += count;
							if (toUnsignedByte(mBuffer[currentPostion]) == 0x81) {
								tagLength = toUnsignedByte(mBuffer[++currentPostion]);
								while (tagLength-- > 0) {
									str.append(toUnsignedByte(mBuffer[++currentPostion]));
								}
								//structNokiaLTE.setaFRecordInformation(str.toString());
								str.setLength(0);
							}
							else {
								currentPostion--;
							}
							if (toUnsignedByte(mBuffer[++currentPostion]) == 0xA2) {
								tagLength = toUnsignedByte(mBuffer[++currentPostion]);
								while (tagLength-- > 0) {
									str.append(toUnsignedByte(mBuffer[++currentPostion]));
								}
								
								str.setLength(0);
							} else {
								currentPostion--;
							}
						}
						else {
							currentPostion--;
						}
						// user Location Information
						if (toUnsignedByte(mBuffer[++currentPostion]) == 0x94) {
							tagLength = mBuffer[++currentPostion];
							while (tagLength-- > 0) {
								str.append(Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion])));
							}
							//structNokiaLTE.setUserLocationInfor(str.toString());
							str.setLength(0);
							
						} else {
							currentPostion--;
						}
						//eventBasedChargingInformation

						if (toUnsignedByte(mBuffer[++currentPostion]) == 0xB5) {
							count = mBuffer[++currentPostion] < 0 ? mBuffer[currentPostion] + 128 : 1;
							currentPostion += count;
							if (toUnsignedByte(mBuffer[currentPostion]) == 0x81) {
								tagLength = toUnsignedByte(mBuffer[++currentPostion]);
								eventBasedNumberOfEvents =0;
								while (tagLength-- > 0) {
									eventBasedNumberOfEvents = (eventBasedNumberOfEvents << 8) + toUnsignedByte(mBuffer[++currentPostion]);
								}
								//structNokiaLTE.seteventBasedNumberOfEvents(eventBasedNumberOfEvents);
								
							}
							else {
								currentPostion--;
							}
							if (toUnsignedByte(mBuffer[++currentPostion]) == 0x82) {
								tagLength = toUnsignedByte(mBuffer[++currentPostion]);
								str.append("_");
								while (tagLength-- > 0) {
									str.append(toUnsignedByte(mBuffer[++currentPostion]));
								}
								//structNokiaLTE.seteventBasedEventTimeStamps(str.toString());
								str.setLength(0);
							} else {
								currentPostion--;
							}
						}
						else {
							currentPostion--;
						}
						
						//timeQuotaMechanism

						if (toUnsignedByte(mBuffer[++currentPostion]) == 0xB6) {
							count = mBuffer[++currentPostion] < 0 ? mBuffer[currentPostion] + 128 : 1;
							currentPostion += count;
							if (toUnsignedByte(mBuffer[currentPostion]) == 0x81) {
								tagLength = toUnsignedByte(mBuffer[++currentPostion]);
								timeQuotaType =0;
								String StimeQuotaType ="";
								while (tagLength-- > 0) {
									timeQuotaType = (timeQuotaType << 8) + toUnsignedByte(mBuffer[++currentPostion]);
								}
								if (timeQuotaType==0) StimeQuotaType = "dISCRETETIMEPERIOD_";
								else StimeQuotaType = "cONTINUOUSTIMEPERIOD_";
								//structNokiaLTE.settimeQuotaType(StimeQuotaType);
								
							}
							else {
								currentPostion--;
							}
							if (toUnsignedByte(mBuffer[++currentPostion]) == 0x82) {
								tagLength = toUnsignedByte(mBuffer[++currentPostion]);
								baseTimeInterval =0;
								while (tagLength-- > 0) {
									baseTimeInterval = (baseTimeInterval << 8) + toUnsignedByte(mBuffer[++currentPostion]);
								}
								//structNokiaLTE.setbaseTimeInterval(baseTimeInterval);
								
							} else {
								currentPostion--;
							}
						}else {
							currentPostion--;
						}
						
						
						
					} else currentPostion = checkListData;
					
				} // end list data record
				

			} else {
				currentPostion -= 2;
			}
			// servingNodeType

					if (toUnsignedByte(mBuffer[++currentPostion])<< 8 + toUnsignedByte(mBuffer[++currentPostion]) ==0xBF << 8 + 0x23 ) {
						tagLength = toUnsignedByte(mBuffer[++currentPostion]);
						int tag2 = tagLength;
						for(int i = 1; i<= tagLength; i++) {
							str.append(Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion])));
						}
						System.out.println("bf23 =" + tag2);
						str.setLength(0);
					} else {
						currentPostion -= 2;
					}
				// p-GWAddressUsed
					
					if (toUnsignedByte(mBuffer[++currentPostion])<< 8 + toUnsignedByte(mBuffer[++currentPostion]) ==0xBF << 8 + 0x24 ) {
						tagLength = toUnsignedByte(mBuffer[++currentPostion]);
						tagLength = 4;
						currentPostion += 2;
						System.out.println("tag =" + tagLength);
						while (tagLength-- > 0) {
							str.append(toUnsignedByte(mBuffer[++currentPostion]) + ".");
						}
						String tmp2 = str.toString();
						System.out.println("tmp =" + tmp2);
						structNokiaLTE.setpGWAddressUsed(tmp2.substring(0, tmp2.length()-1));
						str.setLength(0);
					} else {
								currentPostion -= 2;
							}
					
			// p-GWPLMNIdentifier

			if (toUnsignedByte(mBuffer[++currentPostion])<< 8 + toUnsignedByte(mBuffer[++currentPostion]) ==0x9F << 8 + 0x25 ) {
				tagLength = toUnsignedByte(mBuffer[++currentPostion]);
				while(tagLength-- > 0) {
					str.append(Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion])));
				}
						
				str.setLength(0);
			} else {
						currentPostion -= 2;
					}
			
			if (toUnsignedByte(mBuffer[++currentPostion])<< 8 + toUnsignedByte(mBuffer[++currentPostion]) ==0x9F << 8 + 0x26 ) {
				tagLength = toUnsignedByte(mBuffer[++currentPostion]);
				while(tagLength-- > 0) {
					str.append(Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion])));
				}
						
				str.setLength(0);
			} else {
						currentPostion -= 2;
					}
			
			if (toUnsignedByte(mBuffer[++currentPostion])<< 8 + toUnsignedByte(mBuffer[++currentPostion]) ==0x9F << 8 + 0x27 ) {
				tagLength = toUnsignedByte(mBuffer[++currentPostion]);
				while(tagLength-- > 0) {
					str.append(Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion])));
				}
						
				str.setLength(0);
			} else {
						currentPostion -= 2;
					}
			
			if (toUnsignedByte(mBuffer[++currentPostion])<< 8 + toUnsignedByte(mBuffer[++currentPostion]) ==0x9F << 8 + 0x28 ) {
				tagLength = toUnsignedByte(mBuffer[++currentPostion]);
				while(tagLength-- > 0) {
					str.append(Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion])));
				}
						
				str.setLength(0);
			} else {
						currentPostion -= 2;
					}
			
			// pDNConnectionChargingID

				/*	if (toUnsignedByte(mBuffer[++currentPostion])<< 8 + toUnsignedByte(mBuffer[++currentPostion]) ==0x9F << 8 + 0x29 ) {
						tagLength = toUnsignedByte(mBuffer[++currentPostion]);
						while(tagLength-- > 0) {
							str.append(Global.HexToTBCD(toUnsignedByte(mBuffer[++currentPostion])));
						}
								
						str.setLength(0);
					} else {
								currentPostion -= 2;
							}*/
			mFileByte += mRecLength;

			/**
			 * 
			 * add text to file
			 */
		
		textRecord.setLength(0);
		textRecord.append(stt + "|");
		textRecord.append("84|");
		textRecord.append((structNokiaLTE.getnetworkInitiation()+"|"));
		textRecord.append((structNokiaLTE.getSeveredIMSI()+"|"));
		textRecord.append((structNokiaLTE.getServedIMEISV()+"|"));
		textRecord.append((structNokiaLTE.getsGWAddress()+"|"));
		textRecord.append((structNokiaLTE.getmsNetworkCapability()+"|"));
		textRecord.append((structNokiaLTE.getUserLocationInfor()+"|"));
		textRecord.append((structNokiaLTE.getChargingId()+"|"));
		textRecord.append((structNokiaLTE.getpGWAddressUsed()+"|"));
		textRecord.append((structNokiaLTE.getAccessPointNameID()+"|"));
		textRecord.append((structNokiaLTE.getPdpPDNType()+"|"));
		textRecord.append((structNokiaLTE.getSeveredPDPPDNAdress()+"|"));
		textRecord.append((structNokiaLTE.getDatavolumeFBCUplink()+"|"));
		textRecord.append((structNokiaLTE.getDatavolumeFBCDownlink()+"|"));
		textRecord.append((structNokiaLTE.getRecordOpeningTime()+"|"));
		textRecord.append((structNokiaLTE.getDuration()+"|"));
		textRecord.append((structNokiaLTE.issGWChange()+"|"));
		textRecord.append((structNokiaLTE.getCauseForRecordClosing()+"|"));
		textRecord.append((structNokiaLTE.getDiagnostics()+"|"));
		textRecord.append((structNokiaLTE.getRecordSequenceNumber()+"|"));
		textRecord.append((structNokiaLTE.getNodeId()+"|"));
		textRecord.append((structNokiaLTE.getrecordExtensions()+"|"));
		textRecord.append((structNokiaLTE.getLocalSequenceNumber()+"|"));
		textRecord.append((structNokiaLTE.getApnSelectionMode()+"|"));
		textRecord.append((structNokiaLTE.getaccessPointNameOI()+"|"));
		textRecord.append((structNokiaLTE.getServedMSISDN()+"|"));
		textRecord.append((structNokiaLTE.getChargingCharacteristics()+"|"));
		textRecord.append((structNokiaLTE.getrATType()+"|"));
		textRecord.append(structNokiaLTE.getCAMELInformation()+"|");
		textRecord.append(structNokiaLTE.getrNCUnsentDownlinkVolume()+"|");
		textRecord.append((structNokiaLTE.getChargingCharacteristicsSelectionMode()+"|"));
		textRecord.append((structNokiaLTE.isDynamicAddressFlag()+"|"));
		textRecord.append((structNokiaLTE.getnumberOfSeq()+"|"));
		
		
		
	}
	
	
	public Map<String , Object> analyseMinContent(String type, String name, int length){
		StringBuffer sb = new StringBuffer();
		Map<String , Object> map = new HashMap<String , Object>();
		int typeInt=0;
		if(type.equals("int")){
			typeInt=1;
		}else if(type.equals("long")){
			typeInt=2;
		}else if(type.equals("char")){
			typeInt=3;
		}else if(type.equals("strBCD")){
			typeInt=4;
		}else if(type.equals("strTBCD")){
			typeInt=5;
		}else if(type.equals("strIp4")){
			typeInt=6;
		}else if(type.equals("strIp6")){
			typeInt=7;
		}else if(type.equals("time")){
			typeInt=8;
		}else if(type.equals("plmnId")){
			typeInt=9;
		}
		
		
		switch (typeInt) {
		case 1:
			while (length > 0) {
				sb.append(convertDecToHex( Global.fixSignedByte(mBuffer[mFileByte + mByteN])));
				mByteN++;
				length--;
			}
			int intResult = convertIntDec(sb.toString());
			System.out.println("----name " + name +" content "+intResult+" -----");
			
			
			map.put("int", intResult);
			break;
		case 2:
			while (length > 0) {
				sb.append(convertDecToHex( Global.fixSignedByte(mBuffer[mFileByte + mByteN])));
				mByteN++;
				length--;
			}
			long longResult = convertLongDec(sb.toString());
			System.out.println("----name " + name +" content "+longResult+" -----");
			
			
			map.put("long", longResult);
			break;
		case 3:
			while (length > 0) {
				sb.append((char) Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
				mByteN++;
				length--;
			}
			String charResult = sb.toString();
			System.out.println("----name " + name +" content "+charResult+" -----");
			
			
			map.put("char", charResult);
			break;
		case 4:
			while (length > 0) {
				sb.append(Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN])));
				mByteN++;
				length--;
			}
			String strResult = sb.toString();
			System.out.println("----name " + name +" content "+strResult+" -----");
			
			
			map.put("strBCD", strResult);
			break;
		case 5:
			while (length > 0) {
				sb.append(Global.HexToTBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN])));
				mByteN++;
				length--;
			}
			strResult = sb.toString();
			System.out.println("----name " + name +" content "+strResult+" -----");
			
			
			map.put("strTBCD", strResult);
			break;
		case 6:
			while (length > 0) {
				sb.append(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
				mByteN++;
				sb.append(".");
				length--;
			}
			strResult = sb.toString().substring(0,sb.length()-1);
			System.out.println("----name " + name +" content "+strResult+" -----");
			
			
			map.put("strIp4", strResult);
			break;
		case 7:
			while (length > 0) {
				if(length%2==0){
					sb.append(":");
				}
				sb.append(Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN])));
				mByteN++;
				length--;
			}
			strResult = sb.toString().substring(1, sb.length());
			System.out.println("----name " + name +" content "+strResult+" -----");
			
			
			map.put("strIp6", strResult);
			break;
		case 8:
			int i =0;
			while(length>0){
				length--;
				i++;
				if(i<3){
					sb.append(Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN])));
					mByteN++;
					sb.append("-");
				}else if(i==3){
					sb.append(Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN])));
					mByteN++;
					sb.append(" ");
				}else if(i<6){
					sb.append(Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN])));
					mByteN++;
					sb.append(".");
				}else if(i==6){
					sb.append(Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN])));
					mByteN++;
					sb.append(" ");
				}else if(i==7 && Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN])).equals("2b")){
					mByteN++;
					sb.append("+");
				}else{
					sb.append(Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN])));
					mByteN++;
				}
			}
			strResult = sb.toString();
			System.out.println("----name " + name +" content "+strResult+" -----");
			
			
			map.put("time", strResult);
			break;
		case 9:
			i =0;
			String str = null;
			while(length>0){
				length--;
				i++;
				if(i%2==1){
					sb.append(Global.HexToTBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN])));
					mByteN++;
					if(str!=null){
						sb.append(str.charAt(0));
					}
				}else if(i%2==0){
					str = Global.HexToTBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
					mByteN++;
					sb.append(str.charAt(0));
				}else {
					//System.err.println("---ERR---"+System.lineSeparator());
				}
			}
			strResult = sb.toString();
			System.out.println("----name " + name +" content "+strResult+" -----");
			map.put("plmnId", strResult);
			break;
		default:
			//System.err.println("---ERR---"+System.lineSeparator());
			break;
		}
		return map;
	}
	
	
	
	public  String analysIpAddress( int ipType, String name) throws IOException{
		String ipAddress="";
		if(ipType==0){//ipv4
			System.out.println("IPV4");
			ipAddress =(String) analyseMinContent( "strIp4", name, Global.fixSignedByte(mBuffer[mFileByte + mByteN])).get("strIp4");
			mByteN++;
		}else{//ipv6
			System.out.println("IPV6");
			ipAddress =(String) analyseMinContent( "strIp6", name, Global.fixSignedByte(mBuffer[mFileByte + mByteN])).get("strIp6");
			mByteN++;
		}
		return ipAddress;
	}
	
	public String analysPdpIpAddress(int pdpType, String name) throws IOException{ 
		String ipAddress="";
		if(pdpType==0){//ipAddress
			System.out.println("ipAddress");
			int ipType = Global.fixSignedByte(mBuffer[mFileByte + mByteN])-128;
			mByteN++;
			ipAddress = analysIpAddress(ipType, name);
		}else{//etsi address NOT USE
			
		}
		return ipAddress;
	}


	public Integer convertIntDec(String inputHex) {
		Integer outputDecimal = Integer.parseInt(inputHex, 16);
		return outputDecimal;
	}

	public Short convertShortDec(String inputHex) {
		Integer outputDecimal = Integer.parseInt(inputHex, 16);
		return Short.valueOf(String.valueOf(outputDecimal));
	}

	public long convertLongDec(String inputHex) {
		return Long.parseLong(inputHex, 16);
	}

	public String convertDecToHex(int asc) {
		String str;
		if (asc < 10) {
			str = String.format("%02d", Integer.parseInt(Integer.toHexString(asc)));
		} else {
			str = Integer.toHexString(asc);
		}
		return str;
	}
	public int intDecToHex(int asc){
		return Integer.parseInt(convertDecToHex(asc));
	}


	
	int toUnsignedByte(byte b) {
		return b < 0 ? b + 256 : b;
	}

	 char toChar(byte b) {
		return (char) toUnsignedByte(b);
	}
	
	 class PGWNokiaTagConstanct {
		 public final static int RECORD_TYPE = 0x80;
		 public final static int SERVED_IMSI = 0x83;
		 public final static int PGW_ADDRESS = 0xA4;
		 public final static int CHARGINGID = 0x85;
		 public final static int SERVING_NODE_ADDRESS = 0xA6;
		 public final static int ACCESS_POINT_NAME_NETWORKID = 0x87;
		 public final static int PDP_PDN_TYPE = 0x88;
		 public final static int SERVED_PDP_PDN_ADDRESS = 0xA9;
		 public final static int DYNAMIC_ADDRESS_FLAG = 0x8B;
		 public final static int ac = 0xAC;
		 public final static int RECORD_OPENING_TIME = 0x8D;
		 public final static int DURATION = 0x8E;
		 public final static int CAUSE_FOR_RECORD_CLOSING = 0x8F;
		 public final static int DIAGNOSTICS = 0xB0;
		 public final static int RECORD_SEQUENCE_NUMBER = 0x91;
		 public final static int NODEID = 0x92;
		 public final static int LOCAL_SEQUENCE_NUMBER = 0x94;
		 public final static int APN_SELECTION_MODE = 0x95;
		 public final static int SERVED_MSISDN = 0x96;
		 public final static int CHARGING_CHARACTERISTICS = 0x97;
		 public final static int CHARGING_CHARACTERISTICS_SELECTION_MODE = 0x98;
		 public final static int SERVING_NODE_PLMN_ID = 0x9B;
		 public final static int served_IMEISV = 0x9D;
		 public final static int RATTYPE = 0x9E;
		 public final static int MS_TIME_ZONE = 0x9F << 8 + 0x1F;
		 public final static int USER_LOCATION_INFORMATION = 0x9F << 8 +  0x20;
		 public final static int LIST_OF_SERVICE_DATA = 0xBF <<8 +  0x22;
		 public final static int SEVERED_NODE_TYPE = 0xBF << 8 + 0x23;
		 public final static int PGW_PLMN_IDENTIFIER = 0x9F << 8 + 0x25;
		 public final static int START_TIME = 0x9F << 8 + 0x26;
		 public final static int STOP_TIME = 0x9F << 8 + 0x27;
		 public final static int PDN_CONNECTION_CHARGINGID = 0x9F << 8 + 0x29;
		 public final static int externalChargingID = 0x9A;
		 //		public final static int SERVED_PDP_PDN_ADDRESS_EXT = ;
		 //		public final static int DYNAMIC ADDRESSFLAGEXTENSION
		 //		public final static int RECORD EXTENSIONS
		 //
	 }
}
