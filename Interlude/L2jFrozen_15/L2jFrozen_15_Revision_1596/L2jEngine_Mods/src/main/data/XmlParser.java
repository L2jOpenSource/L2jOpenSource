package main.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.l2jfrozen.Config;

/**
 * @author fissban
 */
public abstract class XmlParser
{
	public final Logger LOG = Logger.getLogger(getClass().getName());
	
	private static final DocumentBuilderFactory BUILDER;
	static
	{
		BUILDER = DocumentBuilderFactory.newInstance();
		BUILDER.setValidating(false);
		BUILDER.setIgnoringComments(true);
	}
	
	private Document currentDocument = null;
	
	/**
	 * This method can be used to load/reload the data.<br>
	 * It's highly recommended to clear the data storage, either the list or map.
	 */
	public abstract void load();
	
	/**
	 * Wrapper for {@link #loadFile(File)} method.
	 * @param path the relative path to the datapack root of the XML file to parse.
	 */
	protected void loadFile(String path)
	{
		loadFile(new File(Config.DATAPACK_ROOT, path));
	}
	
	/**
	 * Read a single XML file.<br>
	 * If the file was successfully parsed, call {@link #parseFile(Document)} for the parsed document.<br>
	 * <b>Validation is enforced.</b>
	 * @param file the XML file to parse.
	 */
	private void loadFile(File file)
	{
		if (!file.getName().toLowerCase().endsWith(".xml"))
		{
			LOG.warning(getClass().getSimpleName() + ": Could not parse " + file.getName() + " is not a file or it doesn't exist!");
			return;
		}
		
		try
		{
			currentDocument = BUILDER.newDocumentBuilder().parse(file);
			currentDocument.normalize();
			
			parseFile();
		}
		catch (Exception e)
		{
			LOG.warning(getClass().getSimpleName() + ": Could not parse " + file.getName() + " file: " + e.getMessage());
			e.printStackTrace();
			return;
		}
	}
	
	/**
	 * Gets the current document.
	 * @return the current document
	 */
	public Document getCurrentDocument()
	{
		return currentDocument;
	}
	
	public List<Node> getNodes(String list)
	{
		List<Node> nodes = new ArrayList<>();
		
		for (Node n = currentDocument.getFirstChild().getFirstChild(); n != null; n = n.getNextSibling())
		{
			if (n.getNodeName().equalsIgnoreCase(list))
			{
				nodes.add(n);
			}
		}
		return nodes;
	}
	
	public List<Node> getNodes()
	{
		List<Node> nodes = new ArrayList<>();
		
		for (Node n = currentDocument.getFirstChild().getFirstChild(); n != null; n = n.getNextSibling())
		{
			nodes.add(n);
		}
		return nodes;
	}
	
	/**
	 * Wrapper for {@link #parseDirectory(File, boolean)}.
	 * @param  file the path to the directory where the XML files are.
	 * @return      {@code false} if it fails to find the directory, {@code true} otherwise.
	 */
	protected boolean readDirectory(File file)
	{
		return readDirectory(file, false);
	}
	
	/**
	 * Wrapper for {@link #parseDirectory(File, boolean)}.
	 * @param  path the path to the directory where the XML files are.
	 * @return      {@code false} if it fails to find the directory, {@code true} otherwise.
	 */
	protected boolean readDirectory(String path)
	{
		return readDirectory(new File(path), false);
	}
	
	/**
	 * Wrapper for {@link #parseDirectory(File, boolean)}.
	 * @param  path      the path to the directory where the XML files are.
	 * @param  recursive parses all sub folders if there is.
	 * @return           {@code false} if it fails to find the directory, {@code true} otherwise.
	 */
	protected boolean readDirectory(String path, boolean recursive)
	{
		return readDirectory(new File(path), recursive);
	}
	
	/**
	 * Loads all XML files from {@code path} and calls {@link #loadFile(File)} for each one of them.
	 * @param  dir       the directory object to scan.
	 * @param  recursive parses all sub folders if there is.
	 * @return           {@code false} if it fails to find the directory, {@code true} otherwise.
	 */
	protected boolean readDirectory(File dir, boolean recursive)
	{
		if (!dir.exists())
		{
			LOG.warning(getClass().getSimpleName() + ": Folder " + dir.getAbsolutePath() + " doesn't exist!");
			return false;
		}
		
		for (File f : dir.listFiles())
		{
			if (recursive && f.isDirectory())
			{
				readDirectory(f, recursive);
			}
			else if (f.getName().toLowerCase().endsWith(".xml"))
			{
				loadFile(f);
			}
		}
		return true;
	}
	
	/**
	 * Abstract method that when implemented will parse the current document.<br>
	 * Is expected to be call from {@link #loadFile(File)}.
	 */
	protected abstract void parseFile();
	
	/**
	 * Parses the int.
	 * @param  n    the named node map.
	 * @param  name the attribute name.
	 * @return      a parsed integer.
	 */
	protected static int parseInt(NamedNodeMap n, String name)
	{
		return Integer.parseInt(n.getNamedItem(name).getNodeValue());
	}
	
	/**
	 * Parses the long.
	 * @param  n    the named node map.
	 * @param  name the attribute name.
	 * @return      a parsed integer.
	 */
	protected static Long parseLong(NamedNodeMap n, String name)
	{
		return Long.valueOf(n.getNamedItem(name).getNodeValue());
	}
	
	/**
	 * Parses the double.
	 * @param  n    the named node map.
	 * @param  name the attribute name.
	 * @return      a parsed double.
	 */
	protected static Double parseDouble(NamedNodeMap n, String name)
	{
		return Double.valueOf(n.getNamedItem(name).getNodeValue());
	}
	
	/**
	 * Parses the boolean.
	 * @param  n    the named node map.
	 * @param  name the attribute name.
	 * @return      {@code true} if the attribute exists and it's value is {@code true}, {@code false} otherwise.
	 */
	protected static boolean parseBoolean(NamedNodeMap n, String name)
	{
		final Node b = n.getNamedItem(name);
		return (b != null) && Boolean.parseBoolean(b.getNodeValue());
	}
	
	/**
	 * @param  n    the named node map
	 * @param  name the attribute name
	 * @return      the node string value for the given node name and named node map if exist, otherwise an empty string
	 */
	protected static String parseString(NamedNodeMap n, String name)
	{
		final Node b = n.getNamedItem(name);
		return (b == null) ? "" : b.getNodeValue();
	}
}
