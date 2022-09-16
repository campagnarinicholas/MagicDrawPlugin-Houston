package hu.modeldriven.cameo;

import com.nomagic.magicdraw.actions.ActionsConfiguratorsManager;
import com.nomagic.magicdraw.core.Application;
import com.nomagic.magicdraw.plugins.Plugin;

public class Lesson1Plugin extends Plugin {

    @Override
    public void init() {
        createMainMenuAction();
        Application.getInstance().getGUILog().showMessage("Test Package Loaded");
    }

    private void createMainMenuAction() {
        // Where the magic happens
        var action = new MainMenuAction("Lesson2MainMenuAction", "Houston, do we have a problem?");
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
