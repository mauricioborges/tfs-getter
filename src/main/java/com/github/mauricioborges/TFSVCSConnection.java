package com.github.mauricioborges;

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
        this.tpc = tpc;
    }

    @Override
    public Repository toRepositoryRoot(String rootPath) {
        if (tpc == null) {
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
        workspace = tpc.getVersionControlClient().tryGetWorkspace(
                TFS.MAPPING_LOCAL_PATH);

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
                log.info("Deleted the workspace");
            } catch (WorkspaceNotFoundException e) {
                log.info("Cannot delete the workspace " + workspace.getDisplayName() + ", probably was deleted before. Ignoring ...");
            }
        }
    }
}
