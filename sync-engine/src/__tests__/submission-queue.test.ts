import { describe, it, expect, vi } from 'vitest';
import { SubmissionQueue } from '../submission-queue.js';
import type { Submission, SyncEvent } from '../types.js';

function makeSubmission(overrides: Partial<Submission> = {}): Submission {
  return {
    instanceId: overrides.instanceId ?? crypto.randomUUID(),
    formId: 'form-1',
    data: { field1: 'value1' },
    submittedAt: new Date().toISOString(),
    ...overrides,
  };
}

function mockFetch(handler: (url: string, init: RequestInit) => Response) {
  return vi.fn(async (url: string | URL | Request, init?: RequestInit) => {
    return handler(String(url), init ?? {});
  }) as unknown as typeof globalThis.fetch;
}

const noDelay = async () => {};

let dbCounter = 0;
function uniqueDbName() {
  return `test-sync-${++dbCounter}-${Date.now()}`;
}

describe('SubmissionQueue', () => {
  describe('queue persistence across page reloads', () => {
    it('should persist enqueued submissions and retrieve them after re-instantiation', async () => {
      const dbName = uniqueDbName();
      const fetchFn = mockFetch(() => new Response(null, { status: 200 }));

      const queue1 = new SubmissionQueue({ dbName, fetch: fetchFn, delay: noDelay });
      const sub1 = makeSubmission({ instanceId: 'persist-1' });
      const sub2 = makeSubmission({ instanceId: 'persist-2' });
      await queue1.enqueue(sub1);
      await queue1.enqueue(sub2);

      const status1 = await queue1.getStatus();
      expect(status1.pending).toBe(2);

      // Simulate page reload: new instance, same DB
      const queue2 = new SubmissionQueue({ dbName, fetch: fetchFn, delay: noDelay });
      const status2 = await queue2.getStatus();
      expect(status2.pending).toBe(2);

      const result = await queue2.sync();
      expect(result.synced).toBe(2);
      expect(result.failed).toBe(0);

      const status3 = await queue2.getStatus();
      expect(status3.pending).toBe(0);
    });

    it('should persist failed status across re-instantiation', async () => {
      const dbName = uniqueDbName();
      let callCount = 0;
      const fetchFn = mockFetch(() => {
        callCount++;
        return new Response(null, { status: 500 });
      });

      const queue1 = new SubmissionQueue({ dbName, fetch: fetchFn, delay: noDelay });
      await queue1.enqueue(makeSubmission({ instanceId: 'fail-persist' }));
      await queue1.sync();

      // 1 initial + 3 retries = 4 total calls
      expect(callCount).toBe(4);

      // "Reload" — failed items should still be tracked
      const queue2 = new SubmissionQueue({ dbName, fetch: fetchFn, delay: noDelay });
      const status = await queue2.getStatus();
      expect(status.failed).toBe(1);
      expect(status.pending).toBe(0);
    });
  });

  describe('conflict detection and resolution', () => {
    it('should detect 409 conflict and resolve with last-write-wins', async () => {
      const dbName = uniqueDbName();
      const events: SyncEvent[] = [];

      const fetchFn = mockFetch((_url, init) => {
        const headers = init.headers as Record<string, string> | undefined;
        if (headers?.['X-Conflict-Resolution'] === 'overwrite') {
          return new Response(null, { status: 200 });
        }
        return new Response(null, { status: 409 });
      });

      const queue = new SubmissionQueue({ dbName, fetch: fetchFn, delay: noDelay });
      queue.events.onAny((event) => events.push(event));

      await queue.enqueue(makeSubmission({ instanceId: 'conflict-1' }));
      const result = await queue.sync();

      expect(result.synced).toBe(1);
      expect(result.conflicts).toHaveLength(1);
      expect(result.conflicts[0].instanceId).toBe('conflict-1');
      expect(result.conflicts[0].resolution).toBe('last-write-wins');

      const eventTypes = events.map((e) => e.type);
      expect(eventTypes).toContain('queued');
      expect(eventTypes).toContain('syncing');
      expect(eventTypes).toContain('conflict');
      expect(eventTypes).toContain('synced');
    });

    it('should mark as failed if conflict resolution POST also fails', async () => {
      const dbName = uniqueDbName();
      const fetchFn = mockFetch(() => new Response(null, { status: 409 }));

      const queue = new SubmissionQueue({ dbName, fetch: fetchFn, delay: noDelay });
      await queue.enqueue(makeSubmission({ instanceId: 'conflict-fail' }));
      const result = await queue.sync();

      expect(result.synced).toBe(0);
      expect(result.failed).toBe(1);
    });
  });

  describe('partial sync recovery', () => {
    it('should resume from item 3 after items 1-2 succeed and item 3 fails', async () => {
      const dbName = uniqueDbName();
      const submissions = Array.from({ length: 5 }, (_, i) =>
        makeSubmission({ instanceId: `partial-${i + 1}` })
      );

      // Items 1,2 ok; item 3 retries all fail (500); items 4,5 ok
      let syncCallNum = 0;
      const fetchRound1 = mockFetch(() => {
        syncCallNum++;
        // calls 1,2 succeed; calls 3-6 are item 3 (initial + 3 retries); calls 7,8 are items 4,5
        if (syncCallNum >= 3 && syncCallNum <= 6) {
          return new Response(null, { status: 500 });
        }
        return new Response(null, { status: 200 });
      });

      const queue1 = new SubmissionQueue({ dbName, fetch: fetchRound1, delay: noDelay });
      for (const sub of submissions) {
        await queue1.enqueue(sub);
      }

      const result1 = await queue1.sync();
      expect(result1.synced).toBe(4);
      expect(result1.failed).toBe(1);

      const status = await queue1.getStatus();
      expect(status.pending).toBe(0);
      expect(status.failed).toBe(1);

      // Second sync: only pending items are synced (none left), failed stays
      const fetchRound2 = mockFetch(() => new Response(null, { status: 200 }));
      const queue2 = new SubmissionQueue({ dbName, fetch: fetchRound2, delay: noDelay });

      const result2 = await queue2.sync();
      expect(result2.synced).toBe(0);

      const finalStatus = await queue2.getStatus();
      expect(finalStatus.failed).toBe(1);
    });
  });

  describe('retry with exponential backoff', () => {
    it('should retry 3 times on 5xx errors with backoff then mark failed', async () => {
      const dbName = uniqueDbName();
      const delays: number[] = [];

      const fetchFn = mockFetch(() => new Response(null, { status: 503 }));
      const trackDelay = async (ms: number) => {
        delays.push(ms);
      };

      const queue = new SubmissionQueue({ dbName, fetch: fetchFn, delay: trackDelay });
      await queue.enqueue(makeSubmission());
      const result = await queue.sync();

      expect(result.failed).toBe(1);
      expect(result.synced).toBe(0);
      // 1 initial + 3 retries = 4 total calls
      expect(fetchFn).toHaveBeenCalledTimes(4);
      // Exponential backoff: 1000, 2000, 4000
      expect(delays).toEqual([1000, 2000, 4000]);
    });

    it('should retry on network errors and succeed on 4th attempt', async () => {
      const dbName = uniqueDbName();
      let attempt = 0;

      const fetchFn = mockFetch(() => {
        attempt++;
        if (attempt <= 3) throw new Error('Network failure');
        return new Response(null, { status: 200 });
      });

      const queue = new SubmissionQueue({ dbName, fetch: fetchFn, delay: noDelay });
      await queue.enqueue(makeSubmission());
      const result = await queue.sync();

      expect(result.synced).toBe(1);
      expect(result.failed).toBe(0);
    });
  });

  describe('event emitter', () => {
    it('should emit events in correct order for successful sync', async () => {
      const dbName = uniqueDbName();
      const events: string[] = [];
      const fetchFn = mockFetch(() => new Response(null, { status: 200 }));

      const queue = new SubmissionQueue({ dbName, fetch: fetchFn, delay: noDelay });
      queue.events.on('queued', () => events.push('queued'));
      queue.events.on('syncing', () => events.push('syncing'));
      queue.events.on('synced', () => events.push('synced'));

      await queue.enqueue(makeSubmission());
      await queue.sync();

      expect(events).toEqual(['queued', 'syncing', 'synced']);
    });

    it('should allow unsubscribing from events', async () => {
      const dbName = uniqueDbName();
      const events: string[] = [];
      const fetchFn = mockFetch(() => new Response(null, { status: 200 }));

      const queue = new SubmissionQueue({ dbName, fetch: fetchFn, delay: noDelay });
      const unsub = queue.events.on('queued', () => events.push('queued'));

      await queue.enqueue(makeSubmission());
      unsub();
      await queue.enqueue(makeSubmission());

      expect(events).toEqual(['queued']);
    });
  });
});
