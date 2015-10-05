package br.com.ceolato.torico.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;

public class ContadorService extends Service implements Runnable {

    private SharedPreferences preferences;

    private int contador;

    private double salarioBruto;
    private int horasTrabalhadas;
    private int percentual;
    private double valorPorSegundo;
    private String valorPorSegundoAsString;

    private Handler myHandler;
    private Handler activityHandler;

    @Override
    public IBinder onBind(Intent arg0) {
        return new ContadorBinder();
    }

    public class ContadorBinder extends Binder {
        public ContadorService getContador() {
            return ContadorService.this;
        }
    }

    @Override
    public void onCreate() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        contador = 0;
        myHandler = new Handler();
    }

    public void play() {
        carregarPreferencias();
        myHandler.post(this);
    }

    public void pause() {
        myHandler.removeCallbacks(this);
    }

    public void stop() {
        pause();
        contador = 0;
    }

    private void carregarPreferencias() {
        salarioBruto = Double.parseDouble(preferences.getString("salario_bruto_key", "0.0"));
        percentual = Integer.parseInt(preferences.getString("percentual_key", "50"));
        horasTrabalhadas = Integer.parseInt(preferences.getString("horas_trabalhadas_key", "220"));

        valorPorSegundo = (salarioBruto / horasTrabalhadas / 3600) * (1 + (percentual / 100D));
        valorPorSegundoAsString = String.format("R$ %.4f", valorPorSegundo);
    }

    public void setHandler(Handler myHandler) {
        this.activityHandler = myHandler;
    }

    public void run() {
        myHandler.postAtTime(this, SystemClock.uptimeMillis() + 1000);
        contar();
    }

    private void contar() {
        contador++;

        if (activityHandler != null) {
            Bundle bundle = new Bundle();

            bundle.putString("tempo", getTempo());
            bundle.putString("valor", getValor());
            bundle.putString("valorSegundo", valorPorSegundoAsString);

            Message msg = new Message();
            msg.setData(bundle);
            activityHandler.sendMessage(msg);
        }
    }

    private String getTempo() {
        int segundos = contador % 60;
        int minutos = (contador / 60) % 60;
        int horas = contador / 3600;
        return String.format("%02d:%02d:%02d", horas, minutos, segundos);
    }

    private String getValor() {
        double valorContado = valorPorSegundo * contador;
        return String.format("R$ %.2f", valorContado);
    }

    @Override
    public void onDestroy() {
        myHandler.removeCallbacks(this);
    }
}
