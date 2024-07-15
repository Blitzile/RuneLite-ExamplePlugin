package com.example;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("collectionlog")
public interface CollectionLogConfig extends Config
{
	@ConfigItem(
			keyName = "hiddenItems",
			name = "Hidden Items",
			description = "Comma-separated list of items to hide in the Collection Log",
			position = 1
	)
	default String hiddenItems()
	{
		return "";
	}
}
