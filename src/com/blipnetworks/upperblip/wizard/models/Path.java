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
 * $Id: Path.java,v 1.2 2009/06/22 21:22:00 jklett Exp $
 */

package com.blipnetworks.upperblip.wizard.models;

import	com.blipnetworks.upperblip.wizard.*;
import java.util.ArrayList;

/**
 * Paths represent a sequence of {@link WizardStep}s.
 */
public abstract class Path {
	private ArrayList<WizardStep> steps = new ArrayList<WizardStep>();

	protected Path() {
	}

	/**
	 * Gets the path that will follow this one.
	 * 
	 * @return the next path.
	 */
	protected abstract Path getNextPath(MultiPathModel model);

	/**
	 * Adds a wizard step to this path. Paths must contain at least one step,
	 * and the steps will be traversed in the order they are added.
	 * 
	 * @param step
	 *            the next {@link WizardStep} in the path.
	 */
	public void addStep(WizardStep step) {
		steps.add(step);
	}

	public WizardStep firstStep() {
		return (WizardStep) steps.get(0);
	}

	public WizardStep nextStep(WizardStep currentStep) {
		int index = steps.indexOf(currentStep);
		return (WizardStep) steps.get(index + 1);
	}

	public WizardStep previousStep(WizardStep currentStep) {
		int index = steps.indexOf(currentStep);
		return (WizardStep) steps.get(index - 1);
	}

	public WizardStep lastStep() {
		return (WizardStep) steps.get(steps.size() - 1);
	}

	/**
	 * Checks if the specified step is the first step in the path.
	 * 
	 * @param step
	 *            the step to check
	 * @return <tt>true</tt> if the step is the first in the path,
	 *         <tt>false</tt> otherwise.
	 */
	public boolean isFirstStep(WizardStep step) {
		return steps.indexOf(step) == 0;
	}

	/**
	 * Checks if the specified step is the last step in the path.
	 * 
	 * @param step
	 *            the step to check
	 * @return <tt>true</tt> if the step is the last in the path, <tt>false</tt>
	 *         otherwise.
	 */
	public boolean isLastStep(WizardStep step) {
		boolean lastStep = steps.lastIndexOf(step) == steps.size() - 1;
		return lastStep;
	}

	public ArrayList<WizardStep> getSteps() {
		return steps;
	}

	public boolean contains(WizardStep step) {
		return steps.contains(step);
	}

	public abstract void acceptVisitor(PathVisitor visitor);
}
