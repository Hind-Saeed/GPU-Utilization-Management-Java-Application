package gpumanagement;

import java.time.*;
import java.time.format.*;
import java.util.*;

//This class reads job records and get data from scheduler.  
// We run it as text rows in AdminFrame and StudentFrame to remove data calculations from those frames' codes.
public class JobRows {
    
	private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static String formatTime(LocalDateTime time) {
        if (time == null) { return "--";}
        return time.format(FORMAT);
    }

    private static long minutesBetween(LocalDateTime start, LocalDateTime end) { //duration
        return Duration.between(start, end).toMinutes();
    }

    public static int count(ArrayList<Job> jobs, JobStatus status) { 
        int total = 0;
        for (Job job : jobs) { // count jobs in each status
            if (job.getStatus() == status) {total++;}
        }
        return total;
    }
    
    public static String[][] gpus(ArrayList<Job> jobs, Scheduler scheduler) { //Build gpu table in AdminFrame
        String[][] rows = new String[4][5];
        for (int i = 0; i < 4; i++) {
            rows[i][0] = "GPU " + (i + 1);
            rows[i][1] = "Available";
            rows[i][2] = "None";
            rows[i][3] = "0 min";
            rows[i][4] = "--";
        }

        if (scheduler == null) { return rows;} 

        LocalDateTime now = scheduler.getSimulationTime();
        for (Job job : jobs) {
            if (job.getStatus() == JobStatus.RUNNING && job.getAssignedGpuId() != null) {
                int row = job.getAssignedGpuId() - 1; //index
                if (row >= 0 && row < 4) {
                    LocalDateTime finish = job.getStartTime().plusMinutes(job.getDuration());
                    rows[row][1] = "Busy";
                    rows[row][2] = "Job " + job.getJobId();
                    rows[row][3] = Math.max(0, minutesBetween(now, finish)) + " min"; //remaining time
                    rows[row][4] = formatTime(finish);
                }
            }
        }

        return rows;
    }

    public static String[][] queued(ArrayList<Job> jobs, Scheduler scheduler) {
        ArrayList<String[]> rows = new ArrayList<>();
        int position = 0;
        for (Job job : jobs) {
            if (job.getStatus() == JobStatus.QUEUED) {
                position++;
                String start = "--";
                String finish = "--";
                String waitText = "--";

                if (scheduler != null) {
                    int wait = scheduler.predictWaitingTime(job);
                    if (wait >= 0) {
                        start = formatTime(scheduler.getPredictedStartTime(job));
                        finish = formatTime(scheduler.getPredictedFinishTime(job));
                        waitText = wait + " min";
                    }
                }
                rows.add(new String[]{"" + job.getJobId(), job.getStudentId(), job.getApplicationName(),
                    job.getDuration() + " min", "" + position, start, finish, waitText});
            }
        }
        return rows.toArray(new String[rows.size()][]);
    }

    public static String[][] completed(ArrayList<Job> jobs) {
        ArrayList<String[]> rows = new ArrayList<>();
        for (Job job : jobs) {
            if (job.getStatus() == JobStatus.COMPLETED) {
                long runtime = minutesBetween(job.getStartTime(), job.getFinishTime());
                long wait = minutesBetween(job.getSubmissionTime(), job.getStartTime());
                rows.add(new String[] {"" + job.getJobId(), job.getStudentId(),"GPU " + job.getAssignedGpuId(),formatTime(job.getSubmissionTime()),
                    formatTime(job.getStartTime()), formatTime(job.getFinishTime()), runtime + " min", wait + " min",runtime + " min"});
            }
        }

        return rows.toArray(new String[rows.size()][]);
    }

    public static String[][] cancelled(ArrayList<Job> jobs) {
        ArrayList<String[]> rows = new ArrayList<>();
        for (Job job : jobs) {
            if (job.getStatus() == JobStatus.CANCELLED) {
                String statusWhenCancelled = "Queued";
                if (job.getStartTime() != null) { //not in the que if we have a starting time
                    statusWhenCancelled = "Running";
                }
                rows.add(new String[] {"" + job.getJobId(), job.getStudentId(), statusWhenCancelled, job.getApplicationName(),job.getCancellationReason()});
            }
        }
        return rows.toArray(new String[rows.size()][]);
    }

    public static String[][] cancellable(ArrayList<Job> jobs, String studentId) {
        ArrayList<String[]> rows = new ArrayList<>();

        for (Job job : jobs) {
            boolean belongsToStudent = job.getStudentId().equals(studentId);

            boolean canBeCancelled = job.getStatus() == JobStatus.QUEUED || job.getStatus() == JobStatus.RUNNING;

            if (belongsToStudent && canBeCancelled) {
                rows.add(new String[] {job.getStudentId(), "" + job.getJobId(), job.getApplicationName(), "" + job.getStatus()});
            }
        }
        return rows.toArray(new String[rows.size()][]);
    }

    public static String[][] studentRows(ArrayList<Job> jobs, String studentId, Scheduler scheduler) {

        ArrayList<String[]> rows = new ArrayList<>();
        for (Job job : jobs) {
            if (!job.getStudentId().equals(studentId)) {
                continue; //job doesn't belong to student
            }
			
            String gpu = "--";
            if (job.getAssignedGpuId() != null) {gpu = "GPU " + job.getAssignedGpuId();}

            String start = formatTime(job.getStartTime());
            String finish = formatTime(job.getFinishTime());
            String wait = "--";
            String result;

            if (job.getStatus() == JobStatus.QUEUED) {
                result = "Queued";
                if (scheduler != null) {
                    int waitMinutes = scheduler.predictWaitingTime(job);
                    if (waitMinutes >= 0) {
                        LocalDateTime predictedStart =scheduler.getSimulationTime().plusMinutes(waitMinutes);
                        start = formatTime(predictedStart);
                        finish = formatTime(predictedStart.plusMinutes(job.getDuration()));
                        wait = waitMinutes + " min";
                    }
                }

            } else if (job.getStatus() == JobStatus.RUNNING) {
                finish = formatTime(job.getStartTime().plusMinutes(job.getDuration()));
                wait = minutesBetween(job.getSubmissionTime(), job.getStartTime()) + " min";
                result = "Running";

            } else if (job.getStatus() == JobStatus.COMPLETED) {
                long runtime = minutesBetween(job.getStartTime(), job.getFinishTime());
                wait = minutesBetween(job.getSubmissionTime(), job.getStartTime()) + " min";
                result = "Completed, charged " + runtime + " min";

            } else { // CANCELLED
                if (job.getStartTime() != null) {
                    wait = minutesBetween(job.getSubmissionTime(), job.getStartTime()) + " min";
                }
                finish = formatTime(job.getCancellationTime());
                result = "Cancelled: " + job.getCancellationReason();
            }

            rows.add(new String[] {"" + job.getJobId(),job.getTitle(),job.getApplicationName(),"" + job.getStatus(),
                gpu,start,finish,wait,result});
        }

        return rows.toArray(new String[rows.size()][]);
    }

    public static String utilization(Scheduler scheduler) {
        if (scheduler == null) {
            return "N/A";
        }
        double percent = scheduler.getUtilization();

        if (Double.isNaN(percent) || Double.isInfinite(percent)) {
            return "N/A";
        }
        return String.format("%.1f%%", percent);
    }
}
