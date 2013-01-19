import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Renderer;
import net.htmlparser.jericho.Segment;
import net.htmlparser.jericho.Source;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class FeedParser {

	public static String extractPicture(String content) {
		Source source = new Source(content);
		source.fullSequentialParse();
		Element image = source.getFirstElement(HTMLElementName.IMG);

		if (image != null)
			return image.getAttributeValue("src");
		else
			return "";
	}

	public static RSSEntry parseRSSEntry(Node data) {
		RSSEntry entry = new RSSEntry();

		NodeList subitems = data.getChildNodes();

		for (int i = 0; i < subitems.getLength(); i++) {
			Node n = subitems.item(i);

			if (n.getNodeName().equals("pubDate"))
				entry.date = n.getTextContent();

			if (n.getNodeName().equals("title")) {
				Source htmlSource = new Source(n.getTextContent());
				Segment htmlSeg = new Segment(htmlSource, 0, n.getTextContent()
						.length());
				Renderer htmlRend = new Renderer(htmlSeg);
				entry.title = htmlRend.toString().replace('\n', ' ')
						.replace('\r', ' ');
			}

			if (n.getNodeName().equals("link"))
				entry.link = n.getTextContent();

			if (n.getNodeName().equals("description")) {
				Source htmlSource = new Source(n.getTextContent());
				Segment htmlSeg = new Segment(htmlSource, 0, n.getTextContent()
						.length());
				Renderer htmlRend = new Renderer(htmlSeg);
				entry.description = htmlRend.toString();

				// Extract picture, if any
				entry.picture = extractPicture(n.getTextContent());

			}
		}

		return entry;
	}

	public static List<RSSEntry> parse(InputStream is) throws Exception {
		List<RSSEntry> entries = new ArrayList<RSSEntry>();

		// Parse XML
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		dbf.setValidating(false);
		dbf.setIgnoringComments(false);
		dbf.setIgnoringElementContentWhitespace(true);
		dbf.setNamespaceAware(true);

		DocumentBuilder db = null;
		db = dbf.newDocumentBuilder();
		db.setEntityResolver(new NullResolver());

		Document doc = db.parse(is);

		// Find root item
		Node root = null;
		NodeList nodes = doc.getChildNodes();

		for (int k = 0; k < nodes.getLength(); k++) {
			Node n = nodes.item(k);
			if (n.getNodeName().toLowerCase().equals("rss"))
				root = n;
		}

		if (root != null) {
			// Get items
			NodeList channels = root.getChildNodes();

			for (int i = 0; i < channels.getLength(); i++) {
				Node channel = channels.item(i);

				if (channel.getNodeName().toLowerCase().equals("channel")) {

					NodeList items = channel.getChildNodes();

					for (int j = 0; j < items.getLength(); j++) {
						Node entry = items.item(j);

						if (entry.getNodeName().equals("item"))
							entries.add(parseRSSEntry(entry));
					}
				}
			}
		} else
			throw new Exception("Not an RSS document");

		return entries;
	}
}
