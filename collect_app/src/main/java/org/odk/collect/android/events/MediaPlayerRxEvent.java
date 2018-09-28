/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.events;

public class MediaPlayerRxEvent extends RxEvent {

    private final EventType eventType;
    private final int playerTag;

    public enum EventType {
        PLAYING_STARTED, PLAYING_STOPPED, PLAYING_COMPLETED
    }

    public MediaPlayerRxEvent(EventType eventType, int playerTag) {
        this.eventType = eventType;
        this.playerTag = playerTag;
    }

    public EventType getEventType() {
        return eventType;
    }

    public int getPlayerTag() {
        return playerTag;
    }
}