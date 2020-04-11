package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.gameserver.model.L2Character;

/**
 * sample 0000: 5a d8 a8 10 48 d8 a8 10 48 10 04 00 00 01 00 00 Z...H...H....... 0010: 00 f0 1a 00 00 68 28 00 00 .....h(.. format dddddd dddh (h)
 * @version $Revision: 1.4.2.1.2.4 $ $Date: 2005/03/27 15:29:39 $
 */
public class MagicSkillUser extends L2GameServerPacket
{
	private int targetId;
	private final int skillId;
	private final int skillLevel;
	private final int hitTime;
	private final int reuseDelay;
	private final int charObjId, x, y, z;
	
	public MagicSkillUser(final L2Character cha, final L2Character target, final int skillId, final int skillLevel, final int hitTime, final int reuseDelay)
	{
		charObjId = cha.getObjectId();
		if (target != null)
		{
			targetId = target.getObjectId();
		}
		else
		{
			targetId = cha.getTargetId();
		}
		this.skillId = skillId;
		this.skillLevel = skillLevel;
		this.hitTime = hitTime;
		this.reuseDelay = reuseDelay;
		x = cha.getX();
		y = cha.getY();
		z = cha.getZ();
	}
	
	public MagicSkillUser(final L2Character cha, final int skillId, final int skillLevel, final int hitTime, final int reuseDelay)
	{
		charObjId = cha.getObjectId();
		targetId = cha.getTargetId();
		this.skillId = skillId;
		this.skillLevel = skillLevel;
		this.hitTime = hitTime;
		this.reuseDelay = reuseDelay;
		x = cha.getX();
		y = cha.getY();
		z = cha.getZ();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x48);
		writeD(charObjId);
		writeD(targetId);
		writeD(skillId);
		writeD(skillLevel);
		writeD(hitTime);
		writeD(reuseDelay);
		writeD(x);
		writeD(y);
		writeD(z);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
		writeH(0x00);
	}
	
	@Override
	public String getType()
	{
		return "[S] 5A MagicSkillUser";
	}
}