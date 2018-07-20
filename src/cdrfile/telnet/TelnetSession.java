package cdrfile.telnet;

/**
 * <p>Title: CDR File(s) System</p>
 * <p>Description: VMS IS Departerment</p>
 * <p>Copyright: Copyright (c)  by eKnowledge 2004</p>
 * <p>Company: VietNam Mobile Telecom Services</p>
 * @author eKnowledge - Software
 * @version 1.0
 */

import java.io.*;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;
import cdrfile.global.ClientUtil;
import cdrfile.global.Global;

public class TelnetSession extends Session
{
    Socket socket;
    private boolean _viaSocket = true;
    private int logLevel = 10;

    /**
     * Initializes the session.
     *
     *
     * @param in
     *            InputStream this session should use to get data from user.
     * @param out
     *            OutputStream this session should use to output to user.
     * @param err
     *            ErrorStream this session should use to output errors to user.
     * @param server
     *            The server in charge of this session.
     */
    public TelnetSession(SystemInputStream in, SystemPrintStream out, SystemPrintStream err, Socket s, Server server)
    {
        super(in, out, err, server);

        socket = s;
        _viaSocket = socket != null ? true : false;

        log("Connected.", 1);
        prompt = "cdrfile$".getBytes(); /*
        * (TININet.getHostname() +
        * environment.get(CURRENT_DIRECTORY) +
        * PROMPT).getBytes();
        */
    }

    public void print(String str)
    {
        out.print(str);
    }

    public void println(String str)
    {
        out.print(str);
        out.print("\r\n");
    }

    /**
     *
     */
    public void flush()
    {
        out.flush();
    }

    private void welcome()
    {
        String welcome = "********************************************************************************\r\n" + "*                     Welcome to CDRFILE Centralized System                    *\r\n" + "*                                 Version 5.0                                  *\r\n" + "*         Copyright(c) by eKnowledge Software 2008. All Rights Reserved.       *\r\n" + "********************************************************************************\r\n";
        out.println(welcome);
    }

    private boolean login(String strUserName, String strPassword) throws Exception
    {
        java.util.Date dtNow = new java.util.Date();
        ResultSet rs = null;
        Statement stmt = null;
        String strHostAddress = "";
        String mSql = "";
        String mReturn = "";
        try
        {
            strHostAddress = (isViaSocket() ? getSocket().getInetAddress().getHostAddress() : "unknown");
            mSql = "select fullname from users a,groupuser b " + " where a.userid=b.userid and  b.groupid=62 and status=1" + " and username='" + strUserName + "' and password='" + strPassword + "'";
            stmt = ClientUtil.openNewConnection().createStatement();
            rs = stmt.executeQuery(mSql);

            while (rs.next())
            {
                mReturn = rs.getString("fullname");
            }
        }
        catch (Exception e)
        {
            try
            {
                System.out.println("Could not connect to DB");
            }
            catch (Exception ex)
            {
            }
        }
        finally
        {
            // Close connection
            try
            {
                // Release
                rs.close();
                rs = null;
                stmt.close();
                stmt = null;
            }
            catch (Exception e)
            {
            }
        }
        if (mReturn.compareTo("") != 0)
        {
            out.print("User: " + mReturn + " - Login at: " + dtNow.toString() + " from " + strHostAddress + "\r\n");
            return true;
        }
        else
        {
            out.print("Invalid username or password. ");
            return false;
        }

    }

    /**
     * Logs the user into the system. This function will prompt the user for
     * username and password.
     */
    public void login() throws IOException
    {
        boolean connected = false;
        int loginAttempts = 0;
        if (Global.mPortManager == 0)
        {
            welcome();
        }
        while (!connected)
        {
            try
            {

                TelnetInputStream tin = (TelnetInputStream) in;
                tin.negotiateEcho();

                out.print("login: ");

                userName = in.readLine();

                if (userName == null)
                {
                    forceEndSession();
                    return;
                }
                out.print("password: ");

                boolean ec = in.getEcho();
                in.setEcho(false); // tell the input stream not to echo
                // this

                password = in.readLine();
                if (password == null)
                {
                    forceEndSession();
                    return;
                }

                in.setEcho(ec);

                // now that we changed TelnetInputStream not to echo all
                // line-feeds, we see
                // password: TINI\>
                // after we've logged in, so print this to make it look nice
                out.print("\r\n");

                // Now, check to see if this is a correct user/password...
                // First, though, check to see if user is attempting to
                // login
                // as root. If this is not allowed (based on the environment
                // variable "TELNET_ALLOW_ROOT"), then don't attempt login.
                if (userName.equals("root") && server != null && !server.isRootAllowed())
                {
                    connected = false;
                }
                else if (userName.equals("sa") && password.equals("sa"))
                {
                    connected = true;
                }
                else if (login(userName, password))
                {
                    connected = true;
                }

                if (!connected)
                {
                    out.print("Login incorrect.\r\n\r\n");

                    if (++loginAttempts == 3)
                    {
                        forceEndSession();

                        return;
                    }
                }

                if (out.checkError())
                {
                    forceEndSession();
                    return;
                }
            }
            catch (Exception e)
            {
                forceEndSession();
                throw new IOException();
            }
        }

        out.setSession(this); // tell the output stream who the session is
        in.setSession(this); // tell the input stream who the session is

        // Now, display a welcome message if one was specified...
        if (server != null)
        {
            String welcomeMsg = server.getWelcomeMsg();
            out.print(welcomeMsg);
        }
        currentCommandFinished();
    }

    /**
     * Called to clean up when the session is ending. (i.e. the user typed
     * "exit", or something bad happened that causes the session to terminate.)
     * Closes all sockets opened by this session.
     */
    protected synchronized void sessionEnding()
    {
        try
        {
            /*
             * we might not necessarily be playing nice here...if we called
             * forceEndSession then we don't really want to say goodbye, so lets
             * not burn those CPU cycles on the exception. say goodbye nice in
             * the Session code try { err.write("Connection
             * Terminated.\r\n".getBytes()); } catch (Throwable t) { //DRAIN }
             */

            // com.dalsemi.system.Debug.debugDump("telnet close:
            // "+socket.getPort());
            log("Disconnected.\n", 1);
            socket.close();

            out = null;
            err = null;
            in = null;
        }
        catch (Throwable t)
        {
        }
    }

    /**
     * Called after each command is completed. For example, the user types "ls".
     * The command is received and parsed, then the appropriate command is
     * called in the shell. Finally, we need to again display the system prompt.
     */
    public void currentCommandFinished()
    {
        try
        {
            out.write(prompt);
        }
        catch (Exception e)
        {
            /*
             * com.dalsemi.system.Debug.debugDump("TELNET Prompt error: " +
             * e.toString());
             */
        }
    }

    /**
     * This method was added to speed up prompt printing. It is called when the
     * prompt needs to change because the user changed directory.
     *
     * @param withThis
     *            the current directory
     */
    public void updatePrompt(String withThis)
    {

        // withThis will be the currentDirectory
        prompt = ( /* TININet.getHostname() + ' ' + */withThis
            /* + PROMPT */).getBytes();
    }

    /**
     * Called when an exception is thrown in a command. This function will
     * attempt to notify the user. Any exceptions raised specifically by shell
     * commands should try to give as descriptive a message to the exception as
     * possible.
     *
     * @param ex
     *            the exception thrown
     */
    protected void exceptionThrown(Exception ex)
    {
        try
        {
            // com.dalsemi.system.Debug.debugDump("telnet exception: "+ex);
            String message = ex.getMessage();

            if ((message == null) || (message.length() == 0))
            {
                out.print("Exception occurred: " + ex.toString() + "\r\n");
            }
            else
            {
                out.print(message + "\r\n");
            }

            if (out.checkError())
            {
                // com.dalsemi.system.Debug.debugDump("T.eT trouble.");
                forceEndSession();
            }
        }
        catch (Exception ioe)
        {
            // com.dalsemi.system.Debug.debugDump("Telnet.eT: PANIC!");
            forceEndSession();
        }
    }

    protected void help(String commandline)
    {
        try
        {
            if (commandline.compareTo("help startup") == 0)
            {
                println("");
                println("");
                println("DESCRIPTION");
                println("");
                println("     startup - bring all the thread up");
                println("");
                println("");
                println("USAGE:");
                println("     startup");
                println("");
                println("");
            }
            else if (commandline.compareTo("help shutdown") == 0)
            {
                println("");
                println("");
                println("DESCRIPTION");
                println("");
                println("     shutdown - bring the service down");
                println("");
                println("");
                println("USAGE:");
                println("     shutdown");
                println("");
                println("");
            }
            else if (commandline.compareTo("help start") == 0)
            {
                println("");
                println("");
                println("DESCRIPTION");
                println("");
                println("     start - bring the thread up");
                println("");
                println("");
                println("USAGE:");
                println("     start [ -all | threadID ]");
                println("");
                println("  Options:");
                println("     -all            Stop all safely thread running.");
                println("");
                println("     threadID        Closes the thread ran before it shuts down. The Service is");
                println("                     safely shutdown.");
                println("");
                println("     -abort          Quickly shuts down. The Service is technically scrashed. ");
                println("");
            }
            else if (commandline.compareTo("help stop") == 0)
            {

            }
            else if (commandline.compareTo("help log") == 0)
            {

            }
            else
            {
                Vector vtCommand = new Vector();
                vtCommand.addElement("For more information on a specific command, type: help command-name");
                vtCommand.addElement(" - exit: logout system");
                vtCommand.addElement(" - help: display help");
                vtCommand.addElement(" - startup: startup service");
                vtCommand.addElement(" - shutdown: shutdown service");
                vtCommand.addElement(" - start: start thread");
                vtCommand.addElement(" - stop: stop thread");
                vtCommand.addElement(" - list: list all threads in the system");
                vtCommand.addElement(" - view: view param of thread");
                vtCommand.addElement(" - set: set param of thread");
                vtCommand.addElement(" - log: view log of thread");
                vtCommand.addElement(" - clear: clear screen");
                for (int i = 0; i < vtCommand.size(); i++)
                {
                    println(vtCommand.elementAt(i).toString());
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void ls(String commandline)
    {
        list(commandline);
    }

    protected void list(String commandline)
    {
        try
        {
            execshell("cdrfile", 10000);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void start(String commandline)
    {
        try
        {
            cdrfileservice(commandline);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void stop(String commandline)
    {
        try
        {
            cdrfileservice(commandline);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void restart(String commandline)
    {
        try
        {
            cdrfileservice(commandline);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void view(String commandline)
    {
        try
        {
            // telnet.println(commandline);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void clear(String commandline)
    {
        try
        {
            char esc = 27;
            String clear = esc + "[2J";
            out.print(clear);
            out.flush();
            welcome();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void ping(String commandline)
    {
        try
        {
            println("SERVER is alive.");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void cls(String commandline)
    {
        clear(commandline);
    }


    protected void cdrfileservice(String commandline) throws Exception
    {
        try
        {
            execshell("/export/home/cdrfile/bin/cdrfileservice " + commandline, 10000);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public int execshell(String strCommand, int iTimeout) throws Exception
    {
        // Execute command
        Process process = Runtime.getRuntime().exec(strCommand);

        // Run cleaner
        new ProcessCleaner(process);

        StringBuffer strLog = new StringBuffer();
        StringBuffer strErrLog = new StringBuffer();
        long lStartDate = System.currentTimeMillis();
        char cLog;
        char cErrLog;

        try
        {
            while (true)
            {
                int iAvailable = process.getInputStream().available();
                int iErrAvailable = process.getErrorStream().available();
                while (iAvailable > 0 || iErrAvailable > 0)
                {
                    if (iAvailable > 0)
                    {
                        // Fill log
                        cLog = (char) process.getInputStream().read();
                        if (cLog == '\n' || cLog == 13)
                        {
                            if (strLog.length() > 0)
                            {
                                log("\t" + strLog.toString(), 1);
                                println(strLog.toString());
                            }
                            lStartDate = new java.util.Date().getTime();
                            strLog = new StringBuffer();
                        }
                        else
                        {
                            strLog.append(cLog);
                        }
                        iAvailable--;
                    }
                    else
                    {
                        // Fill log
                        cErrLog = (char) process.getErrorStream().read();
                        if (cErrLog == '\n' || cErrLog == 13)
                        {
                            if (strErrLog.length() > 0)
                            {
                                log("\t" + strErrLog.toString(), 1);
                                println(strErrLog.toString());
                            }
                            lStartDate = new java.util.Date().getTime();
                            strErrLog = new StringBuffer();
                        }
                        else
                        {
                            strErrLog.append(cErrLog);
                        }
                        iAvailable--;
                    }
                }
                if (System.currentTimeMillis() - lStartDate > iTimeout)
                {
                    process.destroy();
                    throw new Exception("Time out reached, command execution was destroyed.");
                }
            }
        }
        catch (Exception e)
        {
        }
        return process.exitValue();
    }

    protected void shutdown(String commandline)
    {
        try
        {
            endSession();
            super.server.shutDown();
            log("The Service is shutdown abort.", 1);
            System.exit(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public Socket getSocket()
    {
        return this.socket;
    }

    public boolean isViaSocket()
    {
        return _viaSocket;
    }

    public void log(String text, int level)
    {
        if (level <= logLevel)
        {
            System.err.println((_viaSocket ? Global.Format(new java.util.Date(), "dd/MM/yyyy HH:mm:ss") + " - IP:" + getSocket().getInetAddress().getHostAddress() + " - Host:" + socket.getInetAddress().getHostName() : Global.Format(new java.util.Date(), "dd/MM/yyyy HH:mm:ss") + " : ") + " : " + text);
        }
    }
}
