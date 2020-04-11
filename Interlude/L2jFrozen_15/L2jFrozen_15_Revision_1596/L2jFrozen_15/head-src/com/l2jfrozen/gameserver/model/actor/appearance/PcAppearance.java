package com.l2jfrozen.gameserver.model.actor.appearance;

public class PcAppearance
{
	private byte face;
	private byte hairColor;
	private byte hairStyle;
	private boolean sex; // Female true(1)
	/** true if the player is invisible */
	private boolean invisible = false;
	/** The hexadecimal Color of players name (white is 0xFFFFFF) */
	private int nameColor = 0xFFFFFF;
	/** The hexadecimal Color of players name (white is 0xFFFFFF) */
	private int titleColor = 0xFFFF77;
	
	public PcAppearance(final byte Face, final byte HColor, final byte HStyle, final boolean Sex)
	{
		face = Face;
		hairColor = HColor;
		hairStyle = HStyle;
		sex = Sex;
	}
	
	public final byte getFace()
	{
		return face;
	}
	
	/**
	 * @param value
	 */
	public final void setFace(final int value)
	{
		face = (byte) value;
	}
	
	public final byte getHairColor()
	{
		return hairColor;
	}
	
	/**
	 * @param value
	 */
	public final void setHairColor(final int value)
	{
		hairColor = (byte) value;
	}
	
	public final byte getHairStyle()
	{
		return hairStyle;
	}
	
	/**
	 * @param value
	 */
	public final void setHairStyle(final int value)
	{
		hairStyle = (byte) value;
	}
	
	/**
	 * @return true = female <br>
	 *         false = male
	 */
	public boolean getSex()
	{
		return sex;
	}
	
	/**
	 * @param isfemale true means character is female <br>
	 *                     false means male
	 */
	public void setSex(boolean isfemale)
	{
		sex = isfemale;
	}
	
	public void setInvisible()
	{
		invisible = true;
	}
	
	public void setVisible()
	{
		invisible = false;
	}
	
	public boolean isInvisible()
	{
		return invisible;
	}
	
	public int getNameColor()
	{
		return nameColor;
	}
	
	public void setNameColor(final int nameColor)
	{
		this.nameColor = nameColor;
	}
	
	public void setNameColor(final int red, final int green, final int blue)
	{
		nameColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
	}
	
	public int getTitleColor()
	{
		return titleColor;
	}
	
	public void setTitleColor(final int titleColor)
	{
		this.titleColor = titleColor;
	}
	
	public void setTitleColor(final int red, final int green, final int blue)
	{
		titleColor = (red & 0xFF) + ((green & 0xFF) << 8) + ((blue & 0xFF) << 16);
	}
}
