package com.l2jfrozen.gameserver.datatables;

/**
 * @author FBIagent<br>
 */
public class AccessLevel
{
	private int accessLevel = 0;
	private String name = null;
	private int nameColor = 0;
	private int titleColor = 0;
	private boolean isGm = false;
	private boolean allowPeaceAttack = false;
	private boolean allowFixedRes = false;
	private boolean allowTransaction = false;
	private boolean allowAltG = false;
	private boolean giveDamage = false;
	private boolean takeAggro = false;
	private boolean gainExp = false;
	
	/**
	 * @param accessLevel      as int
	 * @param name             as String
	 * @param nameColor        as int
	 * @param titleColor       as int
	 * @param isGm             as boolean
	 * @param allowPeaceAttack as boolean
	 * @param allowFixedRes    as boolean
	 * @param allowTransaction as boolean
	 * @param allowAltG        as boolean
	 * @param giveDamage       as boolean
	 * @param takeAggro        as boolean
	 * @param gainExp          as boolean
	 */
	public AccessLevel(int accessLevel, String name, int nameColor, int titleColor, boolean isGm, boolean allowPeaceAttack, boolean allowFixedRes, boolean allowTransaction, boolean allowAltG, boolean giveDamage, boolean takeAggro, boolean gainExp)
	{
		this.accessLevel = accessLevel;
		this.name = name;
		this.nameColor = nameColor;
		this.titleColor = titleColor;
		this.isGm = isGm;
		this.allowPeaceAttack = allowPeaceAttack;
		this.allowFixedRes = allowFixedRes;
		this.allowTransaction = allowTransaction;
		this.allowAltG = allowAltG;
		this.giveDamage = giveDamage;
		this.takeAggro = takeAggro;
		this.gainExp = gainExp;
	}
	
	public int getLevel()
	{
		return accessLevel;
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getNameColor()
	{
		return nameColor;
	}
	
	public int getTitleColor()
	{
		return titleColor;
	}
	
	/**
	 * @return true if access level have gm access, otherwise false
	 */
	public boolean isGm()
	{
		return isGm;
	}
	
	/**
	 * Returns if the access level is allowed to attack in peace zone or not<br>
	 * <br>
	 * @return boolean: true if the access level is allowed to attack in peace zone, otherwise false<br>
	 */
	public boolean allowPeaceAttack()
	{
		return allowPeaceAttack;
	}
	
	/**
	 * @return true if the access level is allowed to use fixed res, otherwise false.
	 */
	public boolean allowFixedRes()
	{
		return allowFixedRes;
	}
	
	/**
	 * Returns if the access level is allowed to perform transactions or not<br>
	 * <br>
	 * @return boolean: true if access level is allowed to perform transactions, otherwise false<br>
	 */
	public boolean allowTransaction()
	{
		return allowTransaction;
	}
	
	/**
	 * Returns if the access level is allowed to use AltG commands or not<br>
	 * <br>
	 * @return boolean: true if access level is allowed to use AltG commands, otherwise false<br>
	 */
	public boolean allowAltG()
	{
		return allowAltG;
	}
	
	/**
	 * Returns if the access level can give damage or not<br>
	 * <br>
	 * @return boolean: true if the access level can give damage, otherwise false<br>
	 */
	public boolean canGiveDamage()
	{
		return giveDamage;
	}
	
	/**
	 * Returns if the access level can take aggro or not<br>
	 * <br>
	 * @return boolean: true if the access level can take aggro, otherwise false<br>
	 */
	public boolean canTakeAggro()
	{
		return takeAggro;
	}
	
	/**
	 * Returns if the access level can gain exp or not<br>
	 * <br>
	 * @return boolean: true if the access level can gain exp, otherwise false<br>
	 */
	public boolean canGainExp()
	{
		return gainExp;
	}
}
