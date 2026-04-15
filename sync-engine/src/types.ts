export interface Submission {
  instanceId: string;
  formId: string;
  data: Record<string, unknown>;
  submittedAt: string;
}

export interface ConflictRecord {
  instanceId: string;
  resolution: 'last-write-wins';
  mergeLog: string;
}

export interface SyncResult {
  synced: number;
  failed: number;
  conflicts: ConflictRecord[];
  errors: string[];
}

export interface QueueStatus {
  pending: number;
  failed: number;
  lastSyncAt: string | null;
}

export type QueueEntryStatus = 'pending' | 'failed';

export interface QueueEntry {
  id: number;
  submission: Submission;
  status: QueueEntryStatus;
  retries: number;
  createdAt: string;
}

export type SyncEvent =
  | { type: 'queued'; submission: Submission }
  | { type: 'syncing'; submission: Submission }
  | { type: 'synced'; submission: Submission }
  | { type: 'conflict'; record: ConflictRecord }
  | { type: 'failed'; submission: Submission; error: string };

export type SyncEventType = SyncEvent['type'];

export type SyncEventListener = (event: SyncEvent) => void;
