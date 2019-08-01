package com.example.toothpick.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.toothpick.R;
import com.example.toothpick.annotation.ApplicationScope;
import com.example.toothpick.helper.BackpackItemValidator;
import javax.inject.Inject;
import toothpick.Lazy;
import toothpick.Toothpick;

public class AddNewActivity extends AppCompatActivity {

    public static final String NEW_ITEM_NAME_KEY = "name";

    @Inject Lazy<BackpackItemValidator> backpackItemValidator;

    private EditText editText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backpack_new);

        // 1. Open Activity scope as child of Application scope
        // 2. Inject dependencies
        Toothpick.openScopes(ApplicationScope.class, this)
            .inject(this);

        editText = findViewById(R.id.new_name);
        Button button = findViewById(R.id.add_item);
        button.setOnClickListener(v -> {
            if (backpackItemValidator.get().isValidName(editText.getText().toString())) {
                returnNewElement();
            }
        });
    }

    private void returnNewElement() {
        Intent intent = new Intent();
        intent.putExtra(NEW_ITEM_NAME_KEY, editText.getText().toString());
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
