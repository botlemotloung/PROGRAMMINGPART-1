package com.mycompany.prog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LoginTest {

    // FIXED: Changed type from LoginTest to Login to match the assignment below
    private Login login;

    @BeforeEach
    public void setUp() {
        // FIX: Initialized the global test user baseline so it is never null!
        login = new Login("kyl_1", "Ktp@100!", "+27834557896", "Kyle", "Walker");
    }

    // ---------- checkUserName ----------

    @Test
    public void testCheckUserName_Correct() {
        Login validUser = new Login("kyl_1", "Ktp@100!", "+27834557896", "Kyle", "Walker");
        assertTrue(validUser.checkUserName());
    }

    @Test
    public void testCheckUserName_Incorrect() {
        // No underscore, and too long
        Login invalidUser = new Login("kyle!!!!!!", "Ktp@100!", "+27834557896", "Kyle", "Walker");
        assertFalse(invalidUser.checkUserName());
    }

    // ---------- checkPasswordComplexity ----------

    @Test
    public void testCheckPasswordComplexity_Correct() {
        Login validUser = new Login("kyl_1", "Ktp@100!", "+27834557896", "Kyle", "Walker");
        assertTrue(validUser.checkPasswordComplexity());
    }

    @Test
    public void testCheckPasswordComplexity_Incorrect() {
        // Too short, no capital, no special character
        Login invalidUser = new Login("kyl_1", "password1", "+27834557896", "Kyle", "Walker");
        assertFalse(invalidUser.checkPasswordComplexity());
    }

    // ---------- checkCellPhoneNumber ----------

    @Test
    public void testCheckCellPhoneNumber_Correct() {
        Login validUser = new Login("kyl_1", "Ktp@100!", "+27834557896", "Kyle", "Walker");
        assertTrue(validUser.checkCellPhoneNumber());
    }

    @Test
    public void testCheckCellPhoneNumber_Incorrect() {
        // Missing international code
        Login invalidUser = new Login("kyl_1", "Ktp@100!", "0834557896", "Kyle", "Walker");
        assertFalse(invalidUser.checkCellPhoneNumber());
    }

    // ---------- registerUser ----------

    @Test
    public void testRegisterUser_Success() {
        Login validUser = new Login("kyl_1", "Ktp@100!", "+27834557896", "Kyle", "Walker");
        assertEquals("User registered successfully.", validUser.registerUser());
    }

    @Test
    public void testRegisterUser_UsernameFail() {
        Login invalidUser = new Login("kyle_walker", "Ktp@100!", "+27834557896", "Kyle", "Walker");
        assertEquals(
            "Username is not correctly formatted; please ensure that your username contains an underscore and is no more than five characters in length.",
            invalidUser.registerUser()
        );
    }

    // ---------- loginUser ----------

    @Test
    public void testLoginUser_Success() {
        assertTrue(login.loginUser("kyl_1", "Ktp@100!"));
    }

    @Test
    public void testLoginUser_Failure() {
        assertFalse(login.loginUser("kyl_1", "wrongPassword1!"));
    }

    // ---------- returnLoginStatus ----------

    @Test
    public void testReturnLoginStatus_Success() {
        assertEquals(
            "Welcome Kyle, Walker it is great to see you again.",
            login.returnLoginStatus(true)
        );
    }

    @Test
    public void testReturnLoginStatus_Failure() {
        assertEquals(
            "Username or password incorrect, please try again.",
            login.returnLoginStatus(false)
        );
    }
}

