package com.l2jfrozen.gameserver.model;

/**
 * This class ...
 * @author  NightMarez
 * @version $Revision: 1.2.2.1.2.1 $ $Date: 2005/03/27 15:29:32 $
 */
public class L2LvlupData
{
	private int classId;
	private int classLvl;
	private float classHpAdd;
	private float classHpBase;
	private float classHpModifier;
	private float classCpAdd;
	private float classCpBase;
	private float classCpModifier;
	private float classMpAdd;
	private float classMpBase;
	private float classMpModifier;
	
	@Deprecated
	public float getClassHpAdd()
	{
		return classHpAdd;
	}
	
	public void setClassHpAdd(final float hpAdd)
	{
		classHpAdd = hpAdd;
	}
	
	@Deprecated
	public float getClassHpBase()
	{
		return classHpBase;
	}
	
	public void setClassHpBase(final float hpBase)
	{
		classHpBase = hpBase;
	}
	
	@Deprecated
	public float getClassHpModifier()
	{
		return classHpModifier;
	}
	
	public void setClassHpModifier(final float hpModifier)
	{
		classHpModifier = hpModifier;
	}
	
	@Deprecated
	public float getClassCpAdd()
	{
		return classCpAdd;
	}
	
	public void setClassCpAdd(final float cpAdd)
	{
		classCpAdd = cpAdd;
	}
	
	@Deprecated
	public float getClassCpBase()
	{
		return classCpBase;
	}
	
	public void setClassCpBase(final float cpBase)
	{
		classCpBase = cpBase;
	}
	
	@Deprecated
	public float getClassCpModifier()
	{
		return classCpModifier;
	}
	
	public void setClassCpModifier(final float cpModifier)
	{
		classCpModifier = cpModifier;
	}
	
	public int getClassid()
	{
		return classId;
	}
	
	public void setClassid(final int pClassid)
	{
		classId = pClassid;
	}
	
	@Deprecated
	public int getClassLvl()
	{
		return classLvl;
	}
	
	public void setClassLvl(final int lvl)
	{
		classLvl = lvl;
	}
	
	@Deprecated
	public float getClassMpAdd()
	{
		return classMpAdd;
	}
	
	public void setClassMpAdd(final float mpAdd)
	{
		classMpAdd = mpAdd;
	}
	
	@Deprecated
	public float getClassMpBase()
	{
		return classMpBase;
	}
	
	public void setClassMpBase(final float mpBase)
	{
		classMpBase = mpBase;
	}
	
	@Deprecated
	public float getClassMpModifier()
	{
		return classMpModifier;
	}
	
	public void setClassMpModifier(final float mpModifier)
	{
		classMpModifier = mpModifier;
	}
}
