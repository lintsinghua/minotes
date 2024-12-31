/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.micode.notes.ui;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.DataColumns;
import net.micode.notes.data.Notes.DataConstants;
import net.micode.notes.data.Notes.NoteColumns;
import net.micode.notes.tool.DataUtils;

public class WorkingNote {
    private static final String TAG = "WorkingNote";

    private Context mContext;
    private ContentResolver mContentResolver;
    private Cursor mNote;
    private long mId;
    private String mContent;
    private long mAlertDate;
    private long mModifiedDate;
    private int mBgColorId;
    private int mWidgetId;
    private int mWidgetType;
    private long mFolderId;
    private boolean mIsPrivate;
    private String mPrivatePassword;

    /**
     * 验证笔记密码
     * @param password 要验证的密码
     * @return 如果密码正确返回true，否则返回false
     */
    public boolean validatePassword(String password) {
        if (mNote == null) {
            return false;
        }
        int passwordColumnIndex = mNote.getColumnIndex(NoteColumns.PRIVATE_PASSWORD);
        if (passwordColumnIndex == -1) {
            return false;
        }
        String storedPassword = mNote.getString(passwordColumnIndex);
        return storedPassword != null && storedPassword.equals(password);
    }

    /**
     * 设置笔记为加密状态
     * @param password 加密密码
     */
    public void setPrivatePassword(String password) {
        mPrivatePassword = password;
        mIsPrivate = true;
        ContentValues values = new ContentValues();
        values.put(NoteColumns.PRIVATE_PASSWORD, password);
        values.put(NoteColumns.IS_PRIVATE, 1);
        mContentResolver.update(Notes.CONTENT_NOTE_URI, values, 
            NoteColumns.ID + "=?", new String[]{String.valueOf(mId)});
    }

    /**
     * 取消笔记的加密状态
     */
    public void clearPrivatePassword() {
        mPrivatePassword = null;
        mIsPrivate = false;
        ContentValues values = new ContentValues();
        values.putNull(NoteColumns.PRIVATE_PASSWORD);
        values.put(NoteColumns.IS_PRIVATE, 0);
        mContentResolver.update(Notes.CONTENT_NOTE_URI, values, 
            NoteColumns.ID + "=?", new String[]{String.valueOf(mId)});
    }

    public boolean isPrivate() {
        return mIsPrivate;
    }

    public static WorkingNote load(Context context, long id) {
        return new WorkingNote(context, id);
    }

    private WorkingNote(Context context, long id) {
        mContext = context;
        mContentResolver = context.getContentResolver();
        mId = id;
        // 加载笔记数据
        loadNote();
    }

    private void loadNote() {
        mNote = mContentResolver.query(Notes.CONTENT_NOTE_URI, null,
            NoteColumns.ID + "=?", new String[]{String.valueOf(mId)}, null);
        if (mNote != null && mNote.moveToFirst()) {
            mIsPrivate = mNote.getInt(mNote.getColumnIndex(NoteColumns.IS_PRIVATE)) == 1;
            mPrivatePassword = mNote.getString(mNote.getColumnIndex(NoteColumns.PRIVATE_PASSWORD));
            // 加载其他数据...
        }
    }
} 