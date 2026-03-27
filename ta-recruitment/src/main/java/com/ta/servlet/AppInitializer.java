package com.ta.servlet;

import com.ta.model.*;
import com.ta.util.FileManager;
import com.ta.util.PasswordUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;

/**
 * Runs once when the application starts.
 * Ensures data directory and CSV files exist with headers and sample data.
 * This fixes the issue where mvn tomcat7:run does not copy CSV files to target/.
 */
@WebListener
public class AppInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String realPath = sce.getServletContext().getRealPath("/");
        String dataDir = realPath + "data";

        // Create data directory if missing
        new File(dataDir).mkdirs();

        // Create uploads directory if missing
        new File(realPath + "uploads").mkdirs();

        // Initialize users.csv with sample accounts if it doesn't exist
        File usersFile = new File(dataDir + "/users.csv");
        if (!usersFile.exists() || usersFile.length() == 0) {
            System.out.println("[INIT] Creating users.csv with sample accounts...");
            FileManager.appendRow(usersFile.getPath(), User.CSV_HEADER,
                "U001,admin," + PasswordUtil.hash("admin") + ",ADMIN,admin@bupt.edu.cn,pet,admindog,ACTIVE");
            FileManager.appendRow(usersFile.getPath(), User.CSV_HEADER,
                "U002,prof_wang," + PasswordUtil.hash("password") + ",MO,wang@bupt.edu.cn,city,beijing,ACTIVE");
            FileManager.appendRow(usersFile.getPath(), User.CSV_HEADER,
                "U003,student_li," + PasswordUtil.hash("password") + ",TA,li@bupt.edu.cn,school,bupt,ACTIVE");
        }

        // Initialize jobs.csv if missing
        File jobsFile = new File(dataDir + "/jobs.csv");
        if (!jobsFile.exists() || jobsFile.length() == 0) {
            System.out.println("[INIT] Creating jobs.csv with sample data...");
            FileManager.appendRow(jobsFile.getPath(), Job.CSV_HEADER,
                "J001,U002,\"EBU6304 Software Engineering\",\"Assist with lab sessions and marking\",\"Java programming; Git experience\",2,2026-04-30,OPEN,2026-03-20");
            FileManager.appendRow(jobsFile.getPath(), Job.CSV_HEADER,
                "J002,U002,\"EBU5476 Computer Networks\",\"Help with tutorials and invigilation\",\"Networking knowledge; Good communication\",1,2026-04-15,OPEN,2026-03-20");
        }

        // Initialize applications.csv if missing (header only)
        File appsFile = new File(dataDir + "/applications.csv");
        if (!appsFile.exists() || appsFile.length() == 0) {
            System.out.println("[INIT] Creating applications.csv...");
            FileManager.writeAll(appsFile.getPath(), Application.CSV_HEADER, java.util.Collections.emptyList());
        }

        // Initialize profiles.csv if missing (header only)
        File profilesFile = new File(dataDir + "/profiles.csv");
        if (!profilesFile.exists() || profilesFile.length() == 0) {
            System.out.println("[INIT] Creating profiles.csv...");
            FileManager.writeAll(profilesFile.getPath(), TAProfile.CSV_HEADER, java.util.Collections.emptyList());
        }

        System.out.println("[INIT] Data directory ready: " + dataDir);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // nothing to clean up
    }
}
