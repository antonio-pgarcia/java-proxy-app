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
 * MODULE   | public class BufferedInputStreamReader                    *
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

public class BufferedInputStreamReader extends BufferedInputStream
{

    public BufferedInputStreamReader(InputStream objValue, int iValue)
    {
        super(objValue);
        m_iChunkSize = 0;
        m_iCounter = 0;
        set_chunksize(iValue);
        m_buffer = new byte[get_chunksize()];
    }

    private void set_chunksize(int iValue)
    {
        m_iChunkSize = iValue;
    }

    private int get_chunksize()
    {
        return m_iChunkSize;
    }

    public byte[] nextChunk()
        throws Exception
    {
        int m_iBytes = 0;
        m_iBytes = read(m_buffer);
        if(m_iBytes == -1)
            return null;
        if(m_iBytes == m_buffer.length)
        {
            return m_buffer;
        } else
        {
            byte m_baLastChunk[] = new byte[m_iBytes];
            System.arraycopy(m_buffer, 0, m_baLastChunk, 0, m_iBytes);
            return m_baLastChunk;
        }
    }

    private int m_iChunkSize;
    private int m_iCounter;
    private byte m_buffer[];
}
