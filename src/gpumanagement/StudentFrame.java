package gpumanagement;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class StudentFrame extends ScreenFormat implements ActionListener {

    private JLabel allocated, used, reserved, available, durationLabel;

    private JButton submitJobB, myJobsB, logoutB, backB, submitB, searchButton, cancelJobButton;
    private JButton[] sizeButtons;
    
    private JTextField titleField, applicationField, descriptionField;
    private JTextField searchField, cancelJobIdField, cancelReasonField;
    
    private Student student;
    private TaskSize selectedSize;

    public StudentFrame(Student student) {
    	super();
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
    
    public void myJobsPage(String searchText) {
        clearScreen(0, 9);
        
        add(makeLabel("MY JOBS", Color.BLACK, Color.WHITE));

        for (int i = 0; i < 7; i++) {
        	add(new JLabel(""));
        	}

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
        
        ArrayList<Job> jobs;
        if (MainFrame.scheduler == null) {jobs = new ArrayList<Job>();} 
        else {jobs = MainFrame.scheduler.getAllJobs();}

        String[][] rows = JobRows.studentRows(jobs, student.getUniversityId(), MainFrame.scheduler);

        int searchedJobs = 0;
        for (int i = 0; i < rows.length; i++) {
            boolean match = searchText.isEmpty() || rows[i][0].equals(searchText);

            if (match) {
                for (int j = 0; j < rows[i].length; j++) {
                    add(new JLabel(rows[i][j], JLabel.CENTER));
                }
                searchedJobs++;
            }
        }

        if (searchedJobs == 0) {
            add(new JLabel("No jobs with that ID.", JLabel.CENTER));
            for (int i = 0; i < 8; i++) {
                add(new JLabel(""));
            }
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
    
    private void selectSize(int i) { //Show duration and set the size chosen
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

        if (title.isEmpty() || application.isEmpty() || description.isEmpty() || selectedSize == null) {
        	JOptionPane.showMessageDialog(this, "Fill all fields.");
            return;
        }

        Scheduler scheduler = MainFrame.scheduler;
        if (scheduler == null) {
            JOptionPane.showMessageDialog(this, "The simulation clock has not been set yet.");
            return;
        }
        
        Job job = scheduler.submitJob(student.getUniversityId(),title, application, description, selectedSize); //submit using scheduler

        if (job == null) {
            JOptionPane.showMessageDialog(this,"The job could not be submitted. Check the student's available GPU minutes and registered allowance.");
            return;
        }

        JOptionPane.showMessageDialog(this, "Job " + job.getJobId() + " submitted. Status: " + job.getStatus());

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
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter the job ID as a number.");
            return;
        }

        Scheduler scheduler = MainFrame.scheduler;
        if (scheduler == null) {
            JOptionPane.showMessageDialog(this, "The scheduler is not available.");
            return;
        }

        Job selectedJob = null;
        ArrayList<Job> jobs = scheduler.getAllJobs();
        for (Job job : jobs) {
            if (job.getJobId() == requestedId) {
                selectedJob = job;
                break;
            }
        }

        if (selectedJob == null || !selectedJob.getStudentId().equals(student.getUniversityId())) {
            JOptionPane.showMessageDialog(this, "Job not found.");
            return;
        }

        if (selectedJob.getStatus() != JobStatus.QUEUED && selectedJob.getStatus() != JobStatus.RUNNING) {
            JOptionPane.showMessageDialog(this, "Only queued or running jobs can be cancelled.");
            return;
        }

        boolean success = scheduler.cancelJob(requestedId, student.getUniversityId(), reason); //cancel job through scheduler

        if (success) {
            JOptionPane.showMessageDialog(this, "Job cancelled.");
            myJobsPage("");
        } else {
            JOptionPane.showMessageDialog(this, "The job could not be cancelled.");
        }
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
