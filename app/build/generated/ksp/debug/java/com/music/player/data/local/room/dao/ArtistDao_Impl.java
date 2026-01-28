package com.music.player.data.local.room.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.music.player.data.local.room.entity.ArtistEntity;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
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
public final class ArtistDao_Impl implements ArtistDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ArtistEntity> __insertionAdapterOfArtistEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public ArtistDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfArtistEntity = new EntityInsertionAdapter<ArtistEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `artists` (`id`,`name`,`numberOfTracks`,`numberOfAlbums`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ArtistEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getNumberOfTracks());
        statement.bindLong(4, entity.getNumberOfAlbums());
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM artists";
        return _query;
      }
    };
  }

  @Override
  public Object insertArtist(final ArtistEntity artist,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfArtistEntity.insertAndReturnId(artist);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
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
          __preparedStmtOfClearAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ArtistEntity>> getAllArtists() {
    final String _sql = "SELECT * FROM artists ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"artists"}, new Callable<List<ArtistEntity>>() {
      @Override
      @NonNull
      public List<ArtistEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfNumberOfTracks = CursorUtil.getColumnIndexOrThrow(_cursor, "numberOfTracks");
          final int _cursorIndexOfNumberOfAlbums = CursorUtil.getColumnIndexOrThrow(_cursor, "numberOfAlbums");
          final List<ArtistEntity> _result = new ArrayList<ArtistEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ArtistEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpNumberOfTracks;
            _tmpNumberOfTracks = _cursor.getInt(_cursorIndexOfNumberOfTracks);
            final int _tmpNumberOfAlbums;
            _tmpNumberOfAlbums = _cursor.getInt(_cursorIndexOfNumberOfAlbums);
            _item = new ArtistEntity(_tmpId,_tmpName,_tmpNumberOfTracks,_tmpNumberOfAlbums);
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
  public Object getArtistById(final long id, final Continuation<? super ArtistEntity> $completion) {
    final String _sql = "SELECT * FROM artists WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ArtistEntity>() {
      @Override
      @Nullable
      public ArtistEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfNumberOfTracks = CursorUtil.getColumnIndexOrThrow(_cursor, "numberOfTracks");
          final int _cursorIndexOfNumberOfAlbums = CursorUtil.getColumnIndexOrThrow(_cursor, "numberOfAlbums");
          final ArtistEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpNumberOfTracks;
            _tmpNumberOfTracks = _cursor.getInt(_cursorIndexOfNumberOfTracks);
            final int _tmpNumberOfAlbums;
            _tmpNumberOfAlbums = _cursor.getInt(_cursorIndexOfNumberOfAlbums);
            _result = new ArtistEntity(_tmpId,_tmpName,_tmpNumberOfTracks,_tmpNumberOfAlbums);
          } else {
            _result = null;
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
  public Object getArtistByName(final String name,
      final Continuation<? super ArtistEntity> $completion) {
    final String _sql = "SELECT * FROM artists WHERE name = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, name);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ArtistEntity>() {
      @Override
      @Nullable
      public ArtistEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfNumberOfTracks = CursorUtil.getColumnIndexOrThrow(_cursor, "numberOfTracks");
          final int _cursorIndexOfNumberOfAlbums = CursorUtil.getColumnIndexOrThrow(_cursor, "numberOfAlbums");
          final ArtistEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpNumberOfTracks;
            _tmpNumberOfTracks = _cursor.getInt(_cursorIndexOfNumberOfTracks);
            final int _tmpNumberOfAlbums;
            _tmpNumberOfAlbums = _cursor.getInt(_cursorIndexOfNumberOfAlbums);
            _result = new ArtistEntity(_tmpId,_tmpName,_tmpNumberOfTracks,_tmpNumberOfAlbums);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
