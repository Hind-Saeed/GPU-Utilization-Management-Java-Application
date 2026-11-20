package gpu.scheduling;
import java.util.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
public class Scheduler {
	
	private Queue<Job> waitingQueue;
    private GPU[] gpus;
    private LocalDateTime simulationTime;

    public Scheduler(LocalDateTime startTime) {

        waitingQueue = new LinkedList<>();

        gpus = new GPU[4];

        for (int i = 0; i < gpus.length; i++) {
            gpus[i] = new GPU(i + 1);
        }

        simulationTime = startTime;
    }

    public void addJob(Job job) {

        if (job != null) {
            waitingQueue.add(job);
        }
    }

    public int getWaitingCount() {
        return waitingQueue.size();
    }

    public LocalDateTime getSimulationTime() {
        return simulationTime;
    }
	
    public void assignWaitingJobs() {
    	for (GPU gpu: gpus) {
    		if(waitingQueue.isEmpty()) {
    			break;
    		}
    		
    		if (gpu.isAvailable()) {
    			Job job = waitingQueue.peek();
    			if (gpu.assignJob(job)) {
    				waitingQueue.poll();
    				job.startJob(gpu.getGPUId(), simulationTime);
    			}
    		}
    	}
    }
    
    public void advanceTime(int min) {
    	if(min<=0) {
    		return;
    	}
    	LocalDateTime targetTime = simulationTime.plusMinutes(min);
    	while(true) {
    		LocalDateTime nextFinish = null;
    		for (GPU gpu : gpus) {
    			Job job = gpu.getCurrentJob();
    			if(job!=null) {
    				LocalDateTime finish = job.getStartTime().plusMinutes(job.getDuration());
    				if (nextFinish == null||finish.isBefore(nextFinish)) {
    					nextFinish = finish;
    				}
    			}
    		}
    		if(nextFinish==null||nextFinish.isAfter(targetTime)) {
    			break;
    		}
    		simulationTime = nextFinish;
    		for (GPU gpu:gpus) {
    			Job job = gpu.getCurrentJob();
    			if(job!=null) {
    				LocalDateTime finish = job.getStartTime().plusMinutes(job.getDuration());
    				if (!finish.isAfter(simulationTime)) {
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

        long[] availableAt = new long[gpus.length];

        for (int i = 0; i < gpus.length; i++) {

            Job running = gpus[i].getCurrentJob();

            if (running != null) {

                LocalDateTime finish =running.getStartTime().plusMinutes(running.getDuration());

                availableAt[i] = Math.max(0,ChronoUnit.MINUTES.between(simulationTime, finish));
            }
        }

        for (Job waiting : waitingQueue) {

            int earliestGPU = 0;

            for (int i = 1; i < availableAt.length; i++) {

                if (availableAt[i] < availableAt[earliestGPU]) {
                    earliestGPU = i;
                }
            }

            long wait = availableAt[earliestGPU];

            if (waiting == targetJob) {
                return (int) wait;
            }

            availableAt[earliestGPU] += waiting.getDuration();
        }

        return -1;
    	
    }
	
}
