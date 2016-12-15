package com.makotan.lib.web.filter;

import okhttp3.OkHttpClient;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.startup.Tomcat;
import org.junit.After;
import org.junit.Before;

import javax.servlet.ServletException;

/**
 * Created by makotan on 2016/12/11.
 */
public abstract class BaseWebFilterTest {

    /** The tomcat instance. */
    protected Tomcat tomcat;
    /** The temporary directory in which Tomcat and the app are deployed. */
    protected String workingDir = System.getProperty("java.io.tmpdir");

    protected OkHttpClient client = new OkHttpClient();

    @Before
    public void setup() throws Throwable {
        tomcat = new Tomcat();
        tomcat.setPort(0);
        tomcat.setBaseDir(workingDir);
        tomcat.getHost().setAppBase(workingDir);
        tomcat.getHost().setAutoDeploy(true);
        tomcat.getHost().setDeployOnStartup(true);

        setupContext();

        tomcat.start();
    }


    protected void setupContext() throws ServletException {

    }

    @After
    public void stop() throws Throwable {
        if (tomcat.getServer() != null && tomcat.getServer().getState() != LifecycleState.DESTROYED) {
            if (tomcat.getServer().getState() != LifecycleState.STOPPED) {
                tomcat.stop();
            }
            tomcat.destroy();
        }
    }

    protected int getTomcatPort() {
        return tomcat.getConnector().getLocalPort();
    }

}
