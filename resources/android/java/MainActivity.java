package app.financeiro.pessoal;

import android.content.Intent;
import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(FinanceiroPlugin.class);
        super.onCreate(savedInstanceState);
        grab(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        grab(intent);
    }

    private void grab(Intent i) {
        if (i != null && i.hasExtra("imp_id")) FinanceiroPlugin.launchId = i.getStringExtra("imp_id");
    }
}
