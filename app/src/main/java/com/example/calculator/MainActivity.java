package com.example.calculator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.pm.ActivityInfo;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    protected TextView tvDisplay;
    protected String currenInput = "";
    protected String selectedOperator = "";
    protected double firstValue = Double.NaN;
    protected ScrollView layoutHistory;
    protected TextView tvHistory;
    protected ImageButton btnShowHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        if (!(this instanceof LandscapeActivity)) {
            setContentView(R.layout.activity_main);
        }

        View mainView = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tvDisplay = findViewById(R.id.tv_display);
        layoutHistory = findViewById(R.id.layout_history);
        tvHistory = findViewById(R.id.tv_history);
        btnShowHistory = findViewById(R.id.btn_show_history);

        // ini adalah fungsi inheritance dari LandscapeActivity
        setupNumberButtons();
        setupOperatorButtons();
        setupSpecialButtons();

        // Tombol Clear & Hapus
        findViewById(R.id.btn_clear).setOnClickListener(v -> {
            currenInput = ""; selectedOperator = ""; firstValue = Double.NaN; tvDisplay.setText("0");
        });

        findViewById(R.id.btn_eraser).setOnClickListener(v -> {
            if (currenInput.length() > 0) {
                currenInput = currenInput.endsWith(" ") ? currenInput.substring(0, currenInput.length() - 3) : currenInput.substring(0, currenInput.length() - 1);
                tvDisplay.setText(currenInput.isEmpty() ? "0" : formatKeTampilan(currenInput));
            }
        });

        // Tombol Persen
        findViewById(R.id.btn_persen).setOnClickListener(v -> {
            if (!currenInput.isEmpty() && selectedOperator.isEmpty()) {
                double val = Double.parseDouble(currenInput.replace(".", "").replace(",", ".")) / 100;
                currenInput = String.valueOf(val).replace(".", ",");
                tvDisplay.setText(formatKeTampilan(currenInput));
            }
        });

        btnShowHistory.setOnClickListener(v -> layoutHistory.setVisibility(layoutHistory.getVisibility() == View.GONE ? View.VISIBLE : View.GONE));

        Button btnLandscape = findViewById(R.id.btn_landscape);

        if (btnLandscape != null) {
            btnLandscape.setOnClickListener(v -> {
                // Buat Intent untuk pindah ke si "Anak"
                Intent intent = new Intent(MainActivity.this, LandscapeActivity.class);

                // Titipkan angka terakhir agar di Landscape tidak reset jadi 0
                intent.putExtra("input_ekstra", currenInput);
                intent.putExtra("history_ekstra", tvHistory.getText().toString()); // Tambahkan ini

                startActivity(intent);

            });
        }

    }
    // --- LISTENER ANGKA ---
    protected void setupNumberButtons() {
        int[] numberIds = {
                R.id.btn_0, R.id.btn_1, R.id.btn_2, R.id.btn_3, R.id.btn_4,
                R.id.btn_5, R.id.btn_6, R.id.btn_7, R.id.btn_8, R.id.btn_9, R.id.btn_coma
        };

        View.OnClickListener numberListener = v -> {
            Button b = (Button) v;
            String text = b.getText().toString();
            String angkaMurni = currenInput.replace(" ", "").replace(".", "").replace(",", "");
            if (!text.equals(",") && angkaMurni.length() >= 15) return;

            if (text.equals(",")) {
                String lastPart = currenInput;
                if (!selectedOperator.isEmpty()) {
                    int lastOpPos = currenInput.lastIndexOf(selectedOperator);
                    lastPart = currenInput.substring(lastOpPos);
                }
                if (lastPart.contains(",")) return;
                text = (currenInput.isEmpty() || currenInput.endsWith(" ")) ? "0," : ",";
            } else if (currenInput.equals("0")) {
                currenInput = "";
            }
            currenInput += text;
            if (tvDisplay != null) tvDisplay.setText(formatKeTampilan(currenInput));
        };

        for (int id : numberIds) {
            View v = findViewById(id);
            if (v != null) v.setOnClickListener(numberListener); // PAGAR AMAN
        }
    };
    // LISTENER OPERATOR
    protected void setupOperatorButtons() {
        int[] operatorIds = {R.id.btn_plus, R.id.btn_minus, R.id.btn_kali, R.id.btn_bagi};
        View.OnClickListener operatorListener = v -> {
            Button b = (Button) v;
            if (currenInput.isEmpty()) return;

            // Logic Ganti Operator: Potong 3 karakter (spasi-op-spasi)
            if (currenInput.endsWith(" ")) {
                currenInput = currenInput.substring(0, currenInput.length() - 3);
            } else {
                try {
                    firstValue = Double.parseDouble(currenInput.replace(".", "").replace(",", "."));
                } catch (Exception e) {
                    firstValue = 0;
                }
            }

            selectedOperator = b.getText().toString();
            currenInput += " " + selectedOperator + " ";
            tvDisplay.setText(formatKeTampilan(currenInput));
        };
        for (int id : operatorIds) findViewById(id).setOnClickListener(operatorListener);
    };
    // Tombol Hasil
    protected void setupSpecialButtons() {
        findViewById(R.id.btn_result).setOnClickListener(v -> {
            if (!currenInput.isEmpty() && currenInput.contains(" ")) {
                try {
                    String riwayatInput = currenInput;
                    String clean = currenInput.replace(".", "").replace(",", ".");
                    String[] parts = clean.split(" ");
                    double total = Double.parseDouble(parts[0]);

                    for (int i = 1; i < parts.length; i += 2) {
                        double nextVal = Double.parseDouble(parts[i + 1]);
                        switch (parts[i]) {
                            case "+":
                                total += nextVal;
                                break;
                            case "-":
                                total -= nextVal;
                                break;
                            case "×":
                                total *= nextVal;
                                break;
                            case "÷":
                                total = (nextVal != 0) ? total / nextVal : 0;
                                break;
                        }
                    }

                    String formatted = new DecimalFormat("0.######").format(total).replace(".", ",");
                    tvHistory.append(riwayatInput + " = " + formatted + "\n");
                    currenInput = formatted;
                    tvDisplay.setText(formatKeTampilan(currenInput));
                    selectedOperator = "";
                } catch (Exception e) {
                    tvDisplay.setText("Error");
                }
            }
        });

        // Tombol Clear
        View btnClr = findViewById(R.id.btn_clear);
        if (btnClr != null) {
            btnClr.setOnClickListener(v -> {
                currenInput = "";
                selectedOperator = "";
                firstValue = Double.NaN;
                if (tvDisplay != null) tvDisplay.setText("0");
            });
        }

        // Tombol Hapus
        View btnErs = findViewById(R.id.btn_eraser);
        if (btnErs != null) {
            btnErs.setOnClickListener(v -> {
                if (currenInput.length() > 0) {
                    // Jika hapus spasi operator (3 karakter) atau angka biasa (1 karakter)
                    currenInput = currenInput.endsWith(" ") ?
                            currenInput.substring(0, currenInput.length() - 3) :
                            currenInput.substring(0, currenInput.length() - 1);
                    if (tvDisplay != null)
                        tvDisplay.setText(currenInput.isEmpty() ? "0" : formatKeTampilan(currenInput));
                }
            });
        }

        // Tombol Persen
        View btnPct = findViewById(R.id.btn_persen);
        if (btnPct != null) {
            btnPct.setOnClickListener(v -> {
                if (!currenInput.isEmpty() && selectedOperator.isEmpty()) {
                    try {
                        double val = Double.parseDouble(currenInput.replace(".", "").replace(",", ".")) / 100;
                        currenInput = String.valueOf(val).replace(".", ",");
                        if (tvDisplay != null) tvDisplay.setText(formatKeTampilan(currenInput));
                    } catch (Exception e) { }
                }
            });
        }

        // Tombol Nol (0) && Noll (00)
        setNolLogic(R.id.btn_nol, "0");
        setNolLogic(R.id.btn_noll, "00");

        // Tombol Hasil
        View btnRes = findViewById(R.id.btn_result);
        if (btnRes != null) {
            btnRes.setOnClickListener(v -> hitungHasilAkhir()); // Pindahkan logika hitung ke fungsi sendiri
        }
    };

    // Fungsi untuk mengatur tombol nol
    private void setNolLogic(int id, String tambahan) {
        View btn = findViewById(id);
        if (btn != null) {
            btn.setOnClickListener(v -> {
                if (!currenInput.equals("0")) { // Mencegah nol ganda
                    currenInput += tambahan;
                    if (tvDisplay != null) tvDisplay.setText(formatKeTampilan(currenInput));
                }
            });
        }
    }

//  Hasil Akhir
    protected void hitungHasilAkhir() {
        if (!currenInput.isEmpty() && currenInput.contains(" ")) {
            try {
                String cleanInput = currenInput.replace("(", "").replace(")", "");
                String clean = cleanInput.trim().replace(".", "").replace(",", ".");
                String riwayatInput = currenInput;

                String[] parts = clean.split("\\s+");
                double total = Double.parseDouble(parts[0]);

                for (int i = 1; i < parts.length; i += 2) {
                    String operator = parts[i];
                    double nextVal = Double.parseDouble(parts[i + 1]);
                    switch (parts[i]) {
                        case "+": total += nextVal; break;
                        case "-": total -= nextVal; break;
                        case "×": total *= nextVal; break;
                        case "÷": total = (nextVal != 0) ? total / nextVal : 0; break;
                        case "^": total = Math.pow(total, nextVal); break;
                    }
                }

                // Format hasil akhir ke tampilan dengan koma sebagai desimal
                String formatted = new DecimalFormat("0.######").format(total).replace(".", ",");

                if (tvHistory != null) {
                    tvHistory.append(riwayatInput + " = " + formatted + "\n");
                }

                currenInput = formatted;
                if (tvDisplay != null) {
                    tvDisplay.setText(formatKeTampilan(currenInput));
                }
                selectedOperator = "";
            } catch (Exception e) {
                if (tvDisplay != null) tvDisplay.setText("Error");
            }
        }
    }

    // Fungsi Format (TITIK RIBUAN)
     String formatKeTampilan(String input) {
        if (input.isEmpty() || input.equals("0")) return "0";
        try {
            if (input.contains(" ")) {
                String[] parts = input.split(" ", 3);
                StringBuilder sb = new StringBuilder().append(formatSatuAngka(parts[0]));
                if (parts.length > 1) sb.append(" ").append(parts[1]).append(" ");
                if (parts.length > 2) sb.append(formatSatuAngka(parts[2]));
                return sb.toString();
            }
            return formatSatuAngka(input);
        } catch (Exception e) { return input; }
    }

    protected String formatSatuAngka(String s) {
        if (s.isEmpty()) return "";
        try {
            java.math.BigDecimal bd = new java.math.BigDecimal(s.replace(".", "").replace(",", "."));
            DecimalFormatSymbols sym = new DecimalFormatSymbols(Locale.GERMAN);
            sym.setGroupingSeparator('.'); sym.setDecimalSeparator(',');
            String res = new DecimalFormat("#,###.######", sym).format(bd);
            return s.endsWith(",") ? res + "," : res;
        } catch (Exception e) { return s; }
    }
}

