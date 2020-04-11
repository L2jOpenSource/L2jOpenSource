/*
 * Copyright (C) L2J Sunrise
 * This file is part of L2J Sunrise.
 */
package l2r.gameserver.model.actor.instance.PcInstance;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

import l2r.gameserver.GameTimeController;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.data.xml.impl.FishData;
import l2r.gameserver.enums.audio.Music;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.tasks.player.LookingForFishTask;
import l2r.gameserver.model.effects.L2Effect;
import l2r.gameserver.model.fishing.L2Fish;
import l2r.gameserver.model.fishing.L2Fishing;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExFishingEnd;
import l2r.gameserver.network.serverpackets.ExFishingStart;
import l2r.util.Rnd;

import gr.sr.player.PcExtention;

/**
 * @author vGodFather
 */
public class L2PcFishing extends PcExtention
{
	private L2Fishing _fishCombat;
	private boolean _fishing = false;
	private int _fishx = 0;
	private int _fishy = 0;
	private int _fishz = 0;
	
	private L2Fish _fish;
	private L2ItemInstance _lure = null;
	public ScheduledFuture<?> _taskforfish;
	
	public L2PcFishing(L2PcInstance activeChar)
	{
		super(activeChar);
	}
	
	// startFishing() was stripped of any pre-fishing related checks, namely the fishing zone check.
	// Also worthy of note is the fact the code to find the hook landing position was also striped.
	// The stripped code was moved into fishing.java.
	// In my opinion it makes more sense for it to be there since all other skill related checks were also there.
	// Last but not least, moving the zone check there, fixed a bug where baits would always be consumed no matter if fishing actualy took place.
	// startFishing() now takes up 3 arguments, wich are acurately described as being the hook landing coordinates.
	public void startFishing(int _x, int _y, int _z)
	{
		getChar().stopMove(null);
		getChar().setIsImmobilized(true);
		_fishing = true;
		_fishx = _x;
		_fishy = _y;
		_fishz = _z;
		// broadcastUserInfo();
		// Starts fishing
		int lvl = getRandomFishLvl();
		int grade = getRandomFishGrade();
		int group = getRandomFishGroup(grade);
		List<L2Fish> fishs = FishData.getInstance().getFish(lvl, group, grade);
		if ((fishs == null) || fishs.isEmpty())
		{
			getChar().sendMessage("Error - Fishes are not definied");
			endFishing(false);
			return;
		}
		int check = Rnd.get(fishs.size());
		// Use a copy constructor else the fish data may be over-written below
		_fish = fishs.get(check).clone();
		fishs.clear();
		fishs = null;
		getChar().sendPacket(SystemMessageId.CAST_LINE_AND_START_FISHING);
		if (!GameTimeController.getInstance().isNight() && _lure.isNightLure())
		{
			_fish.setFishGroup(-1);
		}
		// sendMessage("Hook x,y: " + _x + "," + _y + " - Water Z, Player Z:" + _z + ", " + getZ()); //debug line, uncoment to show coordinates used in fishing.
		getChar().broadcastPacket(new ExFishingStart(getChar(), _fish.getFishGroup(), _x, _y, _z, _lure.isNightLure()));
		getChar().sendPacket(Music.SF_P_01.getPacket());
		startLookingForFishTask();
	}
	
	public void stopLookingForFishTask()
	{
		if (_taskforfish != null)
		{
			_taskforfish.cancel(false);
			_taskforfish = null;
		}
	}
	
	public void startLookingForFishTask()
	{
		if (!getChar().isDead() && (_taskforfish == null))
		{
			int checkDelay = 0;
			boolean isNoob = false;
			boolean isUpperGrade = false;
			
			if (_lure != null)
			{
				int lureid = _lure.getId();
				isNoob = _fish.getFishGrade() == 0;
				isUpperGrade = _fish.getFishGrade() == 2;
				if ((lureid == 6519) || (lureid == 6522) || (lureid == 6525) || (lureid == 8505) || (lureid == 8508) || (lureid == 8511))
				{
					checkDelay = _fish.getGutsCheckTime() * 133;
				}
				else if ((lureid == 6520) || (lureid == 6523) || (lureid == 6526) || ((lureid >= 8505) && (lureid <= 8513)) || ((lureid >= 7610) && (lureid <= 7613)) || ((lureid >= 7807) && (lureid <= 7809)) || ((lureid >= 8484) && (lureid <= 8486)))
				{
					checkDelay = _fish.getGutsCheckTime() * 100;
				}
				else if ((lureid == 6521) || (lureid == 6524) || (lureid == 6527) || (lureid == 8507) || (lureid == 8510) || (lureid == 8513))
				{
					checkDelay = _fish.getGutsCheckTime() * 66;
				}
			}
			_taskforfish = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new LookingForFishTask(getChar(), _fish.getStartCombatTime(), _fish.getFishGuts(), _fish.getFishGroup(), isNoob, isUpperGrade), 10000, checkDelay);
		}
	}
	
	private int getRandomFishGrade()
	{
		switch (_lure.getId())
		{
			case 7807: // green for beginners
			case 7808: // purple for beginners
			case 7809: // yellow for beginners
			case 8486: // prize-winning for beginners
				return 0;
			case 8485: // prize-winning luminous
			case 8506: // green luminous
			case 8509: // purple luminous
			case 8512: // yellow luminous
				return 2;
			default:
				return 1;
		}
	}
	
	private int getRandomFishGroup(int group)
	{
		int check = Rnd.get(100);
		int type = 1;
		switch (group)
		{
			case 0: // fish for novices
				switch (_lure.getId())
				{
					case 7807: // green lure, preferred by fast-moving (nimble) fish (type 5)
						if (check <= 54)
						{
							type = 5;
						}
						else if (check <= 77)
						{
							type = 4;
						}
						else
						{
							type = 6;
						}
						break;
					case 7808: // purple lure, preferred by fat fish (type 4)
						if (check <= 54)
						{
							type = 4;
						}
						else if (check <= 77)
						{
							type = 6;
						}
						else
						{
							type = 5;
						}
						break;
					case 7809: // yellow lure, preferred by ugly fish (type 6)
						if (check <= 54)
						{
							type = 6;
						}
						else if (check <= 77)
						{
							type = 5;
						}
						else
						{
							type = 4;
						}
						break;
					case 8486: // prize-winning fishing lure for beginners
						if (check <= 33)
						{
							type = 4;
						}
						else if (check <= 66)
						{
							type = 5;
						}
						else
						{
							type = 6;
						}
						break;
				}
				break;
			case 1: // normal fish
				switch (_lure.getId())
				{
					case 7610:
					case 7611:
					case 7612:
					case 7613:
						type = 3;
						break;
					case 6519: // all theese lures (green) are prefered by fast-moving (nimble) fish (type 1)
					case 8505:
					case 6520:
					case 6521:
					case 8507:
						if (check <= 54)
						{
							type = 1;
						}
						else if (check <= 74)
						{
							type = 0;
						}
						else if (check <= 94)
						{
							type = 2;
						}
						else
						{
							type = 3;
						}
						break;
					case 6522: // all theese lures (purple) are prefered by fat fish (type 0)
					case 8508:
					case 6523:
					case 6524:
					case 8510:
						if (check <= 54)
						{
							type = 0;
						}
						else if (check <= 74)
						{
							type = 1;
						}
						else if (check <= 94)
						{
							type = 2;
						}
						else
						{
							type = 3;
						}
						break;
					case 6525: // all theese lures (yellow) are prefered by ugly fish (type 2)
					case 8511:
					case 6526:
					case 6527:
					case 8513:
						if (check <= 55)
						{
							type = 2;
						}
						else if (check <= 74)
						{
							type = 1;
						}
						else if (check <= 94)
						{
							type = 0;
						}
						else
						{
							type = 3;
						}
						break;
					case 8484: // prize-winning fishing lure
						if (check <= 33)
						{
							type = 0;
						}
						else if (check <= 66)
						{
							type = 1;
						}
						else
						{
							type = 2;
						}
						break;
				}
				break;
			case 2: // upper grade fish, luminous lure
				switch (_lure.getId())
				{
					case 8506: // green lure, preferred by fast-moving (nimble) fish (type 8)
						if (check <= 54)
						{
							type = 8;
						}
						else if (check <= 77)
						{
							type = 7;
						}
						else
						{
							type = 9;
						}
						break;
					case 8509: // purple lure, preferred by fat fish (type 7)
						if (check <= 54)
						{
							type = 7;
						}
						else if (check <= 77)
						{
							type = 9;
						}
						else
						{
							type = 8;
						}
						break;
					case 8512: // yellow lure, preferred by ugly fish (type 9)
						if (check <= 54)
						{
							type = 9;
						}
						else if (check <= 77)
						{
							type = 8;
						}
						else
						{
							type = 7;
						}
						break;
					case 8485: // prize-winning fishing lure
						if (check <= 33)
						{
							type = 7;
						}
						else if (check <= 66)
						{
							type = 8;
						}
						else
						{
							type = 9;
						}
						break;
				}
		}
		return type;
	}
	
	private int getRandomFishLvl()
	{
		int skilllvl = getChar().getSkillLevel(1315);
		final L2Effect e = getChar().getFirstEffect(2274);
		if (e != null)
		{
			skilllvl = (int) e.getSkill().getPower();
		}
		if (skilllvl <= 0)
		{
			return 1;
		}
		int randomlvl;
		int check = Rnd.get(100);
		
		if (check <= 50)
		{
			randomlvl = skilllvl;
		}
		else if (check <= 85)
		{
			randomlvl = skilllvl - 1;
			if (randomlvl <= 0)
			{
				randomlvl = 1;
			}
		}
		else
		{
			randomlvl = skilllvl + 1;
			if (randomlvl > 27)
			{
				randomlvl = 27;
			}
		}
		
		return randomlvl;
	}
	
	public void startFishCombat(boolean isNoob, boolean isUpperGrade)
	{
		_fishCombat = new L2Fishing(getChar(), _fish, isNoob, isUpperGrade);
	}
	
	public void endFishing(boolean win)
	{
		_fishing = false;
		_fishx = 0;
		_fishy = 0;
		_fishz = 0;
		// broadcastUserInfo();
		if (_fishCombat == null)
		{
			getChar().sendPacket(SystemMessageId.BAIT_LOST_FISH_GOT_AWAY);
		}
		_fishCombat = null;
		_lure = null;
		// Ends fishing
		getChar().broadcastPacket(new ExFishingEnd(win, getChar()));
		getChar().sendPacket(SystemMessageId.REEL_LINE_AND_STOP_FISHING);
		getChar().setIsImmobilized(false);
		stopLookingForFishTask();
	}
	
	public L2Fishing getFishCombat()
	{
		return _fishCombat;
	}
	
	public int getFishx()
	{
		return _fishx;
	}
	
	public int getFishy()
	{
		return _fishy;
	}
	
	public int getFishz()
	{
		return _fishz;
	}
	
	public void setLure(L2ItemInstance lure)
	{
		_lure = lure;
	}
	
	public L2ItemInstance getLure()
	{
		return _lure;
	}
	
	public boolean isFishing()
	{
		return _fishing;
	}
	
	public void setFishing(boolean fishing)
	{
		_fishing = fishing;
	}
}
