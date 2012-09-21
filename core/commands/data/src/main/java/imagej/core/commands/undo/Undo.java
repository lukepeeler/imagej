package imagej.core.commands.undo;

import imagej.command.Command;
import imagej.command.Unrecordable;
import imagej.data.display.ImageDisplay;
import imagej.menu.MenuConstants;
import imagej.plugin.Menu;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

@Plugin(menu = {
	@Menu(label = MenuConstants.EDIT_LABEL,
		weight = MenuConstants.EDIT_WEIGHT,
		mnemonic = MenuConstants.EDIT_MNEMONIC),
	@Menu(label = "Undo", weight=50)},
	headless = true)
public class Undo implements Command, Unrecordable {

	@Parameter
	private UndoService service;
	
	@Parameter(required = false)
	private ImageDisplay display;
	
	@Override
	public void run() {
		service.undo(display);
	}

}