/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.voicedcommandhandlers;

import l2r.gameserver.handler.IVoicedCommandHandler;
import l2r.gameserver.model.L2Party;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.SystemMessage;

import gr.sr.configsEngine.configs.impl.CustomServerConfigs;

/**
 * @author Zoey76
 */
public class EvenlyDistributeItems implements IVoicedCommandHandler
{
	private static final String[] VOICED_COMMANDS =
	{
		"evenly"
	};
	
	@Override
	public boolean useVoicedCommand(String command, L2PcInstance activeChar, String params)
	{
		if (!CustomServerConfigs.EVENLY_DISTRIBUTED_ITEMS)
		{
			activeChar.sendMessage("This mod is not enabled.");
			return true;
		}
		
		if (CustomServerConfigs.EVENLY_DISTRIBUTED_ITEMS_FORCED)
		{
			activeChar.sendMessage("This mod is enabled but you cannot change its status.");
			return true;
		}
		
		if (VOICED_COMMANDS[0].equalsIgnoreCase(command))
		{
			if (activeChar.hasEvenlyDistributedLoot())
			{
				activeChar.setEvenlyDistributedLoot(false);
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1);
				sm.addString("Evenly distribution of party loot disabled.");
				// If has party and is the leader announce it's party members.
				final L2Party party = activeChar.getParty();
				if ((party != null) && (party.getLeaderObjectId() == activeChar.getObjectId()))
				{
					party.broadcastPacket(sm);
				}
				else
				{
					activeChar.sendPacket(sm);
				}
			}
			else
			{
				activeChar.setEvenlyDistributedLoot(true);
				final SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.S1);
				sm.addString("Evenly distribution of party loot enabled.");
				final L2Party party = activeChar.getParty();
				if (party != null)
				{
					party.broadcastPacket(sm);
					
					if (CustomServerConfigs.EVENLY_DISTRIBUTED_ITEMS_SEND_LIST && (CustomServerConfigs.EVENLY_DISTRIBUTED_ITEMS_CACHED_HTML != null))
					{
						party.broadcastPacket(CustomServerConfigs.EVENLY_DISTRIBUTED_ITEMS_CACHED_HTML);
					}
				}
				else
				{
					activeChar.sendPacket(sm);
				}
			}
		}
		return true;
	}
	
	@Override
	public String[] getVoicedCommandList()
	{
		return VOICED_COMMANDS;
	}
}