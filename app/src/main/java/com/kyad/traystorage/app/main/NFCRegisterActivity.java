package com.kyad.traystorage.app.main;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.kyad.traystorage.R;
import com.kyad.traystorage.app.Constants;
import com.kyad.traystorage.app.common.dialog.AlertDialog;
import com.kyad.traystorage.app.common.dialog.LoadingDialog;
import com.kyad.traystorage.app.common.util.Utils;
import com.kyad.traystorage.databinding.ActivityNfcRegisterBinding;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import base.BaseBindingActivity;
import helper.Util;

public class NFCRegisterActivity extends BaseBindingActivity<ActivityNfcRegisterBinding> {

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    final static String TAG = "nfc_test";

    public Integer documentId = 0;
    String documentCode = "";
    String dimLink = "";
    @Override
    public int getLayout() {
        return R.layout.activity_nfc_register;
    }

    @Override
    protected Dialog loadingDialog() {
        return new LoadingDialog(this);
    }

    @Override
    public void init() {
        binding.setActivity(this);

        initParams();
        initView();
    }

    public void onCancelClick() {
        onBackPressed();
    }

    void initParams() {
        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }

        documentCode = (String) extras.get("code");
        documentId = (Integer) getIntent().getIntExtra("doc_id", 0);
        getDimLink();
    }

    private void initView() {
        NfcManager manager = (NfcManager) getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = manager.getDefaultAdapter();
        if (adapter != null && adapter.isEnabled()) {
            //Yes NFC available
            //Toast.makeText(this, "NFC 사용 가능", Toast.LENGTH_SHORT).show();
        } else if (adapter != null && !adapter.isEnabled()) {
            //NFC is not enabled.Need to enable by the user.
           //Toast.makeText(this, "NFC가 활성화되어 있지 않습니다. 사용자가 활성화해야 합니다.", Toast.LENGTH_SHORT).show();
            AlertDialog.show(NFCRegisterActivity.this)
                    .setText(getString(R.string.nfc_reg_error2), "", getString(R.string.confirm))
                    .setListener(() -> {
                        finish();
                    });
        } else {
            //NFC is not supported头 [tóu]
            AlertDialog.show(NFCRegisterActivity.this)
                    .setText(getString(R.string.nfc_reg_error1), "", getString(R.string.confirm))
                    .setListener(() -> {
                        finish();
                    });
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "NO NFC Capabilities", Toast.LENGTH_SHORT).show();

            //Create a PendingIntent object so the Android system can
            //populate it with the details of the tag when it is scanned.
            //PendingIntent.getActivity(Context,requestcode(identifier for
            //                           intent),intent,int)
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        assert nfcAdapter != null;
        //nfcAdapter.enableForegroundDispatch(context,pendingIntent,
        //                                    intentFilterArray,
        //                                    techListsArray)
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    protected void onPause() {
        super.onPause();
        //Onpause stop listening
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        try {
            resolveIntent(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void resolveIntent(Intent intent) throws Exception {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            assert tag != null;
//            byte[] payload = detectTagData(tag).getBytes();

            Date currentTime = Calendar.getInstance().getTime();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

            //코드 등록
            String strDate = dateFormat.format(currentTime);

            NdefRecord[] arr = new NdefRecord[2];
            //arr[0] = NdefRecord.createMime("text/plain", strDate.getBytes());
            arr[0] = NdefRecord.createMime("text/plain", documentCode.getBytes());
            arr[1] = NdefRecord.createUri(dimLink);
            NdefMessage data = new NdefMessage(arr);
            writeTag(tag, data);

            AlertDialog.show(NFCRegisterActivity.this)
                    .setText(getString(R.string.nfc_register_success_title), getString(R.string.nfc_register_success), getString(R.string.confirm))
                    .setListener(() -> {
                        finish();
                    });
        }
    }

    public static void writeTag(Tag tag, NdefMessage message) throws Exception {
//        int size = message.toByteArray().length;

        Ndef ndef = Ndef.get(tag);
        if (ndef != null) {
            ndef.connect();
//            if (!ndef.isWritable()) {
//                throw new NfcTagNotWritableException();
//            }
//            if (ndef.getMaxSize() < size) {
//                throw new NfcTagInsufficientMemoryException(ndef.getMaxSize(), size);
//            }
            ndef.writeNdefMessage(message);
        } else {
            NdefFormatable format = NdefFormatable.get(tag);
            if (format != null) {
                format.connect();
                format.format(message);
            } else {
                throw new IllegalArgumentException("Ndef format is NULL");
            }
        }
    }

    private void getDimLink(){
        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse(Constants.FIREBASE_DYNAMIC_LINK + "/document/" + documentId.toString()))
                .setDomainUriPrefix(Constants.FIREBASE_DYNAMIC_LINK)
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().build())
                //.setIosParameters(new DynamicLink.IosParameters.Builder(NFCRegisterActivity.this.getPackageName()).build())
                .setIosParameters(
                        new DynamicLink.IosParameters.Builder("com.kyad.traystorage")
                                .setAppStoreId("1608315959")
                                .build())
                .setNavigationInfoParameters(new DynamicLink.NavigationInfoParameters.Builder().setForcedRedirectEnabled(true).build())
                .buildShortDynamicLink()
                .addOnSuccessListener(NFCRegisterActivity.this, new OnSuccessListener<ShortDynamicLink>() {
                    @Override
                    public void onSuccess(ShortDynamicLink shortDynamicLink) {

                        Uri shortLink = shortDynamicLink.getShortLink();
                        Log.d("WinIntec", "deepLink=" + shortLink.toString());
                        dimLink = shortLink.toString();
                    }
                }).addOnFailureListener(NFCRegisterActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Log.d("WinIntec", "deepLink=error:");
                e.printStackTrace();
                Utils.showCustomToast(NFCRegisterActivity.this, R.string.dimlink_make_error);
            }
        });
    }
}
