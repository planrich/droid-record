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
// This file is generated. If you want to save you some time: !!!DO NOT MODIFY!!!
package at.pasra.record.sample.generate;

import at.pasra.record.SQLiteConverter;
import at.pasra.record.RecordBuilder;

public class AbstractUser{
    protected java.lang.String mFirstName;
    protected java.lang.String mLastName;
    protected java.lang.Long mId;
    
    public AbstractUser(java.lang.Long id){
        this.mId = id;
        this.mFirstName = "";
        this.mLastName = "";
    }
    
    public java.lang.String getFirstName() { return mFirstName; }
    public void setFirstName(java.lang.String value) { mFirstName = value; }
    public java.lang.String getLastName() { return mLastName; }
    public void setLastName(java.lang.String value) { mLastName = value; }
    public java.lang.Long getId() { return mId; }
    public void setId(java.lang.Long value) { mId = value; }
    public Gallery loadGallery(LocalSession session){
        return session.queryGalleries().where("user_id = ?", Long.toString(mId)).limit(1).first();
    }
    public java.util.List<Picture> loadPictures(LocalSession session){
        String query = "select d.* from users o, user_pictures t, pictures d" +
                       " where" +
                       " o._id = ? and" +
                       " o._id = t.user_id and" +
                       " d._id = t.picture_id";
        android.database.Cursor c = session.queryRaw(query, Long.toString(mId));
        RecordBuilder<Picture> rb = session.queryPictures();
        return rb.all(c);
    }
    public static User fromCursor(android.database.Cursor cursor){
        User record = new User();
        record.setFirstName(cursor.getString(cursor.getColumnIndex("first_name")));
        record.setLastName(cursor.getString(cursor.getColumnIndex("last_name")));
        record.setId(cursor.getLong(cursor.getColumnIndex("_id")));
        return record;
    }
}
