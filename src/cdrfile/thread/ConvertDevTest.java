package cdrfile.thread;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.sql.SQLException;

import cdrfile.convert.StructSamSung;
import cdrfile.global.Global;
import cdrfile.global.IOUtils;
import cdrfile.global.TextFile;
import cdrfile.global.cdrfileParam;

public class ConvertDevTest extends ThreadInfo {

	/**
	 * SQL command
	 */
	protected String mSQL = null;

	/**
	 * So ban ghi da convert
	 */
	protected int mRecConvert = 0;

	/**
	 * Value no charge
	 */
	protected String mStrValues = "";
	
	/**
	 * Buffer file
	 */
	protected byte[] mBuffer = null;

	protected int mLength = 0;

	/**
	 * kich thuoc file da doc duoc
	 */
	protected int mFileByte = 0;
	
	protected int mRecLength = 0;

	public void finalize() {
		destroy();
		System.runFinalization();
		System.gc();
	}
	
	@Override
	protected void processSession() throws Exception {
		
	}
	
	public void appendValue(String strValue) {
		if (mStrValues.length() == 0) {
			mStrValues = strValue;
		}
		else {
			mStrValues += Global.cstrDelimited;
			mStrValues += strValue;
		}
	}
	
	class ConvertSamSungLTE {
		
		protected StructSamSung structSamSung = new StructSamSung();
		
		protected String mTagModuleCode = "";
		protected int mTagModuleLength = 0;
		protected int mLength = 0;
		protected int mByteN = 0;
		protected String mTagFieldCode = "";
		protected int mTagFieldLength = 0;
		
		private int convertSamSung(String pSourceConvert, String pFileName,
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
				Global.ExecuteSQL(mConnection, "alter session set nls_date_format='dd/mm/yyyy hh24:mi:ss'");
				
				// get path
				mSource = IOUtil.FillPath(pSourceConvert, Global.mSeparate) + pFileName;
				
				// open file to read
				fileCDR = new RandomAccessFile(mSource, "r");
				
				// split by day
				if (pLocalSplitFilebyDay == 1) {
					mSource = IOUtil.FillPath(pDestinationConvert, Global.mSeparate) + pCurrent_dir;
					IOUtil.forceFolderExist(mSource); // maker folder
					mSource += Global.mSeparate + pFileName;
				} else {
					mSource = IOUtil.FillPath(pDestinationConvert, Global.mSeparate) + pFileName;
				}
				
				// delete exists file
				IOUtil.deleteFile(mSource);
				
				fileConvert.openFile(mSource, 5242880);
				
				/**
				 * Add header file convert
				 */
				if (cdrfileParam.ChargeCDRFile) {
					fileConvert.addText(Global.mSamSungHeaderCharge);
				} else {
					fileConvert.addText(Global.mSamSungHeaderNoCharge);
				}
				
				mConnection.setAutoCommit(false);
				
				mSQL = "UPDATE import_header SET time_begin_convert=sysdate ";
				mSQL += "WHERE file_id=" + pFileID;
				
				Global.ExecuteSQL(mConnection, mSQL);
				
				mFileLength = (int) fileCDR.length();
				
				// khai bao buffer
				mBuffer = new byte[mFileLength];
				
				mLength = fileCDR.read(mBuffer);
				
				mFileByte = 0;

				// Total length of the CDR file in octets (header + CDR payload)
				//int cdrDataLength = 0;
				
				mFileByte = 16;
				while (mFileByte < mFileLength) {
					
					mFileByte = mFileByte + 18;
					
					mRecLength = 0;
					mByteN = 0;
					mTagModuleCode = "";
					mTagModuleLength = 0;
					
					// mRecLength
					for(int i=1; i<=2; i++) {
						
						mRecLength += Global.fixSignedByte(mBuffer[mFileByte + mByteN])
								* Math.pow(256, (2 - i));
						mByteN ++;
					}
					
					// mTagModuleCode
					for(int i=1; i<=2; i++) {
						
						mTagModuleCode += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
						mByteN ++;
					}
					
					structSamSung.RecordType = 0;
					structSamSung.ServedIMSI = "";
					structSamSung.IMSIUnauthenticatedFlag = "";
					structSamSung.ServedIMEISV = "";
					structSamSung.SGWAddressUsed = "";
					structSamSung.PGWAddressUsed = "";
					structSamSung.ChargingID = 0;
					structSamSung.ServingNodeAddress = "";
					structSamSung.ServingNodeType = "";
					structSamSung.SGWChange = "";
					structSamSung.AccessPointNameNI = "";
					structSamSung.PGWPLMNIdentifier = "";
					structSamSung.PDNConnectionID = 0;
					structSamSung.PDPPDNType = "";
					structSamSung.ServedPDPPDNAddress = "";
					structSamSung.ServedPDPPDNAddressExtension = "";
					structSamSung.DynamicAddressFlag = "";
					structSamSung.RecordOpeningTime = "";
					structSamSung.MSTimeZone = "";
					structSamSung.StartTime = "";
					structSamSung.StopTime = "";
					structSamSung.Duration = 0;
					structSamSung.CauseforRecordClosing = 0;
					structSamSung.Diagnostics = 0;
					structSamSung.RecordSequenceNumber = "";
					structSamSung.NodeID = "";
					structSamSung.RecordExtensions = "";
					structSamSung.LocalRecordSequenceNumber = 0;
					structSamSung.APNSelectionMode = 0;
					structSamSung.ServedMSISDN = "";
					structSamSung.UserCSGInformation = "";
					structSamSung.UserLocationInformation = "";
					structSamSung.ChargingCharacteristics = "";
					structSamSung.chChSelectionMode = 0;
					structSamSung.IMSSignallingContext = "";
					structSamSung.ExternalChargingIdentifier = "";
					structSamSung.ServingNodePLMNIdentifier = "";
					structSamSung.PSFurnishChargingInformation = "";
					structSamSung.CAMELInformation = "";
					structSamSung.RATType = 0;
					structSamSung.QoSRequested = "";
					structSamSung.QoSNegotiated = "";
					structSamSung.DataVolumeUplink = 0;
					structSamSung.DataVolumeDownlink = 0;
					structSamSung.ChangeCondition = "";
					structSamSung.ChangeTime = "";
					structSamSung.sdRatingGroup = 0;
					structSamSung.sdChargingRuleBaseName = "";
					structSamSung.sdResultCode = "";
					structSamSung.sdLocalSequenceNumber = 0;
					structSamSung.sdTimeofFirstUsage = "";
					structSamSung.sdTimeofLastUsage = "";
					structSamSung.sdTimeUsage = 0;
					structSamSung.sdServiceConditionChange = "";
					structSamSung.sdQosInformation = "";
					structSamSung.sdServingNodeAddress = "";
					structSamSung.sdReportTime = "";
					structSamSung.sdRATtype = "";
					structSamSung.sdFailureHandlingContinue = "";
					structSamSung.sdServiceIdentifier = 0;
					structSamSung.sdUserLocationInformation = "";
					structSamSung.sdPSFreeFormatData = "";
					structSamSung.sdPSFFDAppendIndicator = "";
					structSamSung.sdAFCharingIdentifier = "";
					structSamSung.sdMediaComponentNumber = "";
					structSamSung.sdFlowNumber = "";
					structSamSung.sdNumberofEvents = "";
					structSamSung.sdEventTimeStamps = "";
					structSamSung.sdTimeQuotaType = "";
					structSamSung.sdBaseTimeInterval = "";
					structSamSung.sdServiceSpecificData = "";
					structSamSung.sdServiceSpecificType = "";
					structSamSung.MCCMNC = "";
					
					if (mTagModuleCode.compareTo("bf4f") == 0) {
						pGWRecord();
						mRecP ++;
					} else if (mTagModuleCode.compareTo("bf4e") == 0) {
						sGWRecord();
						mRecS ++;
					} else {
						mByteN = (mByteN - 1) + mRecLength;
						mFileByte += mByteN;
					}
					
					AnalyseGWRecord();
					
					// write to text file
					if (cdrfileParam.ChargeCDRFile) {
						fileConvert.addText(mStrValues);
						mStrValues = "";
					} else {
						mStrValues = structSamSung.RecordType + "|"
								+ structSamSung.ServedIMSI + "|"
								+ structSamSung.IMSIUnauthenticatedFlag + "|"
								+ structSamSung.ServedIMEISV + "|"
								+ structSamSung.SGWAddressUsed + "|"
								+ structSamSung.PGWAddressUsed + "|"
								+ structSamSung.ChargingID + "|"
								+ structSamSung.ServingNodeAddress + "|"
								+ structSamSung.ServingNodeType + "|"
								+ structSamSung.SGWChange + "|"
								+ structSamSung.AccessPointNameNI + "|"
								+ structSamSung.PGWPLMNIdentifier + "|"
								+ structSamSung.PDNConnectionID + "|"
								+ structSamSung.PDPPDNType + "|"
								+ structSamSung.ServedPDPPDNAddress + "|"
								+ structSamSung.ServedPDPPDNAddressExtension + "|"
								+ structSamSung.DynamicAddressFlag + "|"
								+ structSamSung.RecordOpeningTime + "|"
								+ structSamSung.MSTimeZone + "|"
								+ structSamSung.StartTime + "|"
								+ structSamSung.StopTime + "|"
								+ structSamSung.Duration + "|"
								+ structSamSung.CauseforRecordClosing + "|"
								+ structSamSung.Diagnostics + "|"
								+ structSamSung.RecordSequenceNumber + "|"
								+ structSamSung.NodeID + "|"
								+ structSamSung.RecordExtensions + "|"
								+ structSamSung.LocalRecordSequenceNumber + "|"
								+ structSamSung.APNSelectionMode + "|"
								+ structSamSung.ServedMSISDN + "|"
								+ structSamSung.UserCSGInformation + "|"
								+ structSamSung.UserLocationInformation + "|"
								+ structSamSung.ChargingCharacteristics + "|"
								+ structSamSung.chChSelectionMode + "|"
								+ structSamSung.IMSSignallingContext + "|"
								+ structSamSung.ExternalChargingIdentifier + "|"
								+ structSamSung.ServingNodePLMNIdentifier + "|"
								+ structSamSung.PSFurnishChargingInformation + "|"
								+ structSamSung.CAMELInformation + "|"
								+ structSamSung.RATType + "|"
								+ structSamSung.QoSRequested + "|"
								+ structSamSung.QoSNegotiated + "|"
								+ structSamSung.DataVolumeUplink + "|"
								+ structSamSung.DataVolumeDownlink + "|"
								+ structSamSung.ChangeCondition + "|"
								+ structSamSung.ChangeTime + "|"
								+ structSamSung.sdRatingGroup + "|"
								+ structSamSung.sdChargingRuleBaseName + "|"
								+ structSamSung.sdResultCode + "|"
								+ structSamSung.sdLocalSequenceNumber + "|"
								+ structSamSung.sdTimeofFirstUsage + "|"
								+ structSamSung.sdTimeofLastUsage + "|"
								+ structSamSung.sdTimeUsage + "|"
								+ structSamSung.sdServiceConditionChange + "|"
								+ structSamSung.sdQosInformation + "|"
								+ structSamSung.sdServingNodeAddress + "|"
								+ structSamSung.sdReportTime + "|"
								+ structSamSung.sdRATtype + "|"
								+ structSamSung.sdFailureHandlingContinue + "|"
								+ structSamSung.sdServiceIdentifier + "|"
								+ structSamSung.sdUserLocationInformation + "|"
								+ structSamSung.sdPSFreeFormatData + "|"
								+ structSamSung.sdPSFFDAppendIndicator + "|"
								+ structSamSung.sdAFCharingIdentifier + "|"
								+ structSamSung.sdMediaComponentNumber + "|"
								+ structSamSung.sdFlowNumber + "|"
								+ structSamSung.sdNumberofEvents + "|"
								+ structSamSung.sdEventTimeStamps + "|"
								+ structSamSung.sdTimeQuotaType + "|"
								+ structSamSung.sdBaseTimeInterval + "|"
								+ structSamSung.sdServiceSpecificData + "|"
								+ structSamSung.sdServiceSpecificType;
						
						fileConvert.addText(mStrValues);
						mStrValues = "";
					}
				}
				
				mRecConvert = mRecS + mRecP;
				
				System.out.println("mRecN: " + mRecConvert);
				
				writeLogFile("         - sGW               : " + Global.rpad(Integer.toString(mRecS), 6, " "));
				writeLogFile("         - pGW               : " + Global.rpad(Integer.toString(mRecP), 6, " "));
				
				writeLogFile("      -------------------------------");
				writeLogFile("      Total record converted : " + Global.rpad(Integer.toString(mRecConvert), 6, " "));
				
				if (cdrfileParam.ChargeCDRFile) {
					mSQL = "UPDATE import_header SET time_end_convert=sysdate,status=" + Global.StateConverted + ",rec_total=" + mRecConvert + ",min_calling_time='" + lastCallingTime + "',max_calling_time='" + firstCallingTime + "' WHERE file_id = " + pFileID;
				} else {
					mSQL = "UPDATE import_header SET time_end_convert=sysdate,status=" + Global.StateRated + ",rec_total=" + mRecConvert + ",min_calling_time='" + lastCallingTime + "',max_calling_time='" + firstCallingTime + "' WHERE file_id = " + pFileID;
				}
				
				Global.ExecuteSQL(mConnection, mSQL);
				
				mConnection.commit();
			} catch (FileNotFoundException e) {
				e.printStackTrace();

				return Global.ErrFileNotFound;
			} catch (Exception ex) {
				ex.printStackTrace();
				mConnection.rollback();
				mSQL = "UPDATE import_header SET status="
						+ Global.StateConvertedError + ",note='"
						+ ex.toString() + " at rec:" + mRecConvert
						+ "' WHERE file_id = " + pFileID;
				Global.ExecuteSQL(mConnection, mSQL); // update file error converted
				mConnection.commit();

				if (cdrfileParam.OnErrorResumeNext.compareTo("TRUE") == 0) {
					writeLogFile(" - " + ex.toString() + " - at record:"
							+ mRecConvert);
					return Global.ErrFileConverted;
				} else {
					System.out.println(mRecConvert + " " + ex.toString());
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
		
		private void AnalyseGWRecord() {
			
			if (structSamSung.ServedIMSI.endsWith("f")) {
				structSamSung.ServedIMSI = structSamSung.ServedIMSI.substring(0, structSamSung.ServedIMSI.length() - 1);
			}
			
			if (structSamSung.ServedMSISDN.length() > 0) {
				if (structSamSung.ServedMSISDN.substring(0, 2).compareTo("19") == 0) {
					structSamSung.ServedMSISDN = structSamSung.ServedMSISDN.substring(2);
					if (structSamSung.ServedMSISDN.substring(0, 2).compareTo("84") == 0) {
						structSamSung.ServedMSISDN = structSamSung.ServedMSISDN.substring(2);
					}
				}
				if (structSamSung.ServedMSISDN.endsWith("f")) {
					structSamSung.ServedMSISDN = structSamSung.ServedMSISDN.substring(0, structSamSung.ServedMSISDN.length() - 1);
				}
			}
			
			if (structSamSung.PGWPLMNIdentifier.contains("f")) {
				structSamSung.PGWPLMNIdentifier = structSamSung.PGWPLMNIdentifier.replace("f", "");
			}
			
			if (structSamSung.ServingNodePLMNIdentifier.contains("f")) {
				structSamSung.ServingNodePLMNIdentifier = structSamSung.ServingNodePLMNIdentifier.replace("f", "");
			}
			
			if (structSamSung.RecordOpeningTime.length() > 12) {
				structSamSung.RecordOpeningTime = Global.Format(structSamSung.RecordOpeningTime.substring(0, 12), "yyMMddHHmmss", "dd/MM/yyyy HH:mm:ss");
			}
			
			if (structSamSung.StartTime.length() > 12) {
				structSamSung.StartTime = Global.Format(structSamSung.StartTime.substring(0, 12), "yyMMddHHmmss", "dd/MM/yyyy HH:mm:ss");
			}
			
			if (structSamSung.StopTime.length() > 12) {
				structSamSung.StopTime = Global.Format(structSamSung.StopTime.substring(0, 12), "yyMMddHHmmss", "dd/MM/yyyy HH:mm:ss");
			}
			
			if (structSamSung.sdReportTime.length() > 12) {
				structSamSung.sdReportTime = Global.Format(structSamSung.sdReportTime.substring(0, 12), "yyMMddHHmmss", "dd/MM/yyyy HH:mm:ss");
			}
			
			if (structSamSung.ChangeTime.length() > 12) {
				structSamSung.ChangeTime = Global.Format(structSamSung.ChangeTime.substring(0, 12), "yyMMddHHmmss", "dd/MM/yyyy HH:mm:ss");
			}
			
			if (structSamSung.PDPPDNType.length() > 4) {
				structSamSung.PDPPDNType = structSamSung.PDPPDNType.substring(0, 4);
			}
			
			if (structSamSung.UserLocationInformation.contains("f")) {
				structSamSung.UserLocationInformation = structSamSung.UserLocationInformation.replace("f", "");
			}
			
			if (structSamSung.sdUserLocationInformation.contains("f")) {
				structSamSung.sdUserLocationInformation = structSamSung.sdUserLocationInformation.replace("f", "");
			}
		}
		
		private void pGWRecord() {
			
			int mRet = 0;
			String miTagFieldCode;
			int miTagFieldLength;
			int byteN = 0;
			String sdTagFieldCode;
			int sdTagFieldLength;
			int sdByteN = 0;
			
			while (mByteN < mRecLength) {
				mLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
				
				mByteN++;
				if (mLength > 128) {
					
					mRet = mLength - 128;
					for (int i = 1; i <= mRet; i++)
					{
						mTagModuleLength += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (mRet - i));
						mByteN++;
					}
				}
				else {
					
					mTagModuleLength = mLength;
				}
				
				mTagModuleLength += mByteN;
				while (mByteN < mTagModuleLength) {
					
					mTagFieldCode = Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
					mByteN++;
					
					if (mTagFieldCode.compareTo("9f") == 0 || mTagFieldCode.compareTo("bf") == 0) {
						mLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
						if (mLength > 128) {
							mByteN++;
							mRet = mLength - 128;
							for (int i = 1; i <= mRet; i++) {
								mTagFieldCode += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
								mByteN++;
							}
						} else {
							mTagFieldCode += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					
					mTagFieldLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
					mByteN++;
					
					// RecordType
					if (mTagFieldCode.compareTo("80") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.RecordType += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (mTagFieldLength - i));
							mByteN++;
						}
					}
					// ServedIMSI
					else if (mTagFieldCode.compareTo("83") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.ServedIMSI += Global.HexToTBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					// PGWAddressUsed
					else if (mTagFieldCode.compareTo("a4") == 0) {
						byteN = 0;
						while (byteN < mTagFieldLength) {
							miTagFieldCode = Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]));
							byteN++;
							
							miTagFieldLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]);
							byteN++;
							
							for(int i=1; i<=miTagFieldLength; i++) {
								structSamSung.PGWAddressUsed += Global.fixSignedCharByte(mBuffer[mFileByte + mByteN + byteN]);
								byteN++;
							}
						}
						
						mByteN += mTagFieldLength;
					}
					// ChargingID
					else if (mTagFieldCode.compareTo("85") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.ChargingID += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (mTagFieldLength - i));
							mByteN++;
						}
					}
					// ServingNodeAddress
					else if (mTagFieldCode.compareTo("a6") == 0) {
						
						byteN = 0;
						while (byteN < mTagFieldLength) {
							miTagFieldCode = Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]));
							byteN++;
							
							miTagFieldLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]);
							byteN++;
							
							for(int i=1; i<=miTagFieldLength; i++) {
								structSamSung.ServingNodeAddress += Global.fixSignedCharByte(mBuffer[mFileByte + mByteN + byteN]);
								byteN++;
							}
						}
						
						mByteN += mTagFieldLength;
					}
					// AccessPointNameNI
					else if (mTagFieldCode.compareTo("87") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.AccessPointNameNI += Global.fixSignedCharByte(mBuffer[mFileByte + mByteN]);
							mByteN++;
						}
					}
					// PDPPDNType
					else if (mTagFieldCode.compareTo("88") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.PDPPDNType += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					// ServedPDPPDNAddress
					else if (mTagFieldCode.compareTo("a9") == 0) {
						byteN = 0;
						while (byteN < mTagFieldLength) {
							miTagFieldCode = Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]));
							byteN++;
							
							miTagFieldLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]);
							byteN++;
							
							// ServedPDPPDNAddress
							if (miTagFieldCode.compareTo("a0") == 0) {
								
								miTagFieldCode = Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]));
								byteN++;
								
								miTagFieldLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]);
								byteN++;
								
								// IPAddress Data
								for(int i=1; i<=miTagFieldLength; i++) {
									structSamSung.ServedPDPPDNAddress += Global.fixSignedCharByte(mBuffer[mFileByte + mByteN + byteN]);
									byteN++;
								}
							} else {
								byteN += miTagFieldLength;
							}
						}
						
						mByteN += mTagFieldLength;
					}
					// ff - TRUE
					else if (mTagFieldCode.compareTo("8b") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.DynamicAddressFlag += Global.HexToTBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					// RecordOpeningTime
					else if (mTagFieldCode.compareTo("8d") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.RecordOpeningTime += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					// Duration
					else if (mTagFieldCode.compareTo("8e") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.Duration += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (mTagFieldLength - i));
							mByteN++;
						}
					}
					// CauseforRecordClosing
					else if (mTagFieldCode.compareTo("8f") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.CauseforRecordClosing += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (mTagFieldLength - i));
							mByteN++;
						}
					}
					// Diagnostics
					else if (mTagFieldCode.compareTo("b0") == 0) {
						
						byteN = 0;
						while (byteN < mTagFieldLength) {
							miTagFieldCode = Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]));
							byteN++;
							
							miTagFieldLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]);
							byteN++;
							
							// Diagnostics Data
							if (miTagFieldCode.compareTo("80") == 0) {
								
								// IPAddress Data
								for(int i=1; i<=miTagFieldLength; i++) {
									structSamSung.Diagnostics += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]) * Math.pow(256, (miTagFieldLength - i));
									byteN++;
								}
							} else {
								byteN += miTagFieldLength;
							}
						}
						
						mByteN += mTagFieldLength;
					}
					// RecordSequenceNumber - no value
					else if (mTagFieldCode.compareTo("91") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.RecordSequenceNumber += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (mTagFieldLength - i));
							mByteN++;
						}
					}
					// NodeID
					else if (mTagFieldCode.compareTo("92") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.NodeID += Global.fixSignedCharByte(mBuffer[mFileByte + mByteN]);
							mByteN++;
						}
					}
					// LocalRecordSequenceNumber
					else if (mTagFieldCode.compareTo("94") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.LocalRecordSequenceNumber += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (mTagFieldLength - i));
							mByteN++;
						}
					}
					// APNSelectionMode value
					else if (mTagFieldCode.compareTo("95") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.APNSelectionMode += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (mTagFieldLength - i));
							mByteN++;
						}
					}
					// ServedMSISDN
					else if (mTagFieldCode.compareTo("96") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.ServedMSISDN += Global.HexToTBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					// ChargingCharacteristics
					else if (mTagFieldCode.compareTo("97") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.ChargingCharacteristics += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					// chChSelectionMode value
					else if (mTagFieldCode.compareTo("98") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.chChSelectionMode += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (mTagFieldLength - i));
							mByteN++;
						}
					}
					// ServingNodePLMNIdentifier
					else if (mTagFieldCode.compareTo("9b") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.ServingNodePLMNIdentifier += Global.HexToTBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					// ServedIMEISV no value
					else if (mTagFieldCode.compareTo("9d") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.ServedIMEISV += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					// RATType
					else if (mTagFieldCode.compareTo("9e") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.RATType += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (mTagFieldLength - i));
							mByteN++;
						}
					}
					// MSTimeZone - value ""
					else if (mTagFieldCode.compareTo("9f1f") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.MSTimeZone += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					// UserLocationInformation - value ""
					else if (mTagFieldCode.compareTo("9f20") == 0) {
						
						mRet = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
						mByteN ++;
						
						if (mRet != 191) {
							if (structSamSung.MCCMNC.compareTo("") == 0) {
								for (int i = 1; i <= 3; i++) {
									structSamSung.UserLocationInformation += Global.HexToTBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
									mByteN++;
								}
							}
							
							String tmp = Global.rpad(Global.HexToBINARY(mRet), 8, "0");
							
							//System.out.println("---" + mRet + "(" + tmp + ")");
							
							// CGI
							if (tmp.charAt(7) == '1') {
								mRet = 0;
								for (int i = 1; i <= 2; i++) {
									mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (2 - i));
									mByteN++;
									
								}
								structSamSung.UserLocationInformation += " " + mRet;
								
								mRet = 0;
								for (int i = 1; i <= 2; i++) {
									mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (2 - i));
									mByteN++;
									
								}
								structSamSung.UserLocationInformation += " " + mRet;
							}
							
							// SAI
							if (tmp.charAt(6) == '1') {
								mRet = 0;
								for (int i = 1; i <= 2; i++) {
									mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (2 - i));
									mByteN++;
									
								}
								structSamSung.UserLocationInformation += " " + mRet;
								
								mRet = 0;
								for (int i = 1; i <= 2; i++) {
									mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (2 - i));
									mByteN++;
									
								}
								structSamSung.UserLocationInformation += " " + mRet;
							}
							
							// RAI
							if (tmp.charAt(5) == '1') {
								mRet = 0;
								for (int i = 1; i <= 2; i++) {
									mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (2 - i));
									mByteN++;
									
								}
								structSamSung.UserLocationInformation += " " + mRet;
								
								mRet = 0;
								for (int i = 1; i <= 2; i++) {
									mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (2 - i));
									mByteN++;
									
								}
								structSamSung.UserLocationInformation += " " + mRet;
							}
							
							// TAI
							if (tmp.charAt(4) == '1') {
								mRet = 0;
								for (int i = 1; i <= 2; i++) {
									mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (2 - i));
									mByteN++;
									
								}
								structSamSung.UserLocationInformation += " " + mRet;
							}
							
							// ECGI
							if (tmp.charAt(3) == '1') {
								mRet = 0;
								for (int i = 1; i <= 1; i++) {
									mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (2 - i));
									mByteN++;
									
								}
								structSamSung.UserLocationInformation += " " + mRet;
								
								mRet = 0;
								for (int i = 1; i <= 3; i++) {
									mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (2 - i));
									mByteN++;
									
								}
								structSamSung.UserLocationInformation += " " + mRet;
							}
							
							/*for (int i = 1; i <= mTagFieldLength; i++) {
								structSamSung.UserLocationInformation += Global.HexToTBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
								mByteN++;
							}*/
							
							//System.out.println(structSamSung.UserLocationInformation);
						} else {
							mByteN += mTagFieldLength;
						}
					}
					// listOfServiceData - sequence
					else if (mTagFieldCode.compareTo("bf22") == 0) {
						
						int dup = 0;
						byteN = 0;
						while (byteN < mTagFieldLength) {
							miTagFieldCode = Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]));
							byteN++;
							
							miTagFieldLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]);
							byteN++;
							
							if (miTagFieldCode.compareTo("30") == 0) {
								
								dup ++;
								sdByteN = 0;
								while(sdByteN < miTagFieldLength) {
									sdTagFieldCode = Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]));
									sdByteN++;
									
									sdTagFieldLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]);
									byteN++;
									
									if (sdTagFieldCode.compareTo("81") == 0) {
										for (int i = 1; i <= sdTagFieldLength; i++) {
											if (dup == 1)
												structSamSung.sdRatingGroup += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]) * Math.pow(256, (sdTagFieldLength - i));
											sdByteN++;
										}
									}
									else if (sdTagFieldCode.compareTo("82") == 0) {
										for (int i = 1; i <= sdTagFieldLength; i++) {
											if (dup == 1)
												structSamSung.sdChargingRuleBaseName += Global.fixSignedCharByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]);
											sdByteN++;
										}
									}
									else if (sdTagFieldCode.compareTo("84") == 0) {
										for (int i = 1; i <= sdTagFieldLength; i++) {
											if (dup == 1)
												structSamSung.sdLocalSequenceNumber += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]) * Math.pow(256, (sdTagFieldLength - i));
											sdByteN++;
										}
									}
									else if (sdTagFieldCode.compareTo("85") == 0) {
										for (int i = 1; i <= sdTagFieldLength; i++) {
											if (dup == 1)
												structSamSung.sdTimeofFirstUsage += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]));
											sdByteN++;
										}
									}
									else if (sdTagFieldCode.compareTo("86") == 0) {
										for (int i = 1; i <= sdTagFieldLength; i++) {
											if (dup == 1)
												structSamSung.sdTimeofLastUsage += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]));
											sdByteN++;
										}
									}
									else if (sdTagFieldCode.compareTo("87") == 0) {
										for (int i = 1; i <= sdTagFieldLength; i++) {
											if (dup == 1)
												structSamSung.sdTimeUsage += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]) * Math.pow(256, (sdTagFieldLength - i));
											sdByteN++;
										}
									}
									else if (sdTagFieldCode.compareTo("88") == 0) {
										for (int i = 1; i <= sdTagFieldLength; i++) {
											if (dup == 1)
												structSamSung.sdServiceConditionChange += Global.HexToBINARY(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]));
											sdByteN++;
										}
									}
									else if (sdTagFieldCode.compareTo("8c") == 0) {
										for (int i = 1; i <= sdTagFieldLength; i++) {
											structSamSung.DataVolumeUplink += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]) * Math.pow(256, (sdTagFieldLength - i));
											sdByteN++;
										}
									}
									else if (sdTagFieldCode.compareTo("8d") == 0) {
										for (int i = 1; i <= sdTagFieldLength; i++) {
											structSamSung.DataVolumeDownlink += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]) * Math.pow(256, (sdTagFieldLength - i));
											sdByteN++;
										}
									}
									else if (sdTagFieldCode.compareTo("8e") == 0) {
										for (int i = 1; i <= sdTagFieldLength; i++) {
											if (dup == 1)
												structSamSung.sdReportTime += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]));
											sdByteN++;
										}
									}
									else if (sdTagFieldCode.compareTo("91") == 0) {
										for (int i = 1; i <= sdTagFieldLength; i++) {
											if (dup == 1)
												structSamSung.sdServiceIdentifier += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]) * Math.pow(256, (sdTagFieldLength - i));
											sdByteN++;
										}
									}
									else if (sdTagFieldCode.compareTo("94") == 0) {
										mRet = Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]);
										
										if (mRet != 191 && dup == 1) {
											
											sdByteN ++;
											
											if (structSamSung.MCCMNC.compareTo("") == 0) {
												for (int i = 1; i <= 3; i++) {
													structSamSung.sdUserLocationInformation += Global.HexToTBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]));
													sdByteN++;
												}
											}
											
											String tmp = Global.rpad(Global.HexToBINARY(mRet), 8, "0");
											
											// CGI
											if (tmp.charAt(7) == '1') {
												mRet = 0;
												for (int i = 1; i <= 2; i++) {
													mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]) * Math.pow(256, (2 - i));
													sdByteN++;
													
												}
												structSamSung.sdUserLocationInformation += " " + mRet;
												
												mRet = 0;
												for (int i = 1; i <= 2; i++) {
													mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]) * Math.pow(256, (2 - i));
													sdByteN++;
													
												}
												structSamSung.sdUserLocationInformation += " " + mRet;
											}
											
											// SAI
											if (tmp.charAt(6) == '1') {
												mRet = 0;
												for (int i = 1; i <= 2; i++) {
													mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]) * Math.pow(256, (2 - i));
													sdByteN++;
													
												}
												structSamSung.sdUserLocationInformation += " " + mRet;
												
												mRet = 0;
												for (int i = 1; i <= 2; i++) {
													mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]) * Math.pow(256, (2 - i));
													sdByteN++;
													
												}
												structSamSung.sdUserLocationInformation += " " + mRet;
											}
											
											// RAI
											if (tmp.charAt(5) == '1') {
												mRet = 0;
												for (int i = 1; i <= 2; i++) {
													mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]) * Math.pow(256, (2 - i));
													sdByteN++;
													
												}
												structSamSung.sdUserLocationInformation += " " + mRet;
												
												mRet = 0;
												for (int i = 1; i <= 2; i++) {
													mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]) * Math.pow(256, (2 - i));
													sdByteN++;
													
												}
												structSamSung.sdUserLocationInformation += " " + mRet;
											}
											
											// TAI
											if (tmp.charAt(4) == '1') {
												mRet = 0;
												for (int i = 1; i <= 2; i++) {
													mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]) * Math.pow(256, (2 - i));
													sdByteN++;
													
												}
												structSamSung.sdUserLocationInformation += " " + mRet;
											}
											
											// ECGI
											if (tmp.charAt(3) == '1') {
												mRet = 0;
												for (int i = 1; i <= 1; i++) {
													mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]) * Math.pow(256, (2 - i));
													sdByteN++;
													
												}
												structSamSung.sdUserLocationInformation += " " + mRet;
												
												mRet = 0;
												for (int i = 1; i <= 3; i++) {
													mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]) * Math.pow(256, (2 - i));
													sdByteN++;
													
												}
												structSamSung.sdUserLocationInformation += " " + mRet;
											}
										} else {
											sdByteN += sdTagFieldLength;
										}
									}
									else {
										sdByteN += sdTagFieldLength;
									}
								}
							} else {
								byteN += miTagFieldLength;
							}
						}
						
						mByteN += mTagFieldLength;
					}
					// ServingNodeType - sequence 0a
					else if (mTagFieldCode.compareTo("bf23") == 0) {
						
						byteN = 0;
						while (byteN < mTagFieldLength) {
							miTagFieldCode = Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]));
							byteN++;
							
							miTagFieldLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]);
							byteN++;
							
							if (miTagFieldCode.compareTo("0a") == 0) {
								for(int i=1; i<=miTagFieldLength; i++) {
									structSamSung.ServingNodeType += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]));
									byteN++;
								}
							} else {
								byteN += miTagFieldLength;
							}
						}
						
						mByteN += mTagFieldLength;
					}
					// PGWPLMNIdentifier
					else if (mTagFieldCode.compareTo("9f25") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.PGWPLMNIdentifier += Global.HexToTBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					// StartTime
					else if (mTagFieldCode.compareTo("9f26") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.StartTime += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					// StopTime
					else if (mTagFieldCode.compareTo("9f27") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.StopTime += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					// PDNConnectionID
					else if (mTagFieldCode.compareTo("9f29") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.PDNConnectionID += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (mTagFieldLength - i));
							mByteN++;
						}
					}
					// UserCSGInformation - sequence
					else if (mTagFieldCode.compareTo("bf2b") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							//structSamSung.UserCSGInformation += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					// threeGPP2UserLocationInformation
					else if (mTagFieldCode.compareTo("bf2c") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							
							mByteN++;
						}
					}
					// ServedPDPPDNAddressExtension - no value
					else if (mTagFieldCode.compareTo("bf2d") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.ServedPDPPDNAddressExtension += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					// DynamicAddressFlag - no value
					else if (mTagFieldCode.compareTo("9f2f") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.DynamicAddressFlag += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					else {
						mByteN += mTagFieldLength;
					}
				}
			}
			
			mFileByte += mByteN;
		}
		
		/**
		 * sGW Record
		 */
		private void sGWRecord() {
			
			int mRet = 0;
			String miTagFieldCode;
			int miTagFieldLength;
			int byteN = 0;
			String sdTagFieldCode;
			int sdTagFieldLength;
			int sdByteN = 0;
			
			while (mByteN < mRecLength) {
				
				mLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
				
				mByteN++;
				if (mLength > 128) {
					
					mRet = mLength - 128;
					for (int i = 1; i <= mRet; i++)
					{
						mTagModuleLength += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (mRet - i));
						mByteN++;
					}
				}
				else {
					
					mTagModuleLength = mLength;
				}
				
				mTagModuleLength += mByteN;
				while (mByteN < mTagModuleLength) {
					
					mTagFieldCode = Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
					mByteN++;
					
					if (mTagFieldCode.compareTo("9f") == 0 || mTagFieldCode.compareTo("bf") == 0) {
						mLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
						if (mLength > 128) {
							mByteN++;
							mRet = mLength - 128;
							for (int i = 1; i <= mRet; i++) {
								mTagFieldCode += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
								mByteN++;
							}
						} else {
							mTagFieldCode += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					
					mTagFieldLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
					mByteN++;
					
					// RecordType
					if (mTagFieldCode.compareTo("80") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.RecordType += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (mTagFieldLength - i));
							mByteN++;
						}
					}
					
					// ServedIMSI
					else if (mTagFieldCode.compareTo("83") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.ServedIMSI += Global.HexToTBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					
					// s-GWAddress - SGWAddressUsed
					else if (mTagFieldCode.compareTo("a4") == 0) {
						byteN = 0;
						while (byteN < mTagFieldLength) {
							miTagFieldCode = Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]));
							byteN++;
							
							miTagFieldLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]);
							byteN++;
							
							for(int i=1; i<=miTagFieldLength; i++) {
								structSamSung.SGWAddressUsed += Global.fixSignedCharByte(mBuffer[mFileByte + mByteN + byteN]);
								byteN++;
							}
						}
						
						mByteN += mTagFieldLength;
					}
					// ChargingID
					else if (mTagFieldCode.compareTo("85") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.ChargingID += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (mTagFieldLength - i));
							mByteN++;
						}
					}
					// ServingNodeAddress
					else if (mTagFieldCode.compareTo("a6") == 0) {
						/*for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.ServingNodeAddress += Global.HexToTBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}*/
						byteN = 0;
						while (byteN < mTagFieldLength) {
							miTagFieldCode = Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]));
							byteN++;
							
							miTagFieldLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]);
							byteN++;
							
							for(int i=1; i<=miTagFieldLength; i++) {
								structSamSung.ServingNodeAddress += Global.fixSignedCharByte(mBuffer[mFileByte + mByteN + byteN]);
								byteN++;
							}
						}
						
						mByteN += mTagFieldLength;
					}
					// AccessPointNameNI
					else if (mTagFieldCode.compareTo("87") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.AccessPointNameNI += Global.fixSignedCharByte(mBuffer[mFileByte + mByteN]);
							mByteN++;
						}
					}
					// PDPPDNType
					else if (mTagFieldCode.compareTo("88") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.PDPPDNType += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					// ServedPDPPDNAddress
					else if (mTagFieldCode.compareTo("a9") == 0) {
						byteN = 0;
						while (byteN < mTagFieldLength) {
							miTagFieldCode = Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]));
							byteN++;
							
							miTagFieldLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]);
							byteN++;
							
							// ServedPDPPDNAddress
							if (miTagFieldCode.compareTo("a0") == 0) {
								
								miTagFieldCode = Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]));
								byteN++;
								
								miTagFieldLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]);
								byteN++;
								
								// IPAddress Data
								for(int i=1; i<=miTagFieldLength; i++) {
									structSamSung.ServedPDPPDNAddress += Global.fixSignedCharByte(mBuffer[mFileByte + mByteN + byteN]);
									byteN++;
								}
							} else {
								byteN += miTagFieldLength;
							}
						}
						
						mByteN += mTagFieldLength;
					}
					// ff - TRUE
					else if (mTagFieldCode.compareTo("8b") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.DynamicAddressFlag += Global.HexToTBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					else if (mTagFieldCode.compareTo("ac") == 0) {
						
						int dup = 0;
						byteN = 0;
						while (byteN < mTagFieldLength) {
							
							miTagFieldCode = Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]));
							byteN++;
							
							miTagFieldLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]);
							byteN++;
							
							if (miTagFieldCode.compareTo("30") == 0) {
								
								dup ++;
								sdByteN = 0;
								while(sdByteN < miTagFieldLength) {
									
									sdTagFieldCode = Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]));
									sdByteN++;
									
									sdTagFieldLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]);
									byteN++;
									
									// DataVolumeUplink
									if (sdTagFieldCode.compareTo("83") == 0) {
										for (int i = 1; i <= sdTagFieldLength; i++) {
											structSamSung.DataVolumeUplink += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]) * Math.pow(256, (sdTagFieldLength - i));
											sdByteN++;
										}
									}
									// DataVolumeDownlink
									else if (sdTagFieldCode.compareTo("84") == 0) {
										for (int i = 1; i <= sdTagFieldLength; i++) {
											structSamSung.DataVolumeDownlink += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]) * Math.pow(256, (sdTagFieldLength - i));
											sdByteN++;
										}
									}
									// ChangeCondition
									else if (sdTagFieldCode.compareTo("85") == 0) {
										for (int i = 1; i <= sdTagFieldLength; i++) {
											if (dup == 1)
												structSamSung.ChangeCondition += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]));
											sdByteN++;
										}
									}
									// ChangeTime
									else if (sdTagFieldCode.compareTo("86") == 0) {
										for (int i = 1; i <= sdTagFieldLength; i++) {
											if (dup == 1)
												structSamSung.ChangeTime += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]));
											sdByteN++;
										}
									}
									// sdUserLocationInformation
									else if (sdTagFieldCode.compareTo("88") == 0) {
										mRet = Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]);
										
										if (mRet != 191 && dup == 1) {
											
											sdByteN ++;
											
											if (structSamSung.MCCMNC.compareTo("") == 0) {
												for (int i = 1; i <= 3; i++) {
													structSamSung.sdUserLocationInformation += Global.HexToTBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]));
													sdByteN++;
												}
											}
											
											String tmp = Global.rpad(Global.HexToBINARY(mRet), 8, "0");
											
											// CGI
											if (tmp.charAt(7) == '1') {
												mRet = 0;
												for (int i = 1; i <= 2; i++) {
													mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]) * Math.pow(256, (2 - i));
													sdByteN++;
													
												}
												structSamSung.sdUserLocationInformation += " " + mRet;
												
												mRet = 0;
												for (int i = 1; i <= 2; i++) {
													mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]) * Math.pow(256, (2 - i));
													sdByteN++;
													
												}
												structSamSung.sdUserLocationInformation += " " + mRet;
											}
											
											// SAI
											if (tmp.charAt(6) == '1') {
												mRet = 0;
												for (int i = 1; i <= 2; i++) {
													mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]) * Math.pow(256, (2 - i));
													sdByteN++;
													
												}
												structSamSung.sdUserLocationInformation += " " + mRet;
												
												mRet = 0;
												for (int i = 1; i <= 2; i++) {
													mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]) * Math.pow(256, (2 - i));
													sdByteN++;
													
												}
												structSamSung.sdUserLocationInformation += " " + mRet;
											}
											
											// RAI
											if (tmp.charAt(5) == '1') {
												mRet = 0;
												for (int i = 1; i <= 2; i++) {
													mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]) * Math.pow(256, (2 - i));
													sdByteN++;
													
												}
												structSamSung.sdUserLocationInformation += " " + mRet;
												
												mRet = 0;
												for (int i = 1; i <= 2; i++) {
													mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]) * Math.pow(256, (2 - i));
													sdByteN++;
													
												}
												structSamSung.sdUserLocationInformation += " " + mRet;
											}
											
											// TAI
											if (tmp.charAt(4) == '1') {
												mRet = 0;
												for (int i = 1; i <= 2; i++) {
													mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]) * Math.pow(256, (2 - i));
													sdByteN++;
													
												}
												structSamSung.sdUserLocationInformation += " " + mRet;
											}
											
											// ECGI
											if (tmp.charAt(3) == '1') {
												mRet = 0;
												for (int i = 1; i <= 1; i++) {
													mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]) * Math.pow(256, (2 - i));
													sdByteN++;
													
												}
												structSamSung.sdUserLocationInformation += " " + mRet;
												
												mRet = 0;
												for (int i = 1; i <= 3; i++) {
													mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN + sdByteN]) * Math.pow(256, (2 - i));
													sdByteN++;
													
												}
												structSamSung.sdUserLocationInformation += " " + mRet;
											}
										} else {
											sdByteN += sdTagFieldLength;
										}
									}
									else {
										sdByteN += sdTagFieldLength;
									}
								}
							} else {
								byteN += miTagFieldLength;
							}
						}
						
						mByteN += mTagFieldLength;
					}
					// RecordOpeningTime
					else if (mTagFieldCode.compareTo("8d") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.RecordOpeningTime += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					// Duration
					else if (mTagFieldCode.compareTo("8e") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.Duration += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (mTagFieldLength - i));
							mByteN++;
						}
					}
					// CauseforRecordClosing
					else if (mTagFieldCode.compareTo("8f") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.CauseforRecordClosing += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (mTagFieldLength - i));
							mByteN++;
						}
					}
					// Diagnostics
					else if (mTagFieldCode.compareTo("b0") == 0) {
						
						byteN = 0;
						while (byteN < mTagFieldLength) {
							miTagFieldCode = Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]));
							byteN++;
							
							miTagFieldLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]);
							byteN++;
							
							// Diagnostics Data
							if (miTagFieldCode.compareTo("80") == 0) {
								
								// IPAddress Data
								for(int i=1; i<=miTagFieldLength; i++) {
									structSamSung.Diagnostics += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]) * Math.pow(256, (miTagFieldLength - i));
									byteN++;
								}
							} else {
								byteN += miTagFieldLength;
							}
						}
						
						mByteN += mTagFieldLength;
					}
					// RecordSequenceNumber - no value
					else if (mTagFieldCode.compareTo("91") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.RecordSequenceNumber += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (mTagFieldLength - i));
							mByteN++;
						}
					}
					// NodeID
					else if (mTagFieldCode.compareTo("92") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.NodeID += Global.fixSignedCharByte(mBuffer[mFileByte + mByteN]);
							mByteN++;
						}
					}
					// LocalRecordSequenceNumber
					else if (mTagFieldCode.compareTo("94") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.LocalRecordSequenceNumber += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (mTagFieldLength - i));
							mByteN++;
						}
					}
					// APNSelectionMode value
					else if (mTagFieldCode.compareTo("95") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.APNSelectionMode += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (mTagFieldLength - i));
							mByteN++;
						}
					}
					// ServedMSISDN
					else if (mTagFieldCode.compareTo("96") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.ServedMSISDN += Global.HexToTBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					// ChargingCharacteristics
					else if (mTagFieldCode.compareTo("97") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.ChargingCharacteristics += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					// chChSelectionMode value
					else if (mTagFieldCode.compareTo("98") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.chChSelectionMode += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (mTagFieldLength - i));
							mByteN++;
						}
					}
					// ServingNodePLMNIdentifier
					else if (mTagFieldCode.compareTo("9b") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.ServingNodePLMNIdentifier += Global.HexToTBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					// ServedIMEISV no value
					else if (mTagFieldCode.compareTo("9d") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.ServedIMEISV += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					// RATType
					else if (mTagFieldCode.compareTo("9e") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.RATType += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (mTagFieldLength - i));
							mByteN++;
						}
					}
					// MSTimeZone - value ""
					else if (mTagFieldCode.compareTo("9f1f") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.MSTimeZone += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					// UserLocationInformation - value ""
					else if (mTagFieldCode.compareTo("9f20") == 0) {
						
						mRet = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
						
						if (mRet != 191) {
							
							mByteN ++;
							if (structSamSung.MCCMNC.compareTo("") == 0) {
								for (int i = 1; i <= 3; i++) {
									structSamSung.UserLocationInformation += Global.HexToTBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
									mByteN++;
								}
							}
							
							String tmp = Global.rpad(Global.HexToBINARY(mRet), 8, "0");
							
							//System.out.println("---" + mRet + "(" + tmp + ")");
							
							// CGI
							if (tmp.charAt(7) == '1') {
								mRet = 0;
								for (int i = 1; i <= 2; i++) {
									mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (2 - i));
									mByteN++;
									
								}
								structSamSung.UserLocationInformation += " " + mRet;
								
								mRet = 0;
								for (int i = 1; i <= 2; i++) {
									mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (2 - i));
									mByteN++;
									
								}
								structSamSung.UserLocationInformation += " " + mRet;
							}
							
							// SAI
							if (tmp.charAt(6) == '1') {
								mRet = 0;
								for (int i = 1; i <= 2; i++) {
									mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (2 - i));
									mByteN++;
									
								}
								structSamSung.UserLocationInformation += " " + mRet;
								
								mRet = 0;
								for (int i = 1; i <= 2; i++) {
									mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (2 - i));
									mByteN++;
									
								}
								structSamSung.UserLocationInformation += " " + mRet;
							}
							
							// RAI
							if (tmp.charAt(5) == '1') {
								mRet = 0;
								for (int i = 1; i <= 2; i++) {
									mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (2 - i));
									mByteN++;
									
								}
								structSamSung.UserLocationInformation += " " + mRet;
								
								mRet = 0;
								for (int i = 1; i <= 2; i++) {
									mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (2 - i));
									mByteN++;
									
								}
								structSamSung.UserLocationInformation += " " + mRet;
							}
							
							// TAI
							if (tmp.charAt(4) == '1') {
								mRet = 0;
								for (int i = 1; i <= 2; i++) {
									mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (2 - i));
									mByteN++;
									
								}
								structSamSung.UserLocationInformation += " " + mRet;
							}
							
							// ECGI
							if (tmp.charAt(3) == '1') {
								mRet = 0;
								for (int i = 1; i <= 1; i++) {
									mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (2 - i));
									mByteN++;
									
								}
								structSamSung.UserLocationInformation += " " + mRet;
								
								mRet = 0;
								for (int i = 1; i <= 3; i++) {
									mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (2 - i));
									mByteN++;
									
								}
								structSamSung.UserLocationInformation += " " + mRet;
							}
						} else {
							mByteN += mTagFieldLength;
						}
					}
					// ServingNodeType - sequence 0a
					else if (mTagFieldCode.compareTo("bf23") == 0) {
						
						byteN = 0;
						while (byteN < mTagFieldLength) {
							miTagFieldCode = Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]));
							byteN++;
							
							miTagFieldLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]);
							byteN++;
							
							if (miTagFieldCode.compareTo("0a") == 0) {
								for(int i=1; i<=miTagFieldLength; i++) {
									structSamSung.ServingNodeType += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]));
									byteN++;
								}
							} else {
								byteN += miTagFieldLength;
							}
						}
						
						mByteN += mTagFieldLength;
					}
					// PGWAddressUsed
					else if (mTagFieldCode.compareTo("9f24") == 0) {
						byteN = 0;
						while (byteN < mTagFieldLength) {
							miTagFieldCode = Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]));
							byteN++;
							
							miTagFieldLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]);
							byteN++;
							
							for(int i=1; i<=miTagFieldLength; i++) {
								structSamSung.PGWAddressUsed += Global.fixSignedCharByte(mBuffer[mFileByte + mByteN + byteN]);
								byteN++;
							}
						}
						
						mByteN += mTagFieldLength;
					}
					// StartTime
					else if (mTagFieldCode.compareTo("9f26") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.StartTime += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					// StopTime
					else if (mTagFieldCode.compareTo("9f27") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.StopTime += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					// PDNConnectionID
					else if (mTagFieldCode.compareTo("9f28") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.PDNConnectionID += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (mTagFieldLength - i));
							mByteN++;
						}
					}
					// UserCSGInformation - sequence
					else if (mTagFieldCode.compareTo("bf2a") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							//structSamSung.UserCSGInformation += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					// ServedPDPPDNAddressExtension - no value
					else if (mTagFieldCode.compareTo("bf2b") == 0) {
						for (int i = 1; i <= mTagFieldLength; i++) {
							structSamSung.ServedPDPPDNAddressExtension += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					}
					else {
						mByteN += mTagFieldLength;
					}
				}
			}
			
			mFileByte += mByteN;
		}
	}
	
	
	public static void main(String[] args) throws SQLException {
		
		ConvertDevTest convertDev = new ConvertDevTest();
		convertDev.openConnection();
		convertDev.goitest();
		convertDev.closeConnection();
		
	}
	
	public void goitest() {
		
		ConvertSamSungLTE convertSamSung = new ConvertSamSungLTE();
		
		try {
			long startTime = System.currentTimeMillis();
			
			int mret = convertSamSung.convertSamSung("C:\\Users\\datnh\\Desktop\\Data\\CDRFILE\\SamSung", 
					"ChaData_20151026_1716_000001", 1, "C:\\Users\\datnh\\Desktop\\Data\\CDRFILE\\SamSung\\Out", "", 0, 2);
			
			System.out.println("mret=" + mret);
			
			long endTime   = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            System.out.println(totalTime);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
