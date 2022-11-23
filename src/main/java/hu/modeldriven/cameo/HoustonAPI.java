package hu.modeldriven.cameo;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.RepresentationTextCreator;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HoustonAPI {
    private Project project;
    private String modelName;
    private String author;

    public HoustonAPI(Project project){
        this.project = project;
    }

    // Send path to XML file to HoustonAPI
    // (Assuming that HoustonAPI is running locally)
    // String fileName = path to XML file
    public String getFromAPI(String fileName, String request, String body) throws IOException {
        URL url = new URL("http://127.0.0.1:5000/result?xml=" + fileName + "&request=" + request);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        os.write(body.getBytes());
        os.close();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
            content.append("\n");
        }
        in.close();
        con.disconnect();

        return content.toString();
    }

    // Send path to XML file to HoustonAPI
    // (Assuming that HoustonAPI is running locally)
    // String fileName = path to XML file
    public String getFromAPI(String fileName, String request, String body, String diagramElementName) throws IOException {
        URL url = new URL("http://127.0.0.1:5000/result?xml=" + fileName + "&request=" + request + "&element=" + diagramElementName);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        OutputStream os = con.getOutputStream();
        os.write(body.getBytes());
        os.close();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
            content.append("\n");
        }
        in.close();
        con.disconnect();

        return content.toString();
    }

    public String buildAPIBody(Project project) {
        DiagramPresentationElement dpe = project.getActiveDiagram();
        List<PresentationElement> elements = dpe.getPresentationElements();
        List<Element> ownedEls;
        ArrayList<String> attributes = new ArrayList<String>();
        ArrayList<String> behaviors = new ArrayList<String>();
        Element el;
        String request = "{ \"elements\":[";
        el = elements.get(0).getElement();
        modelName = el.getHumanName();
        author = RepresentationTextCreator.getRepresentedText(((List<Element>) el.getOwnedElement()).get(3)).replace("\"", "");
        String attributesAndBehaviors;
        String type;
        for( int i=1; i<elements.size(); i++){
            request += "{";
            el = elements.get(i).getElement();
            request += String.format("\"name\":\"%s\",", RepresentationTextCreator.getRepresentedText(el));
            ownedEls = (List<Element>) el.getOwnedElement();
            getAttributesAndBehaviors(ownedEls, attributes, behaviors);
            attributesAndBehaviors = addAttributesAndBehaviors(attributes, behaviors);
            type = determineType(el);
            request += attributesAndBehaviors;
            request += String.format("\"author\":\"%s\",", author);
            request += String.format("\"model\":\"%s\",", modelName);
            request += "\"associations\":{},";
            request += "\"axioms\":{},";
            request += "\"lineage\":{\"parents\": {}, \"siblings\":{}, \"children\":{}},";
            request += String.format("\"type\":\"%s\"", type);
            if( i < elements.size() - 1) {
                request += "},";
            } else {
                request += "}";
            }
        }
        request += "]}";
        return request;
    }

    private String determineType(Element el) {
        String human_type = el.getHumanType().toLowerCase();
        if( human_type.contains("signal") ){
            return "signal";
        } else if( human_type.contains("interface") ) {
            return "interface";
        } else if( human_type.compareTo("block") == 0 ) {
            return "device";
        } else {
            return human_type;
        }
    }

    public String addAttributesAndBehaviors(ArrayList<String> attributes, ArrayList<String> behaviors) {
        String attributeVal;
        String[] attributeVals;
        String request = "";
        request += "\"attributes\":{";
        for( int j=0; j< attributes.size(); j++){
            attributeVal = attributes.get(j);
            if( attributeVal.contains("=") ){
                attributeVals = attributeVal.split("=", 2);
                request += '"' + attributeVals[0].strip() + '"' + ":" + '"' + attributeVals[1].strip() + '"';
            } else {
                request += "\"attribute" + String.valueOf(j) + "\":\"" + attributes + '"';
            }
            if( j < attributes.size() - 1 ) {
                request += ",";
            }

        }/*
        request += "\"behaviors\":[";
        for( int j=0; j< behaviors.size(); j++) {
            request += "{";
            attributeVal = behaviors.get(j);
            if( attributeVal.contains("=") ){
                attributeVals = attributeVal.split("=", 2);
                request += '"' + attributeVals[0].strip() + '"' + ":" + '"' + attributeVals[1].strip() + '"';
            } else {
                request += "\"attribute" + String.valueOf(j) + "\":\"" + attributes + '"';
            }
            request += "}";
            if( j < behaviors.size() - 1) {
                request += ",";
            }
        }*/
        request += "},";
        return request;
    }

    private void getAttributesAndBehaviors(List<Element> ownedEls, ArrayList<String> attributes, List<String> behaviors) {
        String ownedElement;
        attributes.clear();
        behaviors.clear();
        for( int i=0; i<ownedEls.size(); i++){
            ownedElement = RepresentationTextCreator.getRepresentedText(ownedEls.get(i)).replace("'", "");
            if( ownedElement.toLowerCase().contains("behavior")) {
                attributes.add(ownedElement);
            } else {
                attributes.add(ownedElement);
            }
        }
    }

}
