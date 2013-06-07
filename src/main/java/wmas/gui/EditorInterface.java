package wmas.gui;

import java.awt.Component;

import javax.swing.Icon;

public interface EditorInterface {
	public boolean canEditObject(Object o);

	public void setObject(Object o);

	public Component getEditor();

	public Icon getIcon();

	public String getTitle();

	public boolean isModified();

	public void refresh();
}
