/*
 * Copyright (C) 2004-2015 L2J Server
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
package l2r.gameserver.model.walk;

import java.util.List;

/**
 * @author GKR
 */
public class WalkRoute
{
	private final String _name;
	private final List<WalkNode> _nodeList; // List of nodes
	private final boolean _repeatWalk; // Does repeat walk, after arriving into last point in list, or not
	private boolean _stopAfterCycle; // Make only one cycle or endlessly
	private final RepeatType _repeatType; // Repeat style: 0 - go back, 1 - go to first point (circle style), 2 - teleport to first point (conveyor style), 3 - random walking between points
	
	public WalkRoute(String name, List<WalkNode> route, boolean repeat, boolean once, RepeatType repeatType)
	{
		_name = name;
		_nodeList = route;
		_repeatType = repeatType;
		_repeatWalk = ((_repeatType.getId() >= 0) && (_repeatType.getId() <= 2)) ? repeat : false;
	}
	
	public String getName()
	{
		return _name;
	}
	
	public List<WalkNode> getNodeList()
	{
		return _nodeList;
	}
	
	public WalkNode getLastNode()
	{
		return _nodeList.get(_nodeList.size() - 1);
	}
	
	public boolean repeatWalk()
	{
		return _repeatWalk;
	}
	
	public boolean doOnce()
	{
		return _stopAfterCycle;
	}
	
	public RepeatType getRepeatType()
	{
		return _repeatType;
	}
	
	public int getNodesCount()
	{
		return _nodeList.size();
	}
}
