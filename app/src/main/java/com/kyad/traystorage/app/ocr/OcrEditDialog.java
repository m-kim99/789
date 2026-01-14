package com.kyad.traystorage.app.ocr;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.kyad.traystorage.R;
import com.kyad.traystorage.databinding.DialogOcrEditBinding;

import java.util.List;

public class OcrEditDialog extends Dialog {

    private DialogOcrEditBinding binding;
    private OcrResultListener listener;
    private OcrService ocrService;
    private List<String> imagePaths;
    private Handler mainHandler;

    public interface OcrResultListener {
        void onOcrTextConfirmed(String ocrText);
    }

    public OcrEditDialog(Context context) {
        super(context, R.style.DialogCustomTheme);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_ocr_edit, null, false);
        setContentView(binding.getRoot());
        setCancelable(false);
        binding.setDialog(this);
    }

    public static OcrEditDialog show(Context context, List<String> imagePaths, OcrResultListener listener) {
        OcrEditDialog dialog = new OcrEditDialog(context);
        dialog.imagePaths = imagePaths;
        dialog.listener = listener;
        dialog.show();
        dialog.startOcr();
        return dialog;
    }

    private void startOcr() {
        if (imagePaths == null || imagePaths.isEmpty()) {
            binding.statusText.setText("처리할 이미지가 없습니다.");
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.editOcrText.setEnabled(false);
        binding.btnConfirm.setEnabled(false);
        binding.statusText.setText("이미지 OCR 처리 중...");

        ocrService = new OcrService();
        ocrService.processImages(imagePaths, new OcrService.OcrCallback() {
            @Override
            public void onProgress(int current, int total) {
                mainHandler.post(() -> {
                    binding.statusText.setText(String.format("이미지 처리 중... (%d/%d)", current, total));
                });
            }

            @Override
            public void onSuccess(String combinedText, List<OcrService.OcrResult> results) {
                mainHandler.post(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.editOcrText.setEnabled(true);
                    binding.btnConfirm.setEnabled(true);

                    if (combinedText == null || combinedText.isEmpty()) {
                        binding.statusText.setText("텍스트를 인식하지 못했습니다.");
                        binding.editOcrText.setText("");
                    } else {
                        int successCount = 0;
                        for (OcrService.OcrResult r : results) {
                            if (r.error == null) successCount++;
                        }
                        binding.statusText.setText(String.format("OCR 완료 (%d/%d 이미지 성공). 텍스트를 편집 후 적용하세요.", 
                                successCount, results.size()));
                        binding.editOcrText.setText(combinedText);
                    }
                });
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.editOcrText.setEnabled(true);
                    binding.btnConfirm.setEnabled(true);
                    binding.statusText.setText("OCR 오류: " + error);
                });
            }
        });
    }

    public void onClickConfirm() {
        if (listener != null) {
            String ocrText = binding.editOcrText.getText().toString().trim();
            listener.onOcrTextConfirmed(ocrText);
        }
        dismiss();
    }

    public void onClickCancel() {
        if (ocrService != null) {
            ocrService.cancel();
        }
        dismiss();
    }

    public void onClickOutside() {
        // 바깥 클릭 시 닫지 않음 (작업 중 실수 방지)
    }

    @Override
    public void dismiss() {
        if (ocrService != null) {
            ocrService.cancel();
        }
        super.dismiss();
    }
}
