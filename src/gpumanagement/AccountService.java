package gpumanagement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AccountService {
    private final Map<String, Student> students = new LinkedHashMap<>();
    private final List<AccessRequest> requests = new ArrayList<>();

    public AccessRequest registerStudent(String universityId, String name,
                                         String email, String phone,
                                         String password) {
        requireText(universityId, "University ID");
        requireText(name, "Name");
        requireText(email, "Email");
        requireText(phone, "Phone");
        requireText(password, "Password");

        String id = universityId.trim();

        if (students.containsKey(id) || hasPendingRequest(id)) {
            throw new IllegalArgumentException(
                    "An account or pending request already uses this ID.");
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