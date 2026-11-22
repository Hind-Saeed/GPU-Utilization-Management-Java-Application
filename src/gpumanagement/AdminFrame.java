package gpumanagement;
import java.util.*;
import java.util.List;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class AdminFrame extends ScreenFormat implements ActionListener {

	private JLabel clock, running, queued, completed, cancelled, utilization;
	private JLabel student, jobs, balance, available;
	private JLabel[][] gpusTable = new JLabel[4][5];
    
	private JButton setTime, advance, allowance, search, requests, quedB, cancelB, completedB, canceledB, save, logoutB, backB;
	private JButton setStartB, findStudentB, grantTimeB;
	private JButton viewStudentJobsB, findCancelJobB;
	private JButton[] approveButtons, rejectButtons; //array of approve & reject buttons for each request
	private JButton[] cancelButtons;
	
	private JSpinner startDate, startTime;
    
	private JTextField studentIdField, minutesField, reasonField;
    private JTextField cancelStudentIdField;
    private JTextField[] allowanceFields;

    private String selectedStudentId;
    private String[][] pendingRequests = new String[0][5]; //id, name, email, phone, password
    private String[][] cancellableJobRows = new String[0][4]; //student id, job id, application, status
    
	// implementing other classes
    private Scheduler scheduler;
    private final AccountService accountService;
    private final User currentUser;
    private Student selectedStudent;
    private Storage fileStorage;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private String fmt(LocalDateTime t) {return t == null ? "--" : t.format(FMT);}
	
    private String clockText() { // Set simulation time
        if (scheduler == null) {
        	return "Not set";
        }
        return fmt(scheduler.getSimulationTime());
    }

    public AdminFrame(Scheduler scheduler, AccountService accountService, User currentUser) {
        super();
        this.scheduler = scheduler;
        this.accountService = accountService;
        this.currentUser = currentUser;
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

        ArrayList<Job> jobs = currentJobs();

        running = new JLabel("Running: " + JobRows.count(jobs, JobStatus.RUNNING) + " / 4", JLabel.CENTER);
        queued = new JLabel("Queued: " + JobRows.count(jobs, JobStatus.QUEUED), JLabel.CENTER);
        completed = new JLabel("Completed: " + JobRows.count(jobs, JobStatus.COMPLETED),JLabel.CENTER);
        cancelled = new JLabel("Cancelled: " + JobRows.count(jobs, JobStatus.CANCELLED), JLabel.CENTER);
        utilization = new JLabel("Utilization: " + JobRows.utilization(jobs, MainFrame.scheduler,MainFrame.simulationStart), JLabel.CENTER);

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

        String[][] gpuInfo = JobRows.gpus(jobs, MainFrame.scheduler); //pass scheduler to get busy, remaining mintues, and finish time 

        for (int i = 0; i < 4; i++) { // create label or fields for the information about the column
            for (int j = 0; j < 5; j++) {
                if (j == 0) {
                    gpusTable[i][j] = makeLabel(gpuInfo[i][j], Color.PINK, Color.BLACK); 
                } else {
                    gpusTable[i][j] = new JLabel(gpuInfo[i][j], JLabel.CENTER);
                }
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

    public void setTimePage() { //Setting simulation time using date picker
        clearScreen(4, 2);

        startDate = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH)); //initalize date to today
        startDate.setEditor(new JSpinner.DateEditor(startDate, "dd/MM/yyyy")); //format 

        startTime = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.MINUTE)); //initialize time to now 
        startTime.setEditor(new JSpinner.DateEditor(startTime, "HH:mm")); //format

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
    }

    public void searchStudent() { //allow admin to search for students using their univeristy id
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
        add(makeLabel("Job ID", Color.PINK, Color.BLACK));
        add(makeLabel("Balance", Color.PINK, Color.BLACK));
        add(makeLabel("Available time", Color.PINK, Color.BLACK));

        student = new JLabel("", JLabel.CENTER);
        jobs = new JLabel("", JLabel.CENTER);
        balance = new JLabel("", JLabel.CENTER);
        available = new JLabel("", JLabel.CENTER);
        viewStudentJobsB = makeButton("View this student's jobs");
        viewStudentJobsB.setEnabled(false);

        add(student);
        add(jobs);
        add(balance);
        add(available);
        add(viewStudentJobsB);
        
   
        add(new JLabel(""));
        add(new JLabel("Minutes to add:"));
        minutesField = new JTextField(); //Allows them to add minutes to the student
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

        findStudentB.addActionListener(this);
        grantTimeB.addActionListener(this);
        viewStudentJobsB.addActionListener(this);
        backB.addActionListener(this);

        refreshScreen();
    }

    public void regRequests() { //Allows admin to view registration requests
        List<AccessRequest> list = accountService.getPendingRequests(); //implemented from the class AccountService to view pending requests as a list
        pendingRequests = new String[list.size()][5]; 
        for (int i = 0; i < list.size(); i++) { //loop over list of requests
            AccessRequest req = list.get(i);
            pendingRequests[i][0] = req.getUniversityId();
            pendingRequests[i][1] = req.getName();
            pendingRequests[i][2] = req.getEmail();
            pendingRequests[i][3] = req.getPhone();
            pendingRequests[i][4] = req.getPasswordForAccountCreation();
        }

        allowanceFields = new JTextField[pendingRequests.length]; //Default is 100 but the admin can edit it
        approveButtons = new JButton[pendingRequests.length];
        rejectButtons = new JButton[pendingRequests.length];

        clearScreen(0, 8);
        add(makeLabel("REQUESTS", Color.BLACK, Color.WHITE));
        add(new JLabel("Pending: " + pendingRequests.length, JLabel.CENTER));

        for (int i = 0; i < 5; i++) { //blank space to move to the next row
            add(new JLabel(""));
        }

        backB = makeButton("Back to dashboard");
        add(backB);
        backB.addActionListener(this);

        String[] headings = {"University ID", "Name", "Email", "Phone", "Password", "Allowance (min)", "Approve", "Reject"}; //Organize information labels

        for (int i = 0; i < headings.length; i++) {
            add(makeLabel(headings[i], Color.PINK, Color.BLACK));
        }

        for (int i = 0; i < pendingRequests.length; i++) { //loop over pending requests to put the information in a tabular format
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
    }

    private void decision(int i, boolean approved) { //Handles action that happends if approve or reject is clicked
        String req = pendingRequests[i][0];
        try {
            if (approved) { // admin clicked approve
                int minutes = Integer.parseInt(allowanceFields[i].getText().trim()); //assign allowance
            
                Student student = accountService.approveRequest(req, minutes); //create student
                if (MainFrame.scheduler != null) {
                    MainFrame.scheduler.registerAllowance(student.getUniversityId(),student.getAllowance()); //register with schedueler
                }

                JOptionPane.showMessageDialog(this, req + " approved with " + minutes + " minutes."); 
            } else { //admin clicked reject
                accountService.rejectRequest(req);
                JOptionPane.showMessageDialog(this, req + " rejected.");
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage()); //could be that the minutes are invalid or account is not pending
        }

        regRequests();
    }

    private void cancelRow(int i) { //handles job cancellation
        String studentId = cancellableJobRows[i][0];
        String jobIdText = cancellableJobRows[i][1];
        String reason = JOptionPane.showInputDialog(this, "Reason for cancelling job " + jobIdText + ":");

        if (reason == null) {return;}
        reason = reason.trim();
        if (reason.isEmpty()) {
            JOptionPane.showMessageDialog(this, "A reason is required."); 
            return;
        }

        int jobId;
        try {
            jobId = Integer.parseInt(jobIdText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid job ID.");
            return;
        }

        if (MainFrame.scheduler == null) {
            JOptionPane.showMessageDialog(this, "The scheduler is not available.");
            return;
        }

        boolean success = MainFrame.scheduler.cancelJob(jobId, "admin", reason); //Call the cancelling method from schedueler to officially cancel
        if (success) {
            JOptionPane.showMessageDialog(this, "Job cancelled.");
            cancelJobs(studentId,JobRows.cancellable(currentJobs(), studentId));
        } else {
            JOptionPane.showMessageDialog(this, "The job could not be cancelled.");
        }
    }

    private void showTable(String title, String[] headings, String[][] rows, String empty) {//some buttons had the same screen format - method to easily execute
        int cols = headings.length;
        clearScreen(0, cols);

        add(makeLabel(title, Color.BLACK, Color.WHITE));
        for (int i = 0; i < cols - 2; i++) { //title
            add(new JLabel(""));
        }

        backB = makeButton("Back to dashboard");
        add(backB);

        for (int i = 0; i < cols; i++) { //headings
            add(makeLabel(headings[i], Color.PINK, Color.BLACK));
        }

        if (rows == null || rows.length == 0) { //when there's no data, prints the message
            add(new JLabel(empty, JLabel.CENTER));
            for (int i = 1; i < cols; i++) {
                add(new JLabel(""));
            }
        } else { //acutal data in rows
            for (int i = 0; i < rows.length; i++) {
                for (int j = 0; j < cols; j++) {
                    add(new JLabel(rows[i][j], JLabel.CENTER));
                }
            }
        }

        backB.addActionListener(this);
        refreshScreen();
    }
	//View Student Allowance, Que, Completed, and Cancelled Jobs
    public void studentAllowance(String[][] rows) {
        showTable("STUDENT ALLOWANCE",new String[]{"Student", "Allocated", "Used", "Reserved", "Available"},
                rows, "No students yet");
    }

    public void queuedJobs(String[][] rows) {
        showTable(
                "QUEUED JOBS",new String[]{"Job", "Student", "Application", "Duration","Position", "Predicted start", "Predicted finish", "Wait"},
                rows,"No queued jobs.");
    }

    public void completeJobs(String[][] rows) {
        showTable("COMPLETED JOBS", new String[]{"Job", "Student", "GPU", "Submitted", "Started", "Finished", "Runtime", "Queue wait", "Charged"},
                rows, "No completed jobs.");
    }

    public void cancelledJobs(String[][] rows) { //record of cancelled jobs
        showTable("CANCELLED JOBS", new String[]{"Job", "Student", "Status when cancelled","Application", "Reason"},
                rows, "No cancelled jobs.");
    }

    public void cancelJobs(String studentId, String[][] rows) { // Job cancelling page
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

        if (cancellableJobRows.length == 0) { // jobs aren't running or queued
            add(new JLabel("No queued or running jobs", JLabel.CENTER));
            for (int i = 0; i < 4; i++) {
                add(new JLabel(""));
            }
        } else {
            for (int i = 0; i < cancellableJobRows.length; i++) {
                for (int j = 0; j < 4; j++) { //display the values of the job
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

    public void storage() { //for saving and restoring data
        try {
            fileStorage = new Storage(); 
            List<Student> students = allStudents();
			
			//Get all data about students
            String[][] accounts = new String[students.size()][5];
            String[][] allowances = new String[students.size()][4];
            for (int i = 0; i < students.size(); i++) {
                Student s = students.get(i);
                TimeAllowance a = s.getAllowance();
                accounts[i] = new String[]{s.getUniversityId(), s.getName(), s.getEmail(), s.getPhone(), s.passwordForStorage()};
                allowances[i] = new String[]{s.getUniversityId(), "" + a.getAllocatedMinutes(), "" + a.getUsedMinutes(),"" + a.getReservedMinutes()};
            }
			//Get all requests made
            List<AccessRequest> requestList = accountService.getAllRequests();
            String[][] requests = new String[requestList.size()][6];
            for (int i = 0; i < requestList.size(); i++) {
                AccessRequest r = requestList.get(i);
                requests[i] = new String[]{r.getUniversityId(), r.getName(), r.getEmail(), r.getPhone(), r.getPasswordForAccountCreation(),
                		r.getStatus().toString()};
            }
			//Get all the information about students that were granted time
            List<AllowanceGrant> grantList = accountService.getAllowanceGrants();
            String[][] grants = new String[grantList.size()][4];
            for (int i = 0; i < grantList.size(); i++) {
                AllowanceGrant g = grantList.get(i);
                grants[i] = new String[]{g.getStudentId(), "" + g.getMinutes(), g.getReason(), g.getAdministratorUsername()};
            }

			//Get all jobs that are currently running 
            ArrayList<Job> allJobs = currentJobs();
            String[][] jobRows = new String[allJobs.size()][14];
            ArrayList<String[]> cancellationList = new ArrayList<>();
            ArrayList<String[]> assignmentList = new ArrayList<>();
            for (int i = 0; i < allJobs.size(); i++) {
                Job j = allJobs.get(i); //for each job
                String gpuId = j.getAssignedGpuId() == null ? "" : "" + j.getAssignedGpuId(); //get GPU assignment if it exists
                jobRows[i] = new String[]{"" + j.getJobId(), j.getStudentId(),
                        j.getTitle(), j.getApplicationName(), j.getDescription(),
                        j.getTaskSize().toString(), j.getSubmissionTime().toString(),
                        j.getStatus().toString(), gpuId, dateText(j.getStartTime()),
                        dateText(j.getFinishTime()), dateText(j.getCancellationTime()),
                        safeText(j.getCancelledBy()), safeText(j.getCancellationReason())};

                if (j.getStatus() == JobStatus.CANCELLED) { //get when the job was cancelled
                    String priorStatus = j.getStartTime() == null ? "QUEUED" : "RUNNING";
                    cancellationList.add(new String[]{"" + j.getJobId(), j.getStudentId(),
                            priorStatus, j.getApplicationName(), safeText(j.getCancellationReason()),
                            safeText(j.getCancelledBy()), dateText(j.getCancellationTime())});
                }
                if (j.getStatus() == JobStatus.RUNNING && j.getAssignedGpuId() != null) {
                    assignmentList.add(new String[]{"" + j.getAssignedGpuId(), "" + j.getJobId()});
                }
            }

            String[][] cancellations = cancellationList.toArray(new String[cancellationList.size()][]);
            String[][] assignments = assignmentList.toArray(new String[assignmentList.size()][]);
            String clockValue = scheduler == null ? "Not set" : scheduler.getSimulationTime().toString();
            String startValue = MainFrame.simulationStart == null? "Not set" : MainFrame.simulationStart.toString();
			
            fileStorage.saveInFile(clockValue, startValue, accounts, requests, allowances, grants, jobRows, cancellations, assignments);
            JOptionPane.showMessageDialog(this, "Data saved.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not save data: " + ex.getMessage());
        }
    }
    
    private String dateText(LocalDateTime value) {
        return value == null ? "" : value.toString(); //LocalDateTime that can be parsed
    }

    private String safeText(String value) {
        return value == null ? "" : value; //handle problems that may arise from null
    }

    private List<Student> allStudents() { //Registered students
        return accountService.getStudents(currentUser); 
    }

    private Student findStudentById(String id) { //search list of student
        for (Student s : allStudents()) {
            if (s.getUniversityId().equals(id)) return s;
        }
        return null;
    }
    private ArrayList<Job> currentJobs() { //Submitted Jobs
        if (MainFrame.scheduler == null) {
            return new ArrayList<Job>();
        }

        return MainFrame.scheduler.getAllJobs();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
		if (approveButtons != null) { 
            for (int i = 0; i < approveButtons.length; i++) {
                if (e.getSource() == approveButtons[i]) { //approved
                    decision(i, true);
                    return;
                }
                if (e.getSource() == rejectButtons[i]) { //rejected
                    decision(i, false);
                    return;
                }
            }
        }
        if (cancelButtons != null) {
            for (int i = 0; i < cancelButtons.length; i++) {
                if (e.getSource() == cancelButtons[i]) {
                    cancelRow(i);
                    return;
                }
            }
        }

        if (e.getSource() == logoutB) {
            new MainFrame();
            dispose();
        } else if (e.getSource() == backB) {
            adminPage();
        } else if (e.getSource() == setTime) {
            if (scheduler == null) {
                setTimePage();
            } else {
                JOptionPane.showMessageDialog(this, "The start time is already set.");
            }
        } else if (e.getSource() == setStartB) { //Connect everything after time has been set
            try {
                startDate.commitEdit(); //apply data
                startTime.commitEdit(); //apply time

                Calendar selectedDate = Calendar.getInstance();  //get selected data
                selectedDate.setTime((Date) startDate.getValue());
                Calendar selectedTime = Calendar.getInstance();
                selectedTime.setTime((Date) startTime.getValue());

                selectedDate.set(Calendar.HOUR_OF_DAY, selectedTime.get(Calendar.HOUR_OF_DAY));
                selectedDate.set(Calendar.MINUTE,selectedTime.get(Calendar.MINUTE));
                selectedDate.set(Calendar.SECOND, 0); //start time from the starting second
                selectedDate.set(Calendar.MILLISECOND, 0);

                if (scheduler != null) {
                    JOptionPane.showMessageDialog(this, "The start time is already set."); //can only be set once
                    return;
                }

                LocalDateTime selectedStartTime = LocalDateTime.ofInstant(selectedDate.getTime().toInstant(), ZoneId.systemDefault()); //get start time
                MainFrame.simulationStart = selectedStartTime; //link to scheduler
                MainFrame.scheduler = new Scheduler(selectedStartTime);
                scheduler = MainFrame.scheduler;
                for (Student student : allStudents()) { //registers student allowance 
                    MainFrame.scheduler.registerAllowance(student.getUniversityId(),student.getAllowance());
                }
                adminPage(); //go back to adminPage after setting
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid date and time.");
            }
        } else if (e.getSource() == allowance) { 
            List<Student> list = allStudents(); //get all student allowance details and details
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

            studentAllowance(rows); //pass to student allowance to be displayed
        } else if (e.getSource() == search) {
            searchStudent();
        } else if (e.getSource() == findStudentB) { //when admin clicks search
            String studentId = studentIdField.getText().trim();
            selectedStudent = findStudentById(studentId);
            viewStudentJobsB.setEnabled(false);

            if (selectedStudent == null) { //no student found
                selectedStudentId = null;
                student.setText("Student not found");
                jobs.setText("--");
                balance.setText("--");
                available.setText("--");
                grantTimeB.setEnabled(false); //stay disabled
            } else {
                selectedStudentId = selectedStudent.getUniversityId();
                TimeAllowance a = selectedStudent.getAllowance();
                student.setText(selectedStudent.getName() + " (" + selectedStudentId + ")");
                int jobCount = 0;
                for (Job job : currentJobs()) { //count that student's jobs
                    if (job.getStudentId().equals(selectedStudentId)) {
                    	jobCount++;
                    }
                }
                jobs.setText("" + jobCount);
                balance.setText(a.getAllocatedMinutes() + " min allocated");
                available.setText(a.getAvailableMinutes() + " min");
                grantTimeB.setEnabled(true); //enable
            }
            viewStudentJobsB.setEnabled(selectedStudent != null); //enable
		} else if (e.getSource() == viewStudentJobsB && selectedStudent != null) { //view the student's jobs
			String[] headings = {"Job ID", "Title", "Application", "Status", "GPU", "Predicted start", "Predicted finish", "Wait", "Result"};
			String[][] rows = JobRows.studentRows(currentJobs(), selectedStudent.getUniversityId(), MainFrame.scheduler);
			showTable("JOBS FOR " + selectedStudent.getUniversityId(), headings, rows, "No jobs for this student.");
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
                    if (minutes <= 0) {JOptionPane.showMessageDialog(this, "Minutes must be greater than 0.");
                    } else {
                    	try {
                    	    accountService.grantAdditionalTime(selectedStudentId, minutes, reason, currentUser);
                    	    TimeAllowance a = selectedStudent.getAllowance();
                    	    balance.setText(a.getAllocatedMinutes() + " min allocated");
                    	    available.setText(a.getAvailableMinutes() + " min");
                    	    JOptionPane.showMessageDialog(this, minutes + " minutes granted.");
                    	} catch (IllegalArgumentException | SecurityException ex) {
                    	    JOptionPane.showMessageDialog(this, ex.getMessage());
                    	}
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Minutes must be a whole number.");
                }
            }
        } else if (e.getSource() == requests) {
            regRequests();
        } else if (e.getSource() == quedB) {
        	queuedJobs(JobRows.queued(currentJobs(), MainFrame.scheduler));
        } else if (e.getSource() == completedB) {
        	completeJobs(JobRows.completed(currentJobs()));
        } else if (e.getSource() == canceledB) {
        	cancelledJobs(JobRows.cancelled(currentJobs()));
        } else if (e.getSource() == cancelB) {
            cancelJobs("", new String[0][4]);
        } else if (e.getSource() == findCancelJobB) {
        	String id = cancelStudentIdField.getText().trim();
            cancelJobs(id, JobRows.cancellable(currentJobs(), id));
        } else if (e.getSource() == save) {
            storage();
        } else if (e.getSource() == advance) {
            scheduler.advanceTime(1);
            adminPage();
        }
    }
}
