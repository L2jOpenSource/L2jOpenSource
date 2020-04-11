package com.l2jfrozen.gameserver.model;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.managers.TownManager;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ExManagePartyRoomMember;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * @author Gnacik
 */
public class PartyMatchRoom
{
	private final int id;
	private String title;
	private int loot;
	private int location;
	private int minLvl;
	private int maxLvl;
	private int maxMembers;
	private final List<L2PcInstance> members = new ArrayList<>();
	
	public PartyMatchRoom(final int id, final String title, final int loot, final int minlvl, final int maxlvl, final int maxmem, final L2PcInstance owner)
	{
		this.id = id;
		this.title = title;
		this.loot = loot;
		location = TownManager.getClosestLocation(owner);
		minLvl = minlvl;
		maxLvl = maxlvl;
		maxMembers = maxmem;
		members.add(owner);
	}
	
	public List<L2PcInstance> getPartyMembers()
	{
		return members;
	}
	
	public void addMember(final L2PcInstance player)
	{
		members.add(player);
	}
	
	public void deleteMember(final L2PcInstance player)
	{
		if (player != getOwner())
		{
			members.remove(player);
			notifyMembersAboutExit(player);
		}
		else if (members.size() == 1)
		{
			PartyMatchRoomList.getInstance().deleteRoom(id);
		}
		else
		{
			changeLeader(members.get(1));
			deleteMember(player);
		}
	}
	
	public void notifyMembersAboutExit(final L2PcInstance player)
	{
		for (final L2PcInstance member : getPartyMembers())
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_LEFT_PARTY_ROOM);
			sm.addString(player.getName());
			member.sendPacket(sm);
			member.sendPacket(new ExManagePartyRoomMember(player, this, 2));
		}
	}
	
	public void changeLeader(final L2PcInstance newLeader)
	{
		// Get current leader
		final L2PcInstance oldLeader = members.get(0);
		// Remove new leader
		if (members.contains(newLeader))
		{
			members.remove(newLeader);
		}
		
		// Move him to first position
		if (!members.isEmpty())
		{
			members.set(0, newLeader);
		}
		else
		{
			members.add(newLeader);
		}
		
		// Add old leader as normal member
		if (oldLeader != null && oldLeader != newLeader)
		{
			members.add(oldLeader);
		}
		
		// Broadcast change
		for (final L2PcInstance member : getPartyMembers())
		{
			member.sendPacket(new ExManagePartyRoomMember(newLeader, this, 1));
			member.sendPacket(new ExManagePartyRoomMember(oldLeader, this, 1));
			member.sendPacket(new SystemMessage(SystemMessageId.PARTY_ROOM_LEADER_CHANGED));
		}
	}
	
	public int getId()
	{
		return id;
	}
	
	public L2PcInstance getOwner()
	{
		return members.get(0);
	}
	
	public int getMembers()
	{
		return members.size();
	}
	
	public int getLootType()
	{
		return loot;
	}
	
	public void setLootType(final int loot)
	{
		this.loot = loot;
	}
	
	public int getMinLvl()
	{
		return minLvl;
	}
	
	public void setMinLvl(final int minlvl)
	{
		minLvl = minlvl;
	}
	
	public int getMaxLvl()
	{
		return maxLvl;
	}
	
	public void setMaxLvl(final int maxlvl)
	{
		maxLvl = maxlvl;
	}
	
	public int getLocation()
	{
		return location;
	}
	
	public void setLocation(final int loc)
	{
		location = loc;
	}
	
	public int getMaxMembers()
	{
		return maxMembers;
	}
	
	public void setMaxMembers(final int maxmem)
	{
		maxMembers = maxmem;
	}
	
	public String getTitle()
	{
		return title;
	}
	
	public void setTitle(final String title)
	{
		this.title = title;
	}
}