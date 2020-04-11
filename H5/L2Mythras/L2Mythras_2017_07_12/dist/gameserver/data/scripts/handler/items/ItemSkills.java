package handler.items;

import gnu.trove.set.hash.TIntHashSet;
import l2f.gameserver.data.xml.holder.ItemHolder;
import l2f.gameserver.handler.items.ItemHandler;
import l2f.gameserver.model.Creature;
import l2f.gameserver.model.Playable;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.Skill;
import l2f.gameserver.network.serverpackets.SystemMessage2;
import l2f.gameserver.network.serverpackets.components.SystemMsg;
import l2f.gameserver.model.items.ItemInstance;
import l2f.gameserver.scripts.ScriptFile;
import l2f.gameserver.templates.item.ItemTemplate;

public class ItemSkills extends ScriptItemHandler implements ScriptFile
{
	private int[] _itemIds;


	@Override
	public boolean pickupItem(Playable playable, ItemInstance item)
	{
		return true;
	}

	@Override
	public void onLoad()
	{
		ItemHandler.getInstance().registerItemHandler(this);
	}

	@Override
	public void onReload()
	{

	}

	@Override
	public void onShutdown()
	{

	}

	public ItemSkills()
	{
		TIntHashSet set = new TIntHashSet();
		for (ItemTemplate template : ItemHolder.getInstance().getAllTemplates())
		{
			if (template == null)
				continue;

			for (Skill skill : template.getAttachedSkills())
				if (skill.isHandler())
					set.add(template.getItemId());
		}
		_itemIds = set.toArray();
	}

	@Override
	public boolean useItem(Playable playable, ItemInstance item, boolean ctrl)
	{
		Player player;
		if (playable.isPlayer())
			player = (Player) playable;
		else if (playable.isPet())
			player = playable.getPlayer();
		else
			return false;

		int itemId = item.getItemId();
		
		if (ctrl && ( itemId == Player.autoHp || itemId == Player.autoCp || itemId == Player.autoMp))
		{

			if(itemId == Player.autoCp)
			{
				if(player._autoCp)
				{
					player.AutoCp(false);
					player.sendPacket(new SystemMessage2(SystemMsg.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_DEACTIVATED).addString(item.getName()));
				}
				else
				{
					player.AutoCp(true);
					player.sendPacket(new SystemMessage2(SystemMsg.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_ACTIVATED).addString(item.getName()));
				}
			}
			else if(itemId == Player.autoHp)
			{
				if(player._autoHp)
				{
					player.AutoHp(false);
					player.sendPacket(new SystemMessage2(SystemMsg.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_DEACTIVATED).addString(item.getName()));
				}
				else
				{
					player.AutoHp(true);
					player.sendPacket(new SystemMessage2(SystemMsg.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_ACTIVATED).addString(item.getName()));
				}
			}
			else if(itemId == Player.autoMp)
			{
				if(player._autoMp)
				{
					player.AutoMp(false);
					player.sendPacket(new SystemMessage2(SystemMsg.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_DEACTIVATED).addString(item.getName()));
				}
				else
				{
					player.AutoMp(true);
					player.sendPacket(new SystemMessage2(SystemMsg.THE_AUTOMATIC_USE_OF_S1_HAS_BEEN_ACTIVATED).addString(item.getName()));
				}
			}
			
			return false; // Avoid the use of potion when enabling/disabling feature
		}
		else
		{
			Skill[] skills = item.getTemplate().getAttachedSkills();

			for (int i = 0; i < skills.length; i++)
			{
				Skill skill = skills[i];
				Creature aimingTarget = skill.getAimingTarget(player, player.getTarget());
				if (skill.checkCondition(player, aimingTarget, ctrl, false, true))
					player.getAI().Cast(skill, aimingTarget, ctrl, false);
				else if (i == 0)  //FIXME [VISTALL] всегда первый скил идет вместо конда?
					return false;
			}
		}
		
		return true;
	}

	@Override
	public int[] getItemIds()
	{
		return _itemIds;
	}
}
