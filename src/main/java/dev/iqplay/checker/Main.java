package dev.iqplay.checker;

import java.io.IOException;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java -jar scanner.jar <scan path>");
            return;
        }

        Path path = Paths.get(args[0]);
        if (!Files.isDirectory(path)) {
            System.out.println("Folder does not exist or is not a directory");
            return;
        }

        int threadPoolSize = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        try {
            Files.walk(path)
                .filter(Files::isRegularFile)
                .forEach(file -> executor.submit(() -> {
                    try {
                        // System.out.println("Scanning: " + file.getFileName());
                        byte[] fileBytes = Files.readAllBytes(file);
                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
                        byte[] hash = digest.digest(fileBytes);
                        
                        StringBuilder result = new StringBuilder();
                        for (byte b : hash) {
                            result.append(String.format("%02x", b));
                        }
                        
                        String hashString = result.toString();
                        // System.out.println("Output: " + hashString); // Debug information
                        
                        if (hashString.matches("[a-fA-F0-9]{64}")) { // Matches a 64-character hex string
                            System.out.println(file.getFileName() + " - " + hashString);
                        }
                    } catch (IOException | NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                }));

            executor.shutdown();
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
