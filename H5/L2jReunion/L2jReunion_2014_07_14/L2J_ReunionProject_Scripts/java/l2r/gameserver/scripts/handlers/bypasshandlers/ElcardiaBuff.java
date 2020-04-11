package l2r.gameserver.scripts.handlers.bypasshandlers;

import java.util.StringTokenizer;

import l2r.gameserver.handler.IBypassHandler;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.holders.SkillHolder;

public class ElcardiaBuff implements IBypassHandler
{
	private static final String[] COMMANDS =
	{
		"Request_Blessing"
	};
	
	private final int[][] BUFFS =
	{
		{
			6714,
			6715,
			6716,
			6718,
			6719,
			6720,
			6727,
			6729
		},
		{
			6714,
			6717,
			6720,
			6721,
			6722,
			6723,
			6727,
			6729
		}
	};
	
	@Override
	public boolean useBypass(String command, L2PcInstance activeChar, L2Character target)
	{
		if (!target.isNpc())
		{
			return false;
		}
		
		final L2Npc npc = (L2Npc) target;
		final StringTokenizer st = new StringTokenizer(command);
		try
		{
			String cmd = st.nextToken();
			
			if (cmd.equalsIgnoreCase(COMMANDS[0]))
			{
				for (int skillId : BUFFS[activeChar.isMageClass() ? 1 : 0])
				{
					SkillHolder skill = new SkillHolder(skillId, 1);
					
					if (skill.getSkill() != null)
					{
						npc.setTarget(activeChar);
						npc.doCast(skill.getSkill());
					}
				}
				return true;
			}
		}
		catch (Exception e)
		{
			_log.warn("Exception in " + getClass().getSimpleName(), e);
		}
		return false;
	}
	
	@Override
	public String[] getBypassList()
	{
		return COMMANDS;
	}
}