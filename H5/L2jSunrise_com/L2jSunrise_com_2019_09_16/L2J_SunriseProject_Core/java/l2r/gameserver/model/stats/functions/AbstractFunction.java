package l2r.gameserver.model.stats.functions;

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
public abstract class AbstractFunction
{
	/** Statistics, that is affected by this function (See L2Character.CALCULATOR_XXX constants) */
	private final Stats _stat;
	/**
	 * Order of functions calculation.<br>
	 * Functions with lower order are executed first.<br>
	 * Functions with the same order are executed in unspecified order.<br>
	 * Usually add/subtract functions has lowest order,<br>
	 * then bonus/penalty functions (multiply/divide) are applied, then functions that do more complex<br>
	 * calculations (non-linear functions).
	 */
	private final int _order;
	/**
	 * Owner can be an armor, weapon, skill, system event, quest, etc.<br>
	 * Used to remove all functions added by this owner.
	 */
	private final Object _funcOwner;
	/** Function may be disabled by attached condition. */
	private final Condition _applayCond;
	
	/** The value. */
	private final double _value;
	
	/**
	 * Constructor of Func.
	 * @param stat the stat
	 * @param order the order
	 * @param owner the owner
	 * @param value the value
	 * @param applayCond the apply condition
	 */
	public AbstractFunction(Stats stat, int order, Object owner, double value, Condition applayCond)
	{
		_stat = stat;
		_order = order;
		_funcOwner = owner;
		_value = value;
		_applayCond = applayCond;
	}
	
	/**
	 * Gets the apply condition
	 * @return the apply condition
	 */
	public Condition getApplayCond()
	{
		return _applayCond;
	}
	
	/**
	 * Gets the fuction owner.
	 * @return the function owner
	 */
	public final Object getFuncOwner()
	{
		return _funcOwner;
	}
	
	/**
	 * Gets the function order.
	 * @return the order
	 */
	public final int getOrder()
	{
		return _order;
	}
	
	/**
	 * Gets the stat.
	 * @return the stat
	 */
	public final Stats getStat()
	{
		return _stat;
	}
	
	/**
	 * Gets the value.
	 * @return the value
	 */
	public final double getValue()
	{
		return _value;
	}
	
	/**
	 * Run the mathematics function of the Func.
	 * @param env
	 */
	public abstract void calc(Env env);
}
