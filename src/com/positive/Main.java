package com.positive;

import java.io.File;
import java.nio.file.Path;

public class Main {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Program returns a name of a folder with compilation subset for a certain file");
            System.out.println("Usage: java com.positive.Main file project");
        } else {
            File unit = new File(args[0]);
            Path project = new File(args[1]).toPath();
            try {
                CompilationSubsetDetector subsetDetector = new CompilationSubsetDetector(project);
                File compilationFolder = subsetDetector.getCompileSubset(unit);
                System.out.println(compilationFolder);
            } catch (Throwable t) {
                System.err.println("Unable to find compilation subset for file " + unit + " and project " +
                        project + ". " + t.getMessage());
            }
        }
    }
}
