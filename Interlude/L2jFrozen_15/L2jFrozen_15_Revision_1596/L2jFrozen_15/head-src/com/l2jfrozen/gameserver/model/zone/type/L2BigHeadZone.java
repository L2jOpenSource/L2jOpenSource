package com.l2jfrozen.gameserver.model.zone.type;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.zone.L2ZoneType;

/**
 * @author durgus
 */
public class L2BigHeadZone extends L2ZoneType
{
	public L2BigHeadZone(int id)
	{
		super(id);
	}
	
	@Override
	protected void onEnter(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			character.startAbnormalEffect(L2Character.ABNORMAL_EFFECT_BIG_HEAD);
		}
	}
	
	@Override
	protected void onExit(L2Character character)
	{
		if (character instanceof L2PcInstance)
		{
			character.stopAbnormalEffect(L2Character.ABNORMAL_EFFECT_BIG_HEAD);
		}
	}
	
	@Override
	protected void onDieInside(L2Character character)
	{
		onExit(character);
	}
	
	@Override
	protected void onReviveInside(L2Character character)
	{
		onEnter(character);
	}
}
