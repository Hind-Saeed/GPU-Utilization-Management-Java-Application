package gpumanagement;
import java.util.*;
import java.time.*;

public class Scheduler {
	
	private Queue<Job> waitingQueue;
    private GPU[] gpus;
    private LocalDateTime simulationTime;
    private Map<String, TimeAllowance> studentAllowances;
    private ArrayList<Job> allJobs;
    private int nextJobId;
    private LocalDateTime initialSimulationTime;

	//added these two constructors
    public Scheduler(LocalDateTime startTime) { 
        this(startTime, startTime);
    }
    public Scheduler(LocalDateTime currentTime,LocalDateTime originalStartTime) {
        waitingQueue = new LinkedList<>();
        gpus = new GPU[4];

        for (int i = 0; i < gpus.length; i++) {
            gpus[i] = new GPU(i + 1);
        }

        simulationTime = currentTime;
        initialSimulationTime = originalStartTime;
        studentAllowances = new HashMap<>();
        allJobs = new ArrayList<>();
        nextJobId = 1;
    }
    //provide complete job history
    public ArrayList<Job> getAllJobs() {
        return new ArrayList<>(allJobs);
    }
    //provide access to the 4 GPUs
    public GPU[] getGPUs() {
        return gpus.clone();
    }
    //return the number of queued jobs
    public int getWaitingCount() {
        return waitingQueue.size();
    }
    //reports the current simulation clock
    public LocalDateTime getSimulationTime() {
        return simulationTime;
    }

    public Job submitJob(String studentId, String title, String applicationName, String description, TaskSize size) {
    	
    	if (studentId == null || studentId.isBlank()
    	        || title == null || title.isBlank()
    	        || applicationName == null || applicationName.isBlank()
    	        || description == null || description.isBlank()
    	        || size == null) {               //checks if the required info is not provided

    	    return null;
    	}

        TimeAllowance allowance = studentAllowances.get(studentId);        //return the allowance connected to the studentId

        if (allowance == null) {
            return null;
        }

        int duration = size.getDuration();

        if (!allowance.reserve(duration)) {     //check if there's enough allowance for the requested duration before creating the job
            return null;
        }

        Job job = new Job(nextJobId,studentId,title,applicationName,description,size,simulationTime);

        nextJobId++;

        waitingQueue.add(job);
        sortWaitingQueue();
        allJobs.add(job);

        assignWaitingJobs();    //check if any of the 4 GPUs are available and assign if one is available otherwise it stays in the queue

        return job;         //return job to StudentFrame
    	
    }
    
    private void sortWaitingQueue() {  //sort the queue according to the submission time, if 2 has the same time then compare by Id
    	
    	ArrayList<Job> sorted = new ArrayList<>(waitingQueue);

        for (int i = 0; i < sorted.size(); i++) {

            for (int j = i + 1; j < sorted.size(); j++) {

                Job first = sorted.get(i);
                Job second = sorted.get(j);

                if (second.getSubmissionTime().isBefore(first.getSubmissionTime())||
                   (second.getSubmissionTime().isEqual(first.getSubmissionTime())
                    && second.getJobId() < first.getJobId())) {

                    sorted.set(i, second);
                    sorted.set(j, first);
                }
            }
        }

        waitingQueue.clear();
        waitingQueue.addAll(sorted);
        
    }

    
    public void registerAllowance(String studentId,TimeAllowance allowance) {        //register the student allowance passed from AdminFrame
        
    	if (studentId == null || allowance == null) {
            return;
        }

        TimeAllowance existing = studentAllowances.get(studentId);

        if (existing != null && existing != allowance && existing.getReservedMinutes() > 0) {
        	
            throw new IllegalStateException("Student has active reservations");
        }
    	
    	studentAllowances.put(studentId, allowance);
    }
    
    //allows the Scheduler to accept an existing queued Job object, validate it, reserve its GPU minutes, and add it to the scheduling system.
    
    public boolean addJob(Job job) {

        if (job == null || job.getStatus() != JobStatus.QUEUED) {
            return false;
        }

        for (Job existing : allJobs) {     //checking for duplicates
            if (existing == job || existing.getJobId() == job.getJobId()) {
                return false;
            }
        }

        TimeAllowance allowance = studentAllowances.get(job.getStudentId());

        if (allowance == null || !allowance.reserve(job.getDuration())) {
            return false;
        }

        waitingQueue.add(job);
        sortWaitingQueue();
        allJobs.add(job);

        if (job.getJobId() >= nextJobId) {
            nextJobId = job.getJobId() + 1;
        }

        return true;
    }


    public void assignWaitingJobs() {        //assign a job from the queue when a GPU becomes available

        for (GPU gpu : gpus) {

            if (waitingQueue.isEmpty()) {
                break;
            }

            if (gpu.isAvailable()) {

                Job job = waitingQueue.peek();

                if (gpu.assignJob(job)) {

                    waitingQueue.poll();

                    job.startJob(gpu.getGPUId(),simulationTime);
                }
            }
        }
    }

    //advance the simulation time, check which jobs finish, update the allowances, release the GPUs, start waiting jobs
    public void advanceTime(int min) { 

        if (min <= 0) {
            return;
        }
        assignWaitingJobs();  //assign before advancing if there's an available GPU
        LocalDateTime targetTime = simulationTime.plusMinutes(min);

        while (true) {

            LocalDateTime nextFinish = null;

            for (GPU gpu : gpus) {

                Job job = gpu.getCurrentJob();

                if (job != null) {

                    LocalDateTime finish = job.getStartTime().plusMinutes(job.getDuration());

                    if (nextFinish == null || finish.isBefore(nextFinish)) {
                        nextFinish = finish;
                    }
                }
            }

            if (nextFinish == null || nextFinish.isAfter(targetTime)) {
                break;
            }

            simulationTime = nextFinish;

            for (GPU gpu : gpus) {

                Job job = gpu.getCurrentJob();

                if (job != null) {

                    LocalDateTime finish = job.getStartTime().plusMinutes(job.getDuration());

                    if (!finish.isAfter(simulationTime)) {

                        TimeAllowance allowance = studentAllowances.get(job.getStudentId());

                        if (allowance == null) {
                            throw new IllegalStateException("Allowance not found for job "+ job.getJobId());
                        }

                        allowance.completeJob(job.getDuration());

                        job.completeJob(simulationTime);
                        gpu.releaseJob();
                    }
                }
            }

            assignWaitingJobs();
        }

        simulationTime = targetTime;
        assignWaitingJobs();
    }

    //predict the waiting time after submitting a job and got accepted
    public int predictWaitingTime(Job targetJob) {

        if (targetJob == null) {
            return -1;
        }

        int[] availableAt = new int[gpus.length];         //Stores how many minutes until each GPU becomes available.

        for (int i = 0; i < gpus.length; i++) {
            availableAt[i] = getRemainingMinutes(gpus[i].getGPUId());
        }

        for (Job waiting : waitingQueue) {

            int earliestGPU = 0;

            for (int i = 1; i < availableAt.length; i++) {

                if (availableAt[i] < availableAt[earliestGPU]) {    //compare the availability times of the 4 GPUs
                    earliestGPU = i;
                }
            }

            int wait = availableAt[earliestGPU];         //waiting time before the job can start

            if (waiting == targetJob) {
                return wait;
            }
            availableAt[earliestGPU] +=waiting.getDuration();    
        }

        return -1;
    }
    
    public boolean cancelJob(int jobId, String cancelledBy, String reason) {
    	
    	Iterator<Job> iterator = waitingQueue.iterator();     //visit each job in the waiting queue in order

        while (iterator.hasNext()) {

            Job job = iterator.next();

            if (job.getJobId() == jobId) {         //check if the current job id in the iterator = the requested job id

                TimeAllowance allowance = studentAllowances.get(job.getStudentId());    //get the allowance of the student who requested the job

                if (allowance == null) {
                    return false;
                }

                allowance.releaseReservation(job.getDuration());       //release reserved minutes without charging the student

                iterator.remove();     //remove the job from the waiting queue

                job.cancelJob(simulationTime, cancelledBy, reason);     //record the cancellation and update

                return true;
            }
        }
    	
        //Canceling a running job
    	for (GPU gpu : gpus) {

            Job job = gpu.getCurrentJob();

            if (job != null && job.getJobId() == jobId) {

                TimeAllowance allowance = studentAllowances.get(job.getStudentId());

                if (allowance == null) {
                    return false;
                }

                int elapsed = (int) Duration.between(job.getStartTime(), simulationTime).toMinutes(); //how many minutes were already done

                elapsed = Math.max(0,Math.min(elapsed, job.getDuration()));    

                allowance.cancelRunningJob(job.getDuration(), elapsed);     //update the allowance through TimeAllowance

                job.cancelJob(simulationTime, cancelledBy, reason);

                gpu.releaseJob();

                assignWaitingJobs();

                return true;
            }
        }
        return false;
    }
    
    public int getRemainingMinutes(int gpuId) {

        for (GPU gpu : gpus) {

            if (gpu.getGPUId() == gpuId) {

                Job job = gpu.getCurrentJob();

                if (job == null) {
                    return 0;
                }

                LocalDateTime finish = job.getStartTime().plusMinutes(job.getDuration());

                return (int) Math.max(0,Duration.between(simulationTime, finish).toMinutes());
            }
        }

        return -1;  //if the GPU id is invalid
    }
    
    public LocalDateTime getExpectedFinishTime(int gpuId) {

        for (GPU gpu : gpus) {

            if (gpu.getGPUId() == gpuId) {

                Job job = gpu.getCurrentJob();

                if (job == null) {
                    return null;
                }

                return job.getStartTime().plusMinutes(job.getDuration());
            }
        }

        return null;
    }
    
 // Calculates the percentage of available GPU time spent executing jobs.
    public double getUtilization() {

        // Calculate how many minutes have passed since the simulation started.
        long elapsed = Duration.between(
            initialSimulationTime, simulationTime
        ).toMinutes();

        if (elapsed <= 0) {
            return Double.NaN;
        }

        long busyMinutes = 0;     //the total execution time of every job that has started

        for (Job job : allJobs) {
            if (job.getStartTime() == null) {
                continue;
            }

         // Stores the end of the execution interval being counted.
            LocalDateTime end = null;

            if (job.getStatus() == JobStatus.RUNNING) {
                end = simulationTime;
            } else if (job.getStatus() == JobStatus.COMPLETED) {
                end = job.getFinishTime();
            } else if (job.getStatus() == JobStatus.CANCELLED) {
                end = job.getCancellationTime(); //use cancellationTime not finished time
            }

            if (end != null) {

                long jobMinutes =
                    Duration.between(job.getStartTime(), end).toMinutes();

                // Add this job's non-negative execution time to the total.
                busyMinutes += Math.max(0, jobMinutes);
            }
        }

        return busyMinutes * 100.0 / (4.0 * elapsed);
    }
    
    public LocalDateTime getPredictedStartTime(Job job) {

        if (job == null) {
            return null;
        }

        if (job.getStartTime() != null) {    //job already started
            return job.getStartTime();
        }

        int wait = predictWaitingTime(job);

        if (wait == -1) {
            return null;
        }

        return simulationTime.plusMinutes(wait);     //prediction for a job in the queue
    }
    
    public LocalDateTime getPredictedFinishTime(Job job) {

        if (job == null) {
            return null;
        }

        if (job.getFinishTime() != null) {
            return job.getFinishTime();
        }

        if (job.getStatus() == JobStatus.CANCELLED) {
            return null;
        }

        LocalDateTime start = getPredictedStartTime(job);

        if (start == null) {
            return null;
        }

        return start.plusMinutes(job.getDuration());
    }

    // Restore saved job history after restarting the application.
    public boolean restoreSavedJob(
            Job job,
            JobStatus savedStatus,
            Integer gpuId,
            LocalDateTime startTime,
            LocalDateTime finishTime,
            LocalDateTime cancellationTime,
            String cancelledBy,
            String reason) {

        if (job == null || savedStatus == null) {
            return false;
        }

        for (Job savedJob : allJobs) {      //reject duplicates
            if (savedJob.getJobId() == job.getJobId()) {
                return false;
            }
        }

        if (savedStatus == JobStatus.QUEUED) {      //restore queued jobs 
            waitingQueue.add(job);
            sortWaitingQueue();
        } 
        else if (savedStatus == JobStatus.RUNNING) {      //restore running jobs
            if (gpuId == null || gpuId < 1 || gpuId > gpus.length || !gpus[gpuId - 1].isAvailable() || startTime == null) {
                return false;
            }

            job.startJob(gpuId, startTime);
            gpus[gpuId - 1].assignJob(job);
        }
        else if (savedStatus == JobStatus.COMPLETED) {       //restore completed jobs
            if (gpuId == null || startTime == null || finishTime == null) {
                return false;
            }

            job.startJob(gpuId, startTime);
            job.completeJob(finishTime);
        }
        else if (savedStatus == JobStatus.CANCELLED) {       //restore cancelled jobs
            if (startTime != null && gpuId != null) {
                job.startJob(gpuId, startTime);
            }
            job.cancelJob(cancellationTime, cancelledBy, reason);
        }

        allJobs.add(job);         //preserve the restored job in the history

        if (job.getJobId() >= nextJobId) {      //prevent duplicates after restarting
            nextJobId = job.getJobId() + 1;
        }

        return true;
    }
}
