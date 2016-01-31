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
package com.wx3.galacdecks.game;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.google.common.base.Strings;
import com.wx3.galacdecks.gameevents.GameEvent;

/**
 * An entity rule is a script that is fired in response to a particular
 * GameEvent. The eventTrigger is the (simple) name of the GameEvent
 * class this rule can respond to. E.g., DrawCardEvent.
 * 
 * Rule scripts should be immutable, since all references to the same
 * rule may be backed by the same underlying object.
 * 
 * @author Kevin
 *
 */
@Entity
@Table(name="rules")
public class EntityRule {
	
	@Id
	private String id;
	
	@Column(length = 512)
	private String description;
	
	/**
	 * The simple name of the GameEvent that triggers this rule
	 */
	private String eventTrigger;
	
	/**
	 * The script code that is eval'd when the trigger is fired.
	 */
	@Lob
	@Column(length = 2048)
	private String triggeredScript;
	
	private boolean permanent;
	
	public static EntityRule createRule(Class<? extends GameEvent> trigger, String script, String id, String description) {
		EntityRule rule = new EntityRule();
		rule.eventTrigger = trigger.getSimpleName();
		rule.triggeredScript = script;
		rule.id = id;
		rule.description = description;
		return rule;
	}
	
	public static EntityRule createRule(String triggerName, String script, String id, String description) {
		EntityRule rule = new EntityRule();
		rule.eventTrigger = triggerName;
		rule.triggeredScript = script;
		rule.id = id;
		rule.description = description;
		return rule;
	}
	
	public EntityRule() {}
	
	public EntityRule(EntityRule rule) {
		this.eventTrigger = rule.eventTrigger;
		this.triggeredScript = rule.triggeredScript;
		this.id = rule.id;
		this.description = rule.description;
		this.permanent = rule.permanent;
	}
	
	public String getId() {
		return id;
	}
	
	public String getEventTrigger() {
		return eventTrigger;
	}
	
	public boolean isTriggered(String trigger) {
		if(trigger.equals(eventTrigger)) {
			return true;
		} else {
			return false;	
		}
	}
	
	public boolean isTriggered(GameEvent event) {
		// If event trigger is null (or empty) then we'll fire on a null event:
		if(event == null) {
			if(Strings.isNullOrEmpty(eventTrigger)) return true;
			return false;
		}
		// Otherwise we fire if the event class's simple name matches our trigger string: 
		String className = event.getClass().getSimpleName();
		return isTriggered(className);
	}
	
	public boolean isPermanent() {
		return permanent;
	}

	public String getScript() {
		return triggeredScript;
	}
	
	@Override
	public String toString() {
		return "EntityRule_" + id;
	}

}