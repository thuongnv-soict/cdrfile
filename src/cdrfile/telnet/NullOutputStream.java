package cdrfile.telnet;

/**
 * <p>Title: CRM System</p>
 * <p>Description: Customer Care Departerment</p>
 * <p>Copyright: Copyright (c)  by eKnowledge 2006</p>
 * <p>Company: VietNam Mobile Telecom Services</p>
 * @author eKnowledge - Software
 * @version 1.0
 */


public class NullOutputStream
   extends java.io.OutputStream
{

   /**
	* Writes the specified byte to this <code>NullOutputStream</code>.  The data is not
	* actually written to any port or device.
	*
	* @param b data to be sent
	*/
   public void write (int b)
   {
   }

   /**
	* Writes the specified array to this <code>NullOutputStream</code>.  The data is not
	* actually written to any port or device.  This method is
	* included so writes to this <code>NullOutputStream</code> will occur faster.
	*
	* @param barr the array containing data to be output
	* @param offset offset into array where data starts
	* @param length number of bytes to be sent
	*/
   public void write (byte[] barr, int offset, int length)
   {
	  if (barr == null) throw new NullPointerException();
	  if ((offset < 0) || (length < 0) || ((offset+length) > barr.length))
		throw new ArrayIndexOutOfBoundsException();
   }
}
