package l2f.gameserver.model.items.attachment;

import l2f.gameserver.model.Creature;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.Skill;

public interface FlagItemAttachment extends PickableAttachment
{
	//FIXME [Grivesky] may alter the listener Player
	void onLogout(Player player);
	//FIXME [Grivesky] may alter the listener Player
	void onDeath(Player owner, Creature killer);

	boolean canAttack(Player player);

	boolean canCast(Player player, Skill skill);

	boolean canBeLost();

	boolean canBeUnEquiped();
}
