package cdrfile.global;

/**
 * <p>Title: CDR File(s) System</p>
 * <p>Description: VMS IS Departerment</p>
 * <p>Copyright: Copyright (c) by eKnowledge 2005</p>
 * <p>Company: VietNam Mobile Telecom Services</p>
 * @author eKnowledge - Software
 * @version 1.0
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.util.Vector;

public class DelimitedFile {
	protected FileReader mTextFile = null;
	protected BufferedReader mTextBuffer = null;
	public boolean mblnSuppressHeaders = false;
	public boolean mblnIgnoreCheckSize = false;
	public boolean mbExitIfMatchFirstEOF = false;
	protected String mStrDelimited = ";";
	protected String mStrEndOfFile = "";
	protected Vector mArrHeaders = null;
	protected Vector mArrValues = null;
	protected String mStrHeader = "";
	protected String mStrCurrentLine = null;
	protected String mStrLine = null;

	public DelimitedFile() {
	}

	public int getColumnCount() {
		return mArrHeaders.size();
	}

	public int getCurrentLineColumnCount() {
		return mArrValues.size();
	}

	public int findColumn(String strField) {
		for (int intIndex = 0; intIndex < mArrHeaders.size(); intIndex++) {
			if (strField.equalsIgnoreCase((String) mArrHeaders
					.elementAt(intIndex))) {
				return intIndex;
			}
		}
		return -1;
	}

	private void parseLine(String strLine) throws Exception {
		if (!mStrDelimited.equals("")) {
			mArrValues = StringUtils.vectorFromString(strLine, mStrDelimited);
		}
	}

	public void openDelimitedFile(String strPath, String strDelimited,
			int intIgnoreRows, String strEndOfFile, int intBufferSize)
			throws Exception {
		openDelimitedFile(strPath, strDelimited, intIgnoreRows, strEndOfFile,
				intBufferSize, 0);
	}

	public void openDelimitedFile(String strPath, String strDelimited,
			int intIgnoreRows, String strEndOfFile, int intBufferSize,
			long lngNumCharacterSkip) throws Exception {
		try {
			mTextFile = new FileReader(strPath);
			mTextBuffer = new BufferedReader(mTextFile, intBufferSize);
			mTextBuffer.skip(lngNumCharacterSkip);
			mStrCurrentLine = null;
			mStrDelimited = strDelimited;
			mStrEndOfFile = strEndOfFile;
			parseHeader(intIgnoreRows);
		} catch (Exception e) {
			safeCloseDelimitedFile();
			throw e;
		}
	}

	public void openDelimitedFile(String strPath, int intBufferSize)
			throws Exception {
		openDelimitedFile(strPath, ";", 0, "", intBufferSize);
	}

	public void openDelimitedFile(String strPath) throws Exception {
		openDelimitedFile(strPath, ";", 0, "", 1024 * 1024);
	}

	public void closeDelimitedFile() throws Exception {
		try {
			mTextBuffer.close();
			mTextFile.close();
		} catch (Exception e) {
			throw e;
		} finally {
			safeCloseDelimitedFile();
		}
	}

	public void safeCloseDelimitedFile() {
		if (mArrHeaders != null)
			mArrHeaders.clear();
		this.safeClose(mTextBuffer);
		this.safeClose(mTextFile);
	}

	public void safeClose(Reader reader) {
		try {
			if (reader != null) {
				reader.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void parseHeader(int intIgnoreRows) throws Exception {
		if (intIgnoreRows > 0) {
			for (int j = 0; j < intIgnoreRows; j++) {
				mStrLine = mTextBuffer.readLine();
				if (mStrLine == null) {
					break;
				}
			}
		}
		mStrLine = mTextBuffer.readLine();
		while (mStrLine != null && mStrLine.trim().equals(""))
			mStrLine = mTextBuffer.readLine();

		if ((mStrLine != null)
				&& !mStrDelimited.equals("")
				&& (mStrEndOfFile.equals("") || !mStrLine
						.startsWith(mStrEndOfFile))) {
			mStrHeader = Global.nvl(mStrLine, "");
			mArrHeaders = StringUtils.vectorFromString(mStrHeader,
					mStrDelimited);
			if (mblnSuppressHeaders) {
				for (int intIndex = 0; intIndex < mArrHeaders.size(); intIndex++) {
					mArrHeaders.setElementAt("COLUMN" + intIndex, intIndex);
				}
			} else {
				mStrLine = mTextBuffer.readLine();
			}
			for (int intIndex = 0; intIndex < mArrHeaders.size(); intIndex++) {
				mArrHeaders.setElementAt(mArrHeaders.elementAt(intIndex)
						.toString().toUpperCase(), intIndex);
			}
		}
	}

	public void parseValues() throws Exception {
		parseLine(mStrLine);
		if ((mArrHeaders != null) && (mArrValues != null)) {
			if (!mblnIgnoreCheckSize
					&& (mArrValues.size() != mArrHeaders.size())) {
				throw new Exception("Number of columns does not match header");
			}
		}
	}

	public boolean first() throws Exception {
		throw new Exception("This method does not supported");
	}

	public boolean last() throws Exception {
		throw new Exception("This method does not supported");
	}

	public boolean prev() throws Exception {
		throw new Exception("This method does not supported");
	}

	public boolean next() throws Exception {
		if ((mStrLine == null)
				|| (!mStrEndOfFile.equals("") && mStrLine
						.startsWith(mStrEndOfFile))
				|| (mbExitIfMatchFirstEOF && mStrLine.equals(mStrEndOfFile))) {
			return false;
		} else {
			if (!mStrLine.trim().equals("")) {
				parseValues();
				mStrCurrentLine = mStrLine;
				mStrLine = mTextBuffer.readLine();
				return true;
			} else {
				mStrLine = mTextBuffer.readLine();
				return next();
			}
		}
	}

	public String getString(int intIndex) {
		return Global.nvl(mArrValues.elementAt(intIndex).toString(), "");
	}

	public String getString(String strField) {
		return getString(findColumn(strField));
	}

	public String getLine() {
		return mStrCurrentLine;
	}
}
