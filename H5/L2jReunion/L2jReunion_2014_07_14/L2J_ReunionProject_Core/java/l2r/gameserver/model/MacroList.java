/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import l2r.L2DatabaseFactory;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.interfaces.IRestorable;
import l2r.gameserver.network.serverpackets.SendMacroList;
import l2r.util.StringUtil;

public class MacroList implements IRestorable
{
	private static final Logger _log = Logger.getLogger(MacroList.class.getName());
	
	private final L2PcInstance _owner;
	private int _revision;
	private int _macroId;
	private final Map<Integer, Macro> _macroses = Collections.synchronizedMap(new LinkedHashMap<Integer, Macro>());
	
	public MacroList(L2PcInstance owner)
	{
		_owner = owner;
		_revision = 1;
		_macroId = 1000;
	}
	
	public int getRevision()
	{
		return _revision;
	}
	
	public Map<Integer, Macro> getAllMacroses()
	{
		return _macroses;
	}
	
	public void registerMacro(Macro macro)
	{
		if (macro.getId() == 0)
		{
			macro.setId(_macroId++);
			while (_macroses.containsKey(macro.getId()))
			{
				macro.setId(_macroId++);
			}
			_macroses.put(macro.getId(), macro);
			registerMacroInDb(macro);
		}
		else
		{
			final Macro old = _macroses.put(macro.getId(), macro);
			if (old != null)
			{
				deleteMacroFromDb(old);
			}
			registerMacroInDb(macro);
		}
		sendUpdate();
	}
	
	public void deleteMacro(int id)
	{
		final Macro removed = _macroses.remove(id);
		if (removed != null)
		{
			deleteMacroFromDb(removed);
		}
		
		final L2ShortCut[] allShortCuts = _owner.getAllShortCuts();
		for (L2ShortCut sc : allShortCuts)
		{
			if ((sc.getId() == id) && (sc.getType() == L2ShortCut.TYPE_MACRO))
			{
				_owner.deleteShortCut(sc.getSlot(), sc.getPage());
			}
		}
		
		sendUpdate();
	}
	
	public void sendUpdate()
	{
		_revision++;
		final Collection<Macro> allMacros = _macroses.values();
		synchronized (_macroses)
		{
			if (allMacros.isEmpty())
			{
				_owner.sendPacket(new SendMacroList(_revision, 0, null));
			}
			else
			{
				for (Macro m : allMacros)
				{
					_owner.sendPacket(new SendMacroList(_revision, allMacros.size(), m));
				}
			}
		}
	}
	
	private void registerMacroInDb(Macro macro)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("INSERT INTO character_macroses (charId,id,icon,name,descr,acronym,commands) values(?,?,?,?,?,?,?)"))
		{
			ps.setInt(1, _owner.getObjectId());
			ps.setInt(2, macro.getId());
			ps.setInt(3, macro.getIcon());
			ps.setString(4, macro.getName());
			ps.setString(5, macro.getDescr());
			ps.setString(6, macro.getAcronym());
			final StringBuilder sb = new StringBuilder(300);
			for (MacroCmd cmd : macro.getCommands())
			{
				StringUtil.append(sb, String.valueOf(cmd.getType()), ",", String.valueOf(cmd.getD1()), ",", String.valueOf(cmd.getD2()));
				if ((cmd.getCmd() != null) && (cmd.getCmd().length() > 0))
				{
					StringUtil.append(sb, ",", cmd.getCmd());
				}
				sb.append(';');
			}
			
			if (sb.length() > 255)
			{
				sb.setLength(255);
			}
			
			ps.setString(7, sb.toString());
			ps.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not store macro:", e);
		}
	}
	
	private void deleteMacroFromDb(Macro macro)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("DELETE FROM character_macroses WHERE charId=? AND id=?"))
		{
			ps.setInt(1, _owner.getObjectId());
			ps.setInt(2, macro.getId());
			ps.execute();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not delete macro:", e);
		}
	}
	
	@Override
	public boolean restoreMe()
	{
		_macroses.clear();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT charId, id, icon, name, descr, acronym, commands FROM character_macroses WHERE charId=?"))
		{
			ps.setInt(1, _owner.getObjectId());
			try (ResultSet rset = ps.executeQuery())
			{
				while (rset.next())
				{
					int id = rset.getInt("id");
					int icon = rset.getInt("icon");
					String name = rset.getString("name");
					String descr = rset.getString("descr");
					String acronym = rset.getString("acronym");
					List<MacroCmd> commands = new ArrayList<>();
					StringTokenizer st1 = new StringTokenizer(rset.getString("commands"), ";");
					while (st1.hasMoreTokens())
					{
						StringTokenizer st = new StringTokenizer(st1.nextToken(), ",");
						if (st.countTokens() < 3)
						{
							continue;
						}
						int type = Integer.parseInt(st.nextToken());
						int d1 = Integer.parseInt(st.nextToken());
						int d2 = Integer.parseInt(st.nextToken());
						String cmd = "";
						if (st.hasMoreTokens())
						{
							cmd = st.nextToken();
						}
						commands.add(new MacroCmd(commands.size(), type, d1, d2, cmd));
					}
					_macroses.put(id, new Macro(id, icon, name, descr, acronym, commands));
				}
			}
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "could not store shortcuts:", e);
			return false;
		}
		return true;
	}
}
