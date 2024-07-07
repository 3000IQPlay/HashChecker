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
        if (args.length < 1 || !Files.isDirectory(Paths.get(args[0]))) {
            System.out.println("Usage: java -jar scanner.jar <scan path>");
            return;
        }

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try {
            Files.walk(Paths.get(args[0]))
                    .filter(Files::isRegularFile)
                    .forEach(file -> executor.submit(() -> {
                        try {
                            String hashString = java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Files.readAllBytes(file)));
                            if (hashString.matches("[a-fA-F0-9]{64}")) {
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
