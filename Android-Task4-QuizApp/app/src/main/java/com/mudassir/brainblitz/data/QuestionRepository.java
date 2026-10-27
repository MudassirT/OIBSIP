package com.mudassir.brainblitz.data;

import android.content.Context;
import android.util.Log;

import com.mudassir.brainblitz.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Repository responsible for loading and parsing quiz questions from local JSON resources.
 * Contains defensive error handling and fallbacks to guarantee offline stability.
 */
public class QuestionRepository {

    private static final String TAG = "QuestionRepository";

    /**
     * Loads the questions list from raw resources.
     * Wrapped in try/catch to ensure corrupted or missing files do not crash the app.
     */
    public List<Question> loadQuestions(Context context) {
        if (context == null) {
            return getFallbackQuestions();
        }

        try (InputStream is = context.getResources().openRawResource(R.raw.questions)) {
            return loadFromStream(is);
        } catch (Exception e) {
            Log.e(TAG, "Error loading questions from raw/questions.json. Falling back to default questions.", e);
            return getFallbackQuestions();
        }
    }

    /**
     * Loads and parses questions from any InputStream.
     */
    public List<Question> loadFromStream(InputStream is) {
        if (is == null) {
            return getFallbackQuestions();
        }

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return parseQuestionsJson(sb.toString());
        } catch (Exception e) {
            Log.e(TAG, "Failed reading questions stream: " + e.getMessage(), e);
            return getFallbackQuestions();
        }
    }

    /**
     * Parses a JSON string representation of questions.
     */
    public List<Question> parseQuestionsJson(String jsonString) {
        List<Question> questions = new ArrayList<>();
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return questions;
        }

        try {
            JSONArray array = new JSONArray(jsonString);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                int id = obj.optInt("id", i + 1);
                String category = obj.optString("category", "General Tech");
                String questionText = obj.getString("question");
                JSONArray optionsArray = obj.getJSONArray("options");

                List<String> options = new ArrayList<>();
                for (int j = 0; j < optionsArray.length(); j++) {
                    options.add(optionsArray.getString(j));
                }

                int correctIndex = obj.getInt("correctIndex");
                String explanation = obj.optString("explanation", "");

                // Validate that question is well-formed
                if (!questionText.trim().isEmpty() && options.size() == 4 && correctIndex >= 0 && correctIndex < 4) {
                    questions.add(new Question(id, category, questionText, options, correctIndex, explanation));
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Malformed questions JSON detected: " + e.getMessage(), e);
        }

        if (questions.isEmpty()) {
            return getFallbackQuestions();
        }

        return questions;
    }

    /**
     * Provides a built-in fallback question pool in case raw resource loading ever fails.
     */
    public List<Question> getFallbackQuestions() {
        List<Question> fallback = new ArrayList<>();
        fallback.add(new Question(
                1, "Data Structures",
                "Which data structure follows the Last-In-First-Out (LIFO) principle?",
                Arrays.asList("Stack", "Queue", "Array", "Tree"),
                0, "A Stack pushes and pops elements from the top, strictly enforcing LIFO order."
        ));
        fallback.add(new Question(
                2, "Algorithms",
                "What is the average time complexity of searching an element in a balanced Binary Search Tree?",
                Arrays.asList("O(1)", "O(log n)", "O(n)", "O(n²)"),
                1, "Each comparison in a balanced BST discards half of the remaining subtrees."
        ));
        fallback.add(new Question(
                3, "Web Protocols",
                "Which HTTP method is typically used to request data without modifying server state?",
                Arrays.asList("POST", "DELETE", "GET", "PUT"),
                2, "GET requests are idempotent and intended for safe retrieval of server resources."
        ));
        fallback.add(new Question(
                4, "Android",
                "Which Android lifecycle method is called when an Activity is first created?",
                Arrays.asList("onCreate()", "onStart()", "onResume()", "onPause()"),
                0, "onCreate() is the initial entry point where layout inflation and setup occur."
        ));
        fallback.add(new Question(
                5, "Operating Systems",
                "What is a critical section in concurrent programming?",
                Arrays.asList("A code segment where shared resources are accessed", "A hardware fault handler", "An unused memory buffer", "A fast CPU cache"),
                0, "Critical sections access shared data and must be synchronized to prevent race conditions."
        ));
        return fallback;
    }
}
