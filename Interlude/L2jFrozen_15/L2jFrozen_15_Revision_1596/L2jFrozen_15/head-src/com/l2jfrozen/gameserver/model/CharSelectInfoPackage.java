package com.l2jfrozen.gameserver.model;

/**
 * Used to Store data sent to Client for Character Selection screen.
 * @version $Revision: 1.2.2.2.2.4 $ $Date: 2005/03/27 15:29:33 $
 */
public class CharSelectInfoPackage
{
	private String name;
	private int objectId = 0;
	private int charId = 0x00030b7a;
	private long exp = 0;
	private int sp = 0;
	private int clanId = 0;
	private int race = 0;
	private int classId = 0;
	private int baseClassId = 0;
	private long deleteTimer = 0L;
	private long lastAccess = 0L;
	private int face = 0;
	private int hairStyle = 0;
	private int hairColor = 0;
	private int sex = 0;
	private int level = 1;
	private int maxHp = 0;
	private double currentHp = 0;
	private int maxMp = 0;
	private double currentMp = 0;
	private final int[][] paperdoll;
	private int karma = 0;
	private int augmentationId = 0;
	private int accessLevel;
	
	/**
	 * @param objectId
	 * @param name
	 */
	public CharSelectInfoPackage(final int objectId, final String name)
	{
		setObjectId(objectId);
		this.name = name;
		paperdoll = PcInventory.restoreVisibleInventory(objectId);
	}
	
	public int getObjectId()
	{
		return objectId;
	}
	
	public void setObjectId(final int objectId)
	{
		this.objectId = objectId;
	}
	
	public int getCharId()
	{
		return charId;
	}
	
	public void setCharId(final int charId)
	{
		this.charId = charId;
	}
	
	public int getClanId()
	{
		return clanId;
	}
	
	public void setClanId(final int clanId)
	{
		this.clanId = clanId;
	}
	
	public int getClassId()
	{
		return classId;
	}
	
	public int getBaseClassId()
	{
		return baseClassId;
	}
	
	public void setClassId(final int classId)
	{
		this.classId = classId;
	}
	
	public void setBaseClassId(final int baseClassId)
	{
		this.baseClassId = baseClassId;
	}
	
	public double getCurrentHp()
	{
		return currentHp;
	}
	
	public void setCurrentHp(final double currentHp)
	{
		this.currentHp = currentHp;
	}
	
	public double getCurrentMp()
	{
		return currentMp;
	}
	
	public void setCurrentMp(final double currentMp)
	{
		this.currentMp = currentMp;
	}
	
	public long getDeleteTimer()
	{
		return deleteTimer;
	}
	
	public void setDeleteTimer(final long deleteTimer)
	{
		this.deleteTimer = deleteTimer;
	}
	
	public long getLastAccess()
	{
		return lastAccess;
	}
	
	public void setLastAccess(final long lastAccess)
	{
		this.lastAccess = lastAccess;
	}
	
	public long getExp()
	{
		return exp;
	}
	
	public void setExp(final long exp)
	{
		this.exp = exp;
	}
	
	public int getFace()
	{
		return face;
	}
	
	public void setFace(final int face)
	{
		this.face = face;
	}
	
	public int getHairColor()
	{
		return hairColor;
	}
	
	public void setHairColor(final int hairColor)
	{
		this.hairColor = hairColor;
	}
	
	public int getHairStyle()
	{
		return hairStyle;
	}
	
	public void setHairStyle(final int hairStyle)
	{
		this.hairStyle = hairStyle;
	}
	
	public int getPaperdollObjectId(final int slot)
	{
		return paperdoll[slot][0];
	}
	
	public int getPaperdollItemId(final int slot)
	{
		return paperdoll[slot][1];
	}
	
	public int getLevel()
	{
		return level;
	}
	
	public void setLevel(final int level)
	{
		this.level = level;
	}
	
	public int getMaxHp()
	{
		return maxHp;
	}
	
	public void setMaxHp(final int maxHp)
	{
		this.maxHp = maxHp;
	}
	
	public int getMaxMp()
	{
		return maxMp;
	}
	
	public void setMaxMp(final int maxMp)
	{
		this.maxMp = maxMp;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(final String name)
	{
		this.name = name;
	}
	
	public int getRace()
	{
		return race;
	}
	
	public void setRace(final int race)
	{
		this.race = race;
	}
	
	public int getSex()
	{
		return sex;
	}
	
	public void setSex(final int sex)
	{
		this.sex = sex;
	}
	
	public int getSp()
	{
		return sp;
	}
	
	public void setSp(final int sp)
	{
		this.sp = sp;
	}
	
	public int getEnchantEffect()
	{
		if (paperdoll[Inventory.PAPERDOLL_RHAND][2] > 0)
		{
			return paperdoll[Inventory.PAPERDOLL_RHAND][2];
		}
		
		return paperdoll[Inventory.PAPERDOLL_LRHAND][2];
	}
	
	public void setKarma(final int k)
	{
		karma = k;
	}
	
	public int getKarma()
	{
		return karma;
	}
	
	public void setAugmentationId(final int augmentationId)
	{
		this.augmentationId = augmentationId;
	}
	
	public int getAugmentationId()
	{
		return augmentationId;
	}
	
	public int getAccessLevel()
	{
		return accessLevel;
	}
	
	public void setAccessLevel(final int accessLevel)
	{
		this.accessLevel = accessLevel;
	}
}
