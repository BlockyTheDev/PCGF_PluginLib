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

package at.pcgamingfreaks.Message;

import at.pcgamingfreaks.TestClasses.TestMessage;
import at.pcgamingfreaks.TestClasses.TestMessageComponent;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class MessageTest
{
	@Test
	public void testMessage()
	{
		List<TestMessageComponent> messageComponents = new ArrayList<>();
		messageComponents.add(new TestMessageComponent("Message text"));
		TestMessage message1 = new TestMessage("A message text");
		TestMessage message2 = new TestMessage("Message text");
		TestMessage message3 = new TestMessage(messageComponents);
		assertEquals("The message texts should match", "Message text", message2.getClassicMessage());
		assertEquals("The other message texts should match", "Message text§r", message3.getClassicMessage());
		assertEquals("The messages should be equal", "[{\"text\":\"Message text\"}]", message3.json);
		assertEquals("The message text should be correct", "[{\"text\":\"Message text\"}]", message3.toString());
		//noinspection deprecation
		assertEquals("The message components should be equal", messageComponents.toArray(), message3.getMessageComponents());
		message1.replaceAll("A m", "M");
		message1.replaceAll("ext", "est");
		message2.replaceAll("ext", "est");
		assertEquals("The message texts should match", message1.getClassicMessage(), message2.getClassicMessage());
		assertNotEquals("A string should not equal a message object", "False", message1);
		assertNotEquals("The messages should not be equal", message1, message3);
	}

	@Test
	public void testMessageJSON()
	{
		TestMessage message = new TestMessage("{\"text\":\"Test\"}");
		assertEquals("The message text should match", "Test§r", message.getClassicMessage());
	}

	@Test
	public void testMessageWithError()
	{
		assertEquals("The test message should be empty", "", new TestMessage("").getClassicMessage());
		assertEquals("The test message should be empty", "", new TestMessage("").getClassicMessage());
	}
}