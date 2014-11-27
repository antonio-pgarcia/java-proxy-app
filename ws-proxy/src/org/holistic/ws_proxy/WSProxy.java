/*----------------------------------------------------------------------*
 * HolisticView/MetaKnowledge						*
 *     									*
 *									*
 * Copyright Notice:                                                	*
 * Free use of this library is permitted under the guidelines and   	*
 * in accordance with the most current version of the Common Public 	*
 * License.                                                         	*
 * http://www.opensource.org/licenses/cpl.php                       	*
 *									*
 *									*
 *									*
 *----------+-----------------------------------------------------------*
 * PACKAGE  | package org.holistic.ws_proxy;                            *
 *----------+-----------------------------------------------------------*
 * MODULE   | public class WSProxy extends HttpServlet                  *
 *----------+-----------------------------------------------------------*
 *									*
 *									*
 *----------------------------------------------------------------------*
 * CREATED	    							*
 *----------------------------------------------------------------------*
 * AGARCIA/04-2007							*
 *									*
 *									*
 *----------------------------------------------------------------------*
 * MODIFIED 	   							*
 *----------------------------------------------------------------------*
 * 									*
 *									*
 *----------------------------------------------------------------------*
 * CHANGE LOG    							*
 *----------------------------------------------------------------------*
 *									*
 *									*
 *									*
 *									*
 *----------------------------------------------------------------------*
 * NOTES 	   							*
 *----------------------------------------------------------------------*
 *									*
 *									*
 *									*
 *									*
 *									*
 *									*
 *----------------------------------------------------------------------*/
package org.holistic.ws_proxy;

import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class WSProxy extends HttpServlet implements Servlet {
    private final static String MyLogger= "[Java-Proxy-App]"
    private WSProxyHelper m_objWSProxyHelper;
    private Log log;
    
    public WSProxy() {
        log = LogFactory.getLog(MyLogger);
    }

    public void init() throws ServletException {
        String m_strEndPoint = null;
        String m_strTimeOut = null;
        String m_strProxyHost = null;
        String m_strProxyPort = null;
        
        m_strEndPoint = getInitParameter("endpoint");
        m_strTimeOut = getInitParameter("timeout");
        m_strProxyHost = getInitParameter("proxyHost");        
        m_strProxyPort = getInitParameter("proxyPort");
        
        log.debug("Init parameter::endPoint---> " + m_strEndPoint);
        log.debug("Init parameter::timeout----> " + m_strTimeOut);
        log.debug("Init parameter::proxyHost--> " + m_strProxyHost);
        log.debug("Init parameter::proxyPort--> " + m_strProxyPort);

        if(m_strEndPoint != null && m_strTimeOut != null) {
            m_objWSProxyHelper = new WSProxyHelper(m_strEndPoint, Integer.parseInt(m_strTimeOut));
            if(m_strProxyHost != null && m_strProxyPort != null && !m_strProxyHost.equals("") && !m_strProxyPort.equals(""))
                m_objWSProxyHelper.set_proxy(m_strProxyHost, m_strProxyPort);
        } else
            log.error("Invalid parameter (endpoint|timeout)");
    }

    protected WSProxyHelper get_proxyhelper() throws Exception {
        if(m_objWSProxyHelper == null) {
            log.info("Java-Proxy-App Not initialized"); 
            throw new Exception("Java-Proxy-App Not initialized");
        } else
            return m_objWSProxyHelper;
    }

    protected void set_proxyhelper(WSProxyHelper objValue) {
        m_objWSProxyHelper = objValue;
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            get_proxyhelper().processRequest(request, response);
        } catch(Exception m_objException) {
            log.info("An exception has been catched: " + m_objException.getMessage());
            try {
                response.sendError(502, "Error");
            } catch(IOException m_objIOException) {
                log.info("Exception catched setting http status code: " + m_objIOException.getMessage());
                throw new ServletException(MyLogger + " " + m_objIOException.getMessage());
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
}
