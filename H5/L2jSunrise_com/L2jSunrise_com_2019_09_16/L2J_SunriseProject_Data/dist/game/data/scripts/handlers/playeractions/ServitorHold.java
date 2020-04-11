/*
 * This file is part of the L2J Sunrise project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.playeractions;

import l2r.gameserver.ai.L2SummonAI;
import l2r.gameserver.handler.IPlayerActionHandler;
import l2r.gameserver.model.actor.L2Summon;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.holders.ActionDataHolder;
import l2r.gameserver.network.SystemMessageId;

/**
 * Servitor hold position player action handler.
 * @author Nik
 */
public final class ServitorHold implements IPlayerActionHandler
{
	@Override
	public void useAction(L2PcInstance activeChar, ActionDataHolder data, boolean ctrlPressed, boolean shiftPressed)
	{
		final L2Summon servitor = activeChar.getSummon();
		if (servitor == null)
		{
			activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_A_PET);
		}
		else if (servitor.isBetrayed())
		{
			activeChar.sendPacket(SystemMessageId.YOUR_PET_SERVITOR_IS_UNRESPONSIVE_AND_WILL_NOT_OBEY_ANY_ORDERS);
		}
		else
		{
			((L2SummonAI) servitor.getAI()).notifyFollowStatusChange();
		}
	}
}
