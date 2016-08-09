package com.bladecoder.ink.runtime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.bladecoder.ink.runtime.Path.Component;

/**
 * Base class for all ink runtime content.
 */
/* TODO: abstract */
public class RTObject {
	/**
	 * Runtime.RTObjects can be included in the main Story as a hierarchy.
	 * Usually parents are Container RTObjects. (TODO: Always?) The parent.
	 */
	private RTObject parent;

	private Path path;

	public RTObject() {
	}

	// TODO: Come up with some clever solution for not having
	// to have debug metadata on the RTObject itself, perhaps
	// for serialisation purposes at least.
	DebugMetadata debugMetadata;

	public RTObject getParent() {
		return parent;
	}

	public void setParent(RTObject value) {
		parent = value;
	}

	public DebugMetadata getDebugMetadata() throws Exception {
		if (debugMetadata == null) {
			if (getParent() != null) {
				return getParent().getDebugMetadata();
			}
		}

		return debugMetadata;
	}

	public void setDebugMetadata(DebugMetadata value) throws Exception {
		debugMetadata = value;
	}

	public Integer debugLineNumberOfPath(Path path) throws Exception {
		// FIXME Added path.isRelative() because orginal code not working
		if (path == null || path.isRelative())
			return null;

		// Try to get a line number from debug metadata
		Container root = this.getRootContentContainer();

		if (root != null) {
			RTObject targetContent = root.contentAtPath(path);
			if (targetContent != null) {
				DebugMetadata dm = targetContent.debugMetadata;
				if (dm != null) {
					return dm.startLineNumber;
				}

			}

		}

		return null;
	}

	public Path getPath() {
		if (path == null) {
			if (getParent() == null) {
				path = new Path();
			} else {
				List<Path.Component> comps = new ArrayList<Path.Component>();
				RTObject child = this;
				Container container = child.getParent() instanceof Container ? (Container) child.getParent()
						: (Container) null;
				while (container != null) {
					INamedContent namedChild = child instanceof INamedContent ? (INamedContent) child
							: (INamedContent) null;
					if (namedChild != null && namedChild.hasValidName()) {
						comps.add(new Path.Component(namedChild.getName()));
					} else {
						comps.add(new Component(container.getContent().indexOf(child)));
					}
					child = container;
					container = container.getParent() instanceof Container ? (Container) container.getParent()
							: (Container) null;
				}

				// Reverse list because components are searched in reverse
				// order.
				Collections.reverse(comps);

				path = new Path(comps);
			}
		}

		return path;
	}

	public RTObject resolvePath(Path path) throws Exception {
		if (path.isRelative()) {
			Container nearestContainer = this instanceof Container ? (Container) this : (Container) null;

			if (nearestContainer == null) {
				// Debug.Assert(this.getparent() != null, "Can't resolve
				// relative path because we don't have a parent");
				nearestContainer = this.getParent() instanceof Container ? (Container) this.getParent()
						: (Container) null;
				// Debug.Assert(nearestContainer != null, "Expected parent to be
				// a container");
				// Debug.Assert(path.getcomponents()[0].isParent);
				path = path.getTail();
			}

			return nearestContainer.contentAtPath(path);
		} else {
			return this.getRootContentContainer().contentAtPath(path);
		}
	}

	public Path convertPathToRelative(Path globalPath) {
		// 1. Find last shared ancestor
		// 2. Drill up using ".." style (actually represented as "^")
		// 3. Re-build downward chain from common ancestor
		Path ownPath = this.getPath();
		int minPathLength = Math.min(globalPath.getComponents().size(), ownPath.getComponents().size());
		int lastSharedPathCompIndex = -1;
		for (int i = 0; i < minPathLength; ++i) {
			Component ownComp = ownPath.getComponents().get(i);
			Component otherComp = globalPath.getComponents().get(i);

			if (ownComp.equals(otherComp)) {
				lastSharedPathCompIndex = i;
			} else {
				break;
			}
		}
		// No shared path components, so just use global path
		if (lastSharedPathCompIndex == -1)
			return globalPath;

		int numUpwardsMoves = (ownPath.getComponents().size() - 1) - lastSharedPathCompIndex;
		ArrayList<Component> newPathComps = new ArrayList<com.bladecoder.ink.runtime.Path.Component>();
		for (int up = 0; up < numUpwardsMoves; ++up)
			newPathComps.add(com.bladecoder.ink.runtime.Path.Component.toParent());
		for (int down = lastSharedPathCompIndex + 1; down < globalPath.getComponents().size(); ++down)
			newPathComps.add(globalPath.getComponents().get(down));
		Path relativePath = new Path(newPathComps, true);
		return relativePath;
	}

	// Find most compact representation for a path, whether relative or global
	public String compactPathString(Path otherPath) {
		String globalPathStr = null;
		String relativePathStr = null;
		if (otherPath.isRelative()) {
			relativePathStr = otherPath.getComponentsString();
			globalPathStr = this.getPath().pathByAppendingPath(otherPath).getComponentsString();
		} else {
			Path relativePath = convertPathToRelative(otherPath);
			relativePathStr = relativePath.getComponentsString();
			globalPathStr = otherPath.getComponentsString();
		}
		if (relativePathStr.length() < globalPathStr.length())
			return relativePathStr;
		else
			return globalPathStr;
	}

	public Container getRootContentContainer() {
		RTObject ancestor = this;
		while (ancestor.getParent() != null) {
			ancestor = ancestor.getParent();
		}
		return ancestor instanceof Container ? (Container) ancestor : (Container) null;
	}

	public RTObject copy() throws Exception {
		throw new UnsupportedOperationException(this.getClass().getTypeName() + " doesn't support copying");
	}
}
