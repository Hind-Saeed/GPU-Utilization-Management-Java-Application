package gpumanagement;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class StudentFrame extends ScreenFormat implements ActionListener {

    private Student student;

    private JLabel allocated, used, reserved, available;

    private JButton submitJobB, myJobsB, logoutB, backB;
    private JTextField titleField, applicationField, descriptionField;
    private JButton[] sizeButtons;
    private JButton submitB;
    private JLabel durationLabel;
    private TaskSize selectedSize;

    private JTextField searchField, cancelJobIdField, cancelReasonField;
    private JButton searchButton, cancelJobButton;
    private ArrayList<Job> myJobs = new ArrayList<>();

    //ID counter to help avoid duplicate. Replace with proper ID assignments from member 2.
    private static int nextJobId = 1;

	private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"); // format date

    public StudentFrame(Student student) {
        this.student = student;
        setTitle("Student Dashboard");
        setSize(1300, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        studentPage();
        setVisible(true);
    }

    public void studentPage() {
        clearScreen(0, 2);

        TimeAllowance allowance = student.getAllowance();
        // View allocated, used, reserved, and available GPU minutes
        allocated = new JLabel("Allocated GPU minutes: " + allowance.getAllocatedMinutes(),JLabel.CENTER);
        used = new JLabel("Used GPU minutes: " + allowance.getUsedMinutes(),JLabel.CENTER);
        reserved = new JLabel("Reserved GPU minutes: " + allowance.getReservedMinutes(),JLabel.CENTER);
        available = new JLabel("Available GPU minutes: " + allowance.getAvailableMinutes(),JLabel.CENTER);

        submitJobB = makeButton("Submit a job");
        myJobsB = makeButton("View and search my jobs");
        logoutB = makeButton("Log out");

        add(makeLabel("STUDENT DASHBOARD", Color.BLACK, Color.WHITE));
        add(makeLabel(student.getName(), Color.BLACK, Color.WHITE));
        
        add(allocated);
        add(used);
        add(reserved);
        add(available);

        add(submitJobB);
        add(new JLabel(""));

        add(myJobsB);
        add(logoutB);

        submitJobB.addActionListener(this);
        myJobsB.addActionListener(this);
        logoutB.addActionListener(this);

        refreshScreen();
    }

    public void submitJobPage() {
        clearScreen(0, 2);
        //Enter a job title, application name, and description; select a task size and confirm its duration.
        titleField = new JTextField();
        applicationField = new JTextField();
        descriptionField = new JTextField();
        durationLabel = makeLabel("Choose a size to view duration", Color.PINK, Color.BLACK);

        sizeButtons = new JButton[4];
        sizeButtons[0] = makeButton("Small");
        sizeButtons[1] = makeButton("Medium");
        sizeButtons[2] = makeButton("Large");
        sizeButtons[3] = makeButton("Very large");

        submitB = makeButton("Submit job");
        backB = makeButton("Back to dashboard");

        add(makeLabel("SUBMIT A JOB", Color.BLACK, Color.WHITE));
        add(new JLabel(""));

        add(new JLabel("Job title:"));
        add(titleField);

        add(new JLabel("Application name:"));
        add(applicationField);

        add(new JLabel("Description:"));
        add(descriptionField);

        add(new JLabel("Select task size:"));
        add(new JLabel(""));
       
        add(sizeButtons[0]);
        add(sizeButtons[1]);
        add(sizeButtons[2]);
        add(sizeButtons[3]);
        
        add(durationLabel);
        add(new JLabel("")); 
        add(backB);
        add(submitB);

        sizeButtons[0].addActionListener(this);
        sizeButtons[1].addActionListener(this);
        sizeButtons[2].addActionListener(this);
        sizeButtons[3].addActionListener(this);
        backB.addActionListener(this);
        submitB.addActionListener(this);

        refreshScreen();
    }
    
    //View and search own jobs, including status, assigned GPU, predicted start/finish, waiting time, and results.
    public void myJobsPage(String searchText) {
        clearScreen(0, 9);
        
        add(makeLabel("MY JOBS", Color.BLACK, Color.WHITE));

        for (int i = 0; i < 7; i++) { add(new JLabel(""));}

        backB = makeButton("Back to dashboard");
        add(backB);

        add(new JLabel("Search job ID:"));
        searchField = new JTextField(searchText);
        add(searchField);
        searchButton = makeButton("Search");
        add(searchButton);
        for (int i = 0; i < 6; i++) {add(new JLabel(""));}

        String[] headings = {"Job ID", "Title", "Application", "Status", "GPU","Predicted start", "Predicted finish", "Wait", "Result"};
        for (int i = 0; i < headings.length; i++) {add(makeLabel(headings[i], Color.PINK, Color.BLACK));}

        int searchedJobs = 0;
        for (int i = 0; i < myJobs.size(); i++) {
            Job job = myJobs.get(i);
            boolean match = searchText.isEmpty() || ("" + job.getJobId()).contains(searchText);

            if (match) {
                add(new JLabel("" + job.getJobId(), JLabel.CENTER));
                add(new JLabel(job.getTitle(), JLabel.CENTER));
                add(new JLabel(job.getApplicationName(), JLabel.CENTER));
                add(new JLabel("" + job.getStatus(), JLabel.CENTER));

                Integer gpuId = job.getAssignedGpuId();
                add(new JLabel(gpuId == null ? "--" : "GPU " + gpuId,JLabel.CENTER));

                add(new JLabel(predStart(job), JLabel.CENTER));
                add(new JLabel(predFinish(job), JLabel.CENTER));
                add(new JLabel(waitTime(job), JLabel.CENTER));

                // The provided Job class has no result field yet.
                add(new JLabel("--", JLabel.CENTER));

                searchedJobs++;
            }
        }

        if (searchedJobs == 0) {
            add(new JLabel("No jobs with that ID.", JLabel.CENTER));
            for (int i = 0; i < 8; i++) {add(new JLabel(""));}
        }

        add(new JLabel("Job ID to cancel:"));
        cancelJobIdField = new JTextField();
        add(cancelJobIdField);

        add(new JLabel("Cancellation reason:"));
        cancelReasonField = new JTextField();
        add(cancelReasonField);

        cancelJobButton = makeButton("Cancel my job");
        add(cancelJobButton);

        for (int i = 0; i < 3; i++) {
            add(new JLabel(""));
        }

        backB.addActionListener(this);
        searchButton.addActionListener(this);
        cancelJobButton.addActionListener(this);

        refreshScreen();
    }
 
    private String predStart(Job job) {
        Scheduler scheduler = MainFrame.scheduler;

        if (job.getStartTime() != null) {return formatTime(job.getStartTime());}
        if (scheduler == null) {return "--";}

        int wait = scheduler.predictWaitingTime(job);
        if (wait < 0) {return "--";}
        
        return formatTime(scheduler.getSimulationTime().plusMinutes(wait)); //add waiting to simulating time
    }

    private String predFinish(Job job) {
        if (job.getFinishTime() != null) {return formatTime(job.getFinishTime());}

        String start = predStart(job);
        if (start.equals("--")) {return "--";}

        LocalDateTime startTime;
        if (job.getStartTime() != null) {startTime = job.getStartTime();} 
        else {
            Scheduler scheduler = MainFrame.scheduler;
            int wait = scheduler.predictWaitingTime(job);
            startTime = scheduler.getSimulationTime().plusMinutes(wait);
        }

        return formatTime(startTime.plusMinutes(job.getDuration()));
    }

    private String waitTime(Job job) {
        if (job.getStatus() == JobStatus.RUNNING || job.getStatus() == JobStatus.COMPLETED) {
            return "0 min";
        }

        Scheduler scheduler = MainFrame.scheduler;

        if (scheduler == null) {return "--";}

        int wait = scheduler.predictWaitingTime(job);
        return wait < 0 ? "--" : wait + " min";
    }

    private String formatTime(LocalDateTime time) {return time == null ? "--" : time.format(FMT);}

    private void selectSize(int i) {
        if (i == 0) {selectedSize = TaskSize.SMALL;} 
        else if (i == 1) {selectedSize = TaskSize.MEDIUM;} 
        else if (i == 2) {selectedSize = TaskSize.LARGE;} 
        else {selectedSize = TaskSize.VERY_LARGE;}
        durationLabel.setText("Duration: " + selectedSize.getDuration() + " minutes");
    }

    private void submitJob() {
        String title = titleField.getText().trim();
        String application = applicationField.getText().trim();
        String description = descriptionField.getText().trim();

        if (title.isEmpty() || application.isEmpty()|| description.isEmpty() || selectedSize == null) {
            JOptionPane.showMessageDialog(this,"Fill all fields");
            return;
        }

        Scheduler scheduler = MainFrame.scheduler;
        if (scheduler == null) {
            JOptionPane.showMessageDialog(this, "The simulation clock has not been set yet.");
            return;
        }

        int duration = selectedSize.getDuration();
        TimeAllowance allowance = student.getAllowance();

        if (duration > allowance.getAvailableMinutes()) {
            JOptionPane.showMessageDialog(this, "You do not have enough available GPU minutes.");
            return;
        }

        Job job = new Job(nextJobId++, student.getUniversityId(), title, application, description, selectedSize, scheduler.getSimulationTime());
        scheduler.addJob(job);
        scheduler.assignWaitingJobs();
        myJobs.add(job);

        JOptionPane.showMessageDialog( this,"Job " + job.getJobId() + " was added to the queue.");
        studentPage();
    }

    private void cancelOwnJob() {
        String jobText = cancelJobIdField.getText().trim();
        String reason = cancelReasonField.getText().trim();

        if (jobText.isEmpty() || reason.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter the job ID and cancellation reason.");
            return;
        }

        int requestedId;
        try {
            requestedId = Integer.parseInt(jobText);
        } 
        catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter the job ID as a number.");
            return;
        }

        Job selectedJob = null;
        for (int i = 0; i < myJobs.size(); i++) {
            if (myJobs.get(i).getJobId() == requestedId) {
                selectedJob = myJobs.get(i);
                break;
            }
        }

        if (selectedJob == null|| !selectedJob.getStudentId().equals(student.getUniversityId())) {
            JOptionPane.showMessageDialog(this, "Job not found.");
            return;
        }

        if (selectedJob.getStatus() != JobStatus.QUEUED && selectedJob.getStatus() != JobStatus.RUNNING) {
            JOptionPane.showMessageDialog(this, "Only queued or running jobs can be cancelled.");
            return;
        }

        //Cancellation uses cancelJob method from member 2 to remove it from schedule - must update!
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == submitJobB) {
            submitJobPage();
        } 
        else if (e.getSource() == myJobsB) {
            myJobsPage("");
        } 
        else if (e.getSource() == backB) {
            studentPage();
        } 
        else if (e.getSource() == logoutB) {
            dispose();
            new MainFrame();
        } 
        else if (e.getSource() == searchButton) {
            myJobsPage(searchField.getText().trim());
        } 
        else if (e.getSource() == submitB) {
            submitJob();
        } 
        else if (e.getSource() == cancelJobButton) {
            cancelOwnJob();
        } 
        else if (sizeButtons != null) {
            for (int i = 0; i < sizeButtons.length; i++) {
                if (e.getSource() == sizeButtons[i]) {
                    selectSize(i);
                    return;
                }
            }
        }
    }
}