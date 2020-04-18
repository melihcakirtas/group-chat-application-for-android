package com.melih.chatapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.EventListener;
import java.util.HashMap;
import java.util.UUID;

public class ProfileActivity extends AppCompatActivity {
    EditText agetext;
    ImageView imageView;
    FirebaseAuth firebaseAuth;
    Uri imagedata;
    Bitmap selectedimage;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        agetext = findViewById(R.id.edittext_get_age);
        imageView = findViewById(R.id.imageview_user);
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        getdata();
    }

    public void getdata(){
        DatabaseReference databaseReference1 = firebaseDatabase.getReference("Profile");
        databaseReference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    HashMap<String,String> hashMap = (HashMap<String, String>) ds.getValue();
                    String username = hashMap.get("useremail");

                    if(username.matches(firebaseAuth.getCurrentUser().getEmail().toString())){
                        String age = hashMap.get("userage");
                        String imagrurl = hashMap.get("userimageurl");

                        if(age != null && imagrurl != null){

                            agetext.setText(age);
                            Picasso.get().load(imagrurl).into(imageView);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void selectimage(View view){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},1);
        } else {
            Intent intenttogalery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intenttogalery,2);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==1){
            if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intenttogalery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intenttogalery,2);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==2 && resultCode==RESULT_OK && data !=null){
            imagedata =data.getData();
            try {
                if(Build.VERSION.SDK_INT >= 28){
                    ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(),imagedata);
                    selectedimage = ImageDecoder.decodeBitmap(source);
                    imageView.setImageBitmap(selectedimage);
                } else{
                    selectedimage = MediaStore.Images.Media.getBitmap(this.getContentResolver(),imagedata);
                    imageView.setImageBitmap(selectedimage);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void upload(View view){
        final UUID uuid = UUID.randomUUID();
        String imagename = "images/" + uuid + "jpg";
        StorageReference storageReference1 = storageReference.child(imagename);
        storageReference1.putFile(imagedata).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                StorageReference storageReference2profil = FirebaseStorage.getInstance().getReference("images/" + uuid + "jpg");
                storageReference2profil.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        UUID uuid1 = UUID.randomUUID();
                        String uuid1string= uuid1.toString();
                       String downloadurl = uri.toString();
                       String userage = agetext.getText().toString();
                       FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                       databaseReference.child("Profile").child(uuid1string).child("userimageurl").setValue(downloadurl);
                       databaseReference.child("Profile").child(uuid1string).child("userage").setValue(userage);
                       databaseReference.child("Profile").child(uuid1string).child("useremail").setValue(firebaseUser.getEmail());

                        Toast.makeText(ProfileActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ProfileActivity.this, e.getLocalizedMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
