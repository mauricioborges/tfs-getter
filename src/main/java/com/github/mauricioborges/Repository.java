package com.github.mauricioborges;

import java.io.FileInputStream;

public interface Repository extends Closable {
    FileInputStream getFile(String filePath);
}
