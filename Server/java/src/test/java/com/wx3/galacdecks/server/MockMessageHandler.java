/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2015 Kevin Lin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * 
 */
/**
 * 
 */
package com.wx3.galacdecks.server;

import java.util.ArrayList;
import java.util.List;

import com.wx3.galacdecks.server.MessageHandler;
import com.wx3.galacdecks.server.OutboundMessage;

/**
 * @author Kevin
 *
 */
public class MockMessageHandler implements MessageHandler {

	List<OutboundMessage> messages = new ArrayList<OutboundMessage>();
	
	public OutboundMessage getLastMessage() {
		if(messages.size() < 1) return null;
		return messages.get(messages.size() - 1);
	}
	
	public OutboundMessage getFirstMessage() {
		if(messages.size() < 1) return null;
		return messages.get(0);
	}
	
	public boolean containsMessage(Class<? extends OutboundMessage> clazz) {
		for(OutboundMessage msg : messages) {
			if(msg.getClass() == clazz) return true;
		}
		return false;
	}
	
	@Override
	public void disconnect() {}

	@Override
	public void handleMessage(OutboundMessage message) {
		messages.add(message);
	}

}
