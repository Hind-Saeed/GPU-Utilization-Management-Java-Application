package gpumanagement;

public enum TaskSize {
	
	SMALL(5),
    MEDIUM(15),
    LARGE(30),
    VERY_LARGE(60);

    private final int duration;

    TaskSize(int duration) {
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

}
