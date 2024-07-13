package com.example;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
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
	@Inject
	private Client client;

	@Inject
	private ExampleConfig config;

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

		client.createMenuEntry(1)
				.setOption(COLLECTION_LOG_EXPORT)
				.setTarget(entryTarget)
				.setType(MenuAction.RUNELITE)
				.onClick(e -> {
					boolean collectionLogSaved = collectionLogManager.saveCollectionLogFile(true);
					if (collectionLogSaved)
					{
						String filePath = collectionLogManager.getExportFilePath();
						String message = "Collection log exported to " + filePath;

						if (config.sendExportChatMessage())
						{
							String chatMessage = new ChatMessageBuilder()
									.append(ChatColorType.HIGHLIGHT)
									.append(message)
									.build();

							chatMessageManager.queue(
									QueuedMessage.builder()
											.type(ChatMessageType.CONSOLE)
											.runeLiteFormattedMessage(chatMessage)
											.build()
							);
						}
					}
				});
	}

	@Provides
	ExampleConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ExampleConfig.class);
	}
}
