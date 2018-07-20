package cdrfile.thread;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import cdrfile.convert.StructSMSFW;
import cdrfile.global.MResponse;



public class ConvertFw {

		private static BufferedInputStream bis;
		
		private static StructSMSFW structF = new StructSMSFW();
		private static Map<String, String> mRe = MResponse. mResponse();
		
		static int readLen(int nlen) throws IOException {
			  
			  int clen=0;
			  if((nlen>128)&&(nlen<=132)){
			  for(int i=1;i<=nlen-128;i++)
			  {
				  int x=bis.read();
				  clen+= x << (8*(nlen-128-i));
			  }
			  }
			  else   clen=nlen;
			  return clen;
		  }
		static int nByteLen(int nlen) throws IOException {
			   
			  int clen = 0;
			  if ((nlen >128)&&(nlen<=132)) clen = nlen - 128;
			  return clen;
		  }
		public static String readChar (int len) throws IOException {
			String rl="";
			for (int i=1; i<= len; i++)
			{
				int r = bis.read();
				rl += Character.toString ((char) r);
			}
			return rl;
		}
		public static int ReadInt (int len) throws IOException {
			int rl = 0;
			for (int i=1; i<= len; i++)
			{
				int x=bis.read();
				rl+= x << (8*(len-i));
			}
			return rl;
		}
		public static String nguoc (String by) throws IOException
		{
			String kq="";
			if (by.length() == 1) kq = by + "0";
			else
			{
				String a = by.substring(0, 1);
				String b = by.substring(by.length()-1);
				if ((a.equals("a")) || (a.equals("b")) || (a.equals("c")) || (a.equals("d")) || (a.equals("e")) || (a.equals("f")))
					kq= b +"";
				else kq = b + a +"";
			}
			return kq;
		}
		public static int rec_time (String tag) throws IOException 
		{
			int a= bis.read();
			int len = readLen(a);
			int nlen = nByteLen(a);
			structF.rec_time = readChar(len);
			return (len + nlen);
		} 
		public static void routingAction (String tag) throws IOException
		{
			bis.read();
			int a = bis.read();
			if (a == 0) structF.routingAction = "pass";
			if (a == 1) structF.routingAction = "blockWithTemporaryError";
			if (a == 2) structF.routingAction = "blockWithPermanentError";
			if (a == 3) structF.routingAction = "blockWithNoResponse";
			if (a == 4) structF.routingAction = "blockWithAck";
			if (a == 5)	structF.routingAction = "release";
		
		}
		public static int responseInfo (String tag) throws IOException
		{
			int a= bis.read();
			int len = readLen(a);
			int nlen = nByteLen(a);
			int d = 0;
			while (d < len)
			{
				String b = Integer.toHexString(bis.read());
				d++;
				if ((b.equals("80")) || (b.equals("81")))
				{
					if (b.equals("80"))
					{
						bis.read();
						//System.out.println("----b ="+b);
						String x = Integer.toHexString(bis.read());
						structF.responseInfo_deliveryResult = mRe. get(x);
						d+=2;
					}
					if (b.equals("81"))
					{
						int l = bis.read();
						int len81 = readLen(l);
						int nlen81 = nByteLen(l);
						//System.out.println("----b ="+b);
						d++;
						structF.responseInfo_mtRoutingRule = readChar(len81);
						d+=(len81 + nlen81);
					}
				}
				else 
				{
					int l = bis.read();
					int len8 = readLen(l);
					int nlen8 = nByteLen(l);
					//System.out.println("----b ="+b);
					d++;
					for (int i=1; i<= len8; i++)
					{
						bis.read();
					}
					d+=(len8 + nlen8);
				}
			}
			return (len + nlen );	
		}
		public static int rejectInfo (String tag) throws IOException
		{
			int a= bis.read();
			int len = readLen(a);
			int nlen = nByteLen(a);
			int d = 0;
			while (d < len)
			{
				String b = Integer.toHexString(bis.read());
				d++;
				if (b.equals("81"))
				{
					int l= bis.read();
					int len81 = readLen(l);
					int nlen81 = nByteLen(l);
					d++;
					structF.rejectInfo_mtRoutingRule = readChar(len81);
					d+=(len81 + nlen81);
				}
				else
				{
					int l= bis.read();
					int len8 = readLen(l);
					int nlen8 = nByteLen(l);
					d++;
					for (int i=1; i<= len8; i++)
					{
						bis.read();
					}
					d+=(len8 + nlen8);
				}
			}
			return (len+nlen);
		}
		public static int mapSmsc (String tag) throws IOException
		{
			int a= bis.read();
			int len = readLen(a);
			int nlen = nByteLen(a);
			int d = 0;
			while (d < len)
			{
				String b = Integer.toHexString(bis.read());
				d++;
				if (b.equals("80"))
				{
					int l= bis.read();
					int len80 = readLen(l);
					int nlen80 = nByteLen(l);
					d++;
					for (int i=1; i<= len80-6;i++) bis.read();
					  String kq = "";
						for(int i=1;i<=5;i++)
						{
							String by = Integer.toHexString(bis.read());
							kq += nguoc(by);
						}
					String cuoi = Integer.toHexString(bis.read());
					if (cuoi.length() == 1) kq += cuoi;
					else kq += nguoc(cuoi);
					
					structF.mapSmsc_gsmAddress = kq;
					//structF.mapSmsc_gsmAddress = readChar(len80);
					d+=(len80 + nlen80);
				}
				else
				{
					int l= bis.read();
					int len8 = readLen(l);
					int nlen8 = nByteLen(l);
					d++;
					for (int i=1; i<= len8; i++)
					{
						bis.read();
					}
					d+=(len8 + nlen8);
				}
			}
			return (len + nlen);	
		}
		public static int correlatedSriSm (String tag) throws IOException
		{
			int a= bis.read();
			int len = readLen(a);
			int nlen = nByteLen(a);
			//System.out.println("----len be ="+len);
			int d = 0;
			while (d < len)
			{
				String b = Integer.toHexString(bis.read());
				//System.out.println("----len b ="+b);
				d++;
				if ((b.equals("a1")) || (b.equals("a2")) || (b.equals("a3")) || (b.equals("a5")))
				{
					if (b.equals("a1"))
					{
						int l= bis.read();
						int lena1 = readLen(l);
						int nlena1 = nByteLen(l);
						d++;
						int k = 0;
						while (k < lena1)
						{
							String c = Integer.toHexString(bis.read());
							k++;
							if (c.equals("80"))
							{
								int h= bis.read();
								int len80 = readLen(h);
								int nlen80 = nByteLen(h);
								k++;
								for (int i=1; i<= len80-6;i++) bis.read();
								  String kq = "";
									for(int i=1;i<=5;i++)
									{
										String by = Integer.toHexString(bis.read());
										kq += nguoc(by);
									}
								String cuoi = Integer.toHexString(bis.read());
								if (cuoi.length() == 1) kq += cuoi;
								else kq += nguoc(cuoi);
							
								structF.correlatedSriSm_mapSmsc_gsmAddress = kq;
								k+=(len80 + nlen80);
							}
							else
							{
								int h= bis.read();
								int len8 = readLen(h);
								int nlen8 = nByteLen(h);
								k++;
								for (int i=1; i<= len8; i++)
								{
									bis.read();
								}
								k+=(len8 + nlen8);
							}
						}
						d+=(lena1 + nlena1);
						
					}
					if (b.equals("a2"))
					{
						int l= bis.read();
						int lena2 = readLen(l);
						int nlena2 = nByteLen(l);
						d++;
						int k = 0;
						while (k < lena2)
						{
							String c = Integer.toHexString(bis.read());
							k++;
							if (c.equals("80"))
							{
								int h= bis.read();
								int len80 = readLen(h);
								int nlen80 = nByteLen(h);
								k++;
								for (int i=1; i<= len80-6;i++) bis.read();
								  String kq = "";
									for(int i=1;i<=5;i++)
									{
										String by = Integer.toHexString(bis.read());
										kq += nguoc(by);
									}
								String cuoi = Integer.toHexString(bis.read());
								if (cuoi.length() == 1) kq += cuoi;
								else kq += nguoc(cuoi);
								structF.correlatedSriSm_mapMsisdn_gsmAddress = kq;
								k+=(len80 + nlen80);
							}
							else
							{
								int h= bis.read();
								int len8 = readLen(h);
								int nlen8 = nByteLen(h);
								k++;
								for (int i=1; i<= len8; i++)
								{
									bis.read();
								}
								k+=(len8 + nlen8);
							}
						}
						d+=(lena2 + nlena2);
					}
					if (b.equals("a3"))
					{
						int l= bis.read();
						int lena3 = readLen(l);
						int nlena3 = nByteLen(l);
						d++;
						int k = 0;
						while (k < lena3)
						{
							String c = Integer.toHexString(bis.read());
							k++;
							if (c.equals("80"))
							{
								int h= bis.read();
								int len80 = readLen(h);
								int nlen80 = nByteLen(h);
								k++;
								//String thu = Integer.toHexString(bis.read());
								  String kq = "";
								//int dai = len80 -1;
									for(int i=1;i<=len80-1;i++)
									{
										String by = Integer.toHexString(bis.read());
										kq += nguoc(by);
									}
									String cuoi = Integer.toHexString(bis.read());
									if (cuoi.length() == 1) kq += cuoi;
									else kq += nguoc(cuoi);
								structF.correlatedSriSm_mapImsi_imsi = kq;
								k+=(len80 + nlen80);
							}
							else
							{
								int h= bis.read();
								int len8 = readLen(h);
								int nlen8 = nByteLen(h);
								k++;
								for (int i=1; i<= len8; i++)
								{
									bis.read();
								}
								k+=(len8 + nlen8);
							}
						}
						d+=(lena3 + nlena3);
					}
					if (b.equals("a5"))
					{
						int l= bis.read();
						int lena5 = readLen(l);
						int nlena5 = nByteLen(l);
						
						//System.out.println("----len a5 ="+lena5);
						d++;
						int k = 0;
						while (k < lena5)
						{
							String c = Integer.toHexString(bis.read());
							k++;
							if (c.equals("80"))
							{
								int h= bis.read();
								int len80 = readLen(h);
								int nlen80 = nByteLen(h);
								k++;
								String test = Integer.toHexString(bis.read());
								int x = 1;
								while (!test.equals("91"))
									{
									test= Integer.toHexString(bis.read());
									x++;
									}
								  String kq = "";
									for(int i=1;i<=len80 - x - 1;i++)
									{
										String by = Integer.toHexString(bis.read());
										kq += nguoc(by);
									}
								String cuoi = Integer.toHexString(bis.read());
								if (cuoi.length() == 1) kq += cuoi;
								else kq += nguoc(cuoi);
								structF.correlatedSriSm_mapMsc_gsmAddress = kq;
								k+=(len80 + nlen80);
							}
							else
							{
								int h= bis.read();
								int len8 = readLen(h);
								int nlen8 = nByteLen(h);
								k++;
								for (int i=1; i<= len8; i++)
								{
									bis.read();
								}
								k+=(len8 + nlen8);
							}
						}
						d+=(lena5 + nlena5);
					}
				}
				else
				{
					int l= bis.read();
					int lena = readLen(l);
					int nlena = nByteLen(l);
					if (lena > 128) d++;
					d++;
					for (int i=1; i<= lena; i++)
					{
						bis.read();
					}
					d+=(lena + nlena);
				}
			}
			
			 return (len+nlen);
			
		}
		public static int smsDeliver (String tag) throws IOException
		{
			int a= bis.read();
			int len = readLen(a);
			int nlen = nByteLen(a);
			//System.out.println("----len af = "+len);
			int d = 0;
			while (d < len)
			{
				String b = Integer.toHexString(bis.read());
				//System.out.println("--b = "+b);
				d++;
				if ((b.equals("a1")) || (b.equals("86")))
				{
					if (b.equals("a1"))
					{
						int l= bis.read();
						int lena1 = readLen(l);
						int nlena1 = nByteLen(l);
						
						//System.out.println("----len a1 = "+lena1);
						d++;
						int k = 0;
						while (k < lena1)
						{
							String c = Integer.toHexString(bis.read());
							k++;
							if (c.equals("80"))
							{
								int h= bis.read();
								int len80 = readLen(h);
								int nlen80 = nByteLen(h);
								//System.out.println("----len 80 = "+len80);
								k++;
								
								int x = 0;
								for(int i=1;i<=len80;i++)
									{
									String test= Integer.toHexString(bis.read());
									x++;
									if(test.equals("91")) break;
									}
								if(x < len80){
								  String kq = "";
									for(int i=1;i<=len80 - x - 1;i++)
									{
										String by = Integer.toHexString(bis.read());
										kq += nguoc(by);
									}
								String cuoi = Integer.toHexString(bis.read());
								if (cuoi.length() == 1) kq += cuoi;
								structF.smsDeliver_smsOriginator_gsmAddress = kq;
								}
								else structF.smsDeliver_smsOriginator_gsmAddress = "alphanumeric/unknown Vodafone";
								k+=(len80 + nlen80);
							}
							else
							{
								int h= bis.read();
								int len8 = readLen(h);
								int nlen8 = nByteLen(h);
								if (len8 > 128) k++;
								//System.out.println("----len "+c+" = "+len8);
								k++;
								for (int i=1; i<= len8; i++)
								{
									bis.read();
								}
								k+=(len8 + nlen8);
							}
						}
						d+=(lena1 + nlena1);
					}
					if (b.equals("86"))
					{
						int h= bis.read();
						int len86 = readLen(h);
						int nlen86 = nByteLen(h);
						if (len86 > 128) d++;
						//System.out.println("----len 86 = "+len86);
						d++;
						
						structF.smsDeliver_smsUserData = readChar(len86);
						d+=(len86+ nlen86);
					}
				}		
				else
				{
					int l= bis.read();
					int lena = readLen(l);
					int nlena = nByteLen(l);
					if (lena > 128) d++;
					//System.out.println("--len "+b + " = "+lena);
					d++;
					for (int i=1; i<= lena; i++)
					{
						bis.read();
					}
					d+=(lena+nlena);
				}
			}
			
			 return (len+nlen);			
		}
		public static int sccpCgPa (String tag) throws IOException
		{
			int a= bis.read();
			int len = readLen(a);
			int nlen = nByteLen(a);
			
			int d = 0;
			while (d < len)
			{
				String b = Integer.toHexString(bis.read());
				d++;
				if ((b.equals("80")) || (b.equals("81")) || (b.equals("82")))
				{
					if (b.equals("80"))
					{
						int h= bis.read();
						int len80 = readLen(h);
						int nlen80 = nByteLen(h);
						
						d++;
						for (int i=1; i<= len80-6;i++) bis.read();
						  String kq = "";
							for(int i=1;i<=5;i++)
							{
								String by = Integer.toHexString(bis.read());
								kq += nguoc(by);
							}
						String cuoi = Integer.toHexString(bis.read());
						if (cuoi.length() == 1) kq += cuoi;
						else kq += nguoc(cuoi);
						structF.sccpCgPa_sccpAddress = kq;
						d+=(len80 +nlen80);
					}
					if (b.equals("81"))
					{
						int h= bis.read();
						int len81 = readLen(h);
						int nlen81 = nByteLen(h);
						if (len81 > 128) d++;
						d++;
						structF.sccpCgPa_country = readChar(len81);
						d+=(len81 + nlen81);
					}
					if (b.equals("82"))
					{
						int h= bis.read();
						int len82 = readLen(h);
						int nlen82 = nByteLen(h);
						if (len82 > 128) d++;
						d++;
						structF.sccpCgPa_network = readChar(len82);
						d+=(len82 +nlen82);
					}
				}
				else 
				{
					int h= bis.read();
					int len8 = readLen(h);
					int nlen8 = nByteLen(h);
					if (len8 > 128) d++;
					d++;
					for (int i=1; i<= len8; i++)
					{
						bis.read();
					}
					d+=(len8 +nlen8);
				}
			}
			return (len+nlen);		
		}
		public static void main(String[] args) throws IOException{

		        BufferedReader reader = null;
				BufferedWriter out = null;
		     
		        try {

		        String test = "C:\\Users\\Admin\\Desktop\\sap france\\sap france\\input\\log_TKLHCM-FW01_20161203_181611_560.dat";
		        File f = new File(test);
		        bis = new BufferedInputStream(new FileInputStream(f));
				out = new BufferedWriter(new FileWriter("C:\\Users\\Admin\\Desktop\\sap france\\sap france\\output\\log_TKLHCM-FW01_20161203_181611_560_3.txt"));
				out.write("rec_time|\tinbound|\troutingAction|\tresponseInfo_deliveryResult|\tresponseInfo_mtRoutingRule|\trejectInfo_mtRoutingRule|\t"
						+"mapSmsc_gsmAddress|\tcorrelatedSriSm_mapImsi_imsi|\tsmsDeliver_smsOriginator_gsmAddress|\tsccpCgPa_sccpAddress|\t"
						+"sccpCgPa_country|\tsccpCgPa_network|\tcorrelatedSriSm_mapSmsc_gsmAddress|\tcorrelatedSriSm_mapMsisdn_gsmAddress|\t"
						+ "correlatedSriSm_mapMsc_gsmAddress\tsmsDeliver_smsUserData");
				out.newLine();
				int count = 0;
		        while (1>0)// dk dung vong while
		        {
		        	count++;
		        	
		        	String bt = "";
			        bt = Integer.toHexString(bis.read()) + Integer.toHexString(bis.read());// Kiem tra lai ham nay dung khong?
			        //System.out.println("--Tag = "+bt);
			        if ((bt.equals("7f68")) || (bt.equals("7f69")))
			        {
			        	if (bt.equals("7f68")) structF.inbound = "trustedMtFwdSm";
			        	else structF.inbound = "suspectMtFwdSm";
			        	int a= bis.read();
						int len = readLen(a);
						
			        		//System.out.println("----len = "+len);
			        		int dem = 0;
			        		while (dem < len)
			        		{
			        			String tag = Integer.toHexString(bis.read());
			        			//System.out.println("----tag ="+tag);
			        			dem++;
			        			if ((tag.equals("80")) || (tag.equals("81")) || (tag.equals("a2")) || (tag.equals("a3")) || (tag.equals("ac")) || (tag.equals("af")) || (tag.equals("a8")) || (tag.equals("be")))
			        			{
			        				if (tag.equals("80"))
			        				{
			        					dem++;
			        					dem+= rec_time(tag);
			        					//System.out.println("----dem ="+dem);
			        				}
			        				if (tag.equals("81"))
			        				{
			        					routingAction(tag);
			        					dem+=2;
			        					//System.out.println("----dem ="+dem);
			        				}
			        				if (tag.equals("a2"))
			        				{
			        					dem++;
			        					dem+= rejectInfo(tag);
			        					//System.out.println("----dem ="+dem);
			        				}
			        				if (tag.equals("a3"))
			        				{
			        					dem++;
			        					dem+= responseInfo(tag);
			        					//System.out.println("----dem ="+dem);
			        				}
			        				if (tag.equals("ac"))
			        				{
			        					dem++;
			        					dem+= mapSmsc(tag);
			        					//System.out.println("----dem ="+dem);
			        				}
			        				if (tag.equals("be"))
			        				{
			        					dem++;
			        					dem+= correlatedSriSm(tag);
			        					//System.out.println("----dem ="+dem);
			        				}
			        				if (tag.equals("af"))
			        				{
			        					dem++;
			        					dem+= smsDeliver(tag);
			        					//System.out.println("----dem ="+dem);
			        				}
			        				if (tag.equals("a8"))
			        				{
			        					dem++;
			        					dem+= sccpCgPa(tag);
			        					//System.out.println("----dem ="+dem);
			        				}
			        			}
			        			else
			        			{
			        				if((tag.equals("bf")) || (tag.equals("9f")) )
			        				{
			        					
			        					tag+= Integer.toHexString(bis.read());
			        					//System.out.println("----tag ="+tag);
			        					dem++;
			        					int h= bis.read();
			    						int lenf = readLen(h);
			    						int nlenf = nByteLen(h);
			        					if (lenf > 128) dem++;
			        					dem++;
			        					//System.out.println("----len bf ="+lenf);
			        					for (int i=1;i <= lenf; i++) bis.read();
			        					dem+=(lenf + nlenf);
			        					
			        					//System.out.println("----dem ="+dem);
			        					int duyet = len - dem;
			        					
			        				}
			        				else
			        				{
			        					int h= bis.read();
			    						int lenf = readLen(h);
			    						int nlenf = nByteLen(h);
			        					
			        					dem++;
			        					//System.out.println("----len"+tag+" ="+lenf);
			        					for (int i=1;i <= lenf; i++) bis.read();
			        					dem+=(lenf +nlenf);
			        					//System.out.println("----dem ="+dem);
			        				}
			        			}
			        			
			        		}
			        		//System.out.println("count = "+count);
			        		//System.out.println(structF.toString()); 
			        		out.write(structF.toString());
							out.newLine();
							
			        	}
			        	else
			        	{
			        		if (bt.equals("ffffffffffffffff")) break;
			        		int h= bis.read();
							int len = readLen(h);
							
			        		//System.out.println("----len = "+len);
			        		for (int i =1; i <= len; i++) bis.read();
			        	}
			        
			        //System.out.println("count = "+count);
			     }
		        }catch (IOException e) { 
					e.printStackTrace();
				} finally {
					if (reader != null) {
						try {
							//System.out.println("Close file roi. hihi"); 
							out.flush();
							out.close();
							
						} catch (IOException e) { 
							e.printStackTrace();
						}
					}
				}
					}
				
}

