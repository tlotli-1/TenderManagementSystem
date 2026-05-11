<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ProcureGov – Supplier Registration</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        body { background: linear-gradient(135deg,#f5f5dc 0%,#e6d7c3 100%); min-height:100vh; display:flex; align-items:center; justify-content:center; padding:30px 15px; }
        .reg-card { background:#fff; border:2px solid #000; border-radius:16px; width:100%; max-width:680px; overflow:hidden; box-shadow:0 20px 60px rgba(0,0,0,.15); }
        .reg-header { background:#000; color:#f5f5dc; padding:28px 36px; }
        .reg-header h1 { font-size:1.6rem; font-weight:700; letter-spacing:2px; }
        .reg-header p  { font-size:.85rem; opacity:.7; margin-top:4px; }
        .reg-body { padding:32px 36px; }
        fieldset { border:1px solid #ddd; border-radius:8px; padding:20px 22px; margin-bottom:20px; }
        legend   { font-weight:600; font-size:.9rem; padding:0 8px; color:#000; }
        .form-row { display:grid; grid-template-columns:1fr 1fr; gap:14px; }
        .form-group { display:flex; flex-direction:column; margin-bottom:14px; }
        .form-group:last-child { margin-bottom:0; }
        .form-group label { font-size:.82rem; font-weight:600; color:#333; margin-bottom:5px; }
        .form-group input, .form-group textarea {
            padding:10px 12px; border:1.5px solid #ccc; border-radius:7px;
            font-size:.9rem; transition:border-color .2s;
        }
        .form-group input:focus, .form-group textarea:focus { outline:none; border-color:#000; }
        .form-group small { font-size:.75rem; color:#888; margin-top:4px; }
        .form-group textarea { resize:vertical; min-height:80px; }
        .req { color:#c0392b; }
        .btn-register { width:100%; background:#000; color:#f5f5dc; border:none; padding:13px; border-radius:8px; font-size:1rem; font-weight:600; cursor:pointer; letter-spacing:1px; transition:background .2s; }
        .btn-register:hover { background:#222; }
        .login-link { text-align:center; margin-top:16px; font-size:.85rem; color:#666; }
        .login-link a { color:#000; font-weight:600; }
        .error-message { background:#fdf0f0; border:1px solid #e74c3c; border-radius:8px; padding:12px 16px; margin-bottom:20px; color:#c0392b; font-size:.88rem; line-height:1.7; }
        .success-box { background:#f0fff4; border:2px solid #27ae60; border-radius:10px; padding:24px; text-align:center; }
        .success-box .tick { font-size:3rem; margin-bottom:10px; }
        .success-box h3 { color:#27ae60; margin-bottom:8px; }
        .reg-num { font-size:1.4rem; font-weight:700; color:#000; background:#f5f5dc; display:inline-block; padding:8px 20px; border-radius:6px; margin:10px 0; letter-spacing:1px; }
        .success-box p { color:#555; font-size:.9rem; margin:6px 0; }
        .btn-login { display:inline-block; margin-top:16px; background:#000; color:#f5f5dc; padding:10px 28px; border-radius:7px; text-decoration:none; font-weight:600; }
        /* password strength bar */
        #pwStrengthBar { height:4px; border-radius:2px; margin-top:5px; transition:all .3s; width:0; background:#ccc; }
        #pwStrengthLabel { font-size:.73rem; margin-top:2px; }
    </style>
</head>
<body>
<div class="reg-card">
    <div class="reg-header">
        <h1>🏛 ProcureGov</h1>
        <p>Supplier Registration — Ministry of Public Works, Kingdom of Lesotho</p>
    </div>
    <div class="reg-body">

        <c:choose>
            <%-- ── Success state ── --%>
            <c:when test="${not empty registrationNumber}">
                <div class="success-box">
                    <div class="tick">✅</div>
                    <h3>Registration Successful!</h3>
                    <p>Your supplier account has been created. Your registration number is:</p>
                    <div class="reg-num">${registrationNumber}</div>
                    <p>Keep this number safe — it identifies your company in ProcureGov.</p>
                    <p>You can now log in and start browsing open tenders.</p>
                    <a href="${pageContext.request.contextPath}/login" class="btn-login">Proceed to Login →</a>
                </div>
            </c:when>

            <%-- ── Registration form ── --%>
            <c:otherwise>
                <c:if test="${not empty error}">
                    <div class="error-message">${error}</div>
                </c:if>

                <form method="post" action="${pageContext.request.contextPath}/register" id="regForm" novalidate>

                    <fieldset>
                        <legend>Account Credentials</legend>
                        <div class="form-row">
                            <div class="form-group">
                                <label>Username <span class="req">*</span></label>
                                <input type="text" name="username" value="${form.username}"
                                       minlength="3" maxlength="50" required
                                       placeholder="e.g. acme_supplier">
                                <small>3–50 chars, letters/numbers/._-</small>
                            </div>
                            <div class="form-group">
                                <label>Email Address <span class="req">*</span></label>
                                <input type="email" name="email" value="${form.email}"
                                       maxlength="100" required
                                       placeholder="you@company.co.ls">
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label>Password <span class="req">*</span></label>
                                <input type="password" name="password" id="pwField"
                                       minlength="8" required
                                       placeholder="Min 8 chars, 1 uppercase, 1 number"
                                       oninput="checkStrength(this.value)">
                                <div id="pwStrengthBar"></div>
                                <small id="pwStrengthLabel">At least 8 characters, one uppercase, one number</small>
                            </div>
                            <div class="form-group">
                                <label>Confirm Password <span class="req">*</span></label>
                                <input type="password" name="confirmPassword"
                                       minlength="8" required
                                       placeholder="Re-enter password"
                                       oninput="checkMatch(this.value)">
                                <small id="matchLabel"> </small>
                            </div>
                        </div>
                    </fieldset>

                    <fieldset>
                        <legend>Company Information</legend>
                        <div class="form-row">
                            <div class="form-group">
                                <label>Contact Person (Full Name) <span class="req">*</span></label>
                                <input type="text" name="fullName" value="${form.fullName}"
                                       minlength="2" maxlength="100" required
                                       placeholder="e.g. Thabo Mokoena">
                            </div>
                            <div class="form-group">
                                <label>Company Name <span class="req">*</span></label>
                                <input type="text" name="companyName" value="${form.companyName}"
                                       minlength="2" maxlength="150" required
                                       placeholder="e.g. ACME Construction (Pty) Ltd">
                            </div>
                        </div>
                        <div class="form-row">
                            <div class="form-group">
                                <label>Contact Number <span class="req">*</span></label>
                                <input type="tel" name="contactNumber" value="${form.contactNumber}"
                                       required placeholder="+26622123456">
                                <small>8–15 digits, optional + prefix</small>
                            </div>
                        </div>
                        <div class="form-group">
                            <label>Physical Address <span class="req">*</span></label>
                            <textarea name="physicalAddress" maxlength="300" required
                                      placeholder="Street, City, District">${form.physicalAddress}</textarea>
                        </div>
                    </fieldset>

                    <button type="submit" class="btn-register">Create Supplier Account</button>
                </form>

                <div class="login-link">
                    Already registered? <a href="${pageContext.request.contextPath}/login">Sign in here</a>
                </div>
            </c:otherwise>
        </c:choose>

    </div>
</div>

<script>
    function checkStrength(pw) {
        const bar   = document.getElementById('pwStrengthBar');
        const label = document.getElementById('pwStrengthLabel');
        let score = 0;
        if (pw.length >= 8)              score++;
        if (/[A-Z]/.test(pw))            score++;
        if (/[0-9]/.test(pw))            score++;
        if (/[^a-zA-Z0-9]/.test(pw))    score++;
        const colours = ['#e74c3c','#e67e22','#f1c40f','#27ae60'];
        const labels  = ['Too weak','Weak','Good','Strong'];
        const pct     = (score / 4) * 100;
        bar.style.width      = pct + '%';
        bar.style.background = colours[score - 1] || '#ccc';
        label.textContent    = score > 0 ? labels[score - 1] : 'At least 8 chars, one uppercase, one number';
        label.style.color    = colours[score - 1] || '#888';
    }

    function checkMatch(confirm) {
        const pw    = document.getElementById('pwField').value;
        const label = document.getElementById('matchLabel');
        if (confirm === '') { label.textContent = ' '; return; }
        if (pw === confirm) {
            label.textContent = '✓ Passwords match';
            label.style.color = '#27ae60';
        } else {
            label.textContent = '✗ Passwords do not match';
            label.style.color = '#e74c3c';
        }
    }
</script>
</body>
</html>
