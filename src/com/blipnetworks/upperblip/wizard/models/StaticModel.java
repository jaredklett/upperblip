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
 * $Id: StaticModel.java,v 1.2 2009/06/22 21:22:00 jklett Exp $
 */

package com.blipnetworks.upperblip.wizard.models;

import	com.blipnetworks.upperblip.wizard.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Collections;

/**
 * This class provides the basis for a simple linear wizard model. Steps are
 * added by calling the {@link #add} method and are traversed in the order of
 * addition.
 */
public class StaticModel extends AbstractWizardModel implements OverviewProvider {

	private ArrayList<WizardStep> steps = new ArrayList<WizardStep>();

	private int 				currentStep = 0;
	private StaticModelOverview overviewComponent;
	private boolean				overridePreviousState = false;

	public StaticModel() {
	}

	/**
	 * Resest this model. This method rewinds to the first step in the wizard.
	 */
	public void reset() {
		currentStep = 0;
		setActiveStep(steps.get(currentStep));
	}

	public void nextStep() {
		if (currentStep >= steps.size() - 1) {
			throw new IllegalStateException("Already on last step");
		}

		currentStep++;
		setActiveStep(steps.get(currentStep));
	}

	public void previousStep() {
		if (currentStep == 0) {
			throw new IllegalStateException("Already at first step");
		}

		currentStep--;
		setActiveStep(steps.get(currentStep));
	}

	public void lastStep() {
		currentStep = steps.size() - 1;
		setActiveStep(steps.get(currentStep));
	}

	public boolean isLastStep(WizardStep step) {
		return steps.indexOf(step) == steps.size() - 1;
	}

	public Iterator<WizardStep> stepIterator() {
		return Collections.unmodifiableList(steps).iterator();
	}

	public ArrayList<WizardStep> getSteps() {
		return steps;
	}
	
	/**
	 * Adds a step to the end of the wizard.
	 * 
	 * @param step
	 *            the {@link WizardStep} to add.
	 */
	public void add(WizardStep step) {
		steps.add(step);
		addCompleteListener(step);
	}

	/**
	 * This method is invoked after the current step has been changed to update
	 * the state of the model.
	 */
	@Override
	public void refreshModelState() {
		setNextAvailable(getActiveStep().isComplete() && !isLastStep(getActiveStep()));
		if (!overridePreviousState) {
			setPreviousAvailable(currentStep > 0);
		}
		setLastAvailable(allStepsComplete() && !isLastStep(getActiveStep()));
		setCancelAvailable(true);
	}

	/**
	 * Returns true if all the steps in the wizard return <tt>true</tt> from
	 * {@link WizardStep#isComplete}. This is primarily used to determine if the
	 * last button can be enabled.
	 * 
	 * @return <tt>true</tt> if all the steps in the wizard are complete,
	 *         <tt>false</tt> otherwise.
	 */
	public boolean allStepsComplete() {
		
		for (Iterator<WizardStep> iterator = steps.iterator(); iterator.hasNext();) {
			if (!(iterator.next()).isComplete())
				return false;
		}

		return true;
	}

	/**
	 * Returns an JComponent that will serve as an overview for this wizard. The
	 * overview can be disabled by calling {@link Wizard#setOverviewVisible}
	 * with a value of <tt>false</tt>.
	 * 
	 * @return a component that provides an overview of this wizard and its
	 *         current state.
	 */
	public JComponent getOverviewComponent() {
		if (overviewComponent == null) {
			overviewComponent = new StaticModelOverview(this);
		}

		return overviewComponent;
	}
	
	public void setOverridePreviousState(boolean state) {
		overridePreviousState = state;
	}
	
}
