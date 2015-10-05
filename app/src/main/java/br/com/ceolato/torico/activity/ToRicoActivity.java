package br.com.ceolato.torico.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import br.com.ceolato.torico.R;
import br.com.ceolato.torico.service.ContadorService;

public class ToRicoActivity extends AppCompatActivity {

    private Button playButton;
    private Button pauseButton;
    private Button stopButton;

    private ContadorService contador;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_rico);

        stopButton = (Button) findViewById(R.id.btStop);
        pauseButton = (Button) findViewById(R.id.btPause);
        playButton = (Button) findViewById(R.id.btPlay);

        stopButton.setEnabled(false);
        pauseButton.setEnabled(false);
        playButton.setEnabled(true);

        startService(new Intent(this, ContadorService.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(new Intent(this, ContadorService.class), conexao, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        contador.setHandler(null);
        unbindService(conexao);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_to_rico, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_settings:
                config();
                return true;
            case R.id.about:
                about();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    Handler handler = new Handler(new Handler.Callback() {
        public boolean handleMessage(Message msg) {
            ((TextView) findViewById(R.id.tvValorAcumulado)).setText(msg.getData().getString("valor"));
            ((TextView) findViewById(R.id.tvTempo)).setText(msg.getData().getString("tempo"));
            ((TextView) findViewById(R.id.tvValorSegundo)).setText(msg.getData().getString("valorSegundo"));

            stopButton.setEnabled(true);
            pauseButton.setEnabled(true);
            playButton.setEnabled(false);

            return false;
        }
    });

    private ServiceConnection conexao = new ServiceConnection() {
        public void onServiceConnected(ComponentName name, IBinder service) {
            ContadorService.ContadorBinder binder = (ContadorService.ContadorBinder) service;
            contador = binder.getContador();
            contador.setHandler(handler);
        }
        public void onServiceDisconnected(ComponentName name) {
            contador = null;
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        bundle.putString("valor", (String) ((TextView) findViewById(R.id.tvValorAcumulado)).getText());
        bundle.putString("tempo", (String) ((TextView) findViewById(R.id.tvTempo)).getText());
        bundle.putString("valorSegundo", (String) ((TextView) findViewById(R.id.tvValorSegundo)).getText());

        super.onSaveInstanceState(bundle);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        ((TextView) findViewById(R.id.tvValorAcumulado)).setText(savedInstanceState.getString("valor"));
        ((TextView) findViewById(R.id.tvTempo)).setText(savedInstanceState.getString("tempo"));
        ((TextView) findViewById(R.id.tvValorSegundo)).setText(savedInstanceState.getString("valorSegundo"));
    }

    public void play(View v) {
        contador.play();

        playButton.setEnabled(false);
        pauseButton.setEnabled(true);
        stopButton.setEnabled(true);
    }

    public void pause(View v) {
        contador.pause();

        playButton.setEnabled(true);
        pauseButton.setEnabled(false);
        stopButton.setEnabled(true);
    }

    public void stop(View v) {
        contador.stop();

        playButton.setEnabled(true);
        pauseButton.setEnabled(false);
        stopButton.setEnabled(false);

        stopService(new Intent(this, ContadorService.class));
    }

    private void config(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void about(){
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

}