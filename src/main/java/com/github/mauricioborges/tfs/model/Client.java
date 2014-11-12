package com.github.mauricioborges.tfs.model;

import com.github.mauricioborges.model.Closable;

import java.io.FileInputStream;

public interface Client extends Closable {
    void createAndMapWorkspace(String mappingServerPath);

    FileInputStream getFile(String filePath);
}
