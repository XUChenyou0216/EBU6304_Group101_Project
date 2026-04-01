package com.ta.servlet;

import com.ta.model.*;
import com.ta.util.FileManager;
import com.ta.util.PasswordUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.io.File;

@WebListener
public class AppInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        String realPath = sce.getServletContext().getRealPath("/");
        String dataDir = realPath + "data";

        new File(dataDir).mkdirs();
        new File(realPath + "uploads").mkdirs();

        // ===== users.csv =====
        File usersFile = new File(dataDir + "/users.csv");
        if (!usersFile.exists() || usersFile.length() == 0) {
            System.out.println("[INIT] Creating users.csv with sample accounts...");
            FileManager.appendRow(usersFile.getPath(), User.CSV_HEADER,
                "U001,admin," + PasswordUtil.hash("admin") + ",ADMIN,admin@bupt.edu.cn,pet,admindog,ACTIVE");
            FileManager.appendRow(usersFile.getPath(), User.CSV_HEADER,
                "U002,prof_wang," + PasswordUtil.hash("password") + ",MO,wang@bupt.edu.cn,city,beijing,ACTIVE");
            FileManager.appendRow(usersFile.getPath(), User.CSV_HEADER,
                "U003,student_li," + PasswordUtil.hash("password") + ",TA,li@bupt.edu.cn,school,bupt,ACTIVE");
            FileManager.appendRow(usersFile.getPath(), User.CSV_HEADER,
                "U004,student_zhang," + PasswordUtil.hash("password") + ",TA,zhang@bupt.edu.cn,pet,cat,ACTIVE");
            FileManager.appendRow(usersFile.getPath(), User.CSV_HEADER,
                "U005,prof_chen," + PasswordUtil.hash("password") + ",MO,chen@bupt.edu.cn,city,shanghai,ACTIVE");
        }

        // ===== jobs.csv =====
        File jobsFile = new File(dataDir + "/jobs.csv");
        if (!jobsFile.exists() || jobsFile.length() == 0) {
            System.out.println("[INIT] Creating jobs.csv with sample data...");
            FileManager.appendRow(jobsFile.getPath(), Job.CSV_HEADER,
                "J001,U002,\"EBU6304 Software Engineering\",\"Assist with lab sessions and marking\",\"Java programming; Git experience\",2,2026-04-30,OPEN,2026-03-20");
            FileManager.appendRow(jobsFile.getPath(), Job.CSV_HEADER,
                "J002,U002,\"EBU5476 Computer Networks\",\"Help with tutorials and invigilation\",\"Networking knowledge; Good communication\",1,2026-04-15,OPEN,2026-03-20");
            FileManager.appendRow(jobsFile.getPath(), Job.CSV_HEADER,
                "J003,U002,\"EBU6305 Data Mining\",\"Support practical sessions on data analysis\",\"Python; Machine learning basics\",3,2026-05-10,OPEN,2026-03-25");
            FileManager.appendRow(jobsFile.getPath(), Job.CSV_HEADER,
                "J004,U005,\"EBU5302 Operating Systems\",\"Lab assistance and exam invigilation\",\"C programming; Linux experience\",2,2026-04-20,OPEN,2026-03-22");
            FileManager.appendRow(jobsFile.getPath(), Job.CSV_HEADER,
                "J005,U002,\"EBU6303 Internet of Things\",\"Assist with IoT lab setup and demos\",\"Arduino; Raspberry Pi; Sensor networks\",1,2026-03-15,CLOSED,2026-02-20");
        }

        // ===== profiles.csv =====
        File profilesFile = new File(dataDir + "/profiles.csv");
        if (!profilesFile.exists() || profilesFile.length() == 0) {
            System.out.println("[INIT] Creating profiles.csv with sample data...");
            FileManager.appendRow(profilesFile.getPath(), TAProfile.CSV_HEADER,
                "U003,231220001,Li Student,Computer Science,3,13800138000,");
            FileManager.appendRow(profilesFile.getPath(), TAProfile.CSV_HEADER,
                "U004,231220002,Zhang Wei,Software Engineering,2,13900139000,");
        }

        // ===== applications.csv =====
        File appsFile = new File(dataDir + "/applications.csv");
        if (!appsFile.exists() || appsFile.length() == 0) {
            System.out.println("[INIT] Creating applications.csv with sample data...");
            FileManager.appendRow(appsFile.getPath(), Application.CSV_HEADER,
                "APP001,U003,J001,SUBMITTED,2026-03-25,");
            FileManager.appendRow(appsFile.getPath(), Application.CSV_HEADER,
                "APP002,U004,J001,UNDER_REVIEW,2026-03-26,Good candidate");
            FileManager.appendRow(appsFile.getPath(), Application.CSV_HEADER,
                "APP003,U003,J002,SUBMITTED,2026-03-27,");
            FileManager.appendRow(appsFile.getPath(), Application.CSV_HEADER,
                "APP004,U004,J003,ACCEPTED,2026-03-28,Excellent skills");
        }

        System.out.println("[INIT] Data directory ready: " + dataDir);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {}
}
