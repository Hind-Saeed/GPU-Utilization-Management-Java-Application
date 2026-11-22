package gpumanagement;
import java.util.*;

public class AccountService {
    private final Map<String, Student> students = new LinkedHashMap<>();
    private final List<AccessRequest> requests = new ArrayList<>();
    private final List<AllowanceGrant> allowanceGrants = new ArrayList<>();
    private final Administrator administrator = new Administrator();

    public AccessRequest registerStudent(String universityId, String name,String email, String phone, String password) {
        requireText(universityId, "University ID");
        requireText(name, "Name");
        requireText(email, "Email");
        requireText(phone, "Phone");
        requireText(password, "Password");

        String id = universityId.trim();

        if (students.containsKey(id) || hasPendingRequest(id)) {
            throw new IllegalArgumentException("An account or pending request already uses this ID.");
        }

        AccessRequest request = new AccessRequest(id, name.trim(), email.trim(), phone.trim(), password);
        requests.add(request);
        return request;
    }

    public List<AccessRequest> getPendingRequests() {
        List<AccessRequest> pending = new ArrayList<>();

        for (AccessRequest request : requests) {
            if (request.isPending()) {
                pending.add(request);
            }
        }

        return Collections.unmodifiableList(pending);
    }
    public Student approveRequest(String universityId) {
        return approveRequest(universityId, 100);
    }

    public Student approveRequest(String universityId, int allocatedMinutes) {
        requireText(universityId, "University ID");

        if (allocatedMinutes < 0) {
            throw new IllegalArgumentException("Allocated minutes cannot be negative.");
        }

        String id = universityId.trim();
        AccessRequest request = findPendingRequest(id);
        Student student = new Student(
                request.getUniversityId(),
                request.getName(),
                request.getEmail(),
                request.getPhone(),
                request.getPasswordForAccountCreation(),
                new TimeAllowance(allocatedMinutes)
        );

        request.approve();
        students.put(id, student);
        return student;
    }

    public void rejectRequest(String universityId) {
        requireText(universityId, "University ID");
        AccessRequest request = findPendingRequest(universityId.trim());
        request.reject();
    }

    private AccessRequest findPendingRequest(String universityId) {
        for (AccessRequest request : requests) {
            if (request.getUniversityId().equals(universityId)
                    && request.isPending()) {
                return request;
            }
        }

        throw new IllegalArgumentException(
                "No pending request exists for this university ID.");
    }
    public User login(String username, String password) {
        if (username == null || username.isBlank()|| password == null || password.isBlank()) {
            return null;
        }

        String id = username.trim();

        if (administrator.getUsername().equals(id)
                && administrator.passwordMatches(password)) {
            return administrator;
        }

        Student student = students.get(id);

        if (student != null && student.passwordMatches(password)) {
            return student;
        }

        return null;
    }
    public void grantAdditionalTime(String studentId, int minutes, String reason, User grantedBy) {
    	if (!(grantedBy instanceof Administrator)) {
            throw new SecurityException("Only an administrator can grant time.");
        }

        requireText(studentId, "Student ID");
        requireText(reason, "Reason");

        if (minutes <= 0) {
            throw new IllegalArgumentException("Granted minutes must be positive.");
        }

        String id = studentId.trim();
        Student student = students.get(id);

        if (student == null) {
            throw new IllegalArgumentException("No approved student exists with this ID.");
        }

        student.getAllowance().grantMinutes(minutes);

        allowanceGrants.add(new AllowanceGrant(id, minutes, reason.trim(), grantedBy.getUsername()));
    }

    public List<AllowanceGrant> getAllowanceGrants() {
        return Collections.unmodifiableList(new ArrayList<>(allowanceGrants));
    }
    public List<Student> getStudents(User requestedBy) {
        if (!(requestedBy instanceof Administrator)) {
            throw new SecurityException("Only an administrator can view students.");
        }

        return Collections.unmodifiableList(
                new ArrayList<>(students.values()));
    }
    private boolean hasPendingRequest(String universityId) {
        for (AccessRequest request : requests) {
            if (request.isPending()
                    && request.getUniversityId().equals(universityId)) {
                return true;
            }
        }
        return false;
    }
    
    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
    }
    
    public List<AccessRequest> getAllRequests() {
        return Collections.unmodifiableList(new ArrayList<>(requests));
    }
    public void restoreStudent(Student student) {
        students.put(student.getUniversityId(), student);
    }

    public void restoreRequest(AccessRequest request, AccessRequest.Status status) {
        request.restoreStatus(status);
        requests.add(request);
    }
    public void restoreAllowanceGrant(AllowanceGrant grant) {
        allowanceGrants.add(grant);
    }
}
