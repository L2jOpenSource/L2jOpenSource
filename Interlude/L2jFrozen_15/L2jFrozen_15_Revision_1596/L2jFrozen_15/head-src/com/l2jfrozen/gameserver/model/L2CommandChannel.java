package com.l2jfrozen.gameserver.model;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.network.serverpackets.ExCloseMPCC;
import com.l2jfrozen.gameserver.network.serverpackets.ExOpenMPCC;
import com.l2jfrozen.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * @author chris_00
 */
public class L2CommandChannel
{
	private final List<L2Party> partys;
	private L2PcInstance commandLeader = null;
	private int channelLvl;
	
	/**
	 * Creates a New Command Channel and Add the Leaders party to the CC
	 * @param leader
	 */
	public L2CommandChannel(final L2PcInstance leader)
	{
		commandLeader = leader;
		partys = new ArrayList<>();
		partys.add(leader.getParty());
		channelLvl = leader.getParty().getLevel();
		leader.getParty().setCommandChannel(this);
		leader.getParty().broadcastToPartyMembers(new SystemMessage(SystemMessageId.COMMAND_CHANNEL_FORMED));
		leader.getParty().broadcastToPartyMembers(new ExOpenMPCC());
	}
	
	/**
	 * Adds a Party to the Command Channel
	 * @param party
	 */
	public void addParty(final L2Party party)
	{
		if (party == null)
		{
			return;
		}
		
		partys.add(party);
		
		if (party.getLevel() > channelLvl)
		{
			channelLvl = party.getLevel();
		}
		
		party.setCommandChannel(this);
		party.broadcastToPartyMembers(new SystemMessage(SystemMessageId.JOINED_COMMAND_CHANNEL));
		party.broadcastToPartyMembers(new ExOpenMPCC());
	}
	
	/**
	 * Removes a Party from the Command Channel
	 * @param party
	 */
	public void removeParty(final L2Party party)
	{
		if (party == null)
		{
			return;
		}
		
		partys.remove(party);
		channelLvl = 0;
		
		for (final L2Party pty : partys)
		{
			if (pty.getLevel() > channelLvl)
			{
				channelLvl = pty.getLevel();
			}
		}
		
		party.setCommandChannel(null);
		party.broadcastToPartyMembers(new ExCloseMPCC());
		
		if (partys.size() < 2)
		{
			broadcastToChannelMembers(new SystemMessage(SystemMessageId.COMMAND_CHANNEL_DISBANDED));
			disbandChannel();
		}
	}
	
	/**
	 * disbands the whole Command Channel
	 */
	public void disbandChannel()
	{
		if (partys != null)
		{
			for (final L2Party party : partys)
			{
				if (party != null)
				{
					removeParty(party);
				}
			}
			partys.clear();
		}
	}
	
	/**
	 * @return overall member count of the Command Channel
	 */
	public int getMemberCount()
	{
		int count = 0;
		
		for (final L2Party party : partys)
		{
			if (party != null)
			{
				count += party.getMemberCount();
			}
		}
		return count;
	}
	
	/**
	 * Broadcast packet to every channel member
	 * @param gsp
	 */
	public void broadcastToChannelMembers(final L2GameServerPacket gsp)
	{
		if (partys != null && !partys.isEmpty())
		{
			for (final L2Party party : partys)
			{
				if (party != null)
				{
					party.broadcastToPartyMembers(gsp);
				}
			}
		}
	}
	
	public void broadcastCSToChannelMembers(final CreatureSay gsp, final L2PcInstance broadcaster)
	{
		if (partys != null && !partys.isEmpty())
		{
			for (final L2Party party : partys)
			{
				if (party != null)
				{
					party.broadcastCSToPartyMembers(gsp, broadcaster);
				}
			}
		}
	}
	
	/**
	 * @return list of Parties in Command Channel
	 */
	public List<L2Party> getPartys()
	{
		return partys;
	}
	
	/**
	 * @return list of all Members in Command Channel
	 */
	public List<L2PcInstance> getMembers()
	{
		List<L2PcInstance> members = new ArrayList<>();
		
		for (L2Party party : getPartys())
		{
			members.addAll(party.getPartyMembers());
		}
		
		return members;
	}
	
	/**
	 * @return Level of CC
	 */
	public int getLevel()
	{
		return channelLvl;
	}
	
	/**
	 * @param leader the leader of the Command Channel
	 */
	public void setChannelLeader(final L2PcInstance leader)
	{
		commandLeader = leader;
	}
	
	/**
	 * @return the leader of the Command Channel
	 */
	public L2PcInstance getChannelLeader()
	{
		return commandLeader;
	}
	
	/**
	 * Queen Ant, Core, Orfen, Zaken: MemberCount > 36<br>
	 * Baium: MemberCount > 56<br>
	 * Antharas: MemberCount > 225<br>
	 * Valakas: MemberCount > 99<br>
	 * normal RaidBoss: MemberCount > 18
	 * @param  obj
	 * @return     true if proper condition for RaidWar
	 */
	public boolean meetRaidWarCondition(final L2Object obj)
	{
		if (!(obj instanceof L2RaidBossInstance) || !(obj instanceof L2GrandBossInstance))
		{
			return false;
		}
		
		final int npcId = ((L2Attackable) obj).getNpcId();
		
		switch (npcId)
		{
			case 29001: // Queen Ant
			case 29006: // Core
			case 29014: // Orfen
			case 29022: // Zaken
				return getMemberCount() > 36;
			case 29020: // Baium
				return getMemberCount() > 56;
			case 29019: // Antharas
				return getMemberCount() > 225;
			case 29028: // Valakas
				return getMemberCount() > 99;
			default: // normal Raidboss
				return getMemberCount() > 18;
		}
	}
}
