package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chatapp.Fragments.ChatsFragment;
import com.example.chatapp.Fragments.ProfileFragment;
import com.example.chatapp.Fragments.UsersFragment;
import com.example.chatapp.Model.User;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username;

    FirebaseUser firebaseUser;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //setting the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");

        profile_image = findViewById(R.id.profile_image);
        username = findViewById(R.id.username);


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //what happens for each datachange
                //the User class is fetching the datasnaps from the particular database referencing to the particular id of the user
                User user = dataSnapshot.getValue(User.class);

                username.setText(user.getUsername());

                //setting the profile image based on the condition that the profile image is the default one or has been changed
                if(user.getImageURL().equals("default")){

                    profile_image.setImageResource(R.mipmap.ic_launcher);

                }else{

                    Glide.with(MainActivity.this).load(user.getImageURL()).into(profile_image);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //getting reference to the tablayout and viewpager
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        ViewPager viewPager = findViewById(R.id.view_pager);

        //adding a viewpageradapter. it is used to create separate tabs and their contents, i.e the fragments under the particular tab
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPagerAdapter.addFragment(new ChatsFragment(),"Chats");
        viewPagerAdapter.addFragment(new UsersFragment(),"Users");
        viewPagerAdapter.addFragment(new ProfileFragment(), "Profile");

        //putting the tabs created from the viewpageradapter into the viewpager such that it can be displayed
        viewPager.setAdapter(viewPagerAdapter);
        //putting the viewpager on the tabs for switching
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //i think this is going to inflate the menu bar which is the logout button
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //getting the item value when the logout button is pressed
        switch (item.getItemId()) {

            case R.id.logout:
                //signing out from firebase database
                FirebaseAuth.getInstance().signOut();
                //going to the start activity for new login or register
                startActivity(new Intent(MainActivity.this, StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP ));
//                finish();
                return true;
        }
        return false;
    }

    //viewpageradapter extending the fragmentpageradapter, why? i think this is providing the values of the content inside the particular tabs created earlier
    //we're adding the fragments to the pager through this class
    class ViewPagerAdapter extends FragmentPagerAdapter{

        //providing an arraylist of type fragment and string to add the fragments to it, fragments are individual pages but those are going to be on different tabs
        private ArrayList<Fragment> fragments;
        private ArrayList<String> titles;

        //constructor setting the default value to an arraylist of fragments and their titles on the tabs
        ViewPagerAdapter(FragmentManager fm){
            super(fm);
            this.fragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }

        //this part is getting the position of the fragment
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        //this is returning the fragment size
        @Override
        public int getCount() {
            return fragments.size();
        }

        //this is the add fragment where the fragment and the title is added
        public void addFragment(Fragment fragment, String title){
            fragments.add(fragment);
            titles.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }

    private void status(String status){

        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);

    }

    @Override
    protected void onResume() {
        super.onResume();

        status("online");

    }

    @Override
    protected void onPause() {
        super.onPause();

        status("offline");

    }
}
