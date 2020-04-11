/*
 * Copyright (C) 2004-2015 L2J Server
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

import java.util.Arrays;

import l2r.Config;
import l2r.gameserver.data.xml.impl.ActionData;
import l2r.gameserver.handler.IPlayerActionHandler;
import l2r.gameserver.handler.PlayerActionHandler;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.holders.ActionDataHolder;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ActionFailed;
import l2r.gameserver.network.serverpackets.ExBasicActionList;

/**
 * This class manages the action use request packet.
 * @author Zoey76
 */
public final class RequestActionUse extends L2GameClientPacket
{
	private static final String _C__56_REQUESTACTIONUSE = "[C] 56 RequestActionUse";
	
	private int _actionId;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;
	
	@Override
	protected void readImpl()
	{
		_actionId = readD();
		_ctrlPressed = (readD() == 1);
		_shiftPressed = (readC() == 1);
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (Config.DEBUG)
		{
			_log.info(activeChar + " requested action use Id: " + _actionId + " Ctrl pressed:" + _ctrlPressed + " Shift pressed:" + _shiftPressed);
		}
		
		// Don't do anything if player is dead or confused
		if ((activeChar.isFakeDeath() && (_actionId != 0)) || activeChar.isDead() || activeChar.isOutOfControl())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Don't do anything if player is confused
		if (activeChar.isOutOfControl())
		{
			sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		L2Effect ef = null;
		if (((ef = activeChar.getFirstEffect(L2EffectType.ACTION_BLOCK)) != null) && !ef.checkCondition(_actionId))
		{
			activeChar.sendPacket(SystemMessageId.YOU_HAVE_BEEN_REPORTED_SO_ACTIONS_NOT_ALLOWED);
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// Don't allow to do some action if player is transformed
		if (activeChar.isTransformed())
		{
			int[] allowedActions = activeChar.isTransformed() ? ExBasicActionList.ACTIONS_ON_TRANSFORM : ExBasicActionList.DEFAULT_ACTION_LIST;
			if (!(Arrays.binarySearch(allowedActions, _actionId) >= 0))
			{
				sendPacket(ActionFailed.STATIC_PACKET);
				if (Config.DEBUG)
				{
					_log.warn("Player " + activeChar + " used action which he does not have! Id = " + _actionId + " transform: " + activeChar.getTransformation());
				}
				return;
			}
		}
		
		final ActionDataHolder actionHolder = ActionData.getInstance().getActionData(_actionId);
		if (actionHolder != null)
		{
			final IPlayerActionHandler actionHandler = PlayerActionHandler.getInstance().getHandler(actionHolder.getHandler());
			if (actionHandler != null)
			{
				actionHandler.useAction(activeChar, actionHolder, _ctrlPressed, _shiftPressed);
				return;
			}
			_log.warn("Couldnt find handler with name: " + actionHolder.getHandler());
			return;
		}
		
		switch (_actionId)
		{
			default:
				_log.warn(activeChar.getName() + ": unhandled action type " + _actionId);
				break;
		}
	}
	
	@Override
	public String getType()
	{
		return _C__56_REQUESTACTIONUSE;
	}
	
	@Override
	protected boolean triggersOnActionRequest()
	{
		return (_actionId != 10) && (_actionId != 28);
	}
}
