<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    if (request.getAttribute("config") == null) {
        response.sendRedirect(request.getContextPath() + "/admin/settings");
        return;
    }
    boolean saved = Boolean.TRUE.equals(request.getAttribute("saved"));
    String aiProvider = (String) request.getAttribute("aiProvider");
    String aiModel    = (String) request.getAttribute("aiModel");
    String aiBaseUrl  = (String) request.getAttribute("aiBaseUrl");
    String maskedKey  = (String) request.getAttribute("maskedApiKey");
    boolean hasApiKey = Boolean.TRUE.equals(request.getAttribute("hasApiKey"));
    if (aiProvider == null) aiProvider = "anthropic";
    if (aiModel    == null) aiModel    = "claude-haiku-4-5-20251001";
    if (aiBaseUrl  == null) aiBaseUrl  = "https://api.openai.com/v1";
    if (maskedKey  == null) maskedKey  = "";
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Settings - TA Recruitment System</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .settings-layout { display: grid; grid-template-columns: 220px 1fr; gap: 24px; }
        .settings-nav { background: var(--bg-white); border: 1px solid var(--border-solid); border-radius: var(--radius); padding: 8px; }
        .settings-nav a {
            display: flex; align-items: center; gap: 10px; padding: 10px 14px; border-radius: 8px;
            font-size: 14px; color: var(--text-secondary); text-decoration: none; transition: all 0.15s;
        }
        .settings-nav a:hover { background: var(--bg); color: var(--text-dark); text-decoration: none; }
        .settings-nav a.active { background: var(--primary-light); color: var(--primary); font-weight: 600; }
        .settings-nav a svg { width: 18px; height: 18px; flex-shrink: 0; }
        .settings-panel { display: flex; flex-direction: column; gap: 24px; }
        .settings-card { background: var(--bg-white); border: 1px solid var(--border-solid); border-radius: var(--radius); padding: 28px; }
        .settings-card h3 { font-size: 16px; font-weight: 700; color: var(--text-dark); margin-bottom: 20px; }
        .setting-row { display: grid; grid-template-columns: 200px 1fr; align-items: center; margin-bottom: 16px; }
        .setting-label { font-size: 14px; font-weight: 500; color: var(--text-dark); }
        .setting-input { width: 100%; padding: 10px 14px; border: 1px solid var(--border-solid); border-radius: 8px; font-size: 14px; color: var(--text-dark); background: #f9fafb; outline: none; }
        .setting-input:focus { border-color: var(--primary); background: var(--bg-white); }
        .notif-item { display: flex; align-items: center; gap: 12px; padding: 10px 0; }
        .notif-item label { font-size: 14px; color: var(--text-dark); cursor: pointer; flex: 1; }
        .toggle { position: relative; width: 36px; height: 20px; }
        .toggle input { opacity: 0; width: 0; height: 0; }
        .toggle .slider { position: absolute; top: 0; left: 0; right: 0; bottom: 0; background: #d1d5db; border-radius: 10px; cursor: pointer; transition: 0.2s; }
        .toggle .slider:before { content: ''; position: absolute; height: 16px; width: 16px; left: 2px; bottom: 2px; background: #fff; border-radius: 50%; transition: 0.2s; }
        .toggle input:checked + .slider { background: var(--primary); }
        .toggle input:checked + .slider:before { transform: translateX(16px); }
    </style>
</head>
<body>
<%@ include file="/jsp/common/header.jsp" %>

    <div class="page-header" style="align-items:center;">
        <div>
            <h1>Settings</h1>
            <p>Configure system preferences and policies</p>
        </div>
        <% if (saved) { %>
        <span style="color:#16a34a;font-size:13px;font-weight:600;">&#10003; Saved</span>
        <% } %>
    </div>

    <div class="settings-layout">
        <!-- Left Nav -->
        <div class="settings-nav">
            <a href="#" class="active" onclick="showTab('general')">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="2" y1="12" x2="22" y2="12"/><path d="M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z"/></svg>
                General
            </a>
            <a href="#" onclick="showTab('security')">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="11" width="18" height="11" rx="2" ry="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/></svg>
                Security
            </a>
            <a href="#" onclick="showTab('notifications')">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
                Notifications
            </a>
            <a href="#" onclick="showTab('data')">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="2" width="20" height="8" rx="2" ry="2"/><rect x="2" y="14" width="20" height="8" rx="2" ry="2"/><line x1="6" y1="6" x2="6.01" y2="6"/><line x1="6" y1="18" x2="6.01" y2="18"/></svg>
                Data & Privacy
            </a>
        </div>

        <!-- Right Panel -->
        <div class="settings-panel">
            <!-- General Settings -->
            <div id="tab-general" class="settings-card">
                <h3>General Settings</h3>
                <div class="setting-row">
                    <div class="setting-label">System Name</div>
                    <input type="text" class="setting-input" value="TA Recruitment System">
                </div>
                <div class="setting-row">
                    <div class="setting-label">University Name</div>
                    <input type="text" class="setting-input" value="BUPT International School">
                </div>
                <div class="setting-row">
                    <div class="setting-label">Academic Year</div>
                    <select class="setting-input">
                        <option selected>2025-2026</option>
                        <option>2026-2027</option>
                    </select>
                </div>
                <div class="setting-row">
                    <div class="setting-label">Default Recruitment Period</div>
                    <div style="display:flex;gap:12px;">
                        <div>
                            <div style="font-size:12px;color:var(--text-muted);margin-bottom:4px;">Start Date</div>
                            <input type="date" class="setting-input" value="2026-01-15">
                        </div>
                        <div>
                            <div style="font-size:12px;color:var(--text-muted);margin-bottom:4px;">End Date</div>
                            <input type="date" class="setting-input" value="2026-04-30">
                        </div>
                    </div>
                </div>
            </div>

            <!-- AI Configuration -->
            <div id="tab-ai" class="settings-card">
                <h3>AI Configuration</h3>
                <p style="font-size:13px;color:var(--text-muted);margin-bottom:20px;">
                    Configure the AI API used for workload balancing recommendations on the
                    <a href="<%= request.getContextPath() %>/admin/ai-workload">AI Workload</a> page.
                    Supports Anthropic Claude and any OpenAI-compatible provider (OpenAI, DeepSeek, Gemini, Groq, etc.).
                </p>
                <form method="post" action="<%= request.getContextPath() %>/admin/settings">
                    <div class="setting-row" style="margin-bottom:16px;">
                        <div class="setting-label">Provider</div>
                        <select class="setting-input" name="ai.provider" id="providerSelect" onchange="toggleBaseUrl()">
                            <option value="anthropic"  <%= "anthropic".equals(aiProvider)  ? "selected" : "" %>>Anthropic Claude</option>
                            <option value="openai"     <%= "openai".equals(aiProvider)     ? "selected" : "" %>>OpenAI-compatible (OpenAI / DeepSeek / Gemini / Groq…)</option>
                        </select>
                    </div>
                    <div class="setting-row" style="margin-bottom:16px;" id="baseUrlRow">
                        <div class="setting-label">Base URL</div>
                        <input type="text" class="setting-input" name="ai.base.url"
                               value="<%= aiBaseUrl %>"
                               placeholder="https://api.openai.com/v1">
                    </div>
                    <div class="setting-row" style="margin-bottom:16px;">
                        <div class="setting-label">Model</div>
                        <input type="text" class="setting-input" name="ai.model"
                               value="<%= aiModel %>"
                               placeholder="claude-haiku-4-5-20251001 or gpt-4o-mini">
                    </div>
                    <div class="setting-row" style="margin-bottom:8px;">
                        <div class="setting-label">API Key</div>
                        <input type="password" class="setting-input" name="ai.api.key"
                               placeholder="<%= hasApiKey ? "Leave blank to keep current key" : "Paste your API key here" %>"
                               autocomplete="off">
                    </div>
                    <% if (hasApiKey) { %>
                    <div class="setting-row" style="margin-bottom:16px;">
                        <div class="setting-label"></div>
                        <div style="font-size:12px;color:var(--text-muted);">
                            Current key: <code><%= maskedKey %></code>
                        </div>
                    </div>
                    <% } %>
                    <div class="setting-row" style="margin-top:8px;">
                        <div class="setting-label"></div>
                        <button type="submit" class="btn btn-primary">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/><polyline points="17 21 17 13 7 13 7 21"/></svg>
                            Save AI Settings
                        </button>
                    </div>
                </form>
            </div>

            <!-- Notification Preferences -->
            <div id="tab-notifications" class="settings-card">
                <h3>Notification Preferences</h3>
                <div class="notif-item">
                    <label class="toggle"><input type="checkbox" checked><span class="slider"></span></label>
                    <label>Email notifications for new applications</label>
                </div>
                <div class="notif-item">
                    <label class="toggle"><input type="checkbox" checked><span class="slider"></span></label>
                    <label>Weekly recruitment summary reports</label>
                </div>
                <div class="notif-item">
                    <label class="toggle"><input type="checkbox"><span class="slider"></span></label>
                    <label>AI workload alerts</label>
                </div>
                <div class="notif-item">
                    <label class="toggle"><input type="checkbox" checked><span class="slider"></span></label>
                    <label>System maintenance notifications</label>
                </div>
            </div>
        </div>
    </div>

<%@ include file="/jsp/common/footer.jsp" %>

<script>
function showTab(name) {
    document.querySelectorAll('.settings-nav a').forEach(function(a) { a.classList.remove('active'); });
    event.target.closest('a').classList.add('active');
}
function toggleBaseUrl() {
    var sel = document.getElementById('providerSelect');
    var row = document.getElementById('baseUrlRow');
    if (sel) row.style.display = sel.value === 'openai' ? 'grid' : 'none';
}
document.addEventListener('DOMContentLoaded', toggleBaseUrl);
</script>
</body>
</html>
