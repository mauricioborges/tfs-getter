package com.github.mauricioborges;

import com.microsoft.tfs.core.TFSTeamProjectCollection;
import com.microsoft.tfs.core.clients.versioncontrol.GetOptions;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.GetRequest;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.RecursionType;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace;
import com.microsoft.tfs.core.clients.versioncontrol.specs.ItemSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.LatestVersionSpec;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class TFSRepository extends Loggable implements Repository {
    private final Workspace workspace;
    private final String rootPath;
    private TFSTeamProjectCollection tpc;

    public TFSRepository(Workspace workspace, String rootPath, TFSTeamProjectCollection tpc) {
        this.tpc = tpc;
        this.workspace = workspace;
        this.rootPath = rootPath;
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
        String path = rootPath + '/' + filePath;
        workspace.getClient().getItem(path);
        String fileName = System.getProperty("java.io.tmpdir") + File.pathSeparator + "tfsgetter" + System.currentTimeMillis();
        workspace.getClient().getItem(path, LatestVersionSpec.INSTANCE).downloadFile(workspace.getClient(), fileName);
        return new FileInputStream(fileName);
    }

    @Override
    public void close() {
        tpc.getVersionControlClient().deleteWorkspace(workspace);
        log.debug("Deleted the workspace");
    }
}
