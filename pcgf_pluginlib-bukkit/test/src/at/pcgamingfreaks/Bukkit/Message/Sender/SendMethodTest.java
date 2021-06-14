/*
 * Copyright (C) 2016 MarkusWME
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

package at.pcgamingfreaks.Bukkit.Message.Sender;

import at.pcgamingfreaks.Message.Sender.TitleMetadata;
import at.pcgamingfreaks.TestClasses.TestBukkitServer;
import org.bukkit.Bukkit;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class SendMethodTest
{
	@BeforeClass
	public static void prepareTestData() throws NoSuchFieldException, IllegalAccessException
	{
		if (Bukkit.getServer() == null) {
			Bukkit.setServer(new TestBukkitServer());
		}
		//TestObjects.initNMSReflection();
	}

	@Test
	public void testSendMethod()
	{
		SendMethod sendMethod = SendMethod.TITLE;
		assertEquals("The metadata class should match", TitleMetadata.class, sendMethod.getMetadataClass());
		assertNotNull("The metadata supplier Method should be returned", sendMethod.getMetadataSupplier());
	}

	@Test
	public void testIsAvailable()
	{
		assertTrue(SendMethod.CHAT.isAvailable());
		//assertFalse(SendMethod.BOSS_BAR.isAvailable());
	}

	@Test
	public void testHasMetadata()
	{
		assertFalse(SendMethod.CHAT.hasMetadata());
		assertTrue(SendMethod.TITLE.hasMetadata());
	}
}