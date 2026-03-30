package com.ta.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileManager {

    public static synchronized List<String> readAll(String filePath) {
        List<String> rows = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) return rows;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) { isHeader = false; continue; }
                if (!line.trim().isEmpty()) rows.add(line);
            }
        } catch (IOException e) { e.printStackTrace(); }
        return rows;
    }

    public static synchronized void appendRow(String filePath, String csvHeader, String row) {
        File file = new File(filePath);
        boolean needsHeader = !file.exists() || file.length() == 0;
        try {
            file.getParentFile().mkdirs();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
                if (needsHeader) { bw.write(csvHeader); bw.newLine(); }
                bw.write(row);
                bw.newLine();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static synchronized void writeAll(String filePath, String csvHeader, List<String> rows) {
        File file = new File(filePath);
        try {
            file.getParentFile().mkdirs();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, false))) {
                bw.write(csvHeader); bw.newLine();
                for (String row : rows) { bw.write(row); bw.newLine(); }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static String generateNextId(String filePath, String prefix) {
        List<String> rows = readAll(filePath);
        int maxNum = 0;
        for (String row : rows) {
            String id = row.split(",", 2)[0];
            if (id.startsWith(prefix)) {
                try {
                    int num = Integer.parseInt(id.substring(prefix.length()));
                    if (num > maxNum) maxNum = num;
                } catch (NumberFormatException ignored) {}
            }
        }
        return String.format("%s%03d", prefix, maxNum + 1);
    }
}
