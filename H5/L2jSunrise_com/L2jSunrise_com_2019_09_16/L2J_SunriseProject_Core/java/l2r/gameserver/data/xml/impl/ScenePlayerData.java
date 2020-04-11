/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package l2r.gameserver.data.xml.impl;

import java.util.HashMap;
import java.util.Map;

import l2r.util.data.xml.IXmlReader.IXmlReader;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class ScenePlayerData implements IXmlReader
{
	private final Map<Integer, Integer> _sceneData = new HashMap<>();
	
	protected ScenePlayerData()
	{
		load();
	}
	
	@Override
	public synchronized void load()
	{
		_sceneData.clear();
		parseDatapackFile("data/xml/other/scenePlayerData.xml");
		LOGGER.info(getClass().getSimpleName() + ": Loaded " + _sceneData.size() + " scenes.");
	}
	
	@Override
	public void parseDocument(Document doc)
	{
		for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("scene".equalsIgnoreCase(d.getNodeName()))
					{
						int id = Integer.parseInt(d.getAttributes().getNamedItem("id").getNodeValue());
						int time = Integer.parseInt(d.getAttributes().getNamedItem("time").getNodeValue());
						
						_sceneData.put(id, time);
					}
				}
			}
		}
	}
	
	public Map<Integer, Integer> getSceneTable()
	{
		return _sceneData;
	}
	
	public int getVideoDuration(int vidId)
	{
		return _sceneData.get(vidId);
	}
	
	public static ScenePlayerData getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final ScenePlayerData _instance = new ScenePlayerData();
	}
}
