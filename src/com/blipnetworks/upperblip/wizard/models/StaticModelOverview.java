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
 * $Id: StaticModelOverview.java,v 1.2 2009/06/22 21:22:00 jklett Exp $
 */

package com.blipnetworks.upperblip.wizard.models;

import	com.blipnetworks.upperblip.wizard.*;

import javax.swing.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.awt.*;
import java.util.HashMap;

/**
 * This class provides an overview panel for instances of {@link StaticModel}.
 */
@SuppressWarnings("serial")
public class StaticModelOverview extends JPanel implements PropertyChangeListener {
	
	private StaticModel 				model;
	private HashMap<WizardStep, JLabel> labels = new HashMap<WizardStep, JLabel>();

	public StaticModelOverview(StaticModel model) {
		this.model = model;
		this.model.addPropertyChangeListener(this);
		setBackground(Color.WHITE);
		setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		JLabel title = new JLabel(I18n.getString("wizard.StaticModelOverview.title"));
		title.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));

		title.setAlignmentX(0);
		title.setMaximumSize(new Dimension(Integer.MAX_VALUE, title
				.getMaximumSize().height));
		add(title);
		int i = 1;
		for (WizardStep step : this.model.getSteps()) {
			JLabel label = new JLabel("" + i++ + ". " + step.getName());
			label.setBackground(new Color(240, 240, 240));
			label.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
			label.setAlignmentX(0);
			label.setMaximumSize(new Dimension(Integer.MAX_VALUE, label
					.getMaximumSize().height));
			add(label);
			labels.put(step, label);
		}

		add(Box.createGlue());
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("activeStep")) {
			JLabel old = (JLabel) labels.get(evt.getOldValue());
			if (old != null)
				formatInactive(old);

			JLabel label = (JLabel) labels.get(evt.getNewValue());
			formatActive(label);
			repaint();
		}
	}

	protected void formatActive(JLabel label) {
		label.setOpaque(true);
	}

	protected void formatInactive(JLabel label) {
		label.setOpaque(false);
	}
}
