package demo.com.tutorialapp;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;


public class HomeActivity extends Activity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

   //buttons sign in and sign out
    private SignInButton sign_in_button;
    private ImageButton sign_out_button;

    private GoogleApiClient googleApiClient;
    private int signinprogress;
    private PendingIntent signinintent;

    private static final int SIGNED_IN = 0;
    private static final int STATE_SIGNING_IN = 1;
    private static final int STATE_IN_PROGRESS = 2;
    private static final int RC_SIGN_IN = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sign_in_button = (SignInButton) findViewById(R.id.sign_in_button);
        sign_out_button = (ImageButton) findViewById(R.id.sign_out_button);
        sign_in_button.setOnClickListener(this);
        sign_out_button.setOnClickListener(this);
        googleApiClient = buildGoogleApiClient();
    }


    private GoogleApiClient buildGoogleApiClient() {

        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(new Scope("email")).build();
    }


    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }


    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.i("", "on connected ");
        sign_in_button.setEnabled(false);
        sign_out_button.setEnabled(true);
    }

    @Override
    public void onConnectionSuspended(int i) {
        googleApiClient.connect();
        signinprogress = SIGNED_IN;
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (signinprogress != STATE_IN_PROGRESS) {
            signinintent = connectionResult.getResolution();
            if (signinprogress == STATE_SIGNING_IN) {
                resolveSignInError();
            }
        }
        whenSignedOut();
    }


    private void resolveSignInError() {
        if (signinintent != null) {
            try {
                signinprogress = STATE_IN_PROGRESS;
                startIntentSenderForResult(signinintent.getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                signinprogress = STATE_SIGNING_IN;
                googleApiClient.connect();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    signinprogress = STATE_SIGNING_IN;
                } else {
                    signinprogress = SIGNED_IN;
                }

                if (!googleApiClient.isConnecting()) {
                    googleApiClient.connect();
                }
                break;
        }
    }

    private void whenSignedOut() {
        Log.i("", "when signed out");
        sign_in_button.setEnabled(true);
        sign_out_button.setEnabled(false);
    }

    @Override
    public void onClick(View view) {
        if (!googleApiClient.isConnecting()) {
            switch (view.getId()) {
                case R.id.sign_in_button:
                    // Intent i=new Intent(getApplicationContext(),GcmActivity.class);
                    // startActivity(i);
                    // mStatus.setText("Signing In");
                    Log.i("signed in ", "");
                    resolveSignInError();
                    break;

                case R.id.sign_out_button:
                    Log.i("signed out", "");
                    Plus.AccountApi.clearDefaultAccount(googleApiClient);
                    googleApiClient.disconnect();
                    googleApiClient.connect();
                    break;
            }
        }
    }
}