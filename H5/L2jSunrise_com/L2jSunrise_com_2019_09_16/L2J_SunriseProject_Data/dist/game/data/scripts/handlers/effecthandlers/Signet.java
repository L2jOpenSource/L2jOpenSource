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

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.enums.ZoneIdType;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2EffectPointInstance;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.EffectTemplate;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.effects.L2EffectType;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.skills.l2skills.L2SkillSignet;
import l2r.gameserver.model.skills.l2skills.L2SkillSignetCasttime;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.MagicSkillUse;

/**
 * @author Forsaiken, Sami
 */
public class Signet extends L2Effect
{
	private L2Skill _skill;
	private L2EffectPointInstance _actor;
	private boolean _srcInArena;
	
	public Signet(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.SIGNET_EFFECT;
	}
	
	@Override
	public boolean onStart()
	{
		if ((getSkill() instanceof L2SkillSignet) || (getSkill() instanceof L2SkillSignetCasttime))
		{
			_skill = SkillData.getInstance().getInfo(getSkill().getEffectId(), getLevel());
		}
		
		_actor = (L2EffectPointInstance) getEffected();
		_srcInArena = (getEffector().isInsideZone(ZoneIdType.PVP) && !getEffector().isInsideZone(ZoneIdType.SIEGE));
		return true;
	}
	
	@Override
	public boolean onActionTime()
	{
		if (_skill == null)
		{
			return true;
		}
		int mpConsume = _skill.getMpConsume();
		
		if (mpConsume > getEffector().getCurrentMp())
		{
			getEffector().sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
			return false;
		}
		
		final L2PcInstance activeChar = getEffector().getActingPlayer();
		
		activeChar.reduceCurrentMp(mpConsume);
		List<L2Character> targets = new ArrayList<>();
		for (L2Character cha : _actor.getKnownList().getKnownCharactersInRadius(getSkill().getAffectRange()))
		{
			if (((cha == null) || cha.isAlikeDead()) || ((cha == activeChar) && _skill.isOffensive()) || (!_skill.isOffensive() && !cha.isPlayable()))
			{
				continue;
			}
			
			if (cha.isPlayable())
			{
				if (_skill.isOffensive() && !L2Skill.checkForAreaOffensiveSkills(getEffector(), cha, getSkill(), _srcInArena))
				{
					continue;
				}
				
				if (!_skill.isOffensive() && !cha.getActingPlayer().isFriend(activeChar))
				{
					continue;
				}
				
				if (cha.isPlayer() && activeChar.isPlayer() && _skill.isOffensive() && activeChar.getActingPlayer().isFriend(cha.getActingPlayer()))
				{
					continue;
				}
			}
			
			// there doesn't seem to be a visible effect with MagicSkillLaunched packet...
			_actor.broadcastPacket(new MagicSkillUse(_actor, cha, _skill.getId(), _skill.getLevel(), 0, 0));
			targets.add(cha);
		}
		
		if (!targets.isEmpty())
		{
			getEffector().callSkill(_skill, targets.toArray(new L2Character[targets.size()]));
		}
		return true;
	}
	
	@Override
	public void onExit()
	{
		if (_actor != null)
		{
			_actor.deleteMe();
		}
	}
}
