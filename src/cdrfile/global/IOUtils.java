package cdrfile.global;

/**
 * <p>Title: CDR File(s) System</p>
 * <p>Description: VMS IS Departerment</p>
 * <p>Copyright: Copyright (c)  by eKnowledge 2004</p>
 * <p>Company: VietNam Mobile Telecom Services</p>
 * @author eKnowledge - Software
 * @version 1.0
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Miscellaneous file- and IO-related utility methods
 *
 * @version $Id: IOUtils.java,v 1.6 2002/03/13 22:13:50 alex Exp $
 */
public class IOUtils
{
    /**
     * @param file
     *            file to read
     * @returns String containing contents of file
     */
    public static String readFile(File file) throws IOException
    {
        FileReader filereader = null;
        String s = null;
        try
        {
            filereader = new FileReader(file);
            s = readReader(filereader);
        }
        finally
        {
            if (filereader != null)
            {
                filereader.close();
            }
        }
        return s;
    }

    /**
     * @param input
     *            stream to read
     * @returns String containing contents of file
     */
    public static String readStream(InputStream input) throws IOException
    {
        return readReader(new InputStreamReader(input));
    }

    /**
     * @param input
     *            stream to read
     * @returns String containing contents of file
     */
    public static String readReader(Reader input) throws IOException
    {
        try
        {
            StringBuffer buf = new StringBuffer();
            BufferedReader in = new BufferedReader(input);
            int ch;
            while ((ch = in.read()) != -1)
            {
                buf.append((char) ch);
            }
            return buf.toString();
        }
        finally
        {
            input.close();
        }
    }

    /**
     * Place a ".lock" file next to the given file. If one exists, block
     * (polling every second) until the existing lock expires. If it's still
     * there after 60 seconds, steal the lock. <!-- Also keeps an in-memory
     * monitor lock set as a backup. -->
     *
     * @see #unlock(File)
     */
    public static void lock(File f) throws IOException
    {
        File lock = lockfile(f);
        // System.err.println("locking " + lock);
        for (int i = 0; i < 60; ++i)
        {
            if (i == 59)
            {
                System.err.println("stealing lock on " + f);
                lock.delete();
                i = 0;
            }
            /**
             * javadoc for createNewFile sez: "The check for the existence of
             * the file and the creation of the file if it does not exist are a
             * single operation that is atomic with respect to all other
             * filesystem activities that might affect the file."
             */
            if (lock.createNewFile())
            {
                break;
            }
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
            }
        }
        // timestamp the lock
        PrintWriter out = new PrintWriter(new FileWriter(lock));
        out.println(System.currentTimeMillis());
        out.close();
    }

    /**
     * Removes the ".lock" file
     *
     * @see #lock(File)
     */
    public static void unlock(File f) throws IOException
    {
        File lock = lockfile(f);
        lock.delete();
    }

    private static File lockfile(File f)
    {
        return new File(f.getParent(), f.getName() + ".lock");
    }

    /**
     * Prints a very simple log message to System.err
     */
    public static void log(String s)
    {
        s = new Date().toString() + ": " + s;
        System.err.println(s);
    }

    /**
     * OBSOLETE
     *
     * @returns null if file not found
     * @deprecated readFile
     */
    public static String loadFile(File file) throws IOException
    {
        BufferedReader in = null;
        try
        {
            in = new BufferedReader(new FileReader(file));
        }
        catch (FileNotFoundException fnfe)
        {
            log(fnfe.toString());
            return null;
        }
        StringBuffer buf = new StringBuffer();
        String line;
        while ((line = in.readLine()) != null)
        {
            buf.append(line);
            buf.append("\n");
        }
        if (in != null)
        {
            in.close();
        }
        return buf.toString();
    }

    public static Properties loadProperties(String filename) throws IOException
    {
        return loadProperties(new File(filename));
    }

    public static Properties loadProperties(File file) throws IOException
    {
        Properties properties = null;
        InputStream in = null;
        try
        {
            in = new FileInputStream(file);
            properties = new Properties();
            properties.load(in);
        }
        finally
        {
            try
            {
                if (in != null)
                {
                    in.close();
                }
            }
            catch (IOException e)
            {
            }
        }
        return properties;
    }

    /**
     * Write the input stream to the file as raw data
     *
     * @param dir
     *            directory to put the file in
     * @param name
     *            name of new file
     * @param data
     *            the data to write in the file
     */
    public static void writeFile(File dir, String name, InputStream data) throws IOException
    {
        InputStream in = null;
        OutputStream out = null;
        try
        {
            File outFile = new File(dir, name);
            in = new BufferedInputStream(data);
            out = new BufferedOutputStream(new FileOutputStream(outFile));
            int ch;
            while ((ch = in.read()) != -1)
            {
                out.write(ch);
            }
        }
        catch (IOException ioe)
        {
            log("Error in writeFile: " + ioe);
//			ioe.printStackTrace();
            throw ioe;
        }
        finally
        {
            try
            {
                if (out != null)
                {
                    out.close();
                }
                if (in != null)
                {
                    in.close();
                }
            }
            catch (Exception e)
            {
            }
        }
    } // writeFile

    /**
     * Write the input string to the file as raw data
     *
     * @param dir
     *            directory to put the file in
     * @param name
     *            name of new file
     * @param data
     *            the data to write in the file
     */
    public static void writeString(File dir, String name, String data) throws IOException
    {
        writeString(new File(dir, name), data);
    }

    /**
     * Write the input string to the file as raw data
     *
     * @param name
     *            name of new file
     * @param data
     *            the data to write in the file
     */
    public static void writeString(File file, String data) throws IOException
    {
        PrintWriter out = null;
        try
        {
            out = new PrintWriter(new FileOutputStream(file));
            out.print(data);
        }
        finally
        {
            // try {
            if (out != null)
            {
                out.close();
            }
            // } catch (IOException e) {}
        }
    }

    /**
     * Write the properties parameter to the file as raw data
     *
     * @param dir
     *            directory to put the file in
     * @param name
     *            name of new file
     * @param data
     *            the data to write in the file
     */
    public static void writeProperties(File dir, String name, Properties prop) throws IOException
    {
        File file;
        FileOutputStream out = null;
        try
        {
            file = new File(dir, name);
            out = new FileOutputStream(file);
            prop.store(out, "properties for " + name + " written " + new Date());
        }
        finally
        {
            try
            {
                if (out != null)
                {
                    out.close();
                }
            }
            catch (IOException e)
            {
            }
        }
    }

    public String FillPath(String pPath, String pSeparateFile)
    {
        return (pPath.substring(pPath.length() - 1).compareTo(pSeparateFile) == 0 ? pPath : pPath + pSeparateFile);
    }

    /**
     * Removes "./" and "foo/../" and trailing "/" from pathname turns \
     * (backslash) into / (forward slash)
     */
    public static String fixPath(String path) throws IOException
    {
        Stack stack = getPathStack(path);

        // turn stack into path
        StringBuffer fixed = new StringBuffer(path.length());
        Iterator i = stack.iterator();
        boolean first = true;
        while (i.hasNext())
        {
            String s = (String) i.next();
            if (first)
            {
                first = false;
            }
            else
            {
                fixed.append("/");
            }
            fixed.append(s);
        }
        return fixed.toString();
    }

    /**
     * Given a filesystem path with slashes, turns it into a stack, where each
     * entry on the stack is a directory in the path. Deals properly with "./"
     * and "../" (in the former case, it ignores it; in the latter case, it pops
     * the previous entry) so all you're left with is valid directories. Does
     * not deal with symlinks etc.
     */
    public static Stack getPathStack(String path) throws IOException
    {
        // turn it into a list
        StringTokenizer tok = new StringTokenizer(path, "/\\");
        Stack stack = new Stack();
        String s = "";
        try
        {
            while (tok.hasMoreTokens())
            {
                s = tok.nextToken();
                if (s.equals("."))
                {
                    continue;
                }
                else if (s.equals(".."))
                {
                    stack.pop();
                }
                else
                {
                    stack.push(s);
                }
            }
        }
        catch (EmptyStackException e)
        {
            throw new IOException("Bad path " + path + " - too many ..s");
        }
        return stack;
    }

    /**
     * compares the contents of two files
     *
     * @return true if the two files differ at any point
     */
    public static boolean isDiff(File a, File b) throws IOException
    {
        InputStream inA = null, inB = null;
        try
        {
            inA = new BufferedInputStream(new FileInputStream(a));
            inB = new BufferedInputStream(new FileInputStream(b));
            return isDiff(inA, inB);
        }
        finally
        {
            try
            {
                if (inA != null)
                {
                    inA.close();
                }
            }
            finally
            {
                if (inB != null)
                {
                    inB.close();
                }
            }
        }
    }

    /**
     * compares the contents of two streams
     *
     * @return true if the two streams differ at any point
     */
    public static boolean isDiff(InputStream a, InputStream b) throws IOException
    {
        int x, y;

        do
        {
            x = a.read();
            y = b.read();
            // if a byte doesn't correspond, or if one ends before the
            // other, they're different
            if (x != y)
            {
                return true;
            }

        }
        while (x != -1); return false;
    }

    //* Nang cap pha 6*//
    /**
     * compares the contents of two file:
     * 1. source file
     * 2. destination file in file zip
     * @return true if the two streams differ at any point
     */

    /*public static boolean isDiff(String pSourceFile, String pDestinationZip){
       ZipInputStream zipInStream = null;
       BufferedInputStream sourceStream = null;
       boolean isDiff = false;
       File srcFile = new File(pSourceFile);

       try {
           ZipFile zf = new ZipFile(pDestinationZip);
           sourceStream = new BufferedInputStream(new FileInputStream(srcFile));
           zipInStream = new ZipInputStream(new FileInputStream(pDestinationZip));
           ZipEntry ze = (ZipEntry) zipInStream.getNextEntry();
           InputStream stream = zf.getInputStream(ze);
           if (IOUtils.isDiff(sourceStream, stream))
               isDiff =  true;
            else
                isDiff = false;

       } catch (IOException ex1) {
           isDiff = false;
           ex1.printStackTrace();
       }finally{
            try {
                if (zipInStream != null)
                    zipInStream.close();
                if(sourceStream != null)
                    sourceStream.close();
            } catch (IOException ex) {
            }
       }
       return isDiff;
            }*/
    public static boolean isDiff(String pSourceFile, String pDestinationZip)
    {
        ZipInputStream zipInStream = null;
        boolean isDiff = false;
        File srcFile = new File(pSourceFile);

        try
        {
            long srclastModified = srcFile.lastModified();
            zipInStream = new ZipInputStream(new FileInputStream(pDestinationZip));
            ZipEntry ze = (ZipEntry) zipInStream.getNextEntry();
            long fileInzipTime = ze.getTime();
            Date inzipDate = new Date(fileInzipTime);
            Date insrcDate = new Date(srclastModified);
            if (!inzipDate.toString().equals(insrcDate.toString()))
            {
                isDiff = true;
            }
            else
            {
                isDiff = false;
            }

        }
        catch (Exception ex1)
        {
            isDiff = false;
            System.err.println(Global.Format(new java.util.Date(), "dd/MM/yyyy HH:mm:ss") + ": ERROR in method isDiff():" + ex1.getMessage());
        }
        finally
        {
            try
            {
                if (zipInStream != null)
                {
                    zipInStream.close();
                }
            }
            catch (IOException ex)
            {
                isDiff = false;
            }
        }
        return isDiff;
    }

    public static boolean isDiff(String[] pSourceFiles, String pDestinationZip)
    {
        ZipInputStream zipInStream = null;
        boolean isDiff = false;
        boolean hasInZip = false;
        try
        {
            for (int i = 0; i < pSourceFiles.length; i++)
            {
                File srcFile = new File(pSourceFiles[i]);
                long srcLastmodified = srcFile.lastModified();
                zipInStream = new ZipInputStream(new FileInputStream(pDestinationZip));
                ZipEntry ze = null;
                while ((ze = (ZipEntry) zipInStream.getNextEntry()) != null)
                {
                    if (ze.getName().equals(srcFile.getName()))
                    {
                        hasInZip = true;
                        long fileInZipLastModified = ze.getTime();
                        Date inzipDate = new Date(fileInZipLastModified);
                        Date insrcDate = new Date(srcLastmodified);
                        if (!inzipDate.toString().equals(insrcDate.toString()))
                        {
                            isDiff = true;
                        }
                        else
                        {
                            isDiff = false;
                        }
                    }
                    else
                    {
                        hasInZip = false;
                    }
                }
                zipInStream.close();
                if (hasInZip == false)
                {
                    isDiff = true;
                }
            }

        }
        catch (Exception ex1)
        {
            isDiff = false;
            System.err.println(Global.Format(new java.util.Date(), "dd/MM/yyyy HH:mm:ss") + ": ERROR in method isDiff():" + ex1.getMessage());

        }
        finally
        {
            try
            {
                if (zipInStream != null)
                {
                    zipInStream.close();
                }
            }
            catch (IOException ex)
            {
            }
        }
        return isDiff;
    }

    /**
     * Copies one file to another. NEW CODE -- NOT YET TESTED
     */
    public void copyFile(File source, File target) throws IOException
    {
        InputStream in = null;
        OutputStream out = null;
        try
        {
            in = new BufferedInputStream(new FileInputStream(source));
            out = new BufferedOutputStream(new FileOutputStream(target));
            int ch;
            while ((ch = in.read()) != -1)
            {
                out.write(ch);
            }
            out.flush(); // just in case
        }
        finally
        {
            if (out != null)
            {
                try
                {
                    out.close();
                }
                catch (IOException ioe)
                {
                }
            }
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException ioe)
                {
                }
            }
        }
    }

    public void chmod(File file, String mode) throws IOException
    {
        /*
         * ___________________________________ |Number|Letters|Permissions | |0
         * |--- |no permissions | |1 |--x |executable only | |2 |-w- |write only |
         * |3 |-wx |write/execute | |4 |r-- |read only | |5 |r-x |read/execute |
         * |6 |rw- |read/write | |7 |rwx |read/write/execute |
         */
        Runtime.getRuntime().exec(new String[]
            {"chmod", mode, file.getAbsolutePath()});
    }

    public boolean deleteFile(String strSrc)
    {
        File flSrc = new File(strSrc);
        return flSrc.delete();
    }

    public boolean checkFileExist(String strFileName)
    {
        File flTemp = new File(strFileName);
        if (!flTemp.exists())
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public void forceFolderExist(String strFolder)
    {
        File flTemp = new File(strFolder);
        if (!flTemp.exists())
        {
            flTemp.mkdirs();
        }
    }

    public boolean renameFile(String strSrc, String strDest)
    {
        File flSrc = new File(strSrc);
        File flDest = new File(strDest);
        if (flDest.exists())
        {
            flDest.delete();
        }
        return flSrc.renameTo(flDest);
    }

    public long getFileSize(String filename)
    {
        File file = new File(filename);
        if (!file.exists() || !file.isFile())
        {
            return -1;
        }
        return file.length();
    }

}
