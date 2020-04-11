package com.l2jfrozen.gameserver.util;

/**
 * Flood protector configuration
 * @author fordfrog
 */
public final class FloodProtectorConfig
{
	/**
	 * Type used for identification of logging output.
	 */
	public String FLOOD_PROTECTOR_TYPE;
	/**
	 * Flood protection interval in game ticks.
	 */
	public float FLOOD_PROTECTION_INTERVAL;
	/**
	 * Whether flooding should be logged.
	 */
	public boolean LOG_FLOODING;
	/**
	 * If specified punishment limit is exceeded, punishment is applied.
	 */
	public int PUNISHMENT_LIMIT;
	/**
	 * Punishment type. Either 'none', 'kick', 'ban' or 'jail'.
	 */
	public String PUNISHMENT_TYPE;
	/**
	 * For how long should the char/account be punished.
	 */
	public int PUNISHMENT_TIME;
	
	/**
	 * Alternative flood protection method: check if in given FLOOD_PROTECTION_INTERVAL more then PUNISHMENT_LIMIT actions are performed: if this condition has been verified apply PUNISHMENT_TYPE for PUNISHMENT_TIME minutes
	 */
	public boolean ALTERNATIVE_METHOD;
	
	/**
	 * Creates new instance of FloodProtectorConfig.
	 * @param floodProtectorType {@link #FLOOD_PROTECTOR_TYPE}
	 */
	public FloodProtectorConfig(final String floodProtectorType)
	{
		super();
		FLOOD_PROTECTOR_TYPE = floodProtectorType;
		ALTERNATIVE_METHOD = false;
	}
	
	/**
	 * Creates new instance of FloodProtectorConfig.
	 * @param floodProtectorType {@link #FLOOD_PROTECTOR_TYPE}
	 * @param alt_func
	 */
	public FloodProtectorConfig(final String floodProtectorType, final boolean alt_func)
	{
		super();
		FLOOD_PROTECTOR_TYPE = floodProtectorType;
		ALTERNATIVE_METHOD = alt_func;
	}
}
