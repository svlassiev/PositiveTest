package com.positive.filter;

import org.apache.commons.io.filefilter.AbstractFileFilter;

import java.io.File;

/**
 * Filters files by absolute name's ending
 * Created by serg on 06.05.2015.
 */
public class LongNameFileFilter extends AbstractFileFilter {

    private final String importName;

    public LongNameFileFilter(String importName) {
        this.importName = importName;
    }

    @Override
    public boolean accept(File file) {
        return file.getAbsolutePath().endsWith(importName);
    }

}
