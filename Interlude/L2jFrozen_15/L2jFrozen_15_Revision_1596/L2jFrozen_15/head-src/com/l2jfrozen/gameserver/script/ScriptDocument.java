package com.l2jfrozen.gameserver.script;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * L2JFrozen
 */
public class ScriptDocument
{
	private Document document;
	private final String name;
	
	public ScriptDocument(final String name, final InputStream input)
	{
		this.name = name;
		
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try
		{
			final DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.parse(input);
			
		}
		catch (final SAXException sxe)
		{
			// Error generated during parsing)
			Exception x = sxe;
			if (sxe.getException() != null)
			{
				x = sxe.getException();
			}
			x.printStackTrace();
			
		}
		catch (ParserConfigurationException | IOException pce)
		{
			// Parser with specified options can't be built
			pce.printStackTrace();
		}
		finally
		{
			
			if (input != null)
			{
				try
				{
					input.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public Document getDocument()
	{
		return document;
	}
	
	public String getName()
	{
		return name;
	}
	
	@Override
	public String toString()
	{
		return name;
	}
	
}
