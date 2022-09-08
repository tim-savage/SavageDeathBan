/*
 * Copyright (c) 2022 Tim Savage.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.winterhavenmc.deathban;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.bukkit.configuration.Configuration;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.Set;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SavageDeathBanTests {


	private ServerMock server;
	private PluginMain plugin;


	@BeforeAll
	void setUp() {
		// Start the mock server
		server = MockBukkit.mock();
		// Load your plugin
		plugin = MockBukkit.load(PluginMain.class);
	}

	@AfterAll
	void tearDown() {
		// Stop the mock server
		MockBukkit.unmock();
	}


	@Nested
	@DisplayName("Test mock objects.")
	class MockingTests {

		@Test
		@DisplayName("mock server not null.")
		void mockServerNotNull() {
			Assertions.assertNotNull(server);
		}

		@Test
		@DisplayName("mock plugin not null.")
		void mockPluginNotNull() {
			Assertions.assertNotNull(plugin);
		}
	}


	@Nested
	@DisplayName("Test plugin main objects.")
	class PluginTests {
		@Test
		@DisplayName("language manager not null.")
		void languageManagerNotNull() {
			Assertions.assertNotNull(plugin.messageBuilder);
		}

		@Test
		@DisplayName("world manager not null.")
		void worldManagerNotNull() {
			Assertions.assertNotNull(plugin.worldManager);
		}

		@Test
		@DisplayName("sound config not null.")
		void soundConfigNotNull() {
			Assertions.assertNotNull(plugin.soundConfig);
		}
	}


	@Nested
	@DisplayName("Test plugin config.")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class ConfigTests {

		final Configuration config = plugin.getConfig();
		final Set<String> enumConfigKeyStrings = new HashSet<>();

		public ConfigTests() {
			for (ConfigSetting configSetting : ConfigSetting.values()) {
				this.enumConfigKeyStrings.add(configSetting.getKey());
			}
		}

		@Test
		@DisplayName("config not null.")
		void configNotNull() {
			Assertions.assertNotNull(config);
		}

		@Test
		@DisplayName("test configured language.")
		void getLanguage() {
			Assertions.assertEquals("en-US", config.getString("language"),
					"configured language does not match en-US");
		}

		@SuppressWarnings("unused")
		Set<String> configFileKeys() {
			return plugin.getConfig().getKeys(false);
		}

		@ParameterizedTest
		@DisplayName("file config key is contained in ConfigSetting enum.")
		@MethodSource("configFileKeys")
		void configFileKeyNotNull(String key) {
			Assertions.assertNotNull(key);
			Assertions.assertTrue(enumConfigKeyStrings.contains(key),
					"file config key is not contained in ConfigSetting enum.");
		}

		@ParameterizedTest
		@EnumSource(ConfigSetting.class)
		@DisplayName("ConfigSetting enum matches config file key/value pairs.")
		void configFileKeysContainsEnumKey(ConfigSetting configSetting) {
			Assertions.assertEquals(configSetting.getValue(), plugin.getConfig().getString(configSetting.getKey()));
		}
	}

}
