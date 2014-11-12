package com.github.mauricioborges.model;

import org.apache.log4j.Logger;

public abstract class Loggable {
    protected Logger log = Logger.getLogger(this.getClass());
}
