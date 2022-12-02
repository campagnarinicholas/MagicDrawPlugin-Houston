package hu.modeldriven.cameo;

import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.uml.RepresentationTextCreator;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdinformationflows.InformationFlow;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class HoustonAPI {
    private Project project;
    private String modelName;
    private String author;
    private static final ArrayList<String> cameoDiagramNames = new ArrayList<String>(
            Arrays.asList("Diagram CostRollUpPattern", "Diagram Basic Units", "Diagram requirement verification",
                    "Diagram rollup patterns", "Diagram PowerRollUpPattern", "Diagram MassRollUpPattern",
                    "Diagram Basic Unit Categories")
    );
    public HoustonAPI(Project project){
        this.project = project;
    }

    // Send path to XML file to HoustonAPI
    // (Assuming that HoustonAPI is running locally)
    // String fileName = path to XML file
    public String getFromAPI(String fileName, String request, String body) throws IOException {
        URL url = new URL("http://127.0.0.1:5000/result?xml=" + fileName + "&request=" + request + getCurrentElements());
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

    // Send API Post message
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

    public String getCurrentElements(){
        DiagramPresentationElement dpe = this.project.getActiveDiagram();
        List<PresentationElement> elements = dpe.getPresentationElements();
        String elementsArgument = "";
        NamedElement el;
        for( int i=1; i<elements.size(); i++ ){
            el = (NamedElement) elements.get(i).getElement();
            elementsArgument +=  "&elements=" + RepresentationTextCreator.getRepresentedText(el).replace("-", "").replace("&", "%26");
            /*if( i<elements.size() - 1 ){
                elementsArgument += ",";
            }*/
        }
        return elementsArgument.replace(" ", "%20").replace(":", "%3A"); // Remove spaces and common URL-API reserved char
    }

    // Build JSON request body for API
    public String _buildAPIBody(Project project) {
        Collection<DiagramPresentationElement> diagrams = project.getDiagrams();
        ArrayList<DiagramPresentationElement> filteredDiagrams = new ArrayList<DiagramPresentationElement>();
        for(DiagramPresentationElement currDiagram : diagrams){
            if(!cameoDiagramNames.contains(currDiagram.getHumanName())){
                filteredDiagrams.add(currDiagram);
            }
        }
        DiagramPresentationElement dpe = project.getActiveDiagram();
        List<PresentationElement> elements = dpe.getPresentationElements();
        Element el;
        el = elements.get(0).getElement();
        modelName = el.getHumanName();
        author = RepresentationTextCreator.getRepresentedText(((List<Element>) el.getOwnedElement()).get(3)).replace("\"", "");

        String request = "{ \"elements\":[";
        for(int j=0; j<filteredDiagrams.size(); j++) {
            dpe = filteredDiagrams.get(j);
            dpe.ensureLoaded();
            elements = dpe.getPresentationElements();
            List<Element> ownedEls;
            ArrayList<String> attributes = new ArrayList<String>();

            String attributesAndBehaviors;
            String type;
            for( int i=1; i<elements.size(); i++) {
                request += "{";
                el = elements.get(i).getElement();
                ownedEls = (List<Element>) el.getOwnedElement();
                getAttributesAndBehaviors(ownedEls, attributes);
                attributesAndBehaviors = addAttributesAndBehaviors(attributes);
                type = determineType(el);
                request += String.format("\"name\":\"%s\",", RepresentationTextCreator.getRepresentedText(el).replace("-", ""));
                request += attributesAndBehaviors;
                request += String.format("\"author\":\"%s\",", author);
                request += String.format("\"model\":\"%s\",", modelName);
                request += "\"associations\":{},";
                request += "\"axioms\":{},";
                request += "\"lineage\":{\"parents\": {}, \"siblings\":{}, \"children\":{}},";
                request += String.format("\"type\":\"%s\"", type);
                if (i < elements.size() - 1) {
                    request += "},";
                } else {
                    request += "}";
                }
            }
            if( j < filteredDiagrams.size()-1 && elements.size() > 1){
                request += ",";
            }
        }
        request += "]}";
        return request;
    }

    // Build JSON request body for API
    public String buildAPIBody(Project project) {
        ArrayList<HoustonDBNode> houstonDBNodes = buildNodes(project);
        HoustonDBNode node;
        String request = "{ \"elements\":[";
        for(int i=0; i<houstonDBNodes.size(); i++) {
            node = houstonDBNodes.get(i);
            ArrayList<String> attributes = node.getAttributes();
            String type = node.getType();
            String attributesAndBehaviors = addAttributesAndBehaviors(attributes);
            String associations = addAssociationString(node);
            request += "{";
            request += String.format("\"name\":\"%s\",", node.getName());
            request += attributesAndBehaviors;
            request += String.format("\"author\":\"%s\",", author);
            request += String.format("\"model\":\"%s\",", modelName);
            request += associations;
            request += "\"axioms\":{},";
            request += "\"lineage\":{\"parents\": {}, \"siblings\":{}, \"children\":{}},";
            request += String.format("\"type\":\"%s\"", type);
            if (i < houstonDBNodes.size() - 1) {
                request += "},";
            } else {
                request += "}";
            }
        }
        request += "]}";
        return request;
    }

    // Build nodes list for body for API
    private ArrayList<HoustonDBNode> buildNodes(Project project) {
        Collection<DiagramPresentationElement> diagrams = project.getDiagrams();
        ArrayList<DiagramPresentationElement> filteredDiagrams = new ArrayList<DiagramPresentationElement>();
        for (DiagramPresentationElement currDiagram : diagrams) {
            if (!cameoDiagramNames.contains(currDiagram.getHumanName())) {
                filteredDiagrams.add(currDiagram);
            }
        }
        DiagramPresentationElement dpe = project.getActiveDiagram();
        List<PresentationElement> elements = dpe.getPresentationElements();
        Element el;
        el = elements.get(0).getElement();
        modelName = el.getHumanName();
        author = RepresentationTextCreator.getRepresentedText(((List<Element>) el.getOwnedElement()).get(3)).replace("\"", "");
        HashMap<String, HoustonDBNode> houstonDBNodes = new HashMap<>();
        ArrayList<HoustonDBNode> houstonDBNodesList = new ArrayList<>();
        ArrayList<Connector> connectors = new ArrayList<>();
        for (int j = 0; j < filteredDiagrams.size(); j++) {
            dpe = filteredDiagrams.get(j);
            dpe.ensureLoaded();
            elements = dpe.getPresentationElements();
            for (int i = 1; i < elements.size(); i++) {
                el = elements.get(i).getElement();

                if (el.getHumanType().compareTo("Connector") == 0){
                    connectors.add((Connector) el);
                } else {
                    HoustonDBNode houstonDBNode = new HoustonDBNode((NamedElement) el, modelName, author);
                    houstonDBNodes.put(houstonDBNode.getName(), houstonDBNode);
                    houstonDBNodesList.add(houstonDBNode);
                }
            }
        }
        HoustonDBNode endpointSource, endpointTarg;
        String sourceName, targName;
        sourceName = targName = "";
        for(int i=0; i<connectors.size(); i++){
            List<InformationFlow> ifs = (List<InformationFlow>) (connectors.get(i)).get_informationFlowOfRealizingConnector();
            for(InformationFlow flow : ifs) {
                Collection<NamedElement> infoSource = flow.getInformationSource();
                Collection<NamedElement> infoTarg = flow.getInformationTarget();
                for( NamedElement info : infoSource ){
                    sourceName = RepresentationTextCreator.getRepresentedText(info).replace("-", "");
                }
                for( NamedElement info : infoTarg ){
                    targName = RepresentationTextCreator.getRepresentedText(info).replace("-", "");
                }
                endpointSource = houstonDBNodes.get(sourceName);
                endpointTarg = houstonDBNodes.get(targName);
                endpointSource.addAssociate(endpointTarg, "information flow");
            }
        }
        return houstonDBNodesList;
    }


    // Determine type for our element for Houston DB
    private String determineType(Element el) {
        String human_type = el.getHumanType().toLowerCase();
        if( human_type.contains("signal") ){
            return "signal";
        } else if( human_type.contains("interface") ) {
            return "interface";
        } else if( human_type.compareTo("block") == 0 || human_type.compareTo("part property") == 0) {
            return "device";
        } else {
            return human_type;
        }
    }

    // Gets association string for JSON
    private String addAssociationString(HoustonDBNode node){
        String ret = "\"associations\":{ ";

        ret += "\"device\": [" + addSimpleJsonString(node.getDeviceAssociates()) + "],";
        ret += "\"interface\": [" + addSimpleJsonString(node.getInterfaceAssociates()) + "],";
        ret += "\"signal\": [" + addSimpleJsonString(node.getSignalAssociates()) + "]";

        ret += "},";

        return ret;
    }

    public String addSimpleJsonString(ArrayList<HoustonDBNode> arrayList){
        String ret = "";
        for(int i=0; i<arrayList.size(); i++) {
            ret += "\"" + arrayList.get(i).getName() + "\"";
            if(i < arrayList.size()-1) {
                ret += ",";
            }
        }
        return ret;
    }

    // Builds attribute string for JSON request body
    public String addAttributesAndBehaviors(ArrayList<String> attributes) {
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
            //if( j < attributes.size() - 1 ) {
            request += ",";
            //}

        }
        request += "\"ignored\":[]";
        /*
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

    // Get attributes and sorts to array
    private void getAttributesAndBehaviors(List<Element> ownedEls, ArrayList<String> attributes) {
        String ownedElement;
        attributes.clear();
        for( int i=0; i<ownedEls.size(); i++){
            ownedElement = RepresentationTextCreator.getRepresentedText(ownedEls.get(i)).replace("'", "");
            ownedElement = ownedElement.replace("\"", "");
            attributes.add(ownedElement);
        }
    }

}
