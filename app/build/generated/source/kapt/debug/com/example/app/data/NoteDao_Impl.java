package com.example.app.data;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.LongSparseArray;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.room.util.RelationUtil;
import androidx.room.util.StringUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class NoteDao_Impl implements NoteDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Note> __insertionAdapterOfNote;

  private final Converters __converters = new Converters();

  private final EntityInsertionAdapter<NoteCrossRef> __insertionAdapterOfNoteCrossRef;

  private final EntityDeletionOrUpdateAdapter<Note> __updateAdapterOfNote;

  private final SharedSQLiteStatement __preparedStmtOfUpdateTranscript;

  private final SharedSQLiteStatement __preparedStmtOfUpdateSnippet;

  private final SharedSQLiteStatement __preparedStmtOfUpdateChecklistState;

  private final SharedSQLiteStatement __preparedStmtOfDeleteById;

  private final SharedSQLiteStatement __preparedStmtOfUpdateTitle;

  private final SharedSQLiteStatement __preparedStmtOfArchiveNote;

  public NoteDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfNote = new EntityInsertionAdapter<Note>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `notes` (`id`,`title`,`snippet`,`transcript`,`audioPath`,`highlights`,`isFavorite`,`isArchived`,`createdAt`,`reminderTime`,`checklistState`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Note entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getTitle() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getTitle());
        }
        if (entity.getSnippet() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getSnippet());
        }
        if (entity.getTranscript() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getTranscript());
        }
        if (entity.getAudioPath() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getAudioPath());
        }
        final String _tmp = __converters.listToString(entity.getHighlights());
        if (_tmp == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, _tmp);
        }
        final int _tmp_1 = entity.isFavorite() ? 1 : 0;
        statement.bindLong(7, _tmp_1);
        final int _tmp_2 = entity.isArchived() ? 1 : 0;
        statement.bindLong(8, _tmp_2);
        statement.bindLong(9, entity.getCreatedAt());
        if (entity.getReminderTime() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getReminderTime());
        }
        if (entity.getChecklistState() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getChecklistState());
        }
      }
    };
    this.__insertionAdapterOfNoteCrossRef = new EntityInsertionAdapter<NoteCrossRef>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `note_cross_refs` (`noteId`,`relatedNoteId`) VALUES (?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final NoteCrossRef entity) {
        statement.bindLong(1, entity.getNoteId());
        statement.bindLong(2, entity.getRelatedNoteId());
      }
    };
    this.__updateAdapterOfNote = new EntityDeletionOrUpdateAdapter<Note>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `notes` SET `id` = ?,`title` = ?,`snippet` = ?,`transcript` = ?,`audioPath` = ?,`highlights` = ?,`isFavorite` = ?,`isArchived` = ?,`createdAt` = ?,`reminderTime` = ?,`checklistState` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Note entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getTitle() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getTitle());
        }
        if (entity.getSnippet() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getSnippet());
        }
        if (entity.getTranscript() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getTranscript());
        }
        if (entity.getAudioPath() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getAudioPath());
        }
        final String _tmp = __converters.listToString(entity.getHighlights());
        if (_tmp == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, _tmp);
        }
        final int _tmp_1 = entity.isFavorite() ? 1 : 0;
        statement.bindLong(7, _tmp_1);
        final int _tmp_2 = entity.isArchived() ? 1 : 0;
        statement.bindLong(8, _tmp_2);
        statement.bindLong(9, entity.getCreatedAt());
        if (entity.getReminderTime() == null) {
          statement.bindNull(10);
        } else {
          statement.bindLong(10, entity.getReminderTime());
        }
        if (entity.getChecklistState() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getChecklistState());
        }
        statement.bindLong(12, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateTranscript = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE notes SET transcript = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateSnippet = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE notes SET snippet = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateChecklistState = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE notes SET checklistState = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM notes WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateTitle = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE notes SET title = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfArchiveNote = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE notes SET isArchived = 1 WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final Note note, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfNote.insert(note);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertCrossRef(final NoteCrossRef crossRef,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfNoteCrossRef.insert(crossRef);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object update(final Note note, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfNote.handle(note);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateTranscript(final long id, final String transcript,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateTranscript.acquire();
        int _argIndex = 1;
        if (transcript == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, transcript);
        }
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateTranscript.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSnippet(final long id, final String snippet,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateSnippet.acquire();
        int _argIndex = 1;
        if (snippet == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, snippet);
        }
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateSnippet.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateChecklistState(final long id, final String checklistState,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateChecklistState.acquire();
        int _argIndex = 1;
        if (checklistState == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, checklistState);
        }
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateChecklistState.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteById(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteById.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateTitle(final long id, final String title,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateTitle.acquire();
        int _argIndex = 1;
        if (title == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, title);
        }
        _argIndex = 2;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateTitle.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object archiveNote(final long id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfArchiveNote.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfArchiveNote.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Note>> getAllNotes() {
    final String _sql = "SELECT * FROM notes WHERE isArchived = 0 ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"notes"}, new Callable<List<Note>>() {
      @Override
      @NonNull
      public List<Note> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSnippet = CursorUtil.getColumnIndexOrThrow(_cursor, "snippet");
          final int _cursorIndexOfTranscript = CursorUtil.getColumnIndexOrThrow(_cursor, "transcript");
          final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audioPath");
          final int _cursorIndexOfHighlights = CursorUtil.getColumnIndexOrThrow(_cursor, "highlights");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfIsArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "isArchived");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfReminderTime = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderTime");
          final int _cursorIndexOfChecklistState = CursorUtil.getColumnIndexOrThrow(_cursor, "checklistState");
          final List<Note> _result = new ArrayList<Note>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Note _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpSnippet;
            if (_cursor.isNull(_cursorIndexOfSnippet)) {
              _tmpSnippet = null;
            } else {
              _tmpSnippet = _cursor.getString(_cursorIndexOfSnippet);
            }
            final String _tmpTranscript;
            if (_cursor.isNull(_cursorIndexOfTranscript)) {
              _tmpTranscript = null;
            } else {
              _tmpTranscript = _cursor.getString(_cursorIndexOfTranscript);
            }
            final String _tmpAudioPath;
            if (_cursor.isNull(_cursorIndexOfAudioPath)) {
              _tmpAudioPath = null;
            } else {
              _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
            }
            final List<String> _tmpHighlights;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfHighlights)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfHighlights);
            }
            _tmpHighlights = __converters.fromString(_tmp);
            final boolean _tmpIsFavorite;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp_1 != 0;
            final boolean _tmpIsArchived;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsArchived);
            _tmpIsArchived = _tmp_2 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final Long _tmpReminderTime;
            if (_cursor.isNull(_cursorIndexOfReminderTime)) {
              _tmpReminderTime = null;
            } else {
              _tmpReminderTime = _cursor.getLong(_cursorIndexOfReminderTime);
            }
            final String _tmpChecklistState;
            if (_cursor.isNull(_cursorIndexOfChecklistState)) {
              _tmpChecklistState = null;
            } else {
              _tmpChecklistState = _cursor.getString(_cursorIndexOfChecklistState);
            }
            _item = new Note(_tmpId,_tmpTitle,_tmpSnippet,_tmpTranscript,_tmpAudioPath,_tmpHighlights,_tmpIsFavorite,_tmpIsArchived,_tmpCreatedAt,_tmpReminderTime,_tmpChecklistState);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getAllNotesOnce(final Continuation<? super List<Note>> $completion) {
    final String _sql = "SELECT * FROM notes WHERE isArchived = 0 ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<Note>>() {
      @Override
      @NonNull
      public List<Note> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSnippet = CursorUtil.getColumnIndexOrThrow(_cursor, "snippet");
          final int _cursorIndexOfTranscript = CursorUtil.getColumnIndexOrThrow(_cursor, "transcript");
          final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audioPath");
          final int _cursorIndexOfHighlights = CursorUtil.getColumnIndexOrThrow(_cursor, "highlights");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfIsArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "isArchived");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfReminderTime = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderTime");
          final int _cursorIndexOfChecklistState = CursorUtil.getColumnIndexOrThrow(_cursor, "checklistState");
          final List<Note> _result = new ArrayList<Note>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Note _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpSnippet;
            if (_cursor.isNull(_cursorIndexOfSnippet)) {
              _tmpSnippet = null;
            } else {
              _tmpSnippet = _cursor.getString(_cursorIndexOfSnippet);
            }
            final String _tmpTranscript;
            if (_cursor.isNull(_cursorIndexOfTranscript)) {
              _tmpTranscript = null;
            } else {
              _tmpTranscript = _cursor.getString(_cursorIndexOfTranscript);
            }
            final String _tmpAudioPath;
            if (_cursor.isNull(_cursorIndexOfAudioPath)) {
              _tmpAudioPath = null;
            } else {
              _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
            }
            final List<String> _tmpHighlights;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfHighlights)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfHighlights);
            }
            _tmpHighlights = __converters.fromString(_tmp);
            final boolean _tmpIsFavorite;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp_1 != 0;
            final boolean _tmpIsArchived;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsArchived);
            _tmpIsArchived = _tmp_2 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final Long _tmpReminderTime;
            if (_cursor.isNull(_cursorIndexOfReminderTime)) {
              _tmpReminderTime = null;
            } else {
              _tmpReminderTime = _cursor.getLong(_cursorIndexOfReminderTime);
            }
            final String _tmpChecklistState;
            if (_cursor.isNull(_cursorIndexOfChecklistState)) {
              _tmpChecklistState = null;
            } else {
              _tmpChecklistState = _cursor.getString(_cursorIndexOfChecklistState);
            }
            _item = new Note(_tmpId,_tmpTitle,_tmpSnippet,_tmpTranscript,_tmpAudioPath,_tmpHighlights,_tmpIsFavorite,_tmpIsArchived,_tmpCreatedAt,_tmpReminderTime,_tmpChecklistState);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Note>> searchNotes(final String query) {
    final String _sql = "SELECT * FROM notes WHERE (title LIKE ? OR transcript LIKE ?) AND isArchived = 0 ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (query == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, query);
    }
    _argIndex = 2;
    if (query == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, query);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"notes"}, new Callable<List<Note>>() {
      @Override
      @NonNull
      public List<Note> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSnippet = CursorUtil.getColumnIndexOrThrow(_cursor, "snippet");
          final int _cursorIndexOfTranscript = CursorUtil.getColumnIndexOrThrow(_cursor, "transcript");
          final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audioPath");
          final int _cursorIndexOfHighlights = CursorUtil.getColumnIndexOrThrow(_cursor, "highlights");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfIsArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "isArchived");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfReminderTime = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderTime");
          final int _cursorIndexOfChecklistState = CursorUtil.getColumnIndexOrThrow(_cursor, "checklistState");
          final List<Note> _result = new ArrayList<Note>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Note _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpSnippet;
            if (_cursor.isNull(_cursorIndexOfSnippet)) {
              _tmpSnippet = null;
            } else {
              _tmpSnippet = _cursor.getString(_cursorIndexOfSnippet);
            }
            final String _tmpTranscript;
            if (_cursor.isNull(_cursorIndexOfTranscript)) {
              _tmpTranscript = null;
            } else {
              _tmpTranscript = _cursor.getString(_cursorIndexOfTranscript);
            }
            final String _tmpAudioPath;
            if (_cursor.isNull(_cursorIndexOfAudioPath)) {
              _tmpAudioPath = null;
            } else {
              _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
            }
            final List<String> _tmpHighlights;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfHighlights)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfHighlights);
            }
            _tmpHighlights = __converters.fromString(_tmp);
            final boolean _tmpIsFavorite;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp_1 != 0;
            final boolean _tmpIsArchived;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsArchived);
            _tmpIsArchived = _tmp_2 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final Long _tmpReminderTime;
            if (_cursor.isNull(_cursorIndexOfReminderTime)) {
              _tmpReminderTime = null;
            } else {
              _tmpReminderTime = _cursor.getLong(_cursorIndexOfReminderTime);
            }
            final String _tmpChecklistState;
            if (_cursor.isNull(_cursorIndexOfChecklistState)) {
              _tmpChecklistState = null;
            } else {
              _tmpChecklistState = _cursor.getString(_cursorIndexOfChecklistState);
            }
            _item = new Note(_tmpId,_tmpTitle,_tmpSnippet,_tmpTranscript,_tmpAudioPath,_tmpHighlights,_tmpIsFavorite,_tmpIsArchived,_tmpCreatedAt,_tmpReminderTime,_tmpChecklistState);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Note> getNoteById(final long id) {
    final String _sql = "SELECT * FROM notes WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"notes"}, new Callable<Note>() {
      @Override
      @Nullable
      public Note call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfSnippet = CursorUtil.getColumnIndexOrThrow(_cursor, "snippet");
          final int _cursorIndexOfTranscript = CursorUtil.getColumnIndexOrThrow(_cursor, "transcript");
          final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audioPath");
          final int _cursorIndexOfHighlights = CursorUtil.getColumnIndexOrThrow(_cursor, "highlights");
          final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
          final int _cursorIndexOfIsArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "isArchived");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfReminderTime = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderTime");
          final int _cursorIndexOfChecklistState = CursorUtil.getColumnIndexOrThrow(_cursor, "checklistState");
          final Note _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTitle;
            if (_cursor.isNull(_cursorIndexOfTitle)) {
              _tmpTitle = null;
            } else {
              _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            }
            final String _tmpSnippet;
            if (_cursor.isNull(_cursorIndexOfSnippet)) {
              _tmpSnippet = null;
            } else {
              _tmpSnippet = _cursor.getString(_cursorIndexOfSnippet);
            }
            final String _tmpTranscript;
            if (_cursor.isNull(_cursorIndexOfTranscript)) {
              _tmpTranscript = null;
            } else {
              _tmpTranscript = _cursor.getString(_cursorIndexOfTranscript);
            }
            final String _tmpAudioPath;
            if (_cursor.isNull(_cursorIndexOfAudioPath)) {
              _tmpAudioPath = null;
            } else {
              _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
            }
            final List<String> _tmpHighlights;
            final String _tmp;
            if (_cursor.isNull(_cursorIndexOfHighlights)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getString(_cursorIndexOfHighlights);
            }
            _tmpHighlights = __converters.fromString(_tmp);
            final boolean _tmpIsFavorite;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
            _tmpIsFavorite = _tmp_1 != 0;
            final boolean _tmpIsArchived;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsArchived);
            _tmpIsArchived = _tmp_2 != 0;
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final Long _tmpReminderTime;
            if (_cursor.isNull(_cursorIndexOfReminderTime)) {
              _tmpReminderTime = null;
            } else {
              _tmpReminderTime = _cursor.getLong(_cursorIndexOfReminderTime);
            }
            final String _tmpChecklistState;
            if (_cursor.isNull(_cursorIndexOfChecklistState)) {
              _tmpChecklistState = null;
            } else {
              _tmpChecklistState = _cursor.getString(_cursorIndexOfChecklistState);
            }
            _result = new Note(_tmpId,_tmpTitle,_tmpSnippet,_tmpTranscript,_tmpAudioPath,_tmpHighlights,_tmpIsFavorite,_tmpIsArchived,_tmpCreatedAt,_tmpReminderTime,_tmpChecklistState);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<NoteWithRelations> getNoteWithRelations(final long id) {
    final String _sql = "SELECT * FROM notes WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, true, new String[] {"note_cross_refs",
        "notes"}, new Callable<NoteWithRelations>() {
      @Override
      @Nullable
      public NoteWithRelations call() throws Exception {
        __db.beginTransaction();
        try {
          final Cursor _cursor = DBUtil.query(__db, _statement, true, null);
          try {
            final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
            final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
            final int _cursorIndexOfSnippet = CursorUtil.getColumnIndexOrThrow(_cursor, "snippet");
            final int _cursorIndexOfTranscript = CursorUtil.getColumnIndexOrThrow(_cursor, "transcript");
            final int _cursorIndexOfAudioPath = CursorUtil.getColumnIndexOrThrow(_cursor, "audioPath");
            final int _cursorIndexOfHighlights = CursorUtil.getColumnIndexOrThrow(_cursor, "highlights");
            final int _cursorIndexOfIsFavorite = CursorUtil.getColumnIndexOrThrow(_cursor, "isFavorite");
            final int _cursorIndexOfIsArchived = CursorUtil.getColumnIndexOrThrow(_cursor, "isArchived");
            final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
            final int _cursorIndexOfReminderTime = CursorUtil.getColumnIndexOrThrow(_cursor, "reminderTime");
            final int _cursorIndexOfChecklistState = CursorUtil.getColumnIndexOrThrow(_cursor, "checklistState");
            final LongSparseArray<ArrayList<Note>> _collectionRelatedNotes = new LongSparseArray<ArrayList<Note>>();
            while (_cursor.moveToNext()) {
              final long _tmpKey;
              _tmpKey = _cursor.getLong(_cursorIndexOfId);
              if (!_collectionRelatedNotes.containsKey(_tmpKey)) {
                _collectionRelatedNotes.put(_tmpKey, new ArrayList<Note>());
              }
            }
            _cursor.moveToPosition(-1);
            __fetchRelationshipnotesAscomExampleAppDataNote(_collectionRelatedNotes);
            final NoteWithRelations _result;
            if (_cursor.moveToFirst()) {
              final Note _tmpNote;
              final long _tmpId;
              _tmpId = _cursor.getLong(_cursorIndexOfId);
              final String _tmpTitle;
              if (_cursor.isNull(_cursorIndexOfTitle)) {
                _tmpTitle = null;
              } else {
                _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
              }
              final String _tmpSnippet;
              if (_cursor.isNull(_cursorIndexOfSnippet)) {
                _tmpSnippet = null;
              } else {
                _tmpSnippet = _cursor.getString(_cursorIndexOfSnippet);
              }
              final String _tmpTranscript;
              if (_cursor.isNull(_cursorIndexOfTranscript)) {
                _tmpTranscript = null;
              } else {
                _tmpTranscript = _cursor.getString(_cursorIndexOfTranscript);
              }
              final String _tmpAudioPath;
              if (_cursor.isNull(_cursorIndexOfAudioPath)) {
                _tmpAudioPath = null;
              } else {
                _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
              }
              final List<String> _tmpHighlights;
              final String _tmp;
              if (_cursor.isNull(_cursorIndexOfHighlights)) {
                _tmp = null;
              } else {
                _tmp = _cursor.getString(_cursorIndexOfHighlights);
              }
              _tmpHighlights = __converters.fromString(_tmp);
              final boolean _tmpIsFavorite;
              final int _tmp_1;
              _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
              _tmpIsFavorite = _tmp_1 != 0;
              final boolean _tmpIsArchived;
              final int _tmp_2;
              _tmp_2 = _cursor.getInt(_cursorIndexOfIsArchived);
              _tmpIsArchived = _tmp_2 != 0;
              final long _tmpCreatedAt;
              _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
              final Long _tmpReminderTime;
              if (_cursor.isNull(_cursorIndexOfReminderTime)) {
                _tmpReminderTime = null;
              } else {
                _tmpReminderTime = _cursor.getLong(_cursorIndexOfReminderTime);
              }
              final String _tmpChecklistState;
              if (_cursor.isNull(_cursorIndexOfChecklistState)) {
                _tmpChecklistState = null;
              } else {
                _tmpChecklistState = _cursor.getString(_cursorIndexOfChecklistState);
              }
              _tmpNote = new Note(_tmpId,_tmpTitle,_tmpSnippet,_tmpTranscript,_tmpAudioPath,_tmpHighlights,_tmpIsFavorite,_tmpIsArchived,_tmpCreatedAt,_tmpReminderTime,_tmpChecklistState);
              final ArrayList<Note> _tmpRelatedNotesCollection;
              final long _tmpKey_1;
              _tmpKey_1 = _cursor.getLong(_cursorIndexOfId);
              _tmpRelatedNotesCollection = _collectionRelatedNotes.get(_tmpKey_1);
              _result = new NoteWithRelations(_tmpNote,_tmpRelatedNotesCollection);
            } else {
              _result = null;
            }
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            _cursor.close();
          }
        } finally {
          __db.endTransaction();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }

  private void __fetchRelationshipnotesAscomExampleAppDataNote(
      @NonNull final LongSparseArray<ArrayList<Note>> _map) {
    if (_map.isEmpty()) {
      return;
    }
    if (_map.size() > RoomDatabase.MAX_BIND_PARAMETER_CNT) {
      RelationUtil.recursiveFetchLongSparseArray(_map, true, (map) -> {
        __fetchRelationshipnotesAscomExampleAppDataNote(map);
        return Unit.INSTANCE;
      });
      return;
    }
    final StringBuilder _stringBuilder = StringUtil.newStringBuilder();
    _stringBuilder.append("SELECT `notes`.`id` AS `id`,`notes`.`title` AS `title`,`notes`.`snippet` AS `snippet`,`notes`.`transcript` AS `transcript`,`notes`.`audioPath` AS `audioPath`,`notes`.`highlights` AS `highlights`,`notes`.`isFavorite` AS `isFavorite`,`notes`.`isArchived` AS `isArchived`,`notes`.`createdAt` AS `createdAt`,`notes`.`reminderTime` AS `reminderTime`,`notes`.`checklistState` AS `checklistState`,_junction.`noteId` FROM `note_cross_refs` AS _junction INNER JOIN `notes` ON (_junction.`relatedNoteId` = `notes`.`id`) WHERE _junction.`noteId` IN (");
    final int _inputSize = _map.size();
    StringUtil.appendPlaceholders(_stringBuilder, _inputSize);
    _stringBuilder.append(")");
    final String _sql = _stringBuilder.toString();
    final int _argCount = 0 + _inputSize;
    final RoomSQLiteQuery _stmt = RoomSQLiteQuery.acquire(_sql, _argCount);
    int _argIndex = 1;
    for (int i = 0; i < _map.size(); i++) {
      final long _item = _map.keyAt(i);
      _stmt.bindLong(_argIndex, _item);
      _argIndex++;
    }
    final Cursor _cursor = DBUtil.query(__db, _stmt, false, null);
    try {
      // _junction.noteId;
      final int _itemKeyIndex = 11;
      if (_itemKeyIndex == -1) {
        return;
      }
      final int _cursorIndexOfId = 0;
      final int _cursorIndexOfTitle = 1;
      final int _cursorIndexOfSnippet = 2;
      final int _cursorIndexOfTranscript = 3;
      final int _cursorIndexOfAudioPath = 4;
      final int _cursorIndexOfHighlights = 5;
      final int _cursorIndexOfIsFavorite = 6;
      final int _cursorIndexOfIsArchived = 7;
      final int _cursorIndexOfCreatedAt = 8;
      final int _cursorIndexOfReminderTime = 9;
      final int _cursorIndexOfChecklistState = 10;
      while (_cursor.moveToNext()) {
        final long _tmpKey;
        _tmpKey = _cursor.getLong(_itemKeyIndex);
        final ArrayList<Note> _tmpRelation = _map.get(_tmpKey);
        if (_tmpRelation != null) {
          final Note _item_1;
          final long _tmpId;
          _tmpId = _cursor.getLong(_cursorIndexOfId);
          final String _tmpTitle;
          if (_cursor.isNull(_cursorIndexOfTitle)) {
            _tmpTitle = null;
          } else {
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
          }
          final String _tmpSnippet;
          if (_cursor.isNull(_cursorIndexOfSnippet)) {
            _tmpSnippet = null;
          } else {
            _tmpSnippet = _cursor.getString(_cursorIndexOfSnippet);
          }
          final String _tmpTranscript;
          if (_cursor.isNull(_cursorIndexOfTranscript)) {
            _tmpTranscript = null;
          } else {
            _tmpTranscript = _cursor.getString(_cursorIndexOfTranscript);
          }
          final String _tmpAudioPath;
          if (_cursor.isNull(_cursorIndexOfAudioPath)) {
            _tmpAudioPath = null;
          } else {
            _tmpAudioPath = _cursor.getString(_cursorIndexOfAudioPath);
          }
          final List<String> _tmpHighlights;
          final String _tmp;
          if (_cursor.isNull(_cursorIndexOfHighlights)) {
            _tmp = null;
          } else {
            _tmp = _cursor.getString(_cursorIndexOfHighlights);
          }
          _tmpHighlights = __converters.fromString(_tmp);
          final boolean _tmpIsFavorite;
          final int _tmp_1;
          _tmp_1 = _cursor.getInt(_cursorIndexOfIsFavorite);
          _tmpIsFavorite = _tmp_1 != 0;
          final boolean _tmpIsArchived;
          final int _tmp_2;
          _tmp_2 = _cursor.getInt(_cursorIndexOfIsArchived);
          _tmpIsArchived = _tmp_2 != 0;
          final long _tmpCreatedAt;
          _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
          final Long _tmpReminderTime;
          if (_cursor.isNull(_cursorIndexOfReminderTime)) {
            _tmpReminderTime = null;
          } else {
            _tmpReminderTime = _cursor.getLong(_cursorIndexOfReminderTime);
          }
          final String _tmpChecklistState;
          if (_cursor.isNull(_cursorIndexOfChecklistState)) {
            _tmpChecklistState = null;
          } else {
            _tmpChecklistState = _cursor.getString(_cursorIndexOfChecklistState);
          }
          _item_1 = new Note(_tmpId,_tmpTitle,_tmpSnippet,_tmpTranscript,_tmpAudioPath,_tmpHighlights,_tmpIsFavorite,_tmpIsArchived,_tmpCreatedAt,_tmpReminderTime,_tmpChecklistState);
          _tmpRelation.add(_item_1);
        }
      }
    } finally {
      _cursor.close();
    }
  }
}
