package com.ta;

import com.ta.util.PasswordUtil;
import com.ta.util.Validator;
import com.ta.model.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class BasicTest {
    @Test public void testPasswordHash() {
        String hash = PasswordUtil.hash("password");
        assertNotNull(hash);
        assertEquals(64, hash.length());
        assertTrue(PasswordUtil.verify("password", hash));
        assertFalse(PasswordUtil.verify("wrong", hash));
    }

    @Test public void testAdminHash() {
        assertTrue(PasswordUtil.verify("admin",
            "8c6976e5b5410415bde908bd4dee15dfb167a9c873fc4bb8a81f6f2ab448a918"));
    }

    @Test public void testValidation() {
        assertNotNull(Validator.requireNonEmpty("", "F"));
        assertNull(Validator.requireNonEmpty("ok", "F"));
        assertNull(Validator.validateEmail("a@b.com"));
        assertNotNull(Validator.validateEmail("bad"));
        assertNotNull(Validator.validatePassword("123"));
        assertNull(Validator.validatePassword("123456"));
        assertNull(Validator.validateCvFile("cv.pdf", 1024));
        assertNotNull(Validator.validateCvFile("v.exe", 1024));
    }

    @Test public void testUserCsv() {
        User u = new User("U099","test","hash","TA","t@t.com","pet","dog","ACTIVE");
        User p = User.fromCsvRow(u.toCsvRow());
        assertNotNull(p);
        assertEquals("U099", p.getUserId());
        assertEquals("TA", p.getRole());
    }

    @Test public void testJobCsv() {
        Job j = new Job("J099","U002","Module","Desc, with comma","Skills",3,"2026-05-01","OPEN","2026-03-20");
        Job p = Job.fromCsvRow(j.toCsvRow());
        assertNotNull(p);
        assertEquals("Desc, with comma", p.getDescription());
        assertEquals(3, p.getVacancies());
    }

    @Test public void testApplicationCsv() {
        Application a = new Application("APP001","U003","J001","SUBMITTED","2026-03-25","");
        Application p = Application.fromCsvRow(a.toCsvRow());
        assertNotNull(p);
        assertEquals("SUBMITTED", p.getStatus());
    }
}
