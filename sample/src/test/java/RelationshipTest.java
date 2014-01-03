import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.Date;
import java.util.List;

import at.pasra.record.sample.generate.Gallery;
import at.pasra.record.sample.generate.LocalSession;
import at.pasra.record.sample.generate.Picture;
import at.pasra.record.sample.generate.RecordMigrator;
import at.pasra.record.sample.generate.User;
import at.pasra.record.sample.generate.UserPicture;

/**
 * Created by rich on 10/1/13.
 */
public class RelationshipTest  extends AndroidTestCase {

    private SQLiteDatabase mDB;
    private LocalSession mSession;
    private Gallery gallery1;
    private User user1;
    private Picture picture1;
    private String user1FirstName = "rich";
    private String user1LastName = "plan";
    private String picture1Name = "void 0x00";
    private User user2;
    private String user2FirstName = "richard";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mDB = getContext().openOrCreateDatabase("test", Context.MODE_PRIVATE, null);
        new RecordMigrator(mDB).migrate();
        mSession = new LocalSession(mDB);

        user1 = new User();
        user1.setFirstName(user1FirstName);
        user1.setLastName(user1LastName);
        mSession.saveUser(user1);

        user2 = new User();
        user2.setFirstName(user2FirstName);
        mSession.saveUser(user2);

        gallery1 = Gallery.of(user1);
        gallery1.setName("pixelart");
        mSession.saveGallery(gallery1);

        picture1 = Picture.of(gallery1);
        picture1.setImage(new byte[] { 0x00 });
        picture1.setName(picture1Name);
        picture1.setDate(new Date());
        mSession.savePicture(picture1);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        getContext().deleteDatabase("test");
    }

    public void testHasOne() {
        long id = gallery1.getId();
        gallery1 = user1.loadGallery(mSession);
        assertEquals(gallery1.getId(), new Long(id));
    }

    public void testHasAndBelongsToManyEmpty_MustReturnEmptyList() {
        assertEquals(0, user1.loadPictures(mSession).size());
    }

    public void testHasAndBelongsToHasManyAddOne_MustReturnOne() {
        mSession.saveUserPicture(UserPicture.of(user1, picture1));

        List<Picture> pictures = user1.loadPictures(mSession);
        List<User> users = picture1.loadUsers(mSession);
        assertEquals(1, pictures.size());
        assertEquals(1, users.size());

        User user = users.get(0);
        assertEquals(user.getFirstName(), user1FirstName);
        assertEquals(user.getLastName(), user1LastName);

        Picture picture = pictures.get(0);
        assertEquals(picture.getName(), picture1Name);

    }

    public void testBelongsToAndHasManyFirstEmptyThen2_MustSucceed() {
        List<Picture> pictures = user2.loadPictures(mSession);
        assertEquals(0, pictures.size());

        mSession.saveUserPicture(UserPicture.of(user2, picture1));
        mSession.saveUserPicture(UserPicture.of(user2, picture1));

        pictures = user2.loadPictures(mSession);
        assertEquals(2, pictures.size());
    }
}
