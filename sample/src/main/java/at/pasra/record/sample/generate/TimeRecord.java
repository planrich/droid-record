/* Copyright (c) 2013, Richard Plangger <rich@pasra.at> All rights reserved.
 *
 * Android Record version 0.1.0 generated this file. For more
 * information see http://record.pasra.at/
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This copyright notice must not be modified or deleted.
 */
package at.pasra.record.sample.generate;

import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.database.Cursor;
import at.pasra.record.SQLiteConverter;

public class TimeRecord{
    private final java.util.Map<Long, Time> primaryKeyCache = new java.util.HashMap<Long, Time>();
    public void clearCache(){
        primaryKeyCache.clear();
    }
    public void save(SQLiteDatabase db, AbstractTime record){
        if (record.getId() == null){
            insert(db, record);
        }
        else{
            update(db, record);
        }
    }
    public void insert(SQLiteDatabase db, AbstractTime record){
        ContentValues values = new ContentValues(2);
        values.put("millis", Double.doubleToLongBits(record.getMillis()));
        values.put("micros", record.getMicros());
        long id = db.insert("times", null, values);
        record.setId(id);
        primaryKeyCache.put(id, (Time)record);
    }
    public Time load(SQLiteDatabase db, long id){
        Time cached = primaryKeyCache.get(id);
        if (cached != null){
            return cached;
        }
        Cursor c = db.rawQuery("select millis, micros, _id from times where _id = ?;", new String[] { Long.toString(id) });
        if (c.moveToFirst()){
            Time record = new Time();
            record.setMillis(Double.longBitsToDouble(c.getLong(c.getColumnIndex("millis"))));
            record.setMicros(c.getLong(c.getColumnIndex("micros")));
            record.setId(c.getLong(c.getColumnIndex("_id")));
            primaryKeyCache.put(id, record);
            return record;
        }
        return null;
    }
    public void delete(SQLiteDatabase db, long id){
        db.execSQL("delete from times where  _id = ?;", new String[] { Long.toString(id) });
        primaryKeyCache.remove(id);
    }
    public void update(SQLiteDatabase db, AbstractTime record){
        ContentValues values = new ContentValues(2);
        values.put("millis", Double.doubleToLongBits(record.getMillis()));
        values.put("micros", record.getMicros());
        long id = record.getId();
        db.update("times", values, "_id = ?", new String[] { Long.toString(id) });
    }
}
