package paymentcloud.creaj.sv.com.asignartag;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NFCListener{

    private ProgressDialog pDialog;
    private static final String TAG = "";
    private TextView mEtMessage;
    private Button btnRecargar,btnLeer,btnAsignar;

    final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
    //private NFCWriteFragment mNfcWriteFragment;
    private NFCChargeFragment mNfcReadFragment;

    private boolean isDialogDisplayed = false;
    private boolean isWrite = false;
    int accion =0;
    private NfcAdapter mNfcAdapter;
    private String m_Text = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initNFC();
        btnAsignar=this.findViewById(R.id.btnasignar);

        btnRecargar=this.findViewById(R.id.btnrecargar);
        btnLeer=this.findViewById(R.id.btnleer);
        mEtMessage=this.findViewById(R.id.tstatus);

        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Espere un momento");
        pDialog.setCancelable(false);


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);


        if(preferences.getBoolean("is_logged",false)){


        }
        else{
            //startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
        btnAsignar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                accion=1;
            }
        });



        btnRecargar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                accion=3;
            }
        });

        btnLeer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                accion=4;
            }
        });




    }





    private void initNFC(){

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }


    @Override
    public void onDialogDisplayed() {

        isDialogDisplayed = true;
    }

    @Override
    public void onDialogDismissed() {

        isDialogDisplayed = false;
        isWrite = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter[] nfcIntentFilter = new IntentFilter[]{techDetected,tagDetected,ndefDetected};

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        if(mNfcAdapter!= null)
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, null);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mNfcAdapter!= null)
            mNfcAdapter.disableForegroundDispatch(this);
    }
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    public void actLeer(final String nfctag){//4
        mEtMessage.setText("Datos de la etiqueta: "+nfctag.toString());
            showpDialog();
            try {
                final String _response = "";
                RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                String url = "http://koalafood.com/api/paymentgateway/v2/public/getMyWallet";
                StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("----respuesta",response.toString());
                                try{
                                    JSONArray ja =new JSONArray(response);
                                    JSONObject profile = new JSONObject(ja.get(0).toString());
                                    String id = profile.getString("current_balance");
                                    mEtMessage.setText("Saldo actual: "+id.toString());
                                    hidepDialog();
                                } catch (Exception s){
                                    Log.d("ERROR",s.toString());
                                    hidepDialog();
                                    Toast.makeText(getApplicationContext(), "No se pudo realizar operacion, intenta nuevamente", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // _response.setText("That didn't work!");
                        Log.d("respuesta error",error.toString());
                    }
                }) {
                    //adding parameters to the request
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("pnfc_tag", nfctag.toString());
                        Log.d("PARAMETROS",params.toString());
                        return params;
                    }
                };
                queue.add(stringRequest);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),
                        "Error: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }



        accion=0;
    }
    public void actCobrar(final String nfctag, final String saldo){//2
        showpDialog();
        try {
            final String _response = "";
            RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
            String url = "http://koalafood.com/api/paymentgateway/v2/public/chargeWallet";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("----respuesta",response.toString());
                            try{
//                                JSONArray ja =new JSONArray(response);
//                                JSONObject profile = new JSONObject(ja.get(0).toString());
//                                String id = profile.getString("transaction_id");
                                mEtMessage.setText("Transacción exitosa, se debitaron: "+saldo.toString());
                                hidepDialog();
                            } catch (Exception s){
                                Log.d("ERROR",s.toString());
                                    hidepDialog();
                                    Toast.makeText(getApplicationContext(), "No se pudo realizar operacion, intenta nuevamente", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // _response.setText("That didn't work!");
                    Log.d("respuesta error",error.toString());
                }
            }) {
                //adding parameters to the request
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("pnfc_tag", nfctag.toString());
                    params.put("amount", saldo.toString());
                    params.put("user_id", "1");
                    Log.d("PARAMETROS",params.toString());
                    return params;
                }
            };
            queue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    "Error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
        accion=0;
    }
    public void actRecargar(final String nfctag, final String saldo)//3
    {
        //mEtMessage.setText("Datos de la etiqueta: "+nfctag.toString());
        showpDialog();
        try {
            final String _response = "";
            RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
            String url = "http://koalafood.com/api/paymentgateway/v2/public/rechargeWallet";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("----respuesta",response.toString());
                            try{
//                                JSONArray ja =new JSONArray(response);
//                                JSONObject profile = new JSONObject(ja.get(0).toString());
//                                String id = profile.getString("transaction_id");
                                mEtMessage.setText("Transacción exitosa, se cargaron: "+saldo.toString());
                                hidepDialog();
                                Intent i = new Intent(getApplicationContext(),Lottie.class);
                                startActivity(i);
                            } catch (Exception s){
                                Log.d("ERROR",s.toString());
                                hidepDialog();
                                Toast.makeText(getApplicationContext(), "No se pudo realizar operación, intenta nuevamente", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // _response.setText("That didn't work!");
                    Log.d("respuesta error",error.toString());
                }
            }) {
                //adding parameters to the request
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("pnfc_tag", nfctag.toString());
                    params.put("amount", saldo.toString());
                    params.put("user_id", "1");
                    Log.d("PARAMETROS",params.toString());
                    return params;
                }
            };
            queue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    "Error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
        accion=0;
    }
    public void actAsignar(final String nfctag, final String idUsuario)//1
    {
      //  mEtMessage.setText("Datos de la etiqueta: "+nfctag.toString());
        //mEtMessage.setText("Datos de la etiqueta: "+nfctag.toString());
        showpDialog();
        try {
            final String _response = "";
            RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
            String url = "http://koalafood.com/api/paymentgateway/v2/public/pairNFCtoCustomer";
            StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("----respuesta",response.toString());
                            try{
//                                JSONArray ja =new JSONArray(response);
//                                JSONObject profile = new JSONObject(ja.get(0).toString());
//                                String id = profile.getString("wallet_id");
                                mEtMessage.setText("Transacción exitosa, Wallet asignado correctamente.");
                                hidepDialog();
                            } catch (Exception s){
                                Log.d("ERROR",s.toString());
                                hidepDialog();
                                Toast.makeText(getApplicationContext(), "No se pudo realizar operación, intenta nuevamente", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // _response.setText("That didn't work!");
                    Log.d("respuesta error",error.toString());
                }
            }) {
                //adding parameters to the request
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();
                    params.put("nfc_tag", nfctag.toString());
                    params.put("wallet_id", idUsuario.toString());
                    Log.d("PARAMETROS",params.toString());
                    return params;
                }
            };
            queue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),
                    "Error: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
        accion=0;
    }


    public void askForParameter(final String nfcTag, String title){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();
                if(accion==1){
                    actAsignar(nfcTag,m_Text);
                }
                if(accion==2){
                    actCobrar(nfcTag,m_Text);
                }
                if(accion==3){
                    actRecargar(nfcTag,m_Text);
                }

            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }


    @Override
    protected void onNewIntent(Intent intent) {

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        Log.d(TAG, "onNewIntent: "+intent.getAction());

        if(tag != null) {
            if(accion==0){
                Toast.makeText(this, "Etiqueta detectada, seleccione una acción", Toast.LENGTH_SHORT).show();
            }
            else{

                Ndef ndef = Ndef.get(tag);
                Tag myTag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                Log.i("tag ID ", myTag.getId().toString());
                String vtag= bytesToHex(myTag.getId());
                Log.i("tax ", vtag);

                if(accion==1){
                    askForParameter(vtag,"Ingrese el id del usuario a asignar");
                }
                if(accion==2){
                    askForParameter(vtag,"Ingrese el monto a cobrar");
                }
                if(accion==3){
                    askForParameter(vtag,"Ingrese el monto a recargar");
                }
                if(accion==4){
                    actLeer(vtag);
                }

                if (ndef == null) {
                    return;
                }
                if (isDialogDisplayed) {
                    if (isWrite) {
                        String messageToWrite = mEtMessage.getText().toString();

                    } else {
                        mNfcReadFragment = (NFCChargeFragment) getFragmentManager().findFragmentByTag(NFCChargeFragment.TAG);
                        mNfcReadFragment.onNfcDetected(ndef);
                    }
                }
            }




        }
    }


    private void showpDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }



}
