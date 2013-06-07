package wmas.gui.world;

import wmas.xml.XMLEntity;

public interface WorldAttributeFactory {
	int getNbWorldAttribute();

	String getWorldAttributeName(int id);

	XMLEntity createWorldAttribute(int id);

	WorldAttributeEditor getWorldAttributeView(int id);

	String getWorldAttributeDescription(int id);
}
