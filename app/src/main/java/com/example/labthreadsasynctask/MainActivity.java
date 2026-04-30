package com.example.labthreadsasynctask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView statusTextView;
    private ProgressBar loadingBar;
    private ImageView displayImage;

    // Handler pour communiquer avec le thread principal
    private Handler uiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialisation des vues
        statusTextView = findViewById(R.id.statusTextView);
        loadingBar = findViewById(R.id.loadingBar);
        displayImage = findViewById(R.id.displayImage);

        Button loadButton = findViewById(R.id.loadButton);
        Button computeButton = findViewById(R.id.computeButton);
        Button messageButton = findViewById(R.id.messageButton);

        uiHandler = new Handler(Looper.getMainLooper());

        // Test de réactivité (doit fonctionner pendant les traitements)
        messageButton.setOnClickListener(v ->
                Toast.makeText(this, "L'interface reste fluide !", Toast.LENGTH_SHORT).show()
        );

        // Lancement du Thread pour l'image
        loadButton.setOnClickListener(v -> executeImageTask());

        // Lancement de l'AsyncTask pour le calcul
        computeButton.setOnClickListener(v -> new BackgroundComputeTask(this).execute());
    }

    private void executeImageTask() {
        loadingBar.setVisibility(View.VISIBLE);
        loadingBar.setProgress(0);
        statusTextView.setText("Action : Chargement d'image via Thread...");

        new Thread(() -> {
            try {
                // Simulation d'un travail de fond
                Thread.sleep(1200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

            // Mise à jour de l'UI via le Handler
            uiHandler.post(() -> {
                displayImage.setImageBitmap(bitmap);
                loadingBar.setVisibility(View.GONE);
                statusTextView.setText("Résultat : Image chargée avec succès");
            });
        }).start();
    }

    /**
     * AsyncTask statique avec WeakReference pour éviter les fuites de mémoire.
     */
    private static class BackgroundComputeTask extends AsyncTask<Void, Integer, Double> {
        private final WeakReference<MainActivity> activityReference;

        BackgroundComputeTask(MainActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPreExecute() {
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            activity.loadingBar.setVisibility(View.VISIBLE);
            activity.loadingBar.setProgress(0);
            activity.statusTextView.setText("Action : Calcul intensif (AsyncTask)...");
        }

        @Override
        protected Double doInBackground(Void... params) {
            double sum = 0;
            for (int i = 1; i <= 100; i++) {
                // Simulation de calcul mathématique
                for (int j = 0; j < 250000; j++) {
                    sum += Math.sqrt(i * (double)j) % 8;
                }
                publishProgress(i);
            }
            return sum;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            activity.loadingBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Double result) {
            MainActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;

            activity.loadingBar.setVisibility(View.GONE);
            String formattedResult = String.format(Locale.getDefault(), "Calcul terminé : %.2f", result);
            activity.statusTextView.setText(formattedResult);
        }
    }
}
