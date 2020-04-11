/*
 * Copyright (C) 2004-2013 L2J DataPack
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

import l2r.gameserver.datatables.xml.TransformData;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.network.SystemMessageId;

/**
 * Transformation effect.
 * @author nBd
 */
public class Transformation extends L2Effect
{
	public Transformation(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	public Transformation(Env env, L2Effect effect)
	{
		super(env, effect);
	}
	
	@Override
	public boolean canBeStolen()
	{
		return false;
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.TRANSFORMATION;
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopTransformation(false);
	}
	
	@Override
	public boolean onStart()
	{
		if (!getEffected().isPlayer())
		{
			return false;
		}
		
		L2PcInstance trg = getEffected().getActingPlayer();
		if ((trg == null) || trg.isAlikeDead() || trg.isCursedWeaponEquipped())
		{
			return false;
		}
		else if (trg.isSitting())
		{
			trg.sendPacket(SystemMessageId.CANNOT_TRANSFORM_WHILE_SITTING);
			return false;
		}
		else if (trg.isTransformed() || trg.isInStance())
		{
			getEffected().sendPacket(SystemMessageId.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
			return false;
		}
		else if (trg.isInWater())
		{
			getEffected().sendPacket(SystemMessageId.YOU_CANNOT_POLYMORPH_INTO_THE_DESIRED_FORM_IN_WATER);
			return false;
		}
		else if (trg.isFlyingMounted() || trg.isMounted())
		{
			getEffected().sendPacket(SystemMessageId.YOU_CANNOT_POLYMORPH_WHILE_RIDING_A_PET);
			return false;
		}
		
		TransformData.getInstance().transformPlayer(getSkill().getTransformId(), trg);
		return true;
	}
}
