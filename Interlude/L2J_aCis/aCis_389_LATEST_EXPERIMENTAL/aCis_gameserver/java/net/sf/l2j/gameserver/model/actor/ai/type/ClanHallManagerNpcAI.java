package net.sf.l2j.gameserver.model.actor.ai.type;

import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.ClanHallManagerNpc;
import net.sf.l2j.gameserver.model.holder.SkillUseHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.skills.L2Skill;

public class ClanHallManagerNpcAI extends CreatureAI
{
	public ClanHallManagerNpcAI(Creature creature)
	{
		super(creature);
	}
	
	@Override
	public ClanHallManagerNpc getActor()
	{
		return (ClanHallManagerNpc) _actor;
	}
	
	@Override
	protected void onIntentionCast(SkillUseHolder skillUseHolder, ItemInstance itemInstance)
	{
		final L2Skill skill = skillUseHolder.getSkill();
		
		if (getActor().isSkillDisabled(skill))
			return;
		
		final WorldObject target = skillUseHolder.getTarget();
		final Player player = (Player) target;
		
		if (!((skill.getMpConsume() + skill.getMpInitialConsume()) > getActor().getCurrentMp()))
			super.onIntentionCast(skillUseHolder, itemInstance);
		else
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(getActor().getObjectId());
			html.setFile("data/html/clanHallManager/support-no_mana.htm");
			html.replace("%mp%", (int) getActor().getCurrentMp());
			html.replace("%objectId%", getActor().getObjectId());
			player.sendPacket(html);
			return;
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getActor().getObjectId());
		html.setFile("data/html/clanHallManager/support-done.htm");
		html.replace("%mp%", (int) getActor().getCurrentMp());
		html.replace("%objectId%", getActor().getObjectId());
		player.sendPacket(html);
		
	}
	
	@Override
	protected void thinkCast()
	{
		final SkillUseHolder skillUseHolder = (SkillUseHolder) _currentIntention.getFirstParameter();
		final L2Skill skill = skillUseHolder.getSkill();
		
		final WorldObject target = skillUseHolder.getTarget();
		
		if (checkTargetLost(target))
		{
			getActor().getCast().setIsCastingNow(false);
			return;
		}
		
		getActor().getCast().doCast(skill);
	}
}