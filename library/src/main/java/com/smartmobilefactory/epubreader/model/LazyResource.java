package com.smartmobilefactory.epubreader.model;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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
        InputStream in = new BufferedInputStream(new FileInputStream(fileName));
        try {
            return IOUtil.toByteArray(in);
        } finally {
            in.close();
        }
    }
}
