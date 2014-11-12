package com.github.mauricioborges.model;

public interface VCSConnection extends Closable {
    Repository toRepositoryRoot(String s);

}
