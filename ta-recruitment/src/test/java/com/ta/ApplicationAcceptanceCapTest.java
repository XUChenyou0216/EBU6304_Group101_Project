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
 * Vacancy cap: cannot mark more applications ACCEPTED than {@link Job#getVacancies()}.
 */
public class ApplicationAcceptanceCapTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private void writeApplicationsCsv(File dataDir) throws Exception {
        File apps = new File(dataDir, "applications.csv");
        try (FileWriter w = new FileWriter(apps)) {
            w.write(Application.CSV_HEADER + "\n");
            w.write("APP1,ta1,J1,ACCEPTED,2026-01-01,\"\"\n");
            w.write("APP2,ta2,J1,ACCEPTED,2026-01-02,\"\"\n");
            w.write("APP3,ta3,J1,SUBMITTED,2026-01-03,\"\"\n");
        }
    }

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
