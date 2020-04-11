package net.sf.l2j.gameserver.model.actor.ai;

import net.sf.l2j.gameserver.enums.IntentionType;

/**
 * A datatype used as a simple "wish" of an actor, consisting of an {@link IntentionType} and to up to 3 {@link Object}s of any type.
 */
public class Intention
{
	private IntentionType _intention;
	
	private Object _firstParameter;
	private Object _secondParameter;
	
	public Intention()
	{
		_intention = IntentionType.IDLE;
	}
	
	public Intention(IntentionType intention, Object firstParameter, Object secondParameter)
	{
		_intention = intention;
		
		_firstParameter = firstParameter;
		_secondParameter = secondParameter;
	}
	
	@Override
	public String toString()
	{
		return "[Intention type=" + _intention.toString() + " param1=" + _firstParameter + " param2=" + _secondParameter + "]";
	}
	
	public IntentionType getType()
	{
		return _intention;
	}
	
	public Object getFirstParameter()
	{
		return _firstParameter;
	}
	
	public Object getSecondParameter()
	{
		return _secondParameter;
	}
	
	/**
	 * Update the current {@link Intention} parameters.
	 * @param intention : The new IntentionType to set.
	 * @param firstParameter : The first Object to set.
	 * @param secondParameter : The second Object to set.
	 */
	public synchronized void update(IntentionType intention, Object firstParameter, Object secondParameter)
	{
		_intention = intention;
		
		_firstParameter = firstParameter;
		_secondParameter = secondParameter;
	}
	
	/**
	 * Update the current {@link Intention} with parameters taken from another Intention.
	 * @param intention : The Intention to use as parameters.
	 */
	public synchronized void update(Intention intention)
	{
		_intention = intention.getType();
		
		_firstParameter = intention.getFirstParameter();
		_secondParameter = intention.getSecondParameter();
	}
	
	/**
	 * Reset the current {@link Intention} parameters.
	 */
	public synchronized void reset()
	{
		_intention = IntentionType.IDLE;
		
		_firstParameter = null;
		_secondParameter = null;
	}
	
	/**
	 * @return true if the current {@link Intention} got blank parameters.
	 */
	public boolean isBlank()
	{
		return _intention == IntentionType.IDLE && _firstParameter == null && _secondParameter == null;
	}
	
	/**
	 * @param intention : The intention to test.
	 * @param firstParameter : The first Object to test.
	 * @param secondParameter : The second Object to test.
	 * @return true if all tested parameters are equal (intention and all parameters).
	 */
	public boolean equals(IntentionType intention, Object firstParameter, Object secondParameter)
	{
		return _intention == intention && _firstParameter == firstParameter && _secondParameter == secondParameter;
	}
}