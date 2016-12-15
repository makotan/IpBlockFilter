package com.makotan.lib.web.filter;

import okhttp3.Request;
import okhttp3.Response;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.junit.Test;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by makotan on 2016/12/14.
 */
public class IpBlockTest extends BaseWebFilterTest {

    protected void setupContext() throws ServletException {
        Context context = tomcat.addWebapp("/", ".");
        Tomcat.addServlet(context, "dummy", new DummyServlet());
        context.addServletMappingDecoded("/dummy", "dummy");
        FilterDef filterDef = new FilterDef();
        filterDef.setDisplayName("ipBlocker");
        filterDef.setFilter(new IpBlockerFilter());
        filterDef.setFilterName("ipBlocker");
        context.addFilterDef(filterDef);
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
