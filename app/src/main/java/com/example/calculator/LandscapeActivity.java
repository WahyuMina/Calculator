package com.example.calculator;

import android.os.Bundle;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.widget.Button;


public class LandscapeActivity extends MainActivity {

    private boolean isRadian = false; // Default Degree
    private boolean isInverse = false; // Default Normal


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvDisplay = findViewById(R.id.tv_display);
        layoutHistory = findViewById(R.id.layout_history);
        tvHistory = findViewById(R.id.tv_history);
        btnShowHistory = findViewById(R.id.btn_show_history);

        String data = getIntent().getStringExtra("input_ekstra");
        if (data != null && !data.isEmpty()) {
            currenInput = data;
            if (tvDisplay != null) tvDisplay.setText(formatKeTampilan(currenInput));
        } else {
            currenInput = "0";
        }

        String historyData = getIntent().getStringExtra("history_ekstra");
        if (historyData != null && tvHistory != null) {
            tvHistory.setText(historyData);
        }

        if (tvDisplay != null) {
            tvDisplay.setText(formatKeTampilan(currenInput));
        }

        // Tombol Show History
        btnShowHistory.setOnClickListener(v -> {
            if (layoutHistory.getVisibility() == View.GONE) {
                layoutHistory.setVisibility(View.VISIBLE);
                tvDisplay.setVisibility(View.GONE); // Sembunyikan angka utama
            } else {
                layoutHistory.setVisibility(View.GONE);
                tvDisplay.setVisibility(View.VISIBLE); // Tampilkan angka utama
            }
        });

        setupScientificButtons();

        Button btnPotrait = findViewById(R.id.btn_potrait);
        if (btnPotrait != null) {
            btnPotrait.setOnClickListener(v -> finish());
        }

        // HUBUNGKAN SEMUA TOMBOL
        setupScientificButtons();
        setupNumberButtons();
        setupOperatorButtons();
        setupSpecialButtons();
    }

    private void setupScientificButtons() {
        // Operasi Trigono (Sudah Diperbaiki)
        setSciLogic(R.id.btn_sin, val -> Math.sin(Math.toRadians(val)));
        setSciLogic(R.id.btn_cos, val -> Math.cos(Math.toRadians(val)));
        setSciLogic(R.id.btn_tan, val -> Math.tan(Math.toRadians(val)));

        // Logaritma (Sudah Diperbaiki)
        setSciLogic(R.id.btn_in, Math::log);    // ln
        setSciLogic(R.id.btn_log, Math::log10); // log10

        // Akar & Pangkat
        setSciLogic(R.id.btn_sqrt, Math::sqrt);
        setSciLogic(R.id.btn_rank, val -> Math.pow(val, 2));

        // Faktorial
        setSciLogic(R.id.btn_factorial, this::factorial);

        // Konstanta
        setConstant(R.id.btn_pi, Math.PI);
        setConstant(R.id.btn_e, Math.E);

        setSciLogic(R.id.btn_sin, val -> {
            if (isInverse) {
                // Jika INV aktif, hitung Arcsin
                return Math.toDegrees(Math.asin(val));
            } else {
                // Jika Normal, cek apakah perlu dikonversi ke Radian
                double angle = isRadian ? val : Math.toRadians(val);
                return Math.sin(angle);
            }
        });

        // Tombol Pangkat Bebas (x^y)
        View btnXY = findViewById(R.id.btn_xy);
        if (btnXY != null) {
            btnXY.setOnClickListener(v -> {
                if (!currenInput.isEmpty() && !currenInput.endsWith(" ")) {
                    selectedOperator = "^"; // Pakai simbol ^ untuk pangkat
                    currenInput += " ^ ";
                    tvDisplay.setText(formatKeTampilan(currenInput));
                }
            });
        }


        // Tombol Khusus (Pindahkan ke dalam fungsi ini)
        View btnNoll = findViewById(R.id.btn_noll);
        if (btnNoll != null) {
            btnNoll.setOnClickListener(v -> {
                if (!currenInput.equals("0") && !currenInput.isEmpty()) {
                    currenInput += "00";
                    tvDisplay.setText(formatKeTampilan(currenInput));
                }
            });
        }

        View btnBracket = findViewById(R.id.btn_bracket); // Pastikan ID ini ada di XML
        if (btnBracket != null) {
            btnBracket.setOnClickListener(v -> {
                // Jika input kosong atau diakhiri dengan spasi, tambahkan "("
                if (currenInput.isEmpty() || currenInput.endsWith(" ")) {
                    currenInput += "(";
                } else {
                    currenInput += ")";
                }
                tvDisplay.setText(formatKeTampilan(currenInput));
            });
        }


        // Tombol RAD/DEG
        View btnRad = findViewById(R.id.btn_rad);
        if (btnRad != null) {
            btnRad.setOnClickListener(v -> {
                isRadian = !isRadian; // Balikkan status (Toggle)
                Button b = (Button) v;
                b.setText(isRadian ? "RAD" : "DEG"); // Ubah teks di tombolnya
                tvDisplay.setText(isRadian ? "Mode: Radian" : "Mode: Degree");
            });
        }

        View btnInv = findViewById(R.id.btn_inv);
        if (btnInv != null) {
            btnInv.setOnClickListener(v -> {
                isInverse = !isInverse;


                int warnaAktif = 0xFFFF8C00;
                int warnaMati = 0xFF1E1E1E;


                v.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                        isInverse ? warnaAktif : warnaMati
                ));
                if (v instanceof Button) {
                    ((Button) v).setTextColor(isInverse ? 0xFFFFFFFF : 0xFF808080);
                }
            });
        }
    }

    private void setSciLogic(int id, SciOp op) {
        View btn = findViewById(id);
        if (btn != null) {
            btn.setOnClickListener(v -> {
                if (!currenInput.isEmpty()) {
                    try {
                        double val = Double.parseDouble(currenInput.replace(".", "").replace(",", "."));
                        double result = op.execute(val);
                        currenInput = String.valueOf(result).replace(".", ",");
                        tvDisplay.setText(formatKeTampilan(currenInput));
                    } catch (Exception e) {
                        tvDisplay.setText("Error");
                    }
                }
            });
        }
    }

    private void setConstant(int id, double value) {
        View btn = findViewById(id);
        if (btn != null) {
            btn.setOnClickListener(v -> {
                currenInput = String.valueOf(value).replace(".", ",");
                tvDisplay.setText(formatKeTampilan(currenInput));
            });
        }
    }
    private double factorial(double n) {
        if (n < 0) return 0;
        if (n == 0 || n == 1) return 1;
        double result = 1;
        for (int i = 2; i <= (int) n; i++) result *= i;
        return result;
    }
    interface SciOp {
        double execute(double val);
    }
}