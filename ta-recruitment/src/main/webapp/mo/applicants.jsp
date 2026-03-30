<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.ta.dao.*, com.ta.model.*, com.ta.util.SessionUtil, java.util.*" %>
<%!
    String displayStatusLabel(String status) {
        if (status == null) return "Pending";
        String u = status.toUpperCase();
        if ("SUBMITTED".equals(u) || "PENDING".equals(u)) return "Pending";
        if ("UNDER_REVIEW".equals(u)) return "Under Review";
        if ("INTERVIEWED".equals(u)) return "Interviewed";
        if ("OFFERED".equals(u)) return "Offered";
        if ("ACCEPTED".equals(u)) return "Accepted";
        if ("REJECTED".equals(u)) return "Rejected";
        if ("WITHDRAWN".equals(u)) return "Withdrawn";
        return status.replace('_', ' ');
    }
    String badgeKeySt(String status) {
        if (status == null) return "pending";
        return status.toLowerCase();
    }
    String moduleCode(Job j) {
        if (j == null) return "";
        String n = j.getModuleName();
        if (n == null || n.isEmpty()) return j.getJobId();
        int sp = n.indexOf(' ');
        return sp > 0 ? n.substring(0, sp) : n;
    }
    String skillsOf(TAProfile p) {
        if (p == null) return "";
        try {
            java.lang.reflect.Method m = p.getClass().getMethod("getSkills");
            Object o = m.invoke(p);
            return o != null ? o.toString() : "";
        } catch (Exception e) { return ""; }
    }
    String gpaOf(TAProfile p) {
        if (p == null) return "";
        try {
            java.lang.reflect.Method m = p.getClass().getMethod("getGpa");
            Object o = m.invoke(p);
            return o != null ? String.valueOf(o) : "";
        } catch (Exception e) { return ""; }
    }
    String academicYearOf(TAProfile p) {
        if (p == null) return "";
        try {
            java.lang.reflect.Method m = p.getClass().getMethod("getAcademicYear");
            Object o = m.invoke(p);
            return o != null ? o.toString() : "";
        } catch (Exception e) { return ""; }
    }
    static class AppByDateDescComparator implements Comparator<Application> {
        public int compare(Application a1, Application a2) {
            String d1 = a1.getAppliedDate() != null ? a1.getAppliedDate() : "";
            String d2 = a2.getAppliedDate() != null ? a2.getAppliedDate() : "";
            return d2.compareTo(d1);
        }
    }
    String initialsFromName(String name) {
        if (name == null || name.trim().isEmpty()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length >= 2) {
            return ("" + Character.toUpperCase(parts[0].charAt(0)) + Character.toUpperCase(parts[parts.length - 1].charAt(0)));
        }
        return name.length() >= 2 ? name.substring(0, 2).toUpperCase() : name.toUpperCase();
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Applicants &amp; Review - TA Recruitment</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/applicants-review.css">
</head>
<body>
<%
    User mo = SessionUtil.getCurrentUser(request);
    String dataDir = SessionUtil.getDataDir(request);
    String filterJobId = request.getParameter("jobId");

    JobDAO jobDAO = new JobDAO(dataDir);
    ApplicationDAO appDAO = new ApplicationDAO(dataDir);
    TAProfileDAO profileDAO = new TAProfileDAO(dataDir);
    UserDAO userDAO = new UserDAO(dataDir);

    List<Job> myJobs = jobDAO.findByMo(mo.getUserId());
    Set<String> myJobIds = new HashSet<String>();
    for (Job j : myJobs) myJobIds.add(j.getJobId());

    List<Application> apps = new ArrayList<Application>();
    for (Application a : appDAO.findAll()) {
        if (!myJobIds.contains(a.getJobId())) continue;
        if (filterJobId != null && !filterJobId.isEmpty() && !filterJobId.equals(a.getJobId())) continue;
        apps.add(a);
    }
    Collections.sort(apps, new AppByDateDescComparator());

    List<Application> allAppsMo = new ArrayList<Application>();
    for (Application a : appDAO.findAll()) {
        if (myJobIds.contains(a.getJobId())) allAppsMo.add(a);
    }

    int totalApplicants = allAppsMo.size();
    int confirmedHires = 0;
    for (Application a : allAppsMo) {
        String s = a.getStatus();
        if ("ACCEPTED".equalsIgnoreCase(s)) confirmedHires++;
    }

    int remainingVacancies = 0;
    for (Job j : myJobs) {
        int accJob = 0;
        for (Application x : appDAO.findByJob(j.getJobId())) {
            if ("ACCEPTED".equalsIgnoreCase(x.getStatus())) accJob++;
        }
        remainingVacancies += Math.max(0, j.getVacancies() - accJob);
    }

    Set<String> distinctProgrammes = new TreeSet<String>();
    Set<String> distinctYears = new TreeSet<String>();
    Set<String> distinctSkillTags = new TreeSet<String>();
    for (Application a : apps) {
        TAProfile pr = profileDAO.findByUserId(a.getTaUserId());
        if (pr != null && pr.getProgramme() != null && !pr.getProgramme().trim().isEmpty()) {
            distinctProgrammes.add(pr.getProgramme().trim());
        }
        if (pr != null && pr.getYearOfStudy() != null && !pr.getYearOfStudy().trim().isEmpty()) {
            distinctYears.add(pr.getYearOfStudy().trim());
        }
        String sk = skillsOf(pr);
        if (sk != null && !sk.isEmpty()) {
            for (String part : sk.split(";")) {
                String t = part.trim();
                if (!t.isEmpty()) distinctSkillTags.add(t);
            }
        }
    }
    String[] defaultSkillChips = new String[] { "Python", "Java", "Algorithms", "Data Structures", "Teaching Experience", "Statistics" };
    if (distinctSkillTags.isEmpty()) {
        for (String s : defaultSkillChips) distinctSkillTags.add(s);
    }
%>
<%@ include file="/jsp/common/header.jsp" %>

    <section class="mo-ar-page ta-apps-page">
        <div class="mo-ar-inner">
            <header class="mo-ar-hero">
                <h1 class="mo-ar-title">Applicants &amp; Review</h1>
                <p class="mo-ar-lead">Review, manage, and update application statuses across all TA positions<% if (filterJobId != null && !filterJobId.isEmpty()) { %> <a class="mo-ar-inline-link" href="<%= ctx %>/mo/applicants">Show all positions</a><% } %></p>
            </header>

            <% if ("updated".equals(request.getParameter("success")) || "batch".equals(request.getParameter("success"))) { %>
                <div class="mo-ar-alert-success">Changes saved successfully!</div>
            <% } %>

            <div class="mo-ar-metrics">
                <div class="mo-ar-metric">
                    <div class="mo-ar-metric-top">
                        <span class="mo-ar-metric-label">Total Applicants</span>
                        <div class="mo-ar-metric-icon mo-ar-metric-icon--users" aria-hidden="true">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
                        </div>
                    </div>
                    <div class="mo-ar-metric-value"><%= totalApplicants %></div>
                    <p class="mo-ar-metric-sub">Applications received</p>
                </div>
                <div class="mo-ar-metric">
                    <div class="mo-ar-metric-top">
                        <span class="mo-ar-metric-label">Confirmed Hires</span>
                        <div class="mo-ar-metric-icon mo-ar-metric-icon--hire" aria-hidden="true">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M16 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="8.5" cy="7" r="4"/><polyline points="17 11 19 13 23 9"/></svg>
                        </div>
                    </div>
                    <div class="mo-ar-metric-value"><%= confirmedHires %></div>
                    <p class="mo-ar-metric-sub">TAs confirmed</p>
                </div>
                <div class="mo-ar-metric">
                    <div class="mo-ar-metric-top">
                        <span class="mo-ar-metric-label">Remaining Vacancies</span>
                        <div class="mo-ar-metric-icon mo-ar-metric-icon--alert" aria-hidden="true">
                            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>
                        </div>
                    </div>
                    <div class="mo-ar-metric-value"><%= remainingVacancies %></div>
                    <p class="mo-ar-metric-sub">Positions remaining</p>
                </div>
            </div>

            <% if (apps.isEmpty()) { %>
                <div class="mo-ar-empty">No applications yet. <a href="<%= ctx %>/mo/jobs.jsp">Job Postings</a></div>
            <% } else { %>

            <div class="mo-ar-search-row">
                <div class="mo-ar-search-wrap">
                    <svg class="mo-ar-search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg>
                    <input type="search" id="applicantSearch" class="mo-ar-search-input" placeholder="Search by name or student ID..." autocomplete="off">
                </div>
                <div class="mo-ar-count-pill">
                    <span class="mo-ar-count-label">Showing:</span>
                    <span class="mo-ar-count-num" id="filteredCountPill"><%= apps.size() %></span>
                    <span class="mo-ar-count-of">of <%= totalApplicants %></span>
                </div>
            </div>

            <div class="mo-ar-filters-card">
                <div class="mo-ar-filters-head">
                    <h2 class="mo-ar-filters-title">Filters</h2>
                    <button type="button" class="mo-ar-link-btn" id="clearAllFilters">Clear All</button>
                </div>
                <div class="mo-ar-filters-grid">
                    <div>
                        <label class="mo-ar-field-label" for="filterProgramme">Programme</label>
                        <select id="filterProgramme" class="mo-ar-select">
                            <option value="all">All Programmes</option>
                            <% for (String prog : distinctProgrammes) { %>
                            <option value="<%= prog.replace("\"", "&quot;") %>"><%= prog %></option>
                            <% } %>
                        </select>
                    </div>
                    <div>
                        <label class="mo-ar-field-label" for="filterYearStudy">Year of Study</label>
                        <select id="filterYearStudy" class="mo-ar-select">
                            <option value="all">All Years</option>
                            <% for (String y : distinctYears) { %>
                            <option value="<%= y.replace("\"", "&quot;") %>"><%= y %></option>
                            <% } %>
                        </select>
                    </div>
                    <div>
                        <label class="mo-ar-field-label" for="filterStatusReact">Application Status</label>
                        <select id="filterStatusReact" class="mo-ar-select">
                            <option value="all">All Statuses</option>
                            <option value="under-review">Under Review</option>
                            <option value="accepted">Accepted</option>
                            <option value="rejected">Rejected</option>
                        </select>
                    </div>
                </div>
                <div class="mo-ar-skills-block">
                    <span class="mo-ar-field-label mo-ar-skills-label">Filter by Skills</span>
                    <div class="mo-ar-skill-chips" id="skillChips">
                        <% for (String tag : distinctSkillTags) { %>
                        <button type="button" class="mo-ar-skill-chip" data-skill="<%= tag.replace("\"", "&quot;") %>"><%= tag %></button>
                        <% } %>
                    </div>
                </div>
            </div>

            <div class="mo-ar-table-card">
                <div class="mo-ar-table-head">
                    <h2 class="mo-ar-table-title">Applicant List</h2>
                    <div class="mo-ar-table-actions">
                        <button type="button" class="mo-ar-btn-ghost" id="exportListBtn">Export List</button>
                        <button type="button" class="mo-ar-btn-ghost" id="bulkActionsHeaderBtn" hidden>Bulk Actions (<span id="bulkHeaderCount">0</span>)</button>
                    </div>
                </div>

                <div class="mo-ar-bulk-bar" id="moBulkBar">
                    <label class="mo-ar-bulk-check"><input type="checkbox" id="selectAllBulk"> <span>Select All</span></label>
                    <select id="bulkStatus" class="mo-ar-select mo-ar-select--sm">
                        <option value="">Update Status</option>
                        <option value="PENDING">Pending</option>
                        <option value="UNDER_REVIEW">Under Review</option>
                        <option value="INTERVIEWED">Interviewed</option>
                        <option value="OFFERED">Offered</option>
                        <option value="ACCEPTED">Accepted</option>
                        <option value="REJECTED">Rejected</option>
                        <option value="WITHDRAWN">Withdrawn</option>
                    </select>
                    <button type="button" class="mo-ar-btn-ghost" id="applyBulkStatus">Apply</button>
                    <button type="button" class="mo-ar-btn-ghost" id="exportSelectedBtn">Export Selected</button>
                </div>

                <form id="batchForm" action="<%= ctx %>/mo/batch-update-applications" method="post" style="display:none;">
                    <input type="hidden" name="batchPayload" id="batchPayload" value="">
                    <% if (filterJobId != null && !filterJobId.isEmpty()) { %>
                    <input type="hidden" name="jobId" value="<%= filterJobId %>">
                    <% } %>
                </form>

                <div class="table-wrap mo-ar-table-wrap">
                    <table class="data-table mo-ar-table ta-apps-table" id="applicantsTable">
                        <thead>
                            <tr>
                                <th class="th-check mo-ar-th-check"><input type="checkbox" id="selectAll" title="Select all"></th>
                                <th>Name</th>
                                <th>Student ID</th>
                                <th>Programme</th>
                                <th>Year</th>
                                <th>Applied</th>
                                <th class="mo-ar-th-center">CV</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody id="applicantsTbody">
                            <% for (Application app : apps) {
                                TAProfile p = profileDAO.findByUserId(app.getTaUserId());
                                User tu = userDAO.findById(app.getTaUserId());
                                Job job = jobDAO.findById(app.getJobId());
                                String name = p != null && p.getFullName() != null ? p.getFullName() : app.getTaUserId();
                                String email = tu != null && tu.getEmail() != null ? tu.getEmail() : "";
                                String sid = p != null && p.getStudentId() != null ? p.getStudentId() : "-";
                                String mod = moduleCode(job);
                                String progDisp = p != null && p.getProgramme() != null && !p.getProgramme().isEmpty() ? p.getProgramme() : "—";
                                String yearDisp = p != null && p.getYearOfStudy() != null && !p.getYearOfStudy().isEmpty() ? p.getYearOfStudy() : "—";
                                String gpa = gpaOf(p);
                                String ay = academicYearOf(p);
                                String st = app.getStatus() != null ? app.getStatus() : "";
                                String skillsRaw = skillsOf(p);
                                String skillsLower = skillsRaw != null ? skillsRaw.toLowerCase(Locale.ENGLISH) : "";
                                String search = (name + " " + email + " " + sid + " " + mod + " " + progDisp).toLowerCase(Locale.ENGLISH);
                                String badgeClass = badgeKeySt(st);
                                boolean hasCv = p != null && p.getCvFilePath() != null && !p.getCvFilePath().isEmpty();
                                String avatarInitials = initialsFromName(name);
                            %>
                            <tr class="app-row"
                                data-application-id="<%= app.getApplicationId() %>"
                                data-job-id="<%= app.getJobId() %>"
                                data-ta-id="<%= app.getTaUserId() %>"
                                data-job-name="<%= job != null && job.getModuleName() != null ? job.getModuleName().replace("\"", "&quot;") : "" %>"
                                data-name="<%= name.replace("\"", "&quot;") %>"
                                data-email="<%= email.replace("\"", "&quot;") %>"
                                data-phone="<%= p != null && p.getPhone() != null ? p.getPhone().replace("\"", "&quot;") : "" %>"
                                data-student-id="<%= sid %>"
                                data-programme="<%= p != null && p.getProgramme() != null ? p.getProgramme().replace("\"", "&quot;") : "" %>"
                                data-year-study="<%= p != null && p.getYearOfStudy() != null ? p.getYearOfStudy().replace("\"", "&quot;") : "" %>"
                                data-module="<%= mod %>"
                                data-module-full="<%= job != null && job.getModuleName() != null ? job.getModuleName().replace("\"", "&quot;") : "" %>"
                                data-applied="<%= app.getAppliedDate() != null ? app.getAppliedDate() : "" %>"
                                data-gpa="<%= gpa %>"
                                data-status="<%= st %>"
                                data-academic-year="<%= ay %>"
                                data-skills="<%= skillsLower.replace("&","&amp;").replace("\"","&quot;") %>"
                                data-review-note="<%= app.getReviewNote() != null ? app.getReviewNote().replace("&","&amp;").replace("\"","&quot;").replace("\n"," ") : "" %>"
                                data-search="<%= search.replace("&","&amp;").replace("\"","&quot;") %>"
                                data-has-cv="<%= hasCv ? "1" : "0" %>">
                                <td><input type="checkbox" class="row-check" name="rowSelect"></td>
                                <td>
                                    <div class="mo-ar-name-cell">
                                        <span class="mo-ar-avatar" aria-hidden="true"><%= avatarInitials %></span>
                                        <span class="mo-ar-name-text"><%= name %></span>
                                    </div>
                                </td>
                                <td class="mo-ar-td-muted"><%= sid %></td>
                                <td class="mo-ar-td-muted"><%= progDisp %></td>
                                <td class="mo-ar-td-muted"><%= yearDisp %></td>
                                <td class="mo-ar-td-muted"><%= app.getAppliedDate() != null ? app.getAppliedDate() : "—" %></td>
                                <td class="mo-ar-td-center">
                                    <% if (hasCv) { %>
                                    <a href="<%= ctx %>/cv/serve?action=applicant&amp;jobId=<%= app.getJobId() %>&amp;taUserId=<%= app.getTaUserId() %>&amp;disposition=inline" target="_blank" rel="noopener" class="mo-ar-cv-link">
                                        <svg class="mo-ar-cv-ico" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/></svg>
                                        Open CV
                                    </a>
                                    <% } else { %>
                                    <span class="mo-ar-no-cv">No CV</span>
                                    <% } %>
                                </td>
                                <td>
                                    <span class="mo-ar-status mo-ar-status--<%= badgeClass %>">
                                        <%= displayStatusLabel(st) %>
                                        <svg class="mo-ar-status-chev" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><polyline points="6 9 12 15 18 9"/></svg>
                                    </span>
                                </td>
                                <td>
                                    <button type="button" class="mo-ar-btn-primary ta-btn-view" data-action="view">View Details</button>
                                </td>
                            </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>

                <div class="mo-ar-table-footer">
                    <p class="mo-ar-footer-summary" id="showingRange">Showing 1–0 of 0</p>
                    <div class="mo-ar-footer-right">
                        <span class="mo-ar-saved-msg" id="savedFlash" hidden>Changes saved successfully!</span>
                        <button type="button" class="mo-ar-btn-save" id="saveFinalResultBtn">
                            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" aria-hidden="true"><path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/><polyline points="17 21 17 13 7 13 7 21"/><polyline points="7 3 7 8 15 8"/></svg>
                            Save Final Result
                        </button>
                        <nav class="pagination-nav mo-ar-pagination" id="paginationNav" aria-label="Pagination"></nav>
                    </div>
                </div>
            </div>
            <% } %>
        </div>
    </section>

    <div id="appReviewModal" class="ta-modal-overlay" aria-hidden="true">
        <div class="ta-modal ta-modal--minimal" role="dialog" aria-modal="true" aria-label="Application details">
            <div class="ta-modal-body ta-modal-body--flush">
                <div class="ta-modal-section">
                    <div class="ta-modal-grid" id="modalStudentInfo"></div>
                </div>
                <div class="ta-modal-section">
                    <div class="ta-modal-grid" id="modalAppDetails"></div>
                </div>
                <div class="ta-modal-section">
                    <p id="modalCvLine" class="ta-modal-cv"></p>
                </div>
                <div class="ta-modal-section">
                    <ul class="ta-timeline" id="modalTimeline"></ul>
                </div>
            </div>
        </div>
    </div>

<script>
(function () {
    var tbody = document.getElementById('applicantsTbody');
    if (!tbody) return;

    var rows = Array.prototype.slice.call(tbody.querySelectorAll('tr.app-row'));
    var searchEl = document.getElementById('applicantSearch');
    var fProgramme = document.getElementById('filterProgramme');
    var fYearStudy = document.getElementById('filterYearStudy');
    var fStatusReact = document.getElementById('filterStatusReact');
    var skillChips = document.getElementById('skillChips');
    var pageSize = 10;
    var currentPage = 1;

    function parseDate(s) {
        if (!s) return 0;
        var t = Date.parse(s);
        return isNaN(t) ? 0 : t;
    }

    function applySort() {
        var parent = tbody;
        var list = rows.slice();
        list.sort(function (a, b) {
            var bd = parseDate(b.getAttribute('data-applied'));
            var ad = parseDate(a.getAttribute('data-applied'));
            return bd - ad;
        });
        list.forEach(function (tr) { parent.appendChild(tr); });
    }

    function statusMatchesReact(st, filterVal) {
        if (!filterVal || filterVal === 'all') return true;
        var u = (st || '').toUpperCase();
        if (filterVal === 'under-review') {
            return u === 'UNDER_REVIEW' || u === 'INTERVIEWED' || u === 'SUBMITTED' || u === 'PENDING' || u === 'OFFERED';
        }
        if (filterVal === 'accepted') return u === 'ACCEPTED';
        if (filterVal === 'rejected') return u === 'REJECTED' || u === 'WITHDRAWN';
        return true;
    }

    function selectedSkills() {
        var out = [];
        if (!skillChips) return out;
        skillChips.querySelectorAll('.mo-ar-skill-chip.is-on').forEach(function (b) {
            out.push((b.getAttribute('data-skill') || '').toLowerCase());
        });
        return out;
    }

    function rowMatches(tr) {
        var q = (searchEl && searchEl.value) ? searchEl.value.trim().toLowerCase() : '';
        var ds = (tr.getAttribute('data-search') || '').replace(/&amp;/g, '&');
        if (q && ds.indexOf(q) === -1) return false;
        if (fProgramme && fProgramme.value && fProgramme.value !== 'all') {
            var pr = (tr.getAttribute('data-programme') || '').replace(/&quot;/g, '"');
            if (pr !== fProgramme.value) return false;
        }
        if (fYearStudy && fYearStudy.value && fYearStudy.value !== 'all') {
            var yr = (tr.getAttribute('data-year-study') || '').replace(/&quot;/g, '"');
            if (yr !== fYearStudy.value) return false;
        }
        if (!statusMatchesReact(tr.getAttribute('data-status'), fStatusReact ? fStatusReact.value : 'all')) return false;
        var skSel = selectedSkills();
        if (skSel.length > 0) {
            var skData = (tr.getAttribute('data-skills') || '').replace(/&amp;/g, '&');
            var ok = false;
            for (var i = 0; i < skSel.length; i++) {
                if (skData.indexOf(skSel[i]) !== -1) { ok = true; break; }
            }
            if (!ok) return false;
        }
        return true;
    }

    function updateBulkUi() {
        var n = 0;
        tbody.querySelectorAll('tr.app-row .row-check').forEach(function (cb) { if (cb.checked) n++; });
        var hdr = document.getElementById('bulkActionsHeaderBtn');
        var hc = document.getElementById('bulkHeaderCount');
        if (hc) hc.textContent = String(n);
        if (hdr) hdr.hidden = n === 0;
    }

    function applyFilters() {
        applySort();
        rows = Array.prototype.slice.call(tbody.querySelectorAll('tr.app-row'));
        var matched = [];
        rows.forEach(function (tr) {
            tr.classList.remove('filter-match', 'page-on');
            if (!rowMatches(tr)) {
                tr.style.display = 'none';
                return;
            }
            tr.classList.add('filter-match');
            matched.push(tr);
        });
        var total = matched.length;
        var pill = document.getElementById('filteredCountPill');
        if (pill) pill.textContent = String(total);
        var pages = Math.max(1, Math.ceil(total / pageSize) || 1);
        if (currentPage > pages) currentPage = pages;
        if (currentPage < 1) currentPage = 1;
        var start = (currentPage - 1) * pageSize;
        var startN = total === 0 ? 0 : start + 1;
        var endN = total === 0 ? 0 : Math.min(start + pageSize, total);
        matched.forEach(function (tr, i) {
            if (i >= start && i < start + pageSize) {
                tr.style.display = '';
                tr.classList.add('page-on');
            } else {
                tr.style.display = 'none';
            }
        });
        var sr = document.getElementById('showingRange');
        if (sr) sr.textContent = total === 0 ? 'Showing 0 of 0 applicants' : ('Showing ' + startN + '–' + endN + ' of ' + total + ' applicants');
        renderPagination(total, pages);
    }

    function renderPagination(total, pages) {
        var nav = document.getElementById('paginationNav');
        if (!nav) return;
        var html = '';
        html += '<button type="button" class="page-btn mo-ar-page-btn" id="pagePrev"' + (currentPage <= 1 ? ' disabled' : '') + '>Previous</button>';
        for (var p = 1; p <= pages; p++) {
            html += '<button type="button" class="page-num mo-ar-page-num' + (p === currentPage ? ' active' : '') + '" data-page="' + p + '">' + p + '</button>';
        }
        html += '<button type="button" class="page-btn mo-ar-page-btn" id="pageNext"' + (currentPage >= pages ? ' disabled' : '') + '>Next</button>';
        nav.innerHTML = html;
        var prev = document.getElementById('pagePrev');
        var next = document.getElementById('pageNext');
        if (prev) prev.onclick = function () { if (currentPage > 1) { currentPage--; applyFilters(); } };
        if (next) next.onclick = function () { if (currentPage < pages) { currentPage++; applyFilters(); } };
        nav.querySelectorAll('.page-num').forEach(function (btn) {
            btn.onclick = function () {
                currentPage = parseInt(btn.getAttribute('data-page'), 10);
                applyFilters();
            };
        });
    }

    if (searchEl) searchEl.addEventListener('input', function () { currentPage = 1; applyFilters(); });
    if (fProgramme) fProgramme.addEventListener('change', function () { currentPage = 1; applyFilters(); });
    if (fYearStudy) fYearStudy.addEventListener('change', function () { currentPage = 1; applyFilters(); });
    if (fStatusReact) fStatusReact.addEventListener('change', function () { currentPage = 1; applyFilters(); });
    if (skillChips) skillChips.addEventListener('click', function (e) {
        var t = e.target.closest('.mo-ar-skill-chip');
        if (!t) return;
        t.classList.toggle('is-on');
        currentPage = 1;
        applyFilters();
    });

    var clearBtn = document.getElementById('clearAllFilters');
    if (clearBtn) clearBtn.addEventListener('click', function () {
        if (searchEl) searchEl.value = '';
        if (fProgramme) fProgramme.value = 'all';
        if (fYearStudy) fYearStudy.value = 'all';
        if (fStatusReact) fStatusReact.value = 'all';
        if (skillChips) skillChips.querySelectorAll('.mo-ar-skill-chip').forEach(function (b) { b.classList.remove('is-on'); });
        currentPage = 1;
        applyFilters();
    });

    var selectAll = document.getElementById('selectAll');
    if (selectAll) selectAll.addEventListener('change', function () {
        tbody.querySelectorAll('tr.app-row.page-on .row-check').forEach(function (cb) { cb.checked = selectAll.checked; });
        updateBulkUi();
    });
    var selectAllBulk = document.getElementById('selectAllBulk');
    if (selectAllBulk) selectAllBulk.addEventListener('change', function () {
        tbody.querySelectorAll('tr.app-row.page-on .row-check').forEach(function (cb) { cb.checked = selectAllBulk.checked; });
        updateBulkUi();
    });
    tbody.addEventListener('change', function (e) {
        if (e.target && e.target.classList && e.target.classList.contains('row-check')) updateBulkUi();
    });

    var exportListBtn = document.getElementById('exportListBtn');
    if (exportListBtn) exportListBtn.addEventListener('click', function () {
        var lines = ['Name,Student ID,Programme,Year,Applied,Status'];
        rows.forEach(function (tr) {
            if (!tr.classList.contains('filter-match')) return;
            var name = (tr.getAttribute('data-name') || '').replace(/"/g, '""');
            lines.push('"' + name + '","' + (tr.getAttribute('data-student-id') || '') + '","' + (tr.getAttribute('data-programme') || '') + '","' + (tr.getAttribute('data-year-study') || '') + '","' + (tr.getAttribute('data-applied') || '') + '","' + (tr.getAttribute('data-status') || '') + '"');
        });
        var blob = new Blob([lines.join('\n')], { type: 'text/csv;charset=utf-8' });
        var a = document.createElement('a');
        a.href = URL.createObjectURL(blob);
        a.download = 'applicant-list.csv';
        a.click();
        URL.revokeObjectURL(a.href);
    });

    var saveFlash = document.getElementById('savedFlash');
    var saveBtn = document.getElementById('saveFinalResultBtn');
    if (saveBtn && saveFlash) saveBtn.addEventListener('click', function () {
        saveFlash.hidden = false;
        setTimeout(function () { saveFlash.hidden = true; }, 2500);
    });

    document.getElementById('applyBulkStatus').addEventListener('click', function () {
        var st = document.getElementById('bulkStatus').value;
        if (!st) return;
        var parts = [];
        tbody.querySelectorAll('tr.app-row').forEach(function (tr) {
            var cb = tr.querySelector('.row-check');
            if (cb && cb.checked) parts.push(tr.getAttribute('data-application-id') + ',' + st);
        });
        if (parts.length === 0) return;
        document.getElementById('batchPayload').value = parts.join(';');
        document.getElementById('batchForm').submit();
    });

    document.getElementById('exportSelectedBtn').addEventListener('click', function () {
        var lines = ['Student,ID,Module,Applied,Status,GPA'];
        tbody.querySelectorAll('tr.app-row').forEach(function (tr) {
            var cb = tr.querySelector('.row-check');
            if (!cb || !cb.checked) return;
            var name = (tr.getAttribute('data-name') || '').replace(/"/g, '""');
            var email = (tr.getAttribute('data-email') || '');
            lines.push('"' + name + '","' + (tr.getAttribute('data-student-id') || '') + '","' + (tr.getAttribute('data-module') || '') + '","' + (tr.getAttribute('data-applied') || '') + '","' + (tr.getAttribute('data-status') || '') + '","' + (tr.getAttribute('data-gpa') || '') + '"');
        });
        var blob = new Blob([lines.join('\n')], { type: 'text/csv;charset=utf-8' });
        var a = document.createElement('a');
        a.href = URL.createObjectURL(blob);
        a.download = 'selected-applicants.csv';
        a.click();
        URL.revokeObjectURL(a.href);
    });

    var modal = document.getElementById('appReviewModal');
    function statusLabelUi(st) {
        if (!st) return '-';
        var u = st.toUpperCase();
        var m = { PENDING: 'Pending', SUBMITTED: 'Submitted', UNDER_REVIEW: 'Under Review', INTERVIEWED: 'Interviewed',
            OFFERED: 'Offered', ACCEPTED: 'Accepted', REJECTED: 'Rejected', WITHDRAWN: 'Withdrawn' };
        return m[u] || st;
    }

    function openModal(tr) {
        if (!modal || !tr) return;
        document.getElementById('modalTitle').textContent = 'Application Review - ' + (tr.getAttribute('data-name') || '');

        var si = document.getElementById('modalStudentInfo');
        si.innerHTML = '<div><span class="k">Name</span><span class="v">' + esc(tr.getAttribute('data-name')) + '</span></div>' +
            '<div><span class="k">Student ID</span><span class="v">' + esc(tr.getAttribute('data-student-id')) + '</span></div>' +
            '<div><span class="k">Email</span><span class="v">' + esc(tr.getAttribute('data-email')) + '</span></div>' +
            '<div><span class="k">Phone</span><span class="v">' + esc(tr.getAttribute('data-phone')) + '</span></div>' +
            '<div><span class="k">Programme</span><span class="v">' + esc(tr.getAttribute('data-programme')) + '</span></div>';

        var ad = document.getElementById('modalAppDetails');
        ad.innerHTML = '<div><span class="k">Module</span><span class="v">' + esc(tr.getAttribute('data-module-full')) + '</span></div>' +
            '<div><span class="k">Applied</span><span class="v">' + esc(tr.getAttribute('data-applied')) + '</span></div>' +
            '<div><span class="k">Status</span><span class="v">' + esc(statusLabelUi(tr.getAttribute('data-status'))) + '</span></div>';

        var hasCv = tr.getAttribute('data-has-cv') === '1';
        var jobId = tr.getAttribute('data-job-id');
        var taId = tr.getAttribute('data-ta-id');
        var ctx = '<%= ctx %>';
        document.getElementById('modalCvLine').innerHTML = hasCv
            ? '<a href="' + ctx + '/cv/serve?action=applicant&jobId=' + encodeURIComponent(jobId) + '&taUserId=' + encodeURIComponent(taId) + '&disposition=attachment" class="btn btn-secondary btn-sm">Download CV</a> '
            + '<a href="' + ctx + '/cv/serve?action=applicant&jobId=' + encodeURIComponent(jobId) + '&taUserId=' + encodeURIComponent(taId) + '&disposition=inline" target="_blank" rel="noopener" class="btn btn-ghost btn-sm">Preview</a>'
            : '<span class="text-muted">No CV uploaded</span>';

        var tl = document.getElementById('modalTimeline');
        tl.innerHTML = '<li>' + esc(tr.getAttribute('data-applied')) + ' - Application submitted</li>';
        modal.classList.add('is-open');
        modal.setAttribute('aria-hidden', 'false');
    }
    function esc(s) {
        if (!s) return '-';
        var d = document.createElement('div');
        d.textContent = s;
        return d.innerHTML;
    }
    function closeModal() {
        modal.classList.remove('is-open');
        modal.setAttribute('aria-hidden', 'true');
    }
    modal.addEventListener('click', function (e) { if (e.target === modal) closeModal(); });
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && modal && modal.classList.contains('is-open')) closeModal();
    });

    tbody.querySelectorAll('.ta-btn-view').forEach(function (btn) {
        btn.addEventListener('click', function () {
            openModal(btn.closest('tr'));
        });
    });

    applyFilters();
    updateBulkUi();
})();
</script>

<%@ include file="/jsp/common/footer.jsp" %>
</body>
</html>
