package hu.modeldriven.cameo;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.core.project.ProjectDescriptorsFactory;
import com.nomagic.magicdraw.core.project.ProjectsManager;
import com.nomagic.magicdraw.uml.RepresentationTextCreator;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import java.awt.event.ActionEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainMenuAction extends MDAction {
    private static final String recommendations = "Generate All Recommendations";
    private static final String rules = "Check Against Rules";

    private static final String debug = "Show All Attributes";
    private String name;
    private String author, modelName;
    public MainMenuAction(String id, String name) {
        super(id, name, null, null);
        this.name = name;
    }

    // To be called onclick of MenuItem
    // ActionEvent actionEvent - event that happened to call this fn
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        Project project = saveProject();
        String response = "";
        String fileName = project.getFileName();
        HoustonAPI api = new HoustonAPI(project);
        String body = api.buildAPIBody(project);

        if (fileName != null) {
            fileName = fileName.replace(" ", "%20");
        } // Make usable as API param
        try {
            if( this.name.compareTo(recommendations) == 0 ) {
                response = api.getFromAPI(fileName, "recommendations", body);
            } else if ( this.name.compareTo(rules) == 0 ) {
                response = api.getFromAPI(fileName, "rules", body);
                //response = body;
            } else if ( this.name.compareTo(debug) == 0 ) {
                DiagramPresentationElement dpe = project.getActiveDiagram();
                List<PresentationElement> elements = dpe.getPresentationElements();
                List<Element> ownedEls;
                Element el;
                String[] attributeVals;
                for( int i=0; i<elements.size(); i++){
                    el = elements.get(i).getElement();
                    response += RepresentationTextCreator.getRepresentedText(el);
                    ownedEls = (List<Element>) el.getOwnedElement();
                    response += "\n*Owned El*\n";
                    for( int j=0; j< ownedEls.size(); j++){
                        response += RepresentationTextCreator.getRepresentedText(ownedEls.get(j));
                        response += "\n";
                    }
                    response += "\n*Type*\n" + el.getHumanType();
                    response += "\n---\n";
                }
            } else {
                response = "Unrecognized Command";
            }
            Application.getInstance().getGUILog().showMessage(response);
        } catch (IOException e) {
            Application.getInstance().getGUILog().showMessage(e.getMessage());
        }
    }
/*
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
        }
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
*/
    // Saves project to ensure we have current version
    private Project saveProject(){
        ProjectsManager projectsManager = Application.getInstance().getProjectsManager();
        Project project = projectsManager.getActiveProject();
        ProjectDescriptor projectDescriptor = ProjectDescriptorsFactory.getDescriptorForProject(project);
        projectsManager.saveProject(projectDescriptor, true);

        return project;
    }

}