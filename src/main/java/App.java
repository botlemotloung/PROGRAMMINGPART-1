package com.mycompany.prog;

import javax.swing.JOptionPane;

public class App {
    public static void main(String[] args) {
        // Sample baseline runner to ensure the application starts without crashing
        Login testUser = new Login("kyl_1", "Ktp@100!", "+27834557896", "Kyle", "Walker");
        
        String registrationStatus = testUser.registerUser();
        JOptionPane.showMessageDialog(null, registrationStatus);
        
        boolean loginSuccess = testUser.loginUser("kyl_1", "Ktp@100!");
        String loginStatus = testUser.returnLoginStatus(loginSuccess);
        
        JOptionPane.showMessageDialog(null, loginStatus);
        
        System.exit(0); // Explicit clean exit code 0 to tell Maven everything succeeded
    }
}
