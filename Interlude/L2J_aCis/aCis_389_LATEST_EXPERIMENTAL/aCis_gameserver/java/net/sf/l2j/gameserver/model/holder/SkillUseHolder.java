package net.sf.l2j.gameserver.model.holder;

import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.skills.L2Skill;

/**
 * Skill casting information (used to queue when several skills are cast in a short time)
 **/
public class SkillUseHolder
{
	private L2Skill _skill;
	private WorldObject _target;
	private boolean _ctrlPressed;
	private boolean _shiftPressed;
	
	public SkillUseHolder()
	{
	}
	
	public SkillUseHolder(L2Skill skill, WorldObject target, boolean ctrlPressed, boolean shiftPressed)
	{
		_skill = skill;
		_target = target;
		_ctrlPressed = ctrlPressed;
		_shiftPressed = shiftPressed;
	}
	
	@Override
	public String toString()
	{
		return "SkillUseHolder [skill=" + ((_skill == null) ? "none" : _skill.getName()) + " target=" + ((_target == null) ? "none" : _target.getName()) + " ctrl=" + _ctrlPressed + " shift=" + _shiftPressed + "]";
	}
	
	public L2Skill getSkill()
	{
		return _skill;
	}
	
	public int getSkillId()
	{
		return (getSkill() != null) ? getSkill().getId() : -1;
	}
	
	public WorldObject getTarget()
	{
		return _target;
	}
	
	public boolean isCtrlPressed()
	{
		return _ctrlPressed;
	}
	
	public boolean isShiftPressed()
	{
		return _shiftPressed;
	}
	
	public void setSkill(L2Skill skill)
	{
		_skill = skill;
	}
	
	public void setTarget(WorldObject target)
	{
		_target = target;
	}
	
	public void setCtrlPressed(boolean ctrlPressed)
	{
		_ctrlPressed = ctrlPressed;
	}
	
	public void setShiftPressed(boolean shiftPressed)
	{
		_shiftPressed = shiftPressed;
	}
}