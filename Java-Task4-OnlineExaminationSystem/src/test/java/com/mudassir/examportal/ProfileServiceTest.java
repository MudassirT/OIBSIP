package com.mudassir.examportal;

import com.mudassir.examportal.config.AppConfig;
import com.mudassir.examportal.db.DatabaseManager;
import com.mudassir.examportal.db.UserDao;
import com.mudassir.examportal.model.User;
import com.mudassir.examportal.service.AuthService;
import com.mudassir.examportal.service.ProfileService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Profile Service Update & Validation Tests")
class ProfileServiceTest {

    private File tempDb;
    private DatabaseManager dbManager;
    private UserDao userDao;
    private ProfileService profileService;
    private AuthService authService;
    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        tempDb = File.createTempFile("profile_test_", ".db");
        tempDb.deleteOnExit();
        dbManager = new DatabaseManager("jdbc:sqlite:" + tempDb.getAbsolutePath());
        dbManager.initialize();

        userDao = new UserDao(dbManager);
        profileService = new ProfileService(userDao);
        authService = new AuthService(userDao);

        testUser = userDao.findByUsername(AppConfig.DEMO_STUDENT1_USER).orElseThrow();
    }

    @AfterEach
    void tearDown() {
        if (tempDb != null && tempDb.exists()) {
            tempDb.delete();
        }
    }

    @Test
    @DisplayName("Should successfully update valid display name immediately")
    void testUpdateDisplayNameSuccess() {
        String newName = "Mudassir Senior Dev";
        ProfileService.ProfileResult result = profileService.updateDisplayName(testUser, newName);

        assertTrue(result.isSuccess());
        assertEquals(newName, testUser.getDisplayName(), "In-memory user display name must be updated");

        // Verify in database
        Optional<User> reloaded = userDao.findById(testUser.getId());
        assertTrue(reloaded.isPresent());
        assertEquals(newName, reloaded.get().getDisplayName(), "Database display name must be updated");
    }

    @Test
    @DisplayName("Should reject invalid display names")
    void testUpdateDisplayNameInvalid() {
        String originalName = testUser.getDisplayName();

        // Too short (< 2)
        ProfileService.ProfileResult resShort = profileService.updateDisplayName(testUser, "A");
        assertFalse(resShort.isSuccess());
        assertEquals(originalName, testUser.getDisplayName());

        // Numbers in name
        ProfileService.ProfileResult resNumbers = profileService.updateDisplayName(testUser, "Student123");
        assertFalse(resNumbers.isSuccess());

        // Special characters
        ProfileService.ProfileResult resSpecial = profileService.updateDisplayName(testUser, "Student@Home!");
        assertFalse(resSpecial.isSuccess());
    }

    @Test
    @DisplayName("Should successfully update password and authenticate on subsequent login")
    void testUpdatePasswordSuccess() {
        char[] currentPass = AppConfig.DEMO_STUDENT1_PASS.toCharArray();
        char[] newPass = "NewSecurePass@999".toCharArray();
        char[] confirmPass = "NewSecurePass@999".toCharArray();

        ProfileService.ProfileResult result = profileService.updatePassword(testUser, currentPass, newPass, confirmPass);
        assertTrue(result.isSuccess(), "Password change should succeed");

        // Subsequent login with old password must fail
        AuthService.AuthResult oldLogin = authService.authenticate(testUser.getUsername(), currentPass);
        assertFalse(oldLogin.isSuccess(), "Old password must no longer work");

        // Subsequent login with new password must succeed
        AuthService.AuthResult newLogin = authService.authenticate(testUser.getUsername(), newPass);
        assertTrue(newLogin.isSuccess(), "New password must authenticate successfully");
    }

    @Test
    @DisplayName("Should reject password change if current password is wrong; old password stays valid")
    void testWrongCurrentPasswordRejected() {
        char[] wrongCurrent = "IncorrectPassword@123".toCharArray();
        char[] newPass = "BrandNewPass@999".toCharArray();
        char[] confirmPass = "BrandNewPass@999".toCharArray();

        ProfileService.ProfileResult result = profileService.updatePassword(testUser, wrongCurrent, newPass, confirmPass);
        assertFalse(result.isSuccess(), "Must reject password change when current password is wrong");
        assertEquals("Current password is incorrect.", result.getMessage());

        // Old password must stay valid
        AuthService.AuthResult login = authService.authenticate(testUser.getUsername(), AppConfig.DEMO_STUDENT1_PASS.toCharArray());
        assertTrue(login.isSuccess(), "Old password must remain valid when password change fails");
    }

    @Test
    @DisplayName("Should reject weak new password")
    void testWeakNewPasswordRejected() {
        char[] currentPass = AppConfig.DEMO_STUDENT1_PASS.toCharArray();
        char[] weakPass = "weak".toCharArray(); // too short
        char[] confirmPass = "weak".toCharArray();

        ProfileService.ProfileResult result = profileService.updatePassword(testUser, currentPass, weakPass, confirmPass);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("at least 8 characters"));
    }

    @Test
    @DisplayName("Should reject mismatched password confirmation")
    void testMismatchedConfirmationRejected() {
        char[] currentPass = AppConfig.DEMO_STUDENT1_PASS.toCharArray();
        char[] newPass = "StrongPass@123".toCharArray();
        char[] mismatchedConfirm = "DifferentPass@123".toCharArray();

        ProfileService.ProfileResult result = profileService.updatePassword(testUser, currentPass, newPass, mismatchedConfirm);
        assertFalse(result.isSuccess());
        assertEquals("New password and confirmation password do not match.", result.getMessage());
    }
}
