package org.codehaus.groovy.grails.web.sitemesh;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.codehaus.groovy.grails.web.servlet.mvc.GrailsWebRequest;
import org.codehaus.groovy.grails.web.util.WebUtils;

import com.opensymphony.module.sitemesh.PageParser;
import com.opensymphony.module.sitemesh.PageParserSelector;
import com.opensymphony.sitemesh.Content;
import com.opensymphony.sitemesh.ContentProcessor;
import com.opensymphony.sitemesh.webapp.SiteMeshWebAppContext;

public class GrailsContentBufferingResponse extends HttpServletResponseWrapper {
    private final GrailsPageResponseWrapper pageResponseWrapper;
    private final ContentProcessor contentProcessor;
    private final SiteMeshWebAppContext webAppContext;

    public GrailsContentBufferingResponse(HttpServletResponse response, final ContentProcessor contentProcessor, final SiteMeshWebAppContext webAppContext) {
        super(new GrailsPageResponseWrapper(webAppContext.getRequest(), response, new PageParserSelector() {
            public boolean shouldParsePage(String contentType) {
                return contentProcessor.handles(contentType);
            }

            public PageParser getPageParser(String contentType) {
                // Migration: Not actually needed by PageResponseWrapper, so long as getPage() isn't called.
                return null;
            }
        }){
            public void setContentType(String contentType) {
                webAppContext.setContentType(contentType);
                super.setContentType(contentType);
            }
        });
        this.contentProcessor = contentProcessor;
        this.webAppContext = webAppContext;
        pageResponseWrapper = (GrailsPageResponseWrapper) getResponse();
    }

    public boolean isUsingStream() {
        return pageResponseWrapper.isUsingStream();
    }

    public boolean isActive() {
        GrailsPageResponseWrapper superResponse= (GrailsPageResponseWrapper) getResponse();
        return superResponse.isSitemeshActive() || superResponse.isGspSitemeshActive();
    }

    public Content getContent() throws IOException {
    	GSPSitemeshPage content=(GSPSitemeshPage)webAppContext.getRequest().getAttribute(GrailsPageFilter.GSP_SITEMESH_PAGE);
    	if(content != null && content.isUsed()) {
    		return content;
    	} else {
	        char[] data = pageResponseWrapper.getContents();
	        if (data != null) {
	            return contentProcessor.build(data, webAppContext);
	        } else {
	            return null;
	        }
    	}
    }

    public void sendError(int sc) throws IOException {
        GrailsWebRequest webRequest = WebUtils.retrieveGrailsWebRequest();
        try {
            super.sendError(sc);
        } finally {
            WebUtils.storeGrailsWebRequest(webRequest);
        }
    }

    public void sendError(int sc, String msg) throws IOException {
        GrailsWebRequest webRequest = WebUtils.retrieveGrailsWebRequest();
        try {
            super.sendError(sc, msg);
        } finally {
            WebUtils.storeGrailsWebRequest(webRequest);
        }
    }
}
