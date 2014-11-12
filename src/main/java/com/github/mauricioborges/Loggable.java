package com.github.mauricioborges;

import org.apache.log4j.Logger;

public abstract class Loggable {
    Logger log = Logger.getLogger(this.getClass());
}
