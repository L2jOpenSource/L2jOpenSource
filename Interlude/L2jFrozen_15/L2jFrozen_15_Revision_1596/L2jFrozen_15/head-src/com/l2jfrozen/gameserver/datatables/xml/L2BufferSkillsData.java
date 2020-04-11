package com.l2jfrozen.gameserver.datatables.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author ReynalDev
 */
public class L2BufferSkillsData
{
	private static final Logger LOGGER = Logger.getLogger(L2BufferSkillsData.class);
	
	private final Map<Integer, BuffData> buffTable = new HashMap<>();
	
	private static class SingletonHolder
	{
		protected static final L2BufferSkillsData instance = new L2BufferSkillsData();
	}
	
	public static L2BufferSkillsData getInstance()
	{
		return SingletonHolder.instance;
	}
	
	public L2BufferSkillsData()
	{
		loadData();
	}
	
	private void loadData()
	{
		String path = "data/xml/bufferSkillData.xml";
		try
		{
			File fXmlFile = new File(path);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			
			// optional, but recommended read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();
			
			NodeList nList = doc.getElementsByTagName("buff");
			
			for (int temp = 0; temp < nList.getLength(); temp++)
			{
				Node nNode = nList.item(temp);
				
				if (nNode.getNodeType() == Node.ELEMENT_NODE)
				{
					Element eElement = (Element) nNode;
					
					int buffKey = Integer.parseInt(eElement.getAttribute("key"));
					int buffSkillId = Integer.parseInt(eElement.getAttribute("id"));
					int buffSkillLevel = Integer.parseInt(eElement.getAttribute("level"));
					int buffSkillTime = Integer.parseInt(eElement.getAttribute("time"));
					
					buffTable.put(buffKey, new BuffData(buffSkillId, buffSkillLevel, buffSkillTime));
				}
			}
			
			LOGGER.info("Loaded " + buffTable.size() + " buffs for NPC Buffer.");
		}
		catch (Exception e)
		{
			LOGGER.error("Could not read " + path, e);
		}
	}
	
	public Map<Integer, BuffData> getBuffTable()
	{
		return buffTable;
	}
	
	public class BuffData
	{
		int skillId;
		int skillLevel;
		int skillTime;
		
		public BuffData(int skillId, int skillLevel, int skillTime)
		{
			this.skillId = skillId;
			this.skillLevel = skillLevel;
			this.skillTime = skillTime * 60;
		}
		
		public int getSkillId()
		{
			return skillId;
		}
		
		public int getSkillLevel()
		{
			return skillLevel;
		}
		
		/**
		 * @return time in <b>minutes</b>
		 */
		public int getSkillTime()
		{
			return skillTime;
		}
	}
	
}
