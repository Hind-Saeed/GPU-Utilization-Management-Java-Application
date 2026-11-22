package gpumanagement;

public class GPU {
	private int gpuId;
	private Job currentJob;
	
	public GPU(int gpuId) {
		this.gpuId = gpuId;
		this.currentJob = null;
	}
	
	public int getGPUId() {
		return gpuId;
	}
	
	public Job getCurrentJob() {
		return currentJob;
	}
	
	public boolean isAvailable() {
		return currentJob == null;
	}
	
	public boolean assignJob(Job job) {
		if (!isAvailable()||job==null) {
			return false;
		}
		currentJob = job;
		return true;
	}
	
	public void releaseJob() {
		currentJob = null;
	}
	
	@Override
	public String toString() {
		if(isAvailable()) {
			return "GPU "+gpuId+": Available";
		}
		return "GPU "+gpuId+": Running Job "+currentJob.getJobId();
	}

}