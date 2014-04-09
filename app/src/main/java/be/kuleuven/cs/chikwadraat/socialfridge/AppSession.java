package be.kuleuven.cs.chikwadraat.socialfridge;

import android.content.Context;
import android.content.SharedPreferences;

import be.kuleuven.cs.chikwadraat.socialfridge.endpoint.model.User;

/**
 * Created by Mattias on 1/04/2014.
 */
public class AppSession {

    private User user;

    private SharedPreferences cache;

    private static final String CACHE_KEY = "session_cache";
    private static final String CACHE_USER_ID = "user_id";
    private static final String CACHE_USER_NAME = "user_name";

    public AppSession(Context context) {
        cache = context.getApplicationContext().getSharedPreferences(CACHE_KEY, Context.MODE_PRIVATE);
    }

    public boolean isActive() {
        return getUser() != null;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
        write();
    }

    public void clear() {
        setUser(null);
    }

    public void onResume() {
        read();
    }

    public void onPause() {
        write();
    }

    private void read() {
        String userId = cache.getString(CACHE_USER_ID, null);
        String userName = cache.getString(CACHE_USER_NAME, null);
        if (userId != null && userName != null) {
            User user = new User();
            user.setId(userId);
            user.setName(userName);
            setUser(user);
        } else {
            setUser(null);
        }
    }

    private void write() {
        SharedPreferences.Editor editor = cache.edit();

        User user = getUser();
        if (user != null) {
            editor.putString(CACHE_USER_ID, user.getId());
            editor.putString(CACHE_USER_NAME, user.getName());
        } else {
            editor.remove(CACHE_USER_ID);
            editor.remove(CACHE_USER_NAME);
        }

        editor.commit();
    }

}
