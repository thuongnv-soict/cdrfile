package cdrfile.thread;

/**
 * <p>Title: CDR File(s) System</p>
 * <p>Description: VMS IS Departerment</p>
 * <p>Copyright: Copyright (c)  by eKnowledge 2004</p>
 * <p>Company: VietNam Mobile Telecom Services</p>
 * @author eKnowledge - Software
 * @version 1.0
 */

import java.io.File;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import cdrfile.ftpClient.FtpParam;
import cdrfile.ftpClient.ListData;
import cdrfile.general.General;
import cdrfile.global.Global;
import cdrfile.global.IOUtils;
import cdrfile.global.LastModifiedFileComparator;
import cdrfile.global.cdrfileParam;
import cdrfile.zip.SmartZip;
import ftp.FTPClient;
import ftp.FTPConnectMode;
import ftp.FTPException;
import ftp.FTPFile;
import ftp.FTPTransferType;

public class FtpThread extends ThreadInfo
{
    protected int iFtpID = 0;
    protected FtpParam listParam = null;
    protected Vector vFtpParam = new Vector();
    protected ListData listLoad = null;
    protected Vector vListDownload = new Vector();
    protected long fileMaxLongTime = 0;
    
    private JSch jsch;
    private Session session;
    private Channel channel;
    private ChannelSftp channelSftp;

    public void finalize()
    {
        if (vFtpParam != null)
        {
            vFtpParam.removeAllElements();
            vFtpParam.clear();
        }
        destroy();
        System.runFinalization();
        System.gc();
    }

    protected void processSession() throws Exception
    {
    	String currTime = null;
    	boolean checkFtpTime = true;
    	boolean checkFtpDay = false;
        boolean Found = false;
        long mTime;
        long fileSize = 0;
        // modify 28/2. For check file size.
        Statement stmt = mConnection.createStatement();
        String mSQL = "select b.ptr_value from SYS_PARAM a, SYS_PARAM_DETAIL b" +
            " where a.id = b.ptr_id and ptr_id = 12 and b.ptr_name = 'CheckFileSizeWhenDownload'";

        ResultSet rsSysParam = stmt.executeQuery(mSQL);
        if (rsSysParam.next())
        {
            fileSize = Long.parseLong(rsSysParam.getString("PTR_VALUE"));
        }

        mSQL = "SELECT * FROM data_param a,node_cluster b" +
            " WHERE used_getfile=1 AND a.run_on_node=b.id AND ftp_thread_id =" + getThreadID() +
            " AND b.ip='" + Global.getLocalSvrIP() + "' ORDER BY a.id";
        ResultSet rs = stmt.executeQuery(mSQL);

        try
        {

           while (rs.next() && miThreadCommand != THREAD_STOP) 
            {	
            	checkFtpTime = true;
            	checkFtpDay = false;
            	
            	if(rs.getString("ftp_day") != null && !rs.getString("ftp_day").equals("")) {
            		
            		String[] dayList =  rs.getString("ftp_day").split(",");
            		int dayNow = Global.getDayNow();
            		
            		for(int i=0; i<dayList.length; i++) {
            			
            			if (dayNow == Integer.parseInt(dayList[i])) {
            				checkFtpDay = true;
            				break;
            			}
            		}
            	} else {
            		checkFtpDay = true;
            	}
            	
            	if (!checkFtpDay) {
            		
            		writeLogFile("Skip connect to host " + rs.getString("NOTE") + "=> byDay: " + rs.getString("FTP_DAY"));
            		continue;
            	}
            	
				if (rs.getString("ftp_begin_time") != null && rs.getString("ftp_end_time") != null)
				{
					currTime = Global.Format(new Date(), "dd/MM/yyyy HH:mm:ss");
					
					int cp1 = Global.compareTo(currTime, 
								currTime.substring(0, currTime.indexOf(" ") + 1) + rs.getString("ftp_begin_time"));
					int cp2 = Global.compareTo(currTime, 
								currTime.substring(0, currTime.indexOf(" ") + 1) + rs.getString("ftp_end_time"));
					
					// thoi gian hien tai khong thuoc khoang thoi gian cho phep
					if (cp1 == 1 || cp2 == 2) {
						checkFtpTime = false;
						writeLogFile("Skip connect to host " + rs.getString("NOTE") + "=> byTime: " + rs.getString("FTP_HOST_IP"));
					}
				}
				
				if (!checkFtpTime) {
					continue;
				}
				
                iFtpID = -1;
                mTime = (new java.util.Date().getTime()) / 60000;
                Found = false;
                //add list file into Vector.
                for (int i = 0; i < vFtpParam.size(); i++)
                {
                    listParam = (FtpParam) vFtpParam.get(i);
                    if ((listParam.getFtpID()) == (rs.getInt("id")))
                    {
                        listParam.setTimeConnect(rs.getInt("TIME_CONNECT"));
                        listParam.setTimeDownload(rs.getInt("TIME_DOWNLOAD"));
                        Found = true;
                        iFtpID = i;
                        break;
                    }
                }
                if (iFtpID == -1)
                {
                    iFtpID = vFtpParam.size();
                }

                if (Found == false)
                {
                    // Add param to vector vFtpParam
                    listParam = new FtpParam(rs.getInt("ID"), rs.getString("FILE_NAME"), rs.getLong("FILE_SIZE"), "", rs.getInt("TIME_CONNECT"), rs.getInt("TIME_DOWNLOAD"), mTime);
                    vFtpParam.add(listParam);
                    // Call function ftp
                    // 2015.04.01, datnh, change get file_size long value
                    if (rs.getString("PROTOCOL") == null || rs.getString("PROTOCOL").equals("")) {
                    	 DoFtp(mConnection, vFtpParam, listParam, rs.getInt("ID"), rs.getString("NOTE"), rs.getString("FTP_HOST_IP"), rs.getString("FTP_UID"), rs.getString("FTP_PWD"), rs.getString("CONNECT_MODE"), rs.getInt("TIME_OUT"), rs.getString("REMOTE_GETFILE_DIR"), rs.getString("LOCAL_GETFILE_DIR"), rs.getString("FILE_INFO"), rs.getString("FILE_NAME"), rs.getLong("FILE_SIZE"), rs.getString("HEADER_FILE_RECEIVE"), rs.getString("EXT_FILE_RECEIVE"), rs.getString("LOCAL_PUTFILE_DIR"),
                                 rs.getString("REMOTE_PUTFILE_DIR"), rs.getString("FTP_MODE"), rs.getInt("REMOTE_SPLIT_FILE_BY_DAY"), rs.getString("DIR_CURRENT"), rs.getInt("LOCAL_SPLIT_FILE_BY_DAY"), rs.getInt("ZIP_AFTER_DOWNLOAD"), rs.getInt("RENAME_AFTER_DOWNLOAD"), rs.getInt("SEQ_FROM"), rs.getInt("SEQ_TO"), rs.getInt("RENAME_TYPE"), rs.getString("NEW_PREFIX"), rs.getString("NEW_EXT"), rs.getString("MAIL_TO"), rs.getInt("SEQ_AFTER_DOWNLOAD"), rs.getInt("MIN_SEQ"), rs.getInt("MAX_SEQ"),
                                 rs.getInt("CURR_SEQ"), rs.getInt("WAITING_LAST_FILE"), rs.getString("EXT_TO_GET_FILE"), fileSize, rs.getInt("EXT_SEQ_GETFILE_FROM"), rs.getInt("EXT_SEQ_GETFILE_TO"), rs.getInt("CHECK_SIZE_FOR_GETFILE"));
                     
                    } else {
                    	DoSFtp(mConnection, vFtpParam, listParam, rs.getInt("ID"), rs.getString("NOTE"), rs.getString("FTP_HOST_IP"), rs.getString("FTP_UID"), rs.getString("FTP_PWD"), rs.getString("CONNECT_MODE"), rs.getInt("TIME_OUT"), rs.getString("REMOTE_GETFILE_DIR"), rs.getString("LOCAL_GETFILE_DIR"), rs.getString("FILE_INFO"), rs.getString("FILE_NAME"), rs.getLong("FILE_SIZE"), rs.getString("HEADER_FILE_RECEIVE"), rs.getString("EXT_FILE_RECEIVE"), rs.getString("LOCAL_PUTFILE_DIR"),
                                rs.getString("REMOTE_PUTFILE_DIR"), rs.getString("FTP_MODE"), rs.getInt("REMOTE_SPLIT_FILE_BY_DAY"), rs.getString("DIR_CURRENT"), rs.getInt("LOCAL_SPLIT_FILE_BY_DAY"), rs.getInt("ZIP_AFTER_DOWNLOAD"), rs.getInt("RENAME_AFTER_DOWNLOAD"), rs.getInt("SEQ_FROM"), rs.getInt("SEQ_TO"), rs.getInt("RENAME_TYPE"), rs.getString("NEW_PREFIX"), rs.getString("NEW_EXT"), rs.getString("MAIL_TO"), rs.getInt("SEQ_AFTER_DOWNLOAD"), rs.getInt("MIN_SEQ"), rs.getInt("MAX_SEQ"),
                                rs.getInt("CURR_SEQ"), rs.getInt("WAITING_LAST_FILE"), rs.getString("EXT_TO_GET_FILE"), fileSize, rs.getInt("EXT_SEQ_GETFILE_FROM"), rs.getInt("EXT_SEQ_GETFILE_TO"), rs.getInt("CHECK_SIZE_FOR_GETFILE"));
                    
                    	}
                   }
                else
                {
                    listParam = (FtpParam) vFtpParam.get(iFtpID);
                    if ((mTime - listParam.getTimeCurrentConnect()) >= listParam.getTimeConnect())
                    {
                        listParam.setTimeCurrentConnect(mTime);
                        // Call function ftp
                        // 2015.04.01, datnh, change get file_size long value
                        if (rs.getString("PROTOCOL") == null || rs.getString("PROTOCOL").equals("")) {
                       	 DoFtp(mConnection, vFtpParam, listParam, rs.getInt("ID"), rs.getString("NOTE"), rs.getString("FTP_HOST_IP"), rs.getString("FTP_UID"), rs.getString("FTP_PWD"), rs.getString("CONNECT_MODE"), rs.getInt("TIME_OUT"), rs.getString("REMOTE_GETFILE_DIR"), rs.getString("LOCAL_GETFILE_DIR"), rs.getString("FILE_INFO"), rs.getString("FILE_NAME"), rs.getLong("FILE_SIZE"), rs.getString("HEADER_FILE_RECEIVE"), rs.getString("EXT_FILE_RECEIVE"), rs.getString("LOCAL_PUTFILE_DIR"),
                                    rs.getString("REMOTE_PUTFILE_DIR"), rs.getString("FTP_MODE"), rs.getInt("REMOTE_SPLIT_FILE_BY_DAY"), rs.getString("DIR_CURRENT"), rs.getInt("LOCAL_SPLIT_FILE_BY_DAY"), rs.getInt("ZIP_AFTER_DOWNLOAD"), rs.getInt("RENAME_AFTER_DOWNLOAD"), rs.getInt("SEQ_FROM"), rs.getInt("SEQ_TO"), rs.getInt("RENAME_TYPE"), rs.getString("NEW_PREFIX"), rs.getString("NEW_EXT"), rs.getString("MAIL_TO"), rs.getInt("SEQ_AFTER_DOWNLOAD"), rs.getInt("MIN_SEQ"), rs.getInt("MAX_SEQ"),
                                    rs.getInt("CURR_SEQ"), rs.getInt("WAITING_LAST_FILE"), rs.getString("EXT_TO_GET_FILE"), fileSize, rs.getInt("EXT_SEQ_GETFILE_FROM"), rs.getInt("EXT_SEQ_GETFILE_TO"), rs.getInt("CHECK_SIZE_FOR_GETFILE"));
                        
                       } else {
                       	DoSFtp(mConnection, vFtpParam, listParam, rs.getInt("ID"), rs.getString("NOTE"), rs.getString("FTP_HOST_IP"), rs.getString("FTP_UID"), rs.getString("FTP_PWD"), rs.getString("CONNECT_MODE"), rs.getInt("TIME_OUT"), rs.getString("REMOTE_GETFILE_DIR"), rs.getString("LOCAL_GETFILE_DIR"), rs.getString("FILE_INFO"), rs.getString("FILE_NAME"), rs.getLong("FILE_SIZE"), rs.getString("HEADER_FILE_RECEIVE"), rs.getString("EXT_FILE_RECEIVE"), rs.getString("LOCAL_PUTFILE_DIR"),
                                   rs.getString("REMOTE_PUTFILE_DIR"), rs.getString("FTP_MODE"), rs.getInt("REMOTE_SPLIT_FILE_BY_DAY"), rs.getString("DIR_CURRENT"), rs.getInt("LOCAL_SPLIT_FILE_BY_DAY"), rs.getInt("ZIP_AFTER_DOWNLOAD"), rs.getInt("RENAME_AFTER_DOWNLOAD"), rs.getInt("SEQ_FROM"), rs.getInt("SEQ_TO"), rs.getInt("RENAME_TYPE"), rs.getString("NEW_PREFIX"), rs.getString("NEW_EXT"), rs.getString("MAIL_TO"), rs.getInt("SEQ_AFTER_DOWNLOAD"), rs.getInt("MIN_SEQ"), rs.getInt("MAX_SEQ"),
                                   rs.getInt("CURR_SEQ"), rs.getInt("WAITING_LAST_FILE"), rs.getString("EXT_TO_GET_FILE"), fileSize, rs.getInt("EXT_SEQ_GETFILE_FROM"), rs.getInt("EXT_SEQ_GETFILE_TO"), rs.getInt("CHECK_SIZE_FOR_GETFILE"));
                       
                       }
                    }
                }
            }
        }
        catch (Exception ex)
        {
            Global.writeEventThreadErr(Integer.parseInt(getThreadID()), 2, ex.toString());
            throw ex;
        }
        finally
        {
            try
            {
                rsSysParam.close();
                rsSysParam = null;
                rs.close();
                rs = null;
                stmt.close();
                stmt = null;
            }
            catch (Exception e)
            {
            }
        }
    }

    protected int DoListingRemote(java.sql.Connection pConnection, Vector pVectorParam, FtpParam plistParam, FTPClient pFtp, int pTimeOut, int pFtpID, String pReceiveFrom, String pDirReceive, String pFileInfo, String pFileName, long pFileSize, String pReceiveHeader, String pReceiveExt, int pRemoteFileSplitByDay, String pCurrentDir, String extToGetFile, int extSeqFrom, int extSeqTo) throws Exception
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String mReceiveFrom = null;

        if (extToGetFile == null)
        {
            extToGetFile = "";
        }
        try
        {
            mReceiveFrom = pReceiveFrom;

            pFtp.chdir(pReceiveFrom);
            pFtp.pwd();

            FTPFile[] listings = pFtp.dirDetails("");
            for (int i = 0; i < listings.length; i++)
            {
                if (listings[i].isDir())
                {
                    if ((".".compareTo(listings[i].getName()) != 0) && ("..".compareTo(listings[i].getName()) != 0))
                    {
                        if (pRemoteFileSplitByDay == 1)
                        {
                            if (Global.isNumeric(listings[i].getName()))
                            {
                                if ((pCurrentDir != null) && ((Integer.parseInt(pCurrentDir)) <= (Integer.parseInt(listings[i].getName()))))
                                {
                                    if ("/".compareTo(pReceiveFrom.substring(pReceiveFrom.length() - 1)) != 0)
                                    {
                                        mReceiveFrom = pReceiveFrom + "/" + listings[i].getName();
                                    }
                                    else
                                    {
                                        mReceiveFrom = pReceiveFrom + listings[i].getName();
                                    }

                                    writeLogFile(" - Subdirectory listing: " + listings[i].getName());
                                    if (DoListingRemote(pConnection, pVectorParam, plistParam, pFtp, pTimeOut, pFtpID, mReceiveFrom, pDirReceive, pFileInfo, pFileName, pFileSize, pReceiveHeader, pReceiveExt, pRemoteFileSplitByDay, listings[i].getName(), extToGetFile, extSeqFrom, extSeqTo) != 0)
                                    {
                                        return ( -1);
                                    }
                                }
                            }
                        }
                    }
                }
                else
                {
                    if (extToGetFile.trim() != "")
                    {
                        if (listings[i].getName().substring(extSeqFrom, extSeqTo).indexOf(extToGetFile) >= 0)
                        {
                            if ((Long.parseLong(sdf.format(listings[i].lastModified()))) > (Long.parseLong(pFileInfo)))
                            {
                                listLoad = new ListData(mReceiveFrom, listings[i].getName(), listings[i].size(), sdf.format(listings[i].lastModified()), pCurrentDir, listings[i].lastModified().getTime());
                                vListDownload.add(listLoad);

                                plistParam = (FtpParam) pVectorParam.get(iFtpID);
                                if (listings[i].getName().compareTo(plistParam.getFileName()) == 0)
                                {
                                    if (listings[i].size() == plistParam.getFileSize())
                                    {
                                        plistParam.setStatus("Already");
                                    }
                                    else
                                    {
                                        plistParam.setFileSize(listings[i].size());
                                    }
                                } // end if check file size
                                else
                                {
                                    plistParam.setStatus("Wait");
                                    plistParam.setFileName(listings[i].getName());
                                    plistParam.setFileSize(listings[i].size());
                                } // end if check file name need download
                            } // end if check new FileInfo higheer old
                            // FileInfo
                            else if ((Long.parseLong(sdf.format(listings[i].lastModified()))) == (Long.parseLong(pFileInfo)))
                            {
                                // Check if is a file downloaded then step
                                // get file next
                                if (((pFileName.toLowerCase().compareTo(listings[i].getName().toLowerCase()) == 0) && (pFileSize < listings[i].size())) || (pFileName.toLowerCase().compareTo(listings[i].getName().toLowerCase()) < 0))
                                {
                                    listLoad = new ListData(mReceiveFrom, listings[i].getName(), listings[i].size(), sdf.format(listings[i].lastModified()), pCurrentDir, listings[i].lastModified().getTime());
                                    vListDownload.add(listLoad);

                                    plistParam = (FtpParam) pVectorParam.get(iFtpID);
                                    if (listings[i].getName().compareTo(plistParam.getFileName()) == 0)
                                    {
                                        if (listings[i].size() == plistParam.getFileSize())
                                        {
                                            plistParam.setStatus("Already");
                                        }
                                    } // end if check file size
                                    else
                                    {
                                        plistParam.setStatus("Wait");
                                        plistParam.setFileName(listings[i].getName());
                                        plistParam.setFileSize(listings[i].size());
                                    }
                                } // end if check file name downloaded
                            } // end else if
                        }
                    }
                    else
                    {
                        if ((pReceiveExt == null) || (listings[i].getName().endsWith(pReceiveExt) == true) || (listings[i].getName().substring(listings[i].getName().lastIndexOf(".") + 1).indexOf(pReceiveExt) >= 0))
                        {
                            if ((pReceiveHeader == null) || (pReceiveHeader.length() < listings[i].getName().length()))
                            {
                                if ((pReceiveHeader == null) || (pReceiveHeader.toLowerCase().compareTo(listings[i].getName().substring(0, pReceiveHeader.length()).toLowerCase()) == 0))
                                {
                                    // Check FileInfo

                                    if ((Long.parseLong(sdf.format(listings[i].lastModified()))) > (Long.parseLong(pFileInfo)))
                                    {
                                        listLoad = new ListData(mReceiveFrom, listings[i].getName(), listings[i].size(), sdf.format(listings[i].lastModified()), pCurrentDir, listings[i].lastModified().getTime());
                                        vListDownload.add(listLoad);

                                        plistParam = (FtpParam) pVectorParam.get(iFtpID);
                                        if (listings[i].getName().compareTo(plistParam.getFileName()) == 0)
                                        {
                                            if (listings[i].size() == plistParam.getFileSize())
                                            {
                                                plistParam.setStatus("Already");
                                            }
                                            else
                                            {
                                                plistParam.setFileSize(listings[i].size());
                                            }
                                        } // end if check file size
                                        else
                                        {
                                            plistParam.setStatus("Wait");
                                            plistParam.setFileName(listings[i].getName());
                                            plistParam.setFileSize(listings[i].size());
                                        } // end if check file name need download
                                    } // end if check new FileInfo higheer old
                                    // FileInfo
                                    else if ((Long.parseLong(sdf.format(listings[i].lastModified()))) == (Long.parseLong(pFileInfo)))
                                    {
                                        // Check if is a file downloaded then step
                                        // get file next
                                        if (((pFileName.toLowerCase().compareTo(listings[i].getName().toLowerCase()) == 0) && (pFileSize < listings[i].size())) || (pFileName.toLowerCase().compareTo(listings[i].getName().toLowerCase()) < 0))
                                        {
                                            listLoad = new ListData(mReceiveFrom, listings[i].getName(), listings[i].size(), sdf.format(listings[i].lastModified()), pCurrentDir, listings[i].lastModified().getTime());
                                            vListDownload.add(listLoad);

                                            plistParam = (FtpParam) pVectorParam.get(iFtpID);
                                            if (listings[i].getName().compareTo(plistParam.getFileName()) == 0)
                                            {
                                                if (listings[i].size() == plistParam.getFileSize())
                                                {
                                                    plistParam.setStatus("Already");
                                                }
                                            } // end if check file size
                                            else
                                            {
                                                plistParam.setStatus("Wait");
                                                plistParam.setFileName(listings[i].getName());
                                                plistParam.setFileSize(listings[i].size());
                                            }
                                        } // end if check file name downloaded
                                    } // end else if
                                } // end if check header file download
                            }
                        } // end if check file tmp

                    }
                }
            }
            listings = null;
        }
        catch (StringIndexOutOfBoundsException e)
        {
            writeLogFile(" Warning: substring ext file name. ");
        }
        catch (Exception ex)
        {

            throw ex;
        }

        finally
        {
            sdf = null;
            mReceiveFrom = null;
        }
        return (0);
    }

    protected void DoFtp(java.sql.Connection pConnection, Vector pVectorParam, FtpParam plistParam, int pFtpID, String pNote, String pHost, String pUID, String pPWD, String pConnectMode, int pTimeOut, String pReceiveFrom, String pDirReceive, String pFileInfo, String pFileName, long pFileSize, String pReceiveHeader, String pReceiveExt, String pDirSend, String pSendTo, String pFTPMode, int pRemoteFileSplitByDay, String pCurrentDir, int pLocalFileSplitByDay, int pZipAfterDownload,
        int pRenameAfterDownload, int pSeqFrom, int pSeqTo, int pRenameType, String pNewPreFix, String pNewExt, String pMailTo, int pSeqAfterDownload, int pMinSeq, int pMaxSeq, int pCurrSeq, int pWaitingLastFile, String extToGetFile, long fileSize, int extSeqFrom, int extSeqTo, int checkSizeForgetFile) throws Exception
    {
        try
        {
            writeLogFile("Connecting to host " + pNote + "=>" + pHost);
            // FTPClient ftp = new FTPClient(pHost,21,null,pTimeOut);
            FTPClient ftp = new FTPClient(pHost, 21, pTimeOut);

            pConnection.setAutoCommit(false);
            Global.ExecuteSQL(pConnection, "alter session set nls_date_format='yyyyMMddhh24miss'");
            //Clear download list.
            if (vListDownload != null)
            {
                vListDownload.removeAllElements();
                vListDownload.clear();
            }
            ftp.login(pUID, pPWD);
            ftp.setTransferBufferSize(10240);
            if (pConnectMode.compareTo("ACTIVE") == 0)
            {
                ftp.setConnectMode(FTPConnectMode.ACTIVE);
            }
            else
            {
                ftp.setConnectMode(FTPConnectMode.PASV);
            }

            if (pFTPMode.compareTo("BINARY") == 0)
            {
                ftp.setType(FTPTransferType.BINARY);
            }
            else
            {
                ftp.setType(FTPTransferType.ASCII);
            }

            if ((pReceiveFrom != "") && (pReceiveFrom != null))
            {
                if (DoListingRemote(pConnection, pVectorParam, plistParam, ftp, pTimeOut, pFtpID, pReceiveFrom, pDirReceive, pFileInfo, pFileName, pFileSize, pReceiveHeader, pReceiveExt, pRemoteFileSplitByDay, pCurrentDir, extToGetFile, extSeqFrom, extSeqTo) == 0)
                {
                    if (vListDownload.size() > 0)
                    {
                        DoDownload(pConnection, pVectorParam, plistParam, ftp, pFtpID, pDirReceive, pRemoteFileSplitByDay, pLocalFileSplitByDay, pZipAfterDownload, pRenameAfterDownload, pSeqFrom, pSeqTo, pRenameType, pNewPreFix, pNewExt, pSeqAfterDownload, pMinSeq, pMaxSeq, pCurrSeq, pWaitingLastFile, pFileSize, pMailTo, fileSize, checkSizeForgetFile);
                    }
                }
                else
                {
                    return;
                }
                if (Global.ExecuteOutParameterInt(pConnection, "SELECT count(*) INTO ? " + "FROM sys_param a,sys_param_detail b " + "WHERE a.id = b.ptr_id and " + "b.ptr_name='AutoDownloadMissedFile' " + "AND ptr_value='TRUE'") > 0)
                {
                    writeLogFile(" Getting missed file of " + pNote + "=>" + pHost);
                    DoLoadMissingFile(pConnection, ftp, pFtpID, pReceiveFrom, pDirReceive, pRemoteFileSplitByDay, pZipAfterDownload, pRenameAfterDownload, pSeqFrom, pSeqTo, pRenameType, pNewPreFix, pNewExt, "");
                    writeLogFile(" End get missed file of " + pNote + "...");
                }
            }

            if ((pSendTo != "") && (pSendTo != null))
            {
                writeLogFile("Uploading file to " + pSendTo + "...");
                DoUpload(pConnection, ftp, pFtpID, pDirSend, pSendTo, pLocalFileSplitByDay, pCurrentDir, pRemoteFileSplitByDay, pFileInfo, pFileName, pFileSize);
                writeLogFile("Finish upload file.");
            }
            ftp.quit();
            ftp = null;
            writeLogFile("Disconnecting " + pNote + "...\r\n");
        }
        catch (FTPException ex)
        {
            switch (ex.getReplyCode())
            {
            case 530:
                writeLogFile(ex.getReplyCode() + " - Invalid user name or password:" + pNote);
                General.SendMail(mConnection, 1, pMailTo, ex.getReplyCode() + " - " + ex.getMessage() + " " + pNote);

                /*General.addNewSMS(mConnection,pFtpID, 2,
                                                 "Invalid user name or password:" + pNote);*/


                break;
            case 550:
                writeLogFile(ex.getReplyCode() + " - " + ex.getMessage());
                General.SendMail(mConnection, 1, pMailTo, ex.getReplyCode() + " - " + ex.getMessage() + " " + pNote);

                /*General.addNewSMS(mConnection, pFtpID,1,
                                                "Ftp has occurred error: - 550 " + pNote);*/


                break;
            default:
                writeLogFile(ex.getReplyCode() + " - " + ex.getMessage());
                General.SendMail(mConnection, 1, pMailTo, ex.getReplyCode() + " - " + ex.getMessage() + " " + pNote);

                /*General.addNewSMS(mConnection, pFtpID,1,
                                                "Ftp thread has occurred error " + pNote);*/


                break;
            }
        }
        catch (ConnectException e)
        {
            writeLogFile(" - ConnectException - " + e.getMessage() + "\r\n");
            General.SendMail(mConnection, 1, pMailTo, e.getMessage() + " " + pNote);
            /*General.addNewSMS(mConnection,pFtpID, 1,
                    "ConnectException " + pNote);*/
        }
        catch (NoRouteToHostException e)
        {
            writeLogFile(" - NoRouteToHostException - " + e.getMessage() + "\r\n");
            General.SendMail(mConnection, 1, pMailTo, e.getMessage() + " " + pNote);
            /*General.addNewSMS(mConnection,pFtpID, 1,
                                    "NoRouteToHostException " + pNote);*/

        }
        catch (SocketTimeoutException e)
        {
            writeLogFile(" - SocketTimeoutException - " + e.getMessage() + "\r\n");
            General.SendMail(mConnection, 1, pMailTo, e.getMessage() + " " + pNote);
            /*General.addNewSMS(mConnection,pFtpID, 1,
                                    "SocketTimeoutException " + pNote);*/

        }
        catch (SocketException e)
        {
            writeLogFile(" - SocketException - " + e.getMessage() + "\r\n");
            General.SendMail(mConnection, 1, pMailTo, e.getMessage() + " " + pNote);
            /*General.addNewSMS(mConnection,pFtpID, 1,
                                    "SocketException " + pNote);*/

        }
        catch (Exception e)
        {
            if (cdrfileParam.OnErrorResumeNext.compareTo("TRUE") == 0)
            {
                writeLogFile(" - " + e.toString());
            }
            else
            {
                System.out.println(" - " + e.toString());
                General.SendMail(mConnection, 1, pMailTo, e.toString() + " " + pNote);
                System.err.println(Global.Format(new java.util.Date(), "dd/MM/yyyy HH:mm:ss") + " : - ERROR in module DoFtp : " + e.toString());
                /*General.addNewSMS(mConnection, pFtpID,1,
                                              "ERROR in module DoFtp " + pNote);*/


                throw e;
            }
        }
        finally
        {
            if (vListDownload != null)
            {
                vListDownload.removeAllElements();
                vListDownload.clear();
            }
        }
    }

    private String convertLongToDate(Long dateVal)
    {
        String returnDate = "";
        String DATE_FORMAT = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        Date d = new Date(dateVal);
        returnDate = sdf.format(d);
        return returnDate;
    }

    protected int DoDownload(java.sql.Connection pConnection, Vector pVectorParam, FtpParam plistParam, FTPClient pFtp, int pFtpID, String pDirReceive, int pRemoteFileSplitByDay, int pFileSplitByDay, int pZipAfterDownload, int pRenameAfterDownload, int pSeqFrom, int pSeqTo, int pRenameType, String pNewPreFix, String pNewExt, int pSeqAfterDownload, int pMinSeq, int pMaxSeq, int pCurrSeq, int pWaitingLastFile, long pFileSizeDownloaded, String pMailTo, long pFileSizeForDownload, int checkSizeForGetFile) throws Exception
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        File file = null;
        File fileRename = null;
        String mSourceName = null;
        String mDestinationName = null;
        String mNewFileName = null;
        String mSQL = null;
        int mRetValue;
        Statement stmt = null;
        String mCurrentPath = "";
        IOUtils IOUtil = new IOUtils();
        SmartZip zip = new SmartZip();
        long mFileSizePrevious = 0;
        try
        {
            stmt = pConnection.createStatement();

            // Read param from vector pVectorParam
            plistParam = (FtpParam) pVectorParam.get(iFtpID);
            sort(plistParam);
            int size = 0;
            if (pWaitingLastFile > 0)
            {
                ////bo qua nhung file cuoi theo thoi gian./////
                Calendar cal = Calendar.getInstance();
                Date date = new Date();
                date.setTime(fileMaxLongTime);
                cal.setTime(date);
                cal.add(Calendar.MINUTE, -pWaitingLastFile);
                long longTime = cal.getTimeInMillis();
                int inValidFile = 0;
                for(int i = 0; i < vListDownload.size(); i++)
                {
                    ListData fileIndex = (ListData) vListDownload.get(i);
                    if(fileIndex.getFileDateLongTime() > longTime)
                    {
                        inValidFile++;
                    }
                }
                size = vListDownload.size() - inValidFile;
                if(size >= 0)
                {
                    writeLogFile("Skipped last files with file's time after " + cal.getTime() + ", getting list following files:");

                }
                ////ket thuc bo file cuoi cung theo thoi gian./////


//                size = vListDownload.size() - 1;
//                if (size >= 0)
//                {
//                    ListData lastListData = (ListData) vListDownload.get(size);
//                    String path = IOUtil.FillPath(lastListData.getFilePath(), Global.mSeparate) + lastListData.getFileName();
//                    writeLogFile("Skipped last file: " + path + ", getting list following files:");
//                }
            }
            else
            {
                size = vListDownload.size();
            }
            for (int i = 0; i < size; i++)
            {
                if (miThreadCommand != THREAD_STOP)
                {
                    listLoad = (ListData) vListDownload.get(i);
                    mCurrentPath = "";

                    //Ngay 28/2: check file size.
                    if (checkSizeForGetFile == 1)
                    {
                        if (size == 1)
                        {
                            if (listLoad.getFileSize() < (pFileSizeForDownload * pFileSizeDownloaded) / 100)
                            {
                                writeLogFile("Warning: This file size is unusual reduction. This file name:" + listLoad.getFileName() + ", file's date is: " + convertLongToDate(listLoad.getFileDateLongTime()));
                                General.SendMail(mConnection, 1, pMailTo, "Warning: This file size is unusual reduction. This file name:" + listLoad.getFileName() + ", file date is: " + convertLongToDate(listLoad.getFileDateLongTime()));
                            }
                        }
                        else
                        {
                            if (i == 0)
                            {
                                if (listLoad.getFileSize() < (pFileSizeForDownload * pFileSizeDownloaded) / 100)
                                {
                                    writeLogFile("Warning: This file size is unusual reduction. This file name:" + listLoad.getFileName() + ", file's date is: " + convertLongToDate(listLoad.getFileDateLongTime()));
                                    General.SendMail(mConnection, 1, pMailTo, "Warning: This file size is unusual reduction. This file name:" + listLoad.getFileName() + ", file date is: " + convertLongToDate(listLoad.getFileDateLongTime()));
                                }
                                mFileSizePrevious = listLoad.getFileSize();
                            }
                            else
                            {
                                if (mFileSizePrevious < (pFileSizeForDownload * listLoad.getFileSize()) / 100)
                                {
                                    writeLogFile("Warning: This file size is unusual reduction. This file name:" + listLoad.getFileName() + ", file's date is: " + convertLongToDate(listLoad.getFileDateLongTime()));
                                    General.SendMail(mConnection, 1, pMailTo, "Warning: This file size is unusual reduction. This file name:" + listLoad.getFileName() + ", file date is: " + convertLongToDate(listLoad.getFileDateLongTime()));
                                }
                                mFileSizePrevious = listLoad.getFileSize();
                            }
                        }
                    }
                    //Ket thuc check file size.
                    if (listLoad.getFileName().compareTo(plistParam.getFileName()) != 0)
                    {
                        mSourceName = IOUtil.FillPath(listLoad.getFilePath(), Global.mSeparate) + listLoad.getFileName();
                        if (Global.mSeparate.compareTo(pDirReceive.substring(pDirReceive.length() - 1)) == 0)
                        {
                            if (pFileSplitByDay == 0)
                            {
                                mDestinationName = pDirReceive;
                            }
                            else
                            {
                                mCurrentPath = sdf.format(new java.util.Date(listLoad.getFileDateLongTime()));
                                mDestinationName = pDirReceive + mCurrentPath + Global.mSeparate;
                            }
                        }
                        else if (pFileSplitByDay == 0)
                        {
                            mDestinationName = pDirReceive + Global.mSeparate;
                        }
                        else
                        {
                            mCurrentPath = sdf.format(new java.util.Date(listLoad.getFileDateLongTime()));
                            mDestinationName = pDirReceive + Global.mSeparate + mCurrentPath + Global.mSeparate;
                        }

                        IOUtil.forceFolderExist(mDestinationName);
                        IOUtil.chmod(new File(mDestinationName), "750");
                        file = new File(mDestinationName);
                        if (file.exists() != true)
                        {
                            writeLogFile("The system cannot find the path specified : '" + mDestinationName + "'");
                            return ( -1);
                        }
                        writeLogFile("   .Loading file " + mSourceName + " - Size : " + listLoad.getFileSize() + " bytes.");

                        pFtp.get(mDestinationName + listLoad.getFileName() + ".tmp", mSourceName);
                        file = new File(mDestinationName + listLoad.getFileName() + ".tmp");

                        if (pRenameAfterDownload == 1)
                        {
                            if (pRenameType == 1) // HexToDec
                            {
                                mNewFileName = pNewPreFix + Global.Format(Global.Hex2Dec(listLoad.getFileName().substring(pSeqFrom - 1, pSeqTo - 1)), Global.rpad("", pSeqTo - pSeqFrom + 1, "0")) + "." + pNewExt;
                            }
                            else
                            {
                                mNewFileName = pNewPreFix + Global.Format(Integer.parseInt(listLoad.getFileName().substring(pSeqFrom, pSeqTo)), Global.rpad("", pSeqTo - pSeqFrom, "0")) + "." + pNewExt;
                            }
                        }
                        else
                        {
                            mNewFileName = listLoad.getFileName();
                        }
                        if (pSeqAfterDownload == 1)
                        {
                            if (pCurrSeq < pMinSeq)
                            {
                                pCurrSeq = pMinSeq;
                            }
                            if (pCurrSeq > pMaxSeq)
                            {
                                pCurrSeq = pMinSeq;
                            }
                            int pos = mNewFileName.lastIndexOf(".");
                            mNewFileName = mNewFileName.substring(0, pos) + "." + Global.Format(pCurrSeq, Global.rpad("", pSeqTo - pSeqFrom, "0")) + mNewFileName.substring(pos, mNewFileName.length());

                            pCurrSeq++;
                        }
                        fileRename = new File(mDestinationName + mNewFileName);
                        file.renameTo(fileRename);
                        IOUtil.chmod(fileRename, "750");
                        fileRename.setLastModified(listLoad.getFileDateLongTime());
                        if (fileRename.length() != listLoad.getFileSize())
                        {
                            writeLogFile("   .File downloaded error.Size of file downloaded (" + fileRename.length() + ") not equal file origination.");
                            return (0);
                        }
                        if (pZipAfterDownload == 1)
                        {
                            zip.ZipFile(fileRename.getAbsolutePath(), fileRename.getAbsolutePath() + ".zip");
                            fileRename = new File(fileRename.getAbsolutePath() + ".zip");
                            IOUtil.chmod(fileRename, "750");
                            fileRename.setLastModified(listLoad.getFileDateLongTime());

                            IOUtil.deleteFile(mDestinationName + mNewFileName);
                        }

                        mSQL = "update import_header set current_dir = '" + mCurrentPath + "',file_size=" + listLoad.getFileSize() + ",status=" + Global.StateFileFtpOK + " where status in (" + Global.StateFileFtpOK + "," + Global.StateConvertedError + ")" + " and file_name='" + mNewFileName + "' and ftp_id=" + pFtpID + " and date_createfile ='" + listLoad.getFileInfo() + "'";
                        mRetValue = stmt.executeUpdate(mSQL);
                        if (mRetValue == 0)
                        {
                            if (pRenameAfterDownload == 1)
                            {
                                mSQL = "insert into import_header(status,ftp_id," + "file_name,file_size,date_getfile,current_dir," + "date_createfile,file_name_org) values(" + Global.StateFileFtpOK + "," + pFtpID + ",'" + mNewFileName + "'," + listLoad.getFileSize() + ",sysdate,'" + mCurrentPath + "','" + listLoad.getFileInfo() + "','" + listLoad.getFileName() + "')";
                            }
                            else
                            {
                                mSQL = "insert into import_header(status,ftp_id," + "file_name,file_size,date_getfile,current_dir," + "date_createfile) values(" + Global.StateFileFtpOK + "," + pFtpID + ",'" + mNewFileName + "'," + listLoad.getFileSize() + ",sysdate,'" + mCurrentPath + "','" + listLoad.getFileInfo() + "')";
                            }
                            stmt.executeUpdate(mSQL);
                        }

                        if (pRemoteFileSplitByDay == 1)
                        {
                            mSQL = "update data_param set file_name='" + listLoad.getFileName() + "',file_size=" + listLoad.getFileSize() + ",file_info='" + listLoad.getFileInfo() + "',dir_current='" + listLoad.getFileDir() + "', curr_seq=" + pCurrSeq + " where id=" + pFtpID;
                        }
                        else
                        {
                            mSQL = "update data_param set file_name='" + listLoad.getFileName() + "',file_size=" + listLoad.getFileSize() + ",file_info='" + listLoad.getFileInfo() + "', curr_seq=" + pCurrSeq + " where id=" + pFtpID;
                        }

                        stmt.executeUpdate(mSQL);
                        pConnection.commit();
                        writeLogFile("   .File " + listLoad.getFileName() + " had been loaded successful.");
                    }
                    else
                    {
                        // Kiem tra xem kich thuoc file cuoi cung co thay doi
                        // khong
                        if (("Already".compareTo(plistParam.getStatus()) == 0) || (i < vListDownload.size() - 1))
                        {
                            mSourceName = IOUtil.FillPath(listLoad.getFilePath(), Global.mSeparate) + listLoad.getFileName();

                            if (Global.mSeparate.compareTo(pDirReceive.substring(pDirReceive.length() - 1)) == 0)
                            {
                                if (pFileSplitByDay == 0)
                                {
                                    mDestinationName = pDirReceive;
                                }
                                else
                                {
                                    mCurrentPath = sdf.format(new java.util.Date(listLoad.getFileDateLongTime()));
                                    mDestinationName = pDirReceive + mCurrentPath + Global.mSeparate;
                                }
                            }
                            else if (pFileSplitByDay == 0)
                            {
                                mDestinationName = pDirReceive + Global.mSeparate;
                            }
                            else
                            {
                                mCurrentPath = sdf.format(new java.util.Date(listLoad.getFileDateLongTime()));
                                mDestinationName = pDirReceive + Global.mSeparate + mCurrentPath + Global.mSeparate;
                            }
                            IOUtil.forceFolderExist(mDestinationName);
                            IOUtil.chmod(new File(mDestinationName), "750");
                            file = new File(mDestinationName);
                            if (file.exists() != true)
                            {
                                System.out.println("*********************************************************************");
                                System.out.println("The system cannot find the path specified : '" + mDestinationName + "'");
                                System.out.println("*********************************************************************");
                                return ( -1);
                            }
                            writeLogFile("   .Loading file " + mSourceName + " - Size : " + listLoad.getFileSize() + " bytes.");

                            pFtp.get(mDestinationName + listLoad.getFileName() + ".tmp", mSourceName);
                            file = new File(mDestinationName + listLoad.getFileName() + ".tmp");
                            if (pRenameAfterDownload == 1)
                            {
                                if (pRenameType == 1) // HexToDec
                                {
                                    mNewFileName = pNewPreFix + Global.Format(Global.Hex2Dec(listLoad.getFileName().substring(pSeqFrom - 1, pSeqTo - 1)), Global.rpad("", pSeqTo - pSeqFrom + 1, "0")) + "." + pNewExt;
                                }
                                else
                                {
                                    mNewFileName = pNewPreFix + Global.Format(Integer.parseInt(listLoad.getFileName().substring(pSeqFrom, pSeqTo)), Global.rpad("", pSeqTo - pSeqFrom, "0")) + "." + pNewExt;
                                }
                            }
                            else
                            {
                                mNewFileName = listLoad.getFileName();
                            }
                            if (pSeqAfterDownload == 1)
                            {
                                if (pCurrSeq < pMinSeq)
                                {
                                    pCurrSeq = pMinSeq;
                                }
                                if (pCurrSeq > pMaxSeq)
                                {
                                    pCurrSeq = pMinSeq;
                                }
                                int pos = mNewFileName.lastIndexOf(".");
                                mNewFileName = mNewFileName.substring(0, pos) + "." + Global.Format(pCurrSeq, Global.rpad("", pSeqTo - pSeqFrom, "0")) + mNewFileName.substring(pos, mNewFileName.length());
                                pCurrSeq++;
                            }
                            fileRename = new File(mDestinationName + mNewFileName);
                            file.renameTo(fileRename);
                            IOUtil.chmod(fileRename, "750");
                            fileRename.setLastModified(listLoad.getFileDateLongTime());

                            if (fileRename.length() != listLoad.getFileSize())
                            {
                                writeLogFile("   .File downloaded error.Size of file downloaded (" + fileRename.length() + ") not equal file origination.");
                                return (0);
                            }
                            if (pZipAfterDownload == 1)
                            {
                                zip.ZipFile(fileRename.getAbsolutePath(), fileRename.getAbsolutePath() + ".zip");
                                fileRename = new File(fileRename.getAbsolutePath() + ".zip");
                                IOUtil.chmod(fileRename, "750");
                                fileRename.setLastModified(listLoad.getFileDateLongTime());

                                IOUtil.deleteFile(mDestinationName + mNewFileName);
                            }
                            mSQL = " update import_header set current_dir = '" + mCurrentPath + "',status=" + Global.StateFileFtpOK + " where status in (" + Global.StateFileFtpOK + "," + Global.StateConvertedError + ")" + " and file_name='" + mNewFileName + "' and ftp_id=" + pFtpID + " and date_createfile ='" + listLoad.getFileInfo() + "'";

                            mRetValue = stmt.executeUpdate(mSQL);
                            if (mRetValue == 0)
                            {
                                if (pRenameAfterDownload == 1)
                                {
                                    mSQL = "insert into import_header(status,ftp_id," + "file_name,file_size,date_getfile,current_dir," + "date_createfile,file_name_org) values(" + Global.StateFileFtpOK + "," + pFtpID + ",'" + mNewFileName + "'," + listLoad.getFileSize() + ",sysdate,'" + mCurrentPath + "','" + listLoad.getFileInfo() + "','" + listLoad.getFileName() + "')";
                                }
                                else
                                {
                                    mSQL = "insert into import_header(status,ftp_id," + "file_name,file_size,date_getfile,current_dir," + "date_createfile) values(" + Global.StateFileFtpOK + "," + pFtpID + ",'" + mNewFileName + "'," + listLoad.getFileSize() + ",sysdate,'" + mCurrentPath + "','" + listLoad.getFileInfo() + "')";
                                }
                                stmt.executeUpdate(mSQL);
                            }

                            if (pRemoteFileSplitByDay == 1)
                            {
                                mSQL = "update data_param set file_name='" + listLoad.getFileName() + "',file_size=" + listLoad.getFileSize() + ",file_info='" + listLoad.getFileInfo() + "',dir_current='" + listLoad.getFileDir() + "', curr_seq=" + pCurrSeq + " where id=" + pFtpID;
                            }
                            else
                            {
                                mSQL = "update data_param set file_name='" + listLoad.getFileName() + "',file_size=" + listLoad.getFileSize() + ",file_info='" + listLoad.getFileInfo() + "', curr_seq=" + pCurrSeq + " where id=" + pFtpID;
                            }

                            stmt.executeUpdate(mSQL);
                            pConnection.commit();
                            writeLogFile("   .File " + listLoad.getFileName() + " had been loaded successfull.");
                        }
                    }
                }
                // Thread.sleep(50);
                if (((new java.util.Date().getTime() / 60000) - plistParam.getTimeCurrentConnect()) >= plistParam.getTimeDownoad())
                {
                    writeLogFile("The end time for this connection : time current -> " + (new java.util.Date().getTime() / 60000) + " - time started -> " + plistParam.getTimeCurrentConnect() + " >= time download -> " + plistParam.getTimeDownoad());
                    return (0);
                }
            }
            // stmtmSQL.close();
        } // endif try catch
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            try
            {
                IOUtil = null;
                file = null;
                fileRename = null;
                mSourceName = null;
                mDestinationName = null;
                mSQL = null;

                if (vListDownload != null)
                {
                    vListDownload.removeAllElements();
                    vListDownload.clear();
                }
                stmt.close();
                stmt = null;
            }
            catch (Exception e)
            {
            }
        }
        System.runFinalization();
        System.gc();
        return (0);
    }

    /*protected int DoUpload(java.sql.Connection pConnection, FTPClient pFtp, int pFtpID, String pFTPMode, String pDirSend, String pSendTo) throws Exception
         {
        int mID = 0;
        String mFileName = null;
        String mSourcePath = null;
        String mDestinationPath = null;
        IOUtils IOUtil = new IOUtils();
        int mErrorUpload = 0;
        String mSQL = "SELECT id,file_name FROM export_header ";
        mSQL += "WHERE (status=0 or date_send IS NULL) ";
        mSQL += "AND ftp_id=" + pFtpID + " ORDER BY id ";

        Statement stmt = pConnection.createStatement();
        ResultSet rs = stmt.executeQuery(mSQL);

        pFtp.setConnectMode(FTPConnectMode.PASV);
        if (pFTPMode.compareTo("BINARY") == 0)
        {
            pFtp.setType(FTPTransferType.BINARY);
        }
        else
        {
            pFtp.setType(FTPTransferType.ASCII);
        }

        try
        {
            while (rs.next() && (mErrorUpload != 1))
            {
                pConnection.setAutoCommit(false);
                mID = rs.getInt("id");
                mFileName = rs.getString("file_name");
                mDestinationPath = pSendTo;
                mSourcePath = IOUtil.FillPath(pDirSend, Global.mSeparate) + mFileName;
                mDestinationPath = IOUtil.FillPath(pSendTo, Global.mSeparate) + mFileName;
                writeLogFile("   .Uploading file " + mSourcePath + " to " + pSendTo);
                pFtp.put(mSourcePath, mDestinationPath);
                mSQL = "UPDATE export_header SET status=1, date_send=sysdate ";
                mSQL += "WHERE id=" + mID;
                Global.ExecuteSQL(pConnection, mSQL);
                pConnection.commit();
                writeLogFile("   .Finish upload file " + mFileName + " successfully.");
            }
        }
        catch (Exception e)
        {
            throw e;
        }
        finally
        {
            try
            {
                mFileName = null;
                mSourcePath = null;
                mDestinationPath = null;
                IOUtil = null;
                mSQL = null;
                rs.close();
                rs = null;
                stmt.close();
                stmt = null;
            }
            catch (Exception e)
            {
            }
        }
        return 0;
         }*/

    protected int DoUpload(java.sql.Connection pConnection, FTPClient pFtp, int pFtpID, String pDirSend, String pSendTo, int pLocalFileSplitByDay, String pCurrentDir, int pRemoteSplitFileByDay, String pFileInfo, String pFileName, long pFileSize) throws Exception
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat daySDF = new SimpleDateFormat("yyyyMMdd");
        String mDirSend = null;
        String mDesPath = null;
        IOUtils IOUtil = new IOUtils();
        String mSQL = "";
        String mCurrentPath = "";
        File pFile = null;
        Statement stmt = null;
        try
        {
            stmt = pConnection.createStatement();
            File dirSend = new File(pDirSend);
            File[] files = dirSend.listFiles();
            Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
            if (files == null || files.length == 0)
            {
                writeLogFile("   .No files for upload from " + pDirSend + " to " + pSendTo);
            }
            else
            {
                for (int i = 0; i < files.length; i++)
                {
                    pFile = files[i];
                    if (pFile.isDirectory())
                    {
                        if (pLocalFileSplitByDay == 1)
                        {
                            if (Global.isNumeric(pFile.getName()))
                            {
                                if ((pCurrentDir != null) && ((Integer.parseInt(pCurrentDir)) <= (Integer.parseInt(pFile.getName()))))
                                {
                                    if ("/".compareTo(pDirSend.substring(pDirSend.length() - 1)) != 0)
                                    {
                                        mDirSend = pDirSend + "/" + pFile.getName();
                                    }
                                    else
                                    {
                                        mDirSend = pDirSend + pFile.getName();
                                    }
                                    writeLogFile(" - Subdirectory uploading: " + pFile.getName());
                                    if (DoUpload(pConnection, pFtp, pFtpID, mDirSend, pSendTo, pLocalFileSplitByDay, pFile.getName(), pRemoteSplitFileByDay, pFileInfo, pFileName, pFileSize)!= 0)
                                        return -1;
                                }
                            }
                        }
                    }
                    else
                    {
                        if (pFile.getName().toLowerCase().endsWith(".tmp"))
                            continue;
                        long lastModified = Long.parseLong(sdf.format(new java.util.Date(pFile.lastModified())));
                        if (lastModified > Long.parseLong(pFileInfo) || (lastModified == Long.parseLong(pFileInfo) && (((pFileName.toLowerCase().compareTo(pFile.getName().toLowerCase()) == 0) && (pFileSize < pFile.length())) || (pFileName.toLowerCase().compareTo(pFile.getName().toLowerCase()) < 0))))
                        {
                            if (pRemoteSplitFileByDay == 1)
                            {
                                mCurrentPath = daySDF.format(new java.util.Date(pFile.lastModified()));
                                mDesPath = IOUtil.FillPath(pSendTo, Global.mSeparate) + mCurrentPath;
                            }
                            else
                            {
                                mDesPath = IOUtil.FillPath(pSendTo, Global.mSeparate);
                            }
                            try{
                                pFtp.mkdir(mDesPath);
                            }catch (Exception ex){
                            }
                            writeLogFile("   .Uploading file " + pFile.getName());
                            String mFileName = IOUtil.FillPath(mDesPath, Global.mSeparate) + pFile.getName() ;
                            pFtp.put(pFile.getPath(), mFileName );
                            writeLogFile("   .Finish upload file " + pFile.getName() + " successfully.");

                            mSQL = "update export_header set current_dir = '" + mCurrentPath + "',file_size=" + pFile.length() + ",status= 1 where file_name='" + pFile.getName() + "' and ftp_id=" + pFtpID;
                            int mRetValue = stmt.executeUpdate(mSQL);
                            if (mRetValue == 0)
                            {
                                mSQL = "insert into export_header(status,ftp_id,"
                                         + "file_name,file_size,date_putfile,current_dir,"
                                         + "date_createfile) values("
                                         + "1," + pFtpID + ",'" + pFile.getName() + "',"
                                         + pFile.length() + ",sysdate,'" + mCurrentPath
                                         + "','" + lastModified + "')";
                                stmt.executeUpdate(mSQL);
                            }
                            if (pLocalFileSplitByDay == 1)
                            {
                                mSQL = "update data_param set file_name='" + pFile.getName() + "',file_size=" + pFile.length() + ",file_info='" + lastModified + "',dir_current='" + mCurrentPath + "' where id=" + pFtpID;
                            }
                            else
                            {
                                mSQL = "update data_param set file_name='" + pFile.getName()+ "',file_size=" + pFile.length()  + ",file_info='" + lastModified + "' where id=" + pFtpID;
                            }
                            Global.ExecuteSQL(pConnection, mSQL);
                            pConnection.commit();
                        }
                    }
                }
            }
        }
        catch (Exception ex)
        {
            writeLogFile("Upload Error Details: " + ex.toString());
            Global.writeEventThreadErr(Integer.parseInt(getThreadID()), 2, ex.toString());
//            ex.printStackTrace();
            throw ex;
        }finally{
            try
            {
                if (stmt!= null)
                    stmt.close();
                stmt.close();
//                pFtp.quit();
//                pFtp = null;
            }
            catch (Exception e)
            {
            }
        }
        return 0;
    }


    protected void sort(FtpParam plistParam) throws Exception
    {
        long mFileDateTime = 0;
        String mFileName = null;
        String mFileDir = null;
        int iFileCount = vListDownload.size();
        try
        {
            if (iFileCount > 1)
            {
                for (int j = 0; j < (iFileCount - 1); j++)
                {
                    listLoad = (ListData) vListDownload.get(j);
                    mFileDir = listLoad.getFileDir();
                    mFileDateTime = listLoad.getFileDateLongTime();
                    mFileName = listLoad.getFileName();
                    for (int k = (j + 1); k < iFileCount; k++)
                    {
                        listLoad = (ListData) vListDownload.get(k);
                        // System.out.println(k + "-" + mFileName + " + " +
                        // mFileDateTime + " > " +
                        // listLoad.getFileName() + " + " +
                        // listLoad.getFileDateLongTime());
                        if (mFileDateTime > listLoad.getFileDateLongTime())
                        {

                            // System.out.println("=>Swap datetime position
                            // file:" + mFileName + " to " +
                            // listLoad.getFileName());
                            if ((mFileDir == null) || (mFileDir.compareTo(listLoad.getFileDir()) == 0))
                            {
                                mFileDir = listLoad.getFileDir();
                                mFileDateTime = listLoad.getFileDateLongTime();
                                mFileName = listLoad.getFileName();
                                swapItem(j, k);
                            }
                        }
                        else if ((mFileDateTime == listLoad.getFileDateLongTime()) && (listLoad.getFileName().compareTo(mFileName) < 0))
                        {
                            if ((mFileDir == null) || (mFileDir.compareTo(listLoad.getFileDir()) == 0))
                            {
                                // System.out.println("=>Swap filename position
                                // file:" + mFileName + " to " +
                                // listLoad.getFileName());
                                mFileDir = listLoad.getFileDir();
                            }
                            mFileDateTime = listLoad.getFileDateLongTime();
                            mFileName = listLoad.getFileName();
                            swapItem(j, k);
                        }
                    }
                }
                //Sua doi waiting last file theo thoi gian.
                fileMaxLongTime = mFileDateTime;
                //Ket thuc.
                listLoad = (ListData) vListDownload.get(iFileCount - 1);
                plistParam.setFileName(listLoad.getFileName());
                plistParam.setFileSize(listLoad.getFileSize());
            }
        }
        catch (Exception e)
        {
            writeLogFile("   .Error while sort data:" + e.getMessage());
        }
        mFileName = null;
    }

    protected void swapItem(int j, int k)
    {
        Object objTemp = vListDownload.elementAt(j);
        vListDownload.setElementAt(vListDownload.elementAt(k), j);
        vListDownload.setElementAt(objTemp, k);
        objTemp = null;
    }

    //*Nang cap pha 6 *//
    protected int DoLoadMissingFile(java.sql.Connection pConnection, FTPClient pFtp, int pFtpID, String pReceiveFrom, String pDirReceive, int pRemoteFileSplitByDay, int pZipAfterDownload, int pRenameAfterDownload, int pSeqFrom, int pSeqTo, int pRenameType, String pNewPreFix, String pNewExt, String pCurrMissedDir) throws Exception
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
        File file = null;
        File fileRename = null;
        String mSourceName = null;
        String mDestinationName = null;
        int mRetValue;
        String mReceiveFrom = null;
        String mNewFileName = null;
        IOUtils IOUtil = new IOUtils();
        ResultSet rs = null;
        Statement stmt1 = mConnection.createStatement();
        SmartZip zip = new SmartZip();
        Statement stmt = null;
        String mSQL = "select distinct a.current_dir_missed," + "to_char(sysdate-b.time_check,'yyyymmddhh24miss') min_date_check " + "from missed_file a,data_param b " + "where a.status=0 and a.switch_id=b.id and b.id=" + pFtpID;
        ResultSet rs1 = stmt1.executeQuery(mSQL);
        try
        {
            mReceiveFrom = pReceiveFrom;

            pFtp.chdir(pReceiveFrom);
            pFtp.pwd();

            FTPFile[] listings = pFtp.dirDetails(""); //Duyet cac thu muc tren ftp
            for (int i = 0; i < listings.length; i++)
            {
                if (listings[i].isDir())
                {
                    if ((".".compareTo(listings[i].getName()) != 0) && ("..".compareTo(listings[i].getName()) != 0))
                    {
                        if (pRemoteFileSplitByDay == 1)
                        {

                            if (Global.isNumeric(listings[i].getName()))
                            {
                                while (rs1.next() && miThreadCommand != THREAD_STOP)
                                {
                                    if ((rs1.getString("current_dir_missed") != null) && ((Integer.parseInt(rs1.getString("current_dir_missed"))) == (Integer.parseInt(listings[i].getName()))))
                                    {
                                        if ("/".compareTo(pReceiveFrom.substring(pReceiveFrom.length() - 1)) != 0)
                                        {
                                            mReceiveFrom = pReceiveFrom + "/" + listings[i].getName();
                                        }
                                        else
                                        {
                                            mReceiveFrom = pReceiveFrom + listings[i].getName();
                                        }
                                        if ((Long.parseLong(sdf.format(listings[i].lastModified()))) > (Long.parseLong(rs1.getString("min_date_check"))))
                                        {
                                            writeLogFile("  - Subdirectory listing: " + listings[i].getName());
                                            if (DoLoadMissingFile(pConnection, pFtp, pFtpID, mReceiveFrom, pDirReceive, pRemoteFileSplitByDay, pZipAfterDownload, pRenameAfterDownload, pSeqFrom, pSeqTo, pRenameType, pNewPreFix, pNewExt, listings[i].getName()) != 0)
                                            {
                                                return ( -1);
                                            }
                                        }
                                    }

                                }

                            }
                        }
                    }

                }
                else
                {
                    if (pCurrMissedDir != null && !pCurrMissedDir.equals(""))
                    {
                        mSQL = "select a.id,a.file_name,a.current_dir_missed," + "b.id,b.header_file_send,b.remote_getfile_dir," + "b.local_getfile_dir,b.remote_split_file_by_day," + "b.local_split_file_by_day," + "to_char(sysdate-b.time_check,'yyyymmddhh24miss') min_date_check " + "from missed_file a,data_param b " + "where a.status=0 and a.switch_id=b.id and b.id=" + pFtpID + "and a.current_dir_missed='" + pCurrMissedDir + "'";
                    }
                    else
                    {
                        mSQL = "select a.id,a.file_name,a.current_dir_missed," + "b.id,b.header_file_send,b.remote_getfile_dir," + "b.local_getfile_dir,b.remote_split_file_by_day," + "b.local_split_file_by_day," + "to_char(sysdate-b.time_check,'yyyymmddhh24miss') min_date_check " + "from missed_file a,data_param b " + "where a.status=0 and a.switch_id=b.id and b.id=" + pFtpID;
                    }

                    stmt = mConnection.createStatement();
                    rs = stmt.executeQuery(mSQL);

                    // Duyet cac file bao thieu
                    while (rs.next() && miThreadCommand != THREAD_STOP)
                    {

                        // Kiem tra gia tri Seq cua file trong bang missed_file co trung voi gia tri Seq cua
                        // mot file tren FTP server khong
                        String seqFileStr = "";
                        String seqMissedFileStr = "";
                        if (pRenameAfterDownload == 1)
                        {
                            if (pRenameType == 1)
                            {
                                seqFileStr = listings[i].getName().substring(pSeqFrom - 1, pSeqTo - 1);
                                seqMissedFileStr = rs.getString("file_name").substring(pSeqFrom - 1, pSeqTo - 1);
                            }
                            else
                            {
                                seqFileStr = listings[i].getName().substring(pSeqFrom, pSeqTo);
                                seqMissedFileStr = rs.getString("file_name").substring(pSeqFrom, pSeqTo);
                            }

                        }
                        else
                        {
                            if (pSeqFrom >= 0 && pSeqTo > 0)
                            {
                                try
                                {
                                    seqFileStr = listings[i].getName().substring(pSeqFrom - 1, pSeqTo - 1);
                                    seqMissedFileStr = rs.getString("file_name").substring(pSeqFrom - 1, pSeqTo - 1);
                                }
                                catch (Exception ex)
                                {
                                    seqFileStr = "";
                                    seqMissedFileStr = "";
                                }
                            }
                        }
                        boolean isMissedFile = false;
                        if (!seqFileStr.equals("") && !seqMissedFileStr.equals("") && seqFileStr.equalsIgnoreCase(seqMissedFileStr))
                        {
                            String currMissedDir = (rs.getString("current_dir_missed") == null ? "" : rs.getString("current_dir_missed"));
                            if (currMissedDir.length() > 0 && Global.isNumeric(currMissedDir))
                            {
                                if (sdf2.format(listings[i].lastModified()).equals(currMissedDir))
                                {
                                    isMissedFile = true;
                                }
                            }
                        }
                        // Kiem tra hoac ten file, hoac gia tri Seq cua ten file tren FTP Server
                        // co trung voi ten hoac gia tri Seq cua file trong bang missed_file.
                        if ((rs.getString("file_name").toLowerCase().compareTo(listings[i].getName().toLowerCase()) == 0) || isMissedFile)
                        {
                            if ((Long.parseLong(sdf.format(listings[i].lastModified()))) > (Long.parseLong(rs.getString("min_date_check"))))
                            {
                                mSourceName = IOUtil.FillPath(pReceiveFrom, Global.mSeparate) + listings[i].getName(); //+ rs.getString("file_name");
                                if (Global.mSeparate.compareTo(pDirReceive.substring(pDirReceive.length() - 1)) == 0)
                                {
                                    if (rs.getInt("local_split_file_by_day") == 0)
                                    {
                                        mDestinationName = pDirReceive;
                                    }
                                    else
                                    {
                                        mDestinationName = pDirReceive + rs.getString("current_dir_missed") + Global.mSeparate;
                                    }
                                }
                                else if (rs.getInt("local_split_file_by_day") == 0)
                                {
                                    mDestinationName = pDirReceive + Global.mSeparate;
                                }
                                else
                                {
                                    mDestinationName = pDirReceive + Global.mSeparate + rs.getString("current_dir_missed") + Global.mSeparate;
                                }

                                IOUtil.forceFolderExist(mDestinationName);
                                IOUtil.chmod(new File(mDestinationName), "750");
                                file = new File(mDestinationName);
                                if (file.exists() != true)
                                {
                                    writeLogFile("The system cannot find the path specified : '" + mDestinationName + "'");
                                    return ( -1);
                                }
                                writeLogFile("    .Loading file " + mSourceName + " - Size : " + listings[i].size() + " bytes.");
                                pFtp.get(mDestinationName + "cdr.tmp", mSourceName);
                                file = new File(mDestinationName + "cdr.tmp");
                                if (pRenameAfterDownload == 1)
                                {
                                    if (pRenameType == 1) // HexToDec
                                    {
                                        mNewFileName = pNewPreFix + Global.Format(Global.Hex2Dec(listings[i].getName().substring(pSeqFrom - 1, pSeqTo - 1)), Global.rpad("", pSeqTo - pSeqFrom + 1, "0")) + "." + pNewExt;
                                    }
                                    else
                                    {
                                        mNewFileName = pNewPreFix + Global.Format(Integer.parseInt(listings[i].getName().substring(pSeqFrom, pSeqTo)), Global.rpad("", pSeqTo - pSeqFrom, "0")) + "." + pNewExt;
                                    }
                                }
                                else
                                {
                                    mNewFileName = listings[i].getName();
                                }

                                fileRename = new File(mDestinationName + mNewFileName);
                                file.renameTo(fileRename);
                                fileRename.setLastModified(listings[i].lastModified().getTime());
                                //.getFileDateLongTime());

                                if (fileRename.length() != listings[i].size())
                                {
                                    writeLogFile("    .File downloaded error.Size of file downloaded (" + fileRename.length() + ") not equal file origination.");
                                    return (0);
                                }
                                IOUtil.chmod(fileRename, "750");
                                if (pZipAfterDownload == 1)
                                {

                                    zip.ZipFile(fileRename.getAbsolutePath(), fileRename.getAbsolutePath() + ".zip");
                                    fileRename = new File(fileRename.getAbsolutePath() + ".zip");
                                    //fileRename.setLastModified(listLoad
                                    //.getFileDateLongTime());
                                    fileRename.setLastModified(listings[i].lastModified().getTime());
                                    IOUtil.chmod(fileRename, "750");
                                    IOUtil.deleteFile(mDestinationName + mNewFileName);
                                }

                                mSQL = "update import_header set current_dir = '" + rs.getString("current_dir_missed") + "',status=" + Global.StateFileFtpOK + " where status in (" + Global.StateFileFtpOK + "," + Global.StateConvertedError + ")" + " and file_name='" + listings[i].getName() + "' and ftp_id=" + pFtpID; //+ rs.getString("file_name")
                                mRetValue = Global.ExecuteSQL(pConnection, mSQL);
                                if (mRetValue == 0)
                                {
                                    mSQL = "insert into import_header(status,ftp_id," + "file_name,file_size,date_getfile,current_dir," + "date_createfile) values(" + Global.StateFileFtpOK + "," + pFtpID + ",'" + listings[i].getName() + "'," + listings[i].size() + ",sysdate,'" + (rs.getString("current_dir_missed") == null ? "" : rs.getString("current_dir_missed")) + "','" + sdf.format(listings[i].lastModified()) + "')"; //+ rs.getString("file_name")
                                    Global.ExecuteSQL(pConnection, mSQL);
                                }
                                mSQL = "update missed_file set status=1,date_getfile=sysdate " + "where id=" + rs.getString("id");
                                Global.ExecuteSQL(pConnection, mSQL);
                                pConnection.commit();
                                writeLogFile("   .File "
                                    //+ rs.getString("file_name")
                                    + listings[i].getName() + " had been loaded again successful.");
                            }
                        }

                    }
                    rs.close();
                    stmt.close();
                }
            }
            listings = null;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
        finally
        {
            try
            {
                IOUtil = null;
                rs1.close();
                rs1 = null;
                if (rs != null)
                {
                    rs.close();
                }
                rs = null;
                if (stmt != null)
                {
                    stmt.close();
                }
                stmt = null;
                stmt1.close();
                stmt1 = null; 
            }
            catch (Exception e)
            {
            }
        }
        return (0);
    }

    protected int DoListingRemoteSftp(java.sql.Connection pConnection, Vector pVectorParam, FtpParam plistParam, ChannelSftp channelSftp, int pTimeOut, int pFtpID, String pReceiveFrom, String pDirReceive, String pFileInfo, String pFileName, long pFileSize, String pReceiveHeader, String pReceiveExt, int pRemoteFileSplitByDay, String pCurrentDir, String extToGetFile, int extSeqFrom, int extSeqTo) throws Exception
    {
    	SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
    	SimpleDateFormat sdfComparator = new SimpleDateFormat("yyyyMMddHHmmss");
        String mReceiveFrom = null;
       
        if (extToGetFile == null)
        {
            extToGetFile = "";
        }
        try
        {
            mReceiveFrom = pReceiveFrom;

            channelSftp.cd(pReceiveFrom);

            Vector<LsEntry> fileList = channelSftp.ls(pReceiveFrom);
            for (LsEntry contain : fileList)
            {
                if (contain.getAttrs().isDir())
                {
                    if (!contain.getFilename().startsWith("."))
                    {
                        if (pRemoteFileSplitByDay == 1)
                        {
                            if (Global.isNumeric(contain.getFilename()))
                            {
                                if ((pCurrentDir != null) && ((Integer.parseInt(pCurrentDir)) <= (Integer.parseInt(contain.getFilename()))))
                                {
                                    if ("/".compareTo(pReceiveFrom.substring(pReceiveFrom.length() - 1)) != 0)
                                    {
                                        mReceiveFrom = pReceiveFrom + "/" + contain.getFilename();
                                    }
                                    else
                                    {
                                        mReceiveFrom = pReceiveFrom + contain.getFilename();
                                    }

                                    writeLogFile(" - Subdirectory listing: " + contain.getFilename());
                                    if (DoListingRemoteSftp(pConnection, pVectorParam, plistParam, channelSftp, pTimeOut, pFtpID, mReceiveFrom, pDirReceive, pFileInfo, pFileName, pFileSize, pReceiveHeader, pReceiveExt, pRemoteFileSplitByDay, contain.getFilename(), extToGetFile, extSeqFrom, extSeqTo) != 0)
                                    {
                                        return ( -1);
                                    }
                                }
                            }
                        }
                    }
                }
                else
                {
                	// bo qua cac file bat dau bang .
                	if (contain.getFilename().startsWith(".")) {
                		continue;
                	}
                		
                    if (extToGetFile.trim() != "")
                    {
                        if (contain.getFilename().substring(extSeqFrom, extSeqTo).indexOf(extToGetFile) >= 0)
                        {
                      	
                            if (Long.parseLong(sdfComparator.format(sdf.parse(contain.getAttrs().getMtimeString()))) > Long.parseLong(pFileInfo))
                            {
                                listLoad = new ListData(mReceiveFrom, contain.getFilename(), contain.getAttrs().getSize(), sdfComparator.format(sdf.parse(contain.getAttrs().getMtimeString())), pCurrentDir, sdf.parse(contain.getAttrs().getMtimeString()).getTime());
                                vListDownload.add(listLoad);

                                plistParam = (FtpParam) pVectorParam.get(iFtpID);
                                if (contain.getFilename().compareTo(plistParam.getFileName()) == 0)
                                {
                                    if (contain.getAttrs().getSize() == plistParam.getFileSize())
                                    {
                                        plistParam.setStatus("Already");
                                    }
                                    else
                                    {
                                        plistParam.setFileSize(contain.getAttrs().getSize());
                                    }
                                } // end if check file size
                                else
                                {
                                    plistParam.setStatus("Wait");
                                    plistParam.setFileName(contain.getFilename());
                                    plistParam.setFileSize(contain.getAttrs().getSize());
                                } // end if check file name need download
                            } // end if check new FileInfo higheer old
                            
                            // FileInfo
                            else if (Long.parseLong(sdfComparator.format(sdf.parse(contain.getAttrs().getMtimeString()))) == Long.parseLong(pFileInfo))
                            {
                                // Check if is a file downloaded then step
                                // get file next
                                if (((pFileName.toLowerCase().compareTo(contain.getFilename().toLowerCase()) == 0) && (pFileSize < contain.getAttrs().getSize())) || (pFileName.toLowerCase().compareTo(contain.getFilename().toLowerCase()) < 0))
                                {
                                    listLoad = new ListData(mReceiveFrom, contain.getFilename(), contain.getAttrs().getSize(), sdfComparator.format(sdf.parse(contain.getAttrs().getMtimeString())), pCurrentDir, sdf.parse(contain.getAttrs().getMtimeString()).getTime());
                                    vListDownload.add(listLoad);

                                    plistParam = (FtpParam) pVectorParam.get(iFtpID);
                                    if (contain.getFilename().compareTo(plistParam.getFileName()) == 0)
                                    {
                                        if (contain.getAttrs().getSize() == plistParam.getFileSize())
                                        {
                                            plistParam.setStatus("Already");
                                        }
                                    } // end if check file size
                                    else
                                    {
                                        plistParam.setStatus("Wait");
                                        plistParam.setFileName(contain.getFilename());
                                        plistParam.setFileSize(contain.getAttrs().getSize());
                                    }
                                } // end if check file name downloaded
                            } // end else if
                        }
                    }
                    else
                    {
                        if ((pReceiveExt == null) || (contain.getFilename().endsWith(pReceiveExt) == true) || (contain.getFilename().substring(contain.getFilename().lastIndexOf(".") + 1).indexOf(pReceiveExt) >= 0))
                        {
                            if ((pReceiveHeader == null) || (pReceiveHeader.length() < contain.getFilename().length()))
                            {
                                if ((pReceiveHeader == null) || (pReceiveHeader.toLowerCase().compareTo(contain.getFilename().substring(0, pReceiveHeader.length()).toLowerCase()) == 0))
                                {
                                    // Check FileInfo

                                    if (Long.parseLong(sdfComparator.format(sdf.parse(contain.getAttrs().getMtimeString()))) > Long.parseLong(pFileInfo))
                                    {
                                        listLoad = new ListData(mReceiveFrom, contain.getFilename(), contain.getAttrs().getSize(), sdfComparator.format(sdf.parse(contain.getAttrs().getMtimeString())), pCurrentDir, sdf.parse(contain.getAttrs().getMtimeString()).getTime());
                                        vListDownload.add(listLoad);

                                        plistParam = (FtpParam) pVectorParam.get(iFtpID);
                                        if (contain.getFilename().compareTo(plistParam.getFileName()) == 0)
                                        {
                                            if (contain.getAttrs().getSize() == plistParam.getFileSize())
                                            {
                                                plistParam.setStatus("Already");
                                            }
                                            else
                                            {
                                                plistParam.setFileSize(contain.getAttrs().getSize());
                                            }
                                        } // end if check file size
                                        else
                                        {
                                            plistParam.setStatus("Wait");
                                            plistParam.setFileName(contain.getFilename());
                                            plistParam.setFileSize(contain.getAttrs().getSize());
                                        } // end if check file name need download
                                    } // end if check new FileInfo higheer old
                                    
                                    // FileInfo
                                    else if (Long.parseLong(sdfComparator.format(sdf.parse(contain.getAttrs().getMtimeString()))) == Long.parseLong(pFileInfo))
                                    {
                                        // Check if is a file downloaded then step
                                        // get file next
                                        if (((pFileName.toLowerCase().compareTo(contain.getFilename().toLowerCase()) == 0) && (pFileSize < contain.getAttrs().getSize())) || (pFileName.toLowerCase().compareTo(contain.getFilename().toLowerCase()) < 0))
                                        {
                                            listLoad = new ListData(mReceiveFrom, contain.getFilename(), contain.getAttrs().getSize(), sdfComparator.format(sdf.parse(contain.getAttrs().getMtimeString())), pCurrentDir, sdf.parse(contain.getAttrs().getMtimeString()).getTime());
                                            vListDownload.add(listLoad);

                                            plistParam = (FtpParam) pVectorParam.get(iFtpID);
                                            if (contain.getFilename().compareTo(plistParam.getFileName()) == 0)
                                            {
                                                if (contain.getAttrs().getSize() == plistParam.getFileSize())
                                                {
                                                    plistParam.setStatus("Already");
                                                }
                                            } // end if check file size
                                            else
                                            {
                                                plistParam.setStatus("Wait");
                                                plistParam.setFileName(contain.getFilename());
                                                plistParam.setFileSize(contain.getAttrs().getSize());
                                            }
                                        } // end if check file name downloaded
                                    } // end else if
                                } // end if check header file download
                            }
                        } // end if check file tmp

                    }
                }
            }
            fileList = null;
        }
        catch (StringIndexOutOfBoundsException e)
        {
            writeLogFile(" Warning: substring ext file name. ");
        }
        catch (Exception ex)
        {

            throw ex;
        }

        finally
        {
            sdf = null;
            mReceiveFrom = null;
        }
        return (0);
    }

    protected void DoSFtp(java.sql.Connection pConnection, Vector pVectorParam, FtpParam plistParam, int pFtpID, String pNote, String pHost, String pUID, String pPWD, String pConnectMode, int pTimeOut, String pReceiveFrom, String pDirReceive, String pFileInfo, String pFileName, long pFileSize, String pReceiveHeader, String pReceiveExt, String pDirSend, String pSendTo, String pFTPMode, int pRemoteFileSplitByDay, String pCurrentDir, int pLocalFileSplitByDay, int pZipAfterDownload,
            int pRenameAfterDownload, int pSeqFrom, int pSeqTo, int pRenameType, String pNewPreFix, String pNewExt, String pMailTo, int pSeqAfterDownload, int pMinSeq, int pMaxSeq, int pCurrSeq, int pWaitingLastFile, String extToGetFile, long fileSize, int extSeqFrom, int extSeqTo, int checkSizeForgetFile) throws Exception
        {
            try
            {
                writeLogFile("Connecting to host " + pNote + "=>" + pHost);
                

                pConnection.setAutoCommit(false);
                Global.ExecuteSQL(pConnection, "alter session set nls_date_format='yyyyMMddhh24miss'");
                //Clear download list.
                if (vListDownload != null)
                {
                    vListDownload.removeAllElements();
                    vListDownload.clear();
                }
                
                jsch = new JSch();
    			session = jsch.getSession(pUID, pHost, 22);
    			session.setPassword(pPWD);
    			Properties config = new Properties();
    			config.put("StrictHostKeyChecking", "no");
    			session.setConfig(config);
    			session.connect();
    			channel = session.openChannel("sftp");
                channel.connect(1000);
                channelSftp = (ChannelSftp)channel;

                if ((pReceiveFrom != "") && (pReceiveFrom != null))
                {
                    if (DoListingRemoteSftp(pConnection, pVectorParam, plistParam, channelSftp, pTimeOut, pFtpID, pReceiveFrom, pDirReceive, pFileInfo, pFileName, pFileSize, pReceiveHeader, pReceiveExt, pRemoteFileSplitByDay, pCurrentDir, extToGetFile, extSeqFrom, extSeqTo) == 0)
                    {
                        if (vListDownload.size() > 0)
                        {
                            DoDownloadSftp(pConnection, pVectorParam, plistParam, channelSftp, pFtpID, pDirReceive, pRemoteFileSplitByDay, pLocalFileSplitByDay, pZipAfterDownload, pRenameAfterDownload, pSeqFrom, pSeqTo, pRenameType, pNewPreFix, pNewExt, pSeqAfterDownload, pMinSeq, pMaxSeq, pCurrSeq, pWaitingLastFile, pFileSize, pMailTo, fileSize, checkSizeForgetFile);
                        }
                    }
                    else
                    {
                        return;
                    }
                    if (Global.ExecuteOutParameterInt(pConnection, "SELECT count(*) INTO ? " + "FROM sys_param a,sys_param_detail b " + "WHERE a.id = b.ptr_id and " + "b.ptr_name='AutoDownloadMissedFile' " + "AND ptr_value='TRUE'") > 0)
                    {
                        writeLogFile(" Getting missed file of " + pNote + "=>" + pHost);
                        DoLoadMissingFileSftp(pConnection, channelSftp, pFtpID, pReceiveFrom, pDirReceive, pRemoteFileSplitByDay, pZipAfterDownload, pRenameAfterDownload, pSeqFrom, pSeqTo, pRenameType, pNewPreFix, pNewExt, "");
                        writeLogFile(" End get missed file of " + pNote + "...");
                    }
                }

                if ((pSendTo != "") && (pSendTo != null))
                {
                    writeLogFile("Uploading file to " + pSendTo + "...");
                    DoUploadSftp(pConnection, channelSftp, pFtpID, pDirSend, pSendTo, pLocalFileSplitByDay, pCurrentDir, pRemoteFileSplitByDay, pFileInfo, pFileName, pFileSize);
                    writeLogFile("Finish upload file.");
                }
                
                writeLogFile("Disconnecting " + pNote + "...\r\n");
            }
            catch (FTPException ex)
            {
                switch (ex.getReplyCode())
                {
                case 530:
                    writeLogFile(ex.getReplyCode() + " - Invalid user name or password:" + pNote);
                    General.SendMail(mConnection, 1, pMailTo, ex.getReplyCode() + " - " + ex.getMessage() + " " + pNote);

                    /*General.addNewSMS(mConnection,pFtpID, 2,
                                                     "Invalid user name or password:" + pNote);*/


                    break;
                case 550:
                    writeLogFile(ex.getReplyCode() + " - " + ex.getMessage());
                    General.SendMail(mConnection, 1, pMailTo, ex.getReplyCode() + " - " + ex.getMessage() + " " + pNote);

                    /*General.addNewSMS(mConnection, pFtpID,1,
                                                    "Ftp has occurred error: - 550 " + pNote);*/


                    break;
                default:
                    writeLogFile(ex.getReplyCode() + " - " + ex.getMessage());
                    General.SendMail(mConnection, 1, pMailTo, ex.getReplyCode() + " - " + ex.getMessage() + " " + pNote);

                    /*General.addNewSMS(mConnection, pFtpID,1,
                                                    "Ftp thread has occurred error " + pNote);*/


                    break;
                }
            }
            catch (ConnectException e)
            {
                writeLogFile(" - ConnectException - " + e.getMessage() + "\r\n");
                General.SendMail(mConnection, 1, pMailTo, e.getMessage() + " " + pNote);
                /*General.addNewSMS(mConnection,pFtpID, 1,
                        "ConnectException " + pNote);*/
            }
            catch (NoRouteToHostException e)
            {
                writeLogFile(" - NoRouteToHostException - " + e.getMessage() + "\r\n");
                General.SendMail(mConnection, 1, pMailTo, e.getMessage() + " " + pNote);
                /*General.addNewSMS(mConnection,pFtpID, 1,
                                        "NoRouteToHostException " + pNote);*/

            }
            catch (SocketTimeoutException e)
            {
                writeLogFile(" - SocketTimeoutException - " + e.getMessage() + "\r\n");
                General.SendMail(mConnection, 1, pMailTo, e.getMessage() + " " + pNote);
                /*General.addNewSMS(mConnection,pFtpID, 1,
                                        "SocketTimeoutException " + pNote);*/

            }
            catch (SocketException e)
            {
                writeLogFile(" - SocketException - " + e.getMessage() + "\r\n");
                General.SendMail(mConnection, 1, pMailTo, e.getMessage() + " " + pNote);
                /*General.addNewSMS(mConnection,pFtpID, 1,
                                        "SocketException " + pNote);*/

            }
            catch (Exception e)
            {
                if (cdrfileParam.OnErrorResumeNext.compareTo("TRUE") == 0)
                {
                    writeLogFile(" - " + e.toString());
                }
                else
                {
                    System.out.println(" - " + e.toString());
                    General.SendMail(mConnection, 1, pMailTo, e.toString() + " " + pNote);
                    System.err.println(Global.Format(new java.util.Date(), "dd/MM/yyyy HH:mm:ss") + " : - ERROR in module DoFtp : " + e.toString());
                    /*General.addNewSMS(mConnection, pFtpID,1,
                                                  "ERROR in module DoFtp " + pNote);*/


                    throw e;
                }
            }
            finally
            {
            	session.disconnect();
            	channel.disconnect();
            	channelSftp.disconnect();
                if (vListDownload != null)
                {
                    vListDownload.removeAllElements();
                    vListDownload.clear();
                }
            }
        }
    protected int DoDownloadSftp(java.sql.Connection pConnection, Vector pVectorParam, FtpParam plistParam, ChannelSftp channelSftp, int pFtpID, String pDirReceive, int pRemoteFileSplitByDay, int pFileSplitByDay, int pZipAfterDownload, int pRenameAfterDownload, int pSeqFrom, int pSeqTo, int pRenameType, String pNewPreFix, String pNewExt, int pSeqAfterDownload, int pMinSeq, int pMaxSeq, int pCurrSeq, int pWaitingLastFile, long pFileSizeDownloaded, String pMailTo, long pFileSizeForDownload, int checkSizeForGetFile) throws Exception
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        File file = null;
        File fileRename = null;
        String mSourceName = null;
        String mDestinationName = null;
        String mNewFileName = null;
        String mSQL = null;
        int mRetValue;
        Statement stmt = null;
        String mCurrentPath = "";
        IOUtils IOUtil = new IOUtils();
        SmartZip zip = new SmartZip();
        long mFileSizePrevious = 0;
        try
        {
            stmt = pConnection.createStatement();

            // Read param from vector pVectorParam
            plistParam = (FtpParam) pVectorParam.get(iFtpID);
            sort(plistParam);
            int size = 0;
            if (pWaitingLastFile > 0)
            {
                ////bo qua nhung file cuoi theo thoi gian./////
                Calendar cal = Calendar.getInstance();
                Date date = new Date();
                date.setTime(fileMaxLongTime);
                cal.setTime(date);
                cal.add(Calendar.MINUTE, -pWaitingLastFile);
                long longTime = cal.getTimeInMillis();
                int inValidFile = 0;
                for(int i = 0; i < vListDownload.size(); i++)
                {
                    ListData fileIndex = (ListData) vListDownload.get(i);
                    if(fileIndex.getFileDateLongTime() > longTime)
                    {
                        inValidFile++;
                    }
                }
                size = vListDownload.size() - inValidFile;
                if(size >= 0)
                {
                    writeLogFile("Skipped last files with file's time after " + cal.getTime() + ", getting list following files:");

                }
                ////ket thuc bo file cuoi cung theo thoi gian./////


//                size = vListDownload.size() - 1;
//                if (size >= 0)
//                {
//                    ListData lastListData = (ListData) vListDownload.get(size);
//                    String path = IOUtil.FillPath(lastListData.getFilePath(), Global.mSeparate) + lastListData.getFileName();
//                    writeLogFile("Skipped last file: " + path + ", getting list following files:");
//                }
            }
            else
            {
                size = vListDownload.size();
            }
            for (int i = 0; i < size; i++)
            {
                if (miThreadCommand != THREAD_STOP)
                {
                    listLoad = (ListData) vListDownload.get(i);
                    mCurrentPath = "";

                    //Ngay 28/2: check file size.
                    if (checkSizeForGetFile == 1)
                    {
                        if (size == 1)
                        {
                            if (listLoad.getFileSize() < (pFileSizeForDownload * pFileSizeDownloaded) / 100)
                            {
                                writeLogFile("Warning: This file size is unusual reduction. This file name:" + listLoad.getFileName() + ", file's date is: " + convertLongToDate(listLoad.getFileDateLongTime()));
                                General.SendMail(mConnection, 1, pMailTo, "Warning: This file size is unusual reduction. This file name:" + listLoad.getFileName() + ", file date is: " + convertLongToDate(listLoad.getFileDateLongTime()));
                            }
                        }
                        else
                        {
                            if (i == 0)
                            {
                                if (listLoad.getFileSize() < (pFileSizeForDownload * pFileSizeDownloaded) / 100)
                                {
                                    writeLogFile("Warning: This file size is unusual reduction. This file name:" + listLoad.getFileName() + ", file's date is: " + convertLongToDate(listLoad.getFileDateLongTime()));
                                    General.SendMail(mConnection, 1, pMailTo, "Warning: This file size is unusual reduction. This file name:" + listLoad.getFileName() + ", file date is: " + convertLongToDate(listLoad.getFileDateLongTime()));
                                }
                                mFileSizePrevious = listLoad.getFileSize();
                            }
                            else
                            {
                                if (mFileSizePrevious < (pFileSizeForDownload * listLoad.getFileSize()) / 100)
                                {
                                    writeLogFile("Warning: This file size is unusual reduction. This file name:" + listLoad.getFileName() + ", file's date is: " + convertLongToDate(listLoad.getFileDateLongTime()));
                                    General.SendMail(mConnection, 1, pMailTo, "Warning: This file size is unusual reduction. This file name:" + listLoad.getFileName() + ", file date is: " + convertLongToDate(listLoad.getFileDateLongTime()));
                                }
                                mFileSizePrevious = listLoad.getFileSize();
                            }
                        }
                    }
                    //Ket thuc check file size.
                    if (listLoad.getFileName().compareTo(plistParam.getFileName()) != 0)
                    {
                        mSourceName = IOUtil.FillPath(listLoad.getFilePath(), Global.mSeparate) + listLoad.getFileName();
                        if (Global.mSeparate.compareTo(pDirReceive.substring(pDirReceive.length() - 1)) == 0)
                        {
                            if (pFileSplitByDay == 0)
                            {
                                mDestinationName = pDirReceive;
                            }
                            else
                            {
                                mCurrentPath = sdf.format(new java.util.Date(listLoad.getFileDateLongTime()));
                                mDestinationName = pDirReceive + mCurrentPath + Global.mSeparate;
                            }
                        }
                        else if (pFileSplitByDay == 0)
                        {
                            mDestinationName = pDirReceive + Global.mSeparate;
                        }
                        else
                        {
                            mCurrentPath = sdf.format(new java.util.Date(listLoad.getFileDateLongTime()));
                            mDestinationName = pDirReceive + Global.mSeparate + mCurrentPath + Global.mSeparate;
                        }

                        IOUtil.forceFolderExist(mDestinationName);
                        IOUtil.chmod(new File(mDestinationName), "750");
                        file = new File(mDestinationName);
                        if (file.exists() != true)
                        {
                            writeLogFile("The system cannot find the path specified : '" + mDestinationName + "'");
                            return ( -1);
                        }
                        writeLogFile("   .Loading file " + mSourceName + " - Size : " + listLoad.getFileSize() + " bytes.");

                        channelSftp.get(mSourceName, mDestinationName + listLoad.getFileName() + ".tmp");
                        file = new File(mDestinationName + listLoad.getFileName() + ".tmp");

                        if (pRenameAfterDownload == 1)
                        {
                            if (pRenameType == 1) // HexToDec
                            {
                                mNewFileName = pNewPreFix + Global.Format(Global.Hex2Dec(listLoad.getFileName().substring(pSeqFrom - 1, pSeqTo - 1)), Global.rpad("", pSeqTo - pSeqFrom + 1, "0")) + "." + pNewExt;
                            }
                            else
                            {
                                mNewFileName = pNewPreFix + Global.Format(Integer.parseInt(listLoad.getFileName().substring(pSeqFrom, pSeqTo)), Global.rpad("", pSeqTo - pSeqFrom, "0")) + "." + pNewExt;
                            }
                        }
                        else
                        {
                            mNewFileName = listLoad.getFileName();
                        }
                        if (pSeqAfterDownload == 1)
                        {
                            if (pCurrSeq < pMinSeq)
                            {
                                pCurrSeq = pMinSeq;
                            }
                            if (pCurrSeq > pMaxSeq)
                            {
                                pCurrSeq = pMinSeq;
                            }
                            int pos = mNewFileName.lastIndexOf(".");
                            mNewFileName = mNewFileName.substring(0, pos) + "." + Global.Format(pCurrSeq, Global.rpad("", pSeqTo - pSeqFrom, "0")) + mNewFileName.substring(pos, mNewFileName.length());

                            pCurrSeq++;
                        }
                        fileRename = new File(mDestinationName + mNewFileName);
                        file.renameTo(fileRename);
                        IOUtil.chmod(fileRename, "750");
                        fileRename.setLastModified(listLoad.getFileDateLongTime());
                        if (fileRename.length() != listLoad.getFileSize())
                        {
                            writeLogFile("   .File downloaded error.Size of file downloaded (" + fileRename.length() + ") not equal file origination.");
                            return (0);
                        }
                        if (pZipAfterDownload == 1)
                        {
                            zip.ZipFile(fileRename.getAbsolutePath(), fileRename.getAbsolutePath() + ".zip");
                            fileRename = new File(fileRename.getAbsolutePath() + ".zip");
                            IOUtil.chmod(fileRename, "750");
                            fileRename.setLastModified(listLoad.getFileDateLongTime());

                            IOUtil.deleteFile(mDestinationName + mNewFileName);
                        }

                        mSQL = "update import_header set current_dir = '" + mCurrentPath + "',file_size=" + listLoad.getFileSize() + ",status=" + Global.StateFileFtpOK + " where status in (" + Global.StateFileFtpOK + "," + Global.StateConvertedError + ")" + " and file_name='" + mNewFileName + "' and ftp_id=" + pFtpID + " and date_createfile ='" + listLoad.getFileInfo() + "'";
                        mRetValue = stmt.executeUpdate(mSQL);
                        if (mRetValue == 0)
                        {
                            if (pRenameAfterDownload == 1)
                            {
                                mSQL = "insert into import_header(status,ftp_id," + "file_name,file_size,date_getfile,current_dir," + "date_createfile,file_name_org) values(" + Global.StateFileFtpOK + "," + pFtpID + ",'" + mNewFileName + "'," + listLoad.getFileSize() + ",sysdate,'" + mCurrentPath + "','" + listLoad.getFileInfo() + "','" + listLoad.getFileName() + "')";
                            }
                            else
                            {
                                mSQL = "insert into import_header(status,ftp_id," + "file_name,file_size,date_getfile,current_dir," + "date_createfile) values(" + Global.StateFileFtpOK + "," + pFtpID + ",'" + mNewFileName + "'," + listLoad.getFileSize() + ",sysdate,'" + mCurrentPath + "','" + listLoad.getFileInfo() + "')";
                            }
                            stmt.executeUpdate(mSQL);
                        }

                        if (pRemoteFileSplitByDay == 1)
                        {
                            mSQL = "update data_param set file_name='" + listLoad.getFileName() + "',file_size=" + listLoad.getFileSize() + ",file_info='" + listLoad.getFileInfo() + "',dir_current='" + listLoad.getFileDir() + "', curr_seq=" + pCurrSeq + " where id=" + pFtpID;
                        }
                        else
                        {
                            mSQL = "update data_param set file_name='" + listLoad.getFileName() + "',file_size=" + listLoad.getFileSize() + ",file_info='" + listLoad.getFileInfo() + "', curr_seq=" + pCurrSeq + " where id=" + pFtpID;
                        }

                        stmt.executeUpdate(mSQL);
                        pConnection.commit();
                        writeLogFile("   .File " + listLoad.getFileName() + " had been loaded successful.");
                    }
                    else
                    {
                        // Kiem tra xem kich thuoc file cuoi cung co thay doi
                        // khong
                        if (("Already".compareTo(plistParam.getStatus()) == 0) || (i < vListDownload.size() - 1))
                        {
                            mSourceName = IOUtil.FillPath(listLoad.getFilePath(), Global.mSeparate) + listLoad.getFileName();

                            if (Global.mSeparate.compareTo(pDirReceive.substring(pDirReceive.length() - 1)) == 0)
                            {
                                if (pFileSplitByDay == 0)
                                {
                                    mDestinationName = pDirReceive;
                                }
                                else
                                {
                                    mCurrentPath = sdf.format(new java.util.Date(listLoad.getFileDateLongTime()));
                                    mDestinationName = pDirReceive + mCurrentPath + Global.mSeparate;
                                }
                            }
                            else if (pFileSplitByDay == 0)
                            {
                                mDestinationName = pDirReceive + Global.mSeparate;
                            }
                            else
                            {
                                mCurrentPath = sdf.format(new java.util.Date(listLoad.getFileDateLongTime()));
                                mDestinationName = pDirReceive + Global.mSeparate + mCurrentPath + Global.mSeparate;
                            }
                            IOUtil.forceFolderExist(mDestinationName);
                            
                            // comment lay de chay duoc tren windows os
                      //      IOUtil.chmod(new File(mDestinationName), "750");
                            file = new File(mDestinationName);
                            if (file.exists() != true)
                            {
                                System.out.println("*********************************************************************");
                                System.out.println("The system cannot find the path specified : '" + mDestinationName + "'");
                                System.out.println("*********************************************************************");
                                return ( -1);
                            }
                            writeLogFile("   .Loading file " + mSourceName + " - Size : " + listLoad.getFileSize() + " bytes.");

                            try {
                            	channelSftp.get(mSourceName, mDestinationName + listLoad.getFileName() + ".tmp");
                            	file = new File(mDestinationName + listLoad.getFileName() + ".tmp");
                            } catch (SftpException e) {
                            	e.printStackTrace();
                            }
                            
                            if (pRenameAfterDownload == 1)
                            {
                                if (pRenameType == 1) // HexToDec
                                {
                                    mNewFileName = pNewPreFix + Global.Format(Global.Hex2Dec(listLoad.getFileName().substring(pSeqFrom - 1, pSeqTo - 1)), Global.rpad("", pSeqTo - pSeqFrom + 1, "0")) + "." + pNewExt;
                                }
                                else
                                {
                                    mNewFileName = pNewPreFix + Global.Format(Integer.parseInt(listLoad.getFileName().substring(pSeqFrom, pSeqTo)), Global.rpad("", pSeqTo - pSeqFrom, "0")) + "." + pNewExt;
                                }
                            }
                            else
                            {
                                mNewFileName = listLoad.getFileName();
                            }
                            if (pSeqAfterDownload == 1)
                            {
                                if (pCurrSeq < pMinSeq)
                                {
                                    pCurrSeq = pMinSeq;
                                }
                                if (pCurrSeq > pMaxSeq)
                                {
                                    pCurrSeq = pMinSeq;
                                }
                                int pos = mNewFileName.lastIndexOf(".");
                                mNewFileName = mNewFileName.substring(0, pos) + "." + Global.Format(pCurrSeq, Global.rpad("", pSeqTo - pSeqFrom, "0")) + mNewFileName.substring(pos, mNewFileName.length());
                                pCurrSeq++;
                            }
                            fileRename = new File(mDestinationName + mNewFileName);
                            file.renameTo(fileRename);
                            //IOUtil.chmod(fileRename, "750");
                            fileRename.setLastModified(listLoad.getFileDateLongTime());

                            if (fileRename.length() != listLoad.getFileSize())
                            {
                                writeLogFile("   .File downloaded error.Size of file downloaded (" + fileRename.length() + ") not equal file origination.");
                                return (0);
                            }
                            if (pZipAfterDownload == 1)
                            {
                                zip.ZipFile(fileRename.getAbsolutePath(), fileRename.getAbsolutePath() + ".zip");
                                fileRename = new File(fileRename.getAbsolutePath() + ".zip");
                                //IOUtil.chmod(fileRename, "750");
                                fileRename.setLastModified(listLoad.getFileDateLongTime());

                                IOUtil.deleteFile(mDestinationName + mNewFileName);
                            }
                            mSQL = " update import_header set current_dir = '" + mCurrentPath + "',status=" + Global.StateFileFtpOK + " where status in (" + Global.StateFileFtpOK + "," + Global.StateConvertedError + ")" + " and file_name='" + mNewFileName + "' and ftp_id=" + pFtpID + " and date_createfile ='" + listLoad.getFileInfo() + "'";

                            mRetValue = stmt.executeUpdate(mSQL);
                            if (mRetValue == 0)
                            {
                                if (pRenameAfterDownload == 1)
                                {
                                    mSQL = "insert into import_header(status,ftp_id," + "file_name,file_size,date_getfile,current_dir," + "date_createfile,file_name_org) values(" + Global.StateFileFtpOK + "," + pFtpID + ",'" + mNewFileName + "'," + listLoad.getFileSize() + ",sysdate,'" + mCurrentPath + "','" + listLoad.getFileInfo() + "','" + listLoad.getFileName() + "')";
                                }
                                else
                                {
                                    mSQL = "insert into import_header(status,ftp_id," + "file_name,file_size,date_getfile,current_dir," + "date_createfile) values(" + Global.StateFileFtpOK + "," + pFtpID + ",'" + mNewFileName + "'," + listLoad.getFileSize() + ",sysdate,'" + mCurrentPath + "','" + listLoad.getFileInfo() + "')";
                                }
                                stmt.executeUpdate(mSQL);
                            }

                            if (pRemoteFileSplitByDay == 1)
                            {
                                mSQL = "update data_param set file_name='" + listLoad.getFileName() + "',file_size=" + listLoad.getFileSize() + ",file_info='" + listLoad.getFileInfo() + "',dir_current='" + listLoad.getFileDir() + "', curr_seq=" + pCurrSeq + " where id=" + pFtpID;
                            }
                            else
                            {
                                mSQL = "update data_param set file_name='" + listLoad.getFileName() + "',file_size=" + listLoad.getFileSize() + ",file_info='" + listLoad.getFileInfo() + "', curr_seq=" + pCurrSeq + " where id=" + pFtpID;
                            }

                            stmt.executeUpdate(mSQL);
                            pConnection.commit();
                            writeLogFile("   .File " + listLoad.getFileName() + " had been loaded successfull.");
                        }
                    }
                }
                // Thread.sleep(50);
                if (((new java.util.Date().getTime() / 60000) - plistParam.getTimeCurrentConnect()) >= plistParam.getTimeDownoad())
                {
                    writeLogFile("The end time for this connection : time current -> " + (new java.util.Date().getTime() / 60000) + " - time started -> " + plistParam.getTimeCurrentConnect() + " >= time download -> " + plistParam.getTimeDownoad());
                    return (0);
                }
            }
            // stmtmSQL.close();
        } // endif try catch
        catch (Exception e)
        {
            e.printStackTrace();;
        }
        finally
        {
            try
            {
                IOUtil = null;
                file = null;
                fileRename = null;
                mSourceName = null;
                mDestinationName = null;
                mSQL = null;

                if (vListDownload != null)
                {
                    vListDownload.removeAllElements();
                    vListDownload.clear();
                }
                stmt.close();
                stmt = null;
            }
            catch (Exception e)
            {
            }
        }
        System.runFinalization();
        System.gc();
        return (0);
    }
    protected int DoUploadSftp(java.sql.Connection pConnection, ChannelSftp channelSftp, int pFtpID, String pDirSend, String pSendTo, int pLocalFileSplitByDay, String pCurrentDir, int pRemoteSplitFileByDay, String pFileInfo, String pFileName, long pFileSize) throws Exception
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat daySDF = new SimpleDateFormat("yyyyMMdd");
        String mDirSend = null;
        String mDesPath = null;
        IOUtils IOUtil = new IOUtils();
        String mSQL = "";
        String mCurrentPath = "";
        File pFile = null;
        Statement stmt = null;
        try
        {
            stmt = pConnection.createStatement();
            File dirSend = new File(pDirSend);
            File[] files = dirSend.listFiles();
            Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
            if (files == null || files.length == 0)
            {
                writeLogFile("   .No files for upload from " + pDirSend + " to " + pSendTo);
            }
            else
            {
                for (int i = 0; i < files.length; i++)
                {
                    pFile = files[i];
                    if (pFile.isDirectory())
                    {
                        if (pLocalFileSplitByDay == 1)
                        {
                            if (Global.isNumeric(pFile.getName()))
                            {
                                if ((pCurrentDir != null) && ((Integer.parseInt(pCurrentDir)) <= (Integer.parseInt(pFile.getName()))))
                                {
                                    if ("/".compareTo(pDirSend.substring(pDirSend.length() - 1)) != 0)
                                    {
                                        mDirSend = pDirSend + "/" + pFile.getName();
                                    }
                                    else
                                    {
                                        mDirSend = pDirSend + pFile.getName();
                                    }
                                    writeLogFile(" - Subdirectory uploading: " + pFile.getName());
                                    if (DoUploadSftp(pConnection, channelSftp, pFtpID, mDirSend, pSendTo, pLocalFileSplitByDay, pFile.getName(), pRemoteSplitFileByDay, pFileInfo, pFileName, pFileSize)!= 0)
                                        return -1;
                                }
                            }
                        }
                    }
                    else
                    {
                        if (pFile.getName().toLowerCase().endsWith(".tmp") || pFile.getName().startsWith("."))
                            continue;
                        long lastModified = Long.parseLong(sdf.format(new java.util.Date(pFile.lastModified())));
                        if (lastModified > Long.parseLong(pFileInfo) || (lastModified == Long.parseLong(pFileInfo) && (((pFileName.toLowerCase().compareTo(pFile.getName().toLowerCase()) == 0) && (pFileSize < pFile.length())) || (pFileName.toLowerCase().compareTo(pFile.getName().toLowerCase()) < 0))))
                        {
                            if (pRemoteSplitFileByDay == 1)
                            {
                                mCurrentPath = daySDF.format(new java.util.Date(pFile.lastModified()));
                                mDesPath = IOUtil.FillPath(pSendTo, Global.mSeparate) + mCurrentPath;
                            }
                            else
                            {
                                mDesPath = IOUtil.FillPath(pSendTo, Global.mSeparate);
                            }
                            try{
                                channelSftp.mkdir(mDesPath);
                            }catch (Exception ex){
                            }
                            writeLogFile("   .Uploading file " + pFile.getName());
                            String mFileName = IOUtil.FillPath(mDesPath, Global.mSeparate) + pFile.getName() ;
                            channelSftp.put(pFile.getPath(), mFileName );
                            writeLogFile("   .Finish upload file " + pFile.getName() + " successfully.");

                            mSQL = "update export_header set current_dir = '" + mCurrentPath + "',file_size=" + pFile.length() + ",status= 1 where file_name='" + pFile.getName() + "' and ftp_id=" + pFtpID;
                            int mRetValue = stmt.executeUpdate(mSQL);
                            if (mRetValue == 0)
                            {
                                mSQL = "insert into export_header(status,ftp_id,"
                                         + "file_name,file_size,date_putfile,current_dir,"
                                         + "date_createfile) values("
                                         + "1," + pFtpID + ",'" + pFile.getName() + "',"
                                         + pFile.length() + ",sysdate,'" + mCurrentPath
                                         + "','" + lastModified + "')";
                                stmt.executeUpdate(mSQL);
                            }
                            if (pLocalFileSplitByDay == 1)
                            {
                                mSQL = "update data_param set file_name='" + pFile.getName() + "',file_size=" + pFile.length() + ",file_info='" + lastModified + "',dir_current='" + mCurrentPath + "' where id=" + pFtpID;
                            }
                            else
                            {
                                mSQL = "update data_param set file_name='" + pFile.getName()+ "',file_size=" + pFile.length()  + ",file_info='" + lastModified + "' where id=" + pFtpID;
                            }
                            Global.ExecuteSQL(pConnection, mSQL);
                            pConnection.commit();
                        }
                    }
                }
            }
        }
        catch (Exception ex)
        {
            writeLogFile("Upload Error Details: " + ex.toString());
            Global.writeEventThreadErr(Integer.parseInt(getThreadID()), 2, ex.toString());
//            ex.printStackTrace();
            throw ex;
        }finally{
        	
            try
            {
                if (stmt!= null)
                    stmt.close();
                stmt.close();
//                pFtp.quit();
//                pFtp = null;
            }
            catch (Exception e)
            {
            }
        }
        return 0;
    }
    protected int DoLoadMissingFileSftp(java.sql.Connection pConnection, ChannelSftp channelSftp, int pFtpID, String pReceiveFrom, String pDirReceive, int pRemoteFileSplitByDay, int pZipAfterDownload, int pRenameAfterDownload, int pSeqFrom, int pSeqTo, int pRenameType, String pNewPreFix, String pNewExt, String pCurrMissedDir) throws Exception
    {
    	SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
    	SimpleDateFormat sdfComparator = new SimpleDateFormat("yyyyMMddHHmmss");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMdd");
        File file = null;
        File fileRename = null;
        String mSourceName = null;
        String mDestinationName = null;
        int mRetValue;
        String mReceiveFrom = null;
        String mNewFileName = null;
        IOUtils IOUtil = new IOUtils();
        ResultSet rs = null;
        Statement stmt1 = mConnection.createStatement();
        SmartZip zip = new SmartZip();
        Statement stmt = null;
        String mSQL = "select distinct a.current_dir_missed," + "to_char(sysdate-b.time_check,'yyyymmddhh24miss') min_date_check " + "from missed_file a,data_param b " + "where a.status=0 and a.switch_id=b.id and b.id=" + pFtpID;
        ResultSet rs1 = stmt1.executeQuery(mSQL);
        try
        {
            mReceiveFrom = pReceiveFrom;
            
            channelSftp.cd(pReceiveFrom);

            //FTPFile[] listings = pFtp.dirDetails(""); //Duyet cac thu muc tren ftp
            Vector<LsEntry> fileList = channelSftp.ls(pReceiveFrom);
            for (LsEntry contain : fileList)
            {
                if (contain.getAttrs().isDir())
                {
                    if (!contain.getFilename().startsWith(".") )
                    {
                        if (pRemoteFileSplitByDay == 1)
                        {

                            if (Global.isNumeric(contain.getFilename()))
                            {
                                while (rs1.next() && miThreadCommand != THREAD_STOP)
                                {
                                    if ((rs1.getString("current_dir_missed") != null) && ((Integer.parseInt(rs1.getString("current_dir_missed"))) == (Integer.parseInt(contain.getFilename()))))
                                    {
                                        if ("/".compareTo(pReceiveFrom.substring(pReceiveFrom.length() - 1)) != 0)
                                        {
                                            mReceiveFrom = pReceiveFrom + "/" + contain.getFilename();
                                        }
                                        else
                                        {
                                            mReceiveFrom = pReceiveFrom + contain.getFilename();
                                        }
                                                                            
                                        if (Long.parseLong(sdfComparator.format(sdf.parse(contain.getAttrs().getMtimeString()))) > Long.parseLong(rs1.getString("min_date_check")))
                                        {
                                            writeLogFile("  - Subdirectory listing: " + contain.getFilename());
                                            if (DoLoadMissingFileSftp(pConnection, channelSftp, pFtpID, mReceiveFrom, pDirReceive, pRemoteFileSplitByDay, pZipAfterDownload, pRenameAfterDownload, pSeqFrom, pSeqTo, pRenameType, pNewPreFix, pNewExt, contain.getFilename()) != 0)
                                            {
                                                return ( -1);
                                            }
                                        }
                                    }

                                }

                            }
                        }
                    }

                }
                else
                {
                    if (pCurrMissedDir != null && !pCurrMissedDir.equals(""))
                    {
                        mSQL = "select a.id,a.file_name,a.current_dir_missed," + "b.id,b.header_file_send,b.remote_getfile_dir," + "b.local_getfile_dir,b.remote_split_file_by_day," + "b.local_split_file_by_day," + "to_char(sysdate-b.time_check,'yyyymmddhh24miss') min_date_check " + "from missed_file a,data_param b " + "where a.status=0 and a.switch_id=b.id and b.id=" + pFtpID + "and a.current_dir_missed='" + pCurrMissedDir + "'";
                    }
                    else
                    {
                        mSQL = "select a.id,a.file_name,a.current_dir_missed," + "b.id,b.header_file_send,b.remote_getfile_dir," + "b.local_getfile_dir,b.remote_split_file_by_day," + "b.local_split_file_by_day," + "to_char(sysdate-b.time_check,'yyyymmddhh24miss') min_date_check " + "from missed_file a,data_param b " + "where a.status=0 and a.switch_id=b.id and b.id=" + pFtpID;
                    }

                    stmt = mConnection.createStatement();
                    rs = stmt.executeQuery(mSQL);

                    // Duyet cac file bao thieu
                    while (rs.next() && miThreadCommand != THREAD_STOP)
                    {

                        // Kiem tra gia tri Seq cua file trong bang missed_file co trung voi gia tri Seq cua
                        // mot file tren FTP server khong
                        String seqFileStr = "";
                        String seqMissedFileStr = "";
                        if (pRenameAfterDownload == 1)
                        {
                            if (pRenameType == 1)
                            {
                                seqFileStr = contain.getFilename().substring(pSeqFrom - 1, pSeqTo - 1);
                                seqMissedFileStr = rs.getString("file_name").substring(pSeqFrom - 1, pSeqTo - 1);
                            }
                            else
                            {
                                seqFileStr = contain.getFilename().substring(pSeqFrom, pSeqTo);
                                seqMissedFileStr = rs.getString("file_name").substring(pSeqFrom, pSeqTo);
                            }

                        }
                        else
                        {
                            if (pSeqFrom >= 0 && pSeqTo > 0)
                            {
                                try
                                {
                                    seqFileStr = contain.getFilename().substring(pSeqFrom - 1, pSeqTo - 1);
                                    seqMissedFileStr = rs.getString("file_name").substring(pSeqFrom - 1, pSeqTo - 1);
                                }
                                catch (Exception ex)
                                {
                                    seqFileStr = "";
                                    seqMissedFileStr = "";
                                }
                            }
                        }
                        boolean isMissedFile = false;
                        if (!seqFileStr.equals("") && !seqMissedFileStr.equals("") && seqFileStr.equalsIgnoreCase(seqMissedFileStr))
                        {
                            String currMissedDir = (rs.getString("current_dir_missed") == null ? "" : rs.getString("current_dir_missed"));
                            if (currMissedDir.length() > 0 && Global.isNumeric(currMissedDir))
                            {
                                if (sdf2.format(sdf.parse(contain.getAttrs().getMtimeString())).equals(currMissedDir))
                                {
                                    isMissedFile = true;
                                }
                            }
                        }
                        // Kiem tra hoac ten file, hoac gia tri Seq cua ten file tren FTP Server
                        // co trung voi ten hoac gia tri Seq cua file trong bang missed_file.
                        if ((rs.getString("file_name").toLowerCase().compareTo(contain.getFilename().toLowerCase()) == 0) || isMissedFile)
                        {
                            if (sdf.parse(contain.getAttrs().getMtimeString()).compareTo(rs1.getDate("min_date_check")) > 0)
                            {
                                mSourceName = IOUtil.FillPath(pReceiveFrom, Global.mSeparate) + contain.getFilename(); //+ rs.getString("file_name");
                                if (Global.mSeparate.compareTo(pDirReceive.substring(pDirReceive.length() - 1)) == 0)
                                {
                                    if (rs.getInt("local_split_file_by_day") == 0)
                                    {
                                        mDestinationName = pDirReceive;
                                    }
                                    else
                                    {
                                        mDestinationName = pDirReceive + rs.getString("current_dir_missed") + Global.mSeparate;
                                    }
                                }
                                else if (rs.getInt("local_split_file_by_day") == 0)
                                {
                                    mDestinationName = pDirReceive + Global.mSeparate;
                                }
                                else
                                {
                                    mDestinationName = pDirReceive + Global.mSeparate + rs.getString("current_dir_missed") + Global.mSeparate;
                                }

                                IOUtil.forceFolderExist(mDestinationName);
                                IOUtil.chmod(new File(mDestinationName), "750");
                                file = new File(mDestinationName);
                                if (file.exists() != true)
                                {
                                    writeLogFile("The system cannot find the path specified : '" + mDestinationName + "'");
                                    return ( -1);
                                }
                                writeLogFile("    .Loading file " + mSourceName + " - Size : " + contain.getAttrs().getSize() + " bytes.");
                                channelSftp.get(mSourceName, mDestinationName + "cdr.tmp");
                                file = new File(mDestinationName + "cdr.tmp");
                                if (pRenameAfterDownload == 1)
                                {
                                    if (pRenameType == 1) // HexToDec
                                    {
                                        mNewFileName = pNewPreFix + Global.Format(Global.Hex2Dec(contain.getFilename().substring(pSeqFrom - 1, pSeqTo - 1)), Global.rpad("", pSeqTo - pSeqFrom + 1, "0")) + "." + pNewExt;
                                    }
                                    else
                                    {
                                        mNewFileName = pNewPreFix + Global.Format(Integer.parseInt(contain.getFilename().substring(pSeqFrom, pSeqTo)), Global.rpad("", pSeqTo - pSeqFrom, "0")) + "." + pNewExt;
                                    }
                                }
                                else
                                {
                                    mNewFileName = contain.getFilename();
                                }

                                fileRename = new File(mDestinationName + mNewFileName);
                                file.renameTo(fileRename);
                                fileRename.setLastModified(sdf.parse(contain.getAttrs().getMtimeString()).getTime());
                                //.getFileDateLongTime());

                                if (fileRename.length() != contain.getAttrs().getSize())
                                {
                                    writeLogFile("    .File downloaded error.Size of file downloaded (" + fileRename.length() + ") not equal file origination.");
                                    return (0);
                                }
                                IOUtil.chmod(fileRename, "750");
                                if (pZipAfterDownload == 1)
                                {

                                    zip.ZipFile(fileRename.getAbsolutePath(), fileRename.getAbsolutePath() + ".zip");
                                    fileRename = new File(fileRename.getAbsolutePath() + ".zip");
                                    //fileRename.setLastModified(listLoad
                                    //.getFileDateLongTime());
                                    fileRename.setLastModified(sdf.parse(contain.getAttrs().getMtimeString()).getTime());
                                    IOUtil.chmod(fileRename, "750");
                                    IOUtil.deleteFile(mDestinationName + mNewFileName);
                                }

                                mSQL = "update import_header set current_dir = '" + rs.getString("current_dir_missed") + "',status=" + Global.StateFileFtpOK + " where status in (" + Global.StateFileFtpOK + "," + Global.StateConvertedError + ")" + " and file_name='" + contain.getFilename() + "' and ftp_id=" + pFtpID; //+ rs.getString("file_name")
                                mRetValue = Global.ExecuteSQL(pConnection, mSQL);
                                if (mRetValue == 0)
                                {
                                	
                                	Date createDate = sdf.parse(contain.getAttrs().getMtimeString());
                                	String createDateStr = sdfComparator.format(createDate);
                                    mSQL = "insert into import_header(status,ftp_id," + "file_name,file_size,date_getfile,current_dir," + "date_createfile) values(" + Global.StateFileFtpOK + "," + pFtpID + ",'" + contain.getFilename() + "'," + contain.getAttrs().getSize() + ",sysdate,'" + (rs.getString("current_dir_missed") == null ? "" : rs.getString("current_dir_missed")) + "','" + createDateStr + "')"; //+ rs.getString("file_name")
                                    Global.ExecuteSQL(pConnection, mSQL);
                                }
                                mSQL = "update missed_file set status=1,date_getfile=sysdate " + "where id=" + rs.getString("id");
                                Global.ExecuteSQL(pConnection, mSQL);
                                pConnection.commit();
                                writeLogFile("   .File " + contain.getFilename() + " had been loaded again successful.");
                                    
                                    
                            }
                        }

                    }
                    rs.close();
                    stmt.close();
                }
            }
            fileList = null;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            throw ex;
        }
        finally
        {
            try
            {
                IOUtil = null;
                rs1.close();
                rs1 = null;
                if (rs != null)
                {
                    rs.close();
                }
                rs = null;
                stmt.close();
                stmt = null;
                stmt1.close();
                stmt1 = null;
              
            }
            catch (Exception e)
            {
            }
        }
        return (0);
    }

}
