/*
 * Copyright (c) Microsoft Corporation. All rights reserved. This code released
 * under the terms of the Microsoft Public License (MS-PL,
 * http://opensource.org/licenses/ms-pl.html.)
 */

package com.github.mauricioborges.tfs;

import com.github.mauricioborges.model.Loggable;
import com.microsoft.tfs.core.clients.versioncontrol.events.GetOperationCompletedEvent;
import com.microsoft.tfs.core.clients.versioncontrol.events.OperationCompletedEvent;
import com.microsoft.tfs.core.clients.versioncontrol.events.OperationCompletedListener;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.GetRequest;

public class GetOperationCompletedListener extends Loggable
        implements OperationCompletedListener {
    public void onGetOperationCompleted(final GetOperationCompletedEvent e) {
        for (GetRequest request : e.getRequests()) {
            if (request.getItemSpec() != null) {
                log.debug("Completed getting: " + request.getItemSpec().toString());
            }
        }
    }

    public void onOperationCompleted(final OperationCompletedEvent e) {
        if (e instanceof GetOperationCompletedEvent) {
            onGetOperationCompleted((GetOperationCompletedEvent) e);
        }
    }
}
