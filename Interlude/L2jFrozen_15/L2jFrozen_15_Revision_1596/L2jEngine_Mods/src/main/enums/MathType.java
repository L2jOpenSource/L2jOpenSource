package main.enums;

/**
 * Class used to define mathematical actions within methods such as addition, subtraction, etc. etc.
 * @author fissban
 */
public enum MathType
{
	/** Set value */
	SET,
	/** Add a specific value */
	ADD,
	/** Sub a specific value */
	SUB,
	/** Increase value by one (Used in methods that have no input parameter) */
	INCREASE_BY_ONE,
	/** Decrease value by one (Used in methods that have no input parameter) */
	DECREASE_BY_ONE,
	/** Initialize value */
	INIT,
}
