package br.com.ceolato.torico.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;

import br.com.ceolato.torico.R;
import br.com.ceolato.torico.activity.ToRicoActivity;

public class ContadorService extends Service implements Runnable {

    private SharedPreferences preferences;

    private int contador;

    private double salarioBruto;
    private int horasTrabalhadas;
    private int percentual;
    private int objetivoMaximo;
    private int objetivo;
    private double valorPorSegundo;
    private String valorPorSegundoAsString;
    private boolean notificacao;

    private Handler myHandler;
    private Handler activityHandler;

    @Override
    public void onCreate() {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        contador = 0;
        notificacao = false;
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
        notificacao = false;
    }

    private void carregarPreferencias() {
        salarioBruto = Double.parseDouble(preferences.getString("salario_bruto_key", "0.0"));
        percentual = Integer.parseInt(preferences.getString("percentual_key", "50"));
        horasTrabalhadas = Integer.parseInt(preferences.getString("horas_trabalhadas_key", "220"));
        objetivoMaximo = Integer.parseInt(preferences.getString("objetivo_key", "10"));

        valorPorSegundo = (salarioBruto / horasTrabalhadas / 3600) * (1 + (percentual / 100D));
        valorPorSegundoAsString = String.format("R$ %.4f", valorPorSegundo);
    }

    public void setHandler(Handler myHandler) {
        this.activityHandler = myHandler;
    }

    public void setObjetivo(int objetivo){
        this.objetivo = objetivo;
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
            bundle.putDouble("valorAcumulado", getValorAcumulado());
            bundle.putInt("objetivoMaximo", objetivoMaximo);

            Message msg = new Message();
            msg.setData(bundle);
            activityHandler.sendMessage(msg);
        }

        if(getValorAcumulado() > objetivo && !notificacao){
            this.disparaNotificacao();
            notificacao = true;
        }else if(getValorAcumulado() < objetivo && notificacao){
            notificacao = false;
            this.cancelaNotificacao();
        }
    }

    private String getTempo() {
        int segundos = contador % 60;
        int minutos = (contador / 60) % 60;
        int horas = contador / 3600;
        return String.format("%02d:%02d:%02d", horas, minutos, segundos);
    }

    private double getValorAcumulado(){
        return valorPorSegundo * contador;
    }

    private String getValor() {
        return String.format("R$ %.2f", getValorAcumulado());
    }

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
    public void onDestroy() {
        myHandler.removeCallbacks(this);
    }

    private void disparaNotificacao(){
        PendingIntent p = PendingIntent.getActivity(this, 0, new Intent(this, ToRicoActivity.class), 0);
        Notification n = new Notification.Builder(this)
                .setTicker("Você atingiu seu objetivo!")
                .setContentTitle("Objetivo alcançado.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText("Parabéns, você atingiu seu objetivo!")
                .setWhen(System.currentTimeMillis())
                .setVibrate(new long[]{1000, 1000, 1000})
                .build();
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(R.string.app_name, n);
        notificacao = true;
    }

    private void cancelaNotificacao(){
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(R.string.app_name);
        notificacao = false;
    }
}
