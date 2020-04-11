package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;

public final class AttackRequest extends L2GameClientPacket
{
	private int _objectId;
	@SuppressWarnings("unused")
	private int _originX, _originY, _originZ;
	private boolean _isShiftAction;
	
	@Override
	protected void readImpl()
	{
		_objectId = readD();
		_originX = readD();
		_originY = readD();
		_originZ = readD();
		_isShiftAction = readC() != 0;
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		if (player.isInObserverMode())
		{
			player.sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE);
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// avoid using expensive operations if not needed
		final WorldObject target;
		if (player.getTargetId() == _objectId)
			target = player.getTarget();
		else
			target = World.getInstance().getObject(_objectId);
		
		if (target == null)
		{
			player.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		
		// This happens when you control + click a target without having had it selected beforehand. Behaves as the Action packet and will not trigger an attack.
		if (player.getTarget() != target)
			target.onAction(player);
		else
		{
			if ((target.getObjectId() != player.getObjectId()) && !player.isOperating() && player.getActiveRequester() == null)
				target.onForcedAttack(player, _isShiftAction);
			else
				sendPacket(ActionFailed.STATIC_PACKET);
		}
	}
}