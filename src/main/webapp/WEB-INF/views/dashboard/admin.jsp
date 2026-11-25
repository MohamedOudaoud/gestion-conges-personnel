<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Espace administrateur - GestConges</title>
</head>
<body>
    <main>
        <h1>Espace administrateur</h1>
        <p>Bienvenue, <c:out value="${sessionScope.employePrenom}"/> <c:out value="${sessionScope.employeNom}"/>.</p>
        <p>Le tableau de bord est en cours de développement.</p>
        <p>
            <a href="${pageContext.request.contextPath}/admin/employees">
                Gérer les employés
            </a>
        </p>
        <p>
            <a href="${pageContext.request.contextPath}/admin/departments">
                Gérer les départements
            </a>
        </p>
        <form action="${pageContext.request.contextPath}/logout" method="post">
            <button type="submit">Se déconnecter</button>
        </form>
    </main>
</body>
</html>
