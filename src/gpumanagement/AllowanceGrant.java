package gpumanagement;

public class AllowanceGrant {
    private final String studentId;
    private final int minutes;
    private final String reason;
    private final String administratorUsername;

    public AllowanceGrant(String studentId, int minutes, String reason,String administratorUsername) {
        this.studentId = studentId;
        this.minutes = minutes;
        this.reason = reason;
        this.administratorUsername = administratorUsername;
    }

    public String getStudentId() {
        return studentId;
    }

    public int getMinutes() {
        return minutes;
    }

    public String getReason() {
        return reason;
    }

    public String getAdministratorUsername() {
        return administratorUsername;
    }
}