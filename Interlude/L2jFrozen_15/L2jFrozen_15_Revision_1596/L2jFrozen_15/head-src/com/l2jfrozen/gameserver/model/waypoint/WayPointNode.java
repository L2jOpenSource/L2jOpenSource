package com.l2jfrozen.gameserver.model.waypoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.MyTargetSelected;
import com.l2jfrozen.util.Point3D;

/**
 * @author luisantonioa
 */
public class WayPointNode extends L2Object
{
	private int id;
	private String title, type;
	private static final String NORMAL = "Node", SELECTED = "Selected", LINKED = "Linked";
	private static int lineId = 5560;
	private static final String LINE_TYPE = "item";
	private final Map<WayPointNode, List<WayPointNode>> linkLists;
	
	public WayPointNode(final int objectId)
	{
		super(objectId);
		linkLists = Collections.synchronizedMap(new WeakHashMap<WayPointNode, List<WayPointNode>>());
	}
	
	@Override
	public boolean isAutoAttackable(final L2Character attacker)
	{
		return false;
	}
	
	public static WayPointNode spawn(final String type, final int id, final int x, final int y, final int z)
	{
		final WayPointNode newNode = new WayPointNode(IdFactory.getInstance().getNextId());
		newNode.getPoly().setPolyInfo(type, id + "");
		newNode.spawnMe(x, y, z);
		
		return newNode;
	}
	
	public static WayPointNode spawn(final boolean isItemId, final int id, final L2PcInstance player)
	{
		return spawn(isItemId ? "item" : "npc", id, player.getX(), player.getY(), player.getZ());
	}
	
	public static WayPointNode spawn(final boolean isItemId, final int id, final Point3D point)
	{
		return spawn(isItemId ? "item" : "npc", id, point.getX(), point.getY(), point.getZ());
	}
	
	public static WayPointNode spawn(final Point3D point)
	{
		return spawn(Config.NEW_NODE_TYPE, Config.NEW_NODE_ID, point.getX(), point.getY(), point.getZ());
	}
	
	public static WayPointNode spawn(final L2PcInstance player)
	{
		return spawn(Config.NEW_NODE_TYPE, Config.NEW_NODE_ID, player.getX(), player.getY(), player.getZ());
	}
	
	@Override
	public void onAction(final L2PcInstance player)
	{
		if (player.getTarget() != this)
		{
			player.setTarget(this);
			MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
			player.sendPacket(my);
			my = null;
		}
	}
	
	public void setNormalInfo(final String type, final int id, final String title)
	{
		this.type = type;
		changeID(id, title);
	}
	
	public void setNormalInfo(final String type, final int id)
	{
		this.type = type;
		changeID(id);
	}
	
	private void changeID(final int id)
	{
		this.id = id;
		toggleVisible();
		toggleVisible();
	}
	
	private void changeID(final int id, final String title)
	{
		setName(title);
		setTitle(title);
		changeID(id);
	}
	
	public void setLinked()
	{
		changeID(Config.LINKED_NODE_ID, LINKED);
	}
	
	public void setNormal()
	{
		changeID(Config.NEW_NODE_ID, NORMAL);
	}
	
	public void setSelected()
	{
		changeID(Config.SELECTED_NODE_ID, SELECTED);
	}
	
	@Override
	public boolean isMarker()
	{
		return true;
	}
	
	public final String getTitle()
	{
		return title;
	}
	
	public final void setTitle(final String title)
	{
		this.title = title;
	}
	
	public int getId()
	{
		return id;
	}
	
	public String getType()
	{
		return type;
	}
	
	public void setType(final String type)
	{
		this.type = type;
	}
	
	/**
	 * @param nodeA
	 * @param nodeB
	 */
	public static void drawLine(final WayPointNode nodeA, final WayPointNode nodeB)
	{
		int x1 = nodeA.getX(), y1 = nodeA.getY(), z1 = nodeA.getZ();
		final int x2 = nodeB.getX(), y2 = nodeB.getY(), z2 = nodeB.getZ();
		final int modX = x1 - x2 > 0 ? -1 : 1;
		final int modY = y1 - y2 > 0 ? -1 : 1;
		final int modZ = z1 - z2 > 0 ? -1 : 1;
		
		final int diffX = Math.abs(x1 - x2);
		final int diffY = Math.abs(y1 - y2);
		final int diffZ = Math.abs(z1 - z2);
		
		final int distance = (int) Math.sqrt(diffX * diffX + diffY * diffY + diffZ * diffZ);
		
		final int steps = distance / 40;
		
		List<WayPointNode> lineNodes = new ArrayList<>();
		
		for (int i = 0; i < steps; i++)
		{
			x1 = x1 + modX * diffX / steps;
			y1 = y1 + modY * diffY / steps;
			z1 = z1 + modZ * diffZ / steps;
			
			lineNodes.add(WayPointNode.spawn(LINE_TYPE, lineId, x1, y1, z1));
		}
		
		nodeA.addLineInfo(nodeB, lineNodes);
		nodeB.addLineInfo(nodeA, lineNodes);
		
		lineNodes = null;
	}
	
	public void addLineInfo(final WayPointNode node, final List<WayPointNode> line)
	{
		linkLists.put(node, line);
	}
	
	/**
	 * @param target
	 * @param selectedNode
	 */
	public static void eraseLine(final WayPointNode target, final WayPointNode selectedNode)
	{
		List<WayPointNode> lineNodes = target.getLineInfo(selectedNode);
		
		if (lineNodes == null)
		{
			return;
		}
		
		for (final WayPointNode node : lineNodes)
		{
			node.decayMe();
		}
		
		target.eraseLine(selectedNode);
		selectedNode.eraseLine(target);
		lineNodes = null;
	}
	
	/**
	 * @param target
	 */
	public void eraseLine(final WayPointNode target)
	{
		linkLists.remove(target);
	}
	
	/**
	 * @param  selectedNode
	 * @return
	 */
	private List<WayPointNode> getLineInfo(final WayPointNode selectedNode)
	{
		return linkLists.get(selectedNode);
	}
	
	public static void setLineId(final int line_id)
	{
		lineId = line_id;
	}
	
	public List<WayPointNode> getLineNodes()
	{
		List<WayPointNode> list = new ArrayList<>();
		
		for (List<WayPointNode> points : linkLists.values())
		{
			list.addAll(points);
		}
		
		return list;
	}
}
