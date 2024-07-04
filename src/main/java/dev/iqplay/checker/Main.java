package dev.iqplay.checker;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class Main {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java -jar scanner.jar <scan path>");
            return;
        }
        File path = new File(args[0]);
        if (!path.isDirectory()) {
            System.out.println("Folder does not exist or is not a directory");
            return;
        }

        File[] files = path.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) continue;
                // System.out.println("Scanning: " + file.getName());
                try {
                    ProcessBuilder builder = new ProcessBuilder("certutil", "-hashfile", file.getAbsolutePath(), "SHA256");
                    builder.redirectErrorStream(true);
                    Process process = builder.start();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            // System.out.println("Output: " + line); // Debug information
                            if (line.matches("[a-fA-F0-9]{64}")) { // Matches a 64-character hex string
                                System.out.println(file.getName() + " - " + line.trim());
                                break;
                            }
                        }
                    }
                    process.waitFor();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}