/* Copyright (c) 2013, Richard Plangger <rich@pasra.at> All rights reserved.
 *
 * Android Record version 0.0.5 generated this file. For more
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

public class PictureRecord{
    private final java.util.Map<Long, Picture> primaryKeyCache = new java.util.HashMap<Long, Picture>();
    public void clearCache(){
        primaryKeyCache.clear();
    }
    public void save(SQLiteDatabase db, AbstractPicture record){
        if (record.getId() == null){
            insert(db, record);
        }
        else{
            update(db, record);
        }
    }
    public void insert(SQLiteDatabase db, AbstractPicture record){
        ContentValues values = new ContentValues(4);
        values.put("name", record.getName());
        values.put("image", record.getImage());
        values.put("date", record.getDate().getTime());
        values.put("gallery_id", record.getGalleryId());
        long id = db.insert("pictures", null, values);
        record.setId(id);
        primaryKeyCache.put(id, (Picture)record);
    }
    public Picture load(SQLiteDatabase db, long id){
        Picture cached = primaryKeyCache.get(id);
        if (cached != null){
            return cached;
        }
        Cursor c = db.rawQuery("select * from pictures where _id = ?;", new String[] { Long.toString(id) });
        if (c.moveToFirst()){
            Picture record = new Picture();
            record.setId(c.getLong(0));
            record.setName(c.getString(1));
            record.setImage(c.getBlob(2));
            record.setDate(new java.util.Date(c.getLong(3)));
            record.setGalleryId(c.getLong(4));
            primaryKeyCache.put(id, record);
            return record;
        }
        return null;
    }
    public void delete(SQLiteDatabase db, long id){
        db.execSQL("delete from pictures where  _id = ?;", new String[] { Long.toString(id) });
        primaryKeyCache.remove(id);
    }
    public void update(SQLiteDatabase db, AbstractPicture record){
        ContentValues values = new ContentValues(4);
        values.put("name", record.getName());
        values.put("image", record.getImage());
        values.put("date", record.getDate().getTime());
        values.put("gallery_id", record.getGalleryId());
        long id = record.getId();
        db.update("pictures", values, "_id = ?", new String[] { Long.toString(id) });
    }
}
