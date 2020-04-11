package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ExAskJoinMPCC;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * Format: (ch) S
 * @author chris_00 D0 0D 00 5A 00 77 00 65 00 72 00 67 00 00 00
 */
public final class RequestExAskJoinMPCC extends L2GameClientPacket
{
	private String name;
	
	@Override
	protected void readImpl()
	{
		name = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		
		if (activeChar == null)
		{
			return;
		}
		
		final L2PcInstance player = L2World.getInstance().getPlayer(name);
		
		if (player == null)
		{
			return;
			// invite yourself? ;)
		}
		
		if (activeChar.isInParty() && player.isInParty() && activeChar.getParty().equals(player.getParty()))
		{
			return;
		}
		
		// activeChar is in a Party?
		if (activeChar.isInParty())
		{
			final L2Party activeParty = activeChar.getParty();
			// activeChar is PartyLeader? && activeChars Party is already in a CommandChannel?
			if (activeParty.getLeader().equals(activeChar))
			{
				// if activeChars Party is in CC, is activeChar CCLeader?
				if (activeParty.isInCommandChannel() && activeParty.getCommandChannel().getChannelLeader().equals(activeChar))
				{
					// in CC and the CCLeader
					// target in a party?
					if (player.isInParty())
					{
						// targets party already in a CChannel?
						if (player.getParty().isInCommandChannel())
						{
							activeChar.sendPacket(new SystemMessage(SystemMessageId.S1_ALREADY_MEMBER_OF_COMMAND_CHANNEL).addString(player.getName()));
						}
						else
						{
							askJoinMPCC(activeChar, player);
						}
					}
					else
					{
						activeChar.sendMessage(player.getName() + " doesn't have party and cannot be invited to Command Channel.");
					}
					
				}
				else if (activeParty.isInCommandChannel() && !activeParty.getCommandChannel().getChannelLeader().equals(activeChar))
				{
					activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_INVITE_TO_COMMAND_CHANNEL));
				}
				else
				{
					// target in a party?
					if (player.isInParty())
					{
						// targets party already in a CChannel?
						if (player.getParty().isInCommandChannel())
						{
							activeChar.sendPacket(new SystemMessage(SystemMessageId.S1_ALREADY_MEMBER_OF_COMMAND_CHANNEL).addString(player.getName()));
						}
						else
						{
							askJoinMPCC(activeChar, player);
						}
					}
					else
					{
						activeChar.sendMessage(player.getName() + " doesn't have party and cannot be invited to Command Channel.");
					}
				}
			}
			else
			{
				activeChar.sendPacket(SystemMessageId.CANNOT_INVITE_TO_COMMAND_CHANNEL);
			}
		}
		
	}
	
	private void askJoinMPCC(final L2PcInstance requestor, final L2PcInstance target)
	{
		boolean hasRight = false;
		if (requestor.getClan() != null && requestor.getClan().getLeaderId() == requestor.getObjectId() && requestor.getClan().getLevel() >= 5) // Clanleader of lvl5 Clan or higher
		{
			hasRight = true;
		}
		else if (requestor.getInventory().getItemByItemId(8871) != null)
		{
			hasRight = true;
		}
		else if (requestor.getPledgeClass() >= 5)
		{
			for (final L2Skill skill : requestor.getAllSkills())
			{
				// Skill Clan Imperium
				if (skill.getId() == 391)
				{
					hasRight = true;
					break;
				}
			}
		}
		
		if (!hasRight)
		{
			requestor.sendPacket(SystemMessageId.COMMAND_CHANNEL_ONLY_BY_LEVEL_5_CLAN_LEADER_PARTY_LEADER);
			return;
		}
		
		final L2PcInstance targetLeader = target.getParty().getLeader();
		if (!targetLeader.isProcessingRequest())
		{
			requestor.onTransactionRequest(targetLeader);
			targetLeader.sendPacket(new SystemMessage(SystemMessageId.COMMAND_CHANNEL_CONFIRM).addString(requestor.getName()));
			targetLeader.sendPacket(new ExAskJoinMPCC(requestor.getName()));
		}
		else
		{
			requestor.sendPacket(new SystemMessage(SystemMessageId.S1_IS_BUSY_TRY_LATER).addString(targetLeader.getName()));
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:0D RequestExAskJoinMPCC";
	}
	
}
