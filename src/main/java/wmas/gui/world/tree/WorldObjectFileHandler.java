package wmas.gui.world.tree;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.event.TreeModelEvent;
import javax.swing.tree.TreePath;

import wmas.behaviour.Behaviour;
import wmas.util.Doublet;
import wmas.util.Triplet;
import wmas.world.Entity;
import wmas.world.Model;
import wmas.world.World;
import wmas.xml.XMLEntity;
import wmas.xml.XMLInterpretor;

public class WorldObjectFileHandler implements Iterable<String> {
	private Map<String, XMLEntity> objectFiles = new HashMap<String, XMLEntity>();
	private Map<XMLEntity, String> fileObjects = new HashMap<XMLEntity, String>();

	File rootDirectory = null;

	private HashSet<String> modifiedFiles = new HashSet<String>();

	private WorldObjectTreeModel treeModel = null;

	public WorldObjectFileHandler(WorldObjectTreeModel treeModel) {
		this.treeModel = treeModel;
	}

	public boolean hasModified() {
		return !modifiedFiles.isEmpty();
	}

	public Set<String> getModified() {
		return modifiedFiles;
	}

	public boolean isObjectOfFile(TreePath o, String file) {
		while (o.getPathCount() > 1) {
			if (o.getLastPathComponent() instanceof Doublet) {
				Object e = treeModel.getObject(o.getLastPathComponent());
				String s = fileObjects.get(e);
				if (s == null)
					return false;
				return modifiedFiles.contains(s);
			}
			o = o.getParentPath();
		}
		if (o.getPathCount() == 1) {
			return o.getLastPathComponent() == treeModel.getRoot()
					&& modifiedFiles.contains(null);
		}
		return false;
	}

	public boolean isModified(TreePath p) {
		if (p == null)
			return false;
		for (String orig : modifiedFiles) {
			if (isObjectOfFile(p, orig))
				return true;
		}
		return false;
	}

	public boolean isModified(Object o) {
		return isModified(treeModel.searchParent(treeModel.search(o)));
	}

	public boolean isModified(Object[] o) {
		return isModified(treeModel.searchParent(treeModel.searchFirst(o)));
	}

	@SuppressWarnings("unchecked")
	private String getFileOfPath(TreePath p) {
		Object o = p.getLastPathComponent();
		if (o instanceof Doublet)
			o = treeModel.getObject(((Doublet<World, String>) o));
		if (o == null || !(o instanceof XMLEntity))
			return "";

		if (o == treeModel.getRoot()) {
			return null;
		} else {
			String f = fileObjects.get((XMLEntity) o);
			if (f == null)
				return "";
			return f;
		}
	}

	String getFileOfObject(Object o) {
		return getFileOfPath(treeModel.searchParent(treeModel.search(o)));
	}

	private void setModified(String s, boolean modified) {
		if (s != null) {
			// Not Root
			if (s.length() == 0)
				return; // wrong file
			else if (!objectFiles.containsKey(s))
				return; // wrong file
		}
		if (modified) {
			modifiedFiles.add(s);
		} else
			modifiedFiles.remove(s);
	}

	public void setModified(Object o, boolean modified) {
		TreePath path = treeModel.searchParent(treeModel.search(o));
		setModified(getFileOfPath(path), modified);
		treeModel
				.treeStructureChanged(new TreeModelEvent(this, path.getPath()));
	}

	public void setModified(Object[] p, boolean modified) {
		TreePath path = treeModel.searchParent(treeModel.searchFirst(p));
		setModified(getFileOfPath(path), modified);
		treeModel
				.treeStructureChanged(new TreeModelEvent(this, path.getPath()));
	}

	public void reset(File f) {
		if (f == null)
			rootDirectory = null;
		else
			rootDirectory = f.getParentFile();
		modifiedFiles.clear();
		objectFiles.clear();
		fileObjects.clear();
	}

	public void reset() {
		rootDirectory = null;
		modifiedFiles.clear();
		objectFiles.clear();
		fileObjects.clear();
	}

	private static Triplet<File[], Integer, Integer> lookupCommonAncestor(
			File f1, File f2) {
		// Get the path
		Vector<File> path1 = new Vector<File>();
		Vector<File> path2 = new Vector<File>();
		File f = f1;
		while (f != null) {
			path1.add(0, f);
			f = f.getParentFile();
		}
		f = f2;
		while (f != null) {
			path2.add(0, f);
			f = f.getParentFile();
		}
		// Go up until path is different
		File[] arr = new File[path2.size()];
		path2.copyInto(arr);
		for (int i = 0; i < path1.size() && i < path2.size(); i++) {
			if (!(path1.get(i).equals(path2.get(i)))) {
				return new Triplet<File[], Integer, Integer>(arr, path1.size()
						- i, path2.size() - i);
			}
		}
		if (path1.size() == path2.size()) {
			// Path are equals
			return null;
		}
		int i = Math.min(path1.size(), path2.size());
		return new Triplet<File[], Integer, Integer>(arr, path1.size() - i,
				path2.size() - i);
	}

	private static String getRelativePath(File absolutePath, File rootDirectory) {
		if (rootDirectory == null)
			return absolutePath.getAbsolutePath();
		Triplet<File[], Integer, Integer> f = lookupCommonAncestor(
				rootDirectory, absolutePath.getParentFile());
		if (f == null)
			return absolutePath.getName();
		String r = "";
		for (int i = 0; i < f.getSecond(); i++)
			r += ".." + File.separator;
		for (int i = f.getFirst().length - f.getThird(); i < f.getFirst().length; i++) {
			r += f.getFirst()[i].getName() + File.separator;
		}
		return r + absolutePath.getName();
	}

	private static File getAbsolutePath(String relativePath, File f) {
		if (f == null)
			return new File(relativePath);
		String[] res = relativePath.split("\\" + File.separator);
		for (int i = 0; i < res.length; i++) {
			if (res[i].equals(""))
				return new File(relativePath); // Absolute path
			if (res[i].equals("..")) {
				f = f.getParentFile();
			} else {
				f = new File(f.getAbsolutePath() + File.separator + res[i]);
			}
			if (f == null)
				return new File(relativePath);
		}
		return f;
	}

	void convertPath(File newDir) {
		if (treeModel.getRoot() != null && treeModel.getRoot() instanceof World) {
			World world = (World) treeModel.getRoot();
			HashMap<String, Object> map = new HashMap<String, Object>();
			for (String s : world.getModels().keySet()) {
				map.put(getRelativePath(getAbsolutePath(s, rootDirectory),
						newDir), world.getModels().get(s));
			}
			world.getModels().clear();
			world.getModels().putAll(map);
			rootDirectory = newDir;
			Object[] v = new Object[1];
			v[0] = world;
			treeModel.treeStructureChanged(new TreeModelEvent(this, v));
		}
	}

	public XMLEntity get(String f) {
		return objectFiles.get(f);
	}

	public XMLEntity loadFile(File f, World w) throws Exception {
		XMLEntity xe = objectFiles.containsKey(f.getCanonicalPath()) ? objectFiles
				.get(f.getCanonicalPath()) : XMLInterpretor.convert(
				f.getCanonicalPath(), null);
		if (xe != null) {
			objectFiles.put(f.getCanonicalPath(), xe);
			fileObjects.put(xe, f.getCanonicalPath());
			w.getModels().put(
					getRelativePath(f, w == treeModel.getRoot() ? rootDirectory
							: w.getWorldDirectory()), xe);
		}
		return xe;
	}

	public XMLEntity loadModelFile(World parent, File f) throws Exception {
		XMLEntity xe = XMLInterpretor.convert(f.getCanonicalPath(), null);
		if (xe instanceof World || xe instanceof Entity
				|| xe instanceof Behaviour || xe instanceof Model) {
			parent.getModels().put(
					getRelativePath(f,
							parent == treeModel.getRoot() ? rootDirectory
									: parent.getWorldDirectory()), xe);
			fileObjects.put(xe, f.getCanonicalPath());
			objectFiles.put(f.getCanonicalPath(), xe);
			return xe;
		}
		return null;
	}

	Object getModelObject(Doublet<World, String> e) {
		if (e.getFirst() == null || e.getSecond() == null)
			return null;
		World parent = e.getFirst();
		if (!parent.getModels().containsKey(e.getSecond()))
			return null;
		if (parent.getModels().get(e.getSecond()) != null)
			return parent.getModels().get(e.getSecond());
		File worldDir = parent.getWorldDirectory();
		if (worldDir == null)
			worldDir = rootDirectory;
		String path = e.getSecond();
		File f = getAbsolutePath(path, worldDir);
		try {
			if (f != null && f.exists() && f.isFile())
				path = f.getCanonicalPath();
			f = new File(path);
			XMLEntity xe = (objectFiles.containsKey(path)) ? objectFiles
					.get(path) : XMLInterpretor.convert(path, null);
			if (xe instanceof World) {
				((World) xe).setWorldDirectory(f.getParentFile());
			}
			parent.getModels().put(e.getSecond(), xe);
			objectFiles.put(path, xe);
			fileObjects.put(xe, path);
			return xe;
		} catch (Exception exn) {
			// Do nothing on failure
		}
		return null;
	}

	public void saved(File f) {
		if (f == null)
			modifiedFiles.remove(null);
		else
			modifiedFiles.remove(f.getAbsolutePath());
	}

	public void saved(String f) {
		if (f == null)
			modifiedFiles.remove(null);
		else
			modifiedFiles.remove(f);
	}

	public void remove(Object d) {
		objectFiles.remove(fileObjects.remove(d));
	}

	@Override
	public Iterator<String> iterator() {
		return modifiedFiles.iterator();
	}
}
