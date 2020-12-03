package com.example.assignment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav =findViewById(R.id.bottomNav);
        bottomNav.setOnNavigationItemSelectedListener(bottomNavListener);

        MenuItem menu = findViewById(R.id.menu_log);
        user = FirebaseAuth.getInstance().getCurrentUser();

        Intent getFragment = getIntent();
        int selectedId = getFragment.getIntExtra("FragmentId", -1);
        int fragmentId;
        if(selectedId == -1){
            fragmentId = 1;
        }else{
            fragmentId = selectedId;
        }
        //for switch fragment
        switch (fragmentId) {
            case 1:
                bottomNav.setSelectedItemId(R.id.menu_home);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
                break;
            case 2:
                bottomNav.setSelectedItemId(R.id.menu_myRecipe);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new MyRecipeFragment()).commit();
                break;
            case 3:
                bottomNav.setSelectedItemId(R.id.menu_recipeList);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,  new ListFragment()).commit();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu topMenu){
        getMenuInflater().inflate(R.menu.nav_top, topMenu);
        MenuItem menu = topMenu.findItem(R.id.menu_log);
        if (user != null){
            menu.setTitle("Logout");
        }else{
            menu.setTitle("Login");
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.menu_profile:
                if (user != null){
                    startActivity(new Intent(getApplicationContext(), MyProfileActivity.class));
                    finish();
                } else{
                    Toast.makeText(getApplicationContext(), "You need to login before access to profile!", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.menu_FAQ:
                startActivity(new Intent(getApplicationContext(), FAQActivity.class));
                finish();
                break;
            case R.id.menu_aboutus:
                startActivity(new Intent(getApplicationContext(), AboutUsActivity.class));
                finish();
                break;
            case R.id.menu_log:
                if (user != null){
                    FirebaseAuth.getInstance().signOut();
                }
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
                break;
        }

        return true;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener bottomNavListener= new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            Fragment selectedFragment = null;
            switch (menuItem.getItemId()){
                case R.id.menu_home:
                    selectedFragment = new HomeFragment();
                    break;
                case R.id.menu_myRecipe:
                    selectedFragment = new MyRecipeFragment();
                    break;
                case R.id.menu_recipeList:
                    selectedFragment = new ListFragment();
                    break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

            return true;
        }
    };

}
