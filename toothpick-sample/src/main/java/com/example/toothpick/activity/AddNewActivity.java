package com.example.toothpick.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.example.toothpick.R;
import com.example.toothpick.annotation.ApplicationScope;
import com.example.toothpick.helper.BackpackItemValidator;
import javax.inject.Inject;
import toothpick.Toothpick;

public class AddNewActivity extends AppCompatActivity {

    public final static String NEW_ITEM_NAME_KEY = "name";

    //will be created as a singleton in the root scope
    //and is releasable under memory pressure
    @Inject BackpackItemValidator backpackItemValidator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. Open Activity scope as child of Application scope
        // 2. Inject dependencies
        Toothpick.openScopes(ApplicationScope.class, this)
            .inject(this);

        setupUIComponents();
    }

    private void setupUIComponents() {
        setContentView(R.layout.backpack_new);
        final EditText editText = findViewById(R.id.new_name);
        Button button = findViewById(R.id.add_item);
        button.setOnClickListener( view ->  {
            String text = editText.getText().toString();
            if (backpackItemValidator.isValidName(text)) {
                Intent intent = new Intent()
                    .putExtra(NEW_ITEM_NAME_KEY, text);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }
}
