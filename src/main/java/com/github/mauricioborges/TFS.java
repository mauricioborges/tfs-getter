package com.github.mauricioborges;

import com.microsoft.tfs.core.TFSTeamProjectCollection;
import com.microsoft.tfs.core.clients.versioncontrol.GetOptions;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspaceLocation;
import com.microsoft.tfs.core.clients.versioncontrol.WorkspacePermissionProfile;
import com.microsoft.tfs.core.clients.versioncontrol.path.LocalPath;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.GetRequest;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.RecursionType;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.WorkingFolder;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Workspace;
import com.microsoft.tfs.core.clients.versioncontrol.specs.ItemSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.LatestVersionSpec;
import com.microsoft.tfs.core.httpclient.Credentials;
import com.microsoft.tfs.core.httpclient.DefaultNTCredentials;
import com.microsoft.tfs.core.httpclient.UsernamePasswordCredentials;
import com.microsoft.tfs.core.util.CredentialsUtils;
import com.microsoft.tfs.core.util.URIUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;

public class TFS {

    private static final String NO_PROXY_SERVER = "";
    private TFSTeamProjectCollection tpc;
    private String server;
    private String user;
    private Workspace workspace;
    private String rootPath;
    private Logger log = Logger.getLogger(this.getClass());

    public static String MAPPING_LOCAL_PATH = System.getProperty("java.io.tmpdir") + File.pathSeparator + "tfs-mapping" + System.currentTimeMillis();

    public TFS at(String server) {
        this.server = server;
        return this;
    }

    public TFS withUser(String user) {
        this.user = user;
        return this;
    }

    public TFS andPassword(String password) {
        System.setProperty(
                "com.microsoft.tfs.jni.native.base-directory",
                "C:\\Users\\mauricio.silva\\Downloads\\TFS-SDK-11.0.0.1306\\TFS-SDK-11.0.0\\redist\\native");

        tpc = connectToTFS(user, password, NO_PROXY_SERVER, server);
        return this;
    }

    public TFSTeamProjectCollection connectToTFS(String username, String password, String proxyUrl, String collectionUrl)
    {
        TFSTeamProjectCollection tpc = null;
        Credentials credentials;

        if ((username == null || username.length() == 0) && CredentialsUtils.supportsDefaultCredentials())
        {
            credentials = new DefaultNTCredentials();
        }
        else
        {
            credentials = new UsernamePasswordCredentials(username, password);
        }
        URI httpProxyURI = null;

        if (proxyUrl != null && proxyUrl.length() > 0)
        {
            try
            {
                httpProxyURI = new URI(proxyUrl);
            }
            catch (URISyntaxException e)
            {
                // Do Nothing
            }
        }

        tpc = new TFSTeamProjectCollection(URIUtils.newURI(collectionUrl), credentials);
        return tpc;
    }

    public TFS on(String rootPath) {
        this.rootPath = rootPath;
        this.workspace = createAndMapWorkspace(rootPath);
        return this;
    }

    public FileInputStream get(String filePath) {


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
            tpc.getVersionControlClient().deleteWorkspace(workspace);
            log.debug("Deleted the workspace");
        }
    }

    private Workspace createAndMapWorkspace(String mappingServerPath) {
        String workspaceName = "wkspc"
                + System.currentTimeMillis();
        Workspace workspace = null;

        // Get the workspace
        workspace = tpc.getVersionControlClient().tryGetWorkspace(
                MAPPING_LOCAL_PATH);

        // Create and map the workspace if it does not exist
        if (workspace == null) {
            workspace = tpc.getVersionControlClient().createWorkspace(null,
                    workspaceName, "Sample workspace comment",
                    WorkspaceLocation.SERVER, null,
                    WorkspacePermissionProfile.getPrivateProfile());

            // Map the workspace
            WorkingFolder workingFolder = new WorkingFolder(
                    mappingServerPath,
                    LocalPath.canonicalize(MAPPING_LOCAL_PATH));
            workspace.createWorkingFolder(workingFolder);
        }
        log.debug("Workspace '" + workspaceName
                + "' now exists and is mapped");

        return workspace;
    }

    private void addGetEventListeners() {
        // Adding a get operation started event listener, this is fired once per
        // get call
        SampleGetOperationStartedListener getOperationStartedListener = new SampleGetOperationStartedListener();
        tpc.getVersionControlClient().getEventEngine()
                .addOperationStartedListener(getOperationStartedListener);

        // Adding a get event listener, this fired once per get operation(which
        // might be multiple times per get call)
        SampleGetEventListener getListener = new SampleGetEventListener();
        tpc.getVersionControlClient().getEventEngine()
                .addGetListener(getListener);

        // Adding a get operation completed event listener, this is fired once
        // per get call
        SampleGetOperationCompletedListener getOperationCompletedListener = new SampleGetOperationCompletedListener();
        tpc.getVersionControlClient().getEventEngine()
                .addOperationCompletedListener(getOperationCompletedListener);
    }

    private FileInputStream getLatest(Workspace workspace, String filePath) throws FileNotFoundException {
        ItemSpec spec = new ItemSpec(MAPPING_LOCAL_PATH,
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


}