package com.github.mauricioborges;

import com.microsoft.tfs.core.TFSTeamProjectCollection;
import com.microsoft.tfs.core.httpclient.Credentials;
import com.microsoft.tfs.core.httpclient.DefaultNTCredentials;
import com.microsoft.tfs.core.httpclient.UsernamePasswordCredentials;
import com.microsoft.tfs.core.util.CredentialsUtils;
import com.microsoft.tfs.core.util.URIUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class TFS extends Loggable {

    private static final String NO_PROXY_SERVER = "";
    private TFSTeamProjectCollection tpc;
    private String server;
    private String user;

    public static String MAPPING_LOCAL_PATH = System.getProperty("java.io.tmpdir") + File.pathSeparator + "tfs-mapping" + System.currentTimeMillis();


    public TFS at(String server) {
        this.server = server;
        return this;
    }

    public TFS as(String user) {
        if (this.server == null) {
            log.error("Cannot connect without server defined!");
            throw new WrongUsageException();
        }
        this.user = user;
        return this;
    }

    public TFS with(String password) {
        if (user == null) {
            log.error("Cannot connect without username for login!");
            throw new WrongUsageException();
        }
        System.setProperty(
                "com.microsoft.tfs.jni.native.base-directory",
                "C:\\Users\\mauricio.silva\\Downloads\\TFS-SDK-11.0.0.1306\\TFS-SDK-11.0.0\\redist\\native");

        this.tpc = TFS.connectToTFS(user, password, NO_PROXY_SERVER, server);
        return this;
    }

    public VCSConnection getConnection() {
        if (this.tpc == null) {
            log.error("Cannot connect without valid TFSTeamProjectCollection object!");
            throw new WrongUsageException();
        }
        return new TFSVCSConnection(this.tpc);
    }


    private static TFSTeamProjectCollection connectToTFS(String username, String password, String proxyUrl, String collectionUrl) {
        TFSTeamProjectCollection tpc = null;
        Credentials credentials;

        if ((username == null || username.length() == 0) && CredentialsUtils.supportsDefaultCredentials()) {
            credentials = new DefaultNTCredentials();
        } else {
            credentials = new UsernamePasswordCredentials(username, password);
        }
        URI httpProxyURI = null;

        if (proxyUrl != null && proxyUrl.length() > 0) {
            try {
                httpProxyURI = new URI(proxyUrl);
            } catch (URISyntaxException e) {
                Logger.getLogger(TFS.class).warn("proxy URL invalid");
            }
        }

        tpc = new TFSTeamProjectCollection(URIUtils.newURI(collectionUrl), credentials);
        return tpc;
    }
}