/*
 * Copyright 2013-2016 Indiana University
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.iu.harp.io;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import edu.iu.harp.client.Event;

public class EventQueue {

  /** Class logger */
  private static final Logger LOG = Logger
    .getLogger(EventQueue.class);

  private BlockingQueue<Event> eventQueue;

  public EventQueue() {
    eventQueue = new LinkedBlockingQueue<>();
  }

  public void addEvent(Event event) {
    this.eventQueue.add(event);
  }

  public Event waitEvent() {
    try {
      return eventQueue.take();
    } catch (InterruptedException e) {
      LOG.error(
        "Error when waiting and getting event.", e);
    }
    return null;
  }

  public Event getEvent() {
    return eventQueue.poll();
  }
}
