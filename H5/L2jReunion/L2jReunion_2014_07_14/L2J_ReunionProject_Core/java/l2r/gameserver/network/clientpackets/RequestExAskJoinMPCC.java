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
package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.L2World;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExAskJoinMPCC;
import l2r.gameserver.network.serverpackets.SystemMessage;

/**
 * Format: (ch) S<br>
 * D0 0D 00 5A 00 77 00 65 00 72 00 67 00 00 00
 * @author chris_00
 */
public final class RequestExAskJoinMPCC extends L2GameClientPacket
{
	private static final String _C__D0_06_REQUESTEXASKJOINMPCC = "[C] D0:06 RequestExAskJoinMPCC";
	private String _name;
	
	@Override
	protected void readImpl()
	{
		_name = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		final L2PcInstance player = L2World.getInstance().getPlayer(_name);
		if (player == null)
		{
			return;
		}
		// invite yourself? ;)
		if (activeChar.isInParty() && player.isInParty() && activeChar.getParty().equals(player.getParty()))
		{
			return;
		}
		
		SystemMessage sm;
		// activeChar is in a Party?
		if (activeChar.isInParty())
		{
			L2Party activeParty = activeChar.getParty();
			// activeChar is PartyLeader? && activeChars Party is already in a CommandChannel?
			if (activeParty.getLeader().equals(activeChar))
			{
				// if activeChars Party is in CC, is activeChar CCLeader?
				if (activeParty.isInCommandChannel() && activeParty.getCommandChannel().getLeader().equals(activeChar))
				{
					// in CC and the CCLeader
					// target in a party?
					if (player.isInParty())
					{
						// targets party already in a CChannel?
						if (player.getParty().isInCommandChannel())
						{
							sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ALREADY_MEMBER_OF_COMMAND_CHANNEL);
							sm.addString(player.getName());
							activeChar.sendPacket(sm);
						}
						else
						{
							// ready to open a new CC
							// send request to targets Party's PartyLeader
							askJoinMPCC(activeChar, player);
						}
					}
					else
					{
						activeChar.sendMessage(player.getName() + " doesn't have party and cannot be invited to Command Channel.");
					}
					
				}
				else if (activeParty.isInCommandChannel() && !activeParty.getCommandChannel().getLeader().equals(activeChar))
				{
					// in CC, but not the CCLeader
					sm = SystemMessage.getSystemMessage(SystemMessageId.CANNOT_INVITE_TO_COMMAND_CHANNEL);
					activeChar.sendPacket(sm);
				}
				else
				{
					// target in a party?
					if (player.isInParty())
					{
						// targets party already in a CChannel?
						if (player.getParty().isInCommandChannel())
						{
							sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ALREADY_MEMBER_OF_COMMAND_CHANNEL);
							sm.addString(player.getName());
							activeChar.sendPacket(sm);
						}
						else
						{
							// ready to open a new CC
							// send request to targets Party's PartyLeader
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
	
	private void askJoinMPCC(L2PcInstance requestor, L2PcInstance target)
	{
		boolean hasRight = false;
		if (requestor.isClanLeader() && (requestor.getClan().getLevel() >= 5))
		{
			// Clan leader of lvl5 Clan or higher.
			hasRight = true;
		}
		else if (requestor.getInventory().getItemByItemId(8871) != null)
		{
			// 8871 Strategy Guide.
			// TODO: Should destroyed after successful invite?
			hasRight = true;
		}
		else if ((requestor.getPledgeClass() >= 5) && (requestor.getKnownSkill(391) != null))
		{
			// At least Baron or higher and the skill Clan Imperium
			hasRight = true;
		}
		
		if (!hasRight)
		{
			requestor.sendPacket(SystemMessageId.COMMAND_CHANNEL_ONLY_BY_LEVEL_5_CLAN_LEADER_PARTY_LEADER);
			return;
		}
		
		// Get the target's party leader, and do whole actions on him.
		final L2PcInstance targetLeader = target.getParty().getLeader();
		SystemMessage sm;
		if (!targetLeader.isProcessingRequest())
		{
			requestor.onTransactionRequest(targetLeader);
			sm = SystemMessage.getSystemMessage(SystemMessageId.COMMAND_CHANNEL_CONFIRM_FROM_C1);
			sm.addString(requestor.getName());
			targetLeader.sendPacket(sm);
			targetLeader.sendPacket(new ExAskJoinMPCC(requestor.getName()));
			
			requestor.sendMessage("You invited " + targetLeader.getName() + " to your Command Channel.");
		}
		else
		{
			sm = SystemMessage.getSystemMessage(SystemMessageId.C1_IS_BUSY_TRY_LATER);
			sm.addString(targetLeader.getName());
			requestor.sendPacket(sm);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_06_REQUESTEXASKJOINMPCC;
	}
}
