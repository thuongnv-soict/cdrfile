/*
 * Change phone 11 to 10 number
 * Get list head number need to change in alldau11so.txt
 * 
 * author: thuongnv
 * updated: 16/07/2018
 */
package cdrfile.thread;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

public class Reader {
	public String fileName = "C:\\Users\\Thuong\\Desktop\\alldau11so.txt";
	private BufferedReader br = null;
	public static HashMap<String, String> map = new HashMap<String, String>();
	
	/*
	 * Contructor: Reading file
	 * Save all old and new head number to HashMap
	 */
	public Reader() {
		try {
			String currentLine;
			br = new BufferedReader(new FileReader(fileName));
 
			while ((currentLine = br.readLine()) != null) {
				if (currentLine.contains("N")) {
					String[] split = currentLine.split(Pattern.quote("|"), -1);
					System.out.println(split[0]+ "     " + split[1] + "    "+split[2]);
					map.put(split[0], split[1]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/*
	 * Convert 11 to 10
	 */
	public static String convert(String initNumber) {
		String newNumber = "";
		if (initNumber.length() == 11) {	
			String oldHead = initNumber.substring(1, 6);
			String newHead = map.get(oldHead);
			
			if (newHead != null) {
				newNumber = "0" + newHead + initNumber.substring(6);
				
				return newNumber;
			}
				 
		}
		
		return initNumber;
	}
	
	public static void main(String[] args) {
	
		System.out.println(Reader.convert("01206302154"));
		System.out.println(Reader.map.size());
	}
}
