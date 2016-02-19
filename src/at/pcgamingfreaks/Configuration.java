/*
* Copyright (C) 2014-2016 GeorgH93
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package at.pcgamingfreaks;

import at.pcgamingfreaks.yaml.YAML;
import at.pcgamingfreaks.yaml.YAMLKeyNotFoundException;
import at.pcgamingfreaks.yaml.YAMLNotInitializedException;
import com.google.common.io.ByteStreams;

import java.io.*;
import java.util.Set;
import java.util.logging.Logger;

public class Configuration
{
	protected final Logger logger; // The logger instance of the plugin
	protected YAML config = null;  // The yaml config instance of the configuration

	private final String CONFIG_PATH, IN_JAR_PREFIX;
	private final int CONFIG_VERSION, UPGRADE_THRESHOLD;
	private final File configFile, configBaseDir;

	/**
	 * @param logger  The logger instance of the plugin
	 * @param baseDir The base directory where the configs should be saved (normally plugin_instance.getDataFolder())
	 * @param version current version of the config
	 */
	public Configuration(Logger logger, File baseDir, int version)
	{
		this(logger, baseDir, version, -1, "config.yml");
	}

	/**
	 * @param logger  The logger instance of the plugin
	 * @param baseDir The base directory where the configs should be saved (normally plugin_instance.getDataFolder())
	 * @param version The current version of the config
	 * @param path    The name/path to a config not named "config.yml" or not placed in the plugins folders root
	 */
	public Configuration(Logger logger, File baseDir, int version, String path)
	{
		this(logger, baseDir, version, -1, path);
	}

	/**
	 * @param logger           The logger instance of the plugin
	 * @param baseDir          The base directory where the configs should be saved (normally plugin_instance.getDataFolder())
	 * @param version          The current version of the config
	 * @param upgradeThreshold Versions below this will be upgraded (settings copied into a new config file) instead of updated
	 */
	public Configuration(Logger logger, File baseDir, int version, int upgradeThreshold)
	{
		this(logger, baseDir, version, upgradeThreshold, "config.yml");
	}

	/**
	 * @param logger           The logger instance of the plugin
	 * @param baseDir          The base directory where the configs should be saved (normally plugin_instance.getDataFolder())
	 * @param version          The current version of the config
	 * @param upgradeThreshold Versions below this will be upgraded (settings copied into a new config file) instead of updated
	 * @param path             The name/path to a config not named "config.yml" or not placed in the plugins folders root
	 */
	public Configuration(Logger logger, File baseDir, int version, int upgradeThreshold, String path)
	{
		this(logger, baseDir, version, upgradeThreshold, path, "");
	}

	/**
	 * @param logger           The logger instance of the plugin
	 * @param baseDir          The base directory where the configs should be saved (normally plugin_instance.getDataFolder())
	 * @param version          The current version of the config
	 * @param upgradeThreshold Versions below this will be upgraded (settings copied into a new config file) instead of updated
	 * @param path             The name/path to a config not named "config.yml" or not placed in the plugins folders root
	 * @param inJarPrefix      The Prefix for the file in the jar (e.g. bungee_)
	 */
	public Configuration(Logger logger, File baseDir, int version, int upgradeThreshold, String path, String inJarPrefix)
	{
		this(logger, baseDir, version, upgradeThreshold, path, inJarPrefix, null);
	}

	private Configuration(Logger logger, File baseDir, int version, int upgradeThreshold, String path, String inJarPrefix, YAML oldConfig)
	{
		this.logger = logger;
		configBaseDir = baseDir;
		UPGRADE_THRESHOLD = upgradeThreshold;
		CONFIG_VERSION = version;
		CONFIG_PATH = path;
		IN_JAR_PREFIX = inJarPrefix;
		configFile = new File(baseDir, CONFIG_PATH);
		if(oldConfig == null)
		{
			loadConfig();
		}
		else
		{
			config = oldConfig;
		}
	}

	/**
	 * Checks if the config is loaded or not
	 *
	 * @return true if the config is loaded, false if not
	 */
	public boolean isLoaded()
	{
		return config != null;
	}

	/**
	 * Reloads the config file
	 */
	public void reload()
	{
		loadConfig();
	}

	/**
	 * Allows inheriting classes to implement own code for the config upgrade
	 * If no special code is implemented all keys will be copied 1:1 into the new config file
	 *
	 * @param oldConfiguration the old config file
	 */
	protected void doUpgrade(Configuration oldConfiguration)
	{
		logger.info("No custom config upgrade code implemented! Copying all data from old config to new one.");
		try
		{
			Set<String> keys = oldConfiguration.getConfig().getKeys();
			for(String key : keys)
			{
				if(key.equals("Version"))
					continue;
				config.set(key, oldConfiguration.getConfig().getString(key));
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Allows inheriting classes to implement code for the config update
	 */
	protected void doUpdate()
	{
		logger.info("No config update code implemented! Just updating version!");
	}

	/**
	 * Allows inheriting classes to implement code for setting config values in new created config files
	 *
	 * @return if values in the config have been changed
	 */
	protected boolean newConfigCreated()
	{
		return false;
	}

	/**
	 * Saves all changes in the configuration to the file.
	 *
	 * @throws FileNotFoundException If the file the config should be saved to does not exist.
	 */
	public void saveConfig() throws FileNotFoundException
	{
		try
		{
			config.save(configFile);
		}
		catch(YAMLNotInitializedException e) // It should not happen, but you never know
		{
			e.printStackTrace();
		}
	}

	private void loadConfig()
	{
		try
		{
			if(!configFile.exists())
			{
				logger.info("No config found. Create new one ...");
				if(!configBaseDir.exists() && !configBaseDir.mkdir())
				{
					logger.warning("Couldn't create directory. " + configBaseDir.toString());
				}
				try(InputStream is = getClass().getResourceAsStream("/" + IN_JAR_PREFIX + CONFIG_PATH); OutputStream os = new FileOutputStream(configFile))
				{
					ByteStreams.copy(is, os);
					os.flush();
				}
				logger.info("Configuration extracted successfully!");
				config = new YAML(configFile);
				if(newConfigCreated())
				{
					saveConfig();
				}
			}
			else
			{
				config = new YAML(configFile);
				updateConfig();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			config = null;
		}
	}

	private boolean updateConfig()
	{
		if(CONFIG_VERSION > getVersion())
		{
			if(UPGRADE_THRESHOLD > 0 && getVersion() < UPGRADE_THRESHOLD)
			{
				logger.info("Configuration Version: " + getVersion() + " => Configuration outdated! Upgrading ...");
				upgradeConfig();
			}
			else
			{
				logger.info("Configuration Version: " + getVersion() + " => Configuration outdated! Updating ...");
				doUpdate();
				config.set("Version", CONFIG_VERSION);
			}
			try
			{
				saveConfig();
				logger.info("Configuration has been updated.");
				return true;
			}
			catch(Exception e)
			{
				e.printStackTrace();
				config = null;
			}
		}
		if(CONFIG_VERSION < getVersion())
		{
			logger.info("Configuration File Version newer than expected!");
		}
		return false;
	}

	private void upgradeConfig()
	{
		try
		{
			int oldVersion = getVersion();
			File oldConfig = new File(configFile + ".old_v" + oldVersion);
			if(oldConfig.exists() && !oldConfig.delete())
			{
				logger.warning("Failed to delete old config backup!");
			}
			if(!configFile.renameTo(oldConfig))
			{
				logger.warning("Failed to rename old config! Could not do upgrade!");
				return;
			}
			YAML oldYAML = config;
			loadConfig();
			if(isLoaded())
			{
				doUpgrade(new Configuration(logger, configBaseDir, oldVersion, -1, CONFIG_PATH + ".old_v" + oldVersion, IN_JAR_PREFIX, oldYAML));
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			config = null;
		}
	}

	//region General getter
	/**
	 * Gets the {@link YAML} configuration instance for direct read/write.
	 *
	 * @return The configuration instance
	 */
	public YAML getConfig()
	{
		return config;
	}

	/**
	 * Gets an {@link Integer} value from the configuration.
	 *
	 * @param path The path to the value in the configuration file.
	 * @return The {@link Integer} value from the configuration file.
	 * @throws YAMLKeyNotFoundException When the given path is not found in the configuration
	 * @throws NumberFormatException When the value on the given position can't be converted to an {@link Integer}
	 */
	public int getInt(String path) throws YAMLKeyNotFoundException, NumberFormatException
	{
		return config.getInt(path);
	}

	/**
	 * Gets an {@link Double} value from the configuration.
	 *
	 * @param path The path to the value in the configuration file.
	 * @return The {@link Double} value from the configuration file.
	 * @throws YAMLKeyNotFoundException When the given path is not found in the configuration
	 * @throws NumberFormatException When the value on the given position can't be converted to an {@link Double}
	 */
	public double getDouble(String path) throws YAMLKeyNotFoundException, NumberFormatException
	{
		return config.getDouble(path);
	}

	/**
	 * Gets an {@link String} value from the configuration.
	 *
	 * @param path The path to the value in the configuration file.
	 * @return The {@link String} value from the configuration file.
	 * @throws YAMLKeyNotFoundException When the given path is not found in the configuration
	 */
	public String getString(String path) throws YAMLKeyNotFoundException
	{
		return config.getString(path);
	}

	/**
	 * Gets an {@link Boolean} value from the configuration.
	 *
	 * @param path The path to the value in the configuration file.
	 * @return The {@link Boolean} value from the configuration file.
	 * @throws YAMLKeyNotFoundException When the given path is not found in the configuration
	 */
	public boolean getBool(String path) throws YAMLKeyNotFoundException
	{
		return config.getBoolean(path);
	}

	/**
	 * Gets the version of the configuration.
	 *
	 * @return The version of the configuration. -1 if there is no or an invalid "Version" value in the configuration file.
	 */
	public int getVersion()
	{
		try
		{
			return config.getInt("Version");
		}
		catch(Exception ignored)
		{
			return -1;
		}
	}
	//endregion

	//region Getter for language settings
	/**
	 * Gets the language to use, defined in the configuration.
	 *
	 * @return The language to use.
	 */
	public String getLanguage()
	{
		return config.getString("Language", "en");
	}

	/**
	 * Gets how the language file should be updated, defined in the configuration.
	 *
	 * @return The update method for the language file.
	 */
	public LanguageUpdateMethod getLanguageUpdateMode()
	{
		return ((config.getString("LanguageUpdateMode", "overwrite").equalsIgnoreCase("overwrite")) ? LanguageUpdateMethod.OVERWRITE : LanguageUpdateMethod.UPDATE);
	}
	//endregion

	//region General setter
	/**
	 * Sets a option in the configuration.
	 *
	 * @param path  The path to the configuration option inside the configuration file.
	 * @param value The value it should be set to.
	 */
	public void set(String path, String value)
	{
		config.set(path, value);
	}

	/**
	 * Sets a option in the configuration.
	 *
	 * @param path  The path to the configuration option inside the configuration file.
	 * @param value The value it should be set to.
	 */
	public void set(String path, int value)
	{
		config.set(path, value);
	}

	/**
	 * Sets a option in the configuration.
	 *
	 * @param path  The path to the configuration option inside the configuration file.
	 * @param value The value it should be set to.
	 */
	public void set(String path, double value)
	{
		config.set(path, value);
	}

	/**
	 * Sets a option in the configuration.
	 *
	 * @param path  The path to the configuration option inside the configuration file.
	 * @param value The value it should be set to.
	 */
	public void set(String path, boolean value)
	{
		config.set(path, value);
	}
	//endregion
}