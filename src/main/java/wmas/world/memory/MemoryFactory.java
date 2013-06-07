package wmas.world.memory;

import java.awt.Component;

import wmas.expression.functions.FunctionList;
import wmas.gui.GuiModificationListener;
import wmas.gui.run.EntityAttributeView;
import wmas.gui.world.entity.EntityAttributeFactory;
import wmas.gui.world.entity.EntityEditor;
import wmas.gui.world.memory.MemoryDisplay;
import wmas.gui.world.memory.MemoryEditor;
import wmas.xml.XMLEntity;

public class MemoryFactory implements EntityAttributeFactory {

	private MemoryEditor editor = null;

	public XMLEntity createAttribute(int id) {
		return new Memory();
	}

	@Override
	public Component getAttributeEditor(XMLEntity parent, int id, XMLEntity e,
			GuiModificationListener listener) {
		if (e instanceof Memory) {
			if (editor == null) {
				editor = new MemoryEditor((Memory) e, listener);
			} else
				editor.setOwner((Memory) e, listener);
			return editor;
		}
		return null;
	}

	@Override
	public String getAttributeName(int id) {
		switch (id) {
		case 0:
			return "memory";
		}
		return null;
	}

	@Override
	public int getNbAttribute() {
		return 1;
	}

	public static void registerAll() {
		MemoryFactory f = new MemoryFactory();
		EntityEditor.registerAttributeFactory(f);
		MemoryFunction func = new MemoryFunction();
		FunctionList
				.register(func,
						"Returns the memory data identified by the index given in argument");
	}

	@Override
	public EntityAttributeView getAttributeViewer(int id) {
		return id == 0 ? new MemoryDisplay() : null;
	}

	@Override
	public String getAttributeDescription(int id) {
		switch (id) {
		case 0:
			return "Entities with this attributes have data memories.";
		}
		return null;
	}
}
