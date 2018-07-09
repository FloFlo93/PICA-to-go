package univie.cube.PicaDesktop.remote;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.biojava.nbio.ws.alignment.qblast.*;
import org.biojava.nbio.ws.alignment.qblast.BlastProgramEnum;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.biojava.nbio.ws.alignment.qblast.BlastOutputParameterEnum;


public class Blast {
	
	private String sequence;
	
	public Blast(String sequence) {
		this.sequence = sequence;
	}
	
	
	/**
	 * 
	 * @return Pair (left: accessionNumber, right: recName) of top result
	 * @throws Exception 
	 */
	public String runBlast() throws Exception {
		InputStream in = remoteBlast();
		String result = parseXML(in);
		return removeForbiddenCharacters(result);
	}
	
	private InputStream remoteBlast() throws Exception {
		
		//source-code from: http://biojava.org/wiki/BioJava:CookBook3:NCBIQBlastService; modified 
		
		NCBIQBlastService service = new NCBIQBlastService();
		NCBIQBlastAlignmentProperties props = new NCBIQBlastAlignmentProperties();
		props.setBlastProgram(BlastProgramEnum.blastp);
	    props.setBlastDatabase("swissprot");
	    NCBIQBlastOutputProperties outputProps = new NCBIQBlastOutputProperties();
	    outputProps.setOutputOption(BlastOutputParameterEnum.SHOW_OVERVIEW, "2");
	    String rid = null;          // blast request ID
	    try {
	        // send blast request and save request id
	        rid = service.sendAlignmentRequest(sequence, props);
	
	        // wait until results become available. Alternatively, one can do other computations/send other alignment requests
	        while (!service.isReady(rid)) {
	            Thread.sleep(5000);
	        }
	
	        // read results when they are ready
	        InputStream in = service.getAlignmentResults(rid, outputProps);
	        return in;
	        
	    } finally {
	        service.sendDeleteRequest(rid);
	    }
	}
	
	/**
	 * 
	 * @param in InputStream in XML format (BLAST result)
	 * @return Pair (left: recId, right: recName)
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 * @throws IOException
	 */
	private String parseXML(InputStream in) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse(in);
        NodeList hitsList = doc.getElementsByTagName("Hit");
        if(hitsList == null) return "";
        Node topHit = hitsList.item(0);
        String recId = "";
        String recName = "";
        NodeList childs = topHit.getChildNodes();
        for(int i=0; i<childs.getLength(); i++) {
        	if (childs.item(i).getNodeName().equals("Hit_def")) recName = childs.item(i).getTextContent();
        	else if (childs.item(i).getNodeName().equals("Hit_id")) recId = childs.item(i).getTextContent();
        }
        return recId + " " + recName;
	}
	
	private String removeForbiddenCharacters(String str) {
		str = str.replace(",", "");
		str = str.replace("\t", "");
		str = str.replaceAll(";", "");
		return str;
	}

}
