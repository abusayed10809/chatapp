package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    //declaring variables for buttons and textfields data storage
    MaterialEditText username,email,password;
    Button btn_register;

    FirebaseAuth auth;
    DatabaseReference reference;

    //on create function
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //setting up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //naming the toolbar
        getSupportActionBar().setTitle("Register");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //connecting buttons and textfields from xml file to java class
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btn_register = findViewById(R.id.btn_register);

        //the auth value is already instantiated
        auth = FirebaseAuth.getInstance();

        //defining the register buttons action when it is clicked
        //it shall create a new view when it is clicked
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //copy the values from the textfields and take them as strings into the variables
                String txt_username = username.getText().toString();
                String txt_email = email.getText().toString();
                String txt_password = password.getText().toString();

                //check the values of the variables and depending on the values, we shall call the register function when conditions fulfill
                if(TextUtils.isEmpty(txt_username) || TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)){
                    //check if any field is empty
                    Toast.makeText(RegisterActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();

                }else if(txt_password.length()<6){
                    //check if the password length is smaller than 6
                    Toast.makeText(RegisterActivity.this,"Password is too small",Toast.LENGTH_SHORT).show();

                }else{
                    //calling the register function
                    register(txt_username,txt_email,txt_password);

                }
            }
        });
    }

    private void register(final String username, String email, String password){

        //creating a new user in the users panel in firebase
        //email and password values are passed on from the variables
        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()){
                    //when the task i.e user creation is successful that is a new user has been created in authentication panel of firebase

                    //getting the current user in this variable
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    assert firebaseUser != null;

                    //using a string variable to get the id of the current user
                    String userid = firebaseUser.getUid();

                    //creating a path where child is the userid
                    reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                    //creating a hashmap and mapping the id, username, imageurl, status and search in it
                    HashMap<String , String> hashMap = new HashMap<>();
                    hashMap.put("id",userid);
                    hashMap.put("username",username);
                    hashMap.put("imageURL","default");
                    hashMap.put("status", "offline");
                    hashMap.put("search", username.toLowerCase());

                    //here the values from the hashmap are pushed into the database
                    //also a listener is added when the data pushing is completed

                    //the database will have a path Users and under Users, it will have a child having id
                    //under the id, it shall have the values pushed through the hashmap
                    reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){
                                //checking if the task is completed successfully or not
                                Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();

                            }

                        }
                    });
                }
                else if(task.isCanceled()){
                    //when user is not created in the authentication panel of firebase
                    Toast.makeText(RegisterActivity.this, "You cannot Register with this Email or Password", Toast.LENGTH_SHORT).show();

                }

            }
        });
    }
}
