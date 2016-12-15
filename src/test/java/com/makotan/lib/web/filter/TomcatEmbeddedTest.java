package com.makotan.lib.web.filter;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleState;
import org.apache.catalina.startup.Tomcat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by makotan on 2016/12/11.
 */
public class TomcatEmbeddedTest extends BaseWebFilterTest {

    protected void setupContext() throws ServletException {
        Context context = tomcat.addWebapp("/", ".");
        Tomcat.addServlet(context, "dummy", new DummyServlet());
        context.addServletMappingDecoded("/dummy", "dummy");
    }

    @Test
    public void testCallGetDummy() throws IOException {
        String url = "http://localhost:" + getTomcatPort() + "/dummy";
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        assertThat(response.code()).isEqualTo(200);
        assertThat(response.body().string()).isEqualTo("ok");
    }

}
