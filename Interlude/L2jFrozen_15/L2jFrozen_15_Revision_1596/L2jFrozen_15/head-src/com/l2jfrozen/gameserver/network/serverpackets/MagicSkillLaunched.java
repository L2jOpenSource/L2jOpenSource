package com.l2jfrozen.gameserver.network.serverpackets;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;

/**
 * sample 0000: 8e d8 a8 10 48 10 04 00 00 01 00 00 00 01 00 00 ....H........... 0010: 00 d8 a8 10 48 ....H format ddddd d
 * @version $Revision: 1.4.2.1.2.3 $ $Date: 2005/03/27 15:29:57 $
 */
public class MagicSkillLaunched extends L2GameServerPacket
{
	private final int charObjId;
	private final int skillId;
	private final int skillLevel;
	private int numberOfTargets;
	private L2Object[] targets;
	private final int singleTargetId;
	
	public MagicSkillLaunched(final L2Character cha, final int skillId, final int skillLevel, final L2Object[] targets)
	{
		charObjId = cha.getObjectId();
		this.skillId = skillId;
		this.skillLevel = skillLevel;
		
		if (targets != null)
		{
			numberOfTargets = targets.length;
			this.targets = targets;
		}
		else
		{
			numberOfTargets = 1;
			final L2Object[] objs =
			{
				cha
			};
			this.targets = objs;
		}
		
		singleTargetId = 0;
	}
	
	public MagicSkillLaunched(final L2Character cha, final int skillId, final int skillLevel)
	{
		charObjId = cha.getObjectId();
		this.skillId = skillId;
		this.skillLevel = skillLevel;
		numberOfTargets = 1;
		singleTargetId = cha.getTargetId();
	}
	
	@Override
	protected final void writeImpl()
	{
		writeC(0x76);
		writeD(charObjId);
		writeD(skillId);
		writeD(skillLevel);
		writeD(numberOfTargets); // also failed or not?
		if (singleTargetId != 0 || numberOfTargets == 0)
		{
			writeD(singleTargetId);
		}
		else
		{
			for (final L2Object target : targets)
			{
				try
				{
					writeD(target.getObjectId());
				}
				catch (final NullPointerException e)
				{
					if (Config.ENABLE_ALL_EXCEPTIONS)
					{
						e.printStackTrace();
					}
					
					writeD(0); // untested
				}
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[S] 8E MagicSkillLaunched";
	}
	
}
