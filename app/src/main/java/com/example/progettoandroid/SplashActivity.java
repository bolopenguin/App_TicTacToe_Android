package com.example.progettoandroid;

        import android.content.Intent;
        import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.VideoView;


public class SplashActivity extends AppCompatActivity {

    /** Duration of wait **/
    private final int SPLASH_DISPLAY_LENGTH = 8000; //splash screen will be shown for 8 seconds
    private VideoView Bumper;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.activity_splash);

        getSupportActionBar().hide();

        /*new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent mainIntent = new Intent(SplashActivity.this, GestioneBluetooth.class);
                startActivity(mainIntent);
                finish();
            }
        }, SPLASH_DISPLAY_LENGTH);*/

        Bumper = (VideoView)findViewById(R.id.videoView);
        Bumper.setVideoPath("android.resource://com.example.progettoandroid/" + R.raw.logobumper);
        Bumper.start();
        /*if(!Bumper.isPlaying()){

        }*/
        Bumper.postDelayed(new Runnable() {
            public void run() {
                startActivity(new Intent(SplashActivity.this, GestioneBluetooth.class));
            }
        }, 8000);
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        Bumper.seekTo(7999);
        startActivity(new Intent(SplashActivity.this, GestioneBluetooth.class));
    }
}