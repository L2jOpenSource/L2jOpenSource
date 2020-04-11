package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.L2EffectFlag;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

/**
 * @author -Nemesiss-
 */
public class EffectPhysicalMute extends L2Effect
{
	public EffectPhysicalMute(Env env, EffectTemplate template)
	{
		super(env, template);
	}
	
	@Override
	public L2EffectType getEffectType()
	{
		return L2EffectType.PHYSICAL_MUTE;
	}
	
	@Override
	public boolean onStart()
	{
		getEffected().startPhysicalMuted();
		return true;
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
	
	@Override
	public void onExit()
	{
		getEffected().stopPhysicalMuted(false);
	}
	
	@Override
	public int getEffectFlags()
	{
		return L2EffectFlag.PHYSICAL_MUTED.getMask();
	}
}