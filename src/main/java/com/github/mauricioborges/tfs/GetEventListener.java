/*
 * Copyright (c) Microsoft Corporation. All rights reserved. This code released
 * under the terms of the Microsoft Public License (MS-PL,
 * http://opensource.org/licenses/ms-pl.html.)
 */

package com.github.mauricioborges.tfs;

import com.github.mauricioborges.model.Loggable;
import com.microsoft.tfs.core.clients.versioncontrol.events.GetEvent;
import com.microsoft.tfs.core.clients.versioncontrol.events.GetListener;

public class GetEventListener extends Loggable
        implements GetListener {
    public void onGet(final GetEvent e) {
        String item = e.getTargetLocalItem() != null ? e.getTargetLocalItem() : e.getServerItem();

        log.debug("getting: " + item);
    }
}
