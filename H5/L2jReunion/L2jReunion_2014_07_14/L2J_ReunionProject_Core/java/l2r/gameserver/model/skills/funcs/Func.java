/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.skills.funcs;

import l2r.gameserver.model.conditions.Condition;
import l2r.gameserver.model.stats.Env;
import l2r.gameserver.model.stats.Stats;

/**
 * A Func object is a component of a Calculator created to manage and dynamically calculate the effect of a character property (ex : MAX_HP, REGENERATE_HP_RATE...).<br>
 * In fact, each calculator is a table of Func object in which each Func represents a mathematics function:<br>
 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<br>
 * When the calc method of a calculator is launched, each mathematics function is called according to its priority <B>_order</B>.<br>
 * Indeed, Func with lowest priority order is executed first and Funcs with the same order are executed in unspecified order.<br>
 * The result of the calculation is stored in the value property of an Env class instance.
 */
public abstract class Func
{
	/**
	 * Statistics, that is affected by this function (See L2Character.CALCULATOR_XXX constants)
	 */
	public final Stats stat;
	
	/**
	 * Order of functions calculation.<br>
	 * Functions with lower order are executed first.<br>
	 * Functions with the same order are executed in unspecified order.<br>
	 * Usually add/subtract functions has lowest order,<br>
	 * then bonus/penalty functions (multiply/divide) are applied, then functions that do more complex<br>
	 * calculations (non-linear functions).
	 */
	public final int order;
	
	/**
	 * Owner can be an armor, weapon, skill, system event, quest, etc.<br>
	 * Used to remove all functions added by this owner.
	 */
	public final Object funcOwner;
	
	/**
	 * Function may be disabled by attached condition.
	 */
	public Condition cond;
	
	public Lambda _lambda;
	
	/**
	 * Constructor of Func.
	 * @param pStat
	 * @param pOrder
	 * @param owner
	 * @param lambda
	 */
	public Func(Stats pStat, int pOrder, Object owner, Lambda lambda)
	{
		stat = pStat;
		order = pOrder;
		funcOwner = owner;
		_lambda = lambda;
	}
	
	/**
	 * Add a condition to the Func.
	 * @param pCond
	 */
	public void setCondition(Condition pCond)
	{
		cond = pCond;
	}
	
	/**
	 * Run the mathematics function of the Func.
	 * @param env
	 */
	public abstract void calc(Env env);
}
