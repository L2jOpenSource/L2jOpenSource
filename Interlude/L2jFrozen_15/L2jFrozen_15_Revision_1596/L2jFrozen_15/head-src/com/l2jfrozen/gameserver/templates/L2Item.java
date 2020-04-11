package com.l2jfrozen.gameserver.templates;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.skills.Env;
import com.l2jfrozen.gameserver.skills.effects.EffectTemplate;
import com.l2jfrozen.gameserver.skills.funcs.Func;
import com.l2jfrozen.gameserver.skills.funcs.FuncTemplate;

/**
 * This class contains all informations concerning the item (weapon, armor, etc).<BR>
 * Mother class of :
 * <LI>L2Armor</LI>
 * <LI>L2EtcItem</LI>
 * <LI>L2Weapon</LI>
 * @version $Revision: 1.7.2.2.2.5 $ $Date: 2005/04/06 18:25:18 $
 */
public abstract class L2Item
{
	public static final int TYPE1_WEAPON_RING_EARRING_NECKLACE = 0;
	public static final int TYPE1_SHIELD_ARMOR = 1;
	public static final int TYPE1_ITEM_QUESTITEM_ADENA = 4;
	
	public static final int TYPE2_WEAPON = 0;
	public static final int TYPE2_SHIELD_ARMOR = 1;
	public static final int TYPE2_ACCESSORY = 2;
	public static final int TYPE2_QUEST = 3;
	public static final int TYPE2_MONEY = 4;
	public static final int TYPE2_OTHER = 5;
	public static final int TYPE2_PET_WOLF = 6;
	public static final int TYPE2_PET_HATCHLING = 7;
	public static final int TYPE2_PET_STRIDER = 8;
	public static final int TYPE2_PET_BABY = 9;
	
	public static final int SLOT_NONE = 0x0000;
	public static final int SLOT_UNDERWEAR = 0x0001;
	public static final int SLOT_R_EAR = 0x0002;
	public static final int SLOT_L_EAR = 0x0004;
	public static final int SLOT_NECK = 0x0008;
	public static final int SLOT_R_FINGER = 0x0010;
	public static final int SLOT_L_FINGER = 0x0020;
	public static final int SLOT_HEAD = 0x0040;
	public static final int SLOT_R_HAND = 0x0080;
	public static final int SLOT_L_HAND = 0x0100;
	public static final int SLOT_GLOVES = 0x0200;
	public static final int SLOT_CHEST = 0x0400;
	public static final int SLOT_LEGS = 0x0800;
	public static final int SLOT_FEET = 0x1000;
	public static final int SLOT_BACK = 0x2000;
	public static final int SLOT_LR_HAND = 0x4000;
	public static final int SLOT_FULL_ARMOR = 0x8000;
	public static final int SLOT_HAIR = 0x010000;
	public static final int SLOT_WOLF = 0x020000;
	public static final int SLOT_HATCHLING = 0x100000;
	public static final int SLOT_STRIDER = 0x200000;
	public static final int SLOT_BABYPET = 0x400000;
	public static final int SLOT_FACE = 0x040000;
	public static final int SLOT_DHAIR = 0x080000;
	
	public static final int CRYSTAL_NONE = 0x00; // ??
	public static final int CRYSTAL_D = 0x01; // ??
	public static final int CRYSTAL_C = 0x02; // ??
	public static final int CRYSTAL_B = 0x03; // ??
	public static final int CRYSTAL_A = 0x04; // ??
	public static final int CRYSTAL_S = 0x05; // ??
	
	private static final int[] crystalItemId =
	{
		0,
		1458,
		1459,
		1460,
		1461,
		1462
	};
	private static final int[] crystalEnchantBonusArmor =
	{
		0,
		11,
		6,
		11,
		19,
		25
	};
	private static final int[] crystalEnchantBonusWeapon =
	{
		0,
		90,
		45,
		67,
		144,
		250
	};
	
	private final int itemId;
	private final String name;
	private final int type1; // needed for item list (inventory)
	private final int type2; // different lists for armor, weapon, etc
	private final int weight;
	private final boolean crystallizable;
	private final boolean stackable;
	private final int crystalType; // default to none-grade
	private final int duration;
	private final int bodyPart;
	private final int referencePrice;
	private final int crystalCount;
	private final boolean sellable;
	private final boolean dropable;
	private final boolean destroyable;
	private final boolean tradeable;
	
	protected final Enum<?> type;
	
	protected FuncTemplate[] funcTemplates;
	protected EffectTemplate[] effectTemplates;
	protected L2Skill[] skills;
	
	private static final Func[] emptyFunctionSet = new Func[0];
	protected static final L2Effect[] emptyEffectSet = new L2Effect[0];
	
	/**
	 * Constructor of the L2Item that fill class variables.<BR>
	 * <BR>
	 * <U><I>Variables filled :</I></U><BR>
	 * <LI>type</LI>
	 * <LI>itemId</LI>
	 * <LI>name</LI>
	 * <LI>type1 & type2</LI>
	 * <LI>weight</LI>
	 * <LI>crystallizable</LI>
	 * <LI>stackable</LI>
	 * <LI>crystalType & crystlaCount</LI>
	 * <LI>duration</LI>
	 * <LI>bodypart</LI>
	 * <LI>referencePrice</LI>
	 * <LI>sellable</LI>
	 * @param type : Enum designating the type of the item
	 * @param set  : StatsSet corresponding to a set of couples (key,value) for description of the item
	 */
	protected L2Item(final Enum<?> type, final StatsSet set)
	{
		this.type = type;
		itemId = set.getInteger("item_id");
		name = set.getString("name");
		type1 = set.getInteger("type1"); // needed for item list (inventory)
		type2 = set.getInteger("type2"); // different lists for armor, weapon, etc
		weight = set.getInteger("weight");
		crystallizable = set.getBool("crystallizable");
		stackable = set.getBool("stackable", false);
		crystalType = set.getInteger("crystal_type", CRYSTAL_NONE); // default to none-grade
		duration = set.getInteger("duration");
		bodyPart = set.getInteger("bodypart");
		referencePrice = set.getInteger("price");
		crystalCount = set.getInteger("crystal_count", 0);
		sellable = set.getBool("sellable", true);
		dropable = set.getBool("dropable", true);
		destroyable = set.getBool("destroyable", true);
		tradeable = set.getBool("tradeable", true);
	}
	
	/**
	 * Returns the itemType.
	 * @return Enum
	 */
	public Enum<?> getItemType()
	{
		return type;
	}
	
	/**
	 * Returns the duration of the item
	 * @return int
	 */
	public final int getDuration()
	{
		return duration;
	}
	
	/**
	 * Returns the ID of the iden
	 * @return int
	 */
	public final int getItemId()
	{
		return itemId;
	}
	
	public abstract int getItemMask();
	
	/**
	 * Returns the type 2 of the item
	 * @return int
	 */
	public final int getType2()
	{
		return type2;
	}
	
	/**
	 * Returns the weight of the item
	 * @return int
	 */
	public final int getWeight()
	{
		return weight;
	}
	
	/**
	 * Returns if the item is crystallizable
	 * @return boolean
	 */
	public final boolean isCrystallizable()
	{
		return crystallizable;
	}
	
	/**
	 * Return the type of crystal if item is crystallizable
	 * @return int
	 */
	public final int getCrystalType()
	{
		return crystalType;
	}
	
	/**
	 * Return the type of crystal if item is crystallizable
	 * @return int
	 */
	public final int getCrystalItemId()
	{
		return crystalItemId[crystalType];
	}
	
	/**
	 * Returns the grade of the item.<BR>
	 * <BR>
	 * <U><I>Concept :</I></U><BR>
	 * In fact, this fucntion returns the type of crystal of the item.
	 * @return int
	 */
	public final int getItemGrade()
	{
		return getCrystalType();
	}
	
	/**
	 * Returns the quantity of crystals for crystallization
	 * @return int
	 */
	public final int getCrystalCount()
	{
		return crystalCount;
	}
	
	/**
	 * Returns the quantity of crystals for crystallization on specific enchant level
	 * @param  enchantLevel
	 * @return              int
	 */
	public final int getCrystalCount(final int enchantLevel)
	{
		if (enchantLevel > 3)
		{
			switch (type2)
			{
				case TYPE2_SHIELD_ARMOR:
				case TYPE2_ACCESSORY:
					return crystalCount + crystalEnchantBonusArmor[getCrystalType()] * (3 * enchantLevel - 6);
				case TYPE2_WEAPON:
					return crystalCount + crystalEnchantBonusWeapon[getCrystalType()] * (2 * enchantLevel - 3);
				default:
					return crystalCount;
			}
		}
		else if (enchantLevel > 0)
		{
			switch (type2)
			{
				case TYPE2_SHIELD_ARMOR:
				case TYPE2_ACCESSORY:
					return crystalCount + crystalEnchantBonusArmor[getCrystalType()] * enchantLevel;
				case TYPE2_WEAPON:
					return crystalCount + crystalEnchantBonusWeapon[getCrystalType()] * enchantLevel;
				default:
					return crystalCount;
			}
		}
		else
		{
			return crystalCount;
		}
	}
	
	/**
	 * Returns the name of the item
	 * @return String
	 */
	public final String getName()
	{
		return name;
	}
	
	/**
	 * Return the part of the body used with the item.
	 * @return int
	 */
	public final int getBodyPart()
	{
		return bodyPart;
	}
	
	/**
	 * Returns the type 1 of the item
	 * @return int
	 */
	public final int getType1()
	{
		return type1;
	}
	
	/**
	 * Returns if the item is stackable
	 * @return boolean
	 */
	public final boolean isStackable()
	{
		return stackable;
	}
	
	/**
	 * Returns if the item is consumable
	 * @return boolean
	 */
	public boolean isConsumable()
	{
		return false;
	}
	
	/**
	 * Returns the price of reference of the item
	 * @return int
	 */
	public final int getReferencePrice()
	{
		return isConsumable() ? (int) (referencePrice * Config.RATE_CONSUMABLE_COST) : referencePrice;
	}
	
	/**
	 * Returns if the item can be sold
	 * @return boolean
	 */
	public final boolean isSellable()
	{
		return sellable;
	}
	
	/**
	 * Returns if the item can dropped
	 * @return boolean
	 */
	public final boolean isDropable()
	{
		return dropable;
	}
	
	/**
	 * Returns if the item can destroy
	 * @return boolean
	 */
	public final boolean isDestroyable()
	{
		return destroyable;
	}
	
	/**
	 * Returns if the item can add to trade
	 * @return boolean
	 */
	public final boolean isTradeable()
	{
		return tradeable;
	}
	
	public boolean isPotion()
	{
		return (getItemType() == L2EtcItemType.POTION);
	}
	
	/**
	 * Returns if item is for hatchling
	 * @return boolean
	 */
	public boolean isForHatchling()
	{
		return type2 == TYPE2_PET_HATCHLING;
	}
	
	/**
	 * Returns if item is for strider
	 * @return boolean
	 */
	public boolean isForStrider()
	{
		return type2 == TYPE2_PET_STRIDER;
	}
	
	/**
	 * Returns if item is for wolf
	 * @return boolean
	 */
	public boolean isForWolf()
	{
		return type2 == TYPE2_PET_WOLF;
	}
	
	/**
	 * Returns if item is for wolf
	 * @return boolean
	 */
	public boolean isForBabyPet()
	{
		return type2 == TYPE2_PET_BABY;
	}
	
	/**
	 * Returns array of Func objects containing the list of functions used by the item
	 * @param  instance : L2ItemInstance pointing out the item
	 * @param  player   : L2Character pointing out the player
	 * @return          Func[] : array of functions
	 */
	public Func[] getStatFuncs(final L2ItemInstance instance, final L2Character player)
	{
		if (funcTemplates == null)
		{
			return emptyFunctionSet;
		}
		final List<Func> funcs = new ArrayList<>();
		for (final FuncTemplate t : funcTemplates)
		{
			final Env env = new Env();
			env.player = player;
			env.target = player;
			env.item = instance;
			final Func f = t.getFunc(env, this); // skill is owner
			if (f != null)
			{
				funcs.add(f);
			}
		}
		if (funcs.size() == 0)
		{
			return emptyFunctionSet;
		}
		return funcs.toArray(new Func[funcs.size()]);
	}
	
	/**
	 * Returns the effects associated with the item.
	 * @param  instance : L2ItemInstance pointing out the item
	 * @param  player   : L2Character pointing out the player
	 * @return          L2Effect[] : array of effects generated by the item
	 */
	public L2Effect[] getEffects(final L2ItemInstance instance, final L2Character player)
	{
		if (effectTemplates == null)
		{
			return emptyEffectSet;
		}
		final List<L2Effect> effects = new ArrayList<>();
		for (final EffectTemplate et : effectTemplates)
		{
			final Env env = new Env();
			env.player = player;
			env.target = player;
			env.item = instance;
			final L2Effect e = et.getEffect(env);
			if (e != null)
			{
				effects.add(e);
			}
		}
		if (effects.size() == 0)
		{
			return emptyEffectSet;
		}
		return effects.toArray(new L2Effect[effects.size()]);
	}
	
	/**
	 * Returns effects of skills associated with the item.
	 * @param  caster : L2Character pointing out the caster
	 * @param  target : L2Character pointing out the target
	 * @return        L2Effect[] : array of effects generated by the skill
	 */
	public L2Effect[] getSkillEffects(final L2Character caster, final L2Character target)
	{
		if (skills == null)
		{
			return emptyEffectSet;
		}
		final List<L2Effect> effects = new ArrayList<>();
		
		for (final L2Skill skill : skills)
		{
			if (!skill.checkCondition(caster, target, true))
			{
				continue; // Skill condition not met
			}
			
			if (target.getFirstEffect(skill.getId()) != null)
			{
				target.removeEffect(target.getFirstEffect(skill.getId()));
			}
			for (final L2Effect e : skill.getEffects(caster, target, false, false, false))
			{
				effects.add(e);
			}
		}
		if (effects.size() == 0)
		{
			return emptyEffectSet;
		}
		return effects.toArray(new L2Effect[effects.size()]);
	}
	
	/**
	 * Add the FuncTemplate f to the list of functions used with the item
	 * @param f : FuncTemplate to add
	 */
	public void attach(final FuncTemplate f)
	{
		// If functTemplates is empty, create it and add the FuncTemplate f in it
		if (funcTemplates == null)
		{
			funcTemplates = new FuncTemplate[]
			{
				f
			};
		}
		else
		{
			final int len = funcTemplates.length;
			final FuncTemplate[] tmp = new FuncTemplate[len + 1];
			// Definition : arraycopy(array source, begins copy at this position of source, array destination, begins copy at this position in dest,
			// number of components to be copied)
			System.arraycopy(funcTemplates, 0, tmp, 0, len);
			tmp[len] = f;
			funcTemplates = tmp;
		}
	}
	
	/**
	 * Add the EffectTemplate effect to the list of effects generated by the item
	 * @param effect : EffectTemplate
	 */
	public void attach(final EffectTemplate effect)
	{
		if (effectTemplates == null)
		{
			effectTemplates = new EffectTemplate[]
			{
				effect
			};
		}
		else
		{
			final int len = effectTemplates.length;
			final EffectTemplate[] tmp = new EffectTemplate[len + 1];
			// Definition : arraycopy(array source, begins copy at this position of source, array destination, begins copy at this position in dest,
			// number of components to be copied)
			System.arraycopy(effectTemplates, 0, tmp, 0, len);
			tmp[len] = effect;
			effectTemplates = tmp;
		}
	}
	
	/**
	 * Add the L2Skill skill to the list of skills generated by the item
	 * @param skill : L2Skill
	 */
	public void attach(final L2Skill skill)
	{
		if (skills == null)
		{
			skills = new L2Skill[]
			{
				skill
			};
		}
		else
		{
			final int len = skills.length;
			final L2Skill[] tmp = new L2Skill[len + 1];
			// Definition : arraycopy(array source, begins copy at this position of source, array destination, begins copy at this position in dest,
			// number of components to be copied)
			System.arraycopy(skills, 0, tmp, 0, len);
			tmp[len] = skill;
			skills = tmp;
		}
	}
	
	/**
	 * Returns the name of the item
	 * @return String
	 */
	@Override
	public String toString()
	{
		return name + "(" + getItemId() + ")";
	}
}
