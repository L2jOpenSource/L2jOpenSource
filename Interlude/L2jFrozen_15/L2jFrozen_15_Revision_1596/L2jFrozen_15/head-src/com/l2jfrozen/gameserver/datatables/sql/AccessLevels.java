package com.l2jfrozen.gameserver.datatables.sql;

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

import com.l2jfrozen.gameserver.datatables.AccessLevel;

/**
 * @author FBIagent
 * @author Rayan L2EMU
 * @author ReynalDev
 */
public class AccessLevels
{
	private static final Logger LOGGER = Logger.getLogger(AccessLevels.class);
	
	private AccessLevel masterAccessLevel;
	private AccessLevel userAccessLevel;
	
	private Map<Integer, AccessLevel> accessLevels = new HashMap<>();
	
	public AccessLevels()
	{
		userAccessLevel = new AccessLevel(0, "User", Integer.decode("0xFFFFFF"), Integer.decode("0xFFFFFF"), false, false, false, true, false, true, true, true);
		accessLevels.put(userAccessLevel.getLevel(), userAccessLevel);
		
		loadData();
	}
	
	private void loadData()
	{
		String path = "config/access_level/accessLevels.xml";
		try
		{
			File fXmlFile = new File(path);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			
			// optional, but recommended read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();
			
			NodeList nList = doc.getElementsByTagName("access");
			
			int highestAccessLevel = 0;
			
			for (int temp = 0; temp < nList.getLength(); temp++)
			{
				Node nNode = nList.item(temp);
				
				if (nNode.getNodeType() == Node.ELEMENT_NODE)
				{
					Element element = (Element) nNode;
					
					int level = Integer.parseInt(element.getAttribute("level"));
					String name = element.getAttribute("name");
					String nameColor = element.getAttribute("nameColor");
					String titleColor = element.getAttribute("titleColor");
					boolean isGM = Boolean.parseBoolean(element.getAttribute("isGM"));
					boolean allowPeaceAttack = Boolean.parseBoolean(element.getAttribute("allowPeaceAttack"));
					boolean allowFixedRes = Boolean.parseBoolean(element.getAttribute("allowFixedRes"));
					boolean allowTransaction = Boolean.parseBoolean(element.getAttribute("allowTransaction"));
					boolean allowAltg = Boolean.parseBoolean(element.getAttribute("allowAltg"));
					boolean giveDamage = Boolean.parseBoolean(element.getAttribute("giveDamage"));
					boolean takeAggro = Boolean.parseBoolean(element.getAttribute("takeAggro"));
					boolean gainExp = Boolean.parseBoolean(element.getAttribute("gainExp"));
					
					accessLevels.put(level, new AccessLevel(level, name, Integer.decode("0x" + nameColor), Integer.decode("0x" + titleColor), isGM, allowPeaceAttack, allowFixedRes, allowTransaction, allowAltg, giveDamage, takeAggro, gainExp));
					
					if (level > highestAccessLevel)
					{
						highestAccessLevel = level;
					}
				}
			}
			
			masterAccessLevel = accessLevels.get(highestAccessLevel);
			
			LOGGER.info("AccessLevels: User Access Level is " + userAccessLevel.getLevel());
			LOGGER.info("AccessLevels: Master Access Level is " + masterAccessLevel.getLevel());
			
		}
		catch (Exception e)
		{
			LOGGER.error("Could not read " + path, e);
		}
	}
	
	/**
	 * Returns the access level by characterAccessLevel<br>
	 * <br>
	 * @param  accessLevelNum as int<br>
	 *                            <br>
	 * @return                AccessLevel: AccessLevel instance by char access level<br>
	 */
	public AccessLevel getAccessLevel(final int accessLevelNum)
	{
		AccessLevel accessLevel = null;
		
		synchronized (accessLevels)
		{
			accessLevel = accessLevels.get(accessLevelNum);
		}
		return accessLevel;
	}
	
	public synchronized void addBanAccessLevel(int accessLevel)
	{
		if (accessLevel > -1)
		{
			return;
		}
		
		accessLevels.put(accessLevel, new AccessLevel(accessLevel, "Banned", Integer.decode("0x000000"), Integer.decode("0x000000"), false, false, false, false, false, false, false, false));
	}
	
	public AccessLevel getUserAccessLevel()
	{
		return userAccessLevel;
	}
	
	public AccessLevel getMasterAccessLevel()
	{
		return masterAccessLevel;
	}
	
	private static class SingletonHolder
	{
		static final AccessLevels INSTANCE = new AccessLevels();
	}
	
	public static AccessLevels getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
}
