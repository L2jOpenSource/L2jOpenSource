package com.l2jfrozen.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.model.L2Macro.L2MacroCmd;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.SendMacroList;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class ...
 * @version $Revision: 1.1.2.1.2.2 $ $Date: 2005/03/02 15:38:41 $
 */
public class MacroList
{
	private static Logger LOGGER = Logger.getLogger(MacroList.class);
	
	private static final String INSERT_CHARACTER_MACRO = "INSERT INTO character_macroses (char_obj_id,id,icon,name,descr,acronym,commands) VALUES (?,?,?,?,?,?,?)";
	private static final String SELECT_CHARACTER_MACROS_BY_OBJ_ID = "SELECT char_obj_id, id, icon, name, descr, acronym, commands FROM character_macroses WHERE char_obj_id=?";
	private static final String DELETE_CHARACTER_MACRO_BY_CHAR_ID_AND_ID = "DELETE FROM character_macroses WHERE char_obj_id=? AND id=?";
	
	private final L2PcInstance owner;
	private int revision;
	private int macroId;
	private final Map<Integer, L2Macro> macroses = new HashMap<>();
	
	public MacroList(final L2PcInstance owner)
	{
		this.owner = owner;
		revision = 1;
		macroId = 1000;
	}
	
	public int getRevision()
	{
		return revision;
	}
	
	public L2Macro[] getAllMacroses()
	{
		return macroses.values().toArray(new L2Macro[macroses.size()]);
	}
	
	public L2Macro getMacro(final int id)
	{
		return macroses.get(id - 1);
	}
	
	public void registerMacro(final L2Macro macro)
	{
		if (macro.id == 0)
		{
			macro.id = macroId++;
			
			while (macroses.get(macro.id) != null)
			{
				macro.id = macroId++;
			}
			
			macroses.put(macro.id, macro);
			registerMacroInDb(macro);
		}
		else
		{
			L2Macro old = macroses.put(macro.id, macro);
			
			if (old != null)
			{
				deleteMacroFromDb(old);
			}
			
			registerMacroInDb(macro);
			
			old = null;
		}
		sendUpdate();
	}
	
	public void deleteMacro(final int id)
	{
		L2Macro toRemove = macroses.get(id);
		
		if (toRemove != null)
		{
			deleteMacroFromDb(toRemove);
		}
		
		macroses.remove(id);
		
		for (L2ShortCut sc : owner.getAllShortCuts())
		{
			if (sc.getId() == id && sc.getType() == L2ShortCut.TYPE_MACRO)
			{
				owner.deleteShortCut(sc.getSlot(), sc.getPage());
			}
		}
		
		sendUpdate();
	}
	
	public void sendUpdate()
	{
		revision++;
		
		L2Macro[] all = getAllMacroses();
		
		if (all.length == 0)
		{
			owner.sendPacket(new SendMacroList(revision, all.length, null));
		}
		else
		{
			for (L2Macro m : all)
			{
				owner.sendPacket(new SendMacroList(revision, all.length, m));
			}
		}
	}
	
	private void registerMacroInDb(L2Macro macro)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(INSERT_CHARACTER_MACRO))
		{
			statement.setInt(1, owner.getObjectId());
			statement.setInt(2, macro.id);
			statement.setInt(3, macro.icon);
			statement.setString(4, macro.name);
			statement.setString(5, macro.descr);
			statement.setString(6, macro.acronym);
			
			StringBuilder sb = new StringBuilder();
			
			for (final L2MacroCmd cmd : macro.commands)
			{
				
				final StringBuilder cmd_sb = new StringBuilder();
				
				cmd_sb.append(cmd.type).append(',');
				cmd_sb.append(cmd.d1).append(',');
				cmd_sb.append(cmd.d2);
				
				if (cmd.cmd != null && cmd.cmd.length() > 0)
				{
					cmd_sb.append(',').append(cmd.cmd);
				}
				
				cmd_sb.append(';');
				
				if (sb.toString().length() + cmd_sb.toString().length() < 255)
				{
					sb.append(cmd_sb.toString());
				}
				else
				{
					break;
				}
				
			}
			
			statement.setString(7, sb.toString());
			statement.executeUpdate();
		}
		catch (final Exception e)
		{
			LOGGER.info("Player: " + owner.getName() + " IP:" + owner.getClient().getConnection().getInetAddress().getHostAddress() + " try to use bug with macros");
			LOGGER.warn("could not store macro:", e);
		}
	}
	
	/**
	 * @param macro
	 */
	private void deleteMacroFromDb(final L2Macro macro)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_CHARACTER_MACRO_BY_CHAR_ID_AND_ID))
		{
			statement.setInt(1, owner.getObjectId());
			statement.setInt(2, macro.id);
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("MacroList.deleteMacroFromDb : Could not delete macro in database", e);
		}
	}
	
	public void restore()
	{
		macroses.clear();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(SELECT_CHARACTER_MACROS_BY_OBJ_ID))
		{
			statement.setInt(1, owner.getObjectId());
			try (ResultSet rset = statement.executeQuery())
			{
				while (rset.next())
				{
					final int id = rset.getInt("id");
					final int icon = rset.getInt("icon");
					
					String name = rset.getString("name");
					String descr = rset.getString("descr");
					String acronym = rset.getString("acronym");
					List<L2MacroCmd> commands = new ArrayList<>();
					StringTokenizer st1 = new StringTokenizer(rset.getString("commands"), ";");
					
					while (st1.hasMoreTokens())
					{
						StringTokenizer st = new StringTokenizer(st1.nextToken(), ",");
						
						if (st.countTokens() < 3)
						{
							continue;
						}
						
						final int type = Integer.parseInt(st.nextToken());
						final int d1 = Integer.parseInt(st.nextToken());
						final int d2 = Integer.parseInt(st.nextToken());
						
						String cmd = "";
						
						if (st.hasMoreTokens())
						{
							cmd = st.nextToken();
						}
						
						L2MacroCmd mcmd = new L2MacroCmd(commands.size(), type, d1, d2, cmd);
						commands.add(mcmd);
						
						mcmd = null;
						st = null;
					}
					
					L2Macro m = new L2Macro(id, icon, name, descr, acronym, commands.toArray(new L2MacroCmd[commands.size()]));
					macroses.put(m.id, m);
					m = null;
					name = null;
					descr = null;
					acronym = null;
					commands = null;
					st1 = null;
				}
			}
		}
		catch (Exception e)
		{
			LOGGER.error("MacroList.restore : Could not store shortcuts", e);
		}
	}
}
