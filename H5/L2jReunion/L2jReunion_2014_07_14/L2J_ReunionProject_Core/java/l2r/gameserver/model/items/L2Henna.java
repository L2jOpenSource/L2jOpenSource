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
package l2r.gameserver.model.items;

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.base.ClassId;

/**
 * Class for the Henna object.
 * @author Zoey76
 */
public class L2Henna
{
	private final int _dyeId;
	private final String _dyeName;
	private final int _dyeItemId;
	private final int _str;
	private final int _con;
	private final int _dex;
	private final int _int;
	private final int _men;
	private final int _wit;
	private final int _wear_fee;
	private final int _wear_count;
	private final int _cancel_fee;
	private final int _cancel_count;
	private final List<ClassId> _wear_class;
	
	public L2Henna(StatsSet set)
	{
		_dyeId = set.getInteger("dyeId");
		_dyeName = set.getString("dyeName");
		_dyeItemId = set.getInteger("dyeItemId");
		_str = set.getInteger("str");
		_con = set.getInteger("con");
		_dex = set.getInteger("dex");
		_int = set.getInteger("int");
		_men = set.getInteger("men");
		_wit = set.getInteger("wit");
		_wear_fee = set.getInteger("wear_fee");
		_wear_count = set.getInteger("wear_count");
		_cancel_fee = set.getInteger("cancel_fee");
		_cancel_count = set.getInteger("cancel_count");
		_wear_class = new ArrayList<>();
	}
	
	/**
	 * @return the dye Id.
	 */
	public int getDyeId()
	{
		return _dyeId;
	}
	
	/**
	 * @return the dye server-side name.
	 */
	public String getDyeName()
	{
		return _dyeName;
	}
	
	/**
	 * @return the item Id, required for this dye.
	 */
	public int getDyeItemId()
	{
		return _dyeItemId;
	}
	
	/**
	 * @return the STR stat.
	 */
	public int getStatSTR()
	{
		return _str;
	}
	
	/**
	 * @return the CON stat.
	 */
	public int getStatCON()
	{
		return _con;
	}
	
	/**
	 * @return the DEX stat.
	 */
	public int getStatDEX()
	{
		return _dex;
	}
	
	/**
	 * @return the INT stat.
	 */
	public int getStatINT()
	{
		return _int;
	}
	
	/**
	 * @return the MEN stat.
	 */
	public int getStatMEN()
	{
		return _men;
	}
	
	/**
	 * @return the WIT stat.
	 */
	public int getStatWIT()
	{
		return _wit;
	}
	
	/**
	 * @return the wear fee, cost for adding this dye to the player.
	 */
	public int getWearFee()
	{
		return _wear_fee;
	}
	
	/**
	 * @return the wear count, the required count to add this dye to the player.
	 */
	public int getWearCount()
	{
		return _wear_count;
	}
	
	/**
	 * @return the cancel fee, cost for removing this dye from the player.
	 */
	public int getCancelFee()
	{
		return _cancel_fee;
	}
	
	/**
	 * @return the cancel count, the retrieved amount of dye items after removing the dye.
	 */
	public int getCancelCount()
	{
		return _cancel_count;
	}
	
	/**
	 * @return the list with the allowed classes to wear this dye.
	 */
	public List<ClassId> getAllowedWearClass()
	{
		return _wear_class;
	}
	
	/**
	 * @param c the class trying to wear this dye.
	 * @return {@code true} if the player is allowed to wear this dye, {@code false} otherwise.
	 */
	public boolean isAllowedClass(ClassId c)
	{
		return _wear_class.contains(c);
	}
	
	/**
	 * @param wearClassIds the list of classes that can wear this dye.
	 */
	public void setWearClassIds(List<ClassId> wearClassIds)
	{
		_wear_class.addAll(wearClassIds);
	}
}