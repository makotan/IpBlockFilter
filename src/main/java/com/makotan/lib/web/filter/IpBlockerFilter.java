package com.makotan.lib.web.filter;

import com.makotan.lib.web.filter.ipblocker.BlockingStatus;
import com.makotan.lib.web.filter.ipblocker.IpBlocker;
import com.makotan.lib.web.filter.ipblocker.IpBlockerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * Created by makotan on 2016/12/11.
 */
public class IpBlockerFilter implements Filter {

    private IpBlocker ipBlocker;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Map<String,String> initMap = new HashMap<>();
        Enumeration<String> parameterNames = filterConfig.getInitParameterNames();
        while (parameterNames.hasMoreElements()) {
            String name = parameterNames.nextElement();
            initMap.put(name , filterConfig.getInitParameter(name));
        }
        initIpBlocker(initMap);
    }

    public void initIpBlocker(Map<String,String> initMap) {
        ipBlocker = IpBlockerFactory.get().getIpBlocker(initMap);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        String remoteAddr = getRemoteAddr(servletRequest);
        Optional<BlockingStatus> blockingStatus = ipBlocker.checkBlock(remoteAddr);
        if (blockingStatus.isPresent()) {
            blockResponse(blockingStatus.get(), servletRequest, servletResponse, filterChain);
        } else {
            try {
                filterChain.doFilter(servletRequest, servletResponse);
            } finally {
                calcBlockingStatus(servletRequest, servletResponse);
            }
        }
    }

    /** block時のレスポンスを返す */
    protected void blockResponse(BlockingStatus blockingStatus, ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse res = (HttpServletResponse) servletResponse;
        res.setStatus(blockingStatus.getStatus());
    }

    /** Blockの処理が必要かどうか判断する */
    protected boolean useCalcBlock(ServletRequest servletRequest, ServletResponse servletResponse) {
        return true;
    }

    protected void calcBlockingStatus(ServletRequest servletRequest, ServletResponse servletResponse) {
        if (useCalcBlock(servletRequest, servletResponse)) {
            String remoteAddr = getRemoteAddr(servletRequest);
            ipBlocker.calcBlock(remoteAddr);
        }
    }

    protected String getRemoteAddr(ServletRequest servletRequest) {
        String forwardedFor = ((HttpServletRequest) servletRequest).getHeader("X-Forwarded-For");
        return Optional.ofNullable(forwardedFor).orElseGet(servletRequest::getRemoteAddr);
    }

    @Override
    public void destroy() {

    }
}
