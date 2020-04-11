package com.l2jfrozen.gameserver.model.actor.instance;

import com.l2jfrozen.gameserver.managers.CustomNpcInstanceManager;
import com.l2jfrozen.gameserver.managers.CustomNpcInstanceManager.NpcToPlayer;
import com.l2jfrozen.gameserver.model.base.ClassId;
import com.l2jfrozen.gameserver.model.base.Race;
import com.l2jfrozen.gameserver.templates.L2WeaponType;
import com.l2jfrozen.util.random.Rnd;

/**
 * This class manages Npc Polymorph into player instances, they look like regular players. This effect will show up on all clients.
 * @author Darki699
 */
public class L2CustomNpcInstance
{
	
	private boolean allowRandomWeapons = true; // Default value
	private boolean allowRandomClass = true; // Default value
	private boolean allowRandomAppearance = true; // Default value
	private String name;
	private String title;
	
	private int pcInt[]; // PcInstance integer stats
	private boolean pcBoolean[]; // PcInstance booolean stats
	private L2NpcInstance npcInstance; // Reference to Npc with this stats
	private ClassId classId; // ClassId of this (N)Pc
	
	/**
	 * @param npc - Receives the L2NpcInstance as a reference.
	 */
	public L2CustomNpcInstance(L2NpcInstance npc)
	{
		if (npc == null)
		{
			return;
		}
		
		npcInstance = npc;
		if (npcInstance.getSpawn() == null)
		{
			return;
		}
		
		initialize();
	}
	
	/**
	 * Initializes the semi PcInstance stats for this NpcInstance, making it appear as a PcInstance on all clients
	 */
	private void initialize()
	{
		pcInt = new int[25];
		// karma=1, clanId=2, allyId=3, clanCrest=4, allyCrest=5, race=6, classId=7
		// EnchantWeapon=8, PledgeClass=9, CursedWeaponLevel=10
		// RightHand=11, LeftHand=12, Gloves=13, Chest=14, Legs=15, Feet=16, Hair1=17, Hair2=18
		// HairStyle=19, HairColor=20, Face=21
		// NameColor=22, TitleColor=23
		
		pcBoolean = new boolean[4];
		// pvp=0 , noble=1, hero=2, isFemaleSex=3
		
		// load the Player Morph Data
		NpcToPlayer ci = CustomNpcInstanceManager.getInstance().getCustomData(npcInstance.getSpawn().getId(), npcInstance.getNpcId());
		
		if (ci == null)
		{
			npcInstance.setCustomNpcInstance(null);
			npcInstance = null;
			return;
		}
		
		npcInstance.setCustomNpcInstance(this);
		
		setPcInstanceData(ci);
		
		if (allowRandomClass)
		{
			chooseRandomClass();
		}
		
		if (allowRandomAppearance)
		{
			chooseRandomAppearance();
		}
		
		if (allowRandomWeapons)
		{
			chooseRandomWeapon();
		}
	}
	
	/**
	 * @return the custom npc's name, or the original npc name if no custom name is provided
	 */
	public final String getName()
	{
		return name == null ? npcInstance.getName() : name;
	}
	
	/**
	 * @return the custom npc's title, or the original npc title if no custom title is provided
	 */
	public final String getTitle()
	{
		return title == null ? npcInstance.getTitle() : npcInstance.isChampion() ? "The Champion" + title : title;
	}
	
	/**
	 * @return the npc's karma or aggro range if he has any...
	 */
	public final int getKarma()
	{
		return pcInt[1] > 0 ? pcInt[1] : npcInstance.getAggroRange();
	}
	
	/**
	 * @return the clan Id
	 */
	public final int getClanId()
	{
		return pcInt[2];
	}
	
	/**
	 * @return the ally Id
	 */
	public final int getAllyId()
	{
		return pcInt[3];
	}
	
	/**
	 * @return the clan crest Id
	 */
	public final int getClanCrestId()
	{
		return pcInt[4];
	}
	
	/**
	 * @return the ally crest Id
	 */
	public final int getAllyCrestId()
	{
		return pcInt[5];
	}
	
	/**
	 * @return the Race ordinal
	 */
	public final int getRace()
	{
		return pcInt[6];
	}
	
	/**
	 * @return the class id, e.g.: fighter, warrior, mystic muse...
	 */
	public final int getClassId()
	{
		return pcInt[7];
	}
	
	/**
	 * @return the enchant level of the equipped weapon, if one is equipped (max = 127)
	 */
	public final int getEnchantWeapon()
	{
		return PAPERDOLL_RHAND() == 0 || getCursedWeaponLevel() != 0 ? 0 : pcInt[8] > 127 ? 127 : pcInt[8];
	}
	
	/**
	 * @return the pledge class identifier, e.g. vagabond, baron, marquiz
	 * @remark Champion mobs are always Marquiz
	 */
	public final int getPledgeClass()
	{
		return npcInstance.isChampion() ? 8 : pcInt[9];
	}
	
	/**
	 * @return the cursed weapon level, if one is equipped
	 */
	public final int getCursedWeaponLevel()
	{
		return PAPERDOLL_RHAND() == 0 || pcInt[8] > 0 ? 0 : pcInt[10];
	}
	
	/**
	 * @return the item id for the item in the right hand, if a custom item is not equipped the value returned is the original npc right-hand weapon id
	 */
	public final int PAPERDOLL_RHAND()
	{
		return pcInt[11] != 0 ? pcInt[11] : npcInstance.getRightHandItem();
	}
	
	/**
	 * @return the item id for the item in the left hand, if a custom item is not equipped the value returned is the original npc left-hand weapon id. Setting this value int[12] = -1 will not allow a npc to have anything in the left hand
	 */
	public final int PAPERDOLL_LHAND()
	{
		return pcInt[12] > 0 ? pcInt[12] : pcInt[12] == 0 ? npcInstance.getLeftHandItem() : 0;
	}
	
	/**
	 * @return the item id for the gloves
	 */
	public final int PAPERDOLL_GLOVES()
	{
		return pcInt[13];
	}
	
	/**
	 * @return the item id for the chest armor
	 */
	public final int PAPERDOLL_CHEST()
	{
		return pcInt[14];
	}
	
	/**
	 * @return the item id for the leg armor, or 0 if wearing a full armor
	 */
	public final int PAPERDOLL_LEGS()
	{
		return pcInt[15];
	}
	
	/**
	 * @return the item id for feet armor
	 */
	public final int PAPERDOLL_FEET()
	{
		return pcInt[16];
	}
	
	/**
	 * @return the item id for the 1st hair slot, or all hair
	 */
	public final int PAPERDOLL_HAIR()
	{
		return pcInt[17];
	}
	
	/**
	 * @return the item id for the 2nd hair slot
	 */
	public final int PAPERDOLL_HAIR2()
	{
		return pcInt[18];
	}
	
	/**
	 * @return the npc's hair style appearance
	 */
	public final int getHairStyle()
	{
		return pcInt[19];
	}
	
	/**
	 * @return the npc's hair color appearance
	 */
	public final int getHairColor()
	{
		return pcInt[20];
	}
	
	/**
	 * @return the npc's face appearance
	 */
	public final int getFace()
	{
		return pcInt[21];
	}
	
	/**
	 * @return the npc's name color (in hexadecimal), 0xFFFFFF is the default value
	 */
	public final int nameColor()
	{
		return pcInt[22] == 0 ? 0xFFFFFF : pcInt[22];
	}
	
	/**
	 * @return the npc's title color (in hexadecimal), 0xFFFF77 is the default value
	 */
	public final int titleColor()
	{
		return pcInt[23] == 0 ? 0xFFFF77 : pcInt[23];
	}
	
	/**
	 * @return is npc in pvp mode?
	 */
	public final boolean getPvpFlag()
	{
		return pcBoolean[0];
	}
	
	/**
	 * @return is npc in pvp mode?
	 */
	public final int getHeading()
	{
		return npcInstance.getHeading();
	}
	
	/**
	 * @return true if npc is a noble
	 */
	public final boolean isNoble()
	{
		return pcBoolean[1];
	}
	
	/**
	 * @return true if hero glow should show up
	 * @remark A Champion mob will always have hero glow
	 */
	public final boolean isHero()
	{
		return npcInstance.isChampion() ? true : pcBoolean[2];
	}
	
	/**
	 * @return true if female, false if male
	 * @remark In the DB, if you set
	 * @MALE   value=0
	 * @FEMALE value=1
	 * @MAYBE  value=2 % chance for the <b>Entire Template</b> to become male or female (it's a maybe value) If female, all template will be female, if Male, all template will be male
	 */
	public final boolean isFemaleSex()
	{
		return pcBoolean[3];
	}
	
	/**
	 * Choose a random weapon for this L2CustomNpcInstance
	 */
	private final void chooseRandomWeapon()
	{
		L2WeaponType wpnType = null;
		{
			wpnType = Rnd.get(100) > 40 ? L2WeaponType.BOW : L2WeaponType.BOW;
			{
				wpnType = L2WeaponType.BOW;
			}
		}
		{
			while (true) // Choose correct weapon TYPE
			{
				wpnType = L2WeaponType.values()[Rnd.get(L2WeaponType.values().length)];
				if (wpnType == null)
				{
					continue;
				}
				else if (wpnType == L2WeaponType.BOW || wpnType == L2WeaponType.BOW)
				{
					continue;
				}
				else if (classId.getRace() == Race.human)
				{
				}
				break;
			}
		}
		if (Rnd.get(100) < 10)
		{
		}
	}
	
	/**
	 * Choose a random class & race for this L2CustomNpcInstance
	 */
	private final void chooseRandomClass()
	{
		while (true)
		{
			classId = ClassId.values()[Rnd.get(ClassId.values().length)];
			if (classId == null)
			{
				continue;
			}
			else if (classId.getRace() != null && classId.getParent() != null)
			{
				break;
			}
		}
		pcInt[6] = classId.getRace().ordinal();
		pcInt[7] = classId.getId();
	}
	
	/**
	 * Choose random appearance for this L2CustomNpcInstance
	 */
	private final void chooseRandomAppearance()
	{
		// Karma=1, PledgeClass=9
		// HairStyle=19, HairColor=20, Face=21
		// NameColor=22, TitleColor=23
		// noble=1, hero=2, isFemaleSex=3
		pcBoolean[1] = Rnd.get(100) < 15 ? true : false;
		pcBoolean[3] = Rnd.get(100) < 50 ? true : false;
		pcInt[22] = pcInt[23] = 0;
		if (Rnd.get(100) < 5)
		{
			pcInt[22] = 0x0000FF;
		}
		else if (Rnd.get(100) < 5)
		{
			pcInt[22] = 0x00FF00;
		}
		if (Rnd.get(100) < 5)
		{
			pcInt[23] = 0x0000FF;
		}
		else if (Rnd.get(100) < 5)
		{
			pcInt[23] = 0x00FF00;
		}
		pcInt[1] = Rnd.get(100) > 95 ? 0 : Rnd.get(100) > 10 ? 50 : 1000;
		pcInt[19] = Rnd.get(100) < 34 ? 0 : Rnd.get(100) < 34 ? 1 : 2;
		pcInt[20] = Rnd.get(100) < 34 ? 0 : Rnd.get(100) < 34 ? 1 : 2;
		pcInt[21] = Rnd.get(100) < 34 ? 0 : Rnd.get(100) < 34 ? 1 : 2;
		
		final int pledgeLevel = Rnd.get(100);
		// 30% is left for either pledge=0 or default sql data
		// Only Marqiz are Champion mobs
		if (pledgeLevel > 30)
		{
			pcInt[9] = 1;
		}
		if (pledgeLevel > 50)
		{
			pcInt[9] = 2;
		}
		if (pledgeLevel > 60)
		{
			pcInt[9] = 3;
		}
		if (pledgeLevel > 80)
		{
			pcInt[9] = 4;
		}
		if (pledgeLevel > 90)
		{
			pcInt[9] = 5;
		}
		if (pledgeLevel > 95)
		{
			pcInt[9] = 6;
		}
		if (pledgeLevel > 98)
		{
			pcInt[9] = 7;
		}
	}
	
	/**
	 * Sets the data received from the CustomNpcInstanceManager
	 * @param ci the customInfo data
	 */
	public void setPcInstanceData(NpcToPlayer ci)
	{
		if (ci == null)
		{
			return;
		}
		
		// load the "massive" data
		for (int i = 0; i < 25; i++)
		{
			pcInt[i] = ci.integerData[i];
		}
		for (int i = 0; i < 4; i++)
		{
			pcBoolean[i] = ci.booleanData[i];
		}
		
		// random variables to apply to this L2NpcInstance polymorph
		allowRandomClass = ci.booleanData[4];
		allowRandomAppearance = ci.booleanData[5];
		allowRandomWeapons = ci.booleanData[6];
		
		// name & title override
		name = ci.stringData[0];
		title = ci.stringData[1];
		if (name != null && name.equals(""))
		{
			name = null;
		}
		if (title != null && title.equals(""))
		{
			title = null;
		}
		
		// Not really necessary but maybe called upon on wrong random settings:
		// Initiate this PcInstance class id to the correct pcInstance class.
		ClassId ids[] = ClassId.values();
		if (ids != null)
		{
			for (ClassId id : ids)
			{
				if (id == null)
				{
					continue;
				}
				else if (id.getId() == pcInt[7])
				{
					classId = id;
					pcInt[6] = id.getRace().ordinal();
					break;
				}
			}
		}
	}
}
