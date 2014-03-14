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

import android.database.sqlite.SQLiteDatabase;
import at.pasra.record.RecordBuilder;

public class LocalSession{
    private SQLiteDatabase mDB;
    private final GalleryRecord gallery_record = new GalleryRecord();
    private final PictureRecord picture_record = new PictureRecord();
    private final UserRecord user_record = new UserRecord();
    private final UserPictureRecord user_picture_record = new UserPictureRecord();
    private final TimeRecord time_record = new TimeRecord();
    public LocalSession(SQLiteDatabase database){
        this.mDB = database;
    }
    public void saveGallery(Gallery obj){
        if (obj == null){
            throw new IllegalArgumentException("Tried to save an instance of Gallery which was null. Cannot do that!");
        }
        gallery_record.save(mDB, obj);
    }
    public Gallery findGallery(java.lang.Long id){
        if (id == null){
            throw new IllegalArgumentException("why would you want to load a gallery record with a null key?");
        }
        return gallery_record.load(mDB, id);
    }
    public void destroyGallery(Gallery obj){
        if (obj == null){
            throw new IllegalArgumentException("why would you want to delete a gallery record with a null obj?");
        }
        gallery_record.delete(mDB, obj.getId());
    }
    public GalleryRecordBuilder queryGalleries(){
        return new GalleryRecordBuilder(mDB);
    }
    public void savePicture(Picture obj){
        if (obj == null){
            throw new IllegalArgumentException("Tried to save an instance of Picture which was null. Cannot do that!");
        }
        picture_record.save(mDB, obj);
    }
    public Picture findPicture(java.lang.Long id){
        if (id == null){
            throw new IllegalArgumentException("why would you want to load a picture record with a null key?");
        }
        return picture_record.load(mDB, id);
    }
    public void destroyPicture(Picture obj){
        if (obj == null){
            throw new IllegalArgumentException("why would you want to delete a picture record with a null obj?");
        }
        picture_record.delete(mDB, obj.getId());
    }
    public PictureRecordBuilder queryPictures(){
        return new PictureRecordBuilder(mDB);
    }
    public void saveUser(User obj){
        if (obj == null){
            throw new IllegalArgumentException("Tried to save an instance of User which was null. Cannot do that!");
        }
        user_record.save(mDB, obj);
    }
    public User findUser(java.lang.Long id){
        if (id == null){
            throw new IllegalArgumentException("why would you want to load a user record with a null key?");
        }
        return user_record.load(mDB, id);
    }
    public void destroyUser(User obj){
        if (obj == null){
            throw new IllegalArgumentException("why would you want to delete a user record with a null obj?");
        }
        user_record.delete(mDB, obj.getId());
    }
    public UserRecordBuilder queryUsers(){
        return new UserRecordBuilder(mDB);
    }
    public void saveUserPicture(UserPicture obj){
        if (obj == null){
            throw new IllegalArgumentException("Tried to save an instance of UserPicture which was null. Cannot do that!");
        }
        user_picture_record.save(mDB, obj);
    }
    public UserPicture findUserPicture(java.lang.Long id){
        if (id == null){
            throw new IllegalArgumentException("why would you want to load a user_picture record with a null key?");
        }
        return user_picture_record.load(mDB, id);
    }
    public void destroyUserPicture(UserPicture obj){
        if (obj == null){
            throw new IllegalArgumentException("why would you want to delete a user_picture record with a null obj?");
        }
        user_picture_record.delete(mDB, obj.getId());
    }
    public UserPictureRecordBuilder queryUserPictures(){
        return new UserPictureRecordBuilder(mDB);
    }
    public void saveTime(Time obj){
        if (obj == null){
            throw new IllegalArgumentException("Tried to save an instance of Time which was null. Cannot do that!");
        }
        time_record.save(mDB, obj);
    }
    public Time findTime(java.lang.Long id){
        if (id == null){
            throw new IllegalArgumentException("why would you want to load a time record with a null key?");
        }
        return time_record.load(mDB, id);
    }
    public void destroyTime(Time obj){
        if (obj == null){
            throw new IllegalArgumentException("why would you want to delete a time record with a null obj?");
        }
        time_record.delete(mDB, obj.getId());
    }
    public TimeRecordBuilder queryTimes(){
        return new TimeRecordBuilder(mDB);
    }
    public void clearCache(){
        gallery_record.clearCache();
        picture_record.clearCache();
        user_record.clearCache();
        user_picture_record.clearCache();
        time_record.clearCache();
    }
    public GalleryRecord getGalleryRecord(){
        return gallery_record;
    }
    public PictureRecord getPictureRecord(){
        return picture_record;
    }
    public UserRecord getUserRecord(){
        return user_record;
    }
    public UserPictureRecord getUserPictureRecord(){
        return user_picture_record;
    }
    public TimeRecord getTimeRecord(){
        return time_record;
    }
    public android.database.Cursor queryRaw(String query, String ... args){
        return mDB.rawQuery(query, args);
    }
}
