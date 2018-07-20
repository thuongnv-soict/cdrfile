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
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Vector;

public abstract class Session extends Thread {
	/**
	 * The input stream of the remote connection.
	 */
	protected SystemInputStream in;
	/**
	 * The output stream of the remote connection.
	 */
	protected SystemPrintStream out;
	/**
	 * The error stream of the remote connection.
	 */
	protected SystemPrintStream err;
	/**
	 * Specifies that a command is currently being processed by this session.
	 */
	protected boolean inCommand;
	/**
	 * The environment associated with this session.
	 */
	protected Hashtable environment;
	/**
	 * Specifies that the session should attempt to close the connection after it has
	 * finished processing the current request.
	 */
	protected boolean shutdown;
	/**
	 * A list of the last few commands issued in this session.  This list is implemented
	 * as a circular buffer.
	 */
	protected String[] commandHistory;
	/**
	 * Marks the end of the commandHistory buffer.
	 */
	protected int lastCommand = 0;
	/**
	 * Marks the beginning of the commandHistory buffer.
	 */
	protected int currentCommand = 0;
	/**
	 * The text used as the command line prompt for this session.
	 */
	protected byte[] prompt; //This is for speed of prompt writing.
	/**
	 * The server that created this session.
	 */
	protected Server server;
	/**
	 * The user that is currently logged in with this session.
	 */
	protected String userName;
	/**
	 * The password for the user that is currently logged in.
	 */
	protected String password;
	/**
	 * The list of users that are currently logged into this session.
	 */
	protected Vector loginStack;
	/**
	 * The thread ID of this session.
	 */
	protected Object myThreadID;
	/**
	 * Used as temporary storage when parsing the parameters for a command.
	 */
	protected Vector paramsVector;
	/**
	 * Used as temporary storage when parsing the parameters for a command.
	 */
	protected Object[] paramsArray;
	/**
	 * Used as temporary storage when parsing the parameters for a command.
	 */
	protected Object[] retArray;

	/**
	 * The key used to index the current directory in system environments.
	 */
	public static String CURRENT_DIRECTORY = "current directory";

	/**
	 * The key used to index the current command in system environments.
	 */
	public static String CURRENT_COMMAND = "current command";

	/**
	 * The message shown to all users when they login to this session.
	 */
	public String welcomeMessage = null;

	/**
	 * Intializes the session.
	 *
	 * @param in        stream this session should use to get data from user
	 * @param out       stream this session should use to output to user
	 * @param err       stream this session should use to output errors to user
	 * @param server    the server in charge of this session
	 */
	protected Session(SystemInputStream in, SystemPrintStream out,
			SystemPrintStream err, Server server) {
		this.in = in;
		this.out = out;
		this.err = err;
		inCommand = false;
		this.server = server;
		environment = new Hashtable();
		//environment = TINIOS.getSystemEnvironment();

		welcomeMessage = "Connecting to Telnet server ...";
		//if (welcomeMessage == null)
		//	 welcomeMessage = "\r\nWelcome to " + TINIOS.getShellName() + ".  ("
		//					  + TINIOS.getShellVersion() + ")\r\n\r\n";

		String histSize = (String) environment.get("HISTORY");

		if (histSize != null) {
			try {
				int size = Integer.parseInt(histSize);

				commandHistory = new String[size];
			} catch (NumberFormatException nfe) {
				commandHistory = new String[50];
			}
		} else
			commandHistory = new String[50];

		loginStack = new Vector(5);
		paramsVector = new Vector(5);
		paramsArray = new Object[5];

		for (int i = 0; i < paramsArray.length; i++) {
			paramsArray[i] = new String[i];
		}

		retArray = new Object[2];
	}

	/**
	 * Returns the user name associated with this session.
	 *
	 * @return the current user's name
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Logs a user into the system.  This method is responsible for identifying
	 * and verifying the user.  Typically this is done with a user name and password.
	 */
	protected abstract void login() throws IOException;

	/**
	 * Starts the communication loop of the session.  First the <code>login()</code> method
	 * is called.  If the user is sucessfully logged into the system, commands are accepted
	 * and processed until the session is terminated.
	 */
	public final void run() {
		myThreadID = Thread.currentThread();

		try {
			login();
		} catch (IOException ioe) {
			//com.dalsemi.system.Debug.debugDump("Login IOException.  Aborting...");

			endSession();
			return;
		}

		while (!shutdown) {
			try {
				String command = getNextCommand();

				if (command == null) {
					//com.dalsemi.system.Debug.debugDump("Session: EOS.");   //DEBUG
					forceEndSession();
					return;
				} else {
					if (command.length() == 0) {
						currentCommandFinished();
					}

					// These are the only commands that are intercepted
					else if ((command.equals("quit"))
							|| (command.equals("exit"))
							|| (command.equals("bye"))
							|| (command.equals("logout"))) {
						endSession();

						/* DOH! Don't return, else the thread exits and we have no more session!
						 * this is bad if they have su'd!
						 */
						//  return;
					} else {
						execute(command);
					}
				}

				if ((!shutdown) && out.checkError()) {
					//com.dalsemi.system.Debug.debugDump("Session PANIC: Trouble.");   //DEBUG
					forceEndSession();
					return;
				}
			}
			//let's just shut down the session if any thing comes out this far
			catch (Throwable e) //(IOException ioe)
			{
				if (!shutdown) {

					//Could be a bad IOException or an InterruptedIOException.  Either way, drop session.
					forceEndSession();

					//com.dalsemi.system.Debug.debugDump("THREAD "+ this.myThreadID +"BAILING in IOException!!");
					return;
				}
			}
		}

	}

	/**
	 * Executes the given command in the current shell.  The <code>commandStr</code>
	 * parameter can contain any number of elements.  These elements will be separated
	 * into <code>String[]</code> for the command interpreter.
	 *
	 * @param commandStr  full command line to execute
	 */
	protected void execute(String commandStr) {
		try {
			if ((commandStr == null) || (commandStr.length() == 0))
				return;

			if (commandStr.startsWith("!")) {
				if (commandStr.equals("!!")) {
					commandStr = stepUpHistory();

					out.print(commandStr + "\r\n");
				} else {
					commandStr = commandStr.substring(1);

					try {
						commandStr = getHistoryNumber(Integer
								.parseInt(commandStr));
					} catch (NumberFormatException nfe) {
						boolean found = false;

						//Must be a string match request
						for (int i = 0; i < commandHistory.length; i++) {
							if (commandHistory[i] != null) {
								if (commandHistory[i].startsWith(commandStr)) {
									commandStr = commandHistory[i];
									found = true;

									break;
								}
							}
						}

						if (!found) {
							throw new Exception("No match found.");
						}
					}
					out.print(commandStr + "\r\n");
				}
			}

			//Object[] commandLine = localGetParams(commandStr);

			inCommand = true;

			addToHistory(commandStr);
			environment.put(CURRENT_COMMAND, commandStr);
			//execute(commandStr, in, out, err, environment);
			log(commandStr, 1);
			commandDispather(commandStr);
		} catch (Exception e) {
			if (out.checkError())
				forceEndSession();
			else
				exceptionThrown(e);
		}

		if (!shutdown) {
			currentCommandFinished();
			environment.put(CURRENT_COMMAND, "");
		}

		inCommand = false;
	}

	/**
	 * Gets the stream this session uses for error notification and critical messages.
	 *
	 * @return  the session's PrintStream used for errors
	 */
	public PrintStream getErrStream() {
		return err;
	}

	/**
	 * Gets the stream this session uses for output.
	 *
	 * @return  the session's PrintStream
	 */
	public PrintStream getOutputStream() {
		return out;
	}

	/**
	 * Notifies the server this session is ending and forces the session to terminate.
	 * This is used when some error has occurred that the session and server do not know how to handle.
	 */
	public synchronized final void forceEndSession() {

		//If we are not already attempting to shutdown...
		if (!shutdown) {
			shutdown = true;
			sessionEnding();
			if (server != null)
				server.sessionEnded(this);
			//TINIOS.logout(myThreadID);
		}
	}

	/**
	 * Cleans up the resources used by this session.
	 */
	public synchronized final void endSession() {
		if (loginStack.size() > 0) {
			Login login = (Login) loginStack.elementAt(0);

			loginStack.removeElementAt(0);

			userName = login.userName;
			password = login.password;

			//TINIOS.login(userName, password);
			currentCommandFinished();
		} else {
			//say goodbye because we're trying to behave here
			if (this instanceof cdrfile.telnet.TelnetSession)
				try {
					err.print("Connection Terminated.\r\n");
				} catch (Exception e) {
					//snif...didn't say good-bye
				}
			sessionEnding();
			if (server != null)
				server.sessionEnded(this);

			shutdown = true;

			//TINIOS.logout(myThreadID);
		}
	}

	/**
	 * Encapsulates all of the information needed to log a user into the
	 * system.  This includes the user's name and password.
	 */
	protected class Login {
		String userName;
		String password;

		/**
		 * Stores the user's name and password.
		 *
		 * @param userName the user's name
		 * @param password the user's password
		 */
		public Login(String userName, String password) {
			this.userName = userName;
			this.password = password;
		}
	}

	/**
	 * Allows the current user to login as another user.  The old user's
	 * identity is added to a login stack.  Once the new user terminates
	 * their session, the old user idenity is resumed.
	 *
	 * @param userName  new user's name
	 * @param password  new user's password
	 *
	 * @return <code>true</code> if login was successful, <code>false</code>
	 * otherwise
	 */
	public boolean su(String userName, String password) {
		//if (TINIOS.login(userName, password) != -1)
		{
			loginStack.insertElementAt(new Login(this.userName, this.password),
					0);

			this.userName = userName;
			this.password = password;

			return true;
		}
		//else
		//{
		// return false;
		//}
	}

	/**
	 * Notifies this session that the current command has completed.  For example,
	 * the user types "ls".  The command is received and parsed, then the appropriate
	 * command is called in the shell.  Finally, this session may need to again
	 * display the system prompt or perform other session-specific functions.
	 *
	 * Only call this from a synchronized block!!!
	 */
	protected abstract void currentCommandFinished();

	/**
	 * Cleans up any resources associated with this session when it terminates.
	 */
	protected abstract void sessionEnding();

	/**
	 * Notifies this session that exception was thrown when executing a command.
	 * This method will attempt to notify the user.  Any exceptions raised specifically by
	 * shell commands should try to give as descriptive a message to the
	 * exception as possible.
	 *
	 * @param ex  the exception thrown
	 */
	protected abstract void exceptionThrown(Exception ex);

	// Turns the given string into a string array using quotes, spaces, and tabs as delimiters.
	/*private Object[] localGetParams (String str)
	    {
	    getParams(str, paramsVector, paramsArray, retArray);
	    paramsVector.removeAllElements();
	    return retArray;
	    }
	    /**
	 * Parses the command line into individual elements.  The parser will
	 * use whitespace characters and quotes as delimiters.
	 *
	 * @param str  complete command line
	 *
	 * @return  an object array that contains the <code>String</code>
	 * command in the first element, and a <code>String[]</code> of
	 * the arguments to that command in the second element
	 */
	/*public Object[] getParams (String str)
	    {
	    Object[] cmdArray = new Object [2];
	    getParams(str, new Vector(5), null, cmdArray);
	    return cmdArray;
	    }
	    private void getParams (String str, Vector vector,
	       Object[] paramsArray, Object[] retArray)
	    {
	    int start = 0;
	    int end   = 0;
	    while (start != -1)
	    {
	    start = findStartOfToken(str, start);
	    if (start == -1)
	    {
	    break;
	    }
	    end = findEndOfToken(str, start);
	    int    quotePos = str.indexOf('"', start);
	    String temp     = str.substring(start, end);
	    if (quotePos != -1)
	    {
	    if (quotePos < end)
	    {
	   int tempPos;
	   while ((tempPos = temp.indexOf('"')) != -1)
	   temp = temp.substring(0, tempPos)
	    + temp.substring(tempPos + 1);
	    }
	    }
	    vector.addElement(temp);
	    start = end;
	    }
	    if (vector.size() != 0)
	    {
	    retArray [0] = vector.elementAt(0);
	    vector.removeElementAt(0);
	    }
	    else
	    retArray [0] = "";
	    // Convert the rest of the vector to a string array.
	    String[] params = null;
	    if (paramsArray != null)
	    {
	    if (paramsArray.length > vector.size())
	    {
	    params = ( String[] ) paramsArray [vector.size()];
	    }
	    else
	    {
	    params = new String [vector.size()];
	    }
	    }
	    else
	    {
	    params = new String [vector.size()];
	    }
	    vector.copyInto(params);
	    retArray [1] = params;
	    }
	    private static int findStartOfToken (String str, int pos)
	    {
	    for (; pos < str.length(); pos++)
	    {
	    char c = str.charAt(pos);
	    if (!(c == ' ' || c == '\t'))
	    {
	    return pos;
	    }
	    }
	    return -1;
	    }
	    private static int findEndOfToken (String str, int pos)
	    {
	    String currDelimiters = " \"\t";
	    while (true)
	    {
	    while ((pos < str.length())
	 && (currDelimiters.indexOf(str.charAt(pos)) == -1))
	    {
	    pos++;
	    }
	    if (pos == str.length())
	    {
	    return pos;
	    }
	    else if (str.charAt(pos) == '"')
	    {
	    if (currDelimiters.equals("\""))
	   currDelimiters = " \"\t";
	    else
	   currDelimiters = "\"";
	    }
	    else
	    {
	    return pos;
	    }
	    pos++;
	    }
	    }
	    /**
	 * Gets the next command from this session's input stream.
	 * This effectively performs a readLine(), but gives this session a chance
	 * to parse the incoming data for special characters and commands.
	 *
	 * @return  the next command from the input stream
	 */
	public String getNextCommand() throws IOException {
		return in.readLine();
	}

	/**
	 * Gets a reference to the current environment.
	 *
	 * @return  the current environment as a <code>Hashtable</code>
	 */
	public Hashtable getEnvironment() {
		return environment;
	}

	/**
	 * Gets the value of the key from the current environment.
	 *
	 * @param key  name of desired environment variable
	 *
	 * @return the value specified by the given key, or <code>null</code>
	 * if that key does not exist in the environment
	 */
	public String getFromEnvironment(String key) {
		return (String) environment.get(key);
	}

	/**
	 * Adds a new command to the history buffer.
	 *
	 * @param str  command to add to the history
	 */
	public void addToHistory(String str) {
		commandHistory[lastCommand] = str;
		lastCommand = (lastCommand + 1) % commandHistory.length;
		commandHistory[lastCommand] = null;
		currentCommand = lastCommand;
	}

	/**
	 * Moves the current position in the history buffer up one
	 * and returns the command at that position.
	 *
	 * @return  the command one up from the current position in the
	 * history buffer
	 */
	public String stepUpHistory() {
		if (currentCommand == 0)
			currentCommand = commandHistory.length - 1;
		else
			currentCommand = (currentCommand - 1) % commandHistory.length;

		if (commandHistory[currentCommand] == null) {
			currentCommand = (currentCommand + 1) % commandHistory.length;

			return null;
		}

		return commandHistory[currentCommand];
	}

	/**
	 * Moves the current position in the history buffer down one
	 * and returns the command at that position.
	 *
	 * @return  the command one down from the current position in the
	 * history buffer
	 */
	public String stepDownHistory() {
		if (commandHistory[currentCommand] == null) {
			return "";
		}

		currentCommand = (currentCommand + 1) % commandHistory.length;

		if (commandHistory[currentCommand] == null) {
			return "";
		}

		return commandHistory[currentCommand];
	}

	/**
	 * Gets the command at the given index of the command history.
	 *
	 * @param number  index into the history cache.
	 *
	 * @return the command specified by the given number
	 */
	public String getHistoryNumber(int number) {
		if ((number > commandHistory.length)
				|| (commandHistory[number - 1] == null)) {
			return "";
		}

		return commandHistory[number - 1];
	}

	/**
	 * Prints the list of commands stored in the history buffer of this session.
	 *
	 * @param out  stream used to print the history
	 */
	public void printHistory(PrintStream out) {
		for (int i = 0; i < commandHistory.length; i++) {
			out.print((i + 1) + " "
					+ ((commandHistory[i] == null) ? "" : commandHistory[i])
					+ "\r\n");
		}
	}

	/**
	 * Indicates whether this session is executing a shell command.
	 *
	 * @return <code>true</code> if the session is executing a shell command
	 */
	public boolean inCommand() {
		return inCommand;
	}

	/**
	 * Notifies this session of a directory change.  This allows the command line
	 * prompt to reflect the change.
	 *
	 * @param withThis  the new directory name
	 */
	public void updatePrompt(String withThis) {
	}

	/**
	 * Displays a message in this session.  This functionality may be disabled
	 * by setting the environment variable "BROADCASTS" to "false".
	 *
	 * @param sendThis  message to display
	 */
	public void broadcast(String sendThis) {
		String disable = (String) environment.get("BROADCASTS");

		if ((disable != null) && (disable.equals("false"))) {
			return;
		}

		out.print(sendThis + "\r\n");
	}

	/**
	 * Executes a command in the shell.
	 *
	 * @param commandLine  An Object array containing the command in the first
	 * element, followed by any parameters need for that command in a String[]
	 * in the second element.
	 * @param in  The stream the command will use to get input.
	 * @param out  The stream used to report non-critical messages.
	 * @param err  The stream used to report critical messages.
	 * @param env  A table of environment variables.
	 *
	 * @throws Exception Any exception raised by the command.
	 */
	public void execute(String commandLine, SystemInputStream in,
			SystemPrintStream out, SystemPrintStream err, Hashtable env)
			throws Exception {
		commandDispather(commandLine);
		//out.print(commandLine);   //DEBUG
	}

	private void commandDispather(String commandline) throws Exception {
		Method method = null;
		String methodName = commandline;
		if (commandline.indexOf(" ") >= 0)
			methodName = commandline.substring(0, commandline.indexOf(" "));

		Class[] arrClass = new Class[1];
		arrClass[0] = String.class;
		Object[] arrArg = new Object[1];
		arrArg[0] = commandline;
		try {
			method = this.getClass().getDeclaredMethod(methodName, arrClass);
			if (method != null) {
				method.invoke(this, arrArg);
			}
		} catch (NoSuchMethodException e) {
			log(" - Command '" + commandline + "' not found", 1);
			throw new Exception("Command not found");
		} catch (InvocationTargetException e) {
			log(e.getTargetException().getMessage(), 1);
			throw new Exception(e.getTargetException().getMessage());
		}

	}

	public void log(String text, int level) {
	}
}
