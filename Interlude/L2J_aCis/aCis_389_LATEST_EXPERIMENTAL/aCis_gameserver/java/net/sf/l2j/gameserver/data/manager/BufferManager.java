package net.sf.l2j.gameserver.data.manager;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.lang.StringUtil;

import net.sf.l2j.Config;
import net.sf.l2j.L2DatabaseFactory;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.holder.BuffSkillHolder;
import net.sf.l2j.gameserver.skills.L2Skill;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

/**
 * Loads and stores available {@link BuffSkillHolder}s for the integrated scheme buffer.<br>
 * Loads and stores Players' buff schemes into _schemesTable (under a {@link String} name and a {@link List} of {@link Integer} skill ids).
 */
public class BufferManager implements IXmlReader
{
	private static final String LOAD_SCHEMES = "SELECT * FROM buffer_schemes";
	private static final String DELETE_SCHEMES = "TRUNCATE TABLE buffer_schemes";
	private static final String INSERT_SCHEME = "INSERT INTO buffer_schemes (object_id, scheme_name, skills) VALUES (?,?,?)";
	
	private final Map<Integer, Map<String, ArrayList<Integer>>> _schemesTable = new ConcurrentHashMap<>();
	private final Map<Integer, BuffSkillHolder> _availableBuffs = new LinkedHashMap<>();
	
	protected BufferManager()
	{
		load();
	}
	
	@Override
	public void load()
	{
		parseFile("./data/xml/bufferSkills.xml");
		LOGGER.info("Loaded {} available buffs.", _availableBuffs.size());
		
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
						break;
					
					final int skillId = Integer.valueOf(skill);
					
					// Integrity check to see if the skillId is available as a buff.
					if (_availableBuffs.containsKey(skillId))
						schemeList.add(skillId);
				}
				
				setScheme(rs.getInt("object_id"), rs.getString("scheme_name"), schemeList);
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to load schemes data.", e);
		}
	}
	
	@Override
	public void parseDocument(Document doc, Path path)
	{
		forEach(doc, "list", listNode -> forEach(listNode, "category", categoryNode ->
		{
			final String category = parseString(categoryNode.getAttributes(), "type");
			forEach(categoryNode, "buff", buffNode ->
			{
				final NamedNodeMap attrs = buffNode.getAttributes();
				final int skillId = parseInteger(attrs, "id");
				_availableBuffs.put(skillId, new BuffSkillHolder(skillId, parseInteger(attrs, "level", SkillTable.getInstance().getMaxLevel(skillId)), parseInteger(attrs, "price", 0), category, parseString(attrs, "desc", "")));
			});
		}));
	}
	
	public void saveSchemes()
	{
		final StringBuilder sb = new StringBuilder();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection())
		{
			// Delete all entries from database.
			try (PreparedStatement ps = con.prepareStatement(DELETE_SCHEMES))
			{
				ps.execute();
			}
			
			try (PreparedStatement ps = con.prepareStatement(INSERT_SCHEME))
			{
				// Save _schemesTable content.
				for (Map.Entry<Integer, Map<String, ArrayList<Integer>>> player : _schemesTable.entrySet())
				{
					for (Map.Entry<String, ArrayList<Integer>> scheme : player.getValue().entrySet())
					{
						// Build a String composed of skill ids seperated by a ",".
						for (int skillId : scheme.getValue())
							StringUtil.append(sb, skillId, ",");
						
						// Delete the last "," : must be called only if there is something to delete !
						if (sb.length() > 0)
							sb.setLength(sb.length() - 1);
						
						ps.setInt(1, player.getKey());
						ps.setString(2, scheme.getKey());
						ps.setString(3, sb.toString());
						ps.addBatch();
						
						// Reuse the StringBuilder for next iteration.
						sb.setLength(0);
					}
				}
				ps.executeBatch();
			}
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to save schemes data.", e);
		}
	}
	
	/**
	 * Add or retrieve the Player schemes {@link Map}, then add or update the given scheme based on the {@link String} name set as parameter.
	 * @param playerId : The Player objectId to check.
	 * @param schemeName : The {@link String} used as scheme name.
	 * @param list : The {@link ArrayList} of {@link Integer} used as skill ids.
	 */
	public void setScheme(int playerId, String schemeName, ArrayList<Integer> list)
	{
		final Map<String, ArrayList<Integer>> schemes = _schemesTable.computeIfAbsent(playerId, (k) -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER));
		if (schemes.size() >= Config.BUFFER_MAX_SCHEMES)
			return;
		
		schemes.put(schemeName, list);
	}
	
	/**
	 * @param playerId : The Player objectId to check.
	 * @return the {@link List} of schemes for a given Player.
	 */
	public Map<String, ArrayList<Integer>> getPlayerSchemes(int playerId)
	{
		return _schemesTable.get(playerId);
	}
	
	/**
	 * @param playerId : The Player objectId to check.
	 * @param schemeName : The scheme name to check.
	 * @return The {@link List} holding {@link L2Skill}s for the given scheme name and Player, or null (if scheme or Player isn't registered).
	 */
	public List<Integer> getScheme(int playerId, String schemeName)
	{
		final Map<String, ArrayList<Integer>> schemes = _schemesTable.get(playerId);
		if (schemes == null)
			return Collections.emptyList();
		
		final ArrayList<Integer> scheme = schemes.get(schemeName);
		if (scheme == null)
			return Collections.emptyList();
		
		return scheme;
	}
	
	/**
	 * Apply all effects of a scheme (retrieved by its Player objectId and {@link String} name) upon a {@link Creature} target.
	 * @param npc : The {@link Npc} which apply effects.
	 * @param target : The {@link Creature} benefactor.
	 * @param playerId : The Player objectId to check.
	 * @param schemeName : The scheme name to check.
	 */
	public void applySchemeEffects(Npc npc, Creature target, int playerId, String schemeName)
	{
		for (int skillId : getScheme(playerId, schemeName))
		{
			final BuffSkillHolder holder = getAvailableBuff(skillId);
			if (holder != null)
			{
				final L2Skill skill = holder.getSkill();
				if (skill != null)
					skill.getEffects(npc, target);
			}
		}
	}
	
	/**
	 * @param playerId : The Player objectId to check.
	 * @param schemeName : The scheme name to check.
	 * @param skillId : The {@link L2Skill} id to check.
	 * @return True if the {@link L2Skill} is already registered on the scheme, or false otherwise.
	 */
	public boolean getSchemeContainsSkill(int playerId, String schemeName, int skillId)
	{
		return getScheme(playerId, schemeName).contains(skillId);
	}
	
	/**
	 * @param groupType : The {@link String} group type of skill ids to return.
	 * @return a {@link List} of skill ids based on the given {@link String} groupType.
	 */
	public List<Integer> getSkillsIdsByType(String groupType)
	{
		final List<Integer> skills = new ArrayList<>();
		for (BuffSkillHolder holder : _availableBuffs.values())
		{
			if (holder.getType().equalsIgnoreCase(groupType))
				skills.add(holder.getId());
		}
		return skills;
	}
	
	/**
	 * @return a {@link List} of all available {@link String} buff types.
	 */
	public List<String> getSkillTypes()
	{
		final List<String> skillTypes = new ArrayList<>();
		for (BuffSkillHolder holder : _availableBuffs.values())
		{
			if (!skillTypes.contains(holder.getType()))
				skillTypes.add(holder.getType());
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
	
	public static BufferManager getInstance()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		protected static final BufferManager INSTANCE = new BufferManager();
	}
}