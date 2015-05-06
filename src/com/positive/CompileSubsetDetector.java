package com.positive;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.FieldDeclaration;
import japa.parser.ast.body.InitializerDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.visitor.VoidVisitorAdapter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * Created by serg on 06.05.2015.
 */
public class CompileSubsetDetector {

    private final File root;
    private Map<File, File> compileSubset = new HashMap<>();
    private Queue<File> compilationFiles = new LinkedList<>();

    public CompileSubsetDetector() throws IOException {
        root = new File(FileUtils.getTempDirectory(), "PositiveTechnologiesTest" + System.currentTimeMillis());
        if (!root.mkdirs()) {
            throw new IOException("Unable to create root directory for compilation subset: " + root);
        };
    }

    public File getCompileSubset(File compilationFile, Path project) throws IOException, ParseException {

        compilationFiles.add(compilationFile);
        while (!compilationFiles.isEmpty()) {
            try (FileInputStream in = new FileInputStream(compilationFiles.poll())) {
                CompilationUnit unit = JavaParser.parse(in);
                new MethodVisitor().visit(unit, project);
            }
        }
        return root;
    }

    /**
     * Simple visitor implementation for visiting MethodDeclaration nodes.
     */
    private class MethodVisitor extends VoidVisitorAdapter {

        @Override
        public void visit(MethodDeclaration method, Object arg) {
            System.out.println(method.getName() + " type: " + method.getType());
        }

        @Override
        public void visit(FieldDeclaration n, Object arg) {
            System.out.println("Field declaration: " + n);
        }

        @Override
        public void visit(ImportDeclaration n, Object arg) {
            if (arg instanceof Path) {
                Path project = (Path) arg;
                String importName = n.getName().toString().replace(".", File.separator) +
                        (n.isAsterisk() ? "" : ".java");
                IOFileFilter filter = new AbstractFileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.getAbsolutePath().endsWith(importName);
                    }
                };
                Collection<File> files = FileUtils.listFiles(project.toFile(), filter, TrueFileFilter.INSTANCE);
                if (files.size() > 1) {
                    throw new IllegalArgumentException("There are several possible ways to solve the task with these " +
                            "parameters");
                }
                files.forEach(file -> {
                    if (!compileSubset.containsKey(file)) {
                        compilationFiles.add(file);
                        try {
                            File copyDir = n.isAsterisk() ? new File(root, importName) :
                                    new File(root, importName).getParentFile();
                            copyDir.mkdirs();
                            if (n.isAsterisk()) {
                                compileSubset.put(file, copyDir);
                            } else {
                                File copy = new File(copyDir, file.getName());
                                Files.copy(file.toPath(), copy.toPath(), StandardCopyOption.COPY_ATTRIBUTES);
                                compileSubset.put(file, copy);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            throw new RuntimeException("Unable to copy file to root directory: " + e.getMessage());
                        }
                    }
                });
            }
        }

        @Override
        public void visit(InitializerDeclaration n, Object arg) {
            System.out.println("Initializer declaration: " + n);
        }
    }


}
