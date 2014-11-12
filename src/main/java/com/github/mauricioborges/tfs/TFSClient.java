package com.github.mauricioborges.tfs;

import com.github.mauricioborges.model.Closable;
import com.github.mauricioborges.model.Loggable;
import com.github.mauricioborges.model.exception.CannotGetFileException;
import com.github.mauricioborges.model.exception.WrongUsageException;
import com.github.mauricioborges.tfs.GetEventListener;
import com.github.mauricioborges.tfs.GetOperationCompletedListener;
import com.github.mauricioborges.tfs.GetOperationStartedListener;
import com.github.mauricioborges.tfs.TFS;
import com.github.mauricioborges.tfs.model.Client;
import com.github.mauricioborges.tfs.model.exception.CannotAccessRepositoryException;
import com.microsoft.tfs.core.TFSTeamProjectCollection;
import com.microsoft.tfs.core.clients.versioncontrol.GetOptions;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspaceLocation;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspacePermissionProfile;
import com.microsoft.tfs.core.clients.versioncontrol.exceptions.WorkspaceNotFoundException;
import com.microsoft.tfs.core.clients.versioncontrol.path.LocalPath;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.GetRequest;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.RecursionType;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.WorkingFolder;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace;
import com.microsoft.tfs.core.clients.versioncontrol.specs.ItemSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.LatestVersionSpec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class TFSClient extends Loggable implements Client {

    private TFSTeamProjectCollection tpc;
    private List<Workspace> workspaces=new ArrayList<Workspace>();
    private Workspace workspace;
    private String mappingServerPath=null;

    public TFSClient(TFSTeamProjectCollection tpc) {
        this.tpc=tpc;
        
    }

    @Override
    public void createAndMapWorkspace(String mappingServerPath) {
        this.mappingServerPath=mappingServerPath;

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

        this.workspace = workspace;
        this.workspaces.add(workspace);

    }

    @Override
    public FileInputStream getFile(String filePath) {
        if (workspace == null) {
            log.error("Cannot get file with no workspace defined!");
            throw new WrongUsageException();
        }

        try {
            // Listen to the get event
            //TODO: remove/inject
            addGetEventListeners();

            // Get latest on a project
            return getLatest(workspace, filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new CannotGetFileException(e);
        } finally {
            // Delete the workspace for clean up
            this.close();
        }
    }

    private void addGetEventListeners() {
        // Adding a get operation started event listener, this is fired once per
        // get call
        GetOperationStartedListener getOperationStartedListener = new GetOperationStartedListener();
        tpc.getVersionControlClient().getEventEngine()
                .addOperationStartedListener(getOperationStartedListener);

        // Adding a get event listener, this fired once per get operation(which
        // might be multiple times per get call)
        GetEventListener getListener = new GetEventListener();
        tpc.getVersionControlClient().getEventEngine()
                .addGetListener(getListener);

        // Adding a get operation completed event listener, this is fired once
        // per get call
        GetOperationCompletedListener getOperationCompletedListener = new GetOperationCompletedListener();
        tpc.getVersionControlClient().getEventEngine()
                .addOperationCompletedListener(getOperationCompletedListener);
    }

    private FileInputStream getLatest(Workspace workspace, String filePath) throws FileNotFoundException {
        ItemSpec spec = new ItemSpec(TFS.MAPPING_LOCAL_PATH,
                RecursionType.FULL);
        GetRequest request = new GetRequest(spec, LatestVersionSpec.INSTANCE);
        workspace.get(request, GetOptions.GET_ALL);
        log.debug(workspace.getServerName());
        log.debug(workspace.getQualifiedName());
        log.debug(workspace.getServerURI());
        String path = mappingServerPath + '/' + filePath;
        workspace.getClient().getItem(path);
        String fileName = System.getProperty("java.io.tmpdir") + File.pathSeparator + "tfsgetter" + System.currentTimeMillis();
        workspace.getClient().getItem(path, LatestVersionSpec.INSTANCE).downloadFile(workspace.getClient(), fileName);
        return new FileInputStream(fileName);
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
