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
 * @author ReynalDev
 * @author Visor123 L2EMU
 */
public class AdminCommandAccessRights
{
	protected static final Logger LOGGER = Logger.getLogger(AdminCommandAccessRights.class);
	private static AdminCommandAccessRights instance = null;
	
	private Map<String, Integer> adminCommandAccessRights = new HashMap<>();
	
	private AdminCommandAccessRights()
	{
		loadData();
	}
	
	private void loadData()
	{
		String path = "config/access_level/adminCommands.xml";
		try
		{
			File fXmlFile = new File(path);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			
			// optional, but recommended read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			doc.getDocumentElement().normalize();
			
			NodeList nList = doc.getElementsByTagName("admin");
			
			for (int temp = 0; temp < nList.getLength(); temp++)
			{
				Node nNode = nList.item(temp);
				
				if (nNode.getNodeType() == Node.ELEMENT_NODE)
				{
					Element element = (Element) nNode;
					
					String command = element.getAttribute("command");
					int accessLevel = Integer.parseInt(element.getAttribute("accessLevel"));
					
					adminCommandAccessRights.put(command, accessLevel);
				}
			}
			
			LOGGER.info("Admin Access Rights: Loaded " + adminCommandAccessRights.size() + " Access Rigths from database.");
			
		}
		catch (Exception e)
		{
			LOGGER.error("Could not read " + path, e);
		}
	}
	
	/**
	 * Returns the one and only instance of this class<br>
	 * <br>
	 * @return AdminCommandAccessRights: the one and only instance of this class<br>
	 */
	public static AdminCommandAccessRights getInstance()
	{
		return instance == null ? (instance = new AdminCommandAccessRights()) : instance;
	}
	
	public static void reload()
	{
		instance = null;
		getInstance();
	}
	
	public int accessRightForCommand(final String command)
	{
		int out = -1;
		
		if (adminCommandAccessRights.containsKey(command))
		{
			out = adminCommandAccessRights.get(command);
		}
		
		return out;
	}
	
	public boolean hasAccess(String adminCommand, AccessLevel accessLevel)
	{
		if (!accessLevel.isGm())
		{
			return false;
		}
		
		if (accessLevel.getLevel() <= AccessLevels.getInstance().getUserAccessLevel().getLevel())
		{
			return false;
		}
		
		if (accessLevel.getLevel() == AccessLevels.getInstance().getMasterAccessLevel().getLevel())
		{
			return true;
		}
		
		String command = adminCommand;
		if (adminCommand.indexOf(" ") != -1)
		{
			command = adminCommand.substring(0, adminCommand.indexOf(" "));
		}
		
		int accessLevelForComand = 0;
		
		if (adminCommandAccessRights.get(command) != null)
		{
			accessLevelForComand = adminCommandAccessRights.get(command);
		}
		
		if (accessLevelForComand == 0)
		{
			LOGGER.warn("Admin Access Rights: No rights defined for admin command " + command);
			return false;
		}
		else if (accessLevelForComand <= accessLevel.getLevel())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
