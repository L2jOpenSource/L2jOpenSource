package com.l2jfrozen.gameserver.datatables.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jfrozen.Config;

/**
 * Based on mrTJO's implementation.
 * @author Zoey76
 */
public class ExperienceData
{
	private static Logger LOGGER = Logger.getLogger(ExperienceData.class);
	
	private byte MAX_LEVEL;
	private byte MAX_PET_LEVEL;
	
	private final Map<Integer, Long> expTable = new HashMap<>();
	
	public ExperienceData()
	{
		loadData();
	}
	
	private void loadData()
	{
		final File xml = new File(Config.DATAPACK_ROOT, "data/xml/experience.xml");
		if (!xml.exists())
		{
			LOGGER.warn(getClass().getSimpleName() + ": experience.xml not found!");
			return;
		}
		
		Document doc = null;
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setIgnoringComments(true);
		try
		{
			doc = factory.newDocumentBuilder().parse(xml);
		}
		catch (final Exception e)
		{
			LOGGER.warn("Could not parse experience.xml: " + e.getMessage());
			return;
		}
		
		final Node table = doc.getFirstChild();
		final NamedNodeMap tableAttr = table.getAttributes();
		
		MAX_LEVEL = (byte) (Byte.parseByte(tableAttr.getNamedItem("maxLevel").getNodeValue()) + 1);
		MAX_PET_LEVEL = (byte) (Byte.parseByte(tableAttr.getNamedItem("maxPetLevel").getNodeValue()) + 1);
		
		expTable.clear();
		
		NamedNodeMap attrs;
		Integer level;
		Long exp;
		for (Node experience = table.getFirstChild(); experience != null; experience = experience.getNextSibling())
		{
			if (experience.getNodeName().equals("experience"))
			{
				attrs = experience.getAttributes();
				level = Integer.valueOf(attrs.getNamedItem("level").getNodeValue());
				exp = Long.valueOf(attrs.getNamedItem("tolevel").getNodeValue());
				expTable.put(level, exp);
			}
		}
		
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + expTable.size() + " levels");
		LOGGER.info(getClass().getSimpleName() + ": Max Player Level is: " + (MAX_LEVEL - 1));
		LOGGER.info(getClass().getSimpleName() + ": Max Pet Level is: " + (MAX_PET_LEVEL - 1));
	}
	
	public long getExpForLevel(final int level)
	{
		return expTable.get(level);
	}
	
	public byte getMaxLevel()
	{
		return MAX_LEVEL;
	}
	
	public byte getMaxPetLevel()
	{
		return MAX_PET_LEVEL;
	}
	
	public static ExperienceData getInstance()
	{
		return SingletonHolder.instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ExperienceData instance = new ExperienceData();
	}
}
