package com.l2jfrozen.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.l2jfrozen.Config;

/**
 * @author programmos
 */
public abstract class XmlEngine
{
	protected static final Logger LOGGER = Logger.getLogger(XmlEngine.class);
	
	private final File file;
	
	XmlEngine(final File f)
	{
		file = f;
		parseFile();
	}
	
	public void parseFile()
	{
		Document document = null;
		
		try
		{
			final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			document = factory.newDocumentBuilder().parse(file);
		}
		catch (final ParserConfigurationException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.error("Error loading configure XML: " + file.getName(), e);
		}
		catch (final SAXException e)
		{
			e.printStackTrace();
		}
		catch (final IOException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.error("Error loading file: " + file.getName(), e);
		}
		
		try
		{
			parseDocument(document);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			LOGGER.error("Error in file: " + file.getName(), e);
		}
	}
	
	public abstract void parseDocument(Document document) throws Exception;
	
	public List<Node> parseHeadStandart(final Document doc)
	{
		final List<Node> temp = new ArrayList<>();
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("record".equalsIgnoreCase(d.getNodeName()))
					{
						for (Node e = d.getFirstChild(); e != null; e = n.getNextSibling())
						{
							if ("value".equalsIgnoreCase(n.getNodeName()))
							{
								temp.add(d);
							}
						}
					}
				}
			}
		}
		
		return temp;
	}
	
}
