package gpumanagement;

public class AccessRequest {
    public enum Status {
        PENDING, APPROVED, REJECTED
    }

    private final String universityId;
    private final String name;
    private final String email;
    private final String phone;
    private final String password;
    private Status status = Status.PENDING;

    public AccessRequest(String universityId, String name, String email,
                         String phone, String password) {
        this.universityId = universityId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    public String getUniversityId() {
        return universityId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public Status getStatus() {
        return status;
    }

    public boolean isPending() {
        return status == Status.PENDING;
    }

    void approve() {
        if (!isPending()) {
            throw new IllegalStateException("Only pending requests can be approved.");
        }
        status = Status.APPROVED;
    }

    void reject() {
        if (!isPending()) {
            throw new IllegalStateException("Only pending requests can be rejected.");
        }
        status = Status.REJECTED;
    }

    String getPasswordForAccountCreation() {
        return password;
    }
}