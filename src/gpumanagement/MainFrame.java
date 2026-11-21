package gpumanagement;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import gpu.scheduling.Scheduler;

public class MainFrame extends ScreenFormat implements ActionListener {
	
    // Login fields
    private JTextField usernameField, passwordField;
    private JButton login, register, back, submitRequest;
    
    // student registration
    private JTextField id, name, email, phone, password;
    
    protected static AccountService accountService = new AccountService();
    protected static ArrayList<Student> approvedStudents = new ArrayList<>();
    protected static Scheduler scheduler;
    
    // default constructor to set title and size
    public MainFrame() {
        setTitle("GPU Utilization Management");
        setSize(1300, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        loginPage();
        setVisible(true);
    }
    
    // log in screen format
    public void loginPage() {
        clearScreen(4, 2);
        
        // initialize variables
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        login = makeButton("Log in");
        register = makeButton("Register");
        
        // title and subtitle
        JLabel title = new JLabel("GPU UTILIZATION MANAGEMENT", JLabel.CENTER);
        title.setBackground(Color.BLACK);
        title.setForeground(Color.WHITE);
        title.setOpaque(true);
       
        JLabel subtitle = new JLabel("Sign in to continue", JLabel.CENTER);
        subtitle.setBackground(Color.GRAY);
        subtitle.setForeground(Color.WHITE);
        subtitle.setOpaque(true);
        add(title);
        add(subtitle);

        // add to labels and text fields 
        add(new JLabel("University ID or admin username:"));
        add(usernameField);
        add(new JLabel("Password:"));
        add(passwordField);
        add(login);
        add(register);
        
        login.addActionListener(this);
        register.addActionListener(this);
        refreshScreen();
    }
    
    // if register is clicked
    public void registerPage() {
        clearScreen(7, 2);
        
        // initialize variables
        id = new JTextField();
        name = new JTextField();
        email = new JTextField();
        phone = new JTextField();
        password = new JPasswordField();
        submitRequest = makeButton("Submit registration request");
        back = makeButton("Back to login");
        
        //title and subtitle
        JLabel title = new JLabel("STUDENT REGISTRATION", JLabel.CENTER);
        title.setBackground(Color.BLACK);
        title.setForeground(Color.WHITE);
        title.setOpaque(true);
        JLabel subtitle = new JLabel("Submit an account request", JLabel.CENTER);
        subtitle.setBackground(Color.GRAY);
        subtitle.setForeground(Color.WHITE);
        subtitle.setOpaque(true);

        // add to page
        add(title);
        add(subtitle);
        
        add(new JLabel("University ID:"));
        add(id);
        
        add(new JLabel("Name:"));
        add(name);
        
        add(new JLabel("Email:"));
        add(email);
        
        add(new JLabel("Phone number:"));
        add(phone);
        
        add(new JLabel("Choose a password:"));
        add(password);
        
        add(submitRequest);
        add(back);
        submitRequest.addActionListener(this);
        back.addActionListener(this);

        refreshScreen();
    }
    
    // Connect with member 1 (user)
    public Student studentLogin(String universityID, String password) {
        for (Student s : approvedStudents) {
            if (s.getUniversityId().equals(universityID) && s.passwordMatches(password)) {
                return s;
            }
        }
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    	if (e.getSource() == login) {
    	    String username = usernameField.getText();
    	    String password = passwordField.getText();
    	    Student loggedIn = studentLogin(username, password);   // null if no match

    	    if (username.equals("admin") && password.equals("gpu2026")) {
    	    	new AdminFrame(scheduler, accountService, accountService.login(username, password));
    	        dispose();
    	    }
    	    else if (loggedIn != null) {
    	        new StudentFrame(loggedIn);
    	        dispose();
    	    }
    	    else if (username.equals("student") && password.equals("1234")) {
    	        // demo Student to allow updates.
    	    	Student demoStudent = new Student("S001", "Demo Student", "demo@example.com","0500000000","demoPassword", new TimeAllowance(100));

    	        new StudentFrame(demoStudent);
    	        dispose();
    	    }
    	    else {
    	        JOptionPane.showMessageDialog(this, "Invalid username/password or account not approved.");
    	    }
    	} 
        else if (e.getSource() == register) {
            registerPage();

        } 
        else if (e.getSource() == back) {
            loginPage();
        } 
        else if (e.getSource() == submitRequest) {
            String enteredId = id.getText();
            String enteredName = name.getText();
            String enteredEmail = email.getText();
            String enteredPhone = phone.getText();
            String enteredPassword = password.getText();
            
            // if a field is left empty
            if (enteredId.isEmpty() || enteredName.isEmpty()|| enteredEmail.isEmpty() || enteredPhone.isEmpty()
                    || enteredPassword.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fill all fields");
            } 
            else {
                // Access Request class from member 1
                JOptionPane.showMessageDialog(this, "Access from member1.");
            }
        }
    }
}
