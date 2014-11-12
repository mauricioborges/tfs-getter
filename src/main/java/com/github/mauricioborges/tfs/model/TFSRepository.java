package com.github.mauricioborges.tfs.model;

import com.github.mauricioborges.model.Loggable;
import com.github.mauricioborges.model.Repository;
import com.github.mauricioborges.model.exception.WrongUsageException;

import java.io.FileInputStream;

public class TFSRepository extends Loggable implements Repository {
    private final Client client;

    public TFSRepository(Client client) {
        this.client = client;
    }


    @Override
    public void close() {
        this.client.close();
    }

    @Override
    public FileInputStream getFile(String filePath) {
        if (client==null){
            throw new WrongUsageException();
        }
        return this.client.getFile(filePath);
    }
}
