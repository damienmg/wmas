package wmas.gui.run;

import java.awt.Component;

import wmas.world.EntityInterface;

public interface EntityAttributeView {
	public Component getComponent();

	public void setEntity(EntityInterface e);

	public String getViewName();
}
