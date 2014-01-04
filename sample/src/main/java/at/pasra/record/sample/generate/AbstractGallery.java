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
// This file is generated. If you want to save you some time: !!!DO NOT MODIFY!!!
package at.pasra.record.sample.generate;

import at.pasra.record.SQLiteConverter;
import at.pasra.record.RecordBuilder;

public class AbstractGallery{
    protected java.lang.String mName;
    protected java.lang.Long mId;
    protected java.lang.Long mUserId;
    
    public AbstractGallery(java.lang.Long id){
        this.mId = id;
        this.mName = "";
        this.mUserId = new Long(0L);
    }
    
    public java.lang.String getName() { return mName; }
    public void setName(java.lang.String value) { mName = value; }
    public java.lang.Long getId() { return mId; }
    public void setId(java.lang.Long value) { mId = value; }
    public java.lang.Long getUserId() { return mUserId; }
    public void setUserId(java.lang.Long value) { mUserId = value; }
    public RecordBuilder<Picture> loadPictures(LocalSession session){
        return session.queryPictures().where("gallery_id = ?", Long.toString(mId) );
    }
    public User loadUser(LocalSession session){
        return session.findUser(this.getId());
    }
    public static Gallery of(User obj0){
        Gallery obj = new Gallery();
        obj.setUserId(obj0.getId());
        return obj;
    }
    public static Gallery fromCursor(android.database.Cursor cursor){
        Gallery record = new Gallery();
        record.setId(cursor.getLong(0));
        record.setName(cursor.getString(1));
        record.setUserId(cursor.getLong(2));
        return record;
    }
}
