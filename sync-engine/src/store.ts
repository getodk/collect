import { openDB, type IDBPDatabase } from 'idb';
import type { QueueEntry, QueueEntryStatus, Submission } from './types.js';

const DB_NAME = 'fieldform-sync';
const DB_VERSION = 1;
const STORE_NAME = 'submissions';
const META_STORE = 'meta';

interface SyncDB {
  submissions: {
    key: number;
    value: QueueEntry;
    indexes: { 'by-status': QueueEntryStatus };
  };
  meta: {
    key: string;
    value: { key: string; value: string };
  };
}

export class QueueStore {
  private dbPromise: Promise<IDBPDatabase<SyncDB>>;

  constructor(dbName = DB_NAME) {
    this.dbPromise = openDB<SyncDB>(dbName, DB_VERSION, {
      upgrade(db) {
        const store = db.createObjectStore(STORE_NAME, {
          keyPath: 'id',
          autoIncrement: true,
        });
        store.createIndex('by-status', 'status');
        db.createObjectStore(META_STORE, { keyPath: 'key' });
      },
    });
  }

  async add(submission: Submission): Promise<QueueEntry> {
    const db = await this.dbPromise;
    const entry: Omit<QueueEntry, 'id'> = {
      submission,
      status: 'pending',
      retries: 0,
      createdAt: new Date().toISOString(),
    };
    const id = await db.add(STORE_NAME, entry as QueueEntry);
    return { ...entry, id } as QueueEntry;
  }

  async getPending(): Promise<QueueEntry[]> {
    const db = await this.dbPromise;
    return db.getAllFromIndex(STORE_NAME, 'by-status', 'pending');
  }

  async getFailed(): Promise<QueueEntry[]> {
    const db = await this.dbPromise;
    return db.getAllFromIndex(STORE_NAME, 'by-status', 'failed');
  }

  async update(entry: QueueEntry): Promise<void> {
    const db = await this.dbPromise;
    await db.put(STORE_NAME, entry);
  }

  async remove(id: number): Promise<void> {
    const db = await this.dbPromise;
    await db.delete(STORE_NAME, id);
  }

  async getLastSyncAt(): Promise<string | null> {
    const db = await this.dbPromise;
    const record = await db.get(META_STORE, 'lastSyncAt');
    return record?.value ?? null;
  }

  async setLastSyncAt(value: string): Promise<void> {
    const db = await this.dbPromise;
    await db.put(META_STORE, { key: 'lastSyncAt', value });
  }

  async count(status: QueueEntryStatus): Promise<number> {
    const db = await this.dbPromise;
    return db.countFromIndex(STORE_NAME, 'by-status', status);
  }
}
