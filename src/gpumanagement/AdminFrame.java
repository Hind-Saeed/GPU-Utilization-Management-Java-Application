package gpumanagement;

import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;

public class AdminFrame extends MainFrame implements ActionListener{
// Attributes
	// admin attributes
	private JLabel clock, running, queued, completed, cancelled, utilization;
	private JLabel[][] gpusTable = new JLabel[4][5];
	private JButton setTime, advance, allowance, search, requests, quedB, cancelB, completedB, canceledB, save, logoutB,backB;
	
	//Set stimulation time attributes
	private JSpinner startDate, startTime;
	private JButton setStartB;
	// private Scheduler scheduler (member 2)
	
	//Search student attributes
	private JTextField studentIdField, minutesField, reasonField;
	private JButton findStudentB, grantTimeB;
	private JLabel student, jobs, balance, available;
	private String selectedStudentId;
	
	//Request attributes
	private JTextField[] approvalField;
	private String[][] pendingRequests = new String[0][5]; //get from member 1
	
	//Cancellation attributes
	private JButton findCancelJobB , confirmCancelB;
	private JTextField cancelStudentIdField, cancelJobIdField, cancelReasonField;
	private String[][] cancellableJobRows = new String[0][3];
	private String[][] cancelledHistory = new String[0][5];
	
	//storage
	private Storage fileStorage;

//Default Constructor
	public AdminFrame() { //put Scheduler object inside
		super();
		//this.scheduler = scheduler;
		setTitle("Administration");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        adminPage();
        setVisible(true);
	}
	
//Main administrator page - called when login
	public void adminPage() {
		clearScreen(0, 5);
		
		//Clock called from scheduler
		// boolean clockSet; // = Scheduler.isClockSet();
		String schedulerClock = ""; // Scheduler.getClockText()
		clock = new JLabel("Simulation time: " + schedulerClock, JLabel.CENTER);
		
		String schedulerRunning = "";		// Scheduler.getRunning()
		running = new JLabel("Running: " + schedulerRunning, JLabel.CENTER);
		
		String schedulerQued = "";  		// Scheduler.getQued()
		queued = new JLabel("Queued: " + schedulerQued, JLabel.CENTER);
		
		String schedulerComplete = "";		// Scheduler.getComplete()
		completed =  new JLabel("Completed: " + schedulerComplete, JLabel.CENTER);
		
		String schedulerCancelled = "";		// Scheduler.getCancelled()
		cancelled = new JLabel("Cancelled: " + schedulerCancelled, JLabel.CENTER);
		
		String schedulerUtilization = "";	//Scheduler.getUtilization()
		utilization = new JLabel("Utilization: " + schedulerUtilization, JLabel.CENTER);
		
		// Buttons
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
        logoutB = makeButton("Logoout");
        
        // Screen set up
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
        
        // GPU Table
        add(makeLabel("GPUs", Color.PINK, Color.BLACK));
        add(makeLabel("Availability", Color.PINK, Color.BLACK));
        add(makeLabel("Current job", Color.PINK, Color.BLACK));
        add(makeLabel("Time Remaining", Color.PINK, Color.BLACK));
        add(makeLabel("Finish (simulated)", Color.PINK, Color.BLACK));
        
        setTime.setEnabled(true); // if scheduler.setTime is not set
        advance.setEnabled(false); 

        // temporary, will replace with Scheduler.getGpuInfo();
        String[][] gpuInfo = {
        		{"GPU 1", "Available", "None", "0 min", "--"},
                {"GPU 2", "Available", "None", "0 min", "--"},
                {"GPU 3", "Available", "None", "0 min", "--"},
                {"GPU 4", "Available", "None", "0 min", "--"}
            }; 
		
        // create tabular format
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
	
// Set simulation time	
	public void setTimePage() {
		clearScreen(4, 2);
		// date input
	    startDate = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
	    startDate.setEditor(new JSpinner.DateEditor(startDate, "dd/MM/yyyy"));

	    // time input
	    startTime = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE));
	    startTime.setEditor(new JSpinner.DateEditor(startTime, "HH:mm"));
	    
	    
	    // buttons
	    setStartB = makeButton("Set Start Time");
	    logoutB = makeButton("Back to dashboard");
	    
	    // page
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

//Shows student allowance
	public void studentAllowance(String[][] jobs){
		clearScreen(0, 5);
		add((makeLabel("STUDENT ALLOWANCE", Color.BLACK, Color.WHITE)));
		add(new JLabel(""));
		add(new JLabel(""));
		add(new JLabel(""));
		backB = makeButton("Back to dashboard");
		add(backB);
		
		add(makeLabel("Student", Color.PINK,Color.BLACK));
		add(makeLabel("Allocated", Color.PINK,Color.BLACK));
		add(makeLabel("Used", Color.PINK,Color.BLACK));
		add(makeLabel("Reserved", Color.PINK,Color.BLACK));
		add(makeLabel("Available", Color.PINK,Color.BLACK));
		
		//loop to get all students
		if(jobs.length > 0){for(int i=0; i<jobs.length; i++) {
			for(int j=0 ; j<5; j++) { add(new JLabel(jobs[i][j],JLabel.CENTER));}
			}
		}
		else {
			 add(new JLabel("No students yet", JLabel.CENTER));
		}
		
		backB.addActionListener(this);
		refreshScreen();
	};

//Search students by ID & grant time with reason
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
	    grantTimeB.setEnabled(false);
	    add(grantTimeB);
	    add(new JLabel(""));
	    add(new JLabel(""));

	    findStudentB.addActionListener(this);
	    grantTimeB.addActionListener(this);
	    backB.addActionListener(this);

	    refreshScreen();
	};

// View registration requests
	public void regRequests(String[][] req){
		// replaced later
		if (req == null) {
	        req = new String[0][5];
	    }
		
		pendingRequests = req; //array of pending requests (get from member 1)
		approvalField = new JTextField[req.length];
		
		clearScreen(0, 6);
		
		add((makeLabel("REQUESTS", Color.BLACK, Color.WHITE)));
		add(new JLabel(""));
		add(new JLabel("Pending Requests: ")); // + AccessRequests.getRequests
		add(new JLabel(""));
		add(new JLabel(""));
		backB = makeButton("Back to dashboard");
		add(backB);
		
		add(makeLabel("University ID", Color.PINK,Color.BLACK));
		add(makeLabel("Name", Color.PINK,Color.BLACK));
		add(makeLabel("Email", Color.PINK,Color.BLACK));
		add(makeLabel("Phone", Color.PINK,Color.BLACK));
		add(makeLabel("Approval", Color.PINK,Color.BLACK));
		add(makeLabel("Enter 'approved' or 'rejected'", Color.PINK,Color.BLACK));
		
		for (int i = 0; i<req.length;i++) {
			//add(new JLabel(req[i][0], JLabel.CENTER));
	        //add(new JLabel(req[i][1], JLabel.CENTER));
	        //add(new JLabel(req[i][2], JLabel.CENTER));
	        //add(new JLabel(req[i][3], JLabel.CENTER));
	        
	        approvalField[i]= new JTextField();
	        approvalField[i].addActionListener(this);
	        add(approvalField[i]);
		}
		backB.addActionListener(this);
		refreshScreen();
	};

// Show Queued Jobs
	public void queueddJobs(String[][] rows){
		 clearScreen(0, 7);

	    add(makeLabel("QUEUED JOBS", Color.BLACK, Color.WHITE));
	    add(new JLabel(""));
	    add(new JLabel(""));
	    add(new JLabel(""));
	    add(new JLabel(""));
	    add(new JLabel(""));
	    backB = makeButton("Back to dashboard");
	    add(backB);

	    add(makeLabel("Job", Color.PINK, Color.BLACK));
	    add(makeLabel("Student", Color.PINK, Color.BLACK));
	    add(makeLabel("Application", Color.PINK, Color.BLACK));
	    add(makeLabel("Duration", Color.PINK, Color.BLACK));
	    add(makeLabel("Position", Color.PINK, Color.BLACK));
	    add(makeLabel("Start", Color.PINK, Color.BLACK));
	    add(makeLabel("Wait", Color.PINK, Color.BLACK));

	    if (rows == null || rows.length == 0) {
	        add(new JLabel("No queued jobs.", JLabel.CENTER));
	        for (int i = 0; i < 6; i++) {
	            add(new JLabel(""));
	        }
	    } else {
	        for (int i = 0; i < rows.length; i++) {
	            for (int j = 0; j < 7; j++) {
	                add(new JLabel(rows[i][j], JLabel.CENTER));
	            }
	        }
	    }

	    backB.addActionListener(this);
	    refreshScreen();
	};

// Show completed Job
	public void completeJobs(String[][] rows) {
		clearScreen(0, 7);

	    add(makeLabel("COMPLETED JOBS", Color.BLACK, Color.WHITE));
	    add(new JLabel(""));
	    add(new JLabel(""));
	    add(new JLabel(""));
	    add(new JLabel(""));
	    add(new JLabel(""));
	    backB = makeButton("Back to dashboard");
	    add(backB);

	    add(makeLabel("Job", Color.PINK, Color.BLACK));
	    add(makeLabel("Student", Color.PINK, Color.BLACK));
	    add(makeLabel("GPU", Color.PINK, Color.BLACK));
	    add(makeLabel("Start", Color.PINK, Color.BLACK));
	    add(makeLabel("Finish", Color.PINK, Color.BLACK));
	    add(makeLabel("Runtime", Color.PINK, Color.BLACK));
	    add(makeLabel("Charged", Color.PINK, Color.BLACK));

	    if (rows == null || rows.length == 0) {
	        add(new JLabel("No completed jobs.", JLabel.CENTER));
	        for (int i = 0; i < 6; i++) {
	            add(new JLabel(""));
	        }
	    } else {
	        for (int i = 0; i < rows.length; i++) {
	            for (int j = 0; j < 7; j++) {
	                add(new JLabel(rows[i][j], JLabel.CENTER));
	            }
	        }
	    }

	    backB.addActionListener(this);
	    refreshScreen();
	};
// Show cancelled Jobs
	public void cancelledJobs(String[][] rows) {
	    clearScreen(0, 5);

	    add(makeLabel("CANCELLED JOBS", Color.BLACK, Color.WHITE));
	    add(new JLabel(""));
	    add(new JLabel(""));
	    add(new JLabel(""));
	    backB = makeButton("Back to dashboard");
	    add(backB);

	    add(makeLabel("Job", Color.PINK, Color.BLACK));
	    add(makeLabel("Student", Color.PINK, Color.BLACK));
	    add(makeLabel("Status when cancelled", Color.PINK, Color.BLACK));
	    add(makeLabel("Application", Color.PINK, Color.BLACK));
	    add(makeLabel("Reason", Color.PINK, Color.BLACK));

	    if (rows == null || rows.length == 0) {
	        add(new JLabel("No cancelled jobs.", JLabel.CENTER));
	        for (int i = 0; i < 4; i++) {
	            add(new JLabel(""));
	        }
	    } else {
	        for (int i = 0; i < rows.length; i++) {
	            for (int j = 0; j < 5; j++) {
	                add(new JLabel(rows[i][j], JLabel.CENTER));
	            }
	        }
	    }

	    backB.addActionListener(this);
	    refreshScreen();
	}

// Cancel Jobs
	public void cancelJobs(String studentId, String[][] rows) {
	    clearScreen(0, 4);
	    cancellableJobRows = rows;
	    add(makeLabel("CANCEL JOBS", Color.BLACK, Color.WHITE));
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

	    add(makeLabel("Student", Color.PINK, Color.BLACK));
	    add(makeLabel("Job", Color.PINK, Color.BLACK));
	    add(makeLabel("Application", Color.PINK, Color.BLACK));
	    add(makeLabel("Status", Color.PINK, Color.BLACK));

	    if (rows == null || rows.length == 0) {
	        add(new JLabel(""));
	        add(new JLabel("No queued or running jobs found.", JLabel.CENTER));
	        add(new JLabel(""));
	        add(new JLabel(""));
	    } else {
	        for (int i = 0; i < rows.length; i++) {
	            for (int j = 0; j < 3; j++) {
	                add(new JLabel(rows[i][j], JLabel.CENTER));
	            }
	            add(new JLabel(""));
	        }
	    }

	    add(new JLabel("Job ID to cancel:"));
	    cancelJobIdField = new JTextField();
	    add(cancelJobIdField);
	    add(new JLabel("Reason:"));
	    cancelReasonField = new JTextField();
	    add(cancelReasonField);

	    confirmCancelB = makeButton("Cancel job");
	    add(confirmCancelB);
	    add(new JLabel(""));
	    add(new JLabel(""));
	    add(new JLabel(""));

	    backB.addActionListener(this);
	    findCancelJobB.addActionListener(this);
	    confirmCancelB.addActionListener(this);

	    refreshScreen();
	}
	
//Saved all canceled jobs
	public void cancelHistroy(String job, String student, String status, String application, String reason) {
		 String[][] updatedHistory =  new String[cancelledHistory.length + 1][5]; //change the initialized history
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

	        fileStorage.saveInFile(
	            clock.getText(),
	            null, null, null, null, null, null
	        );

	        System.out.println("saveInFile completed");
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	}

// Implementing Action listener 
	@Override
	public void actionPerformed(ActionEvent e) {	
		if (e.getSource() == logoutB) {
		    loginPage();
		}
		else if (e.getSource() == backB) {
		    adminPage();
		}
		
		// simulation time
		else if (e.getSource() == setTime) { 
			if (true) {setTimePage();} //if time hasn't been set
		}
		else if (e.getSource() == setStartB) { 
		    try {
		        startDate.commitEdit();
		        startTime.commitEdit();
		        
		        Calendar selectedDate = Calendar.getInstance();
		        selectedDate.setTime((Date) startDate.getValue());

		        Calendar selectedTime = Calendar.getInstance();
		        selectedTime.setTime((Date) startTime.getValue());

		        selectedDate.set(
		                Calendar.HOUR_OF_DAY,
		                selectedTime.get(Calendar.HOUR_OF_DAY)
		        );
		        selectedDate.set(
		                Calendar.MINUTE,
		                selectedTime.get(Calendar.MINUTE)
		        );
		        selectedDate.set(Calendar.SECOND, 0);
		        selectedDate.set(Calendar.MILLISECOND, 0);

		        // scheduler.setStartTime(selectedDate.getTime());
		        adminPage();

		    } catch (ParseException ex) {
		        JOptionPane.showMessageDialog(this, "Enter a valid date and time.");
		    }

		} 

		// set allowance
		else if (e.getSource() == allowance){
			// trail sample to see that it works
			// will be replaced with member 1 provided student list
			TimeAllowance sample = new TimeAllowance(100);

			    String[][] sampleRows = {
			        {"Sample student","" + sample.getAllocatedMinutes(),
			        	"" + sample.getUsedMinutes(), "" + sample.getReservedMinutes(),
			        	"" + sample.getAvailableMinutes()
			        }
			    };
		    
		    studentAllowance(sampleRows);  
		} 
		
		//search student
		else if (e.getSource() == search) {
			searchStudent();
		}
		else if (e.getSource() == findStudentB) {
			String studentId = studentIdField.getText().trim();
			selectedStudentId = null;  //accountManager.findApprovedStudent(studentId);
			if (selectedStudentId == null) { // no student yet
		        student.setText("Student not found");
		        jobs.setText("--");
		        balance.setText("--");
		        available.setText("--");
		        grantTimeB.setEnabled(false);
		        }
			else { // After student is initialized
				// TimeAllowance allowance = selectedStudentId.getAllowance();
				// student.setText(// selectedStudentId.getName() + " (" + selectedStudentId.getUsername() + ")");
			    // jobs.setText("" + scheduler.getJobsForStudent(id).size());
			    // balance.setText("" + allowance.getAllocatedMinutes() + " min allocated");
			    //available.setText("" + allowance.getAvailableMinutes() + " min");
				grantTimeB.setEnabled(true);
			    }
			}			
		else if (e.getSource() == grantTimeB) {
			 String minutesText = minutesField.getText().trim();
			 String reason = reasonField.getText().trim();
			if (selectedStudentId == null) {
		        JOptionPane.showMessageDialog(this, "Search for a student first.");
		    }
			else if (minutesText.isEmpty() || reason.isEmpty()) {
		        JOptionPane.showMessageDialog(this, "Enter the minutes and a reason.");
		    }else {
		        
		        int minutes = Integer.parseInt(minutesText);

	            if (minutes <= 0) {JOptionPane.showMessageDialog(this, "Minutes must be greater than 0.");
	            } 
	            else {
	                // accountManager.grantExtraTime(selectedStudent.getUsername(), minutes, reason);
	                // TimeAllowance allowance = selectedStudent.getAllowance();
	                // balance.setText(allowance.getAllocatedMinutes() + " min allocated");
	                // available.setText(allowance.getAvailableMinutes() + " min");
	                JOptionPane.showMessageDialog(this, "Time granted.");
	            }
		    }
		}
		
		//registration requests
		else if(e.getSource() == requests) {
			// pendingRequests = accountManager.getPendingRequests();
			regRequests(pendingRequests);
		}
		else if(approvalField!=null) {
			for (int i = 0;i<approvalField.length;i++) {
				if (e.getSource() == approvalField[i]) {
					String decision = approvalField[i].getText().trim();
					if (decision.equalsIgnoreCase("approved")) {
						// Call Member 1's method to approve this request
						// It should create the Student account and its allowance.
						JOptionPane.showMessageDialog(this, "Request approved.");}
					else if(decision.equalsIgnoreCase("rejected")) {
						// Call Member 1's method to mark this request as rejected.
						JOptionPane.showMessageDialog(this, "Request rejected.");
					}
					else {
						JOptionPane.showMessageDialog( this, "Enter Approved or Rejected.");
					}
					return;   
				}
			}
		}
		//queued
		else if (e.getSource() == quedB) {
			queueddJobs(new String[0][7]); // Replace with queued-job data.
		} 
		// complete
		else if (e.getSource() == completedB) {
		    completeJobs(new String[0][7]); // Replace with completed-job data.
		} 
		// cancelled
		else if (e.getSource() == canceledB) {
		    cancelledJobs(cancelledHistory); // Replace with cancellation history.
		} 
		
		// cancel
		else if (e.getSource() == cancelB) {
		    cancelJobs("", new String[0][3]);
		} 
		else if (e.getSource() == findCancelJobB) {
		    String id = cancelStudentIdField.getText().trim();
		    //  String[][] rows = scheduler.getCancellableJobsForStudent(id);
		    cancelJobs(id, new String[0][3]); //replace with rows from member 2
		}
		else if (e.getSource() == confirmCancelB) {
			String job = cancelJobIdField.getText().trim();
			String student = cancelStudentIdField.getText().trim();
			String reason = cancelReasonField.getText().trim();

			if (job.isEmpty() || reason.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Enter a job ID and a reason.");
			} 
			else {
		        boolean found = false;
		        for (int i = 0; i < cancellableJobRows.length; i++) {
		            if (cancellableJobRows[i][0].equals(job)) {
		                String application = cancellableJobRows[i][2];
		                String status = cancellableJobRows[i][3];
		                if (true) { // replace with scheduler.cancelJob(job, reason)
		                    cancelHistroy(job, student, status, application, reason);
		                    JOptionPane.showMessageDialog(this, "Job cancelled.");
		                } 
		                else {
		                    JOptionPane.showMessageDialog(this, "The scheduler could not cancel that job.");
		                }
		                found = true;
		                break;
		            }
		        }
		        if (!found) {
		            JOptionPane.showMessageDialog(
		                this, "That job isn't in the displayed queued or running jobs."
		            );
		        }
	    	}
    	}
		else if (e.getSource() == save) {
			System.out.println("Save button clicked");
			storage();
		}
	}
// end of actionPerformed	
}
	
