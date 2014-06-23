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
 * MODULE   | public class WSProxyHelper                                *
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

import java.io.*;
import java.net.*;
import java.security.cert.Certificate;
import java.util.*;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import sun.misc.BASE64Encoder;
import sun.misc.CharacterEncoder;

// Referenced classes of package org.holistic.ws_proxy:
//            BufferedInputStreamReader

public class WSProxyHelper
{
    private String HTTP_GET;
    private String HTTP_POST;
    private String COOKIE_SESSION;
    private String EXTRA_COOKIE;
    private String m_strEndPoint;
    private Log log;

    /**
     * 
     * @param strEndPoint The reverse proxy endpoint host
     * @param iTimeOut The timeout for network operations in seconds
     */
    public WSProxyHelper(String strEndPoint, int iTimeOut)
    {
        HTTP_GET = "GET";
        HTTP_POST = "POST";
        COOKIE_SESSION = "JSESSIONID";
        EXTRA_COOKIE = "_endPoint";
        log = LogFactory.getLog("[WSProxyHelper]");
        set_endpoint(strEndPoint);
        set_timeout(iTimeOut);
        log.debug("Se inicializa un WSProxyHelper con parametros endPoint: " + strEndPoint + " y timeOut: " + iTimeOut);
    }

    private void set_endpoint(String strValue)
    {
        m_strEndPoint = strValue;
    }

    private String get_endpoint()
    {
        return m_strEndPoint;
    }

    private void set_timeout(int iValue)
    {
        if(iValue > 0)
        {
            java.util.Properties m_objProperties = System.getProperties();
            m_objProperties.put("sun.net.client.defaultConnectTimeout", "" + iValue * 1000);
            m_objProperties.put("sun.net.client.defaultReadTimeout", "" + iValue * 1000);
            System.setProperties(m_objProperties);
        }
    }

    /**
     * 
     * @param strHost Proxy Host
     * @param strPort Proxy Port
     */
    public void set_proxy(String strHost, String strPort)
    {
        if(strHost != null && strPort != null)
        {
            java.util.Properties m_objProperties = System.getProperties();
            m_objProperties.put("proxySet", "true");
            m_objProperties.put("proxyHost", strHost);
            m_objProperties.put("proxyPort", strPort);
            System.setProperties(m_objProperties);
            log.debug("La comunicacion entre el WSProxyHelper y el endPoint(" + m_strEndPoint + ") se realiza a trav\351s del proxy " + strHost + ":" + strPort);
        }
    }

    /**
     * 
     * @param strURL URL to connect
     * @throws java.lang.Exception 
     * @return Returns HttpURLConnection object
     */
    public HttpURLConnection openurl(String strURL)
        throws Exception
    {
        HttpURLConnection m_objURLConn= null;
        URL m_objURL = new URL(strURL);
        m_objURLConn = (HttpURLConnection) m_objURL.openConnection();
        log.debug("Se abre la conexi\363n a " + strURL);
        m_objURLConn.setDoInput(true);
        m_objURLConn.setUseCaches(true);
        return m_objURLConn;
         
    }

    public String get_request(HttpServletRequest req)
    {
        String m_strRequest = get_endpoint() + req.getRequestURI();
        log.debug("La URI de la peticion es: " + req.getRequestURI());
        String m_strQueryString = req.getQueryString();
        if(m_strQueryString != null)
        {
            log.debug("El QueryString de la peticion es: " + m_strQueryString);
            if(m_strRequest.indexOf("?") < 0)
                m_strRequest = m_strRequest + "?" + m_strQueryString;
            else
                m_strRequest = m_strRequest + "&" + m_strQueryString;
        }
        return m_strRequest;
    }

    public String get_postdata(HttpServletRequest req)
        throws Exception
    {
        String m_strPostData = "";
        for(Enumeration m_objElement = req.getParameterNames(); m_objElement.hasMoreElements();)
        {
            String m_strName = (String)m_objElement.nextElement();
            String sValues[] = req.getParameterValues(m_strName);
            for(int iCont = 0; iCont < sValues.length; iCont++)
            {
                if(m_strPostData.length() > 0)
                    m_strPostData = m_strPostData + "&";
                m_strPostData = m_strPostData + m_strName + "=" + URLEncoder.encode(sValues[iCont], "UTF-8");
            }

        }

        log.debug("Los parametros enviados por POST son: " + m_strPostData);
        return m_strPostData;
    }

    public void set_headers2urlconn(HttpServletRequest req, URLConnection objURLConn)
        throws Exception
    {
        log.debug("Las cabeceras que se transmiten son:");
        String m_strName;
        String m_strValue;
        for(Enumeration m_objElement = req.getHeaderNames(); m_objElement.hasMoreElements(); log.debug("ClientToEndPoint Cabecera[" + m_strName + "] - Valor[" + m_strValue + "]"))
        {
            m_strName = (String)m_objElement.nextElement();
            m_strValue = req.getHeader(m_strName);
            if(m_strName.toUpperCase().equals("COOKIE") && m_strValue.indexOf(COOKIE_SESSION + "=") > 0)
                m_strValue = m_strValue.replaceAll(COOKIE_SESSION + "=", COOKIE_SESSION + "_PROXY=");
            if(m_strName.toUpperCase().equals("COOKIE") && m_strValue.indexOf(EXTRA_COOKIE) > 0)
                m_strValue = m_strValue.replaceAll(EXTRA_COOKIE, "");
            objURLConn.setRequestProperty(m_strName, m_strValue);
        }

        String m_RemoteAddr = req.getRemoteAddr();
        objURLConn.setRequestProperty("x-forwarded-for", m_RemoteAddr);
        log.debug("Transmitimos la ip del cliente del proxy como cabecera HTTP x-forwarded-for (" + m_RemoteAddr + ").");
        String cipherSuite = (String)req.getAttribute("javax.net.ssl.cipher_suite");
        if(cipherSuite != null && req.getAttribute("javax.net.ssl.peer_certificates") != null)
        {
            java.security.cert.X509Certificate certChain[] = (java.security.cert.X509Certificate[])req.getAttribute("javax.net.ssl.peer_certificates");
            java.security.cert.X509Certificate certStandar = certChain[0];
            m_strName = "entrust-client-certificate";
            String m_strTemp = (new BASE64Encoder()).encode(certStandar.getEncoded());
            m_strValue = m_strTemp.replaceAll("\r\n", "").replaceAll("\n", "");
            objURLConn.setRequestProperty(m_strName, m_strValue);
            log.debug("Transmitimos el certificado contenido atributo 'javax.net.ssl.peer_certificates' como cabecera http 'entrust-client-certificate'.");
            log.debug("Cabecera[" + m_strName + "] - Valor[" + m_strValue + "]");
        }
    }

    private void headers_endpoint2client(HttpServletResponse resp, URLConnection objURLConn)
        throws Exception
    {
        int m_iHeaderNumber = 1;
        String m_sHeaderName = objURLConn.getHeaderFieldKey(m_iHeaderNumber);
        for(String m_sHeaderValue= objURLConn.getHeaderField(m_iHeaderNumber); m_sHeaderName != null && m_sHeaderValue != null; m_sHeaderValue = objURLConn.getHeaderField(m_iHeaderNumber))
        {
            if(m_sHeaderName.equalsIgnoreCase("Set-Cookie")) {
                String m_sCookie = objURLConn.getHeaderField(m_iHeaderNumber);
                if(m_sCookie != null && !m_sCookie.equals(""))
                {
                    log.debug("La cookie que me devuelve el endPoint es: " + m_sCookie);
                    int m_posicion = m_sCookie.indexOf("=");
                    String m_sCookieModificada = m_sCookie.substring(0, m_posicion) + EXTRA_COOKIE + m_sCookie.substring(m_posicion);
                    log.debug("Al cliente le transmito la cookie modificada: " + m_sCookieModificada);
                    resp.addHeader("Set-Cookie", m_sCookieModificada);
                }
            } else
                if(!m_sHeaderName.equalsIgnoreCase("Content-Length") && !m_sHeaderName.equalsIgnoreCase("Transfer-Encoding")) {
                
                    if( m_sHeaderName.equalsIgnoreCase("Cache-Control") && m_sHeaderValue.equalsIgnoreCase("max-age=43200") ) {
                        log.debug("Tratamiento Header Cache-Control - eliminar");
                        //resp.addHeader(m_sHeaderName, "post-check=43200, pre-check=3600");
                        //resp.addHeader(m_sHeaderName, "post-check=300, pre-check=420, must-revalidate");
                        resp.addHeader(m_sHeaderName, "post-check=600, pre-check=601");
                    } else {
                        resp.addHeader(m_sHeaderName, m_sHeaderValue);
                        log.debug("EndPointToClient Cabecera[" + m_sHeaderName + "] - Valor[" + m_sHeaderValue + "]");
                    }
                }
            m_iHeaderNumber++;
            m_sHeaderName = objURLConn.getHeaderFieldKey(m_iHeaderNumber);
        }

    }

    private void get_endpointstream(HttpServletResponse resp, HttpURLConnection objURLConn)
        throws Exception
    {
        int m_iIndex = 0;
        byte m_objBuffer[] = (byte[])null;
        BufferedInputStreamReader m_objReader= null;
        
        headers_endpoint2client(resp, objURLConn);
        
        if( objURLConn.getResponseCode() < 400 ) {
            m_objReader= new BufferedInputStreamReader(objURLConn.getInputStream(), 8192);
        } else {
            m_objReader = new BufferedInputStreamReader(objURLConn.getErrorStream(), 8192);
        }
        
        BufferedOutputStream m_objOutput = new BufferedOutputStream(resp.getOutputStream());
        while((m_objBuffer = m_objReader.nextChunk()) != null) 
        {
            if(m_objBuffer.length > 0)
                m_objOutput.write(m_objBuffer);
            m_objOutput.flush();
        }
        if(m_objReader != null)
            m_objReader.close();
        if(m_objOutput != null)
        {
            m_objOutput.flush();
            m_objOutput.close();
        }
    }

    public void processRequest(HttpServletRequest req, HttpServletResponse resp)
        throws Exception
    {
        GregorianCalendar m_objGCalendar = new GregorianCalendar();
        long m_inicio = m_objGCalendar.getTimeInMillis();
        if(HTTP_GET.equals(req.getMethod().toUpperCase()))
            doGet(req, resp);
        else
            doPost(req, resp);
        m_objGCalendar = new GregorianCalendar();
        long m_fin = m_objGCalendar.getTimeInMillis();
        log.info("Tiempo de proceso de la peticion (mseg): " + (m_fin - m_inicio));
    }

    private void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws Exception
    {
        HttpURLConnection m_objURLConnection = null;
        m_objURLConnection= openurl(get_request(req));
        
       // resp.setStatus(m_objURLConnection.getResponseCode());
        
        m_objURLConnection.setDoOutput(true);
        m_objURLConnection.setRequestProperty("content-type", "application/x-www-form-urlencoded");
        set_headers2urlconn(req, m_objURLConnection);
        m_objURLConnection.setRequestProperty("host", m_objURLConnection.getURL().getHost() + ":" + m_objURLConnection.getURL().getPort());
        PrintWriter m_objOutput = new PrintWriter(m_objURLConnection.getOutputStream());
        m_objOutput.print(get_postdata(req));
        m_objOutput.close();
        
        resp.setStatus(m_objURLConnection.getResponseCode());
        if(m_objURLConnection.getContentType() != null)
            resp.setContentType(m_objURLConnection.getContentType());
        get_endpointstream(resp, m_objURLConnection);
    }

    private void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws Exception
    {
        HttpURLConnection m_objURLConnection = null;
        m_objURLConnection = openurl(get_request(req));
        
        // resp.setStatus(m_objURLConnection.getResponseCode());
        
        set_headers2urlconn(req, m_objURLConnection);
        m_objURLConnection.setRequestProperty("host", m_objURLConnection.getURL().getHost() + ":" + m_objURLConnection.getURL().getPort());
        
        resp.setStatus(m_objURLConnection.getResponseCode());
        if(m_objURLConnection.getContentType() != null)
            resp.setContentType(m_objURLConnection.getContentType());
        get_endpointstream(resp, m_objURLConnection);
    }
}
