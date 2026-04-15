import type { SyncEvent, SyncEventType, SyncEventListener } from './types.js';

export class SyncEventEmitter {
  private listeners = new Map<SyncEventType, Set<SyncEventListener>>();
  private allListeners = new Set<SyncEventListener>();

  on(type: SyncEventType, listener: SyncEventListener): () => void {
    let set = this.listeners.get(type);
    if (!set) {
      set = new Set();
      this.listeners.set(type, set);
    }
    set.add(listener);
    return () => set!.delete(listener);
  }

  onAny(listener: SyncEventListener): () => void {
    this.allListeners.add(listener);
    return () => this.allListeners.delete(listener);
  }

  emit(event: SyncEvent): void {
    const set = this.listeners.get(event.type);
    if (set) {
      for (const listener of set) listener(event);
    }
    for (const listener of this.allListeners) listener(event);
  }
}
