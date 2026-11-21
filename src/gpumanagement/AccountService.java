package gpumanagement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AccountService {
    private final Map<String, Student> students = new LinkedHashMap<>();
    private final List<AccessRequest> requests = new ArrayList<>();
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

        AccessRequest request = new AccessRequest(
                id, name.trim(), email.trim(), phone.trim(), password);
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
            throw new IllegalArgumentException(
                    "Allocated minutes cannot be negative.");
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
        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
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
}