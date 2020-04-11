package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.FeedableBeast;
import net.sf.l2j.gameserver.model.holder.SkillUseHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.skills.L2Skill;

public class BeastSpices implements IItemHandler
{
	@Override
	public void useItem(Playable playable, ItemInstance item, boolean forceUse)
	{
		if (!(playable instanceof Player))
			return;
		
		final Player player = (Player) playable;
		
		if (!(player.getTarget() instanceof FeedableBeast))
		{
			player.sendPacket(SystemMessageId.INCORRECT_TARGET);
			return;
		}
		/*
		 * int skillId = 0; switch (item.getItemId()) { case 6643: skillId = 2188; break; case 6644: skillId = 2189; break; }
		 */
		final L2Skill skill = item.getEtcItem().getSkills()[0].getSkill();// SkillTable.getInstance().getInfo(skillId, 1);
		if (skill != null)
			player.getAI().tryTo(IntentionType.CAST, new SkillUseHolder(skill, player.getTarget(), false, false), null);
	}
}