package cdrfile.global;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

public class TextFile {
	public FileOutputStream mFile = null;
	public BufferedOutputStream mBuffer = null;
	public int mintCount = 0;
	public String mstrFilePath = new String();
	IOUtils IOUtil = new IOUtils();

	public TextFile() {
	}

	public void openFile(String strFilePath, int intBuffer) throws Exception {
		mintCount = 0;
		mstrFilePath = strFilePath;
		try {
			mFile = new FileOutputStream(strFilePath);
			mBuffer = new BufferedOutputStream(mFile, intBuffer); // 5M
		} catch (Exception e) {
			throw e;
		}
	}

	public void closeFile() throws Exception {
		mintCount = 0;
		try {
			mBuffer.flush();
			mBuffer.close();
			mFile.close();
		} catch (Exception e) {
			throw e;
		}
	}

	public void clear() throws Exception {
		if ((mstrFilePath != null) && !mstrFilePath.equals("")) {
			try {
				closeFile();
			} catch (Exception e) {
				throw e;
			} finally {
				IOUtil.deleteFile(mstrFilePath);
			}
		}
	}

	public void addText(String strText) throws Exception {
		mintCount++;
		strText += '\n';
		try {
			mBuffer.write(strText.getBytes());
		} catch (Exception e) {
			throw e;
		}
	}

	public long getCount() {
		return mintCount;
	}
}
