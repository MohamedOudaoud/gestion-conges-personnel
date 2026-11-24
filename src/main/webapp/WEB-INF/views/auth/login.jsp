<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Connexion - GestConges</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
          rel="stylesheet">
</head>

<body class="bg-light">

<div class="container d-flex justify-content-center align-items-center min-vh-100">

    <div class="card shadow-sm p-4" style="width: 100%; max-width: 420px;">

        <h2 class="text-center mb-2">GestConges</h2>
        <p class="text-center text-muted mb-4">
            Gestion des congés du personnel
        </p>

        <% if ("success".equals(request.getParameter("logout"))) { %>
            <div class="alert alert-success">
                Vous avez été déconnecté avec succès.
            </div>
        <% } %>

        <% if (request.getAttribute("error") != null) { %>
            <div class="alert alert-danger">
                <%= request.getAttribute("error") %>
            </div>
        <% } %>

        <form action="${pageContext.request.contextPath}/login"
              method="POST">

            <div class="mb-3">
                <label for="email" class="form-label">Adresse email</label>
                <input type="email"
                       class="form-control"
                       id="email"
                       name="email"
                       required>
            </div>

            <div class="mb-3">
                <label for="password" class="form-label">Mot de passe</label>
                <input type="password"
                       class="form-control"
                       id="password"
                       name="password"
                       required>
            </div>

            <button type="submit" class="btn btn-primary w-100">
                Se connecter
            </button>

        </form>

    </div>

</div>

</body>
</html>
