package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.commons.math.MathUtil;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.skills.EffectType;
import net.sf.l2j.gameserver.enums.skills.FlyType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.serverpackets.FlyToLocation;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.skills.AbstractEffect;
import net.sf.l2j.gameserver.skills.L2Skill;

public class EffectWarp extends AbstractEffect
{
	private int _x, _y, _z;
	private Creature _actor;
	
	public EffectWarp(EffectTemplate template, L2Skill skill, Creature effected, Creature effector)
	{
		super(template, skill, effected, effector);
	}
	
	@Override
	public EffectType getEffectType()
	{
		return EffectType.WARP;
	}
	
	@Override
	public boolean onStart()
	{
		_actor = isSelfEffect() ? getEffector() : getEffected();
		
		if (_actor.isMovementDisabled())
			return false;
		
		int _radius = getSkill().getFlyRadius();
		
		double angle = MathUtil.convertHeadingToDegree(_actor.getHeading());
		double radian = Math.toRadians(angle);
		double course = Math.toRadians(getSkill().getFlyCourse());
		
		int x1 = (int) (Math.cos(Math.PI + radian + course) * _radius);
		int y1 = (int) (Math.sin(Math.PI + radian + course) * _radius);
		
		_x = _actor.getX() + x1;
		_y = _actor.getY() + y1;
		_z = _actor.getZ();
		
		Location destiny = GeoEngine.getInstance().getValidLocation(_actor.getX(), _actor.getY(), _actor.getZ(), _x, _y, _z);
		_x = destiny.getX();
		_y = destiny.getY();
		_z = destiny.getZ();
		
		// TODO: check if this AI intention is retail-like. This stops player's previous movement
		_actor.getAI().tryTo(IntentionType.IDLE, null, null);
		
		_actor.broadcastPacket(new FlyToLocation(_actor, _x, _y, _z, FlyType.DUMMY));
		_actor.getAttack().stop();
		_actor.getCast().stop();
		
		_actor.setXYZ(_x, _y, _z);
		_actor.broadcastPacket(new ValidateLocation(_actor));
		
		return true;
	}
	
	@Override
	public boolean onActionTime()
	{
		return false;
	}
}