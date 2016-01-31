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
package com.wx3.galacdecks.ai;

import java.util.Collection;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Updates all of the game AIs. 
 * 
 * TODO: Add worker threads to prevent one game from blocking all updates.
 * 
 * @author Kevin
 */
public final class AiManager {
	
	final static Logger logger = LoggerFactory.getLogger(AiManager.class);

	// Use a concurrent collection so we can safely iterate over the list 
	// while other threads add to it:
	private Collection<GameAI> ais = new ConcurrentLinkedQueue<GameAI>();
	
	private UpdateTask task;
	private long period;
	
	private long updateCount = 0;
	
	/**
	 * Update task runs the the update method on the manager
	 * @author Kevin
	 *
	 */
	class UpdateTask extends TimerTask {
		
		private AiManager manager;
		
		public UpdateTask(AiManager manager) {
			this.manager = manager;
		}

		@Override
		public void run() {
			manager.update();
		}
	
	}
	
	public AiManager(float seconds) {
		period = (long) (seconds * 1000);
	}
	
	public void start() {
		task = new UpdateTask(this);
		Timer timer = new Timer();
		timer.schedule(task, 0, period);
	}
	
	/**
	 * Add a new AI to be updated by the manager
	 * @param ai
	 */
	public void registerAI(GameAI ai){
		ais.add(ai);
	}
	
	void update() {
		long start = System.nanoTime();
		Iterator<GameAI> iter = ais.iterator();
		int i = 0;
		while(iter.hasNext()) {
			GameAI ai = iter.next();
			if(ai.isGameOver()) {
				logger.info(ai + " game over, removing update entry.");
				iter.remove();
			} else {
				ai.update();
				/*
				try {
					ai.update();	
				}
				catch(Exception ex) {
					logger.error("AI threw exception in update, removing: " + ex.getMessage());
					iter.remove();
				}
				*/
				++i;
			}
		}
		long finish = System.nanoTime();
		++updateCount;
		double duration = (finish - start) / 1e9;
			//logger.info("Updated " + i + " ais in " + duration + " seconds");
		
	}
}
