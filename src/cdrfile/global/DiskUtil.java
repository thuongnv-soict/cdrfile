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
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.StringTokenizer;

public class DiskUtil {

	public static long getFreeSpace(String path) throws Exception {
		if (System.getProperty("os.name").startsWith("Windows")) {
			return getFreeSpaceOnWindows(path);
		}
		if (System.getProperty("os.name").startsWith("Linux")) {
			return getFreeSpaceOnLinux(path);
		}
		if (System.getProperty("os.name").startsWith("SunOS")) {
			return getFreeSpaceUnix(path);
		}

		throw new UnsupportedOperationException(
				"The method getFreeSpace(String path) has not been implemented for this operating system.");
	}

	private static long getFreeSpaceOnWindows(String path) throws Exception {
		long bytesFree = -1;

		File script = new File(System.getProperty("java.io.tmpdir"),
				"script.bat");
		PrintWriter writer = new PrintWriter(new FileWriter(script, false));
		writer.println("dir \"" + path + "\"");
		writer.close();

		// get the output from running the .bat file
		Process p = Runtime.getRuntime().exec(script.getAbsolutePath());
		InputStream reader = new BufferedInputStream(p.getInputStream());
		StringBuffer buffer = new StringBuffer();
		for (;;) {
			int c = reader.read();
			if (c == -1) {
				break;
			}
			buffer.append((char) c);
		}
		String outputText = buffer.toString();
		reader.close();

		// parse the output text for the bytes free info
		StringTokenizer tokenizer = new StringTokenizer(outputText, "\n");
		while (tokenizer.hasMoreTokens()) {
			String line = tokenizer.nextToken().trim();
			// see if line contains the bytes free information
			if (line.endsWith("bytes free")) {
				tokenizer = new StringTokenizer(line, " ");
				tokenizer.nextToken();
				tokenizer.nextToken();
				bytesFree = Long.parseLong(tokenizer.nextToken().replaceAll(
						",", ""));
			}
		}
		return bytesFree;
	}

	private static long getFreeSpaceOnLinux(String path) throws Exception {
		long bytesFree = -1;

		Process p = Runtime.getRuntime().exec("df " + path);
		InputStream reader = new BufferedInputStream(p.getInputStream());
		StringBuffer buffer = new StringBuffer();
		for (;;) {
			int c = reader.read();
			if (c == -1) {
				break;
			}
			buffer.append((char) c);
		}
		String outputText = buffer.toString();
		reader.close();

		// parse the output text for the bytes free info
		StringTokenizer tokenizer = new StringTokenizer(outputText, "\n");
		tokenizer.nextToken();
		if (tokenizer.hasMoreTokens()) {
			String line2 = tokenizer.nextToken();
			StringTokenizer tokenizer2 = new StringTokenizer(line2, " ");
			if (tokenizer2.countTokens() >= 4) {
				tokenizer2.nextToken();
				tokenizer2.nextToken();
				tokenizer2.nextToken();
				bytesFree = Long.parseLong(tokenizer2.nextToken());
				return bytesFree;
			}

			return bytesFree;
		}
		throw new Exception("Can not read the free space of " + path + " path");
	}

	private static long getFreeSpaceUnix(String path) throws Exception {
		long bytesFree = -1;

		Process p = Runtime.getRuntime().exec("df -k " + path);
		InputStream reader = new BufferedInputStream(p.getInputStream());
		StringBuffer buffer = new StringBuffer();
		for (;;) {
			int c = reader.read();
			if (c == -1) {
				break;
			}
			buffer.append((char) c);
		}
		String outputText = buffer.toString();
		reader.close();

		// parse the output text for the bytes free info
		StringTokenizer tokenizer = new StringTokenizer(outputText, "\n");
		tokenizer.nextToken();
		if (tokenizer.hasMoreTokens()) {
			String line2 = tokenizer.nextToken();
			StringTokenizer tokenizer2 = new StringTokenizer(line2, " ");
			if (tokenizer2.countTokens() >= 4) {
				tokenizer2.nextToken();
				tokenizer2.nextToken();
				tokenizer2.nextToken();
				bytesFree = Long.parseLong(tokenizer2.nextToken());
				return bytesFree;
			}

			return bytesFree;
		}
		throw new Exception("Can not read the free space of " + path + " path");
	}

	public static void main(String args[]) {
		try {
			System.out.println("Free space of /: " + getFreeSpace("d:")
					/ (1024 * 1024 * 1024));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
