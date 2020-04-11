package com.l2jfrozen.gameserver.model.zone.type;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Node;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.entity.event.TownWar;
import com.l2jfrozen.gameserver.model.zone.L2ZoneType;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.util.random.Rnd;

/**
 * A Town zone
 * @author durgus
 */
public class L2TownZone extends L2ZoneType
{
	private String townName;
	private int townId;
	private int redirectTownId;
	private int taxById;
	private boolean noPeace;
	private final List<int[]> spawnLoc;
	
	public L2TownZone(final int id)
	{
		super(id);
		
		taxById = 0;
		spawnLoc = new ArrayList<>();
		
		// Default to Giran
		redirectTownId = 9;
		
		// Default peace zone
		noPeace = false;
	}
	
	@Override
	public void setParameter(final String name, final String value)
	{
		if (name.equals("name"))
		{
			townName = value;
		}
		else if (name.equals("townId"))
		{
			townId = Integer.parseInt(value);
		}
		else if (name.equals("redirectTownId"))
		{
			redirectTownId = Integer.parseInt(value);
		}
		else if (name.equals("taxById"))
		{
			taxById = Integer.parseInt(value);
		}
		else if (name.equals("noPeace"))
		{
			noPeace = Boolean.parseBoolean(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}
	
	@Override
	public void setSpawnLocs(final Node node)
	{
		int loc[] = new int[3];
		
		Node node1 = node.getAttributes().getNamedItem("X");
		
		if (node1 != null)
		{
			loc[0] = Integer.parseInt(node1.getNodeValue());
		}
		
		node1 = node.getAttributes().getNamedItem("Y");
		
		if (node1 != null)
		{
			loc[1] = Integer.parseInt(node1.getNodeValue());
		}
		
		node1 = node.getAttributes().getNamedItem("Z");
		
		if (node1 != null)
		{
			loc[2] = Integer.parseInt(node1.getNodeValue());
		}
		spawnLoc.add(loc);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (noPeace && TownWar.getInstance().isInProgress())
		{
			character.setInsideZone(L2Character.ZONE_PVP, true);
			character.setInTownWar(true);
			
			if (character.isPlayer())
			{
				character.sendPacket(new SystemMessage(SystemMessageId.ENTERED_COMBAT_ZONE));
			}
		}
		else
		{
			character.setInsideZone(L2Character.ZONE_PEACE, true);
		}
		
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (noPeace && TownWar.getInstance().isInProgress())
		{
			character.setInsideZone(L2Character.ZONE_PVP, false);
			if (character.isinTownWar())
			{
				character.setInTownWar(false);
			}
			
			if (character.isPlayer())
			{
				character.sendPacket(new SystemMessage(SystemMessageId.LEFT_COMBAT_ZONE));
			}
		}
		else
		{
			character.setInsideZone(L2Character.ZONE_PEACE, false);
		}
	}
	
	@Override
	protected void onDieInside(L2Character character)
	{
	}
	
	@Override
	protected void onReviveInside(L2Character character)
	{
	}
	
	/**
	 * @return this town zones name
	 */
	@Override
	public String getZoneName()
	{
		return townName;
	}
	
	/**
	 * @return this zones town id (if any)
	 */
	public int getTownId()
	{
		return townId;
	}
	
	/**
	 * @return the id for this town zones redir town
	 */
	@Deprecated
	public int getRedirectTownId()
	{
		return redirectTownId;
	}
	
	/**
	 * @return this zones spawn location
	 */
	public int[] getSpawnLoc()
	{
		int loc[] = new int[3];
		
		loc = spawnLoc.get(Rnd.get(spawnLoc.size()));
		
		return loc;
	}
	
	/**
	 * @return this town zones castle id
	 */
	public int getTaxById()
	{
		return taxById;
	}
}
