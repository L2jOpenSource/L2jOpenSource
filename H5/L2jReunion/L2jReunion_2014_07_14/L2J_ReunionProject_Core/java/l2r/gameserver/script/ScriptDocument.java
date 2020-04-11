/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.script;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 */
public class ScriptDocument
{
	private static final Logger _log = LoggerFactory.getLogger(ScriptDocument.class);
	
	private Document _document;
	private final String _name;
	
	public ScriptDocument(String name, InputStream input)
	{
		_name = name;
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try
		{
			DocumentBuilder builder = factory.newDocumentBuilder();
			_document = builder.parse(input);
			
		}
		catch (SAXException sxe)
		{
			// Error generated during parsing)
			Exception x = sxe;
			if (sxe.getException() != null)
			{
				x = sxe.getException();
			}
			_log.warn(getClass().getSimpleName() + ": " + x.getMessage());
		}
		catch (ParserConfigurationException pce)
		{
			// Parser with specified options can't be built
			_log.warn("", pce);
			
		}
		catch (IOException ioe)
		{
			// I/O error
			_log.warn("", ioe);
		}
	}
	
	public Document getDocument()
	{
		return _document;
	}
	
	/**
	 * @return Returns the _name.
	 */
	public String getName()
	{
		return _name;
	}
	
	@Override
	public String toString()
	{
		return _name;
	}
	
}
