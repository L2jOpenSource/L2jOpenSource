package com.l2jfrozen.gameserver.handler;

import java.io.IOException;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Skill.SkillType;

/**
 * an IItemHandler implementation has to be stateless
 * @version $Revision: 1.2.2.2.2.3 $ $Date: 2005/04/03 15:55:06 $
 */

public interface ISkillHandler
{
	/**
	 * this is the worker method that is called when using an item.
	 * @param  activeChar
	 * @param  skill
	 * @param  targets
	 * @throws IOException
	 */
	public void useSkill(L2Character activeChar, L2Skill skill, L2Object[] targets) throws IOException;
	
	/**
	 * this method is called at initialization to register all the item ids automatically
	 * @return all known itemIds
	 */
	public SkillType[] getSkillIds();
}
