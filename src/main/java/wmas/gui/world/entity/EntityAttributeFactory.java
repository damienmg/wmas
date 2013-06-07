package wmas.gui.world.entity;

import java.awt.Component;

import wmas.gui.GuiModificationListener;
import wmas.gui.run.EntityAttributeView;
import wmas.xml.XMLEntity;

public interface EntityAttributeFactory {

	int getNbAttribute();

	String getAttributeName(int id);

	XMLEntity createAttribute(int id);

	Component getAttributeEditor(XMLEntity root, int id, XMLEntity e,
			GuiModificationListener listener);

	EntityAttributeView getAttributeViewer(int id);

	String getAttributeDescription(int id);
}
