package gpumanagement;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class AdminFrame extends MainFrame implements ActionListener{
	private JLabel clock, running, queued, completed, cancelled, utilization;
	private JLabel[][] gpusTable = new JLabel[4][5];
	private JButton setTime, advance, allowance, search, requests, quedB, cancelB, completedB, canceledB, save, logoutB,backB;
	
	private JSpinner startDate, startTime;
	private JButton setStartB;
	// private Scheduler scheduler (member 2)
	
	private JTextField studentIdField, minutesField, reasonField;
	private JButton findStudentB, grantTimeB;
	private JLabel student, jobs, balance, available;
	private String selectedStudentId;
	
	private Student selectedStudent;
	private JTextField[] allowanceFields;
	private JButton[] approveButtons, rejectButtons;
	private String[][] pendingRequests = new String[0][5]; //get from member 1
	
	private JButton findCancelJobB;
	private JButton[] cancelButtons;
	private JTextField cancelStudentIdField;
	private String[][] cancellableJobRows = new String[0][4];
	private String[][] cancelledHistory = new String[0][5];
	
	private Storage fileStorage;
	
	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"); // format date
	private String fmt(LocalDateTime t) {return t == null ? "--" : t.format(FMT);}
	
	private String clockText() {
	    if (scheduler == null) return "Not set";
	    return fmt(scheduler.getSimulationTime());
	}
		
	public AdminFrame() { 
		super();
		//this.scheduler = scheduler;
		setTitle("Administration");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        adminPage();
        setVisible(true);
	}

	public void adminPage() {
		clearScreen(0, 5);
		
		String schedulerClock = clockText();
		clock = new JLabel("Simulation time: " + schedulerClock, JLabel.CENTER);
		
		String schedulerRunning = "";		// Scheduler.getRunning()
		running = new JLabel("Running: " + schedulerRunning, JLabel.CENTER);
		
		String schedulerQued = "" + (scheduler == null ? 0 : scheduler.getWaitingCount()); 
		queued = new JLabel("Queued: " + schedulerQued, JLabel.CENTER);
		
		String schedulerComplete = "";		// Scheduler.getComplete()
		completed =  new JLabel("Completed: " + schedulerComplete, JLabel.CENTER);
		
		String schedulerCancelled = "";		// Scheduler.getCancelled()
		cancelled = new JLabel("Cancelled: " + schedulerCancelled, JLabel.CENTER);
		
		String schedulerUtilization = "";	//Scheduler.getUtilization()
		utilization = new JLabel("Utilization: " + schedulerUtilization, JLabel.CENTER);
		
		setTime = makeButton("Set simulation time");
        advance = makeButton("Advance: +1 min");
        allowance = makeButton("Student Allowance");
        search = makeButton("Search Students");
        requests = makeButton("Requests");
        quedB = makeButton("Queued jobs");
        completedB = makeButton("Completed jobs");
        cancelB = makeButton("Cancel jobs");
        canceledB = makeButton("Cancelled Job");
        save = makeButton("Save");
        logoutB = makeButton("Log out");
        
        add(makeLabel("ADMIN", Color.BLACK, Color.WHITE));
        add(clock);
        add(setTime);
        add(advance);
        add(allowance);
        
        add(running);
        add(queued);
        add(completed);
        add(cancelled);
        add(utilization);
        
        add(makeLabel("GPUs", Color.PINK, Color.BLACK));
        add(makeLabel("Availability", Color.PINK, Color.BLACK));
        add(makeLabel("Current job", Color.PINK, Color.BLACK));
        add(makeLabel("Time Remaining", Color.PINK, Color.BLACK));
        add(makeLabel("Finish (simulated)", Color.PINK, Color.BLACK));
        
        setTime.setEnabled(scheduler == null);
        advance.setEnabled(scheduler != null); 

        // temporary, will replace with Scheduler.getGpuInfo();
        String[][] gpuInfo = {
        		{"GPU 1", "Available", "None", "0 min", "--"},
                {"GPU 2", "Available", "None", "0 min", "--"},
                {"GPU 3", "Available", "None", "0 min", "--"},
                {"GPU 4", "Available", "None", "0 min", "--"}
            }; 
		
        for (int i = 0; i<4; i++) {
			for(int j = 0; j < 5; j++) {
				if(j==0) {gpusTable[i][j]= makeLabel(gpuInfo[i][j], Color.PINK, Color.BLACK);}
				else {gpusTable[i][j]= new JLabel(gpuInfo[i][j], JLabel.CENTER); }
				add(gpusTable[i][j]);
			}
		}
        
        add(search);
        add(requests);
        add(quedB);
        add(completedB);
        add(cancelB);
        
        add(canceledB);
        add(new JLabel(""));
        add(new JLabel(""));
        add(save);
        add(logoutB);
        
        setTime.addActionListener(this);
        advance.addActionListener(this);
        allowance.addActionListener(this);
        search.addActionListener(this);
        requests.addActionListener(this);
        quedB.addActionListener(this);
        cancelB.addActionListener(this);
        completedB.addActionListener(this);
        canceledB.addActionListener(this);
        save.addActionListener(this);
        logoutB.addActionListener(this);
        
        refreshScreen();
	}
	
	public void setTimePage() {
		clearScreen(4, 2);
	    
		startDate = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
	    startDate.setEditor(new JSpinner.DateEditor(startDate, "dd/MM/yyyy"));	    
	    startTime = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE));
	    startTime.setEditor(new JSpinner.DateEditor(startTime, "HH:mm"));
	    
	    setStartB = makeButton("Set Start Time");
	    backB = makeButton("Back to dashboard");
	    
	    add(makeLabel("Set simulation time", Color.BLACK, Color.WHITE));
	    add(new JLabel("Input date and time:"));

	    add(new JLabel("Start date:"));
	    add(startDate);

	    add(new JLabel("Start time:"));
	    add(startTime);

	    add(backB);
	    add(setStartB);

	    setStartB.addActionListener(this);
	    backB.addActionListener(this);

	    refreshScreen();
	};

	public void searchStudent(){
		clearScreen(0, 4);

	    add(makeLabel("SEARCH STUDENT", Color.BLACK, Color.WHITE));
	    add(new JLabel(""));
	    add(new JLabel(""));
	    backB = makeButton("Back to dashboard");
	    add(backB);

	    add(new JLabel("University ID:"));
	    studentIdField = new JTextField();
	    add(studentIdField);
	    findStudentB = makeButton("Search");
	    add(findStudentB);
	    add(new JLabel(""));
	    
	    add(makeLabel("Student", Color.PINK, Color.BLACK));
	    add(makeLabel("Jobs", Color.PINK, Color.BLACK));
	    add(makeLabel("Balance", Color.PINK, Color.BLACK));
	    add(makeLabel("Available time", Color.PINK, Color.BLACK));
	    
	    // Replaced later
	    student = new JLabel("", JLabel.CENTER);
	    jobs = new JLabel("", JLabel.CENTER);
	    balance = new JLabel("", JLabel.CENTER);
	    available = new JLabel("", JLabel.CENTER);

	    add(student);
	    add(jobs);
	    add(balance);
	    add(available);
	    //
	    
	    add(new JLabel("Minutes to add:"));
	    minutesField = new JTextField();
	    add(minutesField);
	    add(new JLabel("Reason:"));
	    reasonField = new JTextField();
	    add(reasonField);

	    add(new JLabel(""));
	    grantTimeB = makeButton("Grant GPU minutes");
	    selectedStudentId = null;
	    selectedStudent = null;
	    grantTimeB.setEnabled(false);
	    add(grantTimeB);
	    add(new JLabel(""));
	    add(new JLabel(""));

	    findStudentB.addActionListener(this);
	    grantTimeB.addActionListener(this);
	    backB.addActionListener(this);

	    refreshScreen();
	};

	public void regRequests(){
		List<AccessRequest> list = accountService.getPendingRequests();
	    pendingRequests = new String[list.size()][5];
	    for (int i = 0; i < list.size(); i++) {
	        AccessRequest req = list.get(i);
	        pendingRequests[i][0] = req.getUniversityId();
	        pendingRequests[i][1] = req.getName();
	        pendingRequests[i][2] = req.getEmail();
	        pendingRequests[i][3] = req.getPhone();
	        pendingRequests[i][4] = req.getPasswordForAccountCreation();
	    }
	    allowanceFields = new JTextField[pendingRequests.length];
	    approveButtons = new JButton[pendingRequests.length];
	    rejectButtons = new JButton[pendingRequests.length];
	    
	    clearScreen(0, 8);
	    
	    add(makeLabel("REQUESTS", Color.BLACK, Color.WHITE));
	    add(new JLabel("Pending: " + pendingRequests.length, JLabel.CENTER));
	    for (int i = 0; i < 5; i++) add(new JLabel(""));
	    backB = makeButton("Back to dashboard");
	    add(backB);
	    backB.addActionListener(this);
		
	    String[] headings= {"University ID", "Name", "Email", "Phone", "Password",
                "Allowance (min)", "Approve", "Reject"};
	    for (int i = 0; i < headings.length; i++) {
	        add(makeLabel(headings[i], Color.PINK, Color.BLACK));
	    }
	    for (int i = 0; i < pendingRequests.length; i++) {
	        for (int j = 0; j < 5; j++) {
	            add(new JLabel(pendingRequests[i][j], JLabel.CENTER));
	        }
	        allowanceFields[i] = new JTextField("100"); //default value
	        approveButtons[i] = makeButton("Approve");
	        rejectButtons[i] = makeButton("Reject");
	        approveButtons[i].addActionListener(this);
	        rejectButtons[i].addActionListener(this);
	        add(allowanceFields[i]);
	        add(approveButtons[i]);
	        add(rejectButtons[i]);
	    }
		refreshScreen();
	};
	
	private void decision(int i, boolean approved) {
		String req = pendingRequests[i][0];
		try {
			if (approved) {
				int minutes = Integer.parseInt(allowanceFields[i].getText().trim());
				
				Student s = accountService.approveRequest(req, minutes); //replace with accountService.approveRequest(req, minutes)
				approvedStudents.add(s);
				JOptionPane.showMessageDialog(this, req + " approved with " + minutes + " minutes.");
			} 
			else {
				accountService.rejectRequest(req);
				JOptionPane.showMessageDialog(this, req + " rejected.");
				}	
			}catch (IllegalArgumentException | IllegalStateException ex) {
		        JOptionPane.showMessageDialog(this, ex.getMessage());
		    }
		    regRequests();
	}
	
	private void cancelRow(int i) {
	    String student = cancellableJobRows[i][0];
	    String jobId = cancellableJobRows[i][1];

	    String reason = JOptionPane.showInputDialog(this, "Reason for cancelling job " + jobId + ":");
	    if (reason == null) return;
	    
	    reason = reason.trim();
	    if (reason.isEmpty()) {
	        JOptionPane.showMessageDialog(this, "A reason is required.");
	        return;
	    }

	    //Add member 2 work
	}
	
	private void showTable(String title, String[] headings, String[][] rows, String empty) { //Organize Data
	    int cols = headings.length;
	    clearScreen(0, cols);
	    add(makeLabel(title, Color.BLACK, Color.WHITE));
	    for (int i = 0; i < cols - 2; i++) add(new JLabel(""));
	    backB = makeButton("Back to dashboard");
	    add(backB);

	    for (int i = 0; i < cols; i++) {
	        add(makeLabel(headings[i], Color.PINK, Color.BLACK));
	    }

	    if (rows == null || rows.length == 0) {
	        add(new JLabel(empty, JLabel.CENTER));
	        for (int i = 1; i < cols; i++) add(new JLabel(""));
	    } 
	    else {
	        for (int i = 0; i < rows.length; i++) {
	            for (int j = 0; j < cols; j++) {
	                add(new JLabel(rows[i][j], JLabel.CENTER));
	            }
	        }
	    }
	    backB.addActionListener(this);
	    refreshScreen();
	}

	public void studentAllowance(String[][] rows) {
	    showTable("STUDENT ALLOWANCE", new String[]{"Student", "Allocated", "Used",
	        "Reserved", "Available"}, rows, "No students yet");
	}
	public void queueddJobs(String[][] rows) {      // 8 columns
	    showTable("QUEUED JOBS", new String[]{"Job", "Student", "Application", "Duration",
	        "Position", "Predicted start", "Predicted finish", "Wait"}, rows, "No queued jobs.");
	}

	public void completeJobs(String[][] rows) {     // 9 columns
	    showTable("COMPLETED JOBS", new String[]{"Job", "Student", "GPU", "Submitted", "Started",
	        "Finished", "Runtime", "Queue wait", "Charged"}, rows, "No completed jobs.");
	}

	public void cancelledJobs(String[][] rows) {    // 5 columns
	    showTable("CANCELLED JOBS", new String[]{"Job", "Student", "Status when cancelled",
	        "Application", "Reason"}, rows, "No cancelled jobs.");
	}

	public void cancelJobs(String studentId, String[][] rows) { 
	    cancellableJobRows = (rows == null) ? new String[0][4] : rows;
	    cancelButtons = new JButton[cancellableJobRows.length];
	    
	    clearScreen(0, 5);
	    
	    add(makeLabel("CANCEL JOBS", Color.BLACK, Color.WHITE));
	    add(new JLabel(""));
	    add(new JLabel(""));
	    add(new JLabel(""));
	    backB = makeButton("Back to dashboard");
	    add(backB);

	    add(new JLabel("Student university ID:"));
	    cancelStudentIdField = new JTextField(studentId);
	    add(cancelStudentIdField);
	    findCancelJobB = makeButton("Find jobs");
	    add(findCancelJobB);
	    add(new JLabel(""));
	    add(new JLabel(""));

	    add(makeLabel("Student", Color.PINK, Color.BLACK));
	    add(makeLabel("Job", Color.PINK, Color.BLACK));
	    add(makeLabel("Application", Color.PINK, Color.BLACK));
	    add(makeLabel("Status", Color.PINK, Color.BLACK));
	    add(new JLabel(""));

	    if (cancellableJobRows.length == 0) {
	        add(new JLabel("No queued or running jobs", JLabel.CENTER));
	        for (int i = 0; i < 4; i++) { 
	        	add(new JLabel(""));}
	    } else {
	        for (int i = 0; i < cancellableJobRows.length; i++) {
	            for (int j = 0; j < 4; j++) {
	                add(new JLabel(cancellableJobRows[i][j], JLabel.CENTER));
	            }
	            cancelButtons[i] = makeButton("Cancel job");
	            cancelButtons[i].addActionListener(this);
	            add(cancelButtons[i]);
	        }
	    }

	    backB.addActionListener(this);
	    findCancelJobB.addActionListener(this);
	    refreshScreen();
	}
	
	public void cancelHistroy(String job, String student, String status, String application, String reason) {
		 String[][] updatedHistory =  new String[cancelledHistory.length + 1][5];
		 for (int i = 0; i < cancelledHistory.length; i++) {
		        updatedHistory[i] = cancelledHistory[i];
		  }
		 int lastRow = updatedHistory.length - 1;
		 updatedHistory[lastRow][0] = job;
		 updatedHistory[lastRow][1] = student;
		 updatedHistory[lastRow][2] = status;
		 updatedHistory[lastRow][3] = application;
		 updatedHistory[lastRow][4] = reason;
		 cancelledHistory = updatedHistory;
	}
	
	public void storage() {
	    try {
	        Storage fileStorage = new Storage();

	        fileStorage.saveInFile(clock.getText(), null, null, null, null, null, null);
	        JOptionPane.showMessageDialog(this, "Data saved.");
	    } catch (Exception ex) {
	    	JOptionPane.showMessageDialog(this, "Could not save data: " + ex.getMessage());
	    }
	}
	
	// will add more to these two after member 1's work
	private List<Student> allStudents() {
	    return approvedStudents;
	}
	private Student findStudentById(String id) {
	    for (Student s : allStudents()) {
	        if (s.getUniversityId().equals(id)) return s;
	    }
	    return null;
	}
	@Override
	public void actionPerformed(ActionEvent e) {	
		
		if (approveButtons != null) {
	        for (int i = 0; i < approveButtons.length; i++) {
	        	if (e.getSource() == approveButtons[i]) { decision(i, true);  return; }
	        	if (e.getSource() == rejectButtons[i])  { decision(i, false); return; }
	        }
	    }
		if (cancelButtons != null) {
		    for (int i = 0; i < cancelButtons.length; i++) {
		        if (e.getSource() == cancelButtons[i]) { cancelRow(i); return; }
		    }
		}
	    if (e.getSource() == logoutB) {
	        new MainFrame();
	        dispose();
	    }
		
	    else if (e.getSource() == backB) {
		    adminPage();
		}
	    else if (e.getSource() == setTime) { 
			if (true) { //change inside later
				setTimePage();
				} 
		}
		else if (e.getSource() == setStartB) { 
		    try {
		        startDate.commitEdit();
		        startTime.commitEdit();
		        
		        Calendar selectedDate = Calendar.getInstance();
		        selectedDate.setTime((Date) startDate.getValue());

		        Calendar selectedTime = Calendar.getInstance();
		        selectedTime.setTime((Date) startTime.getValue());

		        selectedDate.set(Calendar.HOUR_OF_DAY,selectedTime.get(Calendar.HOUR_OF_DAY));
		        selectedDate.set(Calendar.MINUTE, selectedTime.get(Calendar.MINUTE));
		        selectedDate.set(Calendar.SECOND, 0);
		        selectedDate.set(Calendar.MILLISECOND, 0);

		        if (scheduler != null) {
		            JOptionPane.showMessageDialog(this, "The start time is already set.");
		            return;
		        }
		       
		        scheduler = new Scheduler(LocalDateTime.ofInstant(
		                selectedDate.getTime().toInstant(), ZoneId.systemDefault()));
		        adminPage();

		    } catch (ParseException ex) {
		        JOptionPane.showMessageDialog(this, "Enter a valid date and time.");
		    }

		} 

		else if (e.getSource() == allowance){
			List<Student> list = allStudents();
		    String[][] rows = new String[list.size()][5];
		    for (int i = 0; i < list.size(); i++) {
		        Student s = list.get(i);
		        TimeAllowance a = s.getAllowance();
		        rows[i][0] = s.getUniversityId() + " " + s.getName();
		        rows[i][1] = "" + a.getAllocatedMinutes();
		        rows[i][2] = "" + a.getUsedMinutes();
		        rows[i][3] = "" + a.getReservedMinutes();
		        rows[i][4] = "" + a.getAvailableMinutes();
		    }
		    studentAllowance(rows);
		} 
		
		else if (e.getSource() == search) {
			searchStudent();
		}
		
		else if (e.getSource() == findStudentB) {
			String studentId = studentIdField.getText().trim();
			selectedStudent = findStudentById(studentId);
			
			if (selectedStudent == null) {  // no student yet
				selectedStudentId = null;
				student.setText("Student not found");
		        jobs.setText("--");
		        balance.setText("--");
		        available.setText("--");
		        grantTimeB.setEnabled(false);
		        }
			else { 
				selectedStudentId = selectedStudent.getUniversityId();
				TimeAllowance a = selectedStudent.getAllowance();
				student.setText(selectedStudent.getName() + " (" + selectedStudentId + ")");
				jobs.setText("0");   // Member 2: "" + scheduler.getJobsForStudent(selectedStudentId).size()
				balance.setText(a.getAllocatedMinutes() + " min allocated");
				available.setText(a.getAvailableMinutes() + " min");
				grantTimeB.setEnabled(true);
			    }
			}			
		else if (e.getSource() == grantTimeB) {
			String minutesText = minutesField.getText().trim();
		    String reason = reasonField.getText().trim();
		    if (selectedStudentId == null || selectedStudent == null) {
		        JOptionPane.showMessageDialog(this, "Search for a student first.");
		    } else if (minutesText.isEmpty() || reason.isEmpty()) {
		        JOptionPane.showMessageDialog(this, "Enter the minutes and a reason.");
		    } else {
		        try {
		            int minutes = Integer.parseInt(minutesText);
		            if (minutes <= 0) {
		                JOptionPane.showMessageDialog(this, "Minutes must be greater than 0.");
		            } else {
		                // when Member 1 adds grantExtraTime:
		                // accountService.grantExtraTime(selectedStudentId, minutes, reason);
		                // TimeAllowance a = selectedStudent.getAllowance();
		                // balance.setText(a.getAllocatedMinutes() + " min allocated");
		                // available.setText(a.getAvailableMinutes() + " min");
		                JOptionPane.showMessageDialog(this, "Not connected yet: waiting for Member 1.");
		            }
		        } catch (NumberFormatException ex) {
		            JOptionPane.showMessageDialog(this, "Minutes must be a whole number.");
		        }
		    }
		}
		else if (e.getSource() == requests) {
		    regRequests();
		}

		else if (e.getSource() == quedB) {
			queueddJobs(new String[0][8]); // Replace with queued-job data.
		} 
		else if (e.getSource() == completedB) {
		    completeJobs(new String[0][9]); // Replace with completed-job data.
		} 
		else if (e.getSource() == canceledB) {
		    cancelledJobs(cancelledHistory); // Replace with cancellation history.
		} 
		else if (e.getSource() == cancelB) {
		    cancelJobs("", new String[0][4]);
		} 
		else if (e.getSource() == findCancelJobB) {
		    String id = cancelStudentIdField.getText().trim();
		    //  String[][] rows = scheduler.getCancellableJobsForStudent(id);
		    cancelJobs(id, new String[0][4]); //replace with rows from member 2
		}
		else if (e.getSource() == save) {
			storage();
		}
		else if (e.getSource() == advance) {
			scheduler.advanceTime(1);
		    adminPage();
		}
	}
}
