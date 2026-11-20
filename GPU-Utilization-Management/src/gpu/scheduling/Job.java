package gpu.scheduling;

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
	
	public Job(int id, String student, String t, String appname, String desc, TaskSize taskSize, LocalDateTime time) {

	    jobId = id;
	    studentId = student;
	    title = t;
	    applicationName = appname;
	    description = desc;
	    this.taskSize = taskSize;
	    this.workloadUnits = taskSize.getDuration();
	    this.estimatedDuration = taskSize.getDuration();
	    this.submissionTime = submissionTime;
	    this.status = JobStatus.QUEUED;
	    this.assignedGpuId = null;
	    this.startTime = null;
	    this.finishTime = null;
	    this.cancellationTime = null;
	    this.cancelledBy = null;
	    this.cancellationReason = null;
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
