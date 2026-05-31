package com.mcpbuilder.ia;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class MainActivity extends Activity {
    private int BLUE;
    private int TEAL;
    private int GREEN;
    private int INK;
    private int MUTED;
    private int LINE;
    private int SURFACE;
    private int CARD;
    private int CODE_BG;
    private int CODE_TEXT;
    private AppColors colors;

    private EditText projectNameInput;
    private EditText intentInput;
    private EditText siteUrlInput;
    private EditText usernameInput;
    private EditText secretInput;
    private Spinner connectorSpinner;
    private Spinner savedProjectSpinner;
    private ArrayAdapter<String> savedProjectAdapter;
    private TextView credentialsTitle;
    private TextView usernameLabel;
    private TextView secretLabel;
    private TextView connectorHelpText;
    private TextView previewText;
    private TextView previewStatsText;
    private TextView statusText;
    private Button toolsTab;
    private Button varsTab;
    private Button codeTab;
    private Button docsTab;
    private Button promptsTab;
    private SharedPreferences preferences;
    private LinkedHashMap<String, String> generatedFiles = new LinkedHashMap<>();
    private List<String> currentTools = new ArrayList<>();
    private String currentProjectName = "mcp-business-agent";
    private String currentConnector = "WooCommerce";
    private String currentIntent = "";
    private String activePreview = "Tools";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        loadThemeColors();
        preferences = getSharedPreferences("mcp_builder_projects", MODE_PRIVATE);
        showIntroScreen();
    }

    private void loadThemeColors() {
        colors = AppColors.from(this);
        BLUE = colors.blue;
        TEAL = colors.teal;
        GREEN = colors.green;
        INK = colors.ink;
        MUTED = colors.muted;
        LINE = colors.line;
        SURFACE = colors.surface;
        CARD = colors.card;
        CODE_BG = colors.codeBg;
        CODE_TEXT = colors.codeText;
    }

    private void showIntroScreen() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(SURFACE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14), dp(14) + systemBarHeight("status_bar_height"), dp(14), dp(28) + systemBarHeight("navigation_bar_height"));
        scrollView.addView(root, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        LinearLayout hero = panel();
        hero.setPadding(dp(18), dp(18), dp(18), dp(18));
        hero.setBackground(bg(colors.heroBg, colors.heroLine, 8));
        root.addView(hero, panelParams());

        TextView title = text("MCP Builder IA", 30, INK, true);
        hero.addView(title);

        TextView subtitle = text("Crea conectores MCP listos para que una IA trabaje con datos y herramientas reales de un negocio.", 15, MUTED, false);
        subtitle.setPadding(0, dp(8), 0, dp(14));
        hero.addView(subtitle);

        LinearLayout heroMeta = new LinearLayout(this);
        heroMeta.setOrientation(LinearLayout.HORIZONTAL);
        hero.addView(heroMeta);
        heroMeta.addView(pill("Conectores", BLUE, colors.primarySoft));
        TextView secondPill = pill("Automatizacion", TEAL, colors.secondarySoft);
        LinearLayout.LayoutParams secondPillParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        secondPillParams.setMargins(dp(8), 0, 0, 0);
        heroMeta.addView(secondPill, secondPillParams);

        LinearLayout purpose = panel();
        root.addView(purpose, panelParams());
        purpose.addView(sectionTitle("Para que sirve"));
        purpose.addView(bodyText("Esta APK ayuda a crear la estructura de un servidor MCP para conectar una IA con sistemas como WooCommerce, WordPress, Google Sheets, Supabase, Notion, CRMs o ERPs propios."));
        purpose.addView(bodyText("En vez de empezar desde cero, eliges el tipo de conector, describes lo que necesitas y la app genera archivos base, variables de entorno, documentacion, prompts y un ZIP exportable."));

        LinearLayout cases = panel();
        root.addView(cases, panelParams());
        cases.addView(sectionTitle("Casos de uso practico"));
        cases.addView(useCase("Tienda online", "Consultar productos, actualizar stock, revisar pedidos y crear informes semanales de ventas."));
        cases.addView(useCase("Web WordPress", "Publicar borradores, revisar paginas, leer entradas y preparar tareas de mantenimiento."));
        cases.addView(useCase("Google Sheets", "Convertir hojas en una base de datos ligera para reportes, seguimiento de leads o inventario."));
        cases.addView(useCase("CRM o ERP propio", "Dar a la IA acceso controlado a clientes, facturas, tickets o procesos internos."));
        cases.addView(useCase("Prototipos MCP", "Generar una base tecnica rapida para probar herramientas antes de desarrollarlas en serio."));

        LinearLayout flow = panel();
        root.addView(flow, panelParams());
        flow.addView(sectionTitle("Como se usa"));
        flow.addView(stepItem("1", "Elige el conector", "WooCommerce, WhatsApp Business, WordPress, Sheets, Supabase o una API propia."));
        flow.addView(stepItem("2", "Describe el trabajo", "Explica que datos debe consultar, que acciones puede ejecutar y que limites debe respetar."));
        flow.addView(stepItem("3", "Revisa y exporta", "Comprueba tools, variables, codigo, docs y prompts antes de crear el ZIP."));

        Button startButton = button("Empezar a crear MCP", BLUE, colors.surface);
        root.addView(startButton, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(54)));
        startButton.setOnClickListener(v -> showBuilderScreen());

        setContentView(scrollView);
    }

    private void showBuilderScreen() {
        setContentView(buildUi());
        refreshSavedProjects();
        generateProject();
    }

    private View buildUi() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setBackgroundColor(SURFACE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(14), dp(12) + systemBarHeight("status_bar_height"), dp(14), dp(32) + systemBarHeight("navigation_bar_height"));
        scrollView.addView(root, new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        LinearLayout header = panel();
        header.setPadding(dp(16), dp(16), dp(16), dp(16));
        header.setBackground(bg(colors.heroBg, colors.heroLine, 8));
        root.addView(header, panelParams());

        TextView title = text("MCP Builder IA", 28, INK, true);
        header.addView(title);

        TextView subtitle = text("Crea conectores MCP para WordPress, WooCommerce y datos de negocio.", 14, MUTED, false);
        subtitle.setPadding(0, dp(8), 0, dp(12));
        header.addView(subtitle);

        LinearLayout headerMeta = new LinearLayout(this);
        headerMeta.setOrientation(LinearLayout.HORIZONTAL);
        header.addView(headerMeta);
        headerMeta.addView(pill("MVP nativo", TEAL, colors.secondarySoft));
        TextView filesPill = pill("ZIP listo", BLUE, colors.primarySoft);
        LinearLayout.LayoutParams filesPillParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        filesPillParams.setMargins(dp(8), 0, 0, 0);
        headerMeta.addView(filesPill, filesPillParams);

        LinearLayout flowHint = new LinearLayout(this);
        flowHint.setOrientation(LinearLayout.HORIZONTAL);
        flowHint.setGravity(Gravity.CENTER_VERTICAL);
        flowHint.setPadding(0, dp(14), 0, 0);
        header.addView(flowHint);
        flowHint.addView(miniStep("1", "Configura"));
        flowHint.addView(miniStep("2", "Genera"));
        flowHint.addView(miniStep("3", "Exporta"));

        LinearLayout form = panel();
        root.addView(form, panelParams());

        form.addView(sectionTitle("Configurar conector"));
        form.addView(label("Proyectos guardados"));
        LinearLayout savedRow = new LinearLayout(this);
        savedRow.setOrientation(LinearLayout.HORIZONTAL);
        form.addView(savedRow);

        savedProjectSpinner = new Spinner(this);
        savedProjectAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>());
        savedProjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        savedProjectSpinner.setAdapter(savedProjectAdapter);
        savedProjectSpinner.setBackground(bg(CARD, LINE, 6));
        savedProjectSpinner.setPadding(dp(10), 0, dp(10), 0);
        savedRow.addView(savedProjectSpinner, new LinearLayout.LayoutParams(0, dp(50), 1));

        Button loadButton = button("Abrir", colors.buttonLight, INK);
        LinearLayout.LayoutParams loadParams = new LinearLayout.LayoutParams(dp(88), dp(50));
        loadParams.setMargins(dp(8), 0, 0, 0);
        savedRow.addView(loadButton, loadParams);

        form.addView(label("Proyecto"));
        projectNameInput = input("woocommerce-agent", false);
        form.addView(inputContainer(projectNameInput));

        form.addView(label("Conector"));
        connectorSpinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{
                "WooCommerce", "WhatsApp Business", "Google Sheets", "Supabase", "WordPress", "Notion", "CRM propio", "ERP pequeno"
        });
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        connectorSpinner.setAdapter(adapter);
        connectorSpinner.setPadding(dp(10), 0, dp(10), 0);
        connectorSpinner.setBackground(bg(CARD, LINE, 6));
        form.addView(connectorSpinner, fixedHeight(50));

        form.addView(divider());
        credentialsTitle = label("Conexion");
        form.addView(credentialsTitle);
        siteUrlInput = input("https://tu-web.com", false);
        form.addView(inputContainer(siteUrlInput));
        usernameLabel = label("Usuario");
        form.addView(usernameLabel);
        usernameInput = input("admin", false);
        form.addView(inputContainer(usernameInput));
        secretLabel = label("Application password");
        form.addView(secretLabel);
        secretInput = input("xxxx xxxx xxxx xxxx xxxx xxxx", false);
        secretInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        form.addView(inputContainer(secretInput));
        connectorHelpText = helperText("Introduce credenciales de prueba si quieres validar la conexion. No se exportan secretos reales al ZIP.");
        form.addView(connectorHelpText);

        form.addView(divider());
        form.addView(label("Que quieres que haga la IA"));
        intentInput = input("Consultar productos de WooCommerce, actualizar stock y generar informes semanales.", true);
        form.addView(inputContainer(intentInput));

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.VERTICAL);
        actions.setPadding(0, dp(16), 0, 0);
        form.addView(actions);

        Button generateButton = button("Generar MCP", BLUE, colors.surface);
        Button exportButton = button("Exportar ZIP", colors.buttonLight, INK);
        Button saveButton = button("Guardar", GREEN, colors.surface);
        Button testButton = button("Probar conexion", colors.darkButton, Color.WHITE);
        actions.addView(generateButton, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(52)));

        LinearLayout actions2 = new LinearLayout(this);
        actions2.setOrientation(LinearLayout.HORIZONTAL);
        actions2.setGravity(Gravity.CENTER_VERTICAL);
        actions2.setPadding(0, dp(8), 0, 0);
        actions.addView(actions2);
        actions2.addView(exportButton, new LinearLayout.LayoutParams(0, dp(48), 1));
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(0, dp(48), 1);
        saveParams.setMargins(dp(8), 0, 0, 0);
        actions2.addView(saveButton, saveParams);

        LinearLayout actions3 = new LinearLayout(this);
        actions3.setOrientation(LinearLayout.HORIZONTAL);
        actions3.setPadding(0, dp(8), 0, 0);
        actions.addView(actions3);
        actions3.addView(testButton, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(48)));

        statusText = text("Listo para generar.", 13, MUTED, false);
        statusText.setPadding(dp(14), dp(12), dp(14), dp(12));
        statusText.setBackground(bg(colors.buttonLight, colors.statusLine, 8));
        root.addView(statusText, panelParams());

        LinearLayout previewPanel = panel();
        root.addView(previewPanel, panelParams());

        TextView previewTitle = sectionTitle("Vista previa");
        previewPanel.addView(previewTitle);
        previewStatsText = helperText("Genera un proyecto para ver archivos y tools.");
        previewStatsText.setTextColor(MUTED);
        previewStatsText.setBackground(bg(colors.buttonLight, LINE, 8));
        previewStatsText.setPadding(dp(12), dp(10), dp(12), dp(10));
        previewPanel.addView(previewStatsText, matchWrap());

        HorizontalScrollView tabScroller = new HorizontalScrollView(this);
        tabScroller.setHorizontalScrollBarEnabled(false);
        previewPanel.addView(tabScroller);

        LinearLayout previewTabs = new LinearLayout(this);
        previewTabs.setOrientation(LinearLayout.HORIZONTAL);
        previewTabs.setGravity(Gravity.CENTER_VERTICAL);
        previewTabs.setPadding(0, dp(2), 0, dp(10));
        tabScroller.addView(previewTabs);

        toolsTab = smallButton("Tools");
        varsTab = smallButton("Variables");
        codeTab = smallButton("Codigo");
        docsTab = smallButton("Docs");
        promptsTab = smallButton("Prompts");
        previewTabs.addView(toolsTab, tabParams());
        previewTabs.addView(varsTab, tabParams());
        previewTabs.addView(codeTab, tabParams());
        previewTabs.addView(docsTab, tabParams());
        previewTabs.addView(promptsTab, tabParams());

        previewText = text("", 13, INK, false);
        previewText.setTypeface(Typeface.MONOSPACE);
        previewText.setTextIsSelectable(true);
        previewText.setPadding(dp(14), dp(14), dp(14), dp(14));
        previewText.setTextColor(CODE_TEXT);
        previewText.setBackground(bg(CODE_BG, colors.codeLine, 8));
        previewPanel.addView(previewText, matchWrap());

        loadButton.setOnClickListener(v -> loadSelectedProject());
        generateButton.setOnClickListener(v -> generateProject());
        exportButton.setOnClickListener(v -> exportZip());
        saveButton.setOnClickListener(v -> saveProject());
        testButton.setOnClickListener(v -> testConnection());
        toolsTab.setOnClickListener(v -> showPreview("Tools"));
        varsTab.setOnClickListener(v -> showPreview("Variables"));
        codeTab.setOnClickListener(v -> showPreview("Codigo"));
        docsTab.setOnClickListener(v -> showPreview("Docs"));
        promptsTab.setOnClickListener(v -> showPreview("Prompts"));
        connectorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateConnectorFields();
                generateProject();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return scrollView;
    }

    private void updateConnectorFields() {
        String connector = connectorSpinner == null ? "WooCommerce" : connectorSpinner.getSelectedItem().toString();
        if (connector.equals("WordPress")) {
            credentialsTitle.setText("Conexion WordPress");
            siteUrlInput.setHint("https://tu-web.com");
            usernameLabel.setText("Usuario");
            usernameInput.setHint("admin");
            secretLabel.setText("Application password");
            secretInput.setHint("xxxx xxxx xxxx xxxx xxxx xxxx");
            connectorHelpText.setText("Usa una application password de WordPress para probar /wp-json/wp/v2/users/me.");
        } else if (connector.equals("WooCommerce")) {
            credentialsTitle.setText("Conexion WooCommerce");
            siteUrlInput.setHint("https://tu-tienda.com");
            usernameLabel.setText("Consumer key");
            usernameInput.setHint("ck_xxx");
            secretLabel.setText("Consumer secret");
            secretInput.setHint("cs_xxx");
            connectorHelpText.setText("Usa claves REST de WooCommerce con permisos de lectura para probar productos.");
        } else if (connector.equals("WhatsApp Business")) {
            credentialsTitle.setText("Conexion WhatsApp Business");
            siteUrlInput.setHint("v25.0");
            usernameLabel.setText("Phone Number ID");
            usernameInput.setHint("123456789012345");
            secretLabel.setText("Access token");
            secretInput.setHint("EAAG...");
            connectorHelpText.setText("Usa WhatsApp Business Cloud API: version de Graph, Phone Number ID y access token.");
        } else {
            credentialsTitle.setText("Conexion API");
            siteUrlInput.setHint("https://api.cliente.com");
            usernameLabel.setText("Identificador");
            usernameInput.setHint("usuario, proyecto o sheet id");
            secretLabel.setText("Token o secreto");
            secretInput.setHint("xxx");
            connectorHelpText.setText("Para APIs propias, usa la URL base y un token de prueba con permisos limitados.");
        }
    }

    private void generateProject() {
        String projectName = ProjectNameSanitizer.clean(projectNameInput == null ? "mcp-business-agent" : projectNameInput.getText().toString());
        String connector = connectorSpinner == null ? "WooCommerce" : connectorSpinner.getSelectedItem().toString();
        String intent = intentInput == null ? "" : intentInput.getText().toString().trim();
        if (intent.isEmpty()) {
            intent = "Consultar datos, actualizar registros y generar informes.";
        }

        currentProjectName = projectName;
        currentConnector = connector;
        currentIntent = intent;
        GeneratedProject project = ProjectGenerator.generate(currentInput());
        currentTools = project.tools;
        generatedFiles = project.files;
        if (previewStatsText != null) {
            previewStatsText.setText(generatedFiles.size() + " archivos preparados - " + currentTools.size() + " tools MCP - " + connector);
        }

        showPreview(activePreview);
        statusText.setText("Generados " + generatedFiles.size() + " archivos para " + connector + ".");
    }

    private ProjectInput currentInput() {
        return new ProjectInput(
                currentProjectName,
                currentConnector,
                currentIntent,
                valueOrHint(siteUrlInput, ""),
                valueOrHint(usernameInput, ""),
                valueOrHint(secretInput, "")
        );
    }

    private void showPreview(String section) {
        activePreview = section;
        refreshTabs();
        if (previewText == null) return;

        previewText.setText(PreviewRenderer.render(section, currentProjectName, currentConnector, new GeneratedProject(generatedFiles, currentTools)));
    }

    private void refreshTabs() {
        styleTab(toolsTab, activePreview.equals("Tools"));
        styleTab(varsTab, activePreview.equals("Variables"));
        styleTab(codeTab, activePreview.equals("Codigo"));
        styleTab(docsTab, activePreview.equals("Docs"));
        styleTab(promptsTab, activePreview.equals("Prompts"));
    }

    private void exportZip() {
        try {
            if (!validateProjectFields(false)) return;
            generateProject();
            File dir = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (dir == null) dir = getFilesDir();
            File zip = ZipExporter.export(dir, currentProjectName, generatedFiles);
            statusText.setText("ZIP exportado: " + zip.getAbsolutePath());
            Toast.makeText(this, "ZIP exportado", Toast.LENGTH_LONG).show();
        } catch (Exception error) {
            statusText.setText("No se pudo exportar: " + error.getMessage());
            Toast.makeText(this, "Error exportando ZIP", Toast.LENGTH_LONG).show();
        }
    }

    private void saveProject() {
        try {
            if (!validateProjectFields(false)) return;
            generateProject();
            ProjectStorage.save(preferences, currentInput());
            refreshSavedProjects();
            statusText.setText("Proyecto guardado: " + currentProjectName);
            Toast.makeText(this, "Proyecto guardado", Toast.LENGTH_SHORT).show();
        } catch (Exception error) {
            statusText.setText("No se pudo guardar: " + error.getMessage());
        }
    }

    private void loadSelectedProject() {
        try {
            if (savedProjectSpinner.getSelectedItem() == null || savedProjectSpinner.getSelectedItem().toString().equals("Sin proyectos guardados")) {
                statusText.setText("No hay proyectos guardados todavia.");
                return;
            }
            String projectName = savedProjectSpinner.getSelectedItem().toString();
            ProjectInput project = ProjectStorage.load(preferences, projectName);
            if (project == null) return;

            projectNameInput.setText(project.projectName);
            setSpinnerValue(connectorSpinner, project.connector);
            intentInput.setText(project.intent);
            siteUrlInput.setText(project.siteUrl);
            usernameInput.setText(project.username);
            secretInput.setText(project.secret);
            updateConnectorFields();
            generateProject();
            statusText.setText("Proyecto abierto: " + projectName);
        } catch (Exception error) {
            statusText.setText("No se pudo abrir: " + error.getMessage());
        }
    }

    private void refreshSavedProjects() {
        if (savedProjectAdapter == null || preferences == null) return;
        savedProjectAdapter.clear();
        for (String projectName : ProjectStorage.names(preferences)) {
            savedProjectAdapter.add(projectName);
        }
        if (savedProjectAdapter.isEmpty()) {
            savedProjectAdapter.add("Sin proyectos guardados");
        }
        savedProjectAdapter.notifyDataSetChanged();
    }

    private void testConnection() {
        if (!validateProjectFields(true)) return;
        generateProject();
        ProjectInput input = currentInput();
        statusText.setText("Probando conexion con " + input.connector + "...");
        new Thread(() -> {
            String result = ConnectionTester.test(input);
            String finalResult = result;
            runOnUiThread(() -> statusText.setText(finalResult));
        }).start();
    }

    private boolean validateProjectFields(boolean requireConnection) {
        clearInputErrors();

        ProjectInput input = new ProjectInput(
                projectNameInput.getText().toString(),
                connectorSpinner == null ? "WooCommerce" : connectorSpinner.getSelectedItem().toString(),
                intentInput == null ? "" : intentInput.getText().toString(),
                siteUrlInput.getText().toString(),
                usernameInput.getText().toString(),
                secretInput.getText().toString()
        );
        ProjectValidator.ValidationResult result = ProjectValidator.validate(input, requireConnection);
        projectNameInput.setError(result.projectNameError);
        siteUrlInput.setError(result.siteUrlError);
        usernameInput.setError(result.usernameError);
        secretInput.setError(result.secretError);
        boolean valid = result.isValid();

        if (!valid) {
            statusText.setText("Revisa los campos marcados antes de continuar.");
        }
        return valid;
    }

    private void clearInputErrors() {
        projectNameInput.setError(null);
        siteUrlInput.setError(null);
        usernameInput.setError(null);
        secretInput.setError(null);
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getAdapter().getCount(); i++) {
            if (spinner.getAdapter().getItem(i).toString().equals(value)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private String valueOrHint(EditText input, String fallback) {
        if (input == null) return fallback;
        String value = input.getText().toString().trim();
        return value.isEmpty() ? fallback : value;
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setLineSpacing(dp(2), 1.0f);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }

    private TextView label(String value) {
        TextView view = text(value, 13, MUTED, true);
        view.setPadding(0, dp(13), 0, dp(6));
        return view;
    }

    private TextView sectionTitle(String value) {
        TextView view = text(value, 19, INK, true);
        view.setPadding(0, 0, 0, dp(10));
        return view;
    }

    private TextView bodyText(String value) {
        TextView view = text(value, 14, MUTED, false);
        view.setPadding(0, 0, 0, dp(10));
        return view;
    }

    private TextView helperText(String value) {
        TextView view = text(value, 12, MUTED, false);
        view.setPadding(0, dp(8), 0, dp(2));
        return view;
    }

    private View stepItem(String number, String title, String description) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.HORIZONTAL);
        item.setGravity(Gravity.TOP);
        item.setPadding(0, 0, 0, dp(12));

        TextView badge = text(number, 13, Color.WHITE, true);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(bg(BLUE, BLUE, 999));
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(dp(30), dp(30));
        badgeParams.setMargins(0, dp(1), dp(12), 0);
        item.addView(badge, badgeParams);

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        item.addView(copy, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        copy.addView(text(title, 14, INK, true));
        TextView body = text(description, 13, MUTED, false);
        body.setPadding(0, dp(3), 0, 0);
        copy.addView(body);
        return item;
    }

    private TextView miniStep(String number, String label) {
        TextView view = text(number + "  " + label, 11, BLUE, true);
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(9), dp(5), dp(9), dp(5));
        view.setBackground(bg(colors.primarySoft, Color.TRANSPARENT, 999));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        params.setMargins(0, 0, dp(6), 0);
        view.setLayoutParams(params);
        return view;
    }

    private View useCase(String title, String description) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setPadding(dp(12), dp(10), dp(12), dp(10));
        item.setBackground(bg(colors.buttonLight, LINE, 8));

        TextView itemTitle = text(title, 14, INK, true);
        item.addView(itemTitle);

        TextView itemDescription = text(description, 13, MUTED, false);
        itemDescription.setPadding(0, dp(4), 0, 0);
        item.addView(itemDescription);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(8));
        item.setLayoutParams(params);
        return item;
    }

    private TextView stepText(String value) {
        TextView view = text(value, 14, INK, false);
        view.setPadding(0, 0, 0, dp(8));
        return view;
    }

    private TextView pill(String value, int fg, int bg) {
        TextView view = text(value, 11, fg, true);
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(10), dp(5), dp(10), dp(5));
        view.setBackground(bg(bg, Color.TRANSPARENT, 999));
        return view;
    }

    private EditText input(String hint, boolean multiLine) {
        EditText editText = new TextInputEditText(this);
        editText.setHint(hint);
        editText.setTextColor(INK);
        editText.setHintTextColor(colors.inputHint);
        editText.setTextSize(14);
        editText.setSingleLine(!multiLine);
        editText.setMinLines(multiLine ? 5 : 1);
        editText.setGravity(multiLine ? Gravity.TOP : Gravity.CENTER_VERTICAL);
        editText.setInputType(multiLine ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE : InputType.TYPE_CLASS_TEXT);
        editText.setBackgroundColor(Color.TRANSPARENT);
        editText.setPadding(dp(12), dp(9), dp(12), dp(9));
        editText.setMinHeight(dp(multiLine ? 118 : 50));
        return editText;
    }

    private TextInputLayout inputContainer(EditText editText) {
        TextInputLayout layout = new TextInputLayout(this);
        layout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);
        layout.setBoxBackgroundColor(CARD);
        layout.setBoxCornerRadii(dp(6), dp(6), dp(6), dp(6));
        layout.setBoxStrokeColor(BLUE);
        layout.setBoxStrokeWidth(dp(1));
        layout.setBoxStrokeWidthFocused(dp(2));
        layout.setHintEnabled(false);
        layout.addView(editText, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(8));
        layout.setLayoutParams(params);
        return layout;
    }

    private Button button(String value, int bg, int fg) {
        MaterialButton button = new MaterialButton(this);
        button.setText(value);
        button.setTextColor(fg);
        button.setTextSize(13);
        button.setAllCaps(false);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setMinHeight(dp(48));
        button.setPadding(dp(8), 0, dp(8), 0);
        int stroke = bg == Color.WHITE || bg == colors.buttonLight ? LINE : bg;
        button.setCornerRadius(dp(8));
        if (bg == BLUE) {
            button.setBackgroundTintList(null);
            button.setBackground(gradient(BLUE, TEAL, 8));
            button.setStrokeWidth(0);
        } else {
            button.setBackgroundTintList(ColorStateList.valueOf(bg));
            button.setStrokeColor(ColorStateList.valueOf(stroke));
            button.setStrokeWidth(dp(1));
        }
        return button;
    }

    private Button smallButton(String value) {
        Button button = button(value, colors.buttonLight, INK);
        button.setTextSize(12);
        button.setPadding(dp(12), 0, dp(12), 0);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return button;
    }

    private void styleTab(Button button, boolean active) {
        if (button == null) return;
        button.setTextColor(active ? colors.surface : MUTED);
        button.setBackground(active ? gradient(BLUE, TEAL, 8) : bg(colors.buttonLight, LINE, 8));
    }

    private LinearLayout panel() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(16), dp(16), dp(16), dp(16));
        panel.setBackground(bg(CARD, LINE, 8));
        return panel;
    }

    private LinearLayout.LayoutParams matchWrap() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(8));
        return params;
    }

    private LinearLayout.LayoutParams fixedHeight(int heightDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(heightDp));
        params.setMargins(0, 0, 0, dp(8));
        return params;
    }

    private LinearLayout.LayoutParams panelParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(12));
        return params;
    }

    private LinearLayout.LayoutParams tabParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(40));
        params.setMargins(dp(6), 0, 0, 0);
        return params;
    }

    private View divider() {
        View view = new View(this);
        view.setBackgroundColor(LINE);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
        params.setMargins(0, dp(16), 0, dp(2));
        view.setLayoutParams(params);
        return view;
    }

    private GradientDrawable bg(int fill, int stroke, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fill);
        drawable.setCornerRadius(dp(radiusDp));
        if (stroke != Color.TRANSPARENT) {
            drawable.setStroke(dp(1), stroke);
        }
        return drawable;
    }

    private GradientDrawable gradient(int start, int end, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.BL_TR, new int[]{start, end});
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private int systemBarHeight(String name) {
        int resourceId = getResources().getIdentifier(name, "dimen", "android");
        return resourceId > 0 ? getResources().getDimensionPixelSize(resourceId) : 0;
    }
}
