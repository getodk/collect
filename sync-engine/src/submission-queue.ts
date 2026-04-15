import type {
  Submission,
  SyncResult,
  QueueStatus,
  ConflictRecord,
  QueueEntry,
} from './types.js';
import { QueueStore } from './store.js';
import { SyncEventEmitter } from './event-emitter.js';

const MAX_RETRIES = 3;
const BACKOFF_BASE_MS = 1000;

export interface SubmissionQueueOptions {
  apiBase?: string;
  dbName?: string;
  fetch?: typeof globalThis.fetch;
  /** Override the delay function (useful for testing). Receives milliseconds. */
  delay?: (ms: number) => Promise<void>;
}

export class SubmissionQueue {
  private store: QueueStore;
  private emitter = new SyncEventEmitter();
  private apiBase: string;
  private fetchFn: typeof globalThis.fetch;
  private delayFn: (ms: number) => Promise<void>;

  constructor(options: SubmissionQueueOptions = {}) {
    this.apiBase = options.apiBase ?? '';
    this.store = new QueueStore(options.dbName);
    this.fetchFn = options.fetch ?? globalThis.fetch.bind(globalThis);
    this.delayFn =
      options.delay ??
      ((ms: number) => new Promise((resolve) => setTimeout(resolve, ms)));
  }

  get events(): SyncEventEmitter {
    return this.emitter;
  }

  async enqueue(submission: Submission): Promise<void> {
    await this.store.add(submission);
    this.emitter.emit({ type: 'queued', submission });
  }

  async sync(): Promise<SyncResult> {
    const entries = await this.store.getPending();
    const result: SyncResult = {
      synced: 0,
      failed: 0,
      conflicts: [],
      errors: [],
    };

    for (const entry of entries) {
      await this.processEntry(entry, result);
    }

    return result;
  }

  async getStatus(): Promise<QueueStatus> {
    const [pending, failed, lastSyncAt] = await Promise.all([
      this.store.count('pending'),
      this.store.count('failed'),
      this.store.getLastSyncAt(),
    ]);
    return { pending, failed, lastSyncAt };
  }

  private async processEntry(
    entry: QueueEntry,
    result: SyncResult
  ): Promise<void> {
    const { submission } = entry;
    this.emitter.emit({ type: 'syncing', submission });

    const outcome = await this.postWithRetry(submission);

    if (outcome.ok) {
      await this.store.remove(entry.id);
      const now = new Date().toISOString();
      await this.store.setLastSyncAt(now);
      result.synced++;
      this.emitter.emit({ type: 'synced', submission });
      return;
    }

    if (outcome.conflict) {
      const conflictResult = await this.resolveConflict(submission);
      if (conflictResult.ok) {
        await this.store.remove(entry.id);
        const now = new Date().toISOString();
        await this.store.setLastSyncAt(now);
        result.synced++;
        const record: ConflictRecord = {
          instanceId: submission.instanceId,
          resolution: 'last-write-wins',
          mergeLog: `Conflict on ${submission.instanceId}: local version overwrote server at ${now}`,
        };
        result.conflicts.push(record);
        this.emitter.emit({ type: 'conflict', record });
        this.emitter.emit({ type: 'synced', submission });
        return;
      }
    }

    entry.status = 'failed';
    await this.store.update(entry);
    result.failed++;
    const errorMsg = outcome.error ?? 'Unknown error';
    result.errors.push(`${submission.instanceId}: ${errorMsg}`);
    this.emitter.emit({ type: 'failed', submission, error: errorMsg });
  }

  private async postWithRetry(
    submission: Submission
  ): Promise<{ ok: boolean; conflict: boolean; error?: string }> {
    for (let attempt = 0; attempt <= MAX_RETRIES; attempt++) {
      try {
        const response = await this.fetchFn(
          `${this.apiBase}/api/submissions`,
          {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(submission),
          }
        );

        if (response.ok) {
          return { ok: true, conflict: false };
        }

        if (response.status === 409) {
          return { ok: false, conflict: true };
        }

        if (response.status >= 500) {
          if (attempt < MAX_RETRIES) {
            await this.delayFn(BACKOFF_BASE_MS * Math.pow(2, attempt));
            continue;
          }
          return {
            ok: false,
            conflict: false,
            error: `Server error ${response.status} after ${MAX_RETRIES + 1} attempts`,
          };
        }

        return {
          ok: false,
          conflict: false,
          error: `HTTP ${response.status}`,
        };
      } catch (err) {
        if (attempt < MAX_RETRIES) {
          await this.delayFn(BACKOFF_BASE_MS * Math.pow(2, attempt));
          continue;
        }
        return {
          ok: false,
          conflict: false,
          error: `Network error after ${MAX_RETRIES + 1} attempts: ${err instanceof Error ? err.message : String(err)}`,
        };
      }
    }

    return { ok: false, conflict: false, error: 'Exhausted retries' };
  }

  private async resolveConflict(
    submission: Submission
  ): Promise<{ ok: boolean }> {
    try {
      const response = await this.fetchFn(
        `${this.apiBase}/api/submissions`,
        {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'X-Conflict-Resolution': 'overwrite',
          },
          body: JSON.stringify(submission),
        }
      );
      return { ok: response.ok };
    } catch {
      return { ok: false };
    }
  }

}
