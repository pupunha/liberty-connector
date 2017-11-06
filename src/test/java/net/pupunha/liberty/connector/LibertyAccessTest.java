package net.pupunha.liberty.connector;

import net.pupunha.liberty.connector.exception.LibertyAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

class LibertyAccessTest {

    LibertyAccess libertyAccess;

    @BeforeEach
    void setUp() {
        libertyAccess = new LibertyAccess();
    }

    @Test
    void getApplications() throws LibertyAccessException {
        LibertyConfiguration configuration = new LibertyConfiguration();
        configuration.setProfileUse("testServer");
        List<Application> applications = libertyAccess.getApplications(configuration);
        for (Application application : applications) {
            System.out.println(application);
        }
    }
}