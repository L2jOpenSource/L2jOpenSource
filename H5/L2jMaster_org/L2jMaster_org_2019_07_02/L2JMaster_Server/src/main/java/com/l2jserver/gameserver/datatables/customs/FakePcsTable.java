/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.datatables.customs;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.l2jserver.commons.database.pool.impl.ConnectionFactory;
import com.l2jserver.gameserver.model.actor.FakePc;

public class FakePcsTable
{
	private static Logger _log = Logger.getLogger(FakePcsTable.class.getName());
	
	private final HashMap<Integer, FakePc> _fakePcs = new HashMap<>();
	
	private static final String SQL_LOAD_FAKE_PCS = "SELECT * FROM fake_pcs";
	
	protected FakePcsTable()
	{
		loadData();
	}
	
	private void loadData()
	{
		_fakePcs.clear();
		
		try (Connection con = ConnectionFactory.getInstance().getConnection();
			Statement st = con.createStatement();
			ResultSet rset = st.executeQuery(SQL_LOAD_FAKE_PCS))
		{
			FakePc fpc = null;
			
			while (rset.next())
			{
				fpc = new FakePc();
				
				int npcId = rset.getInt("npc_id");
				fpc.race = rset.getInt("race");
				fpc.sex = rset.getInt("sex");
				fpc.clazz = rset.getInt("class");
				fpc.title = rset.getString("title");
				fpc.titleColor = Integer.decode("0x" + rset.getString("title_color"));
				fpc.name = rset.getString("name");
				fpc.nameColor = Integer.decode("0x" + rset.getString("name_color"));
				fpc.hairStyle = rset.getInt("hair_style");
				fpc.hairColor = rset.getInt("hair_color");
				fpc.face = rset.getInt("face");
				fpc.mount = rset.getByte("mount");
				fpc.team = rset.getByte("team");
				fpc.hero = rset.getByte("hero");
				fpc.pdUnder = rset.getInt("pd_under");
				fpc.pdUnderAug = rset.getInt("pd_under_aug");
				fpc.pdHead = rset.getInt("pd_head");
				fpc.pdHeadAug = rset.getInt("pd_head_aug");
				fpc.pdRHand = rset.getInt("pd_rhand");
				fpc.pdRHandAug = rset.getInt("pd_rhand_aug");
				fpc.pdLHand = rset.getInt("pd_lhand");
				fpc.pdLHandAug = rset.getInt("pd_lhand_aug");
				fpc.pdGloves = rset.getInt("pd_gloves");
				fpc.pdGlovesAug = rset.getInt("pd_gloves_aug");
				fpc.pdChest = rset.getInt("pd_chest");
				fpc.pdChestAug = rset.getInt("pd_chest_aug");
				fpc.pdLegs = rset.getInt("pd_legs");
				fpc.pdLegsAug = rset.getInt("pd_legs_aug");
				fpc.pdFeet = rset.getInt("pd_feet");
				fpc.pdFeetAug = rset.getInt("pd_feet_aug");
				fpc.pdBack = rset.getInt("pd_back");
				fpc.pdBackAug = rset.getInt("pd_back_aug");
				fpc.pdLRHand = rset.getInt("pd_lrhand");
				fpc.pdLRHandAug = rset.getInt("pd_lrhand_aug");
				fpc.pdHair = rset.getInt("pd_hair");
				fpc.pdHairAug = rset.getInt("pd_hair_aug");
				fpc.pdHair2 = rset.getInt("pd_hair2");
				fpc.pdHair2Aug = rset.getInt("pd_hair2_aug");
				fpc.pdRBracelet = rset.getInt("pd_rbracelet");
				fpc.pdRBraceletAug = rset.getInt("pd_rbracelet_aug");
				fpc.pdLBracelet = rset.getInt("pd_lbracelet");
				fpc.pdLBraceletAug = rset.getInt("pd_lbracelet_aug");
				fpc.pdDeco1 = rset.getInt("pd_deco1");
				fpc.pdDeco1Aug = rset.getInt("pd_deco1_aug");
				fpc.pdDeco2 = rset.getInt("pd_deco2");
				fpc.pdDeco2Aug = rset.getInt("pd_deco2_aug");
				fpc.pdDeco3 = rset.getInt("pd_deco3");
				fpc.pdDeco3Aug = rset.getInt("pd_deco3_aug");
				fpc.pdDeco4 = rset.getInt("pd_deco4");
				fpc.pdDeco4Aug = rset.getInt("pd_deco4_aug");
				fpc.pdDeco5 = rset.getInt("pd_deco5");
				fpc.pdDeco5Aug = rset.getInt("pd_deco5_aug");
				fpc.pdDeco6 = rset.getInt("pd_deco6");
				fpc.pdDeco6Aug = rset.getInt("pd_deco6_aug");
				fpc.enchantEffect = rset.getInt("enchant_effect");
				fpc.pvpFlag = rset.getInt("pvp_flag");
				fpc.karma = rset.getInt("karma");
				fpc.fishing = rset.getByte("fishing");
				fpc.fishingX = rset.getInt("fishing_x");
				fpc.fishingY = rset.getInt("fishing_y");
				fpc.fishingZ = rset.getInt("fishing_z");
				fpc.invisible = rset.getByte("invisible");
				_fakePcs.put(npcId, fpc);
			}
			
			rset.close();
		}
		catch (Exception e)
		{
			_log.log(Level.WARNING, "FakePcsTable: Error while creating fake pc table: ", e);
		}
	}
	
	public void reloadData()
	{
		loadData();
	}
	
	public FakePc getFakePc(int npcId)
	{
		return _fakePcs.get(npcId);
	}
	
	public static FakePcsTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final FakePcsTable _instance = new FakePcsTable();
	}
}