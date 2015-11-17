package br.com.festasonharacordado.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.zxing.integration.android.IntentIntegrator;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class Main extends Activity {
    private static final int SCAN_PRESENCE = 0;
    private static final int SCAN_PAYMENT = 1;
    private static final String ROOT_URL = "http://festasonharacordado.com.br/apiv1";
    private static final String subscriptionsUrl = ROOT_URL + "/subscriptions/";
    private static final String auth = "Token SEU_TOKEN_DE_AUTENTICACAO_AQUI";
    private static IntentIntegrator ii;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getActionBar() != null) {
            getActionBar().setTitle(R.string.welcome_text);
            getActionBar().setDisplayUseLogoEnabled(true);
        }
        ii = new IntentIntegrator(Main.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String result = data.getStringExtra("SCAN_RESULT").trim();
            if (requestCode == SCAN_PRESENCE) {
                JsonObject body = new JsonObject();
                body.addProperty("present", true);
                Ion.with(Main.this).load("PATCH", subscriptionsUrl + result + "/")
                        .setHeader("Authorization", auth)
                        .setHeader("Content-Type", "application/json")
                        .setJsonObjectBody(body)
                        .as(Subscription.class)
                        .setCallback(new FutureCallback<Subscription>() {
                            @Override
                            public void onCompleted(Exception e, Subscription res) {
                                if (e == null)
                                    Toast.makeText(Main.this, R.string.presence_success, Toast.LENGTH_LONG).show();
                                else
                                    Toast.makeText(Main.this, R.string.request_error, Toast.LENGTH_LONG).show();
                                presence(null);
                            }
                        });
            } else if (requestCode == SCAN_PAYMENT) {
                JsonObject body = new JsonObject();
                body.addProperty("paid", 40);
                body.addProperty("payment", "cash");
                Ion.with(Main.this).load("PATCH", subscriptionsUrl + result + "/")
                        .setHeader("Authorization", auth)
                        .setHeader("Content-Type", "application/json")
                        .setJsonObjectBody(body)
                        .as(Subscription.class)
                        .setCallback(new FutureCallback<Subscription>() {
                            @Override
                            public void onCompleted(Exception e, Subscription res) {
                                if (e == null)
                                    Toast.makeText(Main.this, R.string.payment_success, Toast.LENGTH_LONG).show();
                                else
                                    Toast.makeText(Main.this, R.string.request_error, Toast.LENGTH_LONG).show();
                                payment(null);
                            }
                        });
            }
        }
    }

    public void payment(View view) {
        ii.setPrompt(getString(R.string.payment_prompt)).setBarcodeImageEnabled(false);
        startActivityForResult(ii.createScanIntent(), SCAN_PAYMENT);
    }

    public void presence(View view) {
        ii.setPrompt(getString(R.string.presence_prompt)).setBarcodeImageEnabled(false);
        startActivityForResult(ii.createScanIntent(), SCAN_PRESENCE);
    }
}