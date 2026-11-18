package gpumanagement;

public class TimeAllowance {
    private int allocatedMinutes;
    private int usedMinutes;
    private int reservedMinutes;

    public TimeAllowance(int allocatedMinutes) {
        if (allocatedMinutes < 0) {
            throw new IllegalArgumentException("Allocated minutes cannot be negative.");
        }
        this.allocatedMinutes = allocatedMinutes;
    }

    public int getAllocatedMinutes() {
        return allocatedMinutes;
    }

    public int getUsedMinutes() {
        return usedMinutes;
    }

    public int getReservedMinutes() {
        return reservedMinutes;
    }

    public int getAvailableMinutes() {
        return allocatedMinutes - usedMinutes - reservedMinutes;
    }
}