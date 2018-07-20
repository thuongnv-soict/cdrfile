package cdrfile.thread;

/**
 * <p>Title: CDR File(s) System</p>
 * <p>Description: VMS IS Departerment</p>
 * <p>Copyright: VHC</p>
 * <p>Company: VietNam Mobile Telecom Services</p>
 * @author: VHC
 * @version: 1.0
 */

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.sql.SQLException;

import cdrfile.convert.StructGGSN;
import cdrfile.global.Global;
import cdrfile.global.IOUtils;
import cdrfile.global.TextFile;
import cdrfile.global.cdrfileParam;
import cdrfile.thread.ThreadInfo;

/**
 * Convert thread
 * 
 * @author VHC
 */
public class ConvertGGSNThread extends ThreadInfo implements Runnable {

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

	/**
	 * Convert GGSN file
	 * 
	 * @author VHC
	 * 
	 */
	class ConvertGGSN {
		
		/**
		 * Struct GGSN
		 */
		protected StructGGSN structGGSN = new StructGGSN();
		protected int  mRecPDP = 0;

		/**
		 * Convert eG format
		 * 
		 * @param pSourceConvert
		 *            : Thu muc file nguon
		 * @param pFileName
		 *            : Ten file
		 * @param pFileID
		 *            : fileId
		 * @param pDestinationConvert
		 *            : Thu muc file da convert
		 * @param pCurrent_dir
		 * @param pLocalSplitFilebyDay
		 * @param pCenterID
		 * @return
		 * @throws Exception
		 */
		protected int eGSN(String pSourceConvert, String pFileName,
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
			int mRecN = 0;
			
			/**
			 * Time
			 */
			String firstCallingTime = "";
            String lastCallingTime = "";
            
			try {
				Global.ExecuteSQL(mConnection, "alter session set nls_date_format='dd/mm/yyyy hh24:mi:ss'");

				// get path
				mSource = IOUtil.FillPath(pSourceConvert, Global.mSeparate)
						+ pFileName;

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
				/*if (cdrfileParam.ChargeCDRFile) {
					fileConvert.addText(Global.mGGSNHeaderCharge);
				} else {
					fileConvert.addText(Global.mGGSNHeaderNoCharge);
				}*/
				
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
				int fileLength = 0;
				
				int numOfCDRs = 0;		// number of CDRs
				int fileSequenceNumber = 0;
				int fileClosure = 0;
				String nodeIpAddress = "";
				int lostCDRIndicator = 0;
				int lengthOfCDRRoutingFilter = 0;

				// File length: 0 - 4
				while (mFileByte <= 3) {
					fileLength += Global.fixSignedByte(mBuffer[mFileByte])
							* Math.pow(256, (3 - mFileByte));
					mFileByte++;
				}

				// Header length: 5 - 8
//				while (mFileByte <= 7) {
//					headerLength += Global.fixSignedByte(mBuffer[mFileByte])
//							* Math.pow(256, (7 - mFileByte));
//					mFileByte++;
//				}
				
				// number of CDRs
				mFileByte = 18;
				while (mFileByte <= 21) {
					numOfCDRs += Global.fixSignedByte(mBuffer[mFileByte])
							* Math.pow(256, (21 - mFileByte));
					mFileByte++;
				}
				
				//System.out.println(numOfCDRs);
				
				// File sequence number: 23 - 26
				while (mFileByte <= 25) {
					fileSequenceNumber += Global
							.fixSignedByte(mBuffer[mFileByte])
							* Math.pow(256, (25 - mFileByte));
					mFileByte++;
				}

				// File closure: 27
				fileClosure = Global.fixSignedByte(mBuffer[mFileByte]);
				mFileByte++;

				// Node Ip address: 28 - 47
				while (mFileByte <= 46) {
					nodeIpAddress += Global.HexToTBCD(Global
							.fixSignedByte(mBuffer[mFileByte]));
					mFileByte++;
				}
				
				// lostCDR indicator: 48
				lostCDRIndicator = Global.fixSignedByte(mBuffer[mFileByte]);
				mFileByte++;
				
				// lengthOfCDRRoutingFilter: 49 - 50
				for (int i = 1; i <= 2; i++) {
					lengthOfCDRRoutingFilter += Global
							.fixSignedByte(mBuffer[mFileByte])
							* Math.pow(256, (2 - i));
					mFileByte++;
				}
				
				// Loi doc file
				if ((mLength != mFileLength) || (mLength != fileLength)) {
					
					mConnection.rollback(); // rollback
					mSQL = "UPDATE import_header SET status="
							+ Global.StateConvertedError
							+ ",note='Error read buffer at position: "
							+ mFileLength + "'  WHERE file_id = " + pFileID;
					Global.ExecuteSQL(mConnection, mSQL);
					mConnection.commit();
					writeLogFile("    - Error read buffer at position: "
							+ mFileLength);
					return (Global.ErrFileConverted);
				}
				
				//int num = 1;
				//while (num <= numOfCDRs) {
				while (mFileByte < mFileLength) {
					
					structGGSN.recordType = 0;
					structGGSN.servedIMSI = "";
					structGGSN.ggsnAddress = "";
					structGGSN.chargingID = 0;
					structGGSN.sgsnAddress = "";
					structGGSN.accessPointNameNI = "";
					structGGSN.pdpType = "";
					structGGSN.servedPDPAddress = "";
					structGGSN.dynamicAddressFlag = "";
					structGGSN.recordOpeningTime = "";
					structGGSN.duration = 0;
					structGGSN.causeForRecClosing = 0;
					structGGSN.diagnostics = "";
					structGGSN.recordSequenceNumber = 0;
					structGGSN.nodeID = "";
					//structGGSN.recordExtensions = "";
					structGGSN.localSequenceNumber = 0;
					structGGSN.apnSelectionMode = "";
					structGGSN.servedMSISDN = "";
					structGGSN.chargingCharacteristics = "";
					structGGSN.chChSelectionMode = "";
					structGGSN.sgsnPLMNIdentifier = "";
					//structGGSN.pSFurnishChargingInformation = "";
					structGGSN.servedIMEISV = "";
					structGGSN.rATType = 0;
					structGGSN.mSTimeZone = "";
					structGGSN.userLocationInformation = "";
					//structGGSN.datavolumeFBCUplink = 0;
					//structGGSN.datavolumeFBCDownlink = 0;
					
					structGGSN.sdRatingGroup = 0;
					structGGSN.sdChargingRuleBaseName = "";
					structGGSN.sdResultCode = 0;
					structGGSN.sdLocalSequenceNumber = 0;
					structGGSN.sdTimeOfFirstUsage = "";
					structGGSN.sdTimeOfLastUsage = "";
					structGGSN.sdTimeUsage = 0;
					structGGSN.sdServiceConditionChange = "";
					structGGSN.sdQoSInformationNeg = "";
					structGGSN.sdSgsnAddress = "";
					structGGSN.sdSgsnPLMNIdentifier = "";
					structGGSN.sdDatavolumeFBCUplink = 0;
					structGGSN.sdDatavolumeFBCDownlink = 0;
					structGGSN.sdTimeOfReport = "";
					structGGSN.sdRATType = 0;
					structGGSN.sdFailureHandlingContinue = 0;
					structGGSN.sdServiceIdentifier = 0;
					structGGSN.sdUserLocationInformation = "";
					
					structGGSN.causeForRecClosingValue = "";
					structGGSN.rATTypeValue = "";
					structGGSN.sdFailureHandlingContinueValue = "";
					structGGSN.diagnosticsValue = "";
					
					// Tag record length
					mRecLength = 0;
					for (int i = 1; i <= 2; i++) {
						mRecLength += Global.fixSignedByte(mBuffer[mFileByte])
								* Math.pow(256, (2 - i));
						mFileByte++;
					}
					
					// Version
					mFileByte += 2;
					
					// check tag length
					if (mFileByte + mRecLength > mFileLength) {
						mConnection.rollback();
						mSQL = "UPDATE import_header SET status=" + Global.StateConvertedError + ",note='Error read buffer at position: " + mFileByte + " - rec:" + mRecConvert + "'  WHERE file_id = " + pFileID;
						Global.ExecuteSQL(mConnection, mSQL);
						mConnection.commit();
						writeLogFile("    - Error read buffer at position: " + mFileByte + " - rec:" + mRecConvert);
						
						fileCDR.close();
						fileCDR = null;
						
						return (Global.ErrFileConverted);
					}
					
					// Tag record value
					int btTemp = Global.fixSignedByte(mBuffer[(mFileByte)]);
					btTemp &= 0x3f;
					btTemp &= 0x1f;
			
					mFileByte++;
					int miTagID = btTemp;
					
					switch (miTagID) {
					case 17: // eG-CDR format
						convertEGSNPDPRecord(fileConvert);
						analyseEGSNPDPRecord();
						//num++;
						mRecPDP ++;
						mRecN ++;
						break;
						
					default:
						break;
					}
					
					// analyse record
                    //analyseEGSNPDPRecord();
                    
                    // write to text file
                    if (cdrfileParam.ChargeCDRFile) {
						appendValue("" + structGGSN.recordType);
						appendValue(structGGSN.servedIMSI);
						appendValue(structGGSN.ggsnAddress);
						appendValue("" + structGGSN.chargingID);
						appendValue(structGGSN.sgsnAddress);
						appendValue(structGGSN.accessPointNameNI);
						appendValue(structGGSN.pdpType);
						appendValue(structGGSN.servedPDPAddress);
						appendValue(structGGSN.dynamicAddressFlag);
						appendValue(structGGSN.recordOpeningTime);
						appendValue("" + structGGSN.duration);
						appendValue(structGGSN.causeForRecClosingValue);
						appendValue(structGGSN.diagnosticsValue);
						appendValue("" + structGGSN.recordSequenceNumber);
						appendValue(structGGSN.nodeID);
						//appendValue(structGGSN.recordExtensions);
						appendValue("" + structGGSN.localSequenceNumber);
						appendValue(structGGSN.apnSelectionMode);
						appendValue(structGGSN.servedMSISDN);
						appendValue(structGGSN.chargingCharacteristics);
						appendValue(structGGSN.chChSelectionMode);
						appendValue(structGGSN.sgsnPLMNIdentifier);
						//appendValue(structGGSN.pSFurnishChargingInformation);
						appendValue(structGGSN.servedIMEISV);
						appendValue(structGGSN.rATTypeValue);
						appendValue(structGGSN.mSTimeZone);
						appendValue(structGGSN.userLocationInformation);
						//appendValue("" + structGGSN.datavolumeFBCUplink);
						//appendValue("" + structGGSN.datavolumeFBCDownlink);
						appendValue("" + structGGSN.sdRatingGroup);
						appendValue(structGGSN.sdChargingRuleBaseName);
						appendValue("" + structGGSN.sdResultCode);
						appendValue("" + structGGSN.sdLocalSequenceNumber);
						appendValue(structGGSN.sdTimeOfFirstUsage);
						appendValue(structGGSN.sdTimeOfLastUsage);
						appendValue("" + structGGSN.sdTimeUsage);
						appendValue(structGGSN.sdServiceConditionChange);
						appendValue(structGGSN.sdQoSInformationNeg);
						appendValue(structGGSN.sdSgsnAddress);
						appendValue(structGGSN.sdSgsnPLMNIdentifier);
						appendValue("" + structGGSN.sdDatavolumeFBCUplink);
						appendValue("" + structGGSN.sdDatavolumeFBCDownlink);
						appendValue(structGGSN.sdTimeOfReport);
						appendValue("" + structGGSN.sdRATType);
						appendValue(structGGSN.sdFailureHandlingContinueValue);
						appendValue("" + structGGSN.sdServiceIdentifier);
						appendValue(structGGSN.sdUserLocationInformation);
						
						fileConvert.addText(mStrValues);
						
						mStrValues = "";
					} else {
						mStrValues = "EGSNPDPRecord" + ";"
								+ structGGSN.recordType + ";"
								+ structGGSN.servedIMSI + ";"
								+ structGGSN.ggsnAddress + ";"
								+ structGGSN.chargingID + ";"
								+ structGGSN.sgsnAddress + ";"
								+ structGGSN.accessPointNameNI + ";"
								+ structGGSN.pdpType + ";"
								+ structGGSN.servedPDPAddress + ";"
								+ structGGSN.dynamicAddressFlag + ";"
								+ structGGSN.recordOpeningTime + ";"
								+ structGGSN.duration + ";"
								+ structGGSN.causeForRecClosingValue + ";"
								+ structGGSN.diagnosticsValue + ";"
								+ structGGSN.recordSequenceNumber + ";"
								+ structGGSN.nodeID + ";"
								//+ structGGSN.recordExtensions + ";"
								+ structGGSN.localSequenceNumber + ";"
								+ structGGSN.apnSelectionMode + ";"
								+ structGGSN.servedMSISDN + ";"
								+ structGGSN.chargingCharacteristics + ";"
								+ structGGSN.chChSelectionMode + ";"
								+ structGGSN.sgsnPLMNIdentifier + ";"
								//+ structGGSN.pSFurnishChargingInformation + ";"
								+ structGGSN.servedIMEISV + ";"
								+ structGGSN.rATTypeValue + ";"
								+ structGGSN.mSTimeZone + ";"
								+ structGGSN.userLocationInformation  + ";"
								//+ structGGSN.datavolumeFBCUplink  + ";"
								//+ structGGSN.datavolumeFBCDownlink;
								+ structGGSN.sdRatingGroup + ";"
								+ structGGSN.sdChargingRuleBaseName + ";"
								+ structGGSN.sdResultCode + ";"
								+ structGGSN.sdLocalSequenceNumber + ";"
								+ structGGSN.sdTimeOfFirstUsage + ";"
								+ structGGSN.sdTimeOfLastUsage + ";"
								+ structGGSN.sdTimeUsage + ";"
								+ structGGSN.sdServiceConditionChange + ";"
								+ structGGSN.sdQoSInformationNeg + ";"
								+ structGGSN.sdSgsnAddress + ";"
								+ structGGSN.sdSgsnPLMNIdentifier + ";"
								+ structGGSN.sdDatavolumeFBCUplink + ";"
								+ structGGSN.sdDatavolumeFBCDownlink + ";"
								+ structGGSN.sdTimeOfReport + ";"
								+ structGGSN.sdRATType + ";"
								+ structGGSN.sdFailureHandlingContinueValue + ";"
								+ structGGSN.sdServiceIdentifier + ";"
								+ structGGSN.sdUserLocationInformation
								+ ";;;;;;;;";
						
						fileConvert.addText(mStrValues);
                        mStrValues = "";
					}
				}
				
				System.out.println("mRecN: " + mRecN);
				
				writeLogFile("         - PDP               : " + Global.rpad(Integer.toString(mRecPDP), 6, " "));
				//writeLogFile("         - MM                : " + Global.rpad(Integer.toString(mRecMM), 6, " "));
				
				writeLogFile("      -------------------------------");
				writeLogFile("      Total record converted : " + Global.rpad(Integer.toString(mRecConvert), 6, " "));
				mRecN = mRecPDP;
				
				if (cdrfileParam.ChargeCDRFile) {
					mSQL = "UPDATE import_header SET time_end_convert=sysdate,status=" + Global.StateConverted + ",rec_total=" + mRecN + ",min_calling_time='" + lastCallingTime + "',max_calling_time='" + firstCallingTime + "' WHERE file_id = " + pFileID;
				} else {
					mSQL = "UPDATE import_header SET time_end_convert=sysdate,status=" + Global.StateRated + ",rec_total=" + mRecN + ",min_calling_time='" + lastCallingTime + "',max_calling_time='" + firstCallingTime + "' WHERE file_id = " + pFileID;
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
				Global.ExecuteSQL(mConnection, mSQL); // update file error
														// converted
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
	

		/**
		 * eG-CDR format
		 * @throws Exception 
		 */
		private int convertEGSNPDPRecord(TextFile fileConvert) throws Exception {
			int mRecN = 0;
			int mRet = 0;
			
			int mByteN = 0;
			while (mByteN < mRecLength - 1) {
				
				int btTemp = Global.fixSignedByte(mBuffer[(mFileByte + mByteN)]);
				btTemp &= 0x3f;
				byte mbtConstructed = (byte) ((btTemp & 0xe0) >>> 5);
				btTemp &= 0x1f;
		
				mByteN++;
				int miTagID = btTemp;
				
				if (miTagID >= 31) {
					miTagID = 0;
					boolean bfound = false;
					int tmp;
					while (!bfound) {
						tmp = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
						mByteN++;
						bfound = tmp >>> 7 == 0;
						tmp &= 0x7f;
						miTagID |= tmp;
					}
				}
		
				// length of tag
				int length = 0;
				int bOfLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
		
				mByteN++;
				if (bOfLength > 128) {
					bOfLength = bOfLength - 128;
					for (int i = 1; i <= bOfLength; i++) {
						length += Global.fixSignedByte(mBuffer[mFileByte + mByteN])
								* Math.pow(256, (bOfLength - i));
						mByteN++;
					}
		
				} else {
					length = bOfLength;
				}
		
				switch (miTagID) {
				case 0: // recordType
					for (int i = 1; i <= length; i++) {
						structGGSN.recordType += Global.fixSignedByte(mBuffer[mFileByte + mByteN])
								* Math.pow(256, (length - i));
						mByteN++;
					}
					
					break;
				case 3: // servedIMSI
					for (int i = 1; i <= length; i++) {
						structGGSN.servedIMSI += Global.HexToTBCD(Global
								.fixSignedByte(mBuffer[mFileByte + mByteN]));
						mByteN++;
					}
					
					break;
				case 4: // ggsnAddress
					if (mbtConstructed == 0) {
						for (int i = 1; i <= length; i++) {
							structGGSN.ggsnAddress += Global.HexToBCD(Global
									.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					} else {
						int bt = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
						bt &= 0x3f;
						bt &= 0x1f;
						miTagID = bt;
		
						mByteN++;
						if (miTagID >= 31) {
							miTagID = 0;
							boolean bfound = false;
							while (!bfound) {
								int tmp = Global.fixSignedByte(mBuffer[mFileByte
										+ mByteN]);
								mByteN++;
								bfound = tmp >>> 7 == 0;
								tmp &= 0x7f;
								miTagID |= tmp;
							}
						}
		
						// length
						int len = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
						mByteN++;
						if (len == 4)
							for (int i = 1; i <= len; i++) {
								if (structGGSN.ggsnAddress.length() > 0)
									structGGSN.ggsnAddress += ".";
								structGGSN.ggsnAddress += Global.fixSignedByte(mBuffer[mFileByte
										+ mByteN]);
								mByteN++;
							}
						else {
							for (int i = 1; i <= len; i++) {
								structGGSN.ggsnAddress += Global.fixSignedByte(mBuffer[mFileByte
										+ mByteN]);
								mByteN++;
							}
						}
					}
		
					break;
				case 5: // chargingID
					for (int i = 1; i <= length; i++) {
						structGGSN.chargingID += Global.fixSignedByte(mBuffer[mFileByte + mByteN])
								* Math.pow(256, (length - i));
						mByteN++;
					}
					
					break;
				case 6: // sgsnAddress
					if (mbtConstructed == 0) {
						for (int i = 1; i <= length; i++) {
							structGGSN.sgsnAddress += Global.HexToBCD(Global
									.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					} else {
						int bt = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
						bt &= 0x3f;
						bt &= 0x1f;
						miTagID = bt;
		
						mByteN++;
						if (miTagID >= 31) {
							miTagID = 0;
							boolean bfound = false;
							while (!bfound) {
								int tmp = Global.fixSignedByte(mBuffer[mFileByte
										+ mByteN]);
								mByteN++;
								bfound = tmp >>> 7 == 0;
								tmp &= 0x7f;
								miTagID |= tmp;
							}
						}
		
						// length
						int len = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
						mByteN++;
						if (len == 4)
							for (int i = 1; i <= len; i++) {
								if (structGGSN.sgsnAddress.length() > 0)
									structGGSN.sgsnAddress += ".";
								structGGSN.sgsnAddress += Global.fixSignedByte(mBuffer[mFileByte
										+ mByteN]);
								mByteN++;
							}
						else {
							for (int i = 1; i <= len; i++) {
								structGGSN.sgsnAddress += Global.fixSignedByte(mBuffer[mFileByte
										+ mByteN]);
								mByteN++;
							}
						}
					}
		
					break;
				case 7: // accessPointNameNI
		
					for (int i = 1; i <= length; i++) {
						structGGSN.accessPointNameNI += Global.fixSignedCharByte(Global
								.fixSignedByte(mBuffer[mFileByte + mByteN]));
						mByteN++;
					}
		
					break;
				case 8: // pdpType
		
					for (int i = 1; i <= length; i++) {
						structGGSN.pdpType += Global.HexToTBCD(Global
								.fixSignedByte(mBuffer[mFileByte + mByteN]));
						mByteN++;
					}
		
					break;
				case 9: // servedPDPAddress
					
					if (mbtConstructed == 0) {
						for (int i = 1; i <= length; i++) {
							structGGSN.servedPDPAddress += Global.HexToBCD(Global
									.fixSignedByte(mBuffer[mFileByte + mByteN]));
							mByteN++;
						}
					} else {
						int len;
						byte constructed = 1;
						do {
							int bt = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
							bt &= 0x3f;
							constructed = (byte) ((bt & 0xe0) >>> 5);
							bt &= 0x1f;
							miTagID = bt;
		
							mByteN++;
							if (miTagID >= 31) {
								miTagID = 0;
								boolean bfound = false;
								while (!bfound) {
									int tmp = Global.fixSignedByte(mBuffer[mFileByte
											+ mByteN]);
									mByteN++;
									bfound = tmp >>> 7 == 0;
									tmp &= 0x7f;
									miTagID |= tmp;
								}
							}
							// length
							len = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
							mByteN++;
						} while (constructed == 1);
						
						if (len == 4) {
							for (int i = 1; i <= len; i++) {
								if (structGGSN.servedPDPAddress.length() > 0)
									structGGSN.servedPDPAddress += ".";
								structGGSN.servedPDPAddress += Global
										.fixSignedByte(mBuffer[mFileByte + mByteN]);
								mByteN++;
							}
						} else {
							for (int i = 1; i <= len; i++) {
								structGGSN.servedPDPAddress += Global
										.fixSignedByte(mBuffer[mFileByte + mByteN]);
								mByteN++;
							}
						}
					}
		
					break;
				case 11:// dynamicAddressFlag
					for (int i = 1; i <= length; i++) {
						structGGSN.dynamicAddressFlag += Global.HexToBCD(Global
								.fixSignedByte(mBuffer[mFileByte + mByteN]));
						mByteN++;
					}
		
					break;
				case 13: // recordOpeningTime
					for (int i = 1; i <= length; i++) {
						if (i == 7)
							structGGSN.recordOpeningTime += Global
									.fixSignedCharByte(mBuffer[mFileByte + mByteN]);
						else
							structGGSN.recordOpeningTime += Global.HexToBCD(Global
									.fixSignedByte(mBuffer[mFileByte + mByteN]));
						mByteN++;
					}
		
					break;
				case 14: // duration
					for (int i = 1; i <= length; i++) {
						structGGSN.duration += Global.fixSignedByte(mBuffer[mFileByte + mByteN])
								* Math.pow(256, (length - i));
						mByteN++;
					}
		
					break;
				case 15: // causeForRecClosing
					for (int i = 1; i <= length; i++) {
						structGGSN.causeForRecClosing += Global.fixSignedByte(mBuffer[mFileByte
								+ mByteN])
								* Math.pow(256, (length - i));
						mByteN++;
					}
		
					break;
				case 16:// diagnostics
					for (int i = 1; i <= length; i++) {
						//structGGSN.diagnostics += Global.fixSignedByte(mBuffer[mFileByte + mByteN])* Math.pow(256, (length - i));
						structGGSN.diagnostics += Global.fixSignedByte(mBuffer[mFileByte+ mByteN]);
						mByteN++;
					}
		
					break;
				case 17:// recordSequenceNumber
					for (int i = 1; i <= length; i++) {
						structGGSN.recordSequenceNumber += Global.fixSignedByte(mBuffer[mFileByte
								+ mByteN])
								* Math.pow(256, (length - i));
						mByteN++;
					}
		
					break;
				case 18:// nodeId
					for (int i = 1; i <= length; i++) {
//						structGGSN.nodeID += Global.HexToBCD(Global
//								.fixSignedByte(mBuffer[mFileByte + mByteN]));
						structGGSN.nodeID += Global.fixSignedCharByte(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
						mByteN++;
					}
		
					break;
				/*case 19:// recordExtensions
					for (int i = 1; i <= length; i++) {
						structGGSN.recordExtensions += Global.HexToBCD(Global
								.fixSignedByte(mBuffer[mFileByte + mByteN]));
						mByteN++;
					}
		
					break;*/
				case 19:// recordExtensions
					mByteN += length;
		
					break;
				case 20:// localSequenceNumber
					for (int i = 1; i <= length; i++) {
						structGGSN.localSequenceNumber += Global.fixSignedByte(mBuffer[mFileByte
								+ mByteN])
								* Math.pow(256, (length - i));
						mByteN++;
					}
		
					break;
				case 21:// apnSelectionMode
					for (int i = 1; i <= length; i++) {
						structGGSN.apnSelectionMode += Global.HexToTBCD(Global
								.fixSignedByte(mBuffer[mFileByte + mByteN]));
						mByteN++;
					}
		
					break;
				case 22:// servedMSISDN
					for (int i = 1; i <= length; i++) {
						structGGSN.servedMSISDN += Global.HexToTBCD(Global
								.fixSignedByte(mBuffer[mFileByte + mByteN]));
						mByteN++;
					}
		
					break;
				case 23:// chargingCharacteristics
					for (int i = 1; i <= length; i++) {
						structGGSN.chargingCharacteristics += Global.HexToBCD(Global
								.fixSignedByte(mBuffer[mFileByte + mByteN]));
						mByteN++;
					}
		
					break;
				case 24:// chChSelectionMode
					for (int i = 1; i <= length; i++) {
						structGGSN.chChSelectionMode += Global.HexToBCD(Global
								.fixSignedByte(mBuffer[mFileByte + mByteN]));
						mByteN++;
					}
		
					break;
				case 27: // sgsnPLMNIdentifier
					for (int i = 1; i <= length; i++) {
						//structGGSN.sgsnPLMNIdentifier += Global.fixSignedByte(mBuffer[mFileByte
						//		+ mByteN])
						//		* Math.pow(256, (length - i));
						structGGSN.sgsnPLMNIdentifier += Global.HexToTBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
						mByteN++;
					}
		
					break;
				/*case 28: // pSFurnishChargingInformation
					for (int i = 1; i <= length; i++) {
						structGGSN.pSFurnishChargingInformation += Global.HexToBCD(Global
								.fixSignedByte(mBuffer[mFileByte + mByteN]));
						mByteN++;
					}
					
					break;*/
				case 28:// pSFurnishChargingInformation
					mByteN += length;
		
					break;
				case 29: // servedIMEISV
					for (int i = 1; i <= length; i++) {
						structGGSN.servedIMEISV += Global.HexToTBCD(Global
								.fixSignedByte(mBuffer[mFileByte + mByteN]));
						mByteN++;
					}
		
					break;
				case 30:// rATType
					for (int i = 1; i <= length; i++) {
						structGGSN.rATType += Global.fixSignedByte(mBuffer[mFileByte + mByteN])
								* Math.pow(256, (length - i));
						mByteN++;
					}
		
					break;
				case 31: // mSTimeZone
					for (int i = 1; i <= length; i++) {
						//structGGSN.mSTimeZone += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
						
						structGGSN.mSTimeZone += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) + " ";
						
						mByteN++;
					}
		
					break;
				case 32: // userLocationInformation
					/*for (int i = 1; i <= length; i++) {
						structGGSN.userLocationInformation += Global.HexToBCD(Global
								.fixSignedByte(mBuffer[mFileByte + mByteN]));
						mByteN++;
					}*/
					mRet = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
					if (mRet == 1) {
						structGGSN.userLocationInformation = "SAI ";
					} else {
						structGGSN.userLocationInformation = "CGI ";
					}
					
					mByteN++;
					
					for (int i = 1; i <= 3; i++) {
						structGGSN.userLocationInformation += Global.HexToTBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
						mByteN++;
					}
					mRet = 0;
					for (int i = 1; i <= 2; i++) {
						mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (2 - i));
						mByteN++;
						
					}
					structGGSN.userLocationInformation += " " + mRet;
					
					mRet = 0;
					for (int i = 1; i <= 2; i++) {
						mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (2 - i));
						mByteN++;
						
					}
					structGGSN.userLocationInformation += " " + mRet;
		
					break;
				case 34: // listOfServiceData
					int mRecServiceData = 0;
					int tagLength = 0;
					int dup = 0;
					while (tagLength < (length - mRecServiceData * 3)) {
						
						dup ++;
						//structGGSN.sdRatingGroup = 0;
						
						
						/*String chargingRuleBaseName = "";
						int resultCode = 0;
						long localSequenceNumber = 0;
						String timeOfFirstUsage = "";
						String timeOfLastUsage = "";
						long timeUsage = 0;
						String serviceConditionChange = "";
						String qoSInformationNeg = "";
						String sgsnAddress = "";
						int sGSNPLMNIdentifier = 0;
						String timeOfReport = "";
						int rATType = 0;
						int serviceIdentifier = 0;
						String userLocationInformation = "";
						int failureHandlingContinue = 0;*/
						
						int bt = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
						//int tagClass = (byte) ((bt & 0xc0) >>> 6);
						bt &= 0x3f;
						//byte constructed = (byte) ((bt & 0xe0) >>> 5);
						bt &= 0x1f;
						miTagID = bt;
						
						mByteN++;
						if (miTagID >= 31) {
						    miTagID = 0;
						    boolean bfound = false;
						    while (!bfound) {
						        int tmp = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
						        mByteN++;
						        bfound = tmp >>> 7 == 0;
						        tmp &= 0x7f;
						        miTagID |= tmp;
						    }
						}
						
						int bOfLen = 0;
	                    int len = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
	                    mByteN++;
						if (len > 128) {
							len = len - 128;
							for (int i = 1; i <= len; i++) {
								bOfLen += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (len - i));
								mByteN++;
							}
						
						} else{
							bOfLen = len;
						}
						
						int byteN = 0;
						while (byteN < bOfLen) {
							bt = Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]);
							//tagClass = (byte) ((bt & 0xc0) >>> 6);
							bt &= 0x3f;
							int constructed = (byte) ((bt & 0xe0) >>> 5);
							bt &= 0x1f;
							miTagID = bt;
							
							byteN++;
							if (miTagID >= 31) {
								miTagID = 0;
								boolean bfound = false;
								while (!bfound) {
									int tmp = Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]);
									byteN++;
									bfound = tmp >>> 7 == 0;
									tmp &= 0x7f;
									miTagID |= tmp;
								}
							}
							
							int lngth = Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]);
							byteN++;
							
							switch (miTagID) {
							case 1: //ratingGroup
								for (int i = 1; i <= lngth; i++) {
									if (dup == 1)
										structGGSN.sdRatingGroup += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]) * Math.pow(256, (lngth - i));
									byteN++;
								}
								
								break;
							case 2: //chargingRuleBaseName
								for (int i = 1; i <= lngth; i++) {
									if (dup == 1)
										structGGSN.sdChargingRuleBaseName += Global.fixSignedCharByte(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]));
									byteN++;
								}
								
								break;
							case 3: //resultCode
								for (int i = 1; i <= lngth; i++) {
									if (dup == 1)
										structGGSN.sdResultCode += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]) * Math.pow(256, (lngth - i));
									byteN++;
								}
								
								break;
							case 4: //localSequenceNumber
								for (int i = 1; i <= lngth; i++) {
									if (dup == 1)
										structGGSN.sdLocalSequenceNumber += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]) * Math.pow(256, (lngth - i));
									byteN++;
								}
								
								break;
							case 5: //timeOfFirstUsage
								for (int i = 1; i <= lngth; i++) {
									if (i == 7)
										structGGSN.sdTimeOfFirstUsage += Global.fixSignedCharByte(mBuffer[mFileByte + mByteN + byteN]);
									else
										structGGSN.sdTimeOfFirstUsage += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]));
									byteN++;
								}
								
								break;
							case 6: //timeOfLastUsage
								for (int i = 1; i <= lngth; i++) {
									if (i == 7)
										structGGSN.sdTimeOfLastUsage += Global.fixSignedCharByte(mBuffer[mFileByte + mByteN + byteN]);
									else
										structGGSN.sdTimeOfLastUsage += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]));
									byteN++;
								}
								
								break;
							case 7: //timeUsage
								for (int i = 1; i <= lngth; i++) {
									structGGSN.sdTimeUsage += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]) * Math.pow(256, (lngth - i));
									byteN++;
								}
								
								break;
							case 8: //serviceConditionChange
								for (int i = 1; i <= lngth; i++) {
									if (dup == 1)
										structGGSN.sdServiceConditionChange += Global.HexToBINARY(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]));
									byteN++;
								}
								
								break;
							case 9: //qoSInformationNeg
								for (int i = 1; i <= lngth; i++) {
									if (dup == 1 && i <= 8)
										//structGGSN.sdQoSInformationNeg += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]));
										structGGSN.sdQoSInformationNeg += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]) + " ";
									byteN++;
								}
								
								break;
							case 10: //sgsnAddress
								if (constructed == 0) {
									for (int i = 1; i <= lngth; i++) {
										if (dup == 1)
											structGGSN.sdSgsnAddress += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]));
										byteN++;
									}
								} else {
									bt = Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]);
									bt &= 0x3f;
									bt &= 0x1f;
									miTagID = bt;
					
									byteN++;
									if (miTagID >= 31) {
										miTagID = 0;
										boolean bfound = false;
										while (!bfound) {
											int tmp = Global.fixSignedByte(mBuffer[mFileByte
													+ mByteN + byteN]);
											byteN++;
											bfound = tmp >>> 7 == 0;
											tmp &= 0x7f;
											miTagID |= tmp;
										}
									}
					
									// length
									int lenSsgn = Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]);
									byteN++;
									if (lenSsgn == 4)
										for (int i = 1; i <= lenSsgn; i++) {
											if (dup == 1) {
												if (structGGSN.sdSgsnAddress.length() > 0)
													structGGSN.sdSgsnAddress += ".";
												structGGSN.sdSgsnAddress += Global.fixSignedByte(mBuffer[mFileByte
														+ mByteN + byteN]);
											}
											byteN++;
										}
									else {
										for (int i = 1; i <= lenSsgn; i++) {
											if (dup == 1)
												structGGSN.sdSgsnAddress += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]);
											byteN++;
										}
									}
								}
								
								break;
							case 11: //sdSgsnPLMNIdentifier
								for (int i = 1; i <= lngth; i++) {
									if (dup == 1)
										structGGSN.sdSgsnPLMNIdentifier += Global.HexToTBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]));
									
									//structGGSN.sdSgsnPLMNIdentifier += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]) * Math.pow(256, (lngth - i));
									byteN++;
								}
								
								break;
							case 12: //datavolumeFBCUplink
								for (int i = 1; i <= lngth; i++) {
									structGGSN.sdDatavolumeFBCUplink += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]) * Math.pow(256, (lngth - i));
									byteN++;
								}
								
								break;
							case 13: //datavolumeFBCDownlink
								for (int i = 1; i <= lngth; i++) {
									structGGSN.sdDatavolumeFBCDownlink += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]) * Math.pow(256, (lngth - i));
									byteN++;
								}
								
								break;
							/*case 12: //datavolumeFBCUplink
								if (ratingGroup == 0) {
									for (int i = 1; i <= lngth; i++) {
										structGGSN.datavolumeFBCUplink += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]) * Math.pow(256, (lngth - i));
										byteN++;
									}
								} else {
									byteN += lngth;
								}
								
								//System.out.println("[datavolumeFBCUplink] = " + structGGSN.datavolumeFBCUplink);
								
								break;
							case 13: //datavolumeFBCDownlink
								if (ratingGroup == 0) {
									for (int i = 1; i <= lngth; i++) {
										structGGSN.datavolumeFBCDownlink += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]) * Math.pow(256, (lngth - i));
										byteN++;
									}
								} else {
									byteN += lngth;
								}
								
								//System.out.println("[datavolumeFBCDownlink] = " + structGGSN.datavolumeFBCDownlink);
								
								break;*/
							case 14: //timeOfReport
								for (int i = 1; i <= lngth; i++) {
									if (i == 7)
										structGGSN.sdTimeOfReport += Global.fixSignedCharByte(mBuffer[mFileByte + mByteN + byteN]);
									else
										structGGSN.sdTimeOfReport += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]));
									byteN++;
								}
								
								break;
							case 15: //rATType
								for (int i = 1; i <= lngth; i++) {
									if (dup == 1)
										structGGSN.sdRATType += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]) * Math.pow(256, (lngth - i));
									byteN++;
								}
								
								break;
							case 16: //failureHandlingContinue
								for (int i = 1; i <= lngth; i++) {
									structGGSN.sdFailureHandlingContinue += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]) * Math.pow(256, (lngth - i));
									byteN++;
								}
								
								break;
							case 17: //serviceIdentifier
								for (int i = 1; i <= lngth; i++) {
									if (dup == 1)
										structGGSN.sdServiceIdentifier += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]) * Math.pow(256, (lngth - i));
									byteN++;
								}
								
								break;
							case 20: //userLocationInformation
								if (dup == 1) {
									mRet = Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]);
									if (mRet == 1) {
										structGGSN.sdUserLocationInformation = "SAI ";
									} else {
										structGGSN.sdUserLocationInformation = "CGI ";
									}
									
									byteN++;
									
									for (int i = 1; i <= 3; i++) {
										structGGSN.sdUserLocationInformation += Global.HexToTBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]));
										byteN++;
									}
									mRet = 0;
									for (int i = 1; i <= 2; i++) {
										mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]) * Math.pow(256, (2 - i));
										byteN++;
										
									}
									structGGSN.sdUserLocationInformation += " " + mRet;
									
									mRet = 0;
									for (int i = 1; i <= 2; i++) {
										mRet += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]) * Math.pow(256, (2 - i));
										byteN++;
										
									}
									structGGSN.sdUserLocationInformation += " " + mRet;
								} else {
									byteN += lngth;
								}
								
								break;
							default:
								/*int value = 0;
								for (int i = 1; i <= lngth; i++) {
									value += Global.fixSignedByte(mBuffer[mFileByte + mByteN + byteN]) * Math.pow(256, (lngth - i));
									byteN++;
								}
								
								System.out.println("[" + miTagID + "] = " + value);*/
								byteN += lngth;
								
								break;
							}
						}
	                    
						tagLength += bOfLen;
	                    mByteN += bOfLen;
	                    
	                    mRecServiceData ++;
					}
					//mByteN += length;
					
	                break;
				default: // default
					mByteN++;
				}
			}
			
			mFileByte += mByteN;
			
			return mRecN;
		}
		
		/**
		 * 
		 */
		private void analyseEGSNPDPRecord () {
			
			//recordOpeningTime
			if (!structGGSN.recordOpeningTime.trim().contains(" ")) {
				if (structGGSN.recordOpeningTime.length()>=5) {
					structGGSN.recordOpeningTime = structGGSN.recordOpeningTime.substring(0, structGGSN.recordOpeningTime.length() - 5);
					structGGSN.recordOpeningTime = Global.Format(structGGSN.recordOpeningTime,"yyMMddHHmmss","E MMM dd HH:mm:ss yyyy");
				}
			}
			
			//sdTimeOfFirstUsage
			if (structGGSN.sdTimeOfFirstUsage.length()>=5) {
				structGGSN.sdTimeOfFirstUsage = structGGSN.sdTimeOfFirstUsage.substring(0, structGGSN.sdTimeOfFirstUsage.length() - 5);
				structGGSN.sdTimeOfFirstUsage = Global.Format(structGGSN.sdTimeOfFirstUsage,"yyMMddHHmmss","E MMM dd HH:mm:ss yyyy");
			}
			
			//sdTimeOfLastUsage
			if (structGGSN.sdTimeOfLastUsage.length()>=5) {
				structGGSN.sdTimeOfLastUsage = structGGSN.sdTimeOfLastUsage.substring(0, structGGSN.sdTimeOfLastUsage.length() - 5);
				structGGSN.sdTimeOfLastUsage = Global.Format(structGGSN.sdTimeOfLastUsage,"yyMMddHHmmss","E MMM dd HH:mm:ss yyyy");
			}
			
			//sdTimeOfReport
			if (structGGSN.sdTimeOfReport.length()>=5) {
				structGGSN.sdTimeOfReport = structGGSN.sdTimeOfReport.substring(0, structGGSN.sdTimeOfReport.length() - 5);
				structGGSN.sdTimeOfReport = Global.Format(structGGSN.sdTimeOfReport,"yyMMddHHmmss","E MMM dd HH:mm:ss yyyy");
			}
			
			if (structGGSN.servedIMSI.endsWith("f")) {
				structGGSN.servedIMSI = structGGSN.servedIMSI.substring(0, structGGSN.servedIMSI.length() - 1);
			}
			
			if (structGGSN.servedIMSI.length() >= 5) {
				structGGSN.servedIMSI = structGGSN.servedIMSI.substring(0, 3) + " " + structGGSN.servedIMSI.substring(3, 5) + " " + structGGSN.servedIMSI.substring(5);
			}
			
			//sdSGSNPLMNIdentifier
			if (structGGSN.sdSgsnPLMNIdentifier.contains("f")) {
				structGGSN.sdSgsnPLMNIdentifier = structGGSN.sdSgsnPLMNIdentifier.replace("f", " ");
			}
			
			//sGSNPLMNIdentifier
			if (structGGSN.sgsnPLMNIdentifier.contains("f")) {
				structGGSN.sgsnPLMNIdentifier = structGGSN.sgsnPLMNIdentifier.replace("f", " ");
			}
			
			if (structGGSN.userLocationInformation.contains("f")) {
				structGGSN.userLocationInformation = structGGSN.userLocationInformation.replace("f", " ");
			}
			
			if (structGGSN.sdUserLocationInformation.contains("f")) {
				structGGSN.sdUserLocationInformation = structGGSN.sdUserLocationInformation.replace("f", " ");
			}
			
			/*if (structGGSN.servedIMEISV.endsWith("f")) {
				structGGSN.servedIMEISV = structGGSN.servedIMEISV.substring(0, structGGSN.servedIMEISV.length() - 1);
			}*/
			
			if (structGGSN.servedMSISDN.length() > 0) {
				if (structGGSN.servedMSISDN.substring(0, 2).compareTo("19") == 0) {
					structGGSN.servedMSISDN = structGGSN.servedMSISDN.substring(2);
					
					/*if (structGGSN.servedMSISDN.substring(0, 2).compareTo("84") == 0) {
						structGGSN.servedMSISDN = structGGSN.servedMSISDN.substring(2);
					}*/
				}
				
				if (structGGSN.servedMSISDN.endsWith("f")) {
					structGGSN.servedMSISDN = structGGSN.servedMSISDN.substring(0, structGGSN.servedMSISDN.length() - 1);
				}
			}
			
			if (structGGSN.pdpType.contains("f")) {
				structGGSN.pdpType = structGGSN.pdpType.replace("f", "");
			}
			
			if (structGGSN.pdpType.equals("1012")) {
				structGGSN.pdpType = "IETF";
			}
			
			if (structGGSN.dynamicAddressFlag.equals("ff")) {
				structGGSN.dynamicAddressFlag = "TRUE";
			}
			
			// causeForRecClosing
			switch (structGGSN.causeForRecClosing) {
			case 0:
				structGGSN.causeForRecClosingValue = "Normal release";
				break;
			case 4:
				structGGSN.causeForRecClosingValue = "Abnormal release";
				break;
			case 16:
				structGGSN.causeForRecClosingValue = "Volume limit";
				break;
			case 17:
				structGGSN.causeForRecClosingValue = "Time limit";
				break;
			case 18:
				structGGSN.causeForRecClosingValue = "SGSN change";
				break;
			case 19:
				structGGSN.causeForRecClosingValue = "Max change condition";
				break;
			case 22:
				structGGSN.causeForRecClosingValue = "RAT change";
				break;
			case 23:
				structGGSN.causeForRecClosingValue = "MS time zone change";
				break;
			case 24:
				structGGSN.causeForRecClosingValue = "SGSN PLMN ID change";
				break;
			default:
				break;
			}
			
			// chargingCharacteristics
			if (structGGSN.chargingCharacteristics.equals("0400")) {
				structGGSN.chargingCharacteristics = "PREPAID";
			} else if (structGGSN.chargingCharacteristics.equals("0800")) {
				structGGSN.chargingCharacteristics = "POSTPAID";
			}
			
			// chChSelectionMode
			if (structGGSN.chChSelectionMode.equals("00")) {
				structGGSN.chChSelectionMode = "SGSN-supplied";
			} else if (structGGSN.chChSelectionMode.equals("03")) {
				structGGSN.chChSelectionMode = "home default";
			}
			
			// rATType
			/*1: UTRAN
			2: GERAN
			3: WLAN
			4: GAN
			5: HSPA evolution*/
			switch (structGGSN.rATType) {
			case 1:
				structGGSN.rATTypeValue = "UTRAN";
				break;
			case 2:
				structGGSN.rATTypeValue = "GERAN";
				break;
			case 3:
				structGGSN.rATTypeValue = "WLAN";
				break;
			case 4:
				structGGSN.rATTypeValue = "GAN";
				break;
			case 5:
				structGGSN.rATTypeValue = "HSPA evolution";
				break;
			default:
				break;
			}
			
			// sdFailureHandlingContinue
			switch (structGGSN.sdFailureHandlingContinue) {
			case 255:
				structGGSN.sdFailureHandlingContinueValue = "TRUE";
				break;
			default:
				break;
			}
			
			// apnSelectionMode
			if (structGGSN.apnSelectionMode.length() == 2 && structGGSN.apnSelectionMode.endsWith("0")) {
				structGGSN.apnSelectionMode = structGGSN.apnSelectionMode.substring(0, 1);
			}
			
			 /*• 8: Normal bearer release (deletion requested)
			 • 36: Session timer expired
			 • 37: Idle session timer expired
			 • 57: RADIUS disconnect
			 • 70: OCS disconnected
			Supported abnormal bearer release values:
			 • 11: SGSN unreachable
			 • 30: Error Indication received from SGSN
			 • 35: Flexi NG configuration changed
			 • 42: Update PDP Context Request to SGSN failed
			 • 56: PDP context not found
			 • 71: TEID conflict: SGSN assigned the bearer’s user plane 
			TEID to another bearer
			 • 100: OCS was unreachable due to lack of transport connec-tion
			 • 101: OCS did not respond within time allowed
			 • 102: OCS sent a result code in the CCA indicating an error
			 • 103: OCS sent an incomprehensible CCA
			 • 105: OCS failure handling - continue action timeout*/
			if (structGGSN.diagnostics.length() >= 4) {
				int tmp = Integer.parseInt(structGGSN.diagnostics.substring(4));
				
				switch (tmp) {
				case 8:
					structGGSN.diagnosticsValue = "Normal bearer release";
					break;
				case 36:
					structGGSN.diagnosticsValue = "Session timer expired";
					break;
				case 37:
					structGGSN.diagnosticsValue = "Idle session timer expired";
					break;
				case 57:
					structGGSN.diagnosticsValue = "RADIUS disconnect";
					break;
				case 70:
					structGGSN.diagnosticsValue = "OCS disconnected";
					break;
				case 11:
					structGGSN.diagnosticsValue = "SGSN unreachable";
					break;
				case 30:
					structGGSN.diagnosticsValue = "Error Indication received from SGSN";
					break;
				case 35:
					structGGSN.diagnosticsValue = "Flexi NG configuration changed";
					break;
				case 42:
					structGGSN.diagnosticsValue = "Update PDP Context Request to SGSN failed";
					break;
				case 56:
					structGGSN.diagnosticsValue = "PDP context not found";
					break;
				case 71:
					structGGSN.diagnosticsValue = "TEID conflict";
					break;
				case 100:
					structGGSN.diagnosticsValue = "OCS was unreachable due to lack of transport connection";
					break;
				case 101:
					structGGSN.diagnosticsValue = "OCS did not respond within time allowed";
					break;
				case 102:
					structGGSN.diagnosticsValue = "OCS sent a result code in the CCA indicating an error";
					break;
				case 103:
					structGGSN.diagnosticsValue = "OCS sent an incomprehensible CCA";
					break;
				case 105:
					structGGSN.diagnosticsValue = "OCS failure handling - continue action timeout";
					break;
				default:
					break;
				}
			}
			
			// servedIMEISV
			try {
				structGGSN.servedIMEISV = structGGSN.servedIMEISV.substring(0, 6) + " " + structGGSN.servedIMEISV.substring(6, 8) + " " 
						+ structGGSN.servedIMEISV.substring(8, 14) + " " + structGGSN.servedIMEISV.substring(14);
			} catch (Exception e) {
				//System.out.println(e.toString());
			}
			
			structGGSN.mSTimeZone = structGGSN.mSTimeZone.trim();
			structGGSN.sdQoSInformationNeg = structGGSN.sdQoSInformationNeg.trim();
		}
	}
	
	private void goitest() throws Exception {
		ConvertGGSN cvGGSN = new ConvertGGSN();

		try {
			//cvGGSN.eGSN("/home/datnh/Desktop", "RawData_20140602_01153", 1, "/home/datnh/Desktop/Out", "", 0, 2);
			
			int mret = cvGGSN.eGSN("C:\\Users\\datnh\\Desktop\\Data\\CDRFILE\\GGSN", "FNGHCM_1N_-_0000012045.20131014_-_1541+0700", 1,
					"C:\\Users\\datnh\\Desktop\\Data\\CDRFILE\\GGSN\\Out", "", 0, 2);
			
			System.out.println("mret=" + mret);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void convertFiles(String path, String fileName, String outPath)
    {
        ConvertGGSN cvGGSN = new ConvertGGSN();

        try
        {
            //Duong dan file out.
//        int mret = cvIN.INFile_PPS421(path, fileName, 1, "C:/Documents and Settings/do dinh quang/Desktop/cdrfileINConcvertTool(16-9)/Out", "", 0, 2);
            int mret = cvGGSN.eGSN(path, fileName, 1, outPath, "", 0, 2);
            System.out.println("mRet: " + mret);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }
	
	public static void main(String args[]) throws Exception {
		ConvertGGSNThread cThread = new ConvertGGSNThread();
		cThread.openConnection();
		cThread.goitest();
		cThread.closeConnection();
	}

}