package com.github.mauricioborges.tfs.model;

import com.github.mauricioborges.model.Loggable;
import com.github.mauricioborges.model.Repository;
import com.github.mauricioborges.model.VCSConnection;
import com.github.mauricioborges.model.exception.WrongUsageException;

public class TFSVCSConnection extends Loggable implements VCSConnection {


    private Client client;

    public TFSVCSConnection(Client client) {
        //TODO: encapsulate TPC into object, to avoid passing TFS entities around
        this.client=client;
    }

    @Override
    public Repository toRepositoryRoot(String rootPath) {
        if (client == null) {
            log.error("Cannot connect without valid TFSTeamProjectCollection object!");
            throw new WrongUsageException();
        }
        client.createAndMapWorkspace(rootPath);
        return new TFSRepository(client);
    }


    @Override
    public void close() {
        this.client.close();
    }
}
