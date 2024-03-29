import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class SetUpActivity extends AppCompatActivity {
    private CircleImageView circleImageView;
    private Button mSaveBtn;
    private FirebaseAuth auth;
    private Uri mImageUri=null;
    private Toolbar setUpToolBar;
    private StorageReference storageReference;
    private FirebaseFirestore firestore;
    private String Uid;
    private ProgressBar progressBar;
    private EditText mProfileName;
    private boolean isPhotoSelected=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up);

        setUpToolBar = findViewById(R.id.set_up_toolbar);
        setSupportActionBar(setUpToolBar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Profile");


        storageReference= FirebaseStorage.getInstance().getReference();
        firestore=FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        Uid=auth.getCurrentUser().getUid();


        progressBar=findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        circleImageView = findViewById(R.id.circleImageView);
        mProfileName = findViewById(R.id.profile_text);
        mSaveBtn = findViewById(R.id.save_btn);


        firestore.collection("Users").document(Uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful())
                {
                    if(task.getResult().exists())
                    {
                        String name=task.getResult().getString("name");
                        String imageUrl=task.getResult().getString("image");
                        mProfileName.setText(name);
                        
                        mImageUri=Uri.parse(imageUrl);

                        Glide.with(SetUpActivity.this).load(imageUrl).into(circleImageView);

                    }
                }
            }
        });


        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                String name=mProfileName.getText().toString();
                StorageReference imageRef=storageReference.child("Profile_pics").child(Uid+".jpg");
                if(isPhotoSelected) {


                    if (!name.isEmpty() && mImageUri != null) {


                        imageRef.putFile(mImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            saveToFireStore(task, name, uri);
                                        }
                                    });
                                } else {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(SetUpActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(SetUpActivity.this, "Please select your picture and write a name", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    saveToFireStore(null,name,mImageUri);
                }

            }
        });

        circleImageView.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
            {
                if(ContextCompat.checkSelfPermission(SetUpActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
                {
                    //permission not granted in this condition.Let's asking the permission
                    ActivityCompat.requestPermissions(SetUpActivity.this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                }
                else
                {
                    //permission granted and pick up the image ,code from ArthurHub github
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .setAspectRatio(1,1)
                            .start(SetUpActivity.this);
                }
            }
        });
    }

    private void saveToFireStore(Task<UploadTask.TaskSnapshot> task, String name,Uri downloadUri) {




        HashMap<String,Object> map=new HashMap<>();
        map.put("name",name);
        map.put("image",downloadUri.toString());

        firestore.collection("Users").document(Uid).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful())
                {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(SetUpActivity.this, "Profile settings saved", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SetUpActivity.this,MainActivity.class));
                    finish();
                }
                else
                {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(SetUpActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //Why it is coming don't know
    private void setSupportActionBar() {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result=CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK)
            {
                mImageUri=result.getUri();
                circleImageView.setImageURI(mImageUri);

                isPhotoSelected=true;
            }
            else if(resultCode==CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                assert result != null;
                Toast.makeText(this, result.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }


    }
