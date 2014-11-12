package com.github.mauricioborges.model;

import java.io.FileInputStream;

public interface Repository extends Closable {
    FileInputStream getFile(String filePath);
}
