package com.smartmobilefactory.epubreader.model;

import java.io.FileInputStream;
import java.io.IOException;

import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.util.IOUtil;

class LazyResource extends Resource {

    private String fileName;

    LazyResource(String fileName, long size, String href) {
        super(fileName, size, href);
        this.fileName = fileName;
    }

    @Override
    public byte[] getData() throws IOException {
        return IOUtil.toByteArray(new FileInputStream(fileName));
    }
}
