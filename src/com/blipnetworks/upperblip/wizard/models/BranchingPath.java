/**
 * Wizard Framework
 * Copyright 2004 - 2005 Andrew Pietsch
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * $Id: BranchingPath.java,v 1.2 2009/06/22 21:22:00 jklett Exp $
 */

package com.blipnetworks.upperblip.wizard.models;

import	com.blipnetworks.upperblip.wizard.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * BranchingPaths represent a sequence of {@link WizardStep}s that has multiple
 * choices for the next path to traverse.
 * 
 * @see #addBranch
 * @see #addStep
 */
public class BranchingPath extends Path {
	
	private HashMap<Condition, Path> paths = new HashMap<Condition, Path>();

	/**
	 * Creates a new empth BranchingPath.
	 */
	public BranchingPath() {
	}

	/**
	 * Creates a new BranchingPath that is initialized with the specified step.
	 * 
	 * @param step
	 *            the first step of the path.
	 */
	public BranchingPath(WizardStep step) {
		addStep(step);
	}

	/**
	 * Gets the path to traverse after this path has exhausted all its steps.
	 * This method will call iterate over each path selector to determine the
	 * path to return.
	 * 
	 * @return the next path in the sequence.
	 * @throws IllegalStateException
	 *             if no matching path is found.
	 */
	protected Path getNextPath(MultiPathModel model) {
		for (Iterator<Map.Entry<Condition, Path>> iter = paths.entrySet().iterator(); iter.hasNext();) {
			Map.Entry<Condition, Path> entry = iter.next();
			Condition condition = entry.getKey();
			if (condition.evaluate(model))
				return entry.getValue();
		}

		throw new IllegalStateException("No next path selected");
	}

	/**
	 * Adds a possible branch from this path.
	 * 
	 * @param path
	 *            the {@link Path} to traverse based when the condition returns
	 *            <tt>true</tt>.
	 * @param condition
	 *            a {@link Condition} that activates this path.
	 */
	public void addBranch(Path path, Condition condition) {
		paths.put(condition, path);
	}

	public void acceptVisitor(PathVisitor visitor) {
		visitor.visitPath(this);
	}

	public void visitBranches(PathVisitor visitor) {
		for (Iterator<Path> iter = paths.values().iterator(); iter.hasNext();) {
			iter.next().acceptVisitor(visitor);
		}
	}

}
