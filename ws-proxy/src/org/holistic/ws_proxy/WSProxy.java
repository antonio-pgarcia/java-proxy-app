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

// Referenced classes of package org.holistic.ws_proxy:
//            WSProxyHelper

public class WSProxy extends HttpServlet implements Servlet
{

    public WSProxy()
    {
        log = LogFactory.getLog("[WSProxy]");
    }

    public void init()
        throws ServletException
    {
        String m_strEndPoint = null;
        String m_strTimeOut = null;
        String m_strProxyHost = null;
        String m_strProxyPort = null;
        m_strEndPoint = getInitParameter("endpoint");
        log.debug("Recuperamos parametro de inicializacion del Proxy: endPoint = " + m_strEndPoint);
        m_strTimeOut = getInitParameter("timeout");
        log.debug("Recuperamos parametro de inicializacion del Proxy: timeout = " + m_strTimeOut);
        m_strProxyHost = getInitParameter("proxyHost");
        log.debug("Recuperamos parametro de inicializacion del Proxy: proxyHost = " + m_strProxyHost);
        m_strProxyPort = getInitParameter("proxyPort");
        log.debug("Recuperamos parametro de inicializacion del Proxy: proxyPort = " + m_strProxyPort);
        if(m_strEndPoint != null && m_strTimeOut != null)
        {
            m_objWSProxyHelper = new WSProxyHelper(m_strEndPoint, Integer.parseInt(m_strTimeOut));
            if(m_strProxyHost != null && m_strProxyPort != null && !m_strProxyHost.equals("") && !m_strProxyPort.equals(""))
                m_objWSProxyHelper.set_proxy(m_strProxyHost, m_strProxyPort);
        } else
        {
            log.error("Parametos invalidos (endpoint or timeout)");
        }
    }

    protected WSProxyHelper get_proxyhelper()
        throws Exception
    {
        if(m_objWSProxyHelper == null)
        {
            log.info("Proxy no inicializado");
            throw new Exception("Proxy no inicializado");
        } else
        {
            return m_objWSProxyHelper;
        }
    }

    protected void set_proxyhelper(WSProxyHelper objValue)
    {
        m_objWSProxyHelper = objValue;
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
        throws ServletException
    {
        log.info("Accedo");
        try
        {
            get_proxyhelper().processRequest(request, response);
        }
        catch(Exception m_objException)
        {
            log.info("Se ha producido una excepcion: " + m_objException.getMessage());
            try
            {
                response.sendError(502, "Error");
            }
            catch(IOException m_objIOException)
            {
                log.info("Se ha producido una excepcion al establecer el error 504: " + m_objIOException.getMessage());
                throw new ServletException("[WSProxy] " + m_objIOException.getMessage());
            }
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        processRequest(request, response);
    }

    private WSProxyHelper m_objWSProxyHelper;
    private Log log;
}
