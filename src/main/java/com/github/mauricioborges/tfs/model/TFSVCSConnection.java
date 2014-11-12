package com.github.mauricioborges.tfs.model;

import com.github.mauricioborges.model.Loggable;
import com.github.mauricioborges.model.Repository;
import com.github.mauricioborges.model.VCSConnection;
import com.github.mauricioborges.model.exception.WrongUsageException;
import com.microsoft.tfs.core.TFSTeamProjectCollection;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspaceLocation;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspacePermissionProfile;
import com.microsoft.tfs.core.clients.versioncontrol.exceptions.WorkspaceNotFoundException;
import com.microsoft.tfs.core.clients.versioncontrol.path.LocalPath;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.WorkingFolder;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace;

import java.util.ArrayList;
import java.util.List;

public class TFSVCSConnection extends Loggable implements VCSConnection {


    private TFSTeamProjectCollection tpc;
    private List<Workspace> workspaces = new ArrayList<Workspace>();

    public TFSVCSConnection(TFSTeamProjectCollection tpc) {
        //TODO: encapsulate TPC into object, to avoid passing TFS entities around
        this.tpc = tpc;
    }

    @Override
    public Repository toRepositoryRoot(String rootPath) {
        if (tpc == null) {
            log.error("Cannot connect without valid TFSTeamProjectCollection object!");
            throw new WrongUsageException();
        }
        Workspace workspace = createAndMapWorkspace(rootPath);
        this.workspaces.add(workspace);
        return new TFSRepository(workspace, rootPath, tpc);
    }

    private Workspace createAndMapWorkspace(String mappingServerPath) {
        String workspaceName = "wkspc"
                + System.currentTimeMillis();
        Workspace workspace = null;
        // Get the workspace
        try {
            workspace = tpc.getVersionControlClient().tryGetWorkspace(
                    TFS.MAPPING_LOCAL_PATH);
        } catch (UnsatisfiedLinkError e) {
            log.error("Cannot access native library at " + System.getProperty(TFS.COM_MICROSOFT_TFS_JNI_NATIVE_BASE_DIRECTORY));
            throw new CannotAccessRepositoryException();
        }

        // Create and map the workspace if it does not exist
        if (workspace == null) {
            workspace = tpc.getVersionControlClient().createWorkspace(null,
                    workspaceName, "Sample workspace comment",
                    WorkspaceLocation.SERVER, null,
                    WorkspacePermissionProfile.getPrivateProfile());

            // Map the workspace
            WorkingFolder workingFolder = new WorkingFolder(
                    mappingServerPath,
                    LocalPath.canonicalize(TFS.MAPPING_LOCAL_PATH));
            workspace.createWorkingFolder(workingFolder);
        }
        log.debug("Workspace '" + workspaceName
                + "' now exists and is mapped");

        return workspace;
    }

    @Override
    public void close() {
        for (Workspace workspace : workspaces) {
            try {
                tpc.getVersionControlClient().deleteWorkspace(workspace);
                log.info("Deleted the workspace" + workspace.getDisplayName());
            } catch (WorkspaceNotFoundException e) {
                log.info("Cannot delete the workspace " + workspace.getDisplayName() + ", probably was deleted before. Ignoring ...");
            }
        }
    }
}
