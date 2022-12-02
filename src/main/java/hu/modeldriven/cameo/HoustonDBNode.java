package hu.modeldriven.cameo;

import com.nomagic.magicdraw.uml.RepresentationTextCreator;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdinformationflows.InformationFlow;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;

import java.util.*;
import java.util.jar.Attributes;

public class HoustonDBNode {
    private String name;
    private String author;
    private String model;
    private String type;

    private ArrayList<HoustonDBNode> deviceAssociates, interfaceAssociates, signalAssociates;
    private ArrayList<String> attributes;

    private HashMap<String, String> devAssocType, interfaceAssocType, sigAssocType;

    public HoustonDBNode(NamedElement element, String modelName, String authorName) {
        this.name =  RepresentationTextCreator.getRepresentedText(element).replace("-", "");
        this.author = authorName;
        this.model = modelName;
        this.type = determineType(element);
        this.attributes = new ArrayList<>();

        List<Element> ownedElements = (List<Element>) element.getOwnedElement();
        for(int i=0; i<ownedElements.size(); i++){
            String attrText = RepresentationTextCreator.getRepresentedText(ownedElements.get(i)).replace("'", "");
            attrText = attrText.replace("\"", "");
            attributes.add(attrText);
        }

        deviceAssociates = new ArrayList<>();
        interfaceAssociates = new ArrayList<>();
        signalAssociates = new ArrayList<>();

        devAssocType = new HashMap<>();
        interfaceAssocType = new HashMap<>();
        sigAssocType = new HashMap<>();
    }

    public ArrayList<String> getAttributes(){
        return this.attributes;
    }

    public ArrayList<HoustonDBNode> getDeviceAssociates(){
        return this.deviceAssociates;
    }

    public ArrayList<HoustonDBNode> getInterfaceAssociates(){
        return this.interfaceAssociates;
    }

    public ArrayList<HoustonDBNode> getSignalAssociates(){
        return this.signalAssociates;
    }

    public void addAssociate(HoustonDBNode associate, String assocType){
        String nodeType = associate.getType();
        if(nodeType.compareTo("signal") == 0){
            addSignalAssociate(associate, assocType);
        } else if (nodeType.compareTo("interface") == 0) {
            addInterfaceAssociate(associate, assocType);
        } else {
            addDeviceAssociate(associate, assocType);
        }
    }


    private void addDeviceAssociate(HoustonDBNode associate, String type){
        deviceAssociates.add(associate);
        devAssocType.put(associate.getName(), type);
    }

    private void addInterfaceAssociate(HoustonDBNode associate, String type){
        interfaceAssociates.add(associate);
        interfaceAssocType.put(associate.getName(), type);
    }

    private void addSignalAssociate(HoustonDBNode associate, String type){
        signalAssociates.add(associate);
        sigAssocType.put(associate.getName(), type);
    }

    public String getType(){
        return this.type;
    }

    public String getName(){
        return this.name;
    }

    // Determine type for our element for Houston DB
    private String determineType(Element el) {
        String human_type = el.getHumanType().toLowerCase();
        if( human_type.contains("signal") ){
            return "signal";
        } else if( human_type.contains("interface") ) {
            return "interface";
        } else if( human_type.contains("connector") ){
            return "connector";
        } else if( human_type.compareTo("block") == 0 || human_type.compareTo("part property") == 0) {
            return "device";
        } else {
            return human_type;
        }
    }
}
