package hu.modeldriven.cameo;

import com.nomagic.magicdraw.actions.MDAction;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.core.project.ProjectDescriptorsFactory;
import com.nomagic.magicdraw.core.project.ProjectsManager;
import com.nomagic.magicdraw.uml.ConnectorsCollector;
import com.nomagic.magicdraw.uml.RepresentationTextCreator;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.magicdraw.uml.symbols.SymbolElementMap;
import com.nomagic.uml2.ext.magicdraw.auxiliaryconstructs.mdinformationflows.InformationFlow;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Diagram;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.ElementValue;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.NamedElement;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.ConnectableElement;
import com.nomagic.uml2.ext.magicdraw.compositestructures.mdinternalstructures.Connector;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MainMenuAction extends MDAction {
    private static final String recommendations = "Generate All Recommendations";
    private static final String rules = "Check Against Rules";

    private static final String debug = "Show All Attributes";
    private static final ArrayList<String> cameoDiagramNames = new ArrayList<String>(
            Arrays.asList("Diagram CostRollUpPattern", "Diagram Basic Units", "Diagram requirement verification",
                    "Diagram rollup patterns", "Diagram PowerRollUpPattern", "Diagram MassRollUpPattern",
                    "Diagram Basic Unit Categories")
    );
    private String name;
    public MainMenuAction(String id, String name) {
        super(id, name, null, null);
        this.name = name;
    }

    // To be called onclick of MenuItem. Determines action to take, sends
    // correct request to API, then displays API response.
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
                Collection<DiagramPresentationElement> diagrams = project.getDiagrams();
                ArrayList<DiagramPresentationElement> filteredDiagrams = new ArrayList<DiagramPresentationElement>();
                response += "---\nDiagrams Start\n---\n";
                for(DiagramPresentationElement currDiagram : diagrams){
                    if(!cameoDiagramNames.contains(currDiagram.getHumanName())){
                        filteredDiagrams.add(currDiagram);
                    }
                }
                for(DiagramPresentationElement currDiagram : filteredDiagrams) {
                    response += currDiagram.getHumanName() + "\n";
                }
                response += "---\nDiagrams End\n---\n";
                for(DiagramPresentationElement currDiagram : filteredDiagrams ) {
                    response += String.format("---\nDiagram %s Start\n---\n", currDiagram.getHumanName());
                    DiagramPresentationElement dpe = currDiagram;
                    dpe.ensureLoaded();
                    List<PresentationElement> elements = dpe.getPresentationElements();
                    List<Element> ownedEls;
                    List<ElementValue> ownedElVals;
                    Element el;
                    for (int i = 0; i < elements.size(); i++) {
                        el = elements.get(i).getElement();
                        if(el.getHumanType().compareTo("Connector") == 0) {
                            List<InformationFlow> ifs = (List<InformationFlow>) ((Connector) el).get_informationFlowOfRealizingConnector();
                            for(InformationFlow flow : ifs) {
                                Collection<NamedElement> infoSource = flow.getInformationSource();
                                Collection<NamedElement> infoTarg = flow.getInformationTarget();
                                response += "Information Flow: Name\n" + flow.getHumanName() + "\n";
                                response += "Info Source\n";
                                for( NamedElement info : infoSource ){
                                    response += info.getHumanName() + "\n";
                                    response += RepresentationTextCreator.getRepresentedText(info).replace("-", "") + "\n";
                                }
                                response += "Info Targ\n";
                                for( NamedElement info : infoTarg ){
                                    response += info.getHumanName() + "\n";
                                    response += RepresentationTextCreator.getRepresentedText(info).replace("-", "") + "\n";
                                }
                            }
                        }
                        ownedElVals = (List<ElementValue>) el.get_elementValueOfElement();
                        response += "\n*Owned El Vals*\n";
                        for(ElementValue elval : ownedElVals) {
                            response += elval.getHumanName();
                        }
                        response += "\n*Owned El Vals End*\n";
                        response += RepresentationTextCreator.getRepresentedText(el);
                        ownedEls = (List<Element>) el.getOwnedElement();
                        response += "\n*Owned El*\n";
                        for (int j = 0; j < ownedEls.size(); j++) {
                            response += RepresentationTextCreator.getRepresentedText(ownedEls.get(j));
                            response += "\n";
                        }
                        response += "\n*Type*\n" + el.getHumanType();
                        response += "\n---\n";
                    }
                    response += String.format("---\nDiagram %s End\n---\n", currDiagram.getHumanName());
                }
            } else {
                response = "Unrecognized Command";
            }
            Application.getInstance().getGUILog().showMessage(response);
        } catch (IOException e) {
            Application.getInstance().getGUILog().showMessage(e.getMessage());
        }
    }

    // Saves project to ensure we have current version
    private Project saveProject(){
        ProjectsManager projectsManager = Application.getInstance().getProjectsManager();
        Project project = projectsManager.getActiveProject();
        ProjectDescriptor projectDescriptor = ProjectDescriptorsFactory.getDescriptorForProject(project);
        projectsManager.saveProject(projectDescriptor, true);

        return project;
    }

}