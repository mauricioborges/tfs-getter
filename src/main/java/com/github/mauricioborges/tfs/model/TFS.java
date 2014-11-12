package com.github.mauricioborges.tfs.model;

import com.github.mauricioborges.model.Loggable;
import com.github.mauricioborges.model.VCSConnection;
import com.github.mauricioborges.model.exception.WrongUsageException;
import com.microsoft.tfs.core.TFSTeamProjectCollection;
import com.microsoft.tfs.core.httpclient.Credentials;
import com.microsoft.tfs.core.httpclient.DefaultNTCredentials;
import com.microsoft.tfs.core.httpclient.UsernamePasswordCredentials;
import com.microsoft.tfs.core.util.CredentialsUtils;
import com.microsoft.tfs.core.util.URIUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

public class TFS extends Loggable {

    public static final String COM_MICROSOFT_TFS_JNI_NATIVE_BASE_DIRECTORY = "com.microsoft.tfs.jni.native.base-directory";
    private static final String NO_PROXY_SERVER = "";
    public static String MAPPING_LOCAL_PATH = System.getProperty("java.io.tmpdir") + File.pathSeparator + "tfs-mapping" + System.currentTimeMillis();
    private TFSTeamProjectCollection tpc;
    private String server;
    private String user;

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
        loadNativeJniLibsDirectory();

        this.tpc = TFS.connectToTFS(user, password, NO_PROXY_SERVER, server);
        return this;
    }

    private void loadNativeJniLibsDirectory() {
        //TODO: extract property loading
        String jniBaseDirectory = null;
        InputStream tfsPropertiesFile = TFS.class.getClassLoader().getResourceAsStream("tfs.properties");
        if (tfsPropertiesFile == null) {
            setDefaultJniLibsDirectory();
            return;
        }
        try {
            Properties prop = new Properties();
            prop.load(tfsPropertiesFile);
            jniBaseDirectory = prop.getProperty(COM_MICROSOFT_TFS_JNI_NATIVE_BASE_DIRECTORY);

            System.setProperty(COM_MICROSOFT_TFS_JNI_NATIVE_BASE_DIRECTORY, jniBaseDirectory);
            if (System.getProperty(COM_MICROSOFT_TFS_JNI_NATIVE_BASE_DIRECTORY) == null) {
                setDefaultJniLibsDirectory();
            }
        } catch (IOException e) {
            log.info("cannot find tfs.properties. Using default JNI base directory");
            setDefaultJniLibsDirectory();
        }

    }

    private void setDefaultJniLibsDirectory() {
        System.setProperty(COM_MICROSOFT_TFS_JNI_NATIVE_BASE_DIRECTORY, "native");

    }

    public VCSConnection getConnection() {
        if (this.tpc == null) {
            log.error("Cannot connect without valid TFSTeamProjectCollection object!");
            throw new WrongUsageException();
        }
        return new TFSVCSConnection(this.tpc);
    }
}