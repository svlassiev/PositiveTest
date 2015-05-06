package com.positive;

import java.io.File;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("How to use this app");
        } else {
            File unit = new File(args[0]);
            Path project = new File(args[1]).toPath();
            try {
                CompileSubsetDetector subsetDetector = new CompileSubsetDetector();
                File compilationFolder = subsetDetector.getCompileSubset(unit, project);
                System.out.println(compilationFolder);
            } catch (Throwable t) {
                System.err.println("Unable to find compilation subset for file " + unit + " and project " +
                        project + ". " + t.getMessage());
                t.printStackTrace();
            }
        }
    }
}
