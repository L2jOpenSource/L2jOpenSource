/*
 * Copyright (C) 2004-2015 L2J DataPack
 *
 * This file is part of L2J DataPack.
 *
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package handlers.effecthandlers;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.PcInstance.PcFunc;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.holders.SummonRequestHolder;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ConfirmDlg;
import l2r.gameserver.network.serverpackets.SystemMessage;

/**
 * Call Pc effect implementation.
 * @author Adry_85
 */
public class CallPc extends L2Effect
{
	private static int _itemId;
	private static int _itemCount;
	
	public CallPc(Env env, EffectTemplate template)
	{
		super(env, template);
		_itemId = template.getParameters().getInt("itemId", 0);
		_itemCount = template.getParameters().getInt("itemCount", 0);
	}
	
	@Override
	public boolean isInstant()
	{
		return true;
	}
	
	@Override
	public boolean onStart()
	{
		if (getEffected() == getEffector())
		{
			return false;
		}
		
		L2PcInstance target = getEffected().getActingPlayer();
		L2PcInstance activeChar = getEffector().getActingPlayer();
		
		if (!PcFunc.checkSummonTargetStatus(target, activeChar))
		{
			return false;
		}
		
		if (target.getScript(SummonRequestHolder.class) != null)
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.C1_ALREADY_SUMMONED);
			sm.addString(target.getName());
			activeChar.sendPacket(sm);
			return false;
		}
		
		target.addScript(new SummonRequestHolder(activeChar, _itemId, _itemCount));
		final ConfirmDlg confirm = new ConfirmDlg(SystemMessageId.C1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId());
		confirm.addCharName(activeChar);
		confirm.addZoneName(activeChar.getX(), activeChar.getY(), activeChar.getZ());
		confirm.addTime(30000);
		confirm.addRequesterId(activeChar.getObjectId());
		target.sendPacket(confirm);
		return true;
	}
}
