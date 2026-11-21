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

     public TimeAllowance(int allocated, int used, int reserved) {
        if (allocated < 0 || used < 0 || reserved < 0 || used + reserved > allocated) {
            throw new IllegalArgumentException("Invalid saved allowance values.");
        }
        allocatedMinutes = allocated;
        usedMinutes = used;
        reservedMinutes = reserved;
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
    public boolean reserve(int minutes) {
        if (minutes <= 0 || minutes > getAvailableMinutes()) {
            return false;
        }

        reservedMinutes += minutes;
        return true;
    }

    public void releaseReservation(int minutes) {
        requireReservation(minutes);
        reservedMinutes -= minutes;
    }

    public void completeJob(int reservedDuration) {
        requireReservation(reservedDuration);
        reservedMinutes -= reservedDuration;
        usedMinutes += reservedDuration;
    }

    public void cancelRunningJob(int reservedDuration, int elapsedMinutes) {
        requireReservation(reservedDuration);

        if (elapsedMinutes < 0 || elapsedMinutes > reservedDuration) {
            throw new IllegalArgumentException(
                    "Elapsed minutes must be between zero and the reservation.");
        }
        reservedMinutes -= reservedDuration;
        usedMinutes += elapsedMinutes;
    }

    public void grantMinutes(int minutes) {
        if (minutes <= 0) {
            throw new IllegalArgumentException("Granted minutes must be positive.");
        }
        allocatedMinutes += minutes;
    }

    private void requireReservation(int minutes) {
        if (minutes <= 0 || minutes > reservedMinutes) {
            throw new IllegalArgumentException("There is not enough reserved time for this operation.");
        }
    }
}
