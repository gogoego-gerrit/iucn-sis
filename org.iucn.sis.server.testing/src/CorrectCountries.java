import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.iucn.sis.server.api.fields.definitions.FieldDefinitionLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.solertium.util.BaseDocumentUtils;
import com.solertium.util.DynamicWriter;
import com.solertium.util.ElementCollection;
import com.solertium.util.NodeCollection;


public class CorrectCountries extends DynamicWriter {
	
	public static void main(String[] args) throws Exception {
		final File file = new File("/home/adam.schwartz/projects/SIS/countries.sql"); 
		final BufferedWriter writer = new BufferedWriter(new PrintWriter(new FileWriter(file)));
		
		CorrectCountries c = new CorrectCountries();
		c.setOutputStream(writer, "\n");
		c.run();
	}
	
	public void run() {
		final Document document = FieldDefinitionLoader.get("CountryOccurrence");
		final ElementCollection roots = new ElementCollection(
			document.getDocumentElement().getElementsByTagName("root")
		);
		for (Element root : roots) {
			String code = root.getAttribute("code");
			String codeable = root.getAttribute("codeable");
			String depth = root.getAttribute("depth");
			
			final NodeCollection nodes = new NodeCollection(root.getChildNodes());
			for (Node node : nodes) {
				if ("label".equals(node.getNodeName())) {
					toSQL(code, "(root)", depth, codeable, code, node.getTextContent());
				}
				else if ("child".equals(node.getNodeName())) {
					parseChild(node, code);
				}
			}
		}	
	}
	
	private void parseChild(Node node, String parentID) {
		String code = BaseDocumentUtils.impl.getAttribute(node, "code");
		String codeable = BaseDocumentUtils.impl.getAttribute(node, "codeable");
		String depth = BaseDocumentUtils.impl.getAttribute(node, "depth");
		
		final NodeCollection children = new NodeCollection(node.getChildNodes());
		for (Node child : children) {
			if ("label".equals(child.getNodeName()))
				toSQL(code, parentID, depth, codeable, code, child.getTextContent());
			else if ("child".equals(child.getNodeName()))
				parseChild(child, code);
		}
	}
	
	private void toSQL(String code, String parentID, String level, String codeable, String ref, String description) {
		write("INSERT INTO CountryOccurrenceLookup (code, parentID, level, codeable, ref, description) " +
				"VALUES ('" + code + "', '" + parentID + "', " + level + ", " + codeable + 
				", '" + ref + "', '" + description.replaceAll("'", "''") + "');");
	}
}
