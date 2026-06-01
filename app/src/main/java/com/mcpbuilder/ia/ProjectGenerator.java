package com.mcpbuilder.ia;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

final class ProjectGenerator {
    private ProjectGenerator() {
    }

    static GeneratedProject generate(ProjectInput input) {
        ProjectInput safeInput = normalize(input);
        List<String> tools = inferTools(safeInput.connector, safeInput.intent);
        LinkedHashMap<String, String> files = buildFiles(safeInput, tools);
        return new GeneratedProject(files, tools);
    }

    private static ProjectInput normalize(ProjectInput input) {
        if (input == null) {
            return new ProjectInput("mcp-business-agent", "WooCommerce", "", "", "", "");
        }
        return new ProjectInput(
                valueOrFallback(input.projectName, "mcp-business-agent"),
                valueOrFallback(input.connector, "WooCommerce"),
                valueOrFallback(input.intent, ""),
                valueOrFallback(input.siteUrl, ""),
                valueOrFallback(input.username, ""),
                valueOrFallback(input.secret, "")
        );
    }

    static List<String> inferTools(String connector, String intent) {
        String selectedConnector = connector == null ? "WooCommerce" : connector;
        String lower = intent == null ? "" : intent.toLowerCase(Locale.ROOT);
        List<String> tools = new ArrayList<>();
        if (selectedConnector.equals("WooCommerce")) {
            tools.add("list_products");
            if (hasAny(lower, "stock", "inventario")) tools.add("update_stock");
            if (hasAny(lower, "pedido", "pedidos", "order", "orders")) tools.add("list_orders");
            if (hasAny(lower, "informe", "reporte", "ventas")) tools.add("generate_sales_report");
        } else if (selectedConnector.equals("WhatsApp Business")) {
            tools.add("send_whatsapp_text");
            tools.add("send_whatsapp_template");
            if (hasAny(lower, "estado", "numero", "phone", "telefono", "conexion")) tools.add("get_whatsapp_phone_number");
        } else if (selectedConnector.equals("WordPress")) {
            tools.add("search_posts");
            tools.add("get_post");
            if (hasAny(lower, "crear", "publicar", "post", "entrada")) tools.add("create_draft_post");
            if (hasAny(lower, "actualizar", "editar", "update")) tools.add("update_post");
            if (hasAny(lower, "pagina", "page")) tools.add("search_pages");
            if (hasAny(lower, "media", "imagen", "archivo")) tools.add("list_media");
            if (hasAny(lower, "usuario", "cliente", "autor")) tools.add("list_users");
        } else if (selectedConnector.equals("Elementor")) {
            tools.add("check_elementor_environment");
            tools.add("create_elementor_page");
            tools.add("create_elementor_post");
            tools.add("list_elementor_templates");
            if (hasAny(lower, "actualizar", "editar", "update")) tools.add("update_elementor_content");
            if (hasAny(lower, "media", "imagen", "archivo")) tools.add("list_media");
        } else if (selectedConnector.equals("Google Sheets")) {
            tools.add("read_rows");
            tools.add("append_row");
            if (hasAny(lower, "actualizar", "update")) tools.add("update_row");
            if (hasAny(lower, "informe", "reporte", "resumen")) tools.add("summarize_sheet");
        } else if (selectedConnector.equals("Supabase")) {
            tools.add("query_table");
            tools.add("insert_record");
            if (hasAny(lower, "actualizar", "update")) tools.add("update_record");
            if (hasAny(lower, "informe", "reporte", "query")) tools.add("run_report_query");
        } else {
            tools.add("search_records");
            tools.add("get_record");
            tools.add("update_record");
            if (hasAny(lower, "informe", "reporte")) tools.add("generate_report");
        }
        return tools;
    }

    private static boolean hasAny(String value, String... needles) {
        for (String needle : needles) {
            if (value.contains(needle)) return true;
        }
        return false;
    }

    private static LinkedHashMap<String, String> buildFiles(ProjectInput input, List<String> tools) {
        LinkedHashMap<String, String> files = new LinkedHashMap<>();
        files.put("package.json", packageJson(input.projectName));
        files.put(".gitignore", gitignore());
        files.put(".env.example", envExample(input));
        files.put("src/server.js", serverJs(input.connector, tools));
        files.put("src/client.js", clientJs(input.connector));
        if (input.connector.equals("Elementor")) {
            files.put("src/elementor-layouts.js", elementorLayoutsJs());
            files.put("wordpress/elementor-mcp-endpoint.php", elementorEndpointPhp());
            files.put("docs/elementor.md", elementorDocs());
        }
        files.put("docs/README.md", readme(input, tools));
        files.put("prompts/usage.md", prompts(input.connector, tools));
        files.put("mcp.config.example.json", configJson(input.projectName));
        return files;
    }

    private static String packageJson(String projectName) {
        return "{\n"
                + "  \"name\": \"" + projectName + "\",\n"
                + "  \"version\": \"0.1.0\",\n"
                + "  \"type\": \"module\",\n"
                + "  \"scripts\": {\n"
                + "    \"start\": \"node src/server.js\",\n"
                + "    \"check\": \"node --check src/server.js && node --check src/client.js\"\n"
                + "  },\n"
                + "  \"dependencies\": {\n"
                + "    \"@modelcontextprotocol/sdk\": \"latest\",\n"
                + "    \"dotenv\": \"latest\",\n"
                + "    \"zod\": \"latest\"\n"
                + "  }\n"
                + "}\n";
    }

    private static String gitignore() {
        return "node_modules/\n.env\n*.log\n.DS_Store\n";
    }

    private static String envExample(ProjectInput input) {
        if (input.connector.equals("WooCommerce")) {
            return "WOOCOMMERCE_URL=" + valueOrFallback(input.siteUrl, "https://tu-tienda.com") + "\n"
                    + "WOOCOMMERCE_KEY=ck_xxx\n"
                    + "WOOCOMMERCE_SECRET=cs_xxx\n";
        }
        if (input.connector.equals("WhatsApp Business")) {
            return "WHATSAPP_API_VERSION=" + valueOrFallback(input.siteUrl, "v25.0") + "\n"
                    + "WHATSAPP_PHONE_NUMBER_ID=" + valueOrFallback(input.username, "123456789012345") + "\n"
                    + "WHATSAPP_ACCESS_TOKEN=EAAG...\n";
        }
        if (input.connector.equals("WordPress") || input.connector.equals("Elementor")) {
            return "WORDPRESS_URL=" + valueOrFallback(input.siteUrl, "https://tu-web.com") + "\n"
                    + "WORDPRESS_USER=" + valueOrFallback(input.username, "admin") + "\n"
                    + "WORDPRESS_APP_PASSWORD=xxxx xxxx xxxx xxxx xxxx xxxx\n"
                    + (input.connector.equals("Elementor") ? "ELEMENTOR_REQUIRED_THEME=hello-elementor\n" : "");
        }
        if (input.connector.equals("Google Sheets")) {
            return "GOOGLE_SERVICE_ACCOUNT_JSON={}\n"
                    + "GOOGLE_SHEET_ID=" + valueOrFallback(input.username, "sheet_id") + "\n";
        }
        if (input.connector.equals("Supabase")) {
            return "SUPABASE_URL=" + valueOrFallback(input.siteUrl, "https://project.supabase.co") + "\n"
                    + "SUPABASE_SERVICE_ROLE_KEY=xxx\n";
        }
        return "API_BASE_URL=" + valueOrFallback(input.siteUrl, "https://api.cliente.com") + "\n"
                + "API_TOKEN=xxx\n";
    }

    private static String valueOrFallback(String value, String fallback) {
        String clean = value == null ? "" : value.trim();
        return clean.isEmpty() ? fallback : clean;
    }

    private static String clientJs(String connector) {
        if (connector.equals("WooCommerce")) {
            return "export async function api(path, options = {}) {\n"
                    + "  const base = requiredEnv('WOOCOMMERCE_URL');\n"
                    + "  const auth = Buffer.from(`${requiredEnv('WOOCOMMERCE_KEY')}:${requiredEnv('WOOCOMMERCE_SECRET')}`).toString('base64');\n"
                    + "  return request(`${base}/wp-json/wc/v3${path}`, auth, options, 'WooCommerce');\n"
                    + "}\n\n"
                    + sharedClientHelpers("Basic");
        }
        if (connector.equals("WordPress")) {
            return "export async function api(path, options = {}) {\n"
                    + "  const base = requiredEnv('WORDPRESS_URL');\n"
                    + "  const auth = Buffer.from(`${requiredEnv('WORDPRESS_USER')}:${requiredEnv('WORDPRESS_APP_PASSWORD')}`).toString('base64');\n"
                    + "  return request(`${base}/wp-json/wp/v2${path}`, auth, options, 'WordPress');\n"
                    + "}\n\n"
                    + sharedClientHelpers("Basic");
        }
        if (connector.equals("Elementor")) {
            return "export async function api(path, options = {}) {\n"
                    + "  const base = requiredEnv('WORDPRESS_URL');\n"
                    + "  const auth = Buffer.from(`${requiredEnv('WORDPRESS_USER')}:${requiredEnv('WORDPRESS_APP_PASSWORD')}`).toString('base64');\n"
                    + "  const route = path.startsWith('/mcp-builder/') ? `/wp-json${path}` : `/wp-json/wp/v2${path}`;\n"
                    + "  return request(`${base}${route}`, auth, options, 'WordPress Elementor');\n"
                    + "}\n\n"
                    + sharedClientHelpers("Basic");
        }
        if (connector.equals("WhatsApp Business")) {
            return "export async function api(path, options = {}) {\n"
                    + "  const version = process.env.WHATSAPP_API_VERSION || 'v25.0';\n"
                    + "  const base = `https://graph.facebook.com/${version}/${requiredEnv('WHATSAPP_PHONE_NUMBER_ID')}`;\n"
                    + "  return request(`${base}${path}`, requiredEnv('WHATSAPP_ACCESS_TOKEN'), options, 'WhatsApp Business');\n"
                    + "}\n\n"
                    + sharedClientHelpers("Bearer");
        }
        return "export async function api(path, options = {}) {\n"
                + "  return request(`${requiredEnv('API_BASE_URL')}${path}`, requiredEnv('API_TOKEN'), options, 'Business API');\n"
                + "}\n\n"
                + sharedClientHelpers("Bearer");
    }

    private static String sharedClientHelpers(String authScheme) {
        return "async function request(url, token, options, label) {\n"
                + "  const controller = new AbortController();\n"
                + "  const timeout = setTimeout(() => controller.abort(), 15000);\n"
                + "  try {\n"
                + "    const response = await fetch(url, {\n"
                + "      ...options,\n"
                + "      signal: controller.signal,\n"
                + "      headers: { Authorization: '" + authScheme + " ' + token, 'Content-Type': 'application/json', ...(options.headers || {}) }\n"
                + "    });\n"
                + "    const text = await response.text();\n"
                + "    if (!response.ok) throw new Error(`${label} API ${response.status}: ${text.slice(0, 300)}`);\n"
                + "    return text ? JSON.parse(text) : {};\n"
                + "  } finally {\n"
                + "    clearTimeout(timeout);\n"
                + "  }\n"
                + "}\n\n"
                + "function requiredEnv(name) {\n"
                + "  const value = process.env[name];\n"
                + "  if (!value) throw new Error(`Missing required env var: ${name}`);\n"
                + "  return value;\n"
                + "}\n";
    }

    private static String serverJs(String connector, List<String> tools) {
        StringBuilder js = new StringBuilder();
        js.append("import 'dotenv/config';\n");
        js.append("import { McpServer } from '@modelcontextprotocol/sdk/server/mcp.js';\n");
        js.append("import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';\n");
        js.append("import { z } from 'zod';\n");
        js.append("import { api } from './client.js';\n");
        if (connector.equals("Elementor")) {
            js.append("import { buildElementorLayout } from './elementor-layouts.js';\n");
        }
        js.append("\n");
        js.append("const server = new McpServer({ name: '").append(connector.toLowerCase(Locale.ROOT).replace(" ", "-")).append("-mcp', version: '0.1.0' });\n\n");
        for (String tool : tools) {
            js.append(toolDefinition(tool));
        }
        js.append("function asText(value) {\n");
        js.append("  return { content: [{ type: 'text', text: JSON.stringify(value, null, 2) }] };\n");
        js.append("}\n\n");
        js.append("const transport = new StdioServerTransport();\n");
        js.append("await server.connect(transport);\n");
        return js.toString();
    }

    private static String toolDefinition(String tool) {
        if (tool.equals("update_stock")) {
            return "server.tool('update_stock', { productId: z.number().int().positive(), quantity: z.number().int().min(0) }, async ({ productId, quantity }) => {\n"
                    + "  const result = await api(`/products/${productId}`, { method: 'PUT', body: JSON.stringify({ stock_quantity: quantity }) });\n"
                    + "  return asText(result);\n"
                    + "});\n\n";
        }
        if (tool.equals("list_products")) {
            return "server.tool('list_products', { search: z.string().optional(), page: z.number().int().positive().optional() }, async ({ search, page = 1 }) => {\n"
                    + "  const params = new URLSearchParams({ per_page: '20', page: String(page) });\n"
                    + "  if (search) params.set('search', search);\n"
                    + "  const result = await api(`/products?${params}`);\n"
                    + "  return asText(result);\n"
                    + "});\n\n";
        }
        if (tool.equals("list_orders")) {
            return "server.tool('list_orders', { status: z.string().optional(), page: z.number().int().positive().optional() }, async ({ status, page = 1 }) => {\n"
                    + "  const params = new URLSearchParams({ per_page: '20', page: String(page) });\n"
                    + "  if (status) params.set('status', status);\n"
                    + "  const result = await api(`/orders?${params}`);\n"
                    + "  return asText(result);\n"
                    + "});\n\n";
        }
        if (tool.equals("send_whatsapp_text")) {
            return "server.tool('send_whatsapp_text', { to: z.string().min(6), body: z.string().min(1) }, async ({ to, body }) => {\n"
                    + "  const result = await api('/messages', {\n"
                    + "    method: 'POST',\n"
                    + "    body: JSON.stringify({ messaging_product: 'whatsapp', to, type: 'text', text: { preview_url: false, body } })\n"
                    + "  });\n"
                    + "  return asText(result);\n"
                    + "});\n\n";
        }
        if (tool.equals("send_whatsapp_template")) {
            return "server.tool('send_whatsapp_template', { to: z.string().min(6), templateName: z.string().min(1), languageCode: z.string().default('es') }, async ({ to, templateName, languageCode }) => {\n"
                    + "  const result = await api('/messages', {\n"
                    + "    method: 'POST',\n"
                    + "    body: JSON.stringify({ messaging_product: 'whatsapp', to, type: 'template', template: { name: templateName, language: { code: languageCode } } })\n"
                    + "  });\n"
                    + "  return asText(result);\n"
                    + "});\n\n";
        }
        if (tool.equals("get_whatsapp_phone_number")) {
            return "server.tool('get_whatsapp_phone_number', {}, async () => {\n"
                    + "  const result = await api('?fields=display_phone_number,verified_name,quality_rating');\n"
                    + "  return asText(result);\n"
                    + "});\n\n";
        }
        if (tool.equals("search_posts")) {
            return "server.tool('search_posts', { search: z.string().optional(), status: z.string().optional(), page: z.number().int().positive().optional() }, async ({ search, status, page = 1 }) => {\n"
                    + "  const params = new URLSearchParams({ per_page: '20', page: String(page) });\n"
                    + "  if (search) params.set('search', search);\n"
                    + "  if (status) params.set('status', status);\n"
                    + "  const result = await api(`/posts?${params}`);\n"
                    + "  return asText(result);\n"
                    + "});\n\n";
        }
        if (tool.equals("get_post")) {
            return "server.tool('get_post', { postId: z.number().int().positive() }, async ({ postId }) => {\n"
                    + "  const result = await api(`/posts/${postId}`);\n"
                    + "  return asText(result);\n"
                    + "});\n\n";
        }
        if (tool.equals("create_draft_post")) {
            return "server.tool('create_draft_post', { title: z.string().min(1), content: z.string().min(1), excerpt: z.string().optional() }, async ({ title, content, excerpt }) => {\n"
                    + "  const result = await api('/posts', { method: 'POST', body: JSON.stringify({ title, content, excerpt, status: 'draft' }) });\n"
                    + "  return asText(result);\n"
                    + "});\n\n";
        }
        if (tool.equals("update_post")) {
            return "server.tool('update_post', { postId: z.number().int().positive(), title: z.string().optional(), content: z.string().optional(), status: z.string().optional() }, async ({ postId, title, content, status }) => {\n"
                    + "  const payload = Object.fromEntries(Object.entries({ title, content, status }).filter(([, value]) => value !== undefined));\n"
                    + "  const result = await api(`/posts/${postId}`, { method: 'POST', body: JSON.stringify(payload) });\n"
                    + "  return asText(result);\n"
                    + "});\n\n";
        }
        if (tool.equals("search_pages")) {
            return "server.tool('search_pages', { search: z.string().optional(), page: z.number().int().positive().optional() }, async ({ search, page = 1 }) => {\n"
                    + "  const params = new URLSearchParams({ per_page: '20', page: String(page) });\n"
                    + "  if (search) params.set('search', search);\n"
                    + "  const result = await api(`/pages?${params}`);\n"
                    + "  return asText(result);\n"
                    + "});\n\n";
        }
        if (tool.equals("list_media")) {
            return "server.tool('list_media', { search: z.string().optional(), page: z.number().int().positive().optional() }, async ({ search, page = 1 }) => {\n"
                    + "  const params = new URLSearchParams({ per_page: '20', page: String(page) });\n"
                    + "  if (search) params.set('search', search);\n"
                    + "  const result = await api(`/media?${params}`);\n"
                    + "  return asText(result);\n"
                    + "});\n\n";
        }
        if (tool.equals("list_users")) {
            return "server.tool('list_users', { search: z.string().optional(), page: z.number().int().positive().optional() }, async ({ search, page = 1 }) => {\n"
                    + "  const params = new URLSearchParams({ per_page: '20', page: String(page) });\n"
                    + "  if (search) params.set('search', search);\n"
                    + "  const result = await api(`/users?${params}`);\n"
                    + "  return asText(result);\n"
                    + "});\n\n";
        }
        if (tool.equals("check_elementor_environment")) {
            return "server.tool('check_elementor_environment', {}, async () => {\n"
                    + "  const result = await api('/mcp-builder/v1/elementor-environment');\n"
                    + "  return asText(result);\n"
                    + "});\n\n";
        }
        if (tool.equals("create_elementor_page")) {
            return "server.tool('create_elementor_page', {\n"
                    + "  title: z.string().min(1),\n"
                    + "  status: z.enum(['draft', 'publish']).default('draft'),\n"
                    + "  sections: z.array(z.object({ heading: z.string().min(1), text: z.string().min(1), ctaText: z.string().optional(), ctaUrl: z.string().url().optional() })).min(1)\n"
                    + "}, async ({ title, status, sections }) => {\n"
                    + "  const elementorData = buildElementorLayout(sections);\n"
                    + "  const result = await api('/mcp-builder/v1/elementor-content', {\n"
                    + "    method: 'POST',\n"
                    + "    body: JSON.stringify({ type: 'page', title, status, elementorData, template: 'elementor_canvas' })\n"
                    + "  });\n"
                    + "  return asText(result);\n"
                    + "});\n\n";
        }
        if (tool.equals("create_elementor_post")) {
            return "server.tool('create_elementor_post', {\n"
                    + "  title: z.string().min(1),\n"
                    + "  excerpt: z.string().optional(),\n"
                    + "  status: z.enum(['draft', 'publish']).default('draft'),\n"
                    + "  sections: z.array(z.object({ heading: z.string().min(1), text: z.string().min(1), ctaText: z.string().optional(), ctaUrl: z.string().url().optional() })).min(1)\n"
                    + "}, async ({ title, excerpt, status, sections }) => {\n"
                    + "  const elementorData = buildElementorLayout(sections);\n"
                    + "  const result = await api('/mcp-builder/v1/elementor-content', {\n"
                    + "    method: 'POST',\n"
                    + "    body: JSON.stringify({ type: 'post', title, excerpt, status, elementorData })\n"
                    + "  });\n"
                    + "  return asText(result);\n"
                    + "});\n\n";
        }
        if (tool.equals("list_elementor_templates")) {
            return "server.tool('list_elementor_templates', { search: z.string().optional(), page: z.number().int().positive().optional() }, async ({ search, page = 1 }) => {\n"
                    + "  const params = new URLSearchParams({ per_page: '20', page: String(page), status: 'publish' });\n"
                    + "  if (search) params.set('search', search);\n"
                    + "  const result = await api(`/elementor_library?${params}`);\n"
                    + "  return asText(result);\n"
                    + "});\n\n";
        }
        if (tool.equals("update_elementor_content")) {
            return "server.tool('update_elementor_content', {\n"
                    + "  postId: z.number().int().positive(),\n"
                    + "  title: z.string().optional(),\n"
                    + "  status: z.enum(['draft', 'publish', 'private']).optional(),\n"
                    + "  sections: z.array(z.object({ heading: z.string().min(1), text: z.string().min(1), ctaText: z.string().optional(), ctaUrl: z.string().url().optional() })).optional()\n"
                    + "}, async ({ postId, title, status, sections }) => {\n"
                    + "  const payload = { postId, title, status };\n"
                    + "  if (sections) payload.elementorData = buildElementorLayout(sections);\n"
                    + "  const result = await api('/mcp-builder/v1/elementor-content', { method: 'PUT', body: JSON.stringify(payload) });\n"
                    + "  return asText(result);\n"
                    + "});\n\n";
        }
        return "server.tool('" + tool + "', { query: z.string().optional() }, async ({ query }) => {\n"
                + "  const result = { tool: '" + tool + "', query, nextStep: 'Implementa aqui la llamada concreta a la API del cliente.' };\n"
                + "  return asText(result);\n"
                + "});\n\n";
    }

    private static String elementorLayoutsJs() {
        return "import crypto from 'node:crypto';\n\n"
                + "export function buildElementorLayout(sections) {\n"
                + "  return sections.map((section) => ({\n"
                + "    id: id(),\n"
                + "    elType: 'container',\n"
                + "    settings: { content_width: 'boxed', gap: 'default', padding: { unit: 'px', top: '72', right: '24', bottom: '72', left: '24', isLinked: false } },\n"
                + "    elements: [\n"
                + "      widget('heading', { title: section.heading, header_size: 'h2', align: 'center' }),\n"
                + "      widget('text-editor', { editor: `<p>${escapeHtml(section.text)}</p>`, align: 'center' }),\n"
                + "      ...(section.ctaText ? [widget('button', { text: section.ctaText, link: { url: section.ctaUrl || '#' }, align: 'center', button_type: 'info' })] : [])\n"
                + "    ]\n"
                + "  }));\n"
                + "}\n\n"
                + "function widget(widgetType, settings) {\n"
                + "  return { id: id(), elType: 'widget', widgetType, settings, elements: [] };\n"
                + "}\n\n"
                + "function id() {\n"
                + "  return crypto.randomBytes(4).toString('hex');\n"
                + "}\n\n"
                + "function escapeHtml(value) {\n"
                + "  return String(value).replace(/[&<>\\\"']/g, (char) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '\\\"': '&quot;', \"'\": '&#39;' }[char]));\n"
                + "}\n";
    }

    private static String elementorEndpointPhp() {
        return "<?php\n"
                + "/**\n"
                + " * Plugin Name: MCP Builder Elementor Endpoint\n"
                + " * Description: Endpoint REST para crear paginas y entradas Elementor desde un servidor MCP.\n"
                + " */\n\n"
                + "add_action('rest_api_init', function () {\n"
                + "    register_rest_route('mcp-builder/v1', '/elementor-environment', [\n"
                + "        'methods' => 'GET',\n"
                + "        'permission_callback' => function () { return current_user_can('edit_posts'); },\n"
                + "        'callback' => function () {\n"
                + "            $theme = wp_get_theme();\n"
                + "            return [\n"
                + "                'elementor_active' => did_action('elementor/loaded') > 0,\n"
                + "                'theme' => $theme->get_stylesheet(),\n"
                + "                'hello_elementor_recommended' => $theme->get_stylesheet() === 'hello-elementor',\n"
                + "            ];\n"
                + "        },\n"
                + "    ]);\n\n"
                + "    register_rest_route('mcp-builder/v1', '/elementor-content', [\n"
                + "        'methods' => ['POST', 'PUT'],\n"
                + "        'permission_callback' => function () { return current_user_can('edit_pages'); },\n"
                + "        'callback' => 'mcp_builder_save_elementor_content',\n"
                + "    ]);\n"
                + "});\n\n"
                + "function mcp_builder_save_elementor_content(WP_REST_Request $request) {\n"
                + "    if (!did_action('elementor/loaded')) {\n"
                + "        return new WP_Error('elementor_missing', 'Elementor debe estar activo.', ['status' => 400]);\n"
                + "    }\n"
                + "    $data = $request->get_json_params();\n"
                + "    $post_id = isset($data['postId']) ? absint($data['postId']) : 0;\n"
                + "    $postarr = [\n"
                + "        'ID' => $post_id,\n"
                + "        'post_type' => sanitize_key($data['type'] ?? 'page'),\n"
                + "        'post_title' => sanitize_text_field($data['title'] ?? 'Nueva pagina Elementor'),\n"
                + "        'post_status' => sanitize_key($data['status'] ?? 'draft'),\n"
                + "        'post_excerpt' => sanitize_text_field($data['excerpt'] ?? ''),\n"
                + "    ];\n"
                + "    $post_id = $post_id ? wp_update_post($postarr, true) : wp_insert_post($postarr, true);\n"
                + "    if (is_wp_error($post_id)) return $post_id;\n"
                + "    if (!empty($data['template'])) update_post_meta($post_id, '_wp_page_template', sanitize_key($data['template']));\n"
                + "    if (isset($data['elementorData'])) {\n"
                + "        update_post_meta($post_id, '_elementor_edit_mode', 'builder');\n"
                + "        update_post_meta($post_id, '_elementor_template_type', $postarr['post_type'] === 'page' ? 'wp-page' : 'wp-post');\n"
                + "        update_post_meta($post_id, '_elementor_version', defined('ELEMENTOR_VERSION') ? ELEMENTOR_VERSION : 'latest');\n"
                + "        update_post_meta($post_id, '_elementor_data', wp_slash(wp_json_encode($data['elementorData'])));\n"
                + "    }\n"
                + "    return ['id' => $post_id, 'edit_url' => admin_url('post.php?post=' . $post_id . '&action=elementor')];\n"
                + "}\n";
    }

    private static String elementorDocs() {
        return "# Elementor\n\n"
                + "## Requisitos recomendados\n"
                + "- WordPress con Elementor activo.\n"
                + "- Tema `Hello Elementor` activo para trabajar sobre un lienzo limpio y compatible.\n"
                + "- Usuario con permisos para editar paginas y entradas.\n"
                + "- Application Password de WordPress para autenticar el servidor MCP.\n\n"
                + "## Endpoint incluido\n"
                + "Copia `wordpress/elementor-mcp-endpoint.php` en `wp-content/plugins/mcp-builder-elementor-endpoint/mcp-builder-elementor-endpoint.php` y activalo desde WordPress.\n\n"
                + "Este endpoint guarda las claves meta de Elementor desde WordPress, evitando depender de que `_elementor_data` este expuesto directamente en REST.\n\n"
                + "## Widgets iniciales\n"
                + "El layout generado usa containers, `heading`, `text-editor` y `button`. Puedes ampliar `src/elementor-layouts.js` con mas widgets como image, icon-list, form o shortcode.\n";
    }

    private static String readme(ProjectInput input, List<String> tools) {
        String elementorNote = input.connector.equals("Elementor")
                ? "\n## Elementor y tema\n"
                + "Este conector asume Elementor activo y recomienda usar el tema `Hello Elementor` para que las paginas generadas partan de un lienzo limpio. Instala el endpoint de `wordpress/` antes de crear contenido Elementor.\n\n"
                : "";
        return "# " + input.projectName + "\n\n"
                + "Conector MCP generado para " + input.connector + ".\n\n"
                + "## Objetivo\n" + input.intent + "\n\n"
                + "## Instalacion\n"
                + "```bash\nnpm install\ncp .env.example .env\nnpm run check\nnpm start\n```\n\n"
                + elementorNote
                + "## Seguridad\n"
                + "- No subas el archivo `.env` al repositorio.\n"
                + "- Revisa manualmente las tools que modifican datos antes de usarlas con clientes reales.\n\n"
                + "## Tools\n" + joinTools(tools) + "\n";
    }

    private static String prompts(String connector, List<String> tools) {
        return "# Prompts de uso\n\n"
                + "- Usa el conector " + connector + " para responder solo con datos disponibles en las tools.\n"
                + "- Antes de modificar datos, resume el cambio y pide confirmacion cuando afecte stock, pedidos, clientes o contenido publicado.\n"
                + "- En WordPress, crea borradores antes de publicar salvo que el usuario pida publicacion explicitamente.\n"
                + "- Para informes, combina las tools disponibles: " + String.join(", ", tools) + ".\n";
    }

    private static String configJson(String projectName) {
        return "{\n"
                + "  \"mcpServers\": {\n"
                + "    \"" + projectName + "\": {\n"
                + "      \"command\": \"node\",\n"
                + "      \"args\": [\"src/server.js\"],\n"
                + "      \"envFile\": \".env\"\n"
                + "    }\n"
                + "  }\n"
                + "}\n";
    }

    private static String joinTools(List<String> tools) {
        StringBuilder builder = new StringBuilder();
        for (String tool : tools) builder.append("- `").append(tool).append("`\n");
        return builder.toString();
    }
}
