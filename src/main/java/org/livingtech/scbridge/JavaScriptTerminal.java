package org.livingtech.scbridge;

import deformablemesh.gui.SwingJSTerm;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import sc.iview.SciView;

import static sc.iview.commands.MenuWeights.DEMO;

@Plugin(type = Command.class, label = "DM3D Script Editor", menuRoot = "SciView",
        menu = { @Menu(label = "Demo", weight = DEMO),
                @Menu(label = "JS Editor") })
public class JavaScriptTerminal implements Command {
    @Parameter
    private SciView sciView;
    @Override
    public void run(){
        SwingJSTerm swingJSTerm = new SwingJSTerm();
        swingJSTerm.showTerminal();
        swingJSTerm.addToScriptEngine("sciView", sciView);
        swingJSTerm.evaluateHeadless("TestMeshes = Java.type('org.livingtech.scbridge.TestMeshes');");
        swingJSTerm.evaluateHeadless("Vector3f = Java.type('org.joml.Vector3f');");
    }

    public static void main(String[] args) throws Exception{
        SciView sv = SciView.create();
        // This line then runs our MyDemo command via scijava's CommandService.
        sv.getScijavaContext().getService(CommandService.class).run(JavaScriptTerminal.class, true);
    }
}
