package com.l2jfrozen.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.l2jfrozen.util.database.L2DatabaseFactory;
import com.l2jfrozen.util.random.Rnd;

/**
 * Control for Custom NPCs that looks like players.
 * @author Darki699
 */
public class CustomNpcInstanceManager
{
	private final static Logger LOGGER = Logger.getLogger(CustomNpcInstanceManager.class);
	private static final String SELECT_NPC_TO_PC_POLYMORPH = "SELECT spawn,template,name,title,class_id,female,hair_style,hair_color,face,name_color,title_color,noble,hero,pvp,karma,wpn_enchant,right_hand,left_hand,gloves,chest,legs,feet,hair,hair2,pledge,cw_level,clan_id,ally_id,clan_crest,ally_crest,rnd_class,rnd_appearance,rnd_weapon,rnd_armor,max_rnd_enchant FROM npc_to_pc_polymorph";
	
	private static CustomNpcInstanceManager instance;
	private Map<Integer, NpcToPlayer> spawns = new HashMap<>(); // <Object id , info>
	private Map<Integer, NpcToPlayer> templates = new HashMap<>(); // <Npc Template Id , info>
	
	public class NpcToPlayer
	{
		public String stringData[] = new String[2];
		public int integerData[] = new int[27];
		public boolean booleanData[] = new boolean[8];
	}
	
	CustomNpcInstanceManager()
	{
		load();
	}
	
	public static CustomNpcInstanceManager getInstance()
	{
		if (instance == null)
		{
			instance = new CustomNpcInstanceManager();
		}
		return instance;
	}
	
	/**
	 * Flush the old data, and load new data
	 */
	public void reload()
	{
		if (spawns != null)
		{
			spawns.clear();
		}
		
		if (templates != null)
		{
			templates.clear();
		}
		
		load();
	}
	
	private void load()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_NPC_TO_PC_POLYMORPH);
			ResultSet rset = statement.executeQuery())
		{
			int count = 0;
			
			while (rset.next())
			{
				count++;
				NpcToPlayer ci = new NpcToPlayer();
				ci.integerData[26] = rset.getInt("spawn");
				ci.integerData[25] = rset.getInt("template");
				try
				{
					ci.stringData[0] = rset.getString("name");
					ci.stringData[1] = rset.getString("title");
					ci.integerData[7] = rset.getInt("class_id");
					
					int PcSex = rset.getInt("female");
					switch (PcSex)
					{
						case 0:
							ci.booleanData[3] = false;
							break;
						case 1:
							ci.booleanData[3] = true;
							break;
						default:
							ci.booleanData[3] = Rnd.get(100) > 50 ? true : false;
							break;
					}
					
					ci.integerData[19] = rset.getInt("hair_style");
					ci.integerData[20] = rset.getInt("hair_color");
					ci.integerData[21] = rset.getInt("face");
					ci.integerData[22] = rset.getInt("name_color");
					ci.integerData[23] = rset.getInt("title_color");
					ci.booleanData[1] = rset.getInt("noble") > 0 ? true : false;
					ci.booleanData[2] = rset.getInt("hero") > 0 ? true : false;
					ci.booleanData[0] = rset.getInt("pvp") > 0 ? true : false;
					ci.integerData[1] = rset.getInt("karma");
					ci.integerData[8] = rset.getInt("wpn_enchant");
					ci.integerData[11] = rset.getInt("right_hand");
					ci.integerData[12] = rset.getInt("left_hand");
					ci.integerData[13] = rset.getInt("gloves");
					ci.integerData[14] = rset.getInt("chest");
					ci.integerData[15] = rset.getInt("legs");
					ci.integerData[16] = rset.getInt("feet");
					ci.integerData[17] = rset.getInt("hair");
					ci.integerData[18] = rset.getInt("hair2");
					ci.integerData[9] = rset.getInt("pledge");
					ci.integerData[10] = rset.getInt("cw_level");
					ci.integerData[2] = rset.getInt("clan_id");
					ci.integerData[3] = rset.getInt("ally_id");
					ci.integerData[4] = rset.getInt("clan_crest");
					ci.integerData[5] = rset.getInt("ally_crest");
					ci.booleanData[4] = rset.getInt("rnd_class") > 0 ? true : false;
					ci.booleanData[5] = rset.getInt("rnd_appearance") > 0 ? true : false;
					ci.booleanData[6] = rset.getInt("rnd_weapon") > 0 ? true : false;
					ci.booleanData[7] = rset.getInt("rnd_armor") > 0 ? true : false;
					ci.integerData[24] = rset.getInt("max_rnd_enchant");
					// Same object goes in twice:
					
					if (ci.integerData[25] != 0 && !templates.containsKey(ci.integerData[25]))
					{
						templates.put(ci.integerData[25], ci);
					}
					
					if (ci.integerData[25] == 0 && !spawns.containsKey(ci.integerData[26]))
					{
						spawns.put(ci.integerData[26], ci);
					}
				}
				catch (Throwable t)
				{
					LOGGER.error("CustomNpcInstanceManager.load: Failed to load Npc Morph data for Object ID: " + ci.integerData[26] + " template: " + ci.integerData[25], t);
				}
			}
			
			if (count > 0)
			{
				LOGGER.info("CustomNpcInstanceManager: Loaded " + count + " NPC to PC polymorphs.");
			}
		}
		catch (Exception e)
		{
			LOGGER.error("CustomNpcInstanceManager.load : Could not select data from npc_to_pc_polymorph table", e);
		}
	}
	
	/**
	 * Checks if the L2NpcInstance calling this function has polymorphing data
	 * @param  spwnId - L2NpcInstance's unique Object id
	 * @param  npcId  - L2NpcInstance's npc template id
	 * @return
	 */
	public boolean isL2CustomNpcInstance(int spwnId, int npcId)
	{
		if (spwnId == 0 || npcId == 0)
		{
			return false;
		}
		else if (spawns.containsKey(spwnId))
		{
			return true;
		}
		else if (templates.containsKey(npcId))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	 * Return the polymorphing data for this L2NpcInstance if the data exists
	 * @param  spwnId - NpcInstance's unique Object Id
	 * @param  npcId  - NpcInstance's npc template Id
	 * @return        customInfo type data pack, or null if no such data exists.
	 */
	public NpcToPlayer getCustomData(int spwnId, int npcId)
	{
		if (spwnId == 0 || npcId == 0)
		{
			return null;
		}
		
		// First check individual spawn objects - incase they have different values than their template
		for (NpcToPlayer ci : spawns.values())
		{
			if (ci != null && ci.integerData[26] == spwnId)
			{
				return ci;
			}
		}
		
		// Now check if templates contains the morph npc template
		for (NpcToPlayer ci : templates.values())
		{
			if (ci != null && ci.integerData[25] == npcId)
			{
				return ci;
			}
		}
		
		return null;
	}
	
	public final Map<Integer, NpcToPlayer> getAllTemplates()
	{
		return templates;
	}
	
	public final Map<Integer, NpcToPlayer> getAllSpawns()
	{
		return spawns;
	}
}
