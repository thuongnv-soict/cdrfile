package cdrfile.telnet;
/**
 * <p>Title: CRM System</p>
 * <p>Description: Customer Care Departerment</p>
 * <p>Copyright: Copyright (c)  by eKnowledge 2006</p>
 * <p>Company: VietNam Mobile Telecom Services</p>
 * @author eKnowledge - Software
 * @version 1.0
 */


import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

public abstract class Server
   extends Thread
{

   protected boolean anonymousAllowed = true;
   protected boolean rootLoginAllowed = true;
   protected String  logAnon;
   protected String  welcomeMessage;
   protected String  connectionMessage;

   /**
	* Gets the name of the file to be displayed when a user connects to this server.  The
	* contents of this file will be displayed before the login message. This file is specified
	* by setting the "FTP_CONNECT" environment variable equal to the name of the file.  This
	* method will always return the value the variable had when the server was constructed.
	* Changing the value of environment variable after creating the server will have no effect.
	*
	* @return  the name of the connection file to display, or <code>null</code>
	* if no connection file was specified
	*/
   public String getConnectionMsgFile ()
   {
	  return connectionMessage;
   }

   /**
	* Gets the name of the file to be displayed when after a user logs in to this server.
	* This file is specified by setting the "FTP_WELCOME" environment variable equal to
	* the name of the file.  This method will always return the value the variable had
	* when the server was constructed.  Changing the value of environment variable after
	* creating the server will have no effect.
	*
	* @return  the name of the welcome file to display, or <code>null</code>
	* if no welcome file was specified
	*/
   public String getWelcomeMsg ()
   {
	  return welcomeMessage;
   }

   /**
	* Indicates whether anonymous login is allowed to this FTP server.  This
	* is specified with the environment variable "FTP_ANON_ALLOWED".  Set the
	* variable to "false" to disallow anonymous login.  All other values will
	* be interpreted as <code>true</code> and anonymous logins will be accepted.
	* This method will always return the value the variable had when the server
	* was constructed.  Changing the value of environment variable after
	* creating the server will have no effect.
	*
	* @return <code>true</code> if anonymous is allowed to login
	*/
   public boolean isAnonymousAllowed ()
   {
	  return anonymousAllowed;
   }

   /**
	* Indicates whether root access is allowed to this FTP server.  This
	* is specified with the environment variable "FTP_ROOT_ALLOWED".  Set the
	* variable to "false" to disallow root login.  All other values will
	* be interpreted as <code>true</code> and root logins will be accepted.
	* This method will always return the value the variable had when the server
	* was constructed.  Changing the value of environment variable after
	* creating the server will have no effect.
	*
	* @return <code>true</code> if root is allowed to login
	*/
   public boolean isRootAllowed ()
   {
	  return rootLoginAllowed;
   }

   /**
	* Returns the name of the file where anonymous logins should be logged.  This is specified
	* with the environment variable "FTP_LOG_ANON".  This method will always return the value
	* the variable had when the server was constructed.  Changing the value of environment
	* variable after creating the server will have no effect.
	*
	* @return the name of the ftp log file, or <code>null</code> if none specified
	*/
   public String logAnon ()
   {
	  return logAnon;
   }

   /**
	* List of sessions created by this server.
	*/
   protected Vector sessions;

   /**
	* Stops this server when set to true.
	*/
   protected boolean shutdown;

   /**
	* Initializes the server and creates the list of sessions (initially empty).
	*/
   protected Server ()
   {
	  sessions = new Vector();
	  shutdown = false;
   }

   /**
	* Starts the server.  This method continuously loops checking for new
	* connections until the <code>shutdown</code> field is set to <code>true</code>.
	*/
   public void run ()
   {
	  while (!shutdown)
	  {
		 try
		 {
			checkForNewSessions();
		 }
		 catch (Throwable t)
		 {

			//Hmm... just no goodness can come out of a throwable coming all this
			//way up the stack.  All of the recoverable stuff will be handled further
			//down in the internals.  Bring down the server.

			//com.dalsemi.system.Debug.debugDump("Server PANIC: " + t.toString());

			try
			{
			   if (!shutdown)
				  shutDown();
			}
			catch (Exception e)
			{

			   //Be afraid.  Be very, very afraid.
			   shutdown = true;
			}
		 }
	  }
   }

   /**
	* Listens for connection requests.  This method blocks until a request is received.
	* Once the new session is created, it is added to the list of known
	* sessions.
	*/
   protected void checkForNewSessions ()
   {
	  Session newSession = acceptNewSession();

	  if (newSession != null)
	  {
		 sessions.addElement(newSession);
	  }
   }

   /**
	* Waits for a connection request.  This method should block until a request is received.
	* When a request is made, it should create, initialize, and return a
	* new Session to handle that connection.
	*
	* @return  a new Session
	*/
   protected abstract Session acceptNewSession ();

   /**
	* Requests that the server stop taking connections and terminate any current sessions.
	* Cycles through the list of known sessions, closing down each one and then cleaning
	* up its own system resources.
	*/
   public synchronized void shutDown ()
	  throws IOException
   {
	  //com.dalsemi.system.Debug.debugDump("Help! I don't WANT to die!");
	  if (!shutdown)
	  {
		 shutdown = true;

		 if (sessions.size() > 0)
		 {
			//Enumeration sessionsEnum = sessions.elements();
			int size = sessions.size();

			for (int i = 0; i < size; i++)
			{
			   (( Session ) sessions.elementAt(0)).forceEndSession();
			}

		}

		 closeAllPorts();
	  }
   }

   /**
	* Notifies this server that a particular session is ending.  Removes the closing session from the
	* list of known sessions.
	*
	* @param session  the session that has terminated
	*/
   public synchronized void sessionEnded (Session session)
   {
	  sessions.removeElement(session);
	  java.lang.System.gc();
   }

   /**
	* Gets an array of the names of all of the users that are currently connected
	* to this server.
	*
	* @return user names of all connected users
	*/
   public String[] getConnectedUsers ()
   {
	  String[] users = null;
	  synchronized (sessions)
	  {
		users                    = new String [sessions.size()];
		Enumeration sessionsEnum = sessions.elements();
		int         i            = 0;

		while (sessionsEnum.hasMoreElements())
		{
			users [i++] = (( Session ) sessionsEnum.nextElement()).getUserName();
		}
	  }

	  return users;
   }

   /**
	* Cycles through the list of know sessions, sending the specified message.
	*
	* @param sendThis  message to send to all sessions
	*/
   public void broadcast (String sendThis)
   {
	  Enumeration sessionsEnum = sessions.elements();

	  while (sessionsEnum.hasMoreElements())
	  {
		 (( Session ) sessionsEnum.nextElement()).broadcast(sendThis);
	  }
   }

   /**
	* Cleans up any system resources held by this server.
	*/
   protected abstract void closeAllPorts ()
	  throws IOException;
   
}
