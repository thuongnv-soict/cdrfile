package cdrfile.thread;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

import cdrfile.convert.StructICCNEIF;
import cdrfile.global.Global;
import cdrfile.global.IOUtils;
import cdrfile.global.TextFile;
import cdrfile.global.cdrfileParam;

public class ConvertICCNEIF extends ThreadInfo {

	protected String mStrValues = "";
    protected byte mBuffer[] = null;
    protected int mByteN = 0;
    protected int mFileByte = 0;
    /*protected int mRecOG = 0;
    protected int mRecIC = 0;
    protected int mRecSMO = 0;
    protected int mRecSMT = 0;
    protected int mRecAnnoun = 0;
    protected int mRecDivert = 0;
    protected int mRecTransit = 0;
    protected int mRecOther = 0;*/
    protected int mRecConvert = 0;
    protected int mRecData = 0;
    protected String mValue = "";
    protected int mRet = 0;
    protected int mRecLength = 0;
    protected int mLength = 0;
    protected String mSQL = null;
    
    public void appendValue(String strValue)
    {
        if (mStrValues.length() == 0)
        {
            mStrValues = strValue;
        }
        else
        {
            mStrValues += Global.cstrDelimited;
            mStrValues += strValue;
        }
    }

    
	class ConvertICC_NEIF
    {

        protected StructICCNEIF ICC_NEIFCDR = new StructICCNEIF();
        
        protected int mFeature = 0;
        protected int mSubFeature = 0;
        protected int mType = 0;
        protected int mSubType = 0;
        protected int mStktVarParLength = 0;
        protected int mEleLength = 0;
        protected String mEleValue = "";
        protected int mRet = 0;
        protected int mValueLength = 0;
        protected String mTmp = "";

        private void convertICC()
        {
            int mByteN = 0;

            mRecConvert++;
            while (mByteN < mRecLength)
            {
                // header
            	ICC_NEIFCDR.Header = Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
                mByteN++;
                mStktVarParLength = 0;
                mLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
                mByteN++;
                if (mLength > 128)
                {
                    mRet = mLength - 128;
                    for (int i = 1; i <= mRet; i++)
                    {
                        mStktVarParLength += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (mRet - i));
                        mByteN++;
                    }
                }
                else
                {
                    mStktVarParLength = mLength;
                }
                for (int mEn = 1; mEn <= mStktVarParLength; mEn++)
                {
                    mValue = Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
                    mByteN++;
                    if ("a0".compareTo(mValue) == 0)
                    {
                        mLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
                        mByteN++;
                        mEn++;
                        for (int k = 1; k <= mLength; k++)
                        {
                            mByteN++;
                            mEn++;
                        }
                    }
                    else if ("a1".compareTo(mValue) == 0)
                    {

                        mLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
                        mByteN++;
                        mEn++;
                        for (int k = 1; k <= mLength; k++)
                        {
                            mByteN++;
                            mEn++;
                        }
                    }
                    else
                    { // Stkt-element data
                        mLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
                        mByteN++;
                        mEn++;
                        if (mLength > 128)
                        {
                            mRet = mLength - 128;
                            mEleLength = 0;
                            for (int i = 1; i <= mRet; i++)
                            {
                                mEleLength += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (mRet - i));
                                mByteN++;
                                mEn++;
                            }
                        }
                        else
                        {
                            mEleLength = mLength;
                        }
                        // Content Stkt-Ele
                        for (int mEle = 1; mEle <= mEleLength; mEle++)
                        {
                            mValue = Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
                            mByteN++;
                            mEn++;

                            mLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
                            mByteN++;
                            mEn++;
                            mEle++;
                            if ("a0".compareTo(mValue) == 0)
                            { // Call data
                                mValue = Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
                                mByteN++;
                                mEn++;
                                mEle++;
                                if (mValue.substring(1).compareTo("0") == 0)
                                { // Stkt-tag
                                    mLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
                                    mByteN++;
                                    mEn++;
                                    mEle++;

                                    // content Feature
                                    // Type
                                    mFeature = 0;

                                    mByteN++;
                                    mEn++;
                                    mEle++;
                                    // Length
                                    mLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
                                    mByteN++;
                                    mEn++;
                                    mEle++;
                                    // Content
                                    for (int i = 1; i <= mLength; i++)
                                    {
                                        mFeature += Global.fixSignedByte(mBuffer[mFileByte + mByteN]) * Math.pow(256, (mLength - i));
                                        mByteN++;
                                        mEn++;
                                        mEle++;
                                    }
                                    // content SubFeature
                                    // Type
                                    mSubFeature = 0;

                                    mByteN++;
                                    mEn++;
                                    mEle++;
                                    // Length
                                    mLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
                                    mByteN++;
                                    mEn++;
                                    mEle++;
                                    // Content
                                    for (int i = 0; i < mLength; i++)
                                    {
                                        mSubFeature += Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
                                        mByteN++;
                                        mEn++;
                                        mEle++;
                                    }
                                    // content Type
                                    // Type
                                    mType = 0;

                                    mByteN++;
                                    mEn++;
                                    mEle++;
                                    // Length
                                    mLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
                                    mByteN++;
                                    mEn++;
                                    mEle++;
                                    // Content
                                    for (int i = 0; i < mLength; i++)
                                    {
                                        mType += Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
                                        mByteN++;
                                        mEn++;
                                        mEle++;
                                    }
                                    // content SubType
                                    // Type
                                    mSubType = 0;

                                    mByteN++;
                                    mEn++;
                                    mEle++;
                                    // Length
                                    mLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
                                    mByteN++;
                                    mEn++;
                                    mEle++;
                                    // Content
                                    for (int i = 0; i < mLength; i++)
                                    {
                                        mSubType += Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
                                        mByteN++;
                                        mEn++;
                                        mEle++;
                                    }

                                    // octet string DUMP
                                    mByteN++;
                                    mEn++;
                                    mEle++;
                                    // Length
                                    mLength = Global.fixSignedByte(mBuffer[mFileByte + mByteN]);
                                    mByteN++;
                                    mEn++;
                                    mEle++;
                                    mValue = "";
                                    /*String eventVal = "";
                                    String strLocation = "";
                                    String strCell = "";*/

                                    for (int i = 0; i < mLength; i++)
                                    {
                                        mValue += (char) (mBuffer[mFileByte + mByteN]);
                                       /* eventVal += Global.HexToBCD(Global.fixSignedByte(mBuffer[mFileByte + mByteN]));
                                        //Xu ly cho CountryCode + NetworkCode + LocationCode + Cell Code 27 Oct.
                                        //type = GPRS
                                        if(i >= 12 && i <= 15)
                                        {
                                            strLocation += (char) (mBuffer[mFileByte + mByteN]);
                                        }
                                        else if(i > 15)
                                        {
                                            strCell += (char) (mBuffer[mFileByte + mByteN]);
                                        }*/
                                        //End.
                                        mByteN++;
                                        mEn++;
                                        mEle++;
                                    }
                                    
                                    mEleValue = mFeature + "." + mSubFeature + "." + mType + "." + mSubType;
                                    
                                    if (mEleValue.compareTo("3130.1.100.1") == 0)
                                    { // #6740715,452013106747745,1208156725,10,QT2,24/04/2014 06:03:03,39,50000,0,643097563960,050,28/04/2011#
                                    	mTmp = mValue.substring(1, mValue.length() - 1);
                                    	
                                    	String[] data = mTmp.split(",");
                                    	
                                    	try {
	                                    	ICC_NEIFCDR.sequence = data[0];
	                                    	ICC_NEIFCDR.imsi = data[1];
	                                    	ICC_NEIFCDR.isdn = data[2];
	                                    	ICC_NEIFCDR.neifInfo = data[3];
	                                    	ICC_NEIFCDR.profile = data[4];
	                                    	ICC_NEIFCDR.timestamp = data[5];
	                                    	ICC_NEIFCDR.mainValue = data[7];
	                                    	ICC_NEIFCDR.bonusValue = data[8];
	                                    	ICC_NEIFCDR.transactionId = data[9];
	                                    	ICC_NEIFCDR.topupProfile = data[10];
                                    	} catch (Exception e) {
                                    		//System.out.println(mTmp);
                                    	}
                                    }

                                }
                            }
                            else if ("a1".compareTo(mValue) == 0)
                            { // Call event
                                mByteN += mLength;
                                mEn += mLength;
                                mEle += mLength;

                            }
                            else if ("a2".compareTo(mValue) == 0)
                            { // Stkt-obj
                                mByteN += mLength;
                                mEn += mLength;
                                mEle += mLength;
                            }
                        } // end for element
                    } // end if Stkt-element data
                } // End for mStktVarParLength
            } // End while record length
            mFileByte += mByteN;
        }

        protected int ICC_NEIF(String pSourceConvert, String pFileName, int pFileID, String pDestinationConvert, String pCurrent_dir, int pLocalSplitFilebyDay, int pCenterID) throws Exception
        {
            TextFile fileConvert = new TextFile();
            RandomAccessFile fileCDR = null;
            IOUtils IOUtil = new IOUtils();
            String mSource = null;
            int mFileLength = 0;
            int mRecN = 0;
            String lastCallingTime = "";
            String firstCallingTime = "";

            try
            {
                Global.ExecuteSQL(mConnection, "alter session set nls_date_format='dd/mm/yyyy hh24:mi:ss'");
                mSource = IOUtil.FillPath(pSourceConvert, Global.mSeparate) + pFileName;
                fileCDR = new RandomAccessFile(mSource, "r");

                // Initialize output text file
                if (pLocalSplitFilebyDay == 1)
                {
                    mSource = IOUtil.FillPath(pDestinationConvert, Global.mSeparate) + pCurrent_dir;
                    IOUtil.forceFolderExist(mSource);
                    mSource += Global.mSeparate + pFileName;
                }
                else
                {
                    mSource = IOUtil.FillPath(pDestinationConvert, Global.mSeparate) + pFileName;
                }

                IOUtil.deleteFile(mSource);
                fileConvert.openFile(mSource, 5242880);
                if (cdrfileParam.ChargeCDRFile)
                {
                    fileConvert.addText(Global.mICCNEIFHeaderCharge);
                }
                else
                {
                    fileConvert.addText(Global.mICCNEIFHeaderNoCharge);
                }

                mConnection.setAutoCommit(false);

                mSQL = "UPDATE import_header SET time_begin_convert=sysdate ";
                mSQL += "WHERE file_id=" + pFileID;
                Global.ExecuteSQL(mConnection, mSQL);
                mStrValues = "";
                mValueLength = 0;
                mValue = "";
                //mEncapsulationLength = 0;
                mSQL = null;
                mRecLength = 0;
                mLength = 0;
                /*mRecOG = 0;
                mRecIC = 0;
                mRecSMO = 0;
                mRecSMT = 0;
                mRecDivert = 0;
                mRecOther = 0;*/
                mRecConvert = 0;
                mFeature = 0;
                mSubFeature = 0;
                mType = 0;
                mSubType = 0;
                mStktVarParLength = 0;
                mEleLength = 0;
                mEleValue = "";
                mRet = 0;
                //mEventLength = 0;
                //mClass = "";
                //mObject = "";
                //mStartDateTime = "";
                mFileLength = (int) fileCDR.length();
                mBuffer = new byte[mFileLength];
                mLength = fileCDR.read(mBuffer);
                if (mLength != mFileLength)
                {
                    mConnection.rollback();
                    mSQL = "UPDATE import_header SET status=" + Global.StateConvertedError + ",note='Error read buffer at position: " + mFileLength + "'  WHERE file_id = " + pFileID;
                    Global.ExecuteSQL(mConnection, mSQL);
                    mConnection.commit();
                    writeLogFile("    - Error read buffer at position: " + mFileLength);
                    return (Global.ErrFileConverted);
                }
                mFileByte = 0;
                mRecN = 0;
                
                while (mFileByte < mFileLength)
                {
                    
                	ICC_NEIFCDR.sequence = "";
                	ICC_NEIFCDR.imsi = "";
                    ICC_NEIFCDR.isdn = "";
                    ICC_NEIFCDR.neifInfo = "";
                    ICC_NEIFCDR.profile = "";
                    ICC_NEIFCDR.timestamp = "";
                    ICC_NEIFCDR.mainValue = "";
                    ICC_NEIFCDR.bonusValue = "";
                    ICC_NEIFCDR.transactionId = "";
                    ICC_NEIFCDR.topupProfile = "";

                    mRecLength = (Global.fixSignedByte(mBuffer[mFileByte]) * 16777216) + (Global.fixSignedByte(mBuffer[mFileByte + 1]) * 65536) + (Global.fixSignedByte(mBuffer[mFileByte + 2]) * 256) + (Global.fixSignedByte(mBuffer[mFileByte + 3]));
                    mFileByte += 4;
                    if (mFileByte + mRecLength > mFileLength)
                    {
                        mConnection.rollback();
                        mSQL = "UPDATE import_header SET status=" + Global.StateConvertedError + ",note='Error read buffer at position: " + mFileByte + " - rec:" + mRecConvert + "'  WHERE file_id = " + pFileID;
                        Global.ExecuteSQL(mConnection, mSQL);
                        mConnection.commit();
                        writeLogFile("    - Error read buffer at position: " + mFileByte + " - rec:" + mRecConvert);
                        return (Global.ErrFileConverted);
                    }

                    convertICC();
                    
                    // CDR rong
                    if (!ICC_NEIFCDR.isdn.equals("")) {

	                    if (cdrfileParam.ChargeCDRFile)
	                    {
	                        appendValue(ICC_NEIFCDR.sequence);
	                        appendValue(ICC_NEIFCDR.imsi);
	                        appendValue(ICC_NEIFCDR.isdn);
	                        appendValue(ICC_NEIFCDR.neifInfo);
	                        appendValue(ICC_NEIFCDR.profile);
	                        appendValue(ICC_NEIFCDR.timestamp);
	                        appendValue(ICC_NEIFCDR.mainValue);
	                        appendValue(ICC_NEIFCDR.bonusValue);
	                        appendValue(ICC_NEIFCDR.transactionId);
	                        appendValue(ICC_NEIFCDR.topupProfile);
	
	                        fileConvert.addText(mStrValues);
	                        mStrValues = "";
	                        try
	                        {
								 String dateCallTime = ICC_NEIFCDR.timestamp;
								 if (lastCallingTime.equals(""))
								 {
								     lastCallingTime = dateCallTime;
								 }
								 else if (Global.compareTo(dateCallTime, lastCallingTime) == 2)
								 {
								     lastCallingTime = lastCallingTime;
								 }
								 //get first calling time.(18/02/2011)
								 if (firstCallingTime.equals(""))
								 {
								     firstCallingTime = dateCallTime;
								 }
								 else if (Global.compareTo(dateCallTime, firstCallingTime) == 2)
								 {
								     firstCallingTime = dateCallTime;
								 }
	
	                        }
	                        catch (Exception ex)
	                        {
	                        }
	                    }
	                    else
	                    {
	                        mRecN++;
	                        mStrValues = mRecN + "|" + ICC_NEIFCDR.sequence + "|" + ICC_NEIFCDR.imsi + "|" + ICC_NEIFCDR.isdn + "|" + ICC_NEIFCDR.neifInfo + "|"
	                            + ICC_NEIFCDR.profile + "|" + ICC_NEIFCDR.timestamp + "|" + ICC_NEIFCDR.mainValue + "|" + ICC_NEIFCDR.bonusValue + "|" + ICC_NEIFCDR.transactionId + "|" + ICC_NEIFCDR.topupProfile;
	                        
	                        //--End--
	                        fileConvert.addText(mStrValues);
	                        mStrValues = "";
	                        try
	                        {
	                            String dateCallTime = ICC_NEIFCDR.timestamp;
	                            if (lastCallingTime.equals(""))
	                            {
	                                lastCallingTime = dateCallTime;
	                            }
	                            else if (Global.compareTo(dateCallTime, lastCallingTime) == 2)
	                            {
	                                lastCallingTime = lastCallingTime;
	                            }
	                            //get first calling time.(18/02/2011)
	                            if (firstCallingTime.equals(""))
	                            {
	                                firstCallingTime = dateCallTime;
	                            }
	                            else if (Global.compareTo(dateCallTime, firstCallingTime) == 2)
	                            {
	                                firstCallingTime = dateCallTime;
	                            }
	
	                        }
	                        catch (Exception ex)
	                        {
	                        }
	                    }
                    }
                }
                
                writeLogFile("      Total record converted : " + Global.rpad(Integer.toString(mRecN), 10, " "));
                writeLogFile("      -------------------------------");
                
                if (cdrfileParam.ChargeCDRFile)
                {
                    mSQL = "UPDATE import_header SET time_end_convert=sysdate,status=" + Global.StateConverted +",rec_total=" + mRecConvert + ",rec_convert=" + mRecN + ",min_calling_time='" + lastCallingTime + "',max_calling_time='" + firstCallingTime + "' WHERE file_id = " + pFileID;
                }
                else
                {
                    mSQL = "UPDATE import_header SET time_end_convert=sysdate,status=" + Global.StateRated +",rec_total=" + mRecConvert + ",rec_convert=" + mRecN + ",min_calling_time='" + lastCallingTime + "',max_calling_time='" + firstCallingTime + "' WHERE file_id = " + pFileID;
                }
                Global.ExecuteSQL(mConnection, mSQL);
                mConnection.commit();
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
                return Global.ErrFileNotFound;
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                mConnection.rollback();
                mSQL = "UPDATE import_header SET status=" + Global.StateConvertedError + ",note='" + ex.toString() + " at rec:" + mRecConvert + "' WHERE file_id = " + pFileID;
                Global.ExecuteSQL(mConnection, mSQL);
                mConnection.commit();

                if (cdrfileParam.OnErrorResumeNext.compareTo("TRUE") == 0)
                {
                    writeLogFile(" - " + ex.toString() + " - at record:" + mRecConvert);
                    return Global.ErrFileConverted;
                }
                else
                {
                    //System.out.println(mRecConvert + " " + ex.toString());
                    throw ex;
                }

            }
            finally
            {
                try
                {
                    mSource = null;
                    mSQL = null;
                    mValue = null;
                    mBuffer = new byte[0];
                    IOUtil = null;
                    fileConvert.closeFile();
                    fileConvert = null;
                    fileCDR.close();
                    fileCDR = null;
                }
                catch (Exception e)
                {
                }
            }
            return (Global.OKFileConverted);
        }

    }

	@Override
	protected void processSession() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String args[]) throws Exception
    {
        ConvertICCNEIF cThread = new ConvertICCNEIF();
//        cThread.processSession();
        cThread.openConnection();
        cThread.goitest();
        cThread.closeConnection();
    }

    public void goitest()
    {
        ConvertICC_NEIF cvICC = new ConvertICC_NEIF();

        try
        {
        	long startTime = System.currentTimeMillis();
//            int mret = cvA.AlcatelFile_R6("C:\\Documents and Settings\\quangdd3\\Desktop\\Working22-2", "MSCCTO-1347.CDR", 1, "C:\\Documents and Settings\\quangdd3\\Desktop\\Working22-2\\Out", "", 0, 4);
//              int mret = cvE.EricssonFile_R10("D:\\Docs\\CDRFILE\\Ericson", "TTFILE03-106955", 1, "D:\\Docs\\CDRFILE\\Ericson\\Out", "", 0, 2);
              //int mret = cvH.Huawei_V212("C:\\Users\\datnh\\Desktop\\Data\\CDRFILE\\Huawei", "MSC04_TTFILE00_5081.dat", 1, "C:\\Users\\datnh\\Desktop\\Data\\CDRFILE\\Huawei\\Out", "", 0, 2);
//              int mret = cv.INFile_PPS421("c:\\Documents and Settings\\quangdd\\Desktop\\Working 04_Oct\\In", "acc27804753_3209", 1, "c:\\Documents and Settings\\quangdd\\Desktop\\Working 04_Oct\\Out", "", 0, 1);
        	//int mret = cvE.EricssonFile_R10("C:\\Users\\Phoenix\\Desktop\\Data\\CDRFILE\\MSC", "TTFILE04-319933", 1, "C:\\Users\\Phoenix\\Desktop\\Data\\CDRFILE\\MSC\\Out", "", 0, 2);
                int mret = cvICC.ICC_NEIF("C:\\Users\\datnh\\Desktop\\Data\\CDRFILE\\ICC_NEIF", "acc11400038_1864", 1, "C:\\Users\\datnh\\Desktop\\Data\\CDRFILE\\ICC_NEIF\\Out", "", 0, 2);
                //int mret = cvSGSN.SG7CD8_GPP_SGSNFile("C:\\Users\\Phoenix\\Desktop\\Data\\CDRFILE\\SGSN", "SGCTO_2N_CF00071746.D00_20130916202502.ASN", 1, "C:\\Users\\Phoenix\\Desktop\\Data\\CDRFILE\\SGSN\\Out", "", 0, 2);
                long endTime   = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println(totalTime);
//              int mret = cvSGSN.SG7CD8_GPP_SGSNFile("D:\\Docs\\CDRFILE\\SGSN", "SGDNI_1N_CF00000120.D01_20120515232810.ASN", 1, "D:\\Docs\\CDRFILE\\SGSN\\Out", "", 0, 2);
//              int mret = cvSGSN.SG7CD8_GPP_SGSNFile("D:\\Docs\\CDRFILE\\SGSN", "SGDNG_1N_CF00075936.D00_20130911103129.ASN", 1, "D:\\Docs\\CDRFILE\\SGSN\\Out", "", 0, 2);
//              int mret = cvSMSC.BRF_V452("D:\\Docs\\CDRFILE\\SMSC", "bills.1.20130927092000", 1, "D:\\Docs\\CDRFILE\\SMSC\\Out", "", 0, 2);
                System.out.println("mret=" + mret);
        }
        catch (Exception de)
        {
            de.printStackTrace();
        }
    }
}
