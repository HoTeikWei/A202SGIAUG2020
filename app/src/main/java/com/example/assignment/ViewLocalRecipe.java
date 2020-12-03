package com.example.assignment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ViewLocalRecipe extends AppCompatActivity {

    private int id;

    private TextView tvRecipeName, tvRecipeTag, tvRecipeIngredient, tvRecipeStep, tvRecipeCreator;
    private ImageButton ibEdit, ibDelete;

    private LocalRecipe localrecipe;

    private SQLiteDatabaseHelper localDb;

    //top navigation bar - back function
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent intentFragment = new Intent(getApplicationContext(), MainActivity.class);
                intentFragment.putExtra("FragmentId", 2);
                startActivity(intentFragment);
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_local_recipe);
        //used to create top navigation bar
        if (getSupportActionBar() != null ){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_white);
        }

        localDb = new SQLiteDatabaseHelper(this);

        Intent itemPass = getIntent();
        id = itemPass.getIntExtra("LocalPosition", -1);
        if (id == -1){
            Toast.makeText(getApplicationContext(), "ID is " + id, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
        }

        localrecipe = localDb.getLocalRecipe(id);

        tvRecipeName = findViewById(R.id.viewlocalrecipeTvRecipeName);
        tvRecipeTag = findViewById(R.id.viewlocalrecipeTvRecipeTag);
        tvRecipeIngredient = findViewById(R.id.viewlocalrecipeTvRecipeIngredient);
        tvRecipeStep = findViewById(R.id.viewlocalrecipeTvRecipeStep);
        tvRecipeCreator = findViewById(R.id.viewlocalrecipeTvRecipeCreator);
        ibEdit = findViewById(R.id.viewlocalrecipeIbEdit);
        ibDelete = findViewById(R.id.viewlocalrecipeIbDelete);

        tvRecipeName.setText(localrecipe.getName());
        tvRecipeTag.setText(localrecipe.getTag());
        tvRecipeIngredient.setText(localrecipe.getIngredient());
        tvRecipeStep.setText(localrecipe.getStep());
        tvRecipeCreator.setText(localrecipe.getUser());

        ibEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent editItem = new Intent(getApplicationContext(), EditLocalRecipeActivity.class);
                editItem.putExtra("LocalId", id);
                startActivity(editItem);
                finish();
            }
        });

        ibDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder deleteDialog = new AlertDialog.Builder(ViewLocalRecipe.this);
                deleteDialog.setTitle("Delete Recipe");
                deleteDialog.setMessage("Are you sure to delete this recipe?");
                deleteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //used to delete data in sqlite database
                        if(localDb.deleteLocalRecipe(String.valueOf(id))){
                            Toast.makeText(getApplicationContext(), "Reciped deleted!", Toast.LENGTH_SHORT).show();
                            Intent intentFragment = new Intent(getApplicationContext(), MainActivity.class);
                            intentFragment.putExtra("FragmentId", 2);
                            startActivity(intentFragment);
                            finish();
                            finish();
                        }else{
                            Toast.makeText(getApplicationContext(), "Fail to deleted!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "Action cancelled", Toast.LENGTH_SHORT).show();
                    }
                });
                AlertDialog dialog = deleteDialog.create();
                dialog.show();

            }
        });
    }
}
