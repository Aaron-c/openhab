/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.winkhub;

import org.openhab.core.binding.BindingProvider;

/**
 * @author AaronC
 * @since 1.7.0-SNAPSHOT
 */
public interface WinkHubBindingProvider extends BindingProvider {

	String getItemName(String itemName);

}
