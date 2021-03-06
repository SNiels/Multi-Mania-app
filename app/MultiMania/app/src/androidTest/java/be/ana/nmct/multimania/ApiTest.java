package be.ana.nmct.multimania;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.google.gson.reflect.TypeToken;

import junit.framework.Assert;

import java.util.List;
import java.util.concurrent.ExecutionException;

import be.ana.nmct.multimania.data.ApiActions;
import be.ana.nmct.multimania.data.GsonLoader;
import be.ana.nmct.multimania.model.NewsItem;
import be.ana.nmct.multimania.model.Room;
import be.ana.nmct.multimania.model.Speaker;
import be.ana.nmct.multimania.model.Tag;
import be.ana.nmct.multimania.model.Talk;
import be.ana.nmct.multimania.model.TalkSpeaker;
import be.ana.nmct.multimania.model.TalkTag;

/**
 * ApiTest contains all test to regarding communication with the online API
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApiTest extends ApplicationTestCase<Application> {
    public static final String TESTUSERID="testuserid";

    public ApiTest() {
        super(Application.class);
    }

    public void testNews(){
        List<NewsItem> news = new GsonLoader<NewsItem>(mContext,NewsItem.SEGMENT,new TypeToken<List<NewsItem>>(){}).loadInBackground();
        assertNotNull(news);
    }

    public void testTags(){
       List<Tag> tags = new GsonLoader<Tag>(mContext,Tag.SEGMENT,new TypeToken<List<Tag>>(){}).loadInBackground();
       assertNotNull(tags);
    }

    public void testRooms(){
        List<Room> rooms = new GsonLoader<Room>(mContext,Room.SEGMENT,new TypeToken<List<Room>>(){}).loadInBackground();
        assertNotNull(rooms);
    }

    public void testSpeakers(){
        List<Speaker> speakers = new GsonLoader<Speaker>(mContext,Speaker.SEGMENT,new TypeToken<List<Speaker>>(){}).loadInBackground();
        assertNotNull(speakers);
    }

    public void testTalks(){
        List<Talk> talks = new GsonLoader<Talk>(mContext,Talk.SEGMENT,new TypeToken<List<Talk>>(){}).loadInBackground();
        assertNotNull(talks);
    }

    public void testTalkTags(){
        List<TalkTag> talktags = new GsonLoader<TalkTag>(mContext,TalkTag.SEGMENT,new TypeToken<List<TalkTag>>(){}).loadInBackground();
        assertNotNull(talktags);
    }

    public void testTalkSpeakers(){
        List<TalkSpeaker> talkspeakers = new GsonLoader<TalkSpeaker>(mContext,TalkSpeaker.SEGMENT,new TypeToken<List<TalkSpeaker>>(){}).loadInBackground();
        assertNotNull(talkspeakers);
    }

    public void testPostUser() throws ExecutionException, InterruptedException {
        String userId = ApiActions.postUser(mContext,TESTUSERID)
                .get();
        Assert.assertEquals(userId,TESTUSERID);
    }

    public void testPostFavoriteTalk() throws ExecutionException, InterruptedException {
        String status = ApiActions.deleteFavoriteTalk(mContext, TESTUSERID, 1)
                .get();
        Assert.assertEquals(status,"success");
    }

    public void testUserTalks(){
        List<Talk> talks = new GsonLoader<Talk>(mContext,"users/"+TESTUSERID+"/talks",new TypeToken<List<Talk>>(){}).loadInBackground();
        assertNotNull(talks);
    }

    public void testDeleteFavoriteTalk() throws ExecutionException, InterruptedException {
        String status = ApiActions.deleteFavoriteTalk(mContext,TESTUSERID,1)
                .get();
        Assert.assertEquals(status,"success");
    }

    public void testGetLastUpdated() throws ExecutionException, InterruptedException {
        String datetime = ApiActions.getLastUpdated(mContext,TESTUSERID)
                .get();
        Assert.assertNotNull(datetime);
    }
}
