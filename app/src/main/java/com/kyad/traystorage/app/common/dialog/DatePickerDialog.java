package com.kyad.traystorage.app.common.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.aigestudio.wheelpicker.WheelPicker;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.kyad.traystorage.R;
import com.kyad.traystorage.databinding.DialogDatePickerBinding;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;

public class DatePickerDialog extends Dialog {

    private static DialogDatePickerBinding binding;
    private ActionListener listener;

    public interface ActionListener {
        void onSelect(String date);
    }

    DatePickerDialog(Context context) {
        super(context, R.style.DialogCustomTheme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_date_picker, null, false);
        setContentView(binding.getRoot());

        binding.wheelDatePickerYear.setOnWheelChangeListener(new WheelPicker.OnWheelChangeListener() {
            @Override
            public void onWheelScrolled(int i) {

            }

            @Override
            public void onWheelSelected(int i) {

            }

            @Override
            public void onWheelScrollStateChanged(int i) {
                binding.wheelDatePickerDay.setYear(binding.wheelDatePickerYear.getCurrentYear());
            }
        });

        binding.wheelDatePickerMonth.setOnWheelChangeListener(new WheelPicker.OnWheelChangeListener() {
            @Override
            public void onWheelScrolled(int i) {

            }

            @Override
            public void onWheelSelected(int i) {

            }

            @Override
            public void onWheelScrollStateChanged(int i) {
                binding.wheelDatePickerDay.setMonth(binding.wheelDatePickerMonth.getCurrentMonth());
            }
        });

        initView();
    }

    private void initView() {
        binding.setPresenter(new Presenter());
    }

    /************************************************************
     *  Public
     ************************************************************/

    public static DatePickerDialog show(Context context) {
        DatePickerDialog dialog = new DatePickerDialog(context);
        dialog.show();
        return dialog;
    }

    public DatePickerDialog setDate(String date) {
        Calendar calendar = Calendar.getInstance();
        int thisYear = calendar.get(Calendar.YEAR);

        //binding.wheelDatePickerYear.setYearStart(1970);
        //binding.wheelDatePickerYear.setYearEnd(thisYear);

        if (!date.isEmpty()) {
            binding.wheelDatePickerYear.setSelectedYear(Integer.parseInt(date.substring(0, 4)));
            binding.wheelDatePickerMonth.setSelectedMonth(Integer.parseInt(date.substring(5, 7)));
            binding.wheelDatePickerDay.setSelectedDay(Integer.parseInt(date.substring(8, 10)));
        }

        return this;
    }

    public DatePickerDialog setListener(@NotNull ActionListener listener) {
        this.listener = listener;
        return this;
    }

    public class Presenter {
        public void onCancel() {
            dismiss();
        }

        public void onClickConfirm() {
            listener.onSelect(String.format("%04d.%02d.%02d", binding.wheelDatePickerYear.getCurrentYear(), binding.wheelDatePickerMonth.getCurrentMonth(), binding.wheelDatePickerDay.getCurrentDay()));
            dismiss();
        }
    }
}

