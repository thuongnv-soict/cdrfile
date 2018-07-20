package cdrfile.telnet;
/**
 * <p>Title: CDR File(s) System</p>
 * <p>Description: VMS IS Departerment</p>
 * <p>Copyright: Copyright (c)  by eKnowledge 2004</p>
 * <p>Company: VietNam Mobile Telecom Services</p>
 * @author eKnowledge - Software
 * @version 1.0
 */

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

import cdrfile.global.Global;

/**
 * A simple server that uses the Telnet protocol as described in RFC 854.  This server uses a ServerSocket to listen
 * on the specified port (defaults to port 23) for Telnet connection requests.  For each connection made,
 * a Telnet session is created.  All command processing is handled by the Telnet session, not this server.
 */
public class TelnetServer
    extends Server
{
  private static final int DEFAULT_TIMEOUT = 100000; //10 minutes in milliseconds
  static final int PORT = 23;
  private int timeout;
  private ServerSocket socket;

  /**
   * Prepares the Telnet server to listen on the well known Telnet port (23).  The server will not
   * be started and no connections will be accepted until its <code>run()</code> method is executed.
   */
  public TelnetServer() throws IOException
  {
    this(PORT);
  }

  /**
   * Prepares the Telnet server to listen on an arbitrary port.  The server will not
   * be started and no connections will be accepted until its <code>run()</code> method is executed.
   */
  public TelnetServer(int port) throws IOException
  {
    socket = new ServerSocket(port);
    
    System.out.println(Global.Format(new java.util.Date(),
	"dd/MM/yyyy HH:mm:ss") + " - Telnet Server started on port " + port + "\n");

    welcomeMessage = "";

    {
      rootLoginAllowed = true; //Default is allowed
    }

    timeout = DEFAULT_TIMEOUT; // Default is 10 min
  }

  /**
   * Listens on the connection port for connection requests.  Once a
   * request is made, it creates, initializes, and returns a new
   * TelnetSession to handle that request.  This method will block until
   * a connection is made.
   *
   * @return  a new <code>TelnetSession</code>
   */

  private int last_wait = 100; //wait 100 ms first time we try to wait
  private static final int MAXIMUM_WAIT = 1000 * 60 * 5; //wait a max of 5 minutes

  protected Session acceptNewSession()
  {
    TelnetSession newSession = null;

    try
    {
      Socket sock = null;
      try
      {
        sock = socket.accept();
        //if we are succesful, reset the back-off
        last_wait = 100;
        sock.setSoTimeout(timeout);
      }
      catch (BindException be)
      {
        //rethrow it...why would we get this?
        throw be;
      }
      catch (IOException ioe)
      {
        try
        {
          Thread.sleep(last_wait);
        }
        catch (InterruptedException ie)
        {
          //drain it
        }

        //there won't be multiple threads running around in here, so we don't need to synch
        last_wait = last_wait << 1;
        if (last_wait > MAXIMUM_WAIT)
        {
          last_wait = MAXIMUM_WAIT;
        }

        //and just bail out
        return null;
      }
      catch (OutOfMemoryError oome)
      {
        System.gc();
        //give the system some time to clear out any old threads or connections or whatever
        try
        {
          Thread.sleep(10000);
        }
        catch (InterruptedException ioe)
        {
          //drain
        }
        return null;
      }
     if (shutdown)
        return null;

      SystemPrintStream sout =
          new SystemPrintStream(sock.getOutputStream());
      TelnetInputStream sin = new TelnetInputStream(sock.getInputStream(), sout);

      newSession = new TelnetSession(sin, sout, sout, sock, this);
      sin.setSession(newSession);
      try
      {
        newSession.start();
      }
      catch (Throwable t)
      {
        sout.print("Thread limit reached.  Connection Terminated.\r\n");
        sock.close();
        newSession = null;
      }
    }
    catch (IOException ioe)
    {
      try
      {
        if (!shutdown)
          shutDown();
      }
      catch (Throwable t)
      {
        shutdown = true;
      }
    }

    return newSession;
  }

  /**
   * Closes the ServerSocket used to listen for connections.
   */
  protected synchronized void closeAllPorts() throws IOException
  {
    socket.close();
  }
  
}
