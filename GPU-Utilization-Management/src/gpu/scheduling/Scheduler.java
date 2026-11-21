package gpu.scheduling;
import java.util.*;
import java.time.*;
public class Scheduler {
	
	private Queue<Job> waitingQueue;
    private GPU[] gpus;
    private LocalDateTime simulationTime;
    private Map<String, TimeAllowance> studentAllowances;
    private ArrayList<Job> allJobs;
    private int nextJobId;
    
    public Scheduler(LocalDateTime startTime) {
        waitingQueue = new LinkedList<>();
        gpus = new GPU[4];
        for (int i = 0; i < gpus.length; i++) {
            gpus[i] = new GPU(i + 1);
        }
        simulationTime = startTime;
        studentAllowances = new HashMap<>();
        allJobs = new ArrayList<>();
        nextJobId = 1;
    }
    
    public ArrayList<Job> getAllJobs() {
        return new ArrayList<>(allJobs);
    }
    
    public GPU[] getGPUs() {
        return gpus.clone();
    }
    
    public int getWaitingCount() {
        return waitingQueue.size();
    }

    public LocalDateTime getSimulationTime() {
        return simulationTime;
    }
    
    public Job submitJob(String studentId, String title, String applicationName, String description, TaskSize size) {
    	
    	if (studentId == null || studentId.isBlank()|| size == null) {
            return null;
        }

        TimeAllowance allowance = studentAllowances.get(studentId);

        if (allowance == null) {
            return null;
        }

        int duration = size.getDuration();

        if (!allowance.reserve(duration)) {
            return null;
        }

        Job job = new Job(nextJobId,studentId,title,applicationName,description,size,simulationTime);

        nextJobId++;

        waitingQueue.add(job);
        allJobs.add(job);

        assignWaitingJobs();

        return job;
    	
    }
    
    public void registerAllowance(String studentId,TimeAllowance allowance) {
        
    	if (studentId == null || allowance == null) {
            return;
        }

        TimeAllowance existing = studentAllowances.get(studentId);

        if (existing != null && existing != allowance && existing.getReservedMinutes() > 0) {
        	
            throw new IllegalStateException("Student has active reservations");
        }
    	
    	studentAllowances.put(studentId, allowance);
    }

    public boolean addJob(Job job) {

        if (job == null || job.getStatus() != JobStatus.QUEUED) {
            return false;
        }

        for (Job existing : allJobs) {
            if (existing == job || existing.getJobId() == job.getJobId()) {
                return false;
            }
        }

        TimeAllowance allowance = studentAllowances.get(job.getStudentId());

        if (allowance == null || !allowance.reserve(job.getDuration())) {
            return false;
        }

        waitingQueue.add(job);
        allJobs.add(job);

        if (job.getJobId() >= nextJobId) {
            nextJobId = job.getJobId() + 1;
        }

        return true;
    }


    public void assignWaitingJobs() {

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

    public void advanceTime(int min) {

        if (min <= 0) {
            return;
        }
        assignWaitingJobs();
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

    public int predictWaitingTime(Job targetJob) {

        if (targetJob == null) {
            return -1;
        }

        int[] availableAt = new int[gpus.length];

        for (int i = 0; i < gpus.length; i++) {

            Job running = gpus[i].getCurrentJob();

            if (running != null) {

                LocalDateTime finish = running.getStartTime().plusMinutes(running.getDuration());

                availableAt[i] = (int) Math.max(0,Duration.between(simulationTime, finish).toMinutes());
            }
        }

        for (Job waiting : waitingQueue) {

            int earliestGPU = 0;

            for (int i = 1; i < availableAt.length; i++) {

                if (availableAt[i] < availableAt[earliestGPU]) {
                    earliestGPU = i;
                }
            }

            int wait = availableAt[earliestGPU];

            if (waiting == targetJob) {
                return wait;
            }
            availableAt[earliestGPU] +=waiting.getDuration();
        }

        return -1;
    }
    
    public boolean cancelJob(int jobId, String cancelledBy, String reason) {
    	
    	Iterator<Job> iterator = waitingQueue.iterator();

        while (iterator.hasNext()) {

            Job job = iterator.next();

            if (job.getJobId() == jobId) {

                TimeAllowance allowance = studentAllowances.get(job.getStudentId());

                if (allowance == null) {
                    return false;
                }

                allowance.releaseReservation(job.getDuration());

                iterator.remove();

                job.cancelJob(simulationTime, cancelledBy, reason);

                return true;
            }
        }
    	
    	for (GPU gpu : gpus) {

            Job job = gpu.getCurrentJob();

            if (job != null && job.getJobId() == jobId) {

                TimeAllowance allowance = studentAllowances.get(job.getStudentId());

                if (allowance == null) {
                    return false;
                }

                int elapsed = (int) Duration.between(job.getStartTime(), simulationTime).toMinutes();

                elapsed = Math.max(0,Math.min(elapsed, job.getDuration()));

                allowance.cancelRunningJob(job.getDuration(), elapsed);

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

        return -1;
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

	
}
