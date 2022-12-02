package hu.modeldriven.cameo;

import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.core.Project;
import com.nomagic.magicdraw.core.project.ProjectDescriptor;
import com.nomagic.magicdraw.core.project.ProjectDescriptorsFactory;
import com.nomagic.magicdraw.core.project.ProjectsManager;
import com.nomagic.magicdraw.ui.actions.DefaultDiagramAction;
import com.nomagic.magicdraw.uml.RepresentationTextCreator;
import com.nomagic.magicdraw.uml.symbols.DiagramPresentationElement;
import com.nomagic.magicdraw.uml.symbols.PresentationElement;
import com.nomagic.uml2.ext.magicdraw.classes.mdkernel.Element;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;

public class DiagramAction extends DefaultDiagramAction {
    private static final String recommendations = "Generate Element Recommendations";
    private static final String rules = "Check Element Against Rules";
    private static final String debug = "Show Element Attributes";

    private String name;
    public DiagramAction(String id, String name) {
        super(id, name, null, null);
        this.name = name;

    }

    // Send correct API request to Houston
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        var presentationElements = getSelected();

        if (presentationElements != null && presentationElements.size() > 0) {
            var presentationElement = presentationElements.get(0);

            var name = RepresentationTextCreator.getRepresentedText(presentationElement).replace("-", "");

            Project project = saveProject();
            String response = "";
            String fileName = project.getFileName();
            HoustonAPI api = new HoustonAPI(project);
            String body = api.buildAPIBody(project);

            if (fileName != null) {
                fileName = fileName.replace(" ", "%20");
            }
            name = name.replace(" ", "%20"); // Make usable as API param
            try {
                if( this.name.compareTo(recommendations) == 0 ) {
                    response = api.getFromAPI(fileName, "recommendations", body, name);
                } else if( this.name.compareTo(rules) == 0 ) {
                    response = api.getFromAPI(fileName, "rules", body, name);
                } else if( this.name.compareTo(debug) == 0 ){
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
                    response = "Unrecognized command";
                }
                Application.getInstance().getGUILog().showMessage(response);
            } catch (IOException e) {
                Application.getInstance().getGUILog().showMessage(e.getMessage());
            }
        } else {
            Application.getInstance().getGUILog().showMessage("Please first select a node");
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