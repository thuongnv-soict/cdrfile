package cdrfile.thread;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cdrfile.convert.StructEricssonLTE;
import cdrfile.convert.StructNokiaLTE;
import cdrfile.global.Global;
import cdrfile.global.IOUtils;
import cdrfile.global.TextFile;
import cdrfile.global.cdrfileParam;
import cdrfile.thread.ConvertNokiaLTE.PGWNokiaTagConstanct;
import epg.cdr.sgw.ChangeOfCharCondition;
import epg.cdr.sgw.ConstSgwTag;
import epg.cdr.sgw.EnumeRatedSgw;
import epg.cdr.sgw.SgwModel;

public class ConvertIGWSonic {
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

	//private static final int START_NUMBER_FIELDS = 176;
	private static final int STOP_NUMBER_FIELDS = 239;
	private static final int ATTEMPT_NUMBER_FIELDS = 192;
	//private static final int INETERMEDIATE_NUMBER_FIELDS = 169;
	private static final int HEADER_NUMBER_FIELDS = 53;


	/**
	 * 
	 * @author TrungNQ
+	 * startMap, stopMap dung de anh xa vi tri cac fields trong file convert voi vi tri cac fileds trong ban ghi
+	 * voi moi key la vi tri cua field trong header map se tra lai vi tri cua no trong record tuong ung
+	 */

//	private static Map<Integer, Integer> startMap;
	private static Map<Integer, Integer>  stopMap;
	private static Map<Integer, Integer>  attemptMap;
	//private static Map<Integer, Integer>  inermediateMap;

	public static void main(String[] args) throws Exception {
		new ConvertIGWSonic().convertIGWSonic("C:\\Users\\VHCSOFT\\Desktop\\input\\1001A58.act_p", "", 1, "", "", 1, 1);

	}

	private int convertIGWSonic(String pSourceConvert, String pFileName,
			int pFileID, String pDestinationConvert, String pCurrent_dir,
			int pLocalSplitFilebyDay, int pCenterID) throws Exception {

		/**
		 * Text file io tool
		 */
		TextFile fileConvert = new TextFile();

		/**
		 * Read source file
		 */
		BufferedReader fileCDR = null;

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


		try {
			//Global.ExecuteSQL(mConnection, "alter session set nls_date_format='dd/mm/yyyy hh24:mi:ss'");

			// get path
			//mSource = IOUtil.FillPath(pSourceConvert, Global.mSeparate) + pFileName;

			// open file to read			// sua pSourceConvert thanh mSource
			fileCDR = new BufferedReader(new FileReader(pSourceConvert));

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

			fileConvert.openFile("C:\\Users\\VHCSOFT\\Desktop\\output\\1001A58.act_p.txt", 5242880);



			//mConnection.setAutoCommit(false);

			//mSQL = "UPDATE import_header SET time_begin_convert=sysdate ";
			//mSQL += "WHERE file_id=" + pFileID;

			///	Global.ExecuteSQL(mConnection, mSQL);
			/*			
			mFileLength = (int) fileCDR.length();

			// khai bao buffer
			mBuffer = new byte[mFileLength];

			mLength = fileCDR.read(mBuffer);
			 */
			mFileByte = 0;

			// Total length of the CDR file in octets (header + CDR payload)
			//int cdrDataLength = 0;
			mFileByte = 0;

			// BO QUA HEADER
			String line = fileCDR.readLine();
			StringBuilder content = new StringBuilder();
			content.append(Global.IGW_SONUS_HEADER + "\n");
			Pattern patt = Pattern.compile("([^\"|]+)(\"[^\"|]+\")");
			Matcher m;
			String footer = "File administratively closed.";
			String[] fields;
			String[] tmp = null;
			StringBuilder lineConvert = new StringBuilder();
			while ((line = fileCDR.readLine()) != null && !line.contains(footer)) {
				m = patt.matcher(line);
				lineConvert.setLength(0);

				// if - else: thay , bang |
				if (!m.find()) {
					lineConvert.append(line.replaceAll(",", "|") + "\n");
				} else {
					m.reset();
					while (m.find()) {
						lineConvert.append(m.group(1).replaceAll(",", "|"));
						lineConvert.append(m.group(2).replaceAll("\"", ""));
						line = line.substring(m.end());
						m = patt.matcher(line);
					}

					// append end of string
					m = patt.matcher(line);
					if (!m.find()) {
						lineConvert.append(line.replaceAll(",", "|"));
					}					
				}

				fields = lineConvert.toString().split("\\|");				
				int i , j;
				if (fields[0].equals("STOP")) {						
					j= STOP_NUMBER_FIELDS - fields.length;
					if (j > 0) {
						tmp= new String[STOP_NUMBER_FIELDS];
						for (i = 0; i < tmp.length; i++) {
							if (i < fields.length) {
								tmp[i] = fields[i];
							} else {
								tmp[i] = "";
							} 							
						}
						fields = tmp;

					}	
					tmp = new String [HEADER_NUMBER_FIELDS];
					for (i = 0; i < tmp.length; i++) {
						tmp[i] = "";
					}

					// map cac fileds voi header
					for (int pos : stopMap.keySet()) {
					//	int value = stopMap.get(pos);
						tmp[pos-1] = fields[stopMap.get(pos)-1].equals("\n") ? "" : fields[stopMap.get(pos)-1];
						
					}
					
					// lay hai gai tri gateway va trunkgroup cua filed Route Selected
					
					if (tmp[24] == null || tmp[24].split(":").length == 0) {
						tmp[24] = "";
						tmp[25] = "";
					} else {
						String gateway = tmp[24].split(":")[0];
						if (tmp[24].length() == 2) {
							
							String  trunkGroup = tmp[24].split(":")[1];
							
							tmp[25] = trunkGroup;
						}
						tmp[24] = gateway;
					}
					
					
					// lay cac gia tri netType, codecType, audioEncodeType
					if (tmp[34] == null || tmp[34].split(":").length == 0) {
						tmp[34] = "";
						tmp[35] = "";
						tmp[36] = "";
					} else {
						String netType = tmp[34].split(":")[0];
						if (tmp[34].split(":").length >=2) {
							String codecType = tmp[34].split(":")[1];
							tmp[35] = codecType;
						}
						
						if (tmp[34].split(":").length >=3) {
							String audioEncodeType = tmp[34].split(":")[2];
							tmp[36] = audioEncodeType;
						}
						tmp[34] = netType;
					}	

				} else if (fields[0].equals("ATTEMPT")) {
					
					j= ATTEMPT_NUMBER_FIELDS - fields.length;
					if (j > 0) {
						tmp= new String[ATTEMPT_NUMBER_FIELDS];
						for (i = 0; i < tmp.length; i++) {
							if (i < fields.length) {
								tmp[i] = fields[i];
							} else {
								tmp[i] = "";
							} 							
						}
						fields = tmp;
					}	

					tmp = new String [HEADER_NUMBER_FIELDS];
					for (i = 0; i < tmp.length; i++) {
						tmp[i] = "";
					}

					// map cac fileds voi header
					for (int pos : attemptMap.keySet()) {
						tmp[pos-1] = fields[attemptMap.get(pos)-1].equals("\n") ? "" : fields[attemptMap.get(pos)-1];
					}
				
				} 
				else{
					continue;
				}
				lineConvert.setLength(0);
			
				for (String str : tmp) {
					lineConvert.append(str + "|");
				}

				lineConvert.deleteCharAt(lineConvert.length()-1);
				content.append(lineConvert + "\n");
				mRecConvert++;
			}
			//System.out.println(content);
			fileConvert.addText(content.toString());
			System.out.println(content);
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
/*
			if (cdrfileParam.OnErrorResumeNext.compareTo("TRUE") == 0) {
					writeLogFile(" - " + ex.toString() + " - at record:"
						+ mRecConvert);
				return Global.ErrFileConverted;
			} else {
				//	System.out.println(mRecConvert + " " + ex.toString());
				throw ex;
			}*/
		} finally {
			try {
				mSource = null;
				//mSQL = null;
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

	static {

		stopMap = new HashMap<Integer, Integer>();
		attemptMap = new HashMap<Integer, Integer>();
	//	inermediateMap = new HashMap<Integer, Integer>();


		stopMap.put(1, 1);
		stopMap.put(2, 2);
		stopMap.put(3, 4);
		stopMap.put(4, 5);
		stopMap.put(5, 6);
		stopMap.put(6, 7);
		stopMap.put(7, 8);
		stopMap.put(8, 9);
		stopMap.put(9, 10);
		stopMap.put(10, 11);
		stopMap.put(11, 12);
		stopMap.put(12, 13);
		stopMap.put(13, 14);
		stopMap.put(14, 15);
		stopMap.put(15, 16);
		stopMap.put(16, 17);
		stopMap.put(17, 18);
		stopMap.put(18, 20);
		stopMap.put(19, 21);
		stopMap.put(20, 25);
		stopMap.put(21, 27);
		stopMap.put(22, 28);
		stopMap.put(23, 29);
		stopMap.put(24, 30);
		stopMap.put(25, 31);
		stopMap.put(27, 32);
		stopMap.put(28, 33);
		stopMap.put(29, 34);
		stopMap.put(30, 60);
		stopMap.put(31, 64);
		stopMap.put(32, 68);
		stopMap.put(33, 70);
		stopMap.put(34, 75);
		stopMap.put(35, 79);
		stopMap.put(38, 82);
		stopMap.put(41, 102);
		stopMap.put(42, 125);
		stopMap.put(43, 126);
		stopMap.put(44, 199);
		attemptMap.put(1, 1);
		attemptMap.put(5, 6);
		attemptMap.put(6, 7);
		attemptMap.put(8, 9);
		attemptMap.put(14, 12);
		attemptMap.put(17, 15);
		attemptMap.put(18, 17);
		attemptMap.put(19, 18);
		attemptMap.put(22, 25);
		attemptMap.put(23, 26);
		attemptMap.put(24, 27);
		attemptMap.put(27, 29);
		attemptMap.put(28, 30);
		attemptMap.put(29, 31);
		attemptMap.put(30, 53);
		attemptMap.put(31, 57);
		attemptMap.put(32, 58);
		attemptMap.put(33, 60);
		attemptMap.put(35, 69);
		attemptMap.put(36, 70);
		// them 9 gia tri vao cuoi file
		stopMap.put(45, 3);
		stopMap.put(46, 35);
		stopMap.put(47, 36);
		stopMap.put(48, 37);
		stopMap.put(49, 38);
		stopMap.put(50, 46);
		stopMap.put(51, 106);
		stopMap.put(52, 125);
		stopMap.put(53, 126);
		
		attemptMap.put(45, 3);
		attemptMap.put(46, 32);
		attemptMap.put(47, 33);
		attemptMap.put(48, 34);
		attemptMap.put(49, 35);
		attemptMap.put(50, 39);
		attemptMap.put(51, 97);
		attemptMap.put(52, 115);
		attemptMap.put(53, 116);


	}

}

