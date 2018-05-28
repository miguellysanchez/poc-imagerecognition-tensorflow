package mariannelinhares.mnistandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity implements View.OnClickListener {

    private Button goToSketchActivityButton;
    private Button goToImageActivityButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();
    }

    private void initializeViews() {
        goToSketchActivityButton = (Button) findViewById(R.id.main_button_goto_sketch);
        goToSketchActivityButton.setOnClickListener(this);

        goToImageActivityButton = (Button)findViewById(R.id.main_button_goto_image);
        goToImageActivityButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_button_goto_sketch:
                Intent goToSketchIntent = new Intent(this, SketchActivity.class);
                startActivity(goToSketchIntent);
                break;
            case R.id.main_button_goto_image:
                Intent goToImageIntent = new Intent(this, ImageActivity.class);
                startActivity(goToImageIntent);
                break;
            default:
                break;
        }
    }
}
