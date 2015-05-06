package com.positive;

import com.positive.filter.LongNameFileFilter;
import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Detects file compilation subset and creates its copy in a temporary directory
 * Created by serg on 06.05.2015.
 */
public class CompilationSubsetDetector {

    private final File root;
    private final Path project;
    private Set<String> checkedFiles = new HashSet<>();
    private Queue<File> compilationFiles = new LinkedList<>();

    /**
     * Creates new instance for certain project
     * @param project project from where compilation subset must be extracted
     * @throws IOException iff unable to create root directory for compilation subset
     */
    public CompilationSubsetDetector(Path project) throws IOException {
        this.project = project;
        root = new File(FileUtils.getTempDirectory(), "PositiveTechnologiesTest" + System.currentTimeMillis());
        if (!root.mkdirs()) {
            throw new IOException("Unable to create root directory for compilation subset: " + root);
        }
    }

    /**
     * Inspects project folder in attempt to find compilation subset for compilationFile.
     * All the files are copied with their package structure to temp folder, which is returned by this method.
     * @param compilationFile java file which dependencies are analyzed
     * @return temporary root directory where compilation subset is stored
     * @throws IOException in case of problems with writing on disk
     * @throws ParseException if compilationFile cannot be parsed
     */
    public File getCompileSubset(File compilationFile) throws IOException, ParseException {
        compilationFiles.add(compilationFile);
        UnitVisitor visitor = new UnitVisitor();
        while (!compilationFiles.isEmpty()) {
            try (FileInputStream in = new FileInputStream(compilationFiles.poll())) {
                CompilationUnit unit = JavaParser.parse(in);
                visitor.visit(unit, unit.getPackage().getName().toString());
            }
        }
        return root;
    }

    /**
     * Simple visitor implementation for visiting nodes with dependencies information.
     */
    private class UnitVisitor extends VoidVisitorAdapter<String> {

        @Override
        public void visit(ClassOrInterfaceType type, String packageName) {
            super.visit(type, packageName);
            ClassOrInterfaceType scope = type.getScope();
            String searchDirectory;
            if (null != scope) {
                searchDirectory = scope.toString().replace(".", File.separator);
            } else {
                searchDirectory = packageName.replace(".", File.separator);
            }
            String className = searchDirectory + File.separator + type.getName() + ".java";
            getFileCopyFromProject(className, false);
        }

        @Override
        public void visit(ImportDeclaration importDeclaration, String ignored) {
            super.visit(importDeclaration, ignored);
            String importName = importDeclaration.getName().toString().replace(".", File.separator) +
                    (importDeclaration.isAsterisk() ? "" : ".java");
            getFileCopyFromProject(importName, importDeclaration.isAsterisk());
        }

        /**
         * Looks for a fileName in the project directory and in case of success copies it to compilation subset root
         * directory
         * @param fileName name of file to be looked for
         * @param isDirectory true iff file is a directory
         */
        private void getFileCopyFromProject(String fileName, boolean isDirectory) {
            if (!checkedFiles.contains(fileName)) {
                Collection<File> files = FileUtils.listFiles(project.toFile(), new LongNameFileFilter(fileName),
                        TrueFileFilter.INSTANCE);
                if (files.size() > 1) {
                    throw new IllegalArgumentException("There are several possible ways to solve the task with these " +
                            "parameters");
                }
                files.forEach(file -> {
                    compilationFiles.add(file);
                    try {
                        File copyDir = isDirectory ? new File(root, fileName) :
                                new File(root, fileName).getParentFile();
                        if (!copyDir.exists() && !copyDir.mkdirs()) {
                            throw new RuntimeException("Unable to create " + copyDir);
                        }
                        if (!isDirectory) {
                            File copy = new File(copyDir, file.getName());
                            Files.copy(file.toPath(), copy.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException("Unable to copy file to root directory: " + e.getMessage());
                    }
                });
            }
            checkedFiles.add(fileName);
        }
    }
}