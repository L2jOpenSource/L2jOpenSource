package l2f.gameserver.taskmanager;

import java.util.concurrent.Future;

import l2f.commons.threading.RunnableImpl;
import l2f.commons.threading.SteppingRunnableQueueManager;
import l2f.gameserver.ThreadPoolManager;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.Skill;
import l2f.gameserver.network.serverpackets.UserInfo;
import l2f.gameserver.utils.ItemFunctions;

public class AutoRechargeManager extends SteppingRunnableQueueManager
{
	private static final AutoRechargeManager _instance = new AutoRechargeManager();
	private static final int TYPE_CP = 0x01;
	private static final int TYPE_HP = 0x02;
	private static final int TYPE_MP = 0x03;
	private static final long CP_CHECK_TIME = 3000L; // 3 sec
	private static final long MP_CHECK_TIME = 7000L; // 7 sec (actually is 8, because task is set every second)
	private static final long HP_CHECK_TIME = 7000L; // 7 sec

	public static final AutoRechargeManager getInstance()
	{
		return _instance;
	}

	private AutoRechargeManager()
	{
		super(10000L);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this, 1000L, 1000L);
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl()
		{
			@Override
			public void runImpl()
			{
				AutoRechargeManager.this.purge();
			}

		}, 60000L, 60000L);
	}

	public Future<?> addAutoChargeTask(final Player player)
	{
		long delay = 1000L;
		
		return scheduleAtFixedRate(new RunnableImpl()
		{
			
			private long msHpLastCheck = System.currentTimeMillis();
			private long msMpLastCheck = System.currentTimeMillis();
			private long msCpLastCheck = System.currentTimeMillis();

			public boolean consumeItem(int itemId)
			{
				if (ItemFunctions.getItemCount(player, itemId) > 0)
				{
					Skill[] itemSkills = player.getInventory().getItemByItemId(itemId).getTemplate().getAttachedSkills();
					if (itemSkills.length > 0) {
						for (Skill itemSkill : itemSkills) {
							player.altUseSkill(itemSkill, player);
						}
					}
				}
				else
					return false;

				return true;
			}

			public void runValidationAndConsume(int type, int itemId, double percent)
			{
				switch (type)
				{
					case TYPE_CP:
						if ((player.getCurrentCp() / player.getMaxCp()) <= percent) {
							if (!consumeItem(itemId)) {
								player.AutoCp(false);
							}
						}
						break;
					case TYPE_HP:
						if((player.getCurrentHp() / player.getMaxHp()) <= percent) {
							if (!consumeItem(itemId)) {
								player.AutoHp(false);
							}
						}
						break;
					case TYPE_MP:
						if((player.getCurrentMp() / player.getMaxMp()) <= percent) {
							if (!consumeItem(itemId)) {
								player.AutoMp(false);
							}
						}
						break;
				}

				player.broadcastStatusUpdate();
				player.broadcastCharInfo();
				player.sendPacket(new UserInfo(player));
			}

			@Override
			public void runImpl()
			{
				long current = System.currentTimeMillis();

				if(player.isAfraid() || player.isAlikeDead() || player.isInOlympiadMode() || player.isDead() || player.isInFightClub() || player.isInTvT())
					 return;

				if(player._autoCp && (current >= (msCpLastCheck + CP_CHECK_TIME))) {
					runValidationAndConsume(TYPE_CP, Player.autoCp, 0.95);
					msCpLastCheck = current;
					//_log.info("Checking CP");
				}

				if(player._autoHp && (current >= (msHpLastCheck + HP_CHECK_TIME))) {
					runValidationAndConsume(TYPE_HP, Player.autoHp, 0.70);
					msHpLastCheck = current;
					//_log.info("Checking HP");
				}

				if(player._autoMp && (current >= (msMpLastCheck + MP_CHECK_TIME))) {
					runValidationAndConsume(TYPE_MP, Player.autoMp, 0.75);
					msMpLastCheck = current;
					//_log.info("Checking MP");
				}

			}
		}, delay, delay);
	}
}