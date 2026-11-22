package gpumanagement;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Main {
	public static void main(String[] args) {
		loadSavedData(); //restore data from saved file
		new MainFrame();
	}

	private static void loadSavedData() {
		Storage storage = new Storage();
		MainFrame.accountService = new AccountService();
		MainFrame.scheduler = null;
		MainFrame.simulationStart = null;

		String[][] allowanceRows = storage.loadAllowance();
		Map<String, TimeAllowance> allowances = new HashMap<>(); //link student to their allowance
		for (int i = 0; i < allowanceRows.length; i++) {
			try {
				allowances.put(allowanceRows[i][0], new TimeAllowance(
						Integer.parseInt(allowanceRows[i][1]),
						Integer.parseInt(allowanceRows[i][2]),
						Integer.parseInt(allowanceRows[i][3])));
			} catch (Exception ex) {
				System.out.println("Skipped an invalid saved allowance.");
			}
		}

		String[][] accountRows = storage.loadAccount();
		for (int i = 0; i < accountRows.length; i++) {
			try {
				String id = accountRows[i][0];
				TimeAllowance allowance = allowances.get(id);
				if (allowance == null) allowance = new TimeAllowance(100); // default value
				Student student = new Student(id, accountRows[i][1], accountRows[i][2],accountRows[i][3], accountRows[i][4], allowance);
				MainFrame.accountService.restoreStudent(student);
			} catch (Exception ex) {
				System.out.println("Skipped an invalid saved student account.");
			}
		}

		String[][] requestRows = storage.loadRequests();
		for (int i = 0; i < requestRows.length; i++) {
			try {
				AccessRequest request = new AccessRequest(requestRows[i][0], requestRows[i][1],
						requestRows[i][2], requestRows[i][3], requestRows[i][4]);
				MainFrame.accountService.restoreRequest(request,AccessRequest.Status.valueOf(requestRows[i][5]));
			} catch (Exception ex) {
				System.out.println("Skipped an invalid saved registration request.");
			}
		}

		String savedClock = storage.loadClock();
		String startText = storage.loadSimulationStart();
		
		if (!savedClock.equals("Not set") && !savedClock.equals("Clock not set")) {
		    try {
		        LocalDateTime currentTime = LocalDateTime.parse(savedClock);
		        
		        MainFrame.simulationStart = startText.equals("Not set") ? currentTime : LocalDateTime.parse(startText);
		        MainFrame.scheduler = new Scheduler(currentTime, MainFrame.simulationStart);

		        for (Student student : MainFrame.accountService.getStudents(new Administrator())) {
		            MainFrame.scheduler.registerAllowance(student.getUniversityId(), student.getAllowance());
		        }

		        restoreJobs(storage);
		    } catch (Exception ex) {
		        System.out.println("Could not restore the simulation clock: " + ex.getMessage());
		        MainFrame.scheduler = null;
		        MainFrame.simulationStart = null;
		    }
		}
	}
	
	private static void restoreJobs(Storage storage) {
	    String[][] rows = storage.loadJobs();
	    String[][] gpuRows = storage.loadGpuAssignments();

	    Map<Integer, Integer> gpuForJob = new HashMap<>();
	    for (int i = 0; i < gpuRows.length; i++) {
	        try {
	            int gpuId = Integer.parseInt(gpuRows[i][0].trim());
	            int jobId = Integer.parseInt(gpuRows[i][1].trim());
	            gpuForJob.put(jobId, gpuId);
	        } catch (Exception ex) {
	            System.out.println("Skipped an invalid GPU assignment.");
	        }
	    }

	    for (int i = 0; i < rows.length; i++) {
	        try {
	            int jobId = Integer.parseInt(rows[i][0].trim());
	            String studentId = rows[i][1].trim();
	            String title = rows[i][2];
	            String application = rows[i][3];
	            String description = rows[i][4];
	            TaskSize size = TaskSize.valueOf(rows[i][5].trim());
	            LocalDateTime submitted = LocalDateTime.parse(rows[i][6].trim());
	            JobStatus status = JobStatus.valueOf(rows[i][7].trim());

	            Integer gpuId = null;
	            if (!rows[i][8].trim().isEmpty()) {
	                gpuId = Integer.parseInt(rows[i][8].trim());
	            } else if (gpuForJob.containsKey(jobId)) {
	                gpuId = gpuForJob.get(jobId);
	            }

	            LocalDateTime started = savedTime(rows[i][9]);
	            LocalDateTime finished = savedTime(rows[i][10]);
	            LocalDateTime cancelledAt = savedTime(rows[i][11]);

	            String cancelledBy = rows[i][12].trim();
	            String reason = rows[i][13].trim();

	            Job job = new Job(
	                    jobId, studentId, title, application, description, size, submitted);

	            boolean restored = MainFrame.scheduler.restoreSavedJob(
	                    job, status, gpuId, started, finished,
	                    cancelledAt, cancelledBy, reason);

	            if (!restored) {
	                System.out.println("Could not restore job " + jobId);
	            }
	        } catch (Exception ex) {
	            System.out.println("Skipped an invalid saved job: " + ex.getMessage());
	        }
	    }
	}

	private static LocalDateTime savedTime(String value) {
	    if (value == null || value.trim().isEmpty()) {
	        return null;
	    }
	    return LocalDateTime.parse(value.trim());
	}
}
