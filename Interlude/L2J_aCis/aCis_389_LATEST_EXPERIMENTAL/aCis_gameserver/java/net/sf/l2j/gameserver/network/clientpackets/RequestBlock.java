package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.sql.PlayerInfoTable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.container.player.BlockList;
import net.sf.l2j.gameserver.network.SystemMessageId;

public final class RequestBlock extends L2GameClientPacket
{
	private static final int BLOCK = 0;
	private static final int UNBLOCK = 1;
	private static final int BLOCKLIST = 2;
	private static final int ALLBLOCK = 3;
	private static final int ALLUNBLOCK = 4;
	
	private String _targetName;
	private int _type;
	
	@Override
	protected void readImpl()
	{
		_type = readD(); // 0x00 - block, 0x01 - unblock, 0x03 - allblock, 0x04 - allunblock
		
		if (_type == BLOCK || _type == UNBLOCK)
			_targetName = readS();
	}
	
	@Override
	protected void runImpl()
	{
		final Player player = getClient().getPlayer();
		if (player == null)
			return;
		
		switch (_type)
		{
			case BLOCK:
			case UNBLOCK:
				// Can't block/unblock inexisting or self.
				final int targetId = PlayerInfoTable.getInstance().getPlayerObjectId(_targetName);
				if (targetId <= 0 || player.getObjectId() == targetId)
				{
					player.sendPacket(SystemMessageId.FAILED_TO_REGISTER_TO_IGNORE_LIST);
					return;
				}
				
				// Can't block a GM character.
				if (PlayerInfoTable.getInstance().getPlayerAccessLevel(targetId) > 0)
				{
					player.sendPacket(SystemMessageId.YOU_MAY_NOT_IMPOSE_A_BLOCK_ON_GM);
					return;
				}
				
				if (_type == BLOCK)
					BlockList.addToBlockList(player, targetId);
				else
					BlockList.removeFromBlockList(player, targetId);
				break;
			
			case BLOCKLIST:
				BlockList.sendListToOwner(player);
				break;
			
			case ALLBLOCK:
				player.sendPacket(SystemMessageId.MESSAGE_REFUSAL_MODE);// Update by rocknow
				BlockList.setBlockAll(player, true);
				break;
			
			case ALLUNBLOCK:
				player.sendPacket(SystemMessageId.MESSAGE_ACCEPTANCE_MODE);// Update by rocknow
				BlockList.setBlockAll(player, false);
				break;
			
			default:
				LOGGER.warn("Unknown block type detected: {}.", _type);
		}
	}
}