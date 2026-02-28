<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Something went wrong — ShopNow</title>
  <link rel="stylesheet" href="css/theme.css">
  <style>
    .error-container {
      max-width: 520px;
      margin: 80px auto;
      text-align: center;
      padding: 40px 24px;
    }
    .error-code { font-size: 5rem; font-weight: 800; color: var(--accent); margin: 0; }
    .error-title { font-size: 1.4rem; font-weight: 600; margin: 12px 0 8px; }
    .error-msg { color: var(--text-muted); margin-bottom: 28px; }
    .btn-home { display: inline-block; padding: 10px 28px; background: var(--accent);
                color: #fff; border-radius: var(--radius-sm); text-decoration: none;
                font-weight: 600; }
  </style>
</head>
<body>
<script>(function(){var t=localStorage.getItem('theme')||'light';document.documentElement.setAttribute('data-theme',t);})();</script>
<div class="error-container">
  <p class="error-code">⚠️</p>
  <h1 class="error-title">Something went wrong</h1>
  <p class="error-msg">
    We hit an unexpected error. This is usually a temporary issue.<br>
    Please try again in a moment.
  </p>
  <a href="index.jsp" class="btn-home">← Back to Store</a>
</div>
</body>
</html>
