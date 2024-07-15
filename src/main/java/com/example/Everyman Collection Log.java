package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.MenuEntry;
import net.runelite.api.ScriptID;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuOpened;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;


@Slf4j
@PluginDescriptor(
	name = "Example"
)
public class ExamplePlugin extends Plugin
{
	private static final String COLLECTION_LOG_TARGET = "Collection log";
	private static final int ADVENTURE_LOG_COLLECTION_LOG_SELECTED_VARBIT_ID = 12061;


	@Inject
	private Client client;

	@Inject
	private ExampleConfig config;

	@Inject
	private ClientThread clientThread;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Example started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Example stopped!");
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Example says " + config.greeting(), null);
		}
	}

	@Subscribe
	public void onMenuOpened(MenuOpened event)
	{
		if (event.getMenuEntries().length < 2)
		{
			return;
		}

		MenuEntry entry = event.getMenuEntries()[1];

		String entryTarget = entry.getTarget();
		if (entryTarget.isEmpty())
		{
			entryTarget = entry.getOption();
		}

		if (!entryTarget.toLowerCase().endsWith(COLLECTION_LOG_TARGET.toLowerCase()))
		{
			return;
		}

		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "You opened the collection log!", null);
		
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired scriptPostFired)
	{
		if (scriptPostFired.getScriptId() == ScriptID.COLLECTION_DRAW_LIST)
		{
			clientThread.invokeLater(this::getPage);
		}
	}

	/**
	 * Load the current page being viewed in the collection log
	 * and get/update relevant information contained in the page
	 */
	@Subscribe
	private void getPage()
	{
		Widget pageHead = client.getWidget(ComponentID.COLLECTION_LOG_ENTRY_HEADER);
		if (pageHead == null)
		{
			return;
		}

		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Hello from getPage()!", null);
	}

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}
}

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



@PluginDescriptor(
		name = "Collection Log Hider"
)
public class CollectionLogHiderPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private CollectionLogConfig config;

	@Inject
	private ClientThread clientThread;

	private List<String> hiddenItemsList;

	@Override
	protected void startUp() throws Exception
	{
		updateHiddenItemsList();
	}

	@Override
	protected void shutDown() throws Exception
	{
		// Clean up any changes made during startup
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGGED_IN)
		{
			updateHiddenItemsList();
		}
	}

	@Subscribe
	public void onScriptPostFired(ScriptPostFired event)
	{
		if (event.getScriptId() == ScriptID.COLLECTION_DRAW_LIST)
		{
			clientThread.invokeLater(this::filterCollectionLogItems);
		}
	}

	private void updateHiddenItemsList()
	{
		String hiddenItems = config.hiddenItems();
		hiddenItemsList = Arrays.asList(hiddenItems.split(","))
				.stream()
				.map(String::trim)
				.collect(Collectors.toList());
	}

	private void filterCollectionLogItems()
	{
		Widget collectionLogContainer = client.getWidget(ComponentID.COLLECTION_LOG_CONTAINER);
		if (collectionLogContainer == null)
		{
			return;
		}

		for (Widget item : collectionLogContainer.getDynamicChildren())
		{
			if (hiddenItemsList.contains(Text.removeTags(item.getText())))
			{
				item.setHidden(true);
			}
		}
	}
}


