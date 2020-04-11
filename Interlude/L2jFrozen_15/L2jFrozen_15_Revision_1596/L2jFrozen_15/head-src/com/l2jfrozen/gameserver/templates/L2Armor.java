package com.l2jfrozen.gameserver.templates;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.skills.Env;
import com.l2jfrozen.gameserver.skills.funcs.Func;
import com.l2jfrozen.gameserver.skills.funcs.FuncTemplate;

/**
 * This class is dedicated to the management of armors.
 * @version $Revision: 1.2.2.1.2.6 $ $Date: 2005/03/27 15:30:10 $
 */
public final class L2Armor extends L2Item
{
	private final int avoidModifier;
	private final int pDef;
	private final int mDef;
	private final int mpBonus;
	private final int hpBonus;
	private L2Skill itemSkill = null; // for passive skill
	
	/**
	 * Constructor for Armor.<BR>
	 * <BR>
	 * <U><I>Variables filled :</I></U><BR>
	 * <LI>avoidModifier</LI>
	 * <LI>pDef & mDef</LI>
	 * <LI>mpBonus & hpBonus</LI>
	 * @param type : L2ArmorType designating the type of armor
	 * @param set  : StatsSet designating the set of couples (key,value) caracterizing the armor
	 * @see        L2Item constructor
	 */
	public L2Armor(final L2ArmorType type, final StatsSet set)
	{
		super(type, set);
		avoidModifier = set.getInteger("avoid_modify");
		pDef = set.getInteger("p_def");
		mDef = set.getInteger("m_def");
		mpBonus = set.getInteger("mp_bonus", 0);
		hpBonus = set.getInteger("hp_bonus", 0);
		
		final int sId = set.getInteger("item_skill_id");
		final int sLv = set.getInteger("item_skill_lvl");
		if (sId > 0 && sLv > 0)
		{
			itemSkill = SkillTable.getInstance().getInfo(sId, sLv);
		}
	}
	
	/**
	 * Returns the type of the armor.
	 * @return L2ArmorType
	 */
	@Override
	public L2ArmorType getItemType()
	{
		return (L2ArmorType) super.type;
	}
	
	/**
	 * Returns the ID of the item after applying the mask.
	 * @return int : ID of the item
	 */
	@Override
	public final int getItemMask()
	{
		return getItemType().mask();
	}
	
	/**
	 * Returns the magical defense of the armor
	 * @return int : value of the magic defense
	 */
	public final int getMDef()
	{
		return mDef;
	}
	
	/**
	 * Returns the physical defense of the armor
	 * @return int : value of the physical defense
	 */
	public final int getPDef()
	{
		return pDef;
	}
	
	/**
	 * Returns avoid modifier given by the armor
	 * @return int : avoid modifier
	 */
	public final int getAvoidModifier()
	{
		return avoidModifier;
	}
	
	/**
	 * Returns magical bonus given by the armor
	 * @return int : value of the magical bonus
	 */
	public final int getMpBonus()
	{
		return mpBonus;
	}
	
	/**
	 * Returns physical bonus given by the armor
	 * @return int : value of the physical bonus
	 */
	public final int getHpBonus()
	{
		return hpBonus;
	}
	
	/**
	 * Returns passive skill linked to that armor
	 * @return
	 */
	public L2Skill getSkill()
	{
		return itemSkill;
	}
	
	/**
	 * Returns array of Func objects containing the list of functions used by the armor
	 * @param  instance : L2ItemInstance pointing out the armor
	 * @param  player   : L2Character pointing out the player
	 * @return          Func[] : array of functions
	 */
	@Override
	public Func[] getStatFuncs(final L2ItemInstance instance, final L2Character player)
	{
		final List<Func> funcs = new ArrayList<>();
		if (funcTemplates != null)
		{
			for (final FuncTemplate t : funcTemplates)
			{
				final Env env = new Env();
				env.player = player;
				env.item = instance;
				final Func f = t.getFunc(env, instance);
				if (f != null)
				{
					funcs.add(f);
				}
			}
		}
		return funcs.toArray(new Func[funcs.size()]);
	}
}
