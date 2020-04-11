package l2f.gameserver.model.instances;


import l2f.gameserver.model.Player;
import l2f.gameserver.network.serverpackets.L2GameServerPacket;
import l2f.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import l2f.gameserver.templates.npc.NpcTemplate;

public final class ClanRewardInstance extends NpcInstance
{
	private static final long serialVersionUID = 5938813598479742068L;

	public ClanRewardInstance(int objectId, NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(Player player, String command)
	{
		if (!canBypassCheck(player, this))
		{
			return;
		}
		
		if (command.equalsIgnoreCase("getClanReward"))
		{
			if (player.getClan() != null)
			{
				if(player.getClan().getOnlineMembers().size() < 15)
				{
						player.sendMessage("You must have atleast 15 members online to receive reward.");
						return;
				}
				
				if(!player.isClanLeader())
				{
						player.sendMessage("You must be a clan leader in order to receive reward.");
						return;
				}
				
				if(player.getClan().getLevel() >= 6)
				{
						player.sendMessage("Your clan is already level 6 and can't receive reward.");
						return;
				}
				
				if(player.getInventory().getCountOf(37007) < 5)
				{
						player.sendMessage("You don't have enough Vote coins!");
						return;
				}
				
				player.getClan().setLevel(6);
				player.getClan().incReputation(30000, false, "ClanRewardNpc");
				player.getClan().broadcastToOnlineMembers(new L2GameServerPacket[]
				{
					new PledgeShowInfoUpdate(player.getClan())
				});
				player.sendMessage("Your clan received 30 000 clan reputation and level 6 from Clan Reward!");
				player.getInventory().addItem(9816, 30, "ClanReward Earth Eggs");
				player.getInventory().addItem(9818, 10, "ClanReward Angelic Essence");
				player.getInventory().addItem(9817, 20, "ClanReward Angelic Essence");
				player.getInventory().addItem(9815, 20, "ClanReward Angelic Essence");
				player.getInventory().addItem(8176, 1, "ClanReward Destruction Tombstone");
				player.getInventory().destroyItemByItemId(37007, 5, "Clan Reward NPC");
			}
			else
			{
				player.sendMessage("You don't have clan to use this feature!");
			}
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	public boolean isNpc()
	{
		return true;
	}
	
}