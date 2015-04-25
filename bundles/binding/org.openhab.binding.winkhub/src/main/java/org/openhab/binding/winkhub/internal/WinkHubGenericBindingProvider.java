/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.winkhub.internal;

import org.openhab.binding.winkhub.WinkHubBindingProvider;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.core.library.items.DimmerItem;
import org.openhab.core.library.items.SwitchItem;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class is responsible for parsing the binding configuration.
 * 
 * @author AaronC
 * @since 1.7.0-SNAPSHOT
 */
public class WinkHubGenericBindingProvider extends AbstractGenericBindingProvider implements WinkHubBindingProvider {

	static final Logger logger = LoggerFactory
			.getLogger(WinkHubGenericBindingProvider.class);

	/**
	 * {@inheritDoc}
	 */
	public String getBindingType() {
		return "winkhub";
	}

	/**
	 * @{inheritDoc}
	 * currently the wink hub binding only supports SwitchItems, more to follow
	 */
	@Override
	public void validateItemType(Item item, String bindingConfig) throws BindingConfigParseException {
		//logger.debug("Checking for itemname of {} ", item.getName());
		if (!(item instanceof SwitchItem || item instanceof DimmerItem)) {
			throw new BindingConfigParseException("item '" + item.getName()
					+ "' is of type '" + item.getClass().getSimpleName()
					+ "', only Switch- and DimmerItems are allowed - please check your *.items configuration");
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void processBindingConfiguration(String context, Item item, String bindingConfig) throws BindingConfigParseException {
		super.processBindingConfiguration(context, item, bindingConfig);
		WinkHubBindingConfig config = new WinkHubBindingConfig();
		if (bindingConfig.length() == 0) {
			config.item_name = item.getName();	
		} else {
			config.item_name = bindingConfig;
		}
		//config.hub = new winkHub();
		//parse bindingconfig here ...
		addBindingConfig(item, config);		
	}
	
	public String getItemName(String itemName) {
		WinkHubBindingConfig config = (WinkHubBindingConfig) bindingConfigs.get(itemName);
		return config != null ? config.item_name : null;
	}
	
	/**
	 * This is a helper class holding binding specific configuration details
	 * 
	 * @author AaronC
	 * @since 1.7.0-SNAPSHOT
	 */
	class WinkHubBindingConfig implements BindingConfig {
		// put member fields here which holds the parsed values
		String item_name;
	}
	
	
}
