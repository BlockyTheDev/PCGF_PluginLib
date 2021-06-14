/*
 * Copyright (C) 2017 MarkusWME
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package at.pcgamingfreaks.Updater.UpdateProviders;

import at.pcgamingfreaks.TestClasses.TestUtils;
import at.pcgamingfreaks.Updater.ChecksumType;
import at.pcgamingfreaks.Updater.UpdateResult;
import com.google.gson.JsonParser;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.logging.Logger;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.whenNew;

public class JenkinsUpdateProviderTest
{
	@Test(expected = NullPointerException.class)
	public void testJenkinsUpdateProviderWithoutJob()
	{
		//noinspection ConstantConditions
		new JenkinsUpdateProvider("", null, null);
	}

	@Test(expected = NullPointerException.class)
	public void testJenkinsUpdateProviderWithoutHost()
	{
		//noinspection ConstantConditions
		new JenkinsUpdateProvider(null, null, null);
	}

	@Test
	public void testQuery() throws Exception
	{
		int currentWarnings = 0;
		int currentSevere = 0;
		final int[] counts = { 0, 0 };
		Logger mockedLogger = mock(Logger.class);
		doAnswer(invocationOnMock -> {
			System.out.println(invocationOnMock.getArguments()[0]);
			counts[0]++;
			return null;
		}).when(mockedLogger).warning(anyString());
		doAnswer(invocationOnMock -> {
			System.out.println(invocationOnMock.getArguments()[0]);
			counts[1]++;
			return null;
		}).when(mockedLogger).severe(anyString());
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("abc://invalid/", "NOPE", mockedLogger);
		assertEquals("The invalid query should return a failure", UpdateResult.FAIL_FILE_NOT_FOUND, updater.query());
		assertEquals("A warning should be shown", ++currentWarnings, counts[0]);
		updater = new JenkinsUpdateProvider("ci.pcgamingfreaks.at", "PluginLib", "", mockedLogger);
		assertEquals("The query should be successful", UpdateResult.SUCCESS, updater.query());
		assertEquals("No warning should be shown", currentWarnings, counts[0]);
		updater = new JenkinsUpdateProvider("https://ci.pcgamingfreaks.at", "PluginLib", "", mockedLogger);
		assertEquals("The query should be successful", UpdateResult.SUCCESS, updater.query());
		assertEquals("No warning should be shown", currentWarnings, counts[0]);
		TestUtils.initReflection();
		URL mockedURL = new URL("https://github.com/GeorgH93/TelePlusPlus");
		Field urlField = TestUtils.setAccessible(JenkinsUpdateProvider.class, updater, "url", mockedURL);
		assertEquals("The query should return a failure", UpdateResult.FAIL_NO_VERSION_FOUND, updater.query());
		assertEquals("The logger should log the error", currentSevere, counts[1]);
		Field tokenField = TestUtils.setAccessible(JenkinsUpdateProvider.class, updater, "token", null);
		assertEquals("The query should return a failure", UpdateResult.FAIL_NO_VERSION_FOUND, updater.query());
		assertEquals("The logger should log the error", currentSevere, counts[1]);
		TestUtils.setUnaccessible(tokenField, updater, true);
		assertEquals("The query should return a offline message", UpdateResult.FAIL_NO_VERSION_FOUND, updater.query());
		assertEquals("The logger should log the error", currentSevere, counts[1]);
		TestUtils.setUnaccessible(urlField, updater, true);
		updater = new JenkinsUpdateProvider("https://ci.pcgamingfreaks.at", "PluginLib", mockedLogger, "PLib");
		updater.query();
		assertNotNull("The updater object should not be null", updater);
		JsonParser mockedParser = new JsonParser();
		whenNew(JsonParser.class).withAnyArguments().thenReturn(mockedParser);
		assertEquals("No version should be found", UpdateResult.FAIL_FILE_NOT_FOUND, updater.query());
		currentWarnings += 3;
		assertEquals("The number of warnings should match", currentWarnings, counts[0]);
	}

	@Test(expected = NotSuccessfullyQueriedException.class)
	public void testGetLatestVersionFailure() throws NotSuccessfullyQueriedException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("abc://invalid/", "NOPE", mockedLogger);
		updater.getLatestVersion();
	}

	@Test
	public void testGetLatestVersionSuccess() throws NotSuccessfullyQueriedException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("https://ci.pcgamingfreaks.at", "PluginLib", mockedLogger);
		updater.query();
		assertNotNull("The latest version should not be null", updater.getLatestVersion());
	}

	@Test(expected = NotSuccessfullyQueriedException.class)
	public void testGetLatestFileURLFailure() throws NotSuccessfullyQueriedException, RequestTypeNotAvailableException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("abc://invalid/", "NOPE", mockedLogger);
		updater.getLatestFileURL();
	}

	@Test
	public void testGetLatestFileURLSuccess() throws NotSuccessfullyQueriedException, RequestTypeNotAvailableException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("https://ci.pcgamingfreaks.at", "PluginLib", mockedLogger);
		updater.query();
		assertNotNull("The latest file URL should not be null", updater.getLatestFileURL());
	}

	@Test(expected = NotSuccessfullyQueriedException.class)
	public void testGetLatestVersionFileNameFailure() throws NotSuccessfullyQueriedException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("abc://invalid/", "NOPE", mockedLogger);
		updater.getLatestFileName();
	}

	@Test
	public void testGetLatestVersionFileNameSuccess() throws NotSuccessfullyQueriedException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("https://ci.pcgamingfreaks.at", "PluginLib", mockedLogger);
		updater.query();
		assertNotNull("The latest version file name should not be null", updater.getLatestFileName());
	}

	@Test(expected = NotSuccessfullyQueriedException.class)
	public void testGetLatestNameFailure() throws NotSuccessfullyQueriedException, RequestTypeNotAvailableException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("abc://invalid/", "NOPE", mockedLogger);
		updater.getLatestName();
	}

	@Test
	public void testGetLatestNameSuccess() throws NotSuccessfullyQueriedException, RequestTypeNotAvailableException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("https://ci.pcgamingfreaks.at", "PluginLib", mockedLogger);
		updater.query();
		assertNotNull("The latest version should not be null", updater.getLatestName());
	}

	@Test(expected = NotSuccessfullyQueriedException.class)
	public void testGetLatestChecksumFailure() throws NotSuccessfullyQueriedException, RequestTypeNotAvailableException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("abc://invalid/", "NOPE", mockedLogger);
		updater.getLatestChecksum();
	}

	@Test
	public void testGetLatestChecksumSuccess() throws NotSuccessfullyQueriedException, RequestTypeNotAvailableException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("https://ci.pcgamingfreaks.at", "PluginLib", mockedLogger);
		updater.query();
		assertNotNull("The latest checksum should not be null", updater.getLatestChecksum());
	}

	@Test(expected = NotSuccessfullyQueriedException.class)
	public void testGetLatestChangelogFailure() throws NotSuccessfullyQueriedException, RequestTypeNotAvailableException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("abc://invalid/", "NOPE", mockedLogger);
		updater.getLatestChangelog();
	}

	@Test
	public void testGetLatestChangelogSuccess() throws NotSuccessfullyQueriedException, RequestTypeNotAvailableException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("https://ci.pcgamingfreaks.at", "PluginLib", mockedLogger);
		updater.query();
		assertNotNull("The latest changelog should not be null", updater.getLatestChangelog());
	}

	@Test(expected = RequestTypeNotAvailableException.class)
	public void testGetLatestMinecraftVersion() throws RequestTypeNotAvailableException, NotSuccessfullyQueriedException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("https://ci.pcgamingfreaks.at", "PluginLib", mockedLogger);
		updater.getLatestMinecraftVersion();
	}

	@Test(expected = RequestTypeNotAvailableException.class)
	public void testGetLatestDependencies() throws RequestTypeNotAvailableException, NotSuccessfullyQueriedException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("https://ci.pcgamingfreaks.at", "PluginLib", mockedLogger);
		updater.getLatestDependencies();
	}

	@Test(expected = RequestTypeNotAvailableException.class)
	public void testGetUpdateHistory() throws RequestTypeNotAvailableException, NotSuccessfullyQueriedException
	{
		Logger mockedLogger = mock(Logger.class);
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("https://ci.pcgamingfreaks.at", "PluginLib", mockedLogger);
		updater.getUpdateHistory();
	}

	@Test
	public void testSettings() throws IllegalAccessException, InvocationTargetException, InstantiationException
	{
		//noinspection ConstantConditions
		JenkinsUpdateProvider updater = new JenkinsUpdateProvider("https://ci.pcgamingfreaks.at", "PluginLib", null);
		assertTrue("The JenkinsUpdateProvider should provide a download URL", updater.providesDownloadURL());
		assertTrue("The JenkinsUpdateProvider should provide a changelog", updater.providesChangelog());
		assertEquals("The JenkinsUpdateProvider should provide a MD5 checksum", ChecksumType.MD5, updater.providesChecksum());
		/*updater = (JenkinsUpdateProvider) JenkinsUpdateProvider.class.getDeclaredConstructors()[0].newInstance(new IndicateReloadClass());
		assertFalse("The JenkinsUpdateProvider should not provide a Minecraft version", updater.providesMinecraftVersion());
		assertFalse("The JenkinsUpdateProvider should not provide a update history", updater.providesUpdateHistory());
		assertFalse("The JenkinsUpdateProvider should not provide dependencies", updater.providesDependencies());*/
	}
}