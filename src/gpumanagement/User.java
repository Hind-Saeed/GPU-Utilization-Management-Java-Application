package gpumanagement;

public abstract class User {
	 private final String username;
	    private final String name;
	    private final String email;
	    private final String phone;
	    private final String password;

	    protected User(String username, String name, String email, String phone, String password) {
	        this.username = username;
	        this.name = name;
	        this.email = email;
	        this.phone = phone;
	        this.password = password;
	    }

	    public String getUsername() {
	        return username;
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

	    public boolean passwordMatches(String candidate) {
	        return candidate != null && password.equals(candidate);
	    }

	    public abstract String getRole();

}
