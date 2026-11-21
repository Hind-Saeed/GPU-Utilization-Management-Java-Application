package gpumanagement;

public class Student extends User {
    private final TimeAllowance allowance;

    public Student(String universityId, String name, String email, String phone, String password, TimeAllowance allowance) {
        super(universityId, name, email, phone, password);
        this.allowance = allowance;
    }

    public String getUniversityId() {
        return getUsername();
    }

    public TimeAllowance getAllowance() {
        return allowance;
    }

    @Override
    public String getRole() {
        return "Student";
    }
}