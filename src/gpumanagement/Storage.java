package gpumanagement;

import java.io.*;
import java.util.*;

public class Storage{
	
	private static final String FILE_NAME = "gpu_data.txt";
	private void writeTable(PrintWriter file, String name, String[][] accounts){
		file.println(name);
		int nrows = 0;
		int ncolumns = 0;
		if (accounts != null) {
			nrows = accounts.length;
			if(nrows>0) {
				ncolumns = accounts[0].length;
			}
		}
		
		file.println(nrows);
		file.println(ncolumns);
		
		for(int i=0;i<nrows;i++) {
			for(int j=0;j<ncolumns;j++) {
				String value = accounts[i][j];
				if(value == null) {
					value = "";
				}
				
				value = value.replace('\n', ' ').replace('\r', ' ');
				file.println(value);
				if (j < ncolumns - 1) {
		            file.print("|");
	            }
			}
			file.println();
		}
	}
	public void saveInFile(String clockText,String[][] accounts, String[][] requests, String[][] allowances,
            String[][] jobs, String[][] cancellations, String[][] gpuAssignments) throws Exception{
		
		PrintWriter file = new PrintWriter(FILE_NAME);
		file.println("Clock:");
		file.println(clockText == null? "Clock not set" : clockText); 
		writeTable(file, "Accounts:", accounts);
		writeTable(file, "Requests:", requests);
		writeTable(file, "Allowances:", allowances);
		writeTable(file, "Jobs:", jobs);
		writeTable(file, "Cancellations:", cancellations);
		writeTable(file, "GPU Assignments:", gpuAssignments);
		
		file.close();
		System.out.println(
			    "Saving to: " + System.getProperty("user.dir") + "/" + FILE_NAME
			);
	}
	
	private String[][] readSection(String name){
		try {
			Scanner input = new Scanner(new FileReader(FILE_NAME));
			while (input.hasNextLine()) {
				String line = input.nextLine();
				if(line.equals(name)) {
					int nrows = Integer.parseInt(input.nextLine());
					int ncols = Integer.parseInt(input.nextLine());
				
					String[][] accounts = new String[nrows][ncols];
				
					for (int i = 0; i < nrows; i++) {
					    String[] values = input.nextLine().split("\\|", -1);

					    for (int j = 0; j < ncols; j++) {
					        accounts[i][j] = values[j];
					    }
					}
					input.close();
					return accounts;
				}
			}
			
			input.close();
			return new String[0][0];
		} catch (FileNotFoundException e) {
			return new String[0][0];
		}
	}
	
	public String[][] loadAccount()throws FileNotFoundException{return readSection("Accounts:");}
	public String[][] loadRequests()throws FileNotFoundException{return readSection("Requests:");}
	public String[][] loadAllowance()throws FileNotFoundException{return readSection("Allowance:");}
	public String[][] loadJobs()throws FileNotFoundException{return readSection("Jobs:");}
	public String[][] loadCancellation()throws FileNotFoundException{return readSection("Cancellation:");}
	public String[][] loadGpuAssignments()throws FileNotFoundException{return readSection("GPU Assignments:");}
	
	public String loadClock()throws FileNotFoundException{
		try {
			Scanner input = new Scanner(new FileReader(FILE_NAME));
			String result = "Not set";
			while (input.hasNextLine()) {
				if(input.nextLine().equals("Clock:")) {
					if(input.hasNextLine()) {
						result = input.nextLine();
					}
					break;
				}
			}
			input.close();
			return result;
		}catch(FileNotFoundException e) {
			return "Not Set";
		}
	}
}
