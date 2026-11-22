package gpumanagement;

import java.io.*;
import java.util.*;

public class Storage{
	private static final String FILE_NAME = "gpu_data.txt";
	
	private void writeTable(PrintWriter file, String section, String headings, String[][] rows) {
	    file.println("[" + section + "]");
	    String[] headingCells = headings.split("\\s*\\|\\s*");
	    int[] widths = new int[headingCells.length]; //integer array for entries
	    for (int i = 0; i < headingCells.length; i++) {
	        widths[i] = headingCells[i].length();
	    }
	    
	    //Find column widths
	    if (rows != null) {
	        for (int i = 0; i < rows.length; i++) {
	            for (int j = 0; j < headingCells.length; j++) {
	                String value = rows[i][j] == null ? "" : rows[i][j];
	                value = value.replace('\n', ' ').replace('\r', ' ').replace('|', '/'); //clean up values
	                widths[j] = Math.max(widths[j], value.length()); // align like a table
	            }
	        }
	    }
	    //Prints headings
	    for (int j = 0; j < headingCells.length; j++) {
	        file.print(headingCells[j]);
	        for (int space = headingCells[j].length(); space < widths[j]; space++) {
	            file.print(" ");
	        }
	        if (j < headingCells.length - 1) {
	            file.print(" | ");
	        }
	    }
	    file.println();
	    
	    //Prints the rows
	    if (rows == null || rows.length == 0) {
	        file.println("(no records)");
	    } else {
	        for (int i = 0; i < rows.length; i++) {
	            for (int j = 0; j < headingCells.length; j++) {
	                String value = rows[i][j] == null ? "" : rows[i][j];
	                value = value.replace('\n', ' ')
	                             .replace('\r', ' ')
	                             .replace('|', '/');

	                file.print(value);
	                for (int space = value.length(); space < widths[j]; space++) {
	                    file.print(" ");
	                }
	                if (j < headingCells.length - 1) {
	                    file.print(" | ");
	                }
	            }
	            file.println();
	        }
	    }

	    file.println();
	}
	 public void saveInFile(String clockText, String simulationStartText, String[][] accounts, String[][] requests, String[][] allowances,
	            String[][] grants, String[][] jobs, String[][] cancellations, String[][] gpuAssignments) throws Exception {

	        PrintWriter file = new PrintWriter(FILE_NAME); //Create the file
	        file.println("GPU UTILIZATION MANAGEMENT - SAVED DATA");
	        file.println("Clock: " + (clockText == null ? "Not set" : clockText));
	        file.println("Simulation start: " + (simulationStartText == null ? "Not set" : simulationStartText));
	        file.println();

	        writeTable(file, "Accounts", "University ID | Name | Email | Phone | Password", accounts);
	        writeTable(file, "Requests", "University ID | Name | Email | Phone | Password | Status", requests);
	        writeTable(file, "Allowances", "Student ID | Allocated | Used | Reserved", allowances);
	        writeTable(file, "Grants", "Student ID | Minutes | Reason | Administrator", grants);
	        writeTable(file, "Jobs", "Job ID | Student ID | Title | Application | Description | Size | Submitted | Status | GPU | Started | Finished | Cancelled at | Cancelled by | Reason", jobs);
	        writeTable(file, "Cancellations", "Job ID | Student ID | Status before cancellation | Application | Reason | Cancelled by | Cancellation time", cancellations);
	        writeTable(file, "GPU Assignments", "GPU ID | Job ID", gpuAssignments);
	        file.close();
	        System.out.println("Saved data to: " + System.getProperty("user.dir") + "/" + FILE_NAME);
	    }
	
	//Read the file's section
	private String[][] readSection(String name){
		ArrayList<String[]> rows = new ArrayList<>(); //Save rows in each section as a String Array
		try {
			Scanner input = new Scanner(new FileReader(FILE_NAME));
			boolean readingRows = false;
			
			while (input.hasNextLine()) {
				String line = input.nextLine().trim();

                if (!readingRows) {
                    if (line.equals("[" + name + "]")) {
                        readingRows = true;
                        if (input.hasNextLine()) input.nextLine(); 
                    }
                } else {
                    if (line.isEmpty() || line.startsWith("[")) break;
                    if (!line.equals("(no records)")) {
                        rows.add(line.split("\\s*\\|\\s*", -1));
                    }
                }
            }
            input.close();
        } catch (FileNotFoundException ex) {}
        return rows.toArray(new String[rows.size()][]);
    }
	
	private String readTime(String label) {
	    try {
	        Scanner input = new Scanner(new FileReader(FILE_NAME));

	        while (input.hasNextLine()) {
	            String line = input.nextLine().trim();

	            if (line.equals(label)) { //finds the label we're looking for
	                if (input.hasNextLine()) {
	                    String value = input.nextLine().trim();
	                    input.close();
	                    return value.isEmpty() ? "Not set" : value;
	                }
	                break;
	            }

	            if (line.startsWith(label)) {
	                String value = line.substring(label.length()).trim();
	                if (!value.isEmpty()) {
	                    input.close();
	                    return value;
	                }
	            }
	        }

	        input.close();
	    } catch (FileNotFoundException ex) {
	        // File not saved
	    }

	    return "Not set";
	}
	//Reload data when we run again
	public String loadClock() { return readTime("Clock:"); }
    public String loadSimulationStart() { return readTime("Simulation start:"); }
    public String[][] loadAccount() { return readSection("Accounts"); }
    public String[][] loadRequests() { return readSection("Requests"); }
    public String[][] loadAllowance() { return readSection("Allowances"); }
    public String[][] loadAllowanceGrants() { return readSection("Grants"); }
    public String[][] loadJobs() { return readSection("Jobs"); }
    public String[][] loadCancellation() { return readSection("Cancellations"); }
    public String[][] loadGpuAssignments() { return readSection("GPU Assignments"); }
}
