package wmas.world.functions;

import wmas.gui.world.WorldAttributeEditor;
import wmas.gui.world.WorldAttributeFactory;
import wmas.gui.world.WorldEditor;
import wmas.gui.world.functions.FunctionWorldAttributeEditor;
import wmas.xml.XMLEntity;

public class WorldFunctionAttributeFactory implements WorldAttributeFactory {
	private FunctionWorldAttributeEditor editor = new FunctionWorldAttributeEditor();

	@Override
	public XMLEntity createWorldAttribute(int id) {
		return new WorldFunctionAttribute();
	}

	@Override
	public int getNbWorldAttribute() {
		return 1;
	}

	@Override
	public String getWorldAttributeDescription(int id) {
		return "Global user-defined functions";
	}

	@Override
	public String getWorldAttributeName(int id) {
		return "functions";
	}

	@Override
	public WorldAttributeEditor getWorldAttributeView(int id) {
		return editor;
	}

	public static void registerAll() {
		WorldFunctionAttributeFactory f = new WorldFunctionAttributeFactory();
		WorldEditor.registerAttributeFactory(f);
	}
}
