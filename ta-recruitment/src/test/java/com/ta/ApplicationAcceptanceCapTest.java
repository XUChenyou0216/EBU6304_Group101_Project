package com.ta;

import com.ta.dao.ApplicationDAO;
import com.ta.model.Application;
import com.ta.model.Job;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;

/**
 * Unit tests for the TA application acceptance vacancy cap enforced by
 * {@link ApplicationDAO#isAcceptanceCapExceeded(Job, Application, String)}.
 *
 * <p>These tests verify that a job cannot have more applications marked
 * {@code ACCEPTED} than the number of vacancies declared on the job, while
 * still allowing status changes that do not increase accepted count (for example
 * {@code REJECTED}) and permitting an already-accepted application to remain
 * accepted when the cap is exactly full.</p>
 */
public class ApplicationAcceptanceCapTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    /**
     * Writes a fixed three-row {@code applications.csv} fixture into the given data directory
     * for acceptance-cap scenarios (two accepted, one submitted on job {@code J1}).
     *
     * @param dataDir root directory that will contain {@code applications.csv}
     * @throws Exception if the CSV file cannot be created or written
     */
    private void writeApplicationsCsv(File dataDir) throws Exception {
        File apps = new File(dataDir, "applications.csv");
        try (FileWriter w = new FileWriter(apps)) {
            w.write(Application.CSV_HEADER + "\n");
            w.write("APP1,ta1,J1,ACCEPTED,2026-01-01,\"\"\n");
            w.write("APP2,ta2,J1,ACCEPTED,2026-01-02,\"\"\n");
            w.write("APP3,ta3,J1,SUBMITTED,2026-01-03,\"\"\n");
        }
    }

    /**
     * Verifies that a third applicant cannot be accepted when two slots are
     * already filled for a job with two vacancies.
     *
     * <p>Expects {@code isAcceptanceCapExceeded} to return {@code true} when
     * attempting to accept the pending application, and {@code false} when
     * the target status is {@code REJECTED}.</p>
     *
     * @throws Exception if temporary CSV setup or DAO access fails
     */
    @Test
    public void thirdApplicantCannotBeAcceptedWhenCapReached() throws Exception {
        File dataDir = folder.newFolder("data");
        writeApplicationsCsv(dataDir);
        ApplicationDAO dao = new ApplicationDAO(dataDir.getAbsolutePath());
        Job job = new Job();
        job.setJobId("J1");
        job.setVacancies(2);
        Application third = dao.findById("APP3");
        Assert.assertNotNull(third);
        Assert.assertTrue(dao.isAcceptanceCapExceeded(job, third, "ACCEPTED"));
        Assert.assertFalse(dao.isAcceptanceCapExceeded(job, third, "REJECTED"));
    }

    /**
     * Verifies that an application already in {@code ACCEPTED} status may
     * remain accepted even when the vacancy cap is exactly reached.
     *
     * <p>This ensures idempotent re-acceptance of an existing accepted row
     * does not incorrectly trigger a cap-exceeded error.</p>
     *
     * @throws Exception if temporary CSV setup or DAO access fails
     */
    @Test
    public void alreadyAcceptedRowMayStayAccepted() throws Exception {
        File dataDir = folder.newFolder("data");
        writeApplicationsCsv(dataDir);
        ApplicationDAO dao = new ApplicationDAO(dataDir.getAbsolutePath());
        Job job = new Job();
        job.setJobId("J1");
        job.setVacancies(2);
        Application second = dao.findById("APP2");
        Assert.assertNotNull(second);
        Assert.assertFalse(dao.isAcceptanceCapExceeded(job, second, "ACCEPTED"));
    }

    /**
     * Verifies that acceptance is permitted when the number of accepted
     * applications is still below the job vacancy limit.
     *
     * <p>With one accepted and one submitted application on a two-vacancy job,
     * changing the submitted application to {@code ACCEPTED} must not exceed
     * the cap.</p>
     *
     * @throws Exception if temporary CSV setup or DAO access fails
     */
    @Test
    public void slotAvailableWhenBelowCap() throws Exception {
        File dataDir = folder.newFolder("data");
        File apps = new File(dataDir, "applications.csv");
        try (FileWriter w = new FileWriter(apps)) {
            w.write(Application.CSV_HEADER + "\n");
            w.write("APP1,ta1,J1,ACCEPTED,2026-01-01,\"\"\n");
            w.write("APP2,ta2,J1,SUBMITTED,2026-01-02,\"\"\n");
        }
        ApplicationDAO dao = new ApplicationDAO(dataDir.getAbsolutePath());
        Job job = new Job();
        job.setJobId("J1");
        job.setVacancies(2);
        Application pending = dao.findById("APP2");
        Assert.assertFalse(dao.isAcceptanceCapExceeded(job, pending, "ACCEPTED"));
    }
}
