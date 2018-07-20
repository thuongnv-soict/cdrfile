package cdrfile.thread;

import java.io.File;
import java.util.List;

import cdrfile.thread.ConvertThread;

public class ConvertThreadTool implements Runnable {
	
	private static final String CDR_INPUT_PATH = "C:/Users/datnh/Desktop/Data/CDRFILE/GGSN/";
	private static final String CDR_OUTPUT_PATH = "C:/Users/datnh/Desktop/Data/CDRFILE/GGSN_OUT/";
	
	private List mRs;

	public ConvertThreadTool(List c) {
		mRs = c;
	}

	private synchronized String getElement() {
		String file_name = null;
		if (!mRs.isEmpty()) {
			file_name = (String) mRs.remove(mRs.size() - 1);
		}
		return file_name;
	}

	private synchronized boolean ListEmpty() {
		return (mRs.isEmpty());
	}

	public void convert() {
		try {
			String file_name = "";
			file_name = getElement();
			
			if (file_name != null) {
				System.out.println(" threadName " + Thread.currentThread().getName() + " started");
	
				ConvertThread convertThread = new ConvertThread();
				convertThread.openConnection();
				
				// Duong dan file bin.
				convertThread.convertFiles(CDR_INPUT_PATH, file_name, CDR_OUTPUT_PATH);
				
				System.out.print(" threadName " + Thread.currentThread().getName()
						+ ":-----" + file_name + " end");
				
				System.out.println();
	
				convertThread.closeConnection();
			}
		} catch (Exception e) {
			System.out.println(" threadName :" + e.toString());
		}
	}
	
	public void convertGGSN() {
		try {
			String file_name = "";
			file_name = getElement();
			
			if (file_name != null) {
				System.out.println("ThreadName " + Thread.currentThread().getName() + " started");
	
				ConvertGGSNThread convertThread = new ConvertGGSNThread();
				
				// Duong dan file bin.
				convertThread.convertFiles(CDR_INPUT_PATH, file_name, CDR_OUTPUT_PATH);
				
				System.out.println(" threadName " + Thread.currentThread().getName()
						+ ":-----" + file_name + " end");
	
				convertThread.closeConnection();
			}
		} catch (Exception e) {
			System.out.println(" threadName :" + e.toString());
		}
	}

	public void run() {
		while (true) {
			//convert();
			convertGGSN();
			if (ListEmpty()) {
				System.out.println("Finish");
				return;
			}

		}
	}

	public static void main(String[] args) throws Exception {
		// Duong dan file bin.
		File file = new File(CDR_INPUT_PATH);
		java.util.List list = new java.util.ArrayList();
		String[] fileNames = null;
		if (file.isDirectory()) {
			fileNames = file.list();
		}
		for (int i = 0; i < fileNames.length; i++) {
			list.add(fileNames[i]);
		}

		ConvertThreadTool c = new ConvertThreadTool(list);
		Thread thread1 = new Thread(c);
		thread1.setName("thread1");
		Thread thread2 = new Thread(c);
		thread2.setName("thread2");
		// Thread thread3 = new Thread(c);
		// thread3.setName("thread3");
		// Thread thread4 = new Thread(c);
		// thread4.setName("thread4");
		// Thread thread5 = new Thread(c);
		// thread5.setName("thread5");
		thread1.start();
		thread2.start();
		// thread3.start();
		// thread4.start();
		// thread5.start();

	}

}
