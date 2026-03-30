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
                "J002,U002,\"EBU5476 Computer Networks\",\"Help with tutorials and invigilation\",\"Networking knowledge; Good communication\",2,2026-04-15,OPEN,2026-03-20");
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

        seedApplicantsDemoIfEmpty(dataDir);

        System.out.println("[INIT] Data directory ready: " + dataDir);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // nothing to clean up
    }

    /**
     * If there are no application rows yet, populate demo TA users, profiles, and applications
     * so the MO "Applicants & Review" screen matches the project prototype.
     */
    private void seedApplicantsDemoIfEmpty(String dataDir) {
        if (!FileManager.readAll(dataDir + "/applications.csv").isEmpty()) return;

        System.out.println("[INIT] Seeding demo applications & TA profiles...");
        File usersFile = new File(dataDir + "/users.csv");
        boolean hasDemoTa = FileManager.readAll(usersFile.getPath()).stream()
            .anyMatch(r -> r.startsWith("U004,"));
        if (!hasDemoTa) {
            String hash = PasswordUtil.hash("password");
            String[] extraUsers = new String[] {
                "U004,chen_ming," + hash + ",TA,chen@bupt.edu.cn,school,bupt,ACTIVE",
                "U005,wang_fang," + hash + ",TA,fang@bupt.edu.cn,school,bupt,ACTIVE",
                "U006,liu_yang," + hash + ",TA,liu@bupt.edu.cn,school,bupt,ACTIVE",
                "U007,zhao_qian," + hash + ",TA,zhao@bupt.edu.cn,school,bupt,ACTIVE",
                "U008,sun_lei," + hash + ",TA,sun@bupt.edu.cn,school,bupt,ACTIVE",
                "U009,zhou_jing," + hash + ",TA,zhou@bupt.edu.cn,school,bupt,ACTIVE"
            };
            for (String row : extraUsers) {
                FileManager.appendRow(usersFile.getPath(), User.CSV_HEADER, row);
            }
        }

        java.util.List<String> profRows = java.util.Arrays.asList(
            "U003,2024CS1001,\"Li Wei\",\"Computer Science\",3,,,Python;Java;Algorithms;Data Structures,3.85,2024/25",
            "U004,2023CS2045,\"Chen Ming\",\"Computer Science\",3,,,Java;Teaching Experience;Algorithms,3.72,2024/25",
            "U005,2024SE1102,\"Wang Fang\",\"Software Engineering\",2,,,Python;Data Structures;Statistics,3.91,2024/25",
            "U006,2022CS0891,\"Liu Yang\",\"Computer Science\",4,,,Algorithms;Java;Teaching Experience,3.68,2023/24",
            "U007,2024NET2003,\"Zhao Qian\",\"Software Engineering\",2,,,Java;Statistics,3.55,2024/25",
            "U008,2023CS3008,\"Sun Lei\",\"Computer Science\",3,,,Python;Data Structures;Algorithms,3.88,2024/25",
            "U009,2024CS0456,\"Zhou Jing\",\"Computer Science\",2,,,Teaching Experience;Statistics;Algorithms,3.79,2024/25"
        );
        FileManager.writeAll(dataDir + "/profiles.csv", TAProfile.CSV_HEADER, profRows);

        java.util.List<String> appRows = java.util.Arrays.asList(
            "APP001,U003,J001,UNDER_REVIEW,2026-03-18,",
            "APP002,U004,J001,ACCEPTED,2026-03-19,\"Strong candidate\"",
            "APP003,U005,J001,OFFERED,2026-03-19,",
            "APP004,U006,J001,INTERVIEWED,2026-03-20,",
            "APP005,U007,J002,REJECTED,2026-03-17,",
            "APP006,U008,J002,UNDER_REVIEW,2026-03-21,",
            "APP007,U009,J002,PENDING,2026-03-22,"
        );
        FileManager.writeAll(dataDir + "/applications.csv", Application.CSV_HEADER, appRows);
    }
}
