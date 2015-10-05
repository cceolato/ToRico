package br.com.ceolato.torico;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import br.com.ceolato.torico.activity.ToRicoActivity;

public class SplashScreenActivity extends Activity {
    private Handler handler = new SplashScreenHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        handler.sendMessageDelayed(new Message(), 3000);
    }

    private class SplashScreenHandler extends Handler {
        public void handleMessage(Message msg){
            Intent intent = new Intent(getApplicationContext(), ToRicoActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
