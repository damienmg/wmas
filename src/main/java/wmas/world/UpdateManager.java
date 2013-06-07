package wmas.world;

import java.awt.Component;

import wmas.gui.shapes.SchemeView;

public interface UpdateManager {
	public void update();

	public void terminated();

	public void reset();

	public void prepareReset();

	public SchemeView getWorldView();

	public void addWorldButton(Component comp);
}
