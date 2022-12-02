package hu.modeldriven.cameo;

import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.plugins.Plugin;


public class Lesson1Plugin extends Plugin {

    @Override
    public void init() {
        createMainMenuAction("Generate All Recommendations");
        createMainMenuAction("Check Against Rules");
        createMainMenuAction("Show All Attributes");
        //createDiagramAction("Generate Element Recommendations");
        createDiagramAction("Check Element Against Rules");
        //createDiagramAction("Show Element Attributes");
        Application.getInstance().getGUILog().showMessage("Houston Plugin Loaded");
    }

    private void createDiagramAction(String name) {
        var action = new hu.modeldriven.cameo.DiagramAction("Lesson2DiagramAction", name);
        var configurator = new DiagramConfiguration(action);
        ActionsConfiguratorsManager.getInstance().addAnyDiagramCommandBarConfigurator(configurator);
    }

    private void createMainMenuAction(String name) {
        // Where the magic happens
        var action = new MainMenuAction("Lesson2MainMenuAction", name);
        var configurator = new MainMenuConfiguration(action);
        ActionsConfiguratorsManager.getInstance().addMainMenuConfigurator(configurator);
    }
    @Override
    public boolean close() {
        return true;
    }

    @Override
    public boolean isSupported() {
        return true;
    }
}
