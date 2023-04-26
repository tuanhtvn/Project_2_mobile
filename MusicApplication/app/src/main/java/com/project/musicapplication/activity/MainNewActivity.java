package com.project.musicapplication.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.project.musicapplication.R;
import com.project.musicapplication.firebase.firebaseObject;
import com.project.musicapplication.fragment.homeFragment;
import com.project.musicapplication.fragment.personalFragment;
import com.project.musicapplication.fragment.playlistFragment;
import com.project.musicapplication.model.Song;
import com.project.musicapplication.service.DanMusicPlayerService;
import com.project.musicapplication.util.StaticValue;
import com.project.musicapplication.util.enumMusicActionCode;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;

public class MainNewActivity extends AppCompatActivity {
    private static final int LOGIN_REQUEST_CODE = 1;
    ImageView imageNav;
    DrawerLayout drawerLayout;
    BottomNavigationView bottomNavigationView;
    NavigationView navigationView;
    Menu menu;
    MenuItem loginMenuItem, logoutMenuItem;
    LinearLayout notification;
    ImageView imgPlayOrPause, img_song, img_pre, img_next;
    TextView tv_title_song, tv_single_song;
    Fragment fragment;
    SearchView searchView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_new);
        StaticValue.mainContext = getApplicationContext();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.page_trangchu);
        img_song = findViewById(R.id.img_song);
        img_pre = findViewById(R.id.img_pre);
        tv_single_song = findViewById(R.id.tv_single_song);
        tv_title_song = findViewById(R.id.tv_title_song);
        navigationView = findViewById(R.id.navigation_view);
        imageNav = findViewById(R.id.image_nav);
        imgPlayOrPause = findViewById(R.id.img_play_or_pause);
        drawerLayout = findViewById(R.id.drawer_layout);
        searchView = findViewById(R.id.search_view);

        fragment = new homeFragment();
        loadFragment(fragment);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.page_trangchu:
                        // Thực hiện các hành động khi người dùng chọn trang chủ
                        fragment = new homeFragment();
                        break;
                    case R.id.page_playlist:
                        // Thực hiện các hành động khi người dùng chọn playlist
                        fragment = new playlistFragment();
                        break;
                    case R.id.page_canhan:
                        // Thực hiện các hành động khi người dùng chọn cá nhân
                        fragment = new personalFragment();
                        break;
                    default:
                        return false;
                }
                loadFragment(fragment);
                return true;
            }
        });

        menu = navigationView.getMenu();
        loginMenuItem = menu.findItem(R.id.login);
        logoutMenuItem = menu.findItem(R.id.logout);
        loginMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
//                ActivityUtil.openActivity(MainNewActivity.this, LoginActivity.class);
                Intent intent = new Intent(MainNewActivity.this, LoginActivity.class);
                startActivityForResult(intent, LOGIN_REQUEST_CODE);
                return true;
            }
        });
        logoutMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(@NonNull MenuItem item) {
                firebaseObject.myUser = null;
                FirebaseAuth.getInstance().signOut();
                firebaseObject.mAuth = FirebaseAuth.getInstance();
                firebaseObject.user = firebaseObject.mAuth.getCurrentUser();
                firebaseObject.db = FirebaseFirestore.getInstance();
                Toast.makeText( MainNewActivity.this, "Logout success", Toast.LENGTH_LONG).show();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        imageNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            }
        });

        imgPlayOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(StaticValue.curAction == enumMusicActionCode.INIT || StaticValue.curAction == null)
                    return;
                Intent intent = new Intent(StaticValue.mainContext, DanMusicPlayerService.class);
                if(StaticValue.curAction == enumMusicActionCode.PAUSE){
                    intent.setAction(String.valueOf(enumMusicActionCode.RESUME));
                } else
                    intent.setAction(String.valueOf(enumMusicActionCode.PAUSE));
                StaticValue.mainContext.startService(intent);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterSong(newText.toLowerCase());
                return true;
            }
        });

    }

    private void filterSong(String query) {
        List<Song> filteredList= new ArrayList<>();
        if(StaticValue.mSong.size() > 0){
            for (Song song: StaticValue.mSong){
                if (song.getName().toLowerCase().contains(query)){
                    filteredList.add(song);
                }
            }
        }
        if (StaticValue.songAdapter != null){
            StaticValue.songAdapter.filterSongs(filteredList);
        }

    }

    public void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        StaticValue.mainContext = null;
        StaticValue.mMediaPlayer.release();
    }

    private BroadcastReceiver musicPlayerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null) {
                img_song.setImageResource(R.drawable.img_cd);
                tv_single_song.setText(StaticValue.mCurrentSong.getSinger());
                tv_title_song.setText(StaticValue.mCurrentSong.getName());
                switch (enumMusicActionCode.valueOf(intent.getAction())) {
                    case PLAY:
                        // Update UI to show that music is playing
                        imgPlayOrPause.setImageResource(R.drawable.ic_pause);
                        StaticValue.curAction = enumMusicActionCode.PLAY;
                        Log.i("PLAY song", "Play song ");
                        break;
                    case PAUSE:
                        // Update UI to show that music is paused
                        imgPlayOrPause.setImageResource(R.drawable.ic_play);
                        StaticValue.curAction = enumMusicActionCode.PAUSE;
                        Log.i("PAUSE playing", "Pause playing song");
                        break;
                    case RESUME:
                        // Update UI to show that music is resumed
                        imgPlayOrPause.setImageResource(R.drawable.ic_pause);
                        StaticValue.curAction = enumMusicActionCode.RESUME;
                        Log.i("RESUME playing", "Resume playing song");
                        break;
                    case NEXT:
                        // Update UI to show the next song
                        break;
                    case PRE:
                        // Update UI to show the previous song
                        break;
                    case INIT:
                        // Update UI to show that music is initialized
                        break;
                }
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(enumMusicActionCode.PLAY.name());
        filter.addAction(enumMusicActionCode.PAUSE.name());
        filter.addAction(enumMusicActionCode.RESUME.name());
        filter.addAction(enumMusicActionCode.NEXT.name());
        filter.addAction(enumMusicActionCode.PRE.name());
        filter.addAction(enumMusicActionCode.INIT.name());
        registerReceiver(musicPlayerReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(musicPlayerReceiver);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check if the result is from the LoginActivity
        if (requestCode == LOGIN_REQUEST_CODE) {
            // Check if the login was successful
            if (resultCode == RESULT_OK) {
                // Refresh the MainActivity to display the user's information
                recreate();
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        }
    }

}