package gpumanagement;

import java.time.LocalDateTime;

public class Job {
	private int jobId;
    private String studentId;

    private String title;
    private String applicationName;
    private String description;

    private TaskSize taskSize;

    private int workloadUnits;
    private int estimatedDuration;

    private LocalDateTime submissionTime;

    private JobStatus status;

    private Integer assignedGpuId;

    private LocalDateTime startTime;
    private LocalDateTime finishTime;

    private LocalDateTime cancellationTime;
    private String cancelledBy;
    private String cancellationReason;
	
	public Job(int id, String student, String t, String appname, String desc, TaskSize taskSize, LocalDateTime submissionTime) {

	    jobId = id;
	    studentId = student;
	    title = t;
	    applicationName = appname;
	    description = desc;
	    this.taskSize = taskSize;
	    workloadUnits = taskSize.getDuration();
	    estimatedDuration = taskSize.getDuration();
	    this.submissionTime = submissionTime;
	    status = JobStatus.QUEUED;
	    assignedGpuId = null;
	    startTime = null;
	    finishTime = null;
	    cancellationTime = null;
	    cancelledBy = null;
	    cancellationReason = null;
	}
	

	public int getJobId() {
		return jobId;
	}

	public String getStudentId() {
		return studentId;
	}

	public String getTitle() {
		return title;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public String getDescription() {
		return description;
	}

	public TaskSize getTaskSize() {
		return taskSize;
	}

	public int getWorkloadUnits() {
		return workloadUnits;
	}

	public int getEstimatedDuration() {
		return estimatedDuration;
	}

	public int getDuration() {
		return estimatedDuration;
	}

	public LocalDateTime getSubmissionTime() {
		return submissionTime;
	}

	public JobStatus getStatus() {
		return status;
	}

	public Integer getAssignedGpuId() {
		return assignedGpuId;
	}

	public LocalDateTime getStartTime() {
		return startTime;
	}

	public LocalDateTime getFinishTime() {
		return finishTime;
	}

	public LocalDateTime getCancellationTime() {
		return cancellationTime;
	}

	public String getCancelledBy() {
		return cancelledBy;
	}

	public String getCancellationReason() {
		return cancellationReason;
	}


	public void startJob(int gpuId, LocalDateTime time) {
		if (status != JobStatus.QUEUED) {
	        return;
	    }
		assignedGpuId = gpuId;
		startTime = time;
		status = JobStatus.RUNNING;
	}
	
	public void completeJob(LocalDateTime time) {
		if (status != JobStatus.RUNNING) {
	        return;
	    }
		finishTime = time;
		status = JobStatus.COMPLETED;
	}
	
	public void cancelJob(LocalDateTime time, String cancelledBy, String reason) {
		if (status != JobStatus.QUEUED && status != JobStatus.RUNNING) {
		        return;
		    }

		    if (status == JobStatus.RUNNING) {
		        finishTime = time;
		    }
		
		cancellationTime = time;
		this.cancelledBy = cancelledBy;
		cancellationReason = reason;
		status = JobStatus.CANCELLED;
	}

	@Override
	public String toString() {
		return "Job ID: " + jobId
				+ ", Student: " + studentId
				+ ", Title: " + title
				+ ", Size: " + taskSize
				+ ", Duration: " + estimatedDuration
				+ ", Status: " + status
				+ ", GPU: " + assignedGpuId;
	}

}