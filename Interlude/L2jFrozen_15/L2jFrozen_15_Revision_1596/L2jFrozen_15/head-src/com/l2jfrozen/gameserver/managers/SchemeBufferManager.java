package com.l2jfrozen.gameserver.managers;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.holder.BuffSkillHolder;
import com.l2jfrozen.gameserver.util.StringUtil;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * Loads and stores available {@link BuffSkillHolder}s for the integrated scheme buffer.<br>
 * Loads and stores players' buff schemes into _schemesTable (under a String name and a List of Integer skill ids).
 */
public class SchemeBufferManager
{
	private static final Logger LOGGER = Logger.getLogger(SchemeBufferManager.class);
	
	String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	
	private static final String LOAD_SCHEMES = "SELECT * FROM character_schemes";
	private static final String DELETE_SCHEMES = "TRUNCATE TABLE character_schemes";
	private static final String INSERT_SCHEME = "INSERT INTO character_schemes (object_id, scheme_name, skills) VALUES (?,?,?)";
	
	private final Map<Integer, HashMap<String, ArrayList<Integer>>> _schemesTable = new ConcurrentHashMap<>();
	private final Map<Integer, BuffSkillHolder> _availableBuffs = new LinkedHashMap<>();
	
	protected SchemeBufferManager()
	{
		load();
	}
	
	public void load()
	{
		parseFile("./data/xml/bufferSkills.xml");
		LOGGER.info("Loaded " + _availableBuffs.size() + " available buffs.");
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(LOAD_SCHEMES);
			ResultSet rs = ps.executeQuery())
		{
			while (rs.next())
			{
				final ArrayList<Integer> schemeList = new ArrayList<>();
				
				final String[] skills = rs.getString("skills").split(",");
				for (String skill : skills)
				{
					// Don't feed the skills list if the list is empty.
					if (skill.isEmpty())
					{
						break;
					}
					
					final int skillId = Integer.valueOf(skill);
					
					// Integrity check to see if the skillId is available as a buff.
					if (_availableBuffs.containsKey(skillId))
					{
						schemeList.add(skillId);
					}
				}
				
				setScheme(rs.getInt("object_id"), rs.getString("scheme_name"), schemeList);
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to load schemes data.", e);
		}
	}
	
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "category", categoryNode ->
		{
			final String category = parseString(categoryNode.getAttributes(), "type");
			forEach(categoryNode, "buff", buffNode ->
			{
				final NamedNodeMap attrs = buffNode.getAttributes();
				final int skillId = parseInteger(attrs, "id");
				_availableBuffs.put(skillId, new BuffSkillHolder(skillId, parseInteger(attrs, "price"), category, parseString(attrs, "desc")));
			});
		}));
	}
	
	public void saveSchemes()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// Delete all entries from database.
			PreparedStatement ps = con.prepareStatement(DELETE_SCHEMES);
			ps.executeUpdate();
			ps.close();
			
			ps = con.prepareStatement(INSERT_SCHEME);
			
			int counter = 0;
			
			// Save _schemesTable content.
			for (Map.Entry<Integer, HashMap<String, ArrayList<Integer>>> player : _schemesTable.entrySet())
			{
				for (Map.Entry<String, ArrayList<Integer>> scheme : player.getValue().entrySet())
				{
					// Build a String composed of skill ids seperated by a ",".
					final StringBuilder sb = new StringBuilder();
					for (int skillId : scheme.getValue())
					{
						StringUtil.append(sb, skillId, ",");
					}
					
					// Delete the last "," : must be called only if there is something to delete !
					if (sb.length() > 0)
					{
						sb.setLength(sb.length() - 1);
					}
					
					ps.setInt(1, player.getKey());
					ps.setString(2, scheme.getKey());
					ps.setString(3, sb.toString());
					ps.addBatch();
				}
				
				if (counter % 500 == 0)
				{
					ps.executeBatch();
				}
			}
			ps.executeBatch();
			ps.close();
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to save schemes data.", e);
		}
		
		LOGGER.info("Character Schemes data has been saved.");
	}
	
	public void setScheme(int playerId, String schemeName, ArrayList<Integer> list)
	{
		if (!_schemesTable.containsKey(playerId))
		{
			_schemesTable.put(playerId, new HashMap<String, ArrayList<Integer>>());
		}
		else if (_schemesTable.get(playerId).size() >= Config.BUFFER_MAX_SCHEMES)
		{
			return;
		}
		
		_schemesTable.get(playerId).put(schemeName, list);
	}
	
	/**
	 * @param  playerId : The player objectId to check.
	 * @return          the list of schemes for a given player.
	 */
	public Map<String, ArrayList<Integer>> getPlayerSchemes(int playerId)
	{
		return _schemesTable.get(playerId);
	}
	
	/**
	 * @param  playerId   : The player objectId to check.
	 * @param  schemeName : The scheme name to check.
	 * @return            the List holding skills for the given scheme name and player, or null (if scheme or player isn't registered).
	 */
	public List<Integer> getScheme(int playerId, String schemeName)
	{
		if (_schemesTable.get(playerId) == null || _schemesTable.get(playerId).get(schemeName) == null)
		{
			return Collections.emptyList();
		}
		
		return _schemesTable.get(playerId).get(schemeName);
	}
	
	/**
	 * @param  playerId   : The player objectId to check.
	 * @param  schemeName : The scheme name to check.
	 * @param  skillId    : The skill id to check.
	 * @return            true if the skill is already registered on the scheme, or false otherwise.
	 */
	public boolean getSchemeContainsSkill(int playerId, String schemeName, int skillId)
	{
		final List<Integer> skills = getScheme(playerId, schemeName);
		if (skills.isEmpty())
		{
			return false;
		}
		
		for (int id : skills)
		{
			if (id == skillId)
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @param  groupType : The type of skills to return.
	 * @return           a list of skills ids based on the given groupType.
	 */
	public List<Integer> getSkillsIdsByType(String groupType)
	{
		List<Integer> skills = new ArrayList<>();
		for (BuffSkillHolder skill : _availableBuffs.values())
		{
			if (skill.getType().equalsIgnoreCase(groupType))
			{
				skills.add(skill.getId());
			}
		}
		return skills;
	}
	
	/**
	 * @return a list of all buff types available.
	 */
	public List<String> getSkillTypes()
	{
		List<String> skillTypes = new ArrayList<>();
		for (BuffSkillHolder skill : _availableBuffs.values())
		{
			if (!skillTypes.contains(skill.getType()))
			{
				skillTypes.add(skill.getType());
			}
		}
		return skillTypes;
	}
	
	public BuffSkillHolder getAvailableBuff(int skillId)
	{
		return _availableBuffs.get(skillId);
	}
	
	public Map<Integer, BuffSkillHolder> getAvailableBuffs()
	{
		return _availableBuffs;
	}
	
	public void forEach(Node node, String nodeName, Consumer<Node> action)
	{
		forEach(node, innerNode ->
		{
			if (nodeName.contains("|"))
			{
				final String[] nodeNames = nodeName.split("\\|");
				for (String name : nodeNames)
				{
					if (!name.isEmpty() && name.equals(innerNode.getNodeName()))
					{
						return true;
					}
				}
				return false;
			}
			return nodeName.equals(innerNode.getNodeName());
		}, action);
	}
	
	public void forEach(Node node, Predicate<Node> filter, Consumer<Node> action)
	{
		final NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++)
		{
			final Node targetNode = list.item(i);
			if (filter.test(targetNode))
			{
				action.accept(targetNode);
			}
		}
	}
	
	public void parseFile(String path)
	{
		parseFile(Paths.get(path), false, true, true);
	}
	
	public void parseFile(Path path, boolean validate, boolean ignoreComments, boolean ignoreWhitespaces)
	{
		if (Files.isDirectory(path))
		{
			final List<Path> pathsToParse = new LinkedList<>();
			try
			{
				Files.walkFileTree(path, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<Path>()
				{
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
					{
						pathsToParse.add(file);
						return FileVisitResult.CONTINUE;
					}
				});
				
				pathsToParse.forEach(p -> parseFile(p, validate, ignoreComments, ignoreWhitespaces));
			}
			catch (IOException e)
			{
				LOGGER.warn("Could not parse directory: " + path, e);
			}
		}
		else
		{
			final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			dbf.setValidating(validate);
			dbf.setIgnoringComments(ignoreComments);
			dbf.setIgnoringElementContentWhitespace(ignoreWhitespaces);
			dbf.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
			
			try
			{
				final DocumentBuilder db = dbf.newDocumentBuilder();
				db.setErrorHandler(new XMLErrorHandler());
				parseDocument(db.parse(path.toAbsolutePath().toFile()), path);
			}
			catch (SAXParseException e)
			{
				LOGGER.warn("Could not parse file: " + path + " at line: " + e.getLineNumber() + ", column: " + e.getColumnNumber(), e);
			}
			catch (ParserConfigurationException | SAXException | IOException e)
			{
				LOGGER.warn("Could not parse file: " + path, e);
			}
		}
	}
	
	class XMLErrorHandler implements ErrorHandler
	{
		@Override
		public void warning(SAXParseException e) throws SAXParseException
		{
			throw e;
		}
		
		@Override
		public void error(SAXParseException e) throws SAXParseException
		{
			throw e;
		}
		
		@Override
		public void fatalError(SAXParseException e) throws SAXParseException
		{
			throw e;
		}
	}
	
	public String parseString(NamedNodeMap attrs, String name)
	{
		return parseString(attrs.getNamedItem(name));
	}
	
	public String parseString(Node node)
	{
		return parseString(node, null);
	}
	
	public String parseString(Node node, String defaultValue)
	{
		return node != null ? node.getNodeValue() : defaultValue;
	}
	
	public Integer parseInteger(NamedNodeMap attrs, String name)
	{
		return parseInteger(attrs.getNamedItem(name));
	}
	
	public Integer parseInteger(Node node)
	{
		return parseInteger(node, null);
	}
	
	public Integer parseInteger(Node node, Integer defaultValue)
	{
		return node != null ? Integer.decode(node.getNodeValue()) : defaultValue;
	}
	
	public static SchemeBufferManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final SchemeBufferManager INSTANCE = new SchemeBufferManager();
	}
}