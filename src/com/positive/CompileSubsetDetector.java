package com.positive;

import japa.parser.JavaParser;
import japa.parser.ParseException;
import japa.parser.ast.CompilationUnit;
import japa.parser.ast.ImportDeclaration;
import japa.parser.ast.body.MethodDeclaration;
import japa.parser.ast.body.TypeDeclaration;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.visitor.VoidVisitorAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by serg on 06.05.2015.
 */
public class CompileSubsetDetector {

    private Map<Class, File> compileSubset = new HashMap<>();

    public File getCompileSubset(File compilationFile, Path project) throws IOException, ParseException {

        try (FileInputStream in = new FileInputStream(compilationFile)) {
            CompilationUnit unit = JavaParser.parse(in);
            if (null != unit.getImports()) {
                unit.getImports().forEach(importDeclaration -> {
                    if (!importDeclaration.isAsterisk()) {
                        //TBD
                    }
                    System.out.print(importDeclaration);
                });
            }
            System.out.println(unit.getPackage());
            // visit and print the methods names
            new MethodVisitor().visit(unit, null);
        }
        return null;
    }

    /**
     * Simple visitor implementation for visiting MethodDeclaration nodes.
     */
    private static class MethodVisitor extends VoidVisitorAdapter {

        @Override
        public void visit(MethodDeclaration method, Object arg) {
            // here you can access the attributes of the method.
            // this method will be called for all methods in this
            // CompilationUnit, including inner class methods
            System.out.println(method.getName() + " type: " + method.getType());
            if (null != method.getThrows()) {
                method.getThrows().forEach(throwsName -> System.out.println("throws " + throwsName));
            }
        }
    }
}
