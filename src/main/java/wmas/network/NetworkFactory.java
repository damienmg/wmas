package wmas.network;

import java.awt.Component;

import wmas.gui.GuiModificationListener;
import wmas.gui.run.EntityAttributeView;
import wmas.gui.world.WorldAttributeEditor;
import wmas.gui.world.WorldAttributeFactory;
import wmas.gui.world.WorldEditor;
import wmas.gui.world.entity.EntityAttributeFactory;
import wmas.gui.world.entity.EntityEditor;
import wmas.network.gui.NetworkWorldAttributeEditor;
import wmas.network.gui.NetworksAttributeEditor;
import wmas.reports.ReportDescription;
import wmas.world.Model;
import wmas.world.World;
import wmas.xml.XMLEntity;

public class NetworkFactory implements EntityAttributeFactory,
		WorldAttributeFactory {
	private NetworksAttributeEditor attrEditor = null;

	public void refreshAttrEditor() {
		if (attrEditor != null)
			attrEditor.refresh();
	}

	@Override
	public XMLEntity createAttribute(int id) {
		return new NetworksAttribute();
	}

	@Override
	public String getAttributeDescription(int id) {
		return "Enable networking to this entity";
	}

	@Override
	public String getAttributeName(int id) {
		return "networked";
	}

	@Override
	public int getNbAttribute() {
		return 1;
	}

	@Override
	public XMLEntity createWorldAttribute(int id) {
		return new NetworksDescription();
	}

	@Override
	public int getNbWorldAttribute() {
		return 1;
	}

	@Override
	public String getWorldAttributeDescription(int id) {
		return "Enables networking";
	}

	@Override
	public String getWorldAttributeName(int id) {
		return "network";
	}

	@Override
	public WorldAttributeEditor getWorldAttributeView(int id) {
		return new NetworkWorldAttributeEditor(this);
	}

	@Override
	public Component getAttributeEditor(XMLEntity root, int id, XMLEntity e,
			GuiModificationListener listener) {
		if (e instanceof NetworksAttribute) {
			if (attrEditor == null)
				attrEditor = new NetworksAttributeEditor();
			NetworksDescription descr = null;
			if (root instanceof World) {
				if (((World) root).hasAttribute("network")) {
					descr = (NetworksDescription) ((World) root)
							.getAttribute("network");
				}
			} else if (root instanceof Model) {
				if (((Model) root).hasAttribute("network")) {
					descr = (NetworksDescription) ((Model) root)
							.getAttribute("network");
				}
			}
			attrEditor.setParent(descr, (NetworksAttribute) e, listener);
			return attrEditor;
		}
		return null;
	}

	@Override
	public EntityAttributeView getAttributeViewer(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void registerAll() {
		NetworkFactory factory = new NetworkFactory();
		ReportDescription.registerDataReport("network", true);
		ReportDescription.registerEventReport("received");
		ReportDescription.registerEventReport("dropped");

		NetworkFunctions.registerAll();
		EntityEditor.registerAttributeFactory(factory);
		WorldEditor.registerAttributeFactory(factory);
		World.registerGlobalBehaviour("network", new Network());
	}
}
