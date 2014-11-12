package com.github.mauricioborges;

public interface VCSConnection extends Closable {
    Repository toRepositoryRoot(String s);

}
